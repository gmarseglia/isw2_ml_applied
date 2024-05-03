package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MetricsController {
    private static final Map<String, MetricsController> instances = new HashMap<>();

    private final MyLogger logger = MyLogger.getInstance(MetricsController.class);
    private final String projName;

    private List<Version> halfVersions;

    private Entry targetEntry;
    private int currentVersionIndex;
    private Version previousVersion;
    private List<RevCommit> entryCommits;
    private List<RevCommit> entryLastVersionCommits;

    private MetricsController(String projName) {
        this.projName = projName;
    }

    public static MetricsController getInstance(String projName) {
        MetricsController.instances.computeIfAbsent(projName, MetricsController::new);
        return MetricsController.instances.get(projName);
    }

    public void setMetricsForAllEntries(List<Entry> cleanAllEntries) throws GitAPIException {

        this.halfVersions = VersionsController.getInstance(projName).getHalfVersion();

        for (Entry entry : cleanAllEntries) {

            // check out to the tag of the version of the entry
            GitController.getInstance(projName).checkoutByTag(entry.getVersion().getGithubTag());

            targetEntry = entry;

            currentVersionIndex = halfVersions.indexOf(targetEntry.getVersion());
            previousVersion = currentVersionIndex == 0 ? null : halfVersions.get(currentVersionIndex - 1);

            entryCommits = GitController.getInstance(projName).getRevCommitsFromPath(entry.getPath());

            entryLastVersionCommits = entryCommits
                    .stream()
                    .filter(revCommit -> revCommit.getAuthorIdent().getWhen().compareTo(targetEntry.getVersion().getGithubReleaseDate()) <= 0)
                    .filter(revCommit -> {
                        if (previousVersion != null) {
                            return revCommit.getAuthorIdent().getWhen().compareTo(previousVersion.getGithubReleaseDate()) > 0;
                        } else {
                            return true;
                        }
                    })
                    .toList();

            this.LOC();
            this.NR();
        }

    }

    private void LOC() {
        long LOC;

        // https://stackoverflow.com/questions/51639536/java-nio-files-count-method-for-counting-the-number-of-lines
        try (Stream<String> stream = Files.lines(targetEntry.getPath())) {
            LOC = stream.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        targetEntry.getMetrics().setLOC(LOC);
    }

    // computes the number of commits between versions
    private void NR() {
        long NR;

        NR = entryLastVersionCommits.size();

        targetEntry.getMetrics().setNR(NR);
    }
}
