package gitlet;
import java.util.HashSet;
import java.util.Set;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class History implements Serializable {
    public HashMap<String, Commit> allCommits;
    public HashMap<String, String> allBranches;
    public HashMap<String, byte[]> stagedFiles;
    public HashMap<String, byte[]> trackedFiles;
    public Set<String> removedFiles;
    public Set<String> markedForRemove;
    public File headFile;
    public File currentCommitFile;
    public File stagingFile;

    public History() {
        File history = new File(".gitlet/history/" + "history.txt");
        this.allCommits = new HashMap<>();
        this.allBranches = new HashMap<>();
        this.stagedFiles = new HashMap<>();
        this.trackedFiles = new HashMap<>();
        this.markedForRemove = new HashSet<>();
        this.removedFiles = new HashSet<>();
    }

    public File getHeadBranch() { return headFile; }

    public File getCurrentCommit() { return currentCommitFile; }

    public File getStagingFile() {return stagingFile; }

    public void setHeadBranch(File setbranch) { headFile = setbranch; }

    public void setCurrentCommit(File setcommit) { currentCommitFile = setcommit; }

    public void setStagingFile (File setstaging) {stagingFile = setstaging; }

    public HashMap getTrackedFiles() {return trackedFiles; }

    public Set getMarkedForRemove() {return markedForRemove; }

    public HashMap getAllCommits() {return allCommits; }

    public HashMap getAllBranches() {return allBranches; }

    public HashMap getStagedFiles() {return stagedFiles; }
}
