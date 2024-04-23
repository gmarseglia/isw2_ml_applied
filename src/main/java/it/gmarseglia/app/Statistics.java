package it.gmarseglia.app;

import it.gmarseglia.app.controller.IssueController;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.app.model.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.List;
import java.util.function.Predicate;

public class Statistics {

    private static final MyLogger logger = MyLogger.getInstance(Statistics.class);

    public static void main(String[] args) {
        try {
            nonPostReleaseStats();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static void nonPostReleaseStats() throws GitAPIException {
        String projName = "OPENJPA";
        MyLogger.setStaticVerbose(true);

        IssueController ic = IssueController.getInstance(projName);

        List<Issue> allIssues = ic.getIssues(Integer.MAX_VALUE);

        logger.log(() -> System.out.printf("All Issues size: %d\n", allIssues.size()));

        Predicate<Issue> filterNonPostRelease = issue -> issue.getFixVersion().equals(issue.getInjectVersion());

        long nonPostReleaseCount = allIssues.stream()
                .filter(filterNonPostRelease)
                .count();

        logger.log(() -> System.out.printf("All non post release issue size: %d\n", nonPostReleaseCount));
    }
}
