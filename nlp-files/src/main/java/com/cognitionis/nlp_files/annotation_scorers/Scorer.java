package com.cognitionis.nlp_files.annotation_scorers;

import java.io.*;
import com.cognitionis.nlp_files.PipesFile;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
// NEEDS CORRECT BIO Element ANNOTATION PLUS, ATTRIBUTES ANNOTATED IN B...
// annotation scoring possibilities are
// ELEM correct, missing, spurious
// TEXT correct, missing, spurious, incorrect
// ATTRIBUTES over correct instances check if they are correctly classified...
//      CLASS/TYPE ATTRIBUTES WILL BE TREATED IN A SPECIAL WAY

public class Scorer {


    // LA CLAVE pot retornar un objecte score del qual jo puc adquirir els differents scores/measures implementades..
    public Score score(PipesFile annot, String key, int elembiocol, int attrscol) {
        // recorrer ambdos fitxers de forma síncrona i anar calculat els scores segons el constructor
        Score score = null;
        int numline = 0;
        try {
            BufferedReader keyreader = new BufferedReader(new FileReader(key));
            BufferedReader annotreader = new BufferedReader(new FileReader(annot.getFile()));

            score = new Score(key, annot.getFile().getCanonicalPath());
            Judgement keyjudgement = null;
            Judgement annotjudgement = null;
            String keyline, annotline;
            while ((keyline = keyreader.readLine()) != null) {
                numline++;
                if ((annotline = annotreader.readLine()) == null) {
                    throw new Exception("Scored annotation ended before key annotation");
                }
                // check end of sentence and save judgements if needed
                if (keyline.equals("|")) {
                    if (annotline.equals("|")) {
                        if (annotjudgement != null) {
                            score.add(annotjudgement);
                            annotjudgement = null;
                        }
                        if (keyjudgement != null) {
                            score.add(keyjudgement);
                            keyjudgement = null;
                        }
                        continue;
                    } else {
                        throw new Exception("Unaligned sentences in: annotation and key files");
                    }
                }

                String[] keyarr = keyline.split("\\|");
                String[] annotarr = annotline.split("\\|");

                if (keyarr.length < annot.getPipesDescArrCount() || annotarr.length < annot.getPipesDescArrCount()) {
                    System.err.println("Malformed pipesFile line: Has less columns than description file");
                    elembiocol=keyarr.length-1;
                    attrscol=-1;
                }
                if ((!annotarr[elembiocol].equals("O") && annotarr[elembiocol].length() < 3) || (!keyarr[elembiocol].equals("O") && keyarr[elembiocol].length() < 3)) {
                    System.err.println("Malformed BIO format: " + annotarr[elembiocol]);
                }


                // MAIN PART -------------------------------------------------------------------

                // manage ANNOT judgement
                if (!annotarr[elembiocol].equals("O")) {
                    String annotelement = annotarr[elembiocol].substring(2);
                    // new annot element begins
                    if (annotarr[elembiocol].startsWith("B-")) {
                        String annotattrs="";
                        if(attrscol!=-1){
                            annotattrs=annotarr[attrscol];
                        }
                        // if there was another one, not aligned with key, add it
                        if (annotjudgement != null) {
                            score.add(annotjudgement);
                            annotjudgement = null;
                        }
                        // if there is no key element to align -> new spur
                        // or if there was already aligned and was correct and keyarr !=  now it is incorrect
                        if (keyjudgement == null || keyjudgement.isAligned()) {
                            annotjudgement = new Judgement(Judgement.judgements.valueOf("spur").ordinal(), annotelement, annotattrs, numline, keyline, annotline);
                            if(keyjudgement!=null && keyarr[elembiocol].startsWith("I-") && keyjudgement.getJudgement_str().equals("corr")){
                                keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            }
                        } // there could be an alignement
                        else {
                            // New alignement (always inco) (no new judgement needed -> change the previouse one to inco)
                            if (annotelement.equals(keyjudgement.getElement()) && keyarr[elembiocol].startsWith("I-")) {
                                keyjudgement.add_alt_attribs(annotattrs, "annot");
                                keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal()); // miss 2 inco
                                //keyjudgement.updateNumline(numline);
                            } // alignement not possible
                            else {
                                annotjudgement = new Judgement(Judgement.judgements.valueOf("spur").ordinal(), annotelement, annotattrs, numline, keyline, annotline);
                            }
                        }
                    } else {
                        // annot element continuation
                        if (annotarr[elembiocol].startsWith("I-")) {
                            // not aligned -> extend judgement
                            if (annotjudgement != null) {
                                if (annotjudgement.getElement().equals(annotelement)) {
                                    annotjudgement.extendJudgement(keyline, annotline);
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + annotjudgement.getElement());
                                }
                            } // aligned
                            else { // temporally align in annotjudgement. Must be possible (corr/inco)
                                if (keyjudgement != null) {
                                    if (keyjudgement.getElement().equals(annotelement) && keyjudgement.isAligned()) {
                                        if (keyarr[elembiocol].startsWith("B-")) {
                                            keyjudgement.changeAndExtendJudgement(Judgement.judgements.valueOf("inco").ordinal(), keyline, annotline);
                                        } else {
                                            keyjudgement.extendJudgement(keyline, annotline);
                                        }
                                        annotjudgement = keyjudgement;
                                        keyjudgement = null;
                                    } else {
                                        throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + keyjudgement.getElement());
                                    }
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected O");
                                }
                            }
                        } else {
                            throw new Exception("Malformed BIO format: " + annotarr[elembiocol]);
                        }
                    }
                } else { // equals("O")
                    // Not aligned -> add spurious
                    if (annotjudgement != null) {
                        score.add(annotjudgement);
                        annotjudgement = null;
                    }
                    // Aligned -> change if needed and move to keyjudgement
                    if (keyjudgement != null && keyjudgement.isAligned()) {
                        if (keyarr[elembiocol].startsWith("I-") && keyjudgement.getJudgement_str().equals("corr")) {
                            keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                        }
                        annotjudgement = keyjudgement;
                        keyjudgement = null;
                    }
                }





                // manage KEY judgement
                if (!keyarr[elembiocol].equals("O")) { // correct, missing
                    String keyelement = keyarr[elembiocol].substring(2);
                    // new annot element begins
                    if (keyarr[elembiocol].startsWith("B-")) {
                        String keyattrs="";
                        if(attrscol!=-1){
                            keyattrs=keyarr[attrscol];
                        }
                        // if there was another one  (and then not aligned with annot) -> add judgement
                        if (keyjudgement != null) {
                            score.add(keyjudgement);
                            keyjudgement = null;
                        }

                        // if there is no key element to align -> new miss
                        if (annotjudgement == null || annotjudgement.isAligned()) {
                            keyjudgement = new Judgement(Judgement.judgements.valueOf("miss").ordinal(), keyelement, keyattrs, numline, keyline, annotline);
                        } // there could be an alignement
                        else {
                            // New alignement (no new judgement needed)
                            if (keyelement.equals(annotjudgement.getElement())) {
                                annotjudgement.add_alt_attribs(keyattrs, "key");
                                // totally aligned -> change spur to correct
                                if (annotarr[elembiocol].startsWith("B-")) { // from spur 2 corr
                                    annotjudgement.changeJudgement(Judgement.judgements.valueOf("corr").ordinal());
                                } else { // startsWith("I-") -> change spur to incorrect
                                    annotjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                                    annotjudgement.updateNumline(numline);
                                }
                                keyjudgement = annotjudgement;
                                annotjudgement = null;
                            } // already aligned or alignement not possible
                            else {
                                keyjudgement = new Judgement(Judgement.judgements.valueOf("miss").ordinal(), keyelement, keyattrs, numline, keyline, annotline);
                            }
                        }
                    } else {
                        // key element continuation
                        if (keyarr[elembiocol].startsWith("I-")) {
                            // not aligned -> extend judgement
                            if (keyjudgement != null) {
                                if (keyjudgement.getElement().equals(keyelement)) {
                                    keyjudgement.extendJudgement(keyline, annotline);
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + annotjudgement.getElement());
                                }
                            } // aligned
                            else { // temporally align in annotjudgement. Must be possible (corr/inco)
                                if (annotjudgement != null) {
                                    if (annotjudgement.getElement().equals(keyelement) && annotjudgement.isAligned()) {
                                        keyjudgement = annotjudgement;
                                        annotjudgement = null;
                                    } else {
                                        throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + keyjudgement.getElement());
                                    }
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected O");
                                }
                            }
                        } else {
                            throw new Exception("Malformed BIO format: " + annotarr[elembiocol]);
                        }
                    }
                } else { // equals("O")
                    // Not aligned -> add missing
                    if (keyjudgement != null) {
                        score.add(keyjudgement);
                        keyjudgement = null;
                    }
                    // Aligned -> change if needed, move to annotjudgement or save&empty
                    if (annotjudgement != null && annotjudgement.isAligned()) {
                        if (annotarr[elembiocol].startsWith("I-") && annotjudgement.getJudgement_str().equals("corr")) {
                            annotjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            //annotjudgement.updateNumline(numline);
                            keyjudgement = annotjudgement;
                            annotjudgement = null;
                        } else {
                            if (annotarr[elembiocol].equals("O")) {
                                score.add(annotjudgement);
                                annotjudgement = null;
                            }
                        }
                    }
                }
                // -----------------------------------------------------------------------------------
                // Token level score...
                if(keyarr[elembiocol].equals("O")){
                    if(annotarr[elembiocol].equals("O")){
                        score.addtn();
                    }else{
                        String aelem=annotarr[elembiocol].substring(2);
                        score.addfp(aelem);
                        score.addtnrest(aelem);
                    }
                }else{
                    String kelem=keyarr[elembiocol].substring(2);
                    if(annotarr[elembiocol].equals("O")){
                        score.addfn(kelem);
                        score.addtnrest(kelem);
                    }else{
                        String aelem=annotarr[elembiocol].substring(2);
                        if(kelem.equals(aelem)){
                            score.addtp(kelem);
                            score.addtnrest(aelem);
                        }else{ // both are != O then we add one fp and one fn
                            score.addfn(kelem);
                            score.addfp(aelem);
                        }
                    }
                }
            }
            if ((annotline = annotreader.readLine()) != null) {
                throw new Exception("Key annotation ended before scored annotation");
            }
        } catch (Exception e) {
            System.err.println("\nErrors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + " (line " + numline + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        if(score!=null){
            score.sort_judgements_by_numline();
        }

        return score;
    }

    public Score score_recclass(PipesFile annot, String key, int elembiocol, int attrscol) {
        // recorrer ambdos fitxers de forma síncrona i anar calculat els scores segons el constructor
        Score score = null;
        int numline = 0;
        try {
            BufferedReader keyreader = new BufferedReader(new FileReader(key));
            BufferedReader annotreader = new BufferedReader(new FileReader(annot.getFile()));
            score = new Score(key, annot.getFile().getCanonicalPath());
            Judgement keyjudgement = null;
            Judgement annotjudgement = null;
            String keyline, annotline;
            while ((keyline = keyreader.readLine()) != null) {
                numline++;
                if ((annotline = annotreader.readLine()) == null) {
                    throw new Exception("Scored annotation ended before key annotation");
                }
                // check end of sentence and save judgements if needed
                if (keyline.equals("|")) {
                    if (annotline.equals("|")) {
                        if (annotjudgement != null) {
                            score.add(annotjudgement);
                            annotjudgement = null;
                        }
                        if (keyjudgement != null) {
                            score.add(keyjudgement);
                            keyjudgement = null;
                        }
                        continue;
                    } else {
                        throw new Exception("Unaligned sentences in: annotation and key files");
                    }
                }
                String[] keyarr = keyline.split("\\|");
                String[] annotarr = annotline.split("\\|");
                if (keyarr.length < annot.getPipesDescArrCount() || annotarr.length < annot.getPipesDescArrCount()) {
                    System.err.println("Malformed pipesFile line: Has less columns than description file");
                    elembiocol=keyarr.length-1;
                    attrscol=-1;
                }
                if ((!annotarr[elembiocol].equals("O") && annotarr[elembiocol].length() < 3) || (!keyarr[elembiocol].equals("O") && keyarr[elembiocol].length() < 3)) {
                    System.err.println("Malformed BIO format: " + annotarr[elembiocol]);
                }
                // MAIN PART -------------------------------------------------------------------
                // manage ANNOT judgement
                if (!annotarr[elembiocol].equals("O")) {
                    String annotelement = annotarr[elembiocol].substring(2).split("-")[0];
                    // new annot element begins
                    if (annotarr[elembiocol].startsWith("B-")) {
                        String annotattrs="class=\""+annotarr[elembiocol].substring(2).split("-")[1]+"\"";
                        // if there was another one, not aligned with key, add it
                        if (annotjudgement != null) {
                            score.add(annotjudgement);
                            annotjudgement = null;
                        }
                        // if there is no key element to align -> new spur
                        // or if there was already aligned and was correct and keyarr !=  now it is incorrect
                        if (keyjudgement == null || keyjudgement.isAligned()) {
                            annotjudgement = new Judgement(Judgement.judgements.valueOf("spur").ordinal(), annotelement, annotattrs, numline, keyline, annotline);
                            if(keyjudgement!=null && keyarr[elembiocol].startsWith("I-") && keyjudgement.getJudgement_str().equals("corr")){
                                keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            }
                        } // there could be an alignement
                        else {
                            // New alignement (always inco) (no new judgement needed -> change the previouse one to inco)
                            if (annotelement.equals(keyjudgement.getElement()) && keyarr[elembiocol].startsWith("I-")) {
                                keyjudgement.add_alt_attribs(annotattrs, "annot");
                                keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal()); // miss 2 inco
                                //keyjudgement.updateNumline(numline);
                            } // alignement not possible
                            else {
                                annotjudgement = new Judgement(Judgement.judgements.valueOf("spur").ordinal(), annotelement, annotattrs, numline, keyline, annotline);
                            }
                        }
                    } else {
                        // annot element continuation
                        if (annotarr[elembiocol].startsWith("I-")) {
                            // not aligned -> extend judgement
                            if (annotjudgement != null) {
                                if (annotjudgement.getElement().equals(annotelement)) {
                                    annotjudgement.extendJudgement(keyline, annotline);
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + annotjudgement.getElement());
                                }
                            } // aligned
                            else { // temporally align in annotjudgement. Must be possible (corr/inco)
                                if (keyjudgement != null) {
                                    if (keyjudgement.getElement().equals(annotelement) && keyjudgement.isAligned()) {
                                        if (keyarr[elembiocol].startsWith("B-")) {
                                            keyjudgement.changeAndExtendJudgement(Judgement.judgements.valueOf("inco").ordinal(), keyline, annotline);
                                        } else {
                                            keyjudgement.extendJudgement(keyline, annotline);
                                        }
                                        annotjudgement = keyjudgement;
                                        keyjudgement = null;
                                    } else {
                                        throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + keyjudgement.getElement());
                                    }
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected O");
                                }
                            }
                        } else {
                            throw new Exception("Malformed BIO format: " + annotarr[elembiocol]);
                        }
                    }
                } else { // equals("O")
                    // Not aligned -> add spurious
                    if (annotjudgement != null) {
                        score.add(annotjudgement);
                        annotjudgement = null;
                    }
                    // Aligned -> change if needed and move to keyjudgement
                    if (keyjudgement != null && keyjudgement.isAligned()) {
                        if (keyarr[elembiocol].startsWith("I-") && keyjudgement.getJudgement_str().equals("corr")) {
                            keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                        }
                        annotjudgement = keyjudgement;
                        keyjudgement = null;
                    }
                }
                // manage KEY judgement
                if (!keyarr[elembiocol].equals("O")) { // correct, missing
                    String keyelement = keyarr[elembiocol].substring(2).split("-")[0];
                    // new annot element begins
                    if (keyarr[elembiocol].startsWith("B-")) {
                        String keyattrs="class=\""+keyarr[elembiocol].substring(2).split("-")[1]+"\"";
                        // if there was another one  (and then not aligned with annot) -> add judgement
                        if (keyjudgement != null) {
                            score.add(keyjudgement);
                            keyjudgement = null;
                        }
                        // if there is no key element to align -> new miss
                        if (annotjudgement == null || annotjudgement.isAligned()) {
                            keyjudgement = new Judgement(Judgement.judgements.valueOf("miss").ordinal(), keyelement, keyattrs, numline, keyline, annotline);
                        } // there could be an alignement
                        else {
                            // New alignement (no new judgement needed)
                            if (keyelement.equals(annotjudgement.getElement())) {
                                annotjudgement.add_alt_attribs(keyattrs, "key");
                                // totally aligned -> change spur to correct
                                if (annotarr[elembiocol].startsWith("B-")) { // from spur 2 corr
                                    annotjudgement.changeJudgement(Judgement.judgements.valueOf("corr").ordinal());
                                } else { // startsWith("I-") -> change spur to incorrect
                                    annotjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                                    annotjudgement.updateNumline(numline);
                                }
                                keyjudgement = annotjudgement;
                                annotjudgement = null;
                            } // already aligned or alignement not possible
                            else {
                                keyjudgement = new Judgement(Judgement.judgements.valueOf("miss").ordinal(), keyelement, keyattrs, numline, keyline, annotline);
                            }
                        }
                    } else {
                        // key element continuation
                        if (keyarr[elembiocol].startsWith("I-")) {
                            // not aligned -> extend judgement
                            if (keyjudgement != null) {
                                if (keyjudgement.getElement().equals(keyelement)) {
                                    keyjudgement.extendJudgement(keyline, annotline);
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + annotjudgement.getElement());
                                }
                            } // aligned
                            else { // temporally align in annotjudgement. Must be possible (corr/inco)
                                if (annotjudgement != null) {
                                    if (annotjudgement.getElement().equals(keyelement) && annotjudgement.isAligned()) {
                                        keyjudgement = annotjudgement;
                                        annotjudgement = null;
                                    } else {
                                        throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected I-" + keyjudgement.getElement());
                                    }
                                } else {
                                    throw new Exception("Malformed BIO format: found " + annotarr[elembiocol] + " expected O");
                                }
                            }
                        } else {
                            throw new Exception("Malformed BIO format: " + annotarr[elembiocol]);
                        }
                    }
                } else { // equals("O")
                    // Not aligned -> add missing
                    if (keyjudgement != null) {
                        score.add(keyjudgement);
                        keyjudgement = null;
                    }
                    // Aligned -> change if needed, move to annotjudgement or save&empty
                    if (annotjudgement != null && annotjudgement.isAligned()) {
                        if (annotarr[elembiocol].startsWith("I-") && annotjudgement.getJudgement_str().equals("corr")) {
                            annotjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            //annotjudgement.updateNumline(numline);
                            keyjudgement = annotjudgement;
                            annotjudgement = null;
                        } else {
                            if (annotarr[elembiocol].equals("O")) {
                                score.add(annotjudgement);
                                annotjudgement = null;
                            }
                        }
                    }
                }
                // -----------------------------------------------------------------------------------
                // Token level score...
                if(keyarr[elembiocol].equals("O")){
                    if(annotarr[elembiocol].equals("O")){
                        score.addtn();
                    }else{
                        String aelem=annotarr[elembiocol].substring(2).split("-")[0];
                        score.addfp(aelem);
                        score.addtnrest(aelem);
                    }
                }else{
                    String kelem=keyarr[elembiocol].substring(2).split("-")[0];
                    if(annotarr[elembiocol].equals("O")){
                        score.addfn(kelem);
                        score.addtnrest(kelem);
                    }else{
                        String aelem=annotarr[elembiocol].substring(2).split("-")[0];
                        if(kelem.equals(aelem)){
                            score.addtp(kelem);
                            score.addtnrest(aelem);
                        }else{ // both are != O then we add one fp and one fn
                            score.addfn(kelem);
                            score.addfp(aelem);
                        }
                    }
                }
            }
            if ((annotline = annotreader.readLine()) != null) {
                throw new Exception("Key annotation ended before scored annotation");
            }
        } catch (Exception e) {
            System.err.println("\nErrors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + " (line " + numline + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return score;
    }


    public Score score_class(String annot, String key, int classcol) {
        // recorrer ambdos fitxers de forma síncrona i anar calculat els scores segons el constructor
        Score score = null;
        int numline = 0;
        int classcolk=classcol;
        try {
            BufferedReader keyreader = new BufferedReader(new FileReader(key));
            BufferedReader annotreader = new BufferedReader(new FileReader(annot));

            score = new Score(key, annot);
            Judgement judgement = null;
            String keyline, annotline;



            while ((keyline = keyreader.readLine()) != null) {
                numline++;
                if ((annotline = annotreader.readLine()) == null) {
                    throw new Exception("Scored annotation ended before key annotation");
                }
                // check end of sentence and save judgements if needed

                String[] keyarr = keyline.split("\\|");
                String[] annotarr = annotline.split("\\|");

                if(classcol==-1){
                    classcol=annotarr.length - 1;
                    classcolk=keyarr.length - 1;
                }

                /*if (keyarr.length < annot.getPipesDescArrCount() || annotarr.length < annot.getPipesDescArrCount()) {
                    throw new Exception("Malformed pipesFile line ("+numline+"): Has less columns ("+annotarr.length+"|"+keyarr.length+") than description file ("+annot.getPipesDescArrCount()+")");
                }*/

                // MAIN PART -------------------------------------------------------------------
                judgement = new Judgement(Judgement.judgements.valueOf("corr").ordinal(), "class", "", numline, keyline, annotline);
                if(!annotarr[classcol].equals(keyarr[classcolk])){
                    judgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                    score.addfn("class");
                    score.addfp("class");
                }else{
                    score.addtp("class");
                }
                score.add(judgement);
                judgement = null;
            }
            if ((annotline = annotreader.readLine()) != null) {
                throw new Exception("Key annotation ended before scored annotation");
            }
        } catch (Exception e) {
            System.err.println("\nErrors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + " (line " + numline + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return score;

    }





    public void compare_scores(Score improved, Score base){
        Scomp scomp=new Scomp(improved,base);
        scomp.print();

    }





    // LA CLAVE pot retornar un objecte score del qual jo puc adquirir els differents scores/measures implementades..
    public Score autoscore(PipesFile annot, int keybiocol) {
        // recorrer ambdos fitxers de forma síncrona i anar calculat els scores segons el constructor
        Score score = null;
        int numline = 0;
        int annotbiocol=keybiocol+1;
        try {
            BufferedReader annotreader = new BufferedReader(new FileReader(annot.getFile()));

            score = new Score(annot.getFile().getCanonicalPath(), annot.getFile().getCanonicalPath());
            Judgement keyjudgement = null;
            Judgement annotjudgement = null;
            String annotline;
            while ((annotline = annotreader.readLine()) != null) {
                numline++;
                // check end of sentence and save judgements if needed
                if (annotline.equals("|") || annotline.equals("")) {
                        if (annotjudgement != null) {
                            score.add(annotjudgement);
                            annotjudgement = null;
                        }
                        if (keyjudgement != null) {
                            score.add(keyjudgement);
                            keyjudgement = null;
                        }
                        continue;
                }


                String[] annotarr = annotline.split("\\|");

                if(keybiocol==-1){
                    keybiocol=annotarr.length-2;
                    annotbiocol=keybiocol+1;
                }


                if (annotarr.length < annot.getPipesDescArrCount()) {
                    throw new Exception("Malformed pipesFile line: Has less columns than description file");
                }
                if ((!annotarr[keybiocol].equals("O") && annotarr[keybiocol].length() < 3) || (!annotarr[annotbiocol].equals("O") && annotarr[annotbiocol].length() < 3)) {
                    throw new Exception("Malformed BIO format (reading) ["+annotarr[keybiocol]+"] ["+annotarr[annotbiocol]+"]");
                }


                // MAIN PART -------------------------------------------------------------------

                // manage ANNOT judgement
                if (!annotarr[annotbiocol].equals("O")) {
                    String annotelement = annotarr[annotbiocol].substring(2);
                    // new annot element begins
                    if (annotarr[annotbiocol].startsWith("B-")) {
                        String annotattrs="*";
                        // if there was another one, not aligned with key, add it
                        if (annotjudgement != null) {
                            score.add(annotjudgement);
                            annotjudgement = null;
                        }
                        // if there is no key element to align -> new spur
                        // or if there was already aligned and was correct and keyarr !=  now it is incorrect
                        if (keyjudgement == null || keyjudgement.isAligned()) {
                            annotjudgement = new Judgement(Judgement.judgements.valueOf("spur").ordinal(), annotelement, annotattrs, numline, annotline, annotline);
                            if(keyjudgement!=null && annotarr[keybiocol].startsWith("I-") && keyjudgement.getJudgement_str().equals("corr")){
                                keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            }
                        } // there could be an alignement
                        else {
                            // New alignement (always inco) (no new judgement needed -> change the previouse one to inco)
                            if (annotelement.equals(keyjudgement.getElement()) && annotarr[keybiocol].startsWith("I-")) {
                                keyjudgement.add_alt_attribs(annotattrs, "annot");
                                keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            } // alignement not possible
                            else {
                                annotjudgement = new Judgement(Judgement.judgements.valueOf("spur").ordinal(), annotelement, annotattrs, numline, annotline, annotline);
                            }
                        }
                    } else {
                        // annot element continuation
                        if (annotarr[annotbiocol].startsWith("I-")) {
                            // not aligned -> extend judgement
                            if (annotjudgement != null) {
                                if (annotjudgement.getElement().equals(annotelement)) {
                                    annotjudgement.extendJudgement(annotline, annotline);
                                } else {
                                    throw new Exception("Malformed BIO format (annot): found " + annotarr[annotbiocol] + " expected I-" + annotjudgement.getElement());
                                }
                            } // aligned
                            else { // temporally align in annotjudgement. Must be possible (corr/inco)
                                if (keyjudgement != null) {
                                    if (keyjudgement.getElement().equals(annotelement) && keyjudgement.isAligned()) {
                                        if (annotarr[keybiocol].startsWith("B-")) {
                                            keyjudgement.changeAndExtendJudgement(Judgement.judgements.valueOf("inco").ordinal(), annotline, annotline);
                                        } else {
                                            keyjudgement.extendJudgement(annotline, annotline);
                                        }
                                        annotjudgement = keyjudgement;
                                        keyjudgement = null;
                                    } else {
                                        throw new Exception("Malformed BIO format (annot): found " + annotarr[annotbiocol] + " expected I-" + keyjudgement.getElement());
                                    }
                                } else {
                                    throw new Exception("Malformed BIO format (annot): found " + annotarr[annotbiocol] + " expected O");
                                }
                            }
                        } else {
                            throw new Exception("Malformed BIO format (annot): " + annotarr[annotbiocol]);
                        }
                    }
                } else { // equals("O")
                    // Not aligned -> add spurious
                    if (annotjudgement != null) {
                        score.add(annotjudgement);
                        annotjudgement = null;
                    }
                    // Aligned -> change if needed and move to keyjudgement
                    if (keyjudgement != null && keyjudgement.isAligned()) {
                        if (annotarr[keybiocol].startsWith("I-") && keyjudgement.getJudgement_str().equals("corr")) {
                            keyjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                        }
                        annotjudgement = keyjudgement;
                        keyjudgement = null;
                    }
                }





                // manage KEY judgement
                if (!annotarr[keybiocol].equals("O")) { // correct, missing
                    String keyelement = annotarr[keybiocol].substring(2);
                    // new annot element begins
                    if (annotarr[keybiocol].startsWith("B-")) {
                        String keyattrs="*";
                        // if there was another one  (and then not aligned with annot) -> add judgement
                        if (keyjudgement != null) {
                            score.add(keyjudgement);
                            keyjudgement = null;
                        }

                        // if there is no key element to align -> new miss
                        if (annotjudgement == null || annotjudgement.isAligned()) {
                            keyjudgement = new Judgement(Judgement.judgements.valueOf("miss").ordinal(), keyelement, keyattrs, numline, annotline, annotline);
                        } // there could be an alignement
                        else {
                            // New alignement (no new judgement needed)
                            if (keyelement.equals(annotjudgement.getElement())) {
                                annotjudgement.add_alt_attribs(keyattrs, "key");
                                // totally aligned -> change spur to correct
                                if (annotarr[annotbiocol].startsWith("B-")) { // from spur 2 corr
                                    annotjudgement.changeJudgement(Judgement.judgements.valueOf("corr").ordinal());
                                } else { // startsWith("I-") -> change spur to incorrect
                                    annotjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                                }
                                keyjudgement = annotjudgement;
                                annotjudgement = null;
                            } // already aligned or alignement not possible
                            else {
                                keyjudgement = new Judgement(Judgement.judgements.valueOf("miss").ordinal(), keyelement, keyattrs, numline, annotline, annotline);
                            }
                        }
                    } else {
                        // key element continuation
                        if (annotarr[keybiocol].startsWith("I-")) {
                            // not aligned -> extend judgement
                            if (keyjudgement != null) {
                                if (keyjudgement.getElement().equals(keyelement)) {
                                    keyjudgement.extendJudgement(annotline, annotline);
                                } else {
                                    throw new Exception("Malformed BIO format (key): found " + annotarr[keybiocol] + " expected I-" + annotjudgement.getElement());
                                }
                            } // aligned
                            else { // temporally align in annotjudgement. Must be possible (corr/inco)
                                if (annotjudgement != null) {
                                    if (annotjudgement.getElement().equals(keyelement) && annotjudgement.isAligned()) {
                                        keyjudgement = annotjudgement;
                                        annotjudgement = null;
                                    } else {
                                        throw new Exception("Malformed BIO format (key): found " + annotarr[keybiocol] + " expected I-" + keyjudgement.getElement());
                                    }
                                } else {
                                    throw new Exception("Malformed BIO format (key): found " + annotarr[keybiocol] + " expected O");
                                }
                            }
                        } else {
                            throw new Exception("Malformed BIO format (key): " + annotarr[keybiocol]);
                        }
                    }
                } else { // equals("O")
                    // Not aligned -> add missing
                    if (keyjudgement != null) {
                        score.add(keyjudgement);
                        keyjudgement = null;
                    }
                    // Aligned -> change if needed, move to annotjudgement or save&empty
                    if (annotjudgement != null && annotjudgement.isAligned()) {
                        if (annotarr[annotbiocol].startsWith("I-") && annotjudgement.getJudgement_str().equals("corr")) {
                            annotjudgement.changeJudgement(Judgement.judgements.valueOf("inco").ordinal());
                            keyjudgement = annotjudgement;
                            annotjudgement = null;
                        } else {
                            if (annotarr[annotbiocol].equals("O")) {
                                score.add(annotjudgement);
                                annotjudgement = null;
                        }
                        }
                    }
                }
                // -----------------------------------------------------------------------------------
            }
            if ((annotline = annotreader.readLine()) != null) {
                throw new Exception("Key annotation ended before scored annotation");
            }
        } catch (Exception e) {
            System.err.println("\nErrors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + " (line " + numline + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return score;
    }






}
