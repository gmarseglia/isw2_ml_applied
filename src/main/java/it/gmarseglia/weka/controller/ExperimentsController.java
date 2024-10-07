package it.gmarseglia.weka.controller;

import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.entity.Experiment;
import it.gmarseglia.weka.entity.ExperimentPlan;
import it.gmarseglia.weka.entity.ExperimentResult;
import it.gmarseglia.weka.entity.ExperimentSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.WekaException;

import java.util.HashMap;
import java.util.Map;

public class ExperimentsController {

    private static final Map<String, ExperimentsController> instances = new HashMap<>();
    private static final MyLogger logger = MyLogger.getInstance(ExperimentsController.class);
    private static final Logger log = LoggerFactory.getLogger(ExperimentsController.class);
    private final String projName;


    private ExperimentsController(String projName) {
        this.projName = projName;
    }

    public static ExperimentsController getInstance(String projName) {
        ExperimentsController.instances.computeIfAbsent(projName, String -> new ExperimentsController(projName));
        return ExperimentsController.instances.get(projName);
    }

    public void processExperimentPlan(ExperimentPlan plan) {

        while (!plan.getSuites().isEmpty()) {
            ExperimentSuite suite = plan.getSuites().getFirst();
            logger.logFine("Processing suite: " + String.format("%s-%d_(%s)-%s", projName, suite.getVersionCounter(), suite.getVersion(), suite.getName()));
            processExperimentSuite(suite);
            plan.addResultsFromSuite(suite);
            plan.getSuites().removeFirst();
        }
        
    }


    public void processExperimentSuite(ExperimentSuite suite) {
        try {
            // get the last attribute as the label
            Attribute label = suite.getTrainingSet().attribute(suite.getTrainingSet().numAttributes() - 1);

            // compute the percentage of "buggy" classes over the total
            AttributeStats labelStats = suite.getTrainingSet().attributeStats(label.index());
            int nonBuggyCount = labelStats.nominalCounts[0];
            double distribution = (nonBuggyCount * 100D / labelStats.totalCount);

            for (Experiment experiment : suite.getExperiments()) {
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

            }
        } catch (WekaException e) {
            logger.logFine(e.getMessage());
        } catch (Exception e) {
            logger.logFine("Generic Exception: " + e.getMessage());
        }
    }

}
