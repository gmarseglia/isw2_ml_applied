package it.gmarseglia.weka.controller;

import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.entity.Experiment;
import it.gmarseglia.weka.entity.ExperimentPlan;
import it.gmarseglia.weka.entity.ExperimentSuite;
import it.gmarseglia.weka.util.Configs;
import it.gmarseglia.weka.util.CsvSequentialReader;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentsPlanner {

    private static final Map<String, ExperimentsPlanner> instances = new HashMap<>();
    private static final MyLogger logger = MyLogger.getInstance(ExperimentsPlanner.class);
    private final String projName;

    private String actualVersion;
    private int versionCounter;
    private ExperimentPlan plan;
    private DataSource trainingSource;
    private Instances trainingSet;
    private DataSource testingSource;
    private Instances testingSet;


    private ExperimentsPlanner(String projName) {
        this.projName = projName;
        this.versionCounter = 0;
    }

    public static ExperimentsPlanner getInstance(String projName) {
        ExperimentsPlanner.instances.computeIfAbsent(projName, String -> new ExperimentsPlanner(projName));
        return ExperimentsPlanner.instances.get(projName);

    }

    /*
     * In order to plan the experiments:
     * 1. Get the half valid version
     * 2. For each version:
     *   2.1 Get the PATH for the source for training and testing set
     *   2.2 Open the DATASOURCE for training set and testing set
     *   2.3 Create the NAIVE ExperimentSuite:               (2 total)
     *       2.3.a ZeroR
     *       2.3.b UnoR
     *   2.4 Create the SIMPLE ExperimentSuite:              (3 sub, 5 total)
     *       2.4.a RandomForest
     *       2.4.b Naive Bayes
     *       2.4.c IBk
     *   2.5 Create the BALANCING ExperimentSuite:           (3*3=9 sub, 16 total)
     *       2.5.a SIMPLE × over sampling
     *       2.5.b SIMPLE × under sampling
     *       2.5.c SIMPLE × SMOTE
     *   2.6 Create the FEATURE SELECTION ExperimentSuite:   (3*1=3 sub, 19 total)
     *       2.6.a SIMPLE × feature selection
     *   2.7 Create the SENSITIVITY ExperimentSuite:        (3*2=6 sub, 25 total)
     *       2.7.a SIMPLE × Sensitive Threshold
     *       2.7.b SIMPLE × Sensitive Learning
     *  */
    public ExperimentPlan plan() throws IOException {
        // Get the list of all the half versions
        Path halfVersionsCsv = Configs.getProjOutDir(projName).resolve("halfValidVersions.csv");
        List<String> halfVersions = CsvSequentialReader.getFirstColumnEntries(halfVersionsCsv);

        plan = new ExperimentPlan(projName);

        for (String versionName : halfVersions) {
            actualVersion = versionName;
            versionCounter++;

            String extendedVersionName = String.format("%s-%d_(%s)", projName, versionCounter, actualVersion);
            logger.logFine("Creating suite for: " + extendedVersionName);

            planVersion();
        }

        return plan;
    }

    private void planVersion() {
        // Get the associated training set file and path
        String trainingFilename = actualVersion + Configs.TRAINING_SUFFIX + Configs.ARFF_EXT;
        Path trainingPath = Configs.getProjWekaOutDir(projName).resolve(trainingFilename);

        String testingFilename = actualVersion + Configs.TESTING_SUFFIX + Configs.ARFF_EXT;
        Path testingPath = Configs.getProjWekaOutDir(projName).resolve(testingFilename);

        // Open training set source
        try {
            trainingSource = new DataSource(trainingPath.toString());
            trainingSet = trainingSource.getDataSet();
        } catch (Exception e) {
            logger.logFinest("Generic exception for: " + trainingPath + ", e:" + e);
            return;
        }

        // Open testing set source
        try {
            testingSource = new DataSource(testingPath.toString());
            testingSet = testingSource.getDataSet();
        } catch (Exception e) {
            logger.logFinest("Generic exception for: " + testingPath + ", e:" + e);
            return;
        }

        // Set the last attribute as the one to be predicted
        int numAttr = trainingSet.numAttributes();
        trainingSet.setClassIndex(numAttr - 1);
        testingSet.setClassIndex(numAttr - 1);

        // 2.3: NAIVE suite
        addNAIVESuite();

        // 2.4: SIMPLE suite
        addSIMPLESuite();

    }

    private ExperimentSuite getBaseSuite(String name) {
        return new ExperimentSuite(versionCounter, actualVersion, name, trainingSet, testingSet);
    }

    private void addNAIVESuite() {
        ExperimentSuite suite = getBaseSuite("NAIVE");

        suite.add(new Experiment("ZeroR", new ZeroR()));
        suite.add(new Experiment("OneR", new OneR()));

        plan.addSuite(suite);
    }

    private void addSIMPLESuite() {
        ExperimentSuite suite = getBaseSuite("SIMPLE");

        suite.add(new Experiment("RandomForest", new RandomForest()));
        suite.add(new Experiment("Random Forest", new RandomForest()));
        suite.add(new Experiment("Naive Bayes", new NaiveBayes()));
        suite.add(new Experiment("IBk", new IBk()));

        plan.addSuite(suite);
    }

}
