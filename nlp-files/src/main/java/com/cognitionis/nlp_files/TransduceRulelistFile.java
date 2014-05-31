package com.cognitionis.nlp_files;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TransduceRulelistFile consists instances like patterns(one or more
 * words)|Transduced value Example: c_Card c_Month
 * c_Card|day=$1,month=$2,year=$3
 *
 * As in Regex $n are used for replacements, they correspond to values in the
 * pattern
 *
 * There can be also conditions like c_Month
 * c_Card|IF($2<32){day=$2,month=$1}ELSE{month=$1,year=$2}
 *
 * IMPORTANT: Order matters! LONGER PHRASES MUST APPEAR FIRST.
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class TransduceRulelistFile extends NLPFile {
    
    private String name;
    private HashMap<String, String> map; // if some other type is needed you can transform it at run-time (dynamic casting is complicated and makes things complicate)
    private HashSet<String> keyset; // added for efficiency ONLY. Equivalent to map.keySet();
    private String re; // regular expression

    public TransduceRulelistFile(String filename) {
        super(filename);
        name = "c_" + this.f.getName().substring(0, this.f.getName().lastIndexOf(".")).toLowerCase();
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
                    line = line.trim();
                    linen++;
                    if (line.length() != 0) {
                        if (!checked) {
                            if (line.matches("^.+\\|[^\\|]*$")) {
                                re = "(" + line.substring(0, line.lastIndexOf("|"));
                            } else {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Expected | since TranduceRulelist requires it");
                            }
                            checked = true;
                        } else {
                            if (!line.contains("|")) {
                                throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Expected | since TranduceRulelist requires it");
                            } else {
                                re += "|" + line.substring(0, line.lastIndexOf("|"));
                            }
                        }
                    }
                    if (line.length() != 0) {
                        String key = line.substring(0, line.lastIndexOf("|"));
                        String value = line.substring(line.lastIndexOf("|") + 1);
                        if (key.matches(".*[\\(\\)\\[\\]].*")) {
                            /* 
                             * Strategy given nesting is not allowed is just extend one by one
                             */
                            Pattern p = Pattern.compile(key); // this will check if the ( and [ can be parsed (closed, ...)
                            if (key.contains("*") || key.contains("+") || key.contains("\\")) {
                                throw new Exception("Symbols * + \\ are not supported.");
                            }
                            if (key.matches(".*[\\(\\[][^\\)\\]]*[\\(\\[].*")) {
                                throw new Exception("Nesting in rule patterns is not supported."); // to support it we need to split only | outside other groups
                            }                            
                            ArrayList<String> rules = expand_rules(line); // line and not key because value also needs to be copied
                            for (String rule : rules) {
                                key = rule.substring(0, rule.lastIndexOf("|"));
                                value = rule.substring(rule.lastIndexOf("|") + 1);
                                //System.out.println(rule);
                                addToMap(key, value, rule, linen);
                            }

                        } else {
                            addToMap(key, value, line, linen);
                        }
                    }
                }
                if (checked) {
                    re += ")";
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
    
    public ArrayList<String> expand_rules(String rule) {
        ArrayList<String> expanded_rules = new ArrayList<>();
        //for(String rule : rules){
        String key = rule.substring(0, rule.lastIndexOf("|"));
        String value = rule.substring(rule.lastIndexOf("|") + 1);
        // ()
        if (key.contains("(")) {
            Pattern p = Pattern.compile("\\([^\\(]+\\)");
            Matcher m = p.matcher(key);
            if (m.find()) {
                String elem = m.group();
                String beforeelem = "";
                String afterelem = "";
                String newvalue;
                if (m.start() > 0) {
                    beforeelem = key.substring(0, m.start());
                }
                if (m.end() < key.length()) {
                    afterelem = key.substring(m.end());
                }
                int tokens_before_elem = beforeelem.trim().split(" ").length; // even if empty 0 makes no sense since $ always starts with 1
                int tokens = key.trim().split(" ").length;
                //System.out.print("Start index: " + m.start());
                //System.out.print(" End index: " + m.end() + " ");
                //System.out.println(elem);
                String[] options = elem.substring(1, elem.length() - 1).split("\\|");
                int longest_option_tokens = 1;
                for (String option : options) {
                    int option_tokens = option.trim().split(" ").length;
                    if (option_tokens > longest_option_tokens) {
                        longest_option_tokens = option_tokens;
                    }
                }
                for (String option : options) {
                    newvalue = value;
                    int option_tokens = option.trim().split(" ").length;
                    int diff = longest_option_tokens - option_tokens;
                    if (diff != 0 && value.contains("$")) {
                        for (int i = tokens_before_elem; i < tokens; i++) {
                            newvalue = newvalue.replaceAll("\\$" + i, "\\$" + (i - diff));
                        }
                    }
                    //System.out.println(beforeelem + option + afterelem + "|" + newvalue);
                    expanded_rules.addAll(expand_rules(beforeelem + option + afterelem + "|" + newvalue));                    
                }
            }
            //key=key.replaceAll("[\\(\\)]", "_caca_");                                
        } else {
            // []
            if (key.contains("[")) {
                Pattern p = Pattern.compile("\\[[^\\[]+\\]");
                Matcher m = p.matcher(key);
                if (m.find()) {
                    String elem = m.group();
                    String beforeelem = "";
                    String afterelem = "";
                    String newvalue;
                    if (m.start() > 0) {
                        beforeelem = key.substring(0, m.start());
                    }
                    if (m.end() < key.length()) {
                        afterelem = key.substring(m.end());
                    }
                    int tokens_before_elem = beforeelem.trim().split(" ").length; // even if empty 0 makes no sense since $ always starts with 1
                    int tokens = key.trim().split(" ").length;
                    //System.out.print("Start index: " + m.start());
                    //System.out.print(" End index: " + m.end() + " ");
                    //System.out.println(elem);
                    String[] options = elem.substring(1, elem.length() - 1).split("\\|");
                    int longest_option_tokens = 1;
                    for (String option : options) {
                        int option_tokens = option.trim().split(" ").length;
                        if (option_tokens > longest_option_tokens) {
                            longest_option_tokens = option_tokens;
                        }
                    }
                    for (String option : options) {
                        newvalue = value;
                        int option_tokens = option.trim().split(" ").length;
                        int diff = longest_option_tokens - option_tokens;
                        if (diff != 0 && value.contains("$")) {
                            for (int i = tokens_before_elem; i < tokens; i++) {
                                newvalue = newvalue.replaceAll("\\$" + i, "\\$" + (i - diff));
                            }
                        }
                        //System.out.println(beforeelem + option + afterelem + "|" + newvalue);
                        expanded_rules.addAll(expand_rules(beforeelem + option + afterelem + "|" + newvalue));                        
                    }
                    // add the empty option
                    newvalue = value;
                    int diff = longest_option_tokens;
                    if (diff != 0 && value.contains("$")) {
                        for (int i = tokens_before_elem; i < tokens; i++) {
                            newvalue = newvalue.replaceAll("\\$" + i, "\\$" + (i - diff));
                        }
                    }
                    //System.out.println(beforeelem + afterelem + "|" + newvalue);
                    expanded_rules.addAll(expand_rules(beforeelem.trim() + afterelem + "|" + newvalue));                                            
                }
            } else {
                expanded_rules.add(rule);
            }            
        }
        //}
      
        return expanded_rules;
    }
    
    public void addToMap(String key, String value, String line, int linen) throws Exception {
        if (map.containsKey(key)) {
            throw new Exception(this.f.getName() + ". Line " + linen + " (" + line + "): Repeated phrase. Phraselists must not contain repetitions.");
        }
        
        if (value.length() != 0) {
            map.put(key.trim(), value.trim());
        } else {
            map.put(key.trim(), key.trim());
        }
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
