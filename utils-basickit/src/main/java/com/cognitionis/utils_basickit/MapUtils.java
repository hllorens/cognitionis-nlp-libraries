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

import java.util.*;

/**
 *
 * @author hector
 */
public class MapUtils {
    /**
     * Sort a Map by value and save it as a linkedHashMap to ensure the order is
     * kept. You need to provide the map and the comparator so that you can use
     * this function to get different types of orderings by value (ascending,
     * descending, ...)
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @param comparator
     * @return 
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, Comparator comparator) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, comparator);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
