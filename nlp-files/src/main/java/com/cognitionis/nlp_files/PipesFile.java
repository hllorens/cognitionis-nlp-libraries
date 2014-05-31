package com.cognitionis.nlp_files;

import com.cognitionis.utils_basickit.StringUtils;
import com.cognitionis.utils_basickit.FileUtils;
import java.io.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class PipesFile extends NLPFile {

    /*
     * Pipes description array
     */
    private String[] pipes_desc_arr;
    private Integer pipes_desc_arr_count;
    public Boolean isWellFormed;

    public PipesFile(String filename) {
        super(filename);
        pipes_desc_arr = null;
        pipes_desc_arr_count = 0;
        isWellFormed = false;
    }

    public Boolean isWellFormedOptimist() {
        try {

            if (extension.matches("\\s*")) {
                throw new Exception("PipesFile must have an extension (i.e., .tok)");
            }
            if (extension.contains("annotatedWith")) {
                extension = extension.substring(0, extension.lastIndexOf("-annotatedWith") + 14);
            }

            if (extension.contains("annotationKey")) {
                extension = extension.substring(0, extension.lastIndexOf("-annotationKey") + 14);
            }

            File pipes_desc_file = new File(this.f.getCanonicalPath().substring(0, this.f.getCanonicalPath().lastIndexOf('/') + 1) + extension + ".pipes-desc");
            if (!pipes_desc_file.exists() || !pipes_desc_file.isFile()) {
                //System.out.println(FileUtils.getApplicationPath() + FileUtils.NLPFiles_descr_path + extension + ".pipes-desc");
                pipes_desc_file = new File(FileUtils.getApplicationPath() + FileUtils.NLPFiles_descr_path + extension + ".pipes-desc");
                if (!pipes_desc_file.exists() || !pipes_desc_file.isFile()) {
                    throw new Exception("PipesFile description file (" + extension + ".pipes-desc) not found in " + pipes_desc_file.getCanonicalPath());
                }
            }

            // read pipes desc, count fields (cols)
            BufferedReader reader = new BufferedReader(new FileReader(pipes_desc_file));
            try {
                String line = null;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    linen++; //System.getProperty("line.separator")
                    if (line.equals("word")) {
                        pipes_desc_arr = new String[1];
                        pipes_desc_arr[0] = "word";
                        pipes_desc_arr_count = 1;
                        break;
                    }
                    if (line.matches(".*\\|.*")) {
                        pipes_desc_arr = line.split("\\|");
                        pipes_desc_arr_count = pipes_desc_arr.length;
                        break;
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            if (pipes_desc_arr == null) {
                throw new Exception(pipes_desc_file + " is not a valid PipesFile description file");
            }



        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return false;
        }

        this.isWellFormed = true;
        return true;
    }

    public Boolean isWellFormatted() {

        try {

            if (extension.matches("\\s*")) {
                throw new Exception("PipesFile must have an extension (i.e., .tok)");
            }
            if (extension.contains("annotatedWith")) {
                extension = extension.substring(0, extension.lastIndexOf("-annotatedWith") + 14);
            }

            if (extension.contains("annotationKey")) {
                extension = extension.substring(0, extension.lastIndexOf("-annotationKey") + 14);
            }

            File pipes_desc_file = new File(this.f.getCanonicalPath().substring(0, this.f.getCanonicalPath().lastIndexOf('/') + 1) + extension + ".pipes-desc");
            if (!pipes_desc_file.exists() || !pipes_desc_file.isFile()) {
                pipes_desc_file = new File(FileUtils.getApplicationPath() + FileUtils.NLPFiles_descr_path + extension + ".pipes-desc");
                if (!pipes_desc_file.exists() || !pipes_desc_file.isFile()) {
                    throw new Exception("PipesFile description file (" + extension + ".pipes-desc) not found in " + pipes_desc_file.getCanonicalPath());
                }
            }

            // read pipes desc, count fields (cols)
            BufferedReader reader = new BufferedReader(new FileReader(pipes_desc_file));
            try {
                String line = null;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    linen++; //System.getProperty("line.separator")
                    if (line.matches(".*\\|.*")) {
                        pipes_desc_arr = line.split("\\|");
                        pipes_desc_arr_count = pipes_desc_arr.length;
                        break;
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            if (pipes_desc_arr == null) {
                throw new Exception(pipes_desc_file + " is not a valid PipesFile description file");
            }

            // read pipesFile line by line check that at least it have X piped cols
            reader = new BufferedReader(new FileReader(this.f));
            try {
                String line = null;
                String lastline = null;

                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    linen++; //System.getProperty("line.separator")
                    lastline = line;

                    if (line.length() > 0 && line.matches("[^\\|]+")) {
                        throw new Exception("Line " + linen + " is not valid pipesFile line: Has contet without |");
                    }
                    if (line.length() > 1 && line.matches(".*\\|.*")) { // it permits a | as an empty line
                        if (line.split("\\|").length < this.pipes_desc_arr_count) {
                            throw new Exception("Line " + linen + " is not valid pipesFile line: Has less columns (" + line.split("\\|").length + ") than description file(" + pipes_desc_arr_count + ")");
                        }
                    }
                }
                if (!extension.matches("(tab|pipes.*|TempEval.*|roth-.*|srlpaired.*|poslemma.*)") && !lastline.trim().equals("|")) {
                    throw new Exception("Last line does not end with an end sentence marker |. (DEPRECATED... MODIFY..)");
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
                System.exit(1);
            }
            return false;
        }

        this.isWellFormed = true;
        return true;

    }

    public String[] getPipesDescArr() {
        return this.pipes_desc_arr;
    }

    public int getPipesDescArrCount() {
        return this.pipes_desc_arr_count;
    }

    /**
     * Returns the pipesfile splited in sentences by empty |
     *
     * @return outputfilename
     */
    public String sentSplit() {
        String outputfile = this.getFile().toString() + ".pipes";
        int numline = 0;
        try {
            BufferedReader pipesreader = new BufferedReader(new FileReader(f));
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));

            int sentcolumn = 1;
            int filecolumn = 0;
            try {
                String line;
                String numsent = "-1";
                String filename = "-1";
                while ((line = pipesreader.readLine()) != null) {
                    numline++;
                    String[] linearr = line.split("\\|");
                    if ((!filename.equals(linearr[filecolumn]) || !numsent.equals(linearr[sentcolumn])) && !numsent.equals("-1") && !filename.equals("-1")) {
                        outfile.write("|\n");
                    }
                    for (int i = 3; i < linearr.length - 1; i++) {
                        outfile.write(linearr[i] + "|");
                    }
                    outfile.write(linearr[linearr.length - 1] + "\n");

                    numsent = linearr[sentcolumn];
                    filename = linearr[filecolumn];
                }
                outfile.write("|\n");

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + " (line " + numline + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public String detectLanguage() {
        return "en";
    }

    public String toPlain() {
        String outputfile = this.getFile().toString() + ".plain";
        // first look for any paired column (i.e., leading_blanks, offset)
        // if found follow it
        // else one token, one space, one token, one space... (end of sentence -> \n)
        try {
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            if (!this.isWellFormed) {
                throw new Exception("Malformed pipes file");
            }

            int tokcolumn = this.getColumn("(token|word).*");
            int blankscolumn = this.getColumn("(leading.?blanks?|blanks)");
            int sentnumcolumn = this.getColumn("sent-num");

            BufferedReader pipesreader = new BufferedReader(new FileReader(this.f));
            try {
                String line;
                String outputline = "";
                int current_sentence = 0; // (from 0 to n)
                while ((line = pipesreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");
                    if (linearr.length >= this.pipes_desc_arr_count) {
                        String token = linearr[tokcolumn];
                        if (this.extension.equalsIgnoreCase("TempEval-bs")) {
                            //System.err.println("sentnum="+linearr[sentnumcolumn]+" token="+token);
                            if (token.matches("(\\*.*|0)")) {
                                continue;
                            }
                            if (token.matches("-.+-")) {
                                if (token.equals("-LRB-")) {
                                    token = "(";
                                }
                                if (token.equals("-RRB-")) {
                                    token = ")";
                                }
                                if (token.equals("-LSB-")) {
                                    token = "[";
                                }
                                if (token.equals("-RSB-")) {
                                    token = "]";
                                }
                                if (token.equals("-LCB-")) {
                                    token = "{";
                                }
                                if (token.equals("-RCB-")) {
                                    token = "}";
                                }
                            }
                            if ((sentnumcolumn != -1) && (Integer.parseInt(linearr[sentnumcolumn]) != current_sentence)) {
                                current_sentence = Integer.parseInt(linearr[sentnumcolumn]);
                                outfile.write(outputline + "\n");
                                outputline = token;
                            } else {
                                if (!outputline.equals("")) {
                                    outputline += " ";
                                }
                                outputline += token;
                            }
                        } else {
                            if (blankscolumn != -1) {
                                for (int i = 1; i <= Integer.parseInt(linearr[blankscolumn]); i++) {
                                    outputline += " ";
                                }
                            } else {
                                if (!outputline.equals("")) {
                                    outputline += " ";
                                }
                            }
                            outputline += token;
                        }
                    } else {
                        current_sentence++;
                        outfile.write(outputline + "\n");
                        outputline = "";
                    }

                }
                // write last sentence
                if (!outputline.equals("")) {
                    outfile.write(outputline + "\n");
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
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public String pair_pipes_by_column_JOIN(int paircol, String model, int modelcol) {
        String outputfile = this.getFile().toString() + ".paired";
        try {
            if (!this.isWellFormed) {
                throw new Exception("Malformed pipes file");
            }

            if (paircol < 0 || this.pipes_desc_arr_count < paircol) {
                throw new Exception("Paircol (" + paircol + ") does not exist");
            }

            PipesFile modelpipes = new PipesFile(model);
            modelpipes.isWellFormedOptimist();

            if (modelcol < 0 || modelpipes.getPipesDescArrCount() < modelcol) {
                throw new Exception("Modelcol (" + modelcol + ") does not exist (total: " + modelpipes.getPipesDescArrCount() + ") ");
            }

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader modelreader = new BufferedReader(new FileReader(model));
            BufferedReader pipesreader = new BufferedReader(new FileReader(this.f));

            try {
                String line;
                String pairline = "";
                String[] pairarr = null;
                String pipespair = "";
                Boolean paired = true;
                while ((line = modelreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");
                    if (linearr.length >= modelpipes.getPipesDescArrCount()) {
                        String modelpair = linearr[modelcol];
                        if (paired) {
                            pairline = pipesreader.readLine();
                            // handel Freeling/Treetager/etc. newlines
                            while (pairline != null && (pairline.equals("|") || pairline.trim().equals(""))) {
                                pairline = pipesreader.readLine();
                            }
                            if (pairline != null) {
                                pairarr = pairline.split("\\|");
                                pipespair = pairarr[paircol];
                            }
                        }

                        if (modelpair.equals(pipespair)) {
                            paired = true;
                        } else {
                            //System.out.println("Model:(" + modelpair + ") Pair(" + pipespair + ")");

                            if (modelpair.equals("\"") && (pipespair.equals("``") || pipespair.equals("''"))) {
                                paired = true;
                            } else {
                                if (modelpair.matches("-.+-")) {
                                    if ((modelpair.equals("-LRB-") && pipespair.equals("(")) || (modelpair.equals("-RRB-") && pipespair.equals(")"))) {
                                        paired = true;
                                    } else {
                                        if ((modelpair.equals("-LSB-") && pipespair.equals("[")) || (modelpair.equals("-RSB-") && pipespair.equals("]"))) {
                                            paired = true;
                                        } else {
                                            if ((modelpair.equals("-LCB-") && pipespair.equals("{")) || (modelpair.equals("-RCB-") && pipespair.equals("}"))) {
                                                paired = true;
                                            } else {
                                                paired = false;

                                            }
                                        }
                                    }
                                } else {
                                    paired = false;
                                }
                            }

                        }
                    }

                    //System.out.println("paired output: " + line);
                    outfile.write(line);
                    if (modelpipes.getPipesDescArrCount() > 1) {
                        for (int i = 0; i < this.pipes_desc_arr_count; i++) {
                            if (i != paircol) {
                                if (paired && pairarr != null) {
                                    outfile.write("|" + pairarr[i]);
                                } else {
                                    outfile.write("|-");
                                }
                            }
                        }
                    }

                    if (paired) {
                        pairarr = null;
                        pairline = "";
                        pipespair = "";
                    }
                    outfile.write("\n");


                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (modelreader != null) {
                    modelreader.close();
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

    // Para hacerlo bien en el futuro lo mejor seria tener un cierto buffer de memoria... para enparejar grupos de palabras... (5) pej
    public String pair_ancora2pipes(int modelcol, String AnCoraPath, int paircol) {
        String outputfile = this.getFile().toString() + ".roth-treetag";

        try {
            if (!this.isWellFormed) {
                throw new Exception("Malformed pipes file");
            }


            if (modelcol < 0 || this.pipes_desc_arr_count < modelcol) {
                throw new Exception("Pairtextcol (" + modelcol + ") does not exist (total: " + this.pipes_desc_arr_count + ") ");
            }

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader modelreader = new BufferedReader(new FileReader(this.f));

            try {
                int syntcolumn = -1;
                int lemmacolumn = -1;
                String accomulatedsynt = "";
                String line;
                String pairline = "";
                String[] pairarr = null;
                String pipespair = "";
                Boolean inMultiword = false;
                Boolean paired = true;
                String currentpairfile = "";
                BufferedReader pipesreader = null;
                PipesFile pairpipes = null;
                while ((line = modelreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");

                    String pairfile = AnCoraPath + linearr[0].substring(0, linearr[0].lastIndexOf('.') + 1) + "tbf.utf8.roth-treetag";

                    //SEGUN VAMOS LEYENDO LINEAS VAMOS DECIDIENDO QUE FICHERO ENPAREJAMOS...
                    if (!(new File(pairfile).exists()) && pairfile.matches(".*_[a-z]\\.tbf\\.utf8\\.roth-treetag")) {
                        pairfile = pairfile.replaceFirst("_[a-z]\\.tbf", "\\.tbf");
                    }
                    if (!currentpairfile.equals(pairfile)) {
                        currentpairfile = pairfile;

                        if (pipesreader != null) {
                            pipesreader.close();
                        }
                        pairpipes = new PipesFile(pairfile);
                        pairpipes.isWellFormedOptimist();
                        if (paircol < 0 || pairpipes.getPipesDescArrCount() < paircol) {
                            throw new Exception("Paircol (" + paircol + ") does not exist");
                        }
                        pipesreader = new BufferedReader(new FileReader(pairfile));
                        syntcolumn = pairpipes.getColumn("synt");
                        lemmacolumn = pairpipes.getColumn("lemma");
                        System.out.println(pairfile + "(synt " + syntcolumn + ")(lemma " + lemmacolumn + ")");
                    }
                    /*
                    if(linearr[1].equals("2")){
                    System.exit(0);
                    }
                     */
                    if (linearr.length >= this.pipes_desc_arr_count) {

                        String modelpair = linearr[modelcol];
                        if (paired) {
                            pairline = pipesreader.readLine();
                            while (pairline != null && pairline.equals("|")) {
                                pairline = pipesreader.readLine();
                            }
                            if (pairline != null) {
                                pairarr = pairline.split("\\|");
                                pipespair = pairarr[paircol];
                            }
                        }

                        if (modelpair.equalsIgnoreCase(pipespair) || (modelpair.equalsIgnoreCase("Barça") && pipespair.equalsIgnoreCase("Barca"))) {
                            paired = true;
                            inMultiword = false;
                            System.out.println("pairing: " + pipespair);
                        } else {
                            // HACKs
                            System.out.println("not Equal " + pairline + " - " + line + "\n");

                            /*if (pipespair.equals("Madrid") && linearr[0].equals("107_19991001.txt") && linearr[1].equals("1") && linearr[2].equals("1")) {
                            pairline = pipesreader.readLine();
                            if (pairline != null) {
                            pairarr = pairline.split("\\|");
                            pipespair = pairarr[paircol];
                            } else {
                            System.out.println("End of file reached...");
                            System.exit(1);
                            }
                            }*/
                            if (pipespair.equals("(")) {
                                System.out.println("NOT!! Find next braket ) -->");
                                int parlevel = 0;
                                parlevel += StringUtils.countOccurrencesOf(pairarr[syntcolumn], '(');
                                parlevel -= StringUtils.countOccurrencesOf(pairarr[syntcolumn], ')');

                                while (pairline != null && !pipespair.equals(")")) {
                                    pairline = pipesreader.readLine();
                                    if (pairline != null) {
                                        pairarr = pairline.split("\\|");
                                        pipespair = pairarr[paircol];
                                    } else {
                                        System.out.println("End of file reached...");
                                        System.exit(1);
                                    }
                                    parlevel += StringUtils.countOccurrencesOf(pairarr[syntcolumn], '(');
                                    parlevel -= StringUtils.countOccurrencesOf(pairarr[syntcolumn], ')');

                                }
                                if (parlevel < 0) {
                                    for (int p = 0; p > parlevel; p--) {
                                        accomulatedsynt += ")";
                                    }
                                }

                                pairline = pipesreader.readLine();
                                if (pairline != null) {
                                    pairarr = pairline.split("\\|");
                                    pipespair = pairarr[paircol];
                                } else {
                                    System.out.println("End of file reached...while looking for )");
                                    System.exit(1);
                                }
                            }











                            // Avoid symbols... when ulikely sentence/sent-part avoid
                            if (pipespair.matches("[\",%-]") && !modelpair.matches("[\",%-]") && !pairarr[syntcolumn].matches(".*\\((inc|nominalSent)((\\s+|\\().*)?")) {
                                System.out.println("NOT!! Omit symbols ) -->");
                                while (pairline != null && !pipespair.equals(modelpair)) {
                                    accomulatedsynt += pairarr[syntcolumn];
                                    Boolean sentend = false;
                                    if (pairline.equals("|")) {
                                        sentend = true;
                                    }
                                    pairline = pipesreader.readLine();
                                    if (pairline != null) {
                                        if (!pairline.equals("|")) {
                                            pairarr = pairline.split("\\|");
                                            pipespair = pairarr[paircol];
                                        } else {
                                            pipespair = "|";
                                        }
                                    } else {
                                        System.out.println("End of file reached...");
                                        System.exit(1);
                                    }
                                    if (sentend) {
                                        accomulatedsynt = "";
                                        break;
                                    }
                                }
                            }
                            /*if (pipespair.equals("SERGI LOPEZ-EGEA") && linearr[0].equals("107_20000701.txt") && linearr[1].equals("4") && linearr[2].equals("12")) {
                            pairline = pipesreader.readLine();
                            pairline = pipesreader.readLine();
                            pairline = pipesreader.readLine();
                            System.out.println("HACK) -->");
                            pairarr = pairline.split("\\|");
                            pipespair = pairarr[paircol];
                            pairline = pipesreader.readLine();
                            }*/

                            if (modelpair.equals(pipespair)) {
                                paired = true;
                                inMultiword = false;
                            } else {

                                paired = false;
                                //if (modelpair.matches("-.+-")) {
//                                    System.out.println("modelpair:"+modelpair+" - pipespair:"+pipespair);
                                //                          }else{
                                System.out.println("NOT!! modelpair:" + modelpair + " - pipespair:" + pipespair);
//                                if (pipespair.contains("_")) {
                                String trypair = pipespair;
                                if (pipespair.contains("_")) {
                                    trypair = pipespair.substring(0, pipespair.indexOf('_'));
                                }
                                System.out.println("Try:" + trypair);
                                if (trypair.equals(modelpair)) {
                                    inMultiword = true;
                                    pipespair = pipespair.substring(pipespair.indexOf('_') + 1);
                                } else {

                                    while (!paired && pairarr[syntcolumn].matches(".*\\((inc|nominalSent)((\\s+|\\().*)?")) {
                                        // Remove INC
                                        int parlevel = 0;
                                        int incparlevel = 1000;
                                        int nomparlevel = 1000;
                                        String accomulatedinc = "";
                                        String accomulatednorm = "";
                                        if (pairarr[syntcolumn].matches(".*\\(inc((\\s+|\\().*)?")) {
                                            incparlevel = StringUtils.countOccurrencesOf(pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(inc")), '(');
                                            accomulatedinc = pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(inc"));
                                        }

                                        if (pairarr[syntcolumn].matches(".*\\(nominalSent((\\s+|\\().*)?")) {
                                            nomparlevel = StringUtils.countOccurrencesOf(pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(nominalSent")), '(');
                                            accomulatednorm = pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(nominalSent"));
                                        }
                                        if (nomparlevel < incparlevel) {
                                            incparlevel = nomparlevel;
                                            accomulatedsynt += accomulatednorm;
                                        } else {
                                            accomulatedsynt = accomulatedinc;
                                        }

                                        parlevel += StringUtils.countOccurrencesOf(pairarr[syntcolumn], '(');
                                        parlevel -= StringUtils.countOccurrencesOf(pairarr[syntcolumn], ')');
                                        System.out.println("REMOVE INC/nominalsent=" + incparlevel);
                                        while (pairline != null) {
                                            System.out.println("palevel=" + parlevel + "  -  " + pairline + " ---(" + modelpair);
                                            if (parlevel <= incparlevel) {
                                                if (parlevel < incparlevel) {
                                                    for (int p = incparlevel; p > parlevel; p--) {
                                                        accomulatedsynt += ")";
                                                    }
                                                }

                                                System.out.println("END INC");
                                                pairline = pipesreader.readLine();
                                                while (pairline != null && pairline.equals("|")) {
                                                    pairline = pipesreader.readLine();
                                                }
                                                if (pairline != null) {
                                                    pairarr = pairline.split("\\|");
                                                    pipespair = pairarr[paircol];
                                                } else {
                                                    System.out.println("INC End of file reached...");
                                                    System.exit(1);
                                                }
                                                break;
                                            }

                                            pairline = pipesreader.readLine();
                                            if (pairline != null && !pairline.equals("|")) {
                                                pairarr = pairline.split("\\|");
                                                pipespair = pairarr[paircol];
                                                parlevel += StringUtils.countOccurrencesOf(pairarr[syntcolumn], '(');
                                                parlevel -= StringUtils.countOccurrencesOf(pairarr[syntcolumn], ')');
                                            } else {
                                                System.out.println("INC End of file/sentence reached...");
                                                System.exit(1);
                                            }
                                        }




                                        if (modelpair.equals(pipespair)) {
                                            paired = true;
                                            inMultiword = false;

                                        } else {
                                            if (pipespair.contains("_")) {
                                                trypair = pipespair.substring(0, pipespair.indexOf('_'));
                                                System.out.println("Try:" + trypair);
                                                if (trypair.equals(modelpair)) {
                                                    inMultiword = true;
                                                    pipespair = pipespair.substring(pipespair.indexOf('_') + 1);
                                                }
                                            }
                                        }

                                    }


                                    if (!paired && !inMultiword && !modelpair.equals("\"")) { // && !modelpair.equals("\"")) {
                                        System.out.println("NOT!! Find next sentence");
                                        accomulatedsynt = "";
                                        //System.exit(1);
                                        while (pairline != null && !pairline.equals("|")) {
                                            pairline = pipesreader.readLine();
                                        }
                                        while (pairline != null && pairline.equals("|")) {
                                            pairline = pipesreader.readLine();
                                        }
                                        if (pairline != null) {
                                            pairarr = pairline.split("\\|");
                                            pipespair = pairarr[paircol];
                                        } else {
                                            System.out.println("End of file reached...");
                                            System.exit(1);
                                        }
                                        if (modelpair.equals(pipespair)) {
                                            paired = true;
                                            inMultiword = false;

                                        } else {
                                            if (pipespair.contains("_")) {
                                                trypair = pipespair.substring(0, pipespair.indexOf('_'));
                                                System.out.println("Try:" + trypair);
                                                if (trypair.equals(modelpair)) {
                                                    inMultiword = true;
                                                    pipespair = pipespair.substring(pipespair.indexOf('_') + 1);
                                                }

                                            }
                                        }
                                    }
                                    while (!paired && pairarr[syntcolumn].matches(".*\\((inc|nominalSent)((\\s+|\\().*)?")) {
                                        // Remove INC
                                        int parlevel = 0;
                                        int incparlevel = 1000;
                                        int nomparlevel = 1000;
                                        String accomulatedinc = "";
                                        String accomulatednorm = "";
                                        if (pairarr[syntcolumn].matches(".*\\(inc((\\s+|\\().*)?")) {
                                            incparlevel = StringUtils.countOccurrencesOf(pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(inc")), '(');
                                            accomulatedinc = pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(inc"));
                                        }

                                        if (pairarr[syntcolumn].matches(".*\\(nominalSent((\\s+|\\().*)?")) {
                                            nomparlevel = StringUtils.countOccurrencesOf(pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(nominalSent")), '(');
                                            accomulatednorm = pairarr[syntcolumn].substring(0, pairarr[syntcolumn].indexOf("(nominalSent"));
                                        }
                                        if (nomparlevel < incparlevel) {
                                            incparlevel = nomparlevel;
                                            accomulatedsynt += accomulatednorm;
                                        } else {
                                            accomulatedsynt = accomulatedinc;
                                        }

                                        parlevel += StringUtils.countOccurrencesOf(pairarr[syntcolumn], '(');
                                        parlevel -= StringUtils.countOccurrencesOf(pairarr[syntcolumn], ')');
                                        System.out.println("REMOVE INC/nominalsent=" + incparlevel);
                                        while (pairline != null) {
                                            System.out.println("palevel=" + parlevel + "  -  " + pairline + " ---(" + modelpair);
                                            if (parlevel <= incparlevel) {
                                                if (parlevel < incparlevel) {
                                                    for (int p = incparlevel; p > parlevel; p--) {
                                                        accomulatedsynt += ")";
                                                    }
                                                }

                                                pairline = pipesreader.readLine();
                                                while (pairline != null && pairline.equals("|")) {
                                                    pairline = pipesreader.readLine();
                                                }
                                                if (pairline != null) {
                                                    pairarr = pairline.split("\\|");
                                                    pipespair = pairarr[paircol];
                                                } else {
                                                    System.out.println("INC End of file reached...");
                                                    System.exit(1);
                                                }
                                                break;
                                            }

                                            pairline = pipesreader.readLine();
                                            if (pairline != null && !pairline.equals("|")) {
                                                pairarr = pairline.split("\\|");
                                                pipespair = pairarr[paircol];
                                                parlevel += StringUtils.countOccurrencesOf(pairarr[syntcolumn], '(');
                                                parlevel -= StringUtils.countOccurrencesOf(pairarr[syntcolumn], ')');
                                            } else {
                                                System.out.println("INC End of file/sentence reached...");
                                                System.exit(1);
                                            }
                                        }





                                        if (modelpair.equals(pipespair)) {
                                            paired = true;
                                            inMultiword = false;

                                        } else {
                                            if (pipespair.contains("_")) {
                                                trypair = pipespair.substring(0, pipespair.indexOf('_'));
                                                System.out.println("Try:" + trypair);
                                                if (trypair.equals(modelpair)) {
                                                    inMultiword = true;
                                                    pipespair = pipespair.substring(pipespair.indexOf('_') + 1);
                                                }
                                            }
                                        }

                                    }
                                    if (!paired && !inMultiword && !modelpair.equals("\"")) { // && !modelpair.equals("\"")) {
                                        System.out.println("NOT!! Find next sentence");
                                        accomulatedsynt = "";
                                        //System.exit(1);
                                        while (pairline != null && !pairline.equals("|")) {
                                            pairline = pipesreader.readLine();
                                        }
                                        while (pairline != null && pairline.equals("|")) {
                                            pairline = pipesreader.readLine();
                                        }
                                        if (pairline != null) {
                                            pairarr = pairline.split("\\|");
                                            pipespair = pairarr[paircol];
                                        } else {
                                            System.out.println("End of file reached...");
                                            System.exit(1);
                                        }
                                        if (modelpair.equals(pipespair)) {
                                            paired = true;
                                            inMultiword = false;

                                        } else {
                                            if (pipespair.contains("_")) {
                                                trypair = pipespair.substring(0, pipespair.indexOf('_'));
                                                System.out.println("Try:" + trypair);
                                                if (trypair.equals(modelpair)) {
                                                    inMultiword = true;
                                                    pipespair = pipespair.substring(pipespair.indexOf('_') + 1);
                                                }

                                            }
                                        }
                                    }


                                }

                            }
                        }
                    }

                    //System.out.println("paired output: " + line);
                    outfile.write(line);
                    if (pairpipes.getPipesDescArrCount() > 1) {
                        for (int i = 0; i < pairarr.length; i++) {
                            if (i != paircol) {
                                if (paired && pairarr != null) {
                                    if (i == syntcolumn) {
                                        outfile.write("|" + accomulatedsynt + pairarr[i]);
                                    } else {
                                        outfile.write("|" + pairarr[i]);
                                    }
                                } else {
                                    if (inMultiword && pairarr != null) {
                                        if (i == lemmacolumn) {
                                            if (!pairarr[i].contains("_")) {
                                                System.out.println("la caca maxima=" + pairarr[i]);
                                                System.exit(1);
                                            }
                                            outfile.write("|" + pairarr[i].substring(0, pairarr[i].indexOf('_')));
                                            pairarr[i] = pairarr[i].substring(pairarr[i].indexOf('_') + 1);
                                        } else {

                                            if (i == syntcolumn) {
                                                if (pairarr[i].equals("*") || pairarr[i].equals("*)")) {
                                                    outfile.write("|" + accomulatedsynt + "*");
                                                } else {
                                                    outfile.write("|" + accomulatedsynt + pairarr[i].substring(0, pairarr[i].lastIndexOf('*') + 1));
                                                    pairarr[i] = pairarr[i].substring(pairarr[i].lastIndexOf('*'));
                                                }
                                            } else {
                                                if (i >= pairpipes.getPipesDescArrCount()) {
                                                    if (pairarr[i].equals("*")) {
                                                        outfile.write("|*");
                                                    } else {
                                                        outfile.write("|" + pairarr[i].substring(0, pairarr[i].lastIndexOf('*') + 1));
                                                        pairarr[i] = pairarr[i].substring(pairarr[i].lastIndexOf('*'));
                                                    }
                                                } else {
                                                    outfile.write("|-");
                                                }
                                            }

                                        }
                                    } else {
                                        if (i >= pairpipes.getPipesDescArrCount()) {
                                            outfile.write("|*");
                                        } else {
                                            if (i == syntcolumn) {
                                                outfile.write("|" + accomulatedsynt + "-");
                                            } else {
                                                outfile.write("|-");
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        accomulatedsynt = "";
                    }

                    if (paired) {
                        pairarr = null;
                        pairline = "";
                        pipespair = "";
                    }
                    outfile.write("\n");


                }

            } finally {

                if (modelreader != null) {
                    modelreader.close();
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

    public String pair2plain(String plainmodel) {
        String outputfile = this.getFile().toString() + ".pre";
        try {
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            if (!this.isWellFormed) {
                throw new Exception("Malformed pipes file");
            }

            int tokcolumn = this.getColumn("(tok|word).*");
            //System.out.println("Pairing column=" + tokcolumn + " " + this.pipes_desc_arr[tokcolumn]);

            BufferedReader modelreader = new BufferedReader(new FileReader(plainmodel));
            BufferedReader pipesreader = new BufferedReader(new FileReader(this.f));
            try {
                String line;
                char cmodel = '\0';
                int offset = -1;
                boolean readmodel = true;
                while ((line = pipesreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");
                    if (linearr.length >= this.pipes_desc_arr_count) {
                        String token = linearr[tokcolumn];
                        int token_offset = -1;
                        int token_leading_blanks = 0;
                        String paired_token = "";
                        for (int cn = 0; cn < token.length(); cn++) {
                            char cpipes = token.charAt(cn);
                            if (readmodel) {
                                if ((cmodel = (char) modelreader.read()) == -1) {
                                    throw new Exception("Premature end of model file");
                                }
                                offset++;
                            } else {
                                readmodel = true;
                            }

                            //System.out.println("offset=" + offset + " cmodel(" + cmodel + ") cpipes(" + cpipes + ")");
                            if (Character.toLowerCase(cpipes) == Character.toLowerCase(cmodel)) {
                                paired_token += cmodel;
                                if (token_offset == -1) {
                                    token_offset = offset;
                                }
                                // multi-dashes problem
                                if (cmodel == '-' && cn == token.length() - 1) {
                                    // read a new char (cmodel) if not end of file to check multi-dash
                                    if (!((cmodel = (char) modelreader.read()) == -1)) {
                                        readmodel = false;
                                        offset++;
                                        if (cmodel == '-') {
                                            cn--;
                                        }
                                        if (cmodel == ' ' || cmodel == '\n' || cmodel == '\r' || cmodel == '\t') {
                                            cn++;
                                            readmodel = true;
                                        }
                                    }
                                }

                            } else {
                                //if (cmodel == ' ' || cmodel == '\n' || cmodel == '\r') {
                                if (cmodel == ' ') {
                                    cn--;
                                    if (token_offset == -1) {
                                        token_leading_blanks++;
                                    }
                                } else {
                                    // Special for quotes
                                    if (cmodel == '"' && ((cpipes == '`') || (cpipes == '\''))) {
                                        if (cn + 1 < token.length() && cpipes == token.charAt(cn + 1)) {
                                            cn += 2;
                                            paired_token += cmodel;
                                        }
                                    } else {
                                        if (((cmodel == '\'' || cmodel == '`') && (cpipes == '`' || cpipes == '\'')) || (cmodel == '—') || (cmodel == '£')) {
                                            paired_token += cmodel;
                                        } else {

                                            throw new Exception("Distinct chars " + "offset=" + offset + " cmodel(" + cmodel + ") cpipes(" + cpipes + ")");

                                        }
                                    }
                                }
                            }
                        }

                        //System.out.print("paired output: ");
                        for (int i = 0; i < linearr.length - 1; i++) {
                            if (i == tokcolumn) {
                                //System.out.print(paired_token + "|" + offset + "|");
                                //System.out.print(paired_token + "|" + token_leading_blanks + "|");
                                outfile.write(paired_token + "|" + token_leading_blanks + "|");
                            } else {
                                //System.out.print(linearr[i] + "|");
                                outfile.write(linearr[i] + "|");
                            }
                        }
                        //System.out.println(linearr[linearr.length - 1]);
                        outfile.write(linearr[linearr.length - 1] + "\n");


                    } else { // newline new sentence
                        //System.out.println("cmodel(" + cmodel + ")");
                        if (Character.toLowerCase(cmodel) != '\n' && Character.toLowerCase(cmodel) != '\r') {
                            if ((cmodel = (char) modelreader.read()) != (char) -1) {
                                offset++;
                                if (Character.toLowerCase(cmodel) != '\n' && Character.toLowerCase(cmodel) != '\r') {
                                    throw new Exception("End of line not found (n) " + "offset=" + offset + ". cmodel(" + cmodel + ") found instead.");
                                } else {
                                    if (Character.toLowerCase(cmodel) == '\r') {
                                        if ((cmodel = (char) modelreader.read()) != (char) -1) {
                                            offset++;
                                            if (Character.toLowerCase(cmodel) != '\n') {
                                                throw new Exception("End of line not found (rn)" + "offset=" + offset + ". cmodel(" + cmodel + ") found instead.");
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (Character.toLowerCase(cmodel) == '\r') {
                                if ((cmodel = (char) modelreader.read()) != (char) -1) {
                                    offset++;
                                    if (Character.toLowerCase(cmodel) != '\n') {
                                        throw new Exception("End of line not found (rn) " + "offset=" + offset + ". cmodel(" + cmodel + ") found instead.");
                                    }
                                }
                            }
                        }

                        //System.out.println("paired output: " + line);
                        outfile.write(line + "\n");
                    }


                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (modelreader != null) {
                    modelreader.close();
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

    public String merge_tok_n_xml(String tmlfile, String root_tag, String elements_re, String attribs_re, String mergeattrib) {
        String outputfile = this.getFile().toString() + "-annotationKey";
        if (!elements_re.equals(".*")) {
            outputfile += "-" + elements_re;
        }
        if (!attribs_re.equals(".*")) {
            outputfile += "-" + attribs_re.replaceAll("([.]?\\*|[\"=])", "").replace('|', '_');
        }
        if (mergeattrib != null) {
            outputfile += "-" + mergeattrib;
        }

        try {
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            if (!this.isWellFormed) {
                throw new Exception("Malformed pipes file");
            }

            int tokcolumn = this.getColumn("(token|word).*");
            int last_desc_column = this.getLastDescColumn();
            boolean hasRoot_tag = false;
            char cxml = '\0';
            String line;
            String tag = "", attribs = "-", inTag = "", inAttribs = "-";
            //boolean closingtag = false;
            char BIO = 'O';

            BufferedReader xmlreader = new BufferedReader(new FileReader(tmlfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(this.f));
            try {
                // find root tag
                while (true) {
                    if ((cxml = (char) xmlreader.read()) == -1) {
                        throw new Exception("Premature end of model file");
                    }
                    if (cxml == '<') {
                        if ((cxml = (char) xmlreader.read()) == -1) {
                            throw new Exception("Premature end of model file");
                        }
                        do {
                            tag += cxml;
                            if ((cxml = (char) xmlreader.read()) == -1) {
                                throw new Exception("Premature end of model file");
                            }
                        } while (cxml != '>');
                        if (tag.equalsIgnoreCase(root_tag)) {
                            hasRoot_tag = true;
                            break;
                        }
                        tag = "";

                    }
                    //System.err.print(cxml);
                }

                if (!hasRoot_tag) {
                    throw new Exception("Root tag " + root_tag + " not found");
                }

                tag = "";
                cxml = '\0';

                while ((line = pipesreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");
                    if (linearr.length >= this.pipes_desc_arr_count) {
                        //System.out.println(line);
                        String token = linearr[tokcolumn];
                        boolean interTokenTag = false;
                        boolean findtokenIter = false;
                        boolean delayed_closing = false;
                        char prevxmlchar = 'x';
                        char prevprevxmlchar = 'x';
                        for (int cn = 0; cn < token.length(); cn++) {
                            char cpipes = token.charAt(cn);
                            prevprevxmlchar = prevxmlchar;
                            prevxmlchar = cxml;
                            if ((cxml = (char) xmlreader.read()) == -1) {
                                throw new Exception("Premature end of model file");
                            }
                            //System.err.println("cxml(" + cxml + ") cpipes(" + cpipes + "," + cn + ") "+inTag);
                            if (Character.toLowerCase(cpipes) != Character.toLowerCase(cxml)) {
                                if (cxml == ' ' || cxml == '\n' || cxml == '\r' || cxml == '\t') {
                                    cn--;
                                    //System.err.println("blank found cn="+cn);
                                } else {
                                    // tags handling
                                    if (cxml == '<') {
                                        if (cn != 0) {
                                            interTokenTag = true;
                                        }
                                        cn--;
                                        while (((cxml = (char) xmlreader.read()) != (char) -1) && cxml != '>') {
                                            tag += cxml;
                                        }
                                        tag = tag.trim();
                                        if (tag.indexOf(' ') != -1) {
                                            attribs = tag.substring(tag.indexOf(' ') + 1);
                                            tag = tag.substring(0, tag.indexOf(' '));
                                        }

                                        //System.err.println("tag=" + tag + " attribs=" + attribs);
                                        if (tag.matches("(?i)" + elements_re) && !tag.startsWith("/")) {
                                            findtokenIter = true;

                                            //System.err.println("LOOKING opening tag=" + tag + " attribs=" + attribs);
                                            if (interTokenTag) {
                                                System.err.println("Inter-token (" + cn + ") tag consider manual tokenizing: " + token);
                                            }
                                            if (!inTag.equals("")) {
                                                throw new Exception("Nested tags (" + tag + "/" + inTag + ") consider manual correction");
                                            }

                                            inTag = tag;
                                            inAttribs = attribs;
                                            tag = "";
                                            attribs = "-";
                                            BIO = 'B';

                                            if (!inAttribs.matches("(?i)" + attribs_re)) {
                                                BIO = 'O';
                                                inTag = "";
                                                inAttribs = "-";
                                                findtokenIter = false;
                                                interTokenTag = false;
                                            }

                                            if (mergeattrib != null) {
                                                String tmpattrib = inAttribs.substring(inAttribs.indexOf(mergeattrib + "=")).substring(mergeattrib.length() + 1);
                                                tmpattrib = tmpattrib.replace("\"", "");
                                                if (tmpattrib.indexOf(' ') != -1) {
                                                    tmpattrib = tmpattrib.substring(0, tmpattrib.indexOf(' '));
                                                }
                                                inTag = inTag + "+" + tmpattrib;
                                            }

                                            /*
                                            if (inTag.equals("EVENT")) {
                                            inAttribs = inAttribs.substring(inAttribs.indexOf("class=")).substring(6);
                                            inAttribs = inAttribs.replace("\"", "");
                                            if (inAttribs.indexOf(' ') != -1) {
                                            inAttribs = inAttribs.substring(0, inAttribs.indexOf(' '));
                                            }
                                            }*/

                                        } else {
                                            interTokenTag = false;
                                            /*if (tag.contains("TIMEX3") && !tag.matches("/" + inTag)) {
                                            System.err.println("problema:" + tag + " intag:" + inTag);
                                            System.exit(1);
                                            }*/
                                        }

                                        // check if closing                                       
                                        if (tag.matches("/.*")) {
                                            String check = inTag;
                                            if (mergeattrib != null && inTag.matches(".+\\+.+")) {
                                                check = inTag.substring(0, inTag.indexOf('+'));
                                            }
                                            if (tag.matches("/" + "(?i)" + check)) {
                                                if (findtokenIter) {
                                                    // safe for empty tags (events_4_instances and timex3_4_durations)
                                                    if (cn >= 0) {
                                                        System.err.println("Inter Token end of tag (" + inTag + ") cn=" + cn + " " + line);
                                                        delayed_closing = true;
                                                    } else {
                                                        BIO = 'O';
                                                        inTag = "";
                                                        inAttribs = "-";
                                                        findtokenIter = false;
                                                        interTokenTag = false;
                                                    }
                                                } else {
                                                    //System.err.println("closing tag=" + inTag);
                                                    BIO = 'O';
                                                    inTag = "";
                                                }

                                            }
                                        }

                                        // check if end root_tag
                                        if (tag.matches("/" + "(?i)" + root_tag)) {
                                            System.err.println("closing root_tag=" + root_tag);
                                            // do something
                                            // it never reaches this because tok file ends before.
                                        }
                                        tag = "";
                                        attribs = "-";

                                    } else {
                                        // escaped & < >
                                        if (cxml == '&' || (prevxmlchar == '&' && cxml == 'a')  || (prevprevxmlchar == ';' && prevxmlchar == ' ' && cxml == 'a')) {
                                            cn--;
                                            while (((cxml = (char) xmlreader.read()) != (char) -1) && cxml != ';') {
                                                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                                                    System.err.println("Reading XML escaped char in: " + token);
                                                }
                                            }
                                        } else {
                                            throw new Exception("Distinct chars cxml(" + cxml + ") cpipes(" + cpipes + ")");
                                        }
                                    }

                                }
                            }
                        }

                        //System.out.print("paired output: ");


                        for (int i = 0; i < linearr.length - 1; i++) {
                            // There are roles columns in the sentence
                            if (i == last_desc_column) {
                                outfile.write(linearr[i] + "|" + BIO);
                                if (BIO != 'O') { // && !inTag.equals("")
                                    outfile.write("-" + inTag);
                                    //System.err.println(BIO+"-" + inTag);
                                }
                                outfile.write("|" + inAttribs + "|");
                                if (BIO == 'B') {
                                    BIO = 'I';
                                    inAttribs = "-";
                                }
                            } else {
                                outfile.write(linearr[i] + "|");
                            }
                        }

                        // There arent roles columns in the sentences
                        if (linearr.length - 1 == last_desc_column) {
                            outfile.write(linearr[linearr.length - 1] + "|" + BIO);
                            if (BIO != 'O') { // && !inTag.equals("")
                                outfile.write("-" + inTag);
                                //System.err.println(BIO+"-" + inTag);
                            }
                            outfile.write("|" + inAttribs);
                            if (BIO == 'B') {
                                BIO = 'I';
                                inAttribs = "-";
                            }
                        } else {
                            outfile.write(linearr[linearr.length - 1]);
                        }
                            if (delayed_closing) {
                                BIO = 'O';
                                inTag = "";
                                inAttribs = "-";
                                findtokenIter = false;
                                interTokenTag = false;
                                delayed_closing = false;
                            }
                        outfile.write("\n");


                    } else {
                        if (!inTag.equals("")) {
                            throw new Exception("Broken tag: " + inTag + " at the end of the file/sentence");
                        }
                        outfile.write(line + "\n");
                    }


                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                if (xmlreader != null) {
                    xmlreader.close();
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

    public static String IOB2check(PipesFile pipesfile) {
        return IOB2check(pipesfile, pipesfile.getLastDescColumn());
    }

    public static String IOB2check(PipesFile pipesfile, int IOB2column) {

        String outputfile = null;
        try {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Cheking IOB2...");
            }
            outputfile = pipesfile.getFile().getCanonicalPath() + "-IOB2checked";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile.getFile()));

            try {
                String pipesline;
                String[] pipesarr = null;
                String previousIOB2 = "O";
                int linen = 0;
                String previousSENT = "O";

                int toknumcol = pipesfile.getColumn("tok-num");
                int sentnumcol = pipesfile.getColumn("sent-num");

                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\|");
                    // take into account sentences and newlines (only new format n)
                    if ((sentnumcol != -1 && !pipesarr[sentnumcol].equals(previousSENT)) || (sentnumcol != -1 && pipesarr[toknumcol].matches("[^-]+-[^n]*n.*"))) {
                        previousIOB2 = "O";
                    }
                    if (pipesarr.length > 1 && previousIOB2.equals("O") && pipesarr[IOB2column].startsWith("I-")) {
                        int i = 0;
                        for (i = 0; i < (pipesarr.length - 1); i++) {
                            if (i != IOB2column) {
                                outfile.write(pipesarr[i] + "|");
                            } else {
                                outfile.write(pipesarr[i].replaceFirst("I-", "B-") + "|");
                            }
                        }
                        if (i == IOB2column) {
                            //System.out.println(pipesarr[i].replaceFirst("I-", "B-"));
                            outfile.write(pipesarr[i].replaceFirst("I-", "B-"));
                        } else {
                            outfile.write(pipesarr[i]);
                        }
                        outfile.write("\n");
                        previousIOB2 = "B";
                    } else {
                        outfile.write(pipesline + "\n");
                        if (pipesarr.length > 1) {
                            previousIOB2 = pipesarr[IOB2column].substring(0, 1);
                        } else {
                            previousIOB2 = "O";
                        }
                    }

                    if (sentnumcol != -1) {
                        previousSENT = pipesarr[sentnumcol];
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
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * The priority is on the file of the first paramenter
     *
     */
    public static String merge_pipes(String primary, String secondary) {
        String outputfile = null;

        // basic check
        if (primary == null && secondary == null) {
            return null;
        }
        if (primary == null) {
            return secondary;
        }
        if (secondary == null) {
            return primary;
        }

        try {
            outputfile = primary + "-merged";

            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader extentsreader = new BufferedReader(new FileReader(primary));
            BufferedReader extentsreader2 = new BufferedReader(new FileReader(secondary));


            try {
                String extentline;
                String[] extentarr = null;
                String extentline2;
                String[] extentarr2 = null;
                PipesFile keypipes = new PipesFile(primary);
                keypipes.isWellFormedOptimist();
                int iob2col1 = keypipes.getColumn("element\\(IOB2\\)");
                keypipes = new PipesFile(secondary);
                keypipes.isWellFormedOptimist();
                int iob2col2 = keypipes.getColumn("element\\(IOB2\\)");

                boolean firstO = true;

                while ((extentline = extentsreader.readLine()) != null) {
                    extentarr = extentline.split("\\|");

                    if ((extentline2 = extentsreader2.readLine()) == null) {
                        throw new Exception("Secondary file ended prematurely.");
                    }
                    extentarr2 = extentline2.split("\\|");

                    if (!extentarr[iob2col1].equals("O") && !extentarr2[iob2col2].equals("O")) {
                        System.err.println("Error merging pipes files!! overlaping elements.\n" +extentline+"\n"+extentline2);
                        System.err.println("Ignoring event");
                    }

                    if (!extentarr[iob2col1].equals("O")) {
                        if (iob2col1 == (extentarr.length - 1)) {
                            outfile.write(extentline + "|-\n");
                        } else {
                            outfile.write(extentline + "\n");
                        }
                        firstO = true;
                    } else {
                        if (firstO && extentarr2[iob2col2].startsWith("I-")) {
                            String tmpelem = extentarr2[iob2col2].substring(2);
                            extentline2 = extentline2.replaceAll("\\|I-" + tmpelem, "\\|B-" + tmpelem);
                        }
                        if (iob2col2 == (extentarr2.length - 1)) {
                            outfile.write(extentline2 + "|-\n");
                        } else {
                            outfile.write(extentline2 + "\n");
                        }
                        firstO = false;
                    }

                }

            } finally {
                if (extentsreader != null) {
                    extentsreader.close();
                }
                if (extentsreader2 != null) {
                    extentsreader2.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * Return column number or -1 if colname_re does not exist
     * 
     * @param colname_re
     * @return
     */
    public int getColumn(String colname_re) {
        try {
            //System.out.println(colname_re+" "+this.pipes_desc_arr_count);
            for (int i = 0; i < this.pipes_desc_arr_count; i++) {
                if (this.pipes_desc_arr[i].matches(colname_re)) {
                    return i;
                }
            }
            //throw new Exception("Column " + colname_re + " not found");
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return -1;
        }
        return -1;
    }

    public int getLastDescColumn() {
        return this.pipes_desc_arr_count - 1;
    }

    public String saveColumnFile(String colname) {
        String outputfile = this.getFile().getAbsolutePath() + "." + colname;
        int col = this.getColumn(colname);

        try {
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            if (!this.isWellFormed) {
                throw new Exception("Malformed pipes file");
            }

            BufferedReader pipesreader = new BufferedReader(new FileReader(this.f));
            try {
                String line;
                String outputline = "";
                int current_sentence = 0; // (from 0 to n)
                while ((line = pipesreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");
                    if (linearr.length >= this.pipes_desc_arr_count) {
                        outfile.write(linearr[col] + "\n");
                    } else {
                        current_sentence++;
                        outfile.write(outputline + "\n");
                        outputline = "";
                    }

                }
                // write last sentence
                if (!outputline.equals("")) {
                    outfile.write(outputline + "\n");
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
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;



    }

    /**
     * Builts the statistics of the NLPFile in a Stat Object which
     * can be empty or filled with other stats 
     *
     * @param st
     * @param params
     */
    public void fillStats(Stat st, String params) { //File statsf,
        try {
            BufferedReader pipesreader = new BufferedReader(new FileReader(this.f));
            try {
                String line;
                while ((line = pipesreader.readLine()) != null) {
                    String[] linearr = line.split("\\|");
                    if (linearr.length >= this.pipes_desc_arr_count) {
                        st.addData(linearr);
                    }
                }
                //st.print();
            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
                //return st;
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            //return null;
        }
    }

    @Override
    public String toPlain(String filename) {
        throw new UnsupportedOperationException("toPlain not applicable to this type of file");
    }
}
