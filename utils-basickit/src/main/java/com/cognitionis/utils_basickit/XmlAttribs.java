package com.cognitionis.utils_basickit;

import java.util.*;

/**
 * @author Hector Llorens
 * @since 2011
 */

public class XmlAttribs {


     public static HashMap<String, String> parseAttrs(String attrs) {
        // if separated by blanks or consists of only one argument with a quoted value use XML
        if(attrs.matches(".*=\"[^\"]*\"\\s+.*") || attrs.matches("[^=]*=\"[^\"]*\"")){
            return parseXMLattrs(attrs);
        }else{ // otherwise use ";" separator that handles both quoted and un quoted values
            return parseSemiColonAttrs(attrs);
        }
     }

     public static HashMap<String, String> parseXMLattrs(String attrs) {
        HashMap<String, String> parsed_attrs=null;
        try{
        attrs=attrs.trim().replaceAll("\\s+", " ");
        if(attrs.endsWith("\"")){attrs=attrs.substring(0, attrs.length()-1);}
        String[] attrs_arr = attrs.split("\" ");
        parsed_attrs = new HashMap<String, String>();
        for (int i = 0; i < attrs_arr.length; i++) {
            if (attrs_arr[i].matches("[^=]+=\"[^=]+")) {
                parsed_attrs.put(attrs_arr[i].substring(0, attrs_arr[i].indexOf("=\"")), attrs_arr[i].substring(attrs_arr[i].indexOf("=\"") + 2));
            }
        }
        } catch (Exception e) {
            System.err.println("Errors found (XmlAttribs):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return parsed_attrs;
    }

     public static HashMap<String, String> parseSemiColonAttrs(String attrs) {
        HashMap<String, String> parsed_attrs=null;
        try{
        String[] attrs_arr = attrs.trim().replaceAll("\\s+", " ").split(";");
        parsed_attrs = new HashMap<String, String>();
        for (int i = 0; i < attrs_arr.length; i++) {
            if (attrs_arr[i].matches("[^=]+=[^=]+")) {
                String name=attrs_arr[i].substring(0, attrs_arr[i].indexOf('='));
                String value=attrs_arr[i].substring(attrs_arr[i].indexOf('=') + 1);
                if (value.matches("\".*\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                parsed_attrs.put(name, value);
            }
        }
        } catch (Exception e) {
            System.err.println("Errors found (XmlAttribs):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return null;
        }
        return parsed_attrs;
    }




}
