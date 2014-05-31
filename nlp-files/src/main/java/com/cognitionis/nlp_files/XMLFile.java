package com.cognitionis.nlp_files;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.*;
import com.cognitionis.utils_basickit.Xml2PlainHandler;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class XMLFile extends NLPFile {

    /*
     * XML description files
     */
    //private String dtd;  DEPRECATED use XSD
    private String xsd;


    public XMLFile(String filename, String xsd_path) {
        super(filename);        // dtd = null; DEPRECATED use XSD
        xsd = xsd_path;
    }

    public Boolean isWellFormatted() {
        try {
            if (super.getFile()==null) {
                throw new Exception("No file loaded in NLPFile object");
            }
            if (xsd!=null) {
//                File dtd_file = new File(FileUtils.getApplicationPath()+FileUtils.NLPFiles_descr_path + extension + ".dtd");
//                if (dtd_file.exists() && dtd_file.isFile()) {
//                    this.dtd = dtd_file.getCanonicalPath();
//                }
                File xsd_file = new File(xsd);
                if (xsd_file.exists() && xsd_file.isFile()) {
                    this.xsd = xsd_file.getCanonicalPath();
                }
            }


            if (!SAXParserCheck(this.f)) {
                System.err.println("Malformed XML (SAX)");
                return false;
            }

            /*
             DOM parser: Deprecated
             SAX is faster and for the moment does the JOB
             if (!DOMParserCheck(this.f)) {
                System.err.println("Malformed XML (DOM)");
                return false;
            } else {
                System.err.println("Correct DOM");
            }*/

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }

        return true;
    }

    public Boolean SAXParserCheck(File f) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // For DTD (only internal...) DEPRECATED use XSD
            //factory.setValidating(false); 
            //factory.setNamespaceAware(true);

            // For XSD
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            if (xsd != null) {
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println("Validating "+f.getName()+" with xsd (" + xsd + ")");
                }
                SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                factory.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(xsd)}));
            }
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            reader.setErrorHandler(new SimpleErrorHandler());
            reader.parse(f.getCanonicalPath());
            return true;

        } catch (Exception e) {
            System.err.println("Errors found (XMLFile):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
    }


    public Boolean DOMParserCheck(File f) {
        try {

            //  Create a Xerces DOM Parser
            DOMParser parser = new DOMParser();

            //  Turn Validation on
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

            //  Register Error Handler
            parser.setErrorHandler(new SimpleErrorHandler());

            //  Parse the Document and traverse the DOM

            parser.parse(f.getCanonicalPath());
            // Document document = parser.getDocument();
            // traverse (document);



            //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // For XSD
            //factory.setValidating(false);
            //factory.setNamespaceAware(true);

            /*if (xsd != null) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println("Validating "+f.getName()+" with xsd (" + xsd + ")");
            }
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            factory.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(xsd)}));
            }*/

            //DocumentBuilder builder = factory.newDocumentBuilder();

            //builder.setErrorHandler(new SimpleErrorHandler());
            //Document document = builder.parse(new InputSource(f.getCanonicalPath()));


            return true;

        } catch (Exception e) {
            System.err.println("Errors found (XMLFile):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
    }



    @Override
    public String toPlain(String filename) {
        try {
           
            Xml2PlainHandler xml2plain = new Xml2PlainHandler();
            //xml2plain.getText(this.f.getCanonicalPath());
            xml2plain.saveFile(this.f.getCanonicalPath(), filename);
            return filename;
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
    }




private  class SimpleErrorHandler implements ErrorHandler {

    public void warning(SAXParseException e) throws SAXException {
        System.err.println("Warning:" +e.getMessage());
        if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            e.printStackTrace(System.err);
        }
    }

    public void error(SAXParseException e) throws SAXException {
        //System.err.println(e.getMessage());
        if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            e.printStackTrace(System.err);
        }
        throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        //System.err.println(e.getMessage());
        if (System.getProperty("DEBUG")!=null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            e.printStackTrace(System.err);
        }
        throw e;
    }
}

}
