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
    private final MyLogger logger = MyLogger.getInstance(this.getClass());
    private final GitController gc;
    private final VersionsController vc;
    private List<Entry> allEntries;

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
            // checkout local Git dir at given version
            gc.checkoutByTag(version.getGithubTag());

            // get all the .java src files in given version
            Path localPath = this.gc.getLocalPath();
            MyFileUtils.getAllJavaSrcFiles(localPath)
                    .forEach(p -> result.add(new Entry(p, version, p.toString().replace(localPath.toString(), ""))));

            logger.logFinest(() -> System.out.printf("Release %s: %d .java src files found.\n", version.getName(), result.size()));

        } catch (GitAPIException e) {
            logger.logFinest(() -> System.out.printf("Release %s: not found on Git.\n", version.getName()));
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
        if (this.allEntries == null) {
            this.allEntries = new ArrayList<>();

            for (Version v : vc.getHalfVersion()) {
                logger.logFinest(() -> System.out.printf("Getting entries for version %s.\n", v.getName()));
                this.allEntries.addAll(this.findAndAppendEntries(v));
                logger.logFinest(() -> System.out.printf("allEntries size: %d.\n", this.allEntries.size()));
            }

            logger.logFine(() -> System.out.println("allEntries.size(): " + this.allEntries.size()));
        }
        return this.allEntries;
    }
}
