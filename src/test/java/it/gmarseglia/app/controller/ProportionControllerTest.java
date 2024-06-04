package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ProportionControllerTest {

    private final MyLogger logger = MyLogger.getInstance(ProportionController.class);

    @Test
    public void getAllProportionedIssuesTest() throws InterruptedException, GitAPIException {
        String projName = "OPENJPA";
        GitController.getInstance(projName).setTagsRegex("(release-)?%v(-incubating)?");

        int maxTotal = Integer.MAX_VALUE;

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);

        IssueController ic = IssueController.getInstance(projName);

        MyLogger.getInstance(IssueController.class).setVerboseFinest(false);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("%n%n All valid issues");
        List<Issue> allValidIssues = ic.getTotalValidIssues(maxTotal);
        logger.logNoPrefix(String.format("All valid issues size: %d\n", allValidIssues.size()));
        allValidIssues.forEach(logger::logObjectNoPrefix);

        MyLogger.getInstance(IssueController.class).setVerboseFinest(false);

        MyLogger.getInstance(ProportionController.class).setVerboseFinest(true);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("%n%n All valid proportioned issues");
        ProportionController pc = ProportionController.getInstance(projName);
        List<Issue> allValidProportionedIssues = pc.getTotalProportionedIssuesIncrement(maxTotal);
        logger.logNoPrefix(String.format("All valid proportioned issues size: %d\n", allValidProportionedIssues.size()));
        allValidProportionedIssues.forEach(logger::logObjectNoPrefix);

        TimeUnit.SECONDS.sleep(1);
        logger.logNoPrefix("%n%n Correctly assigned IV check");
        long IVNotNullIssues = pc.getTotalProportionedIssuesIncrement(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .count();
        long IVCorrectIssues = pc.getTotalProportionedIssuesIncrement(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .filter(issue -> Objects.equals(issue.IVIndex(), issue.PredictedIVIndex()))
                .count();
        logger.logNoPrefix(String.format("Correctly assigned IV percentage: %d/%d = %.1f%%\n",
                IVCorrectIssues, IVNotNullIssues,
                (float) IVCorrectIssues * 100 / IVNotNullIssues
        ));

        float averageError = 0;
        int updates = 0;
        for (Issue issue : pc.getTotalProportionedIssuesIncrement(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .filter(issue -> !Objects.equals(issue.IVIndex(), issue.PredictedIVIndex()))
                .toList()) {
            updates = updates + 1;
            averageError = averageError + ((float) 1 / updates) * (Math.abs(issue.PredictedIVIndex() - issue.IVIndex()) - averageError);
        }
        float finalAverageError = averageError;
        logger.logNoPrefix(String.format("Average error: %.2f\n",
                finalAverageError
        ));

        float averageDifference = 0;
        updates = 0;
        for (Issue issue : pc.getTotalProportionedIssuesIncrement(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .toList()) {
            updates = updates + 1;
            averageDifference = averageDifference + ((float) 1 / updates) * (Math.abs(issue.PredictedIVIndex() - issue.IVIndex()) - averageDifference);
        }
        float finalAverageDifference = averageDifference;
        logger.logNoPrefix(String.format("Average difference: %.2f\n",
                finalAverageDifference
        ));


    }

}