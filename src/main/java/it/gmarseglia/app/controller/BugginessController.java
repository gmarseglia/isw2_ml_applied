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
            this.allLabelledEntries = new ArrayList<>(ec.getAllEntriesForHalfVersions());

            ProportionController pc = ProportionController.getInstance(projName);
            List<Issue> allValidProportionedIssues = pc.getTotalProportionedIssues(Integer.MAX_VALUE);

            VersionsController vc = VersionsController.getInstance(projName);
            BlameController bc = BlameController.getInstance(projName);

            int HALF_LASTi = vc.getAllReleasedVersions().indexOf(vc.getHalfVersion().getLast());

            int valid = 0;
            int invalid = 0;
            int overHalf = 0;

            List<Entry> newLabelledEntries = new ArrayList<>();

            for (Issue proportionedIssue : allValidProportionedIssues) {
                int IVi = proportionedIssue.IVIndex() != null ? proportionedIssue.IVIndex() : proportionedIssue.PredictedIVIndex();
                int FVi = proportionedIssue.FVIndex() != null ? proportionedIssue.FVIndex() : HALF_LASTi + 1;

                // skip predicted nonPostRelease issues
                if (IVi >= FVi) {
                    invalid++;
                    continue;
                } else if (IVi > HALF_LASTi) {
                    overHalf++;
                    continue;
                } else {
                    valid++;
                }

                List<Version> affectedVersions = vc.getAllValidVersions().subList(IVi, FVi);
                List<Version> affectedVersionsInHalfVersions = affectedVersions.stream().filter(vc.getHalfVersion()::contains).toList();

                logger.logFinest(() -> System.out.println("*** proportionedIssue: " + proportionedIssue));
                logger.logFinest(() -> {
                            List<String> affectedVersionsName = affectedVersions.stream().map(Version::getName).toList();
                            System.out.println("affectedVersions.size(): " + affectedVersions.size() + ", " + affectedVersionsName);
                        }
                );
                logger.logFinest(() -> {
                            List<String> affectedVersionsHalfName = affectedVersionsInHalfVersions.stream().map(Version::getName).toList();
                            System.out.println("affectedVersionsInHalfVersions.size(): " + affectedVersionsInHalfVersions.size() + ", " + affectedVersionsHalfName);
                        }
                );

                List<Version> datasetAffectedVersions = vc.getHalfVersion()
                        .stream()
                        .filter(affectedVersionsInHalfVersions::contains)
                        .toList();

                // Remove duplicates maybe
                List<Path> datasetAffectedPaths = new ArrayList<>(
                        new LinkedHashSet<>(bc.getAllPathsTouchedByIssue(proportionedIssue)));


                logger.logFinest(() ->
                        System.out.println("datasetAffectedPaths.size(): " + datasetAffectedPaths.size() + ", " + datasetAffectedPaths));

                var ref = new Object() {
                    Integer i = 0;
                };

                this.allLabelledEntries
                        .stream()
                        .filter(entry -> datasetAffectedVersions.contains(entry.getVersion()))
                        .filter(entry -> datasetAffectedPaths.contains(entry.getPath()))
                        .forEach(entry -> {
                            newLabelledEntries.add(entry);
                            entry.setBuggy(true);
                            ref.i++;
                        });

                logger.logFinest(() ->
                        System.out.println("Number of entries labelled \"buggy\": " + ref.i));
            }

            int finalValid = valid;
            int finalInvalid = invalid;
            int finalOverHalf = overHalf;

            List<Entry> newLabelledEntriesNoDuplicates = new ArrayList<>(
                    new LinkedHashSet<>(newLabelledEntries));

            logger.logFine(() -> System.out.println("Total number of entries labelled \"buggy\": " + newLabelledEntriesNoDuplicates.size()));
            logger.logFine(() -> System.out.printf("valid: %d, invalid: %d, overHalf: %d, sum: %d\n",
                    finalValid, finalInvalid, finalOverHalf, finalValid + finalInvalid + finalOverHalf));

        }
        return this.allLabelledEntries;
    }
}
