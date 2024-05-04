package it.gmarseglia.app.entity;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class JiraIssue {

    public static class Fields {
        private Date resolutiondate;
        private Date created;
        private List<JiraVersion> versions;
        private List<JiraVersion> fixVersions;

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

        public List<JiraVersion> getVersions() {
            return versions;
        }

        public void setVersions(List<JiraVersion> versions) {
            this.versions = versions;
        }

        public List<JiraVersion> getFixVersions() {
            return fixVersions;
        }

        public JiraVersion getOldestAffectedVersion () {
            return  this.getVersions().stream()
                    .filter(version -> version.getReleaseDate() != null)
                    .min(Comparator.comparing(JiraVersion::getReleaseDate))
                    .orElse(null);
        }
        public JiraVersion getOldestAFixVersion () {
            return  this.getFixVersions().stream()
                    .filter(version -> version.getReleaseDate() != null)
                    .min(Comparator.comparing(JiraVersion::getReleaseDate))
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
