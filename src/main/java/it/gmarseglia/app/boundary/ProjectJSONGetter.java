package it.gmarseglia.app.boundary;


import it.gmarseglia.app.controller.JsonCacheUtils;

public class ProjectJSONGetter {

    private final String projName;
    private static final String URL_BASE = "https://issues.apache.org/jira/rest/api/2/project/";

    public ProjectJSONGetter(String projName) {
        this.projName = projName;
    }

    public String getUrl() {
        return URL_BASE + this.projName;
    }

    public String getJSONFromResources() {
        String targetUrl = this.getUrl();
        String targetFile = String.format("%s_project.json", this.projName);

        return JsonCacheUtils.getStringFromResourcesThenURL(targetFile, targetUrl);
    }

}
