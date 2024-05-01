package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.JiraVersion;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class VersionsController {

    private static final Map<String, VersionsController> instances = new HashMap<>();

    private final String projName;
    private final ProjectController pc;
    private final GitController gc;
    private List<String> allTags;
    private List<JiraVersion> allJiraVersions;
    private List<Version> allVersions;
    private List<Version> allReleasedVersions;
    private List<Version> allValidVersions;

    private VersionsController(String projName) {
        this.projName = projName;
        this.pc = ProjectController.getInstance(projName);
        this.gc = GitController.getInstance(projName);
    }

    public static VersionsController getInstance(String projName) {
        VersionsController.instances.computeIfAbsent(projName, string -> new VersionsController(projName));
        return VersionsController.instances.get(projName);
    }

    /**
     * Get all tags from GitHub.
     *
     * @throws GitAPIException due to {@link GitController}
     */
    public void setAllTags() throws GitAPIException {
        if (this.allTags == null) {
            this.allTags = gc.listTags();
        }
    }

    /**
     * @return A list of the versions listed in Jira.
     */
    public List<JiraVersion> getAllJiraVersions() {
        if (this.allJiraVersions == null) {
            this.allJiraVersions = pc.getProject().getVersions().stream().toList();
        }
        return this.allJiraVersions;
    }

    /**
     * Cached instance is kept in {@code allVersions}.
     *
     * @return A list of all the versions listed in Jira, but the release date is set by their last tag on GitHub or is {@code null}.
     * @throws GitAPIException due to {@link GitController}
     */
    public List<Version> getAllVersions() throws GitAPIException {
        if (this.allVersions == null) {
            this.allVersions = new ArrayList<>();

            VersionFactory versionFactory = VersionFactory.getInstance(projName);

            for (JiraVersion jiraVersion : this.getAllJiraVersions()) {
                allVersions.add(versionFactory.versionFromJiraVersion(jiraVersion));
            }
        }
        return this.allVersions;
    }

    public List<Version> getAllReleasedVersions() throws GitAPIException {
        if (this.allReleasedVersions == null) {
            this.allReleasedVersions = this.getAllVersions()
                    .stream()
                    .filter(Version::isReleased)
                    .sorted(Comparator.comparing(Version::getJiraReleaseDate))
                    .toList();
        }
        return this.allReleasedVersions;
    }

    /**
     * A version is considered valid when a release date with GitHub has been found and
     * has been indicated as released on Jira.
     *
     * @return A list of all valid version present both on GitHub and Jira, with the release date set by the last commit on GitHub.
     * @throws GitAPIException due to {@link GitController}
     */
    public List<Version> getAllValidVersions() throws GitAPIException {
        if (this.allValidVersions == null) {
            this.allValidVersions = this.getAllReleasedVersions()
                    .stream()
                    .filter(version -> version.getGithubReleaseDate() != null)
                    .toList();
        }
        return this.allValidVersions;
    }

    /**
     * @return The list of oldest half valid versions.
     * @throws GitAPIException due to <code>GitController</code>
     */
    public List<Version> getHalfVersion() throws GitAPIException {
        // half the size
        int tmpSize = this.getAllValidVersions().size();
        return this.getAllValidVersions().subList(0, tmpSize / 2);
    }


}
