package com.cognitionis.external_tools;

import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;

/**
 * ACCEPTANCE OF THE MALTPARSER LICENSE REQUIRED
 * @author Hector Llorens
 * @since 2011
 */
public class MaltParser {

    private static String program_path = FileUtils.getApplicationPath() + "program-data/MaltParser/";
    private static String program_bin = program_path + "malt.jar";
    private static String program_model = "http://w3.msi.vxu.se/users/jha/maltparser/mco/english_parser/engmalt.mco"; // par stands for parameter file



    /**
     * Runs MaltParser over a ConLLX formated String tab-separated (one token per line)
     *
     * Format: num  token    lemma   POS    POS     _
     *
     * @param conll_phrase
     * @return Output filename
     */
    public static String run(String conll_phrase) {
        String output="";

        try{
            String inputfile=program_path+"malt_input.conll";

            //Save string in inputfile
            BufferedWriter infilewriter = new BufferedWriter(new FileWriter(inputfile));
            try {
                infilewriter.write(conll_phrase);
            } finally {
                if (infilewriter != null) {
                    infilewriter.close();
                }
            }

            //Delete outputfile if exists

            File f = new File(inputfile+".out");
            if(f.exists()){
                if(f.isFile() && f.canWrite()){
                    if(!f.delete()){
                        throw new Exception("Deleting MaltParser output file: "+inputfile+".out");
                    }
                }else{
                    throw new Exception("Invalid MaltParser output file: "+inputfile+".out");
                }
            }
            //execute with inputfile to outputfile
            execute(inputfile, inputfile+".out");
            f=new File(inputfile);
            f.delete();

            // Read outputfile in a String
            String line;
            BufferedReader freader = new BufferedReader(new FileReader(inputfile+".out"));
            try {
                while ((line = freader.readLine()) != null) {
                        output+=line+"\n";
                }
            } finally {
                if (freader != null) {
                    freader.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (MaltParser):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return "";
        }

        // Return that string
        return output;
    }

    /**
     * Returns the position of the unique head of a phrase
     * If there is a multi-word head (San Francisco) it will
     * return only Francisco as head so it has to be handled
     * externally (i.e., NNS NNS sequences are mutli-word entities)
     *
     * @param conll_phrase
     * @return
     */
    public static int getHeadPosition(String conll_phrase){
        //System.err.println("\n"+conll_phrase+"\n---");
        String output=run(conll_phrase);
        int headposition=0;
        //System.err.println(output);
        try{
            // Read output line by line
            String[] output_arr=output.split("\n");
            for(int i=0;i<output_arr.length;i++){
                if(output_arr[i].matches(".*\t0\tROOT\t.*")){
                    headposition=i;
                    //break;
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (MaltParser):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return 0;
        }
        return headposition;
    }


    private static void execute(String inputfile, String outputfile) {
        try {
            /*String[] command = {"rm", "-rf", program_model};
            Process p = Runtime.getRuntime().exec(command);*/
            //command = new String[]{"java", "-Xmx1024m", "-jar", program_bin, "-c", program_model, "-i", inputfile, "-o", outputfile, "-m", "parse"};
            String [] command = new String[]{"java", "-Xmx1024m", "-jar", program_bin, "-u", program_model, "-i", inputfile, "-o", outputfile, "-m", "parse"};
            Process p = Runtime.getRuntime().exec(command);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            System.err.println("ERRORS: ");
             while ((line = stdError.readLine()) != null) {
                 System.err.println("\t"+line);
             }
            System.err.println("OUTPUT: ");
             while ((line = stdInput.readLine()) != null) {
                 System.err.println("\t"+line);
             }
                if(p!=null){
                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();
                    p.destroy();
                }


        } catch (Exception e) {
            System.err.println("Errors found (MaltParser) execution:\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }

        }


    }





}
