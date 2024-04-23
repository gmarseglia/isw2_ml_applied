package it.gmarseglia.app.entity;

import java.util.List;

public class JiraIssueReport {

    private List<JiraIssue> issues;
    private int total;

    public JiraIssueReport(List<JiraIssue> issues, int total) {
        this.issues = issues;
        this.total = total;
    }

    public List<JiraIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<JiraIssue> issues) {
        this.issues = issues;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
