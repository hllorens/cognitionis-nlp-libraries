package com.cognitionis.nlp_files;




/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class PlainFile extends NLPFile {

    public PlainFile(String filename) {
        super(filename);
    }
    
    private final static String format="PLAIN";

    @Override
    public String toPlain(String filename){
        throw new UnsupportedOperationException("Already a plain file");
    }

    @Override
    public Boolean isWellFormatted() {
        try {
            if (super.getFile()==null) {
                throw new Exception("No file loaded in NLPFile object");
            }
        } catch (Exception e) {
            System.err.println("Errors found ("+this.getClass().getSimpleName()+"):\n\t" + e.toString() + "\n");
            if(System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")){e.printStackTrace(System.err);}
            return false;
        }
        return true;
    }



}
