import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.nio.file.Files;
import java.util.Scanner;
import java.io.FileInputStream;
import static java.nio.file.StandardCopyOption.*;




public class CommitTree implements Serializable {
    HashMap<Integer, HashMap<String, String>> trackTree; //track the previous files
    HashMap<String, Integer> branchmap; //branches and branch pointers
    HashMap<Integer, Commit> commitmap;
    HashSet<String> addfiles;
    HashSet<String> removefiles;
    HashMap<String, String> filepathmap;
    HashMap<String, HashSet<Integer>> messagemap; 
    HashMap<String, ArrayList<Integer>> pathway;
    String curbranch = "master";
    Integer curid;




    public CommitTree() {
        trackTree = new HashMap<Integer, HashMap<String, String>>();
        branchmap = new HashMap<String, Integer>();
        addfiles = new HashSet<String>();
        removefiles = new HashSet<String>();
        messagemap = new HashMap<String, HashSet<Integer>>();
        filepathmap = new HashMap<String, String>();
        pathway = new HashMap<String, ArrayList<Integer>>();
        commitmap = new HashMap<Integer, Commit>();
        curid = 1;
    }


    public void init() {
        File maindir = new File(".gitlet/");
        if (maindir.exists()) {
            System.out.println("A gitlet version control system"  
                               + " already exists in the current directory.");
        } else {
            maindir.mkdirs();
            File curcommit = new File(".gitlet/commit0");
            if (!curcommit.exists()) {
                curcommit.mkdir();
            }
            Commit addcommit = new Commit("initial commit", 0, "master");
            commitmap.put(0, addcommit);
            branchmap.put(curbranch, 0);
            ArrayList<Integer> addin = new ArrayList<Integer>();
            addin.add(0);
            pathway.put(curbranch, addin);
            trackTree.put(0, new HashMap<String, String>());
        }
    }


    public void add(String file) {
        String curdirectory = System.getProperty("user.dir");
        String fullpath = curdirectory + "/" + file;
        File f = new File(fullpath);
        if (f.exists()) {
            String filename = new File(file).getName();
            Integer pointer = branchmap.get(curbranch);
            //check if already committed
            if (trackTree.containsKey(pointer)) {
                if (trackTree.get(pointer).containsKey(filename)) {
                    String filepath = trackTree.get(pointer).get(filename);
                    File oldFile = new File(filepath + "/" + filename);
                    //check the content of two files
                    try {
                        if (compareFile(f, oldFile)) {
                            System.out.println("File has not been modified since the last commit.");
                        } else {                            
                            addfiles.add(fullpath);
                            filepathmap.put(filename, fullpath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } 
                } else {
                    addfiles.add(fullpath);
                    filepathmap.put(filename, fullpath);
                }
            }
        } else {
            System.out.println("File does not exist.");
        }
    }



    public void commit(String message) {
        if ((addfiles.isEmpty()) && (removefiles.isEmpty())) {
            System.out.println("No changes added to the commit.");
        } else {
            //check message
            if (message != null) {
                String curpath = ".gitlet/commit" + curid;
                File curcommit = new File(curpath);
                if (!curcommit.exists()) {
                    curcommit.mkdir();
                }
                Integer pointer = branchmap.get(curbranch);
                HashMap<String, String> putin = new HashMap<String, String>();
                //inherit trackTree
                putin.putAll(trackTree.get(pointer));
                //add files
                if (!addfiles.isEmpty()) {
                    HashSet<String> addin = new HashSet<String>();
                    for (String addfile : addfiles) {
                        String name = new File(addfile).getName();
                        File addFile = new File(curpath + "/" + name);
                        File source = new File(addfile);              
                        if (!addFile.exists()) {
                            try {
                                addFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        copyFile(source, addFile);
                        putin.put(name, curpath);
                    }
                }
                //remove the marked files
                if (!removefiles.isEmpty()) {
                    for (String removefile : removefiles) {
                        if (putin.containsKey(removefile)) {
                            putin.remove(removefile);
                        }
                    }
                }          
                //update commitmap
                Commit addcommit = new Commit(message, curid, curbranch);
                commitmap.put(curid, addcommit);
                //update trackTree
                trackTree.put(curid, putin);
                //clear addfiles and removefiles
                addfiles.clear();
                removefiles.clear();
                //update messagemap
                if (messagemap.containsKey(message)) {
                    messagemap.get(message).add(curid);
                } else {
                    HashSet<Integer> addin = new HashSet<Integer>();
                    addin.add(curid);
                    messagemap.put(message, addin);
                }
                //update pathway
                pathway.get(curbranch).add(curid);
                //update pointer
                branchmap.put(curbranch, curid);
                //update curid
                curid += 1;
            }
        }
    }


    public void remove(String file) {
        //remove from addfiles
        Integer pointer = branchmap.get(curbranch);
        String name = new File(file).getName();
        if (!addfiles.isEmpty()) {
            if (filepathmap.containsKey(file)) {
                String filepath = filepathmap.get(file);
                if (addfiles.contains(filepath)) {
                    addfiles.remove(filepath);
                }
            }
        } else if ((trackTree.containsKey(pointer)) && (trackTree.get(pointer).containsKey(name))) {
            removefiles.add(name);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void log() {
        if (pathway.containsKey(curbranch)) {
            ArrayList<Integer> path = pathway.get(curbranch);
            for (int i = path.indexOf(branchmap.get(curbranch)); i >= 0; i--) {
                System.out.println("====");
                Commit commit = commitmap.get(path.get(i));
                System.out.println("Commit" + commit.id);
                System.out.println(commit.time);
                System.out.println(commit.message);
                System.out.println("              ");
            }
        }
    }


    public void globallog() {
        for (Commit commit : commitmap.values()) {
            System.out.println("====");
            System.out.println("Commit" + commit.id);
            System.out.println(commit.time);
            System.out.println(commit.message);
            System.out.println("              ");
        }
    }

    public void find(String message) {
        if (messagemap != null) {
            if (messagemap.containsKey(message)) {
                for (int id : messagemap.get(message)) {
                    System.out.println(id);
                }
            } else {
                System.out.println("Found no commit with that message.");
            }
        }
    } 

    public void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + curbranch);
        for (String branch : branchmap.keySet()) {
            if (!branch.equals(curbranch)) {
                System.out.println(branch);
            }
        }
        System.out.println("           ");
        System.out.println("=== Staged Files ===");
        if (!addfiles.isEmpty()) {
            for (String addfile : addfiles) {
                System.out.println(addfile);
            }
        }
        System.out.println("           ");
        System.out.println("=== Files Marked for Removal ===");
        if (!removefiles.isEmpty()) {
            for (String markfile : removefiles) {
                System.out.println(markfile);
            }
        }
        
    }

    public void checkout(int id, String file) {
        if (!commitmap.containsKey(id)) {
            System.out.println("No commit with that id exists.");
        } else {
            String filename = new File(file).getName();
            if ((trackTree.containsKey(id)) && (trackTree.get(id).containsKey(filename))) {
                String curdirectory = System.getProperty("user.dir");
                String fullpath = curdirectory + "/" + filename;
                File curfile = new File(fullpath);
                File source = new File(trackTree.get(id).get(filename) + "/" + filename);
                if (!curfile.exists()) {
                    try {
                        curfile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if ((source.exists()) && (curfile.exists())) {
                    copyFile(source, curfile);
                }
            } else {
                System.out.println("File does not exist in that commit.");  
            }
        }
    }

    public void checkout(String input) {
        String filename = new File(input).getName();
        Integer pointer = branchmap.get(curbranch);
        String curdirectory = System.getProperty("user.dir");
        String fullpath = curdirectory + "/" + input;
        File curfile = new File(fullpath);
        if ((!branchmap.containsKey(input)) && ((trackTree.containsKey(pointer)) 
            && (!trackTree.get(pointer).containsKey(filename)))) {
            System.out.println("File does not exist in the most recent commit" 
                                + ", or no such branch exists."); 
        }
        if (branchmap.containsKey(input)) {
            if (curbranch.equals(input)) {
                System.out.println("No need to checkout the current branch.");
            } else {
                curbranch = input;
                for (String file : trackTree.get(pointer).keySet()) {
                    copyFile(new File(trackTree.get(pointer).get(file) + "/" + file), curfile); 
                }
            }
        } 
        if ((trackTree.containsKey(pointer)) && (trackTree.get(pointer).containsKey(filename))) {
            copyFile(new File(trackTree.get(pointer).get(filename) + "/" + filename), curfile);
        }
    }


    public void branch(String branch) {
        if (!branchmap.containsKey(branch)) {
            Integer pointer = branchmap.get(curbranch);
            branchmap.put(branch, pointer);
            //update pathway
            ArrayList<Integer> addin = new ArrayList<Integer>();
            if (pathway.containsKey(curbranch)) {
                int index = pathway.get(curbranch).indexOf(pointer);
                addin.addAll(pathway.get(curbranch).subList(0, index + 1));
                pathway.put(branch, addin);
            }
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    public void removebranch(String branch) {
        if (branchmap.containsKey(branch)) {
            if (!branch.equals(curbranch)) {
                branchmap.remove(branch);
            } else {
                System.out.println("Cannot remove the current branch.");
            }
        } else {
            System.out.println("A branch with that name does not exist.");
        }
    }

    public void reset(int id) {
        if (!commitmap.containsKey(id)) {
            System.out.println("No commit with that id exists.");
        } else {
            String curdirectory = System.getProperty("user.dir");
            for (String file : trackTree.get(id).keySet()) {
                File curfile = new File(filepathmap.get(file));
                File source = new File(trackTree.get(id).get(file) + "/" + file);
                copyFile(source, curfile);
            }
            //update branchmap
            branchmap.put(curbranch, id);
            //update pathway
            ArrayList<Integer> commits = pathway.get(curbranch);
            ArrayList<Integer> addin = new ArrayList<Integer>();
            for (int i = 0; i <= commits.indexOf(id); i++) {
                addin.add(commits.get(i));
            }
            pathway.put(curbranch, addin);
        }
    }


    public void merge(String branch) {
        ArrayList<Integer> curpath = new ArrayList<Integer>();
        ArrayList<Integer> givenpath = new ArrayList<Integer>();
        ArrayList<String> commonfiles = new ArrayList<String>();
        ArrayList<String> diffiles = new ArrayList<String>();
        String curdirectory = System.getProperty("user.dir");
        boolean curmodified;
        boolean givenmodified;
        if (!branchmap.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");  
        } else {
            if (curbranch.equals(branch)) {
                System.out.println("Cannot merge a branch with itself.");   
            } else {
                int givenpointer = branchmap.get(branch);
                int curpointer = branchmap.get(curbranch);
                curpath = pathway.get(curbranch);
                givenpath = pathway.get(branch);
                int splitpoint = findsplitpoint(curpath, givenpath);
                //categorize files
                if (trackTree.containsKey(givenpointer)) {
                    for (String givenfile : trackTree.get(givenpointer).keySet()) {
                        if (trackTree.get(curpointer).containsKey(givenfile)) {
                            commonfiles.add(givenfile);
                        } else {
                            diffiles.add(givenfile); 
                        }
                    }
                }
                //deal with files with same name
                for (String commonfile : commonfiles) {
                    curmodified = checkmodified(curpointer, commonfile, splitpoint);
                    givenmodified = checkmodified(givenpointer, commonfile, splitpoint);
                    String sourcepath1 = trackTree.get(givenpointer).get(commonfile);
                    String destpath1 = filepathmap.get(commonfile);
                    if (givenmodified && !curmodified) {
                        //copy the givenfiles to working directory
                        File f1 = new File(destpath1);
                        copyFile(new File(sourcepath1 + "/" + commonfile), f1);
                    } 
                    if (givenmodified && curmodified) {
                        File f2 = new File(destpath1 + ".conflicted");
                        try {
                            f2.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        copyFile(new File(sourcepath1 + "/" + commonfile), f2);
                    }
                }
                //deal with different files
                for (String diffile : diffiles) {
                    String sourcepath2 = trackTree.get(givenpointer).get(diffile);
                    String destpath2 = filepathmap.get(diffile);
                    File f3 = new File(destpath2);
                    try {
                        f3.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    copyFile(new File(sourcepath2 + "/" + diffile), f3);
                }
            }
        }
    }


    public void rebase(String branch) {      
        ArrayList<Integer> curcommits = new ArrayList<Integer>();
        ArrayList<Integer> givencommits = new ArrayList<Integer>();
        List<Integer> difcur = new ArrayList<Integer>();
        List<Integer> difgiven = new ArrayList<Integer>();
        if (!branchmap.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");  
        } else {
            if (curbranch.equals(branch)) {
                System.out.println("Cannot rebase a branch onto itself.");
            } else {
                int givenpointer = branchmap.get(branch);
                int curpointer = branchmap.get(curbranch);
                curcommits = pathway.get(curbranch);
                givencommits = pathway.get(branch);
                int splitpoint = findsplitpoint(curcommits, givencommits);
                if (splitpoint == curpointer) {
                    branchmap.put(curbranch, givenpointer);
                    //update pathmap
                    ArrayList<Integer> newadd = new ArrayList<Integer>();
                    for (int i = givencommits.indexOf(splitpoint) + 1; 
                        i < givencommits.size(); i++) {
                        newadd.add(givencommits.get(i));
                    }
                    pathway.put(curbranch, newadd);
                    reset(branchmap.get(branch));
                } else if (splitpoint == givenpointer) {
                    System.out.println("Already up-to-date.");   
                } else {
                    difcur = curcommits.subList(curcommits.indexOf(splitpoint) + 1, 
                                                 curcommits.size());
                    difgiven = givencommits.subList(givencommits.indexOf(splitpoint) + 1, 
                                                  givencommits.size());
                    if (!difcur.isEmpty()) {
                        for (int i : difcur) {
                            replay(i, branch);
                        }
                    }
                    curbranch = branch;
                    reset(3);
                }
            }
        }
    }



    public void irebase(String branch) {      
        ArrayList<Integer> curcommits = new ArrayList<Integer>();
        ArrayList<Integer> givencommits = new ArrayList<Integer>();
        List<Integer> difcur = new ArrayList<Integer>();
        List<Integer> difgiven = new ArrayList<Integer>();
        if (!branchmap.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");  
        } else {
            if (curbranch.equals(branch)) {
                System.out.println("Cannot rebase a branch onto itself.");
            } else {
                int givenpointer = branchmap.get(branch);
                int curpointer = branchmap.get(curbranch);
                curcommits = pathway.get(curbranch);
                givencommits = pathway.get(branch);
                int splitpoint = findsplitpoint(curcommits, givencommits);
                if (splitpoint == curpointer) {
                    branchmap.put(curbranch, givenpointer);
                    //update pathmap
                    ArrayList<Integer> newadd = new ArrayList<Integer>();
                    for (int i = givencommits.indexOf(splitpoint) + 1; 
                        i < givencommits.size(); i++) {
                        newadd.add(givencommits.get(i));
                    }
                    pathway.put(curbranch, newadd);
                    reset(branchmap.get(curbranch));
                } else if (splitpoint == givenpointer) {
                    System.out.println("Already up-to-date.");   
                } else {
                    difcur = curcommits.subList(curcommits.indexOf(splitpoint) + 1, 
                                                 curcommits.size());
                    difgiven = givencommits.subList(givencommits.indexOf(splitpoint) + 1, 
                                                    givencommits.size());
                    int num = 0;
                    while (num < difcur.size()) {
                        System.out.println("Currently replaying:");
                        System.out.println("Commit" + difcur.get(num));
                        int replayNum = difcur.get(num);
                        Commit replayC = commitmap.get(replayNum);
                        System.out.println(replayC.time);
                        System.out.println(replayC.message);
                        boolean cycle = true;
                        while (cycle) {
                            System.out.println("Would you like to (c)ontinue,"
                                + "(s)kip this commit, or "
                                + "change this commit's (m)essage?");
                            Scanner scanner = new Scanner(System.in).useDelimiter("\n");
                            String choice = scanner.next();
                            if (choice.equals("c")) {
                                replay(replayNum, branch);
                                num += 1;
                                cycle = false;
                            } else if (choice.equals("m")) {
                                System.out.println("Please enter a new message for this commit.");
                                String message = scanner.next();
                                Commit newC = new Commit(message, replayC.id, replayC.branch);
                                commitmap.put(replayNum, newC);
                                replay(replayNum, branch);                                
                                num += 1;
                                cycle = false;
                            } else if (choice.equals("s") && num != 0 && num != difcur.size() - 1) {
                                num += 1;
                                cycle = false;
                            } else {
                                System.out.println("Invalid input, please enter another input.");
                            }
                        }
                    }
                    curbranch = branch;
                    reset(branchmap.get(branch));
                }
            }
        }
    }


/*====================================================================================*/
 /*======================================Helper========================================*/
 /*====================================================================================*/

    private Boolean checkmodified(int id, String file, int splitpoint) {
        String curlocation = trackTree.get(id).get(file);
        String splitlocation = trackTree.get(splitpoint).get(file);
        if (curlocation.equals(splitlocation)) {
            return false;
        }
        return true;
    }

    


    private int findsplitpoint(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {
        int size = Math.min(arr1.size(), arr2.size());
        for (int i = 0; i < size; i++) {
            if (arr1.get(i) != arr2.get(i)) {
                if (i == 0) {
                    return 0;  
                } else {
                    return arr1.get(i - 1);
                }
            }
        }
        return arr1.get(size - 1);
    }

    private static void copyFile(File source, File dest) {
        try {
            Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.format("fail copy from %s to %s", source.toString(), dest.toString());
        }

    }

    public static boolean compareFile(File f1, File f2) throws IOException {
        FileInputStream fis1 = new FileInputStream(f1);
        FileInputStream fis2 = new FileInputStream(f2);
        if (f1.length() == f2.length()) {
            int n = 0;
            byte[] b1;
            byte[] b2;
            while ((n = fis1.available()) > 0) {
                b1 = new byte[n];
                b2 = new byte[n];
                fis1.read(b1);
                fis2.read(b2);
                return Arrays.equals(b1, b2);
            }
        } else {
            return false;
        }
        return true;
    }


    private void replay(int source, String branch) {
        HashMap<String, String> putin = new HashMap<String, String>();
        //inherit
        putin.putAll(trackTree.get(branchmap.get(branch)));
        //copy from source
        putin.putAll(trackTree.get(source));
        //update trackTree
        trackTree.put(curid, putin);
        //update pathway
        pathway.get(branch).add(curid);
        File newfolder = new File("gitlet/commit" + curid);
        if (!newfolder.exists()) {
            newfolder.mkdir();
        }
        //update commitmap
        Commit sourceC = commitmap.get(source);
        Commit newcommit = new Commit(sourceC.message, curid, branch);
        commitmap.put(curid, newcommit);
        //update pointer
        branchmap.put(branch, curid);
        curid += 1;
    }


    void dangerousM(Boolean dangerous) {
        System.out.println("Warning: The command you entered may alter" 
            + "the files in your directory. Uncommited changes may"
            + "be lost. Are you sure you want to continue? (yes/no)");
    }

}









            










            




