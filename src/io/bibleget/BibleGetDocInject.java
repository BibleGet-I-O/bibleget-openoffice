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
        //System.out.println( "Wanting to set CharFontName to value: "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY );
       
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
                xPropertySet.setPropertyValue("CharFontName", USERPREFS.PARAGRAPHSTYLES_FONTFAMILY );
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
                
                xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()); //USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *200
                xPropertySet.setPropertyValue("ParaRightMargin", rightMarginIn100thMM.intValue());
                //set paragraph line spacing      
                com.sun.star.style.LineSpacing lineSpacing = new com.sun.star.style.LineSpacing();
                lineSpacing.Mode = com.sun.star.style.LineSpacingMode.PROP;
                lineSpacing.Height = USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT.shortValue();
                xPropertySet.setPropertyValue("ParaLineSpacing",lineSpacing);
//                System.out.print("USERPREFS.PARAGRAPHSTYLES_FONTFAMILY: ");
//                System.out.println(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY);
//                System.out.print("USERPREFS.PARAGRAPHSTYLES_LEFTINDENT: ");
//                System.out.println(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
//                System.out.print("USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT: ");
//                System.out.println(USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT);
            } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | com.sun.star.lang.WrappedTargetException ex){
                Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
                    }                                    
                }
                
                try{
                    xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT);

                    // set properties of text change based on user preferences
                    if (USERPREFS.BIBLEVERSIONSTYLES_BOLD){
                        xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                    } 
                    else{
                        xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
                    }
                    if(USERPREFS.BIBLEVERSIONSTYLES_ITALIC){
                        xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
                    }
                    else{
                        xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
                    }
                    if(USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE){
                        xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
                    }
                    else{
                        xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
                    }
                    xPropertySet.setPropertyValue("CharColor", USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR.getRGB());
                    xPropertySet.setPropertyValue("CharBackColor", USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
//                    System.out.print("USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR: ");
//                    System.out.println(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);
//                    System.out.print("USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR: ");
//                    System.out.println(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR);
                    
                    switch (USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION) {
                        case TOP:
                            //xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                            //xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                            //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                            break;
                        case BOTTOM:
                            //xPropertySet.setPropertyValue("CharEscapement", (short)101);
                            //xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                            //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                            break;
                    }
                    xPropertySet.setPropertyValue("CharHeight", USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE.floatValue() );
                } catch (UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | com.sun.star.lang.WrappedTargetException ex){
                    Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
                }
                m_xText.insertString(xTextRange, currentversion, false);
                
                try {                    
                    m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
                    }                
                }
                
                xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT );
                
                // set properties of text change based on user preferences
                if (USERPREFS.BOOKCHAPTERSTYLES_BOLD){
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                } 
                else{
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
                }
                if(USERPREFS.BOOKCHAPTERSTYLES_ITALIC){
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
                }
                if(USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE){
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                switch (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION) {
                    case TOP:
                        //xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                        //xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    case BOTTOM:
                        //xPropertySet.setPropertyValue("CharEscapement", (short)101);
                        //xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    case BOTTOMINLINE:
                        //xPropertySet.setPropertyValue("CharEscapement", (short)0);
                        //xPropertySet.setPropertyValue("CharEscapementHeight", (byte)100);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", false);
                        break;
                }
                xPropertySet.setPropertyValue("CharHeight", (float) USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE);
                
                m_xText.insertString(xTextRange, currentbook+" "+currentchapter, false);
                
                try {                    
                    m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
                } catch (com.sun.star.lang.IllegalArgumentException ex) {
                    Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                switch(USERPREFS.PARAGRAPHSTYLES_ALIGNMENT){
                    case LEFT:
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.LEFT );
                        break;
                    case RIGHT:
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.RIGHT );
                        break;
                    case CENTER:
                        xPropertySet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.CENTER );
                        break;
                    case JUSTIFY:
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
                if (USERPREFS.VERSENUMBERSTYLES_BOLD){
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
                } 
                else{
                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
                }
                if(USERPREFS.VERSENUMBERSTYLES_ITALIC){
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
                }
                else{
                    xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
                }
                if(USERPREFS.VERSENUMBERSTYLES_UNDERLINE){
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
                }
                else{
                    xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
                }
                xPropertySet.setPropertyValue("CharColor", USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR.getRGB());
                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSENUMBERSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
                //System.out.println(USERPREFS.VERSENUMBERSTYLES_VALIGN);
                xPropertySet.setPropertyValue("CharHeight", USERPREFS.VERSENUMBERSTYLES_FONTSIZE.floatValue());
                
                switch (USERPREFS.VERSENUMBERSTYLES_VALIGN) {
                    case SUBSCRIPT:
                        xPropertySet.setPropertyValue("CharEscapement", (short)-101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    case SUPERSCRIPT:
                        //System.out.println("We are in business! This is going to be superscript");
                        xPropertySet.setPropertyValue("CharEscapement", (short)101);
                        xPropertySet.setPropertyValue("CharEscapementHeight", (byte)58);
                        //xPropertySet.setPropertyValue("CharEscapementAuto", true);
                        break;
                    case NORMAL:
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
                        String speakerTagBefore = "";
                        String speakerTagContents = "";
                        String speakerTagAfter = "";
                        
                        if(formattingTagContents.matches("(?su).*<[/]{0,1}speaker>.*")){
                            nestedTag = true;
                            
                            String remainingText2 = formattingTagContents;
                            
                            Matcher matcher2 = pattern1.matcher(formattingTagContents);
                            //int iteration2 = 0;
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
                        
                        if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING){ formattingTagContents = " "+formattingTagContents+" "; }
                        
                        switch (matchedTag) {
                            case "pof":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+400);
                                    xPropertySet.setPropertyValue("ParaRightMargin", rightMarginIn100thMM.intValue());
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "pos":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "poif":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+600);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "po":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "poi":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+600);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                }
                                normalText = false;
                                break;
                            case "pol":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+400);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM);
                                }
                                normalText = false;
                                break;
                            case "poil":
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    if(!firstVerse && normalText){ insertParagraphBreak(m_xText,xTextRange); }
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM.intValue()+600);
                                }
                                if(nestedTag){
                                    insertNestedSpeakerTag(speakerTagBefore, speakerTagContents, speakerTagAfter, m_xText, xTextRange, xPropertySet);
                                }
                                else{
                                    m_xText.insertString(xTextRange, formattingTagContents, false);
                                }
                                if(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING==false){
                                    insertParagraphBreak(m_xText,xTextRange);
                                    xPropertySet.setPropertyValue("ParaLeftMargin", leftMarginIn100thMM);
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
                                if(USERPREFS.VERSETEXTSTYLES_BOLD==false){
                                    xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);                                
                                }
                                xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSETEXTSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
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
            Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
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
        
        if(USERPREFS.VERSETEXTSTYLES_BOLD==false){
            xPropertySet.setPropertyValue("CharWeight", FontWeight.NORMAL);                                
        }
        //xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSETEXTSTYLES_BGCOLOR.getRGB());
        xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSETEXTSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
        //xPropertySet.setPropertyValue("CharColor", USERPREFS.VERSETEXTSTYLES_COLOR.getRGB());
        m_xText.insertString(xTextRange, speakerTagAfter, false);   
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
            if (USERPREFS.VERSETEXTSTYLES_BOLD){
                xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
            } 
            else{
                xPropertySet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
            }
            if(USERPREFS.VERSETEXTSTYLES_ITALIC){
                xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.ITALIC);
            }
            else{
                xPropertySet.setPropertyValue("CharPosture", com.sun.star.awt.FontSlant.NONE);
            }
            if(USERPREFS.VERSETEXTSTYLES_UNDERLINE){
                xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.SINGLE);
            }
            else{
                xPropertySet.setPropertyValue("CharUnderline", com.sun.star.awt.FontUnderline.NONE);
            }
            xPropertySet.setPropertyValue("CharColor", USERPREFS.VERSETEXTSTYLES_TEXTCOLOR.getRGB());
            xPropertySet.setPropertyValue("CharBackColor", USERPREFS.VERSETEXTSTYLES_BGCOLOR.getRGB() & ~0xFF000000);
            xPropertySet.setPropertyValue("CharHeight", USERPREFS.VERSETEXTSTYLES_FONTSIZE.floatValue());
            /*
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
            */
    }
    
    private void insertParagraphBreak(XText m_xText,com.sun.star.text.XTextRange xTextRange)
    {
        try {
            m_xText.insertControlCharacter(xTextRange, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            Logger.getLogger(BibleGetDocInject.class.getName()).log(Level.SEVERE, null, ex);
        }            
    }
}
