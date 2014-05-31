package com.cognitionis.timeml_basickit;

/**
 * @author Hector Llorens
 * @since 2011
 */
public abstract class Element implements Cloneable {

    protected String id;
    protected String expression;
    protected int num_tokens;
    // offset
    protected String doc_id;
    protected long sent_num;
    protected int tok_num;
    protected String subsent_id;
    protected String phra_id;
    private int syntLevel; // the lower value the higher position in hierarchy
    private String govPrep; // governing prepositions if exist
    private String govTMPSub; // coverning temporal subordination element if exists


    @Override public Element clone() throws CloneNotSupportedException{
        try {return (Element) super.clone();} catch (CloneNotSupportedException e) {throw new CloneNotSupportedException("Should never happen.");}
    }

    public String get_id() {
        return id;
    }

    public String get_expression() {
        return expression;
    }

    public String get_doc_id() {
        return doc_id;
    }

    public long get_sent_num() {
        return sent_num;
    }

    public int get_tok_num() {
        return tok_num;
    }

    public String get_subsent_id() {
        return subsent_id;
    }

    public String get_phra_id() {
        return phra_id;
    }

    public int get_syntLevel() {
        return syntLevel;
    }

    public String get_govPrep() {
        return govPrep;
    }

    public String get_govTMPSub() {
        return govTMPSub;
    }

    public void extend_element(String ext) throws Exception {
        if (num_tokens < 1) {
            throw new Exception("Empty element cannot be extended");
        }
        expression = expression + "_" + ext;
        num_tokens++;
    }

    public void set_subsent_num(String sbsid) {
        this.subsent_id = sbsid;
    }

    public void set_phra_id(String pid) {
        this.phra_id = pid;
    }

    public void set_govPrep(String gp) {
        govPrep=gp;
    }

    public void set_govTMPSub(String ts) {
        govTMPSub=ts;
    }

    public void set_syntLevel(int s) {
        this.syntLevel = s;
    }
}
