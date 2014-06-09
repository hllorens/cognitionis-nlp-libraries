package com.cognitionis.nlp_segmentation;

import com.cognitionis.utils_basickit.FileUtils;
import java.io.*;
import java.util.regex.Pattern;

/**
 * Tokenizer_PTB_Rulebased
 * STRATEGY (TreeBank format):
 * 1) Separate what is always a token
 * 2) Separate what is always a token when followed by space
 * 3) Separate periods except accronyms and abbreviations
 *
 * GOOD EXTRAS:
 * 1) Complement this with an alignment tool to get back the original text (either with space, offset handling or both)
 *
 * KNOWN LIMITATIONS:
 * 1) Period ambiguity is not handled correctly (however this is a known problem often handled at POS tagging step)
 *
 * @author Hector Llorens
 * @since May 20, 2013
 */
public class Tokenizer_PTB_Rulebased {

    // do simple regex sentence splitting by default: replaceAll("^\\.$","\\.\n")
    private Boolean doSentSplit;
    // default abbrevs
    public static final String defauldAbbrevRegex = "(adj|Adm|adv|Ala|apdo|Apdo|Ariz|Ark|Aug|Ave|Bancorp|Bhd|Brig|Bros|Ca|Calif|Capt|Cia|Cía|Cie|Co|CO|Col|Colo|Conn|Corp|CORP|Cos|COS|Dec|Del|dept|Dept|D-Mass|Dr|dr|Drs|ej|etc|Etc|fdo|Feb|Fla|ft|Ft|Ga|Gen|Gob|Gov|Hon|Ill|Inc|INC|Ind|Jan|Jr|Kan|Ky|La|Lt|Ltd|Maj|Mass|Md|Messrs|Mfg|Mich|Minn|Miss|Mo|Mr|Mrs|Ms|Neb|Nev|No|Nos|Nov|num|Num|núm|Oct|Okla|Ont|Ore|Pa|pág|págs|Ph|Prof|Prop|Pty|Rep|Reps|Rev|Sen|Sens|Sept|Sgt|Sr|Sra|Srta|St|Ste|tel|Tel|telef|Telef|Tenn|Tex|Ud|Uds|Va|Va|Vd|Vds|vol|vs|Vs|VS|Vt|Wash|Wis|Wyo)";
    // space regexes
    public static Pattern basicSpacePattern = Pattern.compile("^|$|\\s+", Pattern.MULTILINE);
    // Always separated as tokens (grouped if equal)
    public static Pattern alwaysTokenPattern = Pattern.compile("(\\[+|\\(+|\\{+|\\<+|\\]+|\\)+|\\}+|\\>+|[;]+|[?!]+|[¿¡]+|=+|\\.\\.+|--+|\"|`+|''+)");
    // Only token if followed by space (?= is needed not to replace it afterwards)
    public static Pattern bySpacePattern = Pattern.compile("([-:,]\\s|\\s-(?![\\d,.]))");
    // single quote regex \s' and '\s (except '\d0(s)? or '' (which are already separated)
    public static Pattern singleQuotePattern = Pattern.compile("('\\d+0s?|(?<=\\s)'(?!')|(?<!')'(?=\\s))");
    // abbreviation regexes. NOTE: language dependent, could be file.list per lang, however to simplify we use a GENERIC one: English and Catalan are included (?![t]\\b) this is to avoid separating n't which is OK since never happens in Catalan
    public static Pattern oneWordPrefixPattern = Pattern.compile("\\b([ldnmts]')(?=[a-zA-ZÀ-ÿ0-9])(?![t]\\b)", Pattern.CASE_INSENSITIVE);
    public static Pattern oneWordSuffixPattern = Pattern.compile("('ll|'re|'ve|n't|'[smdnl]|-(?:la|li|lo|ho|hi|me|te|se))\\b", Pattern.CASE_INSENSITIVE);
    // contractions
    public static final String[] twoWordContractions = new String[]{"\\b(can)(not)\\b", "\\b(d')(ye)\\b", "\\b(gim)(me)\\b", "\\b(gon)(na)\\b", "\\b(got)(ta)\\b", "\\b(lem)(me)\\b", "\\b(more)('n)\\b", "\\b(wan)(na)\\b"};
    public static final Pattern[] twoWordContractionPatterns = new Pattern[twoWordContractions.length];

    static {
        for (int i = 0; i < twoWordContractions.length; i++) {
            twoWordContractionPatterns[i] = Pattern.compile(twoWordContractions[i], Pattern.CASE_INSENSITIVE);
        }
    }
    public static final String[] threeWordContractionRegexes = new String[]{"\\b(wha)(dd)(ya)\\b", "\\b(wha)(t)(cha)\\b"};
    public static final Pattern[] threeWordContractionsPatterns = new Pattern[threeWordContractionRegexes.length];

    static {
        for (int i = 0; i < threeWordContractionRegexes.length; i++) {
            threeWordContractionsPatterns[i] = Pattern.compile(threeWordContractionRegexes[i], Pattern.CASE_INSENSITIVE);
        }
    }
    //public static Pattern tAbbreviationPattern = Pattern.compile("('t)(is|was)\\b"); // to slang to be supported
    protected Pattern[] patterns;
    private String abbrevRegex;
    public Pattern periodPattern;

    public Tokenizer_PTB_Rulebased() {
        this(true);
    }

    public Tokenizer_PTB_Rulebased(Boolean sentsplit) {
        doSentSplit = sentsplit;
        String periodRegex = "((?<=(\\d|[a-zA-ZÀ-ÿ]['][a-zA-ZÀ-ÿ][a-zA-ZÀ-ÿ]?))\\.(?=[\\s])|(?<!(\\b[a-zA-ZÀ-ÿ]|" + defauldAbbrevRegex + "|\\.\\.))\\.(?=[\\s]))";
        periodPattern = Pattern.compile(periodRegex, Pattern.MULTILINE);
        patterns = new Pattern[]{bySpacePattern, singleQuotePattern, oneWordSuffixPattern, alwaysTokenPattern, oneWordPrefixPattern};
    }

    public Tokenizer_PTB_Rulebased(Boolean sentsplit, File abbrev) {
        doSentSplit = sentsplit;
        abbrevRegex = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(abbrev));
            try {
                String line; // = null; unecessary
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    //abbrevMap.put(line, 1);
                    line = line.trim();
                    if (line.equals("") || line.startsWith("#") || line.startsWith("//")) {
                        continue;
                    }
                    /*if (line.endsWith(".")) {line=line.substring(0, -1); }*/
                    if (i == 0) {
                        abbrevRegex = "(" + line;
                    } else {
                        abbrevRegex += "|" + line;
                    }
                    i++;
                }
                if (!abbrevRegex.equals("")) {
                    abbrevRegex = abbrevRegex.replaceAll("\\.", "") + ")";
                }
            } finally {
                reader.close();     //if (reader != null) { unnecessary
            }
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\t" + e.toString() + ":" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }

        String periodRegex = "((?<=(\\d|[a-zA-ZÀ-ÿ]['][a-zA-ZÀ-ÿ][a-zA-ZÀ-ÿ]?))\\.(?=[\\s])|(?<!(\\b[a-zA-ZÀ-ÿ]|" + abbrevRegex + "|\\.\\.))\\.(?=[\\s]))";
        periodPattern = Pattern.compile(periodRegex, Pattern.MULTILINE);
        patterns = new Pattern[]{bySpacePattern, alwaysTokenPattern, singleQuotePattern, oneWordSuffixPattern, oneWordPrefixPattern};
    }

    /**
     * Tokenizes the input text and returns a string corresponding to the tokens
     * as one token per line
     *
     * @param text (String plain text)
     * @return tokens (String)
     */
    public String tokenize(String text) {
        String tokens = "";
        if (text != null && text.length() != 0) {
            text = basicSpacePattern.matcher(text).replaceAll(" ");
            for (Pattern pattern : patterns) {
                text = pattern.matcher(text).replaceAll(" $1 ");
            }
            for (Pattern pattern : twoWordContractionPatterns) {
                text = pattern.matcher(text).replaceAll(" $1 $2");
            }
            for (Pattern pattern : threeWordContractionsPatterns) {
                text = pattern.matcher(text).replaceAll(" $1 $2 $3");
            }
            text = periodPattern.matcher(text).replaceAll(" . ");
            text = basicSpacePattern.matcher(text).replaceAll(" "); // AGAIN to make sure there are no extra spaces
            //Pattern RemoveStartEndSpacePattern = Pattern.compile("^(\\s+)|(\\s+)$", Pattern.MULTILINE);
            //text = RemoveStartEndSpacePattern.matcher(text).replaceAll(""); // clean first and end spaces (trim?)
            text = text.trim();
            if (!text.isEmpty() && !text.equals("")) {
                //tokens = text.toString().split("\\s+");
                tokens = text.toString().replaceAll("\\s+", "\n");
                if (doSentSplit) {
                    tokens += "\n";
                    tokens = tokens.replaceAll("\n([.!?])\n", "\n$1\n\n");
                }
            }
        }
        return tokens;
    }

    /**
     * Tokenize an input file and output tokens in another file or stdout if
     * null.
     *
     * @param in_file
     * @param out_file
     */
    public void tokenize(File in_file, File out_file) throws Exception {
        String encoding = FileUtils.getEncoding(in_file);
        if (!encoding.equalsIgnoreCase("UTF-8") && !encoding.equalsIgnoreCase("ASCII")) {
            throw new Exception("\n\tError: Only ASCII/UTF-8 text is allowed. " + in_file.getName() + " is " + encoding + "\n");
        }
        //try {
        BufferedWriter out;
        if (out_file != null) {
            out = new BufferedWriter(new FileWriter(out_file));
        } else {
            out = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        // read-line-by-line and write line by line
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(in_file), "UTF-8"));
        try {
            String line;
            String previous_line = null; // avoid extra new line in the last line ended by sent-separator
            while ((line = in.readLine()) != null) {
                if (previous_line != null) {
                    out.write(tokenize(previous_line));
                }
                previous_line = line;
            }
            String last_line = tokenize(previous_line);
            if (doSentSplit && last_line.endsWith("\n\n")) {
                last_line = last_line.substring(0, last_line.length() - 1);
            }
            out.write(last_line);
        } finally {
            if (out_file == null) {
                out.flush();
            } else {
                out.close();
            }
            in.close();
        }
        /*} catch (IOException ex) {
         Logger.getLogger(Tokenizer_PTB_Rulebased.class.getName()).log(Level.SEVERE, null, ex);
         }*/

    }
    
    /**
     * Tokenizes the input text and returns a string array corresponding to the tokens.
     * Normally the array is printed as one token per line
     *
     * @param text (plain text)
     * @return tokens (String [] array)
     */
    public String[] getTokenTexts(String text) throws FileNotFoundException {
        text = basicSpacePattern.matcher(text).replaceAll(" ");
        for (Pattern pattern : patterns) {           text = pattern.matcher(text).replaceAll(" $1 ");        }
        for (Pattern pattern : twoWordContractionPatterns) {            text = pattern.matcher(text).replaceAll(" $1 $2");        }
        for (Pattern pattern : threeWordContractionsPatterns) {            text = pattern.matcher(text).replaceAll(" $1 $2 $3");        }
        text = periodPattern.matcher(text).replaceAll(" . ");
        text = basicSpacePattern.matcher(text).replaceAll(" "); // AGAIN to make sure there are no extra spaces
        Pattern RemoveStartEndSpacePattern = Pattern.compile("^(\\s+)|(\\s+)$", Pattern.MULTILINE);
        text = RemoveStartEndSpacePattern.matcher(text).replaceAll(""); // clean first and end spaces

        String[] tokens = null;
        if(!text.isEmpty() && !text.equals(""))
            tokens=text.toString().split("\\s+");

        return tokens;
    }    

    /**
     * Tokenize an input filename and output tokens in .tok file. It just
     * creates a file as in_filename+".tok"
     *
     * @param in_filename
     * @return out_filename
     */
    public String tokenize_filename_to_tokfile(String in_filename) throws Exception {
        File in_file = new File(in_filename);
        File out_file = new File(in_filename + ".tok");
        String encoding = FileUtils.getEncoding(in_file);
        if (!encoding.equalsIgnoreCase("UTF-8") && !encoding.equalsIgnoreCase("ASCII")) {
            throw new Exception("\n\tError: Only ASCII/UTF-8 text is allowed. " + in_file.getName() + " is " + encoding + "\n");
        }
        //try {
        BufferedWriter out;
        if (out_file != null) {
            out = new BufferedWriter(new FileWriter(out_file));
        } else {
            out = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        // read-line-by-line and write line by line
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(in_file), "UTF-8"));
        try {
            String line;
            String previous_line = null; // avoid extra new line in the last line ended by sent-separator
            while ((line = in.readLine()) != null) {
                if (previous_line != null) {
                    out.write(tokenize(previous_line));
                }
                previous_line = line;
            }
            String last_line = tokenize(previous_line);
            if (doSentSplit && last_line.endsWith("\n\n")) {
                last_line = last_line.substring(0, last_line.length() - 1);
            }
            out.write(last_line);
        } finally {
            if (out_file == null) {
                out.flush();
            } else {
                out.close();
            }
            in.close();
        }
        return in_filename + ".tok";
        /*} catch (IOException ex) {
         Logger.getLogger(Tokenizer_PTB_Rulebased.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }

    
}