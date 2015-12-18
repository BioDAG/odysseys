/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class ExternalScheduler {

    public static void classifyOus(String filename, HashMap<String, String> ousClassified, ArrayList<String> ouCategories) throws IOException {
        String delimiter = ";";
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        HashMap<String, String> keepUnique = new HashMap<String, String>();

        for (String line : lines) {
            String[] tmp = line.split(delimiter);
            //System.out.println(tmp[1]);
            ousClassified.put(tmp[1], tmp[2]);
            if (!keepUnique.containsKey(tmp[2])) {
                keepUnique.put(tmp[2], "");
                ouCategories.add(tmp[2]);
            }
        }

    }

    public static Double resolutionAnalysisForOUConfig(Double sumPower, Double sumSize, ArrayList<Node> nodes, LinkedHashMap<String, Double> files1, Double largestValue) {
        System.out.println("Sum size " + sumSize + " number " + files1.size());
        double oversizeStore = 0;
        HashMap<Double, Double> keepOversizeValue = new HashMap<Double, Double>();
        double minError = 1000000.0;
        int resolution = 10000;
        for (int s = 0; s < resolution; s++) {
            LinkedHashMap<String, Double> testfiles = new LinkedHashMap<String, Double>();
            testfiles.putAll(files1);
            double maxError = 0;
            int tot = 0;

            oversizeStore = largestValue * (double) s / resolution;
            if (oversizeStore > largestValue) {
                break;
            }

            for (int i = 0; i < nodes.size(); i++) {
                double size = (nodes.get(i).cpuPower / sumPower) * sumSize;

                double total = 0.0;
                double oversize = oversizeStore;
                if (i == nodes.size() - 1) {
                    oversize = 30000000.0;
                }
                int flag = 0;
                String keepKey = "";
                int count = 0;

                for (Map.Entry<String, Double> entry1 : testfiles.entrySet()) {
                    String key1 = entry1.getKey();
                    if (testfiles.get(key1) > 0.0) {
                        if (testfiles.get(key1) < size && flag == 0) {
                            size = size - testfiles.get(key1);
                            total = total + testfiles.get(key1);
                            testfiles.put(key1, -100.0);
                            count++;
                        } else {
                            flag = 1;
                            if (oversize > Math.abs(size - testfiles.get(key1))) {
                                oversize = Math.abs(size - testfiles.get(key1));
                                keepKey = key1;
                            }
                        }
                    }
                }
                if (!keepKey.equals("")) {
                    total = total + testfiles.get(keepKey);
                    testfiles.put(keepKey, -100.0);
                    count++;
                }
                //totalCount = totalCount + count;
                tot = tot + count;
                if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize))) / (nodes.get(i).cpuPower / sumPower)) {
                    maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize))) / (nodes.get(i).cpuPower / sumPower);
                }
            }
            if (minError > maxError && tot == testfiles.size()) {
                minError = maxError;
                keepOversizeValue.put(minError, oversizeStore);
            }

        }
        return keepOversizeValue.get(minError);
    }

    public static void setOUConfigforOUClass(ArrayList<Node> nodes, String filesList, HashMap<String, String> ousClassified, String currentCategory, String nodeSpecificDataFilePath) throws IOException {

        LinkedHashMap<String, Double> files = new LinkedHashMap<String, Double>();
        File fileList = new File(filesList);
        List<String> lines = Files.readAllLines(Paths.get(fileList.getAbsolutePath()), StandardCharsets.UTF_8);

        double largestValue = -1;

        double sumSize = 0;
        for (int i = 0; i < lines.size(); i++) {
            String[] temp = lines.get(i).split("MiB ");
            //System.out.println(lines.get(i));

            //if (i == 0) {
            //    largestValue = Double.valueOf(temp[0]);
            //}
            Path p = Paths.get((temp[1]));
            String tmpfile = p.getFileName().toString();
            tmpfile=tmpfile.substring(0, tmpfile.lastIndexOf('.'));
            //System.out.println(tmpfile);

            if (ousClassified.get(tmpfile).equals(currentCategory)) {
                files.put(temp[1], Double.valueOf(temp[0]));
                sumSize = sumSize + Double.valueOf(temp[0]);
                if (Double.valueOf(temp[0]) > largestValue) {
                    largestValue = Double.valueOf(temp[0]);
                }
            }
        }
        double sumPower = 0;
        for (int i = 0; i < nodes.size(); i++) {
            sumPower = sumPower + nodes.get(i).cpuPower;
        }

        double oversizeStore = resolutionAnalysisForOUConfig(sumPower, sumSize, nodes, files, largestValue);
        writeOUClassConfigToFiles(files, nodes, sumPower, sumSize, oversizeStore,nodeSpecificDataFilePath);

    }

    public static void fileSizeClusterer(String ouList, String ouSizes, ArrayList<Node> nodes, String nodeSpecificDataFilePath) throws IOException {
        HashMap<String, String> ousClassified = new HashMap<String, String>();
        ArrayList<String> ouCategories = new ArrayList<String>();
        classifyOus(ouList, ousClassified, ouCategories);
        File folder =new File(nodeSpecificDataFilePath);
        File[] files =folder.listFiles();
        
        for(int i=0;i<files.length;i++)
        {
            if(files[i].getName().endsWith(".organism"));
            files[i].delete();
        }
        
        for (int i = 0; i < ouCategories.size(); i++) {
            System.out.println("sending "+ouCategories.get(i));
            setOUConfigforOUClass(nodes, ouSizes, ousClassified, ouCategories.get(i),nodeSpecificDataFilePath);
        }

    }

    public static void writeOUClassConfigToFiles(LinkedHashMap<String, Double> files, ArrayList<Node> nodes, Double sumPower, Double sumSize, Double oversizeStore, String nodeSpecificDataFilePath) throws IOException {
        //filesKeepCopy.putAll(files);
        //oversizeStore = keepOversize.get(minError);
        double maxError = 0;        
        for (int i = 0; i < nodes.size(); i++) {
            //System.out.println();
            BufferedWriter wr1 = new BufferedWriter(new FileWriter(new File(nodeSpecificDataFilePath+"/" + nodes.get(i).id + ".organisms"),true));
            double size = (nodes.get(i).cpuPower / sumPower) * sumSize;
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

            for (Map.Entry<String, Double> entry1 : files.entrySet()) {
                String key1 = entry1.getKey();
                //System.out.println(files.get(key1));
                if (files.get(key1) > 0.0) {
                    if (files.get(key1) < size && flag == 0) {
                        size = size - files.get(key1);
                        total = total + files.get(key1);
                        wr1.write(key1);
                        wr1.newLine();
                        files.put(key1, -100.0);
                        //files.remove(key1);
                        count++;
                    } else {
                        flag = 1;
                        if (oversize > Math.abs(size - files.get(key1))) {
                            oversize = Math.abs(size - files.get(key1));
                            keepKey = key1;
                        }

                    }
                }

                //System.out.println(key1 + files.get(key1));
            }
            if (!keepKey.equals("")) {
                wr1.write(keepKey);
                wr1.newLine();
                total = total + files.get(keepKey);
                files.put(keepKey, -100.0);
                count++;
            }
            //wr1.write(String.valueOf(total));
            wr1.close();
            //totalCount = totalCount + count;
            System.out.println(total + " " + count + "   expected%" + (nodes.get(i).cpuPower / sumPower) + "   real%" + (total / sumSize));
            if (maxError < (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize))) / (nodes.get(i).cpuPower / sumPower)) {
                maxError = (Math.abs((nodes.get(i).cpuPower / sumPower) - (total / sumSize))) / (nodes.get(i).cpuPower / sumPower);
            }

        }
      //  if (minError != maxError) {
        //      System.out.println("####FATAL ERROR something went wrong, minError found is not same with minError in set written");
        // }
        // System.out.println("CrossCheck minError " + minError + " =  " + maxError);
        System.out.println();
        //System.out.println("caynobacteria, number " + files2.size());
        System.out.println();
    }
/*
    public static void fileSizeClusterer2(String filesList, ArrayList<Node> nodes) throws IOException {

        HashMap<String, String> ousClassified = new HashMap<String, String>();
        ArrayList<String> ouCategories = new ArrayList<String>();

        classifyOus(filesList, ousClassified, ouCategories);

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

        ////
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
*/
}
