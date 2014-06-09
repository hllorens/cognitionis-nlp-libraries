package com.cognitionis.nlp_lang_models;

import com.cognitionis.nlp_files.PlainFile;
import com.cognitionis.nlp_files.NLPFile;
import com.cognitionis.nlp_files.NgramHandler;
import com.cognitionis.nlp_files.PhraselistFile;
import com.cognitionis.nlp_files.XMLFile;
import com.cognitionis.nlp_files.TokenizedFile;
import com.cognitionis.nlp_files.annotation_scorers.*;
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

    // TODO: We miss an action to train a language model and an action to detect language using a specific folder of lang models
    public static enum Action {

        DETECT_LANG
        
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
                case DETECT_LANG: {
                    TextCategorizer teka = new TextCategorizer();
                    System.out.println(teka.categorize(input_text));
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
