package com.cognitionis.feature_builder;


import com.cognitionis.knowledgek.TIMEK.TIMEK;
import com.cognitionis.nlp_files.*;
import com.cognitionis.nlp_files.parentical_parsers.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import org.joda.time.DateTime;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class CategorizationTE2 {

    public static String get_categorization(String categstab, String elem) {
        String output = null;
        try {
            if ((new File(categstab + "." + elem + ".pipes")).exists()) {
                output = categstab + "." + elem + ".pipes";
            } else {
                TabFile tf = new TabFile(categstab);
                tf.isWellFormatted();
                Class c = Class.forName(CategorizationTE2.class.getName());
                Class params[] = new Class[1];
                params[0] = TabFile.class;
                Method m = c.getDeclaredMethod("get_" + elem.toUpperCase() + "_corpus", params);
                Object paramsObj[] = new Object[1];
                paramsObj[0] = tf;
                output = (String) m.invoke(new CategorizationTE2(), paramsObj);
            }
        } catch (Exception e) {
            System.err.println("Errors found (Experimenter):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        return output;
    }


    // get Task C corpus
    public static String get_TASKC_corpus(TabFile file) {
        String outputfile = null;
        String line = null;
        String attrsline = null;
        HashMap<String, String> event = new HashMap<String, String>();
        HashMap<String, String> timex = new HashMap<String, String>();
        try {
            // Leer fichero tabs i leer ficheros tabs para completar info...
            outputfile = file.getFile().getCanonicalPath() + ".TASKC.pipes";
            String directory = file.getFile().getParent();
            if (directory == null) {
                directory = "";
            } else {
                directory += "/";
            }
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader reader = new BufferedReader(new FileReader(file.getFile()));
            try {
                String[] arr = null;
                while ((line = reader.readLine()) != null) {
                    arr = line.split("\t");
                    ArrayList<HashMap<String, String>> events = new ArrayList<HashMap<String, String>>();
                    ArrayList<HashMap<String, String>> timexs = new ArrayList<HashMap<String, String>>();

                    fill_event_attribs(directory, arr, events);
                    fill_timex_attribs(directory, arr, timexs, events.get(0).get("sentN"));




                    String[] attrsarr = null;
                    //BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval-features"));
                    BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval2-features"));
                    int syntcolumn = 7;
                    int rolecolumn = 14; // UPDATED from 13
                    int wordcolumn = 3;
                    int phrasecolumn = 20; // UPDATED from 19
                    int pptimexcolumn = 21; // UPDATED from 20
                    int poscolumn = 4;
                    SyntColSBarTMPRoleParser sbarroleparser = null;
                    String sentN = events.get(0).get("sentN");
                    if (Integer.parseInt(sentN) > Integer.parseInt(timexs.get(0).get("sentN"))) {
                        sentN = timexs.get(0).get("sentN");
                        System.out.println("Change order sentN->" + sentN);
                    }
                    String SBARTMPevent = "-";
                    String PPevent = "-";
                    String SBARTMPtimex = "-";
                    String PPtimex = "-";
                    String valdiff = "-";
                    String interval = "-";
                    Integer relation = 0;
                    int lookinterval = -1;
                    String[] memattrsarr = null;

                    while ((attrsline = attrsreader.readLine()) != null) {
                        attrsarr = attrsline.split("\\|");
                        if (attrsarr[0].equals(arr[0]) && attrsarr[1].equals(sentN)) {
                            sbarroleparser = new SyntColSBarTMPRoleParser();
                            do {
                                attrsarr = attrsline.split("\\|");
                                if (!attrsarr[0].equals(arr[0]) || Integer.parseInt(attrsarr[1]) > Integer.parseInt(sentN)) {
                                    break;
                                }

                                // Synt
                                boolean hasClosingBrakets = false;
                                if (attrsarr[syntcolumn].indexOf(')') != -1) {
                                    hasClosingBrakets = true;
                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(0, attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                } else {
                                    sbarroleparser.parse(attrsarr[syntcolumn], attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }


                                // Remove interval
                                if (lookinterval >= 0 && Integer.parseInt(attrsarr[2]) - lookinterval > 1) {
                                    lookinterval = -1;
                                    interval = "-";
                                }


                                for (HashMap<String, String> a : events) {
                                    if (attrsarr[1].equals(a.get("sentN")) && attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        if (!a.containsKey("pos")) {
                                            if (attrsarr[poscolumn].startsWith("V")) {
                                                a.put("pos", "VERB"); // HACK FOR SPANISH AND ENGLISH TRIAL
                                            } else {
                                                if (attrsarr[poscolumn].startsWith("N")) {
                                                    a.put("pos", "NOUN");
                                                } else {
                                                    a.put("pos", "NONE");
                                                }
                                            }

                                        }
                                        if (arr[1].equals(a.get("id"))) {
                                            sentN = timexs.get(0).get("sentN");
                                            PPevent = attrsarr[pptimexcolumn];
                                            SBARTMPevent = sbarroleparser.getSubsentTMP();
                                            event = a;
                                        }
                                    }
                                }
                                for (HashMap<String, String> a : timexs) {
                                    if (attrsarr[1].equals(a.get("sentN")) && attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        // consolide or remove interval
                                        if (lookinterval >= 0) {
                                            if (a.get("type").equals("DATE")) {
                                                if (!memattrsarr[wordcolumn].matches("(to|and|-|/|y|a)")) {
                                                    interval = "-";
                                                } else {
                                                    if (arr[2].equals(a.get("id"))) {
                                                        interval = "intervalEnd";
                                                    }
                                                }
                                            } else {
                                                interval = "-";
                                            }
                                            lookinterval = -1;
                                        }
                                        if (lookinterval == -1 && interval.equals("-") && a.get("type").equals("DATE") && timexs.size() > 1) {
                                            // possible start interval
                                            lookinterval = Integer.parseInt(a.get("tokN"));
                                            if (arr[2].equals(a.get("id"))) {
                                                interval = "intervalStart";
                                            }
                                        }


                                        if (arr[2].equals(a.get("id"))) {
                                            sentN = events.get(0).get("sentN");
                                            PPtimex = attrsarr[pptimexcolumn];
                                            SBARTMPtimex = sbarroleparser.getSubsentTMP();
                                            timex = a;
                                        }
                                    }

                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }
                                memattrsarr = attrsarr;

                            } while ((attrsline = attrsreader.readLine()) != null);
                            sbarroleparser = null;
                            break;
                        }
                    }
                    if (timex.isEmpty() || event.isEmpty()) {
                        throw new Exception("Sentence " + sentN + " not found in " + arr[0]);
                    }
                    attrsreader.close();

                    // INTERVAL
                    // Si encuentro un TIMEX el siguiente (ventana de max 2) es suceptible de ser un intervalo... (-,intervalStart,intervalEnd)
                    // Si entre uno i otro no hay más de 2 palabras y la última de ellas es (-,/,EN:to,and,ES:a,hasta)
                    // Entonces poner intervalStart si el timex al que nos referimos es el primero de ellos o instervalEnd si es el segundo
                    if (lookinterval != -1) {
                        interval = "-";
                    }


                    // TODO: First check if they are in the same sentence...

                    if (timex.get("subsent").equals(event.get("subsent"))) {
                        //SBARTMPtimex = "=";
                        relation = 1;
                    }
                    if (timex.get("phrase").equals(event.get("phrase"))) {
                        //PPtimex = "=";
                        //SBARTMPtimex = "=";
                        relation = 2;
                    }


                    // Relacions entre event timexes...
                    // Si no hi ha + de un timex --> valrel="-"
                    // Si hi ha + de un timex
                    // Buscar timex + relacionat en la frase
                    // Saber si el event esta definit o no per eixa expressio (mateix PHRASE?phraid,mateixa sub-frase?)
                    // Altres timex? (valor del més relacionat --> relacio del q comparem <=> (si es el mateix será sempre =))
                    // Han de ser de tipus DATE (ISO)



                    if (!timex.get("type").equals("DATE")) {
                        interval = "-";
                        valdiff = "-";
                    } else { // ver si hay que hacer valdiff
                        // si no forma intervalo, no esta en el mismo sintagma y hay más timexes o eventos...
                        if (interval.equals("-") && relation < 2 && (timexs.size() > 1 || events.size() > 1)) {

                            // caso 1: El evento tiene otro timex DATE asociado
                            HashMap<String, String> difftimex = null;
                            if (timexs.size() > 1) {
                                for (HashMap a : timexs) {
                                    if (!timex.get("id").equals(a.get("id")) && a.get("type").equals("DATE")) {
                                        if (event.get("phrase").equals(a.get("phrase"))) {
                                            difftimex = a;
                                            break;
                                        }
                                        if (relation == 0 && event.get("subsent").equals(a.get("subsent"))) {
                                            difftimex = a;
                                        }
                                    }
                                }
                                if (difftimex != null) {
                                    //System.out.println(timex.get("file") + ": Calculando valdif de " + timex.get("id") + " con " + difftimex.get("id"));
                                    if (timex.get("value").matches("[0-9]{4}[^X]*") && difftimex.get("value").matches("[0-9]{4}[^X]*")) {
                                        DateTime date1 = new DateTime(TIMEK.ISOclean(timex.get("value")));
                                        DateTime date2 = new DateTime(TIMEK.ISOclean(difftimex.get("value")));
                                        if (date1.isAfter(date2)) {
                                            valdiff = "gt";
                                        } else {
                                            if (date1.isBefore(date2)) {
                                                valdiff = "lt";
                                            } else {
                                                valdiff = "eq";
                                            }
                                        }
                                    }
                                    if (timex.get("value").endsWith("REF") && difftimex.get("value").endsWith("REF")) {
                                        if (timex.get("value").equals(difftimex.get("value"))) {
                                            valdiff = "eq";
                                        } else {
                                            if (timex.get("value").equals("FUTURE_REF")) {
                                                valdiff = "gt";
                                            }
                                            if (timex.get("value").equals("PAST_REF")) {
                                                valdiff = "lt";
                                            }
                                            if (timex.get("value").equals("PRESENT_REF")) {
                                                if (difftimex.get("value").equals("FUTURE_REF")) {
                                                    valdiff = "lt";
                                                } else {
                                                    valdiff = "gt";
                                                }
                                            }
                                        }
                                    }


                                }
                            }

                            // caso 2: Si no se da el caso 1 puede que el timex este asociado a otro evento
                            // y entonces la relación puede depender de los tenses (comprobar tense/aspect y considerar valdif con DCT)
                            // Realmente no hace falta comparar con DCT solo con los tenses ya sería suficiente pero
                            // Se puede sacar el valdif directamente y si hace honor a los tenses poner, sino dejar valdiff vacio "-"
                            // O dejar más abierto a ML y en vez de gt,eq,lt poner la diferencia de tenses y que aprenda...
                            if (valdiff.equals("-") && event.get("pos") != null && event.get("pos").equals("VERB") && events.size() > 1 && relation == 0) {
                                HashMap<String, String> diffevent = null;
                                for (HashMap a : events) {
                                    if (!(event.get("id").equals(a.get("id"))) && a.get("pos").equals("VERB")) {
                                        if (timex.get("phrase").equals(a.get("phrase"))) {
                                            diffevent = a;
                                            break;
                                        }
                                        if (timex.get("subsent").equals(a.get("subsent"))) {
                                            diffevent = a;
                                        }
                                    }
                                }
                                if (diffevent != null) {
                                    //System.out.println(event.get("file") + ": Calculando eventtensediff de " + event.get("id") + " con " + diffevent.get("id"));
                                    if (!event.get("tense").equals(diffevent.get("tense"))) {
                                        valdiff = event.get("tense") + "-" + diffevent.get("tense");
                                    } else {
                                        if (!event.get("aspect").equals(diffevent.get("aspect"))) {
                                            valdiff = event.get("aspect") + "-" + diffevent.get("aspect");
                                        }
                                    }

                                }
                            }

                        }
                    }







                    outfile.write(arr[0] + "|" + arr[1] + "|" + arr[2] + "|" + SBARTMPevent + "|" + PPevent + "|" + relation + "|" + valdiff + "|" + SBARTMPtimex + "|" + PPtimex + "|" + timex.get("type") + "|" + interval + "|" + arr[3] + "\n");
                    event = null;
                    timex = null;


                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + " (" + line + ")(" + attrsline + ")(" + event + ")(" + timex + ")\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

// tarea D
    // para cada evento intentar asociarlo a un timex (ahora ya no hace falta q haya más de 1) y comparar con DCT (<=>)
    // tense, PPdetail, ... lo mismo
    public static String get_TASKD_corpus(TabFile file) {
        String outputfile = null;
        try {
            // Leer fichero tabs i leer ficheros tabs para completar info...
            outputfile = file.getFile().getCanonicalPath() + ".TASKD.pipes";
            String directory = file.getFile().getParent();
            if (directory == null) {
                directory = "";
            } else {
                directory += "/";
            }
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader reader = new BufferedReader(new FileReader(file.getFile()));
            try {
                String line;
                int linen = 0;
                String[] arr = null;
                HashMap<String, String> DCTs = new HashMap<String, String>();
                fill_DCTs(directory, DCTs);

                while ((line = reader.readLine()) != null) {
                    linen++;
                    arr = line.split("\t");
                    ArrayList<HashMap<String, String>> events = new ArrayList<HashMap<String, String>>();
                    ArrayList<HashMap<String, String>> timexs = new ArrayList<HashMap<String, String>>();
                    //System.out.println(linen);


                    fill_event_attribs(directory, arr, events);
                    fill_timex_attribs(directory, arr, timexs, events.get(0).get("sentN"));


                    String attrsline = null;
                    String[] attrsarr = null;
                    //BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval-features"));
                    BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval2-features"));
                    int syntcolumn = 7;
                    int rolecolumn = 14; // UPDATED from 13
                    int wordcolumn = 3;
                    int phrasecolumn = 20; // UPDATED from 19
                    int pptimexcolumn = 21; // UPDATED from 20
                    int poscolumn = 4;
                    SyntColSBarTMPRoleParser sbarroleparser = null;
                    String sentN = events.get(0).get("sentN");
                    String SBARTMPevent = "-";
                    String PPevent = "-";
                    String valdiff = "-";
                    Integer relation = -1;
                    HashMap<String, String> event = new HashMap<String, String>();
                    HashMap<String, String> difftimex = null;
                    while ((attrsline = attrsreader.readLine()) != null) {
                        attrsarr = attrsline.split("\\|");
                        if (attrsarr[0].equals(arr[0]) && attrsarr[1].equals(sentN)) {
                            sbarroleparser = new SyntColSBarTMPRoleParser();
                            do {
                                attrsarr = attrsline.split("\\|");
                                if (!attrsarr[0].equals(arr[0]) || !attrsarr[1].equals(sentN)) {
                                    break;
                                }

                                // Synt
                                boolean hasClosingBrakets = false;
                                if (attrsarr[syntcolumn].indexOf(')') != -1) {
                                    hasClosingBrakets = true;
                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(0, attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                } else {
                                    sbarroleparser.parse(attrsarr[syntcolumn], attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }




                                for (HashMap<String, String> a : events) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        /*if(attrsarr[poscolumn].startsWith("V")){
                                        a.put("pos", "VERB"); // HACK FOR SPANISH
                                        }*/

                                        if (arr[1].equals(a.get("id"))) {
                                            PPevent = attrsarr[pptimexcolumn];
                                            SBARTMPevent = sbarroleparser.getSubsentTMP();
                                            event = a;
                                        }
                                    }

                                }
                                for (HashMap<String, String> a : timexs) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        a.put("PPtimex", attrsarr[pptimexcolumn].toLowerCase());
                                        a.put("SBARTMPtimex", sbarroleparser.getSubsentTMP());
                                    }

                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }

                            } while ((attrsline = attrsreader.readLine()) != null);
                            sbarroleparser = null;
                            break;
                        }
                    }
                    attrsreader.close();

                    if (timexs.size() > 0) {
                        // Buscar timex con el que se relaciona (DATE) prevalece sobre otro tipo
                        for (HashMap a : timexs) {
                            if (event.get("phrase").equals(a.get("phrase"))) {
                                difftimex = a;
                                relation = 2;
                                if (a.get("type").equals("DATE")) {
                                    break;
                                }
                            }
                            if (relation < 2 && event.get("subsent").equals(a.get("subsent"))) {
                                if (relation < 1 || a.get("type").equals("DATE")) {
                                    difftimex = a;
                                }
                                relation = 1;
                            }
                            if (relation < 1) {
                                if (relation < 0 || a.get("type").equals("DATE")) {
                                    difftimex = a;
                                }
                                relation = 0;
                            }
                        }

                        if (difftimex != null && difftimex.get("type").equals("DATE")) {
                            if (difftimex.get("value").matches("[0-9]{4}[^X]*")) {
                                DateTime date1 = new DateTime(TIMEK.ISOclean(difftimex.get("value")));
                                DateTime date2 = new DateTime(DCTs.get(arr[0]));
                                if (date1.isAfter(date2)) {
                                    valdiff = "gt";
                                } else {
                                    if (date1.isBefore(date2)) {
                                        valdiff = "lt";
                                    } else {
                                        valdiff = "eq";
                                    }
                                }
                            } else {
                                if (difftimex.get("value").startsWith("PRESENT")) {
                                    valdiff = "eq";
                                }
                                if (difftimex.get("value").startsWith("PAST")) {
                                    valdiff = "lt";
                                }
                                if (difftimex.get("value").startsWith("FUTURE")) {
                                    valdiff = "gt";
                                }
                            }
                        }

                    }


                    if (difftimex != null) {
                        outfile.write(arr[0] + "|" + arr[1] + "|" + arr[2] + "|" + SBARTMPevent + "|" + PPevent + "|" + event.get("tense") + "-" + event.get("aspect") + "|" + relation + "|" + valdiff + "|" + difftimex.get("SBARTMPtimex") + "|" + difftimex.get("PPtimex") + "|" + difftimex.get("type") + "|" + arr[3] + "\n");
                    } else {
                        outfile.write(arr[0] + "|" + arr[1] + "|" + arr[2] + "|" + SBARTMPevent + "|" + PPevent + "|" + event.get("tense") + "-" + event.get("aspect") + "|" + relation + "|" + valdiff + "|-|-|-|" + arr[3] + "\n");
                    }
                    event = null;
                    difftimex = null;

                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String get_TASKE_corpus(TabFile file) {
        String outputfile = null;
        try {
            outputfile = file.getFile().getCanonicalPath() + ".TASKE.pipes";
            String directory = file.getFile().getParent();
            if (directory == null) {
                directory = "";
            } else {
                directory += "/";
            }
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader reader = new BufferedReader(new FileReader(file.getFile()));
            try {
                String line;
                String[] arr = null;
                while ((line = reader.readLine()) != null) {
                    arr = line.split("\t");
                    ArrayList<HashMap<String, String>> events = new ArrayList<HashMap<String, String>>();
                    ArrayList<HashMap<String, String>> timexs = new ArrayList<HashMap<String, String>>();
                    ArrayList<HashMap<String, String>> events2 = new ArrayList<HashMap<String, String>>();
                    ArrayList<HashMap<String, String>> timexs2 = new ArrayList<HashMap<String, String>>();



                    fill_event_attribs(directory, arr, events);
                    fill_event2_attribs(directory, arr, events2);
                    fill_timex_attribs(directory, arr, timexs, events.get(0).get("sentN"));
                    fill_timex_attribs(directory, arr, timexs2, events2.get(0).get("sentN"));

//                    System.out.println(line);


                    String attrsline = null;
                    String[] attrsarr = null;
                    //BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval-features"));
                    BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval2-features"));
                    int syntcolumn = 7;
                    int rolecolumn = 14;  // UPDATED from 13
                    int wordcolumn = 3;
                    int poscolumn = 4;
                    int phrasecolumn = 20; // UPDATED from 19
                    int pptimexcolumn = 21; // UPDATED from 20
                    SyntColSBarTMPRoleParser sbarroleparser = null;
                    String sentN = events.get(0).get("sentN");
                    String valdiff = "-";
                    Integer relation = -1;
                    Integer relation2 = -1;
                    Boolean samesent = false;
                    HashMap<String, String> event = new HashMap<String, String>();
                    HashMap<String, String> event2 = new HashMap<String, String>();
                    HashMap<String, String> difftimex = null;
                    HashMap<String, String> difftimex2 = null;

                    while ((attrsline = attrsreader.readLine()) != null) {
                        attrsarr = attrsline.split("\\|");
                        if (attrsarr[0].equals(arr[0]) && attrsarr[1].equals(sentN) && event.size() == 0) {
                            sbarroleparser = new SyntColSBarTMPRoleParser();
                            do {
                                attrsarr = attrsline.split("\\|");
                                if (!attrsarr[0].equals(arr[0]) || !attrsarr[1].equals(sentN)) {
                                    break;
                                }
                                // Synt
                                boolean hasClosingBrakets = false;
                                if (attrsarr[syntcolumn].indexOf(')') != -1) {
                                    hasClosingBrakets = true;
                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(0, attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                } else {
                                    sbarroleparser.parse(attrsarr[syntcolumn], attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }

                                for (HashMap<String, String> a : events) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        a.put("PPevent", attrsarr[pptimexcolumn].toLowerCase());
                                        a.put("SBARTMPevent", sbarroleparser.getSubsentTMP());
                                        /*if(attrsarr[poscolumn].startsWith("V")){
                                        a.put("pos", "VERB"); // HACK FOR SPANISH
                                        }*/

                                        if (arr[1].equals(a.get("id"))) {
                                            event = a;
                                        }
                                        if (arr[2].equals(a.get("id"))) {
                                            event2 = a;
                                        }
                                    }
                                }
                                for (HashMap<String, String> a : timexs) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                    }
                                }

                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }

                            } while ((attrsline = attrsreader.readLine()) != null);
                            sbarroleparser = null;
                            if (sentN.equals(events2.get(0).get("sentN")) && event2.size() > 0) {
                                events2 = events;
                                timexs2 = timexs;
                                samesent = true;
                                break;
                            } else {
                                sentN = events2.get(0).get("sentN");
                            }


                        }



                        if (attrsarr[0].equals(arr[0]) && attrsarr[1].equals(sentN) && event.size() > 0 && event2.size() == 0) {
                            sbarroleparser = new SyntColSBarTMPRoleParser();
                            do {
                                attrsarr = attrsline.split("\\|");
                                if (!attrsarr[0].equals(arr[0]) || !attrsarr[1].equals(sentN)) {
                                    break;
                                }
                                // Synt
                                boolean hasClosingBrakets = false;
                                if (attrsarr[syntcolumn].indexOf(')') != -1) {
                                    hasClosingBrakets = true;
                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(0, attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                } else {
                                    sbarroleparser.parse(attrsarr[syntcolumn], attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }




                                for (HashMap<String, String> a : events2) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        a.put("PPevent", attrsarr[pptimexcolumn].toLowerCase());
                                        a.put("SBARTMPevent", sbarroleparser.getSubsentTMP());
                                        /*if(attrsarr[poscolumn].startsWith("V")){
                                        a.put("pos", "VERB"); // HACK FOR SPANISH
                                        }*/
                                        if (arr[2].equals(a.get("id"))) {
                                            event2 = a;
                                        }
                                    }
                                }
                                for (HashMap<String, String> a : timexs2) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                    }
                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }

                            } while ((attrsline = attrsreader.readLine()) != null);
                            sbarroleparser = null;
                            break;
                        }




                    }
                    attrsreader.close();
                    if (event.size() == 0) {
                        throw new Exception("Event1 not found " + arr[1] + " in sent " + events.get(0).get("sentN"));
                    }

                    if (event2.size() == 0) {
                        throw new Exception("Event2 not found " + arr[2] + " in sent " + events2.get(0).get("sentN"));
                    }

                    if (timexs.size() > 0) {
                        for (HashMap a : timexs) {
                            if (event.get("phrase").equals(a.get("phrase"))) {
                                difftimex = a;
                                relation = 2;
                                if (a.get("type").equals("DATE")) {
                                    break;
                                }
                            }
                            if (relation < 2 && event.get("subsent").equals(a.get("subsent"))) {
                                if (relation < 1 || a.get("type").equals("DATE")) {
                                    difftimex = a;
                                }
                                relation = 1;
                            }
                            if (relation < 1) {
                                if (relation < 0 || a.get("type").equals("DATE")) {
                                    difftimex = a;
                                }
                                relation = 0;
                            }
                        }
                    }

                    if (timexs2.size() > 0) {
                        for (HashMap a : timexs2) {
                            if (event2.get("phrase").equals(a.get("phrase"))) {
                                difftimex2 = a;
                                relation2 = 2;
                                if (a.get("type").equals("DATE")) {
                                    break;
                                }
                            }
                            if (relation2 < 2 && event2.get("subsent").equals(a.get("subsent"))) {
                                if (relation2 < 1 || a.get("type").equals("DATE")) {
                                    difftimex2 = a;
                                }
                                relation2 = 1;
                            }
                            if (relation2 < 1) {
                                if (relation2 < 0 || a.get("type").equals("DATE")) {
                                    difftimex2 = a;
                                }
                                relation = 0;
                            }
                        }
                    }


                    if (difftimex != null && difftimex2 != null && difftimex.get("type").equals("DATE") && difftimex2.get("type").equals("DATE")) {

                        if (difftimex.get("value").matches("[0-9]{4}[^X]*") && difftimex2.get("value").matches("[0-9]{4}[^X]*")) {
                            DateTime date1 = new DateTime(TIMEK.ISOclean(difftimex.get("value")));
                            DateTime date2 = new DateTime(TIMEK.ISOclean(difftimex2.get("value")));
                            if (date1.isAfter(date2)) {
                                valdiff = "gt";
                            } else {
                                if (date1.isBefore(date2)) {
                                    valdiff = "lt";
                                } else {
                                    valdiff = "eq";
                                }
                            }
                        }
                        if (difftimex.get("value").endsWith("REF") && difftimex2.get("value").endsWith("REF")) {
                            if (difftimex.get("value").equals(difftimex2.get("value"))) {
                                valdiff = "eq";
                            } else {
                                if (difftimex.get("value").equals("FUTURE_REF")) {
                                    valdiff = "gt";
                                }
                                if (difftimex.get("value").equals("PAST_REF")) {
                                    valdiff = "lt";
                                }
                                if (difftimex.get("value").equals("PRESENT_REF")) {
                                    if (difftimex2.get("value").equals("FUTURE_REF")) {
                                        valdiff = "lt";
                                    } else {
                                        valdiff = "gt";
                                    }
                                }
                            }
                        }

                    }

                    String samesent_s = "diffsent";
                    if (samesent) {
                        if (event.get("phrase").equals(event2.get("phrase"))) {
                            samesent_s = "samephra";
                        } else {
                            if (event.get("subsent").equals(event2.get("subsent"))) {
                                samesent_s = "samesubsent(" + event.get("PPevent") + "-" + event2.get("PPevent") + ")";
                            } else {
                                samesent_s = "samesent(" + event.get("SBARTMPevent") + "-" + event2.get("SBARTMPevent") + ")";
                            }
                        }

                    }
                    outfile.write(arr[0] + "|" + arr[1] + "|" + arr[2] + "|" + event.get("tense") + "-" + event.get("aspect") + "|" + event2.get("tense") + "-" + event2.get("aspect") + "|" + samesent_s + "|" + valdiff + "|" + arr[3] + "\n");
                    event = null;
                    difftimex = null;
                    event2 = null;
                    difftimex2 = null;

                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return outputfile;
    }

    public static String get_TASKF_corpus(TabFile file) {
        String outputfile = null;
        try {
            outputfile = file.getFile().getCanonicalPath() + ".TASKF.pipes";
            String directory = file.getFile().getParent();


            if (directory == null) {
                directory = "";
            } else {
                directory += "/";
            }
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader reader = new BufferedReader(new FileReader(file.getFile()));


            try {
                String line;
                String[] arr = null;


                while ((line = reader.readLine()) != null) {
                    arr = line.split("\t");
                    ArrayList<HashMap<String, String>> events = new ArrayList<HashMap<String, String>>();

                    fill_event_attribs(directory, arr, events);

                    String attrsline = null;
                    String[] attrsarr = null;
                    //BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval-features"));
                    BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "base-segmentation.TempEval2-features"));
                    int syntcolumn = 7;
                    int rolecolumn = 14;  // UPDATED from 13
                    int wordcolumn = 3;
                    int poscolumn = 4;
                    int phrasecolumn = 20; // UPDATED from 19
                    int pptimexcolumn = 21; // UPDATED from 20

                    SyntColSBarTMPRoleParser sbarroleparser = null;
                    String sentN = events.get(0).get("sentN");
                    HashMap<String, String> event = new HashMap<String, String>();
                    HashMap<String, String> event2 = new HashMap<String, String>();


                    while ((attrsline = attrsreader.readLine()) != null) {
                        attrsarr = attrsline.split("\\|");
                        if (attrsarr[0].equals(arr[0]) && attrsarr[1].equals(sentN)) {
                            sbarroleparser = new SyntColSBarTMPRoleParser();
                            do {
                                attrsarr = attrsline.split("\\|");
                                if (!attrsarr[0].equals(arr[0]) || !attrsarr[1].equals(sentN)) {
                                    break;
                                } // Synt
                                boolean hasClosingBrakets = false;


                                if (attrsarr[syntcolumn].indexOf(')') != -1) {
                                    hasClosingBrakets = true;
                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(0, attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);


                                } else {
                                    sbarroleparser.parse(attrsarr[syntcolumn], attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }




                                for (HashMap<String, String> a : events) {
                                    if (attrsarr[2].equals(a.get("tokN"))) {
                                        a.put("phrase", attrsarr[phrasecolumn]);
                                        a.put("subsent", sbarroleparser.getCurrentSubsent());
                                        a.put("PPevent", attrsarr[pptimexcolumn]);
                                        a.put("SBARTMPevent", sbarroleparser.getSubsentTMP());
                                        /*if(attrsarr[poscolumn].startsWith("V")){
                                        a.put("pos", "VERB"); // HACK FOR SPANISH
                                        }*/

                                        if (arr[1].equals(a.get("id"))) {
                                            event = a;
                                        }
                                        if (arr[2].equals(a.get("id"))) {
                                            event2 = a;
                                        }
                                    }

                                }
                                if (hasClosingBrakets) {
                                    sbarroleparser.parse(attrsarr[syntcolumn].substring(attrsarr[syntcolumn].indexOf(')')), attrsarr[rolecolumn], attrsarr[wordcolumn]);
                                }

                            } while ((attrsline = attrsreader.readLine()) != null);
                            sbarroleparser = null;
                            break;


                        }
                    }
                    attrsreader.close();

                    if (event.isEmpty() || event2.isEmpty()) {
                        //throw new Exception("Event " + arr[1] + " or "+arr[2]+" not found in " + arr[0] + "same sentence);
                        System.err.println("Event " + arr[1] + " or " + arr[2] + " not found in " + arr[0] + " same sentence");
                        if (event.isEmpty()) {
                            event.put("SBARTMPevent", "diffsent");
                            event.put("PPevent", "diffsent");
                            //event.put("SBARTMPevent", "-");
                            //event.put("PPevent", "-");
                            event.put("tense", "-");
                            event.put("aspect", "-");
                            event.put("class","OCCURRENCE");
                        }
                        if (event2.isEmpty()) {
                            event2.put("SBARTMPevent", "diffsent");
                            event2.put("PPevent", "diffsent");
                            //event2.put("SBARTMPevent", "-");
                            //event2.put("PPevent", "-");
                            event2.put("tense", "-");
                            event2.put("aspect", "-");
                            event2.put("class","OCCURRENCE");
                        }
                    }
                    outfile.write(arr[0] + "|" + arr[1] + "|" + arr[2] + "|" + event.get("SBARTMPevent") + "|" + event.get("PPevent") + "|" + event.get("tense") + "-" + event.get("aspect") + "|" + event2.get("SBARTMPevent") + "|" + event2.get("PPevent") + "|" + event2.get("tense") + "-" + event2.get("aspect")+ "|" + event.get("class") + "|" + event2.get("class")  + "|" + arr[3] + "\n");
                    //outfile.write(arr[0] + "|" + arr[1] + "|" + arr[2] + "|" + event.get("class") + "|" + event.get("PPevent") + "|" + event.get("tense") + "-" + event.get("aspect") + "|" + event2.get("class") + "|" + event2.get("PPevent") + "|" + event2.get("tense") + "-" + event2.get("aspect") + "|" + arr[3] + "\n");
                    event = null;
                    event2 = null;



                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outfile != null) {
                    outfile.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");


            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;


        }
        return outputfile;


    }



    // FILL EVENT ATTRIBS
    public static void fill_event_attribs(String directory, String[] arr, ArrayList<HashMap<String, String>> events) {
        try {
            HashMap<String, String> attribs;
            BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "event-attributes.tab"));
            String attrsline;
            String[] attrsarr = null;
            String[] memarr = null;
            Boolean found = false;
            while ((attrsline = attrsreader.readLine()) != null) {
                attrsarr = attrsline.split("\t");
                // File found, look for sentence
                if (attrsarr[0].equals(arr[0])) {
                    String sentence = attrsarr[1];
                    do {
                        attrsarr = attrsline.split("\t");
                        if (!attrsarr[0].equals(arr[0])) {
                            if (!found) {
                                throw new Exception("Event " + arr[1] + " not found in " + arr[0]);
                            }
                            break;
                        }
                        if (!sentence.equals(attrsarr[1])) {
                            if (found) {
                                break;
                            } else {
                                events.clear();
                                sentence = attrsarr[1];
                            }
                        }
                        if (arr[1].equals(attrsarr[4])) {
                            found = true;
                        }
                        String id = attrsarr[4];
                        attribs = new HashMap<String, String>();
                        attribs.put("id", attrsarr[4]);
                        if (memarr != null) {
                            attribs.put(memarr[6], memarr[7]);
                            memarr = null;
                        }
                        attribs.put(attrsarr[6], attrsarr[7]);
                        attribs.put("sentN", attrsarr[1]);
                        attribs.put("tokN", attrsarr[2]);

                        while ((attrsline = attrsreader.readLine()) != null) {
                            attrsarr = attrsline.split("\t");
                            //System.out.println(attrsline);
                            if (id.equals(attrsarr[4])) {
                                attribs.put(attrsarr[6], attrsarr[7]);
                            } else {
                                memarr = attrsarr;
                                break;
                            }
                        }
                        events.add(new HashMap(attribs));
                        attribs = null;
                    } while ((attrsline = attrsreader.readLine()) != null);

                    break;


                }
            }

            attrsreader.close();
            if (!found) {
                throw new Exception("Event " + arr[1] + " not found in " + arr[0]);
            }

        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    public static void fill_event2_attribs(String directory, String[] arr, ArrayList<HashMap<String, String>> events) {
        try {
            HashMap<String, String> attribs;
            BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "event-attributes.tab"));
            String attrsline;
            String[] attrsarr = null;
            String[] memarr = null;
            Boolean found = false;
            while ((attrsline = attrsreader.readLine()) != null) {
                attrsarr = attrsline.split("\t");
                // File found, look for sentence
                if (attrsarr[0].equals(arr[0])) {
                    String sentence = attrsarr[1];
                    do {
                        attrsarr = attrsline.split("\t");
                        if (!attrsarr[0].equals(arr[0])) {
                            if (!found) {
                                throw new Exception("Event " + arr[2] + " not found in " + arr[0]);
                            }
                            break;
                        }
                        if (!sentence.equals(attrsarr[1])) {
                            if (found) {
                                break;
                            } else {
                                events.clear();
                                sentence = attrsarr[1];
                            }
                        }
                        if (arr[2].equals(attrsarr[4])) {
                            found = true;
                        }
                        String id = attrsarr[4];
                        attribs = new HashMap<String, String>();
                        attribs.put("id", attrsarr[4]);
                        if (memarr != null) {
                            attribs.put(memarr[6], memarr[7]);
                            memarr = null;
                        }
                        attribs.put(attrsarr[6], attrsarr[7]);
                        attribs.put("sentN", attrsarr[1]);
                        attribs.put("tokN", attrsarr[2]);

                        while ((attrsline = attrsreader.readLine()) != null) {
                            attrsarr = attrsline.split("\t");
                            if (id.equals(attrsarr[4])) {
                                attribs.put(attrsarr[6], attrsarr[7]);
                            } else {
                                memarr = attrsarr;
                                break;
                            }
                        }
                        events.add(new HashMap(attribs));
                        attribs = null;
                    } while ((attrsline = attrsreader.readLine()) != null);

                    break;


                }
            }

            attrsreader.close();
            if (!found) {
                throw new Exception("Event " + arr[2] + " not found in " + arr[0]);
            }

        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    // FILL TIMEX ATTRIBS
    public static void fill_timex_attribs(String directory, String[] arr, ArrayList<HashMap<String, String>> timexs, String sent) {
        try {
            HashMap<String, String> attribs;
            BufferedReader attrsreader = new BufferedReader(new FileReader(directory + "timex-attributes.tab"));
            String attrsline;
            String[] attrsarr = null;
            Boolean found = false;
            Boolean find = true;
            String sentence = "";

            if (arr[2].matches("(t0|e.*)")) {
                find = false;
                //System.out.println("-No find-");
            }

            while ((attrsline = attrsreader.readLine()) != null) {
                attrsarr = attrsline.split("\t");
                // File found, look for sentence
                if (attrsarr[0].equals(arr[0])) {
                    sentence = attrsarr[1];
                    do {
                        attrsarr = attrsline.split("\t");
                        //System.out.println("sentence "+sentence+"!="+sent);
                        if (!attrsarr[0].equals(arr[0])) {
                            if (find && !found) {
                                throw new Exception("Timex " + arr[2] + " not found in " + arr[0]);
                            }
                            break;
                        }
                        // look new sentence or break
                        if (!sentence.equals(attrsarr[1])) {
                            if (found) {
                                break;
                            } else {
                                timexs.clear();
                                sentence = attrsarr[1];
                            }
                        }
                        if ((!find && sentence.equals(sent)) || (find && arr[2].equals(attrsarr[4]))) {
                            found = true;
                            if (!sentence.equals(sent)) {
                                //throw new Exception(arr[0]+" Timex "+arr[2]+" found in different sentence "+sent+" ("+sentence+")");
                                System.out.println(arr[0] + " Timex " + arr[2] + " found in different sentence " + sent + " (" + sentence + ")");
                            }
                        }
                        attribs = new HashMap<String, String>();
                        attribs.put("id", attrsarr[4]);
                        attribs.put("sentN", attrsarr[1]);
                        attribs.put("tokN", attrsarr[2]);
                        if (attrsarr[6].equals("val")) {
                            attrsarr[6] = "value";
                        }
                        attribs.put(attrsarr[6], attrsarr[7]);
                        attrsline = attrsreader.readLine();
                        attrsarr = attrsline.split("\t");
                        if (attrsarr[6].equals("val")) {
                            attrsarr[6] = "value";
                        }
                        attribs.put(attrsarr[6], attrsarr[7]);
                        timexs.add(new HashMap(attribs));
                        attribs = null;
                    } while ((attrsline = attrsreader.readLine()) != null);

                    /*                    for(HashMap e:events){
                    System.out.println(e.get("id")+" "+e.get("tense"));
                    }
                    for(HashMap e:timexs){
                    System.out.println(e.get("id")+" "+e.get("type"));
                    }*/
                    break;


                }
            }
            if (!found) {
                timexs.clear();
                if (find) {
                    throw new Exception("Timex " + arr[2] + " not found in " + arr[0] + " (or sentence " + sentence + "!=" + sent + ") (find=" + find + ")");
                }
            }
            attrsreader.close();
            //System.out.println("Events/Timex in " + arr[0] + " with " + arr[1] + " e=" + events.size() + " t=" + timexs.size());
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    // FILL DCTS
    public static void fill_DCTs(String directory, HashMap<String, String> DCTs) {
        try {
            BufferedReader dctreader = new BufferedReader(new FileReader(directory + "dct.tab"));
            String line;
            while ((line = dctreader.readLine()) != null) {
                String[] linearr = line.split("\t");
                if (linearr[1].matches("[0-9]{8}")) {
                    linearr[1] = linearr[1].substring(0, 4) + "-" + linearr[1].substring(4, 6) + "-" + linearr[1].substring(6, 8);
                }
                if (linearr.length == 2) {
                    DCTs.put(linearr[0], linearr[1]);
                } else {
                    throw new Exception("Malformed DCT");
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TempEval):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

    }



}
