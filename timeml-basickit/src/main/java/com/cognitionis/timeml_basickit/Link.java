package com.cognitionis.timeml_basickit;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class Link extends Element {
    private String link_type; // TLINK(event-timex-ref, event-timex-DURATION-no-ref,event-dct,main-event,sub-event), ALINK, SLINK
    private String id1;
    private String id2;
    private String link_category; // TLINK 13 allens, ALINKS, SLINK


    /**
     * Constructs a new Link object
     *
     * @param i the link id
     * @param ex the expression (probably null)
     * @param t the link type (tlink, slink, alink, or more specific: tlink-event-timex)
     * @param c the link category (Allen's 13 temp. rels.)(aspectual or subordinate)
     * @param i1 the id of the first element (event or timex)
     * @param i2 the id of the second element (event or timex)
     * @param di the id of the document
     *
     */
    public Link(String i,String t,String c, String i1, String i2, String di){
        this.id=i;
        this.expression=null;
        this.num_tokens=1;
        this.link_type=t;
        this.link_category=c;
        this.id1=i1;
        this.id2=i2;
        this.doc_id=di;
        this.sent_num=0;
        this.tok_num=0;
    }



    public String get_type(){
        return link_type;
    }

    public String get_category(){
        return link_category;
    }

    public String get_id1(){
        return id1;
    }

    public String get_id2(){
        return id2;
    }


    public void swapRelationElements(){
        link_category=reverseRelationCategory(link_category);
        String temp=id1;
        id1=id2;
        id2=temp;
    }


    public void set_category(String cat){
        link_category=cat;
    }

    /**
     * Given a relation name, return the inverse Allen-TimeML relation
     * @param rel
     * @return
     */
    public static String reverseRelationCategory(String rel) {
        try {
            if (rel.equals("BEFORE")) {
                return "AFTER";
            }
            if (rel.equals("AFTER")) {
                return "BEFORE";
            }
            if (rel.equals("IBEFORE")) {
                return "IAFTER";
            }
            if (rel.equals("IAFTER")) {
                return "IBEFORE";
            }
            if (rel.equals("DURING")) {
                return "DURING_INV";
            }
            if (rel.equals("BEGINS")) {
                return "BEGUN_BY";
            }
            if (rel.equals("BEGUN_BY")) {
                return "BEGINS";
            }
            if (rel.equals("ENDS")) {
                return "ENDED_BY";
            }
            if (rel.equals("ENDED_BY")) {
                return "ENDS";
            }
            if (rel.equals("OVERLAPS")) {
                return "OVERLAPPED_BY";
            }
            if (rel.equals("OVERLAPPED_BY")) {
                return "OVERLAPS";
            }
            if (rel.equals("INCLUDES")) {
                return "IS_INCLUDED";
            }
            if (rel.equals("IS_INCLUDED")) {
                return "INCLUDES";
            }
            if (rel.equals("IDENTITY") || rel.equals("SIMULTANEOUS")) {
                return "SIMULTANEOUS";
            }

            // SPANISH
            if (rel.equals("OVERLAP")) {
                return "OVERLAP";
            }
            if (rel.equals("BEFORE-OR-OVERLAP")) {
                return "OVERLAP-OR-AFTER";
            }
            if (rel.equals("OVERLAP-OR-AFTER")) {
                return "BEFORE-OR-OVERLAP";
            }

            throw new Exception("Unknow relation: " + rel);
        } catch (Exception e) {
            System.err.println("Errors found (Link):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
    }
}
