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
        logger.logNoPrefix("%n%n getAllValidVersions");
        List<Version> allValidVersions = VersionsController.getInstance(projName).getAllValidVersions();
        logger.logNoPrefix("getAllValidVersions.size(): " + allValidVersions.size());
        for (Version v : allValidVersions) {
            logger.logNoPrefix(v.toString());
        }

        // Print half valid versions
        logger.logNoPrefix("%n%n getHalfVersion");
        List<Version> halfVersions = VersionsController.getInstance(projName).getHalfVersion();
        logger.logNoPrefix("getHalfVersion.size(): " + halfVersions.size());
        for (Version v : halfVersions) {
            logger.logNoPrefix(v.toString());
        }

        // Print all entries for half versions
        logger.logNoPrefix("%n%n getAllEntriesForHalfVersions");
        List<Entry> halfEntries = ec.getAllEntriesForHalfVersions();
        logger.logNoPrefix("getAllEntriesForHalfVersions: " + halfEntries.size());

        // Print all valid issues
        logger.logNoPrefix("%n%n getTotalValidIssues");
        List<Issue> allValidIssues = IssueController.getInstance(projName).getTotalValidIssues(Integer.MAX_VALUE);
        logger.logNoPrefix("getTotalValidIssues: " + allValidIssues.size());

        // Print all entries for half versions after being labelled
        logger.logNoPrefix("%n%n getAllLabelledEntries");
        List<Entry> allLabelledEntries = bc.getAllLabelledEntries();
        logger.logNoPrefix("getAllLabelledEntries: " + allLabelledEntries.size());

    }
}