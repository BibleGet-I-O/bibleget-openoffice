/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

/**
 *
 * @author Lwangaman
 */
public class BibleVersion {
    private String abbrev;
    private String fullname;
    private String year;
    private String lang;

    public BibleVersion(String abbrev, String fullname, String year, String lang) {
        this.abbrev = abbrev;
        this.fullname = fullname;
        this.year = year;
        this.lang = lang;
    }        

    public String getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return this.getAbbrev() + " — " + this.getFullname() + " (" + this.getYear() + ")"; //To change body of generated methods, choose Tools | Templates.
    }                

}
