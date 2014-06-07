package com.cognitionis.nlp_files.annotation_scorers;

import java.util.*;
import com.cognitionis.utils_basickit.StringUtils;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class Score implements Cloneable {

    private String keyfile;
    private String annotfile;
    private HashMap<String, Integer> corr;
    private HashMap<String, Integer> inco;
    private HashMap<String, Integer> spur;
    private HashMap<String, Integer> miss;
    private HashMap<String, Integer> tp;
    private HashMap<String, Integer> tn;
    private HashMap<String, Integer> fp;
    private HashMap<String, Integer> fn;
    private HashMap<String, ArrayList<Judgement>> elem_judgements;
    private HashMap<String, Set<String>> elem_attribs;
    //int tp, tn,fp,fn;

    @Override
    public Object clone() {
        try {
            // call clone in Object.
            return super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Cloning not allowed.");
            return this;
        }
    }

    // pos = corr+inco+miss
    // act = corr+inco+spur
    // Total tags annot = corr+inco+spur
    // Total tags key   = corr+inco+miss
    // Strict V Relaxed -> inco == corr V
    public Score(String k, String a) {
        keyfile = k;
        annotfile = a;
        corr = new HashMap<String, Integer>();
        inco = new HashMap<String, Integer>();
        spur = new HashMap<String, Integer>();
        miss = new HashMap<String, Integer>();
        elem_judgements = new HashMap<String, ArrayList<Judgement>>();
        tp = new HashMap<String, Integer>();
        tn = new HashMap<String, Integer>();
        tn.put("default", 0);
        fp = new HashMap<String, Integer>();
        fn = new HashMap<String, Integer>();
    }

    /*    public Score(Score score){
    elem_judgements=score.getJudgements();
    }*/
    public HashMap<String, ArrayList<Judgement>> getJudgements() {
        return elem_judgements;
    }

    public void add(Judgement judgement) {
        try {
            String element = judgement.getElement();
            if (!elem_judgements.containsKey(element)) {
                elem_judgements.put(element, new ArrayList<Judgement>());
                corr.put(element, 0);
                inco.put(element, 0);
                spur.put(element, 0);
                miss.put(element, 0);
            }
            elem_judgements.get(element).add(judgement);


            switch (Judgement.judgements.valueOf(judgement.getJudgement_str())) {
                case corr:
                    corr.put(element, corr.get(element) + 1);
                    break;
                case inco:
                    inco.put(element, inco.get(element) + 1);
                    break;
                case spur:
                    spur.put(element, spur.get(element) + 1);
                    break;
                case miss:
                    miss.put(element, miss.get(element) + 1);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void merge(Score s) {
        try {
            keyfile = "Set of files";
            annotfile = "Set of files";
            HashMap<String, ArrayList<Judgement>> juds = s.getJudgements();
            for (String e : juds.keySet()) {
                for (Judgement judgement : s.getElement(e)) {
                    String element = judgement.getElement();
                    if (!elem_judgements.containsKey(element)) {
                        ArrayList<Judgement> elemjudg = new ArrayList<Judgement>();
                        elem_judgements.put(element, elemjudg);
                        corr.put(element, 0);
                        inco.put(element, 0);
                        spur.put(element, 0);
                        miss.put(element, 0);
                    }
                    elem_judgements.get(element).add(judgement);
                    switch (Judgement.judgements.valueOf(judgement.getJudgement_str())) {
                        case corr:
                            corr.put(element, corr.get(element) + 1);
                            break;
                        case inco:
                            inco.put(element, inco.get(element) + 1);
                            break;
                        case spur:
                            spur.put(element, spur.get(element) + 1);
                            break;
                        case miss:
                            miss.put(element, miss.get(element) + 1);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }

    }

    public ArrayList<Judgement> getElement(String element) {
        return elem_judgements.get(element);
    }

    public String getElementAttribsScore(String element) throws Exception {
        String ret = "";
        ArrayList<Judgement> judgs = elem_judgements.get(element);
        HashMap<String, Integer> at_corr = new HashMap<String, Integer>();
        HashMap<String, Integer> at_inco = new HashMap<String, Integer>();

        // These data is not very useful since the missclassification of:
        // missing is obvious
        // spurious is uncheckable

        //HashMap<String, Integer> at_spur = new HashMap<String, Integer>();
        //HashMap<String, Integer> at_miss = new HashMap<String, Integer>();
        HashMap<String, Integer> class_value_corr = new HashMap<String, Integer>();
        HashMap<String, Integer> class_value_inco = new HashMap<String, Integer>();

        //HashMap<String, Integer> class_value_spur = new HashMap<String, Integer>(); // not relevant

        HashMap<String, Integer> class_value_miss = new HashMap<String, Integer>(); // interesting to see what is failing

        for (Judgement judg : judgs) {
            if (judg.isAligned()) {
                for (String at_key : judg.get_alt_attribs_scores().keySet()) {
                    if (!at_corr.containsKey(at_key)) {
                        at_corr.put(at_key, 0);
                        at_inco.put(at_key, 0);
                    }
                    if (!class_value_corr.containsKey(judg.getAttrib(at_key))) {
                        class_value_corr.put(judg.getAttrib(at_key), 0);
                        class_value_inco.put(judg.getAttrib(at_key), 0);
                        class_value_miss.put(judg.getAttrib(at_key), 0);
                    }
                    switch (Judgement.judgements.values()[judg.get_alt_attrib_score(at_key)]) {
                        case corr:
                            at_corr.put(at_key, at_corr.get(at_key) + 1);
                            class_value_corr.put(judg.getAttrib(at_key), class_value_corr.get(judg.getAttrib(at_key)) + 1);
                            break;
                        case inco:
                            at_inco.put(at_key, at_inco.get(at_key) + 1);
                            class_value_inco.put(judg.getAttrib(at_key), class_value_inco.get(judg.getAttrib(at_key)) + 1);
                            ret += "\t\t\t " + at_key + ": " + judg.getAttrib(at_key) + " classified as " + judg.get_alt_attrib(at_key) + " -- (line " + judg.getNumline() + ")\n";
                            break;
                        case spur:
                            throw new Exception("Unexpected Spurious: " + judg.getKeylines());
                        case miss:
                            throw new Exception("Unexpected Missing: " + judg.getKeylines());
                    }
                }
            } else {
                if (judg.getJudgement_str().equalsIgnoreCase("miss") && judg.containsAttrib("class")) {
                    if (!class_value_miss.containsKey(judg.getAttrib("class"))) {
                        class_value_miss.put(judg.getAttrib("class"), 0);
                    }
                    class_value_miss.put(judg.getAttrib("class"), class_value_miss.get(judg.getAttrib("class")) + 1);
                }
            }
        }


        for (String at_key : at_corr.keySet()) {
            double s = doubleDivision(at_corr.get(at_key), (at_corr.get(at_key) + at_inco.get(at_key)));
            ret += "\n\t\t" + at_key + " s=" + twoDecPos(s) + "    --    corr=" + at_corr.get(at_key) + " inco=" + at_inco.get(at_key) + "\n";
        }

        for (String class_value_key : class_value_inco.keySet()) {
            double s = doubleDivision(class_value_corr.get(class_value_key), class_value_corr.get(class_value_key) + class_value_inco.get(class_value_key));
            String nomval = class_value_key;
            if (nomval.length() > 4) {
                nomval = nomval.substring(0, 4);
            }
            ret += "\t\t\t" + nomval + " s=" + twoDecPos(s) + "    --    corr=" + class_value_corr.get(class_value_key) + " inco=" + class_value_inco.get(class_value_key) + " miss=" + class_value_miss.get(class_value_key) + "\n";
        }


        return ret;

    }

    // STATIC IMPLEMENTATIONS ------------------------------------------
    public static double Precision(double corr, double act) {
        return doubleDivision(corr, act) * 100;
    }

    public static double Recall(double corr, double pos) {
        return doubleDivision(corr, pos) * 100;
    }

    public static double F1(double p, double r) {
        return doubleDivision(2 * p * r, p + r);
    }
    // -------------------------------------------------------------------

    /**
     * GENERAL POS
     * Get total key elements (POS)
     * @return total key elements
     */
    public int getPOS() {
        int pos = 0;
        for (String e : corr.keySet()) {
            pos += getPOS(e);
        }
        return pos;
    }

    /**
     * ELEMENT POS
     * Get total key elements (POS)
     * @param e String element
     * @return total key elements
     */
    public int getPOS(String e) {
        return (corr.get(e) + inco.get(e) + miss.get(e));
    }

    /**
     * GENERAL ACT
     * Get total annot elements (ACT)
     * @return total annot elements
     */
    public int getACT() {
        int act = 0;
        for (String e : corr.keySet()) {
            act += getACT(e);
        }
        return act;
    }

    /**
     * ELEMENT ACT
     * Get total annot elements (ACT)
     * @param e String element
     * @return total annot elements
     */
    public int getACT(String e) {
        return (corr.get(e) + inco.get(e) + spur.get(e));
    }

    // IMPLEMENTED MEASURES -----------------------------------------------
    public double getPrecision(String e) {
        return Precision(corr.get(e).doubleValue(), getACT(e));
    }

    public double getRecall(String e) {
        return Recall(corr.get(e).doubleValue(), getPOS(e));
    }

    public double getF1(String e) {
        double p = getPrecision(e);
        double r = getRecall(e);
        return F1(p, r);
    }

    public double getPrecisionTokenLevel(String e) {
        return ((double) tp.get(e) / (tp.get(e) + fp.get(e)));
    }

    public double getRecallTokenLevel(String e) {
        return ((double) tp.get(e) / (tp.get(e) + fn.get(e)));
    }

    public double getF1TokenLevel(String e) {
        double p = getPrecisionTokenLevel(e);
        double r = getRecallTokenLevel(e);
        return F1(p, r);
    }

    public double getAccuracyTokenLevel(String e) {
        return ((double) (tp.get(e) + tn.get(e)) / (tp.get(e) + tn.get(e) + fp.get(e) + fn.get(e)));
    }

    // IMPORTANT: ONLY FOR TEXT TYPE EVALUATION (SPAN) --> SPAN SLOPPY
    // NOT FOR ATTRIBUTES (IE, CLASS)
    public double getPrecisionRelaxed(String e) {
        return Precision(corr.get(e).doubleValue() + inco.get(e).doubleValue(), getACT(e));
    }

    public double getRecallRelaxed(String e) {
        return Recall(corr.get(e).doubleValue() + inco.get(e).doubleValue(), getPOS(e));
    }

    public double getF1Relaxed(String e) {
        double p = getPrecisionRelaxed(e);
        double r = getRecallRelaxed(e);
        return F1(p, r);
    }

    public void print() throws Exception {
        print("simple");
    }

    public void print(String option) throws Exception {
        //System.out.println("KEYFILE: " + this.keyfile);
        for (String e : elem_judgements.keySet()) {
            System.out.println("\t>" + e.toUpperCase() + "<  (POS=" + getPOS(e) + ",ACT=" + getACT(e) + "):\tPrecision=" + oneDecPos(getPrecision(e)) + "%\tRecall=" + oneDecPos(getRecall(e)) + "%\tF1=" + oneDecPos(getF1(e)) + "%\t- corr=" + corr.get(e) + ";inco=" + inco.get(e) + ";spur=" + spur.get(e) + ";miss=" + miss.get(e) + ";");
            System.out.println("\t  TempEval2(token level score)\tPrecision=" + StringUtils.twoDecPosS(getPrecisionTokenLevel(e)) + "\tRecall=" + StringUtils.twoDecPosS(getRecallTokenLevel(e)) + "\tF1=" + StringUtils.twoDecPosS(getF1TokenLevel(e)) + "  \t- accuracy=" + twoDecPos(getAccuracyTokenLevel(e)));
            if (option.contains("attribs")) {
                System.out.println(this.getElementAttribsScore(e));
            }
            if (option.contains("detail")) {
                for (Judgement judgement : elem_judgements.get(e)) {
                    if(!judgement.getJudgement_str().equalsIgnoreCase("corr")){
                    System.out.println("\t\t" + judgement.getJudgement_str() + " numline:" + judgement.getNumline());
                    if(judgement.getAttribs()!=null && judgement.getAttribs().size()>0){
                        if (judgement.isAligned()) {
                            System.out.println("\t\t  keyattribs: " + judgement.getXMLattribs() + "   --   annotattribs: " + judgement.getXMLalt_attribs());
                        } else {
                            System.out.println("\t\t  attribs: " + judgement.getXMLattribs());
                        }
                    }
                    for (String line : judgement.getKeylines().split("\n")) {
                        System.out.println("\t\t\t KEY: " + line);
                    }
                    System.out.println("\t\t\t-");
                    for (String line : judgement.getAnnotlines().split("\n")) {
                        System.out.println("\t\t\t ANNOT: " + line);
                    }
                    System.out.println();
                    System.out.println();
                    }
                }
            }
        }
    }

    public static double oneDecPos(double d) {
        return ((int) Math.round((d) * 10.0)) / 10.0;
    }

    public static double twoDecPos(double d) {
        return ((int) Math.round((d) * 100.0)) / 100.0;
    }



    public static double doubleDivision(double a, double b) {
        if (b == 0) {
            return 0.0;
        }
        return (a / b);
    }

    public void addtn() {
        for (String e : tn.keySet()) {
            tn.put(e, tn.get(e) + 1);
        }
    }

    public void addtnrest(String ne) {
        for (String e : tn.keySet()) {
            if (!ne.equals(e)) {
                tn.put(e, tn.get(e) + 1);
            }
        }
    }

    public void addtp(String e) {
        if (!tn.containsKey(e)) {
            initialize_tokenlevel(e);
        }
        tp.put(e, tp.get(e) + 1);
    }

    public void addfp(String e) {
        if (!tn.containsKey(e)) {
            initialize_tokenlevel(e);
        }
        fp.put(e, fp.get(e) + 1);
    }

    public void addfn(String e) {
        if (!tn.containsKey(e)) {
            initialize_tokenlevel(e);
        }
        fn.put(e, fn.get(e) + 1);
    }

    public void initialize_tokenlevel(String e) {
        tn.put(e, tn.get("default"));
        tp.put(e, 0);
        fn.put(e, 0);
        fp.put(e, 0);
    }

    public void sort_judgements_by_numline() {
        for (String e : this.getJudgements().keySet()) {
            Collections.sort(this.getJudgements().get(e));
        }
    }
}

/*  //RECALCULATE OR MAY BE PRECALCULATE BUT NOT NOW... IT IS NOT IMPORTANT.
public void recalculateALL() {
corr = 0;
inco = 0;
spur = 0;
miss = 0;
try {
for (Judgement j : elem_judgements) {
switch (Judgement.elem_judgements.valueOf(j.getJudgement_str())) {
case corr:
corr++;
break;
case inco:
inco++;
break;
case spur:
spur++;
break;
case miss:
miss++;
break;
}
}
getPrecision();
getRecall();
getF1();
} catch (Exception e) {
System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
e.printStackTrace(System.err);
}
}

}*/

