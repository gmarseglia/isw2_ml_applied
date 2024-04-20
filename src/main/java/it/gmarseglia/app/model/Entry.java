package it.gmarseglia.app.model;

import java.nio.file.Path;

public class Entry {

    private Path path;
    private Version version;
    private String name;
    private boolean buggy;

    public Entry(Path path, Version version) {
        this.path = path;
        this.version = version;
        this.name = path.getFileName().toString();
        this.buggy = false;
    }

    public String toCsvLine() {
        return this.version.getName() + "," +
                this.path + "," +
                buggy;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    private String getModule() {
        String tillSrc = this.getPath().toString().split("src")[0]
    }
}
