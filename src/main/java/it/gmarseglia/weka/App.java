package it.gmarseglia.weka;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.weka.controller.ExperimentsController;
import it.gmarseglia.weka.controller.ExperimentsPlanner;
import it.gmarseglia.weka.entity.ExperimentPlan;
import it.gmarseglia.weka.util.Configs;
import it.gmarseglia.weka.util.CsvToArffConverter;

import java.util.List;

public class App {
    private static final MyLogger logger = MyLogger.getInstance(ExperimentsController.class);

    public static void main(String[] args) throws Exception {
        List<String> projects = Configs.getProjects();
        projects = List.of("BOOKKEEPER", "OPENJPA");
        CsvToArffConverter.convertProjects(projects);

        logger.setVerbose(true);
        logger.setVerboseFine(true);

        for (String projName : projects) {
            run(projName);
        }

    }

    private static void run(String projName) throws Exception {
        ExperimentPlan plan = ExperimentsPlanner.getInstance(projName).plan();
        ExperimentsController.getInstance(projName).processExperimentPlan(plan);

        ToFileBoundary.writeList(plan.getAllResults(), Configs.getProjWekaOutDir("EXPERIMENTS"), projName + "-RESULTS.csv");
    }


}
