import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Gitlet {

    CommitTree cTree;

    public Gitlet() {
        cTree = new CommitTree();
    }

    public static void main(String[] args) {
        Gitlet glt = new Gitlet();
        Scanner checkscanner = new Scanner(System.in);
        if (args == null || args.length == 0) {
            System.out.println("Please enter an input");
        } else {
            String[] commands = args; String command = commands[0];
            String[] tokens = new String[commands.length - 1];
            System.arraycopy(commands, 1, tokens, 0, commands.length - 1);
            glt.loadingGitlet();               
            try {
                switch (command) {
                    case "init": 
                        glt.goinit(); break;
                    case "log":
                        glt.golog(); break;
                    case "global-log":
                        glt.gogloballog(); break;
                    case "status":
                        glt.gostatus(); break;
                    case "add":
                        glt.goadd(tokens[0]); break; 
                    case "rm": 
                        glt.goremove(tokens[0]); break;
                    case "find":
                        glt.gofind(tokens[0]);
                        break;
                    case "branch":
                        glt.gobranch(tokens[0]);
                        break;
                    case "rm-branch":
                        glt.gormbranch(tokens[0]); break;
                    case "reset":
                        // glt.godangerous(checkscanner);
                        glt.goreset(Integer.parseInt(tokens[0])); break;
                    case "merge":
                        // glt.godangerous(checkscanner);
                        // if ((checkscanner.next()).equals("yes")) {
                            glt.gomerge(tokens[0]);
                        // }
                        break;
                    case "rebase":
                        // glt.godangerous(checkscanner);
                        // if ((checkscanner.next()).equals("yes")) {
                            glt.gorebase(tokens[0]);
                        // }
                        break;
                    case "commit":
                        try {
                            glt.gocommit(tokens[0]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("Please enter a commit message.");
                        }
                        break;
                    case "i-rebase":
                        // glt.godangerous(checkscanner);
                        // if ((checkscanner.next()).equals("yes")) {
                            glt.goirebase(tokens[0]);
                        // } 
                        break;
                    case "checkout":
                        // glt.godangerous(checkscanner);
                        // if ((checkscanner.next()).equals("yes")) {
                            if (tokens.length == 1) {
                                glt.gocheckout1(tokens[0]);
                            } else if (tokens.length == 2) {
                                glt.gocheckout2(Integer.parseInt(tokens[0]), tokens[1]);
                            }
                        // }
                        break;
                    default: 
                        break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Did not enter enough arguments.");
            }              
            glt.saveGitlet();
        }
    }

    public void loadingGitlet() {
        File myTreeFile = new File(".gitlet/myTree.ser");
        if (myTreeFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myTreeFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                cTree = (CommitTree) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading myTree.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myTree.";
                System.out.println(msg);
            }
        }
    }

    public void saveGitlet() {
        try {
            File myTreeFile = new File(".gitlet/myTree.ser");
            FileOutputStream fileOut = new FileOutputStream(myTreeFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(cTree);
        } catch (IOException e) {
            String msg = "IOException while saving myGitlet.";
            System.out.println(msg);
        }
    }

    private void goinit() {
        cTree.init();
    }

    private void goadd(String file) {
        cTree.add(file);
    }

    private void gocommit(String file) {
        cTree.commit(file);
    }

    private void goremove(String file) {
        cTree.remove(file);
    }

    private void golog() {
        cTree.log();
    }

    private void gogloballog() {
        cTree.globallog();
    }

    private void gofind(String message) {
        cTree.find(message);
    }

    private void gostatus() {
        cTree.status();
    }

    private void gobranch(String branch) {
        cTree.branch(branch);
    }

    private void gocheckout1(String input) {
        cTree.checkout(input);
    }

    private void gocheckout2(int id, String input) {
        cTree.checkout(id, input);
    }

    private void gormbranch(String branch) {
        cTree.removebranch(branch);
    }

    private void goreset(int id) {
        cTree.reset(id);
    }

    private void gomerge(String input) {
        cTree.merge(input);
    }

    private void gorebase(String input) {
        cTree.rebase(input);
    }

    private void goirebase(String input) {
        cTree.irebase(input);
    }

    private void godangerous(Scanner checkscanner) {
        cTree.dangerousM(true);
    }

}
