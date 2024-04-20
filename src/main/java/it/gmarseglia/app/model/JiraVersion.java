package it.gmarseglia.app.model;

import java.util.Date;

public class JiraVersion {

    private String id;
    private String name;
    private boolean released;
    private Date releaseDate;

    public JiraVersion(String id, String name, boolean released, Date releaseDate) {
        this.id = id;
        this.name = name;
        this.released = released;
        this.releaseDate = releaseDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", released=" + released +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
