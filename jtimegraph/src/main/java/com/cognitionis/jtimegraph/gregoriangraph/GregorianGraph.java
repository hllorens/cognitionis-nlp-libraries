/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cognitionis.jtimegraph.gregoriangraph;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Same as TimeGraph but without chains, all points are on the same chain
 * because all the entities are timexes or events completely attached to time
 * points in gregorian calendar
 *
 * @author hector
 */
public class GregorianGraph {

    // Only for ISO extension but currently useless -----------------
    public static final DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateFormat granul_days = new SimpleDateFormat("yyyy-MM-dd");
    public static final String upper_bound = "9999-12-31T23:59:59";
    public static final String lower_bound = "0000-01-01T00:00:00";
    // -------------------------------------------------------------
    private NavigableMap<Long, GregorianPoint> timepoints;
    // reference GregorianPoint
    private HashMap<String, GregorianPoint> entity_tp_map;

    public GregorianGraph() {
        timepoints = new TreeMap<Long, GregorianPoint>();
        entity_tp_map = new HashMap<String, GregorianPoint>();
    }

    public GregorianGraph(String filename) {
        timepoints = new TreeMap<Long, GregorianPoint>();
        entity_tp_map = new HashMap<String, GregorianPoint>();
        int linen = 0;
        try {
            BufferedReader pipesreader = new BufferedReader(new FileReader(filename));
            try {
                String pipesline;
                String[] pipesarr = null;
                while ((pipesline = pipesreader.readLine()) != null) {
                    linen++;
                    pipesarr = pipesline.split("\\s+");
                    pipesarr[1] = pipesarr[1].substring(pipesarr[1].lastIndexOf("=") + 1).replaceAll("([0-9])-", "$1");
                    pipesarr[2] = pipesarr[2].substring(pipesarr[2].lastIndexOf("=") + 1).replaceAll("([0-9])-", "$1");
                    long s = Long.parseLong(pipesarr[1]);
                    long e = Long.parseLong(pipesarr[2]);

                    if (pipesarr[1].length() == 4 || pipesarr[1].length() == 5) {
                        s *= 100000000;
                        if (s > 0) {
                            s += 1010000;
                        } else {
                            s -= 1010000;
                        }
                    } else {
                        if (pipesarr[1].length() == 8 || pipesarr[1].length() == 9) {
                            s *= 10000;
                        } else {
                            throw new Exception(pipesarr[1] + " malformed date.");
                        }
                    }
                    if (pipesarr[2].length() == 4 || pipesarr[2].length() == 5) {
                        e *= 100000000;
                        if (e >= 0) {
                            e += 12310000;
                        } else {
                            e -= 12310000;
                        }
                    } else {
                        if (pipesarr[2].length() == 8 || pipesarr[2].length() == 9) {
                            e *= 10000;
                        } else {
                            throw new Exception(pipesarr[2] + " malformed date.");
                        }
                    }
                    if (s > e) {
                        System.err.println("Start time is later than end time: " + pipesarr[0]);
                        continue;
                    }
                    this.addEntity(pipesarr[0], s, e);
                }
            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (GregorianGraph):\n\t" + e.toString() + " line" + linen + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    public boolean addEntity(String entity, long gregpoint_s, long gregpoint_e) {
        try {
            if (entity != null) {
                entity = entity.trim();
            }
            if (entity == null || entity.length() == 0 || gregpoint_e <= gregpoint_s) {
                throw new Exception("Mlformed entity (e=" + entity + " gp_s=" + gregpoint_s + " gp_e=" + gregpoint_e + ")");
            }

            String x1 = entity + "_s";
            String x2 = entity + "_e";

            if (!entity_tp_map.containsKey(x1)) {
                GregorianPoint tp = new GregorianPoint(gregpoint_s, x1);
                entity_tp_map.put(x1, tp);
                if (timepoints.containsKey(gregpoint_s)) {
                    timepoints.get(gregpoint_s).associateEntities(x1);
                } else {
                    timepoints.put(gregpoint_s, tp);
                }
            }

            if (!entity_tp_map.containsKey(x2)) {
                GregorianPoint tp = new GregorianPoint(gregpoint_e, x2);
                entity_tp_map.put(x2, tp);
                if (timepoints.containsKey(gregpoint_e)) {
                    timepoints.get(gregpoint_e).associateEntities(x2);
                } else {
                    timepoints.put(gregpoint_e, tp);
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (GregorianGraph):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return false;
        }
        return true;
    }

    /**
     * Returns the point relation between two points in the graph
     *
     * @param x
     * @param y
     * @return
     */
    public String getPointRelation(GregorianPoint x, GregorianPoint y) {
        return getPositionRelation(x.getPosition(), y.getPosition());
    }

    /**
     * Returns =,<,> depending on the tp relation of two elements in the same
     * chain
     *
     * @param a
     * @param b
     * @return
     */
    public static String getPositionRelation(long position_x, long position_y) {
        if (position_x > position_y) {
            return ">";
        } else {
            if (position_x < position_y) {
                return "<";
            } else {
                return "=";
            }
        }
    }

    @Override
    public String toString() {
        String out = "";

        for (int i = timepoints.size() - 1; i >= 0; i--) {
            boolean firstpoint = true;
            out += "\nChain " + i + ": ";
            for (GregorianPoint tp : timepoints.values()) {
                if (firstpoint) {
                    firstpoint = false;
                } else {
                    out += " ---> ";
                }
                out += tp.toGregorian() + "[" + tp.getAssociatedEntities() + "]";
            }
            out += "\n";
        }

        return out;
    }

    /**
     * Given a relation name, return the inverse Allen-TimeML relation
     *
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
            throw new Exception("Unknow relation: " + rel);
        } catch (Exception e) {
            System.err.println("Errors found (TimeML_Merger):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return rel;
        }
    }

    /**
     * Return if the TimeGraph contains a relation between two intervals
     * (entities)
     *
     * @param x
     * @param y
     * @param rel
     * @return
     */
    public String checkRelation(String x, String y, String rel) {
        rel = rel.toUpperCase();
        // inverse relation if needed
        if (!rel.matches("(BEFORE|IBEFORE|BEGINS|ENDS|OVERLAPS|IS_INCLUDED|SIMULTANEOUS)")) {
            rel = reverseRelationCategory(rel);
            String aux = x;
            x = y;
            y = aux;
        }
        // GregorianPoints are already known
        String x1 = x + "_s";
        String x2 = x + "_e";
        String y1 = y + "_s";
        String y2 = y + "_e";

        GregorianPoint tpx1 = entity_tp_map.get(x1);
        GregorianPoint tpx2 = entity_tp_map.get(x2);
        GregorianPoint tpy1 = entity_tp_map.get(y1);
        GregorianPoint tpy2 = entity_tp_map.get(y2);
        try {
            if (tpx1 == null || tpx2 == null) {
                if (!x.matches("^[0-9]+.*")) {
                    throw new Exception(x);
                }
                String interval = get_interval_value(x);
                if (timepoints.containsKey(Long.parseLong(interval.split("\\|")[0])) && timepoints.containsKey(Long.parseLong(interval.split("\\|")[1]))) {
                    x1 = interval.split("\\|")[0];
                    x2 = interval.split("\\|")[1];
                    tpx1 = timepoints.get(Long.parseLong(x1));
                    tpx2 = timepoints.get(Long.parseLong(x2));
                } else {
                    throw new Exception(x);
                }
            }
            if (tpy1 == null || tpy2 == null) {
                if (!y.matches("^[0-9]+.*")) {
                    throw new Exception(y);
                }
                String interval = get_interval_value(y);
                if (timepoints.containsKey(Long.parseLong(interval.split("\\|")[0])) && timepoints.containsKey(Long.parseLong(interval.split("\\|")[1]))) {
                    y1 = interval.split("\\|")[0];
                    y2 = interval.split("\\|")[1];
                    tpy1 = timepoints.get(Long.parseLong(y1));
                    tpy2 = timepoints.get(Long.parseLong(y2));
                } else {
                    throw new Exception(y);
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "unknown (entity " + e.getMessage() + " not found)";
        }

        if (rel.equals("BEFORE")) { // Allen (<)  x1 < x2 < y1 < y2
            String pointrel = getPointRelation(tpx2, tpy1);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("IBEFORE")) { // Allen's meets (m) x1 < (x2 = y1) < y2
            String pointrel = getPointRelation(tpx2, tpy1);
            if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("BEGINS")) { // Allen's starts (s) (x1 = y1) < x2 < y2
            //x1=y1
            String pointrel = getPointRelation(tpx1, tpy1);
            if (!pointrel.equals("=")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unkonwn";
                }
            }
        }

        if (rel.equals("ENDS")) { // Allen's Finishes (f) y1 < x1 < (x2 = y2)
            //x2=y2
            String pointrel = getPointRelation(tpx2, tpy2);
            if (!pointrel.equals("=")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // y1 < x1
            pointrel = getPointRelation(tpy1, tpx1);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("OVERLAPS")) { // Allen's Overlaps (o) x1 < y1 < x2 < y2
            // x1 < y1
            String pointrel = getPointRelation(tpx1, tpy1);
            if (!pointrel.equals("<")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // y1<x2
            pointrel = getPointRelation(tpy1, tpx2);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("IS_INCLUDED")) { // Allen's during (d) y1 < x1 < x2 < y2
            // y1 < x1
            String pointrel = getPointRelation(tpy1, tpx1);
            if (!pointrel.equals("<")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("SIMULTANEOUS")) { //  # Allen's equal (=) (x1 = y1) < (x2 = y2)
            //x1=y1
            String pointrel = getPointRelation(tpx1, tpy1);
            if (!pointrel.equals("=")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            //x2=y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("=")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }
        return "unknown";
    }

    /**
     * Return the relation of 2 entities in the TimeGraph or UNKNOWN if these
     * are not related
     *
     * @param x
     * @param y
     * @param rel
     * @return
     */
    public String getRelation(String x, String y) {
        // GregorianPoints are already known
        String x1 = x + "_s";
        String x2 = x + "_e";
        String y1 = y + "_s";
        String y2 = y + "_e";

        GregorianPoint tpx1 = entity_tp_map.get(x1);
        GregorianPoint tpx2 = entity_tp_map.get(x2);
        GregorianPoint tpy1 = entity_tp_map.get(y1);
        GregorianPoint tpy2 = entity_tp_map.get(y2);
        try {
            if (tpx1 == null || tpx2 == null) {
                if (!x.matches("^[0-9]+.*")) {
                    throw new Exception(x);
                }
                String interval = get_interval_value(x);
                if (timepoints.containsKey(Long.parseLong(interval.split("\\|")[0])) && timepoints.containsKey(Long.parseLong(interval.split("\\|")[1]))) {
                    x1 = interval.split("\\|")[0];
                    x2 = interval.split("\\|")[1];
                    tpx1 = timepoints.get(Long.parseLong(x1));
                    tpx2 = timepoints.get(Long.parseLong(x2));
                } else {
                    throw new Exception(x);
                }
            }
            if (tpy1 == null || tpy2 == null) {
                if (!y.matches("^[0-9]+.*")) {
                    throw new Exception(y);
                }
                String interval = get_interval_value(y);
                if (timepoints.containsKey(Long.parseLong(interval.split("\\|")[0])) && timepoints.containsKey(Long.parseLong(interval.split("\\|")[1]))) {
                    y1 = interval.split("\\|")[0];
                    y2 = interval.split("\\|")[1];
                    tpy1 = timepoints.get(Long.parseLong(y1));
                    tpy2 = timepoints.get(Long.parseLong(y2));
                } else {
                    throw new Exception(y);
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "unknown (entity " + e.getMessage() + " not found)";
        }

        // BEFORE Allen (<)  x1 < x2 < y1 < y2
        String pointrel = getPointRelation(tpx2, tpy1);
        if (pointrel.equals("<")) {
            return "BEFORE";
        }

        // IBEFORE Allen's meets (m) x1 < (x2 = y1) < y2
        if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
            return "IBEFORE";
        }

        // BEGINS Allen's starts (s) (x1 = y1) < x2 < y2
        //x1=y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("=")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "BEGINS";
            }
        }


        // ENDS Allen's Finishes (f) y1 < x1 < (x2 = y2)
        //x2=y2
        pointrel = getPointRelation(tpx2, tpy2);
        if (pointrel.equals("=")) {
            // y1 < x1
            pointrel = getPointRelation(tpy1, tpx1);
            if (pointrel.equals("<")) {
                return "ENDS";
            }
        }


        // OVERLAPS Allen's Overlaps (o) x1 < y1 < x2 < y2
        // x1 < y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("<")) {
            // y1<x2
            pointrel = getPointRelation(tpy1, tpx2);
            if (pointrel.equals("<")) {
                return "OVERLAPS";
            }
        }

        // IS_INCLUDED Allen's during (d) y1 < x1 < x2 < y2
        // y1 < x1
        pointrel = getPointRelation(tpy1, tpx1);
        if (pointrel.equals("<")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "IS_INCLUDED";
            }
        }

        // SIMULTANEOUS Allen's equal (=) (x1 = y1) < (x2 = y2)
        //x1=y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("=")) {
            //x2=y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("=")) {
                return "SIMULTANEOUS";
            }
        }

        // inverse relation and check again
        String aux = x;
        x = y;
        y = aux;
        // GregorianPoints are already known
        x1 = x + "_s";
        x2 = x + "_e";
        y1 = y + "_s";
        y2 = y + "_e";

        tpx1 = entity_tp_map.get(x1);
        tpx2 = entity_tp_map.get(x2);
        tpy1 = entity_tp_map.get(y1);
        tpy2 = entity_tp_map.get(y2);

        // BEFORE Allen (<)  x1 < x2 < y1 < y2
        pointrel = getPointRelation(tpx2, tpy1);
        if (pointrel.equals("<")) {
            return "AFTER";
        }

        // IBEFORE Allen's meets (m) x1 < (x2 = y1) < y2
        if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
            return "IAFTER";
        }

        // BEGINS Allen's starts (s) (x1 = y1) < x2 < y2
        //x1=y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("=")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "BEGUN_BY";
            }
        }


        // ENDS Allen's Finishes (f) y1 < x1 < (x2 = y2)
        //x2=y2
        pointrel = getPointRelation(tpx2, tpy2);
        if (pointrel.equals("=")) {
            // y1 < x1
            pointrel = getPointRelation(tpy1, tpx1);
            if (pointrel.equals("<")) {
                return "ENDED_BY";
            }
        }
        // OVERLAPS Allen's Overlaps (o) x1 < y1 < x2 < y2
        // x1 < y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("<")) {
            // y1<x2
            pointrel = getPointRelation(tpy1, tpx2);
            if (pointrel.equals("<")) {
                return "OVERLAPPED_BY";
            }
        }

        // IS_INCLUDED Allen's during (d) y1 < x1 < x2 < y2
        // y1 < x1
        pointrel = getPointRelation(tpy1, tpx1);
        if (pointrel.equals("<")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "INCLUDES";
            }
        }

        // SIMULTANEOUS Allen's equal (=) (x1 = y1) < (x2 = y2)
        // ALREADY CHECKED

        return "unknown";
    }

    public String getEntitiesBeforeEntity(String x) {
        // GregorianPoints are already known
        String x1 = x + "_s";
        GregorianPoint tpx1 = entity_tp_map.get(x1);
        try {
            if (tpx1 == null) {
                if (!x.matches("^[0-9]+.*")) {
                    throw new Exception(x);
                }
                String interval = get_interval_value(x);
                x1 = interval.split("\\|")[0];
                if (timepoints.containsKey(Long.parseLong(interval.split("\\|")[0]))) {
                    tpx1 = entity_tp_map.get(x1);
                } else {
                    if (timepoints.isEmpty()) {
                        return "[] (there are no dates to compare in the graph)";
                    } else {
                        timepoints.put(Long.parseLong(x1), null);
                        if (timepoints.lowerKey(Long.parseLong(x1)) != null) {
                            String x1x = timepoints.lowerEntry(Long.parseLong(x1)).getValue().toString();
                            tpx1 = timepoints.get(Long.parseLong(x1x));
                        } else {
                            return "[] (there are no dates before " + x + ")";
                        }
                        timepoints.remove(Long.parseLong(x1));
                    }
                }
            }

        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }
        // traverse before
        return contract_entities(get_timegraph_before(tpx1));
    }

    public String get_timegraph_before(GregorianPoint x) {
        String output = x.getAssociatedEntities();
        // check point conections
        if (timepoints.lowerKey(x.getPosition()) != null) {
            String aux = get_timegraph_before(timepoints.lowerEntry(x.getPosition()).getValue());
            if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                output += ",";
            }
            output += aux;
        }

        return output;
    }

    public String getEntitiesAfterEntity(String x) {
        // GregorianPoints are already known
        String x1 = x + "_e";
        GregorianPoint tpx1 = entity_tp_map.get(x1);
        try {
            if (tpx1 == null) {
                if (!x.matches("^[0-9]+.*")) {
                    throw new Exception(x);
                }
                String interval = get_interval_value(x);
                x1 = interval.split("\\|")[1];
                if (timepoints.containsKey(Long.parseLong(interval.split("\\|")[1]))) {
                    tpx1 = entity_tp_map.get(x1);
                } else {
                    if (timepoints.isEmpty()) {
                        return "[] (there are no dates to compare in the graph)";
                    } else {
                        timepoints.put(Long.parseLong(x1), null);
                        if (timepoints.higherKey(Long.parseLong(x1)) != null) {
                            String x1x = timepoints.higherEntry(Long.parseLong(x1)).getValue().toString();
                            tpx1 = timepoints.get(Long.parseLong(x1x));
                        } else {
                            return "[] (there are no dates after " + x + ")";
                        }
                        timepoints.remove(Long.parseLong(x1));
                    }
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }
        // traverse after
        return contract_entities(get_timegraph_after(tpx1));
    }

    public String get_timegraph_after(GregorianPoint x) {
        String output = x.getAssociatedEntities();
        if (timepoints.higherKey(x.getPosition()) != null) {
            String aux = get_timegraph_after(timepoints.higherEntry(x.getPosition()).getValue());
            if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                output += ",";
            }
            output += aux;
        }

        return output;
    }

    public String getEntitiesBetween(String x, String y) {
        // GregorianPoints are already known
        GregorianPoint tpx1 = null;
        GregorianPoint tpy2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String y2 = y + "_e";
        tpy2 = entity_tp_map.get(y2);

        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpy2 == null) {
                throw new Exception(y);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }

        String relation = getPointRelation(tpx1, tpy2);
        if (relation.equals("UNKNOWN")) {
            return "[] (entities not related)";
        }

        if (relation.equals("=") || relation.equals(">")) {
            x1 = y + "_e";
            tpx1 = entity_tp_map.get(x1);
            y2 = x + "_s";
            tpy2 = entity_tp_map.get(y2);
            if (relation.equals("=")) {
                return "[] (same time point)";
            }
        }

        // traverse after
        String output = get_timegraph_after_matching_entity(tpx1, tpy2.getAssociatedEntities());

        return contract_entities(output);
    }

    public String getEntitiesWithinEntity(String x) {
        // GregorianPoints are already known
        GregorianPoint tpx1 = null;
        GregorianPoint tpx2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String x2 = x + "_e";
        tpx2 = entity_tp_map.get(x2);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpx2 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }

        // traverse after
        String output = get_timegraph_after_matching_entity(tpx1, tpx2.getAssociatedEntities());

        return contract_entities(output);
    }

    public String get_timegraph_after_matching_entity(GregorianPoint x, String e) {
        String output = x.getAssociatedEntities();
        if (output.contains(e)) {
            return output;
        }
        if (timepoints.higherKey(x.getPosition()) != null) {
            String aux = get_timegraph_after_matching_entity(timepoints.higherEntry(x.getPosition()).getValue(), e);
            if (aux.contains(e)) {
                if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                    output += ",";
                }
                output += aux;
            }
        }
        if (!output.contains(e)) {
            output = "";
        }
        return output;
    }

    public String getEntitiesSinceEntity(String x) {
        // GregorianPoints are already known
        String x1 = x + "_s";
        GregorianPoint tpx1 = entity_tp_map.get(x1);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }
        // traverse before
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        return contract_entities(tpx1.getAssociatedEntities() + "," + get_timegraph_after(tpx1));
    }

    public String getEntitiesSimultaneous(String x) {
        // GregorianPoints are already known
        GregorianPoint tpx1 = null;
        GregorianPoint tpx2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String x2 = x + "_e";
        tpx2 = entity_tp_map.get(x2);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpx2 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }

        // traverse before
        return tpx1.getAssociatedEntities() + "," + tpx2.getAssociatedEntities();
    }

    public String getEntitiesIncludeEntity(String x) {
        // GregorianPoints are already known
        GregorianPoint tpx1 = null;
        GregorianPoint tpx2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String x2 = x + "_e";
        tpx2 = entity_tp_map.get(x2);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpx2 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }


        // traverse before
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        String ending_after = get_timegraph_after(tpx2).replaceAll("[eit]+[0-9]+_s(,)?", "");
        visited_chains = new HashMap<Integer, String>();
        String starting_before = get_timegraph_before(tpx1).replaceAll("[eit]+[0-9]+_e(,)?", "");

        String output = starting_before;
        if (!output.equals("") && !output.endsWith(",") && !ending_after.equals("")) {
            output += ",";
        }
        if (!ending_after.equals("")) {
            output += ending_after;
        }
        if (output.equals("")) {
            return "[]";
        } else {
            output = contract_entities(output).replaceAll(x + "[,]\\s*", "").replaceAll(x + "[\\]]*", "]").replaceAll("[eit]+[0-9]+_[se](,\\s*)?", "").replaceAll(",\\s*]", "]");
            if (output.equals("")) {
                return "[]";
            } else {
                return output;
            }
        }
    }

    public static String reversePointRelation(String r) {
        String o = r;
        if (r.equals("<")) {
            return ">";
        }
        if (r.equals(">")) {
            return "<";
        }
        return o;
    }

    public static String reversePointStartEnd(String s) {
        if (s.equals("s")) {
            return "e";
        } else {
            return "s";
        }
    }

    public static String contract_entities(String entity_string) {
        HashSet<String> entities = new HashSet<String>();
        if (entity_string != null && entity_string.trim().length() > 0) {
            String[] outputarr = entity_string.split(",");
            for (int i = 0; i < outputarr.length; i++) {
                String[] e = new String[2];
                e[0] = outputarr[i].substring(0, outputarr[i].lastIndexOf("_"));
                e[1] = outputarr[i].substring(outputarr[i].lastIndexOf("_") + 1);
                if (!entities.contains(e[0])) {
                    if (entities.contains(e[0] + "_" + reversePointStartEnd(e[1]))) {
                        entities.remove(e[0] + "_" + reversePointStartEnd(e[1]));
                        entities.add(e[0]);
                    } else {
                        entities.add(outputarr[i]);
                    }
                }
            }
        }
        return entities.toString();
    }

    /**
     * Get all possible consistent relations between two entities of the graph
     */
    public ArrayList<String> getPossibleConsistentRelations(String x, String y) {
        ArrayList<String> possible_rels = new ArrayList<String>();

        String x1 = x + "_s";
        String x2 = x + "_e";
        String y1 = y + "_s";
        String y2 = y + "_e";
        // GregorianPoints are already known
        GregorianPoint tpx1 = entity_tp_map.get(x1);
        GregorianPoint tpx2 = entity_tp_map.get(x2);
        GregorianPoint tpy1 = entity_tp_map.get(y1);
        GregorianPoint tpy2 = entity_tp_map.get(y2);
        try {
            if (tpx1 == null || tpx2 == null) {
                throw new Exception(x);
            }
            if (tpy1 == null || tpy2 == null) {
                throw new Exception(y);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return possible_rels;
        }

        String pointrel_x2_y1 = getPointRelation(tpx2, tpy1);
        String pointrel_x1_y1 = getPointRelation(tpx1, tpy1);
        String pointrel_x2_y2 = getPointRelation(tpx2, tpy2);
        String pointrel_y1_x1 = reversePointRelation(pointrel_x1_y1);
        String pointrel_y2_x1 = getPointRelation(tpy2, tpx1);

        if (pointrel_x2_y1.equals("<") || pointrel_x2_y1.equals("UNKNOWN")) {
            possible_rels.add("BEFORE");
        }
        if (pointrel_y2_x1.equals("<") || pointrel_y2_x1.equals("UNKNOWN")) {
            possible_rels.add("AFTER");
        }

        if (pointrel_x2_y1.equals("=") || pointrel_x2_y1.equals("UNKNOWN")) {
            possible_rels.add("IBEFORE");
        }
        if (pointrel_y2_x1.equals("=") || pointrel_y2_x1.equals("UNKNOWN")) {
            possible_rels.add("IAFTER");
        }

        if (pointrel_x1_y1.equals("=") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals("<") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("BEGINS");
            }
        }

        if (pointrel_x1_y1.equals("=") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("BEGUN_BY");
            }
        }

        if (pointrel_x2_y2.equals("=") || pointrel_x2_y2.equals("UNKNOWN")) {
            if (pointrel_y1_x1.equals("<") || pointrel_y1_x1.equals("UNKNOWN")) {
                possible_rels.add("ENDS");
            }
        }

        if (pointrel_x2_y2.equals("=") || pointrel_x2_y2.equals("UNKNOWN")) {
            if (pointrel_y1_x1.equals(">") || pointrel_y1_x1.equals("UNKNOWN")) {
                possible_rels.add("ENDED_BY");
            }
        }


        if (pointrel_x1_y1.equals("<") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y1.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("OVERLAPS");
            }
        }

        if (pointrel_x1_y1.equals(">") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_y2_x1.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("OVERLAPPED_BY");
            }
        }

        if (pointrel_y1_x1.equals("<") || pointrel_y1_x1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals("<") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("IS_INCLUDED");
            }
        }

        if (pointrel_y1_x1.equals(">") || pointrel_y1_x1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("INCLUDEDS");
            }
        }

        if (pointrel_x1_y1.equals("=") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals("=") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("SIMULTANEOUS");
            }
        }
        return possible_rels;
    }

    public static String get_interval_value(String value) {
        String lv = value;
        String uv = value;
        if (value.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2))")) {
            if (value.matches("(?i).*-(WI|Q1|H1|T1)")) {
                lv = value.substring(0, 4) + "-01-01";
                uv = value.substring(0, 4) + "-03-01";
            }
            if (value.matches("(?i).*-(SP|Q2|T2)")) {
                lv = value.substring(0, 4) + "-03-01";
                uv = value.substring(0, 4) + "-05-31";
            }
            if (value.matches("(?i).*-(SU|Q3|H2|T3)")) {
                lv = value.substring(0, 4) + "-06-01";
                uv = value.substring(0, 4) + "-08-31";
            }
            if (value.matches("(?i).*-(AU|FA|Q4|T4)")) {
                lv = value.substring(0, 4) + "-09-01";
                uv = value.substring(0, 4) + "-12-31";
            }
            lv = value + lower_bound.substring(value.length());
            uv = value + upper_bound.substring(value.length());
            lv = lv.replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
            uv = uv.replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
        }

        if (value.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T(MO|AF|EV|NI|MI|NO)")) {
            // MORNING 5-12
            if (value.matches("(?i).*TMO")) {
                lv = value.substring(0, 10) + "T05:00:00";
                uv = value.substring(0, 10) + "T11:59:59";
            } // NOON 12
            if (value.matches("(?i).*TNO")) {
                lv = value.substring(0, 10) + "T12:00:00";
                uv = value.substring(0, 10) + "T12:00:01";
            } // AFTERNOON 13
            if (value.matches("(?i).*TAF")) {
                lv = value.substring(0, 10) + "T12:00:01";
                uv = value.substring(0, 10) + "T17:59:59";
            } // DEPEND ON WORK BREAKS 17-18
            if (value.matches("(?i).*TEV")) {
                lv = value.substring(0, 10) + "T18:00:00";
                uv = value.substring(0, 10) + "T20:59:59";
            } // AFTER WORK... GOING BACK HOME...
            if (value.matches("(?i).*TNI")) {
                lv = value.substring(0, 10) + "T21:00:00";
                uv = value.substring(0, 10) + "T04:59:59";
            } // MIDNIGHT
            if (value.matches("(?i).*TMI")) {
                lv = value.substring(0, 10) + "T00:00:00";
                uv = value.substring(0, 10) + "T00:00:01";
            }
            lv = lv.replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
            uv = uv.replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
        }

        if (value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(T[0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)?)?)?)?)?)?)?")) {
            lv = value + lower_bound.substring(value.length());
            uv = value + upper_bound.substring(value.length());
            if (value.length() == 7) {
                Date dateaux = null;
                try {
                    dateaux = (Date) iso.parse(lv);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(dateaux);
                cal.add(GregorianCalendar.MONTH, 1);
                cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                uv = granul_days.format(cal.getTime()) + upper_bound.substring(value.length() + 3);
            }
            lv = lv.replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
            uv = uv.replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
        }

        if (value.matches("[0-9]{12,}")) {
            long v = Long.parseLong(value);
            if (v >= 0) {
                uv = "" + (v + 1);
            } else {
                lv = "" + (v - 1);
            }
        }

        return (lv + "|" + uv).replaceAll("[T:-]", "");
    }
}
