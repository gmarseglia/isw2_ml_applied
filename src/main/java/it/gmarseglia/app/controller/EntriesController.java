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

            logger.logFinest(String.format("Release %s: %d .java src files found.", version.getName(), result.size()));

        } catch (GitAPIException e) {
            logger.logFinest(String.format("Release %s: not found on Git.", version.getName()));
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

            List<Version> halfVersion = vc.getHalfVersion();
            List<String> halfVersionNames = halfVersion.stream().map(Version::getName).toList();

            logger.log(String.format("Ready to find .java src files for for the least recent half valid versions: %s" , halfVersionNames));

            for (Version v : halfVersion) {
                logger.logFinest(String.format("Getting entries for version %s.", v.getName()));
                List<Entry> perVersionEntries = this.findAndAppendEntries(v);
                this.allEntries.addAll(perVersionEntries);
                logger.logFine(String.format("Found %d .java src entries for version %s.", perVersionEntries.size(), v.getName()));
            }

            logger.log(String.format("Found %d .java src entries for the least recent half valid versions.", this.allEntries.size()));
        }
        return this.allEntries;
    }
}
