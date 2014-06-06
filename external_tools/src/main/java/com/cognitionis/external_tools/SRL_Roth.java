package com.cognitionis.external_tools;

import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;

/**
 * REGULAR INSTALLATION AND INCLUSION IN PATH REQUIRED
 * @author Hector Llorens
 * @since 2011
 */
public class SRL_Roth {

    private static String program_path = FileUtils.getApplicationPath() + "program-data/SRL_Roth/";
    private static String program_bin = program_path + "srl-client-primitive.pl";
    private static String program_bin2 = program_path + "roth_to_conll";

    /**
     * Runs SRL_Roth over plain text.
     * Recommended: One sentence\n\n
     * and saves the output in a .roth file (PipesFile)
     *
     * Format Token|POS|...
     *
     * @param filename
     * @return Output filename
     */
    public static String run(String filename) {
        return run(filename, 1);
    }

    /**
     * Runs SRL_Roth over a plain or tokenized file: one token one blank & one sentenc per line
     * and saves the output in a .roth file (PipesFile)
     *
     * tokenize: (0) not tokenize, (1) tokenize
     *
     * Format Token|POS|...
     *
     * @param filename
     * @param tokenize
     * @return Output filename
     */
    public static String run(String filename, int tokenize) {
        String outputfile = filename + ".roth";
        try {
            // IMP: It is better to let UTF-8 fail than translate to crappy ISO that misses some chars
            //      then things like " – " are imposible to pair because tokens are missing and there are spaces in the middle
            // IMP2: Since the other solution is even worse... we stay with ISO-translation and break if unpairable...
            String[] command = {"/bin/sh","-c","cat \""+filename+"\" | sed \"s/[|]/-/g\" | sed \"s/\\([^[:blank:]]\\)-\\([^[:blank:]]\\)/\\1 - \\2/g\" | iconv -c -t iso-8859-1 | perl \""+program_bin+"\" "+String.valueOf(tokenize)+" 1 | "+program_bin2+" -f rothcomplete | iconv -c -f iso-8859-1 | sed \"s/^[[:blank:]]*\\$/|/\""};
            //String[] command = {"/bin/sh","-c","cat \""+filename+"\" | "+program_bin+" "+String.valueOf(tokenize)+" 1 | "+program_bin2+" -f rothcomplete | sed \"s/^[[:blank:]]*\\$/|/\""};
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
