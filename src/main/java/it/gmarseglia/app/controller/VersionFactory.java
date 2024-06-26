package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.JiraVersion;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VersionFactory {

    private static final Map<String, VersionFactory> instances = new HashMap<>();
    private final GitController gc;

    private VersionFactory(String projName) {
        this.gc = GitController.getInstance(projName);
    }

    public static VersionFactory getInstance(String projName) {
        VersionFactory.instances.computeIfAbsent(projName, string -> new VersionFactory(projName));
        return VersionFactory.instances.get(projName);
    }

    /**
     * Sets the date of the version by using GitHub information over Jira information.
     *
     * @param jiraVersion Version from Jira.
     * @return Version with release date set using GitHub commits or {@code null}.
     * @throws GitAPIException due to {@link GitController}
     */
    public Version versionFromJiraVersion(JiraVersion jiraVersion) throws GitAPIException {
        // get date by GitHub
        Date gitDate = gc.getVersionGitDate(jiraVersion);
        String gitTag = gc.getTagByNameRegex(jiraVersion.getName());
        return new Version(
                jiraVersion.getId(),
                jiraVersion.isReleased(),
                jiraVersion.getName(),
                jiraVersion.getReleaseDate(),
                gitDate,
                gitTag);
    }
}
