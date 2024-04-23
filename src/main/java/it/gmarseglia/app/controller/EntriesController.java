package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntriesController {

    private static final Map<String, EntriesController> instances = new HashMap<>();

    private final List<Entry> allEntries = new ArrayList<>();
    private final MyLogger logger = MyLogger.getInstance(this.getClass());
    private final GitController gc;
    private final VersionsController vc;

    private EntriesController(String projName) {
        this.gc = GitController.getInstance(projName);
        this.vc = VersionsController.getInstance(projName);
    }

    public static EntriesController getInstance(String projName) {
        instances.computeIfAbsent(projName, string -> new EntriesController(projName));
        return instances.get(projName);
    }

    /**
     * 1. Checks out to a specific version.
     * 2. Gets all the "*src*.java" files.
     *
     * @param version Version to label the entries.
     * @return List of entries (filename, version) for given version.
     */
    public List<Entry> findAndAppendEntries(Version version) {
        List<Entry> result = new ArrayList<>();

        try {
            logger.log(() -> System.out.printf("Release %s: ", version.getName()));

            // checkout local Git dir at given version
            gc.checkoutByTag(version.getName());

            // get all the .java src files in given version
            Path localPath = this.gc.getLocalPath();
            MyFileUtils.getAllJavaSrcFiles(localPath)
                    .forEach(p -> result.add(new Entry(p, version, p.toString().replace(localPath.toString(), ""))));

            logger.log(() -> System.out.printf("%d .java src files found.\n", result.size()));

        } catch (GitAPIException e) {
            logger.log(() -> System.out.print("not found on Git.\n"));
        }

        return result;
    }

    /**
     * 1. Gets half the versions.
     * 2. Appends entries for each every version
     *
     * @return The entries for every version.
     */
    public List<Entry> getAllEntriesForHalfVersions() throws GitAPIException {
        List<Entry> result = new ArrayList<>();

        vc.getHalfVersion()
                .forEach(version -> result.addAll(this.findAndAppendEntries(version)));

        return result;
    }
}
