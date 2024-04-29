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
        logger.logPrefixless(() -> System.out.println("\n\nAll valid issues"));
        List<Issue> allValidIssues = ic.getTotalValidIssues(maxTotal);
        logger.logPrefixless(() -> System.out.printf("All valid issues size: %d\n", allValidIssues.size()));
        allValidIssues.forEach(logger::logObjectPrefixless);

        MyLogger.getInstance(IssueController.class).setVerboseFinest(false);

        MyLogger.getInstance(ProportionController.class).setVerboseFinest(true);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nAll valid proportioned issues"));
        ProportionController pc = ProportionController.getInstance(projName);
        List<Issue> allValidProportionedIssues = pc.getTotalProportionedIssues(maxTotal);
        logger.logPrefixless(() -> System.out.printf("All valid proportioned issues size: %d\n", allValidProportionedIssues.size()));
        allValidProportionedIssues.forEach(logger::logObjectPrefixless);

        TimeUnit.SECONDS.sleep(1);
        logger.logPrefixless(() -> System.out.println("\n\nCorrectly assigned IV check"));
        long IVNotNullIssues = pc.getTotalProportionedIssues(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .count();
        long IVCorrectIssues = pc.getTotalProportionedIssues(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .filter(issue -> Objects.equals(issue.IVIndex(), issue.PredictedIVIndex()))
                .count();
        logger.logPrefixless(() -> System.out.printf("Correctly assigned IV percentage: %d/%d = %.1f%%\n",
                IVCorrectIssues, IVNotNullIssues,
                (float) IVCorrectIssues * 100 / IVNotNullIssues
        ));

        float averageError = 0;
        int updates = 0;
        for (Issue issue : pc.getTotalProportionedIssues(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .filter(issue -> !Objects.equals(issue.IVIndex(), issue.PredictedIVIndex()))
                .toList()) {
            updates = updates + 1;
            averageError = averageError + ((float) 1 / updates) * (Math.abs(issue.PredictedIVIndex() - issue.IVIndex()) - averageError);
        }
        float finalAverageError = averageError;
        logger.logPrefixless(() -> System.out.printf("Average error: %.2f\n",
                finalAverageError
        ));

        float averageDifference = 0;
        updates = 0;
        for (Issue issue : pc.getTotalProportionedIssues(maxTotal)
                .stream()
                .filter(issue -> issue.IVIndex() != null)
                .toList()) {
            updates = updates + 1;
            averageDifference = averageDifference + ((float) 1 / updates) * (Math.abs(issue.PredictedIVIndex() - issue.IVIndex()) - averageDifference);
        }
        float finalAverageDifference = averageDifference;
        logger.logPrefixless(() -> System.out.printf("Average difference: %.2f\n",
                finalAverageDifference
        ));


    }

}