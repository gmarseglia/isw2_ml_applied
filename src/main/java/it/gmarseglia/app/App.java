package it.gmarseglia.app;

import it.gmarseglia.app.controller.DatasetController;
import it.gmarseglia.app.controller.GitController;
import it.gmarseglia.app.controller.IssueFactory;
import it.gmarseglia.app.controller.MyLogger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;


public class App {
    public static void main(String[] args) {
        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerboseFinest(false);

        for (String projName : Arrays.asList("BOOKKEEPER", "OPENJPA")) {
            MyLogger.getInstance(App.class).logNoPrefix("Producing dataset for " + projName);

            GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");

            Path performanceFile = Paths.get(".", "out", projName + "_performance.txt");
            try {
                Files.writeString(performanceFile, "Begin: " + Instant.now().toString().concat(System.lineSeparator()), CREATE, APPEND);
                run(projName);
                Files.writeString(performanceFile, "End: " + Instant.now().toString().concat(System.lineSeparator()), APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static void run(String projName) {
        DatasetController dc = DatasetController.getInstance(projName);

        try {
            dc.populateDataset();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

    }
}
