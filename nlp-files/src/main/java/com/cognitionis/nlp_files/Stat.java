package com.cognitionis.nlp_files;

import java.util.*;
import com.cognitionis.utils_basickit.*;

/**
 * @author Hector Llorens
 * @since 2009
 */
public class Stat {

    private int elemscol;
    private int attribscol;
    private String[] coldesc_arr;
    private Integer coldesc_arr_count;
    private HashMap<String, HashMap> elements; // TIMEX3, EVENT | EVENT-OCURRENCE... (PRIMARY KEY)
    private Integer totalDataAdded;
    private Integer totalGlobalDataAdded;

    public Stat() {
        this(null, null, null);
    }

    public Stat(String[] coldesc) {
        this(coldesc, null, null);
    }

    public Stat(String[] coldesc, String elemscol_re) {
        this(coldesc, elemscol_re, null);
    }

    public Stat(String[] coldesc, String elemscol_re, String attribscol_re) {
        coldesc_arr = null;
        coldesc_arr_count = 0;
        totalDataAdded = 0;
        totalGlobalDataAdded = 0;
        elemscol = -1;
        attribscol = -1;

        try {
            if (coldesc == null || coldesc.length < 1) {
                throw new Exception("Column description is null or empty");
            }
            //coldesc_arr = coldesc;
            coldesc_arr = new String[coldesc.length + 1];
            for(int i=0;i<coldesc.length;i++){
                coldesc_arr[i]=coldesc[i];
            }
            coldesc_arr[coldesc.length]="span";
            coldesc_arr_count = coldesc.length + 1;

            if (elemscol_re != null) {
                elemscol = getColumn(elemscol_re);
            }

            if (attribscol_re != null) {
                attribscol = getColumn(attribscol_re);
            }

            if (elemscol == -1) {
                elemscol = coldesc.length - 1;
            }

            /*numtokcol = getColumn("numtok");*/


            // luego siempre comprovar si attribscol!=-1 y si lo es pues usar el combinado BASE-CLASS
            // crear estructura vacia para la estadistica teniendo en cuenta las columnas
            elements = new HashMap<String, HashMap>();


        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }

    public int getColsCount(){
        return coldesc_arr_count;
    }

    public int getElemsCol() {
        return elemscol;
    }

    public int getAttribsCol() {
        return attribscol;
    }

    public int getColumn(String colname_re) {
        for (int i = 0; i < coldesc_arr_count; i++) {
            if (coldesc_arr[i].matches(colname_re)) {
                return i;
            }
        }
        return -1;
    }

    public String getColumnStr(int colpos) {
        try {
            if (colpos >= 0 && coldesc_arr.length > colpos) {
                return coldesc_arr[colpos];
            } else {
                throw new Exception("Column position < 0 or > total columns");
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
    }

    public void addData(final String[] data) {
        totalDataAdded++;

        try {
            HashMap<String, HashMap> dataMap = new HashMap<String, HashMap>();
            if (data.length != coldesc_arr_count) {
                throw new Exception("Malformed data");
            }

            String datakey;
            if (attribscol != -1) {
                if(data[attribscol].matches(".*=\".*") || data[attribscol].matches(".*\\s+.*") || data[attribscol].equals("*")){ // multiple XML attribs
                    if(data[elemscol].equalsIgnoreCase("EVENT") && data[attribscol].matches(".*class=\".*")){
                        datakey=data[elemscol] + "-" + data[attribscol].substring(data[attribscol].indexOf("class=\"")+7, data[attribscol].indexOf('"', data[attribscol].indexOf("class=\"")+7));
                    }else{
                        if(data[elemscol].equalsIgnoreCase("TIMEX3") && data[attribscol].matches(".*type=\".*")){
                            datakey=data[elemscol] + "-" + data[attribscol].substring(data[attribscol].indexOf("type=\"")+6, data[attribscol].indexOf('"', data[attribscol].indexOf("type=\"")+6));
                        }else{
                          datakey = data[elemscol];
                        }
                    }
                }else{
                    datakey = data[elemscol] + "-" + data[attribscol];
                }
            } else {
                datakey = data[elemscol];
            }


            // PUT SPECIFIC ELEMENT
            if (elements.containsKey(datakey)) {
                HashMap<String, HashMap> colsMap = elements.get(datakey);
                for (int col = 0; col < coldesc_arr_count; col++) {
                    if (col != elemscol && col != attribscol) {
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
                    if (col != elemscol && col != attribscol) {
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


            // GENERAL STATS FOR NON-GENERAL CASES (e.g., EVENT-OCCURRENCE)
            if(!datakey.equals(data[elemscol])){
                HashMap<String, HashMap> dataMapG = new HashMap<String, HashMap>();
                if (elements.containsKey("0_GENERAL_"+data[elemscol])) {
                    HashMap<String, HashMap> colsMapG = elements.get("0_GENERAL_"+data[elemscol]);
                    for (int col = 0; col < coldesc_arr_count; col++) {
                        if (col != elemscol && col != attribscol) {
                            HashMap<String, Integer> colMapG = colsMapG.get(coldesc_arr[col]);
                            Integer colMapValue = colMapG.get(data[col]);
                            if (colMapValue != null) {
                                colMapValue++;
                            } else {
                                colMapValue = 1;
                            }
                            colMapG.put(data[col], colMapValue);
                            //colsMap.put(coldesc_arr[col], colMap); // java modifica obj por valor
                        }
                    }
                    final Integer totalValue = ((Integer) (colsMapG.get("total")).get("total")) + 1;
                    colsMapG.put("total", new HashMap<String, Integer>() {

                        {
                            put("total", totalValue);
                        }
                    });
                    //elements.put(datakey, colsMap); // java modifica obj por valor
                } else {
                    // fill the dataMap
                    for (int col = 0; col < coldesc_arr_count; col++) {
                        if (col != elemscol && col != attribscol) {
                            final String tempdata = data[col];
                            dataMapG.put(coldesc_arr[col], new HashMap<String, Integer>() {

                                {
                                    put(tempdata, 1);
                                }
                            });
                        }
                    }
                    dataMapG.put("total", new HashMap<String, Integer>() {

                        {
                            put("total", 1);
                        }
                    });

                    elements.put("0_GENERAL_"+data[elemscol], dataMapG);
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
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
            Integer colMapValue=1;
            totalGlobalDataAdded++;
            if (!elements.containsKey("GLOBAL")) {
                this.createGLOBALelement();
            }
            HashMap<String, HashMap> colsMapGlobal = elements.get("GLOBAL");
            if (colsMapGlobal.containsKey(statkey)) { // word, pos, etc.
                HashMap<String, Integer> colMapGlobal = colsMapGlobal.get(statkey);
                if (colMapGlobal.containsKey(statsubkey)) { // la, perro, ser, etc.
                    colMapValue = colMapGlobal.get(statsubkey);
                    if (colMapValue != null) {
                        colMapValue++;
                    } else {
                        colMapValue = 1;
                    }
                    colMapGlobal.put(statsubkey, colMapValue); // Necessary because Integer is a basic type and then not a reference
                }
                else{ // create first time (la, perro, etc.)
                    colMapGlobal.put(statsubkey, 1);
                }
            }
            final Integer totalValue = ((Integer) (colsMapGlobal.get("total")).get("total")) + 1;
            colsMapGlobal.put("total", new HashMap<String, Integer>() {
                {
                    put("total", totalValue);
                }
            });

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    public void print() {
        print(1,0);
    }

    public void print(int minpercent, int minpercentCorpus) {
        System.out.println("\nPrinting Statistics (total Data " + totalDataAdded + ")\n-------------------------");
        //System.out.println(elements);
        if (elements.get("GLOBAL") != null) {
            printGlobal(minpercent,minpercentCorpus);
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
                //if(!cols_key.equalsIgnoreCase(coldesc_arr[elemscol]) && !cols_key.equalsIgnoreCase(coldesc_arr[attribscol]) && !cols_key.equalsIgnoreCase("total")){
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

    public void printGlobal(int minpercent, int minpercentCorpus) {
        HashMap<String, HashMap> colsMapGlobal = elements.get("GLOBAL");
        String[] keylist=elements.keySet().toArray(new String[0]);
        Arrays.sort(keylist);
        for (String current_key : keylist) { // elements.keySet()
            if (!current_key.equals("GLOBAL")) {
                HashMap<String, HashMap> colsMap = elements.get(current_key);
                Integer elementTotal = (Integer) (colsMap.get("total")).get("total");
                System.out.println(current_key + "(" + elementTotal + ")");
                for (String cols_key : colsMap.keySet()) {
                    if (!cols_key.equals("total") && !cols_key.equals("attribs")) {
                        System.out.println("\t" + cols_key);
                        HashMap<String, Integer> colMap = colsMap.get(cols_key);
                        HashMap<String, Integer> colMapGlobal = colsMapGlobal.get(cols_key);
                        TreeMap<String, Integer> sortedColMap = new TreeMap(new DescStringIntMapComparator(colMap));
                        sortedColMap.putAll(colMap);
                        for (String col_key : sortedColMap.keySet()) {
                            Integer colValue = colMap.get(col_key);
                            Integer globalValue = colMapGlobal.get(col_key);
                            if(globalValue==null){
                                globalValue=colValue*1000; // non significant value
                            }
                            //double colPercent=((double) colValue/(double) elementTotal)*100.0;
                            int colPercent = (int) Math.round(((double) colValue / (double) elementTotal) * 100.0);
                            if (colPercent >= minpercent) {
                                double globPercent = ((int) Math.round(((double) colValue / (double) globalValue) * 1000.0)) / 10.0;
                                System.out.print("\t\t" + col_key + "\t" + colPercent + "%" + " (" + colValue + ")");
                                if (globPercent >= minpercentCorpus && globPercent > 0.1 && !cols_key.equals("span")) {
                                    //System.out.print("  ----> " + globPercent + "% of corpus \"" + col_key + "\" (" + globalValue + ")");
                                    System.out.print("  ----> " + globPercent + "% of corpus (" + globalValue + ")");
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
        HashMap<String, HashMap> colsMapGlobal = new HashMap<String, HashMap>();
        for(int currcol=0;currcol < coldesc_arr_count;currcol++){
            if(currcol!=elemscol && currcol!=attribscol){
                colsMapGlobal.put(coldesc_arr[currcol], new HashMap<String, Integer>());
            }
        }
        colsMapGlobal.put("total", new HashMap<String, Integer>() {
            {
                put("total", 1);
            }
        });
        elements.put("GLOBAL", colsMapGlobal);
    }


}
