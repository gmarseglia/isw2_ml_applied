package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.JiraVersion;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class VersionsController {

    private static final Map<String, VersionsController> instances = new HashMap<>();

    private final MyLogger logger = MyLogger.getInstance(VersionsController.class);
    private final String projName;
    private final ProjectController pc;
    private List<JiraVersion> allJiraVersions;
    private List<Version> allVersions;
    private List<Version> allReleasedVersions;
    private List<Version> allValidVersions;
    private List<Version> halfValidVersions;

    private VersionsController(String projName) {
        this.projName = projName;
        this.pc = ProjectController.getInstance(projName);
    }

    public static VersionsController getInstance(String projName) {
        VersionsController.instances.computeIfAbsent(projName, string -> new VersionsController(projName));
        return VersionsController.instances.get(projName);
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

            logger.logFine(String.format("Found %d versions: %s",
                    this.allVersions.size(),
                    this.allVersions.stream().map(Version::getName).toList()));
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
            logger.logFine(String.format("Found %d released versions: %s",
                    this.allReleasedVersions.size(),
                    this.allReleasedVersions.stream().map(Version::getName).toList()));
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
            logger.logFine(String.format("Found %d valid versions: %s",
                    this.allValidVersions.size(),
                    this.allValidVersions.stream().map(Version::getName).toList()));
        }
        return this.allValidVersions;
    }

    /**
     * @return The list of oldest half valid versions.
     * @throws GitAPIException due to <code>GitController</code>
     */
    public List<Version> getHalfVersion() throws GitAPIException {
        if (this.halfValidVersions == null) {
            int tmpSize = this.getAllValidVersions().size();
            this.halfValidVersions = this.getAllValidVersions().subList(0, tmpSize / 2);

            logger.logFine(String.format("Found %d half valid versions: %s",
                    this.halfValidVersions.size(),
                    this.halfValidVersions.stream().map(Version::getName).toList()));
        }
        return this.halfValidVersions;
    }


}
