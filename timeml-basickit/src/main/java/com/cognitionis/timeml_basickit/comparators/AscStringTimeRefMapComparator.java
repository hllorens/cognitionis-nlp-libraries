package com.cognitionis.timeml_basickit.comparators;

import com.cognitionis.timeml_basickit.TimeReference;
import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */

public class AscStringTimeRefMapComparator implements Comparator {
        private HashMap<String,TimeReference> _data = null;

        public AscStringTimeRefMapComparator(HashMap<String,TimeReference> data) {
            _data = data;
        }
        
        public int compare(Object o1, Object o2) {
            int diff=((TimeReference) _data.get((String) o1)).get_timex().get_date().compareTo(((TimeReference) _data.get((String) o2)).get_timex().get_date());
            /*if(diff==0){
                diff=1; // if two elements are equal just leave the regular order
            }*/
            return diff;
        }

}
