package it.gmarseglia.app.controller;

import it.gmarseglia.app.boundary.ToFileBoundary;
import it.gmarseglia.app.entity.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.*;

public class ProportionController {

    private static final Map<String, ProportionController> instances = new HashMap<>();
    private final String projName;
    private final IssueController ic;
    private final MyLogger logger = MyLogger.getInstance(ProportionController.class);
    private Integer lastMaxTotal;
    private List<Issue> totalProportionedIssues;

    private ProportionController(String projName) {
        this.projName = projName;
        this.ic = IssueController.getInstance(projName);
    }

    public static ProportionController getInstance(String projName) {
        ProportionController.instances.computeIfAbsent(projName, ProportionController::new);
        return ProportionController.instances.get(projName);
    }

    public List<Issue> getTotalProportionedIssues(int maxTotal) throws GitAPIException {
        if (this.totalProportionedIssues == null || maxTotal != this.lastMaxTotal) {
            this.lastMaxTotal = maxTotal;
            this.totalProportionedIssues = new ArrayList<>(ic.getTotalValidIssues(maxTotal));

            logger.logFine(String.format("Ready to apply Increment Proportion on %d issues.", this.totalProportionedIssues.size()));

            Iterator<Issue> issueIterator = this.totalProportionedIssues
                    .stream()
                    .sorted(Comparator.comparing(Issue::getJiraResolutionDate))
                    .toList()
                    .iterator();

            float P = 1;
            int updates = 0;

            while (issueIterator.hasNext()) {
                Issue i = issueIterator.next();

                if (i.IVIndex() != null) {
                    // update P
                    // if IV is present, then newP = (FV - IV) / (FV - OV)
                    float den = (i.FVIndex() - i.OVIndex() != 0) ? i.FVIndex() - i.OVIndex() : 1;
                    float newP = (i.FVIndex() - i.IVIndex()) / den;
                    // in place average update
                    updates = updates + 1;
                    P = P + ((float) 1 / updates) * (newP - P);

                    float finalP = P;
                    int finalUpdates = updates;
                    logger.logFinest(String.format("P update on %s-> OV: %d, FV: %d, IV: %d, newP: %.3f, updates: %d,P: %.3f",
                                    i.getKey(),
                                    i.OVIndex(), i.FVIndex(), i.IVIndex(),
                                    newP, finalUpdates, finalP));

                }

                // apply P
                // if IV is not present, then predictedIV = FV - (FV - OV) * P
                float step = (i.FVIndex() - i.OVIndex() != 0) ? i.FVIndex() - i.OVIndex() : 1;
                float actualP = (updates <= 5) ? 1.8089F : P;

                float predictedIV = (float) i.FVIndex() - step * actualP;
                int actualPredictedIV = Math.max((int) Math.floor(predictedIV), 0);
                i.setPredictedIVIndex(actualPredictedIV);

                logger.logFinest(String.format("P apply on %s -> OV: %d, FV: %d, IV: %d, P: %.3f, predictedIV: %.3f->%d",
                                i.getKey(),
                                i.OVIndex(), i.FVIndex(), i.IVIndex(),
                                actualP,
                                predictedIV, actualPredictedIV));

            }

            int proportionedIssues = this.totalProportionedIssues.size() - updates;

            logger.log(String.format("Actually proportioned %d issues which didn't have explicit IV.", proportionedIssues));

            ToFileBoundary.writeListProj(this.totalProportionedIssues, projName, "totalProportionedIssue.csv");
        }
        return this.totalProportionedIssues;
    }
}
