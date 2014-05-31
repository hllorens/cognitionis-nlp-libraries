package com.cognitionis.nlp_files;

import java.io.*;
import java.util.regex.Pattern;

/**
 * TokenizedPerSentenceFile consists of a space separated tokens, and one sentence per line
 * word1 word2, word1/tag word2/tag, or word1|<tag> ...
 * 
 * @author Héctor Llorens
 * @since 2011
 */
public class TokenizedPerSentenceFile extends NLPFile {

    private String tag_separator;
    
    public TokenizedPerSentenceFile(String filename) {
        this(filename,null); // null --> untagged
    }
    
    public TokenizedPerSentenceFile(String filename, String separator) {
        super(filename);
        isWellFormatted = false;
        tag_separator=separator;
}
    

    public Boolean isWellFormatted() {
        try {
            if (super.getFile()==null) {
                throw new Exception("No file loaded in NLPFile object");
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(this.f))) {
                String line;
                int linen = 0;
                while ((line = reader.readLine()) != null) {
                    line=line.trim();
                    linen++;
                    if(tag_separator!=null && line.length()!=0){
                        if(!line.matches("^[^"+tag_separator+"]+"+tag_separator+"[^"+tag_separator+"]+(\\s+[^"+tag_separator+"]+"+tag_separator+"[^"+tag_separator+"]+)*$")){
                            throw new Exception("Line " + linen + " ("+line+"): Does not have the format word"+tag_separator+"tag in all the words");
                        }
                    }
                }   
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }else{
                System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");                
            }
            this.isWellFormatted=false;
            return false;
        }
        this.isWellFormatted = true;
        return true;
    }


    @Override
    public String toPlain(String filename) {
        throw new UnsupportedOperationException("This will consist just to remove the tokens"); //To change body of generated methods, choose Tools | Templates.
    }




}
