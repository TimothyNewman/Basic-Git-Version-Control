package gitlet;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Stage implements Serializable {
    public HashMap<String, byte[]> stagedFiles;
    public File stageFile;
    public Set<String> markedForRemove;
    public ArrayList<String> trackedFiles;

    /**
     * Stage constructor
     */
    public Stage() {
        stageFile = new File(".gitlet/staging/" + "stage.txt");
        this.stagedFiles = new HashMap<>();
        this.trackedFiles = new ArrayList<>();
        this.markedForRemove = new HashSet<>();
    }
}
