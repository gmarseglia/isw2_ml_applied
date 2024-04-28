package it.gmarseglia.app.entity;

import java.util.Date;
import java.util.Objects;

public class Version {

    private String name;
    private Date GithubReleaseDate;
    private Date JiraReleaseDate;
    private boolean released;

    public Version(String name, Date GithubReleaseDate, Date jiraReleaseDate, boolean released) {
        this.name = name;
        this.GithubReleaseDate = GithubReleaseDate;
        this.JiraReleaseDate = jiraReleaseDate;
        this.released = released;
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
                "name='" + name + '\'' +
                ", GithubReleaseDate=" + GithubReleaseDate +
                ", JiraReleaseDate=" + JiraReleaseDate +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Version version = (Version) object;
        return Objects.equals(name, version.name) && Objects.equals(GithubReleaseDate, version.GithubReleaseDate) && Objects.equals(JiraReleaseDate, version.JiraReleaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, GithubReleaseDate, JiraReleaseDate);
    }
}
