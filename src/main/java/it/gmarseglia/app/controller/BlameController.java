package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;
import java.util.*;

public class BlameController {

    private static final Map<String, BlameController> instances = new HashMap<>();
    private final String projName;
    private final GitController gc;
    private final MyLogger myLogger = MyLogger.getInstance(BlameController.class);

    private BlameController(String projName) {
        this.projName = projName;
        this.gc = GitController.getInstance(projName);
    }

    public static BlameController getInstance(String projName) {
        BlameController.instances.computeIfAbsent(projName, string -> new BlameController(projName));
        return BlameController.instances.get(projName);
    }

    /**
     * 1. Looks for all the commits of a given issue.
     * 2. Filter commits after issue release date.
     * 2. For each commit, looks for every class touched by it.
     * 3. Aggregate and return the list.
     *
     * @param issue The issue from which to get the ID.
     * @return The list of all the entries without the version.
     */
    public List<Path> getAllPathsTouchedByIssue(Issue issue) throws GitAPIException {
        Set<Path> tmp = new HashSet<>();

        List<RevCommit> commits = gc.getAllCommitsByIssue(issue);

        myLogger.logFinest(() -> System.out.println("Found " + commits.size() + " commits, corresponding to issue " + issue.getKey() + "."));

        myLogger.logFinest(() ->
                commits.forEach(revCommit -> System.out.printf("ID: %s\n", revCommit.getId())
                ));

        /* Filter commits after FV GitHub release date */
        Iterator<RevCommit> iterator = commits
                .stream()
                // .filter(revCommit -> revCommit.getAuthorIdent().getWhen().before(issue.getFixVersion().getGithubReleaseDate()))
                .iterator();

        while (iterator.hasNext()) {
            gc.getAllPathByCommit(iterator.next())
                    .stream()
                    .filter(MyFileUtils::isJavaSrcFile)
                    .forEach(tmp::add);
        }

        return new ArrayList<>(tmp);
    }


}
