package com.cognitionis.timeml_basickit.comparators;

import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */
public class AscINT_lid_Comparator implements Comparator {
    

  public int compare(Object a, Object b) {
    if( Integer.parseInt(((String) a).substring(1)) > Integer.parseInt(((String) b).substring(1)) ) {
      return 1;
    } else if(Integer.parseInt(((String) a).substring(1)) == Integer.parseInt(((String)b).substring(1))) {
      return 0;
    } else {
      return -1;
    }
  }



}
