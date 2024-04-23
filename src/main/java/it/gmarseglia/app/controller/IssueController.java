package it.gmarseglia.app.controller;

import it.gmarseglia.app.boundary.IssueJSONGetter;
import it.gmarseglia.app.model.Issue;
import it.gmarseglia.app.model.JiraIssue;
import it.gmarseglia.app.model.JiraIssueReport;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueController {

    private static final Map<String, IssueController> instances = new HashMap<>();

    private final IssueJSONGetter issueJSONGetter;
    private final IssueFactory issueFactory;
    private final List<JiraIssue> allJiraIssues = new ArrayList<>();
    private final MyLogger logger;

    private IssueController(String projName) {
        this.issueJSONGetter = new IssueJSONGetter(projName);
        this.issueFactory = IssueFactory.getInstance(projName);
        this.logger = MyLogger.getInstance(this.getClass());
    }

    public static IssueController getInstance(String projName) {
        IssueController.instances.computeIfAbsent(projName, IssueController::new);
        return IssueController.instances.get(projName);
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

        // get all Jira issues
        this.getJiraIssues(maxTotal);

        for (JiraIssue jiraIssue : this.allJiraIssues) {
            result.add(this.issueFactory.issueFromJiraIssue(jiraIssue));
        }

        return result;
    }

    /**
     * Call multiple time {@link IssueJSONGetter} to obtain all the Jira issues.
     * {@code allJiraIssues} holds the list.
     */
    private void getJiraIssues(int maxTotal) {
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
