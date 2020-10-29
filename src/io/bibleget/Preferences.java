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

import java.awt.Color;
import java.sql.SQLException;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author John R. D'Orazio <priest@johnromanodorazio.com>
 */
public class Preferences {
    private static Preferences instance;
    private final BibleGetDB biblegetDB;    
    
    public Integer PARAGRAPHSTYLES_LINEHEIGHT;
    public Double PARAGRAPHSTYLES_LEFTINDENT;
    public Double PARAGRAPHSTYLES_RIGHTINDENT;
    public String PARAGRAPHSTYLES_FONTFAMILY;
    public BGET.ALIGN PARAGRAPHSTYLES_ALIGNMENT;
    public Boolean PARAGRAPHSTYLES_NOVERSIONFORMATTING;
    public BGET.MEASUREUNIT PARAGRAPHSTYLES_MEASUREUNIT;
    public Boolean BIBLEVERSIONSTYLES_BOLD;
    public Boolean BIBLEVERSIONSTYLES_ITALIC;
    public Boolean BIBLEVERSIONSTYLES_UNDERLINE;
    public Boolean BIBLEVERSIONSTYLES_STRIKETHROUGH;
    public Color BIBLEVERSIONSTYLES_TEXTCOLOR;
    public Color BIBLEVERSIONSTYLES_BGCOLOR;
    public Integer BIBLEVERSIONSTYLES_FONTSIZE;
    public BGET.VALIGN BIBLEVERSIONSTYLES_VALIGN;
    public Boolean BOOKCHAPTERSTYLES_BOLD;
    public Boolean BOOKCHAPTERSTYLES_ITALIC;
    public Boolean BOOKCHAPTERSTYLES_UNDERLINE;
    public Boolean BOOKCHAPTERSTYLES_STRIKETHROUGH;
    public Color BOOKCHAPTERSTYLES_TEXTCOLOR;
    public Color BOOKCHAPTERSTYLES_BGCOLOR;
    public Integer BOOKCHAPTERSTYLES_FONTSIZE;
    public BGET.VALIGN BOOKCHAPTERSTYLES_VALIGN;
    public Boolean VERSENUMBERSTYLES_BOLD;
    public Boolean VERSENUMBERSTYLES_ITALIC;
    public Boolean VERSENUMBERSTYLES_UNDERLINE;
    public Boolean VERSENUMBERSTYLES_STRIKETHROUGH;
    public Color VERSENUMBERSTYLES_TEXTCOLOR;
    public Color VERSENUMBERSTYLES_BGCOLOR;
    public Integer VERSENUMBERSTYLES_FONTSIZE;
    public BGET.VALIGN VERSENUMBERSTYLES_VALIGN;
    public Boolean VERSETEXTSTYLES_BOLD;
    public Boolean VERSETEXTSTYLES_ITALIC;
    public Boolean VERSETEXTSTYLES_UNDERLINE;
    public Boolean VERSETEXTSTYLES_STRIKETHROUGH;
    public Color VERSETEXTSTYLES_TEXTCOLOR;
    public Color VERSETEXTSTYLES_BGCOLOR;
    public Integer VERSETEXTSTYLES_FONTSIZE;
    public BGET.VALIGN VERSETEXTSTYLES_VALIGN;
    public BGET.VISIBILITY LAYOUTPREFS_BIBLEVERSION_SHOW;
    public BGET.ALIGN LAYOUTPREFS_BIBLEVERSION_ALIGNMENT;
    public BGET.POS LAYOUTPREFS_BIBLEVERSION_POSITION;
    public BGET.WRAP LAYOUTPREFS_BIBLEVERSION_WRAP;
    public BGET.ALIGN LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT;
    public BGET.POS LAYOUTPREFS_BOOKCHAPTER_POSITION;
    public BGET.WRAP LAYOUTPREFS_BOOKCHAPTER_WRAP;
    public BGET.FORMAT LAYOUTPREFS_BOOKCHAPTER_FORMAT;
    public Boolean LAYOUTPREFS_BOOKCHAPTER_FULLQUERY;
    public BGET.VISIBILITY LAYOUTPREFS_VERSENUMBER_SHOW;
    public String PREFERREDVERSIONS;
    
    private Preferences() throws ClassNotFoundException, SQLException, Exception{
        biblegetDB = BibleGetDB.getInstance();
        JsonObject myOptions = biblegetDB.getOptions();
        //System.out.println("PREFERENCES :: " + myOptions.toString());
        //System.out.println("PREFERENCES :: getting JsonValue of options rows");
        JsonValue myResults = myOptions.get("rows");
        //System.out.println("PREFERENCES :: " + myResults.toString());
        //System.out.println("PREFERENCES :: navigating values in json tree and setting global variables");
        navigateTree(myResults, null);        
    }
    
    public static Preferences getInstance() throws ClassNotFoundException, SQLException, Exception{
        if(instance == null){
            instance = new Preferences();
        }
        return instance;
    }
    
    private void navigateTree(JsonValue tree, String key) {
        if (key != null){
            //System.out.print("Key " + key + ": ");
        }
        switch(tree.getValueType()) {
            case OBJECT:
                System.out.println("OBJECT");
                JsonObject object = (JsonObject) tree;
                for (String name : object.keySet())
                   navigateTree(object.get(name), name);
                break;
            case ARRAY:
                System.out.println("ARRAY");
                JsonArray array = (JsonArray) tree;
                for (JsonValue val : array)
                   navigateTree(val, null);
                break;
            case STRING:
                JsonString st = (JsonString) tree;
                System.out.println("key " + key + " | STRING " + st.getString());
                getStringOption(key,st.getString());
                break;
            case NUMBER:
                JsonNumber num = (JsonNumber) tree;
                System.out.println("key " + key + " | NUMBER " + num.toString());
                
                if(num.isIntegral()){ //num.toString().contains(".") || num.toString().contains(",")
                    System.out.println(key + " is an integer");
                    getNumberOption(key,num.intValue());
                } else {
                    System.out.println(key + " is a double");
                    getNumberOption(key,num.doubleValue());
                }
                break;
            case TRUE:
                getBooleanOption(key,true);
                System.out.println("key " + key + " | BOOLEAN " + tree.getValueType().toString());
                break;
            case FALSE:
                getBooleanOption(key,false);
                System.out.println("key " + key + " | BOOLEAN " + tree.getValueType().toString());
                break;
            case NULL:
                System.out.println("key " + key + " | NULL " + tree.getValueType().toString());
                break;
        }
    }
    
    private void getStringOption(String key,String value){
        switch(key){
            case "PARAGRAPHSTYLES_FONTFAMILY": PARAGRAPHSTYLES_FONTFAMILY = value; break;
            case "BIBLEVERSIONSTYLES_TEXTCOLOR": BIBLEVERSIONSTYLES_TEXTCOLOR = Color.decode(value); break; //will need to decode string representation of hex value with Color.decode(BGET.BIBLEVERSIONSTYLES_TEXTCOLOR)
            case "BIBLEVERSIONSTYLES_BGCOLOR": BIBLEVERSIONSTYLES_BGCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "BOOKCHAPTERSTYLES_TEXTCOLOR": BOOKCHAPTERSTYLES_TEXTCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "BOOKCHAPTERSTYLES_BGCOLOR": BOOKCHAPTERSTYLES_BGCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "VERSENUMBERSTYLES_TEXTCOLOR": VERSENUMBERSTYLES_TEXTCOLOR = Color.decode(value); break;
            case "VERSENUMBERSTYLES_BGCOLOR": VERSENUMBERSTYLES_BGCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "VERSETEXTSTYLES_TEXTCOLOR": VERSETEXTSTYLES_TEXTCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "VERSETEXTSTYLES_BGCOLOR": VERSETEXTSTYLES_BGCOLOR = Color.decode(value); break;
            case "PREFERREDVERSIONS": PREFERREDVERSIONS = value; break;
        }
    }
    
    private void getNumberOption(String key,int value){
        switch(key){
            case "PARAGRAPHSTYLES_LINEHEIGHT": PARAGRAPHSTYLES_LINEHEIGHT = value; break; //think of it as percent
            case "PARAGRAPHSTYLES_ALIGNMENT": PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.valueOf(value); break;
            case "PARAGRAPHSTYLES_MEASUREUNIT": PARAGRAPHSTYLES_MEASUREUNIT = BGET.MEASUREUNIT.valueOf(value) ; break;
            case "BIBLEVERSIONSTYLES_FONTSIZE": BIBLEVERSIONSTYLES_FONTSIZE = value; break;       
            case "BIBLEVERSIONSTYLES_VALIGN": BIBLEVERSIONSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "BOOKCHAPTERSTYLES_FONTSIZE": BOOKCHAPTERSTYLES_FONTSIZE = value; break;
            case "BOOKCHAPTERSTYLES_VALIGN": BOOKCHAPTERSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "VERSENUMBERSTYLES_FONTSIZE": VERSENUMBERSTYLES_FONTSIZE = value; break;
            case "VERSENUMBERSTYLES_VALIGN": VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "VERSETEXTSTYLES_FONTSIZE": VERSETEXTSTYLES_FONTSIZE = value; break;
            case "VERSETEXTSTYLES_VALIGN": VERSETEXTSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_SHOW": LAYOUTPREFS_BIBLEVERSION_SHOW = BGET.VISIBILITY.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_ALIGNMENT": LAYOUTPREFS_BIBLEVERSION_ALIGNMENT = BGET.ALIGN.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_POSITION": LAYOUTPREFS_BIBLEVERSION_POSITION = BGET.POS.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_WRAP": LAYOUTPREFS_BIBLEVERSION_WRAP = BGET.WRAP.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT": LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT = BGET.ALIGN.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_POSITION": LAYOUTPREFS_BOOKCHAPTER_POSITION = BGET.POS.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_WRAP": LAYOUTPREFS_BOOKCHAPTER_WRAP = BGET.WRAP.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_FORMAT": LAYOUTPREFS_BOOKCHAPTER_FORMAT = BGET.FORMAT.valueOf(value); break;
            case "LAYOUTPREFS_VERSENUMBER_SHOW": LAYOUTPREFS_VERSENUMBER_SHOW = BGET.VISIBILITY.valueOf(value); break;
        }    
    }
    
    private void getNumberOption(String key,Double value){
        switch(key){
            case "PARAGRAPHSTYLES_LEFTINDENT": PARAGRAPHSTYLES_LEFTINDENT = value; break;
            case "PARAGRAPHSTYLES_RIGHTINDENT": PARAGRAPHSTYLES_RIGHTINDENT = value; break;        
        }
    }
    
    private void getBooleanOption(String key,boolean value){
        switch(key){
            case "PARAGRAPHSTYLES_NOVERSIONFORMATTING": PARAGRAPHSTYLES_NOVERSIONFORMATTING = value; break;
            case "BIBLEVERSIONSTYLES_BOLD": BIBLEVERSIONSTYLES_BOLD = value; break;
            case "BIBLEVERSIONSTYLES_ITALIC": BIBLEVERSIONSTYLES_ITALIC = value; break;
            case "BIBLEVERSIONSTYLES_UNDERLINE": BIBLEVERSIONSTYLES_UNDERLINE = value; break;
            case "BIBLEVERSIONSTYLES_STRIKETHROUGH": BIBLEVERSIONSTYLES_STRIKETHROUGH = value; break;
            case "BOOKCHAPTERSTYLES_BOLD": BOOKCHAPTERSTYLES_BOLD = value; break;
            case "BOOKCHAPTERSTYLES_ITALIC": BOOKCHAPTERSTYLES_ITALIC = value; break;
            case "BOOKCHAPTERSTYLES_UNDERLINE": BOOKCHAPTERSTYLES_UNDERLINE = value; break;
            case "BOOKCHAPTERSTYLES_STRIKETHROUGH": BOOKCHAPTERSTYLES_STRIKETHROUGH = value; break;
            case "VERSENUMBERSTYLES_BOLD": VERSENUMBERSTYLES_BOLD = value; break;
            case "VERSENUMBERSTYLES_ITALIC": VERSENUMBERSTYLES_ITALIC = value; break;
            case "VERSENUMBERSTYLES_UNDERLINE": VERSENUMBERSTYLES_UNDERLINE = value; break;
            case "VERSENUMBERSTYLES_STRIKETHROUGH": VERSENUMBERSTYLES_STRIKETHROUGH = value; break;
            case "VERSETEXTSTYLES_BOLD": VERSETEXTSTYLES_BOLD = value; break;
            case "VERSETEXTSTYLES_ITALIC": VERSETEXTSTYLES_ITALIC = value; break;
            case "VERSETEXTSTYLES_UNDERLINE": VERSETEXTSTYLES_UNDERLINE = value; break;
            case "VERSETEXTSTYLES_STRIKETHROUGH": VERSETEXTSTYLES_STRIKETHROUGH = value; break;
            case "LAYOUTPREFS_BOOKCHAPTER_FULLQUERY": LAYOUTPREFS_BOOKCHAPTER_FULLQUERY = value; break;
        }    
    }
    //other methods here...
}
