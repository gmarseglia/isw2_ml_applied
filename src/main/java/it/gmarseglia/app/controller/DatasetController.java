package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.model.Entry;
import it.gmarseglia.app.model.JiraVersion;
import it.gmarseglia.app.model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class DatasetController {

    private static final Map<String, DatasetController> instances = new HashMap<>();

    private final String projName;
    private final ProjectController pc;
    private final GitController gc;
    private final EntriesController ec;
    private final CsvEntryBoundary cb;

    private List<String> allTags = null;
    private List<JiraVersion> allJiraVersions = null;
    private List<Version> allVersions = null;
    private List<Version> allValidVersions = null;

    /**
     * Private constructor
     *
     * @param projName project name
     */
    private DatasetController(String projName) {
        this.projName = projName;
        this.pc = new ProjectController(projName);
        this.gc = GitController.getInstance(projName);
        this.ec = new EntriesController();
        this.cb = new CsvEntryBoundary(projName);
    }

    /**
     * Singleton
     *
     * @param projName project name
     * @return singleton instance
     */
    public static DatasetController getInstance(String projName) {
        DatasetController.instances.computeIfAbsent(projName, string -> new DatasetController(projName));
        return DatasetController.instances.get(projName);
    }

    /**
     * Populate the dataset. It:
     * 1. takes all the versions from Jira.
     * 2. tries to check out each version using GitHub tickets.
     * If the version is not present, then it's not considered "valid".
     * 3. gets the last commit date and assigns it to the version.
     * Here the version becomes <code>Version.java</code> from <code>JiraVersion.java</code>.
     * 4. discards the latest half versions.
     * 5. finds all '*src*.java' files in the repo, after check out to each valid version.
     * 5. writes those entries to the .csv file.
     *
     * @param verbose option
     * @throws GitAPIException uses <code>GitController</code>
     */
    public void populateDataset(boolean verbose) throws GitAPIException {
        // Get the oldest half valid versions
        List<Version> halfVersions = this.getHalfVersion();

        if (verbose) System.out.printf("Half valid version count: %d\n", halfVersions.size());

        // for each version, get all the .java src files
        for (Version v : halfVersions) {
            appendEntriesForVersion(v, verbose);
            if (verbose) System.out.printf("Total count: %d\n", ec.getAllEntries().size());
        }

        // write all the found entries on the .csv files
        cb.writeEntries(ec.getAllEntries());
    }

    /**
     * @return The list of oldest half valid versions.
     * @throws GitAPIException due to <code>GitController</code>
     */
    private List<Version> getHalfVersion() throws GitAPIException {
        // Get all versions on Jira
        getAllJiraVersions();

        // get all versions by merging data with GitHub
        getAllVersions();

        // Get all versions by tags on GitHub
        setAllTags();

        // Filter only versions in both
        long lost = this.allJiraVersions.stream().filter(version -> allTags.contains(version.getName())).filter(version -> version.getReleaseDate() == null).count();

        System.out.printf("Lost %d valid versions due to \"missing releaseDate\".\n", lost);

        // get all valid versions
        getAllValidVersions();

        // half the size
        return this.allValidVersions.subList(0, this.allValidVersions.size() / 2);
    }

    /**
     * 1. Checks out to a specific version.
     * 2. Gets all the "*src*.java" files.
     * 3. Appends them using {@link EntriesController}.{@code findAndAppendEntries}
     *
     * @param version Version to check out.
     * @param verbose option
     */
    private void appendEntriesForVersion(Version version, boolean verbose) {
        try {
            if (verbose) System.out.printf("Release %s:", version.getName());

            // checkout local Git dir at given version
            gc.checkoutByTag(version.getName());

            // get all the .jav src files in given version
            List<Entry> entriesByVersion = ec.findAndAppendEntries(gc.getLocalPath(), version);

            if (verbose) System.out.printf(" %d .java src files found.\n", entriesByVersion.size());

        } catch (GitAPIException e) {
            if (verbose) System.out.print("\tnot found on Git.\n");
        }
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

    /**
     * @return A list of all valid version present both on GitHub and Jira, with the release date set by the last commit on GitHub.
     * @throws GitAPIException due to {@link GitController}
     */
    public List<Version> getAllValidVersions() throws GitAPIException {
        if (this.allValidVersions == null) {
            this.setAllTags();
            this.allValidVersions = this.getAllVersions().stream().filter(version -> this.allTags.contains(version.getName())).filter(version -> version.getReleaseDate() != null).sorted(Comparator.comparing(Version::getReleaseDate)).toList();
        }
        return this.allValidVersions;
    }
}
