package com.cognitionis.utils_basickit;

import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class AscStringIntMapComparator implements Comparator {
        private HashMap<String,Integer> _data = null;

        public AscStringIntMapComparator(HashMap<String,Integer> data) {
            _data = data;
        }
        
        public int compare(Object o1, Object o2) {
            int diff=((Integer) _data.get((String) o1)) - ((Integer) _data.get((String) o2));
            if(diff==0){
                diff=((String) o2).compareTo((String) o1);
            }
            return diff;
        }

}
