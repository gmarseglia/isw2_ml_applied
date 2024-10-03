package it.gmarseglia.weka.entity;

import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class ExperimentSuite {

    private String projName;
    private String versionaName;
    private Instances trainingSet;
    private Instances testingSet;
    private List<Experiment> experiments = new ArrayList<>();

    public ExperimentSuite(String projName, String versionaName, Instances trainingSet, Instances testingSet) {
        this.projName = projName;
        this.versionaName = versionaName;
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void add(Experiment experiment) {
        this.experiments.add(experiment);
    }

    public String getProjName() {
        return projName;
    }

    public String getVersionaName() {
        return versionaName;
    }

    public Instances getTrainingSet() {
        return trainingSet;
    }

    public Instances getTestingSet() {
        return testingSet;
    }
}
