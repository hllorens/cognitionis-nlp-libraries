package com.cognitionis.nlp_files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NgramHandler implements Iterable<List<String[]>> {

    private final TokenizedFile tokfile;
    private final int ngram_size;
    // included_fields which fields are included (default: all)
    private int[] included_fields; // 0==skip, 1==include
    private final BufferedReader reader;
    private final List<String[]> previous;
    private final String[] pre_ngram, post_ngram;

    public NgramHandler(TokenizedFile tf, int n) throws Exception {
        this(tf, n, null);
    }

    // in the future allow make n-grams of different cardinality (number of
    // tabs) and also select which tabs...
    /**
     * Build an n-gram handler/iterator
     *
     * @param tf
     *            TokenizedFile one word per line and optional features
     *            (columns)
     * @param n
     *            ngram size
     * @param f
     *            included_fields (array of int of the same size of num columns
     *            where 0==skip and 1==include). By default all cols are
     *            included
     * @throws Exception
     */
    public NgramHandler(TokenizedFile tf, int n, int[] f) throws Exception {
        tokfile = tf;
        ngram_size = n;
        reader = new BufferedReader(new FileReader(tf.getFile()));
        previous = new ArrayList<>();
        // Build pre and post ngrams to add them when needed------------
        pre_ngram = new String[tf.getNumFields()];
        post_ngram = new String[tf.getNumFields()];
        for (int i = 0; i < tf.getLastDescColumn(); i++) {
            pre_ngram[i] = post_ngram[i] = "_none_";
        }
        pre_ngram[tf.getLastDescColumn()] = "*";
        post_ngram[tf.getLastDescColumn()] = "STOP";
        // --------------------------------------------------------------
        if (f == null) {
            included_fields = new int[tf.getNumFields()];
            for (int i = 0; i < included_fields.length; i++) {
                included_fields[i] = 1;
            }
        } else {
            included_fields = f;
            if (included_fields.length > tf.getNumFields()) {
                throw new IOException("Selected numfields (" + included_fields.length
                        + ") is greater than the number of fields in tok file ("
                        + tf.getNumFields() + ")");
            }
        }
        // in python is easier to concatenate generators/iterators however here
        // it makes more sense to read a file word by word and add stop if
        // needed.
        /*
         * (hi ha que llegir per frase i hi ha que afegir els special tags * *
         * STOP) (last field) i en la resta de fields "-"
         */
        if (n < 1) {
            throw new Exception("Error: Ngram size n must be > 0");
        }
        /*
         * if(n>1){ for
         *
         *
         * System.err.println("Error: Ngram size greater than input string length"
         * ); }
         */
    }

    @Override
    public Iterator<List<String[]>> iterator() {
        return new Iterator<List<String[]>>() {
            @Override
            public boolean hasNext() {
                try {
                    reader.mark(1);
                    // if previous isn't empty process last line and add "STOP"
                    if (reader.read() < 0 && previous.isEmpty()) {
                        return false;
                    }
                    reader.reset();
                    return true;
                } catch (final IOException e) {
                    return false;
                }
            }

            @Override
            public List<String[]> next() {
                final List<String[]> ngram = new ArrayList<>();
                try {
                    String line = reader.readLine();
                    if (line != null)
                        line = line.trim();
                    else
                        line = "";

                    // Clear all the leading newlines
                    if (previous.isEmpty() && line.isEmpty()) {
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                break;
                            }
                        }
                    }

                    // Clear all the intermediate newlines and add the last
                    // ngram of the sentence STOP
                    if (line.isEmpty()) {
                        if (previous.isEmpty()) {
                            throw new Exception("Empty file: " + tokfile.getFile());
                        }
                        reader.mark(1000); // in case we want to go back, 1000
                                           // is the buffer
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                reader.reset();
                                break;
                            }
                        }
                        ngram.addAll(previous);
                        ngram.add(post_ngram);
                        previous.clear();
                    } else {
                        if (previous.isEmpty()) { // this is a first token in a
                                                  // sentence
                            for (int i = 1; i < ngram_size; i++) {
                                previous.add(pre_ngram);
                            }
                        }
                        final String[] linearr = line
                                .split(tokfile.getFieldSeparatorRE());
                        ngram.addAll(previous);
                        ngram.add(linearr);
                        previous.remove(0);
                        previous.add(linearr);
                    }
                    return ngram;
                } catch (final Exception e) {
                    if (System.getProperty("DEBUG") != null
                            && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        e.printStackTrace(System.err);
                        System.exit(1);
                    } else {
                        System.err.println("Errors found ("
                                + this.getClass().getSimpleName() + "):\n\t"
                                + e.toString() + "\n");
                    }
                    return null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            reader.close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Get n-grams from a space separated string str
     *
     * @param str
     * @param n
     * @return
     */
    public static List<String> getNgrams(String str, int n) {
        final List<String> ngrams = new ArrayList<>();
        if (n < 1) {
            System.err.println("Error: Ngram size n must be > 0");
            return ngrams;
        }
        if (n > str.split(" ").length) {
            System.err.println("Error: Ngram size greater than input string length");
            return ngrams;
        }

        final String[] words = str.split(" ");
        for (int ngram_start = 0; ngram_start < words.length - n + 1; ngram_start++) {
            final StringBuilder ngram = new StringBuilder();
            final int ngram_end = ngram_start + n;
            for (int ngram_word_index = ngram_start; ngram_word_index < ngram_end; ngram_word_index++) {
                ngram.append(ngram_word_index > ngram_start ? " " : "")
                        .append(words[ngram_word_index]).toString();
            }
            ngrams.add(ngram.toString());
        }
        return ngrams;
    }
    /*
     * See the link in the chrome browser to see to java easy solutions...
     * normal (only good for small files.) on-demand (iterator, generator, lazy)
     * implement and document this on doc
     *
     * public Ngram get_ngram(int n){ lo que hace el amigo es que lee frase a
     * frase ngram_start cada frase lee parejas "token tag" }
     *
     * ngram_iterator =
     * get_ngrams(get_sentence_lazy(get_token_tag_lazy(annotated_input)),
     * self.n)
     *
     *
     * luego desde fuera se puede hacer for ngram in ngram_iterator:
     *
     *
     * n-gram is the number you have selected, e.g., 3 --> [[a,o]
     * [b,ngram_start-tag] [c,o]] then you can have an algorithm to do the
     * calculation of smaller n-grams...
     *
     * ngram_start ya se ponen en HashMaps de contadores para luego generar el
     * fichero .model
     */
}
