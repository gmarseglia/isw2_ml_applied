package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueFactory {

    private record FVResult(Version fv, IssueFVType fvType){}
    private record IVResult(Version iv, boolean found){}

    private static final Map<String, IssueFactory> instances = new HashMap<>();
    private static final boolean USE_JIRA_FV = true;

    private final MyLogger logger = MyLogger.getInstance(IssueFactory.class);
    private final VersionsController vc;

    private IssueFactory(String projName) {
        this.vc = VersionsController.getInstance(projName);
    }

    public static IssueFactory getInstance(String projName) {
        IssueFactory.instances.computeIfAbsent(projName, string -> new IssueFactory(projName));
        return IssueFactory.instances.get(projName);
    }

    private Version getOV(JiraIssue jiraIssue) throws GitAPIException {
        return vc.getAllReleasedVersions()
                .stream()
                .filter(version -> version.getJiraReleaseDate().after(jiraIssue.getFields().getCreated()))
                .findFirst()
                .orElse(null);
    }

    private FVResult getFV(JiraIssue jiraIssue) throws GitAPIException {
        Version fv;
        IssueFVType fvType = null;
        if (USE_JIRA_FV && jiraIssue.getFields().getOldestAFixVersion() != null) {
            List<String> idsOfFixedVersions = jiraIssue.getFields().getFixVersions().stream().map(JiraVersion::getId).toList();

            fv = vc.getAllReleasedVersions()
                    .stream()
                    .filter(version ->
                            idsOfFixedVersions.contains(version.getId()))
                    .findFirst()
                    .orElse(null);
            String fvStr = (fv == null) ? "null" : fv.getName();
            if (fv != null) {
                fvType = IssueFVType.BY_EXPLICIT_JIRA;
            }
            logger.logFinestNoPrefix(String.format("FV: %s got from Jira.", fvStr));
        } else {
            fv = vc.getAllReleasedVersions()
                    .stream()
                    .filter(version -> version.getJiraReleaseDate().after(jiraIssue.getFields().getResolutiondate()))
                    .findFirst()
                    .orElse(null);
            String fvStr = (fv == null) ? "null" : fv.getName();
            if (fv != null) {
                fvType = IssueFVType.BY_RESOLUTION_DATE_JIRA;
            }
            logger.logFinestNoPrefix(String.format("FV: %s got from Resolution Date.", fvStr));
        }

        if (fv == null) fvType = IssueFVType.GOT_NULL;

        return new FVResult(fv, fvType);
    }

    private IVResult getIV(JiraIssue jiraIssue) throws GitAPIException {
        Version iv;
        boolean found = false;
        if (jiraIssue.getFields().getOldestAffectedVersion() == null) {
            iv = null;
        } else {
            found = true;
            List<String> idsOfAffectsVersion = jiraIssue.getFields().getVersions().stream().map(JiraVersion::getId).toList();

            iv = vc.getAllReleasedVersions()
                    .stream()
                    .filter(version ->
                            idsOfAffectsVersion.contains(version.getId()))
                    .findFirst()
                    .orElse(null);

            if (iv == null) {
                iv = vc.getAllReleasedVersions()
                        .stream()
                        .filter(version ->
                                version.getJiraReleaseDate().after(jiraIssue.getFields().getOldestAffectedVersion().getReleaseDate()))
                        .findFirst()
                        .orElse(null);
            }
        }

        return new IVResult(iv, found);
    }

    /**
     * The date present on Jira are compared to the valid versions obtained earlier.
     * <p>
     * The creation date on Jira is used to set the OV.
     * The resolution date on Jira is used to set the FV.
     * The release date of the oldest version in AffectsVersion (if present) is used to set the IV.
     *
     * @param jiraIssue Version from Jira.
     * @return Version with OV, FV and IV set to valid versions.
     * @throws GitAPIException due to {@link GitController}
     */
    public Issue issueFromJiraIssue(JiraIssue jiraIssue) throws GitAPIException {
        Version ov;
        Version fv;
        Version iv;
        Integer[] versionsIndex = new Integer[]{null, null, null, null};

        IssueFVType fvType = null;

        List<Version> allReleasedVersions = vc.getAllReleasedVersions();

        ov = getOV(jiraIssue);

        versionsIndex[0] = (ov == null) ? allReleasedVersions.size() : allReleasedVersions.indexOf(ov);

        FVResult result = getFV(jiraIssue);
        fv = result.fv;
        fvType = result.fvType;

        versionsIndex[1] = (fv == null) ? allReleasedVersions.size() : allReleasedVersions.indexOf(fv);

        IVResult ivResult = getIV(jiraIssue);
        iv = ivResult.iv;
        boolean found = ivResult.found;

        if(found) versionsIndex[2] = (iv == null) ? allReleasedVersions.size() : allReleasedVersions.indexOf(iv);

        Issue newIssue = new Issue(jiraIssue.getKey(),
                ov,
                fv,
                iv,
                jiraIssue.getFields().getCreated(),
                jiraIssue.getFields().getResolutiondate(),
                versionsIndex);

        newIssue.setFvType(fvType);

        return newIssue;
    }
}
