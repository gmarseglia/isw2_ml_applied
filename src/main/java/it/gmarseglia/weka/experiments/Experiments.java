package it.gmarseglia.weka.experiments;

import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.entity.ExperimentResult;
import it.gmarseglia.weka.util.Configs;
import it.gmarseglia.weka.util.CsvSequentialReader;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.WekaException;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Experiments {

    private static final MyLogger logger = MyLogger.getInstance(Experiments.class);

    public static void main(String[] args) throws Exception {
        List<String> projects = Configs.getProjects();
        projects = List.of("OPENJPA");


        logger.setVerbose(true);
        logger.setVerboseFine(true);

        for (String projName : projects) {
            run(projName);
        }

    }

    private static void run(String projName) throws Exception {
        // Get the list of all the half versions
        Path halfVersionsCsv = Configs.getProjOutDir(projName).resolve("halfValidVersions.csv");
        List<String> halfVersions = CsvSequentialReader.getFirstColumnEntries(halfVersionsCsv);

        // For each version
        for (String version : halfVersions) {
            logger.logFine("Project-Version: " + projName + "-" + version);

            // Get the associated training set file and path
            String trainingFilename = version + Configs.TRAINING_SUFFIX + Configs.ARFF_EXT;
            logger.logFinest("training: " + trainingFilename);
            Path trainingPath = Configs.getProjWekaOutDir(projName).resolve(trainingFilename);
            Instances training;
            try {
                training = DataSource.read(trainingPath.toString());
                if (training == null) continue;
            } catch (Exception e) {
                logger.logFinest("File not found: " + trainingFilename);
                continue;
            }

            // Get the associated testing set file and path
            String testingFilename = version + Configs.TESTING_SUFFIX + Configs.ARFF_EXT;
            logger.logFinest("testing: " + testingFilename);
            Path testingPath = Configs.getProjWekaOutDir(projName).resolve(testingFilename);
            Instances testing;
            try {
                testing = DataSource.read(testingPath.toString());
                if (testing == null) continue;
            } catch (Exception e) {
                logger.logFinest("File not found: " + testingFilename);
                continue;
            }

            // Delete the column with name
            training.deleteAttributeAt(1);
            testing.deleteAttributeAt(1);

            // Set the last attribute as the one to be predicted
            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            // Choose the classifier
            String classifierName = "RandomForest";
            Classifier classifier = new RandomForest();


            // Classifier classifier = new ZeroR();
            // Classifier classifier = new IBk();
            // Classifier classifier = new NaiveBayes();


            // Create a list of experiment results
            List<ExperimentResult> results = new ArrayList<>();

            try {
                // Build and train the classifier
                classifier.buildClassifier(training);

                // Evaluate the classifier against the testing set, without sampling
                Evaluation eval = new Evaluation(testing);
                eval.evaluateModel(classifier, testing);

                double correctPercentage = eval.pctCorrect();
                double recall = eval.recall(1);
                double precision = eval.precision(1);

                logger.logFine("Correct percentage: " + eval.pctCorrect());
                logger.logFine("Precision: " + eval.precision(1));
                logger.logFine("Recall: " + eval.recall(1));

                results.add(new ExperimentResult(classifierName, projName + "-" + version, correctPercentage, recall, precision));

            } catch (WekaException e) {
                logger.logFine(e.getMessage());
            }

            logger.log("");
        }
    }
}
