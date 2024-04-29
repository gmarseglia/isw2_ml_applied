package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.JiraIssue;
import it.gmarseglia.app.entity.JiraVersion;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueFactory {

    private static final Map<String, IssueFactory> instances = new HashMap<>();
    private final VersionsController vc;

    private IssueFactory(String projName) {
        this.vc = VersionsController.getInstance(projName);
    }

    public static IssueFactory getInstance(String projName) {
        IssueFactory.instances.computeIfAbsent(projName, string -> new IssueFactory(projName));
        return IssueFactory.instances.get(projName);
    }

    /**
     * The date present on Jira are compared to the valid versions obtained earlier.
     * <p>
     * The creation date on Jira is used to set the OV.
     * The resolution date on Jira is used to set the FV.
     * The release date of the oldest version in AffectsVersion (if present) is used to set the IV.
     *
     * @param jiraIssue Version from Jira.
     * @return Version with OV, FV and IV set to valid versions.
     * @throws GitAPIException due to {@link GitController}
     */
    public Issue issueFromJiraIssue(JiraIssue jiraIssue) throws GitAPIException {
        Version ov;
        boolean isFirstOV;
        Version fv;
        Version iv;
        boolean hasExplicitIV;

        ov = vc.getAllReleasedVersions()
                .stream()
                .filter(version -> version.getJiraReleaseDate().after(jiraIssue.getFields().getCreated()))
                .findFirst()
                .orElse(null);

        isFirstOV = vc.getAllReleasedVersions().getFirst().equals(ov);

        fv = vc.getAllReleasedVersions()
                .stream()
                .filter(version -> version.getJiraReleaseDate().after(jiraIssue.getFields().getResolutiondate()))
                .findFirst()
                .orElse(null);

        if (jiraIssue.getFields().getOldestAffectedVersion() == null) {
            hasExplicitIV = false;
            iv = null;
        } else {
            hasExplicitIV = true;

            List<String> idsOfAffectsVersion = jiraIssue.getFields().getVersions().stream().map(JiraVersion::getId).toList();
            
            iv = vc.getAllReleasedVersions()
                    .stream()
                    .filter(version ->
                            idsOfAffectsVersion.contains(version.getId()))
                    .findFirst()
                    .orElse(null);

            if (iv == null) {
                iv = vc.getAllReleasedVersions()
                        .stream()
                        .filter(version ->
                                version.getJiraReleaseDate().after(jiraIssue.getFields().getOldestAffectedVersion().getReleaseDate()))
                        .findFirst()
                        .orElse(null);
            }
        }

        return new Issue(jiraIssue.getKey(),
                ov, isFirstOV,
                fv,
                iv, hasExplicitIV,
                jiraIssue.getFields().getCreated(),
                jiraIssue.getFields().getResolutiondate());
    }
}
