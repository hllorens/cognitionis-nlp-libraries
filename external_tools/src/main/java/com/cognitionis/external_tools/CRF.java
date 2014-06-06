package com.cognitionis.external_tools;

import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;
        
/**
 * REGULAR INSTALLATION AND INCLUSION IN PATH REQUIRED
 * @author Hector Llorens
 * @since 2011
 */
public class CRF {
    // path is not necessary but is used to capture temporal files (if there are), or default templates
    public static String program_path = FileUtils.getApplicationPath() + "program-data/CRF++/";
    /**
     * Runs CRF++ over a features file given a template
     * and saves a model in a .CRFmodel file
     *
     * The template file must be in the same path or in program-data/CRF++ or in program-data/CRF++/templates/
     *
     * Format | | | | pipes
     *
     * @param filename
     * @param template
     * @return Output filename
     */
    public static String train(String featuresfile, String templatefile) {
        // -p number of processors
        // -c hyperparameter
        String outputfile = featuresfile + "." + templatefile.substring(0, templatefile.lastIndexOf('.')) + ".CRFmodel";
        try {
            File tempf = new File(templatefile);
            if (!tempf.exists() || !tempf.isFile()) {
                tempf = new File(program_path + templatefile);
                if (!tempf.exists() || !tempf.isFile()) {
                    tempf = new File(program_path + "templates/" + templatefile);
                    if (!tempf.exists() || !tempf.isFile()) {
                        throw new Exception("Template file (" + templatefile + ") not found.");
                    } else {
                        templatefile = program_path + "templates/" + templatefile;
                    }
                } else {
                    templatefile = program_path + templatefile;
                }
            }

            // CREATE APROPRIATE INPUT FORMAT
            String[] command = {"/bin/sh", "-c", "tr \"|\" \" \" < " + featuresfile + " | sed \"s/^[[:blank:]]*\\$//\" > " + program_path + "temp.tmp"};
            Process p = Runtime.getRuntime().exec(command);
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


            String[] command2 = {"crf_learn", "-c", "1.0", "-p", "2", templatefile, program_path + "temp.tmp", outputfile};
            //System.err.println("\ncrf_learn -c 1.0 -p 2 " + templatefile + " " + featuresfile+" "+outputfile+"\n");
            p = Runtime.getRuntime().exec(command2);
            
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
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

            // CLEARTMP
            String[] command3 = {"/bin/sh", "-c", "rm -rf " + program_path + "*.tmp"};
            p = Runtime.getRuntime().exec(command3);
            
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
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
            System.err.println("Errors found (CRF++):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;

    }

    /**
     * Runs CRF++ over a features file given a model
     * and saves the output as input-annotatedWith-CRFmodel-x file
     *
     * The model must be in the same path or in program-data/CRF++
     *
     * Format | | | | pipes
     *
     * @param filename
     * @param template
     * @return Output filename
     */
    public static String test(String featuresfile, String modelfile) {
        int folderposition = modelfile.lastIndexOf('/');
        String outputfile = featuresfile + "-annotatedWith-CRFmodel-" + modelfile.substring(folderposition + 1, modelfile.lastIndexOf('.'));
        try {
            File tempf = new File(modelfile);
            if (!tempf.exists() || !tempf.isFile()) {
                tempf = new File(program_path + modelfile);
                if (!tempf.exists() || !tempf.isFile()) {
                    tempf = new File(program_path + "models/" + modelfile);
                    if (!tempf.exists() || !tempf.isFile()) {
                        throw new Exception("Template file (" + modelfile + ") not found.");
                    } else {
                        modelfile = program_path + "models/" + modelfile;
                    }
                } else {
                    modelfile = program_path + modelfile;
                }
            }


            // CREATE APROPRIATE INPUT FORMAT
            String[] command = {"/bin/sh", "-c", "tr \"|\" \" \" < " + featuresfile + " | sed \"s/^[[:blank:]]*\\$//\" > " + program_path + "temp.tmp"};
            Process p = Runtime.getRuntime().exec(command);
            
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


            // EXECUTE CRF MODEL
            String[] command2 = {"crf_test", "-m", modelfile, program_path + "temp.tmp", "-o", program_path + "temp2.tmp"};
            p = Runtime.getRuntime().exec(command2);
            
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
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

            // CREATE APPROPRIATE OUTPUT FORMAT
            String[] command3 = {"/bin/sh", "-c", "tr \"\t\" \"|\" < " + program_path + "temp2.tmp" + " | sed '/^[[:blank:]]*$/d' >" + outputfile};
            p = Runtime.getRuntime().exec(command3);
            
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
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

            // CLEARTMP
            String[] command4 = {"/bin/sh", "-c", "rm -rf " + program_path + "*.tmp"};
            p = Runtime.getRuntime().exec(command4);
            
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
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
            System.err.println("Errors found (CRF++):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }
}
