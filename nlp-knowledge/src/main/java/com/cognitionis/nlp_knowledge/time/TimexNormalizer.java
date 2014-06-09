package com.cognitionis.nlp_knowledge.time;


import com.cognitionis.nlp_files.TransduceRulelistFile;
import com.cognitionis.utils_basickit.FileUtils;
import java.io.File;
import java.util.*;
import java.util.regex.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Timex Normalization: Given an input string containing a temporal expression
 * normalize() method obtains its normalized version (canonical).
 * 
 * There is two main options to do this using external
 * knowledge files (entities) plus rules (grammars) 1) Do it as a grammar
 * parsing in one step: 2) Do it as pre-processing+rule-matching in 2 steps:
 * This is the strategy chosen in this class.
 *
 * @author Hector_Llorens
 */
public class TimexNormalizer {

    private Locale locale;
    private Timek timek;
    private TransduceRulelistFile rules;

    public TimexNormalizer() {
        this(Locale.getDefault());
    }

    public TimexNormalizer(Locale l) {
        locale = l;
        timek = new Timek(l);
        String lang = locale.toString().replace('_', '-');
        String extra = "";
        if (File.separator.equals("\\")) {
            extra = "\\";
        }
        String app_path = FileUtils.getApplicationPath(TimexNormalizer.class).replaceAll(extra + File.separator + "classes", "");
        String res_path = app_path + File.separator + "resources" + File.separator + "time" + File.separator + lang + File.separator;
        rules = new TransduceRulelistFile(res_path + "rules.phraselist");
    }

    public String normalize(String expr) {
        // search by pattern first
        // then if more than one is found
        //      filter by other features
        // period... recursive pattern? NO
        // what happens with modifiers (almost, aproximately, ...)
        // default normalization when no pattern is found
        // in patterns, is it important to distinguish card from ordinal_units?

        String norm_value = "default_norm";
        try {
            if (expr == null) {
                throw new Exception("Input expression is null.");
            }
            expr = expr.trim();
            if (expr.length() == 0) {
                throw new Exception("Input expression is empty.");
            }

            String normTextandPattern = timek.getNormTextandPattern(expr);
            if (normTextandPattern == null) {
                throw new Exception("Problem obtaining NormText and Pattern from: " + expr);
            }
            String[] normTextandPattern_arr = normTextandPattern.split("\\|");
            String timex_text = normTextandPattern_arr[0];
            String timex_pattern = normTextandPattern_arr[1];

            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("\n\ntimex:" + expr + "  normtext:" + timex_text + "  pattern:" + timex_pattern + "\nfound rules:");
            }

            if (timex_pattern.matches("(?i)" + rules.getRE())) {
                String rule = rules.getMapValue(timex_pattern); //return timex_text.replaceAll("([^ ]+( |$))+", rule);                
                return applyRule(timex_text, timex_pattern, rule);
            } else {
                // This can be more sophiscticated with pattern, find...
                String left_cycle_pat = timex_pattern;
                String right_cycle_pat = timex_pattern;
                String left_cycle_text = timex_text;
                String right_cycle_text = timex_text;
                while (right_cycle_pat.split(" ").length > 2) {
                    left_cycle_pat = left_cycle_pat.substring(left_cycle_pat.indexOf(' ') + 1);
                    right_cycle_pat = right_cycle_pat.substring(0, right_cycle_pat.lastIndexOf(' '));
                    String med_cycle_pat = left_cycle_pat.substring(0, left_cycle_pat.lastIndexOf(' '));
                    left_cycle_text = left_cycle_text.substring(left_cycle_text.indexOf(' ') + 1);
                    right_cycle_text = right_cycle_text.substring(0, right_cycle_text.lastIndexOf(' '));
                    String med_cycle_text = left_cycle_text.substring(0, left_cycle_text.lastIndexOf(' '));
                    if (left_cycle_pat.matches("(?i)" + rules.getRE())) {
                        String rule = rules.getMapValue(left_cycle_pat); //return timex_text.replaceAll("([^ ]+( |$))+", rule);                
                        return applyRule(left_cycle_text, left_cycle_pat, rule);
                    } else {
                        if (right_cycle_pat.matches("(?i)" + rules.getRE())) {
                            String rule = rules.getMapValue(right_cycle_pat); //return timex_text.replaceAll("([^ ]+( |$))+", rule);                
                            return applyRule(right_cycle_text, right_cycle_pat, rule);
                        } else {
                            if (med_cycle_pat.matches("(?i)" + rules.getRE())) {
                                String rule = rules.getMapValue(med_cycle_pat); //return timex_text.replaceAll("([^ ]+( |$))+", rule);                
                                return applyRule(med_cycle_text, med_cycle_pat, rule);
                            }
                        }
                    }
                }
            }

            norm_value = "NO RULE FOUND FOR: " + normTextandPattern;
            /*

             TIMEX_Instance timex_object = new TIMEX_Instance(normText, tense, dct, reftime);
             ArrayList<Rule> rules_found;
             for (int level = 1; level <= 3; level++) {
             rules_found = get_rules_from_db("RULES_LEVEL" + level, pattern);
             norm_value = apply_rules(rules_found, pattern, timex_object);
             if (!norm_value.equals("default_norm")) {
             break;
             }
             }

             // reduce left-right
             while (pattern.split("_").length > 1 && norm_value.equals("default_norm")) {
             normText = normText.replaceFirst("[^_]+_", "");
             pattern = pattern.replaceFirst("[^_]+_", "");
             timex_object = new TIMEX_Instance(normText, tense, dct, reftime);
             for (int level = 1; level <= 3; level++) {
             rules_found = get_rules_from_db("RULES_LEVEL" + level, pattern);
             norm_value = apply_rules(rules_found, pattern, timex_object);
             if (!norm_value.equals("default_norm")) {
             break;
             }
             }
             }

             // reduce right-left?

             // other heuristics?
             */
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }




        return norm_value;
    }

    /**
     * Given a rule and a matched timex text/pattern output the normalized value
     *
     * @param timex_text
     * @param timex_pattern
     * @param rule
     * @return
     */
    public String applyRule(String timex_text, String timex_pattern, String rule) throws Exception {
        if (rule != null) {
            // Using tokens as elements for replacements $1...$n
            String[] timex_arr = timex_text.split(" ");
            if (rule.contains("if(")) {
                String condition = rule.substring(3, rule.indexOf(')'));
                Pattern p = Pattern.compile("\\$[0-9]+ ");
                Matcher m = p.matcher(condition);
                //System.out.println(rule);
                while (m.find()) {
                    String var = m.group().replace("$", "").trim();
                    String val = timex_arr[Integer.parseInt(var) - 1];
                    condition = condition.replaceAll("\\$" + var + " ", val + " ");
                }
                //System.out.println(condition);
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
                if (engine.eval(condition).toString().equals("true")) {
                    //System.out.println("true");
                    rule = rule.substring(rule.indexOf("){") + 2, rule.indexOf("}else{"));
                } else {
                    //System.out.println("false");
                    rule = rule.substring(rule.indexOf("}else{") + 6, rule.lastIndexOf("}"));
                }
                //System.out.println("debug: "+rule);
            }

            // Variable replacement based on Java Regex: $NUM only checked for possible backreferences
            // It is not possible to replace $12 as $1 + '2'. In this case we have $(num) to allow that 
            for (int i = timex_arr.length; i > 0; i--) {
                // Simplified replacement
                if (rule.contains("$" + i)) {
                    rule = rule.replace("$" + i, timex_arr[i - 1]); // .substring(3) not needed v__ removed before
                }
                // Normal replacement
                if (rule.contains("$(" + i + ")")) {
                    rule = rule.replace("$(" + i + ")", timex_arr[i - 1]);
                }
            }

            // Sub replacement: Only checks for existing back-references
            if (rule.matches(".*\\$\\([0-9]+,[_:./-]+,[0-9]+\\).*")) {
                Pattern p = Pattern.compile("(\\$\\([0-9]+,[_:./-]+,[0-9]+\\))");
                Matcher m = p.matcher(rule);
                while (m.find()) {
                    String var = m.group();
                    String[] expr = var.substring(2, var.length() - 1).split(",");   //.replaceAll("^\\$\\(", "").replaceAll("\\)$", "").trim();
                    if (Integer.parseInt(expr[0]) <= timex_arr.length) {
                        String val = timex_arr[Integer.parseInt(expr[0]) - 1];
                        String[] val_arr = val.split(expr[1]);
                        if (Integer.parseInt(expr[2]) <= val_arr.length) {
                            val = val_arr[Integer.parseInt(expr[2]) - 1];
                            rule = rule.replace(var, val);
                        }
                    }
                }
            }




            // ALTERNATIVE: Using java standard regex group matching for replacements. timex_text needs to be mapped
            // System.out.println(rule+"     "+timex_text); 
            // PROBLEM, CURRENTLY This is implemented as pattern lookup in a hashmap which is efficient, for the other solution
            // we need to do a rule search loop with pattern matching, although maybe the pattern/rule understanding by users is better
            // In short, doing things at loading stage is normally more efficient than doing it at runtime... the problem is running the program for a single timex
            // but if you run it for a set of timexes it will be more efficient if you preload patterns in a hashmap, also you can check for duplicates

            return rule;
        } else {
            return "Strange error... check rules... RULE matched but not foudn... FOR " + timex_text + "|" + timex_pattern;
        }
    }
}
