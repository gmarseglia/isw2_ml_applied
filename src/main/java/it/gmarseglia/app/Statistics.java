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
        logger.logPrefixless(() -> System.out.println("\n\nAll tags"));
        GitController gc = GitController.getInstance(projName);
        gc.setTagsRegex("(release-)?%v(-incubating)?");
        List<String> allTags = gc.listTags();
        logger.logPrefixless(() -> System.out.printf("All tags size: %d\n", allTags.size()));
        allTags.forEach(logger::logObjectPrefixless);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll versions"));
        VersionsController vc = VersionsController.getInstance(projName);
        List<Version> allVersions = vc.getAllVersions();
        logger.logPrefixless(() -> System.out.printf("All versions size: %d\n", allVersions.size()));
        allVersions.forEach(logger::logObjectPrefixless);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll released versions"));
        List<Version> allReleasedVersions = vc.getAllReleasedVersions();
        logger.logPrefixless(() -> System.out.printf("All released versions size: %d\n", allReleasedVersions.size()));
        allReleasedVersions.forEach(logger::logObjectPrefixless);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll valid versions"));
        List<Version> allValidVersions = vc.getAllValidVersions();
        logger.logPrefixless(() -> System.out.printf("All Valid versions: %d\n", allValidVersions.size()));
        allValidVersions.forEach(logger::logObjectPrefixless);

        IssueController ic = IssueController.getInstance(projName);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll issues"));
        List<Issue> allIssues = ic.getTotalIssues(Integer.MAX_VALUE);
        logger.logPrefixless(() -> System.out.printf("All issues size: %d\n", allIssues.size()));
        // allIssues.forEach(logger::logObjectPrefixless);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll valid issues"));
        List<Issue> allValidIssues = ic.getValidIssues(Integer.MAX_VALUE);
        logger.logPrefixless(() -> System.out.printf("All valid issues size: %d\n", allValidIssues.size()));
        allValidIssues.forEach(logger::logObjectPrefixless);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll non valid issues"));
        List<Issue> allNonValidIssues = new ArrayList<>(allIssues);
        allNonValidIssues.removeAll(allValidIssues);
        logger.logPrefixless(() -> System.out.printf("All Non valid issues size: %d\n", allNonValidIssues.size()));
        allNonValidIssues.forEach(logger::logObjectPrefixless);

        MyLogger.getInstance(ProportionController.class).setVerboseFinest(true);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll valid proportioned issues"));
        ProportionController pc = ProportionController.getInstance(projName);
        List<Issue> allValidProportionedIssues = pc.getAllProportionedIssues();
        logger.logPrefixless(() -> System.out.printf("All valid proportioned issues size: %d\n", allValidProportionedIssues.size()));
        allValidProportionedIssues.forEach(logger::logObjectPrefixless);
    }
}
