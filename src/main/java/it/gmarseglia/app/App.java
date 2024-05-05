package it.gmarseglia.app;

import it.gmarseglia.app.controller.*;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;


public class App {
    public static void main(String[] args) {
        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerboseFinest(false);

        for (String projName : List.of("AVRO")) {
            MyLogger.getInstance(App.class).logNoPrefix("Project: " + projName);

            GitController.getInstance(projName).setTagsRegex("(release-)?(syncope-)?%v(-incubating)?");

            MyFileUtils.createDirectory(Paths.get(".", "out", projName));
            Path performanceFile = Paths.get(".", "out", projName, "performance.txt");
            try {
                Files.writeString(performanceFile, "Begin: \t" + Instant.now().toString().concat(System.lineSeparator()), CREATE, WRITE);
                test(projName);
                // run(projName);
                Files.writeString(performanceFile, "End  : \t" + Instant.now().toString().concat(System.lineSeparator()), APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static void test(String projName) {
        try {
//            MyLogger.getInstance(IssueController.class).setVerboseFine(false);
            VersionsController.getInstance(projName).getHalfVersion();
            ProportionController.getInstance(projName).getTotalProportionedIssues(Integer.MAX_VALUE);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static void run(String projName) {
        DatasetController dc = DatasetController.getInstance(projName);

        MyLogger.getInstance(Version.class).setVerboseFinest(true);

        try {
            dc.populateDataset();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

    }
}
