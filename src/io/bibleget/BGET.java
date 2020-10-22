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

import java.util.HashMap;

/**
 *
 * @author John R. D'Orazio <priest@johnromanodorazio.com>
 */
public class BGET {
 
    public enum ALIGN {
        LEFT(1),
        CENTER(2),
        RIGHT(3),
        JUSTIFY(4);

        private int value;
        private static final HashMap<Integer,ALIGN> map = new HashMap<>();

        private ALIGN(int value) {
            this.value = value;
        }

        static {
            for (ALIGN align : ALIGN.values()) {
                map.put(align.value, align);
            }
        }

        public static ALIGN valueOf(int align) {
            return (ALIGN) map.get(align);
        }

        public int getValue() {
            return value;
        }
        
        public String getCSSValue(){
            return CSSRULE.ALIGN[value-1];
        }
    }

    public enum VALIGN {
        SUPERSCRIPT(1),
        SUBSCRIPT(2),
        NORMAL(3);

        private int value;
        private static final HashMap<Integer,VALIGN> map = new HashMap<>();

        private VALIGN(int value) {
            this.value = value;
        }

        static {
            for (VALIGN valign : VALIGN.values()) {
                map.put(valign.value, valign);
            }
        }

        public static VALIGN valueOf(int valign) {
            return (VALIGN) map.get(valign);
        }

        public int getValue() {
            return value;
        }
    }

    public enum WRAP {
        NONE(1),
        PARENTHESES(2),
        BRACKETS(3);

        private int value;
        private static final HashMap<Integer,WRAP> map = new HashMap<>();

        private WRAP(int value) {
            this.value = value;
        }

        static {
            for (WRAP wrap : WRAP.values()) {
                map.put(wrap.value, wrap);
            }
        }

        public static WRAP valueOf(int wrap) {
            return (WRAP) map.get(wrap);
        }

        public int getValue() {
            return value;
        }
    }

    public enum POS {
        TOP(1),
        BOTTOM(2),
        BOTTOMINLINE(3);

        private int value;
        private static final HashMap<Integer,POS> map = new HashMap<>();

        private POS(int value) {
            this.value = value;
        }

        static {
            for (POS pos : POS.values()) {
                map.put(pos.value, pos);
            }
        }

        public static POS valueOf(int pos) {
            return (POS) map.get(pos);
        }

        public int getValue() {
            return value;
        }
    }

    public enum FORMAT {
        USERLANG(1),
        BIBLELANG(2),
        USERLANGABBREV(3),
        BIBLELANGABBREV(4);

        private int value;
        private static final HashMap<Integer,FORMAT> map = new HashMap<>();

        private FORMAT(int value) {
            this.value = value;
        }

        static {
            for (FORMAT format : FORMAT.values()) {
                map.put(format.value, format);
            }
        }

        public static FORMAT valueOf(int format) {
            return (FORMAT) map.get(format);
        }

        public int getValue() {
            return value;
        }
    }

    public enum VISIBILITY {
        SHOW(1),
        HIDE(2);

        private int value;
        private static final HashMap<Integer,VISIBILITY> map = new HashMap<>();

        private VISIBILITY(int value) {
            this.value = value;
        }

        static {
            for (VISIBILITY visibility : VISIBILITY.values()) {
                map.put(visibility.value, visibility);
            }
        }

        public static VISIBILITY valueOf(int visibility) {
            return (VISIBILITY) map.get(visibility);
        }

        public int getValue() {
            return value;
        }
    }

    public enum PARAGRAPHTYPE {
        BIBLEVERSION(1),
        BOOKCHAPTER(2),
        VERSES(3),
        VERSENUMBER(4),
        VERSETEXT(5);

        private int value;
        private static final HashMap<Integer,PARAGRAPHTYPE> map = new HashMap<>();

        private PARAGRAPHTYPE(int value) {
            this.value = value;
        }

        static {
            for (PARAGRAPHTYPE paragraphtype : PARAGRAPHTYPE.values()) {
                map.put(paragraphtype.value, paragraphtype);
            }
        }

        public static PARAGRAPHTYPE valueOf(int paragraphtype) {
            return (PARAGRAPHTYPE) map.get(paragraphtype);
        }

        public int getValue() {
            return value;
        }
    }
    
    public enum TABLE {
        CREATED(1),
        INITIALIZED(2),
        DELETED(3),
        UPDATED(4),
        ERROR(5);
        
        private int value;
        private static final HashMap<Integer,TABLE> map = new HashMap<>();
        
        private TABLE(int value) {
            this.value = value;
        }
        
        static {
            for (TABLE table : TABLE.values()) {
                map.put(table.value, table);
            }
        }
        
        public static TABLE valueOf(int table){
            return (TABLE) map.get(table);
        }
        
        public int getValue(){
            return value;
        }
    }
   
}
