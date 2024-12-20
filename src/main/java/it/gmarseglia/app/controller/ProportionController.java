package it.gmarseglia.app.controller;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.entity.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.text.SimpleDateFormat;
import java.util.*;

public class ProportionController {

    private record ProportionResult(int updates, float p) {
    }

    private static final Map<String, ProportionController> instances = new HashMap<>();
    private final String projName;
    private final IssueController ic;
    private final MyLogger logger = MyLogger.getInstance(ProportionController.class);
    private Integer lastMaxTotal;
    private List<Issue> totalProportionedIssues;
    private String lastMode = "";

    private ProportionController(String projName) {
        this.projName = projName;
        this.ic = IssueController.getInstance(projName);
    }

    public static ProportionController getInstance(String projName) {
        ProportionController.instances.computeIfAbsent(projName, ProportionController::new);
        return ProportionController.instances.get(projName);
    }

    private ProportionResult applyIncrement(Iterator<Issue> issueIterator) {
        int updates = 0;
        float p = 1;

        while (issueIterator.hasNext()) {
            Issue i = issueIterator.next();

            if (i.getIVIndex() != null) {
                // update P
                // if IV is present, then newP = (FV - IV) / (FV - OV)
                float den = (i.getFVIndex() - i.getOVIndex() != 0) ? i.getFVIndex() - i.getOVIndex() : 1;
                float newP = (i.getFVIndex() - i.getIVIndex()) / den;
                // in place average update
                updates = updates + 1;
                p = p + ((float) 1 / updates) * (newP - p);

                float finalP = p;
                int finalUpdates = updates;
                logger.logFinest(String.format("P update on %s-> OV: %d, FV: %d, IV: %d, newP: %.3f, updates: %d,P: %.3f",
                        i.getKey(),
                        i.getOVIndex(), i.getFVIndex(), i.getIVIndex(),
                        newP, finalUpdates, finalP));

            }

            // apply P
            // if IV is not present, then predictedIV = FV - (FV - OV) * P
            float step = (i.getFVIndex() - i.getOVIndex() != 0) ? i.getFVIndex() - i.getOVIndex() : 1;
            // Paper: 1.8089F
            // Weighted avg: (1.386 * 66 + 1.674 * 597 + 2.154 * 631 + 2.553 * 315 + 2.432 * 630 + 1.438 * 494 + 1.420 * 104) / (66 + 597 + 631 + 315 + 630 + 494 + 104) = 1.98957878F
            // Avg: (1.386 + 1.674 + 2.154 + 2.553 + 2.432 + 1.438 + 1.420) / 7 = 1.865285714F
            float actualP = (updates <= 5) ? 1.8089F : p;

            float predictedIV = (float) i.getFVIndex() - step * actualP;
            int actualPredictedIV = Math.max((int) Math.floor(predictedIV), 0);
            i.setPredictedIVIndex(actualPredictedIV);

            logger.logFinest(String.format("P apply on %s -> OV: %d, FV: %d, IV: %d, P: %.3f, predictedIV: %.3f->%d",
                    i.getKey(),
                    i.getOVIndex(), i.getFVIndex(), i.getIVIndex(),
                    actualP,
                    predictedIV, actualPredictedIV));

        }

        return new ProportionResult(updates, p);
    }

    public List<Issue> getTotalProportionedIssuesIncrement(int maxTotal) throws GitAPIException {
        if (!this.lastMode.equals("increment") || this.totalProportionedIssues == null || maxTotal != this.lastMaxTotal) {
            this.lastMode = "increment";
            this.lastMaxTotal = maxTotal;
            this.totalProportionedIssues = new ArrayList<>(ic.getTotalValidIssues(maxTotal));

            logger.logFine(String.format("Ready to apply Increment Proportion on %d issues.", this.totalProportionedIssues.size()));

            Iterator<Issue> issueIterator = this.totalProportionedIssues
                    .stream()
                    .sorted(Comparator.comparing(Issue::getJiraResolutionDate))
                    .toList()
                    .iterator();

            ProportionResult result = applyIncrement(issueIterator);
            float p = result.p;
            int updates = result.updates;

            int proportionedIssues = this.totalProportionedIssues.size() - updates;

            logger.log(String.format("Actually proportioned %d issues which didn't have explicit IV.", proportionedIssues));

            logger.log(String.format("Finally computed P=%.3f on %d updated.", p, updates));

            ToFileBoundary.writeListProj(this.totalProportionedIssues, projName, "totalProportionedIssue.csv");
        }
        return this.totalProportionedIssues;
    }

    private ProportionResult computeAll(Iterator<Issue> issueIterator){
        float p = 1;
        int updates = 0;

        while (issueIterator.hasNext()) {
            Issue i = issueIterator.next();
            if (i.getIVIndex() != null) {
                // update P
                // if IV is present, then newP = (FV - IV) / (FV - OV)
                float den = (i.getFVIndex() - i.getOVIndex() != 0) ? i.getFVIndex() - i.getOVIndex() : 1;
                float newP = (i.getFVIndex() - i.getIVIndex()) / den;
                // in place average update
                updates = updates + 1;
                p = p + ((float) 1 / updates) * (newP - p);

                float finalP = p;
                int finalUpdates = updates;
                logger.logFinest(String.format("P update on %s-> OV: %d, FV: %d, IV: %d, newP: %.3f, updates: %d,P: %.3f",
                        i.getKey(),
                        i.getOVIndex(), i.getFVIndex(), i.getIVIndex(),
                        newP, finalUpdates, finalP));
            }
        }

        return new ProportionResult(updates, p);
    }

    private void applyAll(Iterator<Issue> issueIterator, ProportionResult result){
        float p = result.p;
        int updates = result.updates;

        while (issueIterator.hasNext()) {
            Issue i = issueIterator.next();
            // apply P
            // if IV is not present, then predictedIV = FV - (FV - OV) * P
            float step = (i.getFVIndex() - i.getOVIndex() != 0) ? i.getFVIndex() - i.getOVIndex() : 1;
            // Paper: 1.8089F
            // Weighted avg: (1.386 * 66 + 1.674 * 597 + 2.154 * 631 + 2.553 * 315 + 2.432 * 630 + 1.438 * 494 + 1.420 * 104) / (66 + 597 + 631 + 315 + 630 + 494 + 104) = 1.98957878F
            // Avg: (1.386 + 1.674 + 2.154 + 2.553 + 2.432 + 1.438 + 1.420) / 7 = 1.865285714F
            float actualP = (updates <= 5) ?  1.865285714F : p;

            float predictedIV = (float) i.getFVIndex() - step * actualP;
            int actualPredictedIV = Math.max((int) Math.floor(predictedIV), 0);
            i.setPredictedIVIndex(actualPredictedIV);
            logger.logFinest(String.format("P apply on %s -> OV: %d, FV: %d, IV: %d, P: %.3f, predictedIV: %.3f->%d",
                    i.getKey(),
                    i.getOVIndex(), i.getFVIndex(), i.getIVIndex(),
                    actualP,
                    predictedIV, actualPredictedIV));
        }
    }

    public List<Issue> getTotalProportionedIssuesAll(int maxTotal, Date observationDate) throws GitAPIException {
        if (!("all" + observationDate).equals(this.lastMode)) {
            this.lastMode = "all" + observationDate;
            this.lastMaxTotal = maxTotal;
            List<Issue> temporalAvailableIssues = ic.getTotalValidIssues(maxTotal)
                    .stream()
                    // only issues that have resolution date before to stopDate
                    .filter(issue -> (observationDate == null || issue.getJiraResolutionDate().compareTo(observationDate) < 0))
                    .toList();
            this.totalProportionedIssues = new ArrayList<>(temporalAvailableIssues);

            logger.logFine(String.format("Ready to apply Increment Proportion on %d issues.", this.totalProportionedIssues.size()));

            List<Issue> baseList = this.totalProportionedIssues
                    .stream()
                    .sorted(Comparator.comparing(Issue::getJiraResolutionDate))
                    .toList();

            Iterator<Issue> issueIterator = baseList.iterator();

            ProportionResult result = computeAll(issueIterator);
            float p = result.p;
            int updates = result.updates;

            issueIterator = baseList.iterator();

            applyAll(issueIterator, result);

            int proportionedIssues = this.totalProportionedIssues.size() - updates;

            logger.log(String.format("Actually proportioned %d issues which didn't have explicit IV.", proportionedIssues));

            logger.log(String.format("Finally computed P=%.3f on %d updated.", p, updates));

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
            ToFileBoundary.writeListProj(
                    this.totalProportionedIssues,
                    projName,
                    "totalProportionedIssue_" + ((observationDate == null) ? "final" : formatter.format(observationDate)) + ".csv");
        }
        return this.totalProportionedIssues;
    }
}
