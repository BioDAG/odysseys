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
package client;

import static client.Client.executeBashScript;
import static client.Client.talker;
import static client.Client.waitForCommand;
import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Client {

    private static ServerSocket jobSocket;
    private static ServerSocket querySocket;
    private static String masterAddress = "localhost";
    private static String masterJobPort = "30435";
    private static String masterQueryPort = "localhost";

    /**
     * @param args the command line arguments
     */
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

            out.writeUTF(message);// + client.getLocalSocketAddress()
            // InputStream inFromServer = client.getInputStream();
            // DataInputStream in = new DataInputStream(inFromServer);
            //  System.out.println("Server says " + in.readUTF());
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String waitForCommand() {
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
            // System.out.println(message);
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

//    public Client(int port) throws IOException {
//        serverSocket = new ServerSocket(port);
//        serverSocket.setSoTimeout(10000);
//    }
    public static String executeBashScript(String[] cmd) {
        //StringBuffer output = new StringBuffer();
        StringBuffer output1 = new StringBuffer();
        Process p;
        try {
            //System.out.println("execturing " + cmd);
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            //BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream())); //p.getInputStream()
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = "";
//            while ((line = reader.readLine()) != null) {
//                output.append(line + "\n");
//            }
            while ((line = error.readLine()) != null) {
                output1.append(line + "\n");
            }
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception " + e);
        }
        System.out.println("before");
        //System.out.println(output.toString());
        System.out.println(output1.toString());
        return output1.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        masterAddress = args[0];
        masterJobPort = args[1];
        //masterAddress="127.0.0.1";
        //jobSocket = new ServerSocket(Integer.parseInt("30435"));
        //masterJobPort=args[1];

        jobSocket = new ServerSocket(Integer.parseInt(args[2]));
        jobSocket.setSoTimeout(1000000);
        //String command=waitForCommand();
        System.out.println("CLIENT INITIATED LISTENING ON PORT " + args[2] + " SENDING ON " + masterAddress + ":" + masterJobPort);
        String incoming;
        String splitby = ";";
        String[] command;

        File log = new File("log");
        FileWriter fwr1 = new FileWriter(log);
        BufferedWriter wr1 = new BufferedWriter(fwr1);
        long t1 = 0;
        long t3;
        long t4;
        int firstCommandReceived = 0;
        String proc = "";

        while (true) {
            incoming = waitForCommand();

//            if (firstCommandReceived == 0) {
//                firstCommandReceived = 1;
//                wr1.write(incoming);
//                wr1.newLine();
//                wr1.write((new Date()).toString());
//                wr1.newLine();
//                System.out.println("Initializing with parameters: "+incoming);
//                talker("Initialized @    " + (new Date()).toString()+" with parameters "+incoming, masterAddress, masterJobPort);
//                t1 = System.currentTimeMillis();
//            } else {
            if (incoming.contains("<cmd>")) {
                talker("Bundle received" + incoming + ".. exiting communications @    " + (new Date()).toString(), masterAddress, masterJobPort);
                String[] bundle = incoming.split("<cmd>");
                System.out.println("commands received in bundle : "+bundle.length);
                System.out.println("all INCOMING::::: " + incoming);
                //TimeUnit.SECONDS.sleep(50);
                for (int b = 0; b < bundle.length; b++) {

                    incoming = bundle[b];
                    System.out.println("1 COMMAND :" + incoming);
                    //TimeUnit.SECONDS.sleep(50);
                    if (incoming.contains(("tarUpload"))) {
                        wr1.write("final write before uploading...");
                        //wr1.flush();
                        long t2 = System.currentTimeMillis();
                        wr1.write(String.valueOf((t2 - t1)));
                        wr1.newLine();
                        wr1.write("success exit");
                        wr1.newLine();
                        wr1.write((new Date()).toString());
                        wr1.close();
                        System.out.println("bundle| incoming: " + incoming);
                        //System.out.println("END REACHED.. exiting");

                        command = incoming.split(";");
                        proc = executeBashScript(command);
                        if (!proc.isEmpty()) {
                            System.out.println("sending :" + "FAILURE @ " + incoming);
                            //wr1.write("Bundle| FAILED END (with upload).. exiting @    " + (new Date()).toString());
                        } else {
                            System.out.println("sending :" + "SUCCESS @ " + incoming);
                            //wr1.write("Bundle| SUCCESS END on upload).. exiting @    " + (new Date()).toString());
                        }
                        //wr1.close();
                        break;
                    } else if (incoming.contains("terminate")) {
                        System.out.println("Bundle| END REACHED.. exiting");
                        //talker("END REACHED.. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
                        long t2 = System.currentTimeMillis();
                        wr1.write(String.valueOf((t2 - t1)));
                        wr1.newLine();
                        wr1.write("success exit");
                        wr1.newLine();
                        wr1.write((new Date()).toString());
                        wr1.close();
                        break;

                    } else {
                        System.out.println("bundle | incoming: " + incoming);
                        command = incoming.split(";");
                        t3 = System.currentTimeMillis();
                        wr1.write((new Date()).toString());
                        proc = executeBashScript(command);
                        wr1.newLine();
                        wr1.write(incoming);
                        wr1.newLine();
                        t4 = System.currentTimeMillis();
                        wr1.write(String.valueOf(t4 - t3));
                        wr1.newLine();
                        wr1.write((new Date()).toString());
                        wr1.newLine();
                        wr1.newLine();
                        if (!proc.isEmpty()) {
                            System.out.println("sending :" + "FAILURE @ " + incoming);
                            wr1.write("Bundle, FAILED . exiting @    " + (new Date()).toString() + " on command " + incoming);
                        } else {
                            System.out.println("sending :" + "SUCCESS @ " + incoming);
                            wr1.write("Bundle, SUCCESS ).. exiting @    " + (new Date()).toString() + " on command " + incoming);
                        }
                        wr1.flush();
                    }
                }

                break;
            }

            if (incoming.contains("terminate")) {
                System.out.println("END REACHED.. exiting");
                talker("END REACHED.. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
                long t2 = System.currentTimeMillis();
                wr1.write(String.valueOf((t2 - t1)));
                wr1.newLine();
                wr1.write("success exit");
                wr1.newLine();
                wr1.write((new Date()).toString());
                wr1.close();
                break;
            } else if (incoming.contains("timeout")) {
                System.out.println("server not sending... exiting");
                talker("server not sending... exiting", masterAddress, masterJobPort);
                long t2 = System.currentTimeMillis();
                wr1.write(String.valueOf((t2 - t1)));
                wr1.newLine();
                wr1.write("timeout Exit");
                wr1.newLine();
                wr1.write((new Date()).toString());
                wr1.close();
                break;

            }
            if (incoming.contains(("tarUpload"))) {
                wr1.write("final write before uploading...");
                //wr1.flush();
                long t2 = System.currentTimeMillis();
                wr1.write(String.valueOf((t2 - t1)));
                wr1.newLine();
                wr1.write("success exit");
                wr1.newLine();
                wr1.write((new Date()).toString());
                wr1.close();
                System.out.println("incoming: " + incoming);
                //System.out.println("END REACHED.. exiting");

                command = incoming.split(";");
                proc = executeBashScript(command);
                if (!proc.isEmpty()) {
                    System.out.println("sending :" + "FAILURE @ " + incoming);
                    talker("FAILED END (with upload).. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
                } else {
                    System.out.println("sending :" + "SUCCESS @ " + incoming);
                    talker("SUCCESS END on upload).. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
                }

                break;
            } else {
                System.out.println("incoming: " + incoming);
                command = incoming.split(";");
                t3 = System.currentTimeMillis();
                wr1.write((new Date()).toString());
                proc = executeBashScript(command);
                wr1.newLine();
                wr1.write(incoming);
                wr1.newLine();
                t4 = System.currentTimeMillis();
                wr1.write(String.valueOf(t4 - t3));
                wr1.newLine();
                wr1.write((new Date()).toString());
                wr1.newLine();
                wr1.newLine();
            }
            if (!proc.isEmpty()) {
                System.out.println("sending :" + "FAILURE @ " + incoming);
                talker("FAILURE @ " + incoming + "  " + proc, masterAddress, masterJobPort);
            } else {
                System.out.println("sending :" + "SUCCESS @ " + incoming);
                talker("SUCCESS @ " + incoming, masterAddress, masterJobPort);
            }
        }

    }
    //wr1.close();

}

//}
//while (true) {
//            incoming = waitForCommand();
//            if (logic == 0) {
//                logic = 1;
//                wr1.write(incoming);
//                wr1.newLine();
//                wr1.write((new Date()).toString());
//                wr1.newLine();
//                System.out.println("Initializing with parameters: "+incoming);
//                talker("Initialized @    " + (new Date()).toString()+" with parameters "+incoming, masterAddress, masterJobPort);
//                t1 = System.currentTimeMillis();
//            } else {
//                if (incoming.contains("!")) {
//                    talker("Bundle received"+incoming+".. exiting communications @    " + (new Date()).toString(), masterAddress, masterJobPort);
//                    String[] bundle = incoming.split("!");
//                    System.out.println("all INCOMING::::: "+incoming);
//                    //TimeUnit.SECONDS.sleep(50);
//                    for (int b = 0; b < bundle.length; b++) {
//                        
//                        incoming = bundle[b];
//                        System.out.println("1 COMMAND :"+incoming);
//                        //TimeUnit.SECONDS.sleep(50);
//                        if (incoming.contains(("tarUpload"))) {
//                            wr1.write("final write before uploading...");
//                            //wr1.flush();
//                            long t2 = System.currentTimeMillis();
//                            wr1.write(String.valueOf((t2 - t1)));
//                            wr1.newLine();
//                            wr1.write("success exit");
//                            wr1.newLine();
//                            wr1.write((new Date()).toString());
//                            wr1.close();
//                            System.out.println("bundle| incoming: " + incoming);
//                            //System.out.println("END REACHED.. exiting");
//
//                            command = incoming.split(";");
//                            proc = executeBashScript(command);
//                            if (!proc.isEmpty()) {
//                                System.out.println("sending :" + "FAILURE @ " + incoming);
//                                //wr1.write("Bundle| FAILED END (with upload).. exiting @    " + (new Date()).toString());
//                            } else {
//                                System.out.println("sending :" + "SUCCESS @ " + incoming);
//                                 //wr1.write("Bundle| SUCCESS END on upload).. exiting @    " + (new Date()).toString());
//                            }
//                            //wr1.close();
//                            break;
//                        } else if (incoming.contains("terminate")) {
//                            System.out.println("Bundle| END REACHED.. exiting");
//                            //talker("END REACHED.. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
//                            long t2 = System.currentTimeMillis();
//                            wr1.write(String.valueOf((t2 - t1)));
//                            wr1.newLine();
//                            wr1.write("success exit");
//                            wr1.newLine();
//                            wr1.write((new Date()).toString());
//                            wr1.close();
//                            break;
//
//                        } else {
//                            System.out.println("bundle | incoming: " + incoming);
//                            command = incoming.split(";");
//                            t3 = System.currentTimeMillis();
//                            wr1.write((new Date()).toString());
//                            proc = executeBashScript(command);
//                            wr1.newLine();
//                            wr1.write(incoming);
//                            wr1.newLine();
//                            t4 = System.currentTimeMillis();
//                            wr1.write(String.valueOf(t4 - t3));
//                            wr1.newLine();
//                            wr1.write((new Date()).toString());
//                            wr1.newLine();
//                            wr1.newLine();
//                            if (!proc.isEmpty()) {
//                                System.out.println("sending :" + "FAILURE @ " + incoming);
//                                wr1.write("Bundle, FAILED . exiting @    " + (new Date()).toString() + " on command " + incoming);
//                            } else {
//                                System.out.println("sending :" + "SUCCESS @ " + incoming);
//                                 wr1.write("Bundle, SUCCESS ).. exiting @    " + (new Date()).toString() + " on command " + incoming);
//                            }
//                           // wr1.close();
//                        }
//                    }
//
//                    break;
//                }           
//                
//                if (incoming.contains("terminate")) {
//                    System.out.println("END REACHED.. exiting");
//                    talker("END REACHED.. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
//                    long t2 = System.currentTimeMillis();
//                    wr1.write(String.valueOf((t2 - t1)));
//                    wr1.newLine();
//                    wr1.write("success exit");
//                    wr1.newLine();
//                    wr1.write((new Date()).toString());
//                    wr1.close();
//                    break;
//                } else if (incoming.contains("timeout")) {
//                    System.out.println("server not sending... exiting");
//                    talker("server not sending... exiting", masterAddress, masterJobPort);
//                    long t2 = System.currentTimeMillis();
//                    wr1.write(String.valueOf((t2 - t1)));
//                    wr1.newLine();
//                    wr1.write("timeout Exit");
//                    wr1.newLine();
//                    wr1.write((new Date()).toString());
//                    wr1.close();
//                    break;
//
//                }
//                if (incoming.contains(("tarUpload"))) {
//                    wr1.write("final write before uploading...");
//                    //wr1.flush();
//                    long t2 = System.currentTimeMillis();
//                    wr1.write(String.valueOf((t2 - t1)));
//                    wr1.newLine();
//                    wr1.write("success exit");
//                    wr1.newLine();
//                    wr1.write((new Date()).toString());
//                    wr1.close();
//                    System.out.println("incoming: " + incoming);
//                    //System.out.println("END REACHED.. exiting");
//
//                    command = incoming.split(";");
//                    proc = executeBashScript(command);
//                    if (!proc.isEmpty()) {
//                        System.out.println("sending :" + "FAILURE @ " + incoming);
//                        talker("FAILED END (with upload).. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
//                    } else {
//                        System.out.println("sending :" + "SUCCESS @ " + incoming);
//                        talker("SUCCESS END on upload).. exiting @    " + (new Date()).toString(), masterAddress, masterJobPort);
//                    }
//                    
//                    break;
//                } else {
//                    System.out.println("incoming: " + incoming);
//                    command = incoming.split(";");
//                    t3 = System.currentTimeMillis();
//                    wr1.write((new Date()).toString());
//                    proc = executeBashScript(command);
//                    wr1.newLine();
//                    wr1.write(incoming);
//                    wr1.newLine();
//                    t4 = System.currentTimeMillis();
//                    wr1.write(String.valueOf(t4 - t3));
//                    wr1.newLine();
//                    wr1.write((new Date()).toString());
//                    wr1.newLine();
//                    wr1.newLine();
//                }
//                if (!proc.isEmpty()) {
//                    System.out.println("sending :" + "FAILURE @ " + incoming);
//                    talker("FAILURE @ " + incoming + "  " + proc, masterAddress, masterJobPort);
//                } else {
//                    System.out.println("sending :" + "SUCCESS @ " + incoming);
//                    talker("SUCCESS @ " + incoming, masterAddress, masterJobPort);
//                }
//            }
//
//        }
