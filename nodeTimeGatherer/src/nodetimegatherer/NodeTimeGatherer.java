/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nodetimegatherer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thanos
 */
public class NodeTimeGatherer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String baseFolder = "/home/thanos/base/";

        File baseDir = new File(baseFolder);
        File[] baseDirs = baseDir.listFiles();
        int ccc = 0;

        for (int i = 0; i < baseDirs.length; i++) {
            System.out.println(baseDirs[i].getName());

            //declare max and average here
            ArrayList<String> values = new ArrayList<String>();
            //ArrayList<String> max = new ArrayList<String>();
            File[] contents1 = baseDirs[i].listFiles();

            for (int s = 0; s < contents1.length; s++) {
                if (contents1[s].getName().contains("experiment")) {

                    File[] contents2 = contents1[s].listFiles();
                    if (contents2[0].getName().equals("log")) {

                    } else {
                        contents2 = contents2[0].listFiles();
                    }

                    //File[] contents3 = contents2[0].listFiles();
                    for (int j = 0; j < contents2.length; j++) {
//                        if (contents2[j].getName().equals("log")) {
//                            //System.out.println("1111");
//                            List<String> logLines = Files.readAllLines(Paths.get(contents2[j].getAbsolutePath()), StandardCharsets.UTF_8);
//                            for (int k = 0; k < logLines.size(); k++) {
//                                if (logLines.get(k).contains("code/blastProfDistributer.jar")) {
//                                    values.add(logLines.get(k + 1));
//                                    break;
//                                }
//                            }
//                        }

                        //System.out.println(contents2[j].getName());
                        if (contents2[j].getName().equals("load.log")) {
                            File out = new File("/home/thanos/outs/" + contents1[s].getName() + ".nodelog");
                            ccc++;
                            FileWriter r1 = new FileWriter(out);
                            BufferedWriter wr1 = new BufferedWriter(r1);
                            //System.out.println("1111");
                            List<String> logLines = Files.readAllLines(Paths.get(contents2[j].getAbsolutePath()), StandardCharsets.UTF_8);
                            for (int k = 0; k < logLines.size(); k++) {
                                String value = logLines.get(k).substring(logLines.get(k).indexOf("load average: ")+14, logLines.get(k).length());
                                value = value.substring(0, value.indexOf(","));
                                wr1.write(value);
                                wr1.newLine();
//                                if (logLines.get(k).contains("code/blastProfDistributer.jar")) {
//                                    values.add(logLines.get(k + 1));
//                                    break;
//                                }
                            }
                            wr1.close();

                        }

                        //System.out.println("NO LOG FOUND in folder");
                    }
//                    File out = new File("/home/thanos/outs/" + baseDirs[i].getName()+".nodelog");
//                    FileWriter r1 = new FileWriter(out);
//                    BufferedWriter wr1 = new BufferedWriter(r1);
//
//                    for (int l = 0; l < values.size(); l++) {
//                        wr1.write(values.get(l));
//                        wr1.newLine();
//                    }
//                    wr1.close();
                }
            }

        }
    }

}
