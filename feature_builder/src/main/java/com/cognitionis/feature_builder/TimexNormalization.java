package com.cognitionis.feature_builder;

import com.cognitionis.knowledgek.TIMEK.TIMEK;
import java.io.*;
import java.util.*;
import com.cognitionis.nlp_files.*;
import com.cognitionis.utils_basickit.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class TimexNormalization {

    // PERIOD == DURATION (TimeML)
    public static enum NormTypes {

        PERIOD, ISO, ISOFA, ISOFR, ISOSET, PRESENT_REF, PAST_REF, FUTURE_REF
    };
    /**
     * Returns the input PipesFile (filename), annotated with the TIMEN features for the given language and DCT format
     *
     * @param features_and_attributes  input filename (base-segmentation.TempEval2-features format)
     * @param lang  language code (en for English, es for Spanish)
     * @param corpus_dct_format (TempEval or TimeBank)
     *
     * @return outputfilename
     */
    public static String getTIMEN(String features_and_attributes, String classik, String lang) {
        PipesFile featuresFile = new PipesFile(features_and_attributes);
        featuresFile.setLanguage(lang);
        ((PipesFile) featuresFile).isWellFormedOptimist();

        PipesFile classikFile = new PipesFile(classik);
        classikFile.setLanguage(lang);
        ((PipesFile) classikFile).isWellFormedOptimist();

        return getTIMEN(featuresFile, classikFile);
    }

    /**
     * Returns the input PipesFile (with lang set), annotated with the TIMEN features for a DCT format
     *
     * @param pipesfile   input PipesFile (base-segmentation.TempEval2-features format)
     * @param corpus_dct_format (TempEval or TimeBank)
     *
     * @return outputfilename
     */
    public static String getTIMEN(PipesFile featuresFile, PipesFile classikFile) {
        String outputfile = null;
        Boolean attribsCheck = false;
        Boolean hasAttribs = false;
        try {
            outputfile = featuresFile.getFile().getCanonicalPath() + ".TempEval-normalization";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader featuresreader = new BufferedReader(new FileReader(featuresFile.getFile()));
            BufferedReader classikreader = new BufferedReader(new FileReader(classikFile.getFile()));

            /*
             * file|sent-num|tok-num|word|pos|lemma|rolesconf|simpleroles|depverb|tense|polarity|mainphrase|PPdetail|wn|
             * te-numval|te-pattern|lastword|lastNU|lastwordgranularity|setinicator|element(timex)
             *
             * timex-type|DCT|reference
             *
             * OLD: TYPE|ID|NORMTEXT|PATTERN|Tense|PPdetail|DCT[file](t0)=x|TempFunc|AnchorRel|Anchor|reference [|value]
             * OLD: TempFunc  = TimeML_atrib (true means ISO_function)|AnchorRel = (relative|absolute)|AnchorId  = timex of relative id (t0 == absolute)
             *
             *  reference = value of the relative timex  (last absolute DATE/TIME)
             */

            String pipesline;
            String[] pipesarr = null;

            String classikline;

            String tempexFile = "-";
            String tempexTYPE = null;
            String tempexVALUE = "-";
            String tempexNormType = null;

            HashMap<String, String> tempexAttribsHash = null;
            String tempexAnchor = null;
            String tempexReference = "-";

            TIMEK timek = new TIMEK(new Locale(featuresFile.getLanguage()));

            // TODO improve NORMALIZATION (MULTI-PHASE) see Ahn and Dale work...
            int iob2col = featuresFile.getColumn("element\\(IOB2\\)");
            int attrscol = iob2col + 1;

            if (iob2col == -1) {
                throw new Exception("-- element/attribs column not found.");
            }

            // DCTs should have an id (otherwise is set as t0 by default)
            HashMap<String, String[]> DCTs = TempEvalFiles.getDCTsFromTab(featuresFile.getFile().getCanonicalPath().substring(0, featuresFile.getFile().getCanonicalPath().lastIndexOf("/")) + "/dct.tab");
            HashMap<String, String> trainingTempexReferences = new HashMap<String, String>();

            try {

                while ((pipesline = featuresreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");
                    if (!attribsCheck && pipesarr.length >= featuresFile.getPipesDescArrCount()) {
                        if (iob2col == pipesarr.length - 1) {
                            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                System.err.println("No attribs found. Formating file for testing");
                            }
                        } else {
                            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                System.err.println("Attribs found. Formating file for training");
                            }
                            hasAttribs = true;
                        }
                        attribsCheck = true;
                    }



                    if (pipesarr.length >= featuresFile.getPipesDescArrCount()) {
                        //System.out.println(pipesline);
                        if (pipesarr[iob2col].matches("B-(?:timex|TIMEX)3?.*")) {
                            classikline = classikreader.readLine();
                            String[] classikarr = classikline.split("\\|");


                            tempexFile = pipesarr[0];
                            tempexTYPE = classikarr[classikarr.length - 1];
                            String tempexNormText = classikarr[classikFile.getColumn("extra1")];
                            String tempexPattern = classikarr[classikFile.getColumn("extra2")];



                            // For training the type and value are known and the normalization type is guessable
                            if (hasAttribs) {
                                tempexAttribsHash = XmlAttribs.parseAttrs(pipesarr[attrscol]);
                                tempexVALUE = tempexAttribsHash.get("value");
                                tempexTYPE = tempexAttribsHash.get("type");

                                // esto tendría q inicializarse antes de empezar (2 pasadas Dale et al.)
                                if ((tempexTYPE.equalsIgnoreCase("DATE") || tempexTYPE.equalsIgnoreCase("TIME")) && tempexVALUE.matches("[0-9]{4}.*")) {
                                    trainingTempexReferences.put(tempexAttribsHash.get("tid"), tempexVALUE);
                                }

                                // guess the NormType and put the reference if needed
                                /*if (tempexAttribsHash.containsKey("anchorTimeID") && trainingTempexReferences.get(tempexAttribsHash.get("anchorTimeID"))!=null) {
                                    tempexAnchor = tempexAttribsHash.get("anchorTimeID");
                                    tempexNormType = "ISOFR";
                                    tempexReference = trainingTempexReferences.get(tempexAnchor);
                                } else {*/
                                    tempexNormType = TIMEK.getNormType(tempexVALUE);
                                    if (tempexNormType.equalsIgnoreCase("ISO")) {
                                        if (!tempexNormText.matches("(?:(?:.*_)?[0-9]{4}(?:_.*)?|[0-9]{1,2}[./-][0-9]{1,2}[./-][0-9]{1,4})") && !tempexNormText.matches("(?:.*_)?" + timek.Decades_re) && !tempexNormText.matches("(?:.*_)*(?:(?:the|el)_)?[0-9]+_(?:year|century|millennium)") && !tempexNormText.matches("(?:.*_)*(?:el_)*(?:año|siglo|milenio)_[0-9]+(?:_.*)?")) {
                                            tempexNormType = "ISOFA";
                                        }
                                    }
                                //}
                            }

                            // For testing, the value is unknown and the normalization type must be guessed

                            // Write the train or test feature-vector
                            /*System.out.println(classikline);
                            System.out.println(tempexTYPE);
                            System.out.println(tempexFile);
                            System.out.println(DCTs.get(tempexFile)[0]);
                            System.out.println(tempexReference);*/
                            outfile.write(classikline.substring(0, classikline.lastIndexOf('|')) + "|" + tempexTYPE + "|" + DCTs.get(tempexFile)[0] + "|" + tempexReference+"|"+tempexVALUE);
                            if (hasAttribs) {
                                outfile.write("|"+tempexNormType);
                            }
                            outfile.write("\n");
                        }

                    }
                }

            } finally {
                if (featuresreader != null) {
                    featuresreader.close();
                }
                if (classikreader != null) {
                    classikreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TIMEN):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;

    }

    /**
     * Returns the input PipesFile (string), annotated with the ISO value for a language.
     *
     * @param features_and_attributes  input filename (base-segmentation.TempEval2-features-annotated-with-TIMEN)
     * @param lang  language code (en for English, es for Spanish)
     *
     * @return outputfilename
     */
    public static String get_normalized_values(String timenf, String lang) {
        String output = null;
        PipesFile nlpfile = new PipesFile(timenf);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        nlpfile.setLanguage(lang);
        output = getNormalizedValues((PipesFile) nlpfile);
        return output;
    }

    /**
     * Returns the input PipesFile (TIMEN), annotated with the ISO 8601 value for a language.
     *
     * @param pipesfile   input PipesFile (TIMEN)
     * @param lang  language code (en for English, es for Spanish)
     *
     * @return outputfile
     */
    public static String getNormalizedValues(PipesFile timenFile) {
        String outputfile = null;
        int linen = 0;
        try {
            outputfile = timenFile.getFile().getCanonicalPath() + "-normalized_values";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(timenFile.getFile()));

            int TEnormtypecol = timenFile.getLastDescColumn();
            int TEpatterncol = timenFile.getColumn("extra2");
            int TEnumvalcol = timenFile.getColumn("extra1");
            int TEtensecol = timenFile.getColumn("tense");
            int TEdctcol = timenFile.getColumn("DCT");
            int TErelrefcol = timenFile.getColumn("ref-val");

            TIMEK timek = new TIMEK(new Locale(timenFile.getLanguage()));
            String TEnormtype = null;
            String lastTempexReference = null;
            String curr_fileid = "";

            String pipesline = null;
            String[] pipesarr = null;
            try {
                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");
                    linen++;
                    if (pipesarr.length >= timenFile.getPipesDescArrCount()) {

                        if (TEnormtypecol < (pipesarr.length - 1)) {
                            TEnormtypecol = pipesarr.length - 1;
                        }

                        // Initialize reference as DCT for each file
                        if (!curr_fileid.equals(pipesarr[0])) {
                            curr_fileid = pipesarr[0];
                            lastTempexReference = pipesarr[TEdctcol];
                        }

                        TEnormtype = pipesarr[TEnormtypecol];
                        String[] val = pipesarr[TEnumvalcol].split("_");
                        String[] pat = pipesarr[TEpatterncol].split("_");
                        switch (NormTypes.valueOf(TEnormtype)) {
                            case PERIOD:
                                //BUILD EXPRESSION
                                // TODO meter esto en TIMEK como funcion... solo vale YMD y THMS y NI como excepcion
                                // todo lo demás se tiene q pasar a unidades inferiores (media hora) --> 30 minutos y redondear
                                // 2.5 semanas == 14 dias + 3 o 4 dias (segun redondeo)
                                outfile.write(pipesline + "|P" + timek.getISOperiod(val, pat) + "\n");
                                break;


                            case ISO: // ONLY EXPLICIT ISOs (year is needed unless for decades, centureis, or millennia)
                                //BUILD DATE EXPRESSION
                                String date = "";
                                Boolean inTE = false;
                                if(pipesarr[TEpatterncol].equalsIgnoreCase("Date")){
                                    date=pipesarr[TEnumvalcol];
                                }else{
                                    int iEnd = -1;
                                    for (int i = pat.length - 1; i >= 0; i--) {
                                        if (!pat[i].matches("(TMonth|Num|s|TUnit|" + timek.Decades_re + "|(mid-)?[0-9]{4}s|[0-9]{4}[-/][0-9]{2}[-/][0-9]{2}|[0-9]{2}[:][0-9]{2}([:][0-9]{2})?)")) {
                                            iEnd = i;
                                        } else {
                                            break;
                                        }
                                    }
                                    for (int i = 0; i < pat.length; i++) {
                                        if (i == iEnd) {
                                            break;
                                        }
                                        if (pat[i].matches("(TMonth|Num|TUnit|" + timek.Decades_re + "|(mid-)?[0-9]{4}(s)?|[0-9]{4}[-/][0-9]{2}[-/][0-9]{2})")) {
                                            inTE = true;
                                        }
                                        if (inTE) {
                                            if (pat[i].equals("Num")) {
                                                date += " " + val[i];
                                            }
                                            if (!pat[i].equals("Num")) {
                                                if (!val[i].equals("s")) {
                                                    date += " ";
                                                }
                                                date += val[i];
                                            }
                                        }
                                    }
                                }
                                String iso_explicit = pipesarr[TEdctcol];
                                if (date.equals("")) {
                                    if (!pipesarr[TEnumvalcol].matches("[0-9]{4}[-/][0-9]{4}")) {
                                        System.err.println("Malformed ISO explicit date (empty): " + pipesarr[TEnumvalcol] + " - " + pipesarr[TEpatterncol]);
                                    }
                                }
                                if (!pipesarr[TEnumvalcol].matches("[0-9]{4}[-/][0-9]{4}") && !date.isEmpty()) {
                                    iso_explicit = timek.toISO8601(date.trim());
                                } else {
                                    iso_explicit = pipesarr[TEnumvalcol].replaceAll("-", "/");
                                }
                                outfile.write(pipesline + "|" + iso_explicit + "\n");
                                lastTempexReference = iso_explicit;
                                break;

                            case ISOFR:
                                if (pipesarr[TErelrefcol].equals("-")) {
                                    pipesarr[TErelrefcol] = lastTempexReference;
                                }
                                String isofr = timek.obtainImplicitDate(pipesarr[TEnumvalcol], pipesarr[TEpatterncol], pipesarr[TEtensecol], pipesarr[TErelrefcol]);
                                outfile.write(pipesline + "|" + isofr + "\n");
                                lastTempexReference = isofr;
                                break;

                            case ISOFA:
                                String isofa = timek.obtainImplicitDate(pipesarr[TEnumvalcol], pipesarr[TEpatterncol], pipesarr[TEtensecol], pipesarr[TEdctcol]);
                                outfile.write(pipesline + "|" + isofa + "\n");
                                lastTempexReference = isofa;
                                break;
                            case ISOSET:
                                String set = timek.obtainISOSet(pipesarr[TEnumvalcol], pipesarr[TEpatterncol]);
                                outfile.write(pipesline + "|" + set + "\n");
                                break;
                            case PRESENT_REF:
                            case PAST_REF:
                            case FUTURE_REF:
                                if (TEnormtype.equals("PRESENT_REF")) {
                                    lastTempexReference = pipesarr[TEdctcol];
                                }
                                outfile.write(pipesline + "|" + TEnormtype + "\n");
                                break;
                        }
                    }
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
            System.err.println("Errors found (TIMEN):\n\t" + e.toString() + " (Line " + linen + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    /** Stupid baseline REMOVE**/
    public static String get_normalized_values_baseline(String timenf) {
        String output;
        PipesFile nlpfile = new PipesFile(timenf);
        ((PipesFile) nlpfile).isWellFormedOptimist();
        output = getNormalizedValuesBaseline((PipesFile) nlpfile);
        return output;
    }

    public static String getNormalizedValuesBaseline(PipesFile pipesfile) {
        String outputfile = null;
        int linen = 0;
        try {
            outputfile = pipesfile.getFile().getCanonicalPath() + "-normalized_values";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            int TEdctcol = pipesfile.getColumn("DCT");

            String pipesline = null;
            String[] pipesarr = null;
            try {
                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");
                    linen++;
                    if (pipesarr.length >= pipesfile.getPipesDescArrCount()) {
                        outfile.write(pipesline + "|" + pipesarr[TEdctcol] + "\n");
                    }
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
            System.err.println("Errors found (TIMEN):\n\t" + e.toString() + " (Line " + linen + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String get_key_normalized_values(String timenf) {
        String output = timenf + "-key";
        int linen = 0;
        try {
            BufferedWriter outfile = new BufferedWriter(new FileWriter(output));
            BufferedReader pipesreader = new BufferedReader(new FileReader(timenf));

            PipesFile nlpfile = new PipesFile(timenf);
            ((PipesFile) nlpfile).isWellFormedOptimist();

            int valuecol=nlpfile.getColumn("value");

            try {
                String pipesline;
                String[] pipesarr = null;

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    outfile.write(pipesline + "|" + pipesarr[valuecol] + "\n");
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
            System.err.println("Errors found (TempEval-Experimenter):\n\t" + e.toString() + " - line:" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return output;
    }
}

