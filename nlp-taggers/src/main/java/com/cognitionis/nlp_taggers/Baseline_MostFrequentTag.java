package com.cognitionis.nlp_taggers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cognitionis.nlp_files.NgramHandler;
import com.cognitionis.nlp_files.TokenizedFile;
import com.cognitionis.utils_basickit.DescStringIntMapEntryListComparator;
import com.cognitionis.utils_basickit.FileUtils;

public class Baseline_MostFrequentTag extends Tagger {

    public Baseline_MostFrequentTag() throws Exception { // default values
        word_tag_emission_counts = new HashMap<>();
        tag_ngram_counts = new ArrayList<>();
        tagset = new HashSet<>();
        token_classes_config = "default";
        token_rare_classes_threshold = 0;
    }

    /**
     * Train the tagger, perform counts and replace classes
     *
     * @param tf
     * @param n
     * @param classes_file
     * @throws Exception
     */
    public void train_model(String tf, int n, String classes_file)
            throws Exception {
        final TokenizedFile training_file = new TokenizedFile(tf, " ");

        // Initialize model
        ngram_size = n;
        // Classes handling, TODO check arguments with IF

        // For our beloved Windows
        String extra = "";
        if (File.separator.equals("\\")) {
            System.err.println("NOTE: Using windows separator");
            extra = "\\";
        }
        final String app_path = FileUtils.getApplicationPath(
                Baseline_MostFrequentTag.class).replaceAll(
                extra + File.separator + "classes", "");
        final String res_path = app_path + File.separator + "resources"
                + File.separator + "taggers" + File.separator + "token_classes"
                + File.separator;
        final JSONParser parser = new JSONParser();
        token_classes_config = classes_file;
        token_classes = (JSONObject) parser.parse(new FileReader(res_path
                + token_classes_config + ".json"));
        token_rare_classes_threshold = Integer.parseInt((String) token_classes
                .get("rare_threshold"));
        rare_classes = (JSONObject) token_classes.get("rare_classes");
        always_classes = (JSONObject) token_classes.get("always_classes");

        if (System.getProperty("DEBUG") != null
                && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println("rare threshold: "
                    + token_rare_classes_threshold);
            System.err.println("rare classes: " + rare_classes);
            System.err.println("always classes: " + always_classes);
        }

        for (int i = 0; i < n; i++) {
            tag_ngram_counts.add(new HashMap<String, Integer>());
        }

        // TODO: No classes replacement, is it necessary??
        word_counts = training_file.getTokenCount();

        // Get ngrams of size n (e.g., n=3) for all columns (e.g., [[word1,
        // tag1],[word2, tag2],[word3, tag3]])
        final NgramHandler ngram_iterator = new NgramHandler(training_file, n);

        // Get only tag ngrams and count (e.g., [O B-TIMEX3 O] -> 5)
        for (final List<String[]> ngram : ngram_iterator) {
            final ArrayList<String> tags_ngram = new ArrayList<>();
            for (final String[] item : ngram) {
                // equivalent to item.length - 1
                tags_ngram.add(item[training_file.getLastDescColumn()]);
            }

            String tagNgram = tags_ngram.get(n - 1); // last tag in the ngram
            int tagNgramSize = 1;
            // Count tags 2-gram to n-gram (excludes unigrams to exclude STOP)
            // (Unigrams are not included because we don't want to include
            // [None, STOP] case)
            for (int i = n - 2; i >= 0; i--) {
                tagNgram = tags_ngram.get(i) + " " + tagNgram;
                tagNgramSize++;
                tag_ngram_counts.get(tagNgramSize - 1)
                        .put(tagNgram,
                                tag_ngram_counts.get(tagNgramSize - 1).get(
                                        tagNgram) == null ? 1
                                        : tag_ngram_counts
                                                .get(tagNgramSize - 1).get(
                                                        tagNgram) + 1);
            }

            // Since the loop above will exclude the first min-size ngram
            // If we are at the begging of the sentence add an (n-1)-gram of
            // * (sentence start symbols) e.g., for trigrams add [* *]
            if (ngram.size() > 1
                    && ngram.get(ngram_size - 2)[0].equals("_none_")) {
                tagNgram = "*";
                for (int i = 1; i < ngram_size - 1; i++) {
                    tagNgram += " *";
                }
                tag_ngram_counts.get(ngram_size - 2)
                        .put(tagNgram,
                                tag_ngram_counts.get(ngram_size - 2).get(
                                        tagNgram) == null ? 1
                                        : tag_ngram_counts.get(ngram_size - 2)
                                                .get(tagNgram) + 1);
            }

            // Count 1-grams and emission. This is separated to exclude STOP in
            // tag 1-gramms and emission (tuples), and do class replacement
            final String[] unigram = ngram.get(ngram.size() - 1); // first
                                                                  // "real"
                                                                  // [word,tag]
                                                                  // excluding *
                                                                  // *
            tagNgram = unigram[training_file.getLastDescColumn()]; // first tag
                                                                   // (unigram)
            // Only * and STOP have _none_ as word, and
            // since * is excluded if ==_none_ it must be STOP (last word in a
            // sentence)
            if (!(unigram[0]).equals("_none_")) {
                // tag counts
                tag_ngram_counts.get(0).put(
                        tagNgram,
                        tag_ngram_counts.get(0).get(tagNgram) == null ? 1
                                : tag_ngram_counts.get(0).get(tagNgram) + 1);
                // word-tag count (emission)
                unigram[0] = replace_token_class(unigram[0]);
                word_tag_emission_counts.put(
                        unigram[0] + " " + unigram[1],
                        word_tag_emission_counts.get(unigram[0] + " "
                                + unigram[1]) == null ? 1
                                : word_tag_emission_counts.get(unigram[0] + " "
                                        + unigram[1]) + 1);
            }
        }
    }

    /**
     * Write a readable version of the model
     *
     * @param out_file
     *            if null it writes to stdout
     * @throws Exception
     */
    public void write_model(File out_file) throws Exception {
        write_model(out_file, false);
    }

    /**
     * Write a readable version of the model (maybe json would make it easier
     * [more standard] to read)
     *
     * @param out_file
     *            if null it writes to stdout
     * @param sort_by_freq
     *            if true the output model is sorted by freq
     * @throws Exception
     */
    public void write_model(File out_file, boolean sort_by_freq)
            throws Exception {
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
            // TODO: should include the whole class config to make it
            // independent (auto-contained)
            out.write("HANDLE-LOW-FREQ CONFIG " + token_classes_config + "\n");
            out.write("- ENDCONFIG -\n");

            // Write counts for emissions
            if (sort_by_freq) {
                final List<Map.Entry<String, Integer>> word_tag_emission_counts_list = new LinkedList<>(
                        word_tag_emission_counts.entrySet());
                Collections.sort(word_tag_emission_counts_list,
                        new DescStringIntMapEntryListComparator());
                for (final Map.Entry<String, Integer> entry : word_tag_emission_counts_list) {
                    out.write(entry.getValue() + " WORDTAG " + entry.getKey()
                            + "\n");
                }
            } else {
                for (final String wordtag : this.word_tag_emission_counts
                        .keySet()) {
                    out.write(word_tag_emission_counts.get(wordtag)
                            + " WORDTAG " + wordtag + "\n");
                }
            }
            // Write counts for all ngrams
            for (int n = 0; n < ngram_size; n++) {
                if (sort_by_freq) {
                    final List<Map.Entry<String, Integer>> tag_ngram_counts_list = new LinkedList<>(
                            tag_ngram_counts.get(n).entrySet());
                    Collections.sort(tag_ngram_counts_list,
                            new DescStringIntMapEntryListComparator());
                    for (final Map.Entry<String, Integer> entry : tag_ngram_counts_list) {
                        out.write(entry.getValue() + " " + (n + 1) + "-GRAM "
                                + entry.getKey() + "\n");
                    }
                } else {
                    for (final String ngram : tag_ngram_counts.get(n).keySet()) {
                        out.write(tag_ngram_counts.get(n).get(ngram) + " "
                                + (n + 1) + "-GRAM " + ngram + "\n");
                    }
                }
            }
            // Write common words list (those modeled in the HMM - emission
            // probs)
            if (sort_by_freq) {
                final List<Map.Entry<String, Integer>> word_counts_list = new LinkedList<>(
                        word_counts.entrySet());
                Collections.sort(word_counts_list,
                        new DescStringIntMapEntryListComparator());
                for (final Map.Entry<String, Integer> entry : word_counts_list) {
                    final int temp_count = entry.getValue();
                    if (temp_count >= token_rare_classes_threshold) {
                        out.write(entry.getValue() + " WORD " + entry.getKey()
                                + "\n");
                    }
                }
            } else {
                for (final String word : word_counts.keySet()) {
                    final int temp_count = word_counts.get(word);
                    if (temp_count >= token_rare_classes_threshold) {
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

    /**
     * Read the model Using JSON could make it easier to parse, although a bit
     * bigger in size...
     *
     * @param input_model
     */
    public void read_model(String input_model) {
        ngram_size = 3; // default
        final File file = new File(input_model);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException(
                        "Should be a file (not directory, etc): " + file);
            }

            String line;
            int linen = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                linen++;
                if (line.length() != 0) {
                    final String[] linearr = line.split(" ");
                    switch (linearr[1]) {
                    case "CONFIG":
                        switch (linearr[0]) {
                        case "NGRAM-LEVEL":
                            ngram_size = Integer.parseInt(linearr[2]);
                            for (int i = 0; i < ngram_size; i++) {
                                tag_ngram_counts
                                        .add(new HashMap<String, Integer>());
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
                        word_tag_emission_counts.put(linearr[2] + " "
                                + linearr[3], Integer.parseInt(linearr[0]));
                        tagset.add(linearr[3]);
                        break;
                    case "WORD":
                        word_counts.put(linearr[2],
                                Integer.parseInt(linearr[0]));
                        break;
                    default:
                        if (linearr[1].endsWith("-GRAM")) {
                            int n = Integer.parseInt(linearr[1].replaceAll(
                                    "-GRAM", ""));
                            String ngram = "";
                            if (n > ngram_size) {
                                n = ngram_size;
                            }
                            if (n > 0) {
                                ngram = line.substring(line
                                        .lastIndexOf("-GRAM"));
                            }
                            tag_ngram_counts.get(n - 1).put(ngram,
                                    Integer.parseInt(linearr[0]));
                        }
                        break;
                    }
                }
            }

        } catch (final Exception e) {
            System.err.println("Errors found ("
                    + this.getClass().getSimpleName() + "):\n\t" + e.toString()
                    + "\n");
            if (System.getProperty("DEBUG") != null
                    && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }

    }
}
