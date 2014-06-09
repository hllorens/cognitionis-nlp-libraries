package com.cognitionis.nlp_lang_models;


import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 *
 * Language Model: FingerPrint
 *
 * A language fingerprint is a ranked list of the x most common n-grams (where n normally include from 1 to (3 to 5) grams)
 * It is a very simplistic language model, but has reazonably good results in lang detection task
 * The tool is called TextCategorizer because it can categorize not only langs but topics, classes,...
 *
 */
public class TextCategorizerFingerprint {

    /*private class NGramEntryComparator implements Comparator<Entry<String,Integer>> {
    // Gives priority to hig scored keys and if equal to longer keys
    //     Because a larger n-gram gives more information
    public int compare(Entry<String,Integer> entry1, Entry<String,Integer> entry2) {
    int value_diff=entry2.getValue()-entry1.getValue();
    if(value_diff == 0) {
    int keylength_diff=entry1.getKey().length()-entry2.getKey().length();
    if(keylength_diff == 0) {
    return entry1.getKey().compareTo(entry2.getKey());
    }
    return keylength_diff;
    }
    return value_diff;
    }
    }
     */

    private class NGramMapComparator implements Comparator {
        private Map _data=null;
   		public NGramMapComparator (Map data){
			super();
			_data = data;
		}

                        // Gives priority to hig scored keys and if equal to longer keys
                        //     Because a larger n-gram gives more information
                        public int compare(Object o1, Object o2) {

                            String k1 = (String) o1;
                            String k2 = (String) o2;

                            Integer v1 = (Integer) this._data.get(k1);
                            Integer v2 = (Integer) this._data.get(k2);

                            int diff = v2 - v1;
                            if (diff == 0) {
                                diff = k2.length() - k1.length();
                                if (diff == 0) {
                                    diff=k1.compareTo(k2);
                                }
                            }
                            return diff;
                        }
                    
    }




    private String category = "unknown";
    /**
     * Set of NGrams sorted by the number of occurences in the text which was
     * used for creating the FingerPrint. We used a Set because the comparator takes
     * into account not only the value but the key length then we use a set of entries composed
     * by a String (key) and a Integer (Value)
     * If the key length is not important then use a simple Map and do a generic sort... (generic comparator)
     *
     */
    //private TreeSet<Entry<String, Integer>> sorted_entries;
    private HashMap<String, Integer> sorted_entries= new HashMap<>();
    private HashMap<String, Integer> categoryDistances = new HashMap<>();
    private Pattern pattern = Pattern.compile("^_?[^0-9\\?!\\-_/]*_?$");

    /**
     * creates an empty FingerPrint
     */
    public TextCategorizerFingerprint() {
    }

    /**
     * creates a FingerPrint by reading the FingerPrint Language model-file referenced by the
     * passed path.
     *
     * @param file_path
     *            path to the FingerPrint-file
     * @param cat
     *            category of the FingerPrint-file
     *
     */
    public TextCategorizerFingerprint(String file_path, String cat) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(file_path)));

            this.sorted_entries.clear();

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] line_arr = line.split("\\s+");
                    if (line_arr.length > 0) {
                        if (line_arr.length != 2) {
                            throw new Exception("Malformed TextCategorizer configuration file.\n\tMust contain one fingerprint file path per line.");
                        }
                        this.sorted_entries.put(line_arr[0], new Integer(line_arr[1]));
                    }
                }
                } finally {
                    reader.close();
                this.category=cat;
            }

        } catch (Exception e) {
            System.err.println("Errors found ("+this.getClass().getSimpleName()+"):\n\t" + e.toString() + "\n");
            if(System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")){e.printStackTrace(System.err);}
        }

    }

    /**
     * gets the position of the NGram passed to method in the FingerPrint. the
     * NGrams are in descending order according to the number of occurences in
     * the text which was used creating the FingerPrint.
     *
     * @param key
     *            the NGram
     * @return the position of the NGram in the FingerPrint
     */
/*    public int getPosition(String key) {
        /*int pos = 1;

        int value = this.sorted_entries.first().getValue();
        for (Entry<String, Integer> entry : this.sorted_entries) {
        if (value != entry.getValue()) {
        value = entry.getValue();
        pos++;
        }
        if (entry.getKey().equals(key)) {
        return pos;
        }
        }
        return -1;
        return this.sorted_entries.get(key);

    }*/

    /**
     * saves the fingerprint to a file named <categoryname>.lm in the execution
     * path.
     */
    public void save() {
        File file = new File(this.getCategory() + ".lm");
        try {
            if (file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(this.toString().getBytes());
                fos.close();
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace(System.err);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * returns the category of the FingerPrint or "unknown" if the FingerPrint
     * wasn't categorized yet.
     *
     * @return the category of the FingerPrint
     */
    public String getCategory() {
        return this.category;
    }

    public HashMap getSorted_entries() {
        return this.sorted_entries;
    }


    /**
     * returns the FingerPrint as a String in the FingerPrint file-format
     */
/*    public String toString() {
        String s = "";
        for (Entry<String, Integer> entry : sorted_entries) {
            s += entry.getKey() + "\t" + entry.getValue() + "\n";
        }
        return s;
    }*/   //SWITCH TO MAP FORMAT

    /**
     * creates a FingerPrint by analysing the content of the given file.
     *
     * @param file file to be analysed
     */
    public void create(File file) {
        // TODO TODO HANDLE FILE ENCODING...
        // TODO TODO NO PASSAR A STRING I TREBALLAR SINO ANAR LLEGINT LINEA A LINEA PER A NO
        // CARREGAR LA MEMORIA
        char[] data = new char[1024];
        String s = "";
        int read;
        try {
            FileReader fr = new FileReader(file);
            while ((read = fr.read(data)) != -1) {
                s += new String(data, 0, read);
            }
            fr.close();
            this.create(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void create(String text) {
        HashMap<String, Integer> entries = new HashMap<String, Integer>();
        entries=this.computeNGrams(1, 5, text);
        if (entries.containsKey("_")) {
            int blanksScore = entries.remove("_");
            entries.put("_", blanksScore / 2);
        }

        ArrayList sorted_keys = new ArrayList(entries.keySet());
        Collections.sort(sorted_keys, new NGramMapComparator(entries));

        this.sorted_entries.clear();
        int n=sorted_keys.size();
        for(int i=0;i<n;i++){
            this.sorted_entries.put((String) sorted_keys.get(i), i+1);
            //System.err.println((String) sorted_keys.get(i)+"-"+(i+1)+"-"+entries.get(sorted_keys.get(i)));
        }

        //System.err.println("val a="+this.sorted_entries.get("a"));
        /*for (Entry e : this.sorted_entries.entrySet()) {
            System.err.println(e.getKey()+" "+e.getValue());
        }*/
    }

    /**
     * adds all NGrams with the passed order occuring in the given text to the
     * FingerPrint. For example:
     *
     * text = "text" ngramMinOrder = 2, ngramMaxOrder = 2
     *
     * so the NGrams added to the FingerPrint are:
     *
     * "_t", "te", "ex", "xt", "t_"
     *
     * all with a score (occurence) of 1
     *
     * @param ngramMinOrder
     * @param ngramMaxOrder
     * @param text
     */
    private HashMap computeNGrams(int ngramMinOrder, int ngramMaxOrder, String text) {
        // MOD TEXTCAT: period and comma are removed because they are very common and do not give informaiton in european languages
        text = text.replaceAll("[.,]", "");

        String[] tokens = text.split("\\s+"); // Words plus other language symbols (numbers, -, ...)


        HashMap<String, Integer> entries = new HashMap<String, Integer>();

        // From min to max ngram sizes
        for (int order = ngramMinOrder; order <= ngramMaxOrder; ++order) {
            // For each token
            for (String token : tokens) {
                // consider a space (_) before and after the token
                token = "_" + token + "_";

                // compute each token sub-ngram of current order
                for (int i = 0; i < (token.length() - order + 1); i++) {
                    String ngram = token.substring(i, i + order);

                    Matcher matcher = pattern.matcher(ngram);
                    // toma castanya el pattern del copon
                    // private Pattern pattern = Pattern.compile("^_?[^0-9\\?!\\-_/]*_?$");

                    if (!matcher.find()) {
                        continue;
                    } else if (!entries.containsKey(ngram)) {
                        entries.put(ngram, 1);
                    } else {
                        int score = entries.remove(ngram);
                        entries.put(ngram, ++score);
                    }
                }
            }
        }
        return entries;
    }

    /**
     * categorizes the FingerPrint by computing the distance to the FingerPrints
     * in the passed Collection. the category of the FingerPrint with the lowest
     * distance is assigned to this FingerPrint.
     *
     * @param categories
     * @return the distances
     */
    public Map<String, Integer> categorize(Collection<TextCategorizerFingerprint> categories) {
        int minDistance = Integer.MAX_VALUE;
        for (TextCategorizerFingerprint fp : categories) {
            int distance = this.getDistance(fp.getSorted_entries());
            this.getCategoryDistances().put(fp.getCategory(), distance);
            if (distance < minDistance) {
                minDistance = distance;
                this.category = fp.getCategory();
            }
        }
        return this.getCategoryDistances();
    }

    public Map<String, Integer> getCategoryDistances() {
        return this.categoryDistances;
    }

    /**
     * computes and returns the distance of this FingerPrint HashMap to the FingerPrint HashMap
     * passed to the method.
     *
     * @param category
     *            the FingerPrint HashMap to be compared to this one
     * @return the distance of the passed FingerPrint to this FingerPrint
     */
    private int getDistance(HashMap<String,Integer> category) {
        int distance = 0;
        int count = 0;
        for (Map.Entry<String,Integer> entry: this.sorted_entries.entrySet()){
            String ngram = entry.getKey();
            count++;
            if (count > 400) {
                break;
            }
            if (!category.containsKey(ngram)) {
                distance += category.size();
                continue;
            }
            distance += Math.abs(this.sorted_entries.get(ngram) - category.get(ngram));
        }
        return distance;
    }
}
