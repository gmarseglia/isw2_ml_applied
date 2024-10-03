package it.gmarseglia.weka.entity;

import it.gmarseglia.app.entity.Exportable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

public class ExperimentResult implements Exportable {

    private String experimentName;
    private String version;
    private double correctPercentage;
    private double recall;
    private double precision;

    public ExperimentResult(String experimentName, String version, double correctPercentage, double recall, double precision) {
        this.experimentName = experimentName;
        this.version = version;
        this.correctPercentage = correctPercentage;
        this.recall = recall;
        this.precision = precision;
    }

    @Override
    public List<String> getFieldsNames() {
        return List.of("Experiment name", "Version",
                "Correct Percentage",
                "Recall",
                "Precision");
    }

    @Override
    public List<Serializable> getFieldsValues() {
        DecimalFormat df = new DecimalFormat("0.00");
        return List.of(experimentName, version,
                df.format(correctPercentage),
                df.format(recall),
                df.format(precision));
    }
}
