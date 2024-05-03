package it.gmarseglia.app.controller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

public class GitControllerTest {

    private final MyLogger logger = MyLogger.getInstance(GitControllerTest.class);

    @Test
    public void getAllCommitsByPathTest() throws GitAPIException {
        String projName = "BOOKKEEPER";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);
        MyLogger.getInstance(GitController.class).setVerboseFinest(true);

        /*
        git log --pretty=format:"%H"  --follow -- /tmp/BOOKKEEPER/hedwig-server/src/main/java/org/apache/hedwig/server/subscriptions/TrueFilter.java
        show 3 commits

        git log --all --decorate --oneline --graph --first-parent --remotes --reflog --author-date-order --stat -- bookkeeper-server/src/main/java/org/apache/bookkeeper/client/LedgerHandle.java

        git log --all --first-parent --remotes --reflog --author-date-order --pretty=format:"%h" --decorate --oneline --graph --stat
        */

        // String pathStr = "hedwig-server/src/main/java/org/apache/hedwig/server/subscriptions/TrueFilter.java";
        // String pathStr = "bookkeeper-server/src/main/java/org/apache/bookkeeper/client/LedgerHandle.java";
        String pathStr = "bookkeeper-server/src/main/java/org/apache/bookkeeper/proto/WriteEntryProcessor.java";
        List<RevCommit> commits = GitController.getInstance(projName).getRevCommitsFromPath(Paths.get(pathStr));

        logger.logPrefixless(() -> System.out.println("commits.size(): " + commits.size() + ", " + commits));


    }
}