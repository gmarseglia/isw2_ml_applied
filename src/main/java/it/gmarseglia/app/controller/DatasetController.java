package it.gmarseglia.app.controller;


import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetController {

    private static final Map<String, DatasetController> instances = new HashMap<>();
    private final String projName;
    private final MyLogger logger = MyLogger.getInstance(this.getClass());
    private boolean computeMetrics = true;


    private DatasetController(String projName) {
        this.projName = projName;
    }

    public static DatasetController getInstance(String projName) {
        DatasetController.instances.computeIfAbsent(projName, string -> new DatasetController(projName));
        return DatasetController.instances.get(projName);
    }

    public void setComputeMetrics(boolean computeMetrics) {
        this.computeMetrics = computeMetrics;
    }

    /**
     * Populate the dataset. It:
     * 1. Gets the entries for half the versions.
     * 2. Writes those entries to the .csv file.
     *
     * @throws GitAPIException uses <code>GitController</code>
     */
    public void populateDataset() throws GitAPIException {
        VersionsController vc = VersionsController.getInstance(projName);
        BugginessController bc = BugginessController.getInstance(projName);
        EntriesController ec = EntriesController.getInstance(projName);

        List<Entry> allDatasetEntries = ec.getAllEntriesForHalfVersions();

        logger.log(String.format("Total entries size: %d", allDatasetEntries.size()));

        if (computeMetrics) {
            logger.log("Ready to compute metrics.");
            MetricsController.getInstance(projName).setMetricsForAllEntries(allDatasetEntries);
        }

        logger.log("Ready to create datasets");
        bc.setAllMetricsEntries(allDatasetEntries);

        Path datasetsPath = Path.of(ToFileBoundary.DEFAULT_OUT_DIR.toString(), projName, "datasets");

        for (Version v : vc.getHalfVersion()) {
            List<Entry> perVersionTrainingSet = bc.getAllLabelledEntriesToObservationDate(v.getJiraReleaseDate());

            // write all the found entries on the .csv files
            ToFileBoundary.writeList(perVersionTrainingSet,
                    datasetsPath,
                    v.getName() + "_Trainingset.csv");
        }

        List<Entry> finalDataset = bc.getAllLabelledEntriesToObservationDate(null);

        // write all the found entries on the .csv files
        ToFileBoundary.writeList(finalDataset,
                datasetsPath,
                "final_dataset.csv");

        for (Version v : vc.getHalfVersion()) {
            List<Entry> perVersionTestingSet = finalDataset
                    .stream()
                    .filter(entry -> entry.getVersion().equals(v))
                    .toList();

            ToFileBoundary.writeList(perVersionTestingSet,
                    datasetsPath,
                    v.getName() + "_testingset.csv");
        }
    }
}
