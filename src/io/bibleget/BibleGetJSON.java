/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.bibleget;

import com.sun.star.awt.FontWeight;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.text.XText;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import java.awt.Color;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author Lwangaman
 */
public class BibleGetJSON {

    private final XController m_xController;
    private XModel m_xModel;
    private XTextDocument m_xTextDocument;
    
    private String paragraphAlignment;
    private short paragraphLineSpacing; 
    private String paragraphFontFamily;
    private int paragraphLeftIndent;
    
    private Color textColorBookChapter;
    private Color bgColorBookChapter;
    private boolean boldBookChapter;
    private boolean italicsBookChapter;
    private boolean underscoreBookChapter;
    private float fontSizeBookChapter;
    private String vAlignBookChapter;

    private Color textColorVerseNumber;
    private Color bgColorVerseNumber;
    private boolean boldVerseNumber;
    private boolean italicsVerseNumber;
    private boolean underscoreVerseNumber;
    private float fontSizeVerseNumber;
    private String vAlignVerseNumber;

    private Color textColorVerseText;
    private Color bgColorVerseText;
    private boolean boldVerseText;
    private boolean italicsVerseText;
    private boolean underscoreVerseText;
    private float fontSizeVerseText;
    private String vAlignVerseText;
    
    private boolean noVersionFormatting;
    
    private XTextViewCursor m_xViewCursor;
    
    private final BibleGetDB biblegetDB;
    
   /**
     *
     * @param xController
     * @throws java.lang.ClassNotFoundException
     */
    public BibleGetJSON(XController xController) throws ClassNotFoundException, SQLException{
        m_xController = xController;
        if (xController != null) {
            m_xModel = (XModel) xController.getModel();
            m_xTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,m_xModel);
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier) UnoRuntime.queryInterface(XTextViewCursorSupplier.class,m_xController);
            m_xViewCursor = xViewCursorSupplier.getViewCursor();
        }

        // Get preferences from database       
        biblegetDB = BibleGetDB.getInstance();
        JsonObject myOptions = biblegetDB.getOptions();
        //System.out.println(myOptions.toString());
        JsonValue myResults = myOptions.get("rows");
        navigateTree(myResults, null);
        
    }
    
    public void JSONParse(String jsonString) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException
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
        //System.out.println( "Wanting to set CharFontName to value: "+paragraphFontFamily );
       
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject json = jsonReader.readObject();
        JsonArray arrayJson = json.getJsonArray("results");

        String prevversion = "";
        boolean newversion;
        String prevbook = "";
        boolean newbook;
        int prevchapter = -1;
        boolean newchapter;
        String prevverse = "";
        boolean newverse;
        
        String currentversion;
        String currentbook;
        int currentchapter;
        String currentverse;
        
        boolean firstversion = true;
        boolean firstchapter = true;
        
        boolean firstVerse = false;
        boolean normalText = false;
        
        Iterator pIterator = arrayJson.iterator();
        while (pIterator.hasNext())
        {
            JsonObject currentJson = (JsonObject) pIterator.next();
            currentbook = currentJson.getString("book");
            currentchapter = currentJson.getInt("chapter");
            currentverse = currentJson.getString("verse");
            currentversion = currentJson.getString("version");
            //System.out.println("Current Book = " + currentbook);
            //System.out.println("Current Chapter = " + currentchapter);
            //System.out.println("Current Verse = " + currentverse);
            
            if(!currentverse.equals(prevverse)){
                newverse = true;
                prevverse = currentverse;
            }
            else{
                newverse = false;
            }
            
            if(currentchapter != prevchapter){
                newchapter = true;
                newverse = true;
                prevchapter = currentchapter;
            }
            else{
                newchapter = false;
            }
            
            if(!currentbook.equals(prevbook)){
                newbook = true;
                newchapter = true;
                newverse = true;
                prevbook = currentbook;
            }
            else{
                newbook = false;
            }
            
            if(!currentversion.equals(prevversion)){
                newversion = true;
                newbook = true;
                newchapter = true;
                newverse = true;
                prevversion = currentversion;
            }
            else{
                newversion = false;
            }
            
            //xPropertySet.setPropertyValue("ParaStyleName", "Text body");
            try {
                xPropertySet.setPropertyValue("CharFontName", paragraphFontFamily);
                xPropertySet.setPropertyValue("ParaLeftMargin", paragraphLeftIndent*200 );
                //set paragraph line spacing      
                com.sun.star.style.LineSpacing lineSpacing = new com.sun.star.style.LineSpacing();
                lineSpacing.Mode = com.sun.star.style.LineSpacingMode.PROP;
                lineSpacing.Height = paragraphLineSpacing;
                xPropertySet.setPropertyValue("ParaLineSpacing",lineSpacing);
//                System.out.print("paragraphFontFamily: ");
//                System.out.println(paragraphFontFamily);
//                System.out.print("paragraphLeftIndent: ");
//                System.out.println(paragraphLeftIndent);
//                System.out.print("paragraphLineSpacing: ");
//                System.out.println(paragraphLineSpacing);
            } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | com.sun.star.lang.WrappedTargetException ex){
                Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(newversion){
                firstVerse = true;                
                
                firstchapter = true;
                if(firstversion==true){
                    firstversion=false;
                }
                else{
                    try {                    
                        m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
                    }                                    
                }
                
                try{
                    xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT);

                    // set properties of text change based on user preferences
                    if (boldBookChapter){
                        xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                    } 
                    else{
                        xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
                    }
                    if(italicsBookChapter){
                        xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
                    }
                    else{
                        xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
                    }
                    if(underscoreBookChapter){
                        xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
                    }
                    else{
                        xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
                    }
                    xPropertySet.setPropertyValue("CharColor", textColorBookChapter.getRGB());
                    xPropertySet.setPropertyValue("CharBackColor", bgColorBookChapter.getRGB() & ~0xFF000000);
//                    System.out.print("textColorBookChapter: ");
//                    System.out.println(textColorBookChapter);
//                    System.out.print("bgColorBookChapter: ");
//                    System.out.println(bgColorBookChapter);
                    
                    switch (vAlignBookChapter) {
                        case "sub":
                            xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                            xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                            //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                            break;
                        case "super":
                            xPropertySet.setPropertyValue("CharEscapement", (short)101);
                            xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                            //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                            break;
                        default:
                            xPropertySet.setPropertyValue("CharEscapement", (short)0);
                            xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                            //xPropertySet.setPropertyValue("CharEscapementAuto", false);
                            break;
                    }
                    xPropertySet.setPropertyValue("CharHeight", (float) fontSizeBookChapter);
                } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | com.sun.star.lang.WrappedTargetException ex){
                    Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
                }
                m_xText.insertString(xTextRange, currentversion, false);
                
                try {                    
                    m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(newbook || newchapter){
                //System.out.println(currentbook+" "+currentchapter);
                firstVerse = true;
                
                if(firstchapter==true)
                {
                    firstchapter=false;
                }
                else{
                    try {                    
                        m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
                    }                
                }
                
                xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT );
                
                // set properties of text change based on user preferences
                if (boldBookChapter){
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                } 
                else{
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
                }
                if(italicsBookChapter){
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
                }
                if(underscoreBookChapter){
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", textColorBookChapter.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", bgColorBookChapter.getRGB() & ~0xFF000000);
                switch (vAlignBookChapter) {
                    case "sub":
                        xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    case "super":
                        xPropertySet.setPropertyValue("CharEscapement", (short)101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    default:
                        xPropertySet.setPropertyValue("CharEscapement", (short)0);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", false);
                        break;
                }
                xPropertySet.setPropertyValue("CharHeight", (float) fontSizeBookChapter);
                
                m_xText.insertString(xTextRange, currentbook+" "+currentchapter, false);
                
                try {                    
                    m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
                } catch (com.sun.star.lang.IllegalArgumentException ex) {
                    Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                switch(paragraphAlignment){
                    case "left":
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT );
                        break;
                    case "right":
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.RIGHT );
                        break;
                    case "center":
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.CENTER );
                        break;
                    case "justify":
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.BLOCK );
                        break;
                    default:
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.BLOCK );                        
                }
                
            }
            
            if(newverse){
                normalText = false;
                
                //System.out.print("\n"+currentverse);
                // set properties of text change based on user preferences
                if (boldVerseNumber){
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                } 
                else{
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
                }
                if(italicsVerseNumber){
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
                }
                if(underscoreVerseNumber){
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", textColorVerseNumber.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", bgColorVerseNumber.getRGB() & ~0xFF000000);
                //System.out.println(vAlignVerseNumber);
                xPropertySet.setPropertyValue("CharHeight", (float)fontSizeVerseNumber);
                
                switch (vAlignVerseNumber) {
                    case "sub":
                        xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    case "super":
                        //System.out.println("We are in business! This is going to be superscript");
                        xPropertySet.setPropertyValue("CharEscapement", (short)101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    default:
                        xPropertySet.setPropertyValue("CharEscapement", (short)0);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", false);
                        break;
                }
                
                m_xText.insertString(xTextRange, " "+currentverse, false);
            }
                        
            setVerseTextStyles(xPropertySet);
            
            String currentText = currentJson.getString("text").replace("\n","").replace("\r","");
            String remainingText = currentText;
            //if(currentText.contains("<"))
            if(currentText.matches("(?su).*<[/]{0,1}(?:speaker|sm|po)[f|l|s|i|3]{0,1}[f|l]{0,1}>.*")) //[/]{0,1}(?:sm|po)[f|l|s|i|3]{0,1}[f|l]{0,1}>
            {                
                Pattern pattern1 = Pattern.compile("(.*?)<((speaker|sm|po)[f|l|s|i|3]{0,1}[f|l]{0,1})>(.*?)</\\2>",Pattern.UNICODE_CHARACTER_CLASS);
                Matcher matcher1 = pattern1.matcher(currentText);
                int iteration = 0;
                
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
                        String speakerTagBefore = "";
                        String speakerTagContents = "";
                        String speakerTagAfter = "";
                        
                        if(formattingTagContents.matches("(?su).*<[/]{0,1}speaker>.*")){
                            nestedTag = true;
                            
                            String remainingText2 = formattingTagContents;
                            
                            Matcher matcher2 = pattern1.matcher(formattingTagContents);
                            int iteration2 = 0;
                            while(matcher2.find()){
                                if(matcher2.group(2) != null && matcher2.group(2).isEmpty() == false && "speaker".equals(matcher2.group(2))){
                                    if(matcher2.group(1) != null && matcher2.group(1).isEmpty() == false){
                                        speakerTagBefore = matcher2.group(1);
                                        remainingText2 = remainingText2.replaceFirst(matcher2.group(1), "");
                                    }
                                    speakerTagContents = matcher2.group(4);
                                    speakerTagAfter = remainingText2.replaceFirst("<"+matcher2.group(2)+">"+matcher2.group(4)+"</"+matcher2.group(2)+">", "");
                                }
                            }
                        }
                        
                        if(noVersionFormatting){ formattingTagContents = " "+formattingTagContents+" "; }
                        
                        switch (matchedTag) {
                            case "pof":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "pos":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "poif":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+600);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "po":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "poi":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+600);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "pol":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200));
                                }
                                normalText = false;
                                break;
                            case "poil":
                                if(noVersionFormatting==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200)+600);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(noVersionFormatting==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", (paragraphLeftIndent*200));
                                }
                                normalText = false;
                                break;
                            case "sm":
                                String smallCaps = matcher1.group(4).toLowerCase();
                                //System.out.println("SMALLCAPSIZE THIS TEXT: "+smallCaps);
                                xPropertySet.setPropertyValue("CharCaseMap", com.sun.star.style.CaseMap.SMALLCAPS);
                                m_xText.insertString(xTextRange, smallCaps, false);
                                xPropertySet.setPropertyValue("CharCaseMap", com.sun.star.style.CaseMap.NONE);
                                break;
                            case "speaker":
//                                System.out.println("We have found a speaker tag");
                                xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                                //xPropertySet.setPropertyValue("CharBackTransparent", false);
                                xPropertySet.setPropertyValue("CharBackColor", Color.LIGHT_GRAY.getRGB() & ~0xFF000000);
                                m_xText.insertString(xTextRange, matcher1.group(4), false);
                                if(boldVerseText==false){
                                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);                                
                                }
                                xPropertySet.setPropertyValue("CharBackColor", bgColorVerseText.getRGB() & ~0xFF000000);
                        }
                        remainingText = remainingText.replaceFirst("<"+matcher1.group(2)+">"+matcher1.group(4)+"</"+matcher1.group(2)+">", "");
                    }
                }
//                System.out.println("We have a match for special formatting: "+currentText);
//                System.out.println("And after elaborating our matches, this is what we have left: "+remainingText);
//                System.out.println();
                if(!"".equals(remainingText))
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
        
        try {
            m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
    }
    
    private void insertNestedSpeakerTag(String speakerTagBefore, String speakerTagContents, String speakerTagAfter, XText m_xText, XTextRange xTextRange, XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException
    {
        
//        System.out.println("We are now working with a nested Speaker Tag."); //Using BG="+grayBG.getRGB()+"=R("+r+"),B("+b+"),G("+g+")
//        System.out.println("speakerTagBefore=<"+speakerTagBefore+">,speakerTagContents=<"+speakerTagContents+">,speakerTagAfter=<"+speakerTagAfter+">");
        m_xText.insertString(xTextRange, speakerTagBefore, false);
        
        xPropertySet.setPropertyValue("CharWeight", FontWeight.BOLD);
        //xPropertySet.setPropertyValue("CharBackTransparent", false);
        
        //xPropertySet.setPropertyValue("CharBackColor", Color.LIGHT_GRAY.getRGB());
        xPropertySet.setPropertyValue("CharBackColor", Color.LIGHT_GRAY.getRGB() & ~0xFF000000);
        //xPropertySet.setPropertyValue("CharColor", Color.YELLOW.getRGB());
        
        m_xText.insertString(xTextRange, " "+speakerTagContents+" ", false);
        
        if(boldVerseText==false){
            xPropertySet.setPropertyValue("CharWeight", FontWeight.NORMAL);                                
        }
        //xPropertySet.setPropertyValue("CharBackColor", bgColorVerseText.getRGB());
        xPropertySet.setPropertyValue("CharBackColor", bgColorVerseText.getRGB() & ~0xFF000000);
        //xPropertySet.setPropertyValue("CharColor", textColorVerseText.getRGB());
        m_xText.insertString(xTextRange, speakerTagAfter, false);   
    }
    
    private void navigateTree(JsonValue tree, String key) {
        if (key != null){
            //System.out.print("Key " + key + ": ");
        }
        switch(tree.getValueType()) {
            case OBJECT:
                //System.out.println("OBJECT");
                JsonObject object = (JsonObject) tree;
                for (String name : object.keySet())
                   navigateTree(object.get(name), name);
                break;
            case ARRAY:
                //System.out.println("ARRAY");
                JsonArray array = (JsonArray) tree;
                for (JsonValue val : array)
                   navigateTree(val, null);
                break;
            case STRING:
                JsonString st = (JsonString) tree;
                //System.out.println("STRING " + st.getString());
                getStringOption(key,st.getString());
                break;
            case NUMBER:
                JsonNumber num = (JsonNumber) tree;
                //System.out.println("NUMBER " + num.toString());
                getNumberOption(key,num.intValue());
                break;
            case TRUE:
                getBooleanOption(key,true);
                //System.out.println("BOOLEAN " + tree.getValueType().toString());
                break;
            case FALSE:
                getBooleanOption(key,false);
                //System.out.println("BOOLEAN " + tree.getValueType().toString());
                break;
            case NULL:
                //System.out.println("NULL " + tree.getValueType().toString());
                break;
        }
    }
    
    private void getStringOption(String key,String value){
        switch(key){
            case "PARAGRAPHALIGNMENT": paragraphAlignment = value; break;
            case "PARAGRAPHFONTFAMILY": paragraphFontFamily = value; break;
            case "TEXTCOLORBOOKCHAPTER": textColorBookChapter = Color.decode(value); break; //decode string representation of hex value
            case "BGCOLORBOOKCHAPTER": bgColorBookChapter = Color.decode(value); break; //decode string representation of hex value
            case "VALIGNBOOKCHAPTER": vAlignBookChapter = value; break;
            case "TEXTCOLORVERSENUMBER": textColorVerseNumber = Color.decode(value); break; //decode string representation of hex value
            case "BGCOLORVERSENUMBER": bgColorVerseNumber = Color.decode(value); break; //decode string representation of hex value
            case "VALIGNVERSENUMBER": vAlignVerseNumber = value; break;
            case "TEXTCOLORVERSETEXT": textColorVerseText = Color.decode(value); break; //decode string representation of hex value
            case "BGCOLORVERSETEXT": bgColorVerseText = Color.decode(value); break; //decode string representation of hex value
            case "VALIGNVERSETEXT": vAlignVerseText = value; break;
        }
    }
    
    private void getNumberOption(String key,int value){
        switch(key){
            case "PARAGRAPHLINESPACING": paragraphLineSpacing = (short)value; break; //think of it as percent
            case "PARAGRAPHLEFTINDENT": paragraphLeftIndent = value; break;
            case "FONTSIZEBOOKCHAPTER": fontSizeBookChapter = (float)value; break;       
            case "FONTSIZEVERSENUMBER": fontSizeVerseNumber = (float)value; break;
            case "FONTSIZEVERSETEXT": fontSizeVerseText = (float)value; break;
        }    
    }
    private void getBooleanOption(String key,boolean value){
        switch(key){
            case "BOLDBOOKCHAPTER": boldBookChapter = value; break;
            case "ITALICSBOOKCHAPTER": italicsBookChapter = value; break;
            case "UNDERSCOREBOOKCHAPTER": underscoreBookChapter = value; break;
            case "BOLDVERSENUMBER": boldVerseNumber = value; break;
            case "ITALICSVERSENUMBER": italicsVerseNumber = value; break;
            case "UNDERSCOREVERSENUMBER": underscoreVerseNumber = value; break;
            case "BOLDVERSETEXT": boldVerseText = value; break;
            case "ITALICSVERSETEXT": italicsVerseText = value; break;
            case "UNDERSCOREVERSETEXT": underscoreVerseText = value; break;
            case "NOVERSIONFORMATTING": noVersionFormatting = value; break;
        }    
    }
    
    public double pointsToMillimeters(int points)
    {
        double ratio = 0.352777778;
        double converted = points * ratio;
        double rounded = Math.round(converted*10)/10;
        return rounded;
    }
    
    private void setVerseTextStyles(com.sun.star.beans.XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException
    {
            if (boldVerseText){
                xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
            } 
            else{
                xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
            }
            if(italicsVerseText){
                xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
            }
            else{
                xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
            }
            if(underscoreVerseText){
                xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
            }
            else{
                xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
            }
            xPropertySet.setPropertyValue("CharColor", textColorVerseText.getRGB());
            xPropertySet.setPropertyValue("CharBackColor", bgColorVerseText.getRGB() & ~0xFF000000);
            xPropertySet.setPropertyValue("CharHeight", (float)fontSizeVerseText);
            
            switch (vAlignVerseText) {
                case "sub":
                    xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                    xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                    //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                    break;
                case "super":
                    xPropertySet.setPropertyValue("CharEscapement", (short)101);
                    xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                    //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                    break;
                default:
                    xPropertySet.setPropertyValue("CharEscapement", (short)0);
                    xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                    //xPropertySet.setPropertyValue("CharEscapementAuto", false);
                    break;
            }        
    }
    
    private void insertParagraphBreak(XText m_xText,com.sun.star.text.XTextRange xTextRange)
    {
        try {
            m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            Logger.getLogger(BibleGetJSON.class.getName()).log(Level.SEVERE, null, ex);
        }            
    }
}
