package com.cognitionis.knowledgek.TIMEK;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
import java.text.DateFormatSymbols;
import java.util.*;
import org.joda.time.*;
import org.joda.time.format.*;
import com.cognitionis.knowledgek.NUMEK.NUMEK;
import java.util.regex.*;

/*%%%%%%%%%%%%%%%%%%%%
TODO TODO BCE/CE  BC/AD
 */
public class TIMEK {

    private Locale locale;
    private NUMEK numek;

    public static enum AvailableLocales {

        EN, US, GB, ES;
    }
    public String TUnit_re, TMonths_re, TWeekdays_re, Decades_re, TOD_re, Seasons_re, SET_re, TIMEgranul_re;
    // ENGLISH
    public static String TUnit_re_EN = "(?i)(?:seconds|minute|hour|day|week|month|quarter|year|centur(y|ies)|millennium)s?";
    // hack seconds because the ambiguity with ordinal
    public static String TMonths_re_EN = "(?i)(?:(?:January|February|March|April|May|June|July|August|September|October|November|December)|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)(?:\\.)?)";
    public static String TWeekdays_re_EN = "(?i)(?:Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|(Mon|Tue|Wed|Thu|Fri|Sat|Sun)(?:\\.)?)";
    public static String Decades_re_EN = "(?i)(?:twenties|thirties|forties|fifties|sixties|seventies|eighties|nineties)";
    public static String TOD_re_EN = "(?i)(?:morning|afternoon|evening|night|midnight|overnight)";
    public static String Seasons_re_EN = "(?i)(?:spring|summer|(autumn|fall)|winter)";
    public static String SET_re_EN = "(?i)(?:each(\\s|_)+.*|every(?:\\s|_)+.*|.*" + TWeekdays_re_EN + "s.*|(?:hour|day|week|month|quarter|year)ly)";
    public static String TIMEgranul_re_EN = "(?i)(?:seconds|minute(?:s)?|hour(?:s)?|" + TOD_re_EN + ")";
    // SPANISH
    public static String TUnit_re_ES = "(?i)(?:segundos|minuto|hora|día|semana|mes(?:es)?|trimestre|año|siglo|milenio)s?";
    // hack segundos because the ambiguity with ordinal
    public static String TMonths_re_ES = "(?i)(?:(?:enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)|(?:ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)(?:\\.)?)";
    public static String TWeekdays_re_ES = "(?i)(?:lunes|martes|miércoles|jueves|viernes|sábado|domingo)";
    public static String Decades_re_ES = "(?i)años_(?:veinte|treinta|cuarente|cincuenta|sesenta|setenta|ochenta|noventa|[2-9]0)";
    public static String TOD_re_ES = "(?i)(?:madrugada|mañana|mediodía|tarde|noche|medianoche)";
    public static String Seasons_re_ES = "(?i)(?:primavera|verano|otoño|invierno)";
    public static String SET_re_ES = "(?i)(?:cada(?:\\s|_)+.*|todos(?:\\s|_)+los.*|.*" + TWeekdays_re_ES + "s.*|(?:diari|diaria|semanal|mensual|trimestral|anual|bienal)(?:mente|(?:a|e|o)(?:s)?))";
    public static String TIMEgranul_re_ES = "(?i)(?:segundos|minuto(?:s)?|hora(?:s)?|" + TOD_re_ES + ")";
    public final static HashMap<String, Integer> TUnits_EN = new HashMap<String, Integer>();

    static {
        TUnits_EN.put("millennium", 1000);
        TUnits_EN.put("century", 100);
        TUnits_EN.put("decade", 10);
        TUnits_EN.put("year", GregorianCalendar.YEAR);
        TUnits_EN.put("quarter", 1); // trimestre
        TUnits_EN.put("month", GregorianCalendar.MONTH);
        TUnits_EN.put("week", GregorianCalendar.WEEK_OF_YEAR);
        TUnits_EN.put("day", GregorianCalendar.DAY_OF_MONTH);
        TUnits_EN.put("hour", GregorianCalendar.HOUR_OF_DAY);
        TUnits_EN.put("minute", GregorianCalendar.MINUTE);
        TUnits_EN.put("second", GregorianCalendar.SECOND);
    }
    public final static HashMap<String, Integer> TUnits_ES = new HashMap<String, Integer>();

    static {
        TUnits_ES.put("milenio", 1000);
        TUnits_ES.put("siglo", 100);
        TUnits_ES.put("decada", 10);
        TUnits_ES.put("año", GregorianCalendar.YEAR);
        TUnits_ES.put("trimestre", 1);
        TUnits_ES.put("mes", GregorianCalendar.MONTH);
        TUnits_ES.put("semana", GregorianCalendar.WEEK_OF_YEAR);
        TUnits_ES.put("día", GregorianCalendar.DAY_OF_MONTH);
        TUnits_ES.put("hora", GregorianCalendar.HOUR_OF_DAY);
        TUnits_ES.put("minuto", GregorianCalendar.MINUTE);
        TUnits_ES.put("segundo", GregorianCalendar.SECOND);
    }
    public final static HashMap<String, Integer> decades_EN = new HashMap<String, Integer>();

    static {
        decades_EN.put("twenties", 192);
        decades_EN.put("thirties", 193);
        decades_EN.put("forties", 194);
        decades_EN.put("fifties", 195);
        decades_EN.put("sixties", 196);
        decades_EN.put("seventies", 197);
        decades_EN.put("eighties", 198);
        decades_EN.put("nineties", 199);
    }
    public final static HashMap<String, Integer> decades_ES = new HashMap<String, Integer>();

    static {
        decades_ES.put("años_veinte", 192);
        decades_ES.put("años_20", 192);
        decades_ES.put("años_treinta", 193);
        decades_ES.put("años_30", 193);
        decades_ES.put("años_cuarenta", 194);
        decades_ES.put("años_40", 194);
        decades_ES.put("años_cincuenta", 195);
        decades_ES.put("años_50", 195);
        decades_ES.put("años_sesenta", 196);
        decades_ES.put("años_60", 196);
        decades_ES.put("años_setenta", 197);
        decades_ES.put("años_70", 197);
        decades_ES.put("años_ochenta", 198);
        decades_ES.put("años_80", 198);
        decades_ES.put("años_noventa", 199);
        decades_ES.put("años_90", 199);
    }
    public HashMap<String, Integer> TUnits;
    public HashMap<String, Integer> Weekdays;
    public HashMap<String, Integer> Yearmonths;
    public static HashMap<String, Integer> decades;

    public TIMEK() {
        this(Locale.getDefault());
    }

    public TIMEK(Locale l) {
        loadWeekdays(l);
        loadYearmonths(l);
        loadManualinfo(l);
        numek = new NUMEK(l);
        locale = l;
    }
    public static String granul_years = "yyyy";
    public static String granul_months = "yyyy-MM";
    public static String granul_days = "yyyy-MM-dd";
    public static String granul_time = "yyyy-MM-dd'T'HH:mm";
    public static String granul_seconds = "yyyy-MM-dd'T'HH:mm:ss";
    public static String granul_weeks = "yyyy-'W'ww";

    public void loadWeekdays(Locale l) {
        Weekdays = null;
        Weekdays = new HashMap<String, Integer>();
        String[] temp = new DateFormatSymbols(l).getWeekdays();
        for (int i = 1; i < temp.length; i++) {
            Weekdays.put(temp[i].toLowerCase(l), i);
        }
        temp = new DateFormatSymbols(l).getShortWeekdays();
        for (int i = 1; i < temp.length; i++) {
            Weekdays.put(temp[i].toLowerCase(l), i);
        }
        //System.out.println(Weekdays.toString());
    }

    public void loadYearmonths(Locale l) {
        Yearmonths = null;
        Yearmonths = new HashMap<String, Integer>();
        String[] temp = new DateFormatSymbols(l).getMonths();
        for (int i = 0; i < temp.length - 1; i++) {
            //System.out.println(i+temp[i]);
            Yearmonths.put(temp[i].toLowerCase(l), i);
        }
        temp = new DateFormatSymbols(l).getShortMonths();
        for (int i = 0; i < temp.length - 1; i++) {
            //System.out.println(i+temp[i]);
            Yearmonths.put(temp[i].toLowerCase(l), i);
        }
        //System.out.println(Yearmonths.toString());
    }

    public void loadManualinfo(Locale l) {
        String lang = l.getLanguage().toUpperCase();
        try {
            switch (AvailableLocales.valueOf(lang)) {
                case ES:
                    TUnits = TUnits_ES;
                    TUnit_re = TUnit_re_ES;
                    TMonths_re = TMonths_re_ES;
                    TWeekdays_re = TWeekdays_re_ES;
                    Decades_re = Decades_re_ES;
                    TOD_re = TOD_re_ES;
                    Seasons_re = Seasons_re_ES;
                    decades = decades_ES;
                    SET_re = SET_re_ES;
                    TIMEgranul_re = TIMEgranul_re_ES;
                    break;
                case EN:
                case US:
                case GB:
                    TUnits = TUnits_EN;
                    TUnit_re = TUnit_re_EN;
                    TMonths_re = TMonths_re_EN;
                    TWeekdays_re = TWeekdays_re_EN;
                    Decades_re = Decades_re_EN;
                    TOD_re = TOD_re_EN;
                    Seasons_re = Seasons_re_EN;
                    decades = decades_EN;
                    SET_re = SET_re_EN;
                    TIMEgranul_re = TIMEgranul_re_EN;
                    break;
            }
        } catch (Exception e) {
            TUnits = TUnits_EN;
            TUnit_re = TUnit_re_EN;
            TMonths_re = TMonths_re_EN;
            TWeekdays_re = TWeekdays_re_EN;
            Decades_re = Decades_re_EN;
            TOD_re = TOD_re_EN;
            Seasons_re = Seasons_re_EN;
            decades = decades_EN;
        }
    }

    // Interface to an OWL ontology...
    // USE PROTEGEE TO DESIGN IT... (MAY BE WHEN DESIGN, OWL 2 IS AVAILABLE BUT OWL 1 IS ENOUGH ANYWAY
    // TIMEK must rely on NUMEK (numeric knowledge is basic for many things) -> units, tens, hundreds, thousends, millions, billions, trillions
    // remember multilingual mapping of anyK -> as well as wikipedia, wiktionary (mappings) aid for translations.
    // TIMEE rules for select references use verbs (había estado es una referencia respecto a otra referencia del pasado),
    // el presente, si no es figurado (relatado, reported speech) es referente a la fecha de creación.

    /*    functions:
    - annotate(pipesfile (min word-pos lemma)), new column TIMEK with TIME RELEVENT TAGS (Multi-tags)
    - get_relation()
    - exists_instance
    - exists_class...
     */

    /*
     * 1a pasada columna TIMEK
     * 2a pasada access per a resoldre ISO...,
     *
     */
    /**
     * Returns a valid ISO date from a date in a valid format (default locale)
     * or null if the date is not valid or can not be parsed
     *
     * @param date
     * @return String: the ISO date
     */
    public String toISO8601(String date) {
        return toISO8601(date, locale);
    }

    /**
     * Returns a valid ISO date from a date in a valid format (specified locale)
     * or null if the date is not valid or can not be parsed
     * ISO8601: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ" NOT WHITE SPACES
     *
     * @param date
     * @param loc
     * @return String: the ISO date
     */
    public String toISO8601(String date, Locale loc) {
        String iso = null;

        // TENER EN CUENTA LA GRANULARIDAD DE LA FECHA PARA DEVOLVER LO MISMO...
        // TENER EN CUENTA LOS LOCALES (ESPAÑOL, INGLÉS)

        //System.out.println(date);
        try {
            DateTimeFormatter fmt;

            date = date.replaceAll("mid-", "");
            date = " " + date + " ";
            if (locale.getLanguage().equalsIgnoreCase("en")) {
                date = date.replaceAll(" the ", " ").replaceAll("\\s*-\\s*", "-").replaceAll("\\s*/\\s*", "/").replaceAll("\\s+", " ").trim();
                date = date.replaceAll("^(.*) ([0-9]+)(th|st|nd|rd) (.*)$", "$1 $2 $3 $4");
            }

            if (locale.getLanguage().equalsIgnoreCase("es")) {
                date = date.replaceAll(" (los|las|el|la|en) ", "").replaceAll("\\s*-\\s*", "-").replaceAll("\\s*/\\s*", "/").replaceAll("\\s+", " ");
                date = date.replaceAll(" (primero|uno) ", " 1 ").trim();
            }

            // centuries
            if (date.matches("^[0-9]+ century$")) {
                return "" + (Integer.parseInt(date.split(" ")[0].replace("([0-9]{1,2}).*", "$1")));
            }


            // find the parsing format
            if (!date.contains(" ")) { // UNIGRAM (ONE TOKEN DATE)
                // unformated numeric dates
                if (date.matches("[0-9]+")) {
                    switch (date.length()) {
                        case 2: // 2 digit year
                            return guessYear(date);
                        case 4: // already ISO
                            return date;
                        case 6: // year-month
                            iso = date.substring(0, 4) + "-" + date.substring(4, 6);
                            fmt = DateTimeFormat.forPattern("yyyy-MM");
                            return fmt.parseDateTime(iso).toString("yyyy-MM");
                        case 8: // year-month-day
                            date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                            fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
                            return fmt.parseDateTime(date).toString("yyyy-MM-dd");
                    }

                }


                // numeric decades
                if (date.matches("[0-9]+s")) {
                    switch (date.length()) {
                        case 3:
                            iso = guessYear(date.substring(0, 2));
                            return iso.substring(0, 3);
                        case 5:
                            return date.substring(0, 3);
                        default:
                            throw new Exception("Unknown expression: " + date);
                    }

                }



                // spelled decades
                if (decades.containsKey(date)) {
                    return decades.get(date).toString();
                }


                //normalize two-digit year
                if (date.matches("(?:[0-9]+[./-])?(?:[0-9]+|" + TMonths_re + ")[./-][0-9]{2}")) {
                    date = date.substring(0, date.length() - 2) + guessYear(date.substring(date.length() - 2));
                }
                //remove "." from dates 10/nov./2001 --> 10/nov/2001
                date = date.replaceAll("[.]([./-])", "$1");

                // check if it is already ISO but mantain the original with the granularity
                try {
                    fmt = ISODateTimeFormat.dateOptionalTimeParser();
                    fmt.parseDateTime(ISOclean(date));
                    return date;
                } catch (Exception e) {
                    iso = null; // just NOOP
                }

                /*if(date.matches(".*"+TMonths_re+"[/.-].*")){
                date=date.replaceAll("(.*)("+TMonths_re+")([/.-].*)", "$1\\I$2$3");
                }*/

                // Dates with separators... -/
                // MM-yyyy
                try {
                    fmt = DateTimeFormat.forPattern("MM-yyyy");
                    return fmt.parseDateTime(date).toString("yyyy-MM");
                } catch (Exception e) {
                    iso = null;
                }
                // MM/yyyy
                try {
                    fmt = DateTimeFormat.forPattern("MM/yyyy");
                    return fmt.parseDateTime(date).toString("yyyy-MM");
                } catch (Exception e) {
                    iso = null;
                }


                // dd-MM-yyyy
                try {
                    fmt = DateTimeFormat.forPattern("dd-MM-yyyy");
                    return fmt.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }
                // dd/MM/yyyy
                try {
                    fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
                    return fmt.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }


                // dd-MMMM-yyyy
                try {
                    fmt = DateTimeFormat.forPattern("dd-MMMM-yyyy");
                    DateTimeFormatter fmt_localized = fmt.withLocale(locale);
                    return fmt_localized.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }
                // dd/MMMM/yyyy
                try {
                    fmt = DateTimeFormat.forPattern("dd/MMMM/yyyy");
                    DateTimeFormatter fmt_localized = fmt.withLocale(locale);
                    return fmt_localized.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }

                // MM-dd-yyyy
                try {
                    fmt = DateTimeFormat.forPattern("MM-dd-yyyy");
                    return fmt.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }
                // MM/dd/yyyy
                try {
                    fmt = DateTimeFormat.forPattern("MM/dd/yyyy");
                    return fmt.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }


            } else { // multiword date/time
                if (locale.getLanguage().equalsIgnoreCase("en") && date.matches("(?i)(a good part of|end of|this|that|YEAR|(the )?early|(the )?late|fiscal) [0-9]+(s)?")) {
                    return toISO8601(date.substring(date.lastIndexOf(' ') + 1), loc);
                }
                if (locale.getLanguage().equalsIgnoreCase("es") && date.matches("(?i)(una buena parte|al final de|este|ese|(el )?año|(a )?principios de|(a )?finales de) ([0-9]+|" + Decades_re + ")(s)?")) {
                    return toISO8601(date.substring(date.lastIndexOf(' ') + 1), loc);
                }


                // useful replacements
                date = " " + date + " ";
                if (locale.getLanguage().equalsIgnoreCase("en")) {
                    date = date.replaceAll(" of ", " ").replaceAll("[.,]", " ").replaceAll("\\s+", " ").trim();
                }
                if (locale.getLanguage().equalsIgnoreCase("es")) {
                    date = date.replaceAll(" (?:el|la|los|las) ", " ").replaceAll(" de(?:l)? ", " ").replaceAll("[.,]", " ").replaceAll(" día ", " ").replaceAll("\\s+", " ").trim();
                }


                // spelled decades (spanish). HACK, todo década de los...
                if (date.matches(".*años.*")) {
                    String date_ = date.replaceAll(" ", "_");
                    date_ = date_.substring(date_.indexOf("años"));
                    if (decades.containsKey(date_)) {
                        return decades.get(date_).toString();
                    }
                }


                // MMMM yyyy
                try {
                    fmt = DateTimeFormat.forPattern("MMMM yyyy");
                    DateTimeFormatter fmt_localized = fmt.withLocale(locale);
                    return fmt_localized.parseDateTime(date).toString("yyyy-MM");
                } catch (Exception e) {
                    iso = null;
                }

                // MMMM dd yyyy
                try {
                    fmt = DateTimeFormat.forPattern("MMMM dd yyyy");
                    DateTimeFormatter fmt_localized = fmt.withLocale(locale);
                    return fmt_localized.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }

                // dd MMMM yyyy
                try {
                    fmt = DateTimeFormat.forPattern("dd MMMM yyyy");
                    DateTimeFormatter fmt_localized = fmt.withLocale(locale);
                    return fmt_localized.parseDateTime(date).toString("yyyy-MM-dd");
                } catch (Exception e) {
                    iso = null;
                }

                // Seasons
                if (locale.getLanguage().equalsIgnoreCase("en") && date.matches("(.* )?" + Seasons_re + " [0-9]{4}")) {
                    String[] temp = date.split(" ");
                    if (date.matches(".*winter.*")) {
                        return temp[temp.length - 1] + "-WI";
                    }
                    if (date.matches(".*spring.*")) {
                        return temp[temp.length - 1] + "-SP";
                    }
                    if (date.matches(".*summer.*")) {
                        return temp[temp.length - 1] + "-SU";
                    }
                    if (date.matches(".*(autumn|fall).*")) {
                        return temp[temp.length - 1] + "-FA";
                    }
                }
                if (locale.getLanguage().equalsIgnoreCase("es") && date.matches("(.* )?" + Seasons_re + " (de(l)? )?[0-9]{4}")) {
                    String[] temp = date.split(" ");
                    if (date.matches(".*invierno.*")) {
                        return temp[temp.length - 1] + "-WI";
                    }
                    if (date.matches(".*primavera.*")) {
                        return temp[temp.length - 1] + "-SP";
                    }
                    if (date.matches(".*verano.*")) {
                        return temp[temp.length - 1] + "-SU";
                    }
                    if (date.matches(".*otoño.*")) {
                        return temp[temp.length - 1] + "-FA";
                    }
                }

            }


        } catch (Exception e) {
            System.err.println("Warnings found (TIMEK):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return "PRESENT_REF"; // by default
        }

        return "PRESENT_REF"; // by default
    }

    /**
     * Returns a 4 digit year guessed from a 2 digit representation.
     *
     * TODO TODO TODO should use DCT instead of current system date
     *
     * @param ambiguous_year
     * @return
     */
    public static String guessYear(String ambiguous_year) { //, String dct) {
        // Only guess 2 digit years, the rest are guessed to 000X or 0XXX
        if (ambiguous_year.length() != 2 || !ambiguous_year.matches("[0-9]+")) {
            return ambiguous_year;
        }
        int ambigyear = Integer.parseInt(ambiguous_year);
        DateTime dt = new DateTime();  // TODO SHOULD USE DCT...
        int curryear = dt.getYearOfCentury();
        int currcentury = Integer.parseInt((new Integer(dt.getYear()).toString().substring(0, 2))) * 100;
        if (curryear < ambigyear) {
            currcentury -= 100;
        }
        return new Integer(ambigyear + currcentury).toString();

    }

    /**
     * Returns the ISO8601 representation of an implicit date (e.g., Monday)
     * @param TE        timex normText
     * @param TEpat     timex pattern
     * @param tense
     * @param TEref     timex temporal reference (dct or reference point)
     * @return          ISO8601 String
     */
    public String obtainImplicitDate(String TE, String TEpat, String tense, String TEref) {
        //TE REF AND TE VAL MUST BE ISO
        //Granularity is defined in TEvalue
        //String iso = TEref; // by default return the reference date
        //System.out.println(TEref + "-" + TE + "-" + TEpat + "-" + tense + "-");
        TE = TE.toLowerCase(locale);
        String[] text_arr = null;
        String[] pat_arr = null;
        Date refdate = null;
        try {
            refdate = new DateTime(TEref).toDate();
        } catch (Exception e) {
            if (TEref.substring(0, 4).matches("[0-9]+")) {
                refdate = new DateTime(TEref.substring(0, 4)).toDate();
            } else {
                System.err.println("Invalid ref date: " + TEref);
                System.exit(0);
            }
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(refdate);

        if (tense.startsWith("past-") || tense.startsWith("present-perfect")) {
            tense = "past";
        } else {
            if (tense.equals("conditional")) {
                tense = "future";
            } else {
                if (tense.contains("-")) {
                    tense = tense.substring(0, tense.indexOf('-'));
                }
                if (!tense.matches("(?:past|present|future)")) {
                    tense = "present";
                }
            }
        }




        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println(TE + " - " + TEpat + " - " + tense);
        }

        TE = TE.replaceAll("_", " ");
        TEpat = TEpat.replaceAll("_", " ");

        TE = " " + TE + " ";
        TEpat = " " + TEpat + " ";







        // HACK FOR MISSDETECTED ISO'S
        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(?i) (a good part of|end of|this|that|YEAR|(the )?early|(the )?late(r)?|fiscal) [0-9]{2,4}(s)? ")) {
            TE = TE.trim();
            return toISO8601(TE.substring(TE.lastIndexOf(' ') + 1), locale);
        }
        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(?i).* (of|late as) [0-9]{4} ")) {
            TE = TE.trim();
            return toISO8601(TE.substring(TE.lastIndexOf(' ') + 1), locale);
        }
        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(?i) (early|late) " + TMonths_re + " [0-9]{4} ")) {
            TE = TE.trim();
            return toISO8601(TE.substring(TE.indexOf(' ') + 1), locale);
        }

        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(?i) (the )?(first|last) half (of )?(the )?(' )?[0-9]+(s)?( s)? ")) {
            if (TE.endsWith("0 s ")) {
                TE = TE.replace("0 s ", "0s ");
            }
            TE = TE.trim();
            return toISO8601(TE.substring(TE.lastIndexOf(' ') + 1), locale);
        }
        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches("(?i) (una buena parte|al final de|este|ese|(el )?año|(a )?principios de|(a )?finales de) ([0-9]|" + Decades_re + ")(s)? ")) {
            TE = TE.trim();
            return toISO8601(TE.substring(TE.lastIndexOf(' ') + 1), locale);
        }


        if (locale.getLanguage().equalsIgnoreCase("en")) {
            TE = TE.replaceAll(" the ", " ").replaceAll(" of ", " ").replaceAll(" (then|almost|at|o'clock|middle of|around|a flat|nearly) ", " ").replaceAll("[.,]", " ").replaceAll("\\s+", " ");
            TEpat = TEpat.replaceAll(" the ", " ").replaceAll(" of ", " ").replaceAll(" (then|almost|at|o'clock|middle of|around|a flat|nearly) ", " ").replaceAll("[.,]", " ").replaceAll("\\s+", " ");
        }







        if (locale.getLanguage().equalsIgnoreCase("en")) {
            if (TE.matches("(?i) (the )?end (of )?(the )?month")) {
                return new DateTime(cal.getTime()).toString(granul_months) + "-30";
            }
            TE = TE.replaceAll(" (end|beginning) ", " ").replaceAll("\\s+", " ");
            TEpat = TEpat.replaceAll(" (end|beginning) ", " ").replaceAll("\\s+", " ");
        }

        if (locale.getLanguage().equalsIgnoreCase("es")) {
            if (TE.matches(".* (la|esta) mañana.*")) { // ambiguedad mañana (hoy por la mañana)
                //System.out.println("Hack mañana (morning)");
                TE = "hoy mañana";
                TEpat = "hoy mañana";
            }
            //CAMBIADO DEL TEMPEVAL PQ SE CARGA LAS HORAS Y LOS TODs
            //if (TE.matches(".* ayer .*")) {
            //TE = "ayer";
            //TEpat = TE;
            //}
            //if (TE.matches(".* hoy .*")) {
            //TE = "hoy";
            //TEpat = TE;
            //}
            if (TEpat.matches(".* mañana (, )?TWeekday.*")) {
                TE = "mañana";
                TEpat = TE;
            }

            TE = TE.replaceAll(" (,|el|la|los|las|de(l)?|final(es)? de(l)?|misma|del|por|en punto) ", " ").replaceAll("que viene", "queviene").replaceAll("\\s+", " ");
            TEpat = TEpat.replaceAll(" (,|el|la|los|las|de(l)?|final(es)? de(l)?|misma|del|por|en punto) ", " ").replaceAll("que viene", "queviene").replaceAll("\\s+", " ");
            TE = TE.replaceAll(" (primero|uno) ", " 1 ");
            TEpat = TEpat.replaceAll(" (primero|uno) ", " Num ");

        }


        // MISSDETECTED ISO
        if (TE.matches("(?i) " + Seasons_re + " (de(l)? )?[0-9]{4}") || TE.matches("(?i) ([0-9]{1,2} )?" + TMonths_re + " [0-9]{4} ") || TE.matches(" [0-9]{4} ")) {
            return toISO8601(TE, locale);
        }
        // MISSDETECTED ISO special
        if (TE.matches("(?i).* ([0-9]{1,2} )?" + TMonths_re + " [0-9]{4} ")) {
            String[] datearr = TE.trim().split(" ");
            String theISO = datearr[datearr.length - 2] + " " + datearr[datearr.length - 1];
            if (datearr.length >= 3 && datearr[datearr.length - 3].matches("[0-9]{1,2}")) {
                theISO = datearr[datearr.length - 3] + " " + theISO;
            }
            return toISO8601(theISO, locale);
        }


        // TODO: HACK FOR MISSINTERPRETABLE PERIODS (IMPROVE) - REVIEW REVIEW
        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches(".* ([0-9]{4})[- /].*") && !TE.matches("(?i).* (ago|next|past) .*") && !TE.matches("(?i).* [0-9]{4} " + TUnit_re_EN + " .*")) {
            Matcher matcher = Pattern.compile(".* ([0-9]{4})[- /].*").matcher(TE);
            if (matcher.find()) {
                return toISO8601(matcher.group(1), locale);
            }
        }
        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches(".* ([0-9]{4})[- /].*") && !TE.matches("(?i).* (hace|dentro de) .*") && !TE.matches("(?i).* [0-9]{4} " + TUnit_re_ES + " .*")) {
            Matcher matcher = Pattern.compile(".* ([0-9]{4})[- /].*").matcher(TE);
            if (matcher.find()) {
                return toISO8601(matcher.group(1), locale);
            }
        }


        TE = TE.trim().replaceAll(" ", "_");
        TEpat = TEpat.trim().replaceAll(" ", "_");

        //System.err.println(TE+" - "+TEpat);







        if (locale.getLanguage().equalsIgnoreCase("en")) {
            TE = TE.replaceAll("earl(y|ier)_", "");
            TEpat = TEpat.replaceAll("earl(y|ier)_", "");
            TE = TE.replaceAll("current_fiscal", "current");
            TEpat = TEpat.replaceAll("current_fiscal", "current");
        }


        if (decades.containsKey(TE)) {
            return decades.get(TE).toString();
        }

        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches(".*próxima(s)?_temporada(s)?.*")) {
            return "FUTURE_REF";
        }

        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches(".*weekend")) {
            if (TE.matches(".*(last|previous).*") || tense.equals("past")) {
                cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
            }
            return new DateTime(cal.getTime()).toString(granul_weeks) + "-WE";
        }

        text_arr = TE.split("_");
        pat_arr = TEpat.split("_");


        //System.out.println("Clean: "+TE+" - "+TEpat);

        if (locale.getLanguage().equalsIgnoreCase("en")) {
            if (TE.matches("(this_)?morning")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TMO";
            }
            if (TE.matches("(this_)?afternoon")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TAF";
            }
            if (TE.matches("(this_)?evening")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TEV";
            }
            if (TE.equalsIgnoreCase("last_night")) {
                cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                return new DateTime(cal.getTime()).toString(granul_days) + "TNI";
            }
            if (TE.matches("(this_)?night")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TNI";
            }
        }

        if (locale.getLanguage().equalsIgnoreCase("es")) {
            if (TE.matches("(esta_)?tarde")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TEV";
            }
            if (TE.matches("(esta_)?noche")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TNI";
            }
            if (TE.matches("(este_)?mediodia")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TAF";
            }
            if (TE.matches("(esta_)?madrugada")) {
                return new DateTime(cal.getTime()).toString(granul_days) + "TMO";
            }
        }


        if (pat_arr.length == 1) {



            if (pat_arr[0].equals("TWeekday")) {
                cal.set(GregorianCalendar.DAY_OF_WEEK, Weekdays.get(text_arr[0]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!tense.startsWith("past")) {
                        cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            if (locale.getLanguage().equalsIgnoreCase("es")) {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                            }
                        } else {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                        }
                    }

                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }

            if (locale.getLanguage().equalsIgnoreCase("en") && pat_arr[0].matches("(yesterday|today|tonight|tomorrow)")) {
                if (pat_arr[0].equals("today")) {
                    return new DateTime(refdate).toString(granul_days);
                }
                if (pat_arr[0].equals("tonight")) {
                    return new DateTime(refdate).toString(granul_days) + "TNI";
                }
                if (pat_arr[0].equals("yesterday")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    return new DateTime(cal.getTime()).toString(granul_days);
                }
                if (pat_arr[0].equals("tomorrow")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                    return new DateTime(cal.getTime()).toString(granul_days);
                }
            }

            if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr[0].matches("(anteayer|ayer|anoche|hoy|mañana)")) {
                if (pat_arr[0].equals("hoy")) {
                    return new DateTime(refdate).toString(granul_days);
                }
                if (pat_arr[0].equals("anoche")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    return new DateTime(cal.getTime()).toString(granul_days) + "TNI";
                }
                if (pat_arr[0].equals("ayer")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    return new DateTime(cal.getTime()).toString(granul_days);
                }
                if (pat_arr[0].equals("anteayer")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -2);
                    return new DateTime(cal.getTime()).toString(granul_days);
                }
                if (pat_arr[0].equals("mañana")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                    return new DateTime(cal.getTime()).toString(granul_days);
                }
            }


            if (pat_arr[0].equals("TMonth")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[0]));
                Date result = cal.getTime();
                //System.out.println(refdate.toString()+" - "+cal.getTime().toString()+" - "+result.before(refdate)+" - "+tense);
                if (result.before(refdate)) {
                    //if (!tense.equals("past")) {
                    if (tense.equals("future")) {
//                        if (locale.getLanguage().equalsIgnoreCase("en") || tense.equals("future")) { // hack spanish historical present
                        cal.add(GregorianCalendar.YEAR, 1);
//                        }
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }

                }
                //System.out.println(new DateTime(cal.getTime()).toString(granul_months)+" - "+result.before(refdate)+" - "+tense);
                return new DateTime(cal.getTime()).toString(granul_months);
            }
        }






        if (pat_arr.length == 2) {




            if (TEpat.equals("TMonth_Num")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[0].replaceAll("\\.", "")));
                if (text_arr[1].matches("[0-9]+-.*")) {
                    text_arr[1] = text_arr[1].substring(0, text_arr[1].indexOf('-'));
                }
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[1]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    //if (!tense.equals("past")) {
                    if (tense.equals("future")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }

            if (TEpat.equals("Num_TMonth")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[1].replaceAll("\\.", "")));
                if (text_arr[0].matches("[0-9]+-[0-9]+")) {
                    text_arr[0] = text_arr[0].substring(0, text_arr[0].indexOf('-'));
                }
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[0]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!tense.equals("past")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }


            if (locale.getLanguage().equalsIgnoreCase("en") && TEpat.matches("(this|current|fiscal|last|next|previous|past|following)_TUnit")) {
                if (text_arr[1].matches("(minute|hour|day|week|month|year)(s)?")) {
                    int unit = TUnits.get(text_arr[1].replaceAll("s$", ""));
                    if (text_arr[0].matches("(last|past)")) {
                        cal.add(unit, -1);
                    }
                    if (text_arr[0].equals("previous")) { // TODO
                        cal.add(unit, -2);
                    }
                    if (text_arr[0].matches("(next|following)")) {
                        cal.add(unit, 1);
                    }

                    return new DateTime(cal.getTime()).toString(getGranularityFormat(text_arr[1]));
                }
            }


            // relativos tipo: siguiente|anterior...
            if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("((est(e|a)|pasad(o|a)|anterior|próxim(o|a)|siguiente)_TUnit|TUnit_(pasad(o|a)|próxim(o|a)|anterior|siguiente|queviene|venider(o|a)))")) {
                int TUnitpos = 1;
                int textpos = 0;
                if (TEpat.startsWith("TUnit")) {
                    TUnitpos = 0;
                    textpos = 1;
                }
                if (text_arr[TUnitpos].matches("(minuto|hora|día|semana|mes|año)(s)?")) {
                    if (text_arr[TUnitpos].equals("mes")) {
                        text_arr[TUnitpos] = "mess";
                    }
                    int unit = TUnits.get(text_arr[TUnitpos].replaceAll("s$", ""));
                    if (text_arr[TUnitpos].equals("mess")) {
                        text_arr[TUnitpos] = "mes";
                    }
                    if (text_arr[textpos].startsWith("pasad")) {
                        cal.add(unit, -1);
                    }
                    if (text_arr[textpos].equals("anterior")) { // TODO
                        cal.add(unit, -2);
                    }
                    if (text_arr[textpos].matches("(próxim(o|a)|queviene|venider(o|a))")) {
                        cal.add(unit, 1);
                    }
                    if (text_arr[textpos].equals("siguiente")) { // TODO
                        cal.add(unit, 2);
                    }
                    return new DateTime(cal.getTime()).toString(getGranularityFormatES(text_arr[TUnitpos]));
                }
            }



            if (locale.getLanguage().equalsIgnoreCase("en") && TEpat.matches("(this|last|past|previous|next|following)_TWeekday")) {
                cal.set(GregorianCalendar.DAY_OF_WEEK, Weekdays.get(text_arr[1]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!text_arr[0].matches("(last|past|previous)")) {
                        cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (text_arr[0].matches("(last|past|previous)")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                        }
                    } else { // after
                        if (text_arr[0].matches("(last|past|previous)")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                        }
                    }
                }
                //if (text_arr[0].matches("(last|past)")) {
                //cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                //}
                //if (text_arr[0].matches("(next|following)")) {
                //cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                //}
                return new DateTime(cal.getTime()).toString(granul_days);
            }


            if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("((presente|este|pasado|próximo)_TWeekday|TWeekday_(pasado|próximo|queviene|venidero))")) {
                int TUnitpos = 1;
                int textpos = 0;
                if (TEpat.startsWith("TWeekday")) {
                    TUnitpos = 0;
                    textpos = 1;
                }
                cal.set(GregorianCalendar.DAY_OF_WEEK, Weekdays.get(text_arr[TUnitpos]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!text_arr[textpos].equals("pasado")) {
                        cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (text_arr[textpos].equals("pasado")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                        }
                    } else { // after
                        if (text_arr[textpos].equals("pasado")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                        }
                    }
                }
                //if (text_arr[textpos].equals("pasado")) {
                //cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                //}
                //if (text_arr[textpos].matches("(próximo|queviene)")) {
                //cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                //}
                return new DateTime(cal.getTime()).toString(granul_days);
            }







            if (locale.getLanguage().equalsIgnoreCase("en") && TEpat.matches("(this|last|past|next)_TMonth")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[1].replaceAll("\\.", "")));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!text_arr[0].matches("(last|past)")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (text_arr[0].matches("(last|past)")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (text_arr[0].matches("(last|past)")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                //if (text_arr[0].matches("(last|past)")) {
                //cal.add(GregorianCalendar.YEAR, -1);
                //}
                //if (text_arr[0].equals("next")) {
                //cal.add(GregorianCalendar.YEAR, 1);
                //}
                return new DateTime(cal.getTime()).toString(granul_months);
            }



            if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("((presente|este|pasado|próximo)_TMonth|TMonth_(pasado|próximo|queviene|venidero))")) {
                int TUnitpos = 1;
                int textpos = 0;
                if (TEpat.startsWith("TMonth")) {
                    TUnitpos = 0;
                    textpos = 1;
                }
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[TUnitpos].replaceAll("\\.", "")));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!text_arr[textpos].equals("pasado")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (text_arr[textpos].equals("pasado")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (text_arr[textpos].equals("pasado")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                //if (text_arr[textpos].equals("pasado")) {
                //cal.add(GregorianCalendar.YEAR, -1);
                //}
                //if (text_arr[textpos].matches("(próximo|queviene)")) {
                //cal.add(GregorianCalendar.YEAR, 1);
                //}
                return new DateTime(cal.getTime()).toString(granul_months);
            }

            if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr[0].equals("pasado") && pat_arr[1].equals("mañana")) {
                cal.add(GregorianCalendar.DAY_OF_MONTH, 2);
                return new DateTime(cal.getTime()).toString(granul_days);
            }

            if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches("día_[0-9]+")) {
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[1]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!tense.equals("past")) {
                        cal.add(GregorianCalendar.MONTH, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.MONTH, -1);
                        } else {
                            cal.add(GregorianCalendar.MONTH, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.MONTH, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }

        }









        if (pat_arr.length == 3) { // de 3
            if (locale.getLanguage().equalsIgnoreCase("en") && TEpat.matches("(this|current|last|past|next|following)_TMonth_Num")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[1].replaceAll("\\.", "")));
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[2]));
                Date result = cal.getTime();

                if (result.before(refdate)) {
                    if (!text_arr[0].matches("(last|past)")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (text_arr[0].matches("(last|past)")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (text_arr[0].matches("(last|past)")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }



            // HACK PARA : DÍA (TUNIT) NUM DE .. COMO ESTE 1 DE
            if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("(presente|este|pasado|próximo|TUnit)_Num_TMonth")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[2].replaceAll("\\.", "")));
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[1]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!text_arr[0].equals("pasado")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (text_arr[0].equals("pasado")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (text_arr[0].equals("pasado")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }


            if (TEpat.matches("TWeekday_TMonth_Num")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[1].replaceAll("\\.", "")));
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[2]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!tense.equals("past")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }


            if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("TUnit_Num_TMonth") && text_arr[0].equals("día")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[2].replaceAll("\\.", "")));
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[1]));
                return new DateTime(cal.getTime()).toString(granul_days);
            }

            if (TEpat.matches("TWeekday_Num_TMonth")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[2].replaceAll("\\.", "")));
                cal.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(text_arr[1]));
                Date result = cal.getTime();
                if (result.before(refdate)) {
                    if (!tense.equals("past")) {
                        cal.add(GregorianCalendar.YEAR, 1);
                    }
                } else {
                    if (result.equals(refdate)) {
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        } else {
                            cal.add(GregorianCalendar.YEAR, 1);
                        }
                    } else { // after
                        if (tense.equals("past")) {
                            cal.add(GregorianCalendar.YEAR, -1);
                        }
                    }
                }
                return new DateTime(cal.getTime()).toString(granul_days);
            }



            if (locale.getLanguage().equalsIgnoreCase("en") && TEpat.matches("TMonth_(this|last|current|past|next)_TUnit")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[0].replaceAll("\\.", "")));
                if (text_arr[1].matches("(last|past)")) {
                    cal.add(GregorianCalendar.YEAR, -1);
                }
                if (text_arr[1].equals("next")) {
                    cal.add(GregorianCalendar.YEAR, 1);
                }
                return new DateTime(cal.getTime()).toString(granul_months);
            }

            if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("TMonth_(presente|este|pasado|próximo)_TUnit")) {
                cal.set(GregorianCalendar.MONTH, Yearmonths.get(text_arr[0].replaceAll("\\.", "")));
                if (text_arr[1].equals("pasado")) {
                    cal.add(GregorianCalendar.YEAR, -1);
                }
                if (text_arr[1].equals("próximo")) {
                    cal.add(GregorianCalendar.YEAR, 1);
                }
                return new DateTime(cal.getTime()).toString(granul_months);
            }

        }


        //quarters
        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("((the|its|this_year)_)?('s|this|current|first|second|third|fourth|latest|[0-9]+)_quarter")) {
            String quarternum = "4";
            if (!text_arr[text_arr.length - 2].matches("(latest|this|current)")) {
                quarternum = numek.text2number(text_arr[text_arr.length - 2]);
            } else {
                if (text_arr[text_arr.length - 2].matches("(this|current)")) {
                    Integer tempmonth = Integer.parseInt(new DateTime(cal.getTime()).toString("MM"));
                    if (tempmonth <= 3) {
                        quarternum = "1";
                    }
                    if (tempmonth > 3 || tempmonth <= 6) {
                        quarternum = "2";
                    }
                    if (tempmonth > 6 || tempmonth <= 9) {
                        quarternum = "3";
                    }
                    if (tempmonth > 9) {
                        quarternum = "4";
                    }
                }
            }


            return new DateTime(refdate).toString(granul_years) + "-Q" + quarternum;
        }
        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(first|second|third|fourth|[0-9]+)_quarter(_of)?_[0-9]+")) {
            String quarternum = "4";
            quarternum = numek.text2number(text_arr[0]);
            return text_arr[text_arr.length - 1] + "-Q" + quarternum;
        }

        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("[0-9]+_(first|second|third|fourth|[0-9]+)_quarter")) {
            String quarternum = "4";
            quarternum = numek.text2number(text_arr[1]);
            return text_arr[0] + "-Q" + quarternum;
        }




        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches("(el_)?(presente|este|primer|segundo|tercer|cuarto|último|[0-9]+)_(cua)?trimestre(s)?")) {
            String quarternum = "4";
            if (!text_arr[text_arr.length - 2].matches("(último|este|presente)")) {
                quarternum = numek.text2number(text_arr[text_arr.length - 2]);
            }
            if (text_arr[text_arr.length - 2].matches("(este|presente)")) {
                Integer tempmonth = Integer.parseInt(new DateTime(cal.getTime()).toString("MM"));
                if (tempmonth <= 3) {
                    quarternum = "1";
                }
                if (tempmonth > 3 || tempmonth <= 6) {
                    quarternum = "2";
                }
                if (tempmonth > 6 || tempmonth <= 9) {
                    quarternum = "3";
                }
                if (tempmonth > 9) {
                    quarternum = "4";
                }
            }
            String numeknum = quarternum.toString();
            if (numeknum.matches(".*\\.(0)+")) {
                numeknum = numeknum.substring(0, numeknum.lastIndexOf('.'));
            }

            return new DateTime(refdate).toString(granul_years) + "-Q" + numeknum;
        }
        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches("(primer|segundo|tercero|cuarto|[0-9]+)_(cua)?trimestre(_de)?_[0-9]+")) {
            String quarternum = "4";
            quarternum = numek.text2number(text_arr[0]);
            return text_arr[text_arr.length - 1] + "-Q" + quarternum;
        }




        //agoses

        if (locale.getLanguage().equalsIgnoreCase("en") && TEpat.matches("(a|Num)_TUnit_(ago|earlier|before|after)(_(TWeekday|today))?")) {
            int num = 1;
            if (pat_arr[0].equals("Num")) {
                num = Integer.parseInt(text_arr[0]);
            }
            if (pat_arr.length == 4) {
                cal.set(GregorianCalendar.DAY_OF_WEEK, Weekdays.get(text_arr[3]));
            }

            if (text_arr[1].matches("(minute|hour|day|week|month|year)(s)?")) {
                int unit = TUnits.get(text_arr[1].replaceAll("s$", ""));
                if (TEpat.endsWith("after")) {
                    cal.add(unit, num);
                } else {
                    cal.add(unit, num * -1);
                }
                return new DateTime(cal.getTime()).toString(getGranularityFormat(text_arr[1]));
            }
        }

        if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("(un(a)?|Num)_TUnit_(antes|después)")) {
            int num = 1;
            if (pat_arr[0].equals("Num")) {
                num = Integer.parseInt(text_arr[0]);
            }

            if (text_arr[1].matches("(minuto|hora|día|semana|mes(e)?|año)(s)?")) {
                if (text_arr[1].equals("mes")) {
                    text_arr[1] = "mess";
                }
                int unit = TUnits.get(text_arr[1].replaceAll("s$", ""));
                if (TEpat.endsWith("antes")) {
                    cal.add(unit, num * -1);
                } else {
                    cal.add(unit, num);
                }
                return new DateTime(cal.getTime()).toString(getGranularityFormatES(text_arr[1]));
            }
        }

        // hace Num TUnit
        if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("hace_.*(un(a)?|Num)_TUnit")) {
            int num = 1;
            if (pat_arr[pat_arr.length - 2].equals("Num")) {
                num = Integer.parseInt(text_arr[pat_arr.length - 2]);
            }

            if (text_arr[pat_arr.length - 1].matches("(minuto|hora|día|semana|mes(e)?|año)(s)?")) {
                if (text_arr[pat_arr.length - 1].matches("mes(es)?")) {
                    text_arr[pat_arr.length - 1] = "mess";    // hack for plural removal
                }
                int unit = TUnits.get(text_arr[pat_arr.length - 1].replaceAll("s$", ""));
                cal.add(unit, num * -1);
                return new DateTime(cal.getTime()).toString(getGranularityFormatES(text_arr[pat_arr.length - 1]));
            }
        }


        // dentro de ... Num TUnit
        if (locale.getLanguage().equalsIgnoreCase("es") && TEpat.matches("dentro_de_.*(un(a)?|Num)_TUnit")) {
            int num = 1;
            if (pat_arr[pat_arr.length - 2].equals("Num")) {
                num = Integer.parseInt(text_arr[pat_arr.length - 2]);
            }

            if (text_arr[pat_arr.length - 1].matches("(minuto|hora|día|semana|mes(e)?|año)(s)?")) {
                if (text_arr[pat_arr.length - 1].matches("mes(es)?")) {
                    text_arr[pat_arr.length - 1] = "mess";    // hack for plural removal
                }
                int unit = TUnits.get(text_arr[pat_arr.length - 1].replaceAll("s$", ""));
                cal.add(unit, num);
                return new DateTime(cal.getTime()).toString(getGranularityFormatES(text_arr[pat_arr.length - 1]));
            }
        }



        // TODO TIMES should be able to concatenate in each of the other expressions to reduce the code...
        // For the moment the closest weekday and 1 day window

        if (locale.getLanguage().equalsIgnoreCase("en") && text_arr.length > 1) {
            // Find day
            String tempdate = new DateTime(cal.getTime()).toString(granul_days);
            if (pat_arr[0].matches("(TWeekday|this|yesterday|today|tonight|tomorrow)")) {
                if (pat_arr[0].equals("TWeekday")) {
                    cal.set(GregorianCalendar.DAY_OF_WEEK, Weekdays.get(text_arr[0]));
                    Date result = cal.getTime();
                    if (result.before(refdate)) {
                        if (!tense.equals("past")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                        }
                    } else {
                        if (result.equals(refdate)) {
                            if (tense.equals("past")) {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                            } else {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                            }
                        } else { // after
                            if (tense.equals("past")) {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                            }
                        }

                    }
                    tempdate = new DateTime(cal.getTime()).toString(granul_days);
                }

                if (locale.getLanguage().equalsIgnoreCase("en") && pat_arr[0].matches("(yesterday|today|this|tomorrow)")) {
                    if (pat_arr[0].matches("(today|this)")) {
                        tempdate = new DateTime(refdate).toString(granul_days);
                    }
                    if (pat_arr[0].equals("yesterday")) {
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                        tempdate = new DateTime(cal.getTime()).toString(granul_days);
                    }
                    if (pat_arr[0].equals("tomorrow")) {
                        cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                        tempdate = new DateTime(cal.getTime()).toString(granul_days);
                    }
                }


            }


            // get TOD
            if (text_arr[1].equals("morning")) {
                return tempdate + "TMO";
            }
            if (text_arr[1].equals("afternoon")) {
                return tempdate + "TAF";
            }
            if (text_arr[1].equals("evening")) {
                return tempdate + "TEV";
            }
            if (text_arr[1].equals("night")) {
                return tempdate + "TNI";
            }

            // get TIME
            if (text_arr[1].matches("([0-2]?[0-9][\\.:][0-5][0-9]|[0-2]?[0-9])")) {
                text_arr[1] = text_arr[1].replaceAll("\\.", ":");
                if (text_arr[1].length() == 3) {
                    text_arr[1] = "0" + text_arr[1];
                }
                if (text_arr[1].length() == 2) {
                    text_arr[1] = text_arr[1] + ":00";
                }
                if (text_arr[1].length() == 1) {
                    text_arr[1] = "0" + text_arr[1] + ":00";
                }
                // TODO treat a.m. p.m. to get 24h ISO times
                return tempdate + "T" + text_arr[1];
            }
        }



        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(?i)late_(yesterday|tomorrow)")) {
            if (text_arr[1].equalsIgnoreCase("yesterday")) {
                cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                return new DateTime(cal.getTime()).toString(granul_days);
            }
            if (text_arr[1].equalsIgnoreCase("tomorrow")) {
                cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                return new DateTime(cal.getTime()).toString(granul_days);
            }
        }



        if (locale.getLanguage().equalsIgnoreCase("es") && text_arr.length > 1) {
            // Find day
            String tempdate = new DateTime(cal.getTime()).toString(granul_days);
            if (pat_arr[0].matches("(TWeekday|est(a|e)|ayer|hoy|mañana)")) {
                if (pat_arr[0].equals("TWeekday")) {
                    cal.set(GregorianCalendar.DAY_OF_WEEK, Weekdays.get(text_arr[0]));
                    Date result = cal.getTime();
                    if (result.before(refdate)) {
                        if (!tense.equals("past")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                        }
                    } else {
                        if (result.equals(refdate)) {
                            if (tense.equals("past")) {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                            } else {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
                            }
                        } else { // after
                            if (tense.equals("past")) {
                                cal.add(GregorianCalendar.WEEK_OF_YEAR, -1);
                            }
                        }

                    }
                    tempdate = new DateTime(cal.getTime()).toString(granul_days);
                }
                if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr[0].matches("(anteayer|ayer|hoy|mañana)")) {
                    if (pat_arr[0].equals("hoy")) {
                        tempdate = new DateTime(refdate).toString(granul_days);
                    }
                    if (pat_arr[0].equals("ayer")) {
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                        tempdate = new DateTime(refdate).toString(granul_days);
                    }
                    if (pat_arr[0].equals("anteayer")) {
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -2);
                        tempdate = new DateTime(refdate).toString(granul_days);
                    }
                    if (pat_arr[0].equals("mañana")) {
                        cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
                        tempdate = new DateTime(refdate).toString(granul_days);
                    }
                }



                if (text_arr[1].equals("tarde")) {
                    return tempdate + "TEV";
                }
                if (text_arr[1].equals("noche")) {
                    return tempdate + "TNI";
                }
                if (text_arr[1].equals("mediodia")) {
                    return tempdate + "TAF";
                }
                if (text_arr[1].equals("madrugada")) {
                    return tempdate + "TMO";
                }
                if (text_arr[1].equals("mañana")) {
                    return tempdate + "TMO";
                }

                // get TIME
                // get TIME
                if (text_arr[1].matches("([0-2]?[0-9][\\.:][0-5][0-9]|[0-2]?[0-9])")) {
                    text_arr[1] = text_arr[1].replaceAll("\\.", ":");
                    if (text_arr[1].length() == 3) {
                        text_arr[1] = "0" + text_arr[1];
                    }
                    if (text_arr[1].length() == 2) {
                        text_arr[1] = text_arr[1] + ":00";
                    }
                    if (text_arr[1].length() == 1) {
                        text_arr[1] = "0" + text_arr[1] + ":00";
                    }
                    // TODO treat a.m. p.m. to get 24h ISO times
                    return tempdate + "T" + text_arr[1];
                }
            }
        }


        // Times....
        // TODO
        //today + time (check am/p[.]?m[.]?/gmt/est)
        //Weekday + time
        // horas, hora local, h, gmt
        // si sale horas omitir (de la mañana/tarde/noche/mediodia/medianoche/madrugada)...
        // igual para el inglés 8 p.m. in the evening... 9:00 in the morning.


        // GET TIMES ALONE...(. or : required) (CHECK SOMETHING ELSE BUT NUMBER...gmt,ampm,:,gmt,est...)
        if (text_arr.length == 1 && text_arr[0].matches("[0-2]?[0-9][\\.:][0-5][0-9]")) {
            text_arr[0] = text_arr[0].replaceAll("\\.", ":");
            if (text_arr[0].length() == 3) {
                text_arr[0] = "0" + text_arr[0];
            }
            return new DateTime(cal.getTime()).toString(granul_days) + "T" + text_arr[0];
        }


        // TIMES with some tmp elements EN and ES
        if (text_arr.length > 1 && text_arr[0].matches("([0-2]?[0-9][\\.:][0-5][0-9]|[0-2]?[0-9])")) {
            text_arr[0] = text_arr[0].replaceAll("\\.", ":");
            if (text_arr[0].length() == 3) {
                text_arr[0] = "0" + text_arr[0];
            }
            if (text_arr[0].length() == 2) {
                text_arr[0] = text_arr[0] + ":00";
            }
            if (text_arr[0].length() == 1) {
                text_arr[0] = "0" + text_arr[0] + ":00";
            }

            if (text_arr[1].matches("((p|a)(\\.)?m(\\.)?|gmt|est|horas|hora|" + TOD_re + ")")) {
                if (text_arr[1].matches("(p(\\.)?m(\\.)?|tarde|mediodía|afternoon|evening|noche|night)") && Integer.parseInt((text_arr[0].substring(0, 2))) < 12) {
                    text_arr[0] = (Integer.parseInt(text_arr[0].substring(0, 2)) + 12) + text_arr[0].substring(2, 5);
                }
                String z = "";
                if (TE.contains("gmt")) {
                    z = "Z";
                }
                return new DateTime(cal.getTime()).toString(granul_days) + "T" + text_arr[0] + z;

            }
        }


        // SEASONS






        if (locale.getLanguage().equalsIgnoreCase("en") && TE.matches("(next|this|current|past|last)_" + Seasons_re)) {
            String refyear = new DateTime(refdate).toString(granul_years);
            if (text_arr[1].equals("winter")) {
                refyear = refyear + "-01";
                text_arr[1] = "WI";
            }
            if (text_arr[1].equals("spring")) {
                refyear = refyear + "-03";
                text_arr[1] = "SP";
            }
            if (text_arr[1].equals("summer")) {
                refyear = refyear + "-06";
                text_arr[1] = "SU";
            }
            if (text_arr[1].matches("(autumn|fall)")) {
                refyear = refyear + "-09";
                text_arr[1] = "FA";
            }
            // TODO problem are buying that this fall (DCT 10-10-2011) it is already fall ADDED PATCH
            cal.setTime(new DateTime(refyear).toDate());
            Date result = cal.getTime();
            if (result.before(refdate)) {
                if (!text_arr[0].matches("(past|last|this)")) {
                    cal.add(GregorianCalendar.YEAR, 1);
                }
            } else {
                if (result.after(refdate)) {
                    if (text_arr[0].matches("(past|last)")) {
                        cal.add(GregorianCalendar.YEAR, -1);
                    }
                }
            }

            return new DateTime(cal.getTime()).toString(granul_years) + "-" + text_arr[1];
        }



        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches("((est(e|a)|pasado|próximo)_" + Seasons_re + "|" + Seasons_re + "_(pasado|próximo|queviene|venider(o|a)))")) {
            int TUnitpos = 1;
            int textpos = 0;
            if (text_arr[0].matches(Seasons_re)) {
                TUnitpos = 0;
                textpos = 1;
            }
            String refyear = new DateTime(refdate).toString(granul_years);

            if (text_arr[TUnitpos].equals("invierno")) {
                refyear = refyear + "-01";
                text_arr[TUnitpos] = "WI";
            }
            if (text_arr[TUnitpos].equals("primavera")) {
                refyear = refyear + "-03";
                text_arr[TUnitpos] = "SP";
            }
            if (text_arr[TUnitpos].equals("verano")) {
                refyear = refyear + "-06";
                text_arr[TUnitpos] = "SU";
            }
            if (text_arr[TUnitpos].equals("otoño")) {
                refyear = refyear + "-09";
                text_arr[TUnitpos] = "FA";
            }
            cal.setTime(new DateTime(refyear).toDate());
            Date result = cal.getTime();
            if (result.before(refdate)) {
                if (!text_arr[textpos].equals("pasado")) {
                    cal.add(GregorianCalendar.YEAR, 1);
                }
            } else {
                if (result.after(refdate)) {
                    if (text_arr[textpos].equals("pasado")) {
                        cal.add(GregorianCalendar.YEAR, -1);
                    }
                }
            }

            return new DateTime(cal.getTime()).toString(granul_years) + "-" + text_arr[TUnitpos];
        }


        if (TE.matches("(?i)" + Seasons_re)) {
            String refyear = new DateTime(refdate).toString(granul_years);

            if (text_arr[0].equals("winter")) {
                refyear = refyear + "-01";
                text_arr[0] = "WI";
            }
            if (text_arr[0].equals("spring")) {
                refyear = refyear + "-03";
                text_arr[0] = "SP";
            }
            if (text_arr[0].equals("summer")) {
                refyear = refyear + "-06";
                text_arr[0] = "SU";
            }
            if (text_arr[0].matches("(autumn|fall)")) {
                refyear = refyear + "-09";
                text_arr[0] = "FA";
            }
            cal.setTime(new DateTime(refyear).toDate());
            return new DateTime(cal.getTime()).toString(granul_years) + "-" + text_arr[0];

        }




        // ¿¿¿Summer 1969??? many rules...

        // HACK FOR BADCLASSIFIED PERIODS
        if (TEpat.matches("((más|aproximádamente|unos)_)?(Num|dos|siete|uno)_TUnit")) {
            String punit = text_arr[text_arr.length - 1].substring(0, 3).toUpperCase();
            if (punit.matches("(DEC|CEN|MIL)")) {
                punit = text_arr[text_arr.length - 1].substring(0, 2).toUpperCase();
                if (punit.equals("MI")) {
                    punit = "ML";
                }
            } else {
                punit = text_arr[text_arr.length - 1].substring(0, 1).toUpperCase();
            }
            if (punit.matches("(A|T)")) { // T para temporada
                punit = "Y";
            }
            if (punit.equals("S")) {
                punit = "W";
            }


            if (text_arr[text_arr.length - 2].equals("uno")) {
                text_arr[text_arr.length - 2] = "1";
            }
            if (text_arr[text_arr.length - 2].equals("dos")) {
                text_arr[text_arr.length - 2] = "2";
            }
            if (text_arr[text_arr.length - 2].equals("siete")) {
                text_arr[text_arr.length - 2] = "7";
            }

            return "P" + text_arr[text_arr.length - 2] + punit;
        }

        if (locale.getLanguage().equalsIgnoreCase("es") && TE.matches(".*temporada(s)?.*")) {
            String num = "1";
            for (int i = 0; i < pat_arr.length; i++) {
                if (pat_arr[i].matches("(Num|[0-9]+)")) {
                    num = text_arr[i];
                    break;
                }
                if (pat_arr[i].matches("temporada(s)?")) {
                    break;
                }
            }
            return "P" + num + "Y";
        }
        // HAKS FOR BASCLASSIFIED SETS

        if (text_arr[0].endsWith("ly") && pat_arr.length == 1) {
            if (text_arr[0].equalsIgnoreCase("annualy")) {
                return "XXXX";
            }
            if (text_arr[0].equalsIgnoreCase("monthly")) {
                return "XXXX-XX";
            }
            if (text_arr[0].equalsIgnoreCase("weekly")) {
                return "XXXX-WXX";
            }
            if (text_arr[0].equalsIgnoreCase("da(y|i)ly")) {
                return "XXXX-XX-XX";
            }
        }


        if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr.length == 1) {
            if (text_arr[0].matches("(?i)anual(mente)?")) {
                return "XXXX";
            }
            if (text_arr[0].matches("(?i)mensual(mente)?")) {
                return "XXXX-XX";
            }
            if (text_arr[0].matches("(?i)semanal(mente)?")) {
                return "XXXX-WXX";
            }
            if (text_arr[0].matches("(?i)diari(o|a)(s)?(mente)?")) {
                return "XXXX-XX-XX";
            }
        }


        if (locale.getLanguage().equalsIgnoreCase("en") && pat_arr.length == 2 && pat_arr[0].matches("(?i)(every|each)")) {
            if (pat_arr[1].matches("TWeekday")) {
                return "XXXX-WXX";
            }
            if (pat_arr[1].equals("TUnit")) {
                if (text_arr[1].equals("day")) {
                    return "XXXX-XX-XX";
                }
                if (text_arr[1].equals("week")) {
                    return "XXXX-WXX";
                }
                if (text_arr[1].equals("month")) {
                    return "XXXX-XX";
                }
                if (text_arr[1].equals("year")) {
                    return "XXXX";
                }
            }
        }

        if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr.length == 2 && pat_arr[0].matches("(?i)(tod(a|o)s|cada)")) {
            if (pat_arr[1].matches("TWeekday")) {
                return "XXXX-WXX";
            }
            if (pat_arr[1].equals("TUnit")) {
                if (text_arr[1].equals("día")) {
                    return "XXXX-XX-XX";
                }
                if (text_arr[1].equals("semana")) {
                    return "XXXX-WXX";
                }
                if (text_arr[1].equals("mes")) {
                    return "XXXX-XX";
                }
                if (text_arr[1].equals("año")) {
                    return "XXXX";
                }
            }
        }



        return TEref;


    }

    /**
     * Returns ISO8601 value for a set timex given the normText and the pattern.
     * @param TE
     * @param TEpat
     * @return
     */
    public String obtainISOSet(String TE, String TEpat) {

        // TODO: falta horas y minuts y patrones complejos (cada 3 semanas)

        String[] text_arr = TE.split("_");
        String[] pat_arr = TEpat.split("_");
        if (locale.getLanguage().equalsIgnoreCase("en") && pat_arr.length == 1) {
            if (pat_arr[0].matches("TWeekday")) {
                return "XXXX-WXX";
            }
            if (text_arr[0].endsWith("ly")) {
                if (text_arr[0].equalsIgnoreCase("annualy")) {
                    return "XXXX";
                }
                if (text_arr[0].equalsIgnoreCase("monthly")) {
                    return "XXXX-XX";
                }
                if (text_arr[0].equalsIgnoreCase("weekly")) {
                    return "XXXX-WXX";
                }
                if (text_arr[0].equalsIgnoreCase("da(y|i)ly")) {
                    return "XXXX-XX-XX";
                }
            }
        }
        if (locale.getLanguage().equalsIgnoreCase("en") && pat_arr.length == 2 && pat_arr[0].matches("(?i)(every|each)")) {
            if (pat_arr[1].matches("TWeekday")) {
                return "XXXX-WXX";
            }
            if (pat_arr[1].equals("TUnit")) {
                if (text_arr[1].equals("day")) {
                    return "XXXX-XX-XX";
                }
                if (text_arr[1].equals("week")) {
                    return "XXXX-WXX";
                }
                if (text_arr[1].equals("month")) {
                    return "XXXX-XX";
                }
                if (text_arr[1].equals("year")) {
                    return "XXXX";
                }
            }
        }

        if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr.length == 1) {
            if (pat_arr[0].matches("TWeekday")) {
                return "XXXX-WXX";
            }
            if (text_arr[0].matches("(?i)anual(mente)?")) {
                return "XXXX";
            }
            if (text_arr[0].matches("(?i)mensual(mente)?")) {
                return "XXXX-XX";
            }
            if (text_arr[0].matches("(?i)semanal(mente)?")) {
                return "XXXX-WXX";
            }
            if (text_arr[0].matches("(?i)diari(o|a)(s)?(mente)?")) {
                return "XXXX-XX-XX";
            }
        }

        // OJO: falta los/las
        if (locale.getLanguage().equalsIgnoreCase("es") && pat_arr.length == 2 && pat_arr[0].matches("(?i)(tod(a|o)s|cada)")) {
            if (pat_arr[1].matches("TWeekday")) {
                return "XXXX-WXX";
            }
            if (pat_arr[1].equals("TUnit")) {
                if (text_arr[1].equals("día")) {
                    return "XXXX-XX-XX";
                }
                if (text_arr[1].equals("semana")) {
                    return "XXXX-WXX";
                }
                if (text_arr[1].equals("mes")) {
                    return "XXXX-XX";
                }
                if (text_arr[1].equals("año")) {
                    return "XXXX";
                }
            }
        }

        return "XXXX-XX-XX";
    }

    /**
     * This fucntion will be improved
     * @param TUnit
     * @return
     */
    public String getGranularityFormat(String TUnit) {
        if (TUnit.matches("(?i)year(s)?")) {
            return granul_years;
        }
        if (TUnit.matches("(?i)month(s)?")) {
            return granul_months;
        }
        if (TUnit.matches("(?i)(hour|minute)(s)?")) {
            return granul_time;
        }
        if (TUnit.matches("(?i)(week)(s)?")) {
            return granul_weeks;
        }

        return granul_days;

    }

    /**
     * This fucntion will be improved
     * @param TUnit
     * @return
     */
    public String getGranularityFormatES(String TUnit) {
        if (TUnit.matches("(?i)año(s)?")) {
            return granul_years;
        }
        if (TUnit.matches("(?i)mes(es)?")) {
            return granul_months;
        }
        if (TUnit.matches("(?i)(hora|minuto)(s)?")) {
            return granul_time;
        }
        if (TUnit.matches("(?i)(semana)(s)?")) {
            return granul_weeks;
        }

        return granul_days;

    }

    /**
     * Returns an ISO8601 without abbreviations such as WI, Q1, SU, etc. then it can be represented as an exact GregorianCaledar reference
     * @param date
     * @return
     */
    public static String ISOclean(String date) {
        if (date.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|Q(1|2|3|4)|H(1|2))")) {
            if (date.matches("(?i).*-(WI|Q1|H1)")) {
                date = date.substring(0, 4) + "-01";
            }
            if (date.matches("(?i).*-(SP|Q2)")) {
                date = date.substring(0, 4) + "-03";
            }
            if (date.matches("(?i).*-(SU|Q3|H2)")) {
                date = date.substring(0, 4) + "-06";
            }
            if (date.matches("(?i).*-(AU|FA|Q4)")) {
                date = date.substring(0, 4) + "-09";
            }
        }

        if (date.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T(MO|AF|EV|NI)")) {
            // MORNING 5-12
            if (date.matches("(?i).*TMO")) {
                date = date.substring(0, 10) + "T05:00";
            }
            // NOON 12-13
            if (date.matches("(?i).*TAF")) {
                date = date.substring(0, 10) + "T13:00";
            }
            // DEPEND ON WORK BREAKS
            if (date.matches("(?i).*TEV")) {
                date = date.substring(0, 10) + "T18:00";
            }
            // AFTER WORK... GOING BACK HOME...
            if (date.matches("(?i).*TNI")) {
                date = date.substring(0, 10) + "T21:00";
            }
        }

        // TODO: IN THE FUTURE TREAT BETTER THIS DATES
        // GET THE DATE WITHOUT WE AND THEN SET DAY TO SATURDAY...
        // GET BACK THE DATE IN STRING FORMAT WITH DAY GRANULARITY
        if (date.matches("(?i).*-WE")) {
            date = date.substring(0, date.length() - 3);
        }

        // JODA TIME RARE ERROR
        if (date.equals("1901")) {
            date = "1901-01-02";
        }

        return date;
    }


    /*
    public String getNormTextandPattern(String tempex) {
    String normTE = null;
    String normPAT = null;
    try {

    // Interval freeling wrong separation
    //System.out.println(tempex+" "+tempexPat);
    if (tempex.matches("([0-9]{2})([0-9]{2})?/_[0-9]+")) {
    tempex = tempex.replaceAll("_", "");
    tempexPat = "DateInterval";
    }

    //check for separate date/time separators...
    if (tempex.matches(".*([1-9][0-9]*)[_]?([-/:])_([1-9][0-9]*).*")) {
    tempex = tempex.replaceAll("([1-9][0-9]*)[_]?([-/:])_([1-9][0-9]*)", "$1$2$3");
    tempexPat = tempexPat.replaceAll("Num(_([-/:]|Num))?_Num", "Num");
    }

    //System.out.println(tempex+" "+tempexPat);

    // Special for fractions
    if (tempex.matches("(.*_)?[1-9][0-9]*_[0-9]+/[1-9][0-9]*(_.*)?")) {
    String nums2norm = tempex.replaceFirst("(.*_)?([0-9]+_[0-9]+/[0-9]+)(_.*)?", "$2");
    tempex = tempex.replaceFirst("(.*_)?([0-9]+_[0-9]+/[0-9]+)(_.*)?", "$1" + NUMEK.calc_and_sum_frac(nums2norm.replaceAll("_", " ")) + "$3");
    }

    normTE = "";
    normPAT = "";
    String[] tempex_arr = tempex.split("_");
    String[] tempexPat_arr = tempexPat.split("_");
    String spelledNum = "";
    if (tempex_arr.length != tempexPat_arr.length) {
    throw new Exception("Different sizes tempex (" + tempex + ") and tempexPat (" + tempexPat + ")");
    }

    // splicit periods
    if (tempexPat_arr.length == 1 && tempex_arr[0].matches("[0-9]{2,4}[-/][0-9]{2,4}")) {
    return (tempex + "|" + tempexPat);
    }

    for (int i = 0; i < tempexPat_arr.length; i++) {
    if (!spelledNum.equals("") && (!tempexPat_arr[i].equals("Num") || tempex_arr[i].matches("[0-9]+"))) {
    if (!normTE.equals("")) {
    normTE += "_";
    normPAT += "_";
    }
    spelledNum = spelledNum.trim();
    if (!spelledNum.matches("(mid)?[0-9-]+(s)?")) {
    String numeknum = numek.text2number(spelledNum).toString();
    if (numeknum.matches(".*\\.(0)+")) {
    numeknum = numeknum.substring(0, numeknum.lastIndexOf('.'));
    }
    normTE += numeknum;
    normPAT += "Num";
    } else {
    normTE += spelledNum;
    normPAT += "Num";
    }
    spelledNum = "";
    }

    if (tempexPat_arr[i].matches("(TUnit|TMonth|TWeekday)")) {
    if (!normTE.equals("")) {
    normTE += "_";
    normPAT += "_";
    }
    normTE += tempex_arr[i].toLowerCase().replaceAll("^sept(\\.)?$", "sep"); //.replaceFirst("^.*s$", tempex_arr[i].toLowerCase().substring(0, tempex_arr[i].length() - 1));
    normPAT += tempexPat_arr[i];
    } else {
    if (tempexPat_arr[i].equals("Num")) {
    spelledNum += tempex_arr[i] + " ";
    } else {
    if (!normTE.equals("")) {
    normTE += "_";
    normPAT += "_";
    }
    normTE += tempex_arr[i].toLowerCase();
    normPAT += tempex_arr[i].toLowerCase();
    }
    }
    }

    spelledNum = spelledNum.trim();
    if (!spelledNum.equals("")) {
    if (!normTE.equals("")) {
    normTE += "_";
    normPAT += "_";
    }
    if (!spelledNum.matches("((mid)?[0-9-]+(s)?|.*\\..*\\..*)")) {

    String numeknum = numek.text2number(spelledNum).toString();
    if (numeknum.matches(".*\\.(0)+")) {
    numeknum = numeknum.substring(0, numeknum.lastIndexOf('.'));
    }
    normTE += numeknum;
    normPAT += "Num";
    } else {
    normTE += spelledNum;
    normPAT += "Num";
    }
    }

    } catch (Exception e) {
    System.err.println("Errors found (TIMEN):\n\t" + e.toString() + "\n");
    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
    e.printStackTrace(System.err);
    System.exit(1);
    }
    return null;
    }

    return (normTE + "|" + normPAT);

    }
     */
    /**
     * Returns an ISO8601 period from a timex text and pattern
     * @param val   normText
     * @param pat   pattern
     * @return
     */
    public String getISOperiod(String[] val, String[] pat) {
        String period = "";
        String unit = null;
        Boolean timegran = false;
        if (pat.length == 1) {
            if (val[0].matches("[0-9]{2,4}[-/][0-9]{2,4}")) {
                String separator = "-";
                if (val[0].contains("/")) {
                    separator = "/";
                }
                Integer value = Math.abs(Integer.parseInt(val[0].substring(val[0].length() - 2)) - Integer.parseInt(val[0].substring(val[0].indexOf(separator) - 2, val[0].indexOf(separator))));
                period = value.toString() + "Y";
            }
            if (locale.getLanguage().equalsIgnoreCase("en") && val[0].matches("(da(y|i)|week|month|year|annual)ly")) {
                period = "1" + val[0].substring(0, 1).toUpperCase();
                if (period.endsWith("A")) {
                    period = "1Y";
                }
            }
            if (locale.getLanguage().equalsIgnoreCase("en") && val[0].matches("(quarter)ly")) {
                period = "3M";
            }
            if (locale.getLanguage().equalsIgnoreCase("es") && val[0].matches("(diari(a|o)(s)?|semanal|mensual|anual)(es)?(mente)?")) {
                period = "1" + val[0].substring(0, 1).toUpperCase();
                if (period.endsWith("A")) {
                    period = "1Y";
                }
                if (period.endsWith("S")) {
                    period = "1W";
                }
            }
            if (locale.getLanguage().equalsIgnoreCase("es") && val[0].matches("(quincenal|(bi(mensual|anual)))(es)?(mente)?")) {
                period = "2" + val[0].substring(2, 3).toUpperCase();
                if (period.endsWith("A")) {
                    period = "2Y";
                }
                if (period.endsWith("I")) {
                    period = "2W";
                }
            }
        }
        if (period.equals("")) {
            for (int i = pat.length - 1; i >= 0; i--) {
                if (pat[i].matches("TUnit")) {
                    if (unit != null) {
                        period = "1" + period;
                    }
                    if (locale.getLanguage().equalsIgnoreCase("en")) {
                        if (timegran && !val[i].toLowerCase().matches("(hour|minute|second)(s)?")) {
                            period = "T" + period;
                            timegran = false;
                        }
                        String punit = val[i].substring(0, 3).toUpperCase();
                        if (punit.matches("(DEC|CEN|MIL)")) {
                            punit = val[i].substring(0, 2).toUpperCase();
                            if (punit.equals("MI")) {
                                punit = "ML";
                            }
                            period = punit;
                        } else {
                            period = val[i].toUpperCase().substring(0, 1) + period;
                        }
                        if (val[i].toLowerCase().matches("(hour|minute|second)(s)?")) {
                            timegran = true;
                        }
                        unit = val[i].toLowerCase();
                    }
                    if (locale.getLanguage().equalsIgnoreCase("es")) {
                        if (timegran && !val[i].toLowerCase().matches("(hora|minuto|segundo)(s)?")) {
                            period = "T" + period;
                            timegran = false;
                        }
                        if (val[i].toUpperCase().matches("(?:AÑO|TEMPORADA)(?:S)?")) {
                            period = "Y" + period;
                        } else {
                            if (val[i].toUpperCase().matches("SEMANA(?:S)?")) {
                                period = "W" + period;

                            } else {
                                if (val[i].toUpperCase().matches("SIGLO(?:S)?")) {
                                    period = "CE" + period;

                                } else {
                                    period = val[i].toUpperCase().substring(0, 1) + period;
                                }
                            }
                            if (val[i].toLowerCase().matches("(hora|minuto|segundo)(s)?")) {
                                timegran = true;
                            }
                        }
                        unit = val[i].toLowerCase();
                    }
                }

                if (unit != null && pat[i].matches("Num")) {
                    period = val[i] + period;
                    unit = null;
                }
            }
            if (unit != null) {
                if (unit.matches(".*s")) {
                    period = "X" + period;
                } else {
                    period = "1" + period;
                }
            }
            if (timegran) {
                period = "T" + period;
            }
        }
        if (period.equals("")) {
            period = "XX";
        }

        return period;
    }

    /**
     * Obtain the normalized text (NormText) and Patter from a given timex textual expression
     * @param timex    the timex textual expression
     * @return         the feature-values for NormText and Pattern
     */
    public String getNormTextandPattern(String timex) {
        String normText = "";
        String pattern = "";
        try {
            // lower case
            timex = timex.toLowerCase(locale);

            // Numeric ordinals to numbers
            timex = timex.replaceAll("([0-9]+)(?:_)?(?:st|nd|rd|th)", "$1");

            // Check for separate date/time separators -> UNIFY
            timex = timex.replaceAll("([0-9]+)_([-/:])_([0-9]+|" + TMonths_re + ")_([-/:])_([0-9]+)", "$1$2$3$4$5");
            timex = timex.replaceAll("([0-9]+[-/:])_((?:[0-9]+|" + TMonths_re + ")[-/:])_([0-9]+)", "$1$2$3");
            timex = timex.replaceAll("([0-9]+)_([-/:](?:[0-9]+|" + TMonths_re + "))_([-/:][0-9]+)", "$1$2$3");
            timex = timex.replaceAll("([0-9]+|" + TMonths_re + ")_([-/:])_([0-9]+)", "$1$2$3");
            timex = timex.replaceAll("((?:[0-9]+|" + TMonths_re + ")[-/:])_([0-9]+)", "$1$2");
            timex = timex.replaceAll("([0-9]+|" + TMonths_re + ")_([-/:][0-9]+)", "$1$2");


            // Special for mids
            timex = timex.replaceAll("mid(?:-)?([0-9]+)", "mid_$1");
            timex = timex.replaceAll("mid-(.+)", "mid_$1");

            //Special for 80s, etc.
            timex = timex.replaceAll("([0-9]+)s", "$1_s");

            // Special adjective periods (e.g., 10-hour)
            timex = timex.replaceAll("([^_]+)-(" + this.TUnit_re + ")", "$1_$2");

            // Special for fractions (only one is normalized because there should be no more than one per timex)
            if (timex.matches("(?:.*_)?(?:[0-9]*_)?[1-9][0-9]*/[1-9][0-9]*_" + this.TUnit_re + ".*")) {
                String nums2norm = timex.replaceFirst("(.*_)?((?:[0-9]*_)?[1-9][0-9]*/[1-9][0-9]*)(_" + this.TUnit_re + ".*)", "$2");
                String normalizedfrac = "" + NUMEK.calc_and_sum_frac(nums2norm.replaceAll("_", " "));
                timex = timex.replaceFirst("(.*_)?((?:[0-9]*_)?[1-9][0-9]*/[1-9][0-9]*)(_" + this.TUnit_re + ".*)", "$1" + normalizedfrac + "$3");
            }




            String[] tempex_arr = timex.split("_");

            // check spelled nums and repair other elements (mid, sept., etc.)
            // spelled nums (e.g., one million or 25 hundred)
            // ([0-9]+(\\.[0-9]+_spelledMagnitude_))?(spelled_)+, if after [0-9] there is no spell leave as it is.
            String spelledNum = "";
            String currentPat = "";
            for (int i = 0; i < tempex_arr.length; i++) {
                if (tempex_arr[i].matches(this.TUnit_re)) {
                    currentPat = "TUnit";
                } else {
                    if (tempex_arr[i].matches(this.TMonths_re)) {
                        currentPat = "TMonth";
                    } else {
                        if (tempex_arr[i].matches(this.TWeekdays_re)) {
                            currentPat = "TWeekday";
                        } else {
                            if (tempex_arr[i].matches("(?:[0-2])?[0-9][.:][0-5][0-9](?:(?:p|a)(?:\\.)?m(?:\\.)?|h)?")) {
                                currentPat = "Time";
                            } else {
                                if (tempex_arr[i].matches("(?:[0-3])?[0-9][./-](?:(?:[0-3])?[0-9]|" + TMonths_re + ")[./-][0-9]+")
                                        || tempex_arr[i].matches(TMonths_re + "[/-][0-9]+")
                                        || tempex_arr[i].matches("(?:1[0-2]|(?:0)?[1-9])[/-](?:18|19|20|21)[0-9]{2}")) {
                                    currentPat = "Date";
                                } else {
                                    if (tempex_arr[i].matches("[0-9]+(?:\\.[0-9]+)?") || tempex_arr[i].matches("(" + numek.numbers_re + "|" + numek.tens_re + "-" + numek.units_re + ")") || (!spelledNum.equals("") && !spelledNum.matches(".*([0-9]|" + numek.ordinals_re + ").*") && tempex_arr[i].matches(numek.numdelim))) {
                                        currentPat = "Num";
                                    } else {
                                        currentPat = tempex_arr[i].toLowerCase();
                                    }
                                }
                            }
                        }
                    }
                }

                // check if a spellednum ends
                if (!spelledNum.equals("") && (!currentPat.equals("Num") || tempex_arr[i].matches("[0-9]+(?:\\.[0-9]+)?") || spelledNum.trim().matches(numek.ordinals_re) || tempex_arr[i].trim().matches(numek.ordinals_re))) {
                    //if (!spelledNum.trim().matches(numek.ordinals_re)) {
                    normText += " " + numek.text2number(spelledNum.trim());
                    //} else {
                    //    normText += " " + spelledNum.trim();
                    //}
                    pattern += " Num";
                    spelledNum = ""; // initialize
                }

                // add to normTE or to spelled num
                if (currentPat.equalsIgnoreCase("Num")) {
                    spelledNum += " " + tempex_arr[i];
                } else {
                    normText += " " + tempex_arr[i].toLowerCase().replaceAll("^sept(\\.)?$", "sep"); //.replaceFirst("^.*s$", tempex_arr[i].toLowerCase().substring(0, tempex_arr[i].length() - 1));
                    pattern += " " + currentPat;
                }
            }

            // add last spellednum if exists
            if (!spelledNum.equals("")) {
                //if (!spelledNum.trim().matches(numek.ordinals_re)) {
                normText += " " + numek.text2number(spelledNum.trim());
                //} else {
                //    normText += " " + spelledNum.trim();
                //}
                pattern += " Num";
            }






        } catch (Exception e) {
            System.err.println("Errors found (TIMEN):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }

        return (normText.trim() + "|" + pattern.trim()).replaceAll(" ", "_");

    }

    public static String getNormType(String tempexVALUE) {
        String tempexNormType = null;

        // PRESERNT_REF, PAST_REF, ...
        if (tempexVALUE.endsWith("_REF")) {
            tempexNormType = tempexVALUE;
        }
        if (tempexVALUE.matches("P[TX0-9].*")) {
            tempexNormType = "PERIOD";
        } else {
            if (tempexVALUE.matches("[X0-9].*")) {
                tempexNormType = "ISO";
            }
            if (tempexVALUE.matches("X.*")) {
                tempexNormType = "ISOSET";
            }
        }

        if (tempexNormType == null) {
            tempexNormType = "PRESENT_REF";
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("The normalization type cannot be found for (TIMEK):\n\t" + tempexVALUE + "\n");
            }
        }

        return tempexNormType;
    }

    public static Date add_duration_2_timeref(Date timeRef, String duration) throws Exception {
        Date endref = null;

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(timeRef);

        // By default duration=0
        endref = cal.getTime();

        // Depending on TUnit and the Num
        boolean inTime = false;
        int undefinedvalue = 5;
        String value = "";
        //    System.out.println(timeRef+" Finding duration for duration "+duration);
        if (duration.matches("P(T)?[0-9X]+[YMWDHS].*")) {
            //System.out.println(timeRef+" Finding duration for duration "+duration);
            for (int i = 1; i < duration.length(); i++) {
                String currentchar = "" + duration.charAt(i);
                if (currentchar.equals("T")) {
                    inTime = true;
                    continue;
                }
                if (currentchar.matches("[YMWDHS]")) {
                    if (value.equals("") || (!inTime && !currentchar.matches("[YMWD]")) || (inTime && !currentchar.matches("[HMS]"))) {
                        throw new Exception("Malformed duration when getting timeref - " + duration);
                    }
                    int nvalue = undefinedvalue;
                    if (value.matches("[0-9]+")) {
                        nvalue = Integer.parseInt(value);
                    }
                    if (!inTime) {
                        if (currentchar.equals("Y")) {
                            cal.add(GregorianCalendar.YEAR, nvalue);
                        }
                        if (currentchar.equals("M")) {
                            cal.add(GregorianCalendar.MONTH, nvalue);
                        }
                        if (currentchar.equals("W")) {
                            cal.add(GregorianCalendar.WEEK_OF_YEAR, nvalue);
                        }
                        if (currentchar.equals("D")) {
                            cal.add(GregorianCalendar.DAY_OF_YEAR, nvalue);
                        }
                    } else {
                        if (currentchar.equals("H")) {
                            cal.add(GregorianCalendar.HOUR, nvalue);
                        }
                        if (currentchar.equals("M")) {
                            cal.add(GregorianCalendar.MINUTE, nvalue);
                        }
                        if (currentchar.equals("S")) {
                            cal.add(GregorianCalendar.SECOND, nvalue);
                        }
                    }
                    value = "";
                } else { // numero
                    value += currentchar;
                }

            }
            //System.out.println(endref + "----"+cal.getTime());

        }


        endref = cal.getTime();

        return endref;
    }
}
