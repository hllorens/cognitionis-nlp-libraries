
package com.cognitionis.nlp_segmentation;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hector
 */
public class SentSplit {
    public static void sentSplitAndTokenize(File in_file, File out_file){
        try {
            // Create a new tokenizer (default) and tokenize with sentence split
            Tokenizer_PTB_Rulebased toki=new Tokenizer_PTB_Rulebased();
            toki.tokenize(in_file, out_file);
        } catch (Exception ex) {
            Logger.getLogger(SentSplit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /*
    public static void printEachForward(BreakIterator boundary, String source) {
        int start = boundary.first();
        for (int end = boundary.next();
                end != BreakIterator.DONE;
                start = end, end = boundary.next()) {
            System.out.println(source.substring(start, end));
        }
    }

    public static void printFirst(BreakIterator boundary, String source) {
        int start = boundary.first();
        int end = boundary.next();
        System.out.println(source.substring(start, end));
    }

    public static void printLast(BreakIterator boundary, String source) {
        int end = boundary.last();
        int start = boundary.previous();
        System.out.println(source.substring(start, end));
    }



                case SEGMENT_SENTENCE_PRUEBAS:
                    for (NLPFile nlpfile : nlp_files) {
                        if (!nlpfile.getClass().getSimpleName().equals("PlainFile")) {
                            throw new Exception("TIMEE requires PlainFile files as input. Found: " + nlpfile.getClass().getSimpleName());
                        }

                        String stringToExamine = FileUtils.readFileAsString(nlpfile.getFile().getCanonicalPath(), null);
                        //print each word in order
                        BreakIterator boundary = BreakIterator.getWordInstance();
                        boundary.setText(stringToExamine);
                        printEachForward(boundary, stringToExamine);

                        System.out.println("---------sent-------" + Arrays.toString(BreakIterator.getAvailableLocales()));


                        //print each sentence in reverse order
                        boundary = BreakIterator.getSentenceInstance(new Locale(lang));
                        boundary.setText(stringToExamine);
                        printEachForward(boundary, stringToExamine);
                        printFirst(boundary, stringToExamine);
                        printLast(boundary, stringToExamine);

                        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
                        String source = "This is a test. This is a T.L.A. test. Now with a Dr. in it.";
                        iterator.setText(source);
                        int start = iterator.first();
                        for (int end = iterator.next();
                                end != BreakIterator.DONE;
                                start = end, end = iterator.next()) {
                            System.out.println(source.substring(start, end));
                        }



                    }
                    break;
                    
     */
}
