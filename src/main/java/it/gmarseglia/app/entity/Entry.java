package it.gmarseglia.app.entity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Entry implements Exportable {

    private final String longName;
    private final Metrics metrics;
    private Path path;
    private Version version;
    private String name;
    private boolean buggy;

    public Entry(Path path, Version version, String longName) {
        this.path = path;
        this.version = version;
        this.name = path.getFileName().toString();
        this.buggy = false;
        this.longName = longName;
        this.metrics = new Metrics();
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

    public boolean isNotBuggy() {
        return !buggy;
    }

    public String getLongName() {
        return longName;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return "{" +
                path +
                ", " + version.getName() +
                ", " + buggy +
                '}';
    }

    public String toShortString() {
        return "Entry{" + version.getName() + ", " + path + "}";
    }

    @Override
    public List<String> getFieldsNames() {
        return Arrays.asList("Version", "Name",
                "LOC",
                "Age", "StepAge",
                "NR", "NAuth",
                "LOCAdded", "maxLOCAdded", "avgLOCAdded",
                "Churn", "maxChurn", "avgChurn",
                "ChangeSetSize", "maxChangeSetSize", "avgChangeSetSize",
                "Buggy");
    }

    @Override
    public List<Serializable> getFieldsValues() {
        return Arrays.asList(this.version.getName(), this.longName,
                this.metrics.getLOC(),
                this.metrics.getAge(), this.metrics.getStepAge(),
                this.metrics.getNR(), this.metrics.getNAuth(),
                this.metrics.getLOCAdded(), this.metrics.getMaxLOCAdded(), this.metrics.getAvgLOCAdded(),
                this.metrics.getChurn(), this.metrics.getMaxChurn(), this.metrics.getAvgChurn(),
                this.metrics.getChangeSetSize(), this.metrics.getMaxChangeSetSize(), this.metrics.getAvgChangeSetSize(),
                this.buggy);
    }
}
