/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.bibleget;

import com.sun.star.awt.FontSlant;
import com.sun.star.awt.FontUnderline;
import com.sun.star.awt.FontWeight;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.style.CaseMap;
import com.sun.star.style.LineSpacing;
import com.sun.star.style.LineSpacingMode;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.text.ControlCharacter;
import com.sun.star.text.XText;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import java.awt.Color;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Lwangaman
 * 
 */
public class BibleGetDocInject {

    private final XController m_xController;
    private XModel m_xModel;
    private XTextDocument m_xTextDocument;    
    private XTextViewCursor m_xViewCursor;
    
    private final Preferences USERPREFS;
    private final LocalizedBibleBooks L10NBibleBooks;

    private final Double Inches2MM = 25.4;
    private final Double CM2MM = 10.0;
    private final Double Points2MM = 0.352778;
    private final Double Picas2MM = 4.2333;
    
    private Double leftMarginIn100thMM = 0.0;
    private Double rightMarginIn100thMM = 0.0;
    
    private final Double pofIndent;
    private final Double poiIndent;
    
   /**
     *
     * @param xController
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public BibleGetDocInject(XController xController) throws ClassNotFoundException, SQLException, Exception{
        USERPREFS = Preferences.getInstance();
        if(USERPREFS != null){
            System.out.println("USERPREFS is not null at least.");
        }
        this.L10NBibleBooks = LocalizedBibleBooks.getInstance();
        this.m_xController = xController;
        if (xController != null) {
            m_xModel = (XModel) xController.getModel();
            m_xTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,m_xModel);
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier) UnoRuntime.queryInterface(XTextViewCursorSupplier.class,m_xController);
            m_xViewCursor = xViewCursorSupplier.getViewCursor();
        }
            /**
            * Left / right indents are calculated according to current ruler units
            *      FROM http://www.openoffice.org/api/docs/common/ref/com/sun/star/style/ParagraphProperties.html :
            *          ParaLeftMargin and ParaRightMargin properties determine the left/right margin of the paragraph in 100th mm.
            *      In order to have a precise indent, convert the userpref value from the current unit to mm and then multiply by 100
            */
            switch(USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT){
                case CM:
                    leftMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_LEFTINDENT * CM2MM) * 100;
                    rightMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT * CM2MM) * 100;
                    break;
                case MM:
                    leftMarginIn100thMM = USERPREFS.PARAGRAPHSTYLES_LEFTINDENT * 100;
                    rightMarginIn100thMM = USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT * 100;
                    break;
                case INCH:
                    leftMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_LEFTINDENT * Inches2MM) * 100;
                    rightMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT * Inches2MM) * 100;
                    break;
                case POINT:
                    leftMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_LEFTINDENT * Points2MM) * 100;
                    rightMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT * Points2MM) * 100;
                    break;
                case PICA:
                    leftMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_LEFTINDENT * Picas2MM) * 100;
                    rightMarginIn100thMM = (USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT * Picas2MM) * 100;
                    break;
            }
            pofIndent = leftMarginIn100thMM + 500.0;
            poiIndent = leftMarginIn100thMM + 1000.0;
    }
    
    public void InsertTextAtCurrentCursor(String jsonString) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException
    {
        XText m_xText = m_xTextDocument.getText();
        
        //com.sun.star.view.XViewCursor xViewCursor;
        //Object viewData = m_xController.getViewData();
        //System.out.print("viewData = ");
        //System.out.println(viewData.toString());
        
        //System.out.print("current view cursor = ");
        //System.out.println(m_xViewCursor);
        
        XTextRange xTextRange = m_xViewCursor.getText().createTextCursorByRange(m_xViewCursor);
        //com.sun.star.text.XTextRange xTextRange = m_xText.createTextCursor();
        XPropertySet xPropertySet =
            (XPropertySet)UnoRuntime.queryInterface(
                XPropertySet.class, xTextRange);
        
//        com.sun.star.beans.Property xProperties[] = xPropertySet.getPropertySetInfo().getProperties();
//        for (Property xPropertie : xProperties) {
//            System.out.println(xPropertie.Name);
//        }
        //System.out.println( xPropertySet.getPropertyValue("CharFontName") );
        //System.out.println( xPropertySet.getPropertyValue("CharFontFamily") );
        //System.out.println( "Wanting to set CharFontName to value: "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY );

        ArrayList<String> BibleVersionStack = new ArrayList<>();
        ArrayList<String> BookChapterStack = new ArrayList<>();
        
        String currentFontName = (String)xPropertySet.getPropertyValue("CharFontName");
        float currentCharWeight = (float)xPropertySet.getPropertyValue("CharWeight");
        FontSlant currentCharPosture = (FontSlant)xPropertySet.getPropertyValue("CharPosture");
        Short currentCharUnderline = (Short)xPropertySet.getPropertyValue("CharUnderline");
        Short currentCharStrikeout = (Short)xPropertySet.getPropertyValue("CharStrikeout");
        boolean currentCharCrossedOut = (boolean)xPropertySet.getPropertyValue("CharCrossedOut");
        float currentCharHeight = (float)xPropertySet.getPropertyValue("CharHeight");
        int currentCharColor = (int)xPropertySet.getPropertyValue("CharColor");
        int currentCharBackColor = (int)xPropertySet.getPropertyValue("CharBackColor");
        Short currentCharEscapement = (Short)xPropertySet.getPropertyValue("CharEscapement");
        byte currentCharEscapementHeight = (byte)xPropertySet.getPropertyValue("CharEscapementHeight");
        LineSpacing currentParaLineSpacing = (LineSpacing)xPropertySet.getPropertyValue("ParaLineSpacing");
        int currentParaLeftMargin = (int)xPropertySet.getPropertyValue("ParaLeftMargin");
        int currentParaRightMargin = (int)xPropertySet.getPropertyValue("ParaRightMargin");
        Short currentParaAdjust = (Short)xPropertySet.getPropertyValue("ParaAdjust");
        
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject json = jsonReader.readObject();
        JsonArray arrayJson = json.getJsonArray("results");

        String prevVersion = "";
        boolean newVersion;
        String prevBook = "";
        boolean newBook;
        int prevChapter = -1;
        boolean newChapter;
        String prevVerse = "";
        boolean newVerse;
        
        String currentVersion;
        String currentBook;
        String currentBookAbbrev;
        int currentBookUnivIdx;
        int currentChapter;
        String currentVerse;
        String originalQuery;
        
        boolean firstVersion = true;
        boolean firstChapter = true;
        
        boolean firstVerse = false;
        boolean normalText = false;
        
        Iterator pIterator = arrayJson.iterator();
        while (pIterator.hasNext())
        {
            JsonObject currentJson = (JsonObject) pIterator.next();
            currentBook = currentJson.getString("book");
            currentBookAbbrev = currentJson.getString("bookabbrev");
            currentBookUnivIdx = Integer.decode(currentJson.getString("univbooknum"));
            currentChapter = currentJson.getInt("chapter");
            currentVerse = currentJson.getString("verse");
            currentVersion = currentJson.getString("version");
            originalQuery = currentJson.getString("originalquery");
            
            if(!currentVerse.equals(prevVerse)){
                newVerse = true;
                prevVerse = currentVerse;
            }
            else{
                newVerse = false;
            }
            
            if(currentChapter != prevChapter){
                newChapter = true;
                newVerse = true;
                prevChapter = currentChapter;
            }
            else{
                newChapter = false;
            }
            
            if(!currentBook.equals(prevBook)){
                newBook = true;
                newChapter = true;
                newVerse = true;
                prevBook = currentBook;
            }
            else{
                newBook = false;
            }
            
            if(!currentVersion.equals(prevVersion)){
                newVersion = true;
                newBook = true;
                newChapter = true;
                newVerse = true;
                prevVersion = currentVersion;
            }
            else{
                newVersion = false;
            }
                        
            if(newVersion){
                firstVerse = true;                
                //firstChapter = true;
                
                switch(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP){
                    case PARENTHESES:
                        currentVersion = "(" + currentVersion + ")";
                        break;
                    case BRACKETS:
                        currentVersion = "[" + currentVersion + "]";
                        break;
                }
                
                if(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.SHOW){
                    if(BookChapterStack.size() > 0){
                        switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION){
                            case BOTTOM:
                                insertParagraphBreak(m_xText,xTextRange);
                                setParagraphStyles(xPropertySet,BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                                break;
                            case TOP:
                                insertParagraphBreak(m_xText,xTextRange);
                                setParagraphStyles(xPropertySet,BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                                break;
                            case BOTTOMINLINE:
                                m_xText.insertString(xTextRange, " ", false);
                                break;
                        }
                        setTextStyles(xPropertySet,BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                        m_xText.insertString(xTextRange, BookChapterStack.get(0), false);
                        BookChapterStack.remove(0);
                    }
                    switch(USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION){
                        case BOTTOM:
                            BibleVersionStack.add(currentVersion);
                            if(BibleVersionStack.size() > 1){
                                insertParagraphBreak(m_xText,xTextRange);
                                setParagraphStyles(xPropertySet,BGET.PARAGRAPHTYPE.BIBLEVERSION);
                                setTextStyles(xPropertySet,BGET.PARAGRAPHTYPE.BIBLEVERSION);
                                m_xText.insertString(xTextRange, BibleVersionStack.get(0), false);
                                BibleVersionStack.remove(0);
                            }
                            break;
                        case TOP:
                            if(firstVersion == false){
                                insertParagraphBreak(m_xText,xTextRange);
                            } else {
                                insertParagraphBreak(m_xText,xTextRange); //TODO: perhaps only insert paragraph break if current paragraph is not empty?
                                firstVersion = false;
                            }
                            setParagraphStyles(xPropertySet,BGET.PARAGRAPHTYPE.BIBLEVERSION);
                            setTextStyles(xPropertySet,BGET.PARAGRAPHTYPE.BIBLEVERSION);
                            m_xText.insertString(xTextRange, currentVersion, false);
                            break;
                    }                           
                }
            }
                
            if(newBook || newChapter){
                //System.out.println(currentBook+" "+currentChapter);
                String bkChStr = "";
                switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT){
                    case BIBLELANG:
                        bkChStr = currentBook + " " + currentChapter;
                        break;
                    case BIBLELANGABBREV:
                        bkChStr = currentBookAbbrev + " " + currentChapter;
                        break;
                    case USERLANG:
                        bkChStr = L10NBibleBooks.GetBookByIndex(currentBookUnivIdx - 1).Fullname + " " + currentChapter;
                        break;
                    case USERLANGABBREV:
                        bkChStr = L10NBibleBooks.GetBookByIndex(currentBookUnivIdx - 1).Abbrev + " " + currentChapter;
                        break;
                }
                if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY){
                    String patternStr = "^[1-3]{0,1}((\\p{L}\\p{M}*)+)[1-9][0-9]{0,2}";
                    Pattern pattern = Pattern.compile(patternStr);
                    Matcher matcher = pattern.matcher(originalQuery);
                    if(matcher.find()){
                        bkChStr += matcher.replaceFirst("");
                    } else {
                        Pattern pattern2 = Pattern.compile("^[1-9][0-9]{0,2}");
                        Matcher matcher2 = pattern2.matcher(originalQuery);
                        bkChStr += matcher2.replaceFirst("");
                    }
                }
                switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP){
                    case PARENTHESES:
                        bkChStr = "(" + bkChStr + ")";
                        break;
                    case BRACKETS:
                        bkChStr = "[" + bkChStr + "]";
                        break;
                }
                
                firstVerse = true;
                
                switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION){
                    case BOTTOM:
                        BookChapterStack.add(bkChStr);
                        if(BookChapterStack.size() > 1){
                            insertParagraphBreak(m_xText, xTextRange);
                            setParagraphStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                            BookChapterStack.remove(0);
                        }
                        break;
                    case TOP:
                        if(firstChapter == false){
                            insertParagraphBreak(m_xText, xTextRange);
                        } else if(firstChapter && (USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.HIDE || USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION == BGET.POS.BOTTOM) ){
                            insertParagraphBreak(m_xText, xTextRange); //TODO: perhaps only insertParagraphBreak if current paragraph is not empty?
                            firstChapter = false;
                        } else {
                            insertParagraphBreak(m_xText, xTextRange);
                            firstChapter = false;
                        }
                        setParagraphStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                        setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                        m_xText.insertString(xTextRange, bkChStr, false);
                        break;
                    case BOTTOMINLINE:
                        BookChapterStack.add(bkChStr);
                        if(BookChapterStack.size() > 1){
                            m_xText.insertString(xTextRange, " ", false);
                            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
                            m_xText.insertString(xTextRange, BookChapterStack.get(0) , false);
                            BookChapterStack.remove(0);
                        }
                        break;
                }
            }
            
            if(newVerse){
                normalText = false;
                //System.out.print("\n"+currentVerse);
                if(firstVerse){
                    insertParagraphBreak(m_xText, xTextRange);
                    setParagraphStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSES);
                    firstVerse = false;
                }
                if(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW){
                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSENUMBER);
                    m_xText.insertString(xTextRange, " " + currentVerse, false);
                } else {
                    m_xText.insertString(xTextRange, " ", false);
                }
            }
            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
            String currentText = currentJson.getString("text").replace("\n","").replace("\r","");
            String remainingText = currentText;
            if(currentText.matches("(?su).*<[/]{0,1}(?:speaker|sm|i|pr|po)[f|l|s|i|3]{0,1}[f|l]{0,1}>.*")) //[/]{0,1}(?:sm|po)[f|l|s|i|3]{0,1}[f|l]{0,1}>
            {                
                int currentSpaceAfter = AnyConverter.toInt(xPropertySet.getPropertyValue("ParaBottomMargin"));
                Pattern pattern1 = Pattern.compile("(.*?)<((speaker|sm|i|pr|po)[f|l|s|i|3]{0,1}[f|l]{0,1})>(.*?)</\\2>",Pattern.UNICODE_CHARACTER_CLASS);
                Matcher matcher1 = pattern1.matcher(currentText);
                //int iteration = 0;
                
                while(matcher1.find()){
//                    System.out.print("Iteration ");
//                    System.out.println(++iteration);
//                    System.out.println("group1:"+matcher1.group(1));
//                    System.out.println("group2:"+matcher1.group(2));
//                    System.out.println("group3:"+matcher1.group(3));
//                    System.out.println("group4:"+matcher1.group(4));
                    if(matcher1.group(1) != null && matcher1.group(1).isEmpty() == false)
                    {
                        normalText = true;
                        m_xText.insertString(xTextRange, matcher1.group(1), false);
                        remainingText = remainingText.replaceFirst(matcher1.group(1), "");
                    }
                    if(matcher1.group(4) != null && matcher1.group(4).isEmpty() == false)
                    {
                        String matchedTag = matcher1.group(2);
                        String formattingTagContents = matcher1.group(4);
                        
                        //check for nested speaker tags!
                        boolean nestedTag = false;
                        NestedTagObj nestedTagObj = null;
                        //String speakerTagBefore = "";
                        //String speakerTagContents = "";
                        //String speakerTagAfter = "";
                        
                        if(formattingTagContents.matches("(?su).*<[/]{0,1}(?:speaker|sm|i|pr|po)[f|l|s|i]{0,1}[f|l]{0,1}>.*")){
                            nestedTag = true;
                            nestedTagObj = new NestedTagObj(formattingTagContents);
                        }
                        
                        if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING){ formattingTagContents = " "+formattingTagContents+" "; }
                        
                        switch (matchedTag) {
                            case "pof":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", pofIndent.intValue());
                                    xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                normalText = false;
                                break;
                            case "pos":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", pofIndent.intValue());
                                    xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                normalText = false;
                                break;
                            case "poif":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", poiIndent.intValue());
                                    xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                normalText = false;
                                break;
                            case "po":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", pofIndent.intValue());
                                    xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                normalText = false;
                                break;
                            case "poi":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", poiIndent.intValue());
                                    xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                }
                                normalText = false;
                                break;
                            case "pol":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", pofIndent.intValue());
                                    xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", pofIndent.intValue());
                                }
                                normalText = false;
                                break;
                            case "poil":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", poiIndent.intValue());
                                }
                                if(nestedTag){
                                    insertNestedTag(nestedTagObj, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue());
                                }
                                normalText = false;
                                break;
                            case "sm":
                                String smallCaps = matcher1.group(4).toLowerCase();
                                //System.out.println("SMALLCAPSIZE THIS TEXT: "+smallCaps);
                                xPropertySet.setPropertyValue("CharCaseMap", CaseMap.SMALLCAPS);
                                m_xText.insertString(xTextRange, smallCaps, false);
                                xPropertySet.setPropertyValue("CharCaseMap", CaseMap.NONE);
                                break;
                            case "speaker":
//                                System.out.println("We have found a speaker tag");
                                xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                                //xPropertySet.setPropertyValue("CharBackTransparent", false);
                                xPropertySet.setPropertyValue("CharBackColor", Color.LIGHT_GRAY.getRGB() & ~0xFF000000);
                                m_xText.insertString(xTextRange, " " + matcher1.group(4) + " ", false);
                                if(USERPREFS.VERSETEXTSTYLES_BOLD==false){
                                    xPropertySet.setPropertyValue("CharWeight", FontWeight.NORMAL);                                
                                }
                                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSETEXTSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                        }
                        remainingText = remainingText.replaceFirst("<"+matcher1.group(2)+">"+matcher1.group(4)+"</"+matcher1.group(2)+">", "");
                    }
                }
                xPropertySet.setPropertyValue("ParaBottomMargin", currentSpaceAfter);
//                System.out.println("We have a match for special formatting: "+currentText);
//                System.out.println("And after elaborating our matches, this is what we have left: "+remainingText);
//                System.out.println();
                if(remainingText.isEmpty() == false)
                {
                    m_xText.insertString(xTextRange, remainingText, false);
                }
                /*
                Pattern pattern2 = Pattern.compile("([.^>]+)$",Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL);
                Matcher matcher2 = pattern2.matcher(currentText);
                //String lastPiece = currentText.
                if(matcher2.find())
                {
                    if(matcher2.group(1) != null && !"".equals(matcher2.group(1)))
                    { 
                        m_xText.insertString(xTextRange, matcher2.group(1), false);
                    }
                }
                */
            }
            else
            {
                normalText = true;
                //System.out.println("No match for special case formatting here: "+currentText);
                // set properties of text change based on user preferences
                //setVerseTextStyles(xPropertySet);
                m_xText.insertString(xTextRange, currentText, false);            
            }
            
            if(firstVerse){ firstVerse = false; }
            
        }
        
        if(BookChapterStack.size() > 0){
            if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.BOTTOM){
                insertParagraphBreak(m_xText, xTextRange);
                setParagraphStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
            } else {
                m_xText.insertString(xTextRange, " ", false);
            }
            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.BOOKCHAPTER);
            m_xText.insertString(xTextRange, BookChapterStack.get(0), false);
            BookChapterStack.remove(0);
        }
        
        if(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.SHOW && BibleVersionStack.size() > 0){
            insertParagraphBreak(m_xText, xTextRange);
            setParagraphStyles(xPropertySet, BGET.PARAGRAPHTYPE.BIBLEVERSION);
            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.BIBLEVERSION);
            m_xText.insertString(xTextRange, BibleVersionStack.get(0), false);
            BibleVersionStack.remove(0);
        }
        
        insertParagraphBreak(m_xText, xTextRange);
        //restore original text styles
        xPropertySet.setPropertyValue("CharFontName",currentFontName);
        xPropertySet.setPropertyValue("CharWeight",currentCharWeight);
        xPropertySet.setPropertyValue("CharPosture",currentCharPosture);
        xPropertySet.setPropertyValue("CharUnderline",currentCharUnderline);
        xPropertySet.setPropertyValue("CharStrikeout",currentCharStrikeout);
        xPropertySet.setPropertyValue("CharCrossedOut",currentCharCrossedOut);
        xPropertySet.setPropertyValue("CharHeight",currentCharHeight);
        xPropertySet.setPropertyValue("CharColor",currentCharColor);
        xPropertySet.setPropertyValue("CharBackColor",currentCharBackColor);
        xPropertySet.setPropertyValue("CharEscapement",currentCharEscapement);
        xPropertySet.setPropertyValue("CharEscapementHeight",currentCharEscapementHeight);
        xPropertySet.setPropertyValue("ParaLineSpacing", currentParaLineSpacing);
        xPropertySet.setPropertyValue("ParaLeftMargin", currentParaLeftMargin);
        xPropertySet.setPropertyValue("ParaRightMargin", currentParaRightMargin);
        xPropertySet.setPropertyValue("ParaAdjust", currentParaAdjust);
        
    }
    
    private void insertNestedTag(NestedTagObj nestedTagObj, XText m_xText, XTextRange xTextRange, XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException
    {
        if(nestedTagObj.Before.isEmpty() == false){
            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
            m_xText.insertString(xTextRange, nestedTagObj.Before, false);
        }
        
        switch(nestedTagObj.Tag){
            case "speaker":
                xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                xPropertySet.setPropertyValue("CharPosture", FontSlant.NONE);
                xPropertySet.setPropertyValue("CharUnderline", FontUnderline.NONE);
                xPropertySet.setPropertyValue("CharHeight", USERPREFS.VERSETEXTSTYLES_FONTSIZE);
                xPropertySet.setPropertyValue("CharColor", Color.BLACK.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", Color.LIGHT_GRAY.getRGB() & ~0xFF000000);
                xPropertySet.setPropertyValue("CharEscapement", (short)0);
                xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                m_xText.insertString(xTextRange, " " + nestedTagObj.Contents + " ", false);
                break;
            case "sm":
                xPropertySet.setPropertyValue("CharCaseMap", CaseMap.SMALLCAPS);
                m_xText.insertString(xTextRange, nestedTagObj.Contents.toLowerCase(), false);
                xPropertySet.setPropertyValue("CharCaseMap", CaseMap.NONE);
                break;
            case "poi":
                insertParagraphBreak(m_xText, xTextRange);
                xPropertySet.setPropertyValue("ParaLeftMargin", poiIndent.intValue());
                xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                m_xText.insertString(xTextRange, nestedTagObj.Contents, false);
                insertParagraphBreak(m_xText, xTextRange);
                setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                break;
            case "po":
                insertParagraphBreak(m_xText, xTextRange);
                xPropertySet.setPropertyValue("ParaLeftMargin", pofIndent.intValue());
                xPropertySet.setPropertyValue("ParaBottomMargin", 0);
                setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                m_xText.insertString(xTextRange, nestedTagObj.Contents, false);
                insertParagraphBreak(m_xText, xTextRange);
                setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
                break;
        }
        
        if(nestedTagObj.After.isEmpty() == false){
            setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
            m_xText.insertString(xTextRange, nestedTagObj.After, false);
        }
        
        setTextStyles(xPropertySet, BGET.PARAGRAPHTYPE.VERSETEXT);
        
    }
    
    public double pointsToMillimeters(int points)
    {
        double ratio = 0.352777778;
        double converted = points * ratio;
        double rounded = Math.round(converted*10)/10;
        return rounded;
    }
    
    private void insertParagraphBreak(XText m_xText,XTextRange xTextRange)
    {
        try {
            m_xText.insertControlCharacter(xTextRange, ControlCharacter.PARAGRAPH_BREAK, false);
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
        }            
    }
    
    private void setParagraphStyles(XPropertySet xPropertySet, BGET.PARAGRAPHTYPE parType){
        try {
            xPropertySet.setPropertyValue("CharFontName", USERPREFS.PARAGRAPHSTYLES_FONTFAMILY );
            xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue());
            xPropertySet.setPropertyValue("ParaRightMargin", rightMarginIn100thMM.intValue());
            //set paragraph line spacing      
            LineSpacing lineSpacing = new LineSpacing();
            lineSpacing.Mode = LineSpacingMode.PROP;
            lineSpacing.Height = USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT.shortValue();
            xPropertySet.setPropertyValue("ParaLineSpacing",lineSpacing);
            
            BGET.ALIGN myAlignment = BGET.ALIGN.JUSTIFY;
            switch(parType){
                case BIBLEVERSION:
                    myAlignment = USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT;
                    break;
                case BOOKCHAPTER:
                    myAlignment = USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT;
                    break;
                case VERSES:
                    myAlignment = USERPREFS.PARAGRAPHSTYLES_ALIGNMENT;
                    break;
            }
            if(parType == BGET.PARAGRAPHTYPE.BOOKCHAPTER && USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.BOTTOMINLINE){
                //do nothing
            } else {
                switch(myAlignment){
                    case LEFT:
                        xPropertySet.setPropertyValue("ParaAdjust",ParagraphAdjust.LEFT);
                        break;
                    case CENTER:
                        xPropertySet.setPropertyValue("ParaAdjust",ParagraphAdjust.CENTER);
                        break;
                    case RIGHT:
                        xPropertySet.setPropertyValue("ParaAdjust",ParagraphAdjust.RIGHT);
                        break;
                    case JUSTIFY:
                        xPropertySet.setPropertyValue("ParaAdjust",ParagraphAdjust.BLOCK);
                        break;
                }
            }
            
        } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | com.sun.star.lang.WrappedTargetException ex){
            Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void setTextStyles(XPropertySet xPropertySet, BGET.PARAGRAPHTYPE parType) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException
    {
        switch(parType){
            case BIBLEVERSION:
                xPropertySet.setPropertyValue("CharHeight", USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE.floatValue());
                if(USERPREFS.BIBLEVERSIONSTYLES_BOLD){
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                } else {
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                }
                if(USERPREFS.BIBLEVERSIONSTYLES_ITALIC){
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.NONE);
                }
                if(USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE){
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                xPropertySet.setPropertyValue("CharEscapement", (short)0);
                xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                break;
            case BOOKCHAPTER:
                xPropertySet.setPropertyValue("CharHeight", USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE.floatValue());
                if(USERPREFS.BOOKCHAPTERSTYLES_BOLD){
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                } else {
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                }
                if(USERPREFS.BOOKCHAPTERSTYLES_ITALIC){
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.NONE);
                }
                if(USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE){
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                xPropertySet.setPropertyValue("CharEscapement", (short)0);
                xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                break;
            case VERSENUMBER:
                xPropertySet.setPropertyValue("CharHeight", USERPREFS.VERSENUMBERSTYLES_FONTSIZE.floatValue());
                if(USERPREFS.VERSENUMBERSTYLES_BOLD){
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                } else {
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                }
                if(USERPREFS.VERSENUMBERSTYLES_ITALIC){
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.NONE);
                }
                if(USERPREFS.VERSENUMBERSTYLES_UNDERLINE){
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSENUMBERSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                switch(USERPREFS.VERSENUMBERSTYLES_VALIGN){
                    case SUPERSCRIPT:
                        xPropertySet.setPropertyValue("CharEscapement", (short)101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        break;
                    case SUBSCRIPT:
                        xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        break;
                    case NORMAL:
                        xPropertySet.setPropertyValue("CharEscapement", (short)0);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                        break;
                }
                break;
            case VERSETEXT:
                xPropertySet.setPropertyValue("CharHeight", USERPREFS.VERSETEXTSTYLES_FONTSIZE.floatValue());
                if(USERPREFS.VERSETEXTSTYLES_BOLD){
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                } else {
                    xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
                }
                if(USERPREFS.VERSETEXTSTYLES_ITALIC){
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", FontSlant.NONE);
                }
                if(USERPREFS.VERSETEXTSTYLES_UNDERLINE){
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", USERPREFS.VERSETEXTSTYLES_TEXTCOLOR.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSETEXTSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                xPropertySet.setPropertyValue("CharEscapement", (short)0);
                xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                break;
        }
        
        
    }
    
}
