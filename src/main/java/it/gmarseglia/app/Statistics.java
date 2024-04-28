package it.gmarseglia.app;

import it.gmarseglia.app.controller.IssueController;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.app.controller.VersionsController;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.ArrayList;
import java.util.List;

public class Statistics {

    private static final MyLogger logger = MyLogger.getInstance(Statistics.class);

    public static void main(String[] args) {
        try {
            issueStats();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static void issueStats() throws GitAPIException {
        String projName = "OPENJPA";

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        logger.setVerbose(true);

        VersionsController vc = VersionsController.getInstance(projName);
        List<Version> allVersions = vc.getAllVersions();
        logger.log(() -> System.out.printf("\n\nAll Versions: %d\n", allVersions.size()));
        allVersions.forEach(logger::logObject);

        List<Version> allValidVersions = vc.getAllValidVersions();
        logger.log(() -> System.out.printf("\n\nAll Valid versions: %d\n", allValidVersions.size()));
        allValidVersions.forEach(logger::logObject);

        IssueController ic = IssueController.getInstance(projName);

        List<Issue> allIssues = ic.getIssues(Integer.MAX_VALUE);
        logger.log(() -> System.out.printf("\n\nAll Issues size: %d\n", allIssues.size()));
        allIssues.forEach(logger::logObject);

        List<Issue> allValidIssues = ic.getValidIssues(Integer.MAX_VALUE);
        logger.log(() -> System.out.printf("\n\nAll Valid issues size: %d\n", allValidIssues.size()));
        allValidIssues.forEach(logger::logObject);

        List<Issue> allNonValidIssues = new ArrayList<>(allIssues);
        allNonValidIssues.removeAll(allValidIssues);
        logger.log(() -> System.out.printf("\n\nAll Non valid issues size: %d\n", allNonValidIssues.size()));
        allNonValidIssues.forEach(logger::logObject);

    }
}
