package it.gmarseglia.weka;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.app.exceptions.CustomRuntimeException;
import it.gmarseglia.weka.controller.ExperimentsController;
import it.gmarseglia.weka.controller.ExperimentsPlanner;
import it.gmarseglia.weka.entity.ExperimentPlan;
import it.gmarseglia.weka.util.Configs;
import it.gmarseglia.weka.util.CsvToArffConverter;

import java.io.IOException;
import java.util.List;

public class App {
    private static final MyLogger logger = MyLogger.getInstance(ExperimentsController.class);

    public static void main(String[] args) throws Exception {
        List<String> projects = List.of("BOOKKEEPER", "OPENJPA");
        CsvToArffConverter.convertProjects(projects);

        logger.setVerbose(true);
        logger.setVerboseFine(true);

        for (String projName : projects) {
            run(projName);
        }

    }

    private static void run(String projName) {
        ExperimentPlan plan = null;
        try {
            plan = ExperimentsPlanner.getInstance(projName).plan();
        } catch (IOException e) {
            throw new CustomRuntimeException(e);
        }
        ExperimentsController.getInstance(projName).processExperimentPlan(plan);
        ToFileBoundary.writeList(plan.getAllResults(), Configs.getProjWekaOutDir("EXPERIMENTS"), projName + "-RESULTS.csv");
    }


}
