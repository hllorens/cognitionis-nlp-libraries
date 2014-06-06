package TIMEK;

import java.io.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class VerbAttributesK {
    
    public static String events_attribs_rules_EN(String pipesfile, int iob2col, int wordcol, int poscol, int lemmacol, int assertypecol,int tensecol) {
        String outputfile = null;
        try {
            int attrscol = iob2col + 1;
            outputfile = pipesfile + "-rules";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile));

            try {
                String pipesline;
                String[] pipesarr = null;
                String[] pipesarr_prev = null;
                String[] pipesarr_prev2 = null;

                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");

                    if (!pipesarr[attrscol].equals("-")) {
                        String polarity = "POS";
                        String pos = "VERB";
                        String tense = "PAST";
                        String aspect = "NONE";
                        String modality = "NONE";
                        //System.err.println(pipesline + " ----> "+pipesarr[iob2col]);
                        polarity = pipesarr[assertypecol].substring(0, 3).toUpperCase(); // POS [default], NEG
                        String temp = pipesarr[poscol];// VERB [default], NOUN, ADJECTIVE, PREPOSITION, OTHER
                        if (temp.startsWith("V")) {
                            pos = "VERB";
                        } else {
                            if (temp.startsWith("N")) {
                                pos = "NOUN";
                            } else {
                                if (temp.startsWith("J")) {
                                    pos = "ADJECTIVE";
                                } else {
                                    if (temp.matches("(IN|TO)")) {
                                        pos = "PREPOSITION";
                                    } else {
                                        pos = "OTHER";
                                    }
                                }
                            }
                        }



                        if (!temp.startsWith("V")) {
                            tense = "NONE";
                            aspect = "NONE";
                        } else {
                            //tense // PAST[default], NONE, PRESENT, INFINITIVE, PRESPART, FUTURE, PASTPART
                            tense = pipesarr[tensecol].toUpperCase();
                            if (tense.matches("PAST-PERFECT")) {
                                tense = "PAST";
                                aspect = "PERFECTIVE";
                            }
                            if (tense.matches("CONDITIONAL")) {
                                tense = "NONE";
                                modality = "would";
                            }

                            // 3rd person present
                            if (temp.equals("VBZ")) {
                                tense = "PRESENT";
                            }
                            // Infinitive
                            if (pipesarr_prev2 != null && pipesarr_prev[poscol].equals("TO") || (pipesarr_prev[wordcol].matches("(?i)(have|be)") && pipesarr_prev2[poscol].equals("TO"))) {
                                tense = "INFINITIVE";
                                if (temp.equals("VBG") && pipesarr_prev[wordcol].equalsIgnoreCase("be")) {
                                    aspect = "PROGRESSIVE";
                                }
                                if (temp.equals("VBN") && pipesarr_prev[wordcol].equalsIgnoreCase("have")) {
                                    aspect = "PERFECTIVE";
                                }
                            }

                            // Progressive
                            if (pipesarr_prev != null && temp.equals("VBG")) {
                                if ((!pipesarr_prev[lemmacol].equals("be") && !pipesarr_prev[wordcol].equalsIgnoreCase("not"))
                                        || (pipesarr_prev2 != null && pipesarr_prev[wordcol].equalsIgnoreCase("not") && !pipesarr_prev2[lemmacol].equals("be"))) {
                                    tense = "PRESPART";
                                } else {
                                    aspect = "PROGRESSIVE";
                                    if (pipesarr_prev[wordcol].equals("been")) {
                                        aspect = "PERFECTIVE_PROGRESSIVE";
                                    }
                                }
                            }

                            // Perfective
                            if (pipesarr_prev != null && temp.startsWith("V")) {
                                if (pipesarr_prev[lemmacol].equals("have")) {
                                    aspect = "PERFECTIVE";
                                } else {
                                    if (pipesarr_prev[wordcol].equals("been")) {
                                        aspect = "PERFECTIVE_PROGRESSIVE";
                                    }
                                }
                            }

                            // Past participle (VBN) ... AMBIGUOUS...

                            // Conditional
                            if (pipesarr_prev != null && pipesarr_prev[wordcol].matches("(would|could|can|should|must|may|might)(n't)?")) {
                                tense = "NONE";
                                modality = pipesarr_prev[wordcol];
                            }
                            if (pipesarr_prev2 != null && pipesarr_prev2[wordcol].matches("(would|could|can|should|must|may|might)") && pipesarr_prev[wordcol].matches("(not|n't|be|have)")) {
                                tense = "NONE";
                                modality = pipesarr_prev[wordcol];
                            }
                        }

                        outfile.write(pipesline.substring(0, pipesline.lastIndexOf('|')) + "|polarity=" + polarity + ";modality=" + modality + ";pos=" + pos + ";tense=" + tense + ";aspect=" + aspect + ";" + pipesline.substring(pipesline.lastIndexOf('|') + 1) + "\n");
                    } else {
                        outfile.write(pipesline + "\n");
                    }

                    pipesarr_prev2 = pipesarr_prev;
                    pipesarr_prev = pipesarr;
                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
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

    public static String events_attribs_rules_ES(String pipesfile, int iob2col, int wordcol, int poscol, int lemmacol, int assertypecol) {
        String outputfile = null;
        try {
            int attrscol = iob2col + 1;
            outputfile = pipesfile + "-rules";
            BufferedWriter outfile = new BufferedWriter(new FileWriter(outputfile));
            BufferedReader pipesreader = new BufferedReader(new FileReader(pipesfile));

            try {
                String pipesline;
                String[] pipesarr = null;
                String[] pipesarr_prev = null;
                String[] pipesarr_prev2 = null;

                while ((pipesline = pipesreader.readLine()) != null) {
                    pipesarr = pipesline.split("\\|");

                    if (!pipesarr[attrscol].equals("-")) {
                        String polarity = "POS";
                        String tense = "PAST";
                        String aspect = "NONE";
                        String mood = "NONE";
                        String vform = "NONE";
                        //System.err.println(pipesline + " ----> "+pipesarr[iob2col]);
                        polarity = pipesarr[assertypecol].substring(0, 3).toUpperCase(); // POS [default], NEG
                        String temp = pipesarr[poscol];// VERB [default], NOUN, ADJECTIVE, PREPOSITION, OTHER

                        if (!temp.startsWith("V")) {
                            tense = "NONE";
                            aspect = "NONE";
                            mood = "NONE";
                        } else {

// PIERDE MODALITY Y POS ... GANA MOOD y vform (siguen siendo 6 características)

                            /*   53 CON (conditional)
                            336 FUT (future)
                            273 IMP (IMPERFECT)
                            1232 PAS (past)
                            4029 PRE (present)
                             */


                            /* aspect ----------> waw quin awen
                             *       8
                            3943 IMPERFECTIVE
                            33 IMPERFECTIVE_PROGRESSIVE
                            4818 NONE
                            1875 PERFECTIVE
                            8 PERFECTIVE_PROGRESSIVE
                             */


                            // mood
/*
                            60 GER (UND)
                            29 IMP(ERATIVE)
                            3405 IND (ICATIVE)
                            1706 INF (INFINITIVE)
                            509 PAS (PASTPARTICILPE)
                            214 SUB (SUBJUNCTIVE)
                             */

                            /*
                            7 ???????????-> corregir pq si estan vacios petaran...
                            79 CONDITIONAL
                            5 IMPERATIVE
                            4146 INDICATIVE
                            6175 NONE
                            273 SUBJUNCTIVE
                             */

                            /* vform
                             *     103 GERUNDIVE
                            1260 INFINITIVE
                            8913 NONE
                            408 PARTICIPLE
                             *
                             */

                            //tense // PAST[default], NONE, PRESENT, INFINITIVE, PRESPART, FUTURE, PASTPART
                            //tense = pipesarr[pipesfile.getColumn("tense")].toUpperCase();
                            tense = temp.substring(1, 4);
                            aspect = "IMPERFECTIVE";

                            // tense
                            if (tense.equals("PRE")) {
                                tense = "PRESENT";
                                //aspect = "NONE";
                            }
                            if (tense.equals("CON")) {
                                tense = "NONE";
                                mood = "CONDITIONAL";
                                aspect = "IMPERFECTIVE";
                                vform = "NONE";
                            }
                            if (tense.equals("FUT")) {
                                tense = "FUTURE";
                            }
                            if (tense.equals("PAS")) {
                                tense = "PAST";
                            }
                            if (tense.equals("IMP")) {
                                tense = "PAST";
                                if (pipesarr[wordcol].endsWith("ndo")) {
                                    aspect = "IMPERFECTIVE_PROGRESSIVE";
                                } else {
                                    aspect = "IMPERFECTIVE";
                                }

                            }

                            // mood
                            if (mood.equals("NONE")) {
                                mood = temp.substring(4);
                                if (mood.equals("IND")) {
                                    mood = "INDICATIVE";
                                }
                                if (mood.equals("SUB")) {
                                    mood = "SUBJUNCTIVE";
                                }
                                if (mood.equals("IMP")) {
                                    mood = "IMPERATIVE";
                                }
                                if (mood.equals("INF")) {
                                    vform = "INFINITIVE";
                                    tense = "NONE";
                                }
                                if (mood.equals("GER")) {
                                    vform = "GERUNDIVE";
                                    if (pipesarr_prev != null && pipesarr_prev[poscol].startsWith("V")) {
                                        aspect = "IMPERFECTIVE_PROGRESSIVE";
                                    }
                                    if (pipesarr_prev2 != null && pipesarr_prev2[lemmacol].equals("haber")) {
                                        aspect = "PERFECTIVE_PROGRESSIVE";
                                        if (pipesarr_prev[wordcol].contains("í")) {
                                            tense = "PAST";
                                        }
                                    }
                                }
                                if (mood.equals("PAS")) {
                                    mood = "INDICATIVE";
                                    if (pipesarr[wordcol].endsWith("ndo")) {
                                        aspect = "PERFECTIVE_PROGRESSIVE";
                                    } else {
                                        aspect = "PERFECTIVE";
                                    }
                                    if (pipesarr_prev[wordcol].contains("í")) {
                                        tense = "PAST";
                                    }
                                    vform = "PARTICIPLE";
                                }
                            }

                            // Reglas adhoc pq los verbos no tiene el tense en el propio verbo sino en sus auxiliares...

                            // Perfective
                            if (pipesarr_prev != null && pipesarr_prev[lemmacol].equals("haber")) {
                                aspect = "PERFECTIVE";
                                if (pipesarr_prev[wordcol].contains("í")) {
                                    tense = "PAST";
                                }
                            }
                        }

                        outfile.write(pipesline.substring(0, pipesline.lastIndexOf('|')) + "|polarity=" + polarity + ";mood=" + mood + ";tense=" + tense + ";aspect=" + aspect + ";vform=" + vform + ";" + pipesline.substring(pipesline.lastIndexOf('|') + 1) + "\n");
                    } else {
                        outfile.write(pipesline + "\n");
                    }

                    pipesarr_prev2 = pipesarr_prev;
                    pipesarr_prev = pipesarr;
                }

            } finally {
                if (pipesreader != null) {
                    pipesreader.close();
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


}
