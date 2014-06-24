package com.cognitionis.external_tools;

//import edu.mit.jwi.*;
//import edu.mit.jwi.item.*;
//import java.io.File;
//import java.net.URL;
//import edu.smu.tspell.wordnet.*;
import java.io.*;
import com.cognitionis.utils_basickit.FileUtils;

/**
 *
 * @author Hector Llorens
 * @since 2011
 */
public class WNInterface {

    static final String wndict = "/home/hector/Dropbox/WordNets/WordNet-3.0/dict/";
    public static String wnES4top = FileUtils.getApplicationPath() + "program-data/WN/wn_es_nouns_top4classes2.utf8.ewn";
    public static String wn_zh_time = FileUtils.getApplicationPath() + "program-data/WN/wn_zh_time.txt";
    public static String wn_it_time = FileUtils.getApplicationPath() + "program-data/WN/wn_it_time.txt";
//    IDictionary dict;
//    WordNetDatabase database;

    public void WNInterface() {
        try {
            // construct the dictionary object and open it
            //dict = new edu.mit.jwi.Dictionary(wndict);
            //dict.open();
            System.setProperty("wordnet.database.dir", wndict);
            //           database = WordNetDatabase.getFileInstance();


        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    public String getHypersHACK(String word, String pos) {
        String s = "entity";
        try {
            String minipos = pos.substring(0, 1).toLowerCase(); // to treat not only nouns...

            String[] psCmd = {"sh", "-c", "echo \"" + word + "\" | " + FileUtils.getApplicationPath() + "program-data/wn.toplevel.pl -p " + minipos};

            //System.err.println("command: "+psCmd[0]+psCmd[1]+psCmd[2]);

            Process p = Runtime.getRuntime().exec(psCmd);
            

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

//            BufferedReader stdError = new BufferedReader(new
            //               InputStreamReader(p.getErrorStream()));

            // read the output from the command
            //System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                break;
            }
            stdInput.close();
            if (p != null) {
                p.getInputStream().close();
                p.getOutputStream().close();
                p.getErrorStream().close();
                p.destroy();
            }

            /*
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
            System.out.println(s);
            }*/


        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return s;
    }

    /*   public String getHypers(
    String word) {
    return getHypers(word, "NOUN");
    }*/
    public String getHypersHACKES(String word, String pos) {
        String s = "entity";
        try {
            String minipos = pos.substring(0, 1).toLowerCase(); // to treat not only nouns...
            // DEPRECATED
            String[] psCmd = {"sh", "-c", "echo \"" + word + "\" | " + FileUtils.getApplicationPath() + "program-data/wn.toplevel.es.pl -p " + minipos};
            Process p = Runtime.getRuntime().exec(psCmd);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = stdInput.readLine()) != null) {
                break;
            }
            stdInput.close();
                if(p!=null){
                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();
                    p.destroy();
                }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return s;
    }

    public String getHypersHACKES2(String word, String pos) {
        String s = "entity";
        try {

            String minipos = pos.substring(0, 1).toLowerCase(); // to treat not only nouns...
            if (minipos.equals("n")) {

                BufferedReader reader = new BufferedReader(new FileReader(new File(wnES4top)));

                try {
                    String line;
                    String[] linearr = null;

                    while ((line = reader.readLine()) != null) {
                        linearr = line.split("\\t");
                        if (word.equalsIgnoreCase(linearr[0])) {
                            s = linearr[1];
                            break;
                        }
                    }

                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        return s;
    }

    public String getHypersHACKZH(String word, String pos) {
        String s = "entity";
        try {
            String minipos = pos.substring(0, 1).toLowerCase(); // to treat not only nouns...
            if (minipos.equals("n")) {
                BufferedReader reader = new BufferedReader(new FileReader(new File(wn_zh_time)));
                try {
                    String line;
                    String[] linearr = null;

                    while ((line = reader.readLine()) != null) {
                        linearr = line.split("\\t");
                        if (word.equalsIgnoreCase(linearr[0])) {
                            s = linearr[1];
                            break;
                        }
                    }

                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        return s;
    }

    public String getHypersHACKIT(String word, String pos) {
        String s = "entity";
        try {
            //word=word.replaceAll(" ", "_");
            String minipos = pos.substring(0, 1).toLowerCase(); // to treat not only nouns...
            if (minipos.matches("(?i)(N|A)")) {
                BufferedReader reader = new BufferedReader(new FileReader(new File(wn_it_time)));
                try {
                    String line;
                    String[] linearr = null;
                    while ((line = reader.readLine()) != null) {
                        linearr = line.split("\\t");
                        if (word.equalsIgnoreCase(linearr[0]) && pos.equalsIgnoreCase(linearr[1])) {
                            s = linearr[2];
                            break;
                        }
                    }

                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        return s;
    }
    /*   public String getHypers(
    String word, String pos) {
    String hyper = "";
    edu.smu.tspell.wordnet.Synset[] synsets;
    if (pos.startsWith("N")) {
    synsets = database.getSynsets(word, SynsetType.NOUN);
    } else {
    if (pos.startsWith("V")) {
    synsets = database.getSynsets(word, SynsetType.VERB);
    } else {
    synsets = database.getSynsets(word);
    }

    }

    if (synsets.length > 0) {
    //for (int i = 0; i < synsets.length; i++) {
    //String[] wordForms = synsets[i].getWordForms();
    String[] wordForms = synsets[0].getWordForms();
    /*for (int j = 0; j < wordForms.length; j++) {
    System.out.print((j > 0 ? ", " : "") + wordForms[j]);
    }//* /
    if (wordForms.length > 0) {
    hyper = wordForms[0];
    }
    //synsets[0].
    //System.out.println(": " + synsets[i].getDefinition());
    //}

    } else {
    return "entity";
    }

    return hyper;
    }
     *
     */
}
