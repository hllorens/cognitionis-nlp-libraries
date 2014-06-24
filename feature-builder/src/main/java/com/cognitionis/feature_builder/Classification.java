package com.cognitionis.feature_builder;


import com.cognitionis.knowledgek.TIMEK.TIMEK;
import com.cognitionis.nlp_files.PipesFile;
import com.cognitionis.utils_basickit.XmlAttribs;
import java.io.*;
import java.util.*;


/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class Classification {


    public static void classik_clear_features(HashMap<String, String> features) {
        features.put("file", "-");  // the first
        features.put("sentN", "-"); // the first
        features.put("tokN", "-");  // the first
        features.put("word", "");  // combination
        features.put("pos", "-");   // combination
        features.put("lemma", "-"); // combination
        features.put("roleconf", "-"); // the first
        features.put("simpleroles", "-"); // the distinct
        features.put("depverb", "-"); // the first
        features.put("tense", "-");     // the first
        features.put("polarity", "-"); // the first
        features.put("mainphrase", "-"); // the distinct
        features.put("PPdetail", "-"); // the first
        features.put("wn", "-"); // the timerelatedone or the last
        features.put("element", "-"); // the first
    }

    public static String get_classik(String features_and_attributes, String lang) {
        String output;
        PipesFile nlpfile = new PipesFile(features_and_attributes);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        output = getClassikFormat((PipesFile) nlpfile, new Locale(lang));

        return output;
    }











    public static String getClassikFormat(PipesFile pipesfile, Locale l) {
        String outputfile = null;
        int numline = 0;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + ".TempEval-classik-features";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            TIMEK timek = new TIMEK(l);

            HashMap<String, String> features = new HashMap<String, String>();

            int iob2col = pipesfile.getColumn("element\\(IOB2\\)");
            int attrscol = iob2col + 1;
            int tempevalfilecol = pipesfile.getColumn("file");
            int tokencol = pipesfile.getColumn("(word|token)");
            int POScol = pipesfile.getColumn("(POS|pos)");
            int lemmacol = pipesfile.getColumn("lemma");
            int roleconfcol = pipesfile.getColumn("roleconf");
            int simplerolescol = pipesfile.getColumn("simplerolesIOB2");
            int tensecol = pipesfile.getColumn("tense");
            int ppdetailcol = pipesfile.getColumn("PPdetail");
            int depverbcol = pipesfile.getColumn("depverb");
            int polaritycol = pipesfile.getColumn("assertype");
            int mainphrasecol = pipesfile.getColumn("iobmainphrase");
            int wncol = pipesfile.getColumn("wn");
            Boolean attribsCheck = false;


            if (iob2col == -1 || tokencol == -1 || POScol == -1) {
                String notFoundCol = "";
                if (iob2col == -1) {
                    notFoundCol += "element,attribs,";
                }
                if (tokencol == -1) {
                    notFoundCol += "word/token,";
                }
                if (POScol == -1) {
                    notFoundCol += "POS,";
                }
                throw new Exception("Some of the required columns (element,word/token,POS) not found: " + notFoundCol);
            }


            String pipesline;
            String[] pipesarr = null;
            String tempexNorm = "";
            String tempexPattern = "";
            classik_clear_features(features);

            HashMap<String, String> tempexAttribsHash = null;
            String pipeslineant = "--prior first line--";



            ArrayList<String> sentence = null;
            HashMap<String, String> VerbeventDep = null;

            try {
                String curr_fileid = "";
                String curr_sentN = "";
                String line;
                String[] linearr;
                int numsent = 0;
                while ((line = pipesreader.readLine()) != null) {
                    numline++;
                    linearr = line.split("\\|");
                    if (curr_fileid.equals("")) {
                        curr_fileid = linearr[0];
                    }
                    if (curr_sentN.equals("")) {
                        curr_sentN = linearr[1];
                    }
                    //System.out.println(curr_fileid+" "+curr_sentN+" "+linearr[0]+" "+linearr[1]+"\n");
                    if (curr_fileid.equals(linearr[0]) && curr_sentN.equals(linearr[1])) {
                        //System.out.println(curr_fileid+" adding "+curr_sentN+"\n");
                        if (sentence == null) {
                            sentence = new ArrayList();
                            VerbeventDep = new HashMap<String, String>();
                        }
                        sentence.add(line);

                        // GUARDAR VERB-EVENT-A1 DEPENDENCIES
                        // ARRAY AMB TOTS ELS VERBS Q TINGUEN EVENTS A1
                        //pipesarr = line.split("\\|");
                        if (line.endsWith("B-event") && !linearr[POScol].matches("(V.*|AUX)")) {
                            //System.out.println(linearr[lemmacol] + "       " + linearr[depverbcol] + "       " + line);
                            VerbeventDep.put(linearr[depverbcol], linearr[lemmacol] + "|" + linearr[POScol] + "|" + linearr[wncol] + "|" + linearr[simplerolescol]);
                        }
                    } else {
                        // update curr_markers
                        curr_fileid = linearr[0];
                        curr_sentN = linearr[1];

                        //System.out.println("Processing "+curr_fileid+" "+curr_sentN+" "+linearr[0]+" "+linearr[1]+"\n");
                        for (int numtok = 0; numtok < sentence.size(); numtok++) {
                            //System.out.println("processing token "+numtok+" size="+sentence.size());

                            pipesline = sentence.get(numtok);

                            pipesarr = pipesline.split("\\|");
                            if (!attribsCheck && pipesarr.length >= pipesfile.getPipesDescArrCount()) {
                                if (iob2col == pipesarr.length - 1) {
                                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                        System.err.println("No attribs found. Formating file for testing");
                                    }
                                } else {
                                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                        System.err.println("Attribs found. Formating file for training");
                                    }
                                    features.put("classik", "-");
                                }
                                attribsCheck = true;
                            }

                            if (pipesarr.length >= pipesfile.getPipesDescArrCount()) {
                                if (pipesarr[iob2col].matches("B-.*")) {
                                    if (!features.get("word").equals("")) {
                                        outfile.write(features.get("file") + "|" + features.get("sentN") + "|" + features.get("tokN") + "|" + features.get("word") + "|" + features.get("pos") + "|" + features.get("lemma") + "|" + features.get("roleconf") + "|" + features.get("simpleroles") + "|" + features.get("depverb") + "|" + features.get("tense") + "|" + features.get("polarity") + "|" + features.get("mainphrase") + "|" + features.get("PPdetail") + "|" + features.get("wn"));

                                        // specific features
                                        if (features.get("element").matches("(?i)timex.*")) {
                                            String normalizedTIMEK = timek.getNormTextandPattern(features.get("word"));
                                            String[] normalizedarr = normalizedTIMEK.split("\\|");
                                            tempexNorm = normalizedarr[0];
                                            tempexPattern = normalizedarr[1];
                                            String[] tempexarr = tempexNorm.split("_");
                                            String[] tempexNUarr = tempexPattern.split("_");
                                            String granularity = "-";
                                            String set_indicator = "0";
                                            if (tempexNorm.matches(timek.SET_re)) {
                                                set_indicator = "1";
                                            }
                                            if (tempexNUarr[tempexNUarr.length - 1].equalsIgnoreCase("TUNIT")) {
                                                granularity = "granul_date";
                                            }
                                            if (tempexarr[tempexarr.length - 1].matches(timek.TOD_re)) {
                                                granularity = "granul_time";
                                            }

                                            // normalizedtext | pattern | lastword|lastNU|lastwordgranularity|setinicator
                                            outfile.write("|" + tempexNorm + "|" + tempexPattern + "|" + tempexarr[tempexarr.length - 1] + "|" + tempexNUarr[tempexNUarr.length - 1] + "|" + granularity + "|" + set_indicator);
                                        } else {
                                            if (features.get("element").matches("(?i)event") && features.get("pos").matches("(V.*|AUX)") && VerbeventDep.containsKey(features.get("lemma"))) {
                                                outfile.write("|1|" + VerbeventDep.get(features.get("lemma"))+"|-");
                                            } else {
                                                outfile.write("|-|-|-|-|-|-");
                                            }
                                        }
                                        outfile.write("|" + features.get("element"));
                                        if (features.containsKey("classik")) {
                                            outfile.write("|" + features.get("classik"));
                                        }
                                        outfile.write("\n");
                                        tempexNorm = "";
                                        tempexPattern = "";
                                        classik_clear_features(features);
                                    }

                                    String element = pipesarr[iob2col].substring(2);
                                    if (tempevalfilecol != -1) {
                                        features.put("file", pipesarr[tempevalfilecol]);  // the first
                                        features.put("sentN", pipesarr[tempevalfilecol + 1]); // the first
                                        features.put("tokN", pipesarr[tempevalfilecol + 2]);  // the first
                                    }
                                    features.put("word", pipesarr[tokencol]);
                                    features.put("pos", pipesarr[POScol]);
                                    features.put("lemma", pipesarr[lemmacol]);
                                    features.put("roleconf", pipesarr[roleconfcol]);
                                    features.put("simpleroles", pipesarr[simplerolescol]);
                                    features.put("depverb", pipesarr[depverbcol]);
                                    features.put("tense", pipesarr[tensecol]);
                                    features.put("polarity", pipesarr[polaritycol]);
                                    features.put("mainphrase", pipesarr[mainphrasecol]);
                                    features.put("PPdetail", pipesarr[ppdetailcol]);
                                    features.put("wn", pipesarr[wncol]);
                                    features.put("element", element);


                                    if (features.containsKey("classik")) {
                                        if (pipesarr[attrscol].matches(".*=.*=.*") && !pipesarr[attrscol].contains(";")) {
                                            tempexAttribsHash = XmlAttribs.parseXMLattrs(pipesarr[attrscol]);
                                        } else {
                                            tempexAttribsHash = XmlAttribs.parseSemiColonAttrs(pipesarr[attrscol]);
                                        }
                                        if (pipesarr[iob2col].matches("(?i).*timex.*")) {
                                            features.put("classik", tempexAttribsHash.get("type"));
                                        } else {
                                            features.put("classik", tempexAttribsHash.get("class"));
                                        }
                                    }


                                }

                                if (pipesarr[iob2col].matches("I-.*")) {
                                    if (features.get("word").equals("")) {
                                        throw new Exception("Malformed annotation: " + pipesline + "\n Prev: " + pipeslineant);
                                    }
                                    features.put("word", features.get("word") + "_" + pipesarr[tokencol]);
                                    features.put("pos", features.get("pos") + "_" + pipesarr[POScol]);
                                    features.put("lemma", features.get("lemma") + "_" + pipesarr[lemmacol]);
                                    String[] roles = features.get("simpleroles").split("_");
                                    if (!roles[roles.length - 1].equals(pipesarr[simplerolescol])) {
                                        features.put("simpleroles", features.get("simpleroles") + "_" + pipesarr[simplerolescol]);
                                    }
                                    String[] phrases = features.get("mainphrase").split("_");
                                    if (!phrases[phrases.length - 1].equals(pipesarr[mainphrasecol])) {
                                        features.put("mainphrase", features.get("mainphrase") + "_" + pipesarr[mainphrasecol]);
                                    }
                                    if (!features.get("wn").matches(".*(time|tiempo|periodo).*")) {
                                        features.put("wn", pipesarr[wncol]);
                                    }

                                }

                            }
                            pipeslineant = pipesline;

                        }

                        numsent++;
                        sentence = null;
                        VerbeventDep = null;
                        sentence = new ArrayList();
                        sentence.add(line);
                        VerbeventDep = new HashMap<String, String>();
                        if (line.endsWith("B-event") && !linearr[POScol].matches("(V.*|AUX)")) {
                            //System.out.println(line);
                            VerbeventDep.put(linearr[depverbcol], linearr[lemmacol] + "|" + linearr[POScol] + "|" + linearr[wncol] + "|" + linearr[simplerolescol]);
                        }

                    }


                }





                if (sentence != null) {
                    for (int numtok = 0; numtok < sentence.size(); numtok++) {
                        pipesline = sentence.get(numtok);

                        pipesarr = pipesline.split("\\|");
                        if (!attribsCheck && pipesarr.length >= pipesfile.getPipesDescArrCount()) {
                            if (iob2col == pipesarr.length - 1) {
                                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                    System.err.println("No attribs found. Formating file for testing");
                                }
                            } else {
                                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                    System.err.println("Attribs found. Formating file for training");
                                }
                                features.put("classik", "-");
                            }
                            attribsCheck = true;
                        }

                        if (pipesarr.length >= pipesfile.getPipesDescArrCount()) {
                            if (pipesarr[iob2col].matches("B-.*")) {
                                if (!features.get("word").equals("")) {
                                    outfile.write(features.get("file") + "|" + features.get("sentN") + "|" + features.get("tokN") + "|" + features.get("word") + "|" + features.get("pos") + "|" + features.get("lemma") + "|" + features.get("roleconf") + "|" + features.get("simpleroles") + "|" + features.get("depverb") + "|" + features.get("tense") + "|" + features.get("polarity") + "|" + features.get("mainphrase") + "|" + features.get("PPdetail") + "|" + features.get("wn"));
                                    if (features.get("element").matches("(?i)timex.*")) {
                                        String normalizedTIMEK = timek.getNormTextandPattern(features.get("word"));
                                        String[] normalizedarr = normalizedTIMEK.split("\\|");
                                        tempexNorm = normalizedarr[0];
                                        tempexPattern = normalizedarr[1];
                                        String[] tempexarr = tempexNorm.split("_");
                                        String[] tempexNUarr = tempexPattern.split("_");
                                        String granularity = "-";
                                        String set_indicator = "0";
                                        if (tempexNorm.matches(timek.SET_re)) {
                                            set_indicator = "1";
                                        }
                                        if (tempexNUarr[tempexNUarr.length - 1].equalsIgnoreCase("TUNIT")) {
                                            granularity = "granul_date";
                                        }
                                        if (tempexarr[tempexarr.length - 1].matches(timek.TOD_re)) {
                                            granularity = "granul_time";
                                        }

                                        // normalizedtext | pattern | lastword|lastNU|lastwordgranularity|setinicator
                                        outfile.write("|" + tempexNorm + "|" + tempexPattern + "|" + tempexarr[tempexarr.length - 1] + "|" + tempexNUarr[tempexNUarr.length - 1] + "|" + granularity + "|" + set_indicator);

                                    } else {
                                        if (features.get("element").matches("(?i)event") && features.get("pos").matches("(V.*|AUX)") && VerbeventDep.containsKey(features.get("lemma"))) {
                                            outfile.write("|1|" + VerbeventDep.get(features.get("lemma"))+"|-");
                                        } else {
                                            outfile.write("|-|-|-|-|-|-");
                                        }
                                    }
                                    outfile.write("|" + features.get("element"));
                                    if (features.containsKey("classik")) {
                                        outfile.write("|" + features.get("classik"));
                                    }
                                    outfile.write("\n");
                                    tempexNorm = "";
                                    tempexPattern = "";
                                    classik_clear_features(features);
                                }
                                String element = pipesarr[iob2col].substring(2);
                                if (tempevalfilecol != -1) {
                                    features.put("file", pipesarr[tempevalfilecol]);  // the first
                                    features.put("sentN", pipesarr[tempevalfilecol + 1]); // the first
                                    features.put("tokN", pipesarr[tempevalfilecol + 2]);  // the first
                                }
                                features.put("word", pipesarr[tokencol]);
                                features.put("pos", pipesarr[POScol]);
                                features.put("lemma", pipesarr[lemmacol]);
                                features.put("roleconf", pipesarr[roleconfcol]);
                                features.put("simpleroles", pipesarr[simplerolescol]);
                                features.put("depverb", pipesarr[depverbcol]);
                                features.put("tense", pipesarr[tensecol]);
                                features.put("polarity", pipesarr[polaritycol]);
                                features.put("mainphrase", pipesarr[mainphrasecol]);
                                features.put("PPdetail", pipesarr[ppdetailcol]);
                                features.put("wn", pipesarr[wncol]);
                                features.put("element", element);



                                if (features.containsKey("classik")) {
                                    if (pipesarr[attrscol].matches(".*=.*=.*") && !pipesarr[attrscol].contains(";")) {
                                        tempexAttribsHash = XmlAttribs.parseXMLattrs(pipesarr[attrscol]);
                                    } else {
                                        tempexAttribsHash = XmlAttribs.parseSemiColonAttrs(pipesarr[attrscol]);
                                    }
                                    if (pipesarr[iob2col].matches("(?i).*timex.*")) {
                                        features.put("classik", tempexAttribsHash.get("type"));
                                    } else {
                                        features.put("classik", tempexAttribsHash.get("class"));
                                    }
                                }


                            }

                            if (pipesarr[iob2col].matches("I-.*")) {
                                if (features.get("word").equals("")) {
                                    throw new Exception("Malformed annotation: " + pipesline + "\n Prev: " + pipeslineant);
                                }
                                features.put("word", features.get("word") + "_" + pipesarr[tokencol]);
                                features.put("pos", features.get("pos") + "_" + pipesarr[POScol]);
                                features.put("lemma", features.get("lemma") + "_" + pipesarr[lemmacol]);
                                String[] roles = features.get("simpleroles").split("_");
                                if (!roles[roles.length - 1].equals(pipesarr[simplerolescol])) {
                                    features.put("simpleroles", features.get("simpleroles") + "_" + pipesarr[simplerolescol]);
                                }
                                String[] phrases = features.get("mainphrase").split("_");
                                if (!phrases[phrases.length - 1].equals(pipesarr[mainphrasecol])) {
                                    features.put("mainphrase", features.get("mainphrase") + "_" + pipesarr[mainphrasecol]);
                                }
                                if (!features.get("wn").matches(".*(time|tiempo|periodo).*")) {
                                    features.put("wn", pipesarr[wncol]);
                                }
                            }

                        }
                        pipeslineant = pipesline;

                    }
                }

                if (!features.get("word").equals("")) {
                    outfile.write(features.get("file") + "|" + features.get("sentN") + "|" + features.get("tokN") + "|" + features.get("word") + "|" + features.get("pos") + "|" + features.get("lemma") + "|" + features.get("roleconf") + "|" + features.get("simpleroles") + "|" + features.get("depverb") + "|" + features.get("tense") + "|" + features.get("polarity") + "|" + features.get("mainphrase") + "|" + features.get("PPdetail") + "|" + features.get("wn"));
                    if (features.get("element").matches("(?i)timex.*")) {
                        String normalizedTIMEK = timek.getNormTextandPattern(features.get("word"));
                        String[] normalizedarr = normalizedTIMEK.split("\\|");
                        tempexNorm = normalizedarr[0];
                        tempexPattern = normalizedarr[1];
                        String[] tempexarr = tempexNorm.split("_");
                        String[] tempexNUarr = tempexPattern.split("_");
                        String granularity = "-";
                        String set_indicator = "0";
                        if (tempexNorm.matches(timek.SET_re)) {
                            set_indicator = "1";
                        }

                        if (tempexNUarr[tempexNUarr.length - 1].equalsIgnoreCase("TUNIT")) {
                            granularity = "granul_date";
                        }
                        if (tempexarr[tempexarr.length - 1].matches(timek.TOD_re)) {
                            granularity = "granul_time";
                        }
                        // normalizedtext | pattern | lastword|lastNU|lastwordgranularity|setinicator
                        outfile.write("|" + tempexNorm + "|" + tempexPattern + "|" + tempexarr[tempexarr.length - 1] + "|" + tempexNUarr[tempexNUarr.length - 1] + "|" + granularity + "|" + set_indicator);

                    } else {
                        if (features.get("element").matches("(?i)event") && features.get("pos").matches("(V.*|AUX)") && VerbeventDep.containsKey(features.get("lemma"))) {
                            outfile.write("|1|" + VerbeventDep.get(features.get("lemma"))+"|-");
                        } else {
                            outfile.write("|-|-|-|-|-|-");
                        }

                    }

                    outfile.write("|" + features.get("element"));
                    if (features.containsKey("classik")) {
                        outfile.write("|" + features.get("classik"));
                    }
                    outfile.write("\n");
                    tempexNorm = "";
                    tempexPattern = "";
                    classik_clear_features(features);
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
