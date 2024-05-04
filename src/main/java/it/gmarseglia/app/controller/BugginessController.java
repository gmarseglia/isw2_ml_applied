package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.util.*;

public class BugginessController {

    private static final Map<String, BugginessController> instances = new HashMap<>();
    private final MyLogger logger = MyLogger.getInstance(BugginessController.class);
    private final String projName;
    private List<Entry> allLabelledEntries;

    public BugginessController(String projName) {
        this.projName = projName;
    }

    public static BugginessController getInstance(String projName) {
        BugginessController.instances.computeIfAbsent(projName, BugginessController::new);
        return BugginessController.instances.get(projName);
    }

    public List<Entry> getAllLabelledEntries() throws GitAPIException {
        if (this.allLabelledEntries == null) {
            EntriesController ec = EntriesController.getInstance(projName);
            VersionsController vc = VersionsController.getInstance(projName);
            BlameController bc = BlameController.getInstance(projName);

            // #1. Obtain the list of all entries for half the usable versions
            this.allLabelledEntries = new ArrayList<>(ec.getAllEntriesForHalfVersions());

            // #2. Obtain the list of proportioned issues
            ProportionController pc = ProportionController.getInstance(projName);
            List<Issue> allValidProportionedIssues = pc.getTotalProportionedIssues(Integer.MAX_VALUE);

            // Obtain the index of the last half usable versions
            int HALF_LASTi = vc.getAllReleasedVersions().indexOf(vc.getHalfVersion().getLast());

            int usable = 0;
            int unusable = 0;
            int overHalf = 0;

            // For print purpose
            List<Entry> newLabelledEntries = new ArrayList<>();

            for (Issue proportionedIssue : allValidProportionedIssues) {

                // Actual IV = "Jira IV" if present, otherwise "Predicted IV"
                int IVi = proportionedIssue.IVIndex() != null ? proportionedIssue.IVIndex() : proportionedIssue.PredictedIVIndex();

                // Actual FV = "Jira FV" if in valid versions, otherwise "Practically last possible FV"
                int FVi = proportionedIssue.FVIndex() != null ? proportionedIssue.FVIndex() : HALF_LASTi + 1;

                // skip nonPostRelease && invalid issues -> "unusable" issues
                if (IVi >= FVi) {
                    unusable++;
                    continue;
                }
                // skip "over half" issues
                else if (IVi > HALF_LASTi) {
                    overHalf++;
                    continue;
                } else {
                    usable++;
                }

                // Obtain the list of the valid versions affected by the issue
                List<Version> affectedVersions = vc.getAllValidVersions().subList(IVi, FVi);

                // Obtain the list of the valid versions affected by the issue which are part of
                // the half sublist
                List<Version> datasetAffectedVersions = vc.getHalfVersion()
                        .stream()
                        .filter(affectedVersions::contains)
                        .filter(vc.getHalfVersion()::contains)
                        .toList();

                // Obtain the list of the paths affected by the issue, without duplicates
                List<Path> datasetAffectedPaths = new ArrayList<>(
                        new LinkedHashSet<>(bc.getAllPathsTouchedByIssue(proportionedIssue)));

                // Counter variable for printing purpose
                var ref = new Object() {
                    Integer i = 0;
                };

                /*
                 Label the entries which path is contained in the affected paths AND
                 which version is contained in the half affected version
                 as buggy (is it hadn't been labelled already)
                */
                this.allLabelledEntries
                        .stream()
                        .filter(entry -> datasetAffectedVersions.contains(entry.getVersion()))
                        .filter(entry -> datasetAffectedPaths.contains(entry.getPath()))
                        .filter(Entry::isNotBuggy)
                        .forEach(entry -> {
                            newLabelledEntries.add(entry);
                            entry.setBuggy(true);
                            ref.i++;
                        });

                if (logger.getAnyVerboseFinest()) {
                    logger.logFinest("*** proportionedIssue: " + proportionedIssue);

                    List<String> affectedVersionsName = affectedVersions.stream().map(Version::getName).toList();
                    logger.logFinest("affectedVersions.size(): " + affectedVersions.size() + ", " + affectedVersionsName);

                    List<String> datasetAffectedVersionsName = datasetAffectedVersions.stream().map(Version::getName).toList();
                    logger.logFinest("datasetAffectedVersions.size(): " + datasetAffectedVersions.size() + ", " + datasetAffectedVersionsName);

                    logger.logFinest("datasetAffectedPaths.size(): " + datasetAffectedPaths.size() + ", " + datasetAffectedPaths);

                    logger.logFinest("Number of entries labelled \"buggy\" (without duplicates): " + ref.i);
                }

            }

            int finalValid = usable;
            int finalInvalid = unusable;
            int finalOverHalf = overHalf;

            logger.logFine(String.format("Proportioned issues by type: {Usable: %d, Unusable: %d, Over half: %d}",
                    finalValid, finalInvalid, finalOverHalf));
            logger.logFine(String.format("Total number of entries labelled \"buggy\": %s", newLabelledEntries.size()));

        }
        return this.allLabelledEntries;
    }
}
