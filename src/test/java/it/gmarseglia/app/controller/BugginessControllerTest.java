package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import it.gmarseglia.app.entity.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.util.List;

public class BugginessControllerTest {

    private final MyLogger logger = MyLogger.getInstance(BugginessControllerTest.class);

    @Test
    public void getAllLabelledEntriesTest() throws GitAPIException {
        String projName = "BOOKKEEPER";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);

        MyLogger.getInstance(BugginessController.class).setVerboseFinest(true);
        // MyLogger.getInstance(EntriesController.class).setVerboseFinest(true);
        // MyLogger.getInstance(GitController.class).setVerboseFinest(true);

        BugginessController bc = BugginessController.getInstance(projName);

        logger.logPrefixless(() -> System.out.println("\n\ngetAllValidVersions"));
        List<Version> allValidVersions = VersionsController.getInstance(projName).getAllValidVersions();
        logger.logPrefixless(() -> System.out.println("getAllValidVersions.size(): " + allValidVersions.size()));
        for (Version v : allValidVersions) {
            logger.logPrefixless(() -> System.out.println(v));
        }

        logger.logPrefixless(() -> System.out.println("\n\ngetHalfVersion"));
        List<Version> halfVersions = VersionsController.getInstance(projName).getHalfVersion();
        logger.logPrefixless(() -> System.out.println("getHalfVersion.size(): " + halfVersions.size()));
        for (Version v : halfVersions) {
            logger.logPrefixless(() -> System.out.println(v));
        }

        logger.logPrefixless(() -> System.out.println("\n\ngetAllLabelledEntries"));
        EntriesController ec = EntriesController.getInstance(projName);
        List<Entry> halfEntries = ec.getAllEntriesForHalfVersions();
        logger.logPrefixless(() -> System.out.println("getAllEntriesForHalfVersions: " + halfEntries.size()));

        logger.logPrefixless(() -> System.out.println("\n\ngetAllLabelledEntries"));
        List<Entry> allLabelledEntries = bc.getAllLabelledEntries();
        logger.logPrefixless(() -> System.out.println("getAllLabelledEntries: " + allLabelledEntries.size()));

    }
}