package it.gmarseglia.weka.entity;

import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class ExperimentSuite {

    private int versionCounter;
    private String version;
    private String name;
    private Instances trainingSet;
    private Instances testingSet;
    private List<Experiment> experiments = new ArrayList<>();

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
}
