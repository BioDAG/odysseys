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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;


public class WorkFlow {

    public static void executeBashScript(String[] cmd) {

        StringBuffer output = new StringBuffer();

        Process p;
        //ProcessBuilder pb;
        try {
            //p = Runtime.getRuntime().exec(path + " " + mainPath + " 1>/dev/null");
            System.out.println("execturing " + cmd);

            //p = Runtime.getRuntime().exec(new String[]{"java","-jar", "/home/thanos/Mon-Apr-06-213156-EEST-2015/code/localDistributer.jar","1 6","2","3","4"});//+ " 1>/dev/null");
            p = Runtime.getRuntime().exec(cmd);
// pb = new ProcessBuilder("/bin/bash","/home/thanos/Fotis/plantsProject/cdHIT_makeBlastDB.sh /home/thanos/Fotis/plantsProject/ 1>/dev/null 2>/dev/null");
            //p = Runtime.getRuntime().exec("/home/thanos/Fotis/plantsProject/cdHIT_makeBlastDB.sh /home/thanos/Fotis/plantsProject/ 1>/dev/null 2>/dev/null");
            //p.waitFor();
            //Process p=pb.start();

            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream())); //p.getInputStream()

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            p.destroy();
            //System.out.println(output.toString());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception " + e);
        }
        System.out.println("before");
        System.out.println(output.toString());
        //return output.toString();
        // return "nothing";

    }

    public static void fastaSplitter(String filePath, String outPath, int splitSize) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        int count = 1;
        int rsplit = splitSize;
        int logic = 0;

        BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File(outPath + "chunk_1.fa")));;
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

    public static void initializeNode(Node node, String workingDir, SSHExec ssh) throws Exception {
        String rootPath = "/home/" + node.username + "/" + workingDir + "/";
        //override
        //rootPath="/home/" + node.username + "/" + "Thu-Apr-09-002155-EEST-2015" + "/";
        // command = "bash;" + rootPath + "code/compileTools.sh;" + rootPath + "code/cd-hit/";
        System.out.println("--------------------------------Initializing node " + node.name);
        // cb = new ConnBean(node.ip, node.username, node.password);
        //SSHExec ssh = SSHExec.getInstance(cb);
        // SSHExec.
        ssh.connect();

        CustomTask sampleTask;

        //sampleTask = new ExecCommand("rm -rf ~/*_experimentTBD");
        sampleTask = new ExecCommand("rm -rf ~/*experiment*");
        ssh.exec(sampleTask);

        ssh.uploadAllDataToServer("/home/thanos/Dropbox/NetBeansProjects/genome-profiling/WorkFlow/initCode", rootPath);
        ssh.uploadSingleDataToServer("./list.csv", rootPath);
        ssh.uploadSingleDataToServer("./" + node.id + ".organisms", rootPath);
        ssh.uploadSingleDataToServer("./TreeOrder.txt", rootPath);
        ssh.uploadSingleDataToServer("/home/thanos/.kamakirc", ("/home/" + node.username + "/"));

        sampleTask = new ExecCommand("cd " + rootPath, "mkdir remote");//,"sshfs thanos@"+node.masterAddress+":/home/thanos/Master/"+node.id+" "+rootPath+"remote/ -o nonempty");
        ssh.exec(sampleTask);
        if (node.ismaster == true) {
            sampleTask = new ExecCommand("cd " + rootPath, "mkdir master");//,"sshfs thanos@"+node.masterAddress+":/home/thanos/Master/"+node.id+" "+rootPath+"remote/ -o nonempty");
            ssh.exec(sampleTask);
        }

        sampleTask = new ExecCommand("rm -f ~/load.log", "rm -f ~/log", "rm -f ~/mem.log", "screen -S clientDaemon -d -m java -jar " + rootPath + "initCode/Client.jar " + node.masterAddress + " " + node.localJobPort + " " + node.remoteJobPort, "screen -S loadLoging -d -m bash " + rootPath + "initCode/loadLogScript.sh", "screen -S memLoging -d -m bash " + rootPath + "initCode/memLogScript.sh");
        ssh.exec(sampleTask);

        ssh.disconnect();

        System.out.println("-------------------------------------------Node " + node.name + " initialized");
    }

    public static void fileSizeClusterer(String filesList, ArrayList<Node> nodes) throws IOException {
        // LinkedHashMap nodeResources
        LinkedHashMap<String, Double> files1 = new LinkedHashMap<String, Double>();
        LinkedHashMap<String, Double> files2 = new LinkedHashMap<String, Double>();
        //LinkedHashMap<String, Double> files2 = new LinkedHashMap<String, Double>();
        //List<String> cy = Files.readAllLines(Paths.get("./someCyan"), StandardCharsets.UTF_8);

        //System.out.println(cy.get(0));
        File fileList = new File(filesList);
        List<String> lines = Files.readAllLines(Paths.get(fileList.getAbsolutePath()), StandardCharsets.UTF_8);
        int totalCount = 0;
        double sumSize1 = 0;
        double largestValue = 0;
        // double sumSize2 = 0;
        //double sumSize2 = 0;
        for (int i = 0; i < lines.size(); i++) {
            String[] temp = lines.get(i).split("MiB ");
            //System.out.println(temp[0]);
            //System.out.println(temp[1]);
            if (files1.containsKey(temp[1])) {
                //System.out.println(temp[1]);
            }
            // int flag = 0;
//            for (int j = 0; j < cy.size(); j++) {
//                if (temp[1].contains(cy.get(j))) {
//                    System.out.println("1 " + temp[1]);
//                    System.out.println("2 " + cy.get(j));
//                    System.out.println();
//                    flag = 1;
//                    break;
//                }
//            }
            // if (flag == 1) {
            //    files2.put(temp[1], Double.valueOf(temp[0]));
            //    sumSize2 = sumSize2 + Double.valueOf(temp[0]);
            // } else {
            if (i == 0) {
                largestValue = Double.valueOf(temp[0]);
            }
            files1.put(temp[1], Double.valueOf(temp[0]));
            sumSize1 = sumSize1 + Double.valueOf(temp[0]);
            //}

            // if (cy.get(0).contains(temp[1])) {
            //    files.put(temp[1], Double.valueOf(temp[0]));
            //    System.out.println(temp[1]);
            //sumSize = sumSize + Double.valueOf(temp[0]);
            // } else {
            //}
        }
        double sumPower = 0;
        for (int i = 0; i < nodes.size(); i++) {
            sumPower = sumPower + nodes.get(i).cpuPower;
        }

        // double testsumPow
        double oversizeStore = 0;

        System.out.println("Sum size " + sumSize1 + " number " + files1.size());
        HashMap<Double, Double> keepOversize = new HashMap<Double, Double>();
        double minError = 1000000.0;
        int resolution = 10000;
        for (int s = 0; s < resolution; s++) {
            LinkedHashMap<String, Double> testfiles = new LinkedHashMap<String, Double>();
            testfiles.putAll(files1);
            double maxError = 0;
            int tot = 0;
            //oversizeStore = sumSize1 * (double) s / resolution;
            oversizeStore = largestValue * (double) s / resolution;
            if (oversizeStore > largestValue) {//(3*(nodes.get(0).cpuPower / sumPower) * sumSize1) / 1.0) {
                break;
            }
            //System.out.println("oversize " + oversizeStore);
            for (int i = 0; i < nodes.size(); i++) {
                //System.out.println();
                //BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms")));
                double size = (nodes.get(i).cpuPower / sumPower) * sumSize1;
                //System.out.println("size"+size);
                double total = 0.0;
                //double oversize = 0.6;
                double oversize = oversizeStore;
                //oversize=65735.0991;
                if (i == nodes.size() - 1) {
                    oversize = 30000000.0;
                }
                int flag = 0;
                String keepKey = "";
                int count = 0;

                for (Map.Entry<String, Double> entry1 : testfiles.entrySet()) {
                    String key1 = entry1.getKey();
                    //System.out.println(files.get(key1));
                    if (testfiles.get(key1) > 0.0) {
                        if (testfiles.get(key1) < size && flag == 0) {
                            size = size - testfiles.get(key1);
                            total = total + testfiles.get(key1);
                            //wr1.write(key1);
                            // wr1.newLine();
                            testfiles.put(key1, -100.0);
                            //files.remove(key1);
                            count++;
                        } else {
                            flag = 1;
                            if (oversize > Math.abs(size - testfiles.get(key1))) {
                                oversize = Math.abs(size - testfiles.get(key1));
                                keepKey = key1;
                            }

                        }
                    }

                    //System.out.println(key1 + files.get(key1));
                }
                if (!keepKey.equals("")) {
                    // wr1.write(keepKey);
                    //  wr1.newLine();
                    total = total + testfiles.get(keepKey);
                    testfiles.put(keepKey, -100.0);
                    count++;
                }
                //wr1.write(String.valueOf(total));
                // wr1.close();
                totalCount = totalCount + count;
                //System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize1));
                tot = tot + count;
                if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower)) {
                    maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower);
                }

            }
            //System.out.println("avgError is " + maxError);
            //System.out.println(tot);
            if (minError > maxError && tot == testfiles.size()) {
                minError = maxError;
                keepOversize.put(minError, oversizeStore);
            }

        }

        //oversizeStore=340000;
        oversizeStore = keepOversize.get(minError);
        double maxError = 0;
        for (int i = 0; i < nodes.size(); i++) {
            //System.out.println();
            BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms")));
            double size = (nodes.get(i).cpuPower / sumPower) * sumSize1;
            //System.out.println("size"+size);
            double total = 0.0;
            //double oversize = 0.6;
            double oversize = oversizeStore;
            //oversize=65735.0991;
            if (i == nodes.size() - 1) {
                oversize = 30000000.0;
            }
            int flag = 0;
            String keepKey = "";
            int count = 0;

            for (Map.Entry<String, Double> entry1 : files1.entrySet()) {
                String key1 = entry1.getKey();
                //System.out.println(files.get(key1));
                if (files1.get(key1) > 0.0) {
                    if (files1.get(key1) < size && flag == 0) {
                        size = size - files1.get(key1);
                        total = total + files1.get(key1);
                        wr1.write(key1);
                        wr1.newLine();
                        files1.put(key1, -100.0);
                        //files.remove(key1);
                        count++;
                    } else {
                        flag = 1;
                        if (oversize > Math.abs(size - files1.get(key1))) {
                            oversize = Math.abs(size - files1.get(key1));
                            keepKey = key1;
                        }

                    }
                }

                //System.out.println(key1 + files.get(key1));
            }
            if (!keepKey.equals("")) {
                wr1.write(keepKey);
                wr1.newLine();
                total = total + files1.get(keepKey);
                files1.put(keepKey, -100.0);
                count++;
            }
            //wr1.write(String.valueOf(total));
            wr1.close();
            totalCount = totalCount + count;
            System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize1));
            if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower)) {
                maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower);
            }

        }
        if (minError != maxError) {
            System.out.println("####FATAL ERROR something went wrong, minError found is not same with minError in set written");
        }
        System.out.println("CrossCheck minError " + minError + " =  " + maxError);
        System.out.println();
        System.out.println("caynobacteria, number " + files2.size());
        System.out.println();

        /*
         for (int i = 0; i < nodes.size(); i++) {
         System.out.println();
         BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms"), true));
         double size = (nodes.get(i).cpuPower / sumPower) * sumSize2;
         //System.out.println("size"+size);
         double total = 0.0;
         double oversize = 0.6;
         if (i == nodes.size() - 1) {
         oversize = 10000000;
         }
         int flag = 0;
         String keepKey = "";
         int count = 0;

         for (Map.Entry<String, Double> entry1 : files2.entrySet()) {
         String key1 = entry1.getKey();
         //System.out.println(files.get(key1));
         if (files2.get(key1) > 0.0) {
         if (files2.get(key1) < size && flag == 0) {
         size = size - files2.get(key1);
         total = total + files2.get(key1);
         wr1.write(key1);
         wr1.newLine();
         files2.put(key1, -100.0);
         //files.remove(key1);
         count++;
         } else {
         flag = 1;
         if (oversize > Math.abs(size - files2.get(key1))) {
         oversize = Math.abs(size - files2.get(key1));
         keepKey = key1;
         }

         }
         }

         //System.out.println(key1 + files.get(key1));
         }
         if (!keepKey.equals("")) {
         wr1.write(keepKey);
         wr1.newLine();
         total = total + files2.get(keepKey);
         files2.put(keepKey, -100.0);
         count++;
         }
         //wr1.write(String.valueOf(total));
         wr1.close();
         totalCount = totalCount + count;
         System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize2));
         }
         System.out.println("total organisms added " + totalCount);
         */
        String chkpath = "./";
        HashMap<String, Integer> chkDoubles = new HashMap<String, Integer>(100);
        int recount = 0;
        for (int i = 0; i < nodes.size(); i++) {
            String f = nodes.get(i).id + ".organisms";
            List<String> f1 = Files.readAllLines(Paths.get(f), StandardCharsets.UTF_8);
            for (int j = 0; j < f1.size(); j++) {
                if (chkDoubles.containsKey(f1.get(j))) {
                    System.out.println("FATAL ERROR, duplicate found @ " + f1.get(j));
                } else {
                    //System.out.println(f1.get(j));
                    recount++;
                    chkDoubles.put(f1.get(j), 1);
                }
            }
        }
        System.out.println("Recount is " + recount);

    }

    public static void fileSizeClusterer2(String filesList, ArrayList<Node> nodes) throws IOException {
        // LinkedHashMap nodeResources
        LinkedHashMap<String, Double> files1 = new LinkedHashMap<String, Double>();
        LinkedHashMap<String, Double> files2 = new LinkedHashMap<String, Double>();
        LinkedHashMap<String, Double> filesKeepCopy = new LinkedHashMap<String, Double>();
        //LinkedHashMap<String, Double> files2 = new LinkedHashMap<String, Double>();
        List<String> cy = Files.readAllLines(Paths.get("./someCyan"), StandardCharsets.UTF_8);

        //System.out.println(cy.get(0));
        File fileList = new File(filesList);
        List<String> lines = Files.readAllLines(Paths.get(fileList.getAbsolutePath()), StandardCharsets.UTF_8);
        int totalCount = 0;
        double sumSize1 = 0;
        double largestValue = 0;
        // double sumSize2 = 0;
        double sumSize2 = 0;
        for (int i = 0; i < lines.size(); i++) {
            String[] temp = lines.get(i).split("MiB ");
            //System.out.println(temp[0]);
            //System.out.println(temp[1]);
            if (files1.containsKey(temp[1])) {
                //System.out.println(temp[1]);
            }
            int flag = 0;
            if (i == 0) {
                largestValue = Double.valueOf(temp[0]);
            }
            for (int j = 0; j < cy.size(); j++) {
                if (temp[1].contains(cy.get(j))) {
                    //System.out.println("1 " + temp[1]);
                    //System.out.println("2 " + cy.get(j));
                    //System.out.println();
                    flag = 1;
                    break;
                }
            }
            if (flag == 1) {
                //System.out.println(temp[1]);
                files2.put(temp[1], Double.valueOf(temp[0]));
                sumSize2 = sumSize2 + Double.valueOf(temp[0]);
            } else {

                files1.put(temp[1], Double.valueOf(temp[0]));
                sumSize1 = sumSize1 + Double.valueOf(temp[0]);
            }

            // if (cy.get(0).contains(temp[1])) {
            //    files.put(temp[1], Double.valueOf(temp[0]));
            //    System.out.println(temp[1]);
            //sumSize = sumSize + Double.valueOf(temp[0]);
            // } else {
            //}
        }
        double sumPower = 0;
        for (int i = 0; i < nodes.size(); i++) {
            sumPower = sumPower + nodes.get(i).cpuPower;
        }

        // double testsumPow
        double oversizeStore = 0;

        System.out.println("Sum size " + sumSize1 + " number " + files1.size());
        HashMap<Double, Double> keepOversize = new HashMap<Double, Double>();
        double minError = 1000000.0;
        int resolution = 10000;
        for (int s = 0; s < resolution; s++) {
            LinkedHashMap<String, Double> testfiles = new LinkedHashMap<String, Double>();
            testfiles.putAll(files1);
            double maxError = 0;
            int tot = 0;
            //oversizeStore = sumSize1 * (double) s / resolution;
            oversizeStore = largestValue * (double) s / resolution;
            if (oversizeStore > largestValue) {//(3*(nodes.get(0).cpuPower / sumPower) * sumSize1) / 1.0) {
                break;
            }
            //System.out.println("oversize " + oversizeStore);
            for (int i = 0; i < nodes.size(); i++) {
                //System.out.println();
                //BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms")));
                double size = (nodes.get(i).cpuPower / sumPower) * sumSize1;
                //System.out.println("size"+size);
                double total = 0.0;
                //double oversize = 0.6;
                double oversize = oversizeStore;
                //oversize=65735.0991;
                if (i == nodes.size() - 1) {
                    oversize = 30000000.0;
                }
                int flag = 0;
                String keepKey = "";
                int count = 0;

                for (Map.Entry<String, Double> entry1 : testfiles.entrySet()) {
                    String key1 = entry1.getKey();
                    //System.out.println(files.get(key1));
                    if (testfiles.get(key1) > 0.0) {
                        if (testfiles.get(key1) < size && flag == 0) {
                            size = size - testfiles.get(key1);
                            total = total + testfiles.get(key1);
                            //wr1.write(key1);
                            // wr1.newLine();
                            testfiles.put(key1, -100.0);
                            //files.remove(key1);
                            count++;
                        } else {
                            flag = 1;
                            if (oversize > Math.abs(size - testfiles.get(key1))) {
                                oversize = Math.abs(size - testfiles.get(key1));
                                keepKey = key1;
                            }

                        }
                    }

                    //System.out.println(key1 + files.get(key1));
                }
                if (!keepKey.equals("")) {
                    // wr1.write(keepKey);
                    //  wr1.newLine();
                    total = total + testfiles.get(keepKey);
                    testfiles.put(keepKey, -100.0);
                    count++;
                }
                //wr1.write(String.valueOf(total));
                // wr1.close();
                totalCount = totalCount + count;
                //System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize1));
                tot = tot + count;
                if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower)) {
                    maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower);
                }

            }
            //System.out.println("avgError is " + maxError);
            //System.out.println(tot);
            if (minError > maxError && tot == testfiles.size()) {
                minError = maxError;
                keepOversize.put(minError, oversizeStore);
            }

        }

        //oversizeStore=340000;
        filesKeepCopy.putAll(files1);
        oversizeStore = keepOversize.get(minError);
        double maxError = 0;
        for (int i = 0; i < nodes.size(); i++) {
            //System.out.println();
            BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms")));
            double size = (nodes.get(i).cpuPower / sumPower) * sumSize1;
            //System.out.println("size"+size);
            double total = 0.0;
            //double oversize = 0.6;
            double oversize = oversizeStore;
            //oversize=65735.0991;
            if (i == nodes.size() - 1) {
                oversize = 30000000.0;
            }
            int flag = 0;
            String keepKey = "";
            int count = 0;

            for (Map.Entry<String, Double> entry1 : files1.entrySet()) {
                String key1 = entry1.getKey();
                //System.out.println(files.get(key1));
                if (files1.get(key1) > 0.0) {
                    if (files1.get(key1) < size && flag == 0) {
                        size = size - files1.get(key1);
                        total = total + files1.get(key1);
                        wr1.write(key1);
                        wr1.newLine();
                        files1.put(key1, -100.0);
                        //files.remove(key1);
                        count++;
                    } else {
                        flag = 1;
                        if (oversize > Math.abs(size - files1.get(key1))) {
                            oversize = Math.abs(size - files1.get(key1));
                            keepKey = key1;
                        }

                    }
                }

                //System.out.println(key1 + files.get(key1));
            }
            if (!keepKey.equals("")) {
                wr1.write(keepKey);
                wr1.newLine();
                total = total + files1.get(keepKey);
                files1.put(keepKey, -100.0);
                count++;
            }
            //wr1.write(String.valueOf(total));
            wr1.close();
            totalCount = totalCount + count;
            System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize1));
            if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower)) {
                maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower);
            }

        }
        if (minError != maxError) {
            System.out.println("####FATAL ERROR something went wrong, minError found is not same with minError in set written");
        }
        System.out.println("CrossCheck minError " + minError + " =  " + maxError);
        System.out.println();
        System.out.println("caynobacteria, number " + files2.size());
        System.out.println();

        //CHANGE IT HERE
        //files1 = files2;
        files1.clear();
        files1.putAll(files2);
        sumSize1 = sumSize2;

        System.out.println("Sum size " + sumSize1 + " number " + files1.size());
        //HashMap<Double, Double> keepOversize = new HashMap<Double, Double>();
        minError = 1000000.0;
        //resolution = resolution;
        for (int s = 0; s < resolution; s++) {
            LinkedHashMap<String, Double> testfiles = new LinkedHashMap<String, Double>();
            testfiles.putAll(files1);
            maxError = 0;
            int tot = 0;
            //oversizeStore = sumSize1 * (double) s / resolution;
            oversizeStore = largestValue * (double) s / resolution;
            if (oversizeStore > largestValue) {//(3*(nodes.get(0).cpuPower / sumPower) * sumSize1) / 1.0) {
                break;
            }
            //System.out.println("oversize " + oversizeStore);
            for (int i = 0; i < nodes.size(); i++) {
                //System.out.println();
                //BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms")));
                double size = (nodes.get(i).cpuPower / sumPower) * sumSize1;
                //System.out.println("size"+size);
                double total = 0.0;
                //double oversize = 0.6;
                double oversize = oversizeStore;
                //oversize=65735.0991;
                if (i == nodes.size() - 1) {
                    oversize = 30000000.0;
                }
                int flag = 0;
                String keepKey = "";
                int count = 0;

                for (Map.Entry<String, Double> entry1 : testfiles.entrySet()) {
                    String key1 = entry1.getKey();
                    //System.out.println(files.get(key1));
                    if (testfiles.get(key1) > 0.0) {
                        if (testfiles.get(key1) < size && flag == 0) {
                            size = size - testfiles.get(key1);
                            total = total + testfiles.get(key1);
                            //wr1.write(key1);
                            // wr1.newLine();
                            testfiles.put(key1, -100.0);
                            //files.remove(key1);
                            count++;
                        } else {
                            flag = 1;
                            if (oversize > Math.abs(size - testfiles.get(key1))) {
                                oversize = Math.abs(size - testfiles.get(key1));
                                keepKey = key1;
                            }

                        }
                    }

                    //System.out.println(key1 + files.get(key1));
                }
                if (!keepKey.equals("")) {
                    // wr1.write(keepKey);
                    //  wr1.newLine();
                    total = total + testfiles.get(keepKey);
                    testfiles.put(keepKey, -100.0);
                    count++;
                }
                //wr1.write(String.valueOf(total));
                // wr1.close();
                totalCount = totalCount + count;
                //System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize1));
                tot = tot + count;
                if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower)) {
                    maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower);
                }

            }
            //System.out.println("avgError is " + maxError);
            //System.out.println(tot);
            if (minError > maxError && tot == testfiles.size()) {
                minError = maxError;
                keepOversize.put(minError, oversizeStore);
            }

        }

        //oversizeStore=340000;
        oversizeStore = keepOversize.get(minError);
        maxError = 0;
        for (int i = 0; i < nodes.size(); i++) {
            //System.out.println();
            BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File("./" + nodes.get(i).id + ".organisms"), true));
            double size = (nodes.get(i).cpuPower / sumPower) * sumSize1;
            //System.out.println("size"+size);
            double total = 0.0;
            //double oversize = 0.6;
            double oversize = oversizeStore;
            //oversize=65735.0991;
            if (i == nodes.size() - 1) {
                oversize = 30000000.0;
            }
            int flag = 0;
            String keepKey = "";
            int count = 0;

            for (Map.Entry<String, Double> entry1 : files1.entrySet()) {
                String key1 = entry1.getKey();
                //System.out.println(files.get(key1));
                if (files1.get(key1) > 0.0) {
                    if (files1.get(key1) < size && flag == 0) {
                        size = size - files1.get(key1);
                        total = total + files1.get(key1);
                        wr1.write(key1);
                        wr1.newLine();
                        files1.put(key1, -100.0);
                        //files.remove(key1);
                        count++;
                    } else {
                        flag = 1;
                        if (oversize > Math.abs(size - files1.get(key1))) {
                            oversize = Math.abs(size - files1.get(key1));
                            keepKey = key1;
                        }

                    }
                }

                //System.out.println(key1 + files.get(key1));
            }
            if (!keepKey.equals("")) {
                wr1.write(keepKey);
                wr1.newLine();
                total = total + files1.get(keepKey);
                files1.put(keepKey, -100.0);
                count++;
            }
            //wr1.write(String.valueOf(total));
            wr1.close();
            totalCount = totalCount + count;
            System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize1));
            if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower)) {
                maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize1))) / (nodes.get(i).cpuPower / sumPower);
            }

        }
        if (minError != maxError) {
            System.out.println("####FATAL ERROR something went wrong, minError found is not same with minError in set written");
        }
        System.out.println("CrossCheck minError " + minError + " =  " + maxError);
        System.out.println();
        System.out.println("caynobacteria, number " + files2.size());
        System.out.println();

        String chkpath = "./";
        HashMap<String, Integer> chkDoubles = new HashMap<String, Integer>(100);
        int recount = 0;
        for (int i = 0; i < nodes.size(); i++) {
            String f = nodes.get(i).id + ".organisms";
            List<String> f1 = Files.readAllLines(Paths.get(f), StandardCharsets.UTF_8);
            for (int j = 0; j < f1.size(); j++) {
                if (chkDoubles.containsKey(f1.get(j))) {
                    System.out.println("FATAL ERROR, duplicate found @ " + f1.get(j));
                } else {
                    //System.out.println(f1.get(j));
                    recount++;
                    chkDoubles.put(f1.get(j), 1);
                }
            }
        }
        System.out.println("Recount is " + recount);

    }

    public static void scheduler(ArrayList<Node> nodes, String chunkSize, String descriptionAddon) throws InterruptedException, IOException, Exception {

        //String descriptionAddon = "";
        //descriptionAddon = "COUNTDOWN_ALL_NewThreads_SchSize=100_experimentTBD";
        //String workingDir = (new Date()).toString() + "_" + descriptionAddon + "_experimentTBD";
        String workingDir = (new Date()).toString() + "_" + descriptionAddon;// + "_experimentTBD";
        workingDir = workingDir.replaceAll(" ", "-");
        workingDir = workingDir.replaceAll(":", "");
        ArrayList<NodeThread> NodeThreads = new ArrayList<NodeThread>();
        ConnBean cb = new ConnBean(nodes.get(0).ip, nodes.get(0).username, nodes.get(0).password);
        SSHExec ssh = SSHExec.getInstance(cb);
        for (int i = 0; i < nodes.size(); i++) {
            //ConnBean cb = new ConnBean(nodes.get(i).ip, nodes.get(i).username, nodes.get(i).password);
            if (i == 0) {
                if ((nodes.get(i).enabled.equals("enabled"))) {
                    cb.setHost(nodes.get(i).ip);
                    cb.setUser(nodes.get(i).username);
                    cb.setPassword(nodes.get(i).password);
                    initializeNode(nodes.get(i), workingDir, ssh);
                    NodeThread t = new NodeThread(nodes.get(i).id, nodes.get(i), workingDir, chunkSize);
                    TimeUnit.SECONDS.sleep(5);
                    NodeThreads.add(t);
                }
            }

            //t.start();
        }
        for (int i = 0; i < NodeThreads.size(); i++) {
            //if ((nodes.get(i).enabled.equals("enabled"))) {
            //if (i == 1) {
            NodeThreads.get(i).start();
            //}
            //}

        }
        for (int i = 0; i < NodeThreads.size(); i++) {
            //if ((nodes.get(i).enabled.equals("enabled"))) {
            // if (i == 1) {
            NodeThreads.get(i).join();
            //}
            //}
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException, Exception {

        String mainPath = "/home/thanos/Fotis/chlamydia/";
        String blastDbPath = "/home/thanos/Fotis/chlamydia/blastDB/ChlamydiaDB";
        String blastOutPath = "/home/thanos/Fotis/chlamydia/blastHumanVsChla/";
        String pathwaysPath = "/home/thanos/Fotis/plantsProject/pathways";
        String originalfastaFolderPath = "/home/thanos/Fotis/chlamydia/HumanOriginal/";
        String organismsWithIDFolder = "/home/thanos/Fotis/chlamydia/HumanWithID/";
        String HumanlistPath = "/home/thanos/Fotis/chlamydia/humanList.csv";
//
        String cdHitScriptPath = "/home/thanos/NetBeansProjects/genome-profiling/WorkFlow/src/workflow/cdHIT_makeBlastDB.sh";

        ArrayList<Node> nodes = new ArrayList<Node>();
        //nodes.add(n2);
        String masterIp = "46.12.120.197";
        double okeanosScore = 89003.8;
        Node n5 = new Node("enabled", "BW1", 5, "snf-658570.vm.okeanos.grnet.gr", "", "user", "aWT8fDEtST", okeanosScore, masterIp, "30433", "30431", "8");
        Node n4 = new Node("enabled", "BW2", 4, "snf-658569.vm.okeanos.grnet.gr", "", "user", "t5OLkyGPuR", okeanosScore, masterIp, "30432", "30431", "8");

        Node n3 = new Node("enabled", "Work", 3, "155.207.19.45", "", "thanos", "323143", 130549.8, masterIp, "30434", "30431", "8");
        Node n2 = new Node("enabled", "Home", 2, "127.0.0.1", "", "thanos", "323143", 137856.4, "127.0.0.1", "30431", "30430", "8"); //79.103.149.102
        n2.ismaster = true;
        Node n1 = new Node("enabled", "Aphrodite", 1, "155.207.19.43", "", "thanos", "1212", 180410.9, masterIp, "30444", "30431", "12");

        Node n0 = new Node("enabled", "Certh6", 0, "160.40.71.6", "localhost", "akintsakis", "m318297k", 241824.3, masterIp, "30435", "30491", "24");  //30292 score, 24 threads

        nodes.add(n0);
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        nodes.add(n4);
        nodes.add(n5);

        //*/
        Node n6 = new Node("enabled", "HC1", 6, "snf-658613.vm.okeanos.grnet.gr", "", "user", "s0gS3ZPCsi", okeanosScore, masterIp, "30436", "30431", "8");
        Node n7 = new Node("enabled", "HC2", 7, "snf-657775.vm.okeanos.grnet.gr", "", "user", "P8sfHB3WBd", okeanosScore, masterIp, "30437", "30431", "8");
        Node n8 = new Node("enabled", "HC3", 8, "snf-657776.vm.okeanos.grnet.gr", "", "user", "jFHU3L3Zmd", okeanosScore, masterIp, "30438", "30431", "8");
        Node n9 = new Node("enabled", "HC4", 9, "snf-657777.vm.okeanos.grnet.gr", "", "user", "IsMripQa70", okeanosScore, masterIp, "30439", "30431", "8");
        Node n10 = new Node("enabled", "HC5", 10, "snf-657778.vm.okeanos.grnet.gr", "", "user", "Wn3D6zW3QN", okeanosScore, masterIp, "30440", "30431", "8");
        Node n11 = new Node("enabled", "HC6", 11, "snf-657779.vm.okeanos.grnet.gr", "", "user", "Ruq6AWScQH", okeanosScore, masterIp, "30441", "30431", "8");
        Node n12 = new Node("enabled", "HC7", 12, "snf-657780.vm.okeanos.grnet.gr", "", "user", "NNW4pRt64y", okeanosScore, masterIp, "30442", "30431", "8");
        Node n13 = new Node("enabled", "HC8", 13, "snf-657781.vm.okeanos.grnet.gr", "", "user", "P7RO9YD7qw", okeanosScore, masterIp, "30443", "30431", "8");

        nodes.add(n6);
        nodes.add(n7);
        nodes.add(n8);
        nodes.add(n9);
        nodes.add(n10);
        nodes.add(n11);
        nodes.add(n12);
        nodes.add(n13);

        //*/
        fileSizeClusterer2("./95PithosFileListOrderedScore", nodes);

        //register available nodes
        //identify the workflow and define it
        //create threads = number of nodes
        //ssh to each node and execute code
        String chunkSize = "500";
        String descriptionAddon = "plantsAVAagain=" + chunkSize + "_experimentTBD";
        //COUNTDOWN_12_NewThreads
        //scheduler(nodes, componentsOrdered, chunkSize, descriptionAddon);
    }

}
