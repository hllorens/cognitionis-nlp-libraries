package com.cognitionis.nlp_files;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

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
public class RegexPhraselistFile extends NLPFile {
    
    private String name;
    private Boolean has_canonical;
    private Boolean case_sensitive;
    private HashMap<String, String> map; // if some other type is needed you can transform it at run-time (dynamic casting is complicated and makes things complicate)
    private HashSet<String> keyset; // added for efficiency ONLY. Equivalent to map.keySet();
    private String re; // regular expression
    private Locale lang;

    public RegexPhraselistFile(String filename) {
        this(filename,Boolean.FALSE,new Locale("en", "us"));
    }

    public RegexPhraselistFile(String filename, Boolean casesensitive, Locale locale) {
        super(filename);
        case_sensitive=casesensitive;
        lang=locale;
        name="c_"+this.f.getName().substring(0, this.f.getName().lastIndexOf(".")).toLowerCase();
        has_canonical = null;
        re = "_no_regex_to_match_";
        map = new HashMap();
        keyset = null;
        isWellFormatted(); // good format is mandatory, this loads map<String,String> and re by default
    }
    
    
    @Override
    public Boolean isWellFormatted() {
        try {
            if (super.getFile() == null) {
                throw new Exception("No file loaded in NLPFile object");
            }
            if (!encoding.equalsIgnoreCase("UTF-8") && !encoding.equalsIgnoreCase("ASCII")) {
                throw new Exception("\n\tError: Only ASCII/UTF-8 text is allowed. " + this.f.getName() + " is " + encoding + "\n");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.f), "UTF-8"))) {
                Boolean checked = false;
                String line;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    //line = line.trim(); spaces are important
                    linen++;
                    if (line.length() != 0) {
                        if (!checked) {
                            //if (line.matches("^[^\\|]+\\|[^\\|]*$")) { ambiguous can contain options
                            if (line.matches("^.+\\|[^\\|]*$")) {
                                has_canonical = true;
                                re = "(" + line.substring(0, line.lastIndexOf("|"));                                    
                            } else {
                                has_canonical = false;
                                re = "(" + line;
                            }
                            checked = true;
                        } else if (has_canonical) {
                            if (!line.contains("|")) {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Expected | since other lines had canonical forms");
                            } else {
                                re += "|" + line.substring(0, line.lastIndexOf("|"));
                            }
                        } else {
                            re += "|" + line;
                        }
                        if (!has_canonical && line.contains("\\|")) {
                            throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Canonical (|) not expected since other lines had no canonical forms");
                        }
                    }
                    if (line.length() != 0) {
                        if (has_canonical) {
                            String key = line.substring(0, line.lastIndexOf("|"));
                            if(!case_sensitive)
                                key=key.toLowerCase(lang);
                            String value = line.substring(line.lastIndexOf("|") + 1);
                            if (map.containsKey(key)) {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Repeated phrase. Phraselists must not contain repetitions.");
                            }
                            // check sub-character matching (second/seconds)
                            for(String oldkey: map.keySet()){
                            if(key.contains(oldkey)){
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Repeated sub-character (" + oldkey + "). Longer phrases must appear first ("+key+").");                                
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
                                        if (map.containsKey(token)) {
                                            throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Repeated sub-phrase (" + token + "). Longer phrases must appear first.");
                                        }
                                    }
                                    for (int j = 1; j < multitoken.length - i; j++) {
                                        token += " " + multitoken[i + j];
                                        //System.err.println("----- " + token + " i=" + i + " ngram=" + (j+1));
                                        if (map.containsKey(token)) {
                                            throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Repeated sub-phrase (" + token + "). Longer phrases must appear first.");
                                        }
                                    }
                                }
                            }
                            if (value.length() != 0) {
                                map.put(key.trim(), value.trim());
                            } else {
                                map.put(key.trim(), key.trim());
                            }
                        } else {
                            if (map.containsKey(line)) {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Repeated phrase!! Phraselists must not contain repetitions.");
                            }
                            map.put(line, line);
                        }
                    }
                }
                if (checked) {
                    re += ")";
                    if(!case_sensitive)
                        re=re.toLowerCase(lang);
                    //re=re.replaceAll("\\.", "\\\\."); // this would be a solution to allow dots
                    keyset = new HashSet<>(map.keySet());
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
