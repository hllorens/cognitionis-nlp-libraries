package com.cognitionis.utils_basickit;

import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class DescStringIntMapEntryListComparator implements Comparator<Map.Entry<String, Integer>> {
        
        @Override
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            int diff=o2.getValue() - o1.getValue();
            if(diff==0){
                diff=o2.getKey().compareTo(o1.getKey());
            }
            return diff;
        }

}
