package com.cognitionis.nlp_lang_models;


import java.io.*;
import java.util.*;
import com.cognitionis.utils_basickit.*;
import static com.cognitionis.utils_basickit.FileUtils.URL_exists;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 *
 * This is an implementation of the famous Tenkle Text Categorization algorithm
 * based on character n-grams
 * Best known as TextCat
 *
 */
public class TextCategorizer {

    private final static int MIN_WORDS_4_CATEGORIZE = 5;
    private final static String DEFAULT_CATEGORY = "en"; // English

    private String conf_file_path = "/resources/lang_models/text_categorization/";
    private String conf_file_name = "indoeuropean.conf";
    //private String conf_file_path = "indoeuropean.conf";
    private ArrayList<TextCategorizerFingerprint> categories = new ArrayList();

    public TextCategorizer() {
        loadFingerprints();
    }

    public TextCategorizer(String conf_file_path) {
        this.conf_file_path = conf_file_path;
        loadFingerprints();
    }

    private void loadFingerprints() {
        this.categories.clear();
        try {
            // For our beloved Windows
            String extra = ""; // TODO check if this is really needed
            if (File.separator.equals("\\")) {
                extra = "\\";
            }
            String app_path = FileUtils.getApplicationPath(TextCategorizer.class);
            
        if (!URL_exists(app_path+conf_file_path)) { // Check for external resoucre (outside classes)
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("look outside classes");
            }
            app_path=app_path.replaceAll(extra + File.separator + "classes", ""); // see if we need \\ for windows
        }            

            try (BufferedReader reader = new BufferedReader(new FileReader(new File(app_path+this.conf_file_path+this.conf_file_name)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] line_arr = line.split("\\s+");
                    if(line_arr.length > 0){
                        if(line_arr.length != 2){
                            throw new Exception("Malformed TextCategorizer configuration file.\n\tMust contain one fingerprint file path per line.");
                        }
                        TextCategorizerFingerprint fp=new TextCategorizerFingerprint(app_path+this.conf_file_path+line_arr[0],line_arr[1]);
                        categories.add(fp);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found ("+this.getClass().getSimpleName()+"):\n\t" + e.toString() + "\n");
            if(System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")){e.printStackTrace(System.err);}
        }
    }


	/**
	 * categorizes only a certain amount of characters in the text. recommended
	 * when categorizing large texts in order to increase performance.
	 *
	 * @param text text to be analyzed
	 * @param limit number of characters to be analyzed
	 * @return the category name given in the configuration file
	 */
	public String categorize(String text, int limit) {
		if(limit > (text.length()-1)) {
                    limit=text.length()-1;
		}
		return this.categorize(text.substring(0,limit));
	}

  	public String categorize(String text) {
		if(text.length() < MIN_WORDS_4_CATEGORIZE) {
			return DEFAULT_CATEGORY;
		}
		TextCategorizerFingerprint fp = new TextCategorizerFingerprint();
		fp.create(text.toLowerCase());
		fp.categorize(categories);

		return fp.getCategory();
	}
}
