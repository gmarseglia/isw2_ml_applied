package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.JiraVersion;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

public class GitController {

    private static final Map<String, GitController> instances = new HashMap<>();
    private final MyLogger logger;
    private final String repoUrl;
    private final Path localPath;
    private String tagsRegex = "%v";

    private GitController(String projName) {
        String repoBase = "https://github.com/apache/%s.git";
        this.repoUrl = String.format(repoBase, projName);
        this.localPath = Paths.get(System.getProperty("java.io.tmpdir"), projName);
        this.logger = MyLogger.getInstance(this.getClass());
    }

    public static GitController getInstance(String projName) {
        GitController.instances.computeIfAbsent(projName, GitController::new);
        return GitController.instances.get(projName);
    }

    public List<Path> getAllPathByCommit(RevCommit revCommit) throws GitAPIException {
        List<Path> result = new ArrayList<>();

        logger.logFinest(() -> System.out.printf("Getting all paths for commit: %s\n", revCommit.getId()));

        /* https://www.eclipse.org/forums/index.php/t/213979/ */
        RevWalk rw = new RevWalk(this.getLocalGit().getRepository());
        RevCommit parent;
        try {
            parent = rw.parseCommit(revCommit.getParent(0).getId());
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(this.getLocalGit().getRepository());
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs = df.scan(parent.getTree(), revCommit.getTree());
            for (DiffEntry diff : diffs) {
                result.add(Paths.get(diff.getNewPath()));
                logger.log(() ->
                        System.out.println(MessageFormat.format("({0} {1} {2}",
                                diff.getChangeType().name(),
                                diff.getNewMode().getBits(),
                                diff.getNewPath())));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Looks for all commits which contains {@code issue.getKey()} in their full message.
     */
    public List<RevCommit> getAllCommitsByIssue(Issue issue) throws GitAPIException {
        String issueID = issue.getKey();

        logger.logFinest(() -> System.out.printf("Getting all commits for issue: %s\n", issueID));

        List<RevCommit> result = new ArrayList<>();

        try {
            for (RevCommit revCommit : this.getLocalGit().log().all().call()) {
                if (revCommit.getFullMessage().contains(issueID))
                    result.add(revCommit);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public void setTagsRegex(String tagsRegex) {
        this.tagsRegex = tagsRegex;
    }

    public String getTagByNameRegex(String targetVersionName) throws GitAPIException {
        Pattern pattern = Pattern.compile(tagsRegex.replace("%v", targetVersionName).replace(".", "\\."));
        List<String> tags = this.listTags();

        return tags.stream()
                .filter(string -> pattern.matcher(string).matches())
                .findFirst()
                .orElse(null);
    }

    /**
     * @param jiraVersion Version from Jira.
     * @return If a corresponding tag exists: the date of the last commit; else {@code null}
     */
    public Date getVersionGitDate(JiraVersion jiraVersion) throws GitAPIException {
        String targetTag = this.getTagByNameRegex(jiraVersion.getName());
        try {
            if (targetTag == null) throw new RuntimeException("Tag not found with regex");
            this.checkoutByTag(targetTag);
            RevCommit lastCommit = this.getLastCommit();
            return new Date(lastCommit.getCommitTime() * 1000L);
        } catch (RefNotFoundException | RuntimeException e) {
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

        logger.log(() -> System.out.print("Cloning..."));

        // clone the repository
        Git.cloneRepository().setURI(repoUrl).setDirectory(localPath.toFile()).call();

        logger.log(() -> System.out.print("Done.\n"));
    }
}
