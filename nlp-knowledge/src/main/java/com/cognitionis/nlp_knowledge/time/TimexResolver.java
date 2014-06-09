package com.cognitionis.nlp_knowledge.time;



/**
 * Timex Resolution: This converts a given timex normalization to a more
 * specific normalization given an speech time (document creation time, DCT)
 * and/or a reference time (time we are talked or written about).
 * 
 * Some timexes cannot be completely resolved (unanchored durations or fuzzy).
 * This class includes some parameters to allow the user choose how to handle them
 * 
 * In short: TimeJSON to resolved (e.g., ISO 8601) (applying optionally some guessing when needed)
 *
 * @author Hector_Llorens
 */
public class TimexResolver {
    
    
    // TODO: Can be like this for now but it is much better to have it as external classes that implement an abstract interface
    // Resolver (with an obligatori method resolve)
    // Usage would be TimexResolver resolver=new TimexResolverTIMEMLTIMEX3() o per parametre new TimexResolver("Timex3");
    public static enum Action {
        TIMEML_TIMEX3, ISO, ICAL, VLINGO
    };    

    /**
     * This resolves TimeJSON to TimeML TIMEX3 normalization format (widely used in research, TimeBank corpus). See timeml.org.
     * @param timeJSON
     * @param DCT_ISO
     * @param Reference_ISO
     * @return 
     */
    public String resolve_to_TimeML_TIMEX3(String timeJSON, String DCT_ISO, String Reference_ISO) {
        String resolved="0000-00-00";
        
        // TODO make sure the DCT and the Reference are fully specified, if not complete with the default
        
        // IT would be better to parse JSON into an object instead of doing this fake parsing...
        
        if(timeJSON.contains("relative")){
            
        }
        if(timeJSON.contains("fuzzy")){
            
        }
        // build the date
        if(timeJSON.contains("year")){
            //resolved=
        }
        
        
        
        
        try{
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }

        return resolved;
    }
    
    /**
     * This resolves the time to the closest single ISO
     * This simplifies all periods/durations to their start time
     * and leaves out SETs (repetition patterns)
     * 
     * @param timeJSON
     * @param DCT_ISO
     * @param Reference_ISO
     * @return 
     */
    public String resolve_to_ISO(String timeJSON, String DCT_ISO, String Reference_ISO) {
        String resolved="0000-00-00";
        try{
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }

        return resolved;
    }

    /**
     * This resolves the time to the closest ISO period (two ISO 8601 times)
     * This simplifies all periods/durations to their start time
     * This leaves out SETs (repetition patterns)
     * 
     * @param timeJSON
     * @param DCT_ISO
     * @param Reference_ISO
     * @return 
     */
    public String resolve_to_ISO_period(String timeJSON, String DCT_ISO, String Reference_ISO) {
        String resolved="BEGIN:VEVENT\n" +
                        "DTSTART;TZID=Europe/Madrid:20130514T100000\n" +
                        "DTEND;TZID=Europe/Madrid:20130514T110000\n" +
                        "SUMMARY:my event name\n" +
                        "END:VEVENT\n";
        try{
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }

        return resolved;
    }

    /**
     * This resolves the time to the iCal representation (used by Google calendar among others)
     * 
     * @param timeJSON
     * @param DCT_ISO
     * @param Reference_ISO
     * @return 
     */
    public String resolve_to_iCAL(String timeJSON, String DCT_ISO, String Reference_ISO) {
        String resolved="0000-00-00|0000-00-00";
        try{
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }

        return resolved;
    }
    
}
