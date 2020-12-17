package gitlet;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {
    /** The main to run all the commands of gitlet */
    public static void main (String[] args) {
        Commands commandLine = new Commands();
        if (args.length == 0){
            System.out.println("Please enter a command");
        }
        String command = args[0];
        if (args.length == 1) {
            switch (command) {
                case "init":
                    commandLine.init();
                    break;
                case "log":
                    commandLine.log();
                    break;
                case "global-log":
                    commandLine.globallog();
                    break;
                case "status":
                    commandLine.status();
                    break;
                default:
                    System.out.println("Please enter a valid command");
            }
        }
        if (command.equals("checkout")) {
            if (args.length == 3 && args[1].equals("--")) {
                commandLine.checkout(args[2]);
                return;
            }
            if (args.length == 2) {
                commandLine.checkout3(args[1]);
                return;
            }
            if (args.length == 4) {
                commandLine.checkout2(args[1], args[3]);
                return;
            }
            System.out.println("Please enter a valid command");
            return;
        }


        if (args.length == 2) {
            String file = args[1];
            switch (command) {
                case "add":
                    commandLine.add(file);
                    break;
                case "commit":
                    commandLine.commit(file);
                    break;
                case "rm":
                    commandLine.rm(file);
                    break;
                case "find":
                    commandLine.find(file);
                    break;
                case "branch":
                    commandLine.branch(file);
                    break;
                case "rm-branch":
                    commandLine.rmbranch(file);
                    break;
                case "reset":
                    commandLine.reset(file);
                    break;
                default:
                    System.out.println("Please enter a valid command");
            }
        }
    }
}