package gitlet;
import gitlet.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.nio.file.Files;

public class Commands implements Serializable {

    static File history = new File(".gitlet/history/" + "history.txt");

    public void fileSerial(File someFile, Object someObject) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(someFile));
            out.writeObject(someObject);
            out.close();
        } catch (IOException excp) {
            throw new Error("Internal error serializing commit.");
        }

    }
    public Object fileDSerial(File someFile) {
        Object someObject;
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(someFile));
            someObject = (Object) inp.readObject();
            inp.close();
            return someObject;
        } catch (IOException | ClassNotFoundException excp) {
            someObject = null;
            return null;
        }

    }
    public byte[] byteSerial(File someFile) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(someFile);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw new Error("Internal error serializing commit.");
        }
    }


    public void init() {
        File gitlet = new File(".gitlet");
        if (!gitlet.exists()) {
            gitlet.mkdir();
            File staging = new File(".gitlet/staging/");
            staging.mkdir();
            File commits = new File(".gitlet/commits/");
            commits.mkdir();
            File branches = new File(".gitlet/branches/");
            branches.mkdir();
            File history = new File(".gitlet/history/");
            history.mkdir();
        } else {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            return;
        }
        Stage initstage = new Stage();
        if (initstage.stagedFiles.size() != 0) {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            return;
        }
        Branch master = new Branch("master");
        Commit currcommit = new Commit("initial commit", null, initstage.stagedFiles);
        master.commit = currcommit;
        Branch head = master;
        File commitFile = new File(".gitlet/commits/" + currcommit.commitHash + ".txt");
        File headFile = new File(".gitlet/branches/" + "head.txt");
        File stageFile = new File(".gitlet/staging/" + "stage.txt");
        fileSerial(commitFile, currcommit);
        fileSerial(headFile, head);
        fileSerial(stageFile, initstage);
        History gitHistory = new History();
        gitHistory.setCurrentCommit(commitFile);
        gitHistory.setHeadBranch(headFile);
        gitHistory.setStagingFile(stageFile);
        gitHistory.allCommits.put(currcommit.commitHash, currcommit);
        gitHistory.allBranches.put(master.name, currcommit.commitHash);
        fileSerial(history, gitHistory);
    }

    public void add(String name) {
        File workingDirectoryFile = Utils.join(System.getProperty("user.dir"), name);
        if (!workingDirectoryFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        History gitHistory = (History) fileDSerial(history);
        Stage theStage = (Stage) fileDSerial(gitHistory.getStagingFile());
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        String cFileSha = head.commit.fileMap.get(name);
        String workingString = Utils.sha1(Utils.readContents(workingDirectoryFile));
        File headFile = new File(".gitlet/branches/" + "head.txt");
        if (gitHistory.markedForRemove.contains(name)) {
            gitHistory.markedForRemove.remove(name);
            fileSerial(history, gitHistory);
            return;
        }
        if (gitHistory.trackedFiles.containsKey(name)) {
            String trackedFileSha = Utils.sha1(gitHistory.trackedFiles.get(name));
            if (trackedFileSha.equals(workingString)) {
                fileSerial(history, gitHistory);
                return;
            }
        }
        if (workingString.equals(cFileSha)) {
            fileSerial(history, gitHistory);
            return;
        }
        gitHistory.stagedFiles.put(name, Utils.readContents(workingDirectoryFile));
        gitHistory.trackedFiles.put(name, Utils.readContents(workingDirectoryFile));
        File addFile = new File(".gitlet/staging/" + name);
        fileSerial(headFile, head);
        fileSerial(addFile, theStage);
        fileSerial(history, gitHistory);
    }

    public void commit(String message) {
        if (message.equals("") || message.equals(" ")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        History gitHistory = (History) fileDSerial(history);
        Commit currCommit = (Commit) fileDSerial(gitHistory.getCurrentCommit());
        Stage theStage = (Stage) fileDSerial(gitHistory.getStagingFile());
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (gitHistory.stagedFiles.isEmpty() && gitHistory.markedForRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        File headFile = new File(".gitlet/branches/" + "head.txt");
        Commit newestCommit = new Commit(message, currCommit.commitHash, gitHistory.stagedFiles);
        File commitFile = new File(".gitlet/commits/" + newestCommit.commitHash + ".txt");
        head.commit = newestCommit;
        gitHistory.allCommits.put(newestCommit.commitHash, newestCommit);
        gitHistory.allBranches.put(head.name, newestCommit.commitHash);
        for (String id : newestCommit.fileMap.keySet()) {
            if (!Utils.sha1(gitHistory.stagedFiles.get(id)).equals(Utils.sha1(gitHistory.trackedFiles.get(id)))) {
                gitHistory.trackedFiles.put(id, gitHistory.stagedFiles.get(id));
            }
        }
        for (String name : gitHistory.markedForRemove) {
            gitHistory.removedFiles.add(name);
        }
        File currentCommitFile = new File(".gitlet/commits/" + currCommit.commitHash + ".txt");
        gitHistory.setCurrentCommit(commitFile);
        for (String fileName : gitHistory.stagedFiles.keySet()) {
            File stagingAreaFiles = new File(".gitlet/staging/" + fileName);
            if (stagingAreaFiles.exists()) {
                stagingAreaFiles.delete();
            }
        }
        gitHistory.stagedFiles.clear();
        gitHistory.markedForRemove.clear();
        File stageFile = new File(".gitlet/staging/" + "stage.txt");
        fileSerial(stageFile, theStage);
        fileSerial(currentCommitFile, currCommit);
        fileSerial(headFile, head);
        fileSerial(commitFile, newestCommit);
        fileSerial(history, gitHistory);

    }

    public void rm(String name) {
        History gitHistory = (History) fileDSerial(history);
        File stagedFileDelete = new File(".gitlet/staging/" + name);
        File headFile = new File(".gitlet/branches/" + "head.txt");
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (!gitHistory.stagedFiles.containsKey(name) && !gitHistory.trackedFiles.containsKey(name)) {
            System.out.println("No reason to remove the file.");
            fileSerial(history, gitHistory);
            return;
        }
        if (head.commit.fileMap.containsKey(name)) {
            File directoryFileDelete = Utils.join(System.getProperty("user.dir"), name);
            if (directoryFileDelete.exists()) {
                directoryFileDelete.delete();
            }
            if (stagedFileDelete.exists()) {
                stagedFileDelete.delete();
            }
            gitHistory.trackedFiles.remove(name);
            gitHistory.stagedFiles.remove(name);
            gitHistory.markedForRemove.add(name);
            fileSerial(headFile, head);
            fileSerial(history, gitHistory);
            return;
        }
        if (!head.commit.fileMap.containsKey(name) && gitHistory.stagedFiles.containsKey(name)) {
            gitHistory.stagedFiles.remove(name);
            if (stagedFileDelete.exists()) {
                stagedFileDelete.delete();
            }
            fileSerial(headFile, head);
            fileSerial(history, gitHistory);
            return;
        }
    }

    public void log() {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        Commit tempCommit = head.commit;
        while (tempCommit != null) {
            System.out.println("===");
            System.out.println("Commit " + tempCommit.commitHash);
            System.out.println(tempCommit.time);
            System.out.println(tempCommit.logMessage);
            System.out.println();
            tempCommit = gitHistory.allCommits.get(tempCommit.parent);
        }
        fileSerial(history, gitHistory);
    }

    public void globallog() {
        History gitHistory = (History) fileDSerial(history);
        for (String id : gitHistory.allCommits.keySet()) {
            System.out.println("===");
            System.out.println("Commit " + gitHistory.allCommits.get(id).commitHash);
            System.out.println(gitHistory.allCommits.get(id).time);
            System.out.println(gitHistory.allCommits.get(id).logMessage);
            System.out.println();
        }
        fileSerial(history, gitHistory);
    }

    public void find(String message) {
        History gitHistory = (History) fileDSerial(history);
        int count = 0;
        for (String id : gitHistory.allCommits.keySet()) {
            if (gitHistory.allCommits.get(id).logMessage.equals(message)) {
                System.out.println(gitHistory.allCommits.get(id).commitHash);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
        fileSerial(history, gitHistory);
    }

    public void status() {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        File headFile = new File(".gitlet/branches/" + "head.txt");
        System.out.println("=== Branches ===");
        for (String id : gitHistory.allBranches.keySet()) {
            if (gitHistory.allBranches.get(id).equals(head.commit.commitHash)) {
                System.out.println("*" + id);
            } else {
                System.out.println(id);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String id : gitHistory.stagedFiles.keySet()) {
            System.out.println(id);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String id : gitHistory.markedForRemove) {
            System.out.println(id);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();

        fileSerial(headFile, head);
        fileSerial(history, gitHistory);
    }

    public void checkout(String fileName) {
        History gitHistory = (History) fileDSerial(history);
        Commit currCommit = (Commit) fileDSerial(gitHistory.getCurrentCommit());
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (!head.commit.fileMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            File inFile = new File(".gitlet/branches/" + "head.txt");
            fileSerial(inFile, currCommit);
            fileSerial(history, gitHistory);
            return;
        }
        byte[] theHeadCommitBytes = head.commit.byteMap.get(fileName);
        File workingDirectoryFile = Utils.join(System.getProperty("user.dir"), fileName);
        Utils.writeContents(workingDirectoryFile, theHeadCommitBytes);
        File headFile = new File(".gitlet/branches/" + "head.txt");
        fileSerial(headFile, head);
        fileSerial(history, gitHistory);
        return;
    }

    public void checkout2(String commitId, String fileName) {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (!gitHistory.allCommits.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            fileSerial(history, gitHistory);
            return;
        }
        Commit idCommit = gitHistory.allCommits.get(commitId);
        if (!idCommit.fileMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            fileSerial(history, gitHistory);
            return;
        }
        byte[] givenCommitBytes = idCommit.byteMap.get(fileName);
        File workingDirectoryFile = Utils.join(System.getProperty("user.dir"), fileName);
        Utils.writeContents(workingDirectoryFile, givenCommitBytes);
        File headFile = new File(".gitlet/branches/" + "head.txt");
        fileSerial(headFile, head);
        fileSerial(history, gitHistory);
    }

    public void checkout3(String branchName) {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        for (String fileName : Utils.plainFilenamesIn(System.getProperty("user.dir"))) {
            if (!gitHistory.trackedFiles.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                fileSerial(history, gitHistory);
                return;
            }
        }
        if (!gitHistory.allBranches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            fileSerial(history, gitHistory);
            return;
        }
        if (head.name.equals(branchName)) {
            System.out.print("No need to checkout the current branch.");
            File headFile = new File(".gitlet/branches/" + "head.txt");
            fileSerial(headFile, head);
            fileSerial(history, gitHistory);
            return;
        }
        String headOfBranch = gitHistory.allBranches.get(branchName);
        Commit givenBranchCommit = gitHistory.allCommits.get(headOfBranch);
        for (String fileName : givenBranchCommit.byteMap.keySet()) {
            byte[] fileBytes = givenBranchCommit.byteMap.get(fileName);
            File workingDirectoryFile = Utils.join(System.getProperty("user.dir"), fileName);
            Utils.writeContents(workingDirectoryFile, fileBytes);
        }

        for (String fileName : head.commit.fileMap.keySet()) {
            if (!givenBranchCommit.fileMap.containsKey(fileName)) {
                File fileToDelete = Utils.join(System.getProperty("user.dir"), fileName);
                fileToDelete.delete();
            }
        }
        for (String fileName : gitHistory.stagedFiles.keySet()) {
            File stagingAreaFiles = new File(".gitlet/staging/" + fileName);
            if (stagingAreaFiles.exists()) {
                stagingAreaFiles.delete();
            }
        }
        gitHistory.stagedFiles.clear();
        head.commit = givenBranchCommit;
        Branch currHeadBranch = new Branch(branchName);
        head = currHeadBranch;
        File headFile = new File(".gitlet/branches/" + "head.txt");
        fileSerial(headFile, currHeadBranch);
        fileSerial(history, gitHistory);
    }

    public void branch(String name) {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (gitHistory.allBranches.containsKey(name)) {
            System.out.println("A branch with that name already exists");
            return;
        }
        Branch addedBranch = new Branch(name);
        addedBranch = head;
        gitHistory.allBranches.put(name, addedBranch.commit.commitHash);
        File newBranchPointer = new File(".gitlet/branches/" + name);
        File headFile = new File(".gitlet/branches/" + "head.txt");
        fileSerial(newBranchPointer, addedBranch);
        fileSerial(headFile, head);
        fileSerial(history, gitHistory);
    }

    public void rmbranch(String name) {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (!gitHistory.allBranches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (gitHistory.allBranches.get(name).equals(head.name)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branchToDelete = new File(".gitlet/branches/" + name);
        branchToDelete.delete();
        gitHistory.allBranches.remove(name);
        File headBranch = new File(".gitlet/branches/" + "head.txt");
        fileSerial(headBranch, head);
        fileSerial(history, gitHistory);
    }

    public void reset(String commitid) {
        History gitHistory = (History) fileDSerial(history);
        Branch head = (Branch) fileDSerial(gitHistory.getHeadBranch());
        if (!gitHistory.allCommits.containsKey(commitid)) {
            System.out.println("No commit with that id exists.");
            fileSerial(history, gitHistory);
            return;
        }
        Commit commitWithId = gitHistory.allCommits.get(commitid);
        for (String fileName : Utils.plainFilenamesIn(System.getProperty("user.dir"))) {
            if (!gitHistory.trackedFiles.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                fileSerial(history, gitHistory);
                return;
            }
        }
        for (String id : commitWithId.fileMap.keySet()) {
            if (!gitHistory.trackedFiles.containsKey(id)) {
                gitHistory.trackedFiles.remove(id);

            }
        }
        for (String name : commitWithId.fileMap.keySet()) {
            File workingDirectoryFile = Utils.join(System.getProperty("user.dir"), name);
            Utils.writeContents(workingDirectoryFile, commitWithId.byteMap.get(name));
        }
        for (String fileName : gitHistory.stagedFiles.keySet()) {
            File stagingFile = new File(".gitlet/staging/" + fileName);
            if (stagingFile.exists()) {
                stagingFile.delete();
            }
        }
        String Branch = "";
        for (String branchName : gitHistory.allBranches.keySet()) {
            if (gitHistory.allBranches.containsKey(commitid)) {
                Branch += branchName;
                break;
            }
        }
        gitHistory.stagedFiles.clear();
        File headFile = new File(".gitlet/branches/" + "head.txt");
        File theBranchFile = new File(".gitlet/branches/" + Branch);
        Branch thisBranch = (Branch) fileDSerial(theBranchFile);
        head.commit = commitWithId;
        thisBranch.commit = commitWithId;
        gitHistory.allBranches.put(head.name, commitWithId.commitHash);
        gitHistory.allBranches.put(thisBranch.name, commitWithId.commitHash);
        fileSerial(theBranchFile, thisBranch);
        fileSerial(headFile, head);
        fileSerial(history, gitHistory);
    }
}