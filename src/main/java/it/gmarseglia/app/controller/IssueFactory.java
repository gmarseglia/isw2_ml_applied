package it.gmarseglia.app.controller;

import it.gmarseglia.app.model.Issue;
import it.gmarseglia.app.model.JiraIssue;
import it.gmarseglia.app.model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IssueFactory {

    private static Map<String, IssueFactory> instances = new HashMap<>();
    private GitController gc;
    private DatasetController dc;

    public static IssueFactory getInstance(String projName) {
         IssueFactory.instances.computeIfAbsent(projName, string -> new IssueFactory(projName));
        return IssueFactory.instances.get(projName);
    }

    private IssueFactory(String projName){
        this.gc = GitController.getInstance(projName);
        this.dc = DatasetController.getInstance(projName);
    }

    public Issue issueFromJiraIssue(JiraIssue jiraIssue) throws GitAPIException, IOException {
        Version ov = dc.getAllValidVersions().stream()
                .filter(version -> version.getReleaseDate().after(jiraIssue.getFields().getCreated()))
                .findFirst()
                .orElse(null);

        Version fv = dc.getAllValidVersions().stream()
                .filter(version -> version.getReleaseDate().after(jiraIssue.getFields().getResolutiondate()))
                .findFirst()
                .orElse(null);

        Version iv;
        if (jiraIssue.getFields().getOldestAffectedVersion() == null) {
            iv = null;
        } else {
            iv = dc.getAllValidVersions().stream()
                    .filter(version -> version.getReleaseDate().after(jiraIssue.getFields().getOldestAffectedVersion().getReleaseDate()))
                    .findFirst()
                    .orElse(null);
        }

        return new Issue(jiraIssue.getKey(), ov, fv, iv);

    }


}
