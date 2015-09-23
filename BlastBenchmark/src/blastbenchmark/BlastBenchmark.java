/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blastbenchmark;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thanos
 */
public class BlastBenchmark {

    
    public static void localDistributer(String inputFolder, String outputFolder, String vanillaCommand, int threads,String profileType) throws InterruptedException, IOException {
        int i;
        
        

        File folder = new File(inputFolder);
        //File theDir = new File(blastOutPath);
        //theDir.mkdirs();
        File[] listOfFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        ArrayList<Slave> SlaveList = new ArrayList<Slave>();
        Lock lock = new Lock();
        for (i = 0; i < threads; i++) {
            Slave t = new Slave(i, outputFolder, lock, vanillaCommand,profileType);
            SlaveList.add(t);
            t.start();
        }
        for (i = 0; i < SlaveList.size(); i++) {
            SlaveList.get(i).join();
        }
    }
    /**
     * @param args the command line arguments
     */
       public static void main(String[] args) throws InterruptedException, IOException {
        String inputFolder =  "";
        String outputFolder =  "";
        String vanillaCommand = "";
        String treeOrder = "";
        String threads = "8";
        String profileType="";
        
        
//        String inputFolder =  "/home/thanos/workDirectory/chunks/"; //
//        String outputFolder =  "/home/thanos/workDirectory/blastOut/";//
//        String vanillaCommand = "blastp -query inFile -db /home/thanos/workDirectory/DB/DB -evalue 0.000001 -outfmt 6 -out outFile.blastp";//
//        String treeOrder = "/home/thanos/workDirectory/TreeOrder.txt";
//        String threads = "8";
        //String pathwaysFolder=

        //inputFolder="/home/thanos/workDirectory/organismsWithID";
        //outputFolder="/home/thanos/workDirectory/HITorganismsWithID/";
        //vanillaCommand="/home/thanos/workDirectory/code/cd-hit/cd-hit -i inFile -o outFile -c 1.00 -n 5 -g 1";
        //threads="8";
        //sample arguments for blast !!NOTE DB path is hardcoded, args of blast are hardcoded
        //"/home/thanos/Fotis/plantsProject/pathways/" "/home/thanos/Fotis/plantsProject/blastOutputAllvAll/" "blastp -query inFile -db home/thanos/Fotis/plantsProject/plantBlastDB/PlantsDB -evalue 0.000001 -outfmt 6 -out outFile.blastp" "8"
        //cd hit sample args
        //"/home/thanos/Fotis/plantsProject/organismsWithID" "/home/thanos/Fotis/plantsProject/cdHITED/" "~/tools/cd-hit/cd-hit -i inFile -o outFile -c 1.00 -n 5 -g 1 -T 0 1>/dev/null" "8"
        //System.out.println("ARG0|"+args[0]);
       // System.out.println("ARG1|"+args[1]);
       // System.out.println("ARG2|"+args[2]);
       // System.out.println("ARG3|"+args[3]);
        
        
        
        localDistributer(inputFolder, outputFolder, vanillaCommand, Integer.valueOf(threads),profileType);
        
        
        
        
        //System.out.println(inputFolder);
    }
    
}
