package com.cognitionis.wiki_basickit;

import java.util.*;
import org.xml.sax.*;
import com.cognitionis.utils_basickit.SAXReader;

/**
 * @author Hector Llorens
 * @since 2011
 */

public class WikiHtml2PlainHandler extends SAXReader {

    boolean inText = false, inSection = false, hasText=false, hasSentence=false, inSentence=false;
    boolean inH=false; // section titles
    boolean inSup=false; // references / citations
    int inTable=0;
    //    StringBuilder docidStrb;
    StringBuilder textStrb;
    StringBuilder sentenceStrb;
    StringBuilder H2Strb;

//    String docid;
    String root_tag = null;
    ArrayList<String> sentences;

    @Override
    public void startElement(final String uri, final String localName,
        final String tag, final Attributes attributes) throws SAXException {
        //System.err.println("found "+tag);
        if (textStrb == null) {
            textStrb = new StringBuilder();
        }
        //System.out.println("tag: "+tag);

        if (root_tag == null) {
            root_tag = tag;
        }
        if (tag.equalsIgnoreCase("table")) {
            inTable++;
        }
        if (tag.equalsIgnoreCase("sup")) {
            inSup=true;
        }
        if (tag.matches("h[1234]")) {
            H2Strb=null;
            H2Strb=new StringBuilder();
            inH=true;
        }
        if (tag.equalsIgnoreCase("html")) {
            if (!hasText) {
                textStrb = null;
                textStrb = new StringBuilder();
                hasSentence = false;
                sentences = null;
            }
            hasText = true;
            inText = true;
        }

        if (tag.equalsIgnoreCase("p")) {
            if ((hasText && inText) || !hasText) {
                if (!hasSentence) {
                    hasSentence = true;
                    sentences = null;
                    sentences = new ArrayList<String>();
                    textStrb = null; // For the garbage collector - free memory
                }
                // reload sentenceStrb
                sentenceStrb = null; // For the garbage collector - free memory
                sentenceStrb = new StringBuilder();
                inSentence = true;

            }
        }

    }


    /*
     * Only text, excluding all tags
     */
    @Override
    public void characters(final char[] c, final int start, final int length) {
        //System.err.print(c);
/*        if (inDocid) {
        docidStrb.append(c, start, length);
        }
         */
        if (hasText) {
            if (inText) {
                if (hasSentence) {
                    if (inSentence && inTable==0 && !inSup) {
                        sentenceStrb.append(c, start, length);
                    }
                    if (inH && inTable==0 && !inSup) {
                        H2Strb.append(c, start, length);
                    }
                } else {
                    textStrb.append(c, start, length);
                }
            }
        } else {
            if (hasSentence) {
                if (inSentence && inTable==0 && !inSup) {
                    sentenceStrb.append(c, start, length);
                }
            } else {
                textStrb.append(c, start, length);
            }
        }




    }

    @Override
    public void endElement(final String uri,
            final String localName,
            final String tag)
            throws SAXException {

        if (tag.equalsIgnoreCase("html") && inText) {
            inText = false;
            if (!hasSentence) {
                System.out.println("no sentences");
                strBuilder = textStrb;
            } else {
                int n = sentences.size() - 1;
                for (int i = 0; i <
                        n; i++) {
                    strBuilder.append(sentences.get(i) + "\n\n");
                }

                strBuilder.append(sentences.get(n));
                sentences = null;
            }

            textStrb = null; // For the garbage collector - free memory
        }

        if (tag.equalsIgnoreCase("p") && inSentence) {
            inSentence = false;
            if(sentenceStrb.length()>0){
                 String temp=sentenceStrb.toString().replaceAll("(\n|\r|\\p{javaSpaceChar})", " ").replaceAll("\\s+", " ").replaceAll("(—|–)", " - ").replaceAll("’", "'").trim();
                 //temp=java.text.Normalizer.normalize(temp, java.text.Normalizer.Form.NFD);
                 //sentences.add(temp.replaceAll("[^\\p{ASCII}]",""));
                 sentences.add(temp);
            }
            sentenceStrb = null; // For the garbage collector - free memory
        }

        if (tag.equalsIgnoreCase("table") && inTable>0) {
            inTable--;
        }

        if (tag.equalsIgnoreCase("sup") && inSup) {
            inSup=false;
        }
        if (tag.matches("h[1234]")) {
            inH=false;
            if(H2Strb.length()>0 && !H2Strb.toString().replaceAll("(\n|\r|\\s*\\[\\s*edit(ar)?\\s*\\]\\s*)", "").matches("(Media|Animated maps|See also|Notes|References|External links)")){
                 String temp=H2Strb.toString().replaceAll("(\n|\r|\\s*\\[\\s*edit(ar)?\\s*\\]\\s*|\t)", " ").replaceAll("\\s+", " ").replaceAll("(—|–)", " - ").replaceAll("’", "'").trim();
                 // NOT ALWAYS WORK THAT BELOW NFD + ASCII
                 //temp=java.text.Normalizer.normalize(temp, java.text.Normalizer.Form.NFD);
                 //sentences.add(temp.replaceAll("[^\\p{ASCII}]","")+".");
                 sentences.add(temp+".");
            }
            H2Strb = null; // For the garbage collector - free memory
        }


        // ho puc fer quan s'acaba el document si no tenia text...
        if (tag.equalsIgnoreCase(root_tag)) {
            if (!hasText) {
                if (hasSentence) {
                    int n = sentences.size() - 1;
                    for (int i = 0; i <
                            n; i++) {
                        strBuilder.append(sentences.get(i) + "\n");
                    }

                    strBuilder.append(sentences.get(n));
                    sentences = null;

                } else {

                    strBuilder = textStrb;
                    textStrb = null; // For the garbage collector - free memory
                }
            }

        }


    }
    //es pot gastar start i enddocument...a.
}




