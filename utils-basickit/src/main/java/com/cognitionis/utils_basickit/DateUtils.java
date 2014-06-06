/*
 * Copyright 2014 hector.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognitionis.utils_basickit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author hector
 */
public class DateUtils {
    public static final String granul_years = "yyyy";
    public static final String granul_months = "yyyy-MM";
    public static final String granul_days = "yyyy-MM-dd";
    public static final String granul_time = "yyyy-MM-dd'T'HH:mm";
    public static final String granul_seconds = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateFormat granul_days2 = new SimpleDateFormat("yyyy-MM-dd");
    public static final String upper_bound = "9999-12-31T23:59:59";
    public static final String lower_bound = "0000-01-01T00:00:00";
    
    /**
     * Get the interval values in the format lower|upper given any iso date.
     * If it is not possible lower and upper will be equal to the given 
     * date value
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
            
            if (value.matches("^[0-9]{4}.*")) {
                year = value.substring(0, 4);
            }
            if (value.matches("(?i)[0-9]{4}-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2))")) {
                if (value.matches("(?i).*-(WI|Q1|H1|T1)")) {
                    String february_last_day = "28";
                    if (DateUtils.isLeapYear(Integer.parseInt(year))) {
                        february_last_day = "29";
                    }
                    return  year + "-01-01T00:00:00|" + year + "-02-" + february_last_day + "T23:59:59";
                }
                if (value.matches("(?i).*-(SP|Q2|T2)")) {
                    return  year + "-03-01T00:00:00|" + year + "-05-31T23:59:59";
                }
                if (value.matches("(?i).*-(SU|Q3|H2|T3)")) {
                    return year + "-06-01T00:00:00|" + year + "-08-31T23:59:59";
                }
                if (value.matches("(?i).*-(AU|FA|Q4|T4)")) {
                    return year + "-09-01T00:00:00|" + year + "-12-31T23:59:59";
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
            if (DateUtils.is_clean_ISO8601_date_part(value)) {
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
    
    /**
     * True if the string is a valid clean ISO8601 (yyyy-MM-dd'T'HH:mm:ss)
     *
     * @param value
     * @return
     */
    public static boolean is_clean_ISO8601_date(String value) {
        if (value.matches("[0-9][0-9][0-9][0-9]-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9]")) {
            return true;
        }
        return false;
    }

    /**
     * True if the string is a valid ISO8601
     *
     * @param value
     * @return
     */
    public static boolean is_ISO8601_date(String value) {
        if (value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(  T([0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)? | (MO|AF|EV|NI|MI|NO) )  )?|-W[0-5][0-9](-WE)?)|-(WI|SP|SU|AU|FA|[QT](1|2|3|4)|H(1|2)))?)?)?)?")) {
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
    public static boolean is_clean_ISO8601_date_part(String value) {
        if (value.matches("[0-9]([0-9]([0-9]([0-9](-[0-9]{2}(-[0-9]{2}(T[0-2][0-9](:[0-5][0-9](:[0-5][0-9])?)?)?)?)?)?)?)?")) {
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
    public static boolean isLeapYear(final int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }
}
