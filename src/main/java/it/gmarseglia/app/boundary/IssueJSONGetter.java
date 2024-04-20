package it.gmarseglia.app.boundary;

import com.google.gson.Gson;
import it.gmarseglia.app.model.JiraIssueReport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

public class IssueJSONGetter {

    private final String urlBase = "https://issues.apache.org/jira/rest/api/2/search?" +
            "jql=QUERY" +
            "&startAt=START_AT" +
            "&maxResults=MAX_RESULT";
    private final String query = "project = PROJ_NAME" +
            " AND issueType = Bug" +
            " AND ( status = closed OR status = resolved)" +
            " AND resolution = fixed";

    private final String projName;

    public IssueJSONGetter(String projName) {
        this.projName = projName;
    }

    private String buildQuery() {
        return this.query
                .replace("PROJ_NAME", this.projName);
    }

    private String buildURL(int startAt, int maxResult) {
        return this.urlBase
                .replace("QUERY", this.buildQuery())
                .replace("START_AT", String.valueOf(startAt))
                .replace("MAX_RESULT", String.valueOf(maxResult))
                .replace(" ", "%20");
    }

    public String getIssueJSON(int startAt, int maxResult) {
        String textJson;
        InputStream isJson;

        try {
            isJson = new URL(this.buildURL(startAt, maxResult)).openStream();

            textJson = new String(isJson.readAllBytes(), Charset.defaultCharset());

            isJson.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return textJson;
    }

    public JiraIssueReport getIssueReport(int startAt, int maxResult) {
        Gson gson = new Gson();
        return gson.fromJson(this.getIssueJSON(startAt, maxResult), JiraIssueReport.class);
    }
}
