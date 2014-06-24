package com.cognitionis.nlpbt;

import com.cognitionis.nlp_files.PlainFile;
import com.cognitionis.nlp_files.NLPFile;
import com.cognitionis.nlp_files.NgramHandler;
import com.cognitionis.nlp_files.PhraselistFile;
import com.cognitionis.nlp_files.XMLFile;
import com.cognitionis.nlp_files.TokenizedFile;
import com.cognitionis.nlp_files.annotation_scorers.*;
import com.cognitionis.nlp_knowledge.numbers.Numek;
import com.cognitionis.nlp_knowledge.time.Timek;
import com.cognitionis.nlp_knowledge.time.TimexNormalizer;
import com.cognitionis.nlp_lang_models.TextCategorizer;
import com.cognitionis.nlp_segmentation.Tokenizer_PTB_Rulebased;
import com.cognitionis.nlp_taggers.Baseline_MostFrequentTag;
import com.cognitionis.utils_basickit.*;
import java.io.*;
import java.util.List;
import java.util.Locale;
import org.apache.commons.cli.*;

/**
 * Default entry point for basic functionality testing
 *
 * @author hector
 */
public class Main {

    public static enum Action {

        DETECT_LANG, TOKENIZE, TAG, XML2TOK, TOK2XML, EVAL_ANNOT, TIMEX_PATTERN, TIMEX_NORM, TIMEX_RESOLVE, CANONICALIZE_PHRASELIST, NUMBERIZE, GET_NGRAMS,
        TRAIN_BASELINE_TAGGER, TEST_BASELINE_TAGGER
        
    };

    public static void main(String[] args) {
        try {
            String lang = "en";
            String country = "US";
            String action = "none";
            String action_parameters = null;
            String input_files[];
            String input_text = null;

            // Probably UIMA does this Even Pipelines
            // UIMA: Extract some text from some XML tag, process this and that, output it in format X...

            Options opt = new Options();
            //addOption(String opt, boolean hasArg, String description)
            opt.addOption("h", "help", false, "Print this help");
            opt.addOption("l", "lang", true, "Language locale (default \"en_US\" [English])");
            opt.addOption("a", "action", true, "Action/s to be done (tokenize, tag, parse, ...)");
            opt.addOption("ap", "action_parameters", true, "Optionally actions can have parameters (-a PRINT_STATS -ap POS)");
            opt.addOption("t", "text", true, "To use text instead of a file (for short texts)");
            opt.addOption("d", "debug", false, "Debug mode: Output errors stack trace (default: disabled)");
            PosixParser parser = new PosixParser();
            CommandLine cl_options = parser.parse(opt, args);
            input_files = cl_options.getArgs();
            HelpFormatter hf = new HelpFormatter();
            if (cl_options.hasOption('h')) {
                hf.printHelp("Help", opt);
                System.exit(0);
            } else {
                if (cl_options.hasOption('d')) {
                    System.setProperty("DEBUG", "true");
                }
                if (cl_options.hasOption('l')) {
                    String l = cl_options.getOptionValue('l').toLowerCase();
                    if (l.length() != 5) {
                        hf.printHelp("Help", opt);
                        throw new Exception("Error: incorrect locale " + l + " -- must be 5 chars (e.g., en-US)");
                    } else {
                        lang = l.substring(0, 2).toLowerCase();
                        country = l.substring(3).toUpperCase();
                    }
                }
                if (cl_options.hasOption('a')) {
                    action = cl_options.getOptionValue("a");
                    try {
                        Action.valueOf(action.toUpperCase());
                    } catch (Exception e) {
                        String errortext = "\nValid acctions are:\n";
                        for (Action c : Action.values()) {
                            errortext += "\t" + c.name() + "\n";
                        }
                        throw new RuntimeException("\tIlegal action: " + action.toUpperCase() + "\n" + errortext);
                    }
                    if (cl_options.hasOption("ap")) {
                        action_parameters = cl_options.getOptionValue("ap");
                    }
                    if (cl_options.hasOption("t")) {
                        input_text = cl_options.getOptionValue("t");
                    }
                } else {
                    hf.printHelp("Help", opt);
                    String errortext = "\nValid acctions are:\n";
                    for (Action c : Action.values()) {
                        errortext += "\t" + c.name() + "\n";
                    }
                    throw new IOException("\nError: No action specified." + errortext);
                }
            }

            if ((input_text == null || input_text.length() == 0) && input_files.length == 0) {
                throw new Exception("No input files or input text found");
            }
            if (input_text != null && input_files.length != 0) {
                throw new Exception("Only one input type allowed: input text or input file/s");
            }

            for (int i = 0; i < input_files.length; i++) {
                File f = new File(input_files[i]);
                if (!f.exists()) {
                    throw new FileNotFoundException("File does not exist: " + f);
                }
                if (!f.isFile()) {
                    throw new IllegalArgumentException("Should not be a directory: " + f);
                }
                String file_encoding = FileUtils.getEncoding(f);
                if (!FileUtils.checkEncoding(file_encoding, "(ASCII|UTF-8)")) {
                    throw new Exception("Error: " + f + " has an unsupported encoding " + file_encoding + " (must be (ASCII|UTF-8))");
                }
            }
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("ExecPath: " + FileUtils.getExecutionPath());
                System.err.println("ApplicationPath: " + FileUtils.getApplicationPath());
                System.err.println("\n\nDoing action: " + action.toUpperCase() + "\n------------");
                System.err.println("\n\n");
            }

            doAction(action, input_files, action_parameters, lang, country);
            if(input_text!=null)
                doAction(action, input_text, action_parameters, lang, country);

        } catch (Exception e) {
            System.err.println("\nErrors found:\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }

    public static String getParameter(String params, String param) {
        String paramValue = null;

        if (params != null && params.contains(param)) {
            if (params.matches(".*" + param + "=[^,]*,.*")) {
                paramValue = params.substring(params.lastIndexOf(param + "=") + param.length() + 1, params.indexOf(',', params.lastIndexOf(param + "=")));
            } else {
                if (params.matches(".*" + param + "=[^,]*")) {
                    paramValue = params.substring(params.lastIndexOf(param + "=") + param.length() + 1);
                }
            }
        }

        return paramValue;

    }

    public static void doAction(String action, String[] input_files, String action_parameters, String lang, String country) {
        try {
            switch (Action.valueOf(action.toUpperCase())) {
                case TOKENIZE: {
                    Tokenizer_PTB_Rulebased toki = new Tokenizer_PTB_Rulebased();
                    for (String file : input_files) {
                        toki.tokenize(new File(file), null);
                    }
                }
                break;
                case XML2TOK: {
                    Tokenizer_PTB_Rulebased toki = new Tokenizer_PTB_Rulebased();
                    for (String file : input_files) {
                        NLPFile f = new XMLFile(file, null);
                        f.toPlain(f.getFile().getAbsolutePath() + ".plain");
                        toki.tokenize(new File(f.getFile().getAbsolutePath() + ".plain"), new File(f.getFile().getAbsolutePath() + ".plain.tok"));
                        // put xml tags in tokens
                        TokenizedFile tokenized = new TokenizedFile(f.getFile().getAbsolutePath() + ".plain.tok");
                        tokenized.isWellFormatted(); // this should probably included in constructor
                        String annotated = tokenized.add_IOB_from_XML(file, "TEXT", "TIMEX3", null, null);
                        (new File(f.getFile().getAbsolutePath() + ".plain")).delete();
                        (new File(f.getFile().getAbsolutePath() + ".plain.tok")).delete();
                        // align
                        //System.out.println(annotated);
                    }
                }
                break;
                case TOK2XML: {
                    if (input_files.length != 2) {
                        throw new Exception("Two files required! tok and plain to pair to");
                    }
                    NLPFile tok = new TokenizedFile(input_files[0]);
                    NLPFile plain = new PlainFile(input_files[1]);
                    // put xml tags in tokens
                }
                break;
                case EVAL_ANNOT: {
                    if (input_files.length != 2) {
                        throw new Exception("Two files required! key and annotation");
                    }
                    Scorer s = new Scorer();
                    Score score = s.score(new TokenizedFile(input_files[1]), input_files[0], 1, -1);
                    score.print("");
                }
                break;
                case GET_NGRAMS: {
                    String n = getParameter(action_parameters, "n");
                    if (n == null) {
                        throw new Exception("size of the n-grams --> parameter n is required (e.g., n=3)");
                    }
                    for (String file : input_files) {
                        TokenizedFile f = new TokenizedFile(file, " ");
                        NgramHandler ngramh = new NgramHandler(f, Integer.parseInt(n));
                        for (List<String[]> ngram : ngramh) {
                                System.out.print("[");
                            for(String [] ngram_element:ngram){
                                System.out.print("[");
                                for(int i=0;i<ngram_element.length;i++){
                                    System.out.print((i > 0 ? " " : "") +ngram_element[i]);
                                }
                                System.out.print("]");
                            }
                                System.out.println("]");
                        }
                        System.out.println();
                    }
                }
                break;
                case TRAIN_BASELINE_TAGGER: {
                    String n = getParameter(action_parameters, "n");
                    if (n == null) {
                        throw new Exception("size of the n-grams --> parameter n is required (e.g., n=3)");
                    }
                    String classes = getParameter(action_parameters, "classes");
                    if (classes == null) {
                        classes="default";
                    }
                    for (String file : input_files) {
                        Baseline_MostFrequentTag tagger = new Baseline_MostFrequentTag();
                        tagger.train_model(file,Integer.parseInt(n),classes);
                        tagger.write_model(null);
                    }
                }
                break;
                case TEST_BASELINE_TAGGER: {
                    String model = getParameter(action_parameters, "model");
                    if (model == null) {
                        throw new Exception("A 'model' to test must be specified as a paramter");
                    }
                    for (String file : input_files) {
                        Baseline_MostFrequentTag tagger = new Baseline_MostFrequentTag();
                        tagger.read_model(model);
                        //tagger.test_model(file);
                    }
                }
                break;

                    
            }

        } catch (Exception e) {
            System.err.println("\nErrors found:\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    public static void doAction(String action, String input_text, String action_parameters, String lang, String country) {
        try {
            switch (Action.valueOf(action.toUpperCase())) {
                case TOKENIZE: {
                    Tokenizer_PTB_Rulebased toki = new Tokenizer_PTB_Rulebased();
                    System.out.println(toki.tokenize(input_text));
                }
                break;
                case CANONICALIZE_PHRASELIST: {
                    String entity = getParameter(action_parameters, "entity");
                    if (entity == null) {
                        throw new Exception("entity to be canonicalized --> parameter entity is required (e.g., entity=callmode)");
                    }
                    String app_path = FileUtils.getApplicationPath();
                    PhraselistFile phra=new PhraselistFile(app_path+File.separator+"resources/canonicalization-phraselists"+File.separator+entity+".phraselist", false, new Locale(lang), true, true, true);
                    String cano=phra.getMapValue(input_text);
                    if(cano==null){
                        cano="Not canonicalized";
                    }
                    System.out.println(input_text+" >>>>> "+cano);
                    // TODO, make this happen with just a name it defines a folder and a rule.phraselist
                    //Canonicalizer instance = new Canonicalizer(new Locale(lang, country), entity);
                    //System.out.println(instance.canonicalize(input_text));
                }
                break;
                case TIMEX_PATTERN: {
                    Timek tiki = new Timek(new Locale(lang, country));
                    System.out.println(tiki.getNormTextandPattern(input_text));
                }
                break;
                case TIMEX_NORM: {
                    TimexNormalizer tiki = new TimexNormalizer(new Locale(lang, country));
                    System.out.println(tiki.normalize(input_text));
                }
                break;
                case DETECT_LANG: {
                    TextCategorizer teka = new TextCategorizer();
                    System.out.println(teka.categorize(input_text));
                }
                break;
                case GET_NGRAMS: {
                    String n = getParameter(action_parameters, "n");
                    if (n == null) {
                        throw new Exception("size of the n-grams --> parameter n is required (e.g., n=3)");
                    }
                    for (String ngram : NgramHandler.getNgrams(input_text, Integer.parseInt(n))) {
                        System.out.println(ngram);
                    }
                    System.out.println();
                }
                break;
                case NUMBERIZE: {
                    String type = getParameter(action_parameters, "type");
                    if (type == null) {
                        type = "decimal";
                    }
                    Numek nuki = new Numek(new Locale(lang, country));
                    if (type.equals("roman")) {
                        System.out.println(Numek.Roman2Decimal(input_text));
                    } else {
                        System.out.println(nuki.text2number(input_text));
                    }
                }
                break;
            }

        } catch (Exception e) {
            System.err.println("\nErrors found:\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }
}
