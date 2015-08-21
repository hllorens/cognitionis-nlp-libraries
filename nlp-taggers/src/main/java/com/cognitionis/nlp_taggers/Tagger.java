package com.cognitionis.nlp_taggers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.simple.JSONObject;

public abstract class Tagger {
    protected int ngram_size;
    protected String token_classes_config;
    protected JSONObject token_classes;
    // to pre-process input and do _RARE_ replacement before counting ngrams
    protected HashMap<String, Integer> word_counts;

    // DEFINITELY NOT USE NON OBJECTS String [], use just a String (much
    // simpler) or ArrayList (less convenient)
    protected HashMap<String, Integer> word_tag_emission_counts;

    // better store it as string, decide after I implement test...
    protected ArrayList<HashMap<String, Integer>> tag_ngram_counts;

    protected HashSet<String> tagset; // all tags
    protected int token_rare_classes_threshold; // 0 by default
    protected JSONObject rare_classes;
    protected JSONObject always_classes;

    public HashSet<String> tagSet() {
        return tagset;
    }

    /**
     * Replace tokens per class, this could be standard
     *
     * @param token
     * @return
     */
    public String replace_token_class(String token) {
        for (final String token_class : new HashSet<String>(
                always_classes.keySet())) {
            if (token.matches((String) always_classes.get(token_class))) {
                if (System.getProperty("DEBUG") != null
                        && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(token + " replaced class: : "
                            + token_class);
                }
                return token_class;
            }
        }

        if (word_counts.get(token) == null
                || word_counts.get(token) < this.token_rare_classes_threshold) {
            for (final String token_class : new HashSet<String>(
                    rare_classes.keySet())) {
                if (token.matches((String) rare_classes.get(token_class))) {
                    return token_class;
                }
            }
            return (String) token_classes.get("rare_class");
        }

        return token;

    }

}
