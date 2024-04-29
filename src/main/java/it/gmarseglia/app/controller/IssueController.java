package it.gmarseglia.app.controller;

import it.gmarseglia.app.boundary.IssueJSONGetter;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.JiraIssue;
import it.gmarseglia.app.entity.JiraIssueReport;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class IssueController {

    private static final Map<String, IssueController> instances = new HashMap<>();

    private final IssueJSONGetter issueJSONGetter;
    private final IssueFactory issueFactory;
    private Integer lastMaxTotal;
    private final List<JiraIssue> allJiraIssues = new ArrayList<>();
    private final MyLogger logger;
    private final GitController gc;

    private IssueController(String projName) {
        this.issueJSONGetter = new IssueJSONGetter(projName);
        this.issueFactory = IssueFactory.getInstance(projName);
        this.logger = MyLogger.getInstance(this.getClass());
        this.gc = GitController.getInstance(projName);
    }

    public static IssueController getInstance(String projName) {
        IssueController.instances.computeIfAbsent(projName, IssueController::new);
        return IssueController.instances.get(projName);
    }

    public List<Issue> getValidIssues(int maxTotal) throws GitAPIException {
        List<Issue> allIssues = this.getIssues(maxTotal);

        logger.log(() -> System.out.printf("All issues found: %d\n", allIssues.size()));

        // commits > 0
        Predicate<Issue> noCommitFilter = issue -> {
            try {
                return !gc.getAllCommitsByIssue(issue).isEmpty();
            } catch (GitAPIException e) {
                return false;
            }
        };
        logger.logFine(() -> System.out.printf("Issues which fails \"noCommitFilter\": %d\n", allIssues.stream().filter(noCommitFilter.negate()).count()));

        // IV < FV
        Predicate<Issue> nonPostReleaseFilter = issue -> {
            if (issue.getInjectVersion() != null && issue.getFixVersion() != null) {
                return issue.getInjectVersion().getJiraReleaseDate().compareTo(issue.getFixVersion().getJiraReleaseDate()) < 0;
            } else {
                return true;
            }
        };
        logger.logFine(() -> System.out.printf("Issues which fails \"nonPostRelease\": %d\n", allIssues.stream().filter(nonPostReleaseFilter.negate()).count()));


        // IV <= OV
        Predicate<Issue> IVConsistencyFilter = issue -> {
            if (issue.getInjectVersion() != null && issue.getFixVersion() != null) {
                return issue.getInjectVersion().getJiraReleaseDate().compareTo(issue.getOpeningVersion().getJiraReleaseDate()) <= 0;
            } else {
                return true;
            }
        };
        logger.logFine(() -> System.out.printf("Issues which fails \"IVConsistencyFilter\": %d\n", allIssues.stream().filter(IVConsistencyFilter.negate()).count()));


        // OV <= FV
        Predicate<Issue> openingConsistencyFilter = issue -> {
            if (issue.getOpeningVersion() != null && issue.getFixVersion() != null) {
                return issue.getOpeningVersion().getJiraReleaseDate().compareTo(issue.getFixVersion().getJiraReleaseDate()) <= 0;
            } else {
                return true;
            }
        };
        logger.logFine(() -> System.out.printf("Issues which fails \"openingConsistencyFilter\": %d\n", allIssues.stream().filter(openingConsistencyFilter.negate()).count()));


        List<Issue> filteredIssues = allIssues.stream()
                .filter(nonPostReleaseFilter)
                .filter(IVConsistencyFilter)
                .filter(openingConsistencyFilter)
                .filter(noCommitFilter)
                .toList();

        logger.log(() -> System.out.printf("All valid issues found: %d\n", filteredIssues.size()));

        return filteredIssues;
    }

    /**
     * 1. Gets all the issues from Jira, via {@code getAllJiraIssues}.
     * 2. For each issue:
     * Assigns the OV, FV and AV by comparing Jira and GitHub info. Look {@link IssueFactory} for more.
     *
     * @return A list of all issues.
     * @throws GitAPIException due to {@link GitController}
     */
    public List<Issue> getIssues(int maxTotal) throws GitAPIException {
        List<Issue> result = new ArrayList<>();

        if (this.lastMaxTotal == null || maxTotal != this.lastMaxTotal) {
            // get all Jira issues
            this.lastMaxTotal = maxTotal;
            this.setJiraIssues(maxTotal);
        }

        logger.log(() -> System.out.printf("Found %d Jira Issues.\n", this.allJiraIssues.size()));

        for (JiraIssue jiraIssue : this.allJiraIssues) {
            result.add(this.issueFactory.issueFromJiraIssue(jiraIssue));
        }

        return result;
    }

    /**
     * Call multiple time {@link IssueJSONGetter} to obtain all the Jira issues.
     * {@code allJiraIssues} holds the list.
     */
    private void setJiraIssues(int maxTotal) {
        this.allJiraIssues.clear();

        JiraIssueReport jiraIssueReport;

        int maxResult = 1000;
        int total = maxResult;

        for (int i = 0; i < Math.min(total, maxTotal); i += maxResult) {

            int finalI = i;
            int finalTotal = total;
            logger.log(() -> System.out.printf("Getting Jira issues from %d to %d.\n", finalI, Math.min(finalTotal, maxTotal)));

            jiraIssueReport = issueJSONGetter.getIssueReport(i, Math.min(maxTotal - i, maxResult));

            total = jiraIssueReport.getTotal();

            this.allJiraIssues.addAll(jiraIssueReport.getIssues());
        }
    }


}
