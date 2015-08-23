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
package pathwayidassigner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author thanos
 */
public class PathwayIDAssigner {

    public static void pathwaysID(String originalfastaFolderPath, String organismsWithIDFolder) throws IOException {
        File folder = new File(organismsWithIDFolder);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            listOfFiles[i].delete();
        }
        //BufferedReader br = null;
        folder = new File(originalfastaFolderPath);
        listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String filename = listOfFiles[i].getName();
            List<String> tfile = Files.readAllLines(Paths.get(listOfFiles[i].getAbsolutePath()), StandardCharsets.UTF_8);
            FileWriter fw;
            File file = new File(organismsWithIDFolder + filename);
            fw = new FileWriter(file);
            BufferedWriter t1 = new BufferedWriter(fw);
            int count = 1;
            int countProteins = 0;
            String protID;
            for(int j=0;j<tfile.size();j++) {
                String line=tfile.get(j);

                if (line.contains(">")) {
                    protID = String.format("%06d", count);
                    line = ">"+filename+"$"+protID+"$"+line.substring(1, line.length());
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
            System.out.println(filename+";"+(count-1));

        }
    }

    public static void main(String[] args) throws IOException {
        String organismsFolder ;//= args[0];
        String ouputFolder ;//= args[2];

//        organismsFolder="/home/thanos/workDirectory/organisms/";
//        ouputFolder="/home/thanos/workDirectory/organismsWithID/";
//        list="/home/thanos/workDirectory/list.csv";
        organismsFolder="/home/thanos/Fotis/plantsProject/cyanobacteria/";
        ouputFolder="/home/thanos/Fotis/plantsProject/pathwaysIDCOUNT/";
        pathwaysID(organismsFolder, ouputFolder);
    }

}
