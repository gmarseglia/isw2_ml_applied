package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.CsvBoundary;
import it.gmarseglia.app.entity.Entry;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        logger.log(String.format("Total entries size: %d", allDatasetEntries.size()));

        MetricsController.getInstance(projName).setMetricsForAllEntries(allDatasetEntries);

        // write all the found entries on the .csv files
        CsvBoundary.writeListProj(allDatasetEntries, projName , "dataset.csv");
    }
}
