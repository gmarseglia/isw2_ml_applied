package it.gmarseglia.app.entity;

import java.util.Date;
import java.util.Objects;

public class Issue {

    private String key;
    private Version openingVersion;
    private Version fixVersion;
    private Version injectVersion;
    private Date jiraCreationDate;
    private Date jiraResolutionDate;

    public Issue(String key, Version openingVersion, Version fixVersion, Version injectVersion) {
        this.key = key;
        this.openingVersion = openingVersion;
        this.fixVersion = fixVersion;
        this.injectVersion = injectVersion;
    }

    public Issue(String key, Version openingVersion, Version fixVersion, Version injectVersion, Date jiraCreationDate, Date jiraResolutionDate) {
        this.key = key;
        this.openingVersion = openingVersion;
        this.fixVersion = fixVersion;
        this.injectVersion = injectVersion;
        this.jiraCreationDate = jiraCreationDate;
        this.jiraResolutionDate = jiraResolutionDate;
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
                '}';
    }
}
