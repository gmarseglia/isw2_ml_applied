package it.gmarseglia.app.entity;

import java.util.List;

public class Project {

    private String key;
    private List<JiraVersion> versions;

    public Project(String key, List<JiraVersion> versions) {
        this.key = key;
        this.versions = versions;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<JiraVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<JiraVersion> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return "Project{" +
                "key='" + key + '\'' +
                ", versions=" + versions +
                '}';
    }
}
