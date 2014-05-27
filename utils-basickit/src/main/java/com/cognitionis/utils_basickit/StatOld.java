package com.cognitionis.utils_basickit;


import java.util.*;

/**
 * @author Hector Llorens
 * @since 2009
 */
public class StatOld {

    private int basecol;
    private int classcol;
    private int numtokcol;
    private String[] coldesc_arr;
    private Integer coldesc_arr_count;
    private HashMap<String, HashMap> elements; // TIMEX3, EVENT | EVENT-OCURRENCE... (PRIMARY KEY)
    private Integer totalDataAdded;
    private Integer totalGlobalDataAdded;
    private int MAXnumtok;

    public StatOld() {
        this(null, null, null);
    }

    public StatOld(String[] coldesc) {
        this(coldesc, null, null);
    }

    public StatOld(String[] coldesc, String basecol_re) {
        this(coldesc, basecol_re, null);
    }

    public StatOld(String[] coldesc, String basecol_re, String classcol_re) {
        coldesc_arr = null;
        coldesc_arr_count = 0;
        totalDataAdded = 0;
        totalGlobalDataAdded = 0;
        MAXnumtok = 0;
        basecol = -1;
        classcol = -1;
        numtokcol = -1;

        try {
            if (coldesc == null || coldesc.length < 1) {
                throw new Exception("Column description is null or empty");
            }
            coldesc_arr = coldesc;
            coldesc_arr_count = coldesc.length;

            if (basecol_re != null) {
                basecol = getColumn(basecol_re);
            }

            if (classcol_re != null) {
                classcol = getColumn(classcol_re);
            }

            if (basecol == -1) {
                basecol = 0;
            }

            numtokcol = getColumn("numtok");


            // luego siempre comprovar si classcol!=-1 y si lo es pues usar el combinado BASE-CLASS
            // crear estructura vacia para la estadistica teniendo en cuenta las columnas
            elements = new HashMap<String, HashMap>();


        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }

    public int getColumn(String colname_re) {
        for (int i = 0; i < coldesc_arr_count; i++) {
            if (coldesc_arr[i].matches(colname_re)) {
                return i;
            }
        }
        return -1;
    }

    public void addData(final String[] data) {
        // TODO realment no fa falta fer alguns puts pq el get passa el objecte per valor
        // a no ser que es faça amb new Object() i per tant amb colElement.put(x) es suficient.

        // añadir/procesar en las estructuras creadas
        //System.out.println(data.length + " - " + coldesc_arr_count + " - " + data[2]);
        totalDataAdded++;
        if (numtokcol != -1) {
            if (Integer.parseInt(data[numtokcol]) > MAXnumtok) {
                MAXnumtok = Integer.parseInt(data[numtokcol]);
            }
        }

        try {
            HashMap<String, HashMap> dataMap = new HashMap<String, HashMap>();
            if (data.length != coldesc_arr_count) {
                throw new Exception("Malformed data");
            }

            String datakey;
            if (classcol != -1) {
                datakey = data[basecol] + "-" + data[classcol];
            } else {
                datakey = data[basecol];
            }

            if (elements.containsKey(datakey)) {
                HashMap<String, HashMap> colsMap = elements.get(datakey);
                for (int col = 0; col < coldesc_arr_count; col++) {
                    if (col != basecol && col != classcol) {
                        HashMap<String, Integer> colMap = colsMap.get(coldesc_arr[col]);
                        Integer colMapValue = colMap.get(data[col]);
                        if (colMapValue != null) {
                            colMapValue++;
                        } else {
                            colMapValue = 1;
                        }
                        colMap.put(data[col], colMapValue);
                        //colsMap.put(coldesc_arr[col], colMap); // java modifica obj por valor
                    }
                }
                final Integer totalValue = ((Integer) (colsMap.get("total")).get("total")) + 1;
                colsMap.put("total", new HashMap<String, Integer>() {

                    {
                        put("total", totalValue);
                    }
                });
                //elements.put(datakey, colsMap); // java modifica obj por valor
            } else {
                // fill the dataMap
                for (int col = 0; col < coldesc_arr_count; col++) {
                    if (col != basecol && col != classcol) {
                        final String tempdata = data[col];
                        dataMap.put(coldesc_arr[col], new HashMap<String, Integer>() {

                            {
                                put(tempdata, 1);
                            }
                        });
                    }
                }
                dataMap.put("total", new HashMap<String, Integer>() {

                    {
                        put("total", 1);
                    }
                });

                elements.put(datakey, dataMap);
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    /**
     * adds 1 to the value of GLOBAL statistics subkey in key if it is found
     * (i.e., [pos,VBZ], if it is found and its value is 5 then become 6
     *
     * @param statkey
     * @param statsubkey
     */
    public void addGLOBALdata(String statkey, String statsubkey) {
        try {
            if (elements.get("GLOBAL") == null) {
                this.createGLOBALelement();
            }
            HashMap<String, HashMap> colsMapGlobal = elements.get("GLOBAL");
            if (colsMapGlobal.get(statkey) != null) {
                HashMap<String, Integer> colMapGlobal = colsMapGlobal.get(statkey);
                if (colMapGlobal.get(statsubkey) != null) {
                    Integer colMapValue = colMapGlobal.get(statsubkey);
                    if (colMapValue != null) {
                        colMapValue++;
                    } else {
                        colMapValue = 1;
                    }
                    final Integer totalValue = ((Integer) (colsMapGlobal.get("total")).get("total")) + 1;
                    colsMapGlobal.put("total", new HashMap<String, Integer>() {

                        {
                            put("total", totalValue);
                        }
                    });

                    colMapGlobal.put(statsubkey, colMapValue); // Necessary because Integer is a basic type and then not a reference
                    totalGlobalDataAdded++;
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

    public void print(){
        print(1);
    }

    public void print(int minpercent) {
        System.err.println("\nPrinting Statistics (total Data " + totalDataAdded + ")\n-------------------------");
        //System.out.println(elements);
        if (elements.get("GLOBAL") != null) {
            printGlobal(minpercent);
        } else {
            printSimple(minpercent);
        }
    }

    public void printSimple(int minpercent) {
        for (String current_key : elements.keySet()) {
            HashMap<String, HashMap> colsMap = elements.get(current_key);
            Integer elementTotal = (Integer) (colsMap.get("total")).get("total");
            System.out.println(current_key + "(" + elementTotal + ")");
            for (String cols_key : colsMap.keySet()) {
                //if(!cols_key.equalsIgnoreCase(coldesc_arr[basecol]) && !cols_key.equalsIgnoreCase(coldesc_arr[classcol]) && !cols_key.equalsIgnoreCase("total")){
                if (!cols_key.equalsIgnoreCase("total")) {
                    System.out.println("\t" + cols_key);
                    HashMap<String, Integer> colMap = colsMap.get(cols_key);
                    TreeMap<String, Integer> sortedColMap = new TreeMap(new DescStringIntMapComparator(colMap));
                    sortedColMap.putAll(colMap);
                    for (String col_key : sortedColMap.keySet()) {
                        Integer colValue = colMap.get(col_key);
                        //double colPercent=((double) colValue/(double) elementTotal)*100.0;
                        int colPercent = (int) Math.round(((double) colValue / (double) elementTotal) * 100.0);
                        if (colPercent >= minpercent) {
                            System.out.println("\t\t" + col_key + "\t" + colPercent + "%" + " (" + colValue + ")"); //+" ("+colValue+")"
                        }
                    }
                    System.out.println();
                }

            }
            System.out.println();
        }
        System.out.println();
    }

    public void printGlobal(int minpercent) {
        HashMap<String, HashMap> colsMapGlobal = elements.get("GLOBAL");
        for (String current_key : elements.keySet()) {
            if (!current_key.equals("GLOBAL")) {
                HashMap<String, HashMap> colsMap = elements.get(current_key);
                Integer elementTotal = (Integer) (colsMap.get("total")).get("total");
                System.out.println(current_key + "(" + elementTotal + ")");
                for (String cols_key : colsMap.keySet()) {
                    if (!cols_key.equalsIgnoreCase("total")) {
                        System.out.println("\t" + cols_key);
                        HashMap<String, Integer> colMap = colsMap.get(cols_key);
                        HashMap<String, Integer> colMapGlobal = colsMapGlobal.get(cols_key);
                        TreeMap<String, Integer> sortedColMap = new TreeMap(new DescStringIntMapComparator(colMap));
                        sortedColMap.putAll(colMap);
                        for (String col_key : sortedColMap.keySet()) {
                            Integer colValue = colMap.get(col_key);
                            Integer globalValue = colMapGlobal.get(col_key);
                            //double colPercent=((double) colValue/(double) elementTotal)*100.0;
                            int colPercent = (int) Math.round(((double) colValue / (double) elementTotal) * 100.0);
                            if (colPercent >= minpercent) {
                                double globPercent = ((int) Math.round(((double) colValue / (double) globalValue) * 1000.0))/10.0;
                                System.out.print("\t\t" + col_key + "\t" + colPercent + "%" + " (" + colValue + ")");
                                if(globPercent>0){
                                    System.out.print("-> "+globPercent+"% of corpus \""+col_key+"\" (" + globalValue + ")");
                                }
                                System.out.println();
                            }
                        }
                        System.out.println();
                    }
                }
                System.out.println();
            }
        }
        System.out.println();

    }

    public void createGLOBALelement() {
        // Read all elements and put them toghether in global
        HashMap<String, HashMap> colsMapGlobal = new HashMap<String, HashMap>();

        for (String current_key : elements.keySet()) {
            HashMap<String, HashMap> colsMap = elements.get(current_key);
            if (colsMapGlobal.isEmpty()) {
                // create structure in first iteration
                for (String cols_key : colsMap.keySet()) {
                    //if(!cols_key.equalsIgnoreCase("total")){
                    HashMap<String, Integer> colMap = colsMap.get(cols_key);
                    HashMap<String, Integer> colMapGlobal = new HashMap<String, Integer>();
                    for (String col_key : colMap.keySet()) {
                        colMapGlobal.put(col_key, 0);
                    }
                    colsMapGlobal.put(cols_key, colMapGlobal);
                    //}
                }
                //colsMapGlobal.put((HashMap<String,HashMap>)(elements.get(current_key)).clone());
                //colsMapGlobal.remove("total");
            }

            for (String cols_key : colsMap.keySet()) {
                //if(!cols_key.equalsIgnoreCase("total")){
                HashMap<String, Integer> colMap = colsMap.get(cols_key);
                HashMap<String, Integer> colMapGlobal = colsMapGlobal.get(cols_key);
                // Just put-all values to 0 in Global
                for (String col_key : colMap.keySet()) {
                    colMapGlobal.put(col_key, 0);
                }
                colsMapGlobal.put(cols_key, colMapGlobal);
                //}
            }
        }
        elements.put("GLOBAL", colsMapGlobal);

    }

    public int getMAXnumtok() {
        return MAXnumtok;
    }



}
