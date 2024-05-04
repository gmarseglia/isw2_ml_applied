package it.gmarseglia.app.controller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitControllerTest {

    private static String projName;
    private final MyLogger logger = MyLogger.getInstance(GitControllerTest.class);

    @BeforeClass
    public static void prep() {
        projName = "BOOKKEEPER";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.getInstance(GitController.class).setVerboseFinest(true);
    }

    @Test
    public void getAllCommitsByPathTest() throws GitAPIException {
        /*
        git log --pretty=format:"%H"  --follow -- /tmp/BOOKKEEPER/hedwig-server/src/main/java/org/apache/hedwig/server/subscriptions/TrueFilter.java
        show 3 commits

        git log --all --decorate --oneline --graph --first-parent --remotes --reflog --author-date-order --stat -- bookkeeper-server/src/main/java/org/apache/bookkeeper/client/LedgerHandle.java

        git log --all --first-parent --remotes --reflog --author-date-order --pretty=format:"%h" --decorate --oneline --graph --stat
        */
        String pathStr = "bookkeeper-server/src/main/java/org/apache/bookkeeper/proto/WriteEntryProcessor.java";
        List<RevCommit> commits = GitController.getInstance(projName).getRevCommitsFromPath(Paths.get(pathStr));

        logger.logNoPrefix("commits.size(): " + commits.size() + ", " + commits);

        for (RevCommit commit : commits) {
            List<Path> allPathByCommit = GitController.getInstance(projName).getAllPathByCommit(commit);
            logger.logNoPrefix("allPathByCommit.size(): " + allPathByCommit.size());
        }
    }

    @Test
    public void getFirstCommitTest() throws GitAPIException {
        RevCommit firstCommit = GitController.getInstance(projName).getFirstCommit();

        logger.logNoPrefix("firstCommit: " + firstCommit);

        Assert.assertEquals("9ea37773fa07e8e1c16e654020ae34c3d6564963", firstCommit.getName());
    }
}