package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.model.Entry;
import it.gmarseglia.app.model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class DatasetController {

    private static final Map<String, DatasetController> instances = new HashMap<>();

    private final VersionsController vc;
    private final GitController gc;
    private final EntriesController ec;
    private final CsvEntryBoundary cb;
    private final MyLogger logger = MyLogger.getInstance(this.getClass());


    private DatasetController(String projName) {
        this.vc = VersionsController.getInstance(projName);
        this.gc = GitController.getInstance(projName);
        this.ec = new EntriesController();
        this.cb = new CsvEntryBoundary(projName);
    }

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
        MyLogger.setStaticVerbose(verbose);

        // Get the oldest half valid versions
        List<Version> halfVersions = this.vc.getHalfVersion();

        logger.log(() -> System.out.printf("Half valid version count: %d\n", halfVersions.size()));

        // for each version, get all the .java src files
        for (Version v : halfVersions) {
            appendEntriesForVersion(v);
            logger.log(() -> System.out.printf("Total count: %d\n", ec.getAllEntries().size()));
        }

        // write all the found entries on the .csv files
        cb.writeEntries(ec.getAllEntries());
    }

    /**
     * 1. Checks out to a specific version.
     * 2. Gets all the "*src*.java" files.
     * 3. Appends them using {@link EntriesController}.{@code findAndAppendEntries}
     *
     * @param version Version to check out.
     */
    private void appendEntriesForVersion(Version version) {
        try {
            logger.log(() -> System.out.printf("Release %s:", version.getName()));

            // checkout local Git dir at given version
            gc.checkoutByTag(version.getName());

            // get all the .jav src files in given version
            List<Entry> entriesByVersion = ec.findAndAppendEntries(gc.getLocalPath(), version);

            logger.log(() -> System.out.printf(" %d .java src files found.\n", entriesByVersion.size()));

        } catch (GitAPIException e) {
            logger.log(() -> System.out.print("\tnot found on Git.\n"));
        }
    }
}
