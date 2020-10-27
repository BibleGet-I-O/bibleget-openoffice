/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XController;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XViewSettingsSupplier;
import static io.bibleget.BibleGetI18N.__;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.OS;
import org.cef.browser.CefBrowser;

/**
 *
 * @author Lwangaman
 */
public class OptionsFrame extends javax.swing.JFrame {

    private final BibleGetDB biblegetDB;
    private final Preferences USERPREFS;
    private final LocalizedBibleBooks L10NBibleBooks;
        
    private final FontFamilyListCellRenderer FFLCRenderer;
    private final DefaultComboBoxModel fontFamilies;
    private final CefApp cefApp;
    private final CefClient client;
    private final CefBrowser browser;
    private final Component browserUI;
    private final String previewDocument;
    private final String previewDocStylesheet;
    private final String previewDocScript;
    private String bookChapter;
    private String bookChapterWrapBefore = "";
    private String bookChapterWrapAfter = "";
    private String bibleVersionWrapBefore = "";
    private String bibleVersionWrapAfter = "";
    private String fullQuery = "";
    private LocalizedBibleBook localizedBookSamuel;
    
    private final ImageIcon buttonStateOff = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state off.png"));
    private final ImageIcon buttonStateOn = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state on.png"));
    private final ImageIcon buttonStateOffHover = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state off hover.png"));
    private final ImageIcon buttonStateOnHover = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state on hover.png"));
    private static OptionsFrame instance;
    
    private XController m_xController;
    
    //should we convert to inches as base, and then from inches to the next unit?
    private final Double Inches2CM = 2.54;
    private final Double Inches2MM = 25.4;
    private final Double Inches2Points = 72.0;
    private final Double Inches2Picas = 6.0;
    
    private final Double CM2Inches = 0.393701;
    private final Double CM2MM = 10.0;
    private final Double CM2Points = 28.3465;
    private final Double CM2Picas = 2.36222;
    
    private final Double MM2Inches = 0.0393701;
    private final Double MM2CM = 0.1;
    private final Double MM2Points = 2.83465;
    private final Double MM2Picas = 0.236222;
    
    private final Double Points2Inches = 0.0138889;
    private final Double Points2CM = 0.0352778;
    private final Double Points2MM = 0.352778;
    private final Double Points2Picas = 0.083334;
    
    private final Double Picas2Inches = 0.166665;
    private final Double Picas2CM = 0.42333;
    private final Double Picas2MM = 4.2333;
    private final Double Picas2Points = 11.9999;

    private final Double maxIndentCM = 5.0;
    private final Double stepIndentCM = 1.0;
    
    private final Double maxIndentINCH = 2.5;
    private final Double stepIndentINCH = 0.5;
    
    private final Double maxIndentMM = 48.0;
    private final Double stepIndentMM = 12.0;
    
    private final Double maxIndentPoints = 144.0;
    private final Double stepIndentPoints = 36.0;
    
    private final Double maxIndentPicas = 12.0;
    private final Double stepIndentPicas = 3.0;
    
    
    private String rulerLength = "6.7";
    
    /**
     * Creates new form OptionsFrame
     * @param pkgPath
     */
    private OptionsFrame(XController m_xController) throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception {
        this.L10NBibleBooks = LocalizedBibleBooks.getInstance();
        this.FFLCRenderer = new FontFamilyListCellRenderer();
        this.m_xController = m_xController;
        
        //jTextPane does not initialize correctly, it causes a Null Exception Pointer
        //Following line keeps this from crashing the program
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        // Get preferences from database       
        //System.out.println("getting instance of BibleGetDB");
        biblegetDB = BibleGetDB.getInstance();
        USERPREFS = Preferences.getInstance();
        if(USERPREFS != null){
            System.out.println("USERPREFS is not null at least.");
            //System.out.println("USERPREFS.toString = " + USERPREFS.toString());
            //Field[] fields = Preferences.class.getFields();
            //System.out.println(Arrays.toString(fields));
        }
        //System.out.println("getting JsonObject of biblegetDB options");
        this.fontFamilies = BibleGetIO.getFontFamilies();

        XViewSettingsSupplier xViewSettings = (XViewSettingsSupplier) UnoRuntime.queryInterface(XViewSettingsSupplier.class,m_xController);
        XPropertySet viewSettings=xViewSettings.getViewSettings();
        System.out.println("I got the HorizontalRulerMetric property, here is the type :" + AnyConverter.getType(viewSettings.getPropertyValue("HorizontalRulerMetric")));
        System.out.println("And here is the value: " + AnyConverter.toInt(viewSettings.getPropertyValue("HorizontalRulerMetric")));
        int mUnit1 = AnyConverter.toInt(viewSettings.getPropertyValue("HorizontalRulerMetric"));
        USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT = BGET.MEASUREUNIT.valueOf(mUnit1);
        if(BibleGetIO.measureUnit != USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT){
            System.out.println("Ruler has already changed from initial unit in " + BibleGetIO.measureUnit.name() + " to " + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.name());
        } else {
            System.out.println("Ruler unit does not seem to have changed (" + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.name() + " | " + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ")" );
        }
        //System.out.println("(OptionsFrame: 127) USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR ="+USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);        
        String vnPosition = USERPREFS.VERSENUMBERSTYLES_VALIGN == BGET.VALIGN.NORMAL ? "position: static;" : "position: relative;";
        String vnTop = "";
        switch(USERPREFS.VERSENUMBERSTYLES_VALIGN){
            case SUPERSCRIPT:
                vnTop = " top: -0.6em;";
                break;
            case SUBSCRIPT:
                vnTop = " top: 0.6em;";
                break;
            case NORMAL:
                vnTop = "";
        }
        
        if(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP == BGET.WRAP.PARENTHESES){
            bibleVersionWrapBefore = "(";
            bibleVersionWrapAfter = ")";
        }
        else if(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP == BGET.WRAP.BRACKETS){
            bibleVersionWrapBefore = "[";
            bibleVersionWrapAfter = "]";
        }

        if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP == BGET.WRAP.PARENTHESES){
            bookChapterWrapBefore = "(";
            bookChapterWrapAfter = ")";
        }
        else if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP == BGET.WRAP.BRACKETS){
            bookChapterWrapBefore = "[";
            bookChapterWrapAfter = "]";
        }

        localizedBookSamuel = (L10NBibleBooks != null ? L10NBibleBooks.GetBookByIndex(8) : new LocalizedBibleBook("1Sam","1Samuel") );
        switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT){
            case BIBLELANG:
                bookChapter = "I Samuelis 1";
                break;
            case BIBLELANGABBREV:
                bookChapter = "I Sam 1";
                break;
            case USERLANG:
                bookChapter = localizedBookSamuel.Fullname + " 1";
                break;
            case USERLANGABBREV:
                bookChapter = localizedBookSamuel.Abbrev + " 1";
        }
        
        if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY){
            fullQuery = ",1-3";
        }
        
        Double lineHeight = Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT) / 100;
        previewDocStylesheet = "<style type=\"text/css\">"
            + "html,body { padding: 0px; margin: 0px; background-color: #FFFFFF; width: 1000px; }"
            + "p { padding: 0px; margin: 0px; }"
            + ".previewRuler { margin: 3px auto; }"
            + "div.results { box-sizing: border-box; margin: 0px auto; padding-left: 35px; padding-right:35px; font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            //+ "div.results .bibleVersion { font-family: '" + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "'; }"
            + "div.results .bibleVersion { font-size: " + USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE + "pt; }"
            + "div.results .bibleVersion { font-weight: " + (USERPREFS.BIBLEVERSIONSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .bibleVersion { font-style: " + (USERPREFS.BIBLEVERSIONSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE ? "div.results .bibleVersion { text-decoration: underline; }" : "")
            + "div.results .bibleVersion { color: " + ColorToHexString(USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR) + "; }"
            + "div.results .bibleVersion { background-color: " + ColorToHexString(USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR) + "; }"
            + "div.results .bibleVersion { text-align: " + USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getCSSValue() + "; }"
            //+ "div.results .bookChapter { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            + "div.results .bookChapter { font-size: " + USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE + "pt; }"
            + "div.results .bookChapter { font-weight: " + (USERPREFS.BOOKCHAPTERSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .bookChapter { font-style: " + (USERPREFS.BOOKCHAPTERSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE ? "div.results .bookChapter { text-decoration: underline; }" : "")
            + "div.results .bookChapter { color: " + ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR) + "; }"
            + "div.results .bookChapter { background-color: " + ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR) + "; }"
            + "div.results .bookChapter { text-align: " + USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getCSSValue() + "; }"
            + "div.results span.bookChapter { display: inline-block; margin-left: 6px; }"
            + "div.results .versesParagraph { text-align: " + USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue() + "; }"
            + "div.results .versesParagraph { line-height: " + String.format(Locale.ROOT, "%.1f", lineHeight) + "em; }"
            //+ "div.results .versesParagraph .verseNum { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            + "div.results .versesParagraph .verseNum { font-size: " + USERPREFS.VERSENUMBERSTYLES_FONTSIZE + "pt; }"
            + "div.results .versesParagraph .verseNum { font-weight: " + (USERPREFS.VERSENUMBERSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .versesParagraph .verseNum { font-style: " + (USERPREFS.VERSENUMBERSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.VERSENUMBERSTYLES_UNDERLINE ? "div.results .versesParagraph .verseNum { text-decoration: underline; }" : "")
            + "div.results .versesParagraph .verseNum { color: " + ColorToHexString(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR) + "; }"
            + "div.results .versesParagraph .verseNum { background-color: " + ColorToHexString(USERPREFS.VERSENUMBERSTYLES_BGCOLOR) + "; }"
            + "div.results .versesParagraph .verseNum { vertical-align: baseline; " + vnPosition + vnTop + " }"
            + "div.results .versesParagraph .verseNum { padding-left: 3px; }"
            + "div.results .versesParagraph .verseNum:first-child { padding-left: 0px; }"
            //+ "div.results .versesParagraph .verseText { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            + "div.results .versesParagraph .verseText { font-size: " + USERPREFS.VERSETEXTSTYLES_FONTSIZE + "pt; }"
            + "div.results .versesParagraph .verseText { font-weight: " + (USERPREFS.VERSETEXTSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .versesParagraph .verseText { font-style: " + (USERPREFS.VERSETEXTSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.VERSETEXTSTYLES_UNDERLINE ? "div.results .versesParagraph .verseText { text-decoration: underline; }" : "")
            + "div.results .versesParagraph .verseText { color: " + ColorToHexString(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR) + "; }"
            + "div.results .versesParagraph .verseText { background-color: " + ColorToHexString(USERPREFS.VERSETEXTSTYLES_BGCOLOR) + "; }"
            + "</style>";
        
        previewDocScript = "<script>"
            + "var getPixelRatioVals = function(rulerLength,units){"
            + "let inchesToCM = 2.54,"
            + "inchesToMM = 25.4,"
            + "inchesToPoints = 72,"
            + "inchesToPicas = 6,"
            + "dpr = window.devicePixelRatio,"
            //ppi = ((96 * dpr) / 100),
            //dpi = (96 * ppi),
            + "dpi = 96 * dpr,"
            + "drawInterval = 0.1;"
            + "switch(units){"
            + "case 2: " //CM
            + "dpi /= inchesToCM;"
            + "rulerLength *= inchesToCM;"
            + "drawInterval = .25;"
            + "break;"
            //+ "case 8: " //INCH
            //+ "break;"
            + "case 1: " //MM
            + "dpi /= inchesToMM;"
            + "rulerLength *= inchesToMM;"
            + "drawInterval = 6;"
            + "break;"
            + "case 7: " //PICA
            + "dpi /= inchesToPicas;"
            + "rulerLength *= inchesToPicas;"
            + "drawInterval = 1;"
            + "break;"
            + "case 6: " //POINT
            + "dpi /= inchesToPoints;"
            + "rulerLength *= inchesToPoints;"
            + "drawInterval = 12;"
            + "break;"
            + "}"
            + "return {"
            //+ "inchesToCM: inchesToCM,"
            + "dpr: dpr,"
            //ppi: ppi,
            + "dpi: dpi,"
            + "rulerLength: rulerLength,"
            + "drawInterval: drawInterval"
            + "};"
            + "},"
            + "triangleAt = function (x,context,pixelRatioVals,initialPadding,canvasWidth){"
            + "let xPos = x*pixelRatioVals.dpi;" //x.map(0,pixelRatioVals.rulerLength,0,(canvasWidth-(initialPadding*2)));
            + "window.xVal = x;"
            + "window.xPos = xPos;"
            + "context.lineWidth = 0.5;"
            + "context.fillStyle = \"#4285F4\";"
            + "context.beginPath();"
            + "context.moveTo(initialPadding+xPos-6,11);"
            + "context.lineTo(initialPadding+xPos+6,11);"
            + "context.lineTo(initialPadding+xPos,18);"
            + "context.closePath();"
            + "context.stroke();"
            + "context.fill();"
            + "},"
            /**
             * FUNCTION drawRuler
             * @ rulerLen (float) in Inches (e.g. 7)
             * @ cvtToCM (boolean) whether to convert values from inches to centimeters
             * @ lftindnt (float) in Inches, to set xPos of triangle for left indent
             * @ rgtindnt (float) in Inches, to set xPos of triangle for right indent
            */
            + "drawRuler = function(rulerLen, units, lftindnt, rgtindnt){"
            + "var pixelRatioVals = getPixelRatioVals(rulerLen,units),"
            + "initialPadding = 35,"
            + "$canvas = jQuery('.previewRuler');"
            + "$canvas.each(function(){"
            + "let canvas = this,"
            + "context = canvas.getContext('2d'),"
            + "canvasWidth = (rulerLen * 96 * pixelRatioVals.dpr) + (initialPadding*2);"
            + "canvas.style.width = canvasWidth + 'px';"
            + "canvas.style.height = '20px';"
            + "canvas.width = Math.round(canvasWidth * pixelRatioVals.dpr);"
            + "canvas.height = Math.round(20 * pixelRatioVals.dpr);"
            + "canvas.style.width = Math.round(canvas.width / pixelRatioVals.dpr) + 'px';"
            + "canvas.style.height = Math.round(canvas.height / pixelRatioVals.dpr) + 'px';"
            + "context.scale(pixelRatioVals.dpr,pixelRatioVals.dpr);"
            + "context.translate(pixelRatioVals.dpr, 0);"
            + "context.lineWidth = 0.5;"
            + "context.strokeStyle = '#000';"
            + "context.font = 'bold 10px Arial';"
            + "context.beginPath();"
            + "context.moveTo(initialPadding, 1);"
            + "context.lineTo(initialPadding + (pixelRatioVals.rulerLength * pixelRatioVals.dpi),1);"
            + "context.stroke();"
            + "let currentWholeNumber = 0;"
            + "let offset = 2;"
            + "let roundDecimal = 10;"
            + "if(units==2){ roundDecimal = 100; } "
            + "for(let interval = 0; interval <= pixelRatioVals.rulerLength; interval = (Math.round((interval + pixelRatioVals.drawInterval) * roundDecimal) / roundDecimal)){"
            /*+ "console.log('interval = '+interval+' | interval == Math.floor(interval): '+(interval == Math.floor(interval) && interval > 0)+' | interval == Math.floor(interval)+0.5: '+(interval == Math.floor(interval)+0.5)+' | interval == Math.floor(interval)+0.25: '+(interval == Math.floor(interval)+0.5)+'');"*/
            + "let xPosA = Math.round(interval*pixelRatioVals.dpi)+0.5;"
            + "if(interval == Math.floor(interval) && interval > 0){"
            + "switch(units){"
            + "case 8: " //INCH
            + "if(currentWholeNumber+1 == 10){ offset+=4; }"
            + "context.fillText(++currentWholeNumber,initialPadding+xPosA-offset,14);"
            + "break;"
            + "case 2: " //CM
            + "if(currentWholeNumber+1 == 10){ offset+=4; }"
            + "context.fillText(++currentWholeNumber,initialPadding+xPosA-offset,14);"
            + "break;"
            + "case 1: " //MM
            + "if(Math.floor(interval) % 6 == 0){"
            + "if((currentWholeNumber+1)*6 == 12){ offset+=4; }"
            + "context.fillText(++currentWholeNumber*6,initialPadding+xPosA-offset,14);"
            + "}"
            + "else{"
            + "context.beginPath();"
            + "context.moveTo(initialPadding+xPosA,10);"
            + "context.lineTo(initialPadding+xPosA,5);"
            + "context.closePath();"
            + "context.stroke();"
            + "}"
            + "break;"
            + "case 7: " //PICA
            + "if(Math.floor(interval) % 2 == 0){"
            + "if((currentWholeNumber+1)*2 == 10){ offset+=4; }"
            + "context.fillText(++currentWholeNumber*2,initialPadding+xPosA-offset,14);"
            + "}"
            + "else{"
            + "context.beginPath();"
            + "context.moveTo(initialPadding+xPosA,10);"
            + "context.lineTo(initialPadding+xPosA,5);"
            + "context.closePath();"
            + "context.stroke();"
            + "}"
            + "break;"
            + "case 6: " //POINT
            + "if(Math.floor(interval) % 36 == 0){"
            + "if((currentWholeNumber+1)*36 == 36){ offset+=4; }"
            + "else if((currentWholeNumber+1)*36 == 108){ offset +=4; }"
            + "context.fillText(++currentWholeNumber*36,initialPadding+xPosA-offset,14);"
            + "}"
            + "else{"
            + "context.beginPath();"
            + "context.moveTo(initialPadding+xPosA,10);"
            + "context.lineTo(initialPadding+xPosA,5);"
            + "context.closePath();"
            + "context.stroke();"
            + "}"
            + "break;"
            + "}"
            + "}"
            + "else if(interval == Math.floor(interval)+0.5){"
            + "context.beginPath();"
            + "context.moveTo(initialPadding+xPosA,15);"
            + "context.lineTo(initialPadding+xPosA,5);"
            + "context.closePath();"
            + "context.stroke();"
            + "}"
            + "else{"
            + "context.beginPath();"
            + "context.moveTo(initialPadding+xPosA,10);"
            + "context.lineTo(initialPadding+xPosA,5);"
            + "context.closePath();"
            + "context.stroke();"
            + "}"
            + "}"
            + "triangleAt(lftindnt,context,pixelRatioVals,initialPadding,canvasWidth);"
            + "triangleAt(pixelRatioVals.rulerLength-rgtindnt,context,pixelRatioVals,initialPadding,canvasWidth);"
            + "context.translate(-pixelRatioVals.dpr, -0);"
            + "});"
            + "};"
            + "jQuery(document).ready(function(){"
            + "let rulerLength = "+rulerLength+";"
            + "let pixelRatioVals = getPixelRatioVals(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ");"
            + "let leftindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "let rightindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "let bestWidth = rulerLength * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + ");"
            + "});"
            + "</script>";
        
        previewDocument = "<html><head><meta charset=\"utf-8\">" + previewDocStylesheet
            + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>" 
            + previewDocScript 
            + "</head><body>"
            + "<div style=\"text-align: center;\"><canvas class=\"previewRuler\"></canvas></div>"
            + "<div class=\"results bibleQuote\">"
            + (USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION == BGET.POS.TOP && USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.SHOW ? "<p class=\"bibleVersion\">" + bibleVersionWrapBefore + "NVBSE" + bibleVersionWrapAfter + "</p>" : "")
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.TOP ? "<p class=\"bookChapter\">" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "</p>" : "")
            + "<p class=\"versesParagraph\" style=\"margin-top:0px;\">"
            + (USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW ? "<span class=\"verseNum\">1</span>" : "")
            + "<span class=\"verseText\">Fuit vir unus de Ramathaim Suphita de monte Ephraim, et nomen eius Elcana filius Ieroham filii Eliu filii Thohu filii Suph, Ephrathaeus.</span>"
            + (USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW ? "<span class=\"verseNum\">2</span>" : "")
            + "<span class=\"verseText\">Et habuit duas uxores: nomen uni Anna et nomen secundae Phenenna. Fueruntque Phenennae filii, Annae autem non erant liberi.</span>"
            + (USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW ? "<span class=\"verseNum\">3</span>" : "")
            + "<span class=\"verseText\">Et ascendebat vir ille de civitate sua singulis annis, ut adoraret et sacrificaret Domino exercituum in Silo. Erant autem ibi duo filii Heli, Ophni et Phinees, sacerdotes Domini.</span>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.BOTTOMINLINE ? "<span class=\"bookChapter\">" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "</span>" : "")
            + "</p>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.BOTTOM ? "<p class=\"bookChapter\">" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "</p>" : "")
            + (USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION == BGET.POS.BOTTOM && USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.SHOW ? "<p class=\"bibleVersion\">" + bibleVersionWrapBefore + "NVBSE" + bibleVersionWrapAfter + "</p>" : "")
            + "</div>"
            + "</body>";

                        
        //this.myMessages = BibleGetI18N.getMessages();
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = OS.isLinux();
        //settings.log_severity = LogSeverity.LOGSEVERITY_ERROR;
        cefApp = CefApp.getInstance(settings);
        client = cefApp.createClient();
        //String HTMLStrWithStyles = String.format(HTMLStr,s);
        browser = client.createBrowser( DataUri.create("text/html",previewDocument), OS.isLinux(), false);
        browserUI = browser.getUIComponent();
                
        initComponents();
        jInternalFrame1.getContentPane().add(browserUI, BorderLayout.CENTER);
    }

    public static OptionsFrame getInstance(XController m_xController) throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception
    {
        if(instance == null)
        {
            instance = new OptionsFrame(m_xController);
        }
        return instance;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupParagraphAlign = new javax.swing.ButtonGroup();
        buttonGroupBibleVersionAlign = new javax.swing.ButtonGroup();
        buttonGroupBibleVersionVAlign = new javax.swing.ButtonGroup();
        buttonGroupBibleVersionWrap = new javax.swing.ButtonGroup();
        buttonGroupBookChapterAlign = new javax.swing.ButtonGroup();
        buttonGroupBookChapterVAlign = new javax.swing.ButtonGroup();
        buttonGroupBookChapterFormat = new javax.swing.ButtonGroup();
        buttonGroupBookChapterWrap = new javax.swing.ButtonGroup();
        jPanelParagraph = new javax.swing.JPanel();
        jPanelParagraphAlignment = new javax.swing.JPanel();
        jToolBarParagraphAlignment = new javax.swing.JToolBar();
        jToggleButtonParagraphLeft = new javax.swing.JToggleButton();
        jToggleButtonParagraphCenter = new javax.swing.JToggleButton();
        jToggleButtonParagraphRight = new javax.swing.JToggleButton();
        jToggleButtonParagraphJustify = new javax.swing.JToggleButton();
        jPanelParagraphIndentLeft = new javax.swing.JPanel();
        jToolBarParagraphIndentLeft = new javax.swing.JToolBar();
        jButtonLeftIndentMore = new javax.swing.JButton();
        jButtonLeftIndentLess = new javax.swing.JButton();
        jLabelLeftIndent = new javax.swing.JLabel();
        jPanelParagraphIndentRight = new javax.swing.JPanel();
        jToolBarParagraphIndentRight = new javax.swing.JToolBar();
        jButtonRightIndentMore = new javax.swing.JButton();
        jButtonRightIndentLess = new javax.swing.JButton();
        jLabelRightIndent = new javax.swing.JLabel();
        jPanelParagraphLineHeight = new javax.swing.JPanel();
        jToolBarParagraphLineHeight = new javax.swing.JToolBar();
        jComboBoxParagraphLineHeight = new javax.swing.JComboBox();
        jPanelParagraphFontFamily = new javax.swing.JPanel();
        jToolBarParagraphFontFamily = new javax.swing.JToolBar();
        jComboBoxParagraphFontFamily = new javax.swing.JComboBox();
        Accessible a = jComboBoxParagraphFontFamily.getUI().getAccessibleChild(jComboBoxParagraphFontFamily, 0);
        if (a instanceof javax.swing.plaf.basic.ComboPopup) {
            JList popupList = ((javax.swing.plaf.basic.ComboPopup) a).getList();
            // route the comboBox' prototype to the list
            // should happen in BasicComboxBoxUI
            popupList.setPrototypeCellValue(jComboBoxParagraphFontFamily.getPrototypeDisplayValue());
        }
        jPanel3 = new javax.swing.JPanel();
        jPanelBibleVersion = new javax.swing.JPanel();
        jToolBar8 = new javax.swing.JToolBar();
        jToggleButtonBibleVersionBold = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionItalic = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionUnderline = new javax.swing.JToggleButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        jButtonBibleVersionTextColor = new javax.swing.JButton();
        jButtonBibleVersionHighlightColor = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator9 = new javax.swing.JToolBar.Separator();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jComboBoxBibleVersionFontSize = new javax.swing.JComboBox();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator12 = new javax.swing.JToolBar.Separator();
        jToggleButtonBibleVersionAlignLeft = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionAlignCenter = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionAlignRight = new javax.swing.JToggleButton();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jToggleButtonBibleVersionWrapNone = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionWrapParentheses = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionWrapBrackets = new javax.swing.JToggleButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        jToggleButtonBibleVersionVAlignTop = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionVAlignBottom = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionVisibility = new javax.swing.JToggleButton();
        jPanelBookChapter = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jToggleButtonBookChapterBold = new javax.swing.JToggleButton();
        jToggleButtonBookChapterItalic = new javax.swing.JToggleButton();
        jToggleButtonBookChapterUnderline = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonBookChapterTextColor = new javax.swing.JButton();
        jButtonBookChapterHighlightColor = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator4 = new javax.swing.JToolBar.Separator();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jComboBoxBookChapterFontSize = new javax.swing.JComboBox();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator13 = new javax.swing.JToolBar.Separator();
        jToggleButtonBookChapterAlignLeft = new javax.swing.JToggleButton();
        jToggleButtonBookChapterAlignCenter = new javax.swing.JToggleButton();
        jToggleButtonBookChapterAlignRight = new javax.swing.JToggleButton();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        jToggleButtonBookChapterWrapNone = new javax.swing.JToggleButton();
        jToggleButtonBookChapterWrapParentheses = new javax.swing.JToggleButton();
        jToggleButtonBookChapterWrapBrackets = new javax.swing.JToggleButton();
        jSeparator14 = new javax.swing.JToolBar.Separator();
        jToggleButtonBookChapterVAlignTop = new javax.swing.JToggleButton();
        jToggleButtonBookChapterVAlignBottom = new javax.swing.JToggleButton();
        jToggleButtonBookChapterVAlignBottominline = new javax.swing.JToggleButton();
        jToggleButtonBookChapterFullRef = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jToggleButtonBookChapterBibleLang = new javax.swing.JToggleButton();
        jToggleButtonBookChapterBibleLangAbbrev = new javax.swing.JToggleButton();
        jToggleButtonBookChapterUserLang = new javax.swing.JToggleButton();
        jToggleButtonBookChapterUserLangAbbrev = new javax.swing.JToggleButton();
        jPanelVerseNumber = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        jToggleButtonVerseNumberBold = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberItalic = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberUnderline = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonVerseNumberTextColor = new javax.swing.JButton();
        jButtonVerseNumberHighlightColor = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator5 = new javax.swing.JToolBar.Separator();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jComboBoxVerseNumberFontSize = new javax.swing.JComboBox();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator16 = new javax.swing.JToolBar.Separator();
        jToggleButtonVerseNumberSuperscript = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberSubscript = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberVisibility = new javax.swing.JToggleButton();
        jPanelVerseText = new javax.swing.JPanel();
        jToolBar4 = new javax.swing.JToolBar();
        jToggleButtonVerseTextBold = new javax.swing.JToggleButton();
        jToggleButtonVerseTextItalic = new javax.swing.JToggleButton();
        jToggleButtonVerseTextUnderline = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButtonVerseTextTextColor = new javax.swing.JButton();
        jButtonVerseTextHighlightColor = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator6 = new javax.swing.JToolBar.Separator();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jComboBoxVerseTextFontSize = new javax.swing.JComboBox();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        jSeparator17 = new javax.swing.JToolBar.Separator();
        jCheckBoxUseVersionFormatting = new javax.swing.JCheckBox();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        jInternalFrame1 = new JInternalFrame() {
            @Override
            public void setUI(InternalFrameUI ui) {
                super.setUI(ui); // this gets called internally when updating the ui and makes the northPane reappear
                BasicInternalFrameUI frameUI = (BasicInternalFrameUI) getUI(); // so...
                if (frameUI != null) frameUI.setNorthPane(null); // lets get rid of it
            }
        };

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(__("User Preferences"));
        setBounds(new java.awt.Rectangle(650, 500, 1300, 1000));
        setIconImages(setIconImages());
        setMinimumSize(new java.awt.Dimension(900, 700));
        setName("myoptions"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1300, 850));
        setResizable(false);
        setSize(new java.awt.Dimension(1300, 1000));

        jPanelParagraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Paragraph"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanelParagraph.setName("Paragraph"); // NOI18N
        jPanelParagraph.setPreferredSize(new java.awt.Dimension(774, 90));
        jPanelParagraph.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jPanelParagraphAlignment.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Alignment")));

        jToolBarParagraphAlignment.setFloatable(false);
        jToolBarParagraphAlignment.setRollover(true);

        buttonGroupParagraphAlign.add(jToggleButtonParagraphLeft);
        jToggleButtonParagraphLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_left.png"))); // NOI18N
        jToggleButtonParagraphLeft.setSelected(USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.equals(BGET.ALIGN.LEFT));
        jToggleButtonParagraphLeft.setFocusable(false);
        jToggleButtonParagraphLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonParagraphLeft.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonParagraphLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonParagraphLeft.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonParagraphLeftItemStateChanged(evt);
            }
        });
        jToolBarParagraphAlignment.add(jToggleButtonParagraphLeft);
        jToggleButtonParagraphLeft.getAccessibleContext().setAccessibleParent(jPanelParagraphAlignment);

        buttonGroupParagraphAlign.add(jToggleButtonParagraphCenter);
        jToggleButtonParagraphCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_center.png"))); // NOI18N
        jToggleButtonParagraphCenter.setSelected(USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.equals(BGET.ALIGN.CENTER));
        jToggleButtonParagraphCenter.setFocusable(false);
        jToggleButtonParagraphCenter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonParagraphCenter.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonParagraphCenter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonParagraphCenter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonParagraphCenterItemStateChanged(evt);
            }
        });
        jToolBarParagraphAlignment.add(jToggleButtonParagraphCenter);
        jToggleButtonParagraphCenter.getAccessibleContext().setAccessibleParent(jPanelParagraphAlignment);

        buttonGroupParagraphAlign.add(jToggleButtonParagraphRight);
        jToggleButtonParagraphRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_right.png"))); // NOI18N
        jToggleButtonParagraphRight.setSelected(USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.equals(BGET.ALIGN.RIGHT));
        jToggleButtonParagraphRight.setFocusable(false);
        jToggleButtonParagraphRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonParagraphRight.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonParagraphRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonParagraphRight.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonParagraphRightItemStateChanged(evt);
            }
        });
        jToolBarParagraphAlignment.add(jToggleButtonParagraphRight);
        jToggleButtonParagraphRight.getAccessibleContext().setAccessibleParent(jPanelParagraphAlignment);

        buttonGroupParagraphAlign.add(jToggleButtonParagraphJustify);
        jToggleButtonParagraphJustify.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_justify.png"))); // NOI18N
        jToggleButtonParagraphJustify.setSelected(USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.equals(BGET.ALIGN.JUSTIFY));
        jToggleButtonParagraphJustify.setFocusable(false);
        jToggleButtonParagraphJustify.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonParagraphJustify.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonParagraphJustify.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonParagraphJustify.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonParagraphJustifyItemStateChanged(evt);
            }
        });
        jToolBarParagraphAlignment.add(jToggleButtonParagraphJustify);
        jToggleButtonParagraphJustify.getAccessibleContext().setAccessibleParent(jPanelParagraphAlignment);

        javax.swing.GroupLayout jPanelParagraphAlignmentLayout = new javax.swing.GroupLayout(jPanelParagraphAlignment);
        jPanelParagraphAlignment.setLayout(jPanelParagraphAlignmentLayout);
        jPanelParagraphAlignmentLayout.setHorizontalGroup(
            jPanelParagraphAlignmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarParagraphAlignment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanelParagraphAlignmentLayout.setVerticalGroup(
            jPanelParagraphAlignmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarParagraphAlignment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanelParagraph.add(jPanelParagraphAlignment);

        jPanelParagraphIndentLeft.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Left Indent")));

        jToolBarParagraphIndentLeft.setFloatable(false);
        jToolBarParagraphIndentLeft.setRollover(true);

        jButtonLeftIndentMore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/increase_indent.png"))); // NOI18N
        jButtonLeftIndentMore.setFocusable(false);
        jButtonLeftIndentMore.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonLeftIndentMore.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonLeftIndentMore.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonLeftIndentMore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonLeftIndentMoreMouseClicked(evt);
            }
        });
        jToolBarParagraphIndentLeft.add(jButtonLeftIndentMore);

        jButtonLeftIndentLess.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/decrease_indent.png"))); // NOI18N
        jButtonLeftIndentLess.setFocusable(false);
        jButtonLeftIndentLess.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonLeftIndentLess.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonLeftIndentLess.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonLeftIndentLess.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonLeftIndentLessMouseClicked(evt);
            }
        });
        jToolBarParagraphIndentLeft.add(jButtonLeftIndentLess);

        jLabelLeftIndent.setText(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT + BibleGetIO.measureUnit.name());
        jToolBarParagraphIndentLeft.add(jLabelLeftIndent);

        javax.swing.GroupLayout jPanelParagraphIndentLeftLayout = new javax.swing.GroupLayout(jPanelParagraphIndentLeft);
        jPanelParagraphIndentLeft.setLayout(jPanelParagraphIndentLeftLayout);
        jPanelParagraphIndentLeftLayout.setHorizontalGroup(
            jPanelParagraphIndentLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarParagraphIndentLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanelParagraphIndentLeftLayout.setVerticalGroup(
            jPanelParagraphIndentLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanelParagraphIndentLeftLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToolBarParagraphIndentLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jPanelParagraph.add(jPanelParagraphIndentLeft);

        jPanelParagraphIndentRight.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Right Indent")));

        jToolBarParagraphIndentRight.setFloatable(false);
        jToolBarParagraphIndentRight.setRollover(true);

        jButtonRightIndentMore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/increase_indent_right.png"))); // NOI18N
        jButtonRightIndentMore.setFocusable(false);
        jButtonRightIndentMore.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRightIndentMore.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonRightIndentMore.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRightIndentMore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonRightIndentMoreMouseClicked(evt);
            }
        });
        jToolBarParagraphIndentRight.add(jButtonRightIndentMore);

        jButtonRightIndentLess.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/decrease_indent_right.png"))); // NOI18N
        jButtonRightIndentLess.setFocusable(false);
        jButtonRightIndentLess.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRightIndentLess.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonRightIndentLess.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRightIndentLess.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonRightIndentLessMouseClicked(evt);
            }
        });
        jToolBarParagraphIndentRight.add(jButtonRightIndentLess);

        jLabelRightIndent.setText(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT + BibleGetIO.measureUnit.name());
        jToolBarParagraphIndentRight.add(jLabelRightIndent);

        javax.swing.GroupLayout jPanelParagraphIndentRightLayout = new javax.swing.GroupLayout(jPanelParagraphIndentRight);
        jPanelParagraphIndentRight.setLayout(jPanelParagraphIndentRightLayout);
        jPanelParagraphIndentRightLayout.setHorizontalGroup(
            jPanelParagraphIndentRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarParagraphIndentRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanelParagraphIndentRightLayout.setVerticalGroup(
            jPanelParagraphIndentRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarParagraphIndentRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanelParagraph.add(jPanelParagraphIndentRight);

        jPanelParagraphLineHeight.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Line-spacing")));

        jToolBarParagraphLineHeight.setFloatable(false);
        jToolBarParagraphLineHeight.setRollover(true);

        jComboBoxParagraphLineHeight.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "1½", "2" }));
        jComboBoxParagraphLineHeight.setSelectedIndex(getParaLineSpaceVal());
        jComboBoxParagraphLineHeight.setPreferredSize(new java.awt.Dimension(62, 41));
        jComboBoxParagraphLineHeight.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxParagraphLineHeightItemStateChanged(evt);
            }
        });
        jComboBoxParagraphLineHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxParagraphLineHeightActionPerformed(evt);
            }
        });
        jToolBarParagraphLineHeight.add(jComboBoxParagraphLineHeight);

        javax.swing.GroupLayout jPanelParagraphLineHeightLayout = new javax.swing.GroupLayout(jPanelParagraphLineHeight);
        jPanelParagraphLineHeight.setLayout(jPanelParagraphLineHeightLayout);
        jPanelParagraphLineHeightLayout.setHorizontalGroup(
            jPanelParagraphLineHeightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParagraphLineHeightLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToolBarParagraphLineHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelParagraphLineHeightLayout.setVerticalGroup(
            jPanelParagraphLineHeightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParagraphLineHeightLayout.createSequentialGroup()
                .addComponent(jToolBarParagraphLineHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jPanelParagraph.add(jPanelParagraphLineHeight);

        jPanelParagraphFontFamily.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Font")));

        jToolBarParagraphFontFamily.setFloatable(false);
        jToolBarParagraphFontFamily.setRollover(true);

        jComboBoxParagraphFontFamily.setFont(new java.awt.Font(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY, java.awt.Font.PLAIN, 18));
        jComboBoxParagraphFontFamily.setMaximumRowCount(6);
        jComboBoxParagraphFontFamily.setModel(fontFamilies);
        jComboBoxParagraphFontFamily.setSelectedItem(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY);
        jComboBoxParagraphFontFamily.setPreferredSize(new java.awt.Dimension(300, 41));
        jComboBoxParagraphFontFamily.setPrototypeDisplayValue(fontFamilies);
        jComboBoxParagraphFontFamily.setRenderer(FFLCRenderer);
        jComboBoxParagraphFontFamily.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxParagraphFontFamilyItemStateChanged(evt);
            }
        });
        jToolBarParagraphFontFamily.add(jComboBoxParagraphFontFamily);

        javax.swing.GroupLayout jPanelParagraphFontFamilyLayout = new javax.swing.GroupLayout(jPanelParagraphFontFamily);
        jPanelParagraphFontFamily.setLayout(jPanelParagraphFontFamilyLayout);
        jPanelParagraphFontFamilyLayout.setHorizontalGroup(
            jPanelParagraphFontFamilyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParagraphFontFamilyLayout.createSequentialGroup()
                .addComponent(jToolBarParagraphFontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanelParagraphFontFamilyLayout.setVerticalGroup(
            jPanelParagraphFontFamilyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanelParagraphFontFamilyLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToolBarParagraphFontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelParagraph.add(jPanelParagraphFontFamily);

        getContentPane().add(jPanelParagraph, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jPanelBibleVersion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Bible Version"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanelBibleVersion.setMinimumSize(new java.awt.Dimension(625, 35));
        jPanelBibleVersion.setPreferredSize(new java.awt.Dimension(910, 35));
        jPanelBibleVersion.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jToolBar8.setFloatable(false);
        jToolBar8.setRollover(true);

        jToggleButtonBibleVersionBold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButtonBibleVersionBold.setSelected(USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        jToggleButtonBibleVersionBold.setFocusable(false);
        jToggleButtonBibleVersionBold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionBold.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonBibleVersionBold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionBold.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionBoldItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionBold);

        jToggleButtonBibleVersionItalic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButtonBibleVersionItalic.setSelected(USERPREFS.BOOKCHAPTERSTYLES_ITALIC);
        jToggleButtonBibleVersionItalic.setFocusable(false);
        jToggleButtonBibleVersionItalic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionItalic.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonBibleVersionItalic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionItalic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionItalicItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionItalic);

        jToggleButtonBibleVersionUnderline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButtonBibleVersionUnderline.setSelected(USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE);
        jToggleButtonBibleVersionUnderline.setFocusable(false);
        jToggleButtonBibleVersionUnderline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionUnderline.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonBibleVersionUnderline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionUnderline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionUnderlineItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionUnderline);
        jToolBar8.add(jSeparator8);

        jButtonBibleVersionTextColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButtonBibleVersionTextColor.setFocusable(false);
        jButtonBibleVersionTextColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonBibleVersionTextColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonBibleVersionTextColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonBibleVersionTextColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonBibleVersionTextColorMouseClicked(evt);
            }
        });
        jToolBar8.add(jButtonBibleVersionTextColor);

        jButtonBibleVersionHighlightColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButtonBibleVersionHighlightColor.setFocusable(false);
        jButtonBibleVersionHighlightColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonBibleVersionHighlightColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonBibleVersionHighlightColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonBibleVersionHighlightColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonBibleVersionHighlightColorMouseClicked(evt);
            }
        });
        jToolBar8.add(jButtonBibleVersionHighlightColor);
        jToolBar8.add(filler4);
        jToolBar8.add(jSeparator9);
        jToolBar8.add(filler12);

        jComboBoxBibleVersionFontSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBoxBibleVersionFontSize.setSelectedItem(""+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE+"");
        jComboBoxBibleVersionFontSize.setToolTipText("Book / Chapter font size");
        jComboBoxBibleVersionFontSize.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBoxBibleVersionFontSize.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBoxBibleVersionFontSize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxBibleVersionFontSizeItemStateChanged(evt);
            }
        });
        jToolBar8.add(jComboBoxBibleVersionFontSize);
        jToolBar8.add(filler7);
        jToolBar8.add(jSeparator12);

        buttonGroupBibleVersionAlign.add(jToggleButtonBibleVersionAlignLeft);
        jToggleButtonBibleVersionAlignLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_left.png"))); // NOI18N
        jToggleButtonBibleVersionAlignLeft.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT==BGET.ALIGN.LEFT);
        jToggleButtonBibleVersionAlignLeft.setFocusable(false);
        jToggleButtonBibleVersionAlignLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionAlignLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionAlignLeft.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionAlignLeftItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionAlignLeft);

        buttonGroupBibleVersionAlign.add(jToggleButtonBibleVersionAlignCenter);
        jToggleButtonBibleVersionAlignCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_center.png"))); // NOI18N
        jToggleButtonBibleVersionAlignCenter.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT==BGET.ALIGN.CENTER);
        jToggleButtonBibleVersionAlignCenter.setFocusable(false);
        jToggleButtonBibleVersionAlignCenter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionAlignCenter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionAlignCenter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionAlignCenterItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionAlignCenter);

        buttonGroupBibleVersionAlign.add(jToggleButtonBibleVersionAlignRight);
        jToggleButtonBibleVersionAlignRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_right.png"))); // NOI18N
        jToggleButtonBibleVersionAlignRight.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT==BGET.ALIGN.RIGHT);
        jToggleButtonBibleVersionAlignRight.setFocusable(false);
        jToggleButtonBibleVersionAlignRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionAlignRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionAlignRight.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionAlignRightItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionAlignRight);
        jToolBar8.add(jSeparator10);

        buttonGroupBibleVersionWrap.add(jToggleButtonBibleVersionWrapNone);
        jToggleButtonBibleVersionWrapNone.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jToggleButtonBibleVersionWrapNone.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP==BGET.WRAP.NONE);
        jToggleButtonBibleVersionWrapNone.setText("NONE");
        jToggleButtonBibleVersionWrapNone.setFocusable(false);
        jToggleButtonBibleVersionWrapNone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionWrapNone.setMaximumSize(new java.awt.Dimension(55, 31));
        jToggleButtonBibleVersionWrapNone.setMinimumSize(new java.awt.Dimension(55, 31));
        jToggleButtonBibleVersionWrapNone.setPreferredSize(new java.awt.Dimension(55, 31));
        jToggleButtonBibleVersionWrapNone.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionWrapNoneItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionWrapNone);

        buttonGroupBibleVersionWrap.add(jToggleButtonBibleVersionWrapParentheses);
        jToggleButtonBibleVersionWrapParentheses.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBibleVersionWrapParentheses.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP==BGET.WRAP.PARENTHESES);
        jToggleButtonBibleVersionWrapParentheses.setText("()");
        jToggleButtonBibleVersionWrapParentheses.setFocusable(false);
        jToggleButtonBibleVersionWrapParentheses.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionWrapParentheses.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionWrapParentheses.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionWrapParentheses.setPreferredSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionWrapParentheses.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionWrapParenthesesItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionWrapParentheses);

        buttonGroupBibleVersionWrap.add(jToggleButtonBibleVersionWrapBrackets);
        jToggleButtonBibleVersionWrapBrackets.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBibleVersionWrapBrackets.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP==BGET.WRAP.BRACKETS);
        jToggleButtonBibleVersionWrapBrackets.setText("[]");
        jToggleButtonBibleVersionWrapBrackets.setFocusable(false);
        jToggleButtonBibleVersionWrapBrackets.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionWrapBrackets.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionWrapBrackets.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionWrapBrackets.setPreferredSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionWrapBrackets.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionWrapBracketsItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionWrapBrackets);
        jToolBar8.add(jSeparator11);

        buttonGroupBibleVersionVAlign.add(jToggleButtonBibleVersionVAlignTop);
        jToggleButtonBibleVersionVAlignTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_above.png"))); // NOI18N
        jToggleButtonBibleVersionVAlignTop.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION==BGET.POS.TOP);
        jToggleButtonBibleVersionVAlignTop.setFocusable(false);
        jToggleButtonBibleVersionVAlignTop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionVAlignTop.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignTop.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignTop.setPreferredSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignTop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionVAlignTop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionVAlignTopItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionVAlignTop);

        buttonGroupBibleVersionVAlign.add(jToggleButtonBibleVersionVAlignBottom);
        jToggleButtonBibleVersionVAlignBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_below.png"))); // NOI18N
        jToggleButtonBibleVersionVAlignBottom.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION==BGET.POS.BOTTOM);
        jToggleButtonBibleVersionVAlignBottom.setFocusable(false);
        jToggleButtonBibleVersionVAlignBottom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionVAlignBottom.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignBottom.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignBottom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionVAlignBottom.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionVAlignBottomItemStateChanged(evt);
            }
        });
        jToolBar8.add(jToggleButtonBibleVersionVAlignBottom);

        jPanelBibleVersion.add(jToolBar8);

        jToggleButtonBibleVersionVisibility.setIcon(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW==BGET.VISIBILITY.SHOW?buttonStateOn:buttonStateOff);
        jToggleButtonBibleVersionVisibility.setSelected(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW==BGET.VISIBILITY.SHOW);
        jToggleButtonBibleVersionVisibility.setText(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW==BGET.VISIBILITY.SHOW?"SHOW":"HIDE");
        jToggleButtonBibleVersionVisibility.setBorderPainted(false);
        jToggleButtonBibleVersionVisibility.setContentAreaFilled(false);
        jToggleButtonBibleVersionVisibility.setFocusable(false);
        jToggleButtonBibleVersionVisibility.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionVisibility.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBibleVersionVisibility.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBibleVersionVisibilityItemStateChanged(evt);
            }
        });
        jToggleButtonBibleVersionVisibility.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggleButtonBibleVersionVisibilityMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggleButtonBibleVersionVisibilityMouseExited(evt);
            }
        });
        jToggleButtonBibleVersionVisibility.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonBibleVersionVisibilityActionPerformed(evt);
            }
        });
        jPanelBibleVersion.add(jToggleButtonBibleVersionVisibility);

        jPanel3.add(jPanelBibleVersion);

        jPanelBookChapter.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Book / Chapter"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanelBookChapter.setMinimumSize(new java.awt.Dimension(764, 69));
        jPanelBookChapter.setPreferredSize(new java.awt.Dimension(910, 85));
        jPanelBookChapter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jToggleButtonBookChapterBold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButtonBookChapterBold.setSelected(USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        jToggleButtonBookChapterBold.setFocusable(false);
        jToggleButtonBookChapterBold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterBold.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonBookChapterBold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterBold.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterBoldItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterBold);

        jToggleButtonBookChapterItalic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButtonBookChapterItalic.setSelected(USERPREFS.BOOKCHAPTERSTYLES_ITALIC);
        jToggleButtonBookChapterItalic.setFocusable(false);
        jToggleButtonBookChapterItalic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterItalic.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonBookChapterItalic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterItalic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterItalicItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterItalic);

        jToggleButtonBookChapterUnderline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButtonBookChapterUnderline.setSelected(USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE);
        jToggleButtonBookChapterUnderline.setFocusable(false);
        jToggleButtonBookChapterUnderline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterUnderline.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonBookChapterUnderline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterUnderline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterUnderlineItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterUnderline);
        jToolBar2.add(jSeparator1);

        jButtonBookChapterTextColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButtonBookChapterTextColor.setFocusable(false);
        jButtonBookChapterTextColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonBookChapterTextColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonBookChapterTextColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonBookChapterTextColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonBookChapterTextColorMouseClicked(evt);
            }
        });
        jToolBar2.add(jButtonBookChapterTextColor);

        jButtonBookChapterHighlightColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButtonBookChapterHighlightColor.setFocusable(false);
        jButtonBookChapterHighlightColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonBookChapterHighlightColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonBookChapterHighlightColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonBookChapterHighlightColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonBookChapterHighlightColorMouseClicked(evt);
            }
        });
        jToolBar2.add(jButtonBookChapterHighlightColor);
        jToolBar2.add(filler3);
        jToolBar2.add(jSeparator4);
        jToolBar2.add(filler11);

        jComboBoxBookChapterFontSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBoxBookChapterFontSize.setSelectedItem(""+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE+"");
        jComboBoxBookChapterFontSize.setToolTipText("Book / Chapter font size");
        jComboBoxBookChapterFontSize.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBoxBookChapterFontSize.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBoxBookChapterFontSize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxBookChapterFontSizeItemStateChanged(evt);
            }
        });
        jToolBar2.add(jComboBoxBookChapterFontSize);
        jToolBar2.add(filler5);
        jToolBar2.add(jSeparator13);

        buttonGroupBookChapterAlign.add(jToggleButtonBookChapterAlignLeft);
        jToggleButtonBookChapterAlignLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_left.png"))); // NOI18N
        jToggleButtonBookChapterAlignLeft.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT==BGET.ALIGN.LEFT);
        jToggleButtonBookChapterAlignLeft.setFocusable(false);
        jToggleButtonBookChapterAlignLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterAlignLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterAlignLeft.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterAlignLeftItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterAlignLeft);

        buttonGroupBookChapterAlign.add(jToggleButtonBookChapterAlignCenter);
        jToggleButtonBookChapterAlignCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_center.png"))); // NOI18N
        jToggleButtonBookChapterAlignCenter.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT==BGET.ALIGN.CENTER);
        jToggleButtonBookChapterAlignCenter.setFocusable(false);
        jToggleButtonBookChapterAlignCenter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterAlignCenter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterAlignCenter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterAlignCenterItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterAlignCenter);

        buttonGroupBookChapterAlign.add(jToggleButtonBookChapterAlignRight);
        jToggleButtonBookChapterAlignRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_right.png"))); // NOI18N
        jToggleButtonBookChapterAlignRight.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT==BGET.ALIGN.RIGHT);
        jToggleButtonBookChapterAlignRight.setFocusable(false);
        jToggleButtonBookChapterAlignRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterAlignRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterAlignRight.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterAlignRightItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterAlignRight);
        jToolBar2.add(jSeparator15);

        buttonGroupBookChapterWrap.add(jToggleButtonBookChapterWrapNone);
        jToggleButtonBookChapterWrapNone.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jToggleButtonBookChapterWrapNone.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP==BGET.WRAP.NONE);
        jToggleButtonBookChapterWrapNone.setText("NONE");
        jToggleButtonBookChapterWrapNone.setFocusable(false);
        jToggleButtonBookChapterWrapNone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterWrapNone.setMaximumSize(new java.awt.Dimension(55, 31));
        jToggleButtonBookChapterWrapNone.setMinimumSize(new java.awt.Dimension(55, 31));
        jToggleButtonBookChapterWrapNone.setPreferredSize(new java.awt.Dimension(55, 31));
        jToggleButtonBookChapterWrapNone.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterWrapNoneItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterWrapNone);

        buttonGroupBookChapterWrap.add(jToggleButtonBookChapterWrapParentheses);
        jToggleButtonBookChapterWrapParentheses.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBookChapterWrapParentheses.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP==BGET.WRAP.PARENTHESES);
        jToggleButtonBookChapterWrapParentheses.setText("()");
        jToggleButtonBookChapterWrapParentheses.setFocusable(false);
        jToggleButtonBookChapterWrapParentheses.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterWrapParentheses.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterWrapParentheses.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterWrapParentheses.setPreferredSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterWrapParentheses.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterWrapParenthesesItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterWrapParentheses);

        buttonGroupBookChapterWrap.add(jToggleButtonBookChapterWrapBrackets);
        jToggleButtonBookChapterWrapBrackets.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBookChapterWrapBrackets.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP==BGET.WRAP.BRACKETS);
        jToggleButtonBookChapterWrapBrackets.setText("[]");
        jToggleButtonBookChapterWrapBrackets.setFocusable(false);
        jToggleButtonBookChapterWrapBrackets.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterWrapBrackets.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterWrapBrackets.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterWrapBrackets.setPreferredSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterWrapBrackets.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterWrapBracketsItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterWrapBrackets);
        jToolBar2.add(jSeparator14);

        buttonGroupBookChapterVAlign.add(jToggleButtonBookChapterVAlignTop);
        jToggleButtonBookChapterVAlignTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_above.png"))); // NOI18N
        jToggleButtonBookChapterVAlignTop.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.TOP);
        jToggleButtonBookChapterVAlignTop.setFocusable(false);
        jToggleButtonBookChapterVAlignTop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterVAlignTop.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterVAlignTop.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterVAlignTop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterVAlignTop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterVAlignTopItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterVAlignTop);

        buttonGroupBookChapterVAlign.add(jToggleButtonBookChapterVAlignBottom);
        jToggleButtonBookChapterVAlignBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_below.png"))); // NOI18N
        jToggleButtonBookChapterVAlignBottom.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.BOTTOM);
        jToggleButtonBookChapterVAlignBottom.setFocusable(false);
        jToggleButtonBookChapterVAlignBottom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterVAlignBottom.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterVAlignBottom.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterVAlignBottom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterVAlignBottom.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterVAlignBottomItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterVAlignBottom);

        buttonGroupBookChapterVAlign.add(jToggleButtonBookChapterVAlignBottominline);
        jToggleButtonBookChapterVAlignBottominline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_inline.png"))); // NOI18N
        jToggleButtonBookChapterVAlignBottominline.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.BOTTOMINLINE);
        jToggleButtonBookChapterVAlignBottominline.setFocusable(false);
        jToggleButtonBookChapterVAlignBottominline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterVAlignBottominline.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterVAlignBottominline.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBookChapterVAlignBottominline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterVAlignBottominline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterVAlignBottominlineItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButtonBookChapterVAlignBottominline);

        jPanelBookChapter.add(jToolBar2);

        jToggleButtonBookChapterFullRef.setIcon(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY?buttonStateOn:buttonStateOff);
        jToggleButtonBookChapterFullRef.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY);
        jToggleButtonBookChapterFullRef.setBorderPainted(false);
        jToggleButtonBookChapterFullRef.setContentAreaFilled(false);
        jToggleButtonBookChapterFullRef.setFocusable(false);
        jToggleButtonBookChapterFullRef.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterFullRef.setLabel("Full Ref");
        jToggleButtonBookChapterFullRef.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBookChapterFullRef.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterFullRefItemStateChanged(evt);
            }
        });
        jToggleButtonBookChapterFullRef.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggleButtonBookChapterFullRefMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggleButtonBookChapterFullRefMouseExited(evt);
            }
        });
        jPanelBookChapter.add(jToggleButtonBookChapterFullRef);

        jPanel1.setLayout(new java.awt.GridLayout(4, 1));

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterBibleLang);
        jToggleButtonBookChapterBibleLang.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.BIBLELANG);
        jToggleButtonBookChapterBibleLang.setText("Bible Lang");
        jToggleButtonBookChapterBibleLang.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterBibleLangItemStateChanged(evt);
            }
        });
        jPanel1.add(jToggleButtonBookChapterBibleLang);

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterBibleLangAbbrev);
        jToggleButtonBookChapterBibleLangAbbrev.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.BIBLELANGABBREV);
        jToggleButtonBookChapterBibleLangAbbrev.setText("Bible Lang Abbrev");
        jToggleButtonBookChapterBibleLangAbbrev.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterBibleLangAbbrevItemStateChanged(evt);
            }
        });
        jPanel1.add(jToggleButtonBookChapterBibleLangAbbrev);

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterUserLang);
        jToggleButtonBookChapterUserLang.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.USERLANG);
        jToggleButtonBookChapterUserLang.setText("User Lang");
        jToggleButtonBookChapterUserLang.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterUserLangItemStateChanged(evt);
            }
        });
        jPanel1.add(jToggleButtonBookChapterUserLang);

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterUserLangAbbrev);
        jToggleButtonBookChapterUserLangAbbrev.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.USERLANGABBREV);
        jToggleButtonBookChapterUserLangAbbrev.setText("User Lang Abbrev");
        jToggleButtonBookChapterUserLangAbbrev.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonBookChapterUserLangAbbrevItemStateChanged(evt);
            }
        });
        jPanel1.add(jToggleButtonBookChapterUserLangAbbrev);

        jPanelBookChapter.add(jPanel1);

        jPanel3.add(jPanelBookChapter);

        jPanelVerseNumber.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Verse Number"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanelVerseNumber.setMinimumSize(new java.awt.Dimension(449, 35));
        jPanelVerseNumber.setPreferredSize(new java.awt.Dimension(910, 35));
        jPanelVerseNumber.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        jToggleButtonVerseNumberBold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButtonVerseNumberBold.setSelected(USERPREFS.VERSENUMBERSTYLES_BOLD);
        jToggleButtonVerseNumberBold.setFocusable(false);
        jToggleButtonVerseNumberBold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseNumberBold.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseNumberBold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseNumberBold.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseNumberBoldItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButtonVerseNumberBold);

        jToggleButtonVerseNumberItalic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButtonVerseNumberItalic.setSelected(USERPREFS.VERSENUMBERSTYLES_ITALIC);
        jToggleButtonVerseNumberItalic.setFocusable(false);
        jToggleButtonVerseNumberItalic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseNumberItalic.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseNumberItalic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseNumberItalic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseNumberItalicItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButtonVerseNumberItalic);

        jToggleButtonVerseNumberUnderline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButtonVerseNumberUnderline.setSelected(USERPREFS.VERSENUMBERSTYLES_UNDERLINE);
        jToggleButtonVerseNumberUnderline.setFocusable(false);
        jToggleButtonVerseNumberUnderline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseNumberUnderline.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseNumberUnderline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseNumberUnderline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseNumberUnderlineItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButtonVerseNumberUnderline);
        jToolBar3.add(jSeparator2);

        jButtonVerseNumberTextColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButtonVerseNumberTextColor.setFocusable(false);
        jButtonVerseNumberTextColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonVerseNumberTextColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonVerseNumberTextColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonVerseNumberTextColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonVerseNumberTextColorMouseClicked(evt);
            }
        });
        jToolBar3.add(jButtonVerseNumberTextColor);

        jButtonVerseNumberHighlightColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButtonVerseNumberHighlightColor.setFocusable(false);
        jButtonVerseNumberHighlightColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonVerseNumberHighlightColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonVerseNumberHighlightColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonVerseNumberHighlightColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonVerseNumberHighlightColorMouseClicked(evt);
            }
        });
        jToolBar3.add(jButtonVerseNumberHighlightColor);
        jToolBar3.add(filler2);
        jToolBar3.add(jSeparator5);
        jToolBar3.add(filler10);

        jComboBoxVerseNumberFontSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBoxVerseNumberFontSize.setSelectedItem(""+USERPREFS.VERSENUMBERSTYLES_FONTSIZE+"");
        jComboBoxVerseNumberFontSize.setToolTipText("Verse Number font size");
        jComboBoxVerseNumberFontSize.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBoxVerseNumberFontSize.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBoxVerseNumberFontSize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxVerseNumberFontSizeItemStateChanged(evt);
            }
        });
        jToolBar3.add(jComboBoxVerseNumberFontSize);
        jToolBar3.add(filler6);
        jToolBar3.add(jSeparator16);

        jToggleButtonVerseNumberSuperscript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/subscript.png"))); // NOI18N
        jToggleButtonVerseNumberSuperscript.setSelected(USERPREFS.VERSENUMBERSTYLES_VALIGN.equals(BGET.VALIGN.SUPERSCRIPT));
        jToggleButtonVerseNumberSuperscript.setFocusable(false);
        jToggleButtonVerseNumberSuperscript.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseNumberSuperscript.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseNumberSuperscript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseNumberSuperscript.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseNumberSuperscriptItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButtonVerseNumberSuperscript);

        jToggleButtonVerseNumberSubscript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/superscript.png"))); // NOI18N
        jToggleButtonVerseNumberSubscript.setSelected(USERPREFS.VERSENUMBERSTYLES_VALIGN.equals(BGET.VALIGN.SUBSCRIPT));
        jToggleButtonVerseNumberSubscript.setFocusable(false);
        jToggleButtonVerseNumberSubscript.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseNumberSubscript.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseNumberSubscript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseNumberSubscript.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseNumberSubscriptItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButtonVerseNumberSubscript);

        jPanelVerseNumber.add(jToolBar3);

        jToggleButtonVerseNumberVisibility.setIcon(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW?buttonStateOn:buttonStateOff);
        jToggleButtonVerseNumberVisibility.setSelected(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW);
        jToggleButtonVerseNumberVisibility.setText(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW?"SHOW":"HIDE");
        jToggleButtonVerseNumberVisibility.setBorderPainted(false);
        jToggleButtonVerseNumberVisibility.setContentAreaFilled(false);
        jToggleButtonVerseNumberVisibility.setFocusable(false);
        jToggleButtonVerseNumberVisibility.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseNumberVisibility.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseNumberVisibility.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseNumberVisibilityItemStateChanged(evt);
            }
        });
        jToggleButtonVerseNumberVisibility.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggleButtonVerseNumberVisibilityMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggleButtonVerseNumberVisibilityMouseExited(evt);
            }
        });
        jPanelVerseNumber.add(jToggleButtonVerseNumberVisibility);

        jPanel3.add(jPanelVerseNumber);

        jPanelVerseText.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Verse Text"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanelVerseText.setMinimumSize(new java.awt.Dimension(412, 35));
        jPanelVerseText.setPreferredSize(new java.awt.Dimension(910, 35));
        jPanelVerseText.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        jToggleButtonVerseTextBold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButtonVerseTextBold.setSelected(USERPREFS.VERSETEXTSTYLES_BOLD);
        jToggleButtonVerseTextBold.setFocusable(false);
        jToggleButtonVerseTextBold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseTextBold.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseTextBold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseTextBold.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseTextBoldItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButtonVerseTextBold);

        jToggleButtonVerseTextItalic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButtonVerseTextItalic.setSelected(USERPREFS.VERSETEXTSTYLES_ITALIC);
        jToggleButtonVerseTextItalic.setFocusable(false);
        jToggleButtonVerseTextItalic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseTextItalic.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseTextItalic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseTextItalic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseTextItalicItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButtonVerseTextItalic);

        jToggleButtonVerseTextUnderline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButtonVerseTextUnderline.setSelected(USERPREFS.VERSETEXTSTYLES_UNDERLINE);
        jToggleButtonVerseTextUnderline.setFocusable(false);
        jToggleButtonVerseTextUnderline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonVerseTextUnderline.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButtonVerseTextUnderline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonVerseTextUnderline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonVerseTextUnderlineItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButtonVerseTextUnderline);
        jToolBar4.add(jSeparator3);

        jButtonVerseTextTextColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButtonVerseTextTextColor.setFocusable(false);
        jButtonVerseTextTextColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonVerseTextTextColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonVerseTextTextColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonVerseTextTextColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonVerseTextTextColorMouseClicked(evt);
            }
        });
        jToolBar4.add(jButtonVerseTextTextColor);

        jButtonVerseTextHighlightColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButtonVerseTextHighlightColor.setFocusable(false);
        jButtonVerseTextHighlightColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonVerseTextHighlightColor.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButtonVerseTextHighlightColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonVerseTextHighlightColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonVerseTextHighlightColorMouseClicked(evt);
            }
        });
        jToolBar4.add(jButtonVerseTextHighlightColor);
        jToolBar4.add(filler1);
        jToolBar4.add(jSeparator6);
        jToolBar4.add(filler9);

        jComboBoxVerseTextFontSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBoxVerseTextFontSize.setSelectedItem(""+USERPREFS.VERSETEXTSTYLES_FONTSIZE+"");
        jComboBoxVerseTextFontSize.setToolTipText("Verse Text font size");
        jComboBoxVerseTextFontSize.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBoxVerseTextFontSize.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBoxVerseTextFontSize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxVerseTextFontSizeItemStateChanged(evt);
            }
        });
        jToolBar4.add(jComboBoxVerseTextFontSize);
        jToolBar4.add(filler8);
        jToolBar4.add(jSeparator17);

        jCheckBoxUseVersionFormatting.setSelected(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING);
        jCheckBoxUseVersionFormatting.setText(__("Override Bible Version Formatting"));
        jCheckBoxUseVersionFormatting.setFocusable(false);
        jCheckBoxUseVersionFormatting.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCheckBoxUseVersionFormatting.setMaximumSize(new java.awt.Dimension(200, 64));
        jCheckBoxUseVersionFormatting.setPreferredSize(new java.awt.Dimension(180, 36));
        jCheckBoxUseVersionFormatting.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxUseVersionFormatting.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxUseVersionFormattingItemStateChanged(evt);
            }
        });
        jCheckBoxUseVersionFormatting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseVersionFormattingActionPerformed(evt);
            }
        });
        jToolBar4.add(jCheckBoxUseVersionFormatting);
        jToolBar4.add(jSeparator7);

        jLabel2.setText("<html><head><style>body#versionformatting{border:none;background-color:rgb(214,217,223);}</style></head><body id=\"versionformatting\">"+__("Some Bible versions have their own formatting. This is left by default to keep the text as close as possible to the original. If however you need to have consistent formatting in your document, you may override the Bible version's own formatting.")+"</body></html>");
        jLabel2.setMaximumSize(new java.awt.Dimension(900, 50));
        jLabel2.setOpaque(true);
        jLabel2.setPreferredSize(new java.awt.Dimension(580, 45));
        jToolBar4.add(jLabel2);

        jPanelVerseText.add(jToolBar4);

        jPanel3.add(jPanelVerseText);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        jInternalFrame1.setBorder(null);
        jInternalFrame1.setMinimumSize(new java.awt.Dimension(800, 260));
        jInternalFrame1.setPreferredSize(new java.awt.Dimension(800, 260));
        jInternalFrame1.setVisible(true);
        getContentPane().add(jInternalFrame1, java.awt.BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButtonBibleVersionVisibilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityActionPerformed

    private void jToggleButtonBibleVersionVisibilityMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityMouseExited
        if(jToggleButtonBibleVersionVisibility.isSelected()){
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOn);
        } else {
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOff);
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityMouseExited

    private void jToggleButtonBibleVersionVisibilityMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityMouseEntered
        if(jToggleButtonBibleVersionVisibility.isSelected()){
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOnHover);
        } else {
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOffHover);
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityMouseEntered

    private void jToggleButtonBibleVersionVisibilityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW = BGET.VISIBILITY.SHOW;
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOn);
            jToggleButtonBibleVersionVisibility.setText("SHOW");
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"visibility\":\"visible\"})", browser.getURL(),0);
        }
        else {
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW = BGET.VISIBILITY.HIDE;
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOff);
            jToggleButtonBibleVersionVisibility.setText("HIDE");
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"visibility\":\"hidden\"})", browser.getURL(),0);
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityItemStateChanged

    private void jToggleButtonVerseNumberVisibilityMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberVisibilityMouseExited
        if(jToggleButtonVerseNumberVisibility.isSelected()){
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOn);
        } else {
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOff);
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberVisibilityMouseExited

    private void jToggleButtonVerseNumberVisibilityMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberVisibilityMouseEntered
        if(jToggleButtonVerseNumberVisibility.isSelected()){
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOnHover);
        } else {
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOffHover);
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberVisibilityMouseEntered

    private void jToggleButtonVerseNumberVisibilityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberVisibilityItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            // do something with object
            USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW = BGET.VISIBILITY.SHOW;
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOn);
            jToggleButtonVerseNumberVisibility.setText("SHOW");
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"visibility\":\"visible\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED) {
            // do something with object
            USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW = BGET.VISIBILITY.HIDE;
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOff);
            jToggleButtonVerseNumberVisibility.setText("HIDE");
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"visibility\":\"hidden\"})", browser.getURL(),0);
        }
        if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_SHOW", USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW.getValue())){
            //System.out.println("NOVERSIONFORMATTING was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING);
        }
        else{
            //System.out.println("Error updating NOVERSIONFORMATTING in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberVisibilityItemStateChanged

    private void jToggleButtonBookChapterFullRefMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterFullRefMouseExited
        if(jToggleButtonBookChapterFullRef.isSelected()){
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOn);
        } else {
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOff);
        }
    }//GEN-LAST:event_jToggleButtonBookChapterFullRefMouseExited

    private void jToggleButtonBookChapterFullRefMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterFullRefMouseEntered
        if(jToggleButtonBookChapterFullRef.isSelected()){
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOnHover);
        } else {
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOffHover);
        }
    }//GEN-LAST:event_jToggleButtonBookChapterFullRefMouseEntered

    private void jToggleButtonBookChapterFullRefItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterFullRefItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            // do something with object
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY = true;
            fullQuery = ",1-3";
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOn);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED) {
            // do something with object
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY = false;
            fullQuery = "";
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOff);
        }
        browser.executeJavaScript("jQuery(\".bookChapter\").text('" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "');", browser.getURL(),0);
        if(biblegetDB.setBooleanOption("LAYOUTPREFS_BOOKCHAPTER_FULLQUERY", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY)){
            //System.out.println("NOVERSIONFORMATTING was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING);
        }
        else{
            //System.out.println("Error updating NOVERSIONFORMATTING in database");
        }
    }//GEN-LAST:event_jToggleButtonBookChapterFullRefItemStateChanged

    private void jCheckBoxUseVersionFormattingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseVersionFormattingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxUseVersionFormattingActionPerformed

    private void jCheckBoxUseVersionFormattingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxUseVersionFormattingItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            // do something with object
            USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING = true;
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED) {
            // do something with object
            USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING = false;
        }
        if(biblegetDB.setBooleanOption("PARAGRAPHSTYLES_NOVERSIONFORMATTING", USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING)){
            //System.out.println("NOVERSIONFORMATTING was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING);
        }
        else{
            //System.out.println("Error updating NOVERSIONFORMATTING in database");
        }
    }//GEN-LAST:event_jCheckBoxUseVersionFormattingItemStateChanged

    private void jComboBoxVerseTextFontSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxVerseTextFontSizeItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.VERSETEXTSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            browser.executeJavaScript("jQuery(\".verseText\").css({\"font-size\":\"" + fontsize + "pt\"})", browser.getURL(),0);
            if(biblegetDB.setIntOption("VERSETEXTSTYLES_FONTSIZE", USERPREFS.VERSETEXTSTYLES_FONTSIZE)){
                //System.out.println("FONTSIZEVERSETEXT was successfully updated in database to value "+USERPREFS.VERSETEXTSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating FONTSIZEVERSETEXT in database");
            }
        }
    }//GEN-LAST:event_jComboBoxVerseTextFontSizeItemStateChanged

    private void jButtonVerseTextHighlightColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonVerseTextHighlightColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSETEXTSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSETEXTSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSETEXTSTYLES_BGCOLOR);
            browser.executeJavaScript("jQuery(\".verseText\").css({\"background-color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("VERSETEXTSTYLES_BGCOLOR", rgb)){
                //System.out.println("BGCOLORVERSETEXT was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BGCOLORVERSETEXT in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Verse Text Background Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesBGColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonVerseTextHighlightColorMouseClicked

    private void jButtonVerseTextTextColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonVerseTextTextColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSETEXTSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR);
            browser.executeJavaScript("jQuery(\".verseText\").css({\"color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("VERSETEXTSTYLES_TEXTCOLOR", rgb)){
                //System.out.println("TEXTCOLORVERSETEXT was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating TEXTCOLORVERSETEXT in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Verse Text Font Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesTextColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonVerseTextTextColorMouseClicked

    private void jToggleButtonVerseTextUnderlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseTextUnderlineItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.VERSETEXTSTYLES_UNDERLINE = true;
            browser.executeJavaScript("jQuery(\".verseText\").css({\"text-decoration\":\"underline\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSETEXTSTYLES_UNDERLINE = false;
            browser.executeJavaScript("jQuery(\".verseText\").css({\"text-decoration\":\"none\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("VERSETEXTSTYLES_UNDERLINE", USERPREFS.VERSETEXTSTYLES_UNDERLINE)){
            //System.out.println("UNDERSCOREVERSETEXT was successfully updated in database to value "+USERPREFS.VERSETEXTSTYLES_UNDERLINE);
        }
        else{
            //System.out.println("Error updating UNDERSCOREVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseTextUnderlineItemStateChanged

    private void jToggleButtonVerseTextItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseTextItalicItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.VERSETEXTSTYLES_ITALIC = true;
            browser.executeJavaScript("jQuery(\".verseText\").css({\"font-style\":\"italic\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSETEXTSTYLES_ITALIC = false;
            browser.executeJavaScript("jQuery(\".verseText\").css({\"font-style\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("VERSETEXTSTYLES_ITALIC", USERPREFS.VERSETEXTSTYLES_ITALIC)){
            //System.out.println("ITALICSVERSETEXT was successfully updated in database to value "+USERPREFS.VERSETEXTSTYLES_ITALIC);
        }
        else{
            //System.out.println("Error updating ITALICSVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseTextItalicItemStateChanged

    private void jToggleButtonVerseTextBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseTextBoldItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.VERSETEXTSTYLES_BOLD = true;
            browser.executeJavaScript("jQuery(\".verseText\").css({\"font-weight\":\"bold\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSETEXTSTYLES_BOLD = false;
            browser.executeJavaScript("jQuery(\".verseText\").css({\"font-weight\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("VERSETEXTSTYLES_BOLD", USERPREFS.VERSETEXTSTYLES_BOLD)){
            //System.out.println("BOLDVERSETEXT was successfully updated in database to value "+USERPREFS.VERSETEXTSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseTextBoldItemStateChanged

    private void jToggleButtonVerseNumberSubscriptItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberSubscriptItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButtonVerseNumberSuperscript.setSelected(false);
            USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.SUBSCRIPT;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"position\":\"relative\",\"top\":\"0.6em\"})", browser.getURL(),0);
            //System.out.println("setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.NORMAL;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"position\":\"static\"})", browser.getURL(),0);
            //System.out.println("btn14 :: setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }

        if(biblegetDB.setIntOption("VERSENUMBERSTYLES_VALIGN", USERPREFS.VERSENUMBERSTYLES_VALIGN.getValue())){
            //System.out.println("VERSENUMBERSTYLES_VALIGN was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }
        else{
            //System.out.println("Error updating VERSENUMBERSTYLES_VALIGN in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberSubscriptItemStateChanged

    private void jToggleButtonVerseNumberSuperscriptItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberSuperscriptItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButtonVerseNumberSubscript.setSelected(false);
            USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.SUPERSCRIPT;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"position\":\"relative\",\"top\":\"-0.6em\"})", browser.getURL(),0);
            //System.out.println("setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.NORMAL;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"position\":\"static\"})", browser.getURL(),0);
            //System.out.println("setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }

        if(biblegetDB.setIntOption("VERSENUMBERSTYLES_VALIGN", USERPREFS.VERSENUMBERSTYLES_VALIGN.getValue())){
            //System.out.println("VERSENUMBERSTYLES_VALIGN was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }
        else{
            //System.out.println("Error updating VERSENUMBERSTYLES_VALIGN in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberSuperscriptItemStateChanged

    private void jComboBoxVerseNumberFontSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxVerseNumberFontSizeItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.VERSENUMBERSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"font-size\":\"" + fontsize + "pt\"})", browser.getURL(),0);
            if(biblegetDB.setIntOption("VERSENUMBERSTYLES_FONTSIZE", USERPREFS.VERSENUMBERSTYLES_FONTSIZE)){
                //System.out.println("VERSENUMBERSTYLES_FONTSIZE was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating VERSENUMBERSTYLES_FONTSIZE in database");
            }
        }
    }//GEN-LAST:event_jComboBoxVerseNumberFontSizeItemStateChanged

    private void jButtonVerseNumberHighlightColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonVerseNumberHighlightColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSENUMBERSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSENUMBERSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSENUMBERSTYLES_BGCOLOR);
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"background-color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("VERSENUMBERSTYLES_BGCOLOR", rgb)){
                //System.out.println("BGCOLORVERSENUMBER was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BGCOLORVERSENUMBER in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Verse Number Background Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesBGColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonVerseNumberHighlightColorMouseClicked

    private void jButtonVerseNumberTextColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonVerseNumberTextColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR);
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("VERSENUMBERSTYLES_TEXTCOLOR", rgb)){
                //System.out.println("TEXTCOLORVERSENUMBER was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating TEXTCOLORVERSENUMBER in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Verse Number Font Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesTextColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonVerseNumberTextColorMouseClicked

    private void jToggleButtonVerseNumberUnderlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberUnderlineItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.VERSENUMBERSTYLES_UNDERLINE = true;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"text-decoration\":\"underline\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_UNDERLINE = false;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"text-decoration\":\"none\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("VERSENUMBERSRTYLES_UNDERLINE", USERPREFS.VERSENUMBERSTYLES_UNDERLINE)){
            //System.out.println("UNDERSCOREVERSENUMBER was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_UNDERLINE);
        }
        else{
            //System.out.println("Error updating UNDERSCOREVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberUnderlineItemStateChanged

    private void jToggleButtonVerseNumberItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberItalicItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.VERSENUMBERSTYLES_ITALIC = true;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"font-style\":\"italic\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_ITALIC = false;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"font-style\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("VERSENUMBERSTYLES_ITALIC", USERPREFS.VERSENUMBERSTYLES_ITALIC)){
            //System.out.println("ITALICSVERSENUMBER was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_ITALIC);
        }
        else{
            //System.out.println("Error updating ITALICSVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberItalicItemStateChanged

    private void jToggleButtonVerseNumberBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonVerseNumberBoldItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.VERSENUMBERSTYLES_BOLD = true;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"font-weight\":\"bold\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_BOLD = false;
            browser.executeJavaScript("jQuery(\".verseNum\").css({\"font-weight\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("VERSENUMBERSTYLES_BOLD", USERPREFS.VERSENUMBERSTYLES_BOLD)){
            //System.out.println("BOLDVERSENUMBER was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberBoldItemStateChanged

    private void jComboBoxBookChapterFontSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxBookChapterFontSizeItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"font-size\":\"" + fontsize + "pt\"})", browser.getURL(),0);
            if(biblegetDB.setIntOption("BOOKCHAPTERSTYLES_FONTSIZE", USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE)){
                //System.out.println("FONTSIZEBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating FONTSIZEBOOKCHAPTER in database");
            }
        }
    }//GEN-LAST:event_jComboBoxBookChapterFontSizeItemStateChanged

    private void jButtonBookChapterHighlightColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBookChapterHighlightColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR);
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"background-color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("BOOKCHAPTERSTYLES_BGCOLOR", rgb)){
                //System.out.println("BOOKCHAPTERSTYLES_BGCOLOR was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BOOKCHAPTERSTYLES_BGCOLOR in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Book / Chapter Background Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesBGColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonBookChapterHighlightColorMouseClicked

    private void jButtonBookChapterTextColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBookChapterTextColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("BOOKCHAPTERSTYLES_TEXTCOLOR", rgb)){
                //System.out.println("BOOKCHAPTERSTYLES_TEXTCOLOR was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BOOKCHAPTERSTYLES_TEXTCOLOR in database");
            }
        };
        ActionListener cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Book / Chapter Font Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesTextColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonBookChapterTextColorMouseClicked

    private void jToggleButtonBookChapterUnderlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterUnderlineItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE = true;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"text-decoration\":\"underline\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE = false;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"text-decoration\":\"none\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("BOOKCHAPTERSTYLES_UNDERLINE", USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE)){
            //System.out.println("UNDERSCOREBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE);
        }
        else{
            //System.out.println("Error updating UNDERSCOREBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButtonBookChapterUnderlineItemStateChanged

    private void jToggleButtonBookChapterItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterItalicItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_ITALIC = true;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"font-style\":\"italic\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_ITALIC = false;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"font-style\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("BOOKCHAPTERSTYLES_ITALIC", USERPREFS.BOOKCHAPTERSTYLES_ITALIC)){
            //System.out.println("ITALICSBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_ITALIC);
        }
        else{
            //System.out.println("Error updating ITALICSBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButtonBookChapterItalicItemStateChanged

    private void jToggleButtonBookChapterBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterBoldItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_BOLD = true;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"font-weight\":\"bold\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_BOLD = false;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"font-weight\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("BOOKCHAPTERSTYLES_BOLD", USERPREFS.BOOKCHAPTERSTYLES_BOLD)){
            //System.out.println("BOLDBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButtonBookChapterBoldItemStateChanged

    private void jComboBoxBibleVersionFontSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxBibleVersionFontSizeItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"font-size\":\"" + fontsize + "pt\"})", browser.getURL(),0);
            if(biblegetDB.setIntOption("BIBLEVERSIONSTYLES_FONTSIZE", USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE)){
                //System.out.println("BIBLEVERSIONSTYLES_FONTSIZE was successfully updated in database to value "+USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating BIBLEVERSIONSTYLES_FONTSIZE in database");
            }
        }
    }//GEN-LAST:event_jComboBoxBibleVersionFontSizeItemStateChanged

    private void jButtonBibleVersionHighlightColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBibleVersionHighlightColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR);
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"background-color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("BIBLEVERSIONSTYLES_BGCOLOR", rgb)){
                //System.out.println("BIBLEVERSIONSTYLES_BGCOLOR was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BIBLEVERSIONSTYLES_BGCOLOR in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Bible Version Background Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesBGColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonBibleVersionHighlightColorMouseClicked

    private void jButtonBibleVersionTextColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBibleVersionTextColorMouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR);
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"color\":\"" + rgb + "\"})", browser.getURL(),0);
            if(biblegetDB.setStringOption("BIBLEVERSIONSTYLES_TEXTCOLOR", rgb)){
                //System.out.println("BIBLEVERSIONSTYLES_TEXTCOLOR was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BIBLEVERSIONSTYLES_TEXTCOLOR in database");
            }
        };
        ActionListener cancelActionListener;
        cancelActionListener = (ActionEvent actionEvent) -> {
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Bible Version Text Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesTextColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButtonBibleVersionTextColorMouseClicked

    private void jToggleButtonBibleVersionUnderlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionUnderlineItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE = true;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"text-decoration\":\"underline\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE = false;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"text-decoration\":\"none\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("BIBLEVERSIONSTYLES_UNDERLINE", USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE)){
            //System.out.println("BIBLEVERSIONSTYLES_UNDERLINE was successfully updated in database to value "+USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE);
        }
        else{
            //System.out.println("Error updating BIBLEVERSIONSTYLES_UNDERLINE in database");
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionUnderlineItemStateChanged

    private void jToggleButtonBibleVersionItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionItalicItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BIBLEVERSIONSTYLES_ITALIC = true;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"font-style\":\"italic\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BIBLEVERSIONSTYLES_ITALIC = false;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"font-style\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("BIBLEVERSIONSTYLES_ITALIC", USERPREFS.BIBLEVERSIONSTYLES_ITALIC)){
            //System.out.println("BOLDBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionItalicItemStateChanged

    private void jToggleButtonBibleVersionBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionBoldItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BIBLEVERSIONSTYLES_BOLD = true;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"font-weight\":\"bold\"})", browser.getURL(),0);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BIBLEVERSIONSTYLES_BOLD = false;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"font-weight\":\"normal\"})", browser.getURL(),0);
        }

        if(biblegetDB.setBooleanOption("BIBLEVERSIONSTYLES_BOLD", USERPREFS.BIBLEVERSIONSTYLES_BOLD)){
            //System.out.println("BOLDBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionBoldItemStateChanged

    private void jButtonRightIndentLessMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRightIndentLessMouseClicked
        boolean redraw = false;
        switch(BibleGetIO.measureUnit){
            case MM:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT -= stepIndentMM; redraw = true; }
                break;
            case CM:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT -= stepIndentCM; redraw = true; }
                break;
            case INCH:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT -= stepIndentINCH; redraw = true; }
                break;
            case POINT:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT -= stepIndentPoints; redraw = true; }
                break;
            case PICA:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT -= stepIndentPicas; redraw = true; }
                break;        
        }
        
        if( redraw ){

            jLabelRightIndent.setText(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT + BibleGetIO.measureUnit.name());
            String jScript = "rulerLength = "+rulerLength+";"
            + "pixelRatioVals = getPixelRatioVals(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ");"
            + "leftindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "rightindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "bestWidth = rulerLength * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + ");";
            browser.executeJavaScript(jScript, browser.getURL(),0);

            if(biblegetDB.setDoubleOption("PARAGRAPHSTYLES_RIGHTINDENT", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)){
                //System.out.println("PARAGRAPHSTYLES_RIGHTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_RIGHTINDENT in database");
            }
        }
    }//GEN-LAST:event_jButtonRightIndentLessMouseClicked

    private void jButtonRightIndentMoreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRightIndentMoreMouseClicked
        boolean redraw = false;
        switch(BibleGetIO.measureUnit){
            case MM:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < maxIndentMM ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT += stepIndentMM; redraw = true; }
                break;
            case CM:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < maxIndentCM ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT += stepIndentCM; redraw = true; }
                break;
            case INCH:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < maxIndentINCH ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT += stepIndentINCH; redraw = true; }
                break;
            case POINT:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < maxIndentPoints ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT += stepIndentPoints; redraw = true; }
                break;
            case PICA:
                if( USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < maxIndentPicas ){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT += stepIndentPicas; redraw = true; }
                break;        
        }
        
        if( redraw ){

            jLabelRightIndent.setText(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT + BibleGetIO.measureUnit.name());
            String jScript = "rulerLength = "+rulerLength+";"
            + "pixelRatioVals = getPixelRatioVals(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ");"
            + "leftindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "rightindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "bestWidth = rulerLength * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + ");";
            browser.executeJavaScript(jScript, browser.getURL(),0);

            if(biblegetDB.setDoubleOption("PARAGRAPHSTYLES_RIGHTINDENT", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)){
                //System.out.println("PARAGRAPHSTYLES_RIGHTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_RIGHTINDENT in database");
            }
        }
    }//GEN-LAST:event_jButtonRightIndentMoreMouseClicked

    private void jComboBoxParagraphFontFamilyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxParagraphFontFamilyItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            USERPREFS.PARAGRAPHSTYLES_FONTFAMILY = item.toString();
            if(biblegetDB.setStringOption("PARAGRAPHSTYLES_FONTFAMILY", USERPREFS.PARAGRAPHSTYLES_FONTFAMILY)){
                //System.out.println("PARAGRAPHFONTFAMILY was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY);
            }
            else{
                //System.out.println("Error updating PARAGRAPHFONTFAMILY in database");
            }
        }
        browser.executeJavaScript("jQuery(\"div.results\").css({\"font-family\":\"" + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "\"}) ", browser.getURL(),0);
    }//GEN-LAST:event_jComboBoxParagraphFontFamilyItemStateChanged

    private void jComboBoxParagraphLineHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxParagraphLineHeightActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxParagraphLineHeightActionPerformed

    private void jComboBoxParagraphLineHeightItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxParagraphLineHeightItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            //Object item = evt.getItem();
            int selecIdx = jComboBoxParagraphLineHeight.getSelectedIndex();
            //System.out.println("selected index: "+selecIdx);
            switch(selecIdx)
            {
                case 0: USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = 100; break;
                case 1: USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = 150; break;
                case 2: USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = 200; break;
            }
            // do something with object

            Double lineHeight = Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT) / 100;
            //"div.results .versesParagraph { line-height: " + String.format(Locale.ROOT, "%.1f", lineHeight) + "em; }"
            browser.executeJavaScript("jQuery(\"div.results .versesParagraph\").css({\"line-height\":\"" + String.format(Locale.ROOT, "%.1f", lineHeight) + "em\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_LINEHEIGHT", USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT)){
                //System.out.println("PARAGRAPHLINESPACING was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHLINESPACING in database");
            }
        }
    }//GEN-LAST:event_jComboBoxParagraphLineHeightItemStateChanged

    private void jButtonLeftIndentLessMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonLeftIndentLessMouseClicked
        boolean redraw = false;
        switch(BibleGetIO.measureUnit){
            case MM:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= stepIndentMM; redraw = true; }
                break;
            case CM:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= stepIndentCM; redraw = true; }
                break;
            case INCH:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= stepIndentINCH; redraw = true; }
                break;
            case POINT:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= stepIndentPoints; redraw = true; }
                break;
            case PICA:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > 0 ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= stepIndentPicas; redraw = true; }
                break;        
        }
        
        if( redraw ){

            jLabelLeftIndent.setText(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT + BibleGetIO.measureUnit.name());
            String jScript = "rulerLength = "+rulerLength+";"
            + "pixelRatioVals = getPixelRatioVals(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ");"
            + "leftindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "rightindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "bestWidth = rulerLength * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + ");";
            browser.executeJavaScript(jScript, browser.getURL(),0);
            
            if(biblegetDB.setDoubleOption("PARAGRAPHSTYLES_LEFTINDENT", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)){
                //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
            }
        }
    }//GEN-LAST:event_jButtonLeftIndentLessMouseClicked

    private void jButtonLeftIndentMoreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonLeftIndentMoreMouseClicked
        boolean redraw = false;
        switch(BibleGetIO.measureUnit){
            case MM:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < maxIndentMM ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += stepIndentMM; redraw = true; }
                break;
            case CM:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < maxIndentCM ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += stepIndentCM; redraw = true; }
                break;
            case INCH:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < maxIndentINCH ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += stepIndentINCH; redraw = true; }
                break;
            case POINT:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < maxIndentPoints ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += stepIndentPoints; redraw = true; }
                break;
            case PICA:
                if( USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < maxIndentPicas ){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += stepIndentPicas; redraw = true; }
                break;        
        }

        if( redraw ){

            jLabelLeftIndent.setText(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT + BibleGetIO.measureUnit.name());
            String jScript = "rulerLength = "+rulerLength+";"
            + "pixelRatioVals = getPixelRatioVals(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ");"
            + "leftindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "rightindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + " * pixelRatioVals.dpi + 35;"
            + "bestWidth = rulerLength * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + ");";
            browser.executeJavaScript(jScript, browser.getURL(),0);
            
            if(biblegetDB.setDoubleOption("PARAGRAPHSTYLES_LEFTINDENT", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)){
                //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
            }
        }
    }//GEN-LAST:event_jButtonLeftIndentMoreMouseClicked

    private void jToggleButtonParagraphJustifyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonParagraphJustifyItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.JUSTIFY;
            browser.executeJavaScript("jQuery(\"div.results .versesParagraph\").css({\"text-align\":\"" + USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonParagraphJustifyItemStateChanged

    private void jToggleButtonParagraphRightItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonParagraphRightItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.RIGHT;
            browser.executeJavaScript("jQuery(\"div.results .versesParagraph\").css({\"text-align\":\"" + USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonParagraphRightItemStateChanged

    private void jToggleButtonParagraphCenterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonParagraphCenterItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.CENTER;
            browser.executeJavaScript("jQuery(\"div.results .versesParagraph\").css({\"text-align\":\"" + USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonParagraphCenterItemStateChanged

    private void jToggleButtonParagraphLeftItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonParagraphLeftItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.LEFT;
            browser.executeJavaScript("jQuery(\"div.results .versesParagraph\").css({\"text-align\":\"" + USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonParagraphLeftItemStateChanged

    private void jToggleButtonBibleVersionAlignLeftItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionAlignLeftItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT = BGET.ALIGN.LEFT;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"text-align\":\"" + USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_ALIGNMENT", USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_ALIGNMENT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionAlignLeftItemStateChanged

    private void jToggleButtonBibleVersionAlignCenterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionAlignCenterItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT = BGET.ALIGN.CENTER;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"text-align\":\"" + USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_ALIGNMENT", USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_ALIGNMENT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionAlignCenterItemStateChanged

    private void jToggleButtonBibleVersionAlignRightItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionAlignRightItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT = BGET.ALIGN.RIGHT;
            browser.executeJavaScript("jQuery(\".bibleVersion\").css({\"text-align\":\"" + USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_ALIGNMENT", USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_ALIGNMENT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionAlignRightItemStateChanged

    private void jToggleButtonBookChapterAlignLeftItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterAlignLeftItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT = BGET.ALIGN.LEFT;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"text-align\":\"" + USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterAlignLeftItemStateChanged

    private void jToggleButtonBookChapterAlignCenterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterAlignCenterItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT = BGET.ALIGN.CENTER;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"text-align\":\"" + USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterAlignCenterItemStateChanged

    private void jToggleButtonBookChapterAlignRightItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterAlignRightItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT = BGET.ALIGN.RIGHT;
            browser.executeJavaScript("jQuery(\".bookChapter\").css({\"text-align\":\"" + USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getCSSValue() + "\"}) ", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterAlignRightItemStateChanged

    private void jToggleButtonBibleVersionWrapNoneItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionWrapNoneItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP = BGET.WRAP.NONE;
            browser.executeJavaScript("jQuery(\".bibleVersion\").text(\"NVBSE\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_WRAP", USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_WRAP was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_WRAP in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionWrapNoneItemStateChanged

    private void jToggleButtonBibleVersionWrapParenthesesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionWrapParenthesesItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP = BGET.WRAP.PARENTHESES;
            browser.executeJavaScript("jQuery(\".bibleVersion\").text(\"(NVBSE)\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_WRAP", USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_WRAP was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_WRAP in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionWrapParenthesesItemStateChanged

    private void jToggleButtonBibleVersionWrapBracketsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionWrapBracketsItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP = BGET.WRAP.BRACKETS;
            browser.executeJavaScript("jQuery(\".bibleVersion\").text(\"[NVBSE]\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_WRAP", USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_WRAP was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_WRAP in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionWrapBracketsItemStateChanged

    private void jToggleButtonBookChapterWrapNoneItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterWrapNoneItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP = BGET.WRAP.NONE;
            bookChapterWrapBefore = "";
            bookChapterWrapAfter = "";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"" + bookChapter + fullQuery + "\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_WRAP", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_WRAP was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_WRAP in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterWrapNoneItemStateChanged

    private void jToggleButtonBookChapterWrapParenthesesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterWrapParenthesesItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP = BGET.WRAP.PARENTHESES;
            bookChapterWrapBefore = "(";
            bookChapterWrapAfter = ")";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"(" + bookChapter + fullQuery + ")\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_WRAP", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_WRAP was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_WRAP in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterWrapParenthesesItemStateChanged

    private void jToggleButtonBookChapterWrapBracketsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterWrapBracketsItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP = BGET.WRAP.BRACKETS;
            bookChapterWrapBefore = "[";
            bookChapterWrapAfter = "]";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"[" + bookChapter + fullQuery + "]\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_WRAP", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_WRAP was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_WRAP in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterWrapBracketsItemStateChanged

    private void jToggleButtonBookChapterVAlignTopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterVAlignTopItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION = BGET.POS.TOP;
            browser.executeJavaScript("jQuery(\".bookChapter\").remove();jQuery(\"<p>\",{\"class\":\"bookChapter\",\"text\":\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\"}).insertBefore(\"p.versesParagraph\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_POSITION", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_POSITION was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_POSITION in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterVAlignTopItemStateChanged

    private void jToggleButtonBookChapterVAlignBottomItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterVAlignBottomItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION = BGET.POS.BOTTOM;
            browser.executeJavaScript("jQuery(\".bookChapter\").remove();jQuery(\"<p>\",{\"class\":\"bookChapter\",\"text\":\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\"}).insertAfter(\"p.versesParagraph\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_POSITION", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_POSITION was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_POSITION in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterVAlignBottomItemStateChanged

    private void jToggleButtonBookChapterVAlignBottominlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterVAlignBottominlineItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION = BGET.POS.BOTTOMINLINE;
            browser.executeJavaScript("jQuery(\".bookChapter\").remove();jQuery(\"<span>\",{\"class\":\"bookChapter\",\"text\":\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\"}).appendTo(\"p.versesParagraph\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_POSITION", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_POSITION was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_POSITION in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterVAlignBottominlineItemStateChanged

    private void jToggleButtonBibleVersionVAlignTopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVAlignTopItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION = BGET.POS.TOP;
            browser.executeJavaScript("jQuery(\".bibleVersion\").prependTo(\".bibleQuote\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_POSITION", USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_POSITION was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_POSITION in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVAlignTopItemStateChanged

    private void jToggleButtonBibleVersionVAlignBottomItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVAlignBottomItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION = BGET.POS.BOTTOM;
            browser.executeJavaScript("jQuery(\".bibleVersion\").appendTo(\".bibleQuote\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_POSITION", USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION.getValue())){
                //System.out.println("LAYOUTPREFS_BIBLEVERSION_POSITION was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BIBLEVERSION_POSITION in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVAlignBottomItemStateChanged

    private void jToggleButtonBookChapterBibleLangItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterBibleLangItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT = BGET.FORMAT.BIBLELANG;
            bookChapter = "I Samuelis 1";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_FORMAT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_FORMAT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_FORMAT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterBibleLangItemStateChanged

    private void jToggleButtonBookChapterBibleLangAbbrevItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterBibleLangAbbrevItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT = BGET.FORMAT.BIBLELANG;
            bookChapter = "I Sam 1";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_FORMAT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_FORMAT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_FORMAT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterBibleLangAbbrevItemStateChanged

    private void jToggleButtonBookChapterUserLangItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterUserLangItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT = BGET.FORMAT.BIBLELANG;
            bookChapter = localizedBookSamuel.Fullname + " 1";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_FORMAT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_FORMAT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_FORMAT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterUserLangItemStateChanged

    private void jToggleButtonBookChapterUserLangAbbrevItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterUserLangAbbrevItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT = BGET.FORMAT.BIBLELANG;
            bookChapter = localizedBookSamuel.Abbrev + " 1";
            browser.executeJavaScript("jQuery(\".bookChapter\").text(\"" + bookChapterWrapBefore + bookChapter + fullQuery + bookChapterWrapAfter + "\")", browser.getURL(),0);
            if(biblegetDB.setIntOption("LAYOUTPREFS_BOOKCHAPTER_FORMAT", USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT.getValue())){
                //System.out.println("LAYOUTPREFS_BOOKCHAPTER_FORMAT was successfully updated in database to value "+USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT);
            }
            else{
                //System.out.println("Error updating LAYOUTPREFS_BOOKCHAPTER_FORMAT in database");
            }
        }
    }//GEN-LAST:event_jToggleButtonBookChapterUserLangAbbrevItemStateChanged

    private void jColorChooserClean(JColorChooser jColorChooser){
        AbstractColorChooserPanel panels[] = jColorChooser.getChooserPanels();
        jColorChooser.removeChooserPanel(panels[4]);
        jColorChooser.removeChooserPanel(panels[3]);
        jColorChooser.removeChooserPanel(panels[2]);
    }
        
        
    
    private List<Image> setIconImages()
    {
        List<Image> images = new ArrayList<>(4);
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/preferences-tweak-tool-icon_16x16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/preferences-tweak-tool-icon_24x24.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/preferences-tweak-tool-icon_32x32.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/preferences-tweak-tool-icon_48x48.png")));        
        return images;
    }

    private List<Image> setIconImagesTextColor()
    {
        List<Image> images = new ArrayList<>(4);
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/text_color_x16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/text_color_x24.png")));
        return images;
    }
    
    private List<Image> setIconImagesBGColor()
    {
        List<Image> images = new ArrayList<>(4);
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/background_color_x16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/background_color_x24.png")));
        return images;
    }
    
    /**
     *
     * @param color
     * @return
     */
    private String ColorToHexString(Color color){
        //String rgb = Integer.toHexString(color.getRGB());
        //return "#"+rgb.substring(2, rgb.length());
        String hex = "#000000";
        try{
            if(color.getAlpha() >= 16 ){
                hex = "#"+Integer.toHexString(color.getRGB()).substring(2);
            }
            else{
                hex = String.format("#%06X", (0xFFFFFF & color.getRGB()));
            }
        }
        catch(NullPointerException ex){
            Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hex;
    }
    
    /**
     *
     * @param str
     * @return
     */
    public Color HexStringToColor(String str){
        String color = "";
        switch (str.length()) {
            case 7:
                if(str.substring(0,1).equals("#")){
                    color = str.substring(1,6);
                }   break;
            case 6:
                color = str;
                break;
            case 3:
                char[] chars = str.toCharArray();
                color = ""+chars[0]+chars[0]+chars[1]+chars[1]+chars[2]+chars[2];
                break;
            default:
                break;
        }
        return Color.decode(color);
    }
        
    private int setComboBoxSelected(String str,JComboBox cmb){
        for (int i = 0; i < cmb.getItemCount(); i++) {  
            if (str.equals(cmb.getItemAt(i))) {  
                //System.out.println(i);  
                return i;
            }  
        }  
        return -1;
        //rootEntityComboBox.setSelectedItem(selectedItem.getDisplayName());     
    }
    

    private int getParaLineSpaceVal()
    {
        int retval;
        switch(USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT)
        {
            case 100: retval = 0; break;
            case 150: retval = 1; break;
            case 200: retval = 2; break;
            default: retval = 0;
        }
        return retval;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                //System.out.println(info.getName());
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    //break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BibleGetFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new OptionsFrame(instance.m_xController).setVisible(true);
            } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
                Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    /*
    private ClassLoader findClassLoaderForContext(Class<OptionsFrame> c) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader me = c.getClassLoader();
        ClassLoader system = ClassLoader.getSystemClassLoader();
        return (context == null) ? (me == null) ? system : me : context;
    }
    */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupBibleVersionAlign;
    private javax.swing.ButtonGroup buttonGroupBibleVersionVAlign;
    private javax.swing.ButtonGroup buttonGroupBibleVersionWrap;
    private javax.swing.ButtonGroup buttonGroupBookChapterAlign;
    private javax.swing.ButtonGroup buttonGroupBookChapterFormat;
    private javax.swing.ButtonGroup buttonGroupBookChapterVAlign;
    private javax.swing.ButtonGroup buttonGroupBookChapterWrap;
    private javax.swing.ButtonGroup buttonGroupParagraphAlign;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JButton jButtonBibleVersionHighlightColor;
    private javax.swing.JButton jButtonBibleVersionTextColor;
    private javax.swing.JButton jButtonBookChapterHighlightColor;
    private javax.swing.JButton jButtonBookChapterTextColor;
    private javax.swing.JButton jButtonLeftIndentLess;
    private javax.swing.JButton jButtonLeftIndentMore;
    private javax.swing.JButton jButtonRightIndentLess;
    private javax.swing.JButton jButtonRightIndentMore;
    private javax.swing.JButton jButtonVerseNumberHighlightColor;
    private javax.swing.JButton jButtonVerseNumberTextColor;
    private javax.swing.JButton jButtonVerseTextHighlightColor;
    private javax.swing.JButton jButtonVerseTextTextColor;
    private javax.swing.JCheckBox jCheckBoxUseVersionFormatting;
    private javax.swing.JComboBox jComboBoxBibleVersionFontSize;
    private javax.swing.JComboBox jComboBoxBookChapterFontSize;
    private javax.swing.JComboBox jComboBoxParagraphFontFamily;
    private javax.swing.JComboBox jComboBoxParagraphLineHeight;
    private javax.swing.JComboBox jComboBoxVerseNumberFontSize;
    private javax.swing.JComboBox jComboBoxVerseTextFontSize;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelLeftIndent;
    private javax.swing.JLabel jLabelRightIndent;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelBibleVersion;
    private javax.swing.JPanel jPanelBookChapter;
    private javax.swing.JPanel jPanelParagraph;
    private javax.swing.JPanel jPanelParagraphAlignment;
    private javax.swing.JPanel jPanelParagraphFontFamily;
    private javax.swing.JPanel jPanelParagraphIndentLeft;
    private javax.swing.JPanel jPanelParagraphIndentRight;
    private javax.swing.JPanel jPanelParagraphLineHeight;
    private javax.swing.JPanel jPanelVerseNumber;
    private javax.swing.JPanel jPanelVerseText;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator14;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JToolBar.Separator jSeparator16;
    private javax.swing.JToolBar.Separator jSeparator17;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToggleButton jToggleButtonBibleVersionAlignCenter;
    private javax.swing.JToggleButton jToggleButtonBibleVersionAlignLeft;
    private javax.swing.JToggleButton jToggleButtonBibleVersionAlignRight;
    private javax.swing.JToggleButton jToggleButtonBibleVersionBold;
    private javax.swing.JToggleButton jToggleButtonBibleVersionItalic;
    private javax.swing.JToggleButton jToggleButtonBibleVersionUnderline;
    private javax.swing.JToggleButton jToggleButtonBibleVersionVAlignBottom;
    private javax.swing.JToggleButton jToggleButtonBibleVersionVAlignTop;
    private javax.swing.JToggleButton jToggleButtonBibleVersionVisibility;
    private javax.swing.JToggleButton jToggleButtonBibleVersionWrapBrackets;
    private javax.swing.JToggleButton jToggleButtonBibleVersionWrapNone;
    private javax.swing.JToggleButton jToggleButtonBibleVersionWrapParentheses;
    private javax.swing.JToggleButton jToggleButtonBookChapterAlignCenter;
    private javax.swing.JToggleButton jToggleButtonBookChapterAlignLeft;
    private javax.swing.JToggleButton jToggleButtonBookChapterAlignRight;
    private javax.swing.JToggleButton jToggleButtonBookChapterBibleLang;
    private javax.swing.JToggleButton jToggleButtonBookChapterBibleLangAbbrev;
    private javax.swing.JToggleButton jToggleButtonBookChapterBold;
    private javax.swing.JToggleButton jToggleButtonBookChapterFullRef;
    private javax.swing.JToggleButton jToggleButtonBookChapterItalic;
    private javax.swing.JToggleButton jToggleButtonBookChapterUnderline;
    private javax.swing.JToggleButton jToggleButtonBookChapterUserLang;
    private javax.swing.JToggleButton jToggleButtonBookChapterUserLangAbbrev;
    private javax.swing.JToggleButton jToggleButtonBookChapterVAlignBottom;
    private javax.swing.JToggleButton jToggleButtonBookChapterVAlignBottominline;
    private javax.swing.JToggleButton jToggleButtonBookChapterVAlignTop;
    private javax.swing.JToggleButton jToggleButtonBookChapterWrapBrackets;
    private javax.swing.JToggleButton jToggleButtonBookChapterWrapNone;
    private javax.swing.JToggleButton jToggleButtonBookChapterWrapParentheses;
    private javax.swing.JToggleButton jToggleButtonParagraphCenter;
    private javax.swing.JToggleButton jToggleButtonParagraphJustify;
    private javax.swing.JToggleButton jToggleButtonParagraphLeft;
    private javax.swing.JToggleButton jToggleButtonParagraphRight;
    private javax.swing.JToggleButton jToggleButtonVerseNumberBold;
    private javax.swing.JToggleButton jToggleButtonVerseNumberItalic;
    private javax.swing.JToggleButton jToggleButtonVerseNumberSubscript;
    private javax.swing.JToggleButton jToggleButtonVerseNumberSuperscript;
    private javax.swing.JToggleButton jToggleButtonVerseNumberUnderline;
    private javax.swing.JToggleButton jToggleButtonVerseNumberVisibility;
    private javax.swing.JToggleButton jToggleButtonVerseTextBold;
    private javax.swing.JToggleButton jToggleButtonVerseTextItalic;
    private javax.swing.JToggleButton jToggleButtonVerseTextUnderline;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBar8;
    private javax.swing.JToolBar jToolBarParagraphAlignment;
    private javax.swing.JToolBar jToolBarParagraphFontFamily;
    private javax.swing.JToolBar jToolBarParagraphIndentLeft;
    private javax.swing.JToolBar jToolBarParagraphIndentRight;
    private javax.swing.JToolBar jToolBarParagraphLineHeight;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setVisible(final boolean visible) {
        XViewSettingsSupplier xViewSettings = (XViewSettingsSupplier) UnoRuntime.queryInterface(XViewSettingsSupplier.class,m_xController);
        XPropertySet viewSettings=xViewSettings.getViewSettings();
        try {
            System.out.println("I got the HorizontalRulerMetric property, here is the type :" + AnyConverter.getType(viewSettings.getPropertyValue("HorizontalRulerMetric")));
        } catch (UnknownPropertyException | WrappedTargetException ex) {
            Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            System.out.println("And here is the value: " + AnyConverter.toInt(viewSettings.getPropertyValue("HorizontalRulerMetric")));
        } catch (UnknownPropertyException | WrappedTargetException | IllegalArgumentException ex) {
            Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        int mUnit1;
        try {
            mUnit1 = AnyConverter.toInt(viewSettings.getPropertyValue("HorizontalRulerMetric"));
            if(BibleGetIO.measureUnit != BGET.MEASUREUNIT.valueOf(mUnit1)){
                USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT = BGET.MEASUREUNIT.valueOf(mUnit1);
                switch(BibleGetIO.measureUnit){
                    case MM:
                        switch(USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT){
                            case CM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= MM2CM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentCM ) * stepIndentCM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentCM; }
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= MM2CM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentCM ) * stepIndentCM;
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentCM; }
                                break;
                            case INCH:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= MM2Inches;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentINCH ) * stepIndentINCH;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= MM2Inches;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentINCH ) * stepIndentINCH;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentINCH; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentINCH; }
                                break;
                            case POINT:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= MM2Points;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPoints ) * stepIndentPoints;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= MM2Points;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPoints ) * stepIndentPoints;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPoints; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPoints; }
                                break;
                            case PICA:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= MM2Picas;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPicas ) * stepIndentPicas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= MM2Picas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPicas ) * stepIndentPicas;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPicas; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPicas; }
                                break;
                        }
                        break;
                    case CM:
                        switch(BGET.MEASUREUNIT.valueOf(mUnit1)){
                            case MM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= CM2MM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentMM ) * stepIndentMM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= CM2MM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentMM ) * stepIndentMM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentMM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentMM; }
                                break;
                            case INCH:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= CM2Inches;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentINCH ) * stepIndentINCH;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= CM2Inches;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentINCH ) * stepIndentINCH;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentINCH; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentINCH; }
                                break;
                            case POINT:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= CM2Points;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPoints ) * stepIndentPoints;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= CM2Points;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPoints ) * stepIndentPoints;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPoints; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPoints; }
                                break;
                            case PICA:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= CM2Picas;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPicas ) * stepIndentPicas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= CM2Picas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPicas ) * stepIndentPicas;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPicas; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPicas; }
                                break;
                        }
                        break;
                    case INCH:    
                        switch(BGET.MEASUREUNIT.valueOf(mUnit1)){
                            case CM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Inches2CM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentCM ) * stepIndentCM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Inches2CM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentCM ) * stepIndentCM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentCM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentCM; }
                                break;
                            case MM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Inches2MM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentMM ) * stepIndentMM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Inches2MM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentMM ) * stepIndentMM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentMM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentMM; }
                                break;
                            case POINT:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Inches2Points;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPoints ) * stepIndentPoints;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Inches2Points;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPoints ) * stepIndentPoints;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPoints; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPoints; }
                                break;
                            case PICA:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Inches2Picas;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPicas ) * stepIndentPicas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Inches2Picas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPicas ) * stepIndentPicas;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPicas; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPicas; }
                                break;
                        }
                        break;
                    case POINT:
                        switch(BGET.MEASUREUNIT.valueOf(mUnit1)){
                            case MM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Points2MM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentMM ) * stepIndentMM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Points2MM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentMM ) * stepIndentMM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentMM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentMM; }
                                break;
                            case CM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Points2CM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentCM ) * stepIndentCM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Points2CM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentCM ) * stepIndentCM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentCM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentCM; }
                                break;
                            case INCH:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Points2Inches;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentINCH ) * stepIndentINCH;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Points2Inches;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentINCH ) * stepIndentINCH;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentINCH; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentINCH; }
                                break;
                            case PICA:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Points2Picas;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPicas ) * stepIndentPicas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Points2Picas;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPicas ) * stepIndentPicas;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPicas; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPicas){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPicas; }
                                break;
                        }
                        break;
                    case PICA:
                        switch(BGET.MEASUREUNIT.valueOf(mUnit1)){
                            case MM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Picas2MM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentMM ) * stepIndentMM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Picas2MM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentMM ) * stepIndentMM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentMM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentMM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentMM; }
                                break;
                            case CM:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Picas2CM;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentCM ) * stepIndentCM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Picas2CM;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentCM ) * stepIndentCM;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentCM; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentCM){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentCM; }
                                break;
                            case INCH:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Picas2Inches;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentINCH ) * stepIndentINCH;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Picas2Inches;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentINCH ) * stepIndentINCH;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentINCH; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentINCH){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentINCH; }
                                break;
                            case POINT:
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT *= Picas2Points;
                                USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT / stepIndentPoints ) * stepIndentPoints;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT *= Picas2Points;
                                USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = Math.round(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT / stepIndentPoints ) * stepIndentPoints;
                                if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = maxIndentPoints; }
                                if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT < 0.0){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = 0.0; }
                                else if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT > maxIndentPoints){ USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = maxIndentPoints; }
                                break;
                        }
                        break;
                }

                //value has changed
                String jScript = "rulerLength = "+rulerLength+";"
                + "pixelRatioVals = getPixelRatioVals(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ");"
                + "leftindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + " * pixelRatioVals.dpi + 35;"
                + "rightindent = " + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + " * pixelRatioVals.dpi + 35;"
                + "bestWidth = rulerLength * 96 * window.devicePixelRatio + (35*2);"
                + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
                + "drawRuler(rulerLength," + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT) + "," + String.format(Locale.ROOT, "%.1f", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT) + ");";
                browser.executeJavaScript(jScript, browser.getURL(),0);
                
                jLabelLeftIndent.setText(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.name());
                jLabelRightIndent.setText(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.name());
                
                System.out.println("Ruler switched from " + BibleGetIO.measureUnit.name() + "|" + BibleGetIO.measureUnit.getValue() + " to " + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.name() + "|" + USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT.getValue() + ".");
                System.out.println("rulerLength = "+rulerLength);
                BibleGetIO.measureUnit = USERPREFS.PARAGRAPHSTYLES_MEASUREUNIT;
            } else {
                System.out.println("While making the preferences window visible, we detected that the ruler units have not changed. No need to redraw the ruler as far as I can tell.");
            }
        } catch (UnknownPropertyException | WrappedTargetException | IllegalArgumentException ex) {
            Logger.getLogger(OptionsFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        // make sure that frame is marked as not disposed if it is asked to be visible
      if (visible) {
          //setDisposed(false);
      }
      // let's handle visibility...
      if (!visible || !isVisible()) { // have to check this condition simply because super.setVisible(true) invokes toFront if frame was already visible
          super.setVisible(visible);
      }
      // ...and bring frame to the front.. in a strange and weird way
      if (visible) {
          int state = super.getExtendedState();
          state &= ~JFrame.ICONIFIED;
          super.setExtendedState(state);
          super.setAlwaysOnTop(true);
          super.toFront();
          super.requestFocus();
          super.setAlwaysOnTop(false);
      }
    }

    @Override
    public void toFront() {
      super.setVisible(true);
      int state = super.getExtendedState();
      state &= ~JFrame.ICONIFIED;
      super.setExtendedState(state);
      super.setAlwaysOnTop(true);
      super.toFront();
      super.requestFocus();
      super.setAlwaysOnTop(false);
    }



}
