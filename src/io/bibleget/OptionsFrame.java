/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import static io.bibleget.BibleGetI18N.__;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.joining;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTextPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;

/**
 *
 * @author Lwangaman
 */
public class OptionsFrame extends javax.swing.JFrame {

    private final int screenWidth;
    private final int screenHeight;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameLeft;
    private final int frameTop;
            
    private final BibleGetDB biblegetDB;
    private final Preferences USERPREFS;
    private final LocalizedBibleBooks L10NBibleBooks = new LocalizedBibleBooks();
    
    private final HTMLEditorKit kit;
    private final Document doc;
    private final String HTMLStr;
    private final StyleSheet styles;
    
    private final FontFamilyListCellRenderer FFLCRenderer;
    private final DefaultComboBoxModel fontFamilies;
    //protected int mouseOver;
    /*
    static Color listBackground;
    static Color listSelectionBackground;
    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        listBackground = uid.getColor("List.background");
        listSelectionBackground = uid.getColor("List.selectionBackground");
    }
    */
    //private final ResourceBundle myMessages;
    private final CefApp cefApp;
    private final CefClient client;
    private final CefBrowser browser;
    private final Component browserUI;
    private final HashMap<String, String> styleSheetRules = new HashMap<>();
    private final String previewDocument;
    private final String previewDocStylesheet;
    private final String previewDocScript;
    
    private static OptionsFrame instance;
        
    /**
     * Creates new form OptionsFrame
     * @param pkgPath
     */
    private OptionsFrame() throws ClassNotFoundException, UnsupportedEncodingException, SQLException {
        this.FFLCRenderer = new FontFamilyListCellRenderer();
                
        //jTextPane does not initialize correctly, it causes a Null Exception Pointer
        //Following line keeps this from crashing the program
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        // Get preferences from database       
        //System.out.println("getting instance of BibleGetDB");
        biblegetDB = BibleGetDB.getInstance();
        USERPREFS = Preferences.getInstance();
        
        //System.out.println("getting JsonObject of biblegetDB options");
        JsonObject myOptions = biblegetDB.getOptions();
        //System.out.println(myOptions.toString());
        //System.out.println("getting JsonValue of options rows");
        JsonValue myResults = myOptions.get("rows");
        //System.out.println(myResults.toString());
        //System.out.println("navigating values in json tree and setting global variables");
        navigateTree(myResults, null);
        this.fontFamilies = BibleGetIO.getFontFamilies();
        
        //System.out.println("(OptionsFrame: 127) USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR ="+USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);
        
        this.kit = new HTMLEditorKit();
        this.doc = kit.createDefaultDocument();
        this.styles = kit.getStyleSheet();
        
        String bkChVAlign = "";
        switch(USERPREFS.BOOKCHAPTERSTYLES_VALIGN){
            case SUPERSCRIPT:
                bkChVAlign = "super";
                break;
            case SUBSCRIPT:
                bkChVAlign = "sub";
                break;
            case NORMAL:
                bkChVAlign = "initial";
        }
        
        String vsNumVAlign = "";
        switch(USERPREFS.VERSENUMBERSTYLES_VALIGN){
            case SUPERSCRIPT:
                vsNumVAlign = "super";
                break;
            case SUBSCRIPT:
                vsNumVAlign = "sub";
                break;
            case NORMAL:
                vsNumVAlign = "initial";
        }
        
        String vsTxtVAlign = "";
        switch(USERPREFS.VERSETEXTSTYLES_VALIGN){
            case SUPERSCRIPT:
                vsTxtVAlign = "super";
                break;
            case SUBSCRIPT:
                vsTxtVAlign = "sub";
                break;
            case NORMAL:
                vsTxtVAlign = "initial";
        }
        
        styles.addRule("body { padding: 6px; background-color: #FFFFFF; }");
        styles.addRule("div.results { font-family: "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY+"; }");
        styles.addRule("div.results p.book { font-size:"+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE+"pt; }");
        styles.addRule("div.results p.book { font-weight:"+(USERPREFS.BOOKCHAPTERSTYLES_BOLD?"bold":"normal")+"; }");
        styles.addRule("div.results p.book { color:"+ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR)+"; }");
        styles.addRule("div.results p.book { background-color:"+ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR)+"; }");
        styles.addRule("div.results p.book { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
        styles.addRule("div.results p.book span { vertical-align: "+bkChVAlign+"; }");
        styles.addRule("div.results p.verses { text-align: "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT+"; }");
        styles.addRule("div.results p.verses { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
        styles.addRule("div.results p.verses span.sup { font-size:"+USERPREFS.VERSENUMBERSTYLES_FONTSIZE+"pt; }");
        styles.addRule("div.results p.verses span.sup { color:"+ColorToHexString(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR)+";");
        styles.addRule("div.results p.verses span.sup { background-color:"+ColorToHexString(USERPREFS.VERSENUMBERSTYLES_BGCOLOR)+";");
        styles.addRule("div.results p.verses span.sup { vertical-align: "+vsNumVAlign+"; }");
        styles.addRule("div.results p.verses span.text { font-size:"+USERPREFS.VERSETEXTSTYLES_FONTSIZE+"pt; }");
        styles.addRule("div.results p.verses span.text { color:"+ColorToHexString(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR)+"; }");
        styles.addRule("div.results p.verses span.text { background-color:"+ColorToHexString(USERPREFS.VERSETEXTSTYLES_BGCOLOR)+"; }");
        styles.addRule("div.results p.verses span.text { vertical-align: "+vsTxtVAlign+"; }");

        styleSheetRules.put("body", "body { padding: 6px; background-color: #FFFFFF; }");
        styleSheetRules.put("paragraphFontFamily", "div.results { font-family: "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY+"; }");
        styleSheetRules.put("fontSizeBookChapter", "div.results p.book { font-size:"+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE+"pt; }");
        styleSheetRules.put("boldBookChapter", "div.results p.book { font-weight:"+(USERPREFS.BOOKCHAPTERSTYLES_BOLD?"bold":"normal")+"; }");
        styleSheetRules.put("textColorBookChapter", "div.results p.book { color:"+ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR)+"; }");
        styleSheetRules.put("bgColorBookChapter", "div.results p.book { background-color:"+ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR)+"; }");
        styleSheetRules.put("paragraphLineSpacing1", "div.results p.book { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
        styleSheetRules.put("vAlignBookChapter", "div.results p.book span { vertical-align: "+bkChVAlign+"; }");
        styleSheetRules.put("paragraphAlignment", "div.results p.verses { text-align: "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT+"; }");
        styleSheetRules.put("paragraphLineSpacing2", "div.results p.verses { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
        styleSheetRules.put("fontSizeVerseNumber", "div.results p.verses span.sup { font-size:"+USERPREFS.VERSENUMBERSTYLES_FONTSIZE+"pt; }");
        styleSheetRules.put("textColorVerseNumber", "div.results p.verses span.sup { color:"+ColorToHexString(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR)+"; }");
        styleSheetRules.put("bgColorVerseNumber", "div.results p.verses span.sup { background-color:"+ColorToHexString(USERPREFS.VERSENUMBERSTYLES_BGCOLOR)+"; }");
        styleSheetRules.put("vAlignVerseNumber", "div.results p.verses span.sup { vertical-align: "+vsNumVAlign+"; }");
        styleSheetRules.put("fontSizeVerseText", "div.results p.verses span.text { font-size:"+USERPREFS.VERSETEXTSTYLES_FONTSIZE+"pt; }");
        styleSheetRules.put("textColorVerseText", "div.results p.verses span.text { color:"+ColorToHexString(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR)+"; }");
        styleSheetRules.put("bgColorVerseText", "div.results p.verses span.text { background-color:"+ColorToHexString(USERPREFS.VERSETEXTSTYLES_BGCOLOR)+"; }");
        styleSheetRules.put("vAlignVerseText", "div.results p.verses span.text { vertical-align: "+vsTxtVAlign+"; }");
        String s = styleSheetRules.entrySet()
                     .stream()
                     .map(e -> e.getValue()) //e.getKey()+"="+
                     .collect(joining(System.lineSeparator()));
        System.out.println("styleSheetRules = ");
        System.out.println(s);
        
        String tempStr = "";
        tempStr += "<html><head><meta charset=\"utf-8\"><style>%s</style></head><body><div class=\"results\"><p class=\"book\"><span>";
        tempStr += __("Genesi")+"&nbsp;1";
        tempStr += "</span></p><p class=\"verses\" style=\"margin-top:0px;\"><span class=\"sup\">1</span><span class=\"text\">";
        tempStr += __("In the beginning, when God created the heavens and the earth");
        tempStr += "</span><span class=\"sup\">2</span><span class=\"text\">";
        tempStr += __("and the earth was without form or shape, with darkness over the abyss and a mighty wind sweeping over the waters");
        tempStr += "</span><span class=\"sup\">3</span><span class=\"text\">";
        tempStr += __("Then God said: Let there be light, and there was light.");
        tempStr += "</span></p></div></body></html>";
        HTMLStr = tempStr;

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
        
        String bibleVersionWrapBefore = "";
        String bibleVersionWrapAfter = "";
        if(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP == BGET.WRAP.PARENTHESES){
            bibleVersionWrapBefore = "(";
            bibleVersionWrapAfter = ")";
        }
        else if(USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP == BGET.WRAP.BRACKETS){
            bibleVersionWrapBefore = "[";
            bibleVersionWrapAfter = "]";
        }

        String bookChapterWrapBefore = "";
        String bookChapterWrapAfter = "";
        if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP == BGET.WRAP.PARENTHESES){
            bookChapterWrapBefore = "(";
            bookChapterWrapAfter = ")";
        }
        else if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP == BGET.WRAP.BRACKETS){
            bookChapterWrapBefore = "[";
            bookChapterWrapAfter = "]";
        }

        String bookChapter = "";
        LocalizedBibleBook LocalizedBookSamuel = L10NBibleBooks.GetBookByIndex(8);
        switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT){
            case BIBLELANG:
                bookChapter = "I Samuelis 1";
                break;
            case BIBLELANGABBREV:
                bookChapter = "I Sam 1";
                break;
            case USERLANG:
                bookChapter = LocalizedBookSamuel.Fullname & " 1";
                break;
            case USERLANGABBREV:
                bookChapter = LocalizedBookSamuel.Abbrev & " 1";
        }
        
        if(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY){
            bookChapter += ",1-3";
        }
        bookChapter = bookChapterWrapBefore + bookChapter + bookChapterWrapAfter;
        Double lineHeight = Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT) / 100;
        previewDocStylesheet = "<style type=\"text/css\">"
            + "html,body { padding: 0px; margin: 0px; background-color: #FFFFFF; }"
            + "p { padding: 0px; margin: 0px; }"
            + ".previewRuler { margin: 0px auto; }"
            + "div.results { box-sizing: border-box; margin: 0px auto; padding-left: 35px; padding-right:35px; }"
            + "div.results .bibleVersion { font-family: '" + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "'; }"
            + "div.results .bibleVersion { font-size: " + USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE + "pt; }"
            + "div.results .bibleVersion { font-weight: " + (USERPREFS.BIBLEVERSIONSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .bibleVersion { font-style: " + (USERPREFS.BIBLEVERSIONSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE ? "div.results .bibleVersion { text-decoration: underline; }" : "")
            + "div.results .bibleVersion { color: " + ColorToHexString(USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR) + "; }"
            + "div.results .bibleVersion { background-color: " + ColorToHexString(USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR) + "; }"
            + "div.results .bibleVersion { text-align: " + USERPREFS.BIBLEVERSIONSTYLES_ALIGNMENT.getCSSValue() + "; }"
            + "div.results .bookChapter { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            + "div.results .bookChapter { font-size: " + USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE + "pt; }"
            + "div.results .bookChapter { font-weight: " + (USERPREFS.BOOKCHAPTERSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .bookChapter { font-style: " + (USERPREFS.BOOKCHAPTERSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE ? "div.results .bookChapter { text-decoration: underline; }" : "")
            + "div.results .bookChapter { color: " + ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR) + "; }"
            + "div.results .bookChapter { background-color: " + ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR) + "; }"
            + "div.results .bookChapter { text-align: " + USERPREFS.BOOKCHAPTERSTYLES_ALIGNMENT.getCSSValue() + "; }"
            + "div.results span.bookChapter { display: inline-block; margin-left: 6px; }"
            + "div.results .versesParagraph { text-align: " + USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue() + "; }"
            + "div.results .versesParagraph { line-height: " + String.format(Locale.ROOT, "%.2f", lineHeight) + "em; }"
            + "div.results .versesParagraph .verseNum { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            + "div.results .versesParagraph .verseNum { font-size: " + USERPREFS.VERSENUMBERSTYLES_FONTSIZE + "pt; }"
            + "div.results .versesParagraph .verseNum { font-weight: " + (USERPREFS.VERSENUMBERSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .versesParagraph .verseNum { font-style: " + (USERPREFS.VERSENUMBERSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.VERSENUMBERSTYLES_UNDERLINE ? "div.results .versesParagraph .verseNum { text-decoration: underline; }" : "")
            + "div.results .versesParagraph .verseNum { color: " + ColorToHexString(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR) + "; }"
            + "div.results .versesParagraph .verseNum { background-color: " + ColorToHexString(USERPREFS.VERSENUMBERSTYLES_BGCOLOR) + "; }"
            + "div.results .versesParagraph .verseNum { vertical-align: baseline; " + vnPosition + vnTop + " }"
            + "div.results .versesParagraph .verseNum { padding-left: 3px; }"
            + "div.results .versesParagraph .verseNum:first-child { padding-left: 0px; }"
            + "div.results .versesParagraph .verseText { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
            + "div.results .versesParagraph .verseText { font-size: " + USERPREFS.VERSETEXTSTYLES_FONTSIZE + "pt; }"
            + "div.results .versesParagraph .verseText { font-weight: " + (USERPREFS.VERSETEXTSTYLES_BOLD ? "bold" : "normal") + "; }"
            + "div.results .versesParagraph .verseText { font-style: " + (USERPREFS.VERSETEXTSTYLES_ITALIC ? "italic" : "normal") + "; }"
            + (USERPREFS.VERSETEXTSTYLES_UNDERLINE ? "div.results .versesParagraph .verseText { text-decoration: underline; }" : "")
            + "div.results .versesParagraph .verseText { color: " + ColorToHexString(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR) + "; }"
            + "div.results .versesParagraph .verseText { background-color: " + ColorToHexString(USERPREFS.VERSETEXTSTYLES_BGCOLOR) + "; }"
            + "</style>";
        
        previewDocScript = "<script type=\"text/javascript\">"
            + "var getPixelRatioVals = function(rulerLength,convertToCM){"
            + "let inchesToCM = 2.54,"
            + "dpr = window.devicePixelRatio,"
            //ppi = ((96 * dpr) / 100),
            //dpi = (96 * ppi),
            + "dpi = 96 * dpr,"
            + "drawInterval = 0.125;"
            + "if(convertToCM){"
            //ppi /= inchesToCM;
            + "dpi /= inchesToCM;"
            + "rulerLength *= inchesToCM;"
            + "drawInterval = 0.25;"
            + "}"
            + "return {"
            + "inchesToCM: inchesToCM,"
            + "dpr: dpr,"
            //ppi: ppi,
            + "dpi: dpi,"
            + "rulerLength: rulerLength,"
            + "drawInterval: drawInterval"
            + "};"
            + "},"
            + "triangleAt = function (x,context,pixelRatioVals,initialPadding,canvasWidth){"
            + "let xPos = x*pixelRatioVals.dpi;//x.map(0,pixelRatioVals.rulerLength,0,(canvasWidth-(initialPadding*2)));"
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
            + "drawRuler = function(rulerLen, cvtToCM, lftindnt, rgtindnt){"
            + "var pixelRatioVals = getPixelRatioVals(rulerLen,cvtToCM),"
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
            + "for(let interval = 0; interval <= pixelRatioVals.rulerLength; interval += pixelRatioVals.drawInterval){"
            + "let xPosA = Math.round(interval*pixelRatioVals.dpi)+0.5;"
            + "if(interval == Math.floor(interval) && interval > 0){"
            + "if(currentWholeNumber+1 == 10){ offset+=4; }"
            + "context.fillText(++currentWholeNumber,initialPadding+xPosA-offset,14);"
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
            + "let pixelRatioVals = getPixelRatioVals(7," + (USERPREFS.PARAGRAPHSTYLES_INTERFACEINCM ? "true" : "false") + ");"
            + "let leftindent = " + String.format(Locale.ROOT, "%.2f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)) + " * pixelRatioVals.dpi + 35;"
            + "let rightindent = " + String.format(Locale.ROOT, "%.2f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)) + " * pixelRatioVals.dpi + 35;"
            + "let bestWidth = 7 * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(7," + (USERPREFS.PARAGRAPHSTYLES_INTERFACEINCM ? "true" : "false") + "," + String.format(Locale.ROOT, "%.2f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)) + "," + String.format(Locale.ROOT, "%.2f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)) + ");"
            + "});";
        
        previewDocument = "<html><head><meta charset=\"utf-8\">" + previewDocStylesheet
            + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>" 
            + "<script>const dPR = 1.0;</script>" 
            + "</head><body>"
            + "<canvas class=\"previewRuler\"></canvas>"
            + "<p class=\"bibleversion showtop\" style=\"display:"+(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW==BGET.VISIBILITY.SHOW && USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION==BGET.POS.TOP?"block":"none")+";\"><span class=\"bcWrap\">" + bibleVersionWrapBefore + "</span>NVBSE<span class=\"bcWrap\">" + bibleVersionWrapAfter + "</span></p>"
            + "<div class=\"bookchapterWrapper\">"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.TOP ? "<span class=\"bookchapter\" style=\"display:block;\"><span class=\"bcWrap\">" + bookChapterWrapBefore + "</span><span class=\"bcText1Sam\">" + bkChptr1Sam213 + "</span><span class=\"bcWrap\">" + bookChapterWrapAfter + "</span></span>" : "")
            + "<div class=\"liveview-quote\">"
            + "<span class=\"versenumber\" style=\"display:"+(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW ? "inline" : "none")+";\">1</span><span class=\"versetext\">"+ ISam2_1 +"</span>"
            + "<span class=\"versenumber\" style=\"display:"+(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW ? "inline" : "none")+";\">2</span><span class=\"versetext\">"+ ISam2_2 +"</span>"
            + "<span class=\"versenumber\" style=\"display:"+(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW ? "inline" : "none")+";\">3</span><span class=\"versetext\">"+ ISam2_3 +"</span>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.BOTTOMINLINE) ? "<span class=\"bookchapter\" style=\"margin-left:6px;\"><span class=\"bcWrap\">" + bookChapterWrapBefore + "</span><span class=\"bcText1Sam\">" + bkChptr1Sam213 + "</span><span class=\"bcWrap\">" + bookChapterWrapAfter + "</span></span>" : ""
            + "</div>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.BOTTOM) ? "<span class=\"bookchapter\" style=\"display:block;\"><span class=\"bcWrap\">" + bookChapterWrapBefore + "</span><span class=\"bcText1Sam\">" + bkChptr1Sam213 + "</span><span class=\"bcWrap\">" + bookChapterWrapAfter + "</span></span>" : ""
            + "</div>"
            + "<div class=\"bookchapterWrapper\">"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.TOP) ? "<span class=\"bookchapter\" style=\"display:block;\"><span class=\"bcWrap\">" + bookChapterWrapBefore + "</span><span class=\"bcTextPs\">" + bkChptrPs11412 + "</span><span class=\"bcWrap\">" + bookChapterWrapAfter + "</span></span>" : ""
            + "<div class=\"liveview-quote\">"
            + "<span class=\"versenumber\" style=\"display:"+(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW ? "inline" : "none")+";\">1</span><span class=\"versetext\">"+ Ps114_1 +"</span>"
            + "<span class=\"versenumber\" style=\"display:"+(USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW==BGET.VISIBILITY.SHOW ? "inline" : "none")+";\">2</span><span class=\"versetext\">factus est Iuda sanctuarium eius, Israel potestas eius.</span>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.BOTTOMINLINE) ? "<span class=\"bookchapter\" style=\"margin-left:6px;\"><span class=\"bcWrap\">" + bookChapterWrapBefore + "</span><span class=\"bcTextPs\">" + bkChptrPs11412 + "</span><span class=\"bcWrap\">" + bookChapterWrapAfter + "</span></span>" : ""
            + "</div>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION==BGET.POS.BOTTOM) ? "<span class=\"bookchapter\" style=\"display:block;\"><span class=\"bcWrap\">" + bookChapterWrapBefore + "</span><span class=\"bcTextPs\">" + bkChptrPs11412 + "</span><span class=\"bcWrap\">" + bookChapterWrapAfter + "</span></span>" : ""
            + "</div>"
            + "<p class=\"bibleversion showbottom\" style=\"display:"+(USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW==BGET.VISIBILITY.SHOW && USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION==BGET.POS.BOTTOM?"block":"none")+";\"><span class=\"bcWrap\">" + bibleVersionWrapBefore + "</span>NVBSE<span class=\"bcWrap\">" + bibleVersionWrapAfter + "</span></p>"
            + "</body>";

        
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int)screenSize.getWidth();
        screenHeight = (int)screenSize.getHeight();
        frameWidth = 686;
        frameHeight = 503;
        frameLeft = (screenWidth / 2) - (frameWidth / 2);
        frameTop = (screenHeight / 2) - (frameHeight / 2);
                
        //this.myMessages = BibleGetI18N.getMessages();
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = OS.isLinux();
        cefApp = CefApp.getInstance(settings);
        client = cefApp.createClient();
        String HTMLStrWithStyles = String.format(HTMLStr,s);
        browser = client.createBrowser( DataUri.create("text/html",HTMLStrWithStyles), OS.isLinux(), false);
        browserUI = browser.getUIComponent();
        
        initComponents();
        jInternalFrame1.setLayout(new BorderLayout());
        jInternalFrame1.getContentPane().add(browserUI, BorderLayout.CENTER);
        //jInternalFrame1.setSize(400, 300);
        //jInternalFrame1.setVisible(true);
    }

    public static OptionsFrame getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException
    {
        if(instance == null)
        {
            instance = new OptionsFrame();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jPanel8 = new javax.swing.JPanel();
        jToolBar5 = new javax.swing.JToolBar();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jToolBar7 = new javax.swing.JToolBar();
        jComboBox6 = new javax.swing.JComboBox();
        jPanel10 = new javax.swing.JPanel();
        jToolBar6 = new javax.swing.JToolBar();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jComboBox2 = new javax.swing.JComboBox();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jPanel5 = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        jToggleButton10 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jComboBox3 = new javax.swing.JComboBox();
        jToggleButton13 = new javax.swing.JToggleButton();
        jToggleButton14 = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        jToolBar4 = new javax.swing.JToolBar();
        jToggleButton15 = new javax.swing.JToggleButton();
        jToggleButton16 = new javax.swing.JToggleButton();
        jToggleButton17 = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jComboBox4 = new javax.swing.JComboBox();
        jToggleButton18 = new javax.swing.JToggleButton();
        jToggleButton19 = new javax.swing.JToggleButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jTextPane1.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        jInternalFrame1 = new JInternalFrame() {
            @Override
            public void setUI(InternalFrameUI ui) {
                super.setUI(ui); // this gets called internally when updating the ui and makes the northPane reappear
                BasicInternalFrameUI frameUI = (BasicInternalFrameUI) getUI(); // so...
                if (frameUI != null) frameUI.setNorthPane(null); // lets get rid of it
            }
        };
        //jInternalFrame1.setBorder(null);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(__("User Preferences"));
        setBounds(frameLeft,frameTop,frameWidth,frameHeight);
        setIconImages(setIconImages());
        setName("myoptions"); // NOI18N
        setResizable(false);

        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Paragraph"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanel3.setName("Paragraph"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Alignment")));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        buttonGroup1.add(jToggleButton3);
        jToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_left.png"))); // NOI18N
        jToggleButton3.setSelected(paragraphAlignment.equals("left"));
        jToggleButton3.setFocusable(false);
        jToggleButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton3.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton3ItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleButton3);
        jToggleButton3.getAccessibleContext().setAccessibleParent(jPanel1);

        buttonGroup1.add(jToggleButton2);
        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_center.png"))); // NOI18N
        jToggleButton2.setSelected(paragraphAlignment.equals("center"));
        jToggleButton2.setFocusable(false);
        jToggleButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton2.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton2ItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleButton2);
        jToggleButton2.getAccessibleContext().setAccessibleParent(jPanel1);

        buttonGroup1.add(jToggleButton4);
        jToggleButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_right.png"))); // NOI18N
        jToggleButton4.setSelected(paragraphAlignment.equals("right"));
        jToggleButton4.setFocusable(false);
        jToggleButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton4.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton4ItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleButton4);
        jToggleButton4.getAccessibleContext().setAccessibleParent(jPanel1);

        buttonGroup1.add(jToggleButton5);
        jToggleButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_justify.png"))); // NOI18N
        jToggleButton5.setSelected(paragraphAlignment.equals("justify"));
        jToggleButton5.setFocusable(false);
        jToggleButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton5.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton5ItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleButton5);
        jToggleButton5.getAccessibleContext().setAccessibleParent(jPanel1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Indent")));

        jToolBar5.setFloatable(false);
        jToolBar5.setRollover(true);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/increase_indent.png"))); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jToolBar5.add(jButton3);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/decrease_indent.png"))); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });
        jToolBar5.add(jButton4);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Line-spacing")));

        jToolBar7.setFloatable(false);
        jToolBar7.setRollover(true);

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "1½", "2" }));
        jComboBox6.setSelectedIndex(getParaLineSpaceVal());
        jComboBox6.setPreferredSize(new java.awt.Dimension(62, 41));
        jComboBox6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox6ItemStateChanged(evt);
            }
        });
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox6ActionPerformed(evt);
            }
        });
        jToolBar7.add(jComboBox6);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToolBar7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jToolBar7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Font")));

        jToolBar6.setFloatable(false);
        jToolBar6.setRollover(true);

        jComboBox1.setFont(new java.awt.Font(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY, java.awt.Font.PLAIN, 18));
        jComboBox1.setMaximumRowCount(6);
        jComboBox1.setModel(fontFamilies);
        jComboBox1.setSelectedItem(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY);
        jComboBox1.setPreferredSize(new java.awt.Dimension(300, 41));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
        jToolBar6.add(jComboBox1);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jToolBar6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToolBar6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Book / Chapter"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButton1.setSelected(USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton1ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButton1);

        jToggleButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButton6.setSelected(italicsBookChapter);
        jToggleButton6.setFocusable(false);
        jToggleButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton6.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton6ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButton6);

        jToggleButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButton7.setSelected(underscoreBookChapter);
        jToggleButton7.setFocusable(false);
        jToggleButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton7.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton7ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButton7);
        jToolBar2.add(jSeparator1);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jToolBar2.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });
        jToolBar2.add(jButton2);
        jToolBar2.add(jSeparator4);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBox2.setSelectedItem(""+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE+"");
        jComboBox2.setToolTipText("Book / Chapter font size");
        jComboBox2.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBox2.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox2ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jComboBox2);

        jToggleButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/superscript.png"))); // NOI18N
        jToggleButton8.setSelected(USERPREFS.BOOKCHAPTERSTYLES_VALIGN.equals(BGET.VALIGN.SUPERSCRIPT));
        jToggleButton8.setFocusable(false);
        jToggleButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton8.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton8ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButton8);

        jToggleButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/subscript.png"))); // NOI18N
        jToggleButton9.setSelected(USERPREFS.BOOKCHAPTERSTYLES_VALIGN.equals(BGET.VALIGN.SUBSCRIPT));
        jToggleButton9.setFocusable(false);
        jToggleButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton9.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton9.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton9ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleButton9);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Verse Number"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        jToggleButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButton10.setSelected(boldVerseNumber);
        jToggleButton10.setFocusable(false);
        jToggleButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton10.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton10.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton10ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButton10);

        jToggleButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButton11.setSelected(italicsVerseNumber);
        jToggleButton11.setFocusable(false);
        jToggleButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton11.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton11.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton11ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButton11);

        jToggleButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButton12.setSelected(underscoreVerseNumber);
        jToggleButton12.setFocusable(false);
        jToggleButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton12.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton12.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton12ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButton12);
        jToolBar3.add(jSeparator2);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });
        jToolBar3.add(jButton5);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });
        jToolBar3.add(jButton6);
        jToolBar3.add(jSeparator5);

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBox3.setSelectedItem(""+USERPREFS.VERSENUMBERSTYLES_FONTSIZE+"");
        jComboBox3.setToolTipText("Verse Number font size");
        jComboBox3.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBox3.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox3ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jComboBox3);

        jToggleButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/superscript.png"))); // NOI18N
        jToggleButton13.setSelected(vAlignVerseNumber.equals("super"));
        jToggleButton13.setFocusable(false);
        jToggleButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton13.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton13.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton13.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton13ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButton13);

        jToggleButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/subscript.png"))); // NOI18N
        jToggleButton14.setSelected(vAlignVerseNumber.equals("sub"));
        jToggleButton14.setFocusable(false);
        jToggleButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton14.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton14.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton14.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton14ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleButton14);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Verse Text"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        jToggleButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/bold.png"))); // NOI18N
        jToggleButton15.setSelected(boldVerseText);
        jToggleButton15.setFocusable(false);
        jToggleButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton15.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton15.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton15ItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButton15);

        jToggleButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/italic.png"))); // NOI18N
        jToggleButton16.setSelected(italicsVerseText);
        jToggleButton16.setFocusable(false);
        jToggleButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton16.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton16.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton16ItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButton16);

        jToggleButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/underline.png"))); // NOI18N
        jToggleButton17.setSelected(underscoreVerseText);
        jToggleButton17.setFocusable(false);
        jToggleButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton17.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton17.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton17.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton17ItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButton17);
        jToolBar4.add(jSeparator3);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/text_color.png"))); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton7MouseClicked(evt);
            }
        });
        jToolBar4.add(jButton7);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/background_color.png"))); // NOI18N
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton8MouseClicked(evt);
            }
        });
        jToolBar4.add(jButton8);
        jToolBar4.add(jSeparator6);

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "54", "60", "66", "72", "80", "88", "96" }));
        jComboBox4.setSelectedItem(""+USERPREFS.VERSETEXTSTYLES_FONTSIZE+"");
        jComboBox4.setToolTipText("Verse Text font size");
        jComboBox4.setMinimumSize(new java.awt.Dimension(47, 37));
        jComboBox4.setPreferredSize(new java.awt.Dimension(47, 37));
        jComboBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox4ItemStateChanged(evt);
            }
        });
        jToolBar4.add(jComboBox4);

        jToggleButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/superscript.png"))); // NOI18N
        jToggleButton18.setSelected(vAlignVerseText.equals("super"));
        jToggleButton18.setFocusable(false);
        jToggleButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton18.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton18.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton18ItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButton18);

        jToggleButton19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/subscript.png"))); // NOI18N
        jToggleButton19.setSelected(vAlignVerseText.equals("sub"));
        jToggleButton19.setFocusable(false);
        jToggleButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton19.setMargin(new java.awt.Insets(4, 4, 4, 4));
        jToggleButton19.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButton19.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton19ItemStateChanged(evt);
            }
        });
        jToolBar4.add(jToggleButton19);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(__("Preview")));

        jTextPane1.setEditable(false);
        jTextPane1.setContentType("text/html;charset=UTF-8"); // NOI18N
        jTextPane1.setDocument(doc);
        jTextPane1.setEditorKit(kit);
        jTextPane1.setText(HTMLStr);
        jTextPane1.setMaximumSize(new java.awt.Dimension(300, 300));
        jScrollPane2.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setText("(*"+__("line-spacing not visible in the preview")+")");

        jCheckBox1.setSelected(noVersionFormatting);
        jCheckBox1.setText(__("Override Bible Version Formatting"));
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel2.setText("<html><head><style>body#versionformatting{border:none;background-color:rgb(214,217,223);}</style></head><body id=\"versionformatting\">"+__("Some Bible versions have their own formatting. This is left by default to keep the text as close as possible to the original.<br> If however you need to have consistent formatting in your document, you may override the Bible version's own formatting.")+"</body></html>");
        jLabel2.setOpaque(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSeparator7)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addContainerGap())
        );

        jInternalFrame1.setBorder(null);
        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 437, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jInternalFrame1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jInternalFrame1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jColorChooserClean(JColorChooser jColorChooser){
        AbstractColorChooserPanel panels[] = jColorChooser.getChooserPanels();
        jColorChooser.removeChooserPanel(panels[4]);
        jColorChooser.removeChooserPanel(panels[3]);
        jColorChooser.removeChooserPanel(panels[2]);
    }

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR);
            styles.addRule("div.results p.book { color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
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
    }//GEN-LAST:event_jButton1MouseClicked
    
    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR);
            styles.addRule("div.results p.book { background-color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
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
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR);
            styles.addRule("div.results p.verses span.sup { color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("TEXTCOLORVERSENUMBER", rgb)){
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
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSENUMBERSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSENUMBERSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSENUMBERSTYLES_BGCOLOR);
            styles.addRule("div.results p.verses span.sup { background-color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("BGCOLORVERSENUMBER", rgb)){
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
    }//GEN-LAST:event_jButton6MouseClicked

    private void jButton7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton7MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSETEXTSTYLES_TEXTCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSETEXTSTYLES_TEXTCOLOR);
            styles.addRule("div.results p.verses span.text { color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("TEXTCOLORVERSETEXT", rgb)){
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
    }//GEN-LAST:event_jButton7MouseClicked

    private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(USERPREFS.VERSETEXTSTYLES_BGCOLOR);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            USERPREFS.VERSETEXTSTYLES_BGCOLOR = jColorChooser.getColor();
            String rgb = ColorToHexString(USERPREFS.VERSETEXTSTYLES_BGCOLOR);
            styles.addRule("div.results p.verses span.text { background-color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("BGCOLORVERSETEXT", rgb)){
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
    }//GEN-LAST:event_jButton8MouseClicked

    private void jToggleButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton1ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_BOLD = true;
            styles.addRule("div.results p.book { font-weight:bold; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_BOLD = false;
            styles.addRule("div.results p.book { font-weight:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("BOOKCHAPTERSTYLES_BOLD", USERPREFS.BOOKCHAPTERSTYLES_BOLD)){
            //System.out.println("BOLDBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButton1ItemStateChanged

    private void jToggleButton6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton6ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            italicsBookChapter = true;
            styles.addRule("div.results p.book { font-style:italic; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            italicsBookChapter = false;
            styles.addRule("div.results p.book { font-style:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("ITALICSBOOKCHAPTER", italicsBookChapter)){
            //System.out.println("ITALICSBOOKCHAPTER was successfully updated in database to value "+italicsBookChapter);
        }
        else{
            //System.out.println("Error updating ITALICSBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButton6ItemStateChanged

    private void jToggleButton7ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton7ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            underscoreBookChapter = true;
            styles.addRule("div.results p.book { text-decoration:underline; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            underscoreBookChapter = false;
            styles.addRule("div.results p.book { text-decoration:none; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("UNDERSCOREBOOKCHAPTER", underscoreBookChapter)){
            //System.out.println("UNDERSCOREBOOKCHAPTER was successfully updated in database to value "+underscoreBookChapter);
        }
        else{
            //System.out.println("Error updating UNDERSCOREBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButton7ItemStateChanged

    private void jToggleButton10ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton10ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            boldVerseNumber = true;
            styles.addRule("div.results p.verses span.sup { font-weight:bold; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            boldVerseNumber = false;
            styles.addRule("div.results p.verses span.sup { font-weight:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("BOLDVERSENUMBER", boldVerseNumber)){
            //System.out.println("BOLDVERSENUMBER was successfully updated in database to value "+boldVerseNumber);
        }
        else{
            //System.out.println("Error updating BOLDVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButton10ItemStateChanged

    private void jToggleButton11ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton11ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            italicsVerseNumber = true;
            styles.addRule("div.results p.verses span.sup { font-style:italic; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            italicsVerseNumber = false;
            styles.addRule("div.results p.verses span.sup { font-style:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("ITALICSVERSENUMBER", italicsVerseNumber)){
            //System.out.println("ITALICSVERSENUMBER was successfully updated in database to value "+italicsVerseNumber);
        }
        else{
            //System.out.println("Error updating ITALICSVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButton11ItemStateChanged

    private void jToggleButton12ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton12ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            underscoreVerseNumber = true;
            styles.addRule("div.results p.verses span.sup { text-decoration:underline; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            underscoreVerseNumber = false;
            styles.addRule("div.results p.verses span.sup { text-decoration:none; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("UNDERSCOREVERSENUMBER", underscoreVerseNumber)){
            //System.out.println("UNDERSCOREVERSENUMBER was successfully updated in database to value "+underscoreVerseNumber);
        }
        else{
            //System.out.println("Error updating UNDERSCOREVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButton12ItemStateChanged

    private void jToggleButton15ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton15ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            boldVerseText = true;
            styles.addRule("div.results p.verses span.text { font-weight:bold; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            boldVerseText = false;
            styles.addRule("div.results p.verses span.text { font-weight:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("BOLDVERSETEXT", boldVerseText)){
            //System.out.println("BOLDVERSETEXT was successfully updated in database to value "+boldVerseText);
        }
        else{
            //System.out.println("Error updating BOLDVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButton15ItemStateChanged

    private void jToggleButton16ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton16ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            italicsVerseText = true;
            styles.addRule("div.results p.verses span.text { font-style:italic; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            italicsVerseText = false;
            styles.addRule("div.results p.verses span.text { font-style:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("ITALICSVERSETEXT", italicsVerseText)){
            //System.out.println("ITALICSVERSETEXT was successfully updated in database to value "+italicsVerseText);
        }
        else{
            //System.out.println("Error updating ITALICSVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButton16ItemStateChanged

    private void jToggleButton17ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton17ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            underscoreVerseText = true;
            styles.addRule("div.results p.verses span.text { text-decoration:underline; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            underscoreVerseText = false;
            styles.addRule("div.results p.verses span.text { text-decoration:none; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("UNDERSCOREVERSETEXT", underscoreVerseText)){
            //System.out.println("UNDERSCOREVERSETEXT was successfully updated in database to value "+underscoreVerseText);
        }
        else{
            //System.out.println("Error updating UNDERSCOREVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButton17ItemStateChanged

    private void jToggleButton3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton3ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.LEFT;
            styles.addRule("div.results p.verses { text-align:"+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue()+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton3ItemStateChanged

    private void jToggleButton2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton2ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.CENTER;
            styles.addRule("div.results p.verses { text-align:"+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue()+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton2ItemStateChanged

    private void jToggleButton4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton4ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.RIGHT;
            styles.addRule("div.results p.verses { text-align:"+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue()+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton4ItemStateChanged

    private void jToggleButton5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton5ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.JUSTIFY;
            styles.addRule("div.results p.verses { text-align:"+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getCSSValue()+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_ALIGNMENT", USERPREFS.PARAGRAPHSTYLES_ALIGNMENT.getValue())){
                //System.out.println("PARAGRAPHSTYLES_ALIGNMENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_ALIGNMENT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHSTYLES_ALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton5ItemStateChanged

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += 5;
        styles.addRule("div.results { padding-left:"+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT+"pt; }");
        jTextPane1.setDocument(doc);
        jTextPane1.setText(HTMLStr);
        
        if(biblegetDB.setIntOption("PARAGRAPHSTYLES_LEFTINDENT", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)){
            //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
        }
        else{
            //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
        }
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT>=5){USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= 5;}
        styles.addRule("div.results { padding-left:"+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT+"pt; }");
        jTextPane1.setDocument(doc);
        jTextPane1.setText(HTMLStr);
        
        if(biblegetDB.setIntOption("PARAGRAPHSTYLES_LEFTINDENT", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)){
            //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
        }
        else{
            //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
        }
    }//GEN-LAST:event_jButton4MouseClicked

    private void jToggleButton8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton8ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton9.setSelected(false);
            USERPREFS.BOOKCHAPTERSTYLES_VALIGN = "super";
            //System.out.println("setting book-chapter vertical-align to: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN);
            styles.addRule("div.results p.book span { vertical-align: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_VALIGN = "initial";
            //System.out.println("btn8 :: setting book-chapter vertical-align to: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN);
            styles.addRule("div.results p.book span { vertical-align: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VALIGNBOOKCHAPTER", USERPREFS.BOOKCHAPTERSTYLES_VALIGN)){
            //System.out.println("VALIGNBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN);
        }
        else{
            //System.out.println("Error updating VALIGNBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButton8ItemStateChanged

    private void jToggleButton9ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton9ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton8.setSelected(false);
            USERPREFS.BOOKCHAPTERSTYLES_VALIGN = "sub";
            //System.out.println("setting book-chapter vertical-align to: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN);
            styles.addRule("div.results p.book span { vertical-align: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_VALIGN = "initial";
            //System.out.println("btn9 :: setting book-chapter vertical-align to: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN);
            styles.addRule("div.results p.book span { vertical-align: "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VALIGNBOOKCHAPTER", USERPREFS.BOOKCHAPTERSTYLES_VALIGN)){
            System.out.println("VALIGNBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_VALIGN);
        }
        else{
            //System.out.println("Error updating VALIGNBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButton9ItemStateChanged

    private void jToggleButton13ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton13ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton14.setSelected(false);
            vAlignVerseNumber = "super";
            //System.out.println("setting verse-number vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.sup { vertical-align: "+vAlignVerseNumber+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            vAlignVerseNumber = "initial";
            //System.out.println("btn13 :: setting verse-number vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.sup { vertical-align: "+vAlignVerseNumber+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }                
        
        if(biblegetDB.setStringOption("VALIGNVERSENUMBER", vAlignVerseNumber)){
            //System.out.println("VALIGNVERSENUMBER was successfully updated in database to value "+vAlignVerseNumber);
        }
        else{
            //System.out.println("Error updating VALIGNVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButton13ItemStateChanged

    private void jToggleButton14ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton14ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton13.setSelected(false);
            vAlignVerseNumber = "sub";
            //System.out.println("setting verse-number vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.sup { vertical-align: "+vAlignVerseNumber+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            vAlignVerseNumber = "initial";
            //System.out.println("btn14 :: setting verse-number vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.sup { vertical-align: "+vAlignVerseNumber+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VALIGNVERSENUMBER", vAlignVerseNumber)){
            //System.out.println("VALIGNVERSENUMBER was successfully updated in database to value "+vAlignVerseNumber);
        }
        else{
            //System.out.println("Error updating VALIGNVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButton14ItemStateChanged

    private void jToggleButton18ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton18ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton19.setSelected(false);
            USERPREFS.VERSETEXTSTYLES_VALIGN = BGET.VALIGN.SUPERSCRIPT;
            //System.out.println("setting verse-text vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.text { vertical-align: "+vAlignVerseText+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            vAlignVerseText = "initial";
            //System.out.println("btn18 :: setting verse-text vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.text { vertical-align: "+vAlignVerseText+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VERSENUMBERSTYLES_FONTSIZEVERSETEXTSTYLES_VALIGN", vAlignVerseText)){
            //System.out.println("VERSENUMBERSTYLES_FONTSIZEVERSETEXTSTYLES_VALIGN was successfully updated in database to value "+vAlignVerseText);
        }
        else{
            //System.out.println("Error updating VERSENUMBERSTYLES_FONTSIZEVERSETEXTSTYLES_VALIGN in database");
        }
    }//GEN-LAST:event_jToggleButton18ItemStateChanged

    private void jToggleButton19ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton19ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton18.setSelected(false);
            vAlignVerseText = "sub";
            //System.out.println("setting verse-text vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.text { vertical-align: "+vAlignVerseText+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            vAlignVerseText = "initial";
            //System.out.println("btn19 :: setting verse-text vertical-align to: "+vAlignVerseNumber);
            styles.addRule("div.results p.verses span.text { vertical-align: "+vAlignVerseText+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VERSENUMBERSTYLES_FONTSIZEVERSETEXTSTYLES_VALIGN", USERPREFS.VERSETEXTSTYLES_VALIGN)){
            //System.out.println("VERSENUMBERSTYLES_FONTSIZEVERSETEXTSTYLES_VALIGN was successfully updated in database to value "+USERPREFS.VERSETEXTSTYLES_VALIGN);
        }
        else{
            //System.out.println("Error updating VERSENUMBERSTYLES_FONTSIZEVERSETEXTSTYLES_VALIGN in database");
        }
    }//GEN-LAST:event_jToggleButton19ItemStateChanged

    private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox2ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            styles.addRule("div.results p.book { font-size:"+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE+"pt; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("BOOKCHAPTERSTYLES_FONTSIZE", USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE)){
                //System.out.println("FONTSIZEBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating FONTSIZEBOOKCHAPTER in database");
            }                
        }
    }//GEN-LAST:event_jComboBox2ItemStateChanged

    private void jComboBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox3ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.VERSENUMBERSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            styles.addRule("div.results p.verses span.sup { font-size:"+USERPREFS.VERSENUMBERSTYLES_FONTSIZE+"pt;}");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("VERSENUMBERSTYLES_FONTSIZE", USERPREFS.VERSENUMBERSTYLES_FONTSIZE)){
                //System.out.println("VERSENUMBERSTYLES_FONTSIZE was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating VERSENUMBERSTYLES_FONTSIZE in database");
            }                
        }
    }//GEN-LAST:event_jComboBox3ItemStateChanged

    private void jComboBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox4ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
          Object item = evt.getItem();
          // do something with object
            String fontsize = item.toString();
            USERPREFS.VERSETEXTSTYLES_FONTSIZE = Integer.parseInt(fontsize);
            styles.addRule("div.results p.verses span.text { font-size:"+USERPREFS.VERSETEXTSTYLES_FONTSIZE+"pt;}");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("VERSETEXTSTYLES_FONTSIZE", USERPREFS.VERSETEXTSTYLES_FONTSIZE)){
                //System.out.println("FONTSIZEVERSETEXT was successfully updated in database to value "+USERPREFS.VERSETEXTSTYLES_FONTSIZE);
            }
            else{
                //System.out.println("Error updating FONTSIZEVERSETEXT in database");
            }                
        }
    }//GEN-LAST:event_jComboBox4ItemStateChanged

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
          Object item = evt.getItem();
          // do something with object
            USERPREFS.PARAGRAPHSTYLES_FONTFAMILY = item.toString();
            styles.addRule("div.results { font-family: "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("PARAGRAPHSTYLES_FONTFAMILY", USERPREFS.PARAGRAPHSTYLES_FONTFAMILY)){
                //System.out.println("PARAGRAPHFONTFAMILY was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_FONTFAMILY);
            }
            else{
                //System.out.println("Error updating PARAGRAPHFONTFAMILY in database");
            }                
        }
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jComboBox6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox6ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            //Object item = evt.getItem();
            int selecIdx = jComboBox6.getSelectedIndex();
            //System.out.println("selected index: "+selecIdx);
            switch(selecIdx)
            {
                case 0: USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = 100; break;
                case 1: USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = 150; break;
                case 2: USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = 200; break;    
            }
            // do something with object
            
            styles.addRule("div.results p.book { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
            styles.addRule("div.results p.verses { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
            
            styleSheetRules.replace("paragraphLineSpacing1", "div.results p.book { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
            styleSheetRules.replace("paragraphLineSpacing2", "div.results p.verses { line-height: "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT+"%; }");
            String s = styleSheetRules.entrySet()
                         .stream()
                         .map(e -> e.getValue()) //e.getKey()+"="+
                         .collect(joining(System.lineSeparator()));
            System.out.println("styleSheetRules = ");
            System.out.println(s);

            String HTMLStrWithStyles = String.format(HTMLStr,s);
            browser.loadURL(DataUri.create("text/html",HTMLStrWithStyles));
            
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("PARAGRAPHSTYLES_LINEHEIGHT", USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT)){
                //System.out.println("PARAGRAPHLINESPACING was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT);
            }
            else{
                //System.out.println("Error updating PARAGRAPHLINESPACING in database");
            }                
        }
        
    }//GEN-LAST:event_jComboBox6ItemStateChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
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
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jComboBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox6ActionPerformed
    
        
    
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
                System.out.println("STRING");
                JsonString st = (JsonString) tree;
                System.out.println("key " + key + " | STRING " + st.getString());
                getStringOption(key,st.getString());
                break;
            case NUMBER:
                JsonNumber num = (JsonNumber) tree;
                System.out.println("NUMBER " + num.toString());
                getNumberOption(key,num.intValue());
                break;
            case TRUE:
                getBooleanOption(key,true);
                System.out.println("BOOLEAN " + tree.getValueType().toString());
                break;
            case FALSE:
                getBooleanOption(key,false);
                System.out.println("BOOLEAN " + tree.getValueType().toString());
                break;
            case NULL:
                System.out.println("NULL " + tree.getValueType().toString());
                break;
        }
    }
    
    private void getStringOption(String key,String value){
        switch(key){
            case "PARAGRAPHSTYLES_FONTFAMILY": USERPREFS.PARAGRAPHSTYLES_FONTFAMILY = value; break;
            case "BIBLEVERSIONSTYLES_TEXTCOLOR": USERPREFS.BIBLEVERSIONSTYLES_TEXTCOLOR = Color.decode(value); break; //will need to decode string representation of hex value with Color.decode(BGET.BIBLEVERSIONSTYLES_TEXTCOLOR)
            case "BIBLEVERSIONSTYLES_BGCOLOR": USERPREFS.BIBLEVERSIONSTYLES_BGCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "BOOKCHAPTERSTYLES_TEXTCOLOR": USERPREFS.BOOKCHAPTERSTYLES_TEXTCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "BOOKCHAPTERSTYLES_BGCOLOR": USERPREFS.BOOKCHAPTERSTYLES_BGCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "VERSENUMBERSTYLES_TEXTCOLOR": USERPREFS.VERSENUMBERSTYLES_TEXTCOLOR = Color.decode(value); break;
            case "VERSENUMBERSTYLES_BGCOLOR": USERPREFS.VERSENUMBERSTYLES_BGCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "VERSETEXTSTYLES_TEXTCOLOR": USERPREFS.VERSETEXTSTYLES_TEXTCOLOR = Color.decode(value); break; //decode string representation of hex value
            case "VERSETEXTSTYLES_BGCOLOR": USERPREFS.VERSETEXTSTYLES_BGCOLOR = Color.decode(value); break;
            case "PREFERREDVERSIONS": USERPREFS.PREFERREDVERSIONS = value; break;
        }
    }
    
    private void getNumberOption(String key,int value){
        switch(key){
            case "PARAGRAPHSTYLES_LINEHEIGHT": USERPREFS.PARAGRAPHSTYLES_LINEHEIGHT = value; break; //think of it as percent
            case "PARAGRAPHSTYLES_LEFTINDENT": USERPREFS.PARAGRAPHSTYLES_LEFTINDENT = value; break;
            case "PARAGRAPHSTYLES_RIGHTINDENT": USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT = value; break;
            case "PARAGRAPHSTYLES_ALIGNMENT": USERPREFS.PARAGRAPHSTYLES_ALIGNMENT = BGET.ALIGN.valueOf(value); break;
            case "BIBLEVERSIONSTYLES_FONTSIZE": USERPREFS.BIBLEVERSIONSTYLES_FONTSIZE = value; break;       
            case "BIBLEVERSIONSTYLES_VALIGN": USERPREFS.BIBLEVERSIONSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "BOOKCHAPTERSTYLES_FONTSIZE": USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE = value; break;
            case "BOOKCHAPTERSTYLES_VALIGN": USERPREFS.BOOKCHAPTERSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "VERSENUMBERSTYLES_FONTSIZE": USERPREFS.VERSENUMBERSTYLES_FONTSIZE = value; break;
            case "VERSENUMBERSTYLES_VALIGN": USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "VERSETEXTSTYLES_FONTSIZE": USERPREFS.VERSETEXTSTYLES_FONTSIZE = value; break;
            case "VERSETEXTSTYLES_VALIGN": USERPREFS.VERSETEXTSTYLES_VALIGN = BGET.VALIGN.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_SHOW": USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW = BGET.VISIBILITY.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_ALIGNMENT": USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT = BGET.ALIGN.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_POSITION": USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION = BGET.POS.valueOf(value); break;
            case "LAYOUTPREFS_BIBLEVERSION_WRAP": USERPREFS.LAYOUTPREFS_BIBLEVERSION_WRAP = BGET.WRAP.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT": USERPREFS.LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT = BGET.ALIGN.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_POSITION": USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION = BGET.POS.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_WRAP": USERPREFS.LAYOUTPREFS_BOOKCHAPTER_WRAP = BGET.WRAP.valueOf(value); break;
            case "LAYOUTPREFS_BOOKCHAPTER_FORMAT": USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT = BGET.FORMAT.valueOf(value); break;
            case "LAYOUTPREFS_VERSENUMBER_SHOW": USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW = BGET.VISIBILITY.valueOf(value); break;
        }    
    }
    
    private void getBooleanOption(String key,boolean value){
        switch(key){
            case "PARAGRAPHSTYLES_NOVERSIONFORMATTING": USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING = value; break;
            case "PARAGRAPHSTYLES_INTERFACEINCM": USERPREFS.PARAGRAPHSTYLES_INTERFACEINCM = value; break;
            case "BIBLEVERSIONSTYLES_BOLD": USERPREFS.BIBLEVERSIONSTYLES_BOLD = value; break;
            case "BIBLEVERSIONSTYLES_ITALIC": USERPREFS.BIBLEVERSIONSTYLES_ITALIC = value; break;
            case "BIBLEVERSIONSTYLES_UNDERLINE": USERPREFS.BIBLEVERSIONSTYLES_UNDERLINE = value; break;
            case "BIBLEVERSIONSTYLES_STRIKETHROUGH": USERPREFS.BIBLEVERSIONSTYLES_STRIKETHROUGH = value; break;
            case "BOOKCHAPTERSTYLES_BOLD": USERPREFS.BOOKCHAPTERSTYLES_BOLD = value; break;
            case "BOOKCHAPTERSTYLES_ITALIC": USERPREFS.BOOKCHAPTERSTYLES_ITALIC = value; break;
            case "BOOKCHAPTERSTYLES_UNDERLINE": USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE = value; break;
            case "BOOKCHAPTERSTYLES_STRIKETHROUGH": USERPREFS.BOOKCHAPTERSTYLES_STRIKETHROUGH = value; break;
            case "VERSENUMBERSTYLES_BOLD": USERPREFS.VERSENUMBERSTYLES_BOLD = value; break;
            case "VERSENUMBERSTYLES_ITALIC": USERPREFS.VERSENUMBERSTYLES_ITALIC = value; break;
            case "VERSENUMBERSTYLES_UNDERLINE": USERPREFS.VERSENUMBERSTYLES_UNDERLINE = value; break;
            case "VERSENUMBERSTYLES_STRIKETHROUGH": USERPREFS.VERSENUMBERSTYLES_STRIKETHROUGH = value; break;
            case "VERSETEXTSTYLES_BOLD": USERPREFS.VERSETEXTSTYLES_BOLD = value; break;
            case "VERSETEXTSTYLES_ITALIC": USERPREFS.VERSETEXTSTYLES_ITALIC = value; break;
            case "VERSETEXTSTYLES_UNDERLINE": USERPREFS.VERSETEXTSTYLES_UNDERLINE = value; break;
            case "VERSETEXTSTYLES_STRIKETHROUGH": USERPREFS.VERSETEXTSTYLES_STRIKETHROUGH = value; break;
            case "LAYOUTPREFS_BOOKCHAPTER_FULLQUERY": USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY = value; break;
        }    
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
                new OptionsFrame().setVisible(true);
            } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton13;
    private javax.swing.JToggleButton jToggleButton14;
    private javax.swing.JToggleButton jToggleButton15;
    private javax.swing.JToggleButton jToggleButton16;
    private javax.swing.JToggleButton jToggleButton17;
    private javax.swing.JToggleButton jToggleButton18;
    private javax.swing.JToggleButton jToggleButton19;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBar5;
    private javax.swing.JToolBar jToolBar6;
    private javax.swing.JToolBar jToolBar7;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setVisible(final boolean visible) {
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
