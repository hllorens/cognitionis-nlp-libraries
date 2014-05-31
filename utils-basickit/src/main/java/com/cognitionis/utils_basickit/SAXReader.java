package com.cognitionis.utils_basickit;

import java.io.*;
import java.nio.charset.Charset;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

/**
 * @author Hector Llorens
 * @since 2011
 */

public abstract class SAXReader extends DefaultHandler {

    /**
     * Set initial capacity of 10 KB (more or less the average text size of WSJ)
     */
    protected final int kStringBuilder = 10240;
    protected StringBuilder strBuilder = null;
    protected org.xml.sax.XMLReader reader = null;

    /**
     * load the reader with current hanler
     */
    protected void enableReader() {
        try {
            reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            reader.setContentHandler(this);
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }
    }

    protected void disableReader() {
        reader = null;
    }

    /**
     * Gets a String consisting of the parsed text
     *
     * @param xmlfile
     * @return String of the parsed text
     */
    public String getText(String xmlfile) {
        enableReader();
        strBuilder = new StringBuilder(kStringBuilder);
        try {
            reader.parse(xmlfile);
            disableReader();
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return "";
        }

        return strBuilder.toString();
    }

    /**
     * Creates a file (plainfle) with the parsing of an XML file (xmlfile)
     *
     * @param xmlfile
     * @param plainfile
     * @return boolean: true if success, false if errors found
     */
    public boolean saveFile(String xmlfile, String plainfile) {
        enableReader();
        strBuilder = new StringBuilder(kStringBuilder);
        try {
            reader.parse(xmlfile);
            disableReader();
            OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(plainfile))),Charset.forName("UTF8"));
            // to force utf8 new OutputStreamWriter( new FileOutputStream("Your_file_fullpath" ),Charset.forName("UTF8"))

            try {
                writer.append(strBuilder);
                writer.write("\n"); // avoids errors adding EOF

                // TO CHECK may be more efficient
                //int sbl=strBuilder.length();
                //   for (int i = 0; i < sbl; i++) {
                //           writer.write(strBuilder.charAt(i));
                //   }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }

            return false;
        }
        return true;
    }
}
