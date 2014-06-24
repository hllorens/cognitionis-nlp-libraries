package com.cognitionis.feature_builder;

import java.io.*;
import java.util.*;
import com.cognitionis.nlp_files.*;
import com.cognitionis.utils_basickit.XmlAttribs;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class Timen {

    public static String get_timen(String features_and_attributes, String lang) {
        String output;
        PipesFile nlpfile = new PipesFile(features_and_attributes);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        output = getTimenFormat((PipesFile) nlpfile, new Locale(lang));

        return output;
    }

    public static String getTimenFormat(PipesFile pipesfile, Locale l) {
        String outputfile = null;
        int numline = 0;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + ".TempEval-classik-features";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));


            HashMap<String, String[]> DCTs = TempEvalFiles.getDCTsFromTab(pipesfile.getFile().getParent() + "/dct.tab");

            int iob2col = pipesfile.getColumn("element\\(IOB2\\)");
            int attrscol = iob2col + 1;
            int filecol = pipesfile.getColumn("file");
            int tokencol = pipesfile.getColumn("(word|token)");
            int tensecol = pipesfile.getColumn("tense");

            String file ="";
            String word = "";
            String id = "";
            String tense = "";

            if (iob2col == -1 || tokencol == -1) {
                String notFoundCol = "";
                if (iob2col == -1) {
                    notFoundCol += "element,attribs,";
                }
                if (tokencol == -1) {
                    notFoundCol += "word/token,";
                }
                throw new Exception("Some of the required columns (element,word/token,POS) not found: " + notFoundCol);
            }




            String pipeslineant = "--prior first line--";

            try {
                String line;
                String[] linearr;
                HashMap<String, String> tempexAttribsHash = null;
                while ((line = pipesreader.readLine()) != null) {
                    numline++;
                    linearr = line.split("\\|");

                    if (linearr.length >= pipesfile.getPipesDescArrCount()) {
                        if (linearr[iob2col].matches("B-.*")) {
                            if (!word.equals("")) {
                                outfile.write(id + "|" + word + "|" + tense + "|"+DCTs.get(file)[0]+"\n");
                                id = "";
                                word = "";
                                tense = "";
                            }

                            word = linearr[tokencol];
                            tense = linearr[tensecol];
                            file = linearr[filecol];
                            if (linearr[attrscol].matches(".*=.*=.*") && !linearr[attrscol].contains(";")) {
                                tempexAttribsHash = XmlAttribs.parseXMLattrs(linearr[attrscol]);
                            } else {
                                tempexAttribsHash = XmlAttribs.parseSemiColonAttrs(linearr[attrscol]);
                            }
                            id = tempexAttribsHash.get("tid");
                        }

                        if (linearr[iob2col].matches("I-.*")) {
                            if (word.equals("")) {
                                throw new Exception("Malformed annotation: " + line + "\n Prev: " + pipeslineant);
                            }
                            word += "_" + linearr[tokencol];
                        }

                    }
                    pipeslineant = line;
                }

                if (!word.equals("")) {
                    outfile.write(id + "|" + word + "|" + tense + "|"+DCTs.get(file)[0]+"\n");

                }


            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (CLASSIK):\n\t" + e.toString() + "\n");


            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);


            }
            return null;


        }
        return outputfile;


    }
}
