package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.model.Entry;
import it.gmarseglia.app.model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public class DatasetController {

    private final String projName;
    private final ProjectController pc;
    private final GitController gc;
    private final EntriesController ec;
    private final CsvEntryBoundary cb;

    public DatasetController(String projName) {
        this.projName = projName;
        this.pc = new ProjectController(projName);
        this.gc = new GitController(projName);
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
        List<Version> allVersion = pc.getProject().getVersions();

        // Get all versions by tags on GitHub
        List<String> allTags = gc.listTags();

        // Filter only versions in both
        List<Version> validVersion = allVersion
                .stream()
                .filter(version -> allTags.contains(version.getName()))
                .toList();

        // half the size
        return validVersion.subList(0, validVersion.size()/2);
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
}
