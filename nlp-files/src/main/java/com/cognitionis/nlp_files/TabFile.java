package com.cognitionis.nlp_files;

import java.io.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */

public class TabFile  extends NLPFile {

    public Boolean isWellFormed;

    public TabFile(String filename) {
        super(filename);
        isWellFormed = false;
    }

    @Override
    public Boolean isWellFormatted() {

        try {

            // read pipesFile line by line check that at least it have X piped cols
            BufferedReader reader = new BufferedReader(new FileReader(this.f));
            try {
                String line = null;
                int linen = 0;
                int columns=0;
                while ((line = reader.readLine()) != null) {
                    linen++; //System.getProperty("line.separator")
                    if (line.length() > 0 && !line.contains("\t")) {
                        throw new Exception("Line " + linen + " is not valid pipesFile line: Has contet without tabs");
                    }
                    if(columns==0){                   columns=line.split("\t").length;                    }
                    if (line.length() > 0 && line.split("\t").length != columns) {
                            throw new Exception("Line " + linen + " is not valid pipesFile line: Has less columns (" + line.split("\t").length + ") than other lines (" + columns + "): "+line);
                    }
                    if(linen>100){break;}
                }

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }

        this.isWellFormed = true;
        return true;


    }



    public String getPipesFile(){

        String outputfile=null;
        try {

            outputfile=this.f+".pipes";
            BufferedReader reader = new BufferedReader(new FileReader(this.f));
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    line=line.replaceAll("\t", "|");
                    outfile.write(line+"\n");
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        return outputfile;

    }

    @Override
    public String toPlain(String filename) {
        throw new UnsupportedOperationException("toPlain not applicable to this type of file");
    }
}
