package com.cognitionis.nlp_knowledge.time;

import com.cognitionis.nlp_files.PhraselistFile;
import com.cognitionis.nlp_knowledge.numbers.Numek;
import com.cognitionis.utils_basickit.FileUtils;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.cognitionis.nlp_files.LengthAlphabeticalComparator;

public class Timek {
    // this could probably be loaded from json confing

    public static final String[] phraselist_names = {"weekday", "month", "tunit", "decade", "deictic", "time_of_day", "season", "after_before", "modifier", "relative_ord"};
    public HashMap<String, PhraselistFile> phraselists = new HashMap<>();
    public TreeMap<String, String[]> multitokens = new TreeMap<>(new LengthAlphabeticalComparator()); // merger of all multitokens including their class
    public String multitokens_re = "_no_regex_to_match_"; // merger of all multitokens
    // this can probably be an array of dependent knowledges
    public Numek numek;
    // this could probably be an abstract class
    public Locale locale;
    public HashSet<String> all_keys;
    public HashSet<String> repeated_keys;
    public PhraselistFile ambiguous;
    public PhraselistFile useless_symbols;

    /*
     // set TODO develop the phraselist... or somethign else
     // todo TIMEgranul_re = "(?i)(?:seconds|minute(?:s)?|hour(?:s)?|" + TOD_re + ")";
     * */
    // DEPRECATED could be guessed from phraselists
    // DEPRECATED ALL START WITH c_ public static final String pattern_symbols = "(Card|Ord|c_Tunit|c_Month|c_Weekday|c_Time_of_day|c_Decade|c_Deictic|c_Season|ISOTime|ISODate|.*-Ambiguous)"; // use TIgnore to force decoding in desambiguation
    public Timek() {
        this(new Locale("en", "US"));
    }

    public Timek(Locale l) {
        this(l, "resources");
    }

    public Timek(Locale l, String resources_dir) {
        try {
            locale = l;
            numek = new Numek(l, resources_dir);
            String lang = l.toString().replace('_', '-');
            String shortlang = lang.substring(0, 2);
            String resource_separator=File.separator;
            all_keys = new HashSet<>();
            repeated_keys = new HashSet<>();
            phraselists = new HashMap<>();

            all_keys = numek.all_keys;
            // Load from resource knowledge files (all string, string)            
            String res_path = FileUtils.getResourcesPath(Timek.class,resources_dir + File.separator + "time" + File.separator);

            if(res_path.contains("/")){
                resource_separator="/";
            }           
            if (!FileUtils.URL_exists(res_path + lang + resource_separator)) {
                res_path = res_path + shortlang + resource_separator;
            } else {
                res_path = res_path + lang + resource_separator;
            }

            if (!FileUtils.URL_exists(res_path)) {
                throw new Exception("Not-supported locale: " + lang + " nor " + shortlang);
            } else {
                // these are separated because they allow regex or special things
                if (FileUtils.URL_exists(res_path + "ambiguous.phraselist")) {
                    ambiguous = new PhraselistFile(res_path + "ambiguous.phraselist", false, locale, true, true,true);
                }
                if (FileUtils.URL_exists(res_path + "useless_symbol.phraselist")) {
                    useless_symbols = new PhraselistFile(res_path + "useless_symbol.phraselist", false, locale, false, true,true);
                }

                // TODO: this should only work for some required phraselists
                for (String phra : phraselist_names) {
                    if (FileUtils.URL_exists(res_path + phra + ".phraselist")) {
                        phraselists.put(phra, new PhraselistFile(res_path + phra + ".phraselist", false, locale, false, false,false));
                        repeated_keys.addAll(phraselists.get(phra).intersectPhraselist(all_keys));
                        all_keys.addAll(phraselists.get(phra).keySet());
                        if (!phraselists.get(phra).getMultiRE().equals("_no_regex_to_match_")) {
                            PhraselistFile.mergeMaps(multitokens, phraselists.get(phra).getMultiMap(), phra);
                        }
                    }
                    // ELSE REQUIRED PHRASELIST DOES NOT EXIST
                }
                multitokens_re = PhraselistFile.get_re_from_keyset(multitokens.keySet());
                // set TODO develop the phraselist... or somethign else
                // todo TIMEgranul_re = "(?i)(?:seconds|minute(?:s)?|hour(?:s)?|" + TOD_re + ")";
                if (ambiguous != null) {
                    for (String akey : ambiguous.keySet()) {
                        HashSet<String> temp_keys = new HashSet<>(repeated_keys);
                        for (String key : repeated_keys) {
                            if (akey.contains(key)) {
                                temp_keys.remove(key);
                            }
                        }
                        repeated_keys.clear();
                        repeated_keys.addAll(temp_keys);
                    }
                }
                if (!repeated_keys.isEmpty()) {
                    throw new Exception("This knowledge element has unhandled ambiguity: " + repeated_keys);
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found in " + this.getClass().getName() + ":\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Returns an unambiguous semi-text|semi-pattern NOTE: Ambiguous RE must not
     * contain multi-word replacements
     *
     * @param text
     *
     * @return String: unambiguous text
     */
    public final String disambiguate(String text) {
        //System.out.println("disambig call: "+text);
        String pat = text;
        if (ambiguous != null) {
            Pattern pa = Pattern.compile(ambiguous.getRE(), Pattern.CASE_INSENSITIVE);
            Matcher ma = pa.matcher(text);
            if (ma.find()) {
                // open the ambiguities file, iterate, if matches replace...
                for (String key : ambiguous.getMap().keySet()) {
                    // NOTE: negative patterns cannot be used unless se use another paramter (match-negative-replacement), otherwise everything matches
                /* Boolean positive=true;
                     String negkey="";
                     if(key.contains("-") && key.split("-")[0].equalsIgnoreCase("!negative")){
                     positive=false;
                     negkey=key.split("-")[1];
                     }
                     if (positive && text.matches(key)) {*/
                    //System.out.println("debug111: "+key);
                    Pattern ambig = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                    Matcher m = ambig.matcher(text);
                    if (m.find()) {
                        pat = text.replaceAll(key, ambiguous.getMapValue(key)); // in the file use TIgnore to force decoding in a subsequent step
                        String[] normarr = text.trim().split(" ");
                        String[] patarr = pat.trim().split(" ");
                        text = "";
                        pat="";
                        // THIS DOES NOT WORK FOR Multi-word phraselist... nor for regex based values... e.g., Mon(\\.)?
                        for (int i = 0; i < patarr.length; i++) {
                            if (patarr[i].matches("^c_keep$")){
                                patarr[i]=normarr[i].replaceAll("v__", "");
                                normarr[i]="v__" +normarr[i];
                            }
                            if (patarr[i].matches("^c_.*") && !normarr[i].startsWith("v__")) {// pero açò es un poc merda...                        
                                normarr[i] = "v__" + phraselists.get(patarr[i].substring(2).toLowerCase()).getMapValue(normarr[i]);
                            }
                            text += " " + normarr[i];
                            pat  += " " + patarr[i];
                        }
                        text += " ";
                        pat  += " ";
                    }
                    /*}
                     if (!positive && !text.matches(key)) {
                     return text.replaceAll(key,ambiguous.getMapValue(key));
                     }*/
                }
            }
        }
        return text.trim() + "|" + pat.trim();
    }

    /**
     * Removes all useless symbols from an input text
     *
     * @param text expects space-separated symbols including leading and
     * training spaces
     * @return clean text
     */
    public final String removeUselessSymbols(String text) {
        if (useless_symbols.getRE() != null) {
            return text.replaceAll(" " + useless_symbols.getRE() + " ", " ");
        } else {
            return text;
        }
    }

    /**
     * *****************************************************************
     * NORMALIZING TEXT INPUT
     * ****************************************************************
     */
    /**
     * Obtains the normalized text (NormText) and Patter from a given timex
     * textual expression
     *
     * @param timex the timex textual expression (by default it is case
     * insensitive)
     * @return the feature-values for NormText and Pattern (i.e.,
     * normtext|pattern)
     */
    public String getNormTextandPattern(String timex_text) {
        return getNormTextandPattern(timex_text, Boolean.FALSE);
    }

    /**
     * Obtains the normalized text (NormText) and Patter from a given timex
     * textual expression
     *
     * @param timex the timex textual expression
     * @param case_sensitive boolean selector (by default it is false == case
     * insensitive)
     *
     * @return the feature-values for NormText and Pattern (i.e.,
     * normtext|pattern)
     */
    public String getNormTextandPattern(String timex_text, Boolean case_sensitive) {
        String timex_normtext = "";
        String timex_pattern = "";
        String modifiers = ""; // mid,late,early,almost,approx... 
        try {
            // BASIC CLEAN-UP  -----------------------------------------------------------------------------------
            timex_text = " " + timex_text.replaceAll("\\s+", " ") + " "; // Ensure correct tokenization 
            if (!case_sensitive) {
                timex_text = timex_text.toLowerCase(); //make it all-lowercase
            }
            timex_text = timex_text.replaceAll(" ,", "").replaceAll(", ", " "); // remove tokenized commas untokenized commas
            // REMOVE USELESS SYMBOLS: only if they are completely useless
            // YES: "of" is useless in English dates
            // NO: "los" in Spanish is useful to disambiguate between DATE and SET
            timex_text = this.removeUselessSymbols(timex_text);
            // Unify ISO dates
            timex_text = timex_text.replaceAll("([0-9]+) ([-/:]) ([0-9]+|" + this.phraselists.get("month").getRE() + ") ([-/:]) ([0-9]+)", "$1$2$3$4$5");
            timex_text = timex_text.replaceAll("([0-9]+[-/:]) ((?:[0-9]+|" + this.phraselists.get("month").getRE() + ")[-/:]) ([0-9]+)", "$1$2$3");
            timex_text = timex_text.replaceAll("([0-9]+) ([-/:](?:[0-9]+|" + this.phraselists.get("month").getRE() + ")) ([-/:][0-9]+)", "$1$2$3");
            timex_text = timex_text.replaceAll("([0-9]+|" + this.phraselists.get("month").getRE() + ") ([-/:]) ([0-9]+)", "$1$2$3");
            timex_text = timex_text.replaceAll("((?:[0-9]+|" + this.phraselists.get("month").getRE() + ")[-/:]) ([0-9]+)", "$1$2");
            timex_text = timex_text.replaceAll("([0-9]+|" + this.phraselists.get("month").getRE() + ") ([-/:][0-9]+)", "$1$2");
            timex_text = timex_text.replaceAll("([0-9]0)s", "$1 s");
            // Special for modifiers (SHOULD be loaded from a phraselist)
            timex_text = timex_text.replaceAll("mid(?:-)?([0-9]+)", "mid $1").replaceAll("mid-(.+)", "mid $1");
            // Separate adjective periods num-TUnit
            timex_text = timex_text.replaceAll("([^ ]+)-" + this.phraselists.get("tunit").getRE(), "$1 $2");
            // Special for fractions (only one is normalized because there should be no more than one per timex)
            if (timex_text.matches("(?:.* )?(?:[0-9]* )?[1-9][0-9]*/[1-9][0-9]* " + this.phraselists.get("tunit").getRE() + ".*")) {
                String nums2norm = timex_text.replaceFirst("(.* )?((?:[0-9]* )?[1-9][0-9]*/[1-9][0-9]*)( " + this.phraselists.get("tunit").getRE() + ".*)", "$2");
                String normalizedfrac = "" + Numek.calc_and_sum_frac(nums2norm);
                timex_text = timex_text.replaceFirst("(.* )?((?:[0-9]* )?[1-9][0-9]*/[1-9][0-9]*)( " + this.phraselists.get("tunit").getRE() + ".*)", "$1" + normalizedfrac + "$3");
            }

            //if(this.numek.ordinal_suffixes!=null){ // needed because ordinal card th disambiguation is not included in time.ambiguous
            //    timex_text = timex_text.replaceAll(" ([0-9]+)\\s+("+this.numek.ordinal_suffixes.getRE()+") ", " $1$2 ");
            //}

            // DISAMBIGUATE TO PATTERN IF NEEDED  ----------------------------------------------------------------------------
            // ambiguity (replace also text v_ except c_Card, c_Ord) 
            String norm_and_pat = this.disambiguate(timex_text); // if there is nothing to disambiguate it will return same text and pattern
            timex_text = norm_and_pat.split("\\|")[0].trim();
            timex_pattern = norm_and_pat.split("\\|")[1].trim();

            String[] textarr = timex_text.split(" ");
            String[] patternarr = timex_pattern.split(" ");
            timex_pattern = ""; // reset to build


            //numbers and ISO (Replace both in text v__ and in Pattern c_)            
            // check Nums and ISO (e.g., one million or 25 hundred), if after [0-9] there is no spell leave as it is.
            String currentPat;
            for (int i = 0; i < textarr.length; i++) {
                if (patternarr[i].startsWith("c_") || textarr[i].startsWith("v__")) {
                    currentPat = patternarr[i];
                } else {
                    if (textarr[i].matches("(?:[0-2])?[0-9][.:][0-5][0-9](?:[.:][0-5][0-9])?(?:(?:p|a)(?:\\.)?m(?:\\.)?|h)?") || textarr[i].matches("(?:[0-2])?[0-9](?:(?:p|a)(?:\\.)?m(?:\\.)?)")) {
                        currentPat = "c_isotime";
                    } else {
                        if (textarr[i].matches("(?:[0-3])?[0-9][./-](?:(?:[0-3])?[0-9]|" + this.phraselists.get("month").getRE() + ")[./-][0-9]+") // dd-mm-yyyy
                                || textarr[i].matches(this.phraselists.get("month").getRE() + "[/-][0-9]+") // MM-yyyy
                                || textarr[i].matches("(?:1[0-2]|(?:0)?[1-9])[/-][1-2][0-9]{3}") // mm-yyyy
                                || textarr[i].matches("[0-9]{4}[./-](?:1[0-2]|(?:0)?[1-9])[./-](?:[0-3])?[0-9](?:(T| )[0-2][0-9][.:][0-5][0-9](?:[.:][0-5][0-9])?)?(?:Z)?") // ISO
                                ) {
                            currentPat = "c_isodate";
                        } else {
                            currentPat = textarr[i];
                        }
                    }
                }
                timex_normtext += " " + textarr[i];
                timex_pattern += " " + currentPat;
            }

           
            // Normalize numbers
            String normTextandPattern = this.numek.getNormTextandPattern(timex_normtext + " ", timex_pattern + " ");
            String[] normTextandPattern_arr = normTextandPattern.split("\\|");
            timex_normtext = normTextandPattern_arr[0];
            timex_pattern = normTextandPattern_arr[1];

            //  3 replace all (for each RE) in both text and pattern (don't worry... there wont be strange matches because v__)
            //  if (text matches ^( v__[^\s])+ $ )) break (completely paternized)
            timex_normtext = " " + timex_normtext + " ";
            timex_pattern = " " + timex_pattern + " ";
            Pattern p = Pattern.compile(" " + (multitokens_re).replaceAll("\\\\\\\\", "\\\\") + " ");
            Matcher m = p.matcher(timex_normtext);

            while (m.find()) {
                timex_normtext = timex_normtext.replaceAll("(?i)" + m.group(), " v__" + multitokens.get(m.group().trim())[0] + " ");
                timex_pattern = timex_pattern.replaceAll("(?i)" + m.group(), " " + multitokens.get(m.group().trim())[1] + " ");
            }

            for (String phraselist : this.phraselists.keySet()) {
                p = Pattern.compile(" " + (this.phraselists.get(phraselist).getRE()).replaceAll("\\\\\\\\", "\\\\") + " "); //, Pattern.CASE_INSENSITIVE this must be handled with a parameter (default insensitive, all lowercap)
                m = p.matcher(timex_normtext);
                while (m.find()) {
                    timex_normtext = timex_normtext.replaceAll("(?i)" + m.group(), " v__" + this.phraselists.get(phraselist).getMapValue(m.group().trim()) + " ");
                    timex_pattern = timex_pattern.replaceAll("(?i)" + m.group(), " " + this.phraselists.get(phraselist).getName() + " ");
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found:\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        return (timex_normtext.trim().replaceAll("v__", "") + "|" + timex_pattern.trim());

    }
}
