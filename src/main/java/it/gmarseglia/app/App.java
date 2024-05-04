package it.gmarseglia.app;

import it.gmarseglia.app.controller.*;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.List;


public class App {
    public static void main(String[] args) {
        String projName = "BOOKKEEPER";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerboseFinest(false);

        run(projName);
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
            dc.populateDataset(true);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

    }
}
