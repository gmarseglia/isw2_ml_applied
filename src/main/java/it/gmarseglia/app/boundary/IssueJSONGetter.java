package it.gmarseglia.app.boundary;

import com.google.gson.Gson;
import it.gmarseglia.app.controller.JsonCacheUtils;
import it.gmarseglia.app.controller.MyLogger;
import it.gmarseglia.app.entity.JiraIssueReport;

public class IssueJSONGetter {

    private static final String URL_BASE = "https://issues.apache.org/jira/rest/api/2/search?" +
            "jql=QUERY" +
            "&startAt=START_AT" +
            "&maxResults=MAX_RESULT";
    private static final  String QUERY = "project=PROJ_NAME" +
            " AND issueType=Bug" +
            " AND (status=closed OR status=resolved)" +
            " AND resolution=fixed";

    private final String projName;
    private final MyLogger logger = MyLogger.getInstance(IssueJSONGetter.class);

    public IssueJSONGetter(String projName) {
        this.projName = projName;
    }

    private String buildQuery() {
        return QUERY
                .replace("PROJ_NAME", this.projName);
    }

    private String buildURL(int startAt, int maxResult) {
        return URL_BASE
                .replace("QUERY", this.buildQuery())
                .replace("START_AT", String.valueOf(startAt))
                .replace("MAX_RESULT", String.valueOf(maxResult))
                .replace(" ", "%20");
    }

    public String getIssueJSON(int startAt, int maxResult) {
        String targetUrl = this.buildURL(startAt, maxResult);
        String targetFile = String.format("%s_issues-%d-%d.json", this.projName, startAt, maxResult);

        logger.logFine(String.format("IssueQuery: '%s'", this.buildQuery()));
        logger.logFinest("Issue query URL: " + targetUrl);

        return JsonCacheUtils.getStringFromResourcesThenURL(targetFile, targetUrl);
    }

    public JiraIssueReport getIssueReport(int startAt, int maxResult) {
        Gson gson = new Gson();
        return gson.fromJson(this.getIssueJSON(startAt, maxResult), JiraIssueReport.class);
    }
}
