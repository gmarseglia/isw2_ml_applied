package it.gmarseglia.weka.entity;

import it.gmarseglia.app.entity.Exportable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

public class ExperimentResult implements Exportable {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private final String experimentName;
    private final String version;
    private final double correctPercentage;
    private final double recall;
    private final double precision;
    private final double distribution;

    public ExperimentResult(String experimentName, String version, double correctPercentage, double recall, double precision, double distribution) {
        this.experimentName = experimentName;
        this.version = version;
        this.correctPercentage = correctPercentage;
        this.recall = recall;
        this.precision = precision;
        this.distribution = distribution;
    }

    @Override
    public String toString() {
        return "ExperimentResult{" +
                "experimentName='" + experimentName + '\'' +
                ", version='" + version + '\'' +
                ", correctPercentage=" + DF.format(correctPercentage) +
                ", recall=" + DF.format(recall) +
                ", precision=" + DF.format(precision) +
                ", distribution=" + DF.format(distribution) +
                '}';
    }

    @Override
    public List<String> getFieldsNames() {
        return List.of("Experiment name", "Version",
                "Correct Percentage",
                "Recall",
                "Precision",
                "Distribution");
    }

    @Override
    public List<Serializable> getFieldsValues() {

        return List.of(experimentName, version,
                DF.format(correctPercentage),
                DF.format(recall),
                DF.format(precision),
                DF.format(distribution));
    }
}
