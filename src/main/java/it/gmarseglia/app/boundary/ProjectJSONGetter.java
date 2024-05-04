package it.gmarseglia.app.boundary;


import it.gmarseglia.app.controller.JsonCacheUtils;
import it.gmarseglia.app.controller.MyLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ProjectJSONGetter {

    private final MyLogger logger = MyLogger.getInstance(ProjectJSONGetter.class);
    private final String projName;
    private final String urlBase = "https://issues.apache.org/jira/rest/api/2/project/";

    public ProjectJSONGetter(String projName) {
        this.projName = projName;
    }

    public String getUrl() {
        return urlBase + this.projName;
    }

    public String getJSONFromResources() {
        String targetUrl = this.getUrl();
        String targetFile = String.format("%s_project.json", this.projName);

        return JsonCacheUtils.getStringFromResourcesThenURL(targetFile, targetUrl);
    }

}
