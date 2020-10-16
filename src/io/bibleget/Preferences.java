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

/**
 *
 * @author John R. D'Orazio <priest@johnromanodorazio.com>
 */
public class Preferences {
    private static final Preferences USERPREFS = new Preferences();

    public Integer PARAGRAPHSTYLES_LINEHEIGHT;
    public Integer PARAGRAPHSTYLES_LEFTINDENT;
    public Integer PARAGRAPHSTYLES_RIGHTINDENT;
    public String PARAGRAPHSTYLES_FONTFAMILY;
    public BGET.ALIGN PARAGRAPHSTYLES_ALIGNMENT;
    public Boolean PARAGRAPHSTYLES_NOVERSIONFORMATTING;
    public Boolean PARAGRAPHSTYLES_INTERFACEINCM;
    public Boolean BIBLEVERSIONSTYLES_BOLD;
    public Boolean BIBLEVERSIONSTYLES_ITALIC;
    public Boolean BIBLEVERSIONSTYLES_UNDERLINE;
    public Boolean BIBLEVERSIONSTYLES_STRIKETHROUGH;
    public Color BIBLEVERSIONSTYLES_TEXTCOLOR;
    public Color BIBLEVERSIONSTYLES_BGCOLOR;
    public Integer BIBLEVERSIONSTYLES_FONTSIZE;
    public BGET.VALIGN BIBLEVERSIONSTYLES_VALIGN;
    public BGET.ALIGN BIBLEVERSIONSTYLES_ALIGNMENT;
    public Boolean BOOKCHAPTERSTYLES_BOLD;
    public Boolean BOOKCHAPTERSTYLES_ITALIC;
    public Boolean BOOKCHAPTERSTYLES_UNDERLINE;
    public Boolean BOOKCHAPTERSTYLES_STRIKETHROUGH;
    public Color BOOKCHAPTERSTYLES_TEXTCOLOR;
    public Color BOOKCHAPTERSTYLES_BGCOLOR;
    public Integer BOOKCHAPTERSTYLES_FONTSIZE;
    public BGET.VALIGN BOOKCHAPTERSTYLES_VALIGN;
    public BGET.ALIGN BOOKCHAPTERSTYLES_ALIGNMENT;
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
    
    private Preferences(){}
    
    public static Preferences getInstance(){
        return USERPREFS;
    }
    
    //other methods here...
}
