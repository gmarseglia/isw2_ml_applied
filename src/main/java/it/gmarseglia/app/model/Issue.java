package it.gmarseglia.app.model;

public class Issue {

    private String key;
    private Version openingVersion;
    private Version fixVersion;
    private Version injectVersion;

    public Issue(String key, Version openingVersion, Version fixVersion, Version injectVersion) {
        this.key = key;
        this.openingVersion = openingVersion;
        this.fixVersion = fixVersion;
        this.injectVersion = injectVersion;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "key='" + key + '\'' +
                ", openingVersion=" + openingVersion +
                ", fixVersion=" + fixVersion +
                ", injectVersion=" + injectVersion +
                '}';
    }
}
