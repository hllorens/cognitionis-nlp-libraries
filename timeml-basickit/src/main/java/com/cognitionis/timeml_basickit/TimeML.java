package com.cognitionis.timeml_basickit;

import java.util.*;

/**
 * TimeML class to represent TimeML file contents as Java Objects
 * @author Hector Llorens
 * @since 2011
 */
public class TimeML {
    private Timex DCT;
    private HashMap<String, Timex> timexes;
    private HashMap<String, Event> events;
    private HashMap<String, Event> makeinstances;
    private ArrayList<Link> links;
    private boolean links_normalized;

    public TimeML(Timex dct, HashMap<String, Timex> t, HashMap<String, Event> e, HashMap<String, Event> m,   ArrayList<Link> l){
        DCT=dct;
        timexes=t;
        events=e;
        makeinstances=m;
        links=l;
        links_normalized=false;
    }

    public void normalize_links(){
        if(!links_normalized){
        for(int i=0;i<links.size();i++){
            Link tlink=links.get(i);
            if((tlink.get_id1().startsWith("t") && tlink.get_id2().startsWith("ei"))
              || (tlink.get_id1().startsWith("ei") && tlink.get_id2().startsWith("ei") && Integer.parseInt(tlink.get_id1().substring(2)) > Integer.parseInt(tlink.get_id2().substring(2)))
              || (tlink.get_id1().startsWith("t") && tlink.get_id2().startsWith("t") && Integer.parseInt(tlink.get_id1().substring(1)) > Integer.parseInt(tlink.get_id2().substring(1)))){
                tlink.swapRelationElements();
            }
        }
        links_normalized=true;
        }
    }

    public Timex getDCT(){
        return DCT;
    }

    public HashMap<String, Timex> getTimexes(){
        return timexes;
    }

    public HashMap<String, Event> getEvents(){
        return events;
    }

    public HashMap<String, Event> getMakeinstances(){
        return makeinstances;
    }

    public ArrayList<Link> getLinks(){
        return links;
    }




}
