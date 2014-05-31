package com.cognitionis.nlp_files;

import com.cognitionis.utils_basickit.FileUtils;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;

/**
 * @author Héctor Llorens
 * @since 2011
 */
public abstract class NLPFile {

    public static enum Subclasses {

        PipesFile, PlainFile, TreebankFile, XMLFile;
    }
    protected File f;
    protected URL url;
    protected InputStream inputstream;
    protected String language;
    protected String encoding;
    protected String extension;
    protected Boolean isWellFormatted;

    /**
     * Creates the object and loads a file into it
     * @param filename
     */
    public NLPFile(String filename) {
        try {
            filename = FileUtils.ensureURL(filename);
            if (!FileUtils.URL_exists(filename)) {
                throw new FileNotFoundException("File does not exist: " + filename);
            }
            // this strategy implies creating a temp file in the filesystem instead of directly reading it as input stream
            // TODO: the ideal situation would be that the parent just checks for file existence and the child creates the InputStream and reads and loads the file
            // But it might not be the case for plain or XML files... will see
            url = new URL(filename);
            if (url.getProtocol().equals("file")) {
                f = new File(url.toURI());
                this.inputstream=new FileInputStream(f);
            }
            if (url.getProtocol().equals("jar")) {
                //f = new File(connection.getJarFileURL().toURI());
                f = new File(filename); //url.getPath() 
                //System.out.println("getting it "+filename+" from jar: "+connection.getJarFileURL().toURI());
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                inputstream = connection.getInputStream();
            }
            this.encoding = FileUtils.getEncoding(inputstream);
            this.extension = FileUtils.getExtension(filename);
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            this.f = null;
        }        
    }
    

    /**
     * Basic format check
     *
     * @return String: the canonical path to the created file
     */
    public abstract Boolean isWellFormatted();

    /**
     * Creates a plain file from any type of NLPFile.
     *
     * @return String: the canonical path to the created file
     */
    public abstract String toPlain(String filename);

    public File getFile() {
        return this.f;
    }

    public void setEncoding(String e) {
        this.encoding = e;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getExtension() {
        return this.extension;
    }

    public void overrideExtension(String newext) {
        this.extension = newext;
    }
}
