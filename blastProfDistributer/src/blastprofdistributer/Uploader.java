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
package blastprofdistributer;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Uploader extends Thread {

    ServerSocket jobSocket;
    Socket server;
    DataInputStream in;

    public Uploader(Integer sockid) throws IOException {
        this.jobSocket = new ServerSocket(sockid);

    }

    public void executeCommandLD(String command) {

        //StringBuffer output = new StringBuffer();
        Process p;
        try {
            //command=command+" 1>/dev/null";
            String[] tc = {"sh", "-c", command};
            p = Runtime.getRuntime().exec(command);
            p.waitFor();

//            BufferedReader reader
//                    = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//            String line = "";
//            while ((line = reader.readLine()) != null) {
//                output.append(line + "\n");
//            }
            p.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // return output.toString();
        // return "nothing";
    }

    public String waitForCommand() {
        //while (true) {
        try {
            //System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            server = jobSocket.accept();
            //System.out.println("Just connected to " + server.getRemoteSocketAddress());
            //String sender = server.getRemoteSocketAddress().toString();
            //System.out.println(sender);
            DataInputStream in = new DataInputStream(server.getInputStream());
            String message = in.readUTF();

            //server.close();
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

    public void run() {

        try {
            //DataInputStream in = new DataInputStream(server.getInputStream());
            
            this.server = jobSocket.accept();
            this.in = new DataInputStream(server.getInputStream());

            while (true) {
                String command = in.readUTF();
                if (command.contains("done")) {
                    server.close();
                    break;
                } else {
                    System.out.println("UPPER GOT command " + command);
                    executeCommandLD(command);
                }

                //buildProfile();
                // run upload script
            }
        } catch (IOException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
