package com.cognitionis.external_tools;

import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;

/**
 *
 * @author Hector Llorens
 * @since 2011
 */
public class CoNLL_scorer {
    private static String program_path = FileUtils.getApplicationPath() + "program-data/CoNLL_scorer/conlleval.pl";

    public static String score(String file) {
        return score(file,"\\|","");
    }

    public static String score(String file, String delimiter,String options) {
        String outputfile = file + ".score";
        try {
            // CREATE APROPRIATE INPUT FORMAT
            String[] command = {"/bin/sh","-c","perl \""+program_path+"\" -d \""+delimiter+"\" "+options+" < "+file+" > "+outputfile};
            Process p=Runtime.getRuntime().exec(command);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                String line;
                while ((line = stdInput.readLine()) != null) {
                    System.err.println(line);
                }
            } finally {
                if (stdInput != null) {
                    stdInput.close();
                }
                if(p!=null){
                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();
                    p.destroy();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (CoNLL Scorer):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }


    public static void printscore(String file) {
        printscore(file,"\\|","");
    }

    public static void printscore(String file, String delimiter,String options) {
        try {
            // CREATE APROPRIATE INPUT FORMAT
            String[] command = {"/bin/sh","-c","perl \""+program_path+"\" -d \""+delimiter+"\" "+options+" < "+file};
            Process p=Runtime.getRuntime().exec(command);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                String line;
                while ((line = stdInput.readLine()) != null) {
                    System.out.println(line);
                }
            } finally {
                if (stdInput != null) {
                    stdInput.close();
                }
                if(p!=null){
                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();
                    p.destroy();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (CoNLL Scorer):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

}
