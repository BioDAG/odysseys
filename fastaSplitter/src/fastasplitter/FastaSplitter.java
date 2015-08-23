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
package fastasplitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FastaSplitter {

    /**
     * @param args the command line arguments
     */
    public static void fastaSplitter(String filePath, String outPath, int splitSize) throws IOException {

        //List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        BufferedReader r1 = new BufferedReader(new FileReader(new File(filePath)));
        int count = 1;
        int rsplit = splitSize;
        int logic = 0;

        BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File(outPath + "chunk_1.fa")));
        String line1;
        while ((line1 = r1.readLine()) != null) {
            if (line1.contains(">")) {
                //System.out.println("contains");
                if (logic == 1) {
                    String fileOutPath = outPath + "chunk_" + Integer.toString(count) + ".fa";
                    wr1.close();
                    wr1 = new BufferedWriter(new FileWriter(new File(fileOutPath)));
                    logic = 0;

                }
                wr1.write(line1);
                wr1.newLine();
                rsplit = rsplit - 1;
                if (rsplit == 0) {
                    rsplit = splitSize;
                    count++;
                    logic = 1;
                }
            } else {
                wr1.write(line1);
                wr1.newLine();
            }

        }
        //for (int i = 0; i < lines.size(); i++) {
            

        //}
        wr1.close();

    }
    /* Old function, keep for testing
    public static void fastaSplitter(String filePath, String outPath, int splitSize) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        BufferedReader r1 = new BufferedReader(new FileReader(new File(filePath)));
        int count = 1;
        int rsplit = splitSize;
        int logic = 0;

        BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File(outPath + "chunk_1.fa")));
        String line1;
        while ((line1 = r1.readLine()) != null) {

        }
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(">")) {
                //System.out.println("contains");
                if (logic == 1) {
                    String fileOutPath = outPath + "chunk_" + Integer.toString(count) + ".fa";
                    wr1.close();
                    wr1 = new BufferedWriter(new FileWriter(new File(fileOutPath)));
                    logic = 0;

                }
                wr1.write(lines.get(i));
                wr1.newLine();
                rsplit = rsplit - 1;
                if (rsplit == 0) {
                    rsplit = splitSize;
                    count++;
                    logic = 1;
                }
            } else {
                wr1.write(lines.get(i));
                wr1.newLine();
            }

        }
        wr1.close();

    }
    */

    public static void main(String[] args) throws IOException {
        String filepath = args[0];
        String outpath = args[1];
        String size = args[2];
        fastaSplitter(filepath, outpath, Integer.valueOf(size));
    }

}
