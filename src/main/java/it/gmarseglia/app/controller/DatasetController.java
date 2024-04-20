package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.model.Entry;
import it.gmarseglia.app.model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetController {

    private static final Map<String, DatasetController> instances = new HashMap<>();

    private final String projName;
    private final ProjectController pc;
    private final GitController gc;
    private final EntriesController ec;
    private final CsvEntryBoundary cb;

    private List<String> allTags = null;
    private List<Version> allVersions = null;
    private List<Version> allValidVersions = null;

    public static DatasetController getInstance(String projName){
        DatasetController.instances.computeIfAbsent(projName,string -> new DatasetController(projName));
        return DatasetController.instances.get(projName);
    }

    private DatasetController(String projName) {
        this.projName = projName;
        this.pc = new ProjectController(projName);
        this.gc = GitController.getInstance(projName);
        this.ec = new EntriesController();
        this.cb = new CsvEntryBoundary(projName);
    }

    public void populateDataset(boolean verbose) throws GitAPIException, IOException {
        // Get the oldest half valid versions
        List<Version> halfVersions = this.getHalfVersion();

        if (verbose) System.out.printf("Half valid version count: %d\n", halfVersions.size());

        // for each version, get all the .java src files
        for (Version v: halfVersions) {
            appendEntriesForVersion(v, verbose);
            if (verbose) System.out.printf("Total count: %d\n", ec.getAllEntries().size());
        }

        // write all the found entries on the .csv files
        cb.writeEntries(ec.getAllEntries());
    }

    private List<Version> getHalfVersion () throws GitAPIException, IOException {
        // Get all versions on Jira
        getAllVersions();

        // Get all versions by tags on GitHub
        getAllTags();

        // Filter only versions in both
        long lost = this.allVersions.stream()
                .filter(version -> allTags.contains(version.getName()))
                .filter(version -> version.getReleaseDate() == null)
                .count();

        System.out.printf("Lost %d valid versions due to \"missing releaseDate\".\n", lost);

        // get all valid versions
        getAllValidVersions();

        // half the size
        return this.allValidVersions.subList(0, this.allValidVersions.size()/2);
    }

    private void appendEntriesForVersion(Version version, boolean verbose) throws IOException {
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

    public List<String> getAllTags() throws GitAPIException, IOException {
        if (this.allTags == null) {
            this.allTags = gc.listTags();
        }
        return this.allTags;
    }

    public List<Version> getAllVersions() {
        if (this.allVersions == null) {
            this.allVersions = pc.getProject().getVersions()
                    .stream()
                    .toList();
        }
        return this.allVersions;
    }

    public List<Version> getAllValidVersions() throws GitAPIException, IOException {
        if (this.allValidVersions == null) {
            this.getAllTags();
            this.allValidVersions = this.getAllVersions()
                    .stream()
                    .filter(version -> this.allTags.contains(version.getName()))
                    .filter(version -> version.getReleaseDate() != null)
                    .sorted(Comparator.comparing(Version::getReleaseDate))
                    .toList();
        }
        return this.allValidVersions;
    }
}
