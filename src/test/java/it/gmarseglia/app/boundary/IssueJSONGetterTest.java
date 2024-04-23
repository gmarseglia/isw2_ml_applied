package it.gmarseglia.app.boundary;

import it.gmarseglia.app.controller.MyLogger;
import org.junit.Test;

import static org.junit.Assert.*;

public class IssueJSONGetterTest {

    @Test
    public void getIssueJSONCacheTest() {
        String projName = "OPENJPA";

        IssueJSONGetter issueJSONGetter = new IssueJSONGetter(projName);

        MyLogger.setStaticVerbose(true);
        MyLogger.setStaticVerboseFine(true);

        issueJSONGetter.getIssueJSON(1000, 1000);
    }

}