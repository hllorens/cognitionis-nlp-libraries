package com.cognitionis.jtimegraph.timegraph;

/**
 *
 * @author Hector Llorens
 */
public final class TimePoint implements Comparable<TimePoint> {

    public static final int DEFAULT_position = 100000;
    public static final int DEFAULT_diff = 1000;
    private int position; // ID and pseudo time value in the chain
    // int is faster than Integer (simpler)
    // +-2,147,483,647
    // if necessary extend to long: 9,223,372,036,854,775,807
    // add some warning in some checking function... if position > 2140000000 warning
    private int chain; // useful to hanlde equalTo (timePoints are always in chains)
    private String associated_entities;

    // in the future
    // Gregorian calendar reference time?
    // then the coolest thing is visualizing what you really know (...) in time and the rest as relative...
    // Date timeReference;
    public TimePoint(int c) {
        position = DEFAULT_position;
        chain = c;
        associated_entities = "";
    }

    public TimePoint(int c, int p) {
        chain = c;
        position = p;
        associated_entities = "";
    }

    public TimePoint(int c, String entities) {
        chain = c;
        position = DEFAULT_position;
        try {
            entities = entities.replaceAll("\\s+", "");
            if (entities.length() == 0 || entities.matches(".*,[,]+.*")) {
                throw new Exception("Malformed entities string can not be associated to a time point: " + entities);
            }
            associated_entities = entities;
        } catch (Exception e) {
            System.err.println("Errors found (TimePoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public TimePoint(int c, int p, String entities) {
        chain = c;
        position = p;
        try {
            entities = entities.replaceAll("\\s+", "");
            if (entities.length() == 0 || entities.matches(".*,[,]+.*")) {
                throw new Exception("Malformed entities string can not be associated to a time point: " + entities);
            }
            associated_entities = entities;
        } catch (Exception e) {
            System.err.println("Errors found (TimePoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public int getChain() {
        return chain;
    }

    public void setChain(int v) {
        chain = v;
    }

    public int getPosition() {
        return position;
    }

    public int getPositionBefore() {
        return position - DEFAULT_diff;
    }

    public int getPositionAfter() {
        return position + DEFAULT_diff;
    }

    public void setPosition(int v) {
        position = v;
    }

    public String getAssociatedEntities() {
        return associated_entities;
    }

    public int compareTo(TimePoint o) {
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
            System.err.println("Errors found (TimePoint):\n\t" + e.toString() + "\n");
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
            if (entities.length() == 0 || entities.matches(".*,[,]+.*")) {
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
            System.err.println("Errors found (TimePoint):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Override
    public String toString() {
        return chain + "-" + position;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.position;
        hash = 59 * hash + this.chain;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        //use instanceof instead of getClass (See Effective Java by Joshua Bloch)
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof TimePoint)) {
            return false;
        }
        final TimePoint other = (TimePoint) o;
        return this.position == other.position && this.chain == other.chain;
    }
}
