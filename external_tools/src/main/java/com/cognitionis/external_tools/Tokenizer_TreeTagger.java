package com.cognitionis.external_tools;

import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;

/**
 * Tokenizer.java
 * @author Hector Llorens
 * @since Oct 27, 2011
 */
public class Tokenizer_TreeTagger {

    private static String program_path = FileUtils.getApplicationPath() + "program-data"+File.separator+ "tokenizer"+File.separator;
    private static String program_bin = program_path + "tokenize.pl";
    private static String abbrv = program_path + "english-abbreviations";


    /**
     * Runs the tokenizer over a file and saves file in .tok
     *
     * @param filename
     * @return Output filename
     */
    public static String run(String filename) {
        execute(filename, filename + ".tok");
        return filename + ".tok";
    }

    private static void execute(String filename, String outputfile) {
        try {
            // REMEMBER: DO NOT USE LINUX COMMANDS make it multi-platform
            // | sed 's/^\\([^-]\\+\\)-\\([^-]\\+\\)$/\\1\\n-\\n\\2/'
            System.out.println(program_bin);
            String[] command = {"/bin/sh", "-c","perl "+program_bin+" -e -a \""+abbrv+"\" \"" + filename +"\" "};
            Process p = Runtime.getRuntime().exec(command);

            String line;
            BufferedWriter output = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                while ((line = stdInput.readLine()) != null) {
                            line=line.replaceAll("\\s+", " ").trim().replaceAll("\\|", "-");
                            if (line.length()>0) {
                                        if(line.matches("[^-]+-[^-]+")){
                                            //System.out.println("split -:"+line + "\n");
                                            line=line.replaceAll("-", "\n-\n");
                                            //System.out.println(line + "\n");
                                        }
                                        output.write(line + "\n");
                                        /* uncomment to see results
                                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                            System.out.println(line + "\n");
                                        }*/
                            }
                }
                // if you use debug you will see Error output as well
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    stdInput=new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((line = stdInput.readLine()) != null) {
                        System.out.println(line + "\n");
                    }
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
            System.err.println("Errors found (TreeTagger):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }

        }


    }


}
