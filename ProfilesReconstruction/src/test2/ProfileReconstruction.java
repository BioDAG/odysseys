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
package test2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ProfileReconstruction {

    /**
     * @param args the command line arguments
     */
    public static String[] lineAdder(ArrayList<String[]> lines) {   //tested with matlab WORKS
        Integer[] sum = new Integer[lines.get(0).length];
        for (int i = 0; i < sum.length; i++) {
            sum[i] = 0;
        }
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).length; j++) {
                sum[j] = sum[j] + Integer.valueOf(lines.get(i)[j]);
            }

        }

        String[] strSum = new String[lines.get(0).length];
        for (int i = 0; i < sum.length; i++) {
            strSum[i] = String.valueOf(sum[i]);
        }
        return strSum;
    }

    //matlab tested for no lines missing.. works
    //testing by zeroing all3 prot 4
    public static void merger(String folderIN, String outPath, int numSeqs, int numGenomes) throws IOException {
        //List<String> lines1 = Files.readAllLines(Paths.get(input1), StandardCharsets.UTF_8);
        //List<String> lines2 = Files.readAllLines(Paths.get(input2), StandardCharsets.UTF_8);
        //int numSeqs = 5;
        String[] zeros = new String[numGenomes];
        for (int i = 0; i < numGenomes; i++) {
            zeros[i] = "0";
        }

        File out = new File(outPath);
        FileWriter out1 = new FileWriter(out);
        BufferedWriter wr1 = new BufferedWriter(out1);

        File folder = new File(folderIN);
        File[] files = folder.listFiles();

        FileReader[] fr = new FileReader[files.length];
        BufferedReader[] r = new BufferedReader[files.length];
        String[] currentLines = new String[files.length];
        int[] flags = new int[files.length];

        for (int i = 0; i < files.length; i++) {
            //System.out.println(files[i].getName());
            fr[i] = new FileReader(files[i]);
            r[i] = new BufferedReader(fr[i]);
            flags[i] = 0;
        }
        ArrayList<String[]> current;
        String[] tmp;
        String arrayPart;
        String[] output;
        for (int i = 0; i < numSeqs; i++) {
            current = new ArrayList<String[]>(); //ArrayList<String[]>
            for (int j = 0; j < files.length; j++) {
                if (flags[j] == 0) {
                    // System.out.println("line read");
                    currentLines[j] = r[j].readLine();

                }
                if (currentLines[j] == null) {
                    flags[j] = 1;
                } else {
                    // System.out.println(currentLines[j]);
                    tmp = currentLines[j].split("\\$");
                    //System.out.println(tmp[1] + "           " + i + 1);
                    //System.out.println(Integer.valueOf(tmp[1]));
                    if (Integer.valueOf(tmp[1]) == (i + 1)) {
                        // System.out.println("inside");
                        flags[j] = 0;
                        //System.out.println("file " + files[j] + " line ADDED #" + (i + 1));
                        //System.out.println(currentLines[j].substring(20,currentLines[j].length()));
                        arrayPart = currentLines[j].substring(currentLines[j].lastIndexOf("|") + 1, currentLines[j].length());
                        current.add(arrayPart.split(" "));
                    } else {
                        flags[j] = 1;
                        // System.out.println("file " + files[j] + " line missing #" + (i + 1));
                    }
                }

            }
            if (current.isEmpty()) {
                //System.out.println("line empty");

                output = zeros;
            } else {
                output = lineAdder(current);
            }

            for (int j = 0; j < output.length; j++) {
                wr1.write(output[j] + " ");
            }
            wr1.newLine();

        }

        wr1.close();

    }

    //obsolete
    public static String[] lineComparator(ArrayList<String[]> lines) {   //tested with matlab WORKS
        String[] strMin = new String[lines.get(0).length];
        for (int i = 0; i < strMin.length; i++) {
            strMin[i] = "none";
        }
        double oldEvalue;
        double newEvalue;
        double oldIdentity;
        double newIdentity;
        String[] tmpNew;
        String[] tmpOld;

        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).length; j++) {

                tmpNew = lines.get(i)[j].split(";");
                if (strMin[j].equals("none") && !lines.get(i)[j].equals("none")) {
                    strMin[j] = tmpNew[2];//lines.get(i)[j];
                } else if (!strMin[j].equals("none") && !lines.get(i)[j].equals("none")) {

                    newEvalue = Double.valueOf(tmpNew[0]);
                    newIdentity = Double.valueOf(tmpNew[1]);

                    tmpOld = strMin[j].split(";");
                    System.out.println(strMin[j]);
                    oldEvalue = Double.valueOf(tmpOld[0]);
                    oldIdentity = Double.valueOf(tmpOld[1]);

                    if (newEvalue < oldEvalue) {
                        strMin[j] = tmpNew[2];//lines.get(i)[j];
                    } else if (newEvalue == oldEvalue) {
                        if (newIdentity < oldIdentity) {
                            strMin[j] = tmpNew[2];//lines.get(i)[j];
                        } else if (newIdentity == oldIdentity) {
                            System.out.println("ABSOLUTE MATCH: ");
                        }

                    }
                }
                //strMin[j] = strMin[j] + Integer.valueOf(lines.get(i)[j]);
            }

        }

//        String[] strSum = new String[lines.get(0).length];
//        for (int i = 0; i < min.length; i++) {
//            strSum[i] = String.valueOf(min[i]);
//        }
        return strMin;
    }

    //obsolete
    public static void BBHmerger(String folderIN, String outPath, int numSeqs, int numGenomes) throws IOException {

        //List<String> lines1 = Files.readAllLines(Paths.get(input1), StandardCharsets.UTF_8);
        //List<String> lines2 = Files.readAllLines(Paths.get(input2), StandardCharsets.UTF_8);
        //int numSeqs = 5;
        String[] zeros = new String[numGenomes];
        for (int i = 0; i < numGenomes; i++) {
            zeros[i] = "none";
        }

        File out = new File(outPath);
        FileWriter out1 = new FileWriter(out);
        BufferedWriter wr1 = new BufferedWriter(out1);

        File folder = new File(folderIN);
        File[] files = folder.listFiles();

        FileReader[] fr = new FileReader[files.length];
        BufferedReader[] r = new BufferedReader[files.length];
        String[] currentLines = new String[files.length];
        int[] flags = new int[files.length];

        for (int i = 0; i < files.length; i++) {
            //System.out.println(files[i].getName());
            fr[i] = new FileReader(files[i]);
            r[i] = new BufferedReader(fr[i]);
            flags[i] = 0;
        }
        ArrayList<String[]> current;
        String[] tmp;
        String arrayPart;
        String[] output;
        for (int i = 0; i < numSeqs; i++) {
            current = new ArrayList<String[]>(); //ArrayList<String[]>
            for (int j = 0; j < files.length; j++) {
                if (flags[j] == 0) {
                    // System.out.println("line read");
                    currentLines[j] = r[j].readLine();

                }
                if (currentLines[j] == null) {
                    flags[j] = 1;
                } else {
                    // System.out.println(currentLines[j]);
                    tmp = currentLines[j].split("\\$");
                    //System.out.println(tmp[1] + "           " + i + 1);
                    //System.out.println(Integer.valueOf(tmp[1]));
                    if (Integer.valueOf(tmp[1]) == (i + 1)) {
                        // System.out.println("inside");
                        flags[j] = 0;
                        //System.out.println("file " + files[j] + " line ADDED #" + (i + 1));
                        //System.out.println(currentLines[j].substring(20,currentLines[j].length()));
                        arrayPart = currentLines[j].substring(currentLines[j].lastIndexOf("|") + 1, currentLines[j].length());
                        current.add(arrayPart.split(" "));
                    } else {
                        flags[j] = 1;
                        // System.out.println("file " + files[j] + " line missing #" + (i + 1));
                    }
                }

            }
            if (current.isEmpty()) {
                System.out.println("line empty  " + i + "   file" + folderIN);

                output = zeros;
                System.out.println(output.length);
            } else {
                output = lineComparator(current);
            }

            for (int j = 0; j < output.length; j++) {
                wr1.write(output[j] + " ");
            }
            wr1.newLine();

        }

        wr1.close();

    }

    //obsolete
    public static void columnSplitter(String inPath, String outPath, int columns) throws IOException {
        File[] files = new File[columns];
        FileWriter[] fw = new FileWriter[columns];
        BufferedWriter[] wr = new BufferedWriter[columns];
        File f1 = new File(inPath);
        FileReader fr1 = new FileReader(f1);
        BufferedReader r1 = new BufferedReader(fr1);

        for (int i = 0; i < columns; i++) {
            files[i] = new File(outPath + i);
            fw[i] = new FileWriter(files[i]);
            wr[i] = new BufferedWriter(fw[i]);
        }

        String line;
        String[] tmp;
        int count = 0;
        while ((line = r1.readLine()) != null) {
            {
                if (line.charAt(0) == ' ') {
                    break;
                }
                count++;
                tmp = line.split(" ");
                //System.out.println(tmp.length+"  "+count);
                //System.out.println(line);
                for (int i = 0; i < tmp.length; i++) {
                    // System.out.println(tmp[i]);
                    wr[i].write(tmp[i]);
                    wr[i].newLine();
                }

            }

        }
        for (int i = 0; i < columns; i++) {

            wr[i].close();
        }

    }

    //obsolete
    public static void columnMatcher(String inColsFolder, String outColsFolder, LinkedHashMap<String, Integer> orderedSizes) throws IOException {
        int columns = orderedSizes.size();
        File[] filesOut = new File[columns];
        FileWriter[] fwOut = new FileWriter[columns];
        BufferedWriter[] wr = new BufferedWriter[columns];
        File[] filesIn = new File[columns];
        FileReader[] frIn = new FileReader[columns];
        BufferedReader[] r = new BufferedReader[columns];

        for (int i = 0; i < columns; i++) {
            filesOut[i] = new File(outColsFolder + i);
            fwOut[i] = new FileWriter(filesOut[i]);
            wr[i] = new BufferedWriter(fwOut[i]);

            filesIn[i] = new File(inColsFolder + i);
            frIn[i] = new FileReader(filesIn[i]);
            r[i] = new BufferedReader(frIn[i]);
        }
        int[] sizes = new int[orderedSizes.size()];
        String[] organisms = new String[orderedSizes.size()];
        int kls = 0;
        for (Map.Entry<String, Integer> entry1 : orderedSizes.entrySet()) {
            sizes[kls] = entry1.getValue();
            organisms[kls] = entry1.getKey();
            kls++;
        }
        //currentProtein = key + String.format("%06d", (i + 1));
        String key;
        int value;
        int startingPos;
        String line;
        String[] crossArray;
        String[] localArray;
        String currentProtein;
        int foreignIndex;
        for (int j = 0; j < columns; j++) {
            //for (Map.Entry<String, Integer> entry1 : orderedSizes.entrySet()) {}
            //startingPos = j;

            for (int k = j; k < sizes.length; k++) { //localBlockIndex

                crossArray = new String[sizes[k]];
                for (int i = 0; i < sizes[k]; i++) {    //remote block Index
                    crossArray[i] = r[k].readLine();
                }

                localArray = new String[sizes[k]];
                for (int i = 0; i < sizes[k]; i++) {
                    // currentProtein = organisms[k] + String.format("%06d", (i + 1));
                    //System.out.println(currentProtein);
                    localArray[i] = r[j].readLine();

                }
                for (int i = 0; i < sizes[k]; i++) {
                    currentProtein = organisms[k] + "$" + String.format("%06d", (i + 1));
                    System.out.println(currentProtein);
                    System.out.println(localArray[i]);
                    if (!localArray[i].equals("none")) {
                        foreignIndex = Integer.valueOf(localArray[i].substring(localArray[i].indexOf("$") + 1, localArray[i].length())) - 1;
                        System.out.println(foreignIndex);
                        if (!crossArray[foreignIndex].equals(currentProtein)) {
                            crossArray[foreignIndex] = "0";
                            localArray[i] = "0";
                        }
                    }

                }
                for (int i = 0; i < sizes[k]; i++) {
                    wr[j].write(localArray[i]);
                    wr[j].newLine();

                    wr[k].write(crossArray[i]);
                    wr[k].newLine();
                }
            }

        }

        for (int i = 0; i < columns; i++) {

            wr[i].close();
        }

    }

    public static void blockMatcher(String block1, String block2, String outPath, int size1, int size2) throws IOException {

        File f1 = new File(block1);
        List<String> blockLines1 = Files.readAllLines(Paths.get(block1), StandardCharsets.UTF_8);
        String name1 = blockLines1.get(0).substring(0, blockLines1.get(0).indexOf("$"));

        String[] b1 = new String[size1];
        int index;
        int count = 0;

        for (int i = 0; i < size1; i++) {
            //String temp = blockLines1.
            if (count < blockLines1.size()) {
                index = Integer.valueOf(blockLines1.get(count).substring(blockLines1.get(count).indexOf("$") + 1, blockLines1.get(count).indexOf("|")));
                // System.out.println(index);
                if (index == (i + 1)) {
                    b1[i] = blockLines1.get(count);
                    count++;
                } else {
                    // System.out.println("line missing " + (i + 1));
                    b1[i] = name1 + "$" + String.format("%06d", i + 1) + "|0";

                }
            } else {
                b1[i] = name1 + "$" + String.format("%06d", i + 1) + "|0";;
            }

        }
        int foreignIndex;
        String matching;
        String tmp;
        File fw1 = new File(outPath + "/" + f1.getName());
        FileWriter fww1 = new FileWriter(fw1);
        BufferedWriter wr1 = new BufferedWriter(fww1);

        if (!block1.equals(block2)) {

            File f2 = new File(block2);
            List<String> blockLines2 = Files.readAllLines(Paths.get(block2), StandardCharsets.UTF_8);
            String name2 = blockLines2.get(0).substring(0, blockLines2.get(0).indexOf("$"));
            String[] b2 = new String[size2];

            count = 0;

            for (int i = 0; i < size2; i++) {
                //String temp = blockLines1.
                if (count < blockLines2.size()) {
                    index = Integer.valueOf(blockLines2.get(count).substring(blockLines2.get(count).indexOf("$") + 1, blockLines2.get(count).indexOf("|")));
                    if (index == (i + 1)) {
                        b2[i] = blockLines2.get(count);
                        count++;
                    } else {
                        // System.out.println("line missing " + (i + 1));
                        b2[i] = name2 + "$" + String.format("%06d", i + 1) + "|0";

                    }
                } else {
                    b2[i] = name2 + "$" + String.format("%06d", i + 1) + "|0";;
                }

            }
            for (int i = 0; i < size1; i++) {
                //System.out.println(i+" "+b1[i]);
                matching = b1[i].substring(b1[i].indexOf("|") + 1, b1[i].length());
                //System.out.println(matching);
                if (!matching.equals("0")) {
                    //System.out.println(b1[i]);
                    tmp = b1[i].substring(0, b1[i].indexOf("|"));
                    // System.out.println(tmp);
                    foreignIndex = Integer.valueOf(matching.substring(matching.indexOf("$") + 1, matching.length())) - 1;

                    // System.out.println(b2[foreignIndex]);
                    //System.out.println(foreignIndex+" "+tmp+"      "+(b2[foreignIndex].substring(b2[foreignIndex].indexOf("|")+1,b2[foreignIndex].length())));
                    if (!(b2[foreignIndex].substring(b2[foreignIndex].indexOf("|") + 1, b2[foreignIndex].length())).equals(tmp)) {
                        //System.out.println(b1[i] + "  " + foreignIndex + "    " + (b2[foreignIndex].substring(b2[foreignIndex].indexOf("|") + 1)));
                        //b2[foreignIndex] = b2[foreignIndex].substring(0, b2[foreignIndex].indexOf("|")) + "|" + "0";
                        b1[i] = tmp + "|" + "0";

                    }
                }
                wr1.write(b1[i]);
                wr1.newLine();
            }
            wr1.close();
            File fw2 = new File(outPath + "/" + f2.getName());
            FileWriter fww2 = new FileWriter(fw2);
            BufferedWriter wr2 = new BufferedWriter(fww2);

            for (int i = 0; i < size2; i++) {
                // System.out.println(b2[i]);
                matching = b2[i].substring(b2[i].indexOf("|") + 1, b2[i].length());
                if (!matching.equals("0")) {
                    tmp = b2[i].substring(0, b2[i].indexOf("|"));
                    foreignIndex = Integer.valueOf(matching.substring(matching.indexOf("$") + 1, matching.length())) - 1;
                    //System.out.println(foreignIndex+" "+tmp);
                    //System.out.println(b1[foreignIndex]);
                    if (!(b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length())).equals(tmp)) {

                        //b1[foreignIndex] = b1[foreignIndex].substring(0, b1[foreignIndex].indexOf("|")) + "|" + "0";
                        b2[i] = tmp + "|" + "0";

                    }
                }
                wr2.write(b2[i]);
                wr2.newLine();
            }
            wr2.close();

        } else {
            for (int i = 0; i < size1; i++) {
                //System.out.println(i+" "+b1[i]);
                matching = b1[i].substring(b1[i].indexOf("|") + 1, b1[i].length());
                //System.out.println(matching);
                if (!matching.equals("0")) {
                    //System.out.println(b1[i]);
                    tmp = b1[i].substring(0, b1[i].indexOf("|"));
                    // System.out.println(tmp);
                    foreignIndex = Integer.valueOf(matching.substring(matching.indexOf("$") + 1, matching.length())) - 1;

                    // System.out.println(b2[foreignIndex]);
                    //System.out.println(foreignIndex+" "+tmp+"      "+(b2[foreignIndex].substring(b2[foreignIndex].indexOf("|")+1,b2[foreignIndex].length())));
                    if (!(b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length())).equals(tmp)) {
                        //System.out.println(b1[i] + "  " + foreignIndex + "  " + (b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length())));// + "  " + foreignIndex + "    " + (b1[foreignIndex].substring(0, b1[foreignIndex].indexOf("|"))));
                        //System.out.println((b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length()))+"  "+tmp);
                        //b1[foreignIndex] = b1[foreignIndex].substring(0, b1[foreignIndex].indexOf("|")) + "|" + "0";
                        b1[i] = tmp + "|" + "0";

                    }
                }
                wr1.write(b1[i]);
                wr1.newLine();
            }
            wr1.close();

        }

        //System.out.println(blockLines1.get(0));
//       
//        for (int i = 0; i < size1; i++) {
//            wr1.write(b1[i]);
//            wr1.newLine();
//        }
//        wr1.close();
//
//        for (int i = 0; i < size2; i++) {
//            wr2.write(b2[i]);
//            wr2.newLine();
//        }
        //   wr1.close();
        //   wr2.close();
    }

    public static void fastBlockMatcher(String block1, String block2, String outPath, int size1, int size2) throws IOException {

        File f1 = new File(block1);

        File f2 = new File(block2);

        List<String> blockLines1 = Files.readAllLines(Paths.get(block1), StandardCharsets.UTF_8);

        // int size1 = 10;
        // int size2 = 10;
        //blockLines1.
        List<String> blockLines2 = Files.readAllLines(Paths.get(block2), StandardCharsets.UTF_8);
        //System.out.println(blockLines1.get(0));

        String name1 = blockLines1.get(0).substring(0, blockLines1.get(0).indexOf("$"));
        String name2 = blockLines2.get(0).substring(0, blockLines2.get(0).indexOf("$"));
        String[] b1 = new String[size1];
        HashMap<Integer, Integer> checkOnly1 = new HashMap<Integer, Integer>(size1);
        String[] b2 = new String[size2];
        HashMap<Integer, Integer> checkOnly2 = new HashMap<Integer, Integer>(size2);
        int index;
        int count = 0;
        for (int i = 0; i < size1; i++) {
            //String temp = blockLines1.
            if (count < blockLines1.size()) {
                index = Integer.valueOf(blockLines1.get(count).substring(blockLines1.get(count).indexOf("$") + 1, blockLines1.get(count).indexOf("|")));
                // System.out.println(index);
                if (index == (i + 1)) {
                    b1[i] = blockLines1.get(count);
                    //System.out.println(b1[i].substring(b1[i].indexOf("|"),b1[i].length()));
                    if (b1[i].substring(b1[i].indexOf("|") + 1, b1[i].length()).equals("0")) {
                        // System.out.println(b1[i]);
                        checkOnly1.put(i, 1);
                    }
                    count++;
                } else {
                    // System.out.println("line missing " + (i + 1));
                    b1[i] = name1 + "$" + String.format("%06d", i + 1) + "|0";

                }
            } else {
                b1[i] = name1 + "$" + String.format("%06d", i + 1) + "|0";;
            }

        }
        count = 0;

        for (int i = 0; i < size2; i++) {
            //String temp = blockLines1.
            if (count < blockLines2.size()) {
                index = Integer.valueOf(blockLines2.get(count).substring(blockLines2.get(count).indexOf("$") + 1, blockLines2.get(count).indexOf("|")));
                if (index == (i + 1)) {
                    b2[i] = blockLines2.get(count);
                    if (b2[i].substring(b2[i].indexOf("|") + 1, b2[i].length()).equals("0")) {
                        //System.out.println(b2[i]);
                        checkOnly2.put(i, 1);
                    }
                    count++;
                } else {
                    // System.out.println("line missing " + (i + 1));
                    b2[i] = name2 + "$" + String.format("%06d", i + 1) + "|0";

                }
            } else {
                b2[i] = name2 + "$" + String.format("%06d", i + 1) + "|0";;
            }

        }

        int foreignIndex;
        String matching;
        String tmp;
        File fw1 = new File(outPath + "/" + f1.getName());
        FileWriter fww1 = new FileWriter(fw1);
        BufferedWriter wr1 = new BufferedWriter(fww1);

        if (!block1.equals(block2)) {
            for (String item : b1) {
                //if (!checkOnly1.containsKey(i)) {
                matching = item.substring(item.indexOf("|") + 1, item.length());
                //System.out.println(matching);
                if (!matching.equals("0")) {
                    //System.out.println(b1[i]);
                    tmp = item.substring(0, item.indexOf("|"));
                    // System.out.println(tmp);
                    foreignIndex = Integer.valueOf(matching.substring(matching.indexOf("$") + 1, matching.length())) - 1;

                    // System.out.println(b2[foreignIndex]);
                    //System.out.println(foreignIndex+" "+tmp+"      "+(b2[foreignIndex].substring(b2[foreignIndex].indexOf("|")+1,b2[foreignIndex].length())));
                    if (!(b2[foreignIndex].substring(b2[foreignIndex].indexOf("|") + 1, b2[foreignIndex].length())).equals(tmp)) {
                        //System.out.println(b1[i] + "  " + foreignIndex + "    " + (b2[foreignIndex].substring(b2[foreignIndex].indexOf("|") + 1)));
                        //b2[foreignIndex] = b2[foreignIndex].substring(0, b2[foreignIndex].indexOf("|")) + "|" + "0";
                        item = tmp + "|" + "0";

                    }
                }
                // }
                //System.out.println(i+" "+b1[i]);

                wr1.write(item);
                wr1.newLine();
            }
            wr1.close();
            File fw2 = new File(outPath + "/" + f2.getName());
            FileWriter fww2 = new FileWriter(fw2);
            BufferedWriter wr2 = new BufferedWriter(fww2);
            for (int i = 0; i < size2; i++) {
                // System.out.println(b2[i]);
                if (!checkOnly2.containsKey(i)) {
                    matching = b2[i].substring(b2[i].indexOf("|") + 1, b2[i].length());
                    if (!matching.equals("0")) {
                        tmp = b2[i].substring(0, b2[i].indexOf("|"));
                        foreignIndex = Integer.valueOf(matching.substring(matching.indexOf("$") + 1, matching.length())) - 1;
                        //System.out.println(foreignIndex+" "+tmp);
                        //System.out.println(b1[foreignIndex]);
                        if (!(b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length())).equals(tmp)) {

                            //b1[foreignIndex] = b1[foreignIndex].substring(0, b1[foreignIndex].indexOf("|")) + "|" + "0";
                            b2[i] = tmp + "|" + "0";

                        }
                    }
                }

                wr2.write(b2[i]);
                wr2.newLine();
            }
            wr2.close();
        } else {
            System.out.println("identical");
            for (int i = 0; i < size1; i++) {
                //System.out.println(i+" "+b1[i]);
                matching = b1[i].substring(b1[i].indexOf("|") + 1, b1[i].length());
                //System.out.println(matching);
                if (!matching.equals("0")) {
                    //System.out.println(b1[i]);
                    tmp = b1[i].substring(0, b1[i].indexOf("|"));
                    // System.out.println(tmp);
                    foreignIndex = Integer.valueOf(matching.substring(matching.indexOf("$") + 1, matching.length())) - 1;

                    // System.out.println(b2[foreignIndex]);
                    //System.out.println(foreignIndex+" "+tmp+"      "+(b2[foreignIndex].substring(b2[foreignIndex].indexOf("|")+1,b2[foreignIndex].length())));
                    if (!(b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length())).equals(tmp)) {
                        //System.out.println(b1[i] + "  " + foreignIndex + "  " + (b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length())));// + "  " + foreignIndex + "    " + (b1[foreignIndex].substring(0, b1[foreignIndex].indexOf("|"))));
                        //System.out.println((b1[foreignIndex].substring(b1[foreignIndex].indexOf("|") + 1, b1[foreignIndex].length()))+"  "+tmp);
                        //b1[foreignIndex] = b1[foreignIndex].substring(0, b1[foreignIndex].indexOf("|")) + "|" + "0";
                        b1[i] = tmp + "|" + "0";

                    }
                }
            }
        }
//        

    }

    public static void BBHfinalReconstructor(String inPath, String outPath, LinkedHashMap<String, Integer> orderedSizes, ArrayList<String> order) throws IOException {
        //mainPath = "/home/thanos/firstCyanAllvsAll/filtered/";
        //String[] tmp;
        int size = orderedSizes.size();
        File fwr = new File(outPath);
        FileWriter fw = new FileWriter(fwr);
        BufferedWriter wr = new BufferedWriter(fw);
        for (int j = 0; j < size; j++) {
            File[] f = new File[size];
            FileReader[] fr = new FileReader[size];
            BufferedReader[] r = new BufferedReader[size];
            for (int k = 0; k < size; k++) {
                f[k] = new File(inPath + (String.valueOf(j) + "_" + String.valueOf(k)));
                fr[k] = new FileReader(f[k]);
                r[k] = new BufferedReader(fr[k]);
            }
            String line;
            // line=r[0].readLine();
            //wr.write(line.substring(0,line.indexOf("|")));
            //r[0].close();
            //r[0]=new BufferedReader(fr[0]);
            int size1 = orderedSizes.get(order.get(Integer.valueOf(j)));
            System.out.println(size1 + " " + j);
            for (int l = 0; l < size1; l++) {
                //System.out.println(l);
                for (int k = 0; k < size; k++) {
                    if (k == 0) {
                        line = r[k].readLine();
                        //tmp=line.split("|");
                        //System.out.println(line);
                        //wr.write(line.substring(0,line.indexOf("|")));
                        if (!line.substring(line.indexOf("|") + 1, line.length()).equals("0")) {
                            wr.write("1 ");
                        } else {
                            wr.write("0 ");
                        }
                    } else {
                        line = r[k].readLine();
                        //tmp=line.split("|");                        
                        if (!line.substring(line.indexOf("|") + 1, line.length()).equals("0")) {
                            wr.write("1 ");
                        } else {
                            wr.write("0 ");
                        }
                    }
                }
                wr.newLine();
            }

        }
        wr.close();

    }

    public static void profileBuilder(String profileMode, String mainPath, String outPath, String finalOutPath, String treeOrder, String list) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(treeOrder), StandardCharsets.UTF_8);
        ArrayList<String> order = new ArrayList<String>();
        Map<String, Integer> organisms = new HashMap<String, Integer>();
        int count = 0;
        for (int i = 0; i < lines.size(); i++) {
            String tmp = lines.get(i).substring(0, lines.get(i).indexOf(":"));
            //System.out.println(tmp);
            organisms.put(tmp, i);
            order.add(tmp);
            // count++;
        }
        List<String> pthLines = Files.readAllLines(Paths.get(list), StandardCharsets.UTF_8);

        LinkedHashMap<String, Integer> orderedSizes = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < pthLines.size(); i++) {
            // String[] tmp = pthLines.get(i).split(";");
            //orderedSizes.put(tmp[0], Integer.valueOf(tmp[1]));
            orderedSizes.put(pthLines.get(i).substring(0, pthLines.get(i).indexOf(";")), Integer.valueOf(pthLines.get(i).substring(pthLines.get(i).indexOf(";") + 1, pthLines.get(i).length())));
        }
        if (profileMode.equals("1")) {
            File f1 = new File(mainPath);
            File[] af1 = f1.listFiles();
            for (int i = 0; i < af1.length; i++) {
                System.out.println(af1[i].getAbsolutePath());
                System.out.println(orderedSizes.get(af1[i].getName()));
                System.out.println(orderedSizes.size());
                merger(af1[i].getAbsolutePath(), finalOutPath+"/"+i, orderedSizes.get(af1[i].getName()), orderedSizes.size());
            }

        } else if (profileMode.equals("2")) {
            Map<String, String> combinations = new HashMap<String, String>();
            for (int j = 0; j < orderedSizes.size(); j++) {
                for (int k = 0; k < orderedSizes.size(); k++) {
                    String ind1 = String.valueOf(j);
                    String ind2 = String.valueOf(k);
                    if (!combinations.containsValue(ind2 + "_" + ind1)) {
                        combinations.put(ind1 + "_" + ind2, ind2 + "_" + ind1);
                    }
                    //System.out.println(String.valueOf(j) + "_" + String.valueOf(k));
                }
            }
            //String currentPath;
            // String[] tmp;
            for (Map.Entry<String, String> entry1 : combinations.entrySet()) {
                String key1 = entry1.getKey();
                String value1 = entry1.getValue();

                int size1 = orderedSizes.get(order.get(Integer.valueOf(key1.substring(0, key1.indexOf("_")))));
                int size2 = orderedSizes.get(order.get(Integer.valueOf(value1.substring(0, value1.indexOf("_")))));
                //System.out.println(key1+" "+size1+"      "+value1+" "+size2);
                blockMatcher(mainPath + key1, mainPath + value1, outPath, size1, size2);

            }
            BBHfinalReconstructor(outPath, finalOutPath + "final.ARRAY", orderedSizes, order);

        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        Socket client = new Socket("localhost", 56789);
//        OutputStream outToServer = client.getOutputStream();
//        DataOutputStream out = new DataOutputStream(outToServer);
//        for (int i = 0; i < 10000; i++) {
//            sleep(100);
//            System.out.println(i);
//            out.writeUTF(String.valueOf(i));
//        }

        String profileMode = "1";
        String mainPath = "/home/thanos/merged";
        String outPath = "/home/thanos/fout1/";
        String finalOutPath = "/home/thanos/fout2/";
        String treeOrder = "/home/thanos/Dropbox/NetBeansProjects/genome-profiling/WorkFlow/TreeOrder.txt";
        String list = "/home/thanos/Dropbox/NetBeansProjects/genome-profiling/WorkFlow/95FORMERGE.csv";

        //orderedSizes.
        long startTime = System.currentTimeMillis();
        profileBuilder(profileMode, mainPath, outPath, finalOutPath, treeOrder, list);
//        for (int i = 0; i < 1000; i++) {
//            fastBlockMatcher("/home/thanos/cyan/1/0_1", "/home/thanos/cyan/1/1_0", "/home/thanos/cyan/1/out/", 8172, 5797);
//        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println(duration);

    }

}



 /* Experimental code
 public static void buildProfile(String input, String outputFolder, Map<String, Integer> organisms) throws IOException {

 //File directory = new File(outputFolder);
 List<String> lines = Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8);
 String currentDir = lines.get(0).substring(0, lines.get(0).indexOf("$"));
 String currentFile = lines.get(0).substring(lines.get(0).indexOf("$") + 1, lines.get(0).indexOf("\t"));
 String wholePath = outputFolder + "/" + currentDir + "/" + currentFile;

 File path = new File(outputFolder + "/" + currentDir + "/");
 path.mkdirs();
 File current = new File(wholePath);//.mkdirs();

 //current.createNewFile();
 FileWriter w1 = new FileWriter(current);
 BufferedWriter wr1 = new BufferedWriter(w1);

 double[] matches = new double[organisms.size()];
 for (int j = 0; j < organisms.size(); j++) {
 matches[j] = 0;
 }

 for (int i = 0; i < lines.size(); i++) {

 if (!currentFile.equals(lines.get(i).substring(lines.get(i).indexOf("$") + 1, lines.get(i).indexOf("\t")))) {
                
 for (int j = 0; j < organisms.size(); j++) {
 wr1.write(String.valueOf(matches[j]+" "));
 matches[j] = 0;
 }
 wr1.close();
 currentDir = lines.get(i).substring(0, lines.get(i).indexOf("$"));
 currentFile = lines.get(i).substring(lines.get(i).indexOf("$") + 1, lines.get(i).indexOf("\t"));
 wholePath = outputFolder + "/" + currentDir + "/" + currentFile;

 path = new File(outputFolder + "/" + currentDir + "/");
 path.mkdirs();
 current = new File(wholePath);//.mkdirs();

 //current.createNewFile();
 w1 = new FileWriter(current);
 wr1 = new BufferedWriter(w1);

 }
 String[] tmp = lines.get(i).split("\t");
 tmp[1] = tmp[1].substring(0, 17);
 //System.out.println(tmp[1]);
 int index = organisms.get(tmp[1]);
 //System.out.println(index);
 matches[index] = matches[index] + 1;
 if(matches[index]>1)
 {
 System.out.println(matches[index]);
 }
            
 //System.out.println(currentFile);

 }
 wr1.close();
 //System.out.println(matches.toString());
 }
 */
