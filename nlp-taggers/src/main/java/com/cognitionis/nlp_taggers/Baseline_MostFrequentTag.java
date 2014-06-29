package com.cognitionis.nlp_taggers;

import com.cognitionis.nlp_files.TokenizedFile;
import com.cognitionis.utils_basickit.*;
import com.cognitionis.nlp_files.NgramHandler;
import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Baseline_MostFrequentTag {

    private int ngram_size;
    private String token_classes_config;
    private JSONObject token_classes;
    private HashMap<String, Integer> word_counts; // to pre-process input and do _RARE_ replacement before counting ngrams
    private HashMap<String, Integer> word_tag_emission_counts; //DEFINITELY NOT USE NON OBJECTS String [], use just a String (much simpler) or ArrayList (less convenient)
    private ArrayList<HashMap<String, Integer>> tag_ngram_counts; //better store it as string, decide after I implement test...
    private HashSet<String> tagset; // all tags
    private int token_rare_classes_threshold; // not used by default

    public Baseline_MostFrequentTag() throws Exception { // default values
        word_tag_emission_counts = new HashMap<>();
        tag_ngram_counts = new ArrayList<>();
        tagset = new HashSet<>();
        token_classes_config = "default";
        token_rare_classes_threshold = 0;
    }

    public HashSet tagSet() {
        return tagset;
    }

    private String replace_token_class(String token) {
        this.token_rare_classes_threshold = Integer.parseInt((String) token_classes.get("rare_threshold"));
        JSONObject always_classes = (JSONObject) token_classes.get("always_classes");
        for (String token_class : new HashSet<String>(always_classes.keySet())) {
            if (token.matches((String) always_classes.get(token_class))) {
                return token_class;
            }
        }

        if (word_counts.get(token) == null || word_counts.get(token) < this.token_rare_classes_threshold) {
            JSONObject rare_classes = (JSONObject) token_classes.get("rare_classes");
            for (String token_class : new HashSet<String>(rare_classes.keySet())) {
                if (token.matches((String) rare_classes.get(token_class))) {
                    return token_class;
                }
            }
            return (String) token_classes.get("rare_class");
        }

        return token;


    }

    public void train_model(String tf, int n, String classes_file) throws Exception {
        TokenizedFile training_file = new TokenizedFile(tf, " ");
        ngram_size = n;
        token_classes_config = classes_file;

        // For our beloved Windows
        String extra = "";
        if (File.separator.equals("\\")) {
            extra = "\\";
        }
        String app_path = FileUtils.getApplicationPath(Baseline_MostFrequentTag.class).replaceAll(extra + File.separator + "classes", "");
        String res_path = app_path + File.separator + "resources" + File.separator + "taggers" + File.separator + "token_classes" + File.separator;

        JSONParser parser = new JSONParser();
        token_classes = (JSONObject) parser.parse(new FileReader(res_path + token_classes_config + ".json"));

        for (int i = 0; i < n; i++) {
            tag_ngram_counts.add(new HashMap<String, Integer>());
        }
        word_counts = training_file.getTokenCount();

        NgramHandler ngram_iterator = new NgramHandler(training_file, n);

        for (List<String[]> ngram : ngram_iterator) {
            // Get tags ngram
            ArrayList<String> tags_ngram = new ArrayList<>();
            for (String[] item : ngram) {
                tags_ngram.add(item[training_file.getLastDescColumn()]);
            }
            //System.err.println(ngram.toString());
            // Count tags 2-gram to n-gram: A more intuitive way would be ArrayList<String> tagNgram=new ArrayList<>(); and start adding there. But you can still add the one-liner...
            String tagNgram = tags_ngram.get(0);
            for (int i = 1; i < n; i++) { // sublist exludes the last element   
                tagNgram += " " + tags_ngram.get(i); // if it is an object it is equal by reference... better use as tring
                //tag_ngram_counts.get(i-1).put(new ArrayList<>(tags_ngram.subList(n-i, n)), tag_ngram_counts.get(i-1).get( new ArrayList<>(tags_ngram.subList(n-i, n)))==null ? 1 : tag_ngram_counts.get(i-1).get(new ArrayList<>(tags_ngram.subList(n-i, n)))+1);
                tag_ngram_counts.get(i).put(tagNgram, tag_ngram_counts.get(i).get(tagNgram) == null ? 1 : tag_ngram_counts.get(i).get(tagNgram) + 1);
            }

            // Count 1-grams and emission. This is separated to exclude STOP in tag 1-gramms and emission (tuples)
            String[] unigram = ngram.get(ngram.size() - 1);
            tagNgram = unigram[unigram.length - 1];
            if (!(unigram[0]).equals("_none_")) { // If this is not STOP the last word in a sentence.
                unigram[0] = replace_token_class(unigram[0]);
                tag_ngram_counts.get(0).put(tagNgram, tag_ngram_counts.get(0).get(tagNgram) == null ? 1 : tag_ngram_counts.get(0).get(tagNgram) + 1);
                word_tag_emission_counts.put(unigram[0] + " " + unigram[1], word_tag_emission_counts.get(unigram[0] + " " + unigram[1]) == null ? 1 : word_tag_emission_counts.get(unigram[0] + " " + unigram[1]) + 1);
            }

            // If we are at the begging of the sentence add an (n-1)-gram of sentence start symbols (e.g., for trigrams add [* *])
            if (ngram.size() > 1 && ngram.get(ngram_size - 2)[0].equals("_none_")) {
                tagNgram = "*";
                for (int i = 1; i < ngram_size - 1; i++) {
                    tagNgram += " *";
                }
                tag_ngram_counts.get(ngram_size - 2).put(tagNgram, tag_ngram_counts.get(ngram_size - 2).get(tagNgram) == null ? 1 : tag_ngram_counts.get(ngram_size - 2).get(tagNgram) + 1);
            }
        }
    }

    /**
     * Write a readable version of the model
     *
     * @param out_file if null it writes to stdout
     * @throws Exception
     */
    public void write_model(File out_file) throws Exception {
        write_model(out_file, false);
    }

    /**
     * Write a readable version of the model
     *
     * @param out_file if null it writes to stdout
     * @param sort_by_freq if true the output model is sorted by freq
     * @throws Exception
     */
    public void write_model(File out_file, boolean sort_by_freq) throws Exception {
        BufferedWriter out;
        if (out_file != null) {
            System.err.println("Writing module to file " + out_file);
            out = new BufferedWriter(new FileWriter(out_file));
        } else {
            System.err.println("Writing model to stdout (no file specified)");
            out = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        try {
            out.write("NGRAM-LEVEL CONFIG " + ngram_size + "\n");
            out.write("HANDLE-LOW-FREQ CONFIG " + token_classes_config + "\n");
            out.write("- ENDCONFIG -\n");

            // Write counts for emissions
            if (sort_by_freq) {
                List<Map.Entry<String, Integer>> word_tag_emission_counts_list = new LinkedList<>(word_tag_emission_counts.entrySet());
                Collections.sort(word_tag_emission_counts_list, new DescStringIntMapEntryListComparator());
                for (Map.Entry<String, Integer> entry : word_tag_emission_counts_list) {
                    out.write(entry.getValue() + " WORDTAG " + entry.getKey() + "\n");
                }
            } else {
                for (String wordtag : this.word_tag_emission_counts.keySet()) {
                    out.write(word_tag_emission_counts.get(wordtag) + " WORDTAG " + wordtag + "\n");
                }
            }
            // Write counts for all ngrams
            for (int n = 0; n < ngram_size; n++) {
                if (sort_by_freq) {
                    List<Map.Entry<String, Integer>> tag_ngram_counts_list = new LinkedList<>(tag_ngram_counts.get(n).entrySet());
                    Collections.sort(tag_ngram_counts_list, new DescStringIntMapEntryListComparator());
                    for (Map.Entry<String, Integer> entry : tag_ngram_counts_list) {
                        out.write(entry.getValue() + " " + (n + 1) + "-GRAM " + entry.getKey() + "\n");
                    }
                } else {
                    for (String ngram : tag_ngram_counts.get(n).keySet()) {
                        out.write(tag_ngram_counts.get(n).get(ngram) + " " + (n + 1) + "-GRAM " + ngram + "\n");
                    }
                }
            }
            // Write common words list (those modeled in the HMM - emission probs)
            if (sort_by_freq) {
                List<Map.Entry<String, Integer>> word_counts_list = new LinkedList<>(word_counts.entrySet());
                Collections.sort(word_counts_list, new DescStringIntMapEntryListComparator());
                for (Map.Entry<String, Integer> entry : word_counts_list) {
                    out.write(entry.getValue() + " WORD " + entry.getKey() + "\n");
                }
            } else {
                for (String word : word_counts.keySet()) {
                    int temp_count = word_counts.get(word);
                    if (temp_count >= this.token_rare_classes_threshold) {
                        out.write(temp_count + " WORD " + word + "\n");
                    }
                }
            }
        } finally {
            if (out_file == null) {
                out.flush();
            } else {
                out.close();
            }
        }
    }

    public void read_model(String input_model) {
        ngram_size = 3; // default
        File file = new File(input_model);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("Should be a file (not directory, etc): " + file);
            }

            String line;
            int linen = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                linen++;
                if (line.length() != 0) {
                    String[] linearr = line.split(" ");
                    switch (linearr[1]) {
                        case "CONFIG":
                            switch (linearr[0]) {
                                case "NGRAM-LEVEL":
                                    ngram_size = Integer.parseInt(linearr[2]);
                                    for (int i = 0; i < ngram_size; i++) {
                                        tag_ngram_counts.add(new HashMap<String, Integer>());
                                    }
                                    break;
                                case "TOKEN-CLASSES":
                                    token_classes_config = linearr[2];
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case "WORDTAG":
                            word_tag_emission_counts.put(linearr[2] + " " + linearr[3], Integer.parseInt(linearr[0]));
                            tagset.add(linearr[3]);
                            break;
                        case "WORD":
                            word_counts.put(linearr[2], Integer.parseInt(linearr[0]));
                            break;
                        default:
                            if (linearr[1].endsWith("-GRAM")) {
                                int n = Integer.parseInt(linearr[1].replaceAll("-GRAM", ""));
                                String ngram = "";
                                if (n > ngram_size) {
                                    n = ngram_size;
                                }
                                if (n > 0) {
                                    ngram = line.substring(line.lastIndexOf("-GRAM"));
                                }
                                tag_ngram_counts.get(n - 1).put(ngram, Integer.parseInt(linearr[0]));
                            }
                            break;
                    }
                }
            }






        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }

    }
}
