package com.cognitionis.nlp_files;


import java.io.*;
import java.util.regex.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class TreebankFile extends NLPFile {

    public TreebankFile(String filename) {
        super(filename);
    }
    
    @Override
    public Boolean isWellFormatted() {
        int par_level = 0;


        try {
            if (super.getFile()==null) {
                throw new Exception("No file loaded in NLPFile object");
            }
            BufferedReader reader = new BufferedReader(new FileReader(this.f));
            try {
                String line = null;
                int linen = 0;
                Pattern p = Pattern.compile("[\\(\\)]");

                while ((line = reader.readLine()) != null) {
                    linen++; //System.getProperty("line.separator")
                    if (line.matches("\\s*[^\\(\\s].*")) {
                        throw new Exception("Treebank format error: line " + linen + " not begining with \\s*(");
                    }
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        if (m.group().equals("(")) {
                            par_level++;
                        } else {
                            par_level--;
                            if (par_level < 0) {
                                throw new Exception("Treebank format error: par_level lower than 0");
                            }
                        }
                    }
                    //System.out.println(linen+": "+line+" - parlevel="+ par_level);

                }
            } finally {
                    reader.close();
            }

            if (par_level != 0) {
                throw new Exception("Treebank format error: positive unbalancement, par_level=" + par_level);
            }

        } catch (Exception e) {
            System.err.println("Errors found ("+this.getClass().getSimpleName()+"):\n\t" + e.toString() + "\n");
            if(System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")){e.printStackTrace(System.err);}
            return false;
        }

        return true;
    }



    public String toPlain(String filename){
        // one token, one space, one token, one space... (end of sentence -> \n)
        return this.getFile().toString();
    }







}
