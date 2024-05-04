package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.entity.Entry;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class DatasetController {

    private static final Map<String, DatasetController> instances = new HashMap<>();
    private final String projName;
    private final MyLogger logger = MyLogger.getInstance(this.getClass());


    private DatasetController(String projName) {
        this.projName = projName;
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
     * @throws GitAPIException uses <code>GitController</code>
     */
    public void populateDataset() throws GitAPIException {
        List<Entry> allDatasetEntries = BugginessController.getInstance(projName).getAllLabelledEntries();

        logger.log(() -> System.out.printf("Total entries size: %d\n", allDatasetEntries.size()));

        MetricsController.getInstance(projName).setMetricsForAllEntries(allDatasetEntries);

        // write all the found entries on the .csv files
        CsvEntryBoundary toCsv = new CsvEntryBoundary(projName);
        toCsv.writeEntries(allDatasetEntries);
    }
}
