package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Entry;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.util.List;

public class MetricsControllerTest {

    private final MyLogger logger = MyLogger.getInstance(MetricsController.class);

    @Test
    public void setMetricsForAllEntries() throws GitAPIException {
        String projName = "BOOKKEEPER";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");
        MetricsController mc = MetricsController.getInstance(projName);

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);

        String pathStr = "bookkeeper-server/src/main/java/org/apache/bookkeeper/bookie/EntryLogger.java";

        List<Entry> labelledEntries = BugginessController.getInstance(projName).getAllLabelledEntries();

        // "bookkeeper-server/src/main/java/org/apache/bookkeeper/bookie/EntryLogger.java"
        // 4.2.2
        List<Entry> testEntries = labelledEntries.stream().filter(entry -> entry.getLongName().contains(pathStr)).toList();

        logger.logPrefixless(() -> System.out.println("testEntries.size(): " + testEntries.size() + ", testEntries: " + testEntries));

        mc.setMetricsForAllEntries(testEntries);

        for (Entry entry : testEntries) {
            logger.logPrefixless(() -> System.out.println("entry: " + entry + ", metrics: " + entry.getMetrics()));
        }
    }
}