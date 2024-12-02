package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Version;
import it.gmarseglia.app.exceptions.CustomRuntimeException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class MetricsController {
    private static final Map<String, MetricsController> instances = new HashMap<>();

    private final MyLogger logger = MyLogger.getInstance(MetricsController.class);
    private final String projName;

    private List<Version> halfVersions;

    private Entry targetEntry;
    private Version previousVersion;
    private List<RevCommit> entryLastVersionCommits;
    private Map<RevCommit, DiffEntry> commitAndDiffAll;
    private Map<RevCommit, List<DiffEntry>> commitAndDiffListAll;

    private MetricsController(String projName) {
        this.projName = projName;
    }

    public static MetricsController getInstance(String projName) {
        MetricsController.instances.computeIfAbsent(projName, MetricsController::new);
        return MetricsController.instances.get(projName);
    }

    public void setMetricsForAllEntries(List<Entry> cleanAllEntries) throws GitAPIException {
        List<RevCommit> entryCommits;
        int currentVersionIndex;

        logger.log("Ready to compute " + (cleanAllEntries.getFirst().getFieldsNames().size() - 3) + " metrics for " + cleanAllEntries.size() + " entries.");

        this.halfVersions = VersionsController.getInstance(projName).getHalfVersion();

        long i = 0;
        long percentage = 5;
        long expectedSize = cleanAllEntries.size();
        Instant begin = Instant.now();

        boolean useStepMetrics = ConfigsController.getPropertyUseStepMetrics();

        for (Entry entry : cleanAllEntries) {

            if (logger.getAnyVerboseFine()) {
                String logMsg;
                if ((++i * 100 / expectedSize) > percentage) {
                    long minutes = ChronoUnit.MINUTES.between(begin, Instant.now());
                    if (minutes == 0) {
                        long seconds = ChronoUnit.SECONDS.between(begin, Instant.now());
                        logMsg = "Computed metrics for " + percentage + "% of all entries in " + seconds + " seconds.";
                    } else {
                        logMsg = "Computed metrics for " + percentage + "% of all entries in " + minutes + " minutes.";
                    }
                    percentage += 5;
                    logger.logFine(logMsg);
                }
            }

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

            // option to toggle between step and storic metric

            // get all commits for the path and the version
            entryLastVersionCommits = entryCommits
                    .stream()
                    .filter(revCommit -> revCommit.getAuthorIdent().getWhen().compareTo(targetEntry.getVersion().getJiraReleaseDate()) <= 0)
                    .filter(revCommit -> {
                        if (useStepMetrics && previousVersion != null) {
                            return revCommit.getAuthorIdent().getWhen().compareTo(previousVersion.getJiraReleaseDate()) > 0;
                        } else {
                            return true;
                        }
                    })
                    .toList();

            // for each commit get all the diffs
            this.commitAndDiffListAll = new LinkedHashMap<>();
            for (RevCommit commit : entryLastVersionCommits) {
                List<DiffEntry> commitDiffEntries = GitController.getInstance(projName).getDiffListByRevCommit(commit);
                this.commitAndDiffListAll.put(commit, commitDiffEntries);
            }

            // follow filename through commits and assign correct diff to each commit
            this.commitAndDiffAll = new LinkedHashMap<>();
            String mostRecentPath = entry.getLongName().substring(1);
            boolean addFound = false;

            for (Map.Entry<RevCommit, List<DiffEntry>> commitAndDiff : this.commitAndDiffListAll.entrySet()) {
                if (addFound) break;

                for (DiffEntry diffEntry : commitAndDiff.getValue()) {
                    if (mostRecentPath.equals(diffEntry.getNewPath())) {
                        mostRecentPath = diffEntry.getOldPath();
                    }
                    if (mostRecentPath.equals(diffEntry.getOldPath())) {
                        this.commitAndDiffAll.put(commitAndDiff.getKey(), diffEntry);

                        if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD)
                            addFound = true;
                        break;
                    }
                }
            }

            this.getLOC();
            this.getAge();
            this.getNR();
            this.getNAuth();
            this.getLOCAdded();
            this.getChurn();
            this.getChangeSetSize();
        }

        logger.log("Computed all metrics for " + cleanAllEntries.size() + " entries.");
    }

    private void getLOC() {
        long loc;

        // https://stackoverflow.com/questions/51639536/java-nio-files-count-method-for-counting-the-number-of-lines
        try (Stream<String> stream = Files.lines(targetEntry.getPath())) {
            loc = stream.count();
        } catch (IOException e) {
            throw new CustomRuntimeException(e);
        }

        targetEntry.getMetrics().setLoc(loc);
    }

    private void getAge() {
        long age;
        long stepAge;

        Date firstCommitDate = halfVersions.getFirst().getGithubReleaseDate();
        Date versionGithubReleaseDate = targetEntry.getVersion().getGithubReleaseDate();
        Date previousVersionGithubReleaseDate = previousVersion == null ? firstCommitDate : previousVersion.getGithubReleaseDate();

        age = ChronoUnit.DAYS.between(firstCommitDate.toInstant(), versionGithubReleaseDate.toInstant());
        stepAge = ChronoUnit.DAYS.between(previousVersionGithubReleaseDate.toInstant(), versionGithubReleaseDate.toInstant());

        targetEntry.getMetrics().setAge(age);
        targetEntry.getMetrics().setStepAge(stepAge);
    }

    // computes the number of commits between versions

    private void getNR() {
        long nr = entryLastVersionCommits.size();

        targetEntry.getMetrics().setNr(nr);
    }

    private void getNAuth() {
        long nAuth = entryLastVersionCommits.stream()
                .map(revCommit -> revCommit.getAuthorIdent().getName())
                .toList()
                .stream()
                .distinct()
                .count();

        targetEntry.getMetrics().setnAuth(nAuth);
    }

    private void getLOCAdded() throws GitAPIException {
        long lOCAdded = 0;
        long maxLOCAdded = 0;
        long avgLOCAdded = 0;
        long entries = 0;

        for (Map.Entry<RevCommit, DiffEntry> commitAndDiff : this.commitAndDiffAll.entrySet()) {
            DiffEntry diffEntry = commitAndDiff.getValue();
            long perCommitLOCAdded = GitController.getInstance(projName).getLOCModifiedByDiff(diffEntry)[0];
            lOCAdded += perCommitLOCAdded;
            avgLOCAdded = avgLOCAdded + (long) ((1.0F / ++entries) * (perCommitLOCAdded - avgLOCAdded));
            if (perCommitLOCAdded > maxLOCAdded) maxLOCAdded = perCommitLOCAdded;
        }

        targetEntry.getMetrics().setLocAdded(lOCAdded);
        targetEntry.getMetrics().setAvgLOCAdded(avgLOCAdded);
        targetEntry.getMetrics().setMaxLOCAdded(maxLOCAdded);
    }

    private void getChurn() throws GitAPIException {
        long churn = 0;
        long maxChurn = 0;
        long avgChurn = 0;
        long entries = 0;

        for (Map.Entry<RevCommit, DiffEntry> commitAndDiff : this.commitAndDiffAll.entrySet()) {
            DiffEntry diffEntry = commitAndDiff.getValue();
            long perCommitLOCAdded = GitController.getInstance(projName).getLOCModifiedByDiff(diffEntry)[0];
            long perCommitLOCDeleted = GitController.getInstance(projName).getLOCModifiedByDiff(diffEntry)[1];
            long perCommitChurn = perCommitLOCAdded + perCommitLOCDeleted;
            churn += perCommitChurn;
            avgChurn = avgChurn + (long) ((1.0F / ++entries) * (perCommitChurn - avgChurn));
            if (perCommitChurn > maxChurn) maxChurn = perCommitChurn;
        }

        targetEntry.getMetrics().setChurn(churn);
        targetEntry.getMetrics().setAvgChurn(avgChurn);
        targetEntry.getMetrics().setMaxChurn(maxChurn);
    }

    private void getChangeSetSize() {
        long changeSetSize = 0;
        long maxChangeSetSize = 0;
        long avgChangeSetSize = 0;
        long entries = 0;

        for (Map.Entry<RevCommit, List<DiffEntry>> commitAndDiff : this.commitAndDiffListAll.entrySet()) {
            long perCommitChangeSetSize = commitAndDiff.getValue().size();
            changeSetSize += perCommitChangeSetSize;
            avgChangeSetSize = avgChangeSetSize + (long) ((1.0F / ++entries) * (perCommitChangeSetSize - avgChangeSetSize));
            if (perCommitChangeSetSize > maxChangeSetSize) maxChangeSetSize = perCommitChangeSetSize;
        }

        targetEntry.getMetrics().setChangeSetSize(changeSetSize);
        targetEntry.getMetrics().setAvgChangeSetSize(avgChangeSetSize);
        targetEntry.getMetrics().setMaxChangeSetSize(maxChangeSetSize);

    }
}
