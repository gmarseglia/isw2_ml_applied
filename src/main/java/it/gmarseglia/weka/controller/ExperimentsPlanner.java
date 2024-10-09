package it.gmarseglia.weka.controller;

import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.entity.Experiment;
import it.gmarseglia.weka.entity.ExperimentPlan;
import it.gmarseglia.weka.entity.ExperimentSuite;
import it.gmarseglia.weka.util.Configs;
import it.gmarseglia.weka.util.CsvSequentialReader;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
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
    private Instances trainingSet;
    private Instances testingSet;


    private ExperimentsPlanner(String projName) {
        this.projName = projName;
        this.versionCounter = 0;
    }

    public static ExperimentsPlanner getInstance(String projName) {
        ExperimentsPlanner.instances.computeIfAbsent(projName, s -> new ExperimentsPlanner(projName));
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
     *   2.4 Create the SIMPLE ExperimentSuite:              (4 sub, 5 total)
     *       2.4.a ZeroR
     *       2.4.a RandomForest
     *       2.4.b Naive Bayes
     *       2.4.c IBk
     *   2.5 Create the BALANCING ExperimentSuite:           (4*3=12 sub, 19 total)
     *       2.5.a SIMPLE × over sampling
     *       2.5.b SIMPLE × under sampling
     *       2.5.c SIMPLE × SMOTE
     *   2.6 Create the FEATURE SELECTION ExperimentSuite:   (4*1=4 sub, 23 total)
     *       2.6.a SIMPLE × feature selection
     *   2.7 Create the SENSITIVITY ExperimentSuite:        (4*2=8 sub, 31 total)
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

            String extendedVersionName = String.format("%s-%d_v%s", projName, versionCounter, actualVersion);
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
            DataSource trainingSource = new DataSource(trainingPath.toString());
            trainingSet = trainingSource.getDataSet();
        } catch (Exception e) {
            logger.logFinest("Generic exception for: " + trainingPath + ", e:" + e);
            return;
        }

        // Open testing set source
        try {
            DataSource testingSource = new DataSource(testingPath.toString());
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

        // 2.5: BALANCING suite
        addBALANCINGSuite();

        // 2.6: FEATURE SELECTION suite
        addFEATURESELECTIONSuite();

        // 2.7: SENSITIVITY suite
        addSENSITIVITYSuite();

    }

    private ExperimentSuite getBaseSuite(String name) {
        return new ExperimentSuite(versionCounter, actualVersion, name, trainingSet, testingSet);
    }

    private Map<String, Classifier> getBaseClassifiersMap() {
        Map<String, Classifier> map = new HashMap<>();
        map.put("Random_Forest", new RandomForest());
        map.put("Naive_Bayes", new NaiveBayes());
        map.put("IBk", new IBk());
        return map;
    }

    private void addNAIVESuite() {
        ExperimentSuite suite = getBaseSuite("NAIVE");

        suite.add(new Experiment("ZeroR", "no", new ZeroR()));
        suite.add(new Experiment("OneR", "no", new OneR()));

        plan.addSuite(suite);
    }

    private void addSIMPLESuite() {
        ExperimentSuite suite = getBaseSuite("SIMPLE");

        suite.add(new Experiment("Random_Forest", "no", new RandomForest()));
        suite.add(new Experiment("Naive_Bayes", "no", new NaiveBayes()));
        suite.add(new Experiment("IBk", "no", new IBk()));

        plan.addSuite(suite);
    }

    private void addBALANCINGSuite() {
        ExperimentSuite suite = getBaseSuite("BALANCING");

        // under sampling
        try {
            SpreadSubsample spreadSubsample = new SpreadSubsample();
            String[] opts = new String[]{"-M", "1.0"};
            spreadSubsample.setOptions(opts);
            for (Map.Entry<String, Classifier> entry : getBaseClassifiersMap().entrySet()) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setFilter(spreadSubsample);
                filteredClassifier.setClassifier(entry.getValue());
                suite.add(new Experiment(entry.getKey(), "under_sampling", filteredClassifier));
            }
        } catch (Exception e) {
            logger.log("Generic exception during under sample: " + e);
        }


        // over sampling
        try {
            Resample resample = new Resample();
            resample.setInputFormat(trainingSet);

            // get the last attribute as the label
            Attribute label = suite.getTrainingSet().attribute(suite.getTrainingSet().numAttributes() - 1);

            // compute the percentage of "buggy" classes over the total
            AttributeStats labelStats = suite.getTrainingSet().attributeStats(label.index());
            int buggyCount = labelStats.nominalCounts[0];
            double scale = (100D * labelStats.totalCount / buggyCount);
            DecimalFormat df = new DecimalFormat("#.0");
            String scaleStr = df.format(scale);

            String[] optsOver = new String[]{"-B", "1.0", "-Z", scaleStr};
            resample.setOptions(optsOver);

            for (Map.Entry<String, Classifier> entry : getBaseClassifiersMap().entrySet()) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setFilter(resample);
                filteredClassifier.setClassifier(entry.getValue());
                suite.add(new Experiment(entry.getKey(), "over_sampling", filteredClassifier));
            }
        } catch (Exception e) {
            logger.log("Generic exception, during over sample: " + e);
        }

        // SMOTE
        try {
            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingSet);
            for (Map.Entry<String, Classifier> entry : getBaseClassifiersMap().entrySet()) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setFilter(smote);
                filteredClassifier.setClassifier(entry.getValue());
                suite.add(new Experiment(entry.getKey(), "SMOTE", filteredClassifier));
            }
        } catch (Exception e) {
            logger.log("Generic exception, during SMOTE: " + e);
        }

        plan.addSuite(suite);
    }

    private void addFEATURESELECTIONSuite() {
        try {
            AttributeSelection filter = new AttributeSelection();
            // create evaluator and search algorithm objects
            CfsSubsetEval eval = new CfsSubsetEval();
            GreedyStepwise search = new GreedyStepwise();
            // set the algorithm to search backward
            search.setSearchBackwards(true);
            // set the filter to use the evaluator and search algorithm
            filter.setEvaluator(eval);
            filter.setSearch(search);
            // specify the dataset
            filter.setInputFormat(trainingSet);

            // apply to the training set AND the testing set
            Instances filteredTrainingSet = Filter.useFilter(trainingSet, filter);
            Instances filteredTestingSet = Filter.useFilter(testingSet, filter);

            int numAttrFiltered = filteredTrainingSet.numAttributes();
            filteredTrainingSet.setClassIndex(numAttrFiltered - 1);
            filteredTestingSet.setClassIndex(numAttrFiltered - 1);

            ExperimentSuite suite = new ExperimentSuite(versionCounter, actualVersion, "FEATURE_SELECTION", filteredTrainingSet, filteredTestingSet);

            suite.setUnfilteredTestingSet(testingSet);

            for (Map.Entry<String, Classifier> entry : getBaseClassifiersMap().entrySet()) {
                suite.add(new Experiment(entry.getKey(), "feature_selection", entry.getValue()));
            }

            plan.addSuite(suite);

        } catch (Exception e) {
            logger.log("Generic exception during feature selection: " + e);
        }
    }

    private void addSENSITIVITYSuite() {
        ExperimentSuite suite = getBaseSuite("SENSITIVITY");

        double weightFalsePositive = 1.0;
        double weightFalseNegative = 10.0;
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFalseNegative);
        costMatrix.setCell(0, 1, weightFalsePositive);
        costMatrix.setCell(1, 1, 0.0);

        try {
            for (Map.Entry<String, Classifier> entry : getBaseClassifiersMap().entrySet()) {
                CostSensitiveClassifier classifier = new CostSensitiveClassifier();
                classifier.setClassifier(entry.getValue());
                classifier.setCostMatrix(costMatrix);
                classifier.setMinimizeExpectedCost(true);
                suite.add(new Experiment(entry.getKey(), "sensitivity_threshold", classifier));
            }
        } catch (Exception e) {
            logger.log("Generic exception during sensitivity threshold: " + e);
        }

        try {
            for (Map.Entry<String, Classifier> entry : getBaseClassifiersMap().entrySet()) {
                CostSensitiveClassifier classifier = new CostSensitiveClassifier();
                classifier.setClassifier(entry.getValue());
                classifier.setCostMatrix(costMatrix);
                classifier.setMinimizeExpectedCost(false);
                suite.add(new Experiment(entry.getKey(), "sensitivity_learning", classifier));
            }
        } catch (Exception e) {
            logger.log("Generic exception during sensitivity learning: " + e);
        }

        plan.addSuite(suite);
    }

}
