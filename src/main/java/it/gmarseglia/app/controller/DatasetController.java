package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.model.Entry;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class DatasetController {

    private static final Map<String, DatasetController> instances = new HashMap<>();

    private final EntriesController ec;
    private final CsvEntryBoundary cb;
    private final MyLogger logger = MyLogger.getInstance(this.getClass());


    private DatasetController(String projName) {
        this.ec = EntriesController.getInstance(projName);
        this.cb = new CsvEntryBoundary(projName);
    }

    public static DatasetController getInstance(String projName) {
        DatasetController.instances.computeIfAbsent(projName, string -> new DatasetController(projName));
        return DatasetController.instances.get(projName);
    }

    /**
     * Populate the dataset. It:
     * 1. Gets the entries for half the versions.
     * 2. Writes those entries to the .csv file.
     *
     * @param verbose option
     * @throws GitAPIException uses <code>GitController</code>
     */
    public void populateDataset(boolean verbose) throws GitAPIException {
        MyLogger.setStaticVerbose(verbose);

        List<Entry> allDatasetEntries = ec.getAllEntriesForHalfVersions();

        logger.log(() -> System.out.printf("Total entries size: %d\n", allDatasetEntries.size()));

        // write all the found entries on the .csv files
        cb.writeEntries(allDatasetEntries);
    }
}
