/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcloutputreader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author thanos
 */
public class MCLOutputReader {

    /**
     * @param args the command line arguments
     */
    public static String replaceThanos(String in,String org, String rep) {
        
        char[] inChar=in.toCharArray();
        char[] orgChar=org.toCharArray();
        char[] repChar=rep.toCharArray();
        int pos=0;
        int flag=0;
        int lng=inChar.length;
        while( pos<lng)
        {
            if(inChar[pos]==orgChar[1])
            {
                pos++;
                flag=1;
                for(int j=0;j<orgChar.length-1;j++)
                {
                    //if()
                }
            }
        }
  

        return in;
    }

    public static String[] splitThanos(String in, String del) {
        int size = del.length();
        int tmp;
        int lastpos = 0;
        //String t;
        ArrayList<String> list = new ArrayList<>();
        while ((tmp = in.indexOf(del, lastpos)) != -1) {
            list.add(in.substring(lastpos, tmp));
            lastpos = tmp + size;
        }
        in = in.substring(lastpos, in.length());
        if (in.length() > 0) {
            list.add(in);
        }
        return list.toArray(new String[list.size()]);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        String organismsInputFile = "/home/thanos/Dropbox/NetBeansProjects/odysseys/variousInputFiles/98TreeOrder.txt";
        String outCounts = "/home/thanos/Dropbox/NetBeansProjects/odysseys/variousInputFiles/MCLoutCounts";
        String outList = "/home/thanos/Dropbox/NetBeansProjects/odysseys/variousInputFiles/MCLoutList";

        List<String> lines = Files.readAllLines(Paths.get(organismsInputFile), StandardCharsets.UTF_8);
        HashMap<String, Integer> organisms = new HashMap<>();
        int id = 0;
        for (String line : lines) {
            String tmp = line.substring(0, line.indexOf(":"));
            //System.out.println(tmp);
            organisms.put(tmp, id);
            id++;
        }

        String inputFile = "/home/thanos/Fotis/plantsProject/MCL98PlantsOutput/Outsimple.mcl";
        BufferedReader r1 = new BufferedReader(new FileReader(new File(inputFile)));
        String line;

        line = r1.readLine();

        String l1 = "allallall";
        l1=l1.replace("allall", "X");
        System.out.println(l1);

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            //System.out.println(line);
            //String[] tmp = splitThanos(line, "\t");//line.split("\t");
            String[] tmp1 = line.split("\t");
            //System.out.println(line);
//            if (tmp.length != tmp1.length) {
//                System.out.println("FATAL ERROR, Sizes do not match");
//                break;
//            }
//            for (int j = 0; j < tmp.length; j++) {
//                if(!tmp[j].equals(tmp1[j]))
//                {
//                    System.out.println("Mismatch detected...!!!!!!!!!!!!!!!");
//                    System.out.println(tmp[j]+" VS "+tmp1[j]);
//                    break;
//                }
//                
//            }
        }

//        BufferedWriter counts_wr=new BufferedWriter(new FileWriter(new File(outCounts)));
//        BufferedWriter list_wr=new BufferedWriter(new FileWriter(new File(outList)));
//        int[] counts= new int[organisms.size()];
//        String[] lists= new String[organisms.size()];
//        int t2=0;
//        
//        while ((line = r1.readLine()) != null) {
//            //System.out.println(line);
//            line = line.substring(line.indexOf(" ") + 1, line.length());
//            String[] tmp = line.split("\t");
//            
//            //System.out.println(Arrays.toString(tmp));
//            for(int i=0;i<organisms.size();i++)
//            {
//                counts[i]=0;
//                lists[i]="";
//            }           
//            for (int i = 0; i < tmp.length; i++) {                
//                tmp[i] = tmp[i].substring(0, tmp[i].indexOf("_01_") + 3);
//                if(!organisms.containsKey(tmp[i]))
//                {
//                    System.out.println("KEY LOST "+tmp[i]);
//                }
//                else
//                {
//                    t2=organisms.get(tmp[i]);
//                    counts[t2]=counts[t2]+1;
//                    lists[t2]=lists[t2]+tmp[i]+",";
//                    
//                    //}
//                    //lists[t2]=lists[t2].substring(0,lists[t2].length()-1)+" "+tmp[i]+"}";
//                }
//                //System.out.println(tmp[i]);
//            }
//            //System.out.println(Arrays.toString(counts));
//            String tmpCount="";
//            String tmpList="";
//            for(int i=0;i<organisms.size();i++)
//            {
//                //list_wr.write("{"+lists[t2]);
//                //counts_wr.write(tmpCount);
//                tmpCount=tmpCount+counts[i]+" ";
//                if(lists[i].length()>0)
//                {
//                    lists[i]=lists[i].substring(0,lists[i].length()-1);
//                }
//                tmpList=tmpList+"{"+lists[i]+"} ";
//                //counts[i]=0;
//                //lists[i]="{}";
//            } 
//            
//            tmpCount=tmpCount.substring(0,tmpCount.length()-1);
//            tmpList=tmpList.substring(0,tmpList.length()-1);
//            counts_wr.write(tmpCount);
////counts_wr.write(Arrays.toString(counts));
//            counts_wr.newLine(); 
//            //list_wr.write(Arrays.toString(lists));
//            list_wr.write(tmpList);
//            list_wr.newLine();
//        }
//        counts_wr.close();
//        list_wr.close();
        ////end here
        System.out.println("Time: " + (System.currentTimeMillis() - t1));
    }

}
