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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeThread extends Thread {

    Node node;
    ServerSocket jobSocket;
    int id;
    String workingDir;
    String chunkSize;// = "100";
    String profileMode = "1";
    Boolean saveNodeLogs = false;
    String datasetInfo = "";
    Boolean activeMonitoring = false;
    //String cdhit = "0";
    Boolean useLocalQuery = false;
    ArrayList<Component> componentsTobeExecuted;

    public NodeThread(int id, Node node, String workingDir, String chunkSize, ArrayList<Component> componentsTobeExecuted) throws IOException, Exception {
        this.id = id;
        this.node = node;
        this.workingDir = workingDir;
        jobSocket = new ServerSocket(Integer.parseInt(node.localJobPort));
        this.chunkSize = chunkSize;
        this.componentsTobeExecuted = componentsTobeExecuted;
        if (WorkFlow.generalConfiguration.getProperty("activeMonitoring").toLowerCase().equals("true")) {
            activeMonitoring = true;
        }
        if (WorkFlow.generalConfiguration.getProperty("useLocalQuery").toLowerCase().equals("true")) {
            useLocalQuery = true;
        }
        if (WorkFlow.generalConfiguration.getProperty("saveNodeLogs").toLowerCase().equals("true")) {
            saveNodeLogs = true;
        }
        datasetInfo = WorkFlow.generalConfiguration.getProperty("datasetInfo");
        profileMode = WorkFlow.generalConfiguration.getProperty("profileMode");        
    }

    public static void talker(String message, String serverName, String portString) {
        //String serverName = "Node1";
        int port = Integer.parseInt(portString);
        try {
            // System.out.println("Connecting to " + serverName+" on port " + port);
            Socket client = new Socket(serverName, port);
            // System.out.println("Just connected to "
            //         + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out
                    = new DataOutputStream(outToServer);

            out.writeUTF(message); // + client.getLocalSocketAddress()
            // InputStream inFromServer = client.getInputStream();
            // DataInputStream in = new DataInputStream(inFromServer);
            //  System.out.println("Server says " + in.readUTF());
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<String> constructWorkflowCommands(String rootPath) throws InterruptedException {
        ArrayList<String> commands = new ArrayList<String>();
        String currentCommand;
        //currentCommand = "chunk size " + chunkSize + " profile mode " + profileMode + " dataset " + dataset;
        //commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        //
        currentCommand = "bash;" + rootPath + "initCode/kamakiCodeDownload.sh;" + rootPath;
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);
        //

        currentCommand = "bash;" + rootPath + "code/workspaceInit.sh;" + rootPath;
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        //command = "bash;" + rootPath + "code/compileTools.sh;" + rootPath + "code/cd-hit/";
        //sendIt(command, node.ip, node.remoteJobPort);
        currentCommand = "bash;" + rootPath + "code/kamakiDownloader.sh;" + rootPath + id + ".organisms;" + rootPath + "./organisms";
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        currentCommand = "java;-jar;" + rootPath + "/code/organismIdAssigner.jar;" + rootPath + "/organisms/;" + rootPath + "/list.csv;" + rootPath + "/organismsWithID/";
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        if (useLocalQuery) {
            currentCommand = "bash;" + rootPath + "code/bypassPathways.sh;" + rootPath;
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);

        } else {
            TimeUnit.SECONDS.sleep(id * 20);
            currentCommand = "bash;" + rootPath + "code/downloadPathways.sh;" + rootPath;
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
        }

        long t1 = System.currentTimeMillis();

        if (profileMode.equals("1")) {
//            if (cdhit.equals("1")) {
//                currentCommand = "java;-jar;" + rootPath + "code/localDistributer.jar;" + rootPath + "organismsWithID;" + rootPath + "HITorganismsWithID/;" + rootPath + "code/cd-hit/cd-hit -i inFile -o outFile -c 1.00 -n 5 -g 1;" + node.threads;//"8";
//                sendIt(currentCommand, node.ip, node.remoteJobPort);
//
//                currentCommand = "bash;" + rootPath + "code/buildDB.sh;" + rootPath;
//                sendIt(currentCommand, node.ip, node.remoteJobPort);
//            } else {
            currentCommand = "bash;" + rootPath + "code/buildDBNoHit.sh;" + rootPath;
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
            //  }

        } else if (profileMode.equals("2")) {
            currentCommand = "bash;" + rootPath + "code/buildDBAVA.sh;" + rootPath;
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
        }

        currentCommand = "java;-jar;" + rootPath + "code/fastaSplitter.jar;" + rootPath + "allPathways.fasta;" + rootPath + "chunks/;" + chunkSize;
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        //command = "java;-jar;" + rootPath + "code/localDistributerNC.jar;" + rootPath + "chunks/;" + rootPath + "blastOut/;" + "/opt/ncbi-blast-2.2.30+/bin/blastp -query inFile -db " + rootPath + "DB/DB" + " -evalue 0.000001 -outfmt 6 -out outFile.blastp;" + "8";
        //sendIt(command, node.ip, node.remoteJobPort);
        //    if (noActiveMonitoring.equals("0")) {
        currentCommand = "java;-jar;" + rootPath + "code/blastProfDistributer.jar;" + rootPath + "chunks/;" + rootPath + "blastOut/;" + "/opt/ncbi-blast-2.2.30+/bin/blastp -query inFile -db " + rootPath + "DB/DB" + " -evalue 0.000001 -outfmt 6 -out outFile.blastp;" + rootPath + "TreeOrder.txt;" + node.threads + ";" + profileMode;
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        if (profileMode.equals("1")) {
            currentCommand = "bash;" + rootPath + "code/catNodeScript.sh;" + rootPath + "blastOut/;" + String.valueOf(node.id);
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
        } else if (profileMode.equals("2")) {
            currentCommand = "bash;" + rootPath + "code/catAVA.sh;" + rootPath + "blastOut/;" + String.valueOf(node.id);
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
        }
        long t2 = System.currentTimeMillis();
        //System.out.println(node.ip + " finished @ " + (t2 - t1) + " seconds");

        currentCommand = "bash;" + rootPath + "code/killLogers.sh";
        commands.add(currentCommand);
        //sendIt(currentCommand, node.ip, node.remoteJobPort);

        if (saveNodeLogs) {
            currentCommand = "bash;" + rootPath + "code/tarUploadonlyLogs.sh;" + rootPath + ";" + id + ".tar.gz;" + workingDir;
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
        } else {
            currentCommand = "terminate";
            commands.add(currentCommand);
            //sendIt(currentCommand, node.ip, node.remoteJobPort);
        }
//        } else {
//            currentCommand = "java;-jar;" + rootPath + "code/blastProfDistributer.jar;" + rootPath + "chunks/;" + rootPath + "blastOut/;" + "/opt/ncbi-blast-2.2.30+/bin/blastp -query inFile -db " + rootPath + "DB/DB" + " -evalue 0.000001 -outfmt 6 -out outFile.blastp;" + rootPath + "TreeOrder.txt;" + node.threads + ";" + profileMode + "!";
//
//            if (profileMode.equals("1")) {
//                currentCommand = currentCommand + "bash;" + rootPath + "code/catNodeScript.sh;" + rootPath + "blastOut/;" + String.valueOf(node.id) + "!";
//            } else {
//                currentCommand = currentCommand + "bash;" + rootPath + "code/catAVA.sh;" + rootPath + "blastOut/;" + String.valueOf(node.id) + "!";
//            }
//            currentCommand = currentCommand + "bash;" + rootPath + "code/killLogers.sh" + "!";
//            if (saveResults.equals("1")) {
//                currentCommand = currentCommand + "bash;" + rootPath + "code/tarUploadonlyLogs.sh;" + rootPath + ";" + id + ".tar.gz;" + workingDir;
//            } else {
//                currentCommand = currentCommand + "terminate";
//            }
//            sendIt(currentCommand, node.ip, node.remoteJobPort);
//
//        }
        return commands;
    }

    public String waitForCommand() {
        //while (true) {
        try {
            //System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = jobSocket.accept();
            //System.out.println("Just connected to " + server.getRemoteSocketAddress());
            String sender = server.getRemoteSocketAddress().toString();
            System.out.println(sender);
            DataInputStream in = new DataInputStream(server.getInputStream());
            String message = in.readUTF();
                //message=in.readLine();

            //message=message.substring(0,message.indexOf("EOC"));
            //System.out.println(message);
            //DataOutputStream out = new DataOutputStream(server.getOutputStream());
            //out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
            server.close();
            return message;
        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
            return "timeout exception";
            //break;
        } catch (IOException e) {
            e.printStackTrace();
            //return "IOException";
            //break;
        }
        return "Error";
        //}

    }

    public void sendIt(String command, String ip, String port) {
        if (!node.tunnelIP.equals("")) {
            talker(command, node.tunnelIP, port);
        } else {
            talker(command, ip, port);
        }

        String response = waitForCommand();
//        if (!response.contains("SUCCESS")) {
//            System.out.println("FATAL ERROR " + response);
//        }
        System.out.println("NODE " + node.name + " " + response);
    }

    public String buildCommandBundle(ArrayList<String> commands) {
        String bundle = "";
        for (int i = 0; i < commands.size(); i++) {
            bundle = bundle + commands.get(i) + "<cmd>";
        }
        //System.out.println(" PRINTING BUNDLE = "+bundle);
        // String[] tmp = bundle.split("<cmd>");
        //System.out.println("bundle [0] "+tmp[0]);
        return bundle;
    }

    @Override
    public void run() {

        try {
            String arg1, arg2, arg3, arg4;
            //connect
            //buildworkingDirectory
            //copy all code
            //copy all initial input
            //create organisms directory
            //download localList
            //download list
            //id organisms
            //download pathways
            //merge them and split them
            //cd hit organisms
            //create blastdb
            //start blasts

//            String command;
//            String response;
            String rootPath = "/home/" + node.username + "/" + workingDir + "/";
            ArrayList<String> commands = constructWorkflowCommands(rootPath);

            if (activeMonitoring) {
                for (int i = 0; i < commands.size(); i++) {
                    sendIt(commands.get(i), node.ip, node.remoteJobPort);
                }
            } else {
                String commandBundle = buildCommandBundle(commands);
                System.out.println(commandBundle);
                sendIt(commandBundle, node.ip, node.remoteJobPort);
            }
        } catch (Exception ex) {
            Logger.getLogger(NodeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
