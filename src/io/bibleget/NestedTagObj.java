/*
 * Copyright 2020 johnrdorazio.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author johnrdorazio
 */
public class NestedTagObj {
    private String remainingText;
    private final Pattern NABREfmt = Pattern.compile("(.*?)<((speaker|sm|i|pr|po)[f|l|s|i|3]{0,1}[f|l]{0,1})>(.*?)</\\2>",Pattern.UNICODE_CHARACTER_CLASS);
    private final Matcher NABREfmtMatch;
    public String Before = "";
    public String Contents = "";
    public String After = "";
    public String Tag = "";
    
    public NestedTagObj(String formattingTagContents){
        remainingText = formattingTagContents;
        NABREfmtMatch = NABREfmt.matcher(formattingTagContents);
        while(NABREfmtMatch.find()){
            if(NABREfmtMatch.group(2) != null && NABREfmtMatch.group(2).isEmpty() == false){
                Tag = NABREfmtMatch.group(2);
                if(NABREfmtMatch.group(1) != null && NABREfmtMatch.group(1).isEmpty() == false ){
                    Before = NABREfmtMatch.group(1);
                    remainingText = remainingText.replace(Before, "");
                }
                
                Contents = NABREfmtMatch.group(4);
                After = remainingText.replace("<" + Tag + ">" + Contents + "</" + Tag + ">", "");
            }
        }
    }
    
}
