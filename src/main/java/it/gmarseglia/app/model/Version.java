package it.gmarseglia.app.model;

import java.util.Date;

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
}
