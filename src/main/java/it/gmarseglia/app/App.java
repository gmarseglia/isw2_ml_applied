package it.gmarseglia.app;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.controller.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;


public class App {
    public static void main(String[] args) {
        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerboseFinest(false);

        for (String projName : List.of("BOOKKEEPER", "OPENJPA", "SYNCOPE", "AVRO")) {
            MyLogger.getInstance(App.class).logNoPrefix("Project: " + projName);

            GitController.getInstance(projName).setTagsRegex("(release-)?(syncope-)?%v(-incubating)?");

            ToFileBoundary.writeStringProj(String.format("Begin: \t%s", Instant.now().toString()), projName, "performance.csv");
            run(projName);
            ToFileBoundary.writeStringProj(String.format("End  : \t%s", Instant.now().toString()), projName, "performance.csv");

        }

    }

    private static void run(String projName) {
        DatasetController dc = DatasetController.getInstance(projName);

        try {
            dc.setComputeMetrics(false);
            dc.populateDataset();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

    }
}
