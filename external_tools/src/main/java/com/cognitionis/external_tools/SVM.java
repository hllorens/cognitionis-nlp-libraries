package com.cognitionis.external_tools;


import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;

/**
 * REGULAR INSTALLATION AND INCLUSION IN PATH REQUIRED
 * @author Hector Llorens
 * @since 2011
 */
public class SVM {

    // path is not necessary but is used to capture temporal files (if there are), or default templates
    public static String program_path = FileUtils.getApplicationPath() + "program-data/SVM/";


    /**
     * Runs SVM over a features file
     * and saves a model in a .SVMmodel file
     *
     * Format 
     *
     * @param filename
     * @return Output filename
     */
    public static String train(String featuresfile, String templatefile) {
        String outputfile = featuresfile +"."+ templatefile.substring(0,templatefile.lastIndexOf('.')) +".SVMmodel";
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
           // add a blank line at the end
            String[] command = {"/bin/sh","-c","tr \"|\" \" \" < "+featuresfile+" | sed '${/^$/!s/$/\\n/;}' > "+program_path + "temp.tmp"};
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

            //System.err.println("\n"+program_path +"myTrain.sh "+templatefile+" "+program_path + "temp.tmp"+" "+outputfile+"\n");
            String[] command2 = {"/bin/sh","-c","sh "+program_path +"myTrain.sh "+templatefile+" "+program_path + "temp.tmp"+" "+outputfile};
            p=Runtime.getRuntime().exec(command2);
            
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
            String[] command3 = {"/bin/sh","-c","rm -rf "+program_path +"*.tmp"};
            p=Runtime.getRuntime().exec(command3);
            
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
            System.err.println("Errors found (SVM):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;

    }


    public static String test(String featuresfile, String modelfile) {
        int folderposition=modelfile.lastIndexOf('/');
        String outputfile = featuresfile + "-annotatedWith-SVMmodel-" + modelfile.substring(folderposition+1, modelfile.lastIndexOf('.'));
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
            String[] command = {"/bin/sh","-c","tr \"|\" \" \" < "+featuresfile+" | sed '${/^$/!s/$/\\n/;}' > "+program_path + "temp.tmp"};
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


            // EXECUTE CRF MODEL
            String[] command2 = {"/bin/sh","-c","yamcha -m "+modelfile+" < "+program_path + "temp.tmp > "+program_path + "temp2.tmp"};
            p=Runtime.getRuntime().exec(command2);
            
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
            // TODO (OJO EN UN MOMENTO DADO PUEDE INTERESSARME NO ELIMINAR LAS LINEAS EN BLANCO)
            String[] command3 = {"/bin/sh","-c","tr \"\t\" \"|\" < "+program_path + "temp2.tmp"+" | sed '/^[[:blank:]]*$/d' >" +outputfile};
            p=Runtime.getRuntime().exec(command3);
            
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
            String[] command4 = {"/bin/sh","-c","rm -rf "+program_path +"*.tmp"};
            p=Runtime.getRuntime().exec(command4);
            
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
            System.err.println("Errors found (SVM):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

}
