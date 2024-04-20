package it.gmarseglia.app.controller;

import it.gmarseglia.app.boundary.IssueJSONGetter;
import it.gmarseglia.app.model.Issue;
import it.gmarseglia.app.model.JiraIssue;
import it.gmarseglia.app.model.JiraIssueReport;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueController {

    private static final Map<String, IssueController> instances = new HashMap<>();

    private final IssueJSONGetter issueJSONGetter;
    private final IssueFactory issueFactory;
    private final List<JiraIssue> allJiraIssues = new ArrayList<>();


    public static IssueController getInstance(String projName){
        IssueController.instances.computeIfAbsent(projName, IssueController::new);
        return IssueController.instances.get(projName);
    }

    private IssueController(String projName){
        this.issueJSONGetter = new IssueJSONGetter(projName);
        this.issueFactory = IssueFactory.getInstance(projName);
    }

    public List<Issue> getAllIssues(boolean verbose) throws GitAPIException, IOException {
        // get all Jira issues
        this.getAllJiraIssues(verbose);

        List<Issue> result = new ArrayList<>();

        for (JiraIssue jiraIssue : this.allJiraIssues){
            result.add(this.issueFactory.issueFromJiraIssue(jiraIssue));
        }

        return result;
    }

    private void getAllJiraIssues(boolean verbose) {
        JiraIssueReport jiraIssueReport;

        int maxResult = 1000;
        int total = maxResult;
        int maxTotal = Integer.MAX_VALUE;

        for (int i = 0; i < Math.min(total, maxTotal); i += maxResult){
            System.out.printf("Getting Jira issues from %d to %d.\n", i, Math.min(total, maxTotal));
            jiraIssueReport = issueJSONGetter.getIssueReport(i, Math.min(maxTotal - i, maxResult));
            total = jiraIssueReport.getTotal();
            this.allJiraIssues.addAll(jiraIssueReport.getIssues());
        }
    }


}
