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
package organismidassigner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class OrganismIdAssigner {

    /**
     * @param args the command line arguments
     */
    public static void organismsID(String originalfastaFolderPath, String organismsWithIDFolder, String listPath) throws IOException {
        File folder = new File(organismsWithIDFolder);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            listOfFiles[i].delete();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(listPath));
            String line;

            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String[] list = line.split(";");
                BufferedReader br1 = null;
                String filepath = originalfastaFolderPath + list[1] + ".fasta";
                String filepath2 = originalfastaFolderPath + list[1] + ".tfa";
                //System.out.println(filepath);
                File r1 = new File(filepath);
                File r2 = new File(filepath2);
                if (!r1.exists() && !r2.exists()) {
                    //System.out.println(list[1]);
                //    filepath = originalfastaFolderPath + list[1] + ".tfa";
                    //      r1 = new File(filepath);
//                    if (!r1.exists()) {
                    //System.out.println("FILE " + list[1] + " does not exist.. maybe belongs to different node?");
//                    }
                } else {
                    //System.out.println(filepath);
                    if(r1.exists())
                    {
                        filepath=filepath;
                    }
                    else
                    {
                        filepath=filepath2;
                    }
                    br1 = new BufferedReader(new FileReader(filepath));
                    File file = new File(organismsWithIDFolder + list[4].substring(0, list[4].length() - 1) + ".fasta");
                    FileWriter fw;
                    fw = new FileWriter(file);
                    BufferedWriter t1 = new BufferedWriter(fw);
                    int count = 1;
                    int countProteins = 0;
                    while ((line = br1.readLine()) != null) {

                        if (line.contains(">")) {
                            String protId;
                            countProteins++;
                            //temp=Integer.toString(count);
                            protId = String.format("%06d", count);
                            //System.out.println(temp);
                            line = ">" + list[4].substring(0,list[4].length()-1) +"$"+ protId + "$" + line.substring(1, line.length());
                            count++;
                            //  System.out.println(line);
                            t1.write(line);
                            t1.newLine();
                        } else {
                            t1.write(line);
                            t1.newLine();
                        }

                    }
                    t1.close();

                    int old = Integer.valueOf(list[3]);
                    if (old != countProteins) {
                        System.out.println("MISMATCH DETECTED");
                        System.out.println(list[1] + "  WAS   " + list[3] + "   COUNTED   " + countProteins);
                    }

                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

    }

    public static void main(String[] args) throws IOException {
//        String organismsFolder = args[0];
//        String list = args[1];
//        String ouputFolder = args[2];
        
        String organismsFolder = "/home/thanos/Fotis/plantsProject/cyanobacteria/";
        String list = "/home/thanos/Fotis/plantsProject/list.csv";
        String ouputFolder = "/home/thanos/Fotis/plantsProject/cyanobacteriaWithID/";
        
//        organismsFolder="/home/thanos/workDirectory/organisms/";
//        ouputFolder="/home/thanos/workDirectory/organismsWithID/";
//        list="/home/thanos/workDirectory/list.csv";
        organismsID(organismsFolder, ouputFolder, list);
    }

}
