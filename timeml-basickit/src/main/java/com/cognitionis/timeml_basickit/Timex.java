package com.cognitionis.timeml_basickit;

import java.text.*;
import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public final class Timex extends Element implements Cloneable {

    public static final String granul_years = "yyyy";
    public static final String granul_months = "yyyy-MM";
    public static final String granul_days = "yyyy-MM-dd";
    public static final String granul_time = "yyyy-MM-dd'T'HH:mm";
    public static final String granul_seconds = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateFormat granul_days2 = new SimpleDateFormat("yyyy-MM-dd");
    public static final String upper_bound = "9999-12-31T23:59:59";
    public static final String lower_bound = "0000-01-01T00:00:00";
    private String type;
    private String value;
    private String lower_value;
    private String upper_value;
    private String granularity; // todo
    private Date date;
    private boolean is_DCT;

    public Timex(String i, String ex, String t, String v, String di, long snum, int tnum) {
        this(i, ex, t, v, di, snum, tnum, false);
    }

    public Timex(String i, String ex, String t, String v, String di, long snum, int tnum, boolean idct) {
        this.id = i;
        this.expression = ex;
        this.num_tokens = 1;
        this.type = t;
        this.value = v;
        this.doc_id = di;
        this.sent_num = snum;
        this.tok_num = tnum;
        this.date = null;
        this.is_DCT = idct;
        this.lower_value = null;
        this.upper_value = null;
        try {
            // GregorianCalendar problems (1 million years BCE, years over 9999)
            // ERA solves the AD (Anno Domini)/CE (Common or Current Era) and  BC(before Christ)/BCE (before Common Era)
            //calendar.set(Calendar.ERA, GregorianCalendar.BC);
            // BY NOW: NO YEARS DIFFERENT OF 4 DIGITS
            if (is_ISO8601_date(value)) {
                initialize_interval_values();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                date = df.parse(this.lower_value);
                //DateTimeFormatter fmt = ISODateTimeFormat.dateOptionalTimeParser();
                //date = new DateTime(fmt.parseDateTime(cleanvalue)).toDate();
            }

        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    @Override
    public Timex clone() {
        try {
            return (Timex) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Returns if the Timex contains a concrete date/time
     *
     * @return boolean for being a reference
     */
    public boolean isReference() {
        boolean ret = true;
        if (date == null) {
            ret = false;
        }
        return ret;
    }

    public void set_type(String t) {
        this.type = t;
    }

    public void set_value(String v) {
        this.value = v;
    }

    public void set_is_DCT(boolean idct) {
        this.is_DCT = idct;
    }

    public String get_type() {
        return type;
    }

    public String get_value() {
        return value;
    }

    public Date get_date() {
        return date;
    }

    public boolean is_DCT() {
        return is_DCT;
    }

    /**
     * Get the lower value for the timex value At initialization is null. Once
     * it is obtained once for a Timex it stores the value so it is not
     * calculated again.
     *
     * @return lower value
     */
    public void initialize_interval_values() {
        try {
            if (this.lower_value == null || this.upper_value == null) {
                String[] interval_values_arr = get_interval_value(this.value).split("\\|");
                this.lower_value = interval_values_arr[0];
                this.upper_value = interval_values_arr[1];
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    /**
     * Get the interval values in the format lower|upper given any timex If it
     * is not possible lower and upper will be equal to the given timex value
     *
     * @param value
     * @return
     */
    public static String get_interval_value(String value) {
        try {
            if (value == null) {
                return null;
            }
            String lv = value;
            String uv = value;
            // malformed T with - (-T)
            value = value.replaceAll("-T", "T");
            // one digit hours
            if (value.matches(".*T[0-9]:.*")) {
                value = value.replaceAll("(.*)T(.*)", "$1T0$2");
            }
            // add 0 to <4 dates
            String year = "0000";
            if (value.matches("^[0-9]+$")) {
                if (value.length() > 4) {
                    // error year above 9999
                    throw new Exception("Year above 9999. Omitted.");
                }
            }
            
            if (value.matches("^[0-9]{4}")) {
                year = value.substring(0, 4);
            }
            if (value.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2))")) {
                if (value.matches("(?i).*-(WI|Q1|H1|T1)")) {
                    String february_last_day = "28";
                    if (isLeapYear(Integer.parseInt(year))) {
                        february_last_day = "29";
                    }
                    return  year + "-01-01|" + year + "-02-" + february_last_day;
                }
                if (value.matches("(?i).*-(SP|Q2|T2)")) {
                    return  year + "-03-01|" + year + "-05-31";
                }
                if (value.matches("(?i).*-(SU|Q3|H2|T3)")) {
                    return year + "-06-01|" + year + "-08-31";
                }
                if (value.matches("(?i).*-(AU|FA|Q4|T4)")) {
                    return year + "-09-01|" + year + "-12-31";
                }
            }

            if (value.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T(MO|AF|EV|NI|MI|NO)")) {
                String date_string = value.substring(0, 10);
                // MORNING 5-12
                if (value.matches("(?i).*TMO")) {
                    return  date_string + "T05:00:00|" + date_string + "T11:59:59";
                } // NOON 12
                if (value.matches("(?i).*TNO")) {
                    return date_string + "T12:00:00|" + date_string + "T12:00:01";
                } // AFTERNOON 13
                if (value.matches("(?i).*TAF")) {
                    return date_string + "T12:00:01|" + date_string + "T17:59:59";
                } // DEPEND ON WORK BREAKS 17-18
                if (value.matches("(?i).*TEV")) {
                    return date_string + "T18:00:00|" + date_string + "T20:59:59";
                } // AFTER WORK... GOING BACK HOME...
                if (value.matches("(?i).*TNI")) {
                    return date_string + "T21:00:00|" + date_string + "T23:59:59";
                } // MIDNIGHT
                if (value.matches("(?i).*TMI")) {
                    return date_string + "T00:00:00|" + date_string + "T00:00:01";
                }
            }

            if (value.matches("(?i)[0-9]{4}-W[0-9]{2}(-WE)?")) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date dateaux = df.parse(year + "-01-01");
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(dateaux);
                SimpleDateFormat df2 = new SimpleDateFormat(granul_days);
                cal.add(GregorianCalendar.WEEK_OF_YEAR, Integer.parseInt(value.substring(6, 8)));
                lv = df2.format(cal.getTime()) + "T00:00:00";
                cal.add(GregorianCalendar.DAY_OF_MONTH, 6);
                uv = df2.format(cal.getTime()) + "T23:59:59";
                if (value.endsWith("WE")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    lv = df2.format(cal.getTime()) + "T00:00:00";
                }
                return  lv + "|" + uv;
            }

            //months and days and normal times (clean)
            if (is_clean_ISO8601_date_part(value)) {
                lv = value + lower_bound.substring(value.length());
                uv = value + upper_bound.substring(value.length());
                if (value.length() == 7) { // months
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date dateaux = df.parse(lv);
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(dateaux);
                    // calculate last day of the month
                    cal.add(GregorianCalendar.MONTH, 1);
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    uv = granul_days2.format(cal.getTime()) + upper_bound.substring(value.length() + 3);
                }
            }


            return  lv + "|" + uv;
        } catch (Exception e) {
            System.err.println("Errors found (Timex):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
    }

    public String get_lower_value() {
        if (this.lower_value == null) {
            initialize_interval_values();
        }
        return this.lower_value;
    }

    public String get_upper_value() {
        if (this.upper_value == null) {
            initialize_interval_values();
        }
        return this.upper_value;
    }

    
    
    /**
     * True if the string is a valid ISO8601
     *
     * @param value
     * @return
     */
    static boolean is_ISO8601_date(String value) {
        if(value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(  T([0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)? | (MO|AF|EV|NI|MI|NO) )  )?|-W[0-5][0-9](-WE)?)|-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2)))?)?)?)?")){
            return true;
        }
        return false;
    }    

    /**
     * True if the string is a valid clean ISO8601 (yyyy-MM-dd'T'HH:mm:ss)
     *
     * @param value
     * @return
     */
    static boolean is_clean_ISO8601_date_part(String value) {
        if(value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(T[0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)?)?)?)?)?)?)?")){
            return true;
        }
        return false;
    }    
    
    
    /**
     * True if a given year is a lap year
     *
     * @param year
     * @return
     */
    static boolean isLeapYear(final int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }
}
