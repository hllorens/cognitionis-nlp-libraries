/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cognitionis.wiki_basickit;

import java.io.*;

/**
 *
 * @author hector
 */
public class DBpedia_bk {

    public void create_name_numlinks(String sortedNames, String pagelinks){
        try{
            BufferedReader namesreader = new BufferedReader(new FileReader(sortedNames));
            BufferedReader linksreader = new BufferedReader(new FileReader(pagelinks));

            long linen = 0;
            String linksline="";
            String nameline="";
            String[] linksarr = null;
            while ((linksline = linksreader.readLine()) != null) {
                linksarr=linksline.split(": ");
                long linknum=Long.parseLong(linksarr[0]);
                long numlinks=linksarr[1].split(" ").length;
                while (linen!=linknum) {
                    linen++;
                    if((nameline = namesreader.readLine()) == null){
                        throw new Exception("No name for pagelinks: "+linksline);
                    }
                }
                System.out.println(nameline+" "+numlinks);
            }
        } catch (Exception e) {
            System.err.println("Errors found :\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }
    }
    

}
