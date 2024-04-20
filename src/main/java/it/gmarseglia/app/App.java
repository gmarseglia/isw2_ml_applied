package it.gmarseglia.app;

import it.gmarseglia.app.boundary.CsvEntryBoundary;
import it.gmarseglia.app.controller.DatasetController;
import it.gmarseglia.app.controller.EntriesController;
import it.gmarseglia.app.controller.GitController;
import it.gmarseglia.app.controller.ProjectController;
import it.gmarseglia.app.model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;


public class App 
{
    public static void main( String[] args ) {
        String projName = "OPENJPA";

        run(projName);
    }

    private static void test(String projName) {
        GitController gc = new GitController(projName);

        try {
            System.out.println(gc.listTags());
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static void run(String projName) {

        DatasetController dc = new DatasetController(projName);

        try {
            dc.populateDataset(true);
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
