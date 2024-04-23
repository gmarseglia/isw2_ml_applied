package it.gmarseglia.app.entity;

import java.util.Date;
import java.util.Objects;

public class Version {

    private String name;
    private Date releaseDate;
    private Date JiraReleaseDate;

    public Version(String name, Date releaseDate) {
        this.name = name;
        this.releaseDate = releaseDate;
    }

    public Version(String name, Date releaseDate, Date jiraReleaseDate) {
        this.name = name;
        this.releaseDate = releaseDate;
        JiraReleaseDate = jiraReleaseDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getJiraReleaseDate() {
        return JiraReleaseDate;
    }

    public void setJiraReleaseDate(Date jiraReleaseDate) {
        JiraReleaseDate = jiraReleaseDate;
    }

    @Override
    public String toString() {
        return "Version{" +
                "name='" + name + '\'' +
                ", releaseDate=" + releaseDate +
                ", JiraReleaseDate=" + JiraReleaseDate +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Version version = (Version) object;
        return Objects.equals(name, version.name) && Objects.equals(releaseDate, version.releaseDate) && Objects.equals(JiraReleaseDate, version.JiraReleaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, releaseDate, JiraReleaseDate);
    }
}
