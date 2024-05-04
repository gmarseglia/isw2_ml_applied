package it.gmarseglia.app.controller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

public class VersionsControllerTest {

    private static final MyLogger logger = MyLogger.getInstance(VersionsControllerTest.class);

    @Test
    public void VersionsComparingTest() throws GitAPIException {
        String projName = "BOOKKEEPER";
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerbose(true);

        VersionsController vc = VersionsController.getInstance(projName);
        GitController gc = GitController.getInstance(projName);

        logger.log("All Jira Versions:");
        vc.getAllJiraVersions().forEach(logger::logObject);

        logger.log("All GitHub Tags:");
        gc.listTags().forEach(logger::logObject);

        logger.log("All Versions:");
        vc.getAllVersions().forEach(logger::logObject);

        logger.log("All Valid Versions:");
        vc.getAllValidVersions().forEach(logger::logObject);

    }

}