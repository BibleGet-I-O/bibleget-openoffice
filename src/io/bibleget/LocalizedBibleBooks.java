/*
 * Copyright 2020 John R. D'Orazio <priest@johnromanodorazio.com>.
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
package io.bibleget;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;

/**
 *
 * @author John R. D'Orazio <priest@johnromanodorazio.com>
 */
public class LocalizedBibleBooks {
    private final DBHelper biblegetDB;
    private final HashMap<Integer,String> BookAbbreviations = new HashMap<>();
    private final HashMap<Integer,String> BookNames = new HashMap<>();
    private final String curLangDisplayName;
    private Integer curLangIdx;
    private JsonReader jsonReader;
    private ArrayList<String> langsArr = new ArrayList<>();
    private JsonArray bibleBooksInCurLang;
    private String bbBooks;
    private String bookName;
    private String bookAbbrev;
    private JsonArray bibleBooksArr;
    private String langsSupported;
    private JsonArray langsJArr;

    private static LocalizedBibleBooks instance = null;
    
    private LocalizedBibleBooks() throws ClassNotFoundException, SQLException, Exception{
        curLangDisplayName = BibleGetIO.getUILocale().getDisplayName(Locale.ENGLISH).toUpperCase().trim();
        biblegetDB = DBHelper.getInstance();
        langsSupported = biblegetDB.getMetaData("LANGUAGES");
        jsonReader = Json.createReader(new StringReader(langsSupported));
        langsJArr = jsonReader.readArray();
        if(langsJArr != null){
            for(int i=0;i<langsJArr.size();i++){
                langsArr.add(langsJArr.getString(i));
            }
            if(langsArr.contains(curLangDisplayName)){
                curLangIdx = langsArr.indexOf(curLangDisplayName);
                System.out.println("langsArr at least does seem to have the value " + curLangDisplayName);
            } else {
                System.out.println("For some strange reason, it seems that langsArr does not have the value " + curLangDisplayName + "?");
                System.out.println("langsArr has size " + langsArr.size());
                System.out.println("langsArr = " + langsArr);
                if(langsArr.contains("\"" + curLangDisplayName + "\"")){
                    System.out.println("Oh so that's it, language names include quotation marks, arghhh");
                } else {
                    System.out.println("It's not the parentheses, then what is it?");
                }
                curLangIdx = 0;
            }
            for(int i=0;i<=72;i++){
                bbBooks = biblegetDB.getMetaData("BIBLEBOOKS"+i);
                //System.out.println("BIBLEBOOKS"+i+" = "+bbBooks);
                try{
                    jsonReader = Json.createReader(new StringReader(bbBooks));
                    bibleBooksArr = jsonReader.readArray();
                    bibleBooksInCurLang = bibleBooksArr.getJsonArray(curLangIdx);
                    bookName = bibleBooksInCurLang.getString(0);
                    bookAbbrev = bibleBooksInCurLang.getString(1);
                    //System.out.println("bookAbbrev = " + bookAbbrev + ", bookName = " + bookName);
                    if(bookAbbrev.contains("|")){
                        String[] bookAbbrevArr = bookAbbrev.split("\\|");
                        bookAbbrev = bookAbbrevArr[0].trim();
                    }
                    if(bookName.contains("|")){
                        String[] bookNameArr = bookName.split("\\|");
                        bookName = bookNameArr[0].trim();
                    }
                    //System.out.println("book at index " + i + " = " + bookAbbrev + " :: " + bookName);
                    BookAbbreviations.put(i, bookAbbrev);
                    BookNames.put(i, bookName);
                } catch(JsonParsingException ex) {
                    System.out.println("LOCALIZEDBIBLEBOOKS.JAVA | Error while parsing JSON: " + ex.getMessage());
                } catch(ArrayIndexOutOfBoundsException ex){
                    System.out.println("LOCALIZEDBIBLEBOOKS.JAVA | Which array is giving out of bounds exeption?");
                    if(bibleBooksArr == null){
                        System.out.println("bibleBooksArr is null!");
                    } else {
                        System.out.println("bibleBooksArr at least is not null, size is " + bibleBooksArr.size());
                        System.out.println(bibleBooksArr);
                    }
                    
                    if(bibleBooksInCurLang == null){
                        System.out.println("bibleBooksInCurLang is null!");
                        System.out.println("curLangDisplayName = " + curLangDisplayName);
                        System.out.println("langsSupported = " + langsSupported);
                        System.out.println("langsJArr has size " + langsJArr.size());
                        System.out.println("langsArr has size " + langsArr.size());
                        System.out.println("langsArr = " + langsArr);
                        System.out.println("curLangIdx = " + curLangIdx);
                    } else {
                        System.out.println("bibleBooksInCurLang at least is not null, size is " + bibleBooksInCurLang.size());
                        System.out.println(bibleBooksInCurLang);
                    }
                    
                } catch(Exception ex){
                    System.out.println("LOCALIZEDBIBLEBOOKS.JAVA | Error: " + ex.getMessage());
                    System.out.println(ex);
                }
            }
        }
        
    }
    
    
    public static LocalizedBibleBooks getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception
    {
        if(instance == null)
        {
            instance = new LocalizedBibleBooks();
        }
        return instance;
    }

    public LocalizedBibleBook GetBookByIndex(Integer idx){
        return new LocalizedBibleBook(BookAbbreviations.get(idx), BookNames.get(idx));
    }
    
}
