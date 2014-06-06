package com.cognitionis.timeml_basickit;

import com.cognitionis.utils_basickit.DateUtils;
import java.text.*;
import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public final class Timex extends Element implements Cloneable {


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
            if (DateUtils.is_ISO8601_date(value)) {
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
                String[] interval_values_arr = DateUtils.get_interval_value(this.value).split("\\|");
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

    


  
    
    
}
