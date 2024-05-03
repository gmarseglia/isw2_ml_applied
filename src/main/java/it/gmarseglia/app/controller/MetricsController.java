package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
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
    private Map<RevCommit, DiffEntry> commitAndDiffAll;

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

            // assign targetEntry
            targetEntry = entry;

            // compute index
            currentVersionIndex = halfVersions.indexOf(targetEntry.getVersion());

            // compute previous version
            previousVersion = currentVersionIndex == 0 ? null : halfVersions.get(currentVersionIndex - 1);

            // get all commits for the path corresponding to the entry
            entryCommits = GitController.getInstance(projName).getRevCommitsFromPath(entry.getPath());

            // get all commits for the path and the version
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

            // for each commit get all the diffs
            Map<RevCommit, List<DiffEntry>> tmpCommitAndDiffs = new HashMap<>();
            for (RevCommit commit : entryLastVersionCommits) {
                List<DiffEntry> commitDiffEntries = GitController.getInstance(projName).getDiffListByRevCommit(commit);
                tmpCommitAndDiffs.put(commit, commitDiffEntries);
            }

            // follow filename through commits and assign correct diff to each commit
            this.commitAndDiffAll = new HashMap<>();
            String mostRecentPath = entry.getLongName().substring(1);
            boolean addFound = false;

            for (RevCommit commit : tmpCommitAndDiffs.keySet()) {
                if (addFound) break;

                for (DiffEntry diffEntry : tmpCommitAndDiffs.get(commit)) {
                    if (mostRecentPath.equals(diffEntry.getNewPath())) {
                        mostRecentPath = diffEntry.getOldPath();
                    }
                    if (mostRecentPath.equals(diffEntry.getOldPath())) {
                        this.commitAndDiffAll.put(commit, diffEntry);

                        if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD)
                            addFound = true;
                        break;
                    }
                }
            }

            logger.logFinest(() -> System.out.println("commitAndDiffAll for " + entry.getVersion().getName() + ": " + this.commitAndDiffAll));

            this.LOC();
            this.NR();
            this.NAuth();
            this.LOCAdded();
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
        long NR = entryLastVersionCommits.size();

        targetEntry.getMetrics().setNR(NR);
    }

    private void NAuth() {
        long NAuth = entryLastVersionCommits.stream()
                .map(revCommit -> revCommit.getAuthorIdent().getName())
                .toList()
                .stream()
                .distinct()
                .count();

        targetEntry.getMetrics().setNAuth(NAuth);
    }

    private void LOCAdded() throws GitAPIException {
        long LOCAdded = 0;

        for (RevCommit commit : this.commitAndDiffAll.keySet()) {
            DiffEntry diffEntry = this.commitAndDiffAll.get(commit);
            LOCAdded += GitController.getInstance(projName).getLOCModifiedByDiff(diffEntry)[0];
        }

        targetEntry.getMetrics().setLOCAdded(LOCAdded);
    }
}
