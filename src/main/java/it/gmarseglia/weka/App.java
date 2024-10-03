package it.gmarseglia.weka;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.controller.ExperimentsController;
import it.gmarseglia.weka.entity.Experiment;
import it.gmarseglia.weka.entity.ExperimentResult;
import it.gmarseglia.weka.entity.ExperimentSuite;
import it.gmarseglia.weka.util.Configs;
import it.gmarseglia.weka.util.CsvSequentialReader;
import it.gmarseglia.weka.util.CsvToArffConverter;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final MyLogger logger = MyLogger.getInstance(ExperimentsController.class);

    public static void main(String[] args) throws Exception {
        List<String> projects = Configs.getProjects();
        projects = List.of("SYNCOPE", "BOOKKEEPER", "OPENJPA");
        // projects = List.of("OPENJPA");
        CsvToArffConverter.convertProjects(projects);

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

        List<ExperimentResult> allResults = new ArrayList<>();

        int versionCounter = 0;

        // For each version
        for (String version : halfVersions) {
            logger.logFine("Project-Version: " + projName + "-" + version);

            // Get the associated training set file and path
            String trainingFilename = version + Configs.TRAINING_SUFFIX + Configs.ARFF_EXT;
            logger.logFinest("training: " + trainingFilename);
            Path trainingPath = Configs.getProjWekaOutDir(projName).resolve(trainingFilename);
            Instances training;
            try {
                training = ConverterUtils.DataSource.read(trainingPath.toString());
                if (training == null || training.numInstances() < 2) continue;
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
                testing = ConverterUtils.DataSource.read(testingPath.toString());
                if (testing == null) continue;
            } catch (Exception e) {
                logger.logFinest("File not found: " + testingFilename);
                continue;
            }

            // Delete the column with name
            // training.deleteAttributeAt(1);
            // testing.deleteAttributeAt(1);

            // Set the last attribute as the one to be predicted
            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            // Create the experiment suite
            ExperimentSuite suite = new ExperimentSuite(projName, String.format("(%02d)-%s", versionCounter++, version), training, testing);

            suite.add(new Experiment("ZeroR", new ZeroR()));
            suite.add(new Experiment("Random Forest", new RandomForest()));
            suite.add(new Experiment("Naive Bayes", new NaiveBayes()));
            suite.add(new Experiment("IBk", new IBk()));

            ExperimentsController ec = ExperimentsController.getInstance();

            ec.processExperimentSuite(suite);

            for (Experiment experiment : suite.getExperiments()) {
                logger.logFine(experiment.getResult().toString());
                allResults.add(experiment.getResult());
            }
        }

        ToFileBoundary.writeList(allResults, Configs.getProjWekaOutDir("EXPERIMENTS"), projName + "-RESULTS.csv");

    }


}
