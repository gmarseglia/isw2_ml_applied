package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.util.List;

public class BugginessControllerTest {

    private final MyLogger logger = MyLogger.getInstance(BugginessControllerTest.class);

    @Test
    public void getAllLabelledEntriesTest() throws GitAPIException {
        String projName = "OPENJPA";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");
        BugginessController bc = BugginessController.getInstance(projName);
        EntriesController ec = EntriesController.getInstance(projName);

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.getInstance(BugginessController.class).setVerboseFinest(null);

        // Print all valid versions
        logger.logPrefixless(() -> System.out.println("\n\ngetAllValidVersions"));
        List<Version> allValidVersions = VersionsController.getInstance(projName).getAllValidVersions();
        logger.logPrefixless(() -> System.out.println("getAllValidVersions.size(): " + allValidVersions.size()));
        for (Version v : allValidVersions) {
            logger.logPrefixless(() -> System.out.println(v));
        }

        // Print half valid versions
        logger.logPrefixless(() -> System.out.println("\n\ngetHalfVersion"));
        List<Version> halfVersions = VersionsController.getInstance(projName).getHalfVersion();
        logger.logPrefixless(() -> System.out.println("getHalfVersion.size(): " + halfVersions.size()));
        for (Version v : halfVersions) {
            logger.logPrefixless(() -> System.out.println(v));
        }

        // Print all entries for half versions
        logger.logPrefixless(() -> System.out.println("\n\ngetAllEntriesForHalfVersions"));
        List<Entry> halfEntries = ec.getAllEntriesForHalfVersions();
        logger.logPrefixless(() -> System.out.println("getAllEntriesForHalfVersions: " + halfEntries.size()));

        // Print all valid issues
        logger.logPrefixless(() -> System.out.println("\n\ngetTotalValidIssues"));
        List<Issue> allValidIssues = IssueController.getInstance(projName).getTotalValidIssues(Integer.MAX_VALUE);
        logger.logPrefixless(() -> System.out.println("getTotalValidIssues: " + allValidIssues.size()));

        // Print all entries for half versions after being labelled
        logger.logPrefixless(() -> System.out.println("\n\ngetAllLabelledEntries"));
        List<Entry> allLabelledEntries = bc.getAllLabelledEntries();
        logger.logPrefixless(() -> System.out.println("getAllLabelledEntries: " + allLabelledEntries.size()));

    }
}