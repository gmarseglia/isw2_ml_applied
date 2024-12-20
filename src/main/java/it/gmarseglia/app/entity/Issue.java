package it.gmarseglia.app.entity;

import it.gmarseglia.app.exceptions.CustomRuntimeException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Issue implements Exportable {

    private final Integer[] versionsIndex;
    private IssueFVType fvType;
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
        if (versionsIndex.length != 4) throw new CustomRuntimeException("4 Integer has to be given");
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

    public Integer[] getVersionsIndex() {
        return versionsIndex;
    }

    public Integer getOVIndex() {
        return this.versionsIndex[0];
    }

    public Integer getFVIndex() {
        return this.versionsIndex[1];
    }

    public Integer getIVIndex() {
        return this.versionsIndex[2];
    }

    public Integer getPredictedIVIndex() {
        return this.versionsIndex[3];
    }

    public void setPredictedIVIndex(Integer newIVIndex) {
        this.setHasBeenProportioned(true);
        this.versionsIndex[3] = newIVIndex;
    }

    public void setFvType(IssueFVType fvType) {
        this.fvType = fvType;
    }

    public IssueFVType getFvType() {
        return fvType;
    }

    @Override
    public List<String> getFieldsNames() {
        return Arrays.asList(
                "Key",
                "indexOf(OV)",
                "indexOf(FV)",
                "indexOf(IV explicit)",
                "indexOf(IV proportion)",
                "OV",
                "FV",
                "IV explicit",
                "Jira Creation Date",
                "Jira Resolution Date",
                "FV Source",
                "Has been proportioned");
    }

    @Override
    public List<Serializable> getFieldsValues() {
        Stream<Serializable> tmp = Stream.of(
                key,
                (getOVIndex() == null ? "null" : getOVIndex()),
                (getFVIndex() == null ? "null" : getFVIndex()),
                (getIVIndex() == null ? "null" : getIVIndex()),
                (getPredictedIVIndex() == null ? "null" : getPredictedIVIndex()),
                (openingVersion == null ? "last" : openingVersion.getName()),
                (fixVersion == null ? "last" : fixVersion.getName()),
                (injectVersion == null ? "NA" : injectVersion.getName()),
                jiraCreationDate,
                jiraResolutionDate,
                fvType.toString(),
                hasBeenProportioned
        );

        return tmp.toList();
    }
}
