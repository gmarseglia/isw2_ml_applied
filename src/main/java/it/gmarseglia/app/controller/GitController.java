package it.gmarseglia.app.controller;

import it.gmarseglia.app.entity.Issue;
import it.gmarseglia.app.entity.JiraVersion;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class GitController {

    private static final Map<String, GitController> instances = new HashMap<>();

    private final String projName;
    private final MyLogger logger = MyLogger.getInstance(GitController.class);
    private final String repoUrl;
    private final Path localPath;
    private String tagsRegex = "%v";
    private String lastTag = "";


    private GitController(String projName) {
        this.projName = projName;
        String repoBase = "https://github.com/apache/%s.git";
        this.repoUrl = String.format(repoBase, projName);
        this.localPath = Paths.get(System.getProperty("java.io.tmpdir"), projName);
    }

    public static GitController getInstance(String projName) {
        GitController.instances.computeIfAbsent(projName, GitController::new);
        return GitController.instances.get(projName);
    }

    public List<Path> getAllPathByCommit(RevCommit revCommit) throws GitAPIException {
        List<Path> result = new ArrayList<>();

        logger.logFinest(String.format("Getting all paths for commit: %s", revCommit.getId()));

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
                Path fullPath = getLocalPath().resolve(diff.getNewPath());
                result.add(fullPath);
                logger.logFinest(String.format("diff: %s|%s|%s",
                        diff.getChangeType().name(),
                        diff.getNewMode().getBits(),
                        fullPath));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public List<DiffEntry> getDiffListByRevCommit(RevCommit revCommit) throws GitAPIException {
        List<DiffEntry> diffs;

        logger.logFinest(String.format("Getting all DiffEntries for commit: %s", revCommit.getId()));

        /* https://www.eclipse.org/forums/index.php/t/213979/ */
        RevWalk rw = new RevWalk(this.getLocalGit().getRepository());
        try {
            RevCommit parent = rw.parseCommit(revCommit.getParent(0).getId());

            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(this.getLocalGit().getRepository());
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);

            diffs = df.scan(parent.getTree(), revCommit.getTree());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return diffs;
    }


    /**
     * Looks for all commits which contains {@code issue.getKey()} in their full message.
     */
    public List<RevCommit> getAllCommitsByIssue(Issue issue) throws GitAPIException {
        String issueID = issue.getKey();

        logger.logFinest(String.format("Getting all commits for issue: %s", issueID));

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
        if (!Objects.equals(tag, this.lastTag)) {
            this.lastTag = tag;
            Git git = this.getLocalGit();
            int tries = 0;
            while (tries < 2) {
                try {
                    git.checkout().setName("refs/tags/" + tag).call();
                    return;
                } catch (CheckoutConflictException e) {
                    this.cloneRepo();
                    tries++;
                }
            }
        }
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
        // if .git in the expected dir is not present, then clone the repo
        Path localGitPath = localPath.resolve(".git");
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

        logger.log("Cloning repository...");

        // clone the repository
        Git.cloneRepository().setURI(repoUrl).setDirectory(localPath.toFile()).call();

        logger.log("Done.");
    }

    /**
     * As suggested in <a href="https://stackoverflow.com/a/75916692/10494676">...</a>.
     */
    public List<RevCommit> getRevCommitsFromPath(Path path) throws GitAPIException {
        List<RevCommit> result = new ArrayList<>();
        List<String> commitsID = new ArrayList<>();

        Repository repository = this.getLocalGit().getRepository();

        Path fullPath;

        // prefix only if necessary
        if (!path.toString().contains(this.localPath.toString())) {
            fullPath = Paths.get(this.localPath.toString(), path.toString());
        } else {
            fullPath = path;
        }


        // https://stackoverflow.com/a/34666649/10494676
        String[] commands = {"git", "log", "--all", "--first-parent", "--remotes", "--reflog", "--author-date-order", "--pretty=format:\"%H\"", "--follow", "--", fullPath.toString()};

        logger.logFinest(String.join(" ", commands));

        Runtime rt = Runtime.getRuntime();

        try {
            Process proc = rt.exec(commands, null, this.localPath.toFile());

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String cmdResult;

            while ((cmdResult = stdInput.readLine()) != null) {
                commitsID.add(cmdResult.replace("\"", ""));
            }

            // https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/api/WalkAllCommits.java
            // a RevWalk allows to walk over commits based on some filtering that is defined
            Collection<Ref> allRefs = repository.getRefDatabase().getRefs();
            try (RevWalk revWalk = new RevWalk(repository)) {

                for (Ref ref : allRefs) {
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }

                for (RevCommit commit : revWalk) {
                    if (commitsID.contains(commit.getName())) {
                        result.add(commit);
                    }
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        logger.logFinest(String.format("Found commits by \"git log ...\": %s, found by \"jgit\": %s", commitsID.size(), result.size()));

        return result;
    }

    public long[] getLOCModifiedByDiff(DiffEntry diff) throws GitAPIException {
        long[] result = {0, 0};

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(this.getLocalGit().getRepository());
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        // https://stackoverflow.com/a/38947015/10494676

        int linesAdded = 0;
        int linesDeleted = 0;

        try {
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                linesAdded += edit.getEndB() - edit.getBeginB();
                linesDeleted += edit.getEndA() - edit.getBeginA();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        result[0] = linesAdded;
        result[1] = linesDeleted;

        return result;
    }

    public RevCommit getFirstCommit() throws GitAPIException {
        RevCommit firstCommit;

        try (Repository repository = this.getLocalGit().getRepository()) {
            try (RevWalk revWalk = new RevWalk(repository)) {
                // Start from the HEAD commit
                RevCommit headCommit = revWalk.parseCommit(repository.resolve("HEAD"));

                // Sort commits in reverse chronological order (i.e., newest first)
                revWalk.sort(RevSort.REVERSE);

                // Start walking backward through the commit history
                revWalk.markStart(headCommit);

                // Get the first commit
                firstCommit = revWalk.next();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return firstCommit;
    }
}
