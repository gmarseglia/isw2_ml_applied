package it.gmarseglia.app.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GitController {

    private final String projName;
    private final String repoBase = "https://github.com/apache/%s.git";
    private String repoUrl;
    private Path localPath;

    public GitController(String projName) {
        this.projName = projName;
        this.repoUrl = String.format(repoBase, projName);
        this.localPath = Paths.get(System.getProperty("java.io.tmpdir"), this.projName);
    }

    public void cloneRepo() throws GitAPIException {
        // delete dir
        MyFileUtils.deleteDirectory(localPath);

        System.out.print("Cloning...");

        // clone the repository
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath.toFile())
                .call();

        System.out.print("Done.\n");
    }

    public void checkoutByTag(String tag) throws IOException, GitAPIException {
        Git git = this.getLocalGit();
        git.checkout().setName("refs/tags/" + tag).call();
    }

    public List<String> listTags() throws IOException, GitAPIException {
        Git git = this.getLocalGit();

        // List all tags on git
        List<Ref> tagsRef = git.tagList().call();

        // map Ref to String
        List<String> tagsStr = new ArrayList<>();
        tagsRef.forEach(ref -> tagsStr.add(ref.getName().replace("refs/tags/","")));

        return tagsStr;
    }

    private Git getLocalGit() throws GitAPIException {
        Repository repository;
        Path localGitPath = localPath.resolve(".git");

        // if .git in the expected dir is not present, then clone the repo
        if (!Files.exists(localGitPath)) this.cloneRepo();

        try {
            // open the local git repo
            repository = FileRepositoryBuilder.create(localGitPath.toFile());
            return new Git(repository);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open local Git");
        }
    }

    public Path getLocalPath() {
        return this.localPath;
    }
}
