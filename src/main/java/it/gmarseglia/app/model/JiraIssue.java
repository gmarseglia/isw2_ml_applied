package it.gmarseglia.app.model;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class JiraIssue {

    public static class Fields {
        private Date resolutiondate;
        private Date created;
        private List<Version> versions;

        public Date getResolutiondate() {
            return resolutiondate;
        }

        public void setResolutiondate(Date resolutiondate) {
            this.resolutiondate = resolutiondate;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        public List<Version> getVersions() {
            return versions;
        }

        public void setVersions(List<Version> versions) {
            this.versions = versions;
        }

        public Version getOldestAffectedVersion () {
            return  this.getVersions().stream()
                    .filter(version -> version.getReleaseDate() != null)
                    .min(Comparator.comparing(Version::getReleaseDate))
                    .orElse(null);
        }

        @Override
        public String toString() {
            return "Fields{" +
                    "resolutiondate=" + resolutiondate +
                    ", created=" + created +
                    ", versions=" + versions +
                    '}';
        }
    }

    private String key;
    private Fields fields;

    public JiraIssue(String key, Fields fields) {
        this.key = key;
        this.fields = fields;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Fields getFields() {
        return fields;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "key='" + key + '\'' +
                ", fields=" + fields +
                '}';
    }
}
