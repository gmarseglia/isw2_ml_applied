package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.JiraVersion;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

import static org.junit.Assert.*;

public class VersionsControllerTest {

    private static final MyLogger logger = MyLogger.getInstance(VersionsControllerTest.class);

    @Test
    public void VersionsComparingTest() throws GitAPIException {
        String projName = "BOOKKEEPER";
        MyLogger.setStaticVerboseFine(true);
        MyLogger.setStaticVerbose(true);

        VersionsController vc = VersionsController.getInstance(projName);
        GitController gc = GitController.getInstance(projName);

        logger.log(() -> System.out.println("\n\nAll Jira Versions:\n"));
        vc.getAllJiraVersions().forEach(logger::logObject);

        logger.log(() -> System.out.println("\n\nAll GitHub Tags:\n"));
        gc.listTags().forEach(logger::logObject);

        logger.log(() -> System.out.print("\n\nAll Versions:"));
        vc.getAllVersions().forEach(logger::logObject);

        logger.log(() -> System.out.println("\n\nAll Valid Versions:"));
        vc.getAllValidVersions().forEach(logger::logObject);

    }

}