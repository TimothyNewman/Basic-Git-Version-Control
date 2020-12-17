package gitlet;
import gitlet.Commit;

import java.io.Serializable;

/** Constructs a Branch class */
public class Branch implements Serializable {
    public String name;
    public Commit commit;
    public Branch parentNode;
    public Branch childNode;

    /** Branch constructor */
    public Branch(String branchname) {
        this.name = branchname;
        this.commit = null;
        this.parentNode = null;
        this.childNode = null;
    }
}