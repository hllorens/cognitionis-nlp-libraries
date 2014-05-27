package com.cognitionis.jtimegraph.gregoriangraph;

import java.text.*;
import java.util.*;

/**
 *
 * @author Hector Llorens
 */
public final class GregorianPoint implements Comparable<GregorianPoint> {

    public DateFormat basicDate;
    public static final String zero_filling="000000000000000000";
    private long position; // ID and time value in the chain
    // int is faster than Integer (simpler) +-2,147,483,647 (10 positions are not precise enough...)
    // 0000-00-00 00:00 needs at least 12 positions
    // Extend to long: +-9,223,372,036,854,775,807 (18+1)--> (1 sign) (6 extra 000000) 0000-00-00 00:00
    // up to 10,000,000,000 years --> universe age and beyond
    //          -13,700,000 big bang 13.7 billion years ago
    // IMPORTANT: Another options is just use UnixTime or PosixTime (UT): seconds from 1970
    // PROBLEM: conversion date -> UT --> UT->DATE is slower (check the limits)

    private String associated_entities;
    private String gregorianString; // (1 sign) (6 extra 000000) (0000-00-00 00:00) 12
    //private int chain; // always 0 in gregorianpoint (no relative ordering always absolute)

    public GregorianPoint(long p) {
        this(p, "");
    }

    public GregorianPoint(long p, String entities) {
        position = p;
        try {
            entities = entities.replaceAll("\\s+", "");
            if (entities.length() == 0 || entities.matches(".*[,]+.*")) {
                throw new Exception("Malformed entities string can not be associated to a time point: " + entities);
            }
            associated_entities = entities;
        } catch (Exception e) {
            System.err.println("Errors found (GregorianPoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        basicDate= new SimpleDateFormat("yyyy-MM-dd HH:mm");
        basicDate.setLenient(false); // do not accept 32 January
        
        try{
        //9,223,372,036,854,775,807 (18+1) (1 sign) (6 extra 000000) (0000-00-00 00:00) 12
        // seconds could be used but when you need seconds probably you need more precision
        String sign="+";
        if(position<0){
            sign="-";
        }
        String gs = zero_filling.substring((""+Math.abs(position)).length())+(Math.abs(position));
        //System.out.println(gs);
        String year=gs.substring(0, 10);
        String month=gs.substring(10, 12);
        String day=gs.substring(12, 14);
        String hour=gs.substring(14, 16);
        String minute=gs.substring(16, 18);
        gs=sign+year+"-"+month+"-"+day+" "+hour+":"+minute;
        basicDate.parse(gs.substring(7)); // check date format
        gregorianString=gs;

        } catch (Exception e) {
            System.err.println("Errors found (GregorianPoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }


    public long getPosition() {
        return position;
    }


    public void setPosition(long p) {
        position = p;
    }

    public String getAssociatedEntities() {
        return associated_entities;
    }

    public int compareTo(GregorianPoint o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        try {
            //this optimization is usually worthwhile, and can
            //always be added
            if (this == o) {
                return EQUAL;
            }

            //primitive numbers follow this form
            if (this.position < o.getPosition()) {
                return BEFORE;
            }
            if (this.position > o.getPosition()) {
                return AFTER;
            }
            if (this.position == o.getPosition()) {
                throw new Exception("Two TimePoints with the same value: " + this.position + " and " + o.getPosition());
            }

        } catch (Exception e) {
            System.err.println("Errors found (GregorianPoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        return EQUAL;
    }

    /**
     * Add one or more entities (separated by ",")
     * e.g., "e2_s" or "e2_s,t1_s"
     *
     * @param entities
     */
    public void associateEntities(String entities) {
        try {
            // Minimum check
            entities = entities.replaceAll("[\\s+]", "").trim();
            if (entities.length() == 0 || entities.matches(".*[,]+.*")) {
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    throw new Exception("Malformed entities string can not be associated to a time point: " + entities);
                } else {
                    return;
                }
            }
            if(entities.endsWith(",")){
                entities=entities.substring(0,entities.length()-1);
            }
            String[] e2add = entities.split(",");
            for (int i = 0; i < e2add.length; i++) {
                if (!associated_entities.contains(e2add[i])) {
                    if (associated_entities.length() == 0) {
                        associated_entities += e2add[i];
                    } else {
                        associated_entities += "," + e2add[i];
                    }
                } else {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        throw new Exception("Entity is already associated to this point: " + e2add[i] + "  -  " + associated_entities);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (GregorianPoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public Date toDate(){
        // only if it is between 0-9999 and BC can be represented by JAVA ERA
              System.err.println("Not implemented yet");
            System.exit(1);
        return null;
    }
    
    public String toGregorian() {
        return gregorianString;
    }


    @Override
    public String toString() {
        return ""+position;
    }




    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (int) this.position;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        //use instanceof instead of getClass (See Effective Java by Joshua Bloch)
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof GregorianPoint)) {
            return false;
        }
        final GregorianPoint other = (GregorianPoint) o;
        return this.position == other.position;
    }
}
