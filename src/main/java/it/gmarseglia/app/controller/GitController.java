package it.gmarseglia.app.controller;

import it.gmarseglia.app.model.JiraVersion;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GitController {

    private static final Map<String, GitController> instances = new HashMap<>();

    private final String repoUrl;
    private final Path localPath;

    private GitController(String projName) {
        String repoBase = "https://github.com/apache/%s.git";
        this.repoUrl = String.format(repoBase, projName);
        this.localPath = Paths.get(System.getProperty("java.io.tmpdir"), projName);
    }

    public static GitController getInstance(String projName) {
        GitController.instances.computeIfAbsent(projName, GitController::new);
        return GitController.instances.get(projName);
    }

    /**
     * @param jiraVersion Version from Jira.
     * @return If a corresponding tag exists: the date of the last commit; else {@code null}
     */
    public Date getVersionGitDate(JiraVersion jiraVersion) throws GitAPIException {
        try {
            this.checkoutByTag(jiraVersion.getName());
            RevCommit lastCommit = this.getLastCommit();
            return new Date(lastCommit.getCommitTime() * 1000L);
        } catch (RefNotFoundException e) {
            return null;
        }
    }

    /**
     * Checks out to a specific tag.
     *
     * @param tag Tag to check out to, can be {@code Version.getName()}
     */
    public void checkoutByTag(String tag) throws GitAPIException {
        Git git = this.getLocalGit();
        git.checkout().setName("refs/tags/" + tag).call();
    }

    /**
     * @return A list all tags present on GitHub.
     */
    public List<String> listTags() throws GitAPIException {
        Git git = this.getLocalGit();

        // List all tags on git
        List<Ref> tagsRef = git.tagList().call();

        // map Ref to String
        List<String> tagsStr = new ArrayList<>();
        tagsRef.forEach(ref -> tagsStr.add(ref.getName().replace("refs/tags/", "")));

        return tagsStr;
    }

    /**
     * @return The path of local cloned repo. {@code System.getProperty("java.io.tmpdir")} is used.
     */
    public Path getLocalPath() {
        return this.localPath;
    }

    /**
     * @return The last commit of the last check-out.
     */
    private RevCommit getLastCommit() throws GitAPIException {
        return getLocalGit().log().setMaxCount(1).call().iterator().next();
    }

    /**
     * If the repo is not present on local, then is cloned.
     *
     * @return {@code Git} object of the local repo.
     */
    private Git getLocalGit() throws GitAPIException {
        Path localGitPath = localPath.resolve(".git");

        // if .git in the expected dir is not present, then clone the repo
        if (!Files.exists(localGitPath)) this.cloneRepo();

        try {
            // open the local git repo
            Repository repository = FileRepositoryBuilder.create(localGitPath.toFile());
            return new Git(repository);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open local Git");
        }
    }

    /**
     * Clone the repo.
     * Should only be called from {@code getLocalGit}.
     */
    private void cloneRepo() throws GitAPIException {
        // delete dir
        MyFileUtils.deleteDirectory(localPath);

        System.out.print("Cloning...");

        // clone the repository
        Git.cloneRepository().setURI(repoUrl).setDirectory(localPath.toFile()).call();

        System.out.print("Done.\n");
    }
}
