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
import java.io.FileInputStream;
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
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class WorkFlow {

    public static Properties generalConfiguration = new Properties();
    public static final String generalConfigFile = "/home/thanos/Dropbox/Code/odysseys/MasterNode/generalConfiguration.properties";

    public static void initializeNode(Node node, String workingDir, SSHExec ssh) throws Exception {

        String initCodeFolderPath = generalConfiguration.getProperty("initCodeFolderPath");
        String initDataFolderPath = generalConfiguration.getProperty("initDataFolderPath");
        String nodeSpecificData = generalConfiguration.getProperty("nodeSpecificData");
        String kamakiAuthenticationFilePath = generalConfiguration.getProperty("kamakiAuthenticationFilePath");

        String rootPath = "/home/" + node.username + "/" + workingDir + "/";
        System.out.println("--------------------------------Initializing node " + node.name);

        ssh.connect();
        CustomTask sampleTask;
        sampleTask = new ExecCommand("rm -rf ~/*deleteOnNextRunOdysseus*");
        ssh.exec(sampleTask);

        ssh.uploadAllDataToServer(initCodeFolderPath, rootPath);
        ssh.uploadSingleDataToServer(initDataFolderPath + "/list.csv", rootPath);
        ssh.uploadSingleDataToServer(initDataFolderPath + "/TreeOrder.txt", rootPath);
        ssh.uploadSingleDataToServer(nodeSpecificData + "/" + node.id + ".organisms", rootPath);
        ssh.uploadSingleDataToServer(kamakiAuthenticationFilePath, ("/home/" + node.username + "/"));

        sampleTask = new ExecCommand("cd " + rootPath, "mkdir remote");//,"sshfs thanos@"+node.masterAddress+":/home/thanos/Master/"+node.id+" "+rootPath+"remote/ -o nonempty");
        ssh.exec(sampleTask);
        if (node.ismaster == true) {
            sampleTask = new ExecCommand("cd " + rootPath, "mkdir master");//,"sshfs thanos@"+node.masterAddress+":/home/thanos/Master/"+node.id+" "+rootPath+"remote/ -o nonempty");
            ssh.exec(sampleTask);
        }
        sampleTask = new ExecCommand("screen -X -S clientDaemon kill", "screen -X -S loadLoging kill", "screen -X -S memLoging kill", "rm -f ~/load.log", "rm -f ~/log", "rm -f ~/mem.log", "screen -S clientDaemon -d -m java -jar " + rootPath + "initCode/Client.jar " + node.masterAddress + " " + node.localJobPort + " " + node.remoteJobPort, "screen -S loadLoging -d -m bash " + rootPath + "initCode/loadLogScript.sh", "screen -S memLoging -d -m bash " + rootPath + "initCode/memLogScript.sh");
        ssh.exec(sampleTask);
        ssh.disconnect();

        System.out.println("-------------------------------------------Node " + node.name + " initialized");
    }

    public static void scheduler(ArrayList<Node> nodes) throws InterruptedException, IOException, Exception {

        String chunkSize = generalConfiguration.getProperty("chunkSize");
        String descriptionAddon = generalConfiguration.getProperty("descriptionAddon");
        descriptionAddon = descriptionAddon + "_chunkSize=" + chunkSize;

        String workingDir = (new Date()).toString() + "_" + descriptionAddon;// + "_experimentTBD";
        workingDir = workingDir.replaceAll(" ", "-");
        workingDir = workingDir.replaceAll(":", "");
        ArrayList<NodeThread> NodeThreads = new ArrayList<NodeThread>();
        ConnBean cb = new ConnBean(nodes.get(0).ip, nodes.get(0).username, nodes.get(0).password);
        SSHExec ssh = SSHExec.getInstance(cb);
        ArrayList<Component> componentsTobeExecuted = new ArrayList<Component>();

        for (int i = 0; i < nodes.size(); i++) {

            if ((nodes.get(i).enabled.equals("enabled"))) {
                cb.setHost(nodes.get(i).ip);
                cb.setUser(nodes.get(i).username);
                cb.setPassword(nodes.get(i).password);
                initializeNode(nodes.get(i), workingDir, ssh);
                NodeThread t = new NodeThread(nodes.get(i).id, nodes.get(i), workingDir, chunkSize, componentsTobeExecuted);
                TimeUnit.SECONDS.sleep(5);
                NodeThreads.add(t);
            }

        }
        for (int i = 0; i < NodeThreads.size(); i++) {
            NodeThreads.get(i).start();
        }
        for (int i = 0; i < NodeThreads.size(); i++) {
            NodeThreads.get(i).join();
        }

    }

    public static void buildNodeComponentExecutionList(ArrayList<Node> nodes, ArrayList<Component> componentsGeneral) {

        for (int i = 0; i < nodes.size(); i++) {

        }

    }

    public static ArrayList<Node> constructNodes(String filename) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        ArrayList<Node> nodes = new ArrayList<Node>();

        for (int i = 1; i < lines.size(); i++) {
            if (!lines.contains("//")) {
                String[] tmp = lines.get(i).split(",");
                String tunnelIp;
                if (tmp[0].toLowerCase().equals("enabled")) {
                    if (!tmp[5].toLowerCase().equals("null")) {
                        tunnelIp = tmp[5];
                    } else {
                        tunnelIp = "";
                    }
                    String enabled = "false";
                    if (tmp[0].toLowerCase().equals("enabled")) {
                        enabled = "enabled";
                    }
                    System.out.println(enabled + " " + tmp[2] + " " + Integer.valueOf(tmp[3]) + " " + tmp[4] + " " + tunnelIp + " " + tmp[6] + " " + tmp[7] + " " + Double.valueOf(tmp[8]) + " " + tmp[9] + " " + tmp[10] + " " + tmp[11] + " " + tmp[12]);
                    Node n0 = new Node(enabled, tmp[2], Integer.valueOf(tmp[3]), tmp[4], tunnelIp, tmp[6], tmp[7], Double.valueOf(tmp[8]), tmp[9], tmp[10], tmp[11], tmp[12]); //30292 score, 24 threads 
                    if (tmp[1].toLowerCase().equals("true")) {
                        n0.ismaster = true;
                    }
                    nodes.add(n0);
                }
            }

        }
        return nodes;

        //Node n1 = new Node("enabled", "Aphrodite", 1, "155.207.19.43", "", "thanos", "1212", 180410.9, masterIp, "30444", "30431", "12");
    }

    public static void main(String[] args) throws IOException, InterruptedException, Exception {

        FileInputStream generalConfigurationFile
                = new FileInputStream(generalConfigFile);
        try {
            generalConfiguration.load(generalConfigurationFile);
            generalConfigurationFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<Node> nodes = constructNodes(generalConfiguration.getProperty("nodesFile"));
        //fileSizeClusterer2(generalConfiguration.getProperty("OUnamesAndSizesPath"), nodes);
        ExternalScheduler.fileSizeClusterer("/home/thanos/Dropbox/Code/odysseys/variousInputFiles/list.csv", "/home/thanos/Dropbox/Code/odysseys/MasterNode/initData/95PithosFileListOrderedScore", nodes, "/home/thanos/Dropbox/Code/odysseys/MasterNode/nodeSpecificData");
        //register available nodes
        //identify the workflow and define it
        //create threads = number of nodes
        //ssh to each node and execute code

        scheduler(nodes);
    }

}
