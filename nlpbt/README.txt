Natural Language Processing Basic Tools.

Goal: Provide basic NLP tools in such a way that they are simple and lighweight enogh for the user to learn how they work.

Features: 
	- Basic: few lines of code, few classes, well structured 
	- Framework: like NLTK but minimal and lighweight 
	- Learning: rather than production 
	- Minimal: Something really simple that works than a professional library
	- Well documented: no misetery

Tools: 
- Tokenizer (Rule-based, Treebank-like)
- Sentence splitter (Rule-based, Treebank-like)
- Baseline taggers:
    - Memory: most frequent tag 
    - HMM: trigram (with configurable low-freq classes) 
    - CRF - MaxEnt? - LogLinear? 
    
Basic ideas of NLP Tool classes:
- Allow processing of big files (>1MB) with lazy line-by-line reading/writing:
	- INPUT: String or File
	- OUTPUT: Stdout or File
	- LAZY LIBRARY OUTPUT: String (for processing a chunk, sentence, phrase or the minimal processing unit)

Some may argue that as a library some object output would be useful (e.g., tokenize output String[]) but in our opinion that breaks scalability.
If you aim to make big NLP pipelines there is already UIMA, NLTK, etc... in nlpbt we only provide stdout lazy output to allow pipelining.


 




