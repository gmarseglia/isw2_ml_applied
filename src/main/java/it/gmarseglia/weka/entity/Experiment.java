package it.gmarseglia.weka.entity;

import weka.classifiers.Classifier;

public class Experiment {

    private String classifierName;
    private Classifier classifier;
    private ExperimentResult result;

    public Experiment(String classifierName, Classifier classifer) {
        this.classifierName = classifierName;
        this.classifier = classifer;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public ExperimentResult getResult() {
        return result;
    }

    public void setResult(ExperimentResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "classifierName='" + classifierName + "\t" + '\'' +
                ", result=" + result +
                '}';
    }
}
