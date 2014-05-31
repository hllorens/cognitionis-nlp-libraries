package com.cognitionis.timeml_basickit.comparators;

import com.cognitionis.timeml_basickit.Timex;
import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */

public class AscStringTimexMapComparator implements Comparator {

    private HashMap<String, Timex> _data = null;

    public AscStringTimexMapComparator(HashMap<String, Timex> data) {
        _data = data;
    }

    public int compare(Object o1, Object o2) {
        int diff=1;
        //if(_data!=null && _data.get((String) o1)!=null && _data.get((String) o2)!=null && _data.get((String) o1).get_date()!=null && _data.get((String) o2).get_date()!=null){
        diff = ((Timex) _data.get((String) o1)).get_date().compareTo(((Timex) _data.get((String) o2)).get_date());
        if (diff == 0) {
            int o1l = ((Timex) _data.get((String) o1)).get_value().length();
            int o2l = ((Timex) _data.get((String) o2)).get_value().length();
            // for seasons and TODs keep it as it is
            if (((Timex) _data.get((String) o1)).get_value().contains("W")) {
                if (o1l < 8) {
                    o1l = 8;
                }
                if(o1l>8){
                    o1l = 9;
                }
            }
            if (((Timex) _data.get((String) o2)).get_value().contains("W")) {
                if (o2l < 8) {
                    o2l = 8;
                }
                if(o2l>8){
                    o2l = 9;
                }
            }
            diff = o1l - o2l;
        }
        if (diff == 0) {
            diff = 1; // if two elements are equal just leave the regular order
        }
        //}
        return diff;
    }
}
