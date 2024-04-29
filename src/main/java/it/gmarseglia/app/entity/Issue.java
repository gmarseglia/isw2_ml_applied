package it.gmarseglia.app.entity;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class Issue {

    private final Integer[] versionsIndex;
    private String key;
    private Version openingVersion;
    private Version fixVersion;
    private Version injectVersion;
    private Date jiraCreationDate;
    private Date jiraResolutionDate;
    private boolean hasBeenProportioned = false;

    public Issue(String key, Version openingVersion, Version fixVersion, Version injectVersion, Date jiraCreationDate, Date jiraResolutionDate, Integer[] versionsIndex) {
        this.key = key;
        this.openingVersion = openingVersion;
        this.fixVersion = fixVersion;
        this.injectVersion = injectVersion;
        this.jiraCreationDate = jiraCreationDate;
        this.jiraResolutionDate = jiraResolutionDate;
        this.versionsIndex = versionsIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Version getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Version openingVersion) {
        this.openingVersion = openingVersion;
    }

    public Version getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Version fixVersion) {
        this.fixVersion = fixVersion;
    }

    public Version getInjectVersion() {
        return injectVersion;
    }

    public void setInjectVersion(Version injectVersion) {
        this.injectVersion = injectVersion;
    }

    public Date getJiraCreationDate() {
        return jiraCreationDate;
    }

    public void setJiraCreationDate(Date jiraCreationDate) {
        this.jiraCreationDate = jiraCreationDate;
    }

    public Date getJiraResolutionDate() {
        return jiraResolutionDate;
    }

    public void setJiraResolutionDate(Date jiraResolutionDate) {
        this.jiraResolutionDate = jiraResolutionDate;
    }

    @Override
    public String toString() {
        String strOV = Objects.isNull(openingVersion) ? "null" : openingVersion.toShortString();
        String strFV = Objects.isNull(fixVersion) ? "null" : fixVersion.toShortString();
        String strIV = Objects.isNull(injectVersion) ? "null" : injectVersion.toShortString();
        return "Issue{" +
                "key='" + key + '\'' +
                ", openingVersion=" + strOV +
                ", fixVersion=" + strFV +
                ", injectVersion=" + strIV +
                ", jiraCreationDate=" + jiraCreationDate +
                ", jiraResolutionDate=" + jiraResolutionDate +
                ", hasBeenProportioned=" + hasBeenProportioned +
                ", versionsIndex=" + Arrays.toString(versionsIndex) +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Issue issue = (Issue) object;
        return hasBeenProportioned == issue.hasBeenProportioned && Objects.equals(key, issue.key) && Objects.equals(openingVersion, issue.openingVersion) && Objects.equals(fixVersion, issue.fixVersion) && Objects.equals(injectVersion, issue.injectVersion) && Objects.equals(jiraCreationDate, issue.jiraCreationDate) && Objects.equals(jiraResolutionDate, issue.jiraResolutionDate) && Objects.deepEquals(versionsIndex, issue.versionsIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, openingVersion, fixVersion, injectVersion, jiraCreationDate, jiraResolutionDate, hasBeenProportioned, Arrays.hashCode(versionsIndex));
    }

    public boolean isHasBeenProportioned() {
        return hasBeenProportioned;
    }

    public void setHasBeenProportioned(boolean hasBeenProportioned) {
        this.hasBeenProportioned = hasBeenProportioned;
    }
}
