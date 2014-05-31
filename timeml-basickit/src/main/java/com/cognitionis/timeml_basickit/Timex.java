package com.cognitionis.timeml_basickit;

import java.text.*;
import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class Timex extends Element implements Cloneable {

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
            String cleanvalue = ISOclean(value);
            if (cleanvalue != null && cleanvalue.matches("^[0-9][0-9TW:-]+")) {
                // SEASONS? TODs?
                // GregorianCalendar problems (1 million years BCE, years over 9999)
                // ERA solves the AD (Anno Domini)/CE (Common or Current Era) and  BC(before Christ)/BCE (before Common Era)
                //calendar.set(Calendar.ERA, GregorianCalendar.BC);
                // BY NOW: NO YEARS DIFFERENT OF 4 DIGITS

                // add 0 to <4 dates
                if (!cleanvalue.matches("[0-9]{4}(-.*)?")) {
                    if (cleanvalue.matches("[0-9]+")) {
                        if (cleanvalue.length() < 4) {
                            for (int x = cleanvalue.length(); x <= 4; x++) {
                                cleanvalue += "0";
                            }
                        } else {
                            // error year above 9999
                            throw new Exception("Year above 9999. Omitted.");
                        }
                    }
                } else {
                    // error bad format
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                date = df.parse(cleanvalue);
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
     * Given a String TimeML date/time value it returns a propver ISO/Java
     * date/time. It replaces WI, SU, MO, AF etc.
     *
     * @param date String of the TimeML ISO 8601 Version...
     * @return clean date
     */
    public static String ISOclean(String date) {
        if (date != null) {
            // malformed T with - (-T)
            date = date.replaceAll("-T", "T");
            // one digit hours
            if (date.matches(".*T[0-9]:.*")) {
                date = date.replaceAll("(.*)T(.*)", "$1T0$2");
            }
            if (date.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2))")) {
                if (date.matches("(?i).*-(WI|Q1|H1|T1)")) {
                    date = date.substring(0, 4) + "-01";
                }
                if (date.matches("(?i).*-(SP|Q2|T2)")) {
                    date = date.substring(0, 4) + "-03";
                }
                if (date.matches("(?i).*-(SU|Q3|H2|T3)")) {
                    date = date.substring(0, 4) + "-06";
                }
                if (date.matches("(?i).*-(AU|FA|Q4|T4)")) {
                    date = date.substring(0, 4) + "-09";
                }
            }

            if (date.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T(MO|AF|EV|NI|MI|NO)")) {
                // MORNING 5-12
                if (date.matches("(?i).*TMO")) {
                    date = date.substring(0, 10) + "T05:00";
                } // NOON 12
                if (date.matches("(?i).*TNO")) {
                    date = date.substring(0, 10) + "T12:00";
                } // AFTERNOON 13
                if (date.matches("(?i).*TAF")) {
                    date = date.substring(0, 10) + "T13:00";
                } // DEPEND ON WORK BREAKS 17-18
                if (date.matches("(?i).*TEV")) {
                    date = date.substring(0, 10) + "T18:00";
                } // AFTER WORK... GOING BACK HOME...
                if (date.matches("(?i).*TNI")) {
                    date = date.substring(0, 10) + "T21:00";
                } // MIDNIGHT
                if (date.matches("(?i).*TMI")) {
                    date = date.substring(0, 10) + "T00:00";
                }
            }

            if (date.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T[3-9].*") || date.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T[2][4-9].*")) {
                System.err.println("Bad time normalization: " + date);
                date = date.substring(0, 10);
            } // TODO: IN THE FUTURE TREAT BETTER THIS DATES
            // GET THE DATE WITHOUT WE AND THEN SET DAY TO SATURDAY...
            // GET BACK THE DATE IN STRING FORMAT WITH DAY GRANULARITY
            if (date.matches("(?i).*-WE")) {
                date = date.substring(0, date.length() - 3);
            }

            // TODO: DON'T USE JODA TIME... TOO COMPLICATED
            // JODA TIME RARE ERROR
            if (date.equals("1901")) {
                date = "1901-01-02";
            }
        }
        return date;
    }

    /**
     * Get the lower value for the timex value
     *
     * @return lower value
     */
    public void get_interval_values() {
        try {
            String lv = this.lower_value;
            String uv = this.upper_value;
            if (lv == null || uv == null) {
                lv = value;
                uv = value;
                if (value.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2))")) {
                    if (value.matches("(?i).*-(WI|Q1|H1|T1)")) {
                        lv = value.substring(0, 4) + "-01-01";
                        uv = value.substring(0, 4) + "-03-01";
                    }
                    if (value.matches("(?i).*-(SP|Q2|T2)")) {
                        lv = value.substring(0, 4) + "-03-01";
                        uv = value.substring(0, 4) + "-05-31";
                    }
                    if (value.matches("(?i).*-(SU|Q3|H2|T3)")) {
                        lv = value.substring(0, 4) + "-06-01";
                        uv = value.substring(0, 4) + "-08-31";
                    }
                    if (value.matches("(?i).*-(AU|FA|Q4|T4)")) {
                        lv = value.substring(0, 4) + "-09-01";
                        uv = value.substring(0, 4) + "-12-31";
                    }
                }

                if (value.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T(MO|AF|EV|NI|MI|NO)")) {
                    // MORNING 5-12
                    if (value.matches("(?i).*TMO")) {
                        lv = value.substring(0, 10) + "T05:00:00";
                        uv = value.substring(0, 10) + "T11:59:59";
                    } // NOON 12
                    if (value.matches("(?i).*TNO")) {
                        lv = value.substring(0, 10) + "T12:00:00";
                        uv = value.substring(0, 10) + "T12:00:01";
                    } // AFTERNOON 13
                    if (value.matches("(?i).*TAF")) {
                        lv = value.substring(0, 10) + "T12:00:01";
                        uv = value.substring(0, 10) + "T17:59:59";
                    } // DEPEND ON WORK BREAKS 17-18
                    if (value.matches("(?i).*TEV")) {
                        lv = value.substring(0, 10) + "T18:00:00";
                        uv = value.substring(0, 10) + "T20:59:59";
                    } // AFTER WORK... GOING BACK HOME...
                    if (value.matches("(?i).*TNI")) {
                        lv = value.substring(0, 10) + "T21:00:00";
                        uv = value.substring(0, 10) + "T04:59:59";
                    } // MIDNIGHT
                    if (value.matches("(?i).*TMI")) {
                        lv = value.substring(0, 10) + "T00:00:00";
                        uv = value.substring(0, 10) + "T00:00:01";
                    }
                }

                if (value.matches("(?i)[0-9]{4}-W[0-9]{2}(-WE)?")) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date dateaux = df.parse(value);
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(dateaux);

                    SimpleDateFormat df2 = new SimpleDateFormat(granul_days);
                    lv = df2.format(cal.getTime()) + "T00:00:00";
                    cal.add(GregorianCalendar.DAY_OF_MONTH, 6);
                    uv = df2.format(cal.getTime()) + "T23:59:59";
                    if (value.endsWith("WE")) {
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                        lv = df2.format(cal.getTime()) + "T00:00:00";
                    }
                }

                if (value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(T[0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)?)?)?)?)?)?)?")) {
                    lv = value + lower_bound.substring(value.length());
                    uv = value + upper_bound.substring(value.length());
                    if (value.length() == 7) {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date dateaux = df.parse(value);
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(dateaux);
                        cal.add(GregorianCalendar.MONTH, 1);
                        cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                        uv = granul_days2.format(cal.getTime()) + upper_bound.substring(value.length() + 3);
                    }
                }


                this.lower_value = lv;
                this.upper_value = uv;
            }
        } catch (Exception e) {
            System.err.println("Errors found (" + this.getClass().getSimpleName() + "):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    public static String get_interval_value(String value) {
        try {
            String lv = value;
            String uv = value;
            if (value.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2))")) {
                if (value.matches("(?i).*-(WI|Q1|H1|T1)")) {
                    lv = value.substring(0, 4) + "-01-01";
                    uv = value.substring(0, 4) + "-03-01";
                }
                if (value.matches("(?i).*-(SP|Q2|T2)")) {
                    lv = value.substring(0, 4) + "-03-01";
                    uv = value.substring(0, 4) + "-05-31";
                }
                if (value.matches("(?i).*-(SU|Q3|H2|T3)")) {
                    lv = value.substring(0, 4) + "-06-01";
                    uv = value.substring(0, 4) + "-08-31";
                }
                if (value.matches("(?i).*-(AU|FA|Q4|T4)")) {
                    lv = value.substring(0, 4) + "-09-01";
                    uv = value.substring(0, 4) + "-12-31";
                }
            }

            if (value.matches("(?i)[0-9]{4}-[0-9]{2}-[0-9]{2}T(MO|AF|EV|NI|MI|NO)")) {
                // MORNING 5-12
                if (value.matches("(?i).*TMO")) {
                    lv = value.substring(0, 10) + "T05:00:00";
                    uv = value.substring(0, 10) + "T11:59:59";
                } // NOON 12
                if (value.matches("(?i).*TNO")) {
                    lv = value.substring(0, 10) + "T12:00:00";
                    uv = value.substring(0, 10) + "T12:00:01";
                } // AFTERNOON 13
                if (value.matches("(?i).*TAF")) {
                    lv = value.substring(0, 10) + "T12:00:01";
                    uv = value.substring(0, 10) + "T17:59:59";
                } // DEPEND ON WORK BREAKS 17-18
                if (value.matches("(?i).*TEV")) {
                    lv = value.substring(0, 10) + "T18:00:00";
                    uv = value.substring(0, 10) + "T20:59:59";
                } // AFTER WORK... GOING BACK HOME...
                if (value.matches("(?i).*TNI")) {
                    lv = value.substring(0, 10) + "T21:00:00";
                    uv = value.substring(0, 10) + "T04:59:59";
                } // MIDNIGHT
                if (value.matches("(?i).*TMI")) {
                    lv = value.substring(0, 10) + "T00:00:00";
                    uv = value.substring(0, 10) + "T00:00:01";
                }
            }

            if (value.matches("(?i)[0-9]{4}-W[0-9]{2}(-WE)?")) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date dateaux = df.parse(value);
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(dateaux);
                SimpleDateFormat df2 = new SimpleDateFormat(granul_days);
                lv = df2.format(cal.getTime()) + "T00:00:00";
                cal.add(GregorianCalendar.DAY_OF_MONTH, 6);
                uv = df2.format(cal.getTime()) + "T23:59:59";
                if (value.endsWith("WE")) {
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    lv = df2.format(cal.getTime()) + "T00:00:00";
                }
            }

            if (value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(T[0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)?)?)?)?)?)?)?")) {
                lv = value + lower_bound.substring(value.length());
                uv = value + upper_bound.substring(value.length());
                if (value.length() == 7) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date dateaux = df.parse(value);
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(dateaux);
                    cal.add(GregorianCalendar.MONTH, 1);
                    cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
                    uv = granul_days2.format(cal.getTime()) + upper_bound.substring(value.length() + 3);
                }
            }

            return lv + "|" + uv;
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
            get_interval_values();
        }
        return this.lower_value;
    }

    public String get_upper_value() {
        if (this.upper_value == null) {
            get_interval_values();
        }
        return this.upper_value;
    }
}
