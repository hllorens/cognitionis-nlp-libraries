
package com.cognitionis.nlp_taggers;

public class HMM extends Tagger{
    
}

   /* def trigram_prob(self, trigram):
      "Return the probability of the trigram given the prefix bigram."
      bigram = trigram[:-1]
      #print trigram
      #print bigram
      #print self.tagonly_ngram_counts[1]
      trigram_freq=self.tagonly_ngram_counts[2].get(trigram, 0.0)
      if trigram_freq==0: return 0.0  # avoid 0/0 because if trigram prob is 0 the result would be 0 already and there is no guarantee that the bigram exists
      else: return trigram_freq / self.tagonly_ngram_counts[1][bigram]                
                
    def emission_prob(self, word, tag): #e(x | y)
      "Return the probability of the tag emitting the word."
      if tag in ["*", "STOP"] : return 0.0
      new_word = self.replace_word(word)
      #print self.tagonly_ngram_counts[0] #print self.word_tag_emission_counts
      #sys.stderr.write("%s %s e=%f.\n" % (new_word,tag,self.word_tag_emission_counts.get((new_word, tag), 0.0) / self.tagonly_ngram_counts[0][tag]))
      #print self.word_counts #print self.word_tag_emission_counts.get((new_word, tag), 0.0)
      #print self.tagonly_ngram_counts[0][tag]
      return self.word_tag_emission_counts.get((new_word, tag), 0.0) / self.tagonly_ngram_counts[0][tag]

    def argmax(self,in_list): #"Take a list of pairs (item, score), return the argmax."
     return max(in_list, key = lambda x: x[1]) # x[1] is the key (score) to be compared

    def unigram(self, sentence):
     def e(x, u): return self.emission_prob(x, u)  # e(x | y) "This is just added for using e for emission_prob"
     #print sentence
     #print self.all_tags
     return [self.argmax([(y, e(x, y)) for y in self.all_tags])[0] for x in sentence] # Compute y* = argmax_checkall_y e(x | y) for all x.
    
    def viterbi(self, sentence):
        "Run the Viterbi algorithm to find the best tagging."
        # Define the variables to be the same as in the class slides.
        n = len(sentence)
        # The tag sets K_k.
        def K(k):
            if k in (-1, 0): return ["*"]
            else: return self.all_tags
        # Pad the sentence so that x[1] is the first word.
        x = [""] + sentence
        y = [""] * (n + 1)
        def q(w, u, v): return self.trigram_prob((u, v, w))
        def e(x, u): return self.emission_prob(x, u)
        # The Viterbi algorithm.
        pi = {} # Create and initialize the lookup table
        pi[0, "*", "*"] = 1.0
        bp = {} # backpointer
        # Run the main loop
        for k in range(1, n + 1):
            for u in K(k - 1):
                for v in K(k):
                    bp[k, u, v], pi[k, u, v] = self.argmax([(w, pi[k - 1, w, u] * q(v, w, u) * e(x[k], v)) for w in K(k - 2)])
        # Follow the back pointers in the table (pi)
        (y[n - 1], y[n]), score = self.argmax([((u,v), pi[n, u, v] * q("STOP", u, v)) for u in K(n - 1) for v in K(n)])
        for k in range(n - 2, 0, -1): y[k] = bp[k + 2, y[k + 1], y[k + 2]]
        y[0] = "*"
        scores = [pi[i, y[i - 1], y[i]] for i in range(1, n)]
        return y[1:n + 1], scores + [score]
    
    
    
    def test(self, test_file, mode="trigram"):
     for sentence in read_sentences(test_file):
      if mode == "trigram":
        tagging, scores = self.viterbi(sentence)
        #print scores (could print the whole table used to decide)
      elif mode == "baseline":
        tagging = self.unigram(sentence)
      print_tags(sentence, tagging)
      print    */