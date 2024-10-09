package it.gmarseglia.weka.entity;

import it.gmarseglia.app.entity.Exportable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

public class ExperimentResult implements Exportable {

    private static final DecimalFormat DF = new DecimalFormat("0.000");
    private static final DecimalFormat DF_INT = new DecimalFormat("00");
    private final double correctPercentage;
    private final double recall;
    private final double precision;
    private final double auc;
    private final double kappa;
    private final double distribution;
    private String experimentName;
    private int versionCounter;
    private String experimentType;
    private String classifierName;

    public ExperimentResult(double correctPercentage, double recall, double precision, double auc, double kappa, double distribution) {
        this.correctPercentage = correctPercentage;
        this.recall = recall;
        this.precision = precision;
        this.auc = auc;
        this.kappa = kappa;
        this.distribution = distribution;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public void setVersionCounter(int versionCounter) {
        this.versionCounter = versionCounter;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    public void setClassifierName(String classifierName) {
        this.classifierName = classifierName;
    }

    @Override
    public String toString() {
        return "ExperimentResult{" +
                "experimentName='" + experimentName + '\'' +
                ", correctPercentage=" + DF.format(correctPercentage) +
                ", recall=" + DF.format(recall) +
                ", precision=" + DF.format(precision) +
                ", auc=" + DF.format(auc) +
                ", kappa=" + DF.format(kappa) +
                ", distribution=" + DF.format(distribution) +
                '}';
    }

    @Override
    public List<String> getFieldsNames() {
        return List.of("Experiment name",
                "VersionCounter",
                "ClassifierName",
                "Type",
                "Correct Percentage",
                "Recall",
                "Precision",
                "AUC",
                "Kappa",
                "Distribution");
    }

    @Override
    public List<Serializable> getFieldsValues() {

        return List.of(experimentName,
                DF_INT.format(versionCounter),
                classifierName,
                experimentType,
                DF.format(correctPercentage / 100D),
                DF.format(recall),
                DF.format(precision),
                DF.format(auc),
                DF.format(kappa),
                DF.format(distribution / 100D));
    }
}
