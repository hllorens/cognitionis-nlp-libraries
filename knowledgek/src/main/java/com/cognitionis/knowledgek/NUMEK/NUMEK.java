package com.cognitionis.knowledgek.NUMEK;

import com.cognitionis.utils_basickit.StringUtils;
import java.util.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class NUMEK {

    public static enum AvailableLocales {

        EN, US, GB, ES;
    }
    private Locale locale;

    // NEDED BECAUSE DOUBLE DOES NOT SAVE OBJECTS BY REFERENCE...
    private class Number {

        public Double value;

        public Number() {
            value = 0.0;
        }

        public Number(Double v) {
            value = v;
        }
    }
    // Freeling does recognition and resolution it for English, Spanish and Catalan
    public final static Integer MAX_NUMBERS_ORDER = 99;  // Explicit numbers (one token) MAX order of magnitude
    // Magnitude is whatever can have numbers or lower order magnitudes on the left
    // A number is whatever distinct to magnitude that cannot be operated with magnitudes on the left
    public static String numbers_re_EN = "(?i)(?:one|a|an|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth|seventeenth|eighteenth|nineteenth|twentieth|(?:hundred|thousand|million|billion|pair|couple|dozen)(?:s)?)";
    public static String units_re_EN = "(?i)(?:one|a|an|two|three|four|five|six|seven|eight|nine)";
    public static String tens_re_EN = "(?i)(?:ten|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)";
    public static String irregular_tens_re_EN = "(?i)(?:eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen)";

    public static String numbers_re_ES = "(?i)(?:un|uno|una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|cartorce|quince|dieciséis|diecisiete|dieciocho|diecinueve|veinte|treinta|cuarenta|cincuenta|sesenta|setenta|ochenta|noventa|primer(?:o|a)?|segund(?:o|a)|tercer(?:o|a)?|cuart(?:o|a)|quint(?:o|a)|sext(?:o|a)|séptim(?:o|a)|octav(?:o|a)|noven(?:o|a)|décim(?:o|a)|undécim(?:o|a)|duodécim(?:o|a)|cien|ciento|mil|millon|veintiuno|veintiuna|veintidós|veintitrés|veinticuatro|veinticinco|veintiséis|veintisiete|veintiocho|veintinueve|ciento|doscient[oa]s|trescient[oa]s|cuatrocient[oa]s|quinient[oa]s|seicient[oa]s|setecient[oa]s|novecient[oa]s|millones)";
    public static String units_re_ES = "(?i)(?:un|uno|una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve)";
    public static String tens_re_ES = "(?i)(?:diez|veinte|treinta|cuarenta|cincuenta|sesenta|setenta|ochenta|noventa)";
    public static String irregular_tens_re_ES = "(?i)(?:once|doce|trece|cartorce|quince|dieciséis|diecisiete|dieciocho|diecinueve)";


    public static String ordinals_re_EN = "(?i)(first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth|seventeenth|eighteenth|nineteenth|twentieth)";
    public static String ordinals_re_ES = "(?i)(primer(?:o|a)?|segund(?:o|a)|tercer(?:o|a)?|cuart(?:o|a)|quint(?:o|a)|sext(?:o|a)|séptim(?:o|a)|octav(?:o|a)|noven(?:o|a)|décim(?:o|a)|undécim(?:o|a)|duodécim(?:o|a))";
    public HashMap<String, Integer> units, irregular_tens, tens, magnitudes, restric, ordinals, numgrups;
    public String numdelim, numbers_re, ordinals_re, units_re, tens_re, irregular_tens_re;
    public final static HashMap<String, Integer> units_EN = new HashMap<String, Integer>();

    static {
        units_EN.put("zero", 0);
        units_EN.put("a", 1);
        units_EN.put("an", 1);
        units_EN.put("one", 1);
        units_EN.put("two", 2);
        units_EN.put("three", 3);
        units_EN.put("four", 4);
        units_EN.put("five", 5);
        units_EN.put("six", 6);
        units_EN.put("seven", 7);
        units_EN.put("eight", 8);
        units_EN.put("nine", 9);
    }
    public final static HashMap<String, Integer> irregular_tens_EN = new HashMap<String, Integer>();

    static {
        irregular_tens_EN.put("ten", 10);        // (irregular*
        irregular_tens_EN.put("eleven", 11);
        irregular_tens_EN.put("twelve", 12);
        irregular_tens_EN.put("thirteen", 13);
        irregular_tens_EN.put("fourteen", 14);
        irregular_tens_EN.put("fifteen", 15);
        irregular_tens_EN.put("sixteen", 16);
        irregular_tens_EN.put("seventeen", 17);
        irregular_tens_EN.put("eighteen", 18);
        irregular_tens_EN.put("nineteen", 19);   // *irregular)
    }
    public final static HashMap<String, Integer> tens_EN = new HashMap<String, Integer>();

    static {
        tens_EN.put("twenty", 20);
        tens_EN.put("thirty", 30);
        tens_EN.put("forty", 40);
        tens_EN.put("fifty", 50);
        tens_EN.put("sixty", 60);
        tens_EN.put("seventy", 70);
        tens_EN.put("eighty", 80);
        tens_EN.put("ninety", 90);
    }
    public final static HashMap<String, Integer> magnitudes_EN = new HashMap<String, Integer>();

    static {
        magnitudes_EN.put("hundred", 100);
        magnitudes_EN.put("thousand", 1000);
        magnitudes_EN.put("million", 1000000);
        magnitudes_EN.put("billion", 1000000000);
    }
    public final static HashMap<String, Integer> restric_EN = new HashMap<String, Integer>();

    static {
        restric_EN.put("hundred", 1);
        //restric put (xxx, 10);
    }
    public final static HashMap<String, Integer> ordinals_EN = new HashMap<String, Integer>();

    static {
        ordinals_EN.put("first", 1);
        ordinals_EN.put("second", 2);
        ordinals_EN.put("third", 3);
        ordinals_EN.put("fourth", 4);
        ordinals_EN.put("fifth", 5);
        ordinals_EN.put("sixth", 6);
        ordinals_EN.put("seventh", 7);
        ordinals_EN.put("eighth", 8);
        ordinals_EN.put("ninth", 9);
        ordinals_EN.put("tenth", 10);
        ordinals_EN.put("eleventh", 11);
        ordinals_EN.put("twelfth", 12);
        ordinals_EN.put("thirteenth", 13);
        ordinals_EN.put("fourteenth", 14);
        ordinals_EN.put("fifteenth", 15);
        ordinals_EN.put("sixteenth", 16);
        ordinals_EN.put("seventeenth", 17);
        ordinals_EN.put("eighteenth", 18);
        ordinals_EN.put("nineteenth", 19);
        ordinals_EN.put("twentieth", 20);
    }
    public final static HashMap<String, Integer> numgrups_EN = new HashMap<String, Integer>();

    static {
        numgrups_EN.put("pair", 2);
        numgrups_EN.put("couple", 2);
        numgrups_EN.put("dozen", 12);
    }
    public final static String numdelim_EN = "(-|and)";
    public final static HashMap<String, Integer> units_ES = new HashMap<String, Integer>();

    static {
        units_ES.put("cero", 0);
        units_ES.put("uno", 1);
        units_ES.put("un", 1);
        units_ES.put("una", 1);
        units_ES.put("dos", 2);
        units_ES.put("tres", 3);
        units_ES.put("cuatro", 4);
        units_ES.put("cinco", 5);
        units_ES.put("seis", 6);
        units_ES.put("siete", 7);
        units_ES.put("ocho", 8);
        units_ES.put("nueve", 9);
    }
    public final static HashMap<String, Integer> irregular_tens_ES = new HashMap<String, Integer>();

    static {
        irregular_tens_ES.put("diez", 10);
        irregular_tens_ES.put("once", 11);
        irregular_tens_ES.put("doce", 12);
        irregular_tens_ES.put("trece", 13);
        irregular_tens_ES.put("catorce", 14);
        irregular_tens_ES.put("quince", 15);
        irregular_tens_ES.put("dieciséis", 16);
        irregular_tens_ES.put("diecisiete", 17);
        irregular_tens_ES.put("dieciocho", 18);
        irregular_tens_ES.put("diecinueve", 19);
        irregular_tens_ES.put("veinte", 20);
        irregular_tens_ES.put("veintiuno", 21);
        irregular_tens_ES.put("veintiuna", 21);
        irregular_tens_ES.put("veintidós", 22);
        irregular_tens_ES.put("veintitrés", 23);
        irregular_tens_ES.put("veinticuatro", 24);
        irregular_tens_ES.put("veinticinco", 25);
        irregular_tens_ES.put("veintiséis", 26);
        irregular_tens_ES.put("veintisiete", 27);
        irregular_tens_ES.put("veintiocho", 28);
        irregular_tens_ES.put("veintinueve", 29);
    }
    public final static HashMap<String, Integer> tens_ES = new HashMap<String, Integer>();

    static {
        tens_ES.put("treinta", 30);
        tens_ES.put("cuarenta", 40);
        tens_ES.put("cincuenta", 50);
        tens_ES.put("sesenta", 60);
        tens_ES.put("setenta", 70);
        tens_ES.put("ochenta", 80);
        tens_ES.put("noventa", 90);
    }
    public final static HashMap<String, Integer> magnitudes_ES = new HashMap<String, Integer>();

    static {
        magnitudes_ES.put("cien", 100);
        magnitudes_ES.put("ciento", 100);
        magnitudes_ES.put("doscientos", 200);
        magnitudes_ES.put("trescientos", 300);
        magnitudes_ES.put("cuatrocientos", 400);
        magnitudes_ES.put("quinientos", 500);
        magnitudes_ES.put("seiscientos", 600);
        magnitudes_ES.put("sietecientos", 700);
        magnitudes_ES.put("ochocientos", 800);
        magnitudes_ES.put("novecientos", 900);
        magnitudes_ES.put("doscientas", 200);
        magnitudes_ES.put("trescientas", 300);
        magnitudes_ES.put("cuatrocientas", 400);
        magnitudes_ES.put("quinientas", 500);
        magnitudes_ES.put("seiscientas", 600);
        magnitudes_ES.put("sietecientas", 700);
        magnitudes_ES.put("ochocientas", 800);
        magnitudes_ES.put("novecientas", 900);
        magnitudes_ES.put("mil", 1000);
        magnitudes_ES.put("millón", 1000000);
        magnitudes_ES.put("millones", 1000000);
    }
    public final static HashMap<String, Integer> restric_ES = new HashMap<String, Integer>();

    static {
        restric_ES.put("cien", 0);
        restric_ES.put("ciento", 0);
        restric_ES.put("doscientos", 0);
        restric_ES.put("trescientos", 0);
        restric_ES.put("cuatrocientos", 0);
        restric_ES.put("quinientos", 0);
        restric_ES.put("seiscientos", 0);
        restric_ES.put("sietecientos", 0);
        restric_ES.put("ochocientos", 0);
        restric_ES.put("novecientos", 0);
        restric_ES.put("doscientas", 0);
        restric_ES.put("trescientas", 0);
        restric_ES.put("cuatrocientas", 0);
        restric_ES.put("quinientas", 0);
        restric_ES.put("seiscientas", 0);
        restric_ES.put("sietecientas", 0);
        restric_ES.put("ochocientas", 0);
        restric_ES.put("novecientas", 0);
        //restric put (xxx, 10);
    }
    public final static HashMap<String, Integer> ordinals_ES = new HashMap<String, Integer>();

    static {
        // TODO FALTA PONER EL RESTO DE ORDINALES...
        ordinals_ES.put("primero", 1);
        ordinals_ES.put("segundo", 2);
        ordinals_ES.put("tercero", 3);
        ordinals_ES.put("cuarto", 4);
        ordinals_ES.put("quinto", 5);
        ordinals_ES.put("sexto", 6);
        ordinals_ES.put("séptimo", 7);
        ordinals_ES.put("octava", 8);
        ordinals_ES.put("novena", 9);
        ordinals_ES.put("décima", 10);
        ordinals_ES.put("undécima", 11);
        ordinals_ES.put("duodécima", 12);
        ordinals_ES.put("primera", 1);
        ordinals_ES.put("segunda", 2);
        ordinals_ES.put("tercera", 3);
        ordinals_ES.put("cuarta", 4);
        ordinals_ES.put("quinta", 5);
        ordinals_ES.put("sexta", 6);
        ordinals_ES.put("séptima", 7);
        ordinals_ES.put("octava", 8);
        ordinals_ES.put("novena", 9);
        ordinals_ES.put("décima", 10);
        ordinals_ES.put("undécima", 11);
        ordinals_ES.put("duodécima", 12);
        ordinals_ES.put("primer", 1);
        ordinals_ES.put("tercer", 3);
    }
    public final static HashMap<String, Integer> numgrups_ES = new HashMap<String, Integer>();

    static {
        numgrups_ES.put("par", 2);
        numgrups_ES.put("docena", 12);
    }
    public final static String numdelim_ES = "(-|y)";

    public NUMEK() {
        this(Locale.getDefault());
    }

    public NUMEK(Locale l) {
        loadManualinfo(l);
        locale = l;
    }

    public void loadManualinfo(Locale l) {
        String lang = l.getLanguage().toUpperCase();
        try {
            switch (AvailableLocales.valueOf(lang)) {
                case ES:
                    units = units_ES;
                    irregular_tens = irregular_tens_ES;
                    tens = tens_ES;
                    magnitudes = magnitudes_ES;
                    restric = restric_ES;
                    ordinals = ordinals_ES;
                    numgrups = numgrups_ES;
                    numdelim = numdelim_ES;
                    numbers_re = numbers_re_ES;
                    units_re = units_re_ES;
                    tens_re = tens_re_ES;
                    irregular_tens_re = irregular_tens_re_ES;
                    ordinals_re = ordinals_re_ES;
                    break;
                case EN:
                case US:
                case GB:
                    units = units_EN;
                    irregular_tens = irregular_tens_EN;
                    tens = tens_EN;
                    magnitudes = magnitudes_EN;
                    restric = restric_EN;
                    ordinals = ordinals_EN;
                    numgrups = numgrups_EN;
                    numdelim = numdelim_EN;
                    numbers_re = numbers_re_EN;
                    units_re = units_re_EN;
                    tens_re = tens_re_EN;
                    irregular_tens_re = irregular_tens_re_EN;
                    ordinals_re = ordinals_re_EN;
                    break;
            }
        } catch (Exception e) {
            units = units_EN;
            irregular_tens = irregular_tens_EN;
            tens = tens_EN;
            magnitudes = magnitudes_EN;
            restric = restric_EN;
            ordinals = ordinals_EN;
            numgrups = numgrups_EN;
            numdelim = numdelim_EN;
            numbers_re = numbers_re_EN;
            units_re = units_re_EN;
            tens_re = tens_re_EN;
            irregular_tens_re = irregular_tens_re_EN;
            ordinals_re = ordinals_re_EN;
        }
    }

    /**
     * Returns value of a fraction (e.g., 1/2 --> 0.5) or the sum of a number and a fraction (e.g., 2 1/2 --> 2.5).
     *
     * @param number (separator is whitespace " ")
     *
     * @return result
     */
    public static Double calc_and_sum_frac(String snumber) {
        if (snumber.matches("([0-9]+ )?[0-9]+/[0-9]+")) {
            String[] temp;
            if (snumber.contains(" ")) {
                temp = snumber.split(" ");
                return Double.parseDouble(temp[0]) + Double.parseDouble(temp[1].substring(0, temp[1].indexOf('/'))) / Double.parseDouble(temp[1].substring(temp[1].indexOf('/') + 1));
            } else {
                return Double.parseDouble(snumber.substring(0, snumber.indexOf('/'))) / Double.parseDouble(snumber.substring(snumber.indexOf('/') + 1));
            }
        } else {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Warining: error normalizing fraction (" + snumber + ") it has been set to 0.0 by default.");
            }
            return 0.0;
        }
    }

    // UK English       "and" is hundreds/thousands and tens/units separator
    // UK/US English    "-" is tens and units separator
    // 1506 --> one thousand five hundred and six
    // English units are always singular. Plural (milions) are only informal/indefinite (e.g., there were millions of people)
    // FREELING ERRORS IN NUMEK:      Eleven_hundred_ten_thousand_and_six = 1110006 (tens Hundreds are correct in English "informal")
    public String text2number(String snumber) {
        //Double number = new Double(0.0); // NO BECAUSE DO NOT SAVE OBJECTS BY REFERENCE...
        Number number = new Number();
        try {
            snumber = snumber.trim().toLowerCase().replaceAll("\\s+(st|nd|rd|th|o|a)$", "");
            // a twenty year period exception
            snumber = snumber.replaceAll("^(?:a|an)\\s+((?:"+units_re+"|"+tens_re+"|"+irregular_tens_re+").*)$", "$1");

            Integer magnitude = 0; // order of magnitude
            Integer max_magn = MAX_NUMBERS_ORDER;
            String[] elements = snumber.split("(\\s*-\\s*|\\s+" + numdelim + "\\s+|\\s+)");

            //System.out.println(snumber + " - numelems: "+elements.length);

            // already numeric expression
            if (locale.getLanguage().equalsIgnoreCase("es")) {
                snumber = snumber.replaceFirst(",", ".");
                if (StringUtils.countOccurrencesOf(snumber, '.') > 1) {
                    do {
                        snumber = snumber.replaceFirst("\\.", "");
                    } while (StringUtils.countOccurrencesOf(snumber, '.') > 1);
                }
            }

            if ((locale.getLanguage().equalsIgnoreCase("en") && snumber.matches("half"))
                    || (locale.getLanguage().equalsIgnoreCase("es") && snumber.matches("medi(a|o)"))) {
                return "0.5";
            }

            if (locale.getLanguage().equalsIgnoreCase("en")) {
                snumber = snumber.replaceAll(",", "");
            }
            if (snumber.matches("(-)?[0-9]+(\\.[0-9]+)?")) {
                return snumber;
            }
            if (snumber.matches("(-)?[0-9]+%")) {
                return "" + (Double.parseDouble(snumber.substring(0, snumber.length() - 1)) / 100);
            }

            if (snumber.matches("([0-9]+ )?[0-9]+/[0-9]+")) {
                return "" + calc_and_sum_frac(snumber);
            }


            for (int i = 0; i < elements.length; i++) {
                if (i == 0 && !elements[i].matches("[0-9]+") && !units.containsKey(elements[i]) && !tens.containsKey(elements[i]) && !irregular_tens.containsKey(elements[i]) && !magnitudes.containsKey(elements[i]) && !ordinals.containsKey(elements[i]) && !numgrups.containsKey(elements[i])) {
                    throw new Exception("Unknown element (0): " + elements[i] + " in " + snumber);
                }
                if (i != 0 && !units.containsKey(elements[i]) && !tens.containsKey(elements[i]) && !irregular_tens.containsKey(elements[i]) && !magnitudes.containsKey(elements[i])  && !numgrups.containsKey(elements[i])) {
                    throw new Exception("Unknown element (" + i + "): " + elements[i] + " in " + snumber);
                }
            }

            // date spelled number (nineteen eighty == 1980, 2010, 1919,2081,1981)
            if ((elements.length == 2 || elements.length == 3) && (irregular_tens.containsKey(elements[0]) || tens.containsKey(elements[0])) && !magnitudes.containsKey(elements[1]) && (tens.containsKey(elements[1]) || irregular_tens.containsKey(elements[1]))) {
                Integer value=0;
                if(irregular_tens.containsKey(elements[0])){
                    value = irregular_tens.get(elements[0]) * 100;
                }else{
                    value = tens.get(elements[0]) * 100;
                }
                if(irregular_tens.containsKey(elements[1])){
                    value+=irregular_tens.get(elements[1]);
                }else{
                    value+=tens.get(elements[1]);
                }
                if (elements.length == 3 && units.containsKey(elements[2])) {
                    value += units.get(elements[2]);
                }
                return "" + value;
            }
            
            // ordinals for quarters
            if (elements.length == 1 && ordinals.containsKey(elements[0])) {
                return "" + ordinals.get(elements[0]);
            }

            // num grup expression
            if (elements.length == 1 && numgrups.containsKey(elements[0])) {
                return "" + numgrups.get(elements[0]);
            }
            if (elements.length == 2 && units.containsKey(elements[0]) && numgrups.containsKey(elements[1])) {
                return "" + (units.get(elements[0]) * numgrups.get(elements[1]));
            }


            // number + magnitude expression
            if (elements[0].matches("([0-9]+|([0-9]*.[0-9]+))")) {
                if (elements.length != 2) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.err.println("Only the number and the first magnitude will be normalized: " + snumber);
                    }
                }
                Integer magn = magnitudes.get(elements[1]);
                if (magn == null) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.err.println("In [0-9]+ magnitude numbers the second component must be a valid magnitude. Found: " + elements[1] + "   from: " + snumber);
                    }
                    magn = 1;
                }
                // TODO provar a ver si va .5
                return removeUselessDecimals("" + (Double.parseDouble(elements[0]) * magn));


            } else { // regular spelled number
                // the number (Number) object increases while i increases from units to the highest magnitude-unit pair
                Integer i = elements.length - 1; // i means the analyzed position from units to the highest magnitude-unit pair
                while (i >= 0) {
                    String element = elements[i].trim();
                    magnitude = magnitudes.get(element);
                    //System.err.println("i=" + i + " - " + element);
                    if (magnitude != null) {
                        //operate magnitude
                        if (magnitude <= (((int) Math.pow(10, number.value.toString().substring(0, number.value.toString().lastIndexOf('.')).length()) - 1))) {
                            throw new Exception("Greater magnitude expected in " + element + " (" + snumber + ")");
                        }
                        i = operateMagnitude(elements, i, number);
                    } else {
                        // operate number (only at rightest position)
                        if (i != (elements.length - 1)) {
                            // TODO no exception but warning...
                            throw new Exception("Unexpected number when looking for a manitude. Found: " + element + " (" + snumber + ")");
                        }
                        i = getNumber(elements, i, number, max_magn);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (NUMEK):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                //System.exit(1);
            }
            //return snumber.replaceAll(" ", "-"); // not null nor textual string because it breaks the application
            if(number.value!=null && number.value>0){
                return removeUselessDecimals("" + number.value);
            }else{
                return "1";
            }
        }

        // remove useless decimals
        return removeUselessDecimals("" + number.value);
    }

    private String removeUselessDecimals(String numeknum) {
        if (numeknum.matches(".*\\.(0)+")) {
            numeknum = numeknum.substring(0, numeknum.lastIndexOf('.'));
        }
        return numeknum;
    }

    private Integer getNumber(String[] elements, Integer i, Number number, Integer maxorder) {
        Integer ret = i;
        try {
            if (maxorder > MAX_NUMBERS_ORDER) {
                maxorder = MAX_NUMBERS_ORDER;
            }
            if (maxorder <= 0) {
                throw new Exception("Order not in correct range : " + maxorder);
            }
            if (maxorder <= 10) {
                ret = ret - look4units(elements[i], number);
            }
            if (maxorder > 10) {
                ret = i - look4irregular_tens(elements[i], number);
                if (ret == i) {
                    ret = i - look4tens(elements[i], number);
                    if (ret == i) {
                        ret = ret - look4units(elements[i], number);
                        if (ret == i) {
                            throw new Exception("Malformed number: " + elements[i]);
                        }
                        if (i > 0) {
                            ret = ret - look4tens(elements[i - 1], number);
                        }
                    }
                }
            }
            if (ret == i) {
                throw new Exception("Malformed number, unexected (max order " + maxorder + "): " + elements[i]);
            }

        } catch (Exception e) {
            System.err.println("Errors found (NUMEK):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                //System.exit(1);
            }
            return -1;
        }
        return ret;
    }

    private Integer operateMagnitude(String[] elements, Integer i, Number number) {
        try {
            Number magnumber = new Number();
            //Double magnumber = 0.0;
            String magnitude = elements[i].trim();
            Integer magnvalue = magnitudes.get(magnitude);
            if (!magnitudes.containsKey(magnitude)) {
                throw new Exception("Expected magnitude, found " + magnitude);
            }
            //System.err.println("Magnitude: " + magnitude);
            Integer maxmagn = magnvalue - 1;

            if (restric.containsKey(magnitude)) {
                maxmagn = restric.get(magnitude);
            }

            i--;

            if (i >= 0) {
                Integer currentmagn = 0;
                while (i >= 0) {
                    // if there is a number and it can be operated it then operate it
                    if (!magnitudes.containsKey(elements[i])) {
                        if (currentmagn > MAX_NUMBERS_ORDER) {
                            throw new Exception("Expected magnitude, found " + elements[i]);
                        }
                        i = getNumber(elements, i, magnumber, maxmagn);
                        currentmagn = ((int) Math.pow(10, number.value.toString().substring(0, number.value.toString().lastIndexOf('.')).length()));
                    } else {
                        // If magnitudes can be eelements[i]xpected for the current magnitude
                        if (magnitudes.get(elements[i]) < maxmagn) {
                            i = operateMagnitude(elements, i, magnumber);
                        } else {  // resolver magnitud present y subir arriba para seguir operando
                            //if no value for magnitude go by
                            if (magnumber.value == 0L) {
                                magnumber.value = 1.0;
                            }
                            // finally operate whatever and break
                            number.value += magnumber.value * magnvalue;
                            magnumber.value = 0.0;
                            break;
                        }
                    }
                }
                if (magnumber.value != 0L) {
                    number.value += magnumber.value * magnvalue;
                }
            } else {
                magnumber.value = 1.0;
                number.value += magnumber.value * magnvalue;
                i--;
            }
        } catch (Exception e) {
            System.err.println("Errors found (NUMEK):\n\t" + e.toString());
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                //System.exit(1);
            }
            return -1;
        }
        return i;

    }

    private Integer look4units(String element, Number number) {
        if (units.get(element) != null) {
            number.value += units.get(element);
            return 1;
        }
        return 0;
    }

    private Integer look4irregular_tens(String element, Number number) {
        if (irregular_tens.get(element) != null) {
            number.value += irregular_tens.get(element);
            return 1;
        }
        return 0;
    }

    private Integer look4tens(String element, Number number) {
        if (tens.get(element) != null) {
            number.value += tens.get(element);
            return 1;
        }
        return 0;
    }
}
