package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class BlameControllerTest
{
    /**
     */
    @Test
    public void getAllPathsTouchedByIssueTest() throws GitAPIException {
        String projName = "OPENJPA";
        int issueNumber = 42;
        MyLogger logger = MyLogger.getInstance(this.getClass());
        MyLogger.setStaticVerbose(true);
        IssueController issueController = IssueController.getInstance(projName);
        BlameController blameController = BlameController.getInstance(projName);

        List<Issue> issues = issueController.getTotalValidIssues(issueNumber + 1);

        Issue target = issues.get(issueNumber);
        System.out.println(target);

        List<Path> paths = blameController.getAllPathsTouchedByIssue(target);

        paths.forEach(logger::logObject);
    }

    /**
     */
    @Test
    public void getAllPathByCommitTest() throws GitAPIException {
        String projName = "OPENJPA";
        GitController gitController = GitController.getInstance(projName);
        IssueController issueController = IssueController.getInstance(projName);
        BlameController blameController = BlameController.getInstance(projName);

        List<Issue> issues = issueController.getTotalValidIssues(40);

        Issue target = issues.get(39);
        System.out.println(target);

        List<RevCommit> commits = gitController.getAllCommitsByIssue(target);

        RevCommit targetCommit = commits.getFirst();

        System.out.println(targetCommit);

        gitController.getAllPathByCommit(targetCommit);
    }
}
