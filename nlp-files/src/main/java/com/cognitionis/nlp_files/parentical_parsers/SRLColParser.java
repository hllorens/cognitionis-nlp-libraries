package com.cognitionis.nlp_files.parentical_parsers;

import java.util.TreeSet;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class SRLColParser {

    private String verb;
    private String role;
    private TreeSet<String> roleconf; // csv
    private int size;
    private boolean close;

    public SRLColParser(){
        verb="-";
        role="*";
        size=0;
        close=false;
        roleconf=new TreeSet<String>();
    }

    public SRLColParser(String v, String r, int s) {
        verb = v;
        role = r;
        size = s;
        close = false;
        roleconf=new TreeSet<String>();
    }

    public String getVerb() {
        return verb;
    }

    public String getRole() {
        return role;
    }

    public String getRoleconf() {
        String sroleconf="";
        for(String tmprole : roleconf){
            if(sroleconf.length()>0){
                sroleconf+=",";
            }
            sroleconf+=tmprole;
        }
        if(sroleconf.equals("")){
            sroleconf="-";
        }

        return sroleconf;
    }

    public void setRole(String r) {
        role = r;
    }

    public int getSize() {
        if(role.equals("*")){
            return 0;
        }
        if(close){
            return size;
        }else{
            return size+1; // for heuristics
        }
    }

    public void parse(String s) {
        s = s.trim();
        boolean just_open = false;
        if (close) {
            close = false;
            role = "*";
            size = 0;
        }
        if (s.matches("\\*")) {
            if (!role.equals("*")) {
                size++;
            }
        } else {
            if (s.matches("\\(.*")) {
                role = s.substring(1, s.indexOf('*'));
                roleconf.add(role);
                size = 1;
                just_open = true;
            }
            if (s.matches(".*\\)")) {
                if (!just_open) {
                    size++;
                }
                close = true;
            }
        }
    }
}
