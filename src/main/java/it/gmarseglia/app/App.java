package it.gmarseglia.app;

import it.gmarseglia.app.boundary.IssueJSONGetter;
import it.gmarseglia.app.controller.*;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;


public class App {
    public static void main(String[] args) {
        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerboseFinest(false);

        for (String projName : Arrays.asList("BOOKKEEPER", "OPENJPA")) {
            MyLogger.getInstance(App.class).logPrefixless(() -> System.out.println("Producing dataset for " + projName));

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

    private static void test(String projName) {
        MyLogger.setStaticVerbose(true);

        System.out.println("Printing all versions on Jira:");
        ProjectController pc = ProjectController.getInstance(projName);
        pc.getProject().getVersions().forEach(System.out::println);

        System.out.println("\n\nPrinting all tags on GitHub:");
        GitController gc = GitController.getInstance(projName);
        try {
            gc.listTags().forEach(System.out::println);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n\nPrinting all valid version crossing data from Jira and GitHub:");
        VersionsController vc = VersionsController.getInstance(projName);
        List<Version> allValidVersions;
        try {
            allValidVersions = vc.getAllValidVersions();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        allValidVersions.forEach(System.out::println);

        System.out.println("\n\nPrinting all issues crossing data from Jira and GitHub:");
        IssueController ic = IssueController.getInstance(projName);
        List<Issue> issues;
        try {
            issues = ic.getTotalValidIssues(Integer.MAX_VALUE);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        issues.forEach(System.out::println);

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
