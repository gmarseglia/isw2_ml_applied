package it.gmarseglia.app;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.controller.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;


public class App {

    private static final MyLogger logger = MyLogger.getInstance(App.class);

    public static void main(String[] args) {
        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerboseFinest(false);

        Map<String, Boolean> configurations = new LinkedHashMap<>();
        configurations.put("BOOKKEEPER", true);
       configurations.put("OPENJPA", true);

        for (Map.Entry<String, Boolean> configuration : configurations.entrySet()) {
            String projName = configuration.getKey();
            Boolean computeMetrics = configuration.getValue();

            MyLogger.getInstance(App.class).logNoPrefix("Project: " + projName);

            GitController.getInstance(projName).setTagsRegex("(release-)?(syncope-)?(v)?%v(-incubating)?");
            DatasetController.getInstance(projName).setComputeMetrics(computeMetrics);

            ToFileBoundary.writeStringProj(String.format("Begin: \t%s", Instant.now().toString()), projName, "performance.csv");
            run(projName);
            ToFileBoundary.writeStringProj(String.format("End  : \t%s", Instant.now().toString()), projName, "performance.csv");
        }

    }

    private static void run(String projName) {
        DatasetController dc = DatasetController.getInstance(projName);

        MyLogger.getInstance(IssueController.class).setVerboseFine(false);

        try {
            dc.populateDataset();
        } catch (GitAPIException e) {
            logger.log(String.format("jgit throw an exception: %s", e));
        }
    }
}
