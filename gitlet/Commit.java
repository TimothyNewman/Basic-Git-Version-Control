package gitlet;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/** Constructs a Commit class */
public class Commit implements Serializable {
    public String parent;
    public String logMessage;
    public String commitHash;
    public String time;
    public HashMap<String, String> fileMap;
    public HashMap<String, byte[]> byteMap;

    /** Commit constructor */
    public Commit(String message, String refofparent, HashMap allFiles) {
        this.logMessage = message;
        this.parent = refofparent;
        LocalDateTime now = LocalDateTime.now();
        this.time = getDateAndTime();
        this.fileMap = new HashMap<>();
        this.byteMap = new HashMap<>();
        getNamesMap(allFiles);
        this.commitHash = hashCommit();
        close();
    }

    public String getDateAndTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void getNamesMap(HashMap<String, byte[]> map) {
        for (String i : map.keySet()) {
            fileMap.put(i, Utils.sha1(map.get(i)));
            byteMap.put(i, map.get(i));
        }
    }

    /** Returns the commits hashcode */
    public String hashCommit() {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(stream);
            objStream.writeObject(this);
            objStream.close();
            File newFile = new File(".gitlet/commits" + Utils.sha1(stream.toByteArray()) + ".txt");
            return Utils.sha1(stream.toByteArray());
        } catch (IOException excp) {
            throw new Error("Internal error serializing commit.");
        }
    }

    public void close() {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(stream);
            objStream.writeObject(this);
            objStream.close();
            File newFile = new File(".gitlet/commits" + commitHash + ".txt");
            Utils.writeContents(newFile, stream.toByteArray());

        } catch (IOException excp) {
            throw new Error ("Internal error serializing commit.");
        }
    }

}