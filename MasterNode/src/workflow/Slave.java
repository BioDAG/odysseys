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
package workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

    public Slave(int threadid, File[] listOfFiles, int fileNumber, String blastOutPath,Lock lock, String vanillaCommand) {
        this.message = message;
        this.listOfFiles = listOfFiles;
        this.fileNumber = fileNumber;
        this.outFolder=blastOutPath;       
        this.lock=lock;
        this.vanillaCommand=vanillaCommand;

    }

    public static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            p.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return output.toString();
        // return "nothing";

    }

    @Override
    public void run() {
        //System.out.println(message);
        
        while (globalCount < fileNumber) {
            try {
                lock.lock();
            } catch (InterruptedException ex) {
                Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
            }

            //System.out.println("I am thread "+message+ " and I hold the lock and I change gc "+globalCount);
            if (globalCount < fileNumber) {
                //formulate command
                String pwyFile = listOfFiles[globalCount].getPath();// + listOfFiles[globalCount].getName();
                //String command = "blastp -query " + pwyFile + " -db " + dbPath + " -evalue 0.000001 -outfmt 6 -out " + blastOutPath + listOfFiles[globalCount].getName() + ".blastp";
                String command=vanillaCommand.replaceAll("inFile", pwyFile);
                command=command.replaceAll("outFile", outFolder + listOfFiles[globalCount].getName());
//end formulate command
                globalCount++;
                lock.unlock();
                System.out.println(command);
                
                //String output = executeCommand(command);
                
               // System.out.println("Pathway "+pwyFile+" "+output);
            } else {
                lock.unlock();
                break;
            }

        }
    }

}
