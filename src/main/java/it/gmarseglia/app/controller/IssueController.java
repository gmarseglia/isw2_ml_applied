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
    private final MyLogger logger;
    private final GitController gc;
    private List<Issue> totalValidIssues;
    private Integer lastMaxTotal;
    private List<Issue> totalIssues;

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

    public List<Issue> getTotalValidIssues(int maxTotal) throws GitAPIException {
        if (this.totalValidIssues == null || maxTotal != this.lastMaxTotal) {
            List<Issue> totalIssues = this.getTotalIssues(maxTotal);

            logger.logFine(() -> System.out.printf("Total issues found: %d\n", totalIssues.size()));

            // commits > 0
            Predicate<Issue> noCommitFilter = issue -> {
                try {
                    return !gc.getAllCommitsByIssue(issue).isEmpty();
                } catch (GitAPIException e) {
                    return false;
                }
            };
            logger.logFinest(() ->
                    System.out.printf("Issues which fails \"noCommitFilter\": %d\n",
                            totalIssues.stream().filter(noCommitFilter.negate()).count()));

            // IV < FV
            Predicate<Issue> nonPostReleaseFilter = issue -> {
                if (issue.getInjectVersion() != null && issue.getFixVersion() != null) {
                    return issue.getInjectVersion().getJiraReleaseDate().compareTo(issue.getFixVersion().getJiraReleaseDate()) < 0;
                } else {
                    return true;
                }
            };
            logger.logFinest(() ->
                    System.out.printf("Issues which fails \"nonPostRelease\": %d\n",
                            totalIssues.stream().filter(nonPostReleaseFilter.negate()).count()));

            // IV <= OV
            Predicate<Issue> IVConsistencyFilter = issue -> {
                if (issue.getInjectVersion() != null && issue.getOpeningVersion() != null) {
                    return issue.getInjectVersion().getJiraReleaseDate().compareTo(issue.getOpeningVersion().getJiraReleaseDate()) <= 0;
                } else {
                    return true;
                }
            };
            logger.logFinest(() ->
                    System.out.printf("Issues which fails \"IVConsistencyFilter\": %d\n",
                            totalIssues.stream().filter(IVConsistencyFilter.negate()).count()));

            // OV <= FV
            Predicate<Issue> openingConsistencyFilter = issue -> {
                if (issue.getOpeningVersion() != null && issue.getFixVersion() != null) {
                    return issue.getOpeningVersion().getJiraReleaseDate().compareTo(issue.getFixVersion().getJiraReleaseDate()) <= 0;
                } else {
                    return true;
                }
            };
            logger.logFinest(() ->
                    System.out.printf("Issues which fails \"openingConsistencyFilter\": %d\n",
                            totalIssues.stream().filter(openingConsistencyFilter.negate()).count()));

            this.totalValidIssues = totalIssues.stream()
                    .filter(nonPostReleaseFilter)
                    .filter(IVConsistencyFilter)
                    .filter(openingConsistencyFilter)
                    .filter(noCommitFilter)
                    .toList();

            logger.log(() -> System.out.printf("Total valid issues found: %d\n", this.totalValidIssues.size()));
        }
        return this.totalValidIssues;
    }

    /**
     * 1. Gets all the issues from Jira, via {@code getAllJiraIssues}.
     * 2. For each issue:
     * Assigns the OV, FV and AV by comparing Jira and GitHub info. Look {@link IssueFactory} for more.
     *
     * @return A list of all issues.
     * @throws GitAPIException due to {@link GitController}
     */
    public List<Issue> getTotalIssues(int maxTotal) throws GitAPIException {
        if (this.totalIssues == null || maxTotal != this.lastMaxTotal) {
            this.totalIssues = new ArrayList<>();
            this.lastMaxTotal = maxTotal;

            List<JiraIssue> totalJiraIssues = this.getJiraIssues(maxTotal);

            logger.logFine(() -> System.out.printf("Total Jira issues found %d.\n", totalJiraIssues.size()));

            for (JiraIssue jiraIssue : totalJiraIssues) {
                this.totalIssues.add(this.issueFactory.issueFromJiraIssue(jiraIssue));
            }
        }
        return this.totalIssues;
    }

    /**
     * Call multiple time {@link IssueJSONGetter} to obtain all the Jira issues.
     * {@code allJiraIssues} holds the list.
     */
    private List<JiraIssue> getJiraIssues(int maxTotal) {
        List<JiraIssue> result = new ArrayList<>();

        JiraIssueReport jiraIssueReport;

        int maxResult = 1000;
        int total = maxResult;

        for (int i = 0; i < Math.min(total, maxTotal); i += maxResult) {

            int finalI = i;
            int finalTotal = Math.min(total, maxTotal);
            logger.log(() -> System.out.printf("Getting Jira issues from %d to %d.\n", finalI, finalTotal));

            jiraIssueReport = issueJSONGetter.getIssueReport(i, Math.min(maxTotal - i, maxResult));

            total = jiraIssueReport.getTotal();

            result.addAll(jiraIssueReport.getIssues());
        }

        return result;
    }


}
