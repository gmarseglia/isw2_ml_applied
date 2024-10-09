package it.gmarseglia.weka.entity;

import weka.classifiers.Classifier;

import java.util.ArrayList;
import java.util.List;

public class Experiment {

    private final String classifierName;
    private final Classifier classifier;
    private ExperimentResult result;
    private List<Prediction> predictions;

    public Experiment(String classifierName, Classifier classifier) {
        this.classifierName = classifierName;
        this.classifier = classifier;
        this.predictions = new ArrayList<>();
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

    public void addPrediction(Prediction newPrediction) {
        this.predictions.add(newPrediction);
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "classifierName='" + classifierName + "\t" + '\'' +
                ", result=" + result +
                '}';
    }
}
