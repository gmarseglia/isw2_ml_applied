package it.gmarseglia.app.model;

import java.util.List;

public class Project {

    private String key;
    private List<Version> versions;

    public Project(String key, List<Version> versions) {
        this.key = key;
        this.versions = versions;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
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
