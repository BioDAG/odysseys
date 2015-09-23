/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blastbenchmark;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author thanos
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.TreeMap;
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
    //Uploader ps;
    LinkedHashMap<String, Integer> organisms;
    Integer port;
    String profileType;
    int threadid;
    //static Map<String,BufferedWriter> writers=new HashMap<String,BufferedWriter>();

    public Slave(int threadid,String blastOutPath, Lock lock, String vanillaCommand, String profileType) throws IOException {
        this.message = message;
        this.listOfFiles = listOfFiles;
        this.fileNumber = fileNumber;
        this.outFolder = blastOutPath;
        this.lock = lock;
        this.vanillaCommand = vanillaCommand;
        
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

    @Override
    public void run() {

        long t1=System.currentTimeMillis();
        String command = "/opt/ncbi-blast-2.2.30+/bin/blastp -query test_chunk.fa -db DB/DB -evalue 0.000001 -outfmt 6 -out outFile.blastp";
   
        String proc=executeCommandLD(command);
        long t2=System.currentTimeMillis();
        System.out.println("Thread "+threadid +" time "+(t2-t1));

    }

}
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
