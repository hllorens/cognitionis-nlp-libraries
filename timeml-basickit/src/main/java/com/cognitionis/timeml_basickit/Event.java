package com.cognitionis.timeml_basickit;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class Event extends Element implements Cloneable {

    // why 1000000? It is enough: 1 million events by average correspond to a 10 milion words document - like 50MB txt (Wiki-WWI 20000w, Quijote 400000w, Bible 500000w, more than 1000 pages)
    //              It improves eficiency: not id-taken check
    public static final String firstExtraMakeinstanceId = "1000000";
    private String eiid;
    private String event_class;
    private String event_pos;
    private String event_tense;
    private String event_aspect;
    private String event_polarity;
    private String event_modality;
    private String event_context; // sentence contents
    private String event_participants; // depending on the PoS: verbal, nominal...
    private String event_aspectual_modifier;
    private String event_duration;
    private boolean is_main;
    private boolean is_linked_to_tref;

    public Event(String i, String ex, String c, String di, long snum, int tnum) {
        this.id = i;
        this.expression = ex;
        this.num_tokens = 1;
        this.event_class = c;
        this.doc_id = di;
        this.sent_num = snum;
        this.tok_num = tnum;
        this.event_aspectual_modifier = "";
        this.event_duration = "0";
        this.is_main = false;
        this.is_linked_to_tref = false;
    }

    @Override
    public Event clone() {
        try {
            return (Event) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String get_class() {
        return event_class;
    }

    public String get_POS() {
        return event_pos;
    }

    public String get_context() {
        return event_context;
    }

    public String get_participants() {
        return event_participants;
    }

    public String get_aspectual_modifier() {
        return event_aspectual_modifier;
    }

    public String get_duration() {
        return event_duration;
    }

    public String get_pos() {
        return event_pos;
    }

    public String get_tense() {
        return event_tense;
    }

    public String get_aspect() {
        return event_aspect;
    }

    public String get_polarity() {
        return event_polarity;
    }

    public String get_modality() {
        return event_modality;
    }

    public boolean is_main() {
        return is_main;
    }

    public boolean is_linked_to_a_ref() {
        return is_linked_to_tref;
    }

    public String get_eiid() {
        return eiid;
    }

    public void set_context(String context) {
        this.event_context = context;
    }

    public void set_participants(String participants) {
        this.event_participants = participants;
    }

    public void set_aspectual_modifier(String mod) {
        this.event_aspectual_modifier = mod;
    }

    public void set_duration(String duration) {
        this.event_duration = duration;
    }

    public void set_pos(String s) {
        this.event_pos = s;
    }

    public void set_tense(String s) {
        this.event_tense = s;
    }

    public void set_aspect(String s) {
        this.event_aspect = s;
    }

    public void set_modality(String s) {
        this.event_modality = s;
    }

    public void set_tense_aspect_modality(String s,String pos) {
        if (!s.equals("-")) {
            String[] sarray = s.split("-");
            // hack for Spanish: deprecated, makes the relations worse
           /* if(pos.equals("VMN0")){
                event_tense = "NONE";
                event_aspect = "IMPERFECTIVE";
            }else{*/
            for (int i = 0; i < sarray.length; i++) {
                if (event_tense == null) {
                    if (sarray[i].matches("(past|present|future|infinitive)")) {
                        event_tense = sarray[i].toUpperCase();
                    } else {
                        if (sarray[i].equals("conditional")) {
                            event_tense = "FUTURE";
                            event_modality = "would";
                        }
                    }
                } else {
                    if (event_tense != null && sarray[i].matches("(perfect|continuous)")) {
                        String aspect = "";
                        if (sarray[i].equals("perfect")) {
                            aspect = "PERFECTIVE";
                        } else {
                            aspect = "PROGRESSIVE";
                        }
                        if (event_aspect != null) {
                            event_aspect += "_" + aspect;
                        } else {
                            event_aspect = aspect;
                        }
                    }
                }

            }
            //}
        }
        if (event_tense == null) {
            event_tense = "NONE";
        }
        if (event_aspect == null) {
            event_aspect = "NONE";
        }
    }

    public void set_polarity(String s) {
        if (s != null) {
            if (s.length() > 3) {
                s = s.substring(0, 3).toUpperCase();
            }
            if (!s.matches("(POS|NEG)")) {
                s = "POS";
            }
        }
        this.event_polarity = s;
    }

    public void set_is_main(boolean v) {
        this.is_main = v;
    }

    public void set_is_linked_to_ref(boolean v) {
        this.is_linked_to_tref = v;
    }

    public void set_eiid(String s) {
        this.eiid = s;
    }

    public static String treebank2tml_pos(String pos) {
        String out = "OTHER";
        if (pos.startsWith("V")) {
            out = "VERB";
        } else {
            if (pos.startsWith("N")) {
                out = "NOUN";
            } else {
                if (pos.startsWith("J")) {
                    out = "ADJECTIVE";
                } else {
                    if (pos.equals("IN") || pos.equals("TO")) {
                        out = "PREPOSITION";
                    }
                }
            }
        }
        return out;
    }
}
