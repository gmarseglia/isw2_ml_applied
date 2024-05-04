package it.gmarseglia.app;

import it.gmarseglia.app.controller.*;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Statistics {

    private static final MyLogger logger = MyLogger.getInstance(Statistics.class);

    public static void main(String[] args) {
        try {
            issueStats();
        } catch (GitAPIException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void issueStats() throws GitAPIException, InterruptedException {
        String projName = "BOOKKEEPER";

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        logger.setVerbose(true);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All tags");
        GitController gc = GitController.getInstance(projName);
        gc.setTagsRegex("(release-)?%v(-incubating)?");
        List<String> allTags = gc.listTags();
        logger.logNoPrefix(String.format("All tags size: %d", allTags.size()));
        allTags.forEach(logger::logObjectNoPrefix);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All versions");
        VersionsController vc = VersionsController.getInstance(projName);
        List<Version> allVersions = vc.getAllVersions();
        logger.logNoPrefix(String.format("All versions size: %d", allVersions.size()));
        allVersions.forEach(logger::logObjectNoPrefix);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All released versions");
        List<Version> allReleasedVersions = vc.getAllReleasedVersions();
        logger.logNoPrefix(String.format("All released versions size: %d", allReleasedVersions.size()));
        allReleasedVersions.forEach(logger::logObjectNoPrefix);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All valid versions");
        List<Version> allValidVersions = vc.getAllValidVersions();
        logger.logNoPrefix(String.format("All Valid versions: %d", allValidVersions.size()));
        allValidVersions.forEach(logger::logObjectNoPrefix);

        IssueController ic = IssueController.getInstance(projName);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All issues");
        List<Issue> allIssues = ic.getTotalIssues(Integer.MAX_VALUE);
        logger.logNoPrefix(String.format("All issues size: %d", allIssues.size()));

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All valid issues");
        List<Issue> allValidIssues = ic.getTotalValidIssues(Integer.MAX_VALUE);
        logger.logNoPrefix(String.format("All valid issues size: %d", allValidIssues.size()));
        allValidIssues.forEach(logger::logObjectNoPrefix);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All non valid issues");
        List<Issue> allNonValidIssues = new ArrayList<>(allIssues);
        allNonValidIssues.removeAll(allValidIssues);
        logger.logNoPrefix(String.format("All Non valid issues size: %d", allNonValidIssues.size()));
        allNonValidIssues.forEach(logger::logObjectNoPrefix);

        MyLogger.getInstance(ProportionController.class).setVerboseFinest(true);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("All valid proportioned issues");
        ProportionController pc = ProportionController.getInstance(projName);
        List<Issue> allValidProportionedIssues = pc.getTotalProportionedIssues(Integer.MAX_VALUE);
        logger.logNoPrefix(String.format("All valid proportioned issues size: %d", allValidProportionedIssues.size()));
        allValidProportionedIssues.forEach(logger::logObjectNoPrefix);
    }
}
