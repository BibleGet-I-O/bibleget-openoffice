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
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author John R. D'Orazio <priest@johnromanodorazio.com>
 */
public class LocalizedBibleBooks {
    private final BibleGetDB biblegetDB;
    private final HashMap<Integer,String> BookAbbreviations = new HashMap<>();
    private final HashMap<Integer,String> BookNames = new HashMap<>();
    private String curLangDisplayName;
    private Integer curLangIdx;
    private JsonReader jsonReader;

    private static LocalizedBibleBooks instance = null;
    
    private LocalizedBibleBooks() throws ClassNotFoundException, SQLException{
        curLangDisplayName = BibleGetIO.getUILocale().getDisplayName(Locale.ENGLISH).toUpperCase();
        biblegetDB = BibleGetDB.getInstance();
        String langsSupported = biblegetDB.getMetaData("LANGUAGES");
        jsonReader = Json.createReader(new StringReader(langsSupported));
        JsonArray langsJArr = jsonReader.readArray();
        ArrayList<String> langsArr = new ArrayList<>();
        JsonArray bibleBooksInCurLang;
        ArrayList<String> langsArrInCurLang = new ArrayList<>();
        if(langsJArr != null){
            for(int i=0;i<langsJArr.size();i++){
                langsArr.add(langsJArr.get(i).toString());
            }
            curLangIdx = langsArr.indexOf(curLangDisplayName);
            String bbBooks;
            String bookName;
            String bookAbbrev;
            JsonArray bibleBooksObj;
            for(int i=0;i<=72;i++){
                bbBooks = biblegetDB.getMetaData("BIBLEBOOKS"+i);
                jsonReader = Json.createReader(new StringReader(bbBooks));
                bibleBooksObj = jsonReader.readArray();
                bibleBooksInCurLang = bibleBooksObj.getJsonArray(curLangIdx);
                bookName = bibleBooksInCurLang.getString(0);
                bookAbbrev = bibleBooksInCurLang.getString(1);
                if(bookName.contains("|")){
                    bookName = bookName.split("|")[0].trim();
                }
                if(bookAbbrev.contains("|")){
                    bookAbbrev = bookAbbrev.split("|")[0].trim();
                }
                BookAbbreviations.put(i, bookAbbrev);
                BookNames.put(i, bookName);
            }
        }
        
    }
    
    
    public static LocalizedBibleBooks getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException
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
