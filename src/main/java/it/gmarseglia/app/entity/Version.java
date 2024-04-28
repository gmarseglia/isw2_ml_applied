package it.gmarseglia.app.entity;

import java.util.Date;
import java.util.Objects;

public class Version {

    private final String id;
    private String name;
    private Date GithubReleaseDate;
    private Date JiraReleaseDate;
    private final boolean released;

    public Version(String id, String name, Date githubReleaseDate, Date jiraReleaseDate, boolean released) {
        this.id = id;
        this.name = name;
        GithubReleaseDate = githubReleaseDate;
        JiraReleaseDate = jiraReleaseDate;
        this.released = released;
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

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", GithubReleaseDate=" + GithubReleaseDate +
                ", JiraReleaseDate=" + JiraReleaseDate +
                ", released=" + released +
                '}';
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Version version = (Version) object;
        return released == version.released && Objects.equals(id, version.id) && Objects.equals(name, version.name) && Objects.equals(GithubReleaseDate, version.GithubReleaseDate) && Objects.equals(JiraReleaseDate, version.JiraReleaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, GithubReleaseDate, JiraReleaseDate, released);
    }
}
