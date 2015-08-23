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
package localdistributer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;


public class LocalDistributer {

    /**
     * @param args the command line arguments
     */
    public static void localDistributer(String inputFolder, String outputFolder, String vanillaCommand, int threads) throws InterruptedException {
        int i;
        File folderClear = new File(outputFolder);
        File[] listOfFilesClear = folderClear.listFiles();
        if (listOfFilesClear.length > 0) {
            for (i = 0; i < listOfFilesClear.length; i++) {
                listOfFilesClear[i].delete();
            }
        }

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
            Slave t = new Slave(i, listOfFiles, listOfFiles.length, outputFolder, lock, vanillaCommand);
            SlaveList.add(t);
            t.start();
        }
        for (i = 0; i < SlaveList.size(); i++) {
            SlaveList.get(i).join();
        }
    }    

    public static void main(String[] args) throws InterruptedException {
        String inputFolder = args[0];
        String outputFolder = args[1];
        String vanillaCommand = args[2];
        String threads = args[3];

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
        localDistributer(inputFolder, outputFolder, vanillaCommand, Integer.valueOf(threads));
        
        
        
        
        //System.out.println(inputFolder);
    }

}
