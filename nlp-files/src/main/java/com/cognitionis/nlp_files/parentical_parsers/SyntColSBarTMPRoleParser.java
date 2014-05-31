package com.cognitionis.nlp_files.parentical_parsers;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
import java.util.*;

public class SyntColSBarTMPRoleParser {

    private String full;
    private int parlevel;
    private String currentSubsent;
    private int currentSubsent_parlevel;
    private int sentnum;
    HashMap<String, String> subsentTMP;
    private Stack st;

    public SyntColSBarTMPRoleParser() {
        full = "";
        parlevel = 0;
        sentnum = 1;
        currentSubsent = "0";
        currentSubsent_parlevel = 0;
        st = new Stack();
        subsentTMP = new HashMap<String, String>();
        subsentTMP.put(currentSubsent, "-");
    }

    public void parse(String synt, String role, String word) {
        try {
            String linput = synt.trim();
            word = word.toLowerCase();
            String element = "";
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
                        if (element.equals("SBAR")) {
                            currentSubsent = currentSubsent + sentnum;
                            sentnum = 1;
                            currentSubsent_parlevel = parlevel;
                            if (role.matches(".*TMP.*")) {
                                subsentTMP.put(currentSubsent, word);
                            } else {
                                String tempsub = currentSubsent;
                                while (tempsub.length() > 1) {
                                    tempsub = tempsub.substring(0, tempsub.length() - 1);
                                    subsentTMP.put(currentSubsent, subsentTMP.get(tempsub));
                                    if (!subsentTMP.get(tempsub).equals("-")) {
                                        break;
                                    }
                                }
                            }
                        }
                        element = "";
                    } else {
                        inElem = true;
                    }
                    parlevel++;
                } else {
                    if (cinput == ')') {
                        if (parlevel == currentSubsent_parlevel && !currentSubsent.equals("0")) {
                            currentSubsent = currentSubsent.substring(0, currentSubsent.length() - 1);
                            currentSubsent_parlevel = 0;
                            sentnum = Integer.parseInt(currentSubsent.substring(currentSubsent.length() - 1)) + 1;
                        }
                        parlevel--;
                        //if (!st.empty()) {
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
                    if (role.matches(".*TMP.*")) {
                        subsentTMP.put(currentSubsent, word);
                    } else {
                        String tempsub = currentSubsent;
                        while (tempsub.length() > 1) {
                            tempsub = tempsub.substring(0, tempsub.length() - 1);
                            subsentTMP.put(currentSubsent, subsentTMP.get(tempsub));
                            if (!subsentTMP.get(tempsub).equals("-")) {
                                break;
                            }
                        }
                    }
                }
                element = "";
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
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

    public String getSubsentTMP(String ss) {
        return subsentTMP.get(ss);
    }

    public String getSubsentTMP() {
        return subsentTMP.get(currentSubsent);
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
                    phraseDept = 1;
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
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
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
                    if (synt_arr[i].endsWith("P")) {
                        phraseSynt = synt_arr[i];
                        //phrases.add(phraseSynt);
                        phraseDept++;
                    } else {
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
        if (!phraseSynt.equals("")) {
            phrases.add(phraseSynt);
        }

    }
}
