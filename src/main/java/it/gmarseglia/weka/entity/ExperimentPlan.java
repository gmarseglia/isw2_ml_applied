package it.gmarseglia.weka.entity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ExperimentPlan {

    private final String projName;
    private final List<ExperimentSuite> suites;
    private final List<ExperimentResult> allResults;

    public ExperimentPlan(String projName) {
        this.projName = projName;
        this.suites = new ArrayList<>();
        this.allResults = new ArrayList<>();
    }

    public void addSuite(ExperimentSuite newSuite) {
        this.suites.add(newSuite);
    }

    public List<ExperimentSuite> getSuites() {
        return suites;
    }

    public void addResultsFromSuite(ExperimentSuite suite) {
        for (Experiment experiment : suite.getExperiments()) {
            ExperimentResult result = experiment.getResult();
            if (result != null) {
                DecimalFormat df = new DecimalFormat("00");
                String extendedExperimentName = String.format("%s-%s_v%s-%s-%s-%s",
                        projName,
                        df.format(suite.getVersionCounter()), suite.getVersion().replace(".", "+"),
                        suite.getName(),
                        experiment.getClassifierName(),
                        experiment.getClassifierModifier());
                result.setExperimentName(extendedExperimentName);
                result.setExperimentType(suite.getName());
                result.setClassifierName(experiment.getClassifierName());
                result.setClassifierModifier(experiment.getClassifierModifier());
                result.setVersionCounter(suite.getVersionCounter());
                this.allResults.add(experiment.getResult());
            }
        }
    }

    public List<ExperimentResult> getAllResults() {
        return allResults;
    }
}
