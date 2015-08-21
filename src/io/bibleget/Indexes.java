/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Lwangaman
 */
public class Indexes {

    private static BibleGetDB bibleGetDB;
    private static Indexes instance = null;
    private static Set<String> versionsabbrev = null;
    private static HashMap<String,VersionIDX> VersionIndexes;
    
    private Indexes() throws ClassNotFoundException {
        bibleGetDB = BibleGetDB.getInstance();
        if(versionsabbrev==null) {
            String versions = bibleGetDB.getMetaData("VERSIONS");
            JsonReader jsonReader = Json.createReader(new StringReader(versions));
            JsonObject jsbooks = jsonReader.readObject();
            versionsabbrev = jsbooks.keySet();        
        }
        int len = versionsabbrev.size();
        VersionIndexes = new HashMap<>(len,1);
        for(String s:versionsabbrev) {
            VersionIndexes.put(s, new VersionIDX(s));
        }
    }
    
    public static Indexes getInstance() throws ClassNotFoundException {
        if(instance==null) {
            instance = new Indexes();
        }
        return instance;
    }
    
    public boolean isValidVersion(String version) {
        version = version.toUpperCase();
        for(String s:versionsabbrev) {
            if(s.equals(version)){ return true; }
        }        
        return false;
    }
    
    public boolean isValidChapter(int chapter,int book,List<String> selectedVersions) {
        boolean flag = true;
        for(String version:selectedVersions) {
            int idx = VersionIndexes.get(version).book_num().indexOf(book);
            if(VersionIndexes.get(version).chapter_limit().get(idx) < chapter){ flag=false; }                        
        }                
        return flag;
    }
    
    public boolean isValidVerse(int verse,int chapter,int book,List<String> selectedVersions) {
        boolean flag = true;
        for(String version:selectedVersions) {
            int idx = VersionIndexes.get(version).book_num().indexOf(book);
            //System.out.println("corresponding book index in VersionIndexes for version "+version+", book "+Integer.toString(book)+" is "+Integer.toString(idx));
            if(VersionIndexes.get(version).verse_limit(idx).get(chapter-1) < verse){ flag = false; }
        }
        return flag;
    }
    
    public int[] getChapterLimit(int book,List<String> selectedVersions) {
        int[] retInt = new int[selectedVersions.size()];
        int count = 0;
        for(String version:selectedVersions) {
            int idx = VersionIndexes.get(version).book_num().indexOf(book);
            retInt[count++] = VersionIndexes.get(version).chapter_limit().get(idx);
        }        
//        System.out.print("value of chapter retInt = ");
//        System.out.println(Arrays.toString(retInt));
        return retInt;
    }

    public int[] getVerseLimit(int chapter,int book,List<String> selectedVersions) {
        int[] retInt = new int[selectedVersions.size()];
        int count = 0;
        for(String version:selectedVersions) {
            int idx = VersionIndexes.get(version).book_num().indexOf(book);
            retInt[count++] = VersionIndexes.get(version).verse_limit(idx).get(chapter-1);
        }        
//        System.out.print("value of verse retInt = ");
//        System.out.println(Arrays.toString(retInt));
        return retInt;
    }
    
    
    private class VersionIDX {
        
        private JsonObject versionIDX;
        
        public VersionIDX() {
            throw new UnsupportedOperationException("Must initialize with desired version.");
        }
        
        public VersionIDX(String version) {
            String versionIdxStr = bibleGetDB.getMetaData(version+"IDX");
            JsonReader jsonReader = Json.createReader(new StringReader(versionIdxStr));
            versionIDX = jsonReader.readObject();
        }
        
        public List<Integer> book_num() {
            JsonArray booknum = versionIDX.getJsonArray("book_num");
            int len = booknum.size();
            Integer[] bookNum = new Integer[len];
            for(int i=0;i<len;i++) {
                bookNum[i] = booknum.getInt(i);
            }
            return Arrays.asList(bookNum);
        }
        
        public List<Integer> chapter_limit() {
            JsonArray chapterlimit = versionIDX.getJsonArray("chapter_limit");
            int len = chapterlimit.size();
            Integer[] chapterLimit = new Integer[len];
            for(int i=0;i<len;i++){
                chapterLimit[i] = chapterlimit.getInt(i);
            }
            return Arrays.asList(chapterLimit);
        }
        
        public List<Integer> verse_limit(int book) {
            JsonArray verselimit_temp = versionIDX.getJsonArray("verse_limit");
            //System.out.println(verselimit_temp.toString());
            //System.out.println("array has "+Integer.toString(verselimit_temp.size())+" elements which correspond to books...");
            //System.out.println("requesting array element corresponding to book "+Integer.toString(book+1)+" at index "+Integer.toString(book));
            JsonArray verselimit = verselimit_temp.getJsonArray(book);
            //System.out.println(verselimit.toString());
            int len = verselimit.size();
            //System.out.println("verse_limit array has "+Integer.toString(len)+" elements which correspond to actual verse limits in the given book");
            Integer[] verseLimit = new Integer[len];
            for(int i=0;i<len;i++){
                verseLimit[i] = verselimit.getInt(i);
            }
            return Arrays.asList(verseLimit);            
        }
        
    }
    
}
