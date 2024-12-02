package it.gmarseglia.app.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Version implements Exportable {

    private final String id;
    private final boolean released;
    private final String githubTag;
    private String name;
    private Date jiraReleaseDate;
    private Date githubReleaseDate;

    public Version(String id, boolean released, String name, Date jiraReleaseDate, Date githubReleaseDate, String githubTag) {
        this.id = id;
        this.released = released;
        this.name = name;
        this.jiraReleaseDate = jiraReleaseDate;
        this.githubReleaseDate = githubReleaseDate;
        this.githubTag = githubTag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getGithubReleaseDate() {
        return githubReleaseDate;
    }

    public void setGithubReleaseDate(Date githubReleaseDate) {
        this.githubReleaseDate = githubReleaseDate;
    }

    public Date getJiraReleaseDate() {
        return jiraReleaseDate;
    }

    public void setJiraReleaseDate(Date jiraReleaseDate) {
        this.jiraReleaseDate = jiraReleaseDate;
    }

    public boolean isReleased() {
        return released;
    }

    public String toShortString() {
        return "v{" + name + ", " + getJiraReleaseDate() + "}";
    }

    public String getGithubTag() {
        return githubTag;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", released=" + released +
                ", name='" + name + '\'' +
                ", JiraReleaseDate=" + (jiraReleaseDate == null ? "null" : jiraReleaseDate) +
                ", GithubReleaseDate=" + (githubReleaseDate == null ? "null" : githubReleaseDate) +
                ", GithubTag='" + (githubTag == null ? "null" : githubTag) + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Version version = (Version) object;
        return released == version.released && Objects.equals(id, version.id) && Objects.equals(name, version.name) && Objects.equals(githubTag, version.githubTag) && Objects.equals(githubReleaseDate, version.githubReleaseDate) && Objects.equals(jiraReleaseDate, version.jiraReleaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, githubTag, githubReleaseDate, jiraReleaseDate, released);
    }

    @Override
    public List<String> getFieldsNames() {
        return Arrays.asList("Name", "ID", "Released", "Jira ReleaseDate", "Github tag", "GitHub ReleaseDate");
    }

    @Override
    public List<Serializable> getFieldsValues() {
        return Arrays.asList(
                name,
                id,
                released,
                jiraReleaseDate,
                githubTag,
                githubReleaseDate);
    }
}
