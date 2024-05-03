package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MetricsController {
    private static final Map<String, MetricsController> instances = new HashMap<>();

    private final MyLogger logger = MyLogger.getInstance(MetricsController.class);
    private final GitController gc;
    private final String projName;

    private MetricsController(String projName) {
        this.projName = projName;
        this.gc = GitController.getInstance(projName);
    }

    public static MetricsController getInstance(String projName) {
        MetricsController.instances.computeIfAbsent(projName, MetricsController::new);
        return MetricsController.instances.get(projName);
    }

    public void setMetricsForAllEntries(List<Entry> cleanAllEntries) throws GitAPIException {

        for (Entry target : cleanAllEntries) {

            target.getMetrics().setLOC(this.computeLOC(target));


        }

    }

    private long computeLOC(Entry entry) throws GitAPIException {
        long LOC = 0;

        gc.checkoutByTag(entry.getVersion().getGithubTag());

        // https://stackoverflow.com/questions/51639536/java-nio-files-count-method-for-counting-the-number-of-lines
        try (Stream<String> stream = Files.lines(entry.getPath())) {
            LOC = stream.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return LOC;
    }
}
