package it.gmarseglia.weka.entity;

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
                String extendedExperimentName = String.format("%s-%d_(%s)-%s-%s",
                        projName, suite.getVersionCounter(), suite.getVersion(), suite.getName(), experiment.getClassifierName());
                result.setExperimentName(extendedExperimentName);
                result.setExperimentType(String.format("%s-%s", suite.getName(), experiment.getClassifierName()));
                result.setVersionCounter(suite.getVersionCounter());
                this.allResults.add(experiment.getResult());
            }
        }
    }

    public List<ExperimentResult> getAllResults() {
        return allResults;
    }
}
