package com.cognitionis.nlp_files.annotation_scorers;


import com.cognitionis.utils_basickit.XmlAttribs;
import java.util.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class Judgement implements Comparable<Judgement> {
    // public and static to be accessed from outside in switchs if needed
    public static enum judgements {corr,inco,spur,miss;}  // Correct, Incorrect, Spurious, Missing

    private int judgement;
    private String element;
    private HashMap <String,String> attribs;
    private HashMap <String,String> alt_annot_attribs;
    private HashMap <String,Integer> alt_annot_attribs_scores;
    private int numline;  //
    private String keylines;
    private String annotlines;


    public Judgement(int j, String elem, String attrs, int nline, String keyline, String annotline){
        try{
        if(elem==null || keyline==null || annotline==null){
           throw new Exception("Null element, keyline or annotline");
        }
        //judgements.valueOf(j); // Check judgement
        judgements jdm=judgements.values()[j];  // Check judgement
        judgement=j;

        element=elem;
        attribs=XmlAttribs.parseXMLattrs(attrs);
        if(attribs.isEmpty()){
            attribs=XmlAttribs.parseSemiColonAttrs(attrs);
        }
        alt_annot_attribs=null; // only for aligned elements (corr/inco)
        numline=nline;
        keylines=keyline;
        annotlines=annotline;
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }


    public void changeJudgement(int j){
        try{
        judgements jdm=judgements.values()[j];  // Check judgement
        judgement=j;
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }


    public void updateNumline(int newnum){
        numline=newnum;
    }

    public void extendJudgement(String keyline, String annotline){
        try{
        if(keyline==null || annotline==null){
           throw new Exception("Null keyline or annotline");
        }
            keylines+="\n"+keyline;
            annotlines+="\n"+annotline;
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }



    public void changeAndExtendJudgement(int j, String keyline, String annotline){
        try{
        if(keyline==null || annotline==null){
           throw new Exception("Null keyline or annotline");
        }
        //judgements.valueOf(j); // Check judgement
        judgements jdm=judgements.values()[j];  // Check judgement
        judgement=j;
        keylines+="\n"+keyline;
        annotlines+="\n"+annotline;
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }





    public void add_alt_attribs(String attrs, String type){
        try{
        if(type.equals("key")){
            alt_annot_attribs=attribs;
            attribs=XmlAttribs.parseXMLattrs(attrs);
            if(attribs.isEmpty()){
                attribs=XmlAttribs.parseSemiColonAttrs(attrs);
            }
        }else{
            if(type.equals("annot")){
                alt_annot_attribs=XmlAttribs.parseXMLattrs(attrs);
                if(alt_annot_attribs.isEmpty()){
                    alt_annot_attribs=XmlAttribs.parseSemiColonAttrs(attrs);
                }
            }else{
                throw new Exception("alt_attribs type must be 'key' or 'annot'");
            }
        }

        alt_annot_attribs_scores=new HashMap<String, Integer>();

        for(String key : attribs.keySet()){
            alt_annot_attribs_scores.put(key, judgements.valueOf("miss").ordinal());
        }

        for(String key : alt_annot_attribs.keySet()){
            if(alt_annot_attribs_scores.get(key)!=null){
                if(attribs.get(key).equalsIgnoreCase(alt_annot_attribs.get(key))){
                    alt_annot_attribs_scores.put(key, judgements.valueOf("corr").ordinal());
                }else{
                    alt_annot_attribs_scores.put(key, judgements.valueOf("inco").ordinal());
                }
            }else{
                alt_annot_attribs_scores.put(key, judgements.valueOf("spur").ordinal());
            }
        }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }



    public int getJugement(){
        return judgement;
    }

    public String getJudgement_str(){
        return judgements.values()[judgement].toString();
    }

    public int getJudgement_int(){
        return judgements.values()[judgement].ordinal();
    }

    public String getElement(){
        return element;
    }

    public HashMap <String,String> getAttribs(){
        return attribs;
    }

    public String getAttrib(String key){
        return attribs.get(key);
    }

    public boolean containsAttrib(String key){
        return attribs.containsKey(key);
    }

    public String getXMLattribs(){
        String xmlattrs="";
        for(String key : attribs.keySet()){
            xmlattrs+=" "+key+"=\""+attribs.get(key)+"\"";
        }
        if(xmlattrs.equals("")){
            xmlattrs="*";
        }
        return xmlattrs;
    }

    public HashMap <String,String> get_alt_attribs(){
        return alt_annot_attribs;
    }

    public String get_alt_attrib(String key){
        return alt_annot_attribs.get(key);
    }
    public String getXMLalt_attribs(){
        String xmlattrs="";
        for(String key : alt_annot_attribs.keySet()){
            xmlattrs+=" "+key+"=\""+alt_annot_attribs.get(key)+"\"";
        }
        if(xmlattrs.equals("")){
            xmlattrs="*";
        }
        return xmlattrs;
    }

    public HashMap <String,Integer> get_alt_attribs_scores(){
        return alt_annot_attribs_scores;
    }

    public Integer get_alt_attrib_score(String key){
        return alt_annot_attribs_scores.get(key);
    }


    public int getNumline(){
        return numline;
    }

    public String getKeylines(){
        return keylines;
    }

    public String getAnnotlines(){
        return annotlines;
    }

    public Boolean isAligned(){
       if(judgement==judgements.valueOf("corr").ordinal() || judgement==judgements.valueOf("inco").ordinal()){
           return true;
       }else{
           return false;
       }
    }

    public int compareTo(Judgement obj)
      {
         int result = this.getNumline() - obj.getNumline();
         return result;
      }

}
