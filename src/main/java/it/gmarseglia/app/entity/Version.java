package it.gmarseglia.app.entity;

import java.util.Date;
import java.util.Objects;

public class Version {

    private final String id;
    private final boolean released;
    private final String GithubTag;
    private String name;
    private Date JiraReleaseDate;
    private Date GithubReleaseDate;

    public Version(String id, boolean released, String name, Date jiraReleaseDate, Date githubReleaseDate, String githubTag) {
        this.id = id;
        this.released = released;
        this.name = name;
        JiraReleaseDate = jiraReleaseDate;
        GithubReleaseDate = githubReleaseDate;
        GithubTag = githubTag;
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
        return GithubReleaseDate;
    }

    public void setGithubReleaseDate(Date githubReleaseDate) {
        this.GithubReleaseDate = githubReleaseDate;
    }

    public Date getJiraReleaseDate() {
        return JiraReleaseDate;
    }

    public void setJiraReleaseDate(Date jiraReleaseDate) {
        JiraReleaseDate = jiraReleaseDate;
    }

    public boolean isReleased() {
        return released;
    }

    public String toShortString() {
        return "v{" + name + ", " + getJiraReleaseDate() + "}";
    }

    public String getGithubTag() {
        return GithubTag;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", released=" + released +
                ", name='" + name + '\'' +
                ", JiraReleaseDate=" + JiraReleaseDate +
                ", GithubReleaseDate=" + GithubReleaseDate +
                ", GithubTag='" + GithubTag + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Version version = (Version) object;
        return released == version.released && Objects.equals(id, version.id) && Objects.equals(name, version.name) && Objects.equals(GithubTag, version.GithubTag) && Objects.equals(GithubReleaseDate, version.GithubReleaseDate) && Objects.equals(JiraReleaseDate, version.JiraReleaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, GithubTag, GithubReleaseDate, JiraReleaseDate, released);
    }
}
