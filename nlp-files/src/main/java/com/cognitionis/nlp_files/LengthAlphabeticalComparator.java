/*
 * Copyright 2013 hector.
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
package com.cognitionis.nlp_files;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 *
 * @author hector
 */
public class LengthAlphabeticalComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
      //String key1 = ((Entry<String,String[]>)o1).getKey();  
      String key1 = (String) o1;  
      String key2 = (String) o2;  
   
      if (key1.length() > key2.length())  
         return(-1);  
      else if (key1.length() < key2.length())  
         return(1);  
      else  //if (key1.equals(key2)) return 0;  //else 
          return key1.compareTo(key2);
    }


}
