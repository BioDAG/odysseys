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

package blastbenchmark;


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

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