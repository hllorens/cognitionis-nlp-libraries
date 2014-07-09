
package com.cognitionis.wiki_basickit;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class Wiki_bk {

    public static String wiki2txt(String title) {
        return wiki2txt(title, "en", "ascii");
    }

    public static String wiki2txt(String title, String lang) {
        String charset="ascii";
        if (!lang.equalsIgnoreCase("en")){
            charset="utf8";
        }
        return wiki2txt(title, "en", charset);
    }    
    
    public static String wiki2txt(String title, String lang, String charset) {
        try {
            title = title.trim().replaceAll(" ", "_");
            if (title.matches("(http://)?(en|es).wikipedia.*")) {
                if (title.matches("(http://)?(en|es)\\.wikipedia\\.org/wiki/.+")) {
                    lang = title.replaceFirst("(http://)?(en|es)\\..*", "$2");
                    title = title.replaceFirst(".*wiki/(.+)", "$1");
                } else {
                    throw new MalformedURLException("Malformed URL: " + title);
                }
            }

            if (lang == null) {
                lang = "en";
            }

            File f = new File(title.replaceAll("/", "-") + "-" + lang + ".txt");
            if (!f.exists()) {
                URL url;
                String line;
                String input = "";
                    url = new URL("http://" + lang + ".wikipedia.org/w/index.php?title=" + title + "&printable=yes");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(title.replaceAll("/", "-") + ".cleanhtml")));

                    try {
                        while ((line = reader.readLine()) != null) {
                            input += line + " ";   //System.err.println(line);
                        }

                        // IMP: faster than write only the body in the bucle like comented below
                        // clean html for parsers: remove headers, remove link, img and other tags
                        input = input.replaceFirst(".*<body[^>]*>(.*)</body>.*", "<html>$1</html>").replaceAll("(?i)<[/]?(a|img|small|br|input)(\\s+[^>]*)?>", "").replaceAll("&(nbsp|reg);", " ").replaceAll("\\s+", " ")+"\n"; //.replaceAll("<([^/])", "\n<$1"); //.replaceAll("-->", "-->\n");
                        writer.write(input);
                    } finally {
                        if (reader != null) {
                            reader.close();

                        }
                        if (writer != null) {
                            writer.close();
                        }
                    }

                // NOTE THAT NOW EN AND ES ARE THE SAME (unless charset is set)
                if (lang.equalsIgnoreCase("en")) {
                    WikiHtml2PlainHandler wikihtml2plain = new WikiHtml2PlainHandler();
                    wikihtml2plain.init(charset);
                    wikihtml2plain.saveFile(title.replaceAll("/", "-") + ".cleanhtml", title.replaceAll("/", "-") + "-" + lang + ".txt");
                } else {
                    System.err.println("Leaving accents an non-ascii chars");
                    WikiHtml2PlainESHandler wikihtml2plainES = new WikiHtml2PlainESHandler();
                    wikihtml2plainES.saveFile(title.replaceAll("/", "-") + ".cleanhtml", title.replaceAll("/", "-") + "-" + lang + ".txt");
                }

                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println("Creating " + title.replaceAll("/", "-") + "-" + lang + ".txt");
                }

                File f2 = new File(title.replaceAll("/", "-") + ".cleanhtml");
                f2.delete();
            }

            return title.replaceAll("/", "-") + "-" + lang + ".txt";

        } catch (Exception e) {
            System.err.println("Errors found :\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
    }
}
