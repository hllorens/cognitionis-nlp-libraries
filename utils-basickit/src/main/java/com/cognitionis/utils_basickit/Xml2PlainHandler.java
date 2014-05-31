/*
 *
 * This SAX parser treats a basic NLP corpus document in XML
 *
 * The basic elements are a main tag (i.e., TimeML, then a DOC tag that may contain DOC_?(ID|NO) tag
 * And basic <TEXT> and <s> tags for wrapping the complete text and each segmented sentence
 *
 * If <TEXT>        -> Only consider text in <TEXT>
 * If <s>           -> Only consider text in <s>
 * If <TEXT> && <s> -> Only consider text in <s> in <TEXT>
 *
 * Priorize <TEXT> if there is <TEXT> anything even <s> outside <TEXT> are not considered
 *
 * Removes all the tags and returns the text between the text (including sentence segmentation if present).
 * Replaces &amp; like xml scapes by the original characters
 * 
 */
package com.cognitionis.utils_basickit;

import com.cognitionis.utils_basickit.SAXReader;
import java.util.*;
import org.xml.sax.*;

/**
 * @author Hector Llorens
 * @since 2011
 */

public class Xml2PlainHandler extends SAXReader {

    boolean inText = false, inSentence = false, hasText = false, hasSentence = false, inDocid = false;
//    StringBuilder docidStrb;
    StringBuilder textStrb;
    StringBuilder sentenceStrb;
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
        if (root_tag == null) {
            root_tag = tag;
            //System.out.println("roottag: "+tag);
        }

        /*        if (tag.toUpperCase().matches("DOC_?(ID|NO)")) {
        docidStrb = new StringBuilder();
        inDocid = true;
        }
         */
        if (tag.equalsIgnoreCase("text")) {
            if (!hasText) {
                textStrb = null;
                textStrb = new StringBuilder();
                hasSentence = false;
                sentences = null;
            }
            hasText = true;
            inText = true;
        }

        if (tag.equalsIgnoreCase("s")) {
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
                    if (inSentence) {
                        sentenceStrb.append(c, start, length);
                    }
                } else {
                    textStrb.append(c, start, length);
                }
            }
        } else {
            if (hasSentence) {
                if (inSentence) {
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

        /*        if (tag.toUpperCase().matches("DOC_?(ID|NO)") && inDocid) {
        inDocid = false;
        docid =
        docidStrb.toString().trim();
        docidStrb = null; // For the garbage collector - free memory
        System.out.println("Docid: (" + docid + ")");
        }
         */
        if (tag.equalsIgnoreCase("text") && inText) {
            inText = false;
            if (!hasSentence) {
                strBuilder = textStrb;
            } else {
                int n = sentences.size() - 1;
                for (int i = 0; i <
                        n; i++) {
                    strBuilder.append(sentences.get(i) + "\n");
                }

                strBuilder.append(sentences.get(n));
                sentences = null;
            }

            textStrb = null; // For the garbage collector - free memory
        }

        if (tag.equalsIgnoreCase("s") && inSentence) {
            inSentence = false;
            sentences.add(sentenceStrb.toString().replaceAll("(\n|\r)", "").replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
            sentenceStrb = null; // For the garbage collector - free memory
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

                    strBuilder.append(textStrb.toString().replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
                    textStrb = null; // For the garbage collector - free memory
                }
            }

        }


    }
    //es pot gastar start i enddocument...a.
}




