package it.gmarseglia.weka.controller;

import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.entity.Experiment;
import it.gmarseglia.weka.entity.ExperimentResult;
import it.gmarseglia.weka.entity.ExperimentSuite;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.WekaException;

public class ExperimentsController {

    private static final MyLogger logger = MyLogger.getInstance(ExperimentsController.class);
    private static ExperimentsController instance = null;

    private ExperimentsController() {
    }

    public static ExperimentsController getInstance() {
        if (instance == null) {
            instance = new ExperimentsController();
        }
        return instance;
    }

    public void processExperimentSuite(ExperimentSuite suite) throws Exception {
        try {
            // get the last attribute as the label
            Attribute label = suite.getTrainingSet().attribute(suite.getTrainingSet().numAttributes() - 1);

            // compute the percentage of "non buggy" classes over the total
            AttributeStats labelStats = suite.getTrainingSet().attributeStats(label.index());
            int nonBuggyCount = labelStats.nominalCounts[1];
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


                // logger.logFine("Correct percentage: " + eval.pctCorrect());
                // logger.logFine("Precision: " + eval.precision(1));
                // logger.logFine("Recall: " + eval.recall(1));

                ExperimentResult result = new ExperimentResult(
                        experiment.getClassifierName(),
                        suite.getProjName() + "-" + suite.getVersionaName(),
                        correctPercentage, recall, precision, distribution);

                experiment.setResult(result);

            }
        } catch (WekaException e) {
            logger.logFine(e.getMessage());
        }
    }

}
