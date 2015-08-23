/*
 The MIT License (MIT)

 Copyright (c) 2014 BioDAG

 Odysseus: a versatile high-performance framework for enabling bioinformatics workflows on
 hybrid cloud environments

 Athanassios M. Kintsakis akintsakis@issel.ee.auth.gr
 Fotis E. Psomopoulos     fpsom@issel.ee.auth.gr
 Perciles A. Mitkas       mitkas@auth.gr

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 Author: Athanassios Kintsakis
 contact: akintsakis@issel.ee.auth.gr
 */
package blastprofdistributer;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Slave extends Thread {

    boolean isLocked = false;
    private static int globalCount = 0;
    private Lock lock;// = new Lock();
    private static int fileNumber = 0;
    // private static String pwyPath = "/home/thanos/Desktop/fsts/";
    private static String outFolder;
    String message;
    File[] listOfFiles;
    String vanillaCommand;
    //String profilesOutPath;
    Uploader ps;
    LinkedHashMap<String, Integer> organisms;
    Integer port;
    String profileType;
    int threadid;
    //static Map<String,BufferedWriter> writers=new HashMap<String,BufferedWriter>();

    public Slave(int threadid, File[] listOfFiles, int fileNumber, String blastOutPath, Lock lock, String vanillaCommand, LinkedHashMap<String, Integer> organisms, String profileType) throws IOException {
        this.message = message;
        this.listOfFiles = listOfFiles;
        this.fileNumber = fileNumber;
        this.outFolder = blastOutPath;
        this.lock = lock;
        this.vanillaCommand = vanillaCommand;
        this.organisms = organisms;
        //this.profilesOutPath=profilesOutPath;
        port = 56700 + threadid;
        this.profileType = profileType;
        this.threadid = threadid;

    }

    public static String executeCommandLD(String command) {

        StringBuffer output = new StringBuffer();

        // Process p;
        try {
            //command=command+" 1>/dev/null";
            //String[] tc = {"sh","-c",command};
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();

//            BufferedReader reader
//                    = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//            String line = "";
//            while ((line = reader.readLine()) != null) {
//                output.append(line + "\n");
//            }
            p.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
        // return "nothing";

    }

    public static void talker(String message, String serverName, String portString) {
        //String serverName = "Node1";
        int port = Integer.parseInt(portString);
        try {
            // System.out.println("Connecting to " + serverName+" on port " + port);
            Socket client = new Socket(serverName, port);
            // System.out.println("Just connected to "
            //         + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out
                    = new DataOutputStream(outToServer);

            out.writeUTF(message);// + client.getLocalSocketAddress()
            // InputStream inFromServer = client.getInputStream();
            // DataInputStream in = new DataInputStream(inFromServer);
            //  System.out.println("Server says " + in.readUTF());
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void BBHbuildProfile(String input, String outputFolder, LinkedHashMap<String, Integer> organisms) throws IOException {

        int organismsSize = organisms.size();
        File inp = new File(input);
        List<String> lines = Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8);

        Map<String, Integer> pathwaysPreserve = new HashMap<String, Integer>();
        Map<String, Integer> protsPreserve = new HashMap<String, Integer>();

        if (lines.size() > 0) {
            String currentProtein = lines.get(0).substring(0, lines.get(0).indexOf("\t"));
            String currentPathway = lines.get(0).substring(0, lines.get(0).indexOf("\t"));
            currentPathway = currentPathway.substring(0, currentPathway.indexOf("$"));
            //System.out.println(currentPathway);

            ArrayList<String[]> matches = new ArrayList<String[]>(100);

            String[] tttt1 = new String[organismsSize];
            for (int s1 = 0; s1 < tttt1.length; s1++) {
                tttt1[s1] = "0";
            }
            matches.add(tttt1);
            matches.get(0);
            ArrayList<String> names = new ArrayList<String>(100);
            names.add(currentProtein);

            //Map<String, Integer> duplicates = new HashMap<String, Integer>();
            //String pathName;
            File t1;
            FileWriter w1;
            BufferedWriter wr1;
            double currentEvalue;
            double currentIdentity;
            double previousEvalue;
            double previousIdentity;
            //String[] tmp2;
            String[] brk;
            String orgName;

            //int countDuplicates = 0;
            //double evalue;
            int listIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                String[] tmp = lines.get(i).split("\t");
                String comb = tmp[0] + "\t" + tmp[1];

                //duplicates.put(comb, 1);
                if (!tmp[0].equals(currentProtein)) {
                    //System.out.println("loop "+i+"  inside");
                    if (protsPreserve.containsKey(tmp[0])) {
                        System.out.println("fatal error, protein was repeated.. OUT OF ORDER");
                    } else {
                        protsPreserve.put(tmp[0], i);
                        currentProtein = tmp[0];
                        //System.out.println(currentProtein);

                        if (!tmp[0].substring(0, tmp[0].indexOf("$")).equals(currentPathway)) {
                            if (pathwaysPreserve.containsKey(tmp[0])) {
                                System.out.println("fatal error, pathway was repeated.. OUT OF ORDER");
                            } else {
                                pathwaysPreserve.put(tmp[0], i);
                                // System.out.println(currentPathway);
                            }

                            //write them
                            //pathName = currentPathway;
                            //System.out.println(currentPathway);
                            for (int s = 0; s < organismsSize; s++) {
                                t1 = new File(outputFolder + "/" + organisms.get(currentPathway) + "_" + s);//+"/"+inp.getName());
                                t1.mkdirs();
                                t1 = new File(outputFolder + "/" + organisms.get(currentPathway) + "_" + s + "/" + inp.getName());
                                w1 = new FileWriter(t1);
                                wr1 = new BufferedWriter(w1);
                                for (int j = 0; j < matches.size(); j++) {
                                    //System.out.println(names.get(j));
                                    //wr1.write(names.get(j) + "|");
                                    wr1.write(names.get(j).substring(0, names.get(j).indexOf("$", names.get(j).indexOf("$") + 1)) + "|");

//                                for (int k = 0; k < matches.get(j).length; k++) {
//                                    wr1.write(matches.get(j)[k] + " ");
//                                }
                                    // wr1.write(matches.get(j)[s] + " ");
                                    if (matches.get(j)[s].equals("0")) {
                                        wr1.write(matches.get(j)[s]);
                                    } else {
                                        wr1.write(matches.get(j)[s].substring(matches.get(j)[s].indexOf(";", matches.get(j)[s].indexOf(";") + 1) + 1, matches.get(j)[s].length()));
                                    }
                                    wr1.newLine();
                                }
                                wr1.close();
                            }

                            //System.out.println(currentPathway + " emptied");
                            //empty them
                            listIndex = 0;
                            matches = new ArrayList<String[]>(100);
                            String[] ttt1 = new String[organismsSize];
                            for (int s1 = 0; s1 < ttt1.length; s1++) {
                                ttt1[s1] = "0";
                            }
                            matches.add(ttt1);
                            names = new ArrayList<String>(100);
                            names.add(currentProtein);
                            currentPathway = tmp[0].substring(0, tmp[0].indexOf("$"));

                        } else {

                            names.add(currentProtein);
                            String[] ttt1 = new String[organismsSize];
                            for (int s1 = 0; s1 < ttt1.length; s1++) {
                                ttt1[s1] = "0";
                            }
                            matches.add(ttt1);
                            listIndex++;
                        }
                    }

                }

                //System.out.println(currentEvalue);
                // orgName = tmp[1].substring(0, 17);
                //tmp2 = tmp[1].split("\\$");
                //System.out.println(tmp[1]);
                //tmp[1] = tmp2[0] + "$" + tmp2[1];
                tmp[1] = tmp[1].substring(0, tmp[1].indexOf("$", tmp[1].indexOf("$") + 1));
                orgName = tmp[1].substring(0, 17);
//                int[] tmpArray = prots.get(tmp[0]);
//                int index = organisms.get(tmp[1]);
//                tmpArray[index] = tmpArray[index] + 1;
                //String inValue = matches.get(listIndex)[organisms.get(orgName)];
                if (matches.get(listIndex)[organisms.get(orgName)].equals("0")) {
                    // System.out.println(tmp[10]);
                    matches.get(listIndex)[organisms.get(orgName)] = tmp[10] + ";" + tmp[2] + ";" + tmp[1];
                    //  System.out.println("insterted " + matches.get(listIndex)[organisms.get(orgName)]);
                } else {
                    //brk = matches.get(listIndex)[organisms.get(orgName)].split(";");
                    currentEvalue = Double.valueOf(tmp[10]);
                    previousEvalue = Double.valueOf(matches.get(listIndex)[organisms.get(orgName)].substring(0, matches.get(listIndex)[organisms.get(orgName)].indexOf(";"))); //brk[0]
                    if (currentEvalue < previousEvalue) {
                        matches.get(listIndex)[organisms.get(orgName)] = tmp[10] + ";" + tmp[2] + ";" + tmp[1];
                    } else if (currentEvalue == previousEvalue) {
                        //System.out.println("we have a tie old:" + matches.get(listIndex)[organisms.get(orgName)] + "   new:" + tmp[10]+";"+tmp[2]+";"+tmp[1]);
                        currentIdentity = Double.valueOf(tmp[2]);
                        String keeptmp1 = matches.get(listIndex)[organisms.get(orgName)].substring(matches.get(listIndex)[organisms.get(orgName)].indexOf(";") + 1, matches.get(listIndex)[organisms.get(orgName)].length());
                        keeptmp1 = keeptmp1.substring(0, keeptmp1.indexOf(";"));
                        //previousIdentity = Double.valueOf(matches.get(listIndex)[organisms.get(orgName)].substring(matches.get(listIndex)[organisms.get(orgName)].indexOf(";") + 1, matches.get(listIndex)[organisms.get(orgName)].length())); //brk[1]
                        previousIdentity = Double.valueOf(keeptmp1); //brk[1]

                        if (currentIdentity < previousIdentity) {
                            matches.get(listIndex)[organisms.get(orgName)] = tmp[10] + ";" + tmp[2] + ";" + tmp[1];
                        } else if (currentIdentity == previousIdentity) {
                            //System.out.println("WE HAVE A DOUBLE MATCH.. KEEPING OLD");
                        }

                    }
                }

                //matches.get(listIndex)[organisms.get(tmp[1])] = 1 + matches.get(listIndex)[organisms.get(tmp[1])];
            }
            for (int s = 0; s < organismsSize; s++) {
                t1 = new File(outputFolder + "/" + organisms.get(currentPathway) + "_" + s);//+"/"+inp.getName());
                t1.mkdirs();
                t1 = new File(outputFolder + "/" + organisms.get(currentPathway) + "_" + s + "/" + inp.getName());
                w1 = new FileWriter(t1);
                wr1 = new BufferedWriter(w1);
                for (int j = 0; j < matches.size(); j++) {
                    //System.out.println(names.get(j));
                    //wr1.write(names.get(j) + "|");
                    wr1.write(names.get(j).substring(0, names.get(j).indexOf("$", names.get(j).indexOf("$") + 1)) + "|");

//                                for (int k = 0; k < matches.get(j).length; k++) {
//                                    wr1.write(matches.get(j)[k] + " ");
//                                }
                    // wr1.write(matches.get(j)[s] + " ");
                    if (matches.get(j)[s].equals("0")) {
                        wr1.write(matches.get(j)[s]);
                    } else {
                        wr1.write(matches.get(j)[s].substring(matches.get(j)[s].indexOf(";", matches.get(j)[s].indexOf(";") + 1) + 1, matches.get(j)[s].length()));
                    }

                    wr1.newLine();
                }
                wr1.close();
            }

            //System.out.println(currentPathway + " emptied");
            //System.out.println(countDuplicates);
        }

    }

    public static void buildProfile(String input, String outputFolder, Map<String, Integer> organisms) throws IOException {

        //File directory = new File(outputFolder);
        //Uploader up = new Uploader();
        //up.start();
        int organismsSize = organisms.size();
        File inp = new File(input);
        List<String> lines = Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8);

        Map<String, Integer> pathwaysPreserve = new HashMap<String, Integer>();
        Map<String, Integer> protsPreserve = new HashMap<String, Integer>();
        if (lines.size() > 0) {
            String currentProtein = lines.get(0).substring(0, lines.get(0).indexOf("\t"));
            String currentPathway = lines.get(0).substring(0, lines.get(0).indexOf("\t"));
            currentPathway = currentPathway.substring(0, currentPathway.indexOf("$"));
            //System.out.println(currentPathway);

            ArrayList<int[]> matches = new ArrayList<int[]>(100);

            matches.add(new int[organismsSize]);
            matches.get(0);
            ArrayList<String> names = new ArrayList<String>(100);
            names.add(currentProtein);

            Map<String, Integer> duplicates = new HashMap<String, Integer>();
            //String pathName;
            File t1;
            FileWriter w1;
            BufferedWriter wr1;

            //int countDuplicates = 0;
            int listIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                String[] tmp = lines.get(i).split("\t");
                String comb = tmp[0] + "\t" + tmp[1];
                if (duplicates.containsKey(comb)) {
                    //System.out.println(i);
                    //countDuplicates++;
                } else {
                    duplicates.put(comb, 1);
                    if (!tmp[0].equals(currentProtein)) {
                        //System.out.println("loop "+i+"  inside");
                        if (protsPreserve.containsKey(tmp[0])) {
                            System.out.println("fatal error, protein was repeated.. OUT OF ORDER");
                        } else {
                            protsPreserve.put(tmp[0], i);
                            currentProtein = tmp[0];
                            //System.out.println(currentProtein);

                            if (!tmp[0].substring(0, tmp[0].indexOf("$")).equals(currentPathway)) {
                                if (pathwaysPreserve.containsKey(tmp[0])) {
                                    System.out.println("fatal error, pathway was repeated.. OUT OF ORDER");
                                } else {
                                    pathwaysPreserve.put(tmp[0], i);
                                    // System.out.println(currentPathway);
                                }

                                //write them
                                //pathName = currentPathway;
                                //System.out.println(currentPathway);
                                t1 = new File(outputFolder + "/" + currentPathway);//+"/"+inp.getName());
                                t1.mkdirs();
                                t1 = new File(outputFolder + "/" + currentPathway + "/" + inp.getName());
                                w1 = new FileWriter(t1);
                                wr1 = new BufferedWriter(w1);
                                for (int j = 0; j < matches.size(); j++) {
                                    //System.out.println(names.get(j));
                                    wr1.write(names.get(j) + "|");
                                    for (int k = 0; k < matches.get(j).length; k++) {
                                        wr1.write(matches.get(j)[k] + " ");
                                    }
                                    wr1.newLine();
                                }
                                wr1.close();
                                //System.out.println(currentPathway + " emptied");
                                //empty them
                                listIndex = 0;
                                matches = new ArrayList<int[]>(100);
                                matches.add(new int[organismsSize]);
                                names = new ArrayList<String>(100);
                                names.add(currentProtein);
                                currentPathway = tmp[0].substring(0, tmp[0].indexOf("$"));

                            } else {

                                names.add(currentProtein);
                                matches.add(new int[organismsSize]);
                                listIndex++;
                            }
                        }

                    }
                    tmp[1] = tmp[1].substring(0, 17);
//                int[] tmpArray = prots.get(tmp[0]);
//                int index = organisms.get(tmp[1]);
//                tmpArray[index] = tmpArray[index] + 1;

                    matches.get(listIndex)[organisms.get(tmp[1])] = 1 + matches.get(listIndex)[organisms.get(tmp[1])];

                }

            }
            t1 = new File(outputFolder + "/" + currentPathway);//+"/"+inp.getName());
            t1.mkdirs();
            t1 = new File(outputFolder + "/" + currentPathway + "/" + inp.getName());
            w1 = new FileWriter(t1);
            wr1 = new BufferedWriter(w1);
            for (int j = 0; j < matches.size(); j++) {
                //System.out.println(names.get(j));
                wr1.write(names.get(j) + "|");
                for (int k = 0; k < matches.get(j).length; k++) {
                    wr1.write(matches.get(j)[k] + " ");
                }
                wr1.newLine();
            }
            wr1.close();

            //System.out.println(currentPathway + " emptied");
            //System.out.println(countDuplicates);
        }

    }

    @Override
    public void run() {
        //System.out.println(message);

        // try {
//            
//            Uploader up = new Uploader(port);
//            
//            up.start();
//            
//            Socket client;
//            client = new Socket("localhost", port);
//            
//            OutputStream outToServer = client.getOutputStream();
//            DataOutputStream out = new DataOutputStream(outToServer);
        //   System.out.println("Just connected to " + client.getRemoteSocketAddress());
        // Process p = null;
        File threadFolder = new File(outFolder + "ThreadLogs/");// + String.valueOf(threadid));
        threadFolder.mkdirs();
        File threadLog = new File(outFolder + "ThreadLogs/" + String.valueOf(threadid));
        long t1;
        long t2;
        int localcount;
        long sleptTime = -1;
        try {
            FileWriter fw1 = new FileWriter(threadLog);
            BufferedWriter wr1 = new BufferedWriter(fw1);

            if (profileType.equals("1")) {
                while (globalCount < fileNumber) {
                    try {
                        sleptTime = System.nanoTime();
                        lock.lock();
                        sleptTime = System.nanoTime() - sleptTime;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    localcount = globalCount;
                    globalCount++;
                    lock.unlock();
                    //System.out.println("I am thread "+message+ " and I hold the lock and I change gc "+globalCount);
                    if (localcount < fileNumber) {
                        //formulate command
                        String pwyFile = listOfFiles[localcount].getPath();// + listOfFiles[globalCount].getName();
                        //String command = "blastp -query " + pwyFile + " -db " + dbPath + " -evalue 0.000001 -outfmt 6 -out " + blastOutPath + listOfFiles[globalCount].getName() + ".blastp";
                        String command = vanillaCommand.replaceAll("inFile", pwyFile);
                        String outFile = outFolder + listOfFiles[localcount].getName();
                        command = command.replaceAll("outFile", outFile);
                        outFile = outFile + ".blastp";
//end formulate command
                        //   globalCount++;
                        //   lock.unlock();
                        //command="pwd";
                        //System.out.println(command);

                        //String output = 
                        t1 = System.currentTimeMillis();
                        executeCommandLD(command);
                        t2 = System.currentTimeMillis();
                        wr1.write(String.valueOf(t2 - t1) + " ");
                        t1 = System.currentTimeMillis();
                        try {
                            buildProfile(outFile, outFolder, organisms);
                        } catch (IOException ex) {
                            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        t2 = System.currentTimeMillis();
                        wr1.write(String.valueOf(t2 - t1) + " " + String.valueOf(sleptTime));
                        wr1.newLine();

                        //String profilerCommand="inPath;outPath";
                        //alker(profilerCommand,"localhost","56789");
                        //System.out.println("Pathway "+pwyFile+" "+output);
                    } else {
                        lock.unlock();
                        break;
                    }

                }
            } else if (profileType.equals("2")) {
                while (globalCount < fileNumber) {
                    try {
                        sleptTime = System.nanoTime();
                        lock.lock();
                        sleptTime = System.nanoTime() - sleptTime;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    localcount = globalCount;
                    globalCount++;
                    lock.unlock();

                    //System.out.println("I am thread "+message+ " and I hold the lock and I change gc "+globalCount);
                    if (localcount < fileNumber) {
                        //formulate command
                        String pwyFile = listOfFiles[localcount].getPath();// + listOfFiles[globalCount].getName();
                        //String command = "blastp -query " + pwyFile + " -db " + dbPath + " -evalue 0.000001 -outfmt 6 -out " + blastOutPath + listOfFiles[globalCount].getName() + ".blastp";
                        String command = vanillaCommand.replaceAll("inFile", pwyFile);
                        String outFile = outFolder + listOfFiles[localcount].getName();
                        command = command.replaceAll("outFile", outFile);
                        outFile = outFile + ".blastp";
//end formulate command
                        //globalCount++;
                        // lock.unlock();
                        //command="pwd";
                        //System.out.println(command);

                        //String output = 
                        t1 = System.currentTimeMillis();
                        executeCommandLD(command);
                        t2 = System.currentTimeMillis();
                        wr1.write(String.valueOf(t2 - t1) + " ");
                        t1 = System.currentTimeMillis();

                        try {
                            BBHbuildProfile(outFile, outFolder, organisms);
                        } catch (IOException ex) {
                            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        t2 = System.currentTimeMillis();
                        wr1.write(String.valueOf(t2 - t1) + " " + String.valueOf(sleptTime));
                        wr1.newLine();

                        //String profilerCommand="inPath;outPath";
                        //alker(profilerCommand,"localhost","56789");
                        //System.out.println("Pathway "+pwyFile+" "+output);
                    } else {
                        lock.unlock();
                        break;
                    }

                }
            } else {
                System.out.println("FATAL error, unknown profile type. exiting...");
            }

            //p.destroy();
            //     out.close();
            //     client.close();
//        } catch (IOException ex) {
//            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
//        }
            wr1.close();
        } catch (IOException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

//Experimental code below

/*
 public static void buildProfile(String input, String outputFolder, Map<String, Integer> organisms) throws IOException {

 //File directory = new File(outputFolder);
 //Uploader up = new Uploader();
 //up.start();
 int organismsSize = organisms.size();
 List<String> lines = Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8);
 Map<String, Integer[]> prots = new HashMap<String, Integer[]>();
 Map<String, Integer> duplicates = new HashMap<String, Integer>();
 int countDuplicates = 0;
 for (int i = 0; i < lines.size(); i++) {
 String[] tmp = lines.get(i).split("\t");
 String comb = tmp[0] + "\t" + tmp[1];
 if (duplicates.containsKey(comb)) {
 //System.out.println(i);
 // countDuplicates++;
 } else {
 duplicates.put(comb, 1);
 //System.out.println(comb);
 String tmpProt = tmp[0];//lines.get(i).substring(0, lines.get(i).indexOf("\t"));

 if (!prots.containsKey(tmpProt)) {
 Integer[] matches = new Integer[organismsSize];
 for (int sot = 0; sot < organismsSize; sot++) {
 matches[sot] = 0;
 }
 prots.put(tmpProt, matches);
 } else {
 // System.out.println(tmpProt);
 }
 tmp[1] = tmp[1].substring(0, 17);
 Integer[] tmpArray = prots.get(tmp[0]);
 int index = organisms.get(tmp[1]);
 tmpArray[index] = tmpArray[index] + 1;

 }

 }
 String currentDir;
 File inp = new File(input);
 String wholePath;
 String file;
 int count = 0;
 //TreeMap<Integer,String> sortedMap = new TreeMap<Integer,String>();
 Map<String, TreeMap<Integer, String>> fs = new HashMap<String, TreeMap<Integer, String>>();
 for (Map.Entry<String, Integer[]> entry1 : prots.entrySet()) {
 String key1 = entry1.getKey();
 // System.out.println(key1);
 String[] splitted = key1.split("\\$");
 String filename = splitted[0];//key1.substring(0, key1.indexOf("$"));
 // System.out.println("Filename :"+filename);
 if (!fs.containsKey(filename)) {

 TreeMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
 fs.put(filename, sortedMap);
 }

 String id = splitted[1];//key1.substring(key1.indexOf("$"),key1.length());
 // System.out.println("id "+id);
 String arrayLine = "";
 Integer[] smthing = entry1.getValue();
 for (int i = 0; i < smthing.length; i++) {
 //System.out.println(smthing[i] + " ");
 arrayLine = arrayLine + (smthing[i] + " ");
 }

 fs.get(filename).put(Integer.valueOf(splitted[1]), key1 + "|" + arrayLine);

 }

 //note that you can avoid this loop by copying this part above
 for (Map.Entry<String, TreeMap<Integer, String>> entry1 : fs.entrySet()) {
 File t1 = new File(outputFolder + "/" + entry1.getKey());//+"/"+inp.getName());
 t1.mkdirs();
 t1 = new File(outputFolder + "/" + entry1.getKey() + "/" + inp.getName());
 FileWriter w1 = new FileWriter(t1);
 BufferedWriter wr1 = new BufferedWriter(w1);
 for (Map.Entry<Integer, String> entry2 : entry1.getValue().entrySet()) {
 wr1.write(entry2.getValue());
 wr1.newLine();
 //System.out.println(entry2.getValue());
 }
 wr1.close();

 }
 // System.out.println("closed");
 //UPLOADER here

 }
 */
