package com.cognitionis.utils_basickit;

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.*;
//import javax.activation.MimetypesFileTypeMap;
//import java.nio.charset.CharsetDecoder;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class FileUtils {

    // TODO Separar FileUtils de NLPFileUtils (tot aixo de els formats de NLP...)
    // The working directory is the location in the file system from where the java command was invoked.
    // Windows: absolute and canonical are the same (link from the root c:\ o / for Unix)
    // Linux: When there are symbolic links could be 2 absolute paths (one to the link and another to the real file)
    //        However a unique canonical path (the real one to the file)
    // System.getProperty("user.dir"); .. user.home, user, ... interesting
    /*for(Entry<Object,Object> entry : System.getProperties().entrySet()) {
    String name = entry.getValue().toString();
    if(name.contains("CorpusInterface")) {
    System.out.println(entry.getKey());
    }
    }*/
    // MIME Types, not so accurate nor useful by the moment...
    //String mime=new MimetypesFileTypeMap().getContentType(f);
    // File encodings (charsets)
    public static String UTF8 = "UTF-8";
    public static String ASCII = "ASCII";
    public static String ISO88591 = "ISO-8859-1";
    // NLP File formats
    public static String Treebank = "Treebank";
    public static String XML = "XML";
    public static String Pipes = "Pipes";
    public static String Plain = "Plain";
    public static String Tab = "Tab";
    // Important File Paths
    public static String ApplicationPath = null;
    public static String NLPFiles_descr_path = "program-data/default-NLPFiles-descriptions/";
    public static FileFilter onlyFilesFilter = new FileFilter() {

        @Override
        public boolean accept(File file) {
            return (!file.isDirectory() && !file.getName().matches("\\..*"));
        }
    };
    public static FileFilter onlyDirsNonAuxDirs = new FileFilter() {

        @Override
        public boolean accept(File file) {
            return (!file.isFile() && !file.getName().matches(".*([-_]features|\\.d)"));
        }
    };
    public static Comparator fileSizeAsc = new Comparator() {

        @Override
        public int compare(Object f1, Object f2) {
            if (((File) f1).length() < ((File) f2).length()) {
                return -1;
            } else if (((File) f1).length() > ((File) f2).length()) {
                return 1;
            } else {
                return 0;
            }
        }
    };
    public static Comparator fileSizeDesc = new Comparator() {

        @Override
        public int compare(Object f1, Object f2) {
            if (((File) f1).length() > ((File) f2).length()) {
                return -1;
            } else if (((File) f1).length() < ((File) f2).length()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /*
     * The path in which the application is executed from
     */
    public static String getExecutionPath() {
        try {
            // Old way: String executionPath=(new File (".")).getCanonicalPath();
            return System.getProperty("user.dir");
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\tApplication path not found: " + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return "";
        }
    }

    /*
     * The path in which the application code/class/jar/executable is located
     */
    public static String getApplicationPath() {
        try {
            String innerpath = FileUtils.ApplicationPath;
            if (FileUtils.ApplicationPath == null) {
                //innerpath = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                //innerpath = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();

            URL url = FileUtils.class.getProtectionDomain().getCodeSource().getLocation();
            innerpath = (new File(URLDecoder.decode(url.getFile(), "UTF-8"))).getAbsolutePath();

                //System.out.println("dfdf "+innerpath);
                if (innerpath.contains(".jar")) {
                    innerpath = innerpath.substring(0, innerpath.lastIndexOf(File.separator) + 1);
                    if (innerpath.endsWith(File.separator+"lib"+File.separator)) {
                        innerpath = innerpath.substring(0, innerpath.length() - 4);
                    }
                    // When you release the final dist you must use a name different than "dist"
                    // NOOOO YOU MUST say to ant (build.xml) that lib and program-data must be copied to dist
                    /*if (innerpath.endsWith("dist"+File.separator)) {
                        innerpath = innerpath.substring(0, innerpath.length() - 5);
                    }*/
                } else {
                    /*if (innerpath.endsWith("/lib/")) {
                    innerpath = innerpath.substring(0, innerpath.length() - 4);
                    }*/
                    if (innerpath.endsWith("build"+File.separator+"classes"+File.separator)) {
                        innerpath = innerpath.substring(0, innerpath.length() - 14);
                    }
                }
                if (innerpath.matches(".*Utils_BasicKit.*")) {
                // Hack for debugging (executed from NetBeans... and project added, not compiled)
                innerpath = "/home/hector/Dropbox/JApplications/TIMEE/";
                //System.err.println("utils_bk FileUtils.java. This must be solved in some other way.");
                //System.exit(0);
                }
                FileUtils.ApplicationPath = innerpath;
            }
            return innerpath;
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\tApplication path not found: " + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return "";
        }
    }

    public static void copyFileUtil(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     *
     * Concatenates/appends/merges two files out+in in out.
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFileUtilappend(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out, true).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public static boolean deleteRecursively(File f) throws Exception {
        if (f.isDirectory()) {
            File[] aFiles = f.listFiles();
            for (File oFileCur : aFiles) {
                deleteRecursively(new File(oFileCur.getAbsolutePath()));
            }
        }
        return f.delete();
    }

    public static String renameTo(String filename, String rename_re, String rename_replacement) {
        try {
            String newname = filename;
            newname = newname.replaceAll(rename_re, rename_replacement);
            File f = new File(filename);
            File f2 = new File(newname);
            if (f2.exists()) {
                if (!f2.delete()) {
                    throw new Exception("renameTo: destination file (" + f2 + ") exists and cannot be deleted or overwritten.");
                }
            }
            Boolean correct = f.renameTo(new File(newname));
            if (correct) {
                return newname;
            } else {
                throw new Exception("Error renaming");
            }
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
    }

    public static String getNLPFormat(File f) {
        String nlp_format = Plain;

        Map<String, Integer> scores = new HashMap<String, Integer>();
        scores.put(Treebank, 0);
        scores.put(XML, 0);
        scores.put(Pipes, 0);
        scores.put(Plain, 1);
        scores.put(Tab, 0);

        try {

            BufferedReader reader = new BufferedReader(new FileReader(f));
            try {
                String line = null;
                int linen = 0;

                while ((line = reader.readLine()) != null && linen < 50) {
                    linen++; //System.getProperty("line.separator")

                    if (scores.get(Pipes) != -1 && line.length() > 0) {
                        if (line.contains("|")) {
                            scores.put(Pipes, scores.get(Pipes) + 1);
                        } else {
                            scores.put(Pipes, -1);
                        }
                    }
                    if (scores.get(Tab) != -1 && line.length() > 0) {
                        if (line.contains("\t")) {
                            scores.put(Tab, scores.get(Tab) + 1);
                        } else {
                            scores.put(Tab, -1);
                        }
                    }
                    if (scores.get(Treebank) != -1 && line.matches("\\s*[^\\(\\s].*")) {
                        scores.put(Treebank, -1);
                    }
                    if (scores.get(Treebank) != -1 && line.matches("\\s*\\(.*")) {
                        scores.put(Treebank, scores.get(Treebank) + 1);
                    }
                    if (line.matches(".*<(/)?[^>]+>.*")) {
                        scores.put(XML, scores.get(XML) + 1);
                    }
                    //System.out.println("pipes("+ scores.get(Pipes) + ")\ttb("+ scores.get(Treebank) + ")\txml("+ scores.get(XML) + ")\t" +linen + " (" + line.length() +"):" + line);
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            int highscore = 0;
            for (Entry<String, Integer> score : scores.entrySet()) {
                if (score.getValue() > highscore) {
                    highscore = score.getValue();
                    nlp_format = score.getKey();
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\t" + e.toString() + ":" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        }


        return nlp_format;
    }

    public static String getExtension(String filename) {
        try {
            if (!filename.contains(".")) {
                throw new Exception("\tFileUtils: " + filename + " does not have extension");
            }
            return filename.substring(filename.lastIndexOf('.') + 1, filename.length());
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return "";
        }
    }

    public static String getFolder(String f) throws IOException {
        return f.substring(0, f.lastIndexOf('/') + 1);
    }

    // FOR PATH USE JAVA .getParent() function
    // NOT NEEDED:   public static String getPath(String filename)
    public static Boolean checkEncoding(File f, String encodings) {
        String encoding = getEncoding(f);
        if (encoding.equals(ASCII) || encodings.toUpperCase().contains(encoding)) {
            return true;
        }
        return false;
    }

    public static Boolean checkEncoding(String encoding, String encodings) {
        if (encoding.equals(ASCII) || encodings.toUpperCase().contains(encoding)) {
            return true;
        }
        return false;
    }

    /**
     * Returns if a file is ascii, iso or utf
     * @param bytes
     * @return
     */
    public static String getEncoding(byte[] bytes) {
        int comptador = 0;

        int i = 0;
        short b = '\0';
        boolean ascii = true;

        for (i = 0; i < bytes.length; i++) {
            b = (short) (0xFF & bytes[i]);

            if (comptador > 0) {
                if ((b >> 6) != 0x2) {
                    return ISO88591;
                } else {
                    comptador--;
                }
            } else if ((b & 0x80) > 0) {
                ascii = false;
                if ((b >> 5) == 0x6) {
                    comptador = 1;
                } else if ((b >> 4) == 0xE) {
                    comptador = 2;
                } else if ((b >> 3) == 0x1E) {
                    comptador = 3;
                } else {
                    return ISO88591;
                }
            }
        }

        return ((ascii) ? ASCII : UTF8);
    }

    /**
     * Returns if a file is ascii, iso or utf
     * @param f
     * @return
     */
    public static String getEncoding(File f) {
        return getEncoding(FileUtils.file2bytes(f));
    }

    public static String bytes2file(byte[] b, String filename) {
        String ret = null;

        try {
            File f = new File(filename);
            if (f.exists()) {
                throw new IOException("\n\n\tError: file " + filename + " already exists");
            }
            f = null;
            ret = filename;
            FileOutputStream fw = new FileOutputStream(ret);
            if (b != null) {
                fw.write(b);
            }
            fw.close();
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }

        return ret;
    }

    public static byte[] file2bytes(File f) {
        try {
            FileInputStream fi = new FileInputStream(f);

            ArrayList<Byte> bytes = new ArrayList<Byte>();
            byte c;
            int rc;
            while ((rc = fi.read()) > -1) {
                c = (byte) (rc & 255);
                bytes.add(c);
            }
            fi.close();

            int bsize = bytes.size();
            byte[] rbytes = new byte[bsize];

            for (int i = 0; i < bsize; i++) {
                rbytes[i] = ((Byte) bytes.get(i)).byteValue();
            }

            return rbytes;
        } catch (Exception e) {
            System.err.println("Errors found (FileUtils):\n\t" + e.getMessage() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
        }

        return null;
    }

//    static public String getTempDir() {
//        String ret = System.getProperty("java.io.tmpdir");
//
//        if (!(ret.endsWith("/") || ret.endsWith("\\"))) {
//            ret = ret + System.getProperty("file.separator");
//        }
//
//        return ret;
//    }
    // charsetName can be null to use the default charset.
    public static String readFileAsString(String fileName, String charsetName)
            throws java.io.IOException {
        java.io.InputStream is = new java.io.FileInputStream(fileName);
        try {
            final int bufsize = 4096;
            int available = is.available();
            byte data[] = new byte[available < bufsize ? bufsize : available];
            int used = 0;
            while (true) {
                if (data.length - used < bufsize) {
                    byte newData[] = new byte[data.length << 1];
                    System.arraycopy(data, 0, newData, 0, used);
                    data = newData;
                }
                int got = is.read(data, used, data.length - used);
                if (got <= 0) {
                    break;
                }
                used += got;
            }
            return charsetName != null ? new String(data, 0, used, charsetName)
                    : new String(data, 0, used);
        } finally {
            is.close();
        }
    }

    public static String readFileAsString(String fileName, String charsetName, int size)
            throws java.io.IOException {
        FileInputStream f = new FileInputStream(fileName);
        try {
            int readsize = (int) new File(fileName).length();
            if (size < readsize) {
                readsize = size;
            }
            byte[] buffer = new byte[readsize];
            f.read(buffer);
            return charsetName != null ? new String(buffer, charsetName)
                    : new String(buffer);
        } finally {
            f.close();
        }
    }

    /*
     * This now forces UTF-8
     */
    public static void writeFileFromString(String str, String fileName) throws java.io.IOException {
        //BufferedWriter of = new BufferedWriter(new FileWriter(fileName));
        OutputStreamWriter of = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(fileName))),Charset.forName("UTF8"));

        try {
            of.write(str);
        } finally {
            of.close();
        }
    }

}
