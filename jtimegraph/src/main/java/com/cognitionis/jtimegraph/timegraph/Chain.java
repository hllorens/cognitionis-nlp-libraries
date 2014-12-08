/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cognitionis.jtimegraph.timegraph;

import java.util.*;

/**
 *
 * @author hector
 */
public class Chain {

    private int id;
    // TimePoints
    private NavigableMap<Integer, TimePoint> timepoints;
    // element x of this chain is connected to element y of chain z with relation X
    // IMPORTANT: connections are set in both directions < and > because anthoug to get
    // the relation between two entities only one is needed, to print all the < or > elements
    // the graph must be able to be traversed in both directions...
    // It is better to have 2 connections because then you can make a hash per conected chain
    // That way if you want to know is there are direct connections you just need to check key "chain"
    // <local_point_positions, <dest_chain,dest_point>>   // dest_chain ensures that 1 point only has 1 conn
    // to the same chain and optimizes the chain information
    private HashMap<Integer, HashMap<Integer, TimePoint>> after_connections;
    private HashMap<Integer, HashMap<Integer, TimePoint>> before_connections;
    
    /* difficult and conflictive...
    public Chain get_deep_copy(){        
    }*/

    public Chain(int i) {
        id = i;
        after_connections = new HashMap<Integer, HashMap<Integer, TimePoint>>();
        before_connections = new HashMap<Integer, HashMap<Integer, TimePoint>>();
        timepoints = null;
        timepoints = new TreeMap<Integer, TimePoint>();
    }

    public Integer getId() {
        return id;
    }

    public NavigableMap<Integer, TimePoint> getTimePoints() {
        return this.timepoints;
    }

    public void setTimePoints(NavigableMap<Integer, TimePoint> tps){
        this.timepoints = new TreeMap<Integer, TimePoint>();
        this.timepoints.putAll(tps);
    }

    public void addConnection(Integer local_point, TimePoint dest_point, String relation) throws Exception {
        // this must detect other connections and add them and if detects
        //fully connected points of different chains it has to collapse chains (in fact that should be done by TimeGraph itself...)

        // Many checks have to be done here as well as when adding relations with the two entities added
        // when a new conexion is added many things can change... study in paper
        try {
            HashMap<Integer, TimePoint> conn = null;
            if (relation.equals("<")) {
                if (before_connections.containsKey(local_point)) {
                    conn = before_connections.get(local_point);
                } else {
                    conn = new HashMap<Integer, TimePoint>();
                }
                if (!conn.containsKey(dest_point.getChain())) {
                    conn.put(dest_point.getChain(), dest_point);
                    before_connections.put(local_point, conn);
                } else {
                    throw new Exception("Connection from point " + local_point + " in chain " + id + " is already connected in a before relation with chain " + dest_point.getChain());
                }
            } else {
                if (after_connections.containsKey(local_point)) {
                    conn = after_connections.get(local_point);
                } else {
                    conn = new HashMap<Integer, TimePoint>();
                }
                if (!conn.containsKey(dest_point.getChain())) {
                    conn.put(dest_point.getChain(), dest_point);
                    after_connections.put(local_point, conn);
                } else {
                    throw new Exception("Connection from point " + local_point + " in chain " + id + " is already connected in a before relation with chain " + dest_point.getChain());
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            throw e;
        }
        
    }


    public boolean isMoreInformative(Integer local_point, TimePoint dest_point, String relation){
            HashMap<Integer, TimePoint> conn = null;
            if (relation.equals("<")) {
                if (before_connections.containsKey(local_point)) {
                    conn = before_connections.get(local_point);
                } else {
                    conn = new HashMap<Integer, TimePoint>();
                }
                if (conn.containsKey(dest_point.getChain()) && conn.get(dest_point.getChain()).getPosition()>dest_point.getPosition()) {
                    return true;
                }
            } else {
                if (after_connections.containsKey(local_point)) {
                    conn = after_connections.get(local_point);
                } else {
                    conn = new HashMap<Integer, TimePoint>();
                }
                if (conn.containsKey(dest_point.getChain()) && conn.get(dest_point.getChain()).getPosition()<dest_point.getPosition()) {
                    return true;
                }
            }
        
        return false;
    }

    public void removeConnection(Integer local_point, TimePoint dest_point, String relation) throws Exception {
        try {
            HashMap<Integer, TimePoint> conn = null;
            if (relation.equals("<")) {
                if (before_connections.containsKey(local_point)) {
                    conn = before_connections.get(local_point);
                } else {
                    throw new Exception("Before connection from point " + local_point + " in chain " + id + " to " + dest_point.getChain() + "-" + dest_point.getPosition() + " is not found.");
                }
                if (!conn.containsKey(dest_point.getChain())) {
                    throw new Exception("Before connection from point " + local_point + " in chain " + id + " to " + dest_point.getChain() + "-" + dest_point.getPosition() + " is not found.");
                } else {
                    conn.remove(dest_point.getChain());
                    if(conn.size()==0){
                        before_connections.remove(local_point);
                    }else{
                        before_connections.put(local_point, conn);
                    }
                }
            } else {
                if (after_connections.containsKey(local_point)) {
                    conn = after_connections.get(local_point);
                } else {
                    throw new Exception("After connection from point " + local_point + " in chain " + id + " to " + dest_point.getChain() + "-" + dest_point.getPosition() + " is not found.");
                }
                if (!conn.containsKey(dest_point.getChain())) {
                    throw new Exception("After connection from point " + local_point + " in chain " + id + " to " + dest_point.getChain() + "-" + dest_point.getPosition() + " is not found.");
                } else {
                    conn.remove(dest_point.getChain());
                    if(conn.size()==0){
                        after_connections.remove(local_point);
                    }else{
                        after_connections.put(local_point, conn);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            throw e;
        }
    }

    public HashMap<Integer, TimePoint> getAfterConnections(int local_point) {
        return after_connections.get(local_point);
    }

    public HashMap<Integer, TimePoint> getBeforeConnections(int local_point) {
        return before_connections.get(local_point);
    }


    public HashMap<Integer, HashMap<Integer, TimePoint>> getAllAfterConnections(){
        return after_connections;
    }

    public HashMap<Integer, HashMap<Integer, TimePoint>> getAllBeforeConnections(){
        return before_connections;
    }


    public boolean isEmptyChain() {
        try {
            if (timepoints.isEmpty() && (!after_connections.isEmpty() || !before_connections.isEmpty())) {
                throw new Exception("Graph Integrity Error: Empty Chain with conections: " + id);
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return timepoints.isEmpty();
    }

    public boolean hasPrevious(TimePoint tp) {
        try {
            if (tp == null || tp.getChain() != id || !timepoints.containsKey(tp.getPosition())) {
                throw new Exception("Point is null (tp=" + tp + ") or not contained in chain " + id + ".");
            }
            if (timepoints.lowerKey(tp.getPosition()) == null) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    public boolean hasNext(TimePoint tp) {
        try {
            if (tp == null || tp.getChain() != id || !timepoints.containsKey(tp.getPosition())) {
                throw new Exception("Point is null (tp=" + tp + ") or not contained in chain " + id + ".");
            }
            if (timepoints.higherKey(tp.getPosition()) == null) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    public boolean hasPrevious(int position) {
        try {
            if (!timepoints.containsKey(position)) {
                throw new Exception("Point is null (position=" + position + ") or not contained in chain " + id + ".");
            }
            if (timepoints.lowerKey(position) == null) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    public boolean hasNext(int position) {
        try {
            if (!timepoints.containsKey(position)) {
                throw new Exception("Points are null (position=" + position + ") or not contained in chain " + id + ".");
            }
            if (timepoints.higherKey(position) == null) {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    /**
     * Returns if tp1 and tp2 are sequence-inverse ordered (tp2 is prvious for tp1, tp1 is next to tp2)
     * @param tp1
     * @param tp2
     * @return
     */
    public boolean isPreviousFor(TimePoint tp1, TimePoint tp2) {
        try {
            if (tp1 == null || tp2 == null || !timepoints.containsKey(tp1.getPosition()) || !timepoints.containsKey(tp1.getPosition())) {
                throw new Exception("Points are null (tp=" + tp1 + " tp2=" + tp2 + ") or not contained in chain " + id + ".");
            }
            if (tp1.getChain() == id && tp1.getChain() == tp2.getChain() && timepoints.lowerKey(tp1.getPosition()) != null && timepoints.lowerKey(tp1.getPosition()) == tp2.getPosition()) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return false;
    }

    /**
     * Returns if tp1 and tp2 are sequentially ordered (tp2 is next for tp1, tp1 previous to tp2)
     * @param tp1
     * @param tp2
     * @return
     */
    public boolean isNextFor(TimePoint tp1, TimePoint tp2) {
        try {
            if (tp1 == null || tp2 == null || !timepoints.containsKey(tp1.getPosition()) || !timepoints.containsKey(tp1.getPosition())) {
                throw new Exception("Points are null (tp=" + tp1 + " tp2=" + tp2 + ") or not contained in chain " + id + ".");
            }
            if (tp1.getChain() == id && tp1.getChain() == tp2.getChain() && timepoints.higherKey(tp1.getPosition()) != null && timepoints.higherKey(tp1.getPosition()) == tp2.getPosition()) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Errors found (Chain):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return false;
    }

    /**
     * Returns the previous position or null if not exists
     * @param tp
     * @return
     */
    public Integer getPreviousPosition(TimePoint tp) {
        return timepoints.lowerKey(tp.getPosition());
    }

    /**
     * Returns the next position or null if not exists
     * @param tp
     * @return
     */
    public Integer getNextPosition(TimePoint tp) {
        return timepoints.higherKey(tp.getPosition());
    }

    /**
     * Returns the previous position or null if not exists
     * @param tp
     * @return
     */
    public TimePoint getPreviousTimePoint(TimePoint tp) {
        if (timepoints.lowerEntry(tp.getPosition()) == null) {
            return null;
        } else {
            return timepoints.lowerEntry(tp.getPosition()).getValue();
        }
    }

    /**
     * Returns the next position or null if not exists
     * @param tp
     * @return
     */
    public TimePoint getNextTimePoint(TimePoint tp) {
        if (timepoints.higherEntry(tp.getPosition()) == null) {
            return null;
        } else {
            return timepoints.higherEntry(tp.getPosition()).getValue();
        }
    }

     /**
     * Returns a navigalbe map of the points after tp (including tp)
     * @param tp
     * @return
     */
    public NavigableMap<Integer, TimePoint> getTimePointsGreaterOrEqual(TimePoint tp) {
        if (timepoints.get(tp.getPosition()) == null) {
            return null;
        } else {
            /*NavigableMap<Integer, TimePoint> result= new TreeMap<Integer, TimePoint>();
            result.putAll(timepoints.tailMap(tp.getPosition(), true));
            return result;*/
            return timepoints.tailMap(tp.getPosition(), true);
        }
    }


     /**
     * Returns a navigalbe map of the points before tp (including tp)
     * @param tp
     * @return
     */
    public NavigableMap<Integer, TimePoint> getTimePointsLowerOrEqual(TimePoint tp) {
        if (timepoints.get(tp.getPosition()) == null) {
            return null;
        } else {
            /*NavigableMap<Integer, TimePoint> result= new TreeMap<Integer, TimePoint>();
            result.putAll(timepoints.headMap(tp.getPosition(), true));
            return result;*/
            return timepoints.headMap(tp.getPosition(), true);
        }
    }


    /**
     * Empties the chain
     */
    public void clear() {
        this.timepoints.clear();
        this.before_connections.clear();
        this.after_connections.clear();
    }
}
