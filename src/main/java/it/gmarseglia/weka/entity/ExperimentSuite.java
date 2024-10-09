package it.gmarseglia.weka.entity;

import weka.core.Instances;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ExperimentSuite {

    private final int versionCounter;
    private final String version;
    private final String name;
    private final Instances testingSet;
    private final List<Experiment> experiments = new ArrayList<>();
    private Instances trainingSet;
    private Instances unfilteredTestingSet;

    public ExperimentSuite(int versionCounter, String version, String name, Instances trainingSet, Instances testingSet) {
        this.versionCounter = versionCounter;
        this.version = version;
        this.name = name;
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void add(Experiment experiment) {
        this.experiments.add(experiment);
    }

    public int getVersionCounter() {
        return versionCounter;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Instances getTrainingSet() {
        return trainingSet;
    }

    public void setTrainingSet(Instances trainingSet) {
        this.trainingSet = trainingSet;
    }

    public Instances getTestingSet() {
        return testingSet;
    }

    public Instances getUnfilteredTestingSet() {
        if (unfilteredTestingSet != null) {
            return unfilteredTestingSet;
        } else {
            return getTestingSet();
        }
    }

    public void setUnfilteredTestingSet(Instances unfilteredTestingSet) {
        this.unfilteredTestingSet = unfilteredTestingSet;
    }
}
