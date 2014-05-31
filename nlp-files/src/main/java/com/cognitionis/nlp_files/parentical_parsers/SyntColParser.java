package com.cognitionis.nlp_files.parentical_parsers;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */

import java.util.*;

public class SyntColParser {

    private String full;
    private int parlevel;
    private int tokens_parsed;
    private String currentMainPhrase;
    private int currentPositionInMainPhrase;
    private Integer [] currentMainPhraseSpan;
    private int currentMainPhrase_parlevel;
    private int sentnum;
    private String currentSubsent;
    private int currentSubsent_parlevel;
    private Stack st;
    private ArrayList<Integer []> mainphrases_span;

    public SyntColParser() {
        full = "";
        parlevel = 0;
        tokens_parsed=0;
        currentMainPhrase="O";
        currentSubsent="O";
        currentPositionInMainPhrase=0;
        currentMainPhrase_parlevel=0;
        st = new Stack();
        currentMainPhraseSpan=new Integer [2];
        currentMainPhraseSpan[0]=0;
        currentMainPhraseSpan[1]=0;
        mainphrases_span=new ArrayList<Integer []>();
    }

    public void parse(String input) {
        try {
            String linput = input.trim();
            String element = "";

            if(!currentMainPhrase.equals("O")){
                currentMainPhrase="I"+currentMainPhrase.substring(1);
                currentPositionInMainPhrase++;
                currentMainPhraseSpan[1]=tokens_parsed;
            }

            boolean inElem = false;
            for (int cn = 0; cn < linput.length(); cn++) {
                char cinput = linput.charAt(cn);
                // Ignore blanks
                if (cinput == ' ' || cinput == '\n' || cinput == '\r' || cinput == '\t') {
                    continue;
                }
                // Ignore *
                if (cinput == '*') {
                    continue;
                }

                // Parse
                full += cinput;
                if (cinput == '(') {
                    if (inElem) {
                        if (element.length() < 1) {
                            throw new Exception("Empty element");
                        }
                        st.push(element);
                        // IMPORTANT: NOTE THAT ONLY SBAR IS INCLUDED NOT (S for example in (VP start (S calling
                        if (element.equals("SBAR")) {
                            currentSubsent = currentSubsent + sentnum;
                            sentnum = 1;
                            currentSubsent_parlevel = parlevel;
                        }
                        if((currentMainPhrase.equals("O") && element.matches("(PP|NP|VP|ADJP|ADVP|CONJP|WH(ADJP|AVP|PP|NP)|QP)"))
                                || (currentMainPhrase.matches("(B|I)\\-VP") && element.matches("(PP|NP|ADJP|ADVP|CONJP|WH(ADJP|AVP|PP|NP)|QP)"))){
                            currentMainPhrase="B-"+element;
                            currentPositionInMainPhrase=1;
                            currentMainPhrase_parlevel=parlevel;
                            currentMainPhraseSpan[0]=tokens_parsed;
                            currentMainPhraseSpan[1]=tokens_parsed;
                        }
                        element = "";
                    } else {
                        inElem = true;
                    }
                    parlevel++;
                } else {
                    if (cinput == ')') {
                        if(parlevel==currentMainPhrase_parlevel && !currentMainPhrase.endsWith("O")){
                            currentMainPhrase="O";
                            currentPositionInMainPhrase=0;
                            currentMainPhrase_parlevel=0;
                            // MEGA-HACK for two step parsing of one token
                            if(input.matches("[)]*"))
                                currentMainPhraseSpan[1]--;
                            // add an empty main phrase span
                            mainphrases_span.add(currentMainPhraseSpan.clone());
                            currentMainPhraseSpan=null;
                            currentMainPhraseSpan=new Integer[2];
                        }
                        parlevel--;
                        //if(!st.empty()){
                            st.pop();
                        //}
                    } else {
                        element += cinput;
                    }
                }
            }

            if (inElem) {
                if (element.length() < 1) {
                    throw new Exception("Empty element");
                }
                st.push(element);
                if (element.equals("SBAR")) {
                    currentSubsent = currentSubsent + sentnum;
                    currentSubsent_parlevel = parlevel;
                }
                if((currentMainPhrase.equals("O") && element.matches("(PP|NP|VP|ADJP|ADVP|CONJP|WH(ADJP|AVP|PP|NP)|QP)"))
                        || (currentMainPhrase.matches("(B|I)\\-VP") && element.matches("(PP|NP|ADJP|ADVP|CONJP|WH(ADJP|AVP|PP|NP)|QP)"))){
                    currentMainPhrase="B-"+element;
                    currentPositionInMainPhrase=1;
                    currentMainPhrase_parlevel=parlevel;
                    currentMainPhraseSpan[0]=tokens_parsed;
                    currentMainPhraseSpan[1]=tokens_parsed;
                }
                element = "";
            }

            // MEGA-HACK FOR TWO PARSES OF THE SAME TOKEN (CHECKING IF ITS ONLY CLOSING BRAKETS)
            if(!input.matches("[)]*"))
                tokens_parsed++;

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }

    public int getParlevel() {
        return parlevel;
    }

    public String getCurrentSubsent() {
        return currentSubsent;
    }

    public String getCurrentMainPhraseBIO() {
        return currentMainPhrase;
    }

    public int getCurrentPositionInMainPhrase() {
        return currentPositionInMainPhrase;
    }


    public String getCurrent() {
        String[] st_arr = new String[st.size()];
        String current = "";
        st.copyInto(st_arr);
        for (int i = 0; i < st_arr.length; i++) {
            if (i != 0) {
                current += "-";
            }
            current += st_arr[i];
        }
        return current;
    }

    public String updateCurrent(String past) {
        String[] st_arr = new String[st.size()];
        String current = "";
        st.copyInto(st_arr);
        for (int i = 0; i < st_arr.length; i++) {
            if (i != 0) {
                current += "-";
            }
            current += st_arr[i];
        }

        String[] past_arr = past.split("-");
        String[] curr_arr = current.split("-");
        String commonSynt = "";

        int shortestSyntTree = curr_arr.length;
        if (past_arr.length < shortestSyntTree) {
            shortestSyntTree = past_arr.length;
        }

        for (int i = 0; i < shortestSyntTree; i++) {
            if (curr_arr[i].equals(past_arr[i])) {
                if (i != 0) {
                    commonSynt += "-";
                }
                commonSynt += curr_arr[i];
            } else {
                break;
            }
        }

        return commonSynt;
    }


    // IMPORTANT: IT WAS THE ORIGINAL "getCurrentPhrase"
    public String getCurrentPhrase(String synt) {
        String[] synt_arr = synt.split("-");
        String phraseSynt = "";
        int phraseDept = 0;
        for (int i = synt_arr.length - 1; i >= 0; i--) {
            if (phraseDept == 0) {
                if (synt_arr[i].equals("VP")) {
                    phraseSynt = synt_arr[i];
                    break;
                } else {
                    phraseSynt = synt_arr[i];
                    phraseDept++;
                }
            } else {
                // Originally uncommented
                /*if (phraseDept >= 3) {
                    break;
                }*/
                if (synt_arr[i].equals("VP") || synt_arr[i].matches("S.*")) {
                    break;
                } else {
                    phraseSynt = synt_arr[i] + "-" + phraseSynt;
                    phraseDept++;
                }
            }
        }

        return phraseSynt;
    }

    public String getCurrentMainPhrase(String synt) {
        String[] synt_arr = synt.split("-");
        String phraseSynt = "";
        int phraseDept = 0;
        for (int i = synt_arr.length - 1; i >= 0; i--) {
            if (phraseDept == 0) {
                if (synt_arr[i].equals("VP")) {
                    phraseSynt = synt_arr[i];
                    break;
                } else {
                    phraseSynt = synt_arr[i];
                    phraseDept=1;
                }
            } else {
                if (synt_arr[i].equals("VP") || synt_arr[i].matches("S.*")) {
                    break;
                } else {
                    phraseSynt = synt_arr[i];
                }
            }
        }
        return phraseSynt;
    }


    public String getFull() {
        return full;
    }

    public ArrayList<Integer []> getMainPhrasesSpan(){
        return mainphrases_span;
    }

    public ArrayList<String> getAllPhrases() {
        ArrayList<String> phrases = new ArrayList<String>();
        try {
            Stack localst = new Stack();
            String linput = full.trim();
            String element = "";
            boolean inElem = false;
            for (int cn = 0; cn < linput.length(); cn++) {
                char cinput = linput.charAt(cn);
                // Ignore blanks and *
                if (cinput == ' ' || cinput == '\n' || cinput == '\r' || cinput == '\t' || cinput == '*') {
                    continue;
                }

                // Parse
                if (cinput == '(') {
                    if (inElem) {
                        if (element.length() < 1) {
                            throw new Exception("Empty element");
                        }
                        localst.push(element);
                        getAllCurrentPhrases(localst, phrases);
                        element = "";
                    } else {
                        inElem = true;
                    }
                } else {
                    if (cinput == ')') {
                        if (inElem) {
                            if (element.length() < 1) {
                                throw new Exception("Empty element");
                            }
                            localst.push(element);
                            getAllCurrentPhrases(localst, phrases);
                            element = "";
                            inElem = false;
                        }
                        localst.pop();
                    } else {
                        element += cinput;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }


        return phrases;
    }

    public void getAllCurrentPhrases(Stack localst, ArrayList<String> phrases) {
        String[] synt_arr = new String[localst.size()];
        localst.copyInto(synt_arr);
        String phraseSynt = "";
        int phraseDept = 0;
        for (int i = synt_arr.length - 1; i >= 0; i--) {
            if (phraseDept == 0) {
                if (synt_arr[i].equals("VP")) {
                    phraseSynt = synt_arr[i];
                    //phrases.add(phraseSynt);
                    break;
                } else {
                    if(synt_arr[i].endsWith("P")){
                        phraseSynt = synt_arr[i];
                        //phrases.add(phraseSynt);
                        phraseDept++;
                    }else{
                        break;
                    }
                }
            } else {
                if (phraseDept >= 3) {
                    break;
                }
                if (synt_arr[i].equals("VP") || synt_arr[i].matches("S.*")) {
                    break;
                } else {
                    phraseSynt = synt_arr[i] + "-" + phraseSynt;
                    //phrases.add(phraseSynt);
                    phraseDept++;
                }
            }
        }
        if(!phraseSynt.equals("")){
            phrases.add(phraseSynt);
        }

    }
}
