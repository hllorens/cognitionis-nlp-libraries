
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
}
