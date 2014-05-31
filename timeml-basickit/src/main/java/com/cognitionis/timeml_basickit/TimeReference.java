package com.cognitionis.timeml_basickit;

import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class TimeReference extends Element{

    private Timex timex;
    private ArrayList<Event> events;


    public TimeReference(String i,Timex t,Event e){
        events=new ArrayList<Event>();
        this.id=i;
        this.timex=t;
        events.add(e);
    }

    public Timex get_timex(){
        return timex;
    }

    public ArrayList<Event> get_events(){
        return events;
    }

    public void add_event(Event e){
        events.add(e);
    }



}
