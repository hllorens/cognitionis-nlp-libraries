package com.cognitionis.timeml_basickit;

import com.cognitionis.timeml_basickit.comparators.AscStringTimexMapComparator;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.cognitionis.utils_basickit.FileUtils;

/**
 * Class with static methods to handle a TimeML file and convert it o a Java Object of TimML class
 * @author Héctor Llorens
 * @since 2011
 */
public class TML_file_utils {

    /**
     * Read a tml file and return a TML object
     *
     * Print stats after reading
     *
     * @param tmlfile
     * @return
     */
    public static TimeML ReadTml2Object(String tmlfile) {
        TimeML tml_object = null;
        Timex dctTimex = null;
        HashMap<String, Timex> timexes = new HashMap<String, Timex>();
        HashMap<String, Event> events = new HashMap<String, Event>();
        HashMap<String, Event> makeinstances = new HashMap<String, Event>();
        ArrayList<Link> links = new ArrayList<Link>();
        try {
            File file = new File(tmlfile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            }
            Element dct = ((Element) ((NodeList) ((Element) doc.getElementsByTagName("DCT").item(0)).getElementsByTagName("TIMEX3")).item(0));
            if (dct != null) {
                dctTimex = new Timex(dct.getAttribute("tid"), dct.getTextContent(), dct.getAttribute("type"), dct.getAttribute("value"), tmlfile, -1, -1, true);
                // probably add to timxes....
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.out.println("DCT: " + dct.getAttribute("tid") + " " + dct.getAttribute("value"));
                }
            }

            NodeList text = doc.getElementsByTagName("TEXT");
            String current_tag = "";

            // TEXT
            if (text.getLength() > 1) {
                throw new Exception("More than one TEXT tag found.");
            }

            Element TextElmnt = (Element) text.item(0); // If not ELEMENT NODE will throw exception

            //load everything and make sure it is properly linked

            current_tag = "TIMEX3";
            NodeList current_node = TextElmnt.getElementsByTagName(current_tag);
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("There's " + current_node.getLength() + " " + current_tag + ".");
            }
            for (int s = 0; s < current_node.getLength(); s++) {
                Element element = (Element) current_node.item(s);
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.out.println(element.getNodeName() + "(" + element.getAttribute("tid") + ", " + element.getAttribute("type") + ", " + element.getAttribute("value") + "): " + element.getTextContent());
                }
                timexes.put(element.getAttribute("tid"), new Timex(element.getAttribute("tid"), element.getTextContent(), element.getAttribute("type"), element.getAttribute("value"), tmlfile, -1, -1));
            }

            current_tag = "EVENT";
            current_node = TextElmnt.getElementsByTagName(current_tag);
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("There's " + current_node.getLength() + " " + current_tag + ".");
            }
            for (int s = 0; s < current_node.getLength(); s++) {
                Element element = (Element) current_node.item(s);
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.out.println(element.getNodeName() + "(" + element.getAttribute("eid") + ", " + element.getAttribute("class") + "): " + element.getTextContent());
                }
                events.put(element.getAttribute("tid"), new Event(dct.getAttribute("eid"), dct.getTextContent(), dct.getAttribute("class"), tmlfile, -1, -1));
            }

            current_tag = "MAKEINSTANCE";
            current_node = doc.getElementsByTagName(current_tag);
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("There's " + current_node.getLength() + " " + current_tag + ".");
            }
            for (int s = 0; s < current_node.getLength(); s++) {
                Element element = (Element) current_node.item(s);
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.out.println(element.getNodeName() + "(" + element.getAttribute("eiid") + ", " + element.getAttribute("eventID") + ")");
                }
                Event auxe = new Event(dct.getAttribute("eventID"), dct.getTextContent(), dct.getAttribute("class"), tmlfile, -1, -1);
                auxe.set_eiid(element.getAttribute("eiid"));
                makeinstances.put(element.getAttribute("eiid"), auxe);
            }


            current_tag = "TLINK";
            current_node = doc.getElementsByTagName(current_tag);
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("There's " + current_node.getLength() + " " + current_tag + ".");
            }
            for (int s = 0; s < current_node.getLength(); s++) {
                Element element = (Element) current_node.item(s);
                String relType = element.getAttribute("relType");
                String linkType = "unkonwn";
                if (relType.matches("(DURING|DURING_INV|IDENTITY)")) {
                    relType = "SIMULTANEOUS";
                }
                String entity1 = null;
                String entity2 = null;
                // event-event
                if (element.hasAttribute("eventInstanceID") && element.hasAttribute("relatedToEventInstance")) {
                    linkType = "tlink-event-event";
                    entity1 = element.getAttribute("eventInstanceID");
                    entity2 = element.getAttribute("relatedToEventInstance");
                    // Order by id (for normalization)
                    if (Integer.parseInt(entity1.substring(2)) > Integer.parseInt(entity2.substring(2))) {
                        entity1 = entity2;
                        entity2 = element.getAttribute("eventInstanceID");
                        relType = Link.reverseRelationCategory(relType);
                    }
                }
                // event-time
                if (element.hasAttribute("eventInstanceID") && element.hasAttribute("relatedToTime")) {
                    linkType = "tlink-event-timex";
                    entity1 = element.getAttribute("eventInstanceID");
                    entity2 = element.getAttribute("relatedToTime");
                }
                if (element.hasAttribute("timeID") && element.hasAttribute("relatedToEventInstance")) {
                    linkType = "tlink-event-timex";
                    entity1 = element.getAttribute("relatedToEventInstance");
                    entity2 = element.getAttribute("timeID");
                    relType = Link.reverseRelationCategory(relType);
                }
                // time-time
                if (element.hasAttribute("timeID") && element.hasAttribute("relatedToTime")) {
                    linkType = "tlink-timex-timex";
                    entity1 = element.getAttribute("timeID");
                    entity2 = element.getAttribute("relatedToTime");
                    // Order by id (for normalization)
                    if (Integer.parseInt(entity1.substring(1)) > Integer.parseInt(entity2.substring(1))) {
                        entity1 = entity2;
                        entity2 = element.getAttribute("timeID");
                        relType = Link.reverseRelationCategory(relType);
                    }
                }

                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.out.println(element.getNodeName() + "(" + element.getAttribute("lid") + ", " + linkType + ", " + entity1 + ", " + entity2 + ", " + relType + ")");
                }
                links.add(new Link(element.getAttribute("lid"), linkType, relType, entity1, entity2, tmlfile));
            }
            tml_object = new TimeML(dctTimex, timexes, events, events, links);


        } catch (Exception e) {
            System.err.println("Errors found (TML_file_utils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return tml_object;
    }

    /**
     * Converts a TimeML 1.2 file into a inline ISO-TimeML file
     * 
     * @param tmlfile
     * @return
     */
    public static String TML2ISOTML(String tmlfile) {
        String outputfile = null;
        try {
            String filecontents = null;
            File file = new File(tmlfile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            //System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            String current_tag = "";


            filecontents = FileUtils.readFileAsString(tmlfile, "UTF-8");

            HashMap<String, String> instancedevents = new HashMap<String, String>();
            current_tag = "MAKEINSTANCE";
            NodeList makeinstances = doc.getElementsByTagName(current_tag);
            System.out.println("There's " + makeinstances.getLength() + " " + current_tag + ".");
            for (int s = 0; s < makeinstances.getLength(); s++) {
                Element element = (Element) makeinstances.item(s);
                if (!instancedevents.containsKey(element.getAttribute("eventID"))) {
                    System.out.println("Removing: " + element.getNodeName() + "(" + element.getAttribute("eiid") + ", " + element.getAttribute("eventID") + ")");
                    instancedevents.put(element.getAttribute("eventID"), "ok");
                    String addition = " eiid=\"" + element.getAttribute("eiid") + "\"";
                    if (!element.getAttribute("pos").equals("")) {
                        addition += " pos=\"" + element.getAttribute("pos") + "\"";
                    }
                    if (!element.getAttribute("tense").equals("")) {
                        addition += " tense=\"" + element.getAttribute("tense") + "\"";
                    }
                    if (!element.getAttribute("aspect").equals("")) {
                        addition += " aspect=\"" + element.getAttribute("aspect") + "\"";
                    }
                    if (!element.getAttribute("polarity").equals("")) {
                        addition += " polarity=\"" + element.getAttribute("polarity") + "\"";
                    }
                    if (!element.getAttribute("modality").equals("")) {
                        addition += " modality=\"" + element.getAttribute("modality") + "\"";
                    }
                    filecontents = filecontents.replaceFirst("(<EVENT [^>]*eid=\"" + element.getAttribute("eventID") + "\"[^>]*)>", "$1" + addition + ">");
                    filecontents = filecontents.replaceFirst("<MAKEINSTANCE ([^/]*eiid=\"" + element.getAttribute("eiid") + "\"[^/]*)[/][^>]*>[ \t]*\n", "");
                } else {
                    System.out.println("Replacing: " + element.getNodeName() + "(" + element.getAttribute("eiid") + ", " + element.getAttribute("eventID") + ")");
                    // add class from event
                    Pattern MY_PATTERN = Pattern.compile("<EVENT [^>]*eid=\"" + element.getAttribute("eventID") + "\"[^>]*>");
                    Matcher m = MY_PATTERN.matcher(filecontents);
                    m.find();
                    String eclass = m.group(0).replaceFirst(".*class=\"([^\"]+)\".*", "$1");
                    filecontents = filecontents.replaceFirst("<MAKEINSTANCE ([^/]*eiid=\"" + element.getAttribute("eiid") + "\"[^/]*)/[^>]*>", "<EVENT $1 class=\"" + eclass + "\" />");
                }
            }
            filecontents = filecontents.replaceAll("eventID=\"", "eid=\"");
            if (filecontents.contains("MAKEINSTANCE")) {
                throw new Exception("Some makeinstances have not been replaced..." + tmlfile);
            }

            BufferedWriter outfile = null;
            try {
                outputfile = tmlfile + ".isotml";
                outfile = new BufferedWriter(new FileWriter(outputfile));
                outfile.write(filecontents);

            } finally {
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TML_file_utils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * Converts a TimeML 1.2 file into a inline ISO-TimeML file
     *
     * @param tmlfile
     * @return
     */
    public static String ISOTML2TML(String tmlfile) {
        String outputfile = null;
        try {
            String filecontents = null;
            File file = new File(tmlfile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            //System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList text = doc.getElementsByTagName("TEXT");
            String current_tag = "";
            Element TextElmnt = (Element) text.item(0); // If not ELEMENT NODE will throw exception

            //load everything and make sure it is properly linked

            filecontents = FileUtils.readFileAsString(tmlfile, "UTF-8");

            String makeinstances2add = "";
            current_tag = "EVENT";
            NodeList textevents = TextElmnt.getElementsByTagName(current_tag);
            System.out.println("There's " + textevents.getLength() + " " + current_tag + ".");
            for (int s = 0; s < textevents.getLength(); s++) {
                Element element = (Element) textevents.item(s);
                makeinstances2add += "\n<MAKEINSTANCE eventID=\"" + element.getAttribute("eid") + "\" eiid=\"" + element.getAttribute("eiid") + "\"";
                if (!element.getAttribute("pos").equals("")) {
                    makeinstances2add += " pos=\"" + element.getAttribute("pos") + "\"";
                }
                if (!element.getAttribute("tense").equals("")) {
                    makeinstances2add += " tense=\"" + element.getAttribute("tense") + "\"";
                }
                if (!element.getAttribute("aspect").equals("")) {
                    makeinstances2add += " aspect=\"" + element.getAttribute("aspect") + "\"";
                }
                if (!element.getAttribute("polarity").equals("")) {
                    makeinstances2add += " polarity=\"" + element.getAttribute("polarity") + "\"";
                }
                if (!element.getAttribute("modality").equals("")) {
                    makeinstances2add += " modality=\"" + element.getAttribute("modality") + "\"";
                }
                makeinstances2add += " />";
                filecontents = filecontents.replaceFirst("(<EVENT [^>]*eiid=\"" + element.getAttribute("eiid") + "\"[^>]*)>", "<EVENT eid=\"" + element.getAttribute("eid") + "\" class=\"" + element.getAttribute("class") + "\">");
            }

            filecontents = filecontents.replaceAll("<EVENT ([^/]* /)", "<MAKEINSTANCE $1");
            filecontents = filecontents.replaceAll("<MAKEINSTANCE ([^/]*)eid=([^/]*/)", "<MAKEINSTANCE $1eventID=$2");
            filecontents = filecontents.replaceFirst("</TEXT>", "</TEXT>\n" + makeinstances2add);

            BufferedWriter outfile = null;
            try {
                outputfile = tmlfile + ".tml";
                outfile = new BufferedWriter(new FileWriter(outputfile));
                outfile.write(filecontents);

            } finally {
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TML_file_utils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * Converts a TimeML 1.2 file into a non-tagged TE3 TimeML input
     * get TE3-input, TE3input from tml
     *
     * @param tmlfile
     * @return
     */
    public static String TML2TE3(String tmlfile) {
        String outputfile = null;
        try {
            String line;
            boolean textfound = false;
            String header = "";
            String footer = "";
            String text = "";

            //process header (and dct)/text/footer
            outputfile = tmlfile + ".TE3input";
            BufferedWriter te3writer = new BufferedWriter(new FileWriter(new File(outputfile)));
            BufferedReader TE3inputReader = new BufferedReader(new FileReader(new File(tmlfile)));

            try {

                // read out header
                while ((line = TE3inputReader.readLine()) != null) {
                    if (line.length() > 0) {
                        // break on TEXT
                        if (line.matches(".*<TEXT>.*")) {
                            textfound = true;
                            break;
                        }
                    }
                    header += line + "\n";
                }

                if (!textfound) {
                    throw new Exception("Premature end of file (" + tmlfile + ")");
                }

                // read out text
                while ((line = TE3inputReader.readLine()) != null) {
                    if (line.length() > 0) {
                        // break on TEXT
                        if (line.matches(".*</TEXT>.*")) {
                            textfound = false;
                            break;
                        }
                    }
                    text += line.replaceAll("<[^>]*>", "") + "\n";
                }

                if (textfound) {
                    throw new Exception("Premature end of file (" + tmlfile + ")");
                }

                // read out footer
                while ((line = TE3inputReader.readLine()) != null) {
                    line = line.replaceAll("<(!--|[TSA]LINK|MAKEINSTANCE)[^>]*>", "").trim();
                    if (line.length() > 0) {
                        footer += line + "\n";
                    }
                }

                te3writer.write(header + "\n");
                te3writer.write("\n<TEXT>\n" + text + "</TEXT>\n");
                te3writer.write(footer + "\n");

                System.err.println("Processing file: " + tmlfile);

            } finally {
                if (TE3inputReader != null) {
                    TE3inputReader.close();
                }
                if (te3writer != null) {
                    te3writer.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TML_file_utils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * Converts a TimeML 1.2 file into a non-taged links TimeML input (only entities)
     *
     * @param tmlfile
     * @return
     */
    public static String TML2onlyEntities(String tmlfile) {
        String outputfile = null;
        try {
            String line;
            boolean textfound = false;
            String header = "";
            String footer = "";
            String text = "";

            //process header (and dct)/text/footer
            outputfile = tmlfile + ".TE3input"; // same extension but it contains tags
            BufferedWriter te3writer = new BufferedWriter(new FileWriter(new File(outputfile)));
            BufferedReader TE3inputReader = new BufferedReader(new FileReader(new File(tmlfile)));

            try {

                // read out header
                while ((line = TE3inputReader.readLine()) != null) {
                    if (line.length() > 0) {
                        // break on TEXT
                        if (line.matches(".*<TEXT>.*")) {
                            textfound = true;
                            break;
                        }
                    }
                    header += line + "\n";
                }

                if (!textfound) {
                    throw new Exception("Premature end of file (" + tmlfile + ")");
                }

                // read out text
                while ((line = TE3inputReader.readLine()) != null) {
                    if (line.length() > 0) {
                        // break on TEXT
                        if (line.matches(".*</TEXT>.*")) {
                            textfound = false;
                            break;
                        }
                    }
                    text += line + "\n";
                }

                if (textfound) {
                    throw new Exception("Premature end of file (" + tmlfile + ")");
                }

                // read out footer
                while ((line = TE3inputReader.readLine()) != null) {
                    line = line.replaceAll("<(!--|[TSA]LINK|MAKEINSTANCE)[^>]*>", "").trim();
                    if (line.length() > 0) {
                        footer += line + "\n";
                    }
                }

                te3writer.write(header + "\n");
                te3writer.write("\n<TEXT>\n" + text + "</TEXT>\n");
                te3writer.write(footer + "\n");

                System.err.println("Processing file: " + tmlfile);

            } finally {
                if (TE3inputReader != null) {
                    TE3inputReader.close();
                }
                if (te3writer != null) {
                    te3writer.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TML_file_utils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return outputfile;
    }

    /**
     * Given a TimeML file, adds the implicit tref-tref links.
     * It searches for explicit dates and add their timex-timex implicit
     * temporal relations creating a time backbone of the document.
     * This trust the timex date values over other annotated information.
     * (removes original links if necessary)
     *
     * @param tmlfile
     * @return
     */
    public static String TML_add_tref_tref_links(String tmlfile) {
        String outputfile = null;
        ArrayList<Link> tref_tref_links = new ArrayList<Link>();
        ArrayList<Link> original_links = new ArrayList<Link>();
        HashMap<String, Timex> refs = new HashMap<String, Timex>();

        try {
            String filecontents = null;
            File file = new File(tmlfile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            //System.out.println("PARSING "+file);
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Timex dctTimex = null;
            Element dct = ((Element) ((NodeList) ((Element) doc.getElementsByTagName("DCT").item(0)).getElementsByTagName("TIMEX3")).item(0));
            if (dct != null) {
                dctTimex = new Timex(dct.getAttribute("tid"), dct.getTextContent(), dct.getAttribute("type"), dct.getAttribute("value"), tmlfile, -1, -1, true);
            } else {
                throw new Exception("NO DCT FOUND: " + tmlfile);
            }
            refs.put(dctTimex.get_id(), dctTimex);

            //System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList text = doc.getElementsByTagName("TEXT");
            String current_tag = "";
            Element TextElmnt = (Element) text.item(0); // If not ELEMENT NODE will throw exception
            filecontents = FileUtils.readFileAsString(tmlfile, "UTF-8");
            // Get reference Timexes
            current_tag = "TIMEX3";
            NodeList texttimexes = TextElmnt.getElementsByTagName(current_tag);
            for (int s = 0; s < texttimexes.getLength(); s++) {
                Element element = (Element) texttimexes.item(s);
                if (element.getAttribute("type").matches("(?:DATE|TIME)")) {
                    // omit weeks, season, quarters, half, trimesters...
                    if (element.getAttribute("value").matches("[0-9]+.*") && !element.getAttribute("value").matches(".*-(W|FA|WI|SP|SU|Q|T|H).*")) {
                        Timex reftimex = new Timex(element.getAttribute("tid"), element.getTextContent(), element.getAttribute("type"), element.getAttribute("value"), file.getName(), -1, -1);
                        if (reftimex == null || reftimex.get_date() == null) {
                            System.err.println("Omitted ref timex: " + reftimex.get_value());
                        } else {
                            refs.put(element.getAttribute("tid"), reftimex);
                        }
                    }
                }
            }

            NodeList current_node = doc.getElementsByTagName("TLINK");
            int count = (refs.size() * 2) - 1; // links id starts with n-1 (there is no l0)
            for (int s = 0; s < current_node.getLength(); s++) {
                count++;
                Element element = (Element) current_node.item(s);
                String relType = element.getAttribute("relType");
                String linkType = "unkonwn";
                String entity1 = null;
                String entity2 = null;
                // event-event
                if (element.hasAttribute("eventInstanceID") && element.hasAttribute("relatedToEventInstance")) {
                    linkType = "tlink-event-event";
                    entity1 = element.getAttribute("eventInstanceID");
                    entity2 = element.getAttribute("relatedToEventInstance");
                    // Order by id (for normalization)
                    if (Integer.parseInt(entity1.substring(2)) > Integer.parseInt(entity2.substring(2))) {
                        entity1 = entity2;
                        entity2 = element.getAttribute("eventInstanceID");
                        relType = Link.reverseRelationCategory(relType);
                    }
                }
                // event-time
                if (element.hasAttribute("eventInstanceID") && element.hasAttribute("relatedToTime")) {
                    linkType = "tlink-event-timex";
                    entity1 = element.getAttribute("eventInstanceID");
                    entity2 = element.getAttribute("relatedToTime");
                }
                if (element.hasAttribute("timeID") && element.hasAttribute("relatedToEventInstance")) {
                    linkType = "tlink-event-timex";
                    entity1 = element.getAttribute("relatedToEventInstance");
                    entity2 = element.getAttribute("timeID");
                    relType = Link.reverseRelationCategory(relType);
                }
                // time-time
                if (element.hasAttribute("timeID") && element.hasAttribute("relatedToTime")) {
                    linkType = "tlink-timex-timex";
                    entity1 = element.getAttribute("timeID");
                    entity2 = element.getAttribute("relatedToTime");
                    // ommit links that will be already introduced in tref-tref chain
                    if (refs.containsKey(entity1) || refs.containsKey(entity2)) {
                        continue;
                    }
                    // Order by id (for normalization)
                    if (Integer.parseInt(entity1.substring(1)) > Integer.parseInt(entity2.substring(1))) {
                        entity1 = entity2;
                        entity2 = element.getAttribute("timeID");
                        relType = Link.reverseRelationCategory(relType);
                    }
                }
                original_links.add(new Link("l" + count, linkType, relType, entity1, entity2, tmlfile));
            }

            //System.out.println(refs);
            // this could be more sophisticated is uses lower bound to sort first
            TreeMap<String, Timex> sorted_refs = new TreeMap(new AscStringTimexMapComparator(refs));
            sorted_refs.putAll(refs);

            // build the new links over sorted refs
            String improved_links = "";
            Timex last_timex = null;
            Timex last_includer_timex = null;
            count = 0;
            for (Timex t : sorted_refs.values()) {
                // includer
                if (last_includer_timex != null) {
                    count++;
                    String reltype = "BEFORE";
                    if (last_includer_timex.get_date().equals(t.get_date()) || t.get_value().startsWith(last_includer_timex.get_value())) {
                        if (last_includer_timex.get_value().equals(t.get_value())) {
                            reltype = "SIMULTANEOUS";
                            tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_includer_timex.get_id(), t.get_id(), tmlfile));
                        } else {
                            int lastincl = last_includer_timex.get_value().length();
                            int tl = t.get_value().length();
                            if (lastincl > tl) {
                                reltype = "IS_INCLUDED";
                                tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_includer_timex.get_id(), t.get_id(), tmlfile));
                                last_includer_timex = t;
                            } else {
                                if (tl > lastincl) {
                                    reltype = "INCLUDES";
                                    tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_includer_timex.get_id(), t.get_id(), tmlfile));
                                } else {
                                    System.err.println("Special values made SIMULATNEOUS: " + last_includer_timex.get_value() + "   " + t.get_value());
                                    reltype = "SIMULTANEOUS";
                                    tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_includer_timex.get_id(), t.get_id(), tmlfile));
                                }
                            }

                        }
                    } else {
                        tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_includer_timex.get_id(), t.get_id(), tmlfile));
                        last_timex = t;
                        last_includer_timex = null;
                    }
                }

                // last timex (if not considered before)
                if (last_timex == null || last_timex.get_id().equals(t.get_id())) {
                    last_timex = t;
                } else {
                    count++;
                    // before, simultaneous, is_included
                    String reltype = "BEFORE";
                    if (last_timex.get_date().equals(t.get_date()) || t.get_value().startsWith(last_timex.get_value())) {
                        if (last_timex.get_value().equals(t.get_value())) {
                            reltype = "SIMULTANEOUS";
                            tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_timex.get_id(), t.get_id(), tmlfile));
                        } else {
                            int lastl = last_timex.get_value().length();
                            int tl = t.get_value().length();
                            // for seasons and TODs keep it as it is  // weeks-special case   /*if (last_timex.get_value().contains("W")) {  if(lastincl<8)      lastincl = 8; if(lastincl>8) lastincl = 9;    } if (t.get_value().contains("W")) {    if(tl<8)  tl = 8;   if(tl>8)  tl = 9;   }*/
                            if (lastl > tl) {
                                reltype = "IS_INCLUDED";
                                tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_timex.get_id(), t.get_id(), tmlfile));
                                last_includer_timex = t;
                            } else {
                                if (tl > lastl) {
                                    reltype = "INCLUDES";
                                    tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_timex.get_id(), t.get_id(), tmlfile));
                                    last_includer_timex = last_timex;
                                    last_timex = t;
                                } else {
                                    System.err.println("Special values made SIMULATNEOUS: " + last_timex.get_value() + "   " + t.get_value());
                                    reltype = "SIMULTANEOUS";
                                    tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_timex.get_id(), t.get_id(), tmlfile));
                                }
                            }

                        }
                    } else {
                        tref_tref_links.add(new Link("l" + count, "tlink-timex-timex", reltype, last_timex.get_id(), t.get_id(), tmlfile));
                        last_timex = t;
                    }
                }

            }

            // remove all links
            filecontents = filecontents.replaceAll("<[TSAR]LINK[^>]*>[^\\n]*\\n", "");

            // create new links string (1.tref_tref + 2.original)
            // addAll only adds the links that do not break the timegraph concistency
            tref_tref_links.addAll(original_links); 
            for (int i = 0; i < tref_tref_links.size(); i++) {
                Link l = tref_tref_links.get(i);
                if (l.get_type().startsWith("tlink-event-timex")) {
                    improved_links += "<TLINK lid=\"" + l.get_id() + "\" relType=\"" + l.get_category().toUpperCase() + "\" eventInstanceID=\"" + l.get_id1() + "\" relatedToTime=\"" + l.get_id2() + "\" />\n";
                }
                if (l.get_type().startsWith("tlink-event-event")) {
                    improved_links += "<TLINK lid=\"" + l.get_id() + "\" relType=\"" + l.get_category().toUpperCase() + "\" eventInstanceID=\"" + l.get_id1() + "\" relatedToEventInstance=\"" + l.get_id2() + "\" />\n";
                }
                if (l.get_type().startsWith("tlink-timex-timex")) {
                    improved_links += "<TLINK lid=\"" + l.get_id() + "\" relType=\"" + l.get_category().toUpperCase() + "\" timeID=\"" + l.get_id1() + "\" relatedToTime=\"" + l.get_id2() + "\" />\n";
                }
            }

            // put all links just above the TimeML closing tag
            filecontents = filecontents.replaceFirst("</TimeML>", "\n" + improved_links + "\n</TimeML>\n");

            BufferedWriter outfile = null;
            try {
                outputfile = tmlfile + ".tref-links";
                outfile = new BufferedWriter(new FileWriter(outputfile));
                outfile.write(filecontents);
            } finally {
                if (outfile != null) {
                    outfile.close();
                }
            }



        } catch (Exception e) {
            System.err.println("Errors found (TML_file_utils):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return outputfile;
    }
}
