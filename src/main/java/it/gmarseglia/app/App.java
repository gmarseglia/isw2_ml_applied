package it.gmarseglia.app;

import it.gmarseglia.app.boundary.IssueJSONGetter;
import it.gmarseglia.app.controller.DatasetController;
import it.gmarseglia.app.controller.GitController;
import it.gmarseglia.app.controller.IssueController;
import it.gmarseglia.app.controller.ProjectController;
import it.gmarseglia.app.model.Issue;
import it.gmarseglia.app.model.JiraIssueReport;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;


public class App 
{
    public static void main( String[] args ) {
        String projName = "OPENJPA";

        test(projName);
    }

    private static void test(String projName) {

        ProjectController pc = new ProjectController(projName);

        pc.getProject().getVersions().forEach(System.out::println);

        GitController gc = GitController.getInstance(projName);

        try {
            gc.listTags().forEach(System.out::println);
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
//
//        IssueController ic = IssueController.getInstance(projName);
//
//        List<Issue> issues = null;
//        try {
//            issues = ic.getAllIssues(true);
//        } catch (GitAPIException | IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        issues.forEach(System.out::println);
    }

    private static void run(String projName) {

        DatasetController dc = DatasetController.getInstance(projName);

        try {
            dc.populateDataset(true);
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
