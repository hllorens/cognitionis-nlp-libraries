package com.cognitionis.external_tools;

import java.io.*;
import com.cognitionis.utils_basickit.*;

/**
 * REGULAR INSTALLATION AND INCLUSION IN PATH REQUIRED
 * @author Hector Llorens
 * @since 2011
 */
public class FreeLing {

    // path for configurations, temporal files (if there are), or default templates
    public static String program_path = FileUtils.getApplicationPath() + "program-data/FreeLing/";



    /**
     * Runs Freeling over plain text.
     * Recommended: One sentence per line
     * and saves the output in a .freeling file (PipesFile)
     *
     * Default: Spanish and tokenize
     *
     * Format Token|POS|...
     *
     * @param filename
     * @return Output filename
     */
    public static String run(String filename) {
        return run(filename, "es", 1);
    }


    public static String run(String filename, String lang, int tokenize) {
        String outputfile = filename + ".freeling";
        try {
            String[] command=new String[3];
            command[0]="/bin/sh";
            command[1]="-c";
            // Freeling is rule based and uses ISO internally so needs ISO as input...
            if(tokenize==0){
                command[2]="cat \""+filename+"\" | tr \"|\" \"-\" | iconv -c -t iso-8859-1 | analyze -f "+program_path+lang.toLowerCase()+".cfg --inpf token | iconv -c -f iso-8859-1 | cut -f 1-3 -d \" \" | tr \" \" \"|\" | sed \"s/^[[:blank:]]*\\$/|/\"";
            }else{
                command[2]="cat \""+filename+"\" | tr \"|\" \"-\" | iconv -c -t iso-8859-1 | analyze -f "+program_path+lang.toLowerCase()+".cfg --inpf plain | iconv -c -f iso-8859-1 | cut -f 1-3 -d \" \" | tr \" \" \"|\" | sed \"s/^[[:blank:]]*\\$/|/\"";
            }

            // UTF-8 works but tagging does not work properly
            /*if(tokenize==0){
                command[2]="cat \""+filename+"\" | tr \"|\" \"-\" | analyze -f "+program_path+lang.toLowerCase()+".cfg --inpf token | cut -f 1-3 -d \" \" | tr \" \" \"|\" | sed \"s/^[[:blank:]]*\\$/|/\"";
            }else{
                command[2]="cat \""+filename+"\" | tr \"|\" \"-\" | analyze -f "+program_path+lang.toLowerCase()+".cfg --inpf plain | cut -f 1-3 -d \" \" | tr \" \" \"|\" | sed \"s/^[[:blank:]]*\\$/|/\"";
            }*/

            //System.out.println(command[2]);

            Process p = Runtime.getRuntime().exec(command);
            
            BufferedWriter output = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                String line;
                while ((line = stdInput.readLine()) != null) {
                    output.write(line + "\n");
                }
            } finally {
                if (stdInput != null) {
                    stdInput.close();
                }
                if (output != null) {
                    output.close();
                }
                if(p!=null){
                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();
                    p.destroy();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (SRL_Roth):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;

    }




}


