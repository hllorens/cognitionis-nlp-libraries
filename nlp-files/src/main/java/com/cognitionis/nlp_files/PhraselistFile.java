package com.cognitionis.nlp_files;

import java.io.*;
import java.net.JarURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * PhraselistFile consists instances like phrase(one or more
 * words)[|[canonical_form]] Example: one|1 or address book|contact_list or
 * Monday|TWeekday or Lunes|Monday
 *
 * IMPORTANT: Order matters! LONGER PHRASES MUST APPEAR FIRST. The regex for the
 * phrases is built in order FIFO, if your phraselist contains a shorter phrase
 * that contain a longer phrase which appears afterwards the later would never
 * be matched.
 *
 * NOTE: If there is canonical form phrases can also contain | like (a|b)|c ->
 * only the last | is considered This saves lines but perhaps to do the inverse
 * mapping it would be better to have one phrase per line... There can be
 * functions to condense or expand this (as grammars)
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class PhraselistFile extends NLPFile {

    private String name;
    private Boolean has_canonical;
    private Boolean case_sensitive;
    private Boolean require_canonical;
    private Boolean allow_regex;
    private Boolean unify_multitokens;
    private HashMap<String, String> map; // if some other type is needed you can transform it at run-time (dynamic casting is complicated and makes things complicate)
    private HashMap<String, String> multitoken_map; // if some other type is needed you can transform it at run-time (dynamic casting is complicated and makes things complicate)
    private HashSet<String> keyset; // added for efficiency ONLY. Equivalent to map.keySet();
    private String multitoken_re; // regular expression
    private String re; // regular expression
    private Locale lang;

    /**
     * Creates a new phraselist from a file. By default: not case-sensitive,
     * en-US locale, canonical forms are not required, regex are not allowed,
     * and unify multi-tokens false
     *
     * @param filename
     */
    public PhraselistFile(String filename) {
        this(filename, Boolean.FALSE, new Locale("en", "us"), false, false, false);
    }

    /**
     * Creates a new phraselist from a file, indicating if it has to be case
     * sensitive, and the locale By default: canonical forms are not required,
     * and regex are not allowed
     *
     * @param filename
     * @param casesensitive
     * @param locale
     */
    public PhraselistFile(String filename, Boolean casesensitive, Locale locale) {
        this(filename, casesensitive, locale, false, false, false);
    }

    /**
     * Creates a new phraselist from a file, indicating if it has to be case
     * sensitive, the locale, if canonical forms are required and if regexes are
     * allowed
     *
     * @param filename
     * @param casesensitive
     * @param locale
     * @param require_canonical
     * @param allow_regex
     */
    public PhraselistFile(String filename, Boolean casesensitive, Locale locale, Boolean req_canonical, Boolean allow_re, Boolean uni_multitokens) {
        super(filename);
        case_sensitive = casesensitive;
        require_canonical = req_canonical;
        unify_multitokens = uni_multitokens;
        allow_regex = allow_re;
        lang = locale;
        name = "c_" + this.f.getName().substring(0, this.f.getName().lastIndexOf(".")).toLowerCase();
        has_canonical = null;
        re = "_no_regex_to_match_";
        multitoken_re = "_no_regex_to_match_";
        map = new HashMap();
        multitoken_map = new HashMap();
        keyset = null;
        isWellFormatted(); // good format is mandatory, this loads map<String,String> and re by default
    }

    @Override
    public Boolean isWellFormatted() {
        try {
            if (super.getFile() == null || url==null) {
                throw new Exception("No file loaded in NLPFile object");
            }
            if (encoding == null || (!encoding.equalsIgnoreCase("UTF-8") && !encoding.equalsIgnoreCase("ASCII"))) {
                throw new Exception("\n\tError: Only ASCII/UTF-8 text is allowed. " + this.f.getName() + " is " + encoding + "\n");
            }
            if (url.getProtocol().equals("file")) {
                this.inputstream=new FileInputStream(f);
            }
            if (url.getProtocol().equals("jar")) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                inputstream = connection.getInputStream();
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"))) {
                Boolean checked = false;
                String line;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    //line = line.trim(); spaces are important
                    linen++;
                    if (line.length() != 0) {
                        String token = line;
                        if (!checked) {
                            //if (line.matches("^[^\\|]+\\|[^\\|]*$")) { ambiguous can contain options
                            if (line.matches("^.+\\|[^\\|]*$")) {
                                has_canonical = true;
                                token = line.substring(0, line.lastIndexOf("|"));
                                if (!token.contains(" ") || unify_multitokens) {
                                    re = "(" + token;
                                } else {
                                    multitoken_re = "(" + token;
                                }
                            } else {
                                has_canonical = false;
                                if (require_canonical) {
                                    throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Required canonical form not found.");
                                }
                                if (!line.contains(" ") || unify_multitokens) {
                                    re = "(" + token;
                                } else {
                                    multitoken_re = "(" + token;
                                }
                            }
                            checked = true;
                        } else {
                            if (has_canonical && !line.contains("|")) {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Expected | since other lines had canonical forms");
                            }

                            if (!has_canonical && line.contains("\\|")) {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Canonical (|) not expected since other lines had no canonical forms");
                            }
                            if (has_canonical) {
                                token = line.substring(0, line.lastIndexOf("|"));
                            }

                            if (!token.contains(" ") || unify_multitokens) {
                                if (re.equals("_no_regex_to_match_")) {
                                    re = "(" + token;
                                } else {
                                    re += "|" + token;
                                }
                            } else {
                                if (multitoken_re.equals("_no_regex_to_match_")) {
                                    multitoken_re = "(" + token;
                                } else {
                                    multitoken_re += "|" + token;
                                }
                            }
                        }

                        if (has_canonical) {
                            String value = line.substring(line.lastIndexOf("|") + 1);
                            if (value.length() == 0) {
                                value = token; // key|  (value omitted case)
                            }
                            add_to_map(token, value, linen);
                        } else {
                            add_to_map(token, token, linen);
                        }
                    }
                }
                if (checked) {
                    if (!re.equals("_no_regex_to_match_")) {
                        re += ")";
                    }
                    if (!multitoken_re.equals("_no_regex_to_match_")) {
                        multitoken_re += ")";
                    }
                    if (!case_sensitive) {
                        re = re.toLowerCase(lang);
                        multitoken_re = multitoken_re.toLowerCase(lang);
                    }
                    if (!allow_regex) {
                        re = re.replaceAll("\\.", "\\\\\\\\."); // escape points 
                        multitoken_re = multitoken_re.replaceAll("\\.", "\\\\\\\\."); // escape points 
                    }                    //re=re.replaceAll("\\.", "\\\\."); // this would be a solution to allow dots
                    keyset = new HashSet<>(map.keySet());
                    keyset.addAll(multitoken_map.keySet());
                    // Check for multi-word ambiguity (partial match): can be done lively since longest first can be allowed
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            } else {
                System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            }
            this.isWellFormatted = false;
            return false;
        }
        this.isWellFormatted = true;
        return true;
    }

    public void add_to_map(String key, String value, int linen) throws Exception {

        if (!case_sensitive) {
            key = key.toLowerCase(lang);
        }
        if (!case_sensitive) {
            value = value.toLowerCase(lang);
        }

        if (!allow_regex) {
            if (key.matches(".*[*+?()\\[\\]].*")) {
                throw new Exception(this.f.getName() + ".Regex not allowed. Symbols * + \\ () [] are not supported.");
            }
        } else {
            Pattern p = Pattern.compile(key); // this will check if the ( and [ can be parsed (closed, ...)
        }



        if (map.containsKey(key) || multitoken_map.containsKey(key)) {
            throw new Exception(this.f.getName() + ". Line " + linen + " (" + key + "): Repeated phrase. Phraselists must not contain repetitions.");
        }
        // check sub-character matching (second/seconds)
        for (String oldkey : map.keySet()) {
            if (key.contains(oldkey)) {
                throw new Exception(this.f.getName() + ". Line " + linen + " (" + key + "): Repeated sub-character (" + oldkey + "). Longer phrases must appear first (" + key + ").");
            }
        }
        for (String oldkey : multitoken_map.keySet()) {
            if (key.contains(oldkey)) {
                throw new Exception(this.f.getName() + ". Line " + linen + " (" + key + "): Repeated sub-character (" + oldkey + "). Longer phrases must appear first (" + key + ").");
            }
        }

        // check sub-phrase matching (longer phrases should appear first)
        String[] multitoken = key.trim().split(" "); // trim to avoid matching empty
        if (multitoken.length > 1) {
            //System.err.println("--------------------testing:" + this.f.getName() + " -- " + key);
            for (int i = 0; i < multitoken.length; i++) {
                String token = multitoken[i];
                //System.err.println("----- " + token + " i=" + i + " ngram=1");
                if (!token.equals("^") && !token.equals("$")) {
                    //System.out.println(this.f.getName() +" trying " + token);
                    if (map.containsKey(token) || multitoken_map.containsKey(token)) {
                        throw new Exception(this.f.getName() + ". Line " + linen + " (" + key + "): Repeated sub-phrase (" + token + "). Longer phrases must appear first.");
                    }
                }
                for (int j = 1; j < multitoken.length - i; j++) {
                    token += " " + multitoken[i + j];
                    //System.err.println("----- " + token + " i=" + i + " ngram=" + (j+1));
                    if (map.containsKey(token) || multitoken_map.containsKey(token)) {
                        throw new Exception(this.f.getName() + ". Line " + linen + " (" + key + "): Repeated sub-phrase (" + token + "). Longer phrases must appear first.");
                    }
                }
            }
            if (unify_multitokens) {
                map.put(key.trim(), value.trim());
            } else {
                multitoken_map.put(key.trim(), value.trim());
            }
        } else {
            map.put(key.trim(), value.trim());
        }
    }

    public static TreeMap<String, String[]> mergeMaps(TreeMap<String, String[]> base, HashMap<String, String> newmap, String c_name) {
        if (base == null) {
            base = new TreeMap<>(new LengthAlphabeticalComparator());
        }
        for (Entry<String, String> e : newmap.entrySet()) {
            base.put(e.getKey(), new String[]{e.getValue(), "c_" + c_name});
        }
        return base;
    }

    public static String get_re_from_keyset(Set<String> keyset) {
        String k_re = "_no_regex_to_match_";
        if (keyset != null && keyset.size() != 0) {
            k_re = "(";
            for (String key : keyset) {
                if (k_re.equals("(")) {
                    k_re += key;
                } else {
                    k_re += "|" + key;
                }
            }
            k_re += ")";
        }
        return k_re;
    }

    @Override
    public String toPlain(String filename) {
        throw new UnsupportedOperationException("toPlain not applicable to this type of file");
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public HashSet<String> keySet() {
        return keyset;
    }

    public String getMapValue(String key) {
        return map.get(key);
    }

    public String getRE() {
        return re;
    }

    public HashMap<String, String> getMultiMap() {
        return multitoken_map;
    }

    public String getMultiMapValue(String key) {
        return multitoken_map.get(key);
    }

    public String getMultiRE() {
        return multitoken_re;
    }

    public String getName() {
        return name;
    }

    public HashSet<String> intersectPhraselist(HashSet s) {
        /*MANUAL METHOD: HashSet<String> contained=new HashSet<>();for(String k: map.keySet()){ if(s.contains(k)){    contained.add(k);            }        }*/
        HashSet<String> intersection = new HashSet<>(keyset); // create a set to do intersecion
        intersection.retainAll(s); // java standard for set intersection
        return intersection;
    }
}
