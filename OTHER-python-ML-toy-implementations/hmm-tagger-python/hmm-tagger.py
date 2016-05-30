#! /usr/bin/python

import sys
from collections import defaultdict
import math

"""
Based on https://www.coursera.org/course/pgm and Alexander Rush reference solution pdf
TRAINING consists on preparing a model of frequencies (emisions and transitions)
Also with __RARE__ or other classes

TEST HMM
Tests a model with HMM

TEST BASELINE
Only with emission probabilities (max-likelihood estimate, probabilities, no context)
Calculate emission probabilities and write them to input.baseline-model

Run -h for usage details.
"""


def get_word_counts(annotated_input):
    """
	Normal function that counts words
    """
    temp_word_count=defaultdict(int)
    l = annotated_input.readline()
    while l:
        line = l.strip() # trim
        if line: # Nonempty line
            fields = line.split(" ")
            word = " ".join(fields[:-1]) # This would work for multiple parameters or words with more than 1 token (multi-words) THIS IS A BIT STUPPID FOR THE CASE OF MULTIWORDS because they should be preprocessed
            temp_word_count[word] += 1
        l = annotated_input.readline()
    return temp_word_count
    
def get_token_tag_lazy(annotated_input):
    """
    Lazy token-tag reader (iterator: one instance per iteration).
	Elements are tuples (word,tag). Blank lines, indicating sentence boundaries return (None, None).
    """
    l = annotated_input.readline()
    while l:
        line = l.strip() # trim
        if line: # Nonempty line
            fields = line.split(" ")
            tag = fields[-1] # MULTIPLE TAGGING: #phrase_tag = fields[-2] #Unused #pos_tag = fields[-3] #Unused
            word = " ".join(fields[:-1]) # This would work for multiple parameters or words with more than 1 token
            yield word, tag
        else: # Empty line
            yield (None, None)                        
        l = annotated_input.readline()
def get_sentence_lazy(token_tag_iterator):
    """
    Return an iterator object that yields one sentence at a time. Sentences are represented as lists of (word, tag) tuples.
    """
    current_sentence = [] #Buffer for the current sentence
    for l in token_tag_iterator:        
            if l==(None, None):
                if current_sentence:  #Reached the end of a sentence
                    yield current_sentence
                    current_sentence = [] #Reset buffer
                else: # Got empty input stream
                    sys.stderr.write("WARNING: Got empty input file/stream.\n")
                    raise StopIteration
            else:
                current_sentence.append(l) #Add token to the buffer

    if current_sentence: # If the last line was blank, we're done
        yield current_sentence  #Otherwise when there is no more token in the stream return the last sentence.		
def get_ngrams(sent_iterator, n):
    """
    Get a generator that returns n-grams over the entire corpus,respecting sentence boundaries and inserting boundary tokens.
    Sent_iterator is a generator object whose elements are lists of tokens.
    """
    for sent in sent_iterator:
         #Add boundary symbols to the sentence
         sentence_with_boundaries = (n-1) * [(None, "*")]   # e.g, for trigrams insert n-1 (2) [none,*] tuples 
         sentence_with_boundaries.extend(sent) # then add the sentence
         sentence_with_boundaries.append((None, "STOP")) # then add stop
         #Then extract n-grams
         ngrams = (tuple(sentence_with_boundaries[i:i+n]) for i in xrange(len(sentence_with_boundaries)-n+1)) # gets n-tuples from 0 to the last complete element
         for n_gram in ngrams: #Return one n-gram at a time
            yield n_gram     		
            
def read_sentences(handle):
 "Lazily read sentences from a handle." # good practice to comment here for func doc
 sentence = []
 for l in handle:
   if l.strip():
     sentence.append(l.strip())
   else:
     yield sentence
     sentence = []
   
def print_tags(sentence, tagging):
 "Print out a tagged sentence."
 print "\n".join([w + " " + t
 for w, t in zip(sentence, tagging)])
		
class BaselineTagger(object):
    """
    Stores BaselineTagger
    """
    def __init__(self,n=3):
        assert n>=2, "Expecting n>=2. Otherwise start/end symbols cannot be used, no context"
        self.n = n
        self.low_freq_approach="classes"
        self.word_tag_emission_counts = defaultdict(int)
        self.tagonly_ngram_counts = [defaultdict(int) for i in xrange(self.n)]
        self.all_tags = set() # TAGS is what we call states in the HMM (why? because they are less than words and it is more efficient/lightweight to do it this way)
        self.word_counts = defaultdict(int) # added to pre-process input to do the _RARE_ replacement before counting tag n-grams and emission #we could add the word count in the n-gram count and then, replace only in the emission probs but let's do it the easy way
		
    def replace_wordtag(self, wordtag):
      """Returns the wordtag or its replacement."""
      if self.word_counts.get(wordtag[0], 0.0) < 5:
        if self.low_freq_approach != "classes":
            return "_RARE_",wordtag[1]   # if not found then it returns 0 by default
        else:
            if all([c.isdigit() for c in wordtag[0]]): return "_DIGITS"+str(len(wordtag[0]))+"_",wordtag[1] # updated to all (instead of any) digits and number of 
            elif any([c.isdigit() for c in wordtag[0]]): return "_SOME-DIGITS_",wordtag[1]
            elif all([c.isupper() for c in wordtag[0]]): return "_ALLCAP_",wordtag[1]
            elif wordtag[0][-1].isupper(): return "_LASTCAP_",wordtag[1]
            else: return "_RARE_",wordtag[1]   
      else: return wordtag	  

    def replace_word(self, word):
      """Returns the word or its replacement."""
      if self.word_counts.get(word, 0.0) < 5: 
        if self.low_freq_approach != "classes":
            return "_RARE_"   # if not found then it returns 0 by default
        else:
            if all([c.isdigit() for c in word]): return "_DIGITS"+str(len(word))+"_" 
            elif any([c.isdigit() for c in word]): return "_SOME-DIGITS_"
            elif all([c.isupper() for c in word]): return "_ALLCAP_"
            elif word[-1].isupper(): return "_LASTCAP_"
            else: return "_RARE_"            
      else: return word	  
    
    def train(self, annotated_input,n=3,approach="classes"):
        """
        Count n-gram frequencies and emission probabilities from a corpus file.
        """
        self.n=n
        self.low_freq_approach=approach
        self.word_counts=get_word_counts(annotated_input)
        annotated_input.seek(0) # if the file is small storing it in a string is ok, but if the file is huge that might use a lot of memory
        ngram_iterator = get_ngrams(get_sentence_lazy(get_token_tag_lazy(annotated_input)), self.n)

		# ngram is e.g., [[a,o] [b,i-tag] [c,o]]
        for ngram in ngram_iterator: # this returns a n-gram of words and tags (e.g., trigram)            
            assert len(ngram) == self.n, "ngram in stream is %i, expected %i" % (len(ngram, self.n)) #Sanity check: n-gram we get from the corpus stream needs to have the right length
            tagsonly_ngram = tuple([tag for word, tag in ngram]) #retrieve only the tags            
			
	    #Count tag 2-grams..n-grams of one insgle ngram (Unigrams are not included because we don't want to include [None, STOP] case
            for i in xrange(2, self.n+1): 
                self.tagonly_ngram_counts[i-1][tagsonly_ngram[-i:]] += 1 # automatically if it does not exist will create with 1. Example tagonly_ngram_counts [2][tag1 tag2 tag3] #-i means from -3 to the end of the ngram
			# Example in a trigram model, in the first ngram [ * * tag ], this would count [* tag]=1 and [* * tag]=1 but not [* *] which is needed for tag-trigram parameter estimation
            
			# Count 1-grams and emission. This is separated to exclude STOP in tag 1-gramms and emission (tuples)
            if ngram[-1][0] is not None: # If this is not STOP the last word in a sentence.
                self.tagonly_ngram_counts[0][tagsonly_ngram[-1:]] += 1 # count 1-gram
                self.word_tag_emission_counts[self.replace_wordtag(ngram[-1])] += 1 # and emission frequencies, replace the word by RARE in the word tag touple if freq < 5

            # If we are at the begging of the sentence add an (n-1)-gram of sentence start symbols (e.g., for trigrams add [* *])
            if ngram[-2][0] is None: # this is the first n-gram in a sentence becaue the first is of the form n-1 * "*" and first real word [[none,*], [none,*],...[none,*],[word,tag]] so ngram[-2] is [[none,*], [none,*],...>>>[none,*]<<<,[word,tag]] if we are in the first tuple of a sentence
                self.tagonly_ngram_counts [self.n - 2][tuple((self.n - 1) * ["*"])] += 1
				
    def write_model(self, output, printngrams=[1,2,3]):
        """
        Writes counts to the output file object.      Format:
        """
        # BEFORE ALL WRITE SOME CONFIG
        output.write("NGRAM-LEVEL CONFIG %i\n" % (self.n))
        output.write("HANDLE-LOW-FREQ CONFIG %s\n" % (self.low_freq_approach))
        output.write("- ENDCONFIG -\n") # alternative solution to avoid rewinding
    
        # First write counts for emissions
        for word, tag in self.word_tag_emission_counts:            
            output.write("%i WORDTAG %s %s\n" % (self.word_tag_emission_counts[(word, tag)], tag, word))
        # Then write counts for all ngrams
        for n in printngrams:            
            for ngram in self.tagonly_ngram_counts[n-1]:
                ngramstr = " ".join(ngram)
                output.write("%i %i-GRAM %s\n" %(self.tagonly_ngram_counts[n-1][ngram], n, ngramstr))
        # Then write common words list (those modeled in the HMM - emission probs)
        for word in self.word_counts:
           temp_count=self.word_counts[word]
           if temp_count > 4:
             output.write("%i WORD %s\n" % (temp_count,word))
			
				
    def read_model(self, input_model):
        self.n = 3 # default
        #config first
        #DEPRECATED-rewind strategy for config, does not work. last_position=0   #last_position=input_model.tell(); # needed to rewind when config ends #input_model.seek(last_position)
        for line in input_model:
            parts = line.strip().split(" ")
            if parts[1] == "CONFIG":
                if parts[0] == "NGRAM-LEVEL":
                    self.n=int(parts[2])
                elif parts[0] == "HANDLE-LOW-FREQ":
                    self.low_freq_approach=parts[2]
            else:
                break
        #print self.low_freq_approach
        # then counts     
        self.word_tag_emission_counts = defaultdict(int)
        self.tagonly_ngram_counts = [defaultdict(int) for i in xrange(self.n)]
        self.all_tags = set()
        self.word_counts = defaultdict(int)
        for line in input_model:
            #print line
            parts = line.strip().split(" ")
            count = float(parts[0])
            if parts[1] == "WORDTAG":
                tag = parts[2]
                word = parts[3]
                self.word_tag_emission_counts[(word, tag)] = count # TODO TODO TODO: REPLACE ALLOVER word,tag per tag,word ... this is a mess
                self.all_tags.add(tag)
            elif parts[1].endswith("GRAM"):
                n = int(parts[1].replace("-GRAM",""))
                #if n > self.n: self.n=n
                if n>1:
                 ngram = tuple(parts[2:])
                else:
                 ngram = parts[2]
                self.tagonly_ngram_counts[n-1][ngram] = count
            elif parts[1] == "WORD":				
                self.word_counts[parts[2]]=parts[0]

    def trigram_prob(self, trigram):
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
      print
	  

def usage():
    print """
	hmm-tagger.py   train (or train-no-class)   training_file    n-gram_size
	hmm-tagger.py   test (or test-baseline)    model_file       testing_file   // n-gram size is obtained from the model_file
    """

if __name__ == "__main__":
       
    if  len(sys.argv)<3 or( len(sys.argv)!=4 and sys.argv[1]=="train") or (len(sys.argv)!=4 and (sys.argv[1]=="test" or sys.argv[1]=="test-baseline")):
        usage()
        sys.exit(1)
        
    #sys.stderr.write("argv=%d" % len(sys.argv))
		
    try:
        input = file(sys.argv[2],"r")
    except IOError:
        sys.stderr.write("ERROR: Cannot read inputfile %s.\n" % arg)
        sys.exit(1)
    
    if sys.argv[1] == "train-no-class":
     mytagger = BaselineTagger()
     mytagger.train(input,int(sys.argv[3]),"no-class-just-rare")
     mytagger.write_model(sys.stdout)

    if sys.argv[1] == "train":
     mytagger = BaselineTagger()
     mytagger.train(input,int(sys.argv[3]))
     mytagger.write_model(sys.stdout)


    if sys.argv[1] == "test-baseline": # using CLASS or just RARE depends on the training data
      try:
        test_file = file(sys.argv[3],"r")
      except IOError:
        sys.stderr.write("ERROR: Cannot read testfile %s.\n" % arg)
        sys.exit(1)
      mytagger = BaselineTagger()
      mytagger.read_model(input)
      mytagger.test(test_file,"baseline")
    
    if sys.argv[1] == "test":
      try:
        test_file = file(sys.argv[3],"r")
      except IOError:
        sys.stderr.write("ERROR: Cannot read testfile %s.\n" % arg)
        sys.exit(1)
      mytagger = BaselineTagger()
      mytagger.read_model(input)
      mytagger.test(test_file,"trigram")    
    
