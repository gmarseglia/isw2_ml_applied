package it.gmarseglia.weka.controller;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.entity.*;
import it.gmarseglia.weka.util.Configs;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.*;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExperimentsController {

    private static final Map<String, ExperimentsController> instances = new HashMap<>();
    private static final MyLogger logger = MyLogger.getInstance(ExperimentsController.class);
    private final String projName;


    private ExperimentsController(String projName) {
        this.projName = projName;
    }

    public static ExperimentsController getInstance(String projName) {
        ExperimentsController.instances.computeIfAbsent(projName, s -> new ExperimentsController(projName));
        return ExperimentsController.instances.get(projName);
    }

    public void processExperimentPlan(ExperimentPlan plan) {
        while (!plan.getSuites().isEmpty()) {
            ExperimentSuite suite = plan.getSuites().getFirst();
            logger.logFine("Processing suite: " + String.format("%s-%d_v%s-%s", projName, suite.getVersionCounter(), suite.getVersion(), suite.getName()));
            processExperimentSuite(suite);
            plan.addResultsFromSuite(suite);
            plan.getSuites().removeFirst();
        }
    }

    public void processExperimentSuite(ExperimentSuite suite) {
        // get the last attribute as the label
        Attribute label = suite.getTrainingSet().attribute(suite.getTrainingSet().numAttributes() - 1);

        // compute the percentage of "buggy" classes over the total
        AttributeStats labelStats = suite.getTrainingSet().attributeStats(label.index());
        int buggyCount = labelStats.nominalCounts[0];
        double distribution = (buggyCount * 100D / labelStats.totalCount);

        for (Experiment experiment : suite.getExperiments()) {
            try {
                logger.logFine("\tExperiment: " + experiment.getClassifierName() + "-" + experiment.getClassifierModifier());

                // Build and train the classifier
                Classifier classifier = experiment.getClassifier();
                classifier.buildClassifier(suite.getTrainingSet());

                // Evaluate the classifier against the testing set, without sampling
                Evaluation eval = new Evaluation(suite.getTestingSet());
                eval.evaluateModel(classifier, suite.getTestingSet());

                double correctPercentage = eval.pctCorrect();
                double recall = eval.recall(0);
                double precision = eval.precision(0);
                double auc = eval.areaUnderROC(0);
                double kappa = eval.kappa();

                ExperimentResult result = new ExperimentResult(
                        correctPercentage, recall, precision, auc, kappa, distribution
                );

                experiment.setResult(result);

                exportExperimentForAcume(suite, experiment);

            } catch (WekaException e) {
                logger.log(String.format("\t ! Weka exception during experiment %s: %s", experiment.getClassifierName(), e));
            } catch (Exception e) {
                logger.log(String.format("\t ! Generic exception during experiment %s: %s", experiment.getClassifierName(), e));
            }
        }
    }

    private void exportExperimentForAcume(ExperimentSuite suite, Experiment experiment) {
        DecimalFormat df = new DecimalFormat("00");
        String extendedExperimentName = String.format("%s-%s_v%s-%s-%s-%s",
                projName,
                df.format(suite.getVersionCounter()), suite.getVersion().replace(".", "+"),
                suite.getName(),
                experiment.getClassifierName(),
                experiment.getClassifierModifier());

        logger.logFinest("\tACUME for: " + extendedExperimentName);

        Instances testing = suite.getTestingSet();
        Instances unfilteredTesting = suite.getUnfilteredTestingSet();
        try {
            for (int i = 0; i < testing.numInstances(); i++) {
                Instance instance = testing.instance(i);
                Instance unfilteredInstance = unfilteredTesting.instance(i);
                String id = unfilteredInstance.stringValue(1);
                int size = (int) unfilteredInstance.value(2);
                String actual = Objects.equals(unfilteredInstance.stringValue(unfilteredInstance.classIndex()), "true") ? "YES" : "NO";

                double[] distribution = experiment.getClassifier().distributionForInstance(instance);
                double predicted = distribution[0];

                logger.logFinest(String.format("ID: %s, size: %d, predicted: %.3f, actual: %s",
                        id.substring(id.length() - 8), size, predicted, actual));

                experiment.addPrediction(new Prediction(id, size, predicted, actual));
            }

            ToFileBoundary.writeList(experiment.getPredictions(), Configs.getProjWekaOutDir("ACUME").resolve(projName), extendedExperimentName + ".csv");
        } catch (Exception e) {
            logger.log("Generic exception during Acume: " + e + ": " + e.getMessage());
        }
    }

}
