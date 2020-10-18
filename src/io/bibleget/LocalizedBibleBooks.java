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

    private static LocalizedBibleBooks instance = null;
    
    private LocalizedBibleBooks() throws ClassNotFoundException, SQLException{
        curLangDisplayName = BibleGetIO.getUILocale().getDisplayName(Locale.ENGLISH).toUpperCase();
        biblegetDB = BibleGetDB.getInstance();
        String langsSupported = biblegetDB.getMetaData("LANGUAGES");
        JsonReader jsonReader = Json.createReader(new StringReader(langsSupported));
        JsonObject langsObj = jsonReader.readObject();
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
        return new LocalizedBibleBook(BookAbbreviations.Item(idx), BookNames.Item(idx));
    }
    
}
