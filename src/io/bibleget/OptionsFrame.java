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
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
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
    private final LocalizedBibleBooks L10NBibleBooks;
        
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
    private final String previewDocument;
    private final String previewDocStylesheet;
    private final String previewDocScript;
    
    private final ImageIcon buttonStateOff = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state off.png"));
    private final ImageIcon buttonStateOn = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state on.png"));
    private final ImageIcon buttonStateOffHover = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state off hover.png"));
    private final ImageIcon buttonStateOnHover = new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/toggle button state on hover.png"));
    
    private static OptionsFrame instance;
        
    /**
     * Creates new form OptionsFrame
     * @param pkgPath
     */
    private OptionsFrame() throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception {
        this.L10NBibleBooks = LocalizedBibleBooks.getInstance();
        this.FFLCRenderer = new FontFamilyListCellRenderer();
                
        //jTextPane does not initialize correctly, it causes a Null Exception Pointer
        //Following line keeps this from crashing the program
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        // Get preferences from database       
        //System.out.println("getting instance of BibleGetDB");
        biblegetDB = BibleGetDB.getInstance();
        USERPREFS = Preferences.getInstance();
        if(USERPREFS != null){
            System.out.println("USERPREFS is not null at least.");
            System.out.println("USERPREFS.toString = " + USERPREFS.toString());
            Field[] fields = Preferences.class.getFields();
            System.out.println(Arrays.toString(fields));
        }
        //System.out.println("getting JsonObject of biblegetDB options");
        this.fontFamilies = BibleGetIO.getFontFamilies();
        
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
        LocalizedBibleBook LocalizedBookSamuel = (L10NBibleBooks != null ? L10NBibleBooks.GetBookByIndex(8) : new LocalizedBibleBook("1Sam","1Samuel") );
        switch(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT){
            case BIBLELANG:
                bookChapter = "I Samuelis 1";
                break;
            case BIBLELANGABBREV:
                bookChapter = "I Sam 1";
                break;
            case USERLANG:
                bookChapter = LocalizedBookSamuel.Fullname + " 1";
                break;
            case USERLANGABBREV:
                bookChapter = LocalizedBookSamuel.Abbrev + " 1";
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
            + "div.results .bibleVersion { text-align: " + USERPREFS.LAYOUTPREFS_BIBLEVERSION_ALIGNMENT.getCSSValue() + "; }"
            + "div.results .bookChapter { font-family: " + USERPREFS.PARAGRAPHSTYLES_FONTFAMILY + "; }"
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
        
        previewDocScript = "<script>"
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
            + "let leftindent = " + String.format(Locale.ROOT, "%.1f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)) + " * pixelRatioVals.dpi + 35;"
            + "let rightindent = " + String.format(Locale.ROOT, "%.1f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)) + " * pixelRatioVals.dpi + 35;"
            + "let bestWidth = 7 * 96 * window.devicePixelRatio + (35*2);"
            + "$('.bibleQuote').css({\"width\":bestWidth+\"px\",\"padding-left\":leftindent+\"px\",\"padding-right\":rightindent+\"px\"});"
            + "drawRuler(7," + (USERPREFS.PARAGRAPHSTYLES_INTERFACEINCM ? "true" : "false") + "," + String.format(Locale.ROOT, "%.1f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)) + "," + String.format(Locale.ROOT, "%.1f", Double.valueOf(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)) + ");"
            + "});"
            + "</script>";
        
        previewDocument = "<html><head><meta charset=\"utf-8\">" + previewDocStylesheet
            + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>" 
            + previewDocScript 
            + "</head><body>"
            + "<div style=\"text-align: center;\"><canvas class=\"previewRuler\"></canvas></div>"
            + "<div class=\"results bibleQuote\">"
            + (USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION == BGET.POS.TOP && USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.SHOW ? "<p class=\"bibleVersion\">" + bibleVersionWrapBefore + "NVBSE" + bibleVersionWrapAfter + "</p>" : "")
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.TOP ? "<p class=\"bookChapter\">" + bookChapter + "</p>" : "")
            + "<p class=\"versesParagraph\" style=\"margin-top:0px;\">"
            + (USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW ? "<span class=\"verseNum\">1</span>" : "")
            + "<span class=\"verseText\">Fuit vir unus de Ramathaim Suphita de monte Ephraim, et nomen eius Elcana filius Ieroham filii Eliu filii Thohu filii Suph, Ephrathaeus.</span>"
            + (USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW ? "<span class=\"verseNum\">2</span>" : "")
            + "<span class=\"verseText\">Et habuit duas uxores: nomen uni Anna et nomen secundae Phenenna. Fueruntque Phenennae filii, Annae autem non erant liberi.</span>"
            + (USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW == BGET.VISIBILITY.SHOW ? "<span class=\"verseNum\">3</span>" : "")
            + "<span class=\"verseText\">Et ascendebat vir ille de civitate sua singulis annis, ut adoraret et sacrificaret Domino exercituum in Silo. Erant autem ibi duo filii Heli, Ophni et Phinees, sacerdotes Domini.</span>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.BOTTOMINLINE ? "<span class=\"bookChapter\">" + bookChapter + "</span>" : "")
            + "</p>"
            + (USERPREFS.LAYOUTPREFS_BOOKCHAPTER_POSITION == BGET.POS.BOTTOM ? "<p class=\"bookChapter\">" + bookChapter + "</p>" : "")
            + (USERPREFS.LAYOUTPREFS_BIBLEVERSION_POSITION == BGET.POS.BOTTOM && USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW == BGET.VISIBILITY.SHOW ? "<p class=\"bibleVersion\">" + bibleVersionWrapBefore + "NVBSE" + bibleVersionWrapAfter + "</p>" : "")
            + "</div>"
            + "</body>";

        
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int)screenSize.getWidth();
        screenHeight = (int)screenSize.getHeight();
        frameWidth = 732;
        frameHeight = 790;
        frameLeft = (screenWidth / 2) - (frameWidth / 2);
        frameTop = (screenHeight / 2) - (frameHeight / 2);
                
        //this.myMessages = BibleGetI18N.getMessages();
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = OS.isLinux();
        cefApp = CefApp.getInstance(settings);
        client = cefApp.createClient();
        //String HTMLStrWithStyles = String.format(HTMLStr,s);
        browser = client.createBrowser( DataUri.create("text/html",previewDocument), OS.isLinux(), false);
        browserUI = browser.getUIComponent();
        
        initComponents();
        //jInternalFrame1.setLayout(new BorderLayout());
        jInternalFrame1.getContentPane().add(browserUI, BorderLayout.CENTER);
        //jInternalFrame1.setSize(400, 300);
        //jInternalFrame1.setVisible(true);
    }

    public static OptionsFrame getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception
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

        buttonGroupParagraphAlign = new javax.swing.ButtonGroup();
        buttonGroupBibleVersionAlign = new javax.swing.ButtonGroup();
        buttonGroupBibleVersionVAlign = new javax.swing.ButtonGroup();
        buttonGroupBibleVersionWrap = new javax.swing.ButtonGroup();
        buttonGroupBookChapterAlign = new javax.swing.ButtonGroup();
        buttonGroupBookChapterVAlign = new javax.swing.ButtonGroup();
        buttonGroupBookChapterFormat = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
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
        jPanelParagraphLineHeight = new javax.swing.JPanel();
        jToolBarParagraphLineHeight = new javax.swing.JToolBar();
        jComboBoxParagraphLineHeight = new javax.swing.JComboBox();
        jPanelParagraphFontFamily = new javax.swing.JPanel();
        jToolBarParagraphFontFamily = new javax.swing.JToolBar();
        jComboBoxParagraphFontFamily = new javax.swing.JComboBox();
        jPanelParagraphIndentRight = new javax.swing.JPanel();
        jToolBarParagraphIndentRight = new javax.swing.JToolBar();
        jButtonRightIndentMore = new javax.swing.JButton();
        jButtonRightIndentLess = new javax.swing.JButton();
        jPanelBibleVersion = new javax.swing.JPanel();
        jToolBar8 = new javax.swing.JToolBar();
        jToggleButtonBibleVersionBold = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionItalic = new javax.swing.JToggleButton();
        jToggleButtonBibleVersionUnderline = new javax.swing.JToggleButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        jButtonBibleVersionTextColor = new javax.swing.JButton();
        jButtonBibleVersionHighlightColor = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        jComboBoxBibleVersionFontSize = new javax.swing.JComboBox();
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
        jPanelBookChapter = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jToggleButtonBookChapterBold = new javax.swing.JToggleButton();
        jToggleButtonBookChapterItalic = new javax.swing.JToggleButton();
        jToggleButtonBookChapterUnderline = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonBookChapterTextColor = new javax.swing.JButton();
        jButtonBookChapterHighlightColor = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jComboBoxBookChapterFontSize = new javax.swing.JComboBox();
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
        jPanelVerseNumber = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        jToggleButtonVerseNumberBold = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberItalic = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberUnderline = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonVerseNumberTextColor = new javax.swing.JButton();
        jButtonVerseNumberHighlightColor = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jComboBoxVerseNumberFontSize = new javax.swing.JComboBox();
        jToggleButtonVerseNumberSuperscript = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberSubscript = new javax.swing.JToggleButton();
        jPanelVerseText = new javax.swing.JPanel();
        jToolBar4 = new javax.swing.JToolBar();
        jToggleButtonVerseTextBold = new javax.swing.JToggleButton();
        jToggleButtonVerseTextItalic = new javax.swing.JToggleButton();
        jToggleButtonVerseTextUnderline = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButtonVerseTextTextColor = new javax.swing.JButton();
        jButtonVerseTextHighlightColor = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jComboBoxVerseTextFontSize = new javax.swing.JComboBox();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxUseVersionFormatting = new javax.swing.JCheckBox();
        jToggleButtonBookChapterFullRef = new javax.swing.JToggleButton();
        jToggleButtonVerseNumberVisibility = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jToggleButtonBibleVersionVisibility = new javax.swing.JToggleButton();
        jToggleButtonBookChapterBibleLang = new javax.swing.JToggleButton();
        jToggleButtonBookChapterBibleLangAbbrev = new javax.swing.JToggleButton();
        jToggleButtonBookChapterUserLang = new javax.swing.JToggleButton();
        jToggleButtonBookChapterUserLangAbbrev = new javax.swing.JToggleButton();
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

        jPanelParagraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Paragraph"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanelParagraph.setName("Paragraph"); // NOI18N

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
                .addGap(0, 0, 0))
        );
        jPanelParagraphLineHeightLayout.setVerticalGroup(
            jPanelParagraphLineHeightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParagraphLineHeightLayout.createSequentialGroup()
                .addComponent(jToolBarParagraphLineHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jPanelParagraphFontFamily.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Font")));

        jToolBarParagraphFontFamily.setFloatable(false);
        jToolBarParagraphFontFamily.setRollover(true);

        jComboBoxParagraphFontFamily.setFont(new java.awt.Font(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY, java.awt.Font.PLAIN, 18));
        jComboBoxParagraphFontFamily.setMaximumRowCount(6);
        jComboBoxParagraphFontFamily.setModel(fontFamilies);
        jComboBoxParagraphFontFamily.setSelectedItem(USERPREFS.PARAGRAPHSTYLES_FONTFAMILY);
        jComboBoxParagraphFontFamily.setPreferredSize(new java.awt.Dimension(300, 41));
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

        jPanelParagraphIndentRight.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), __("Right Indent")));

        jToolBarParagraphIndentRight.setFloatable(false);
        jToolBarParagraphIndentRight.setRollover(true);

        jButtonRightIndentMore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/increase_indent.png"))); // NOI18N
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

        jButtonRightIndentLess.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/decrease_indent.png"))); // NOI18N
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

        javax.swing.GroupLayout jPanelParagraphLayout = new javax.swing.GroupLayout(jPanelParagraph);
        jPanelParagraph.setLayout(jPanelParagraphLayout);
        jPanelParagraphLayout.setHorizontalGroup(
            jPanelParagraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParagraphLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanelParagraphAlignment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParagraphIndentLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParagraphIndentRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParagraphLineHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanelParagraphFontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanelParagraphLayout.setVerticalGroup(
            jPanelParagraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParagraphLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelParagraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelParagraphAlignment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelParagraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelParagraphIndentLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanelParagraphLineHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanelParagraphFontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanelParagraphIndentRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanelBibleVersion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Bible Version"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

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
        jToolBar8.add(jSeparator9);

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
        jToolBar8.add(jSeparator12);

        buttonGroupBibleVersionAlign.add(jToggleButtonBibleVersionAlignLeft);
        jToggleButtonBibleVersionAlignLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_left.png"))); // NOI18N
        jToggleButtonBibleVersionAlignLeft.setFocusable(false);
        jToggleButtonBibleVersionAlignLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionAlignLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar8.add(jToggleButtonBibleVersionAlignLeft);

        buttonGroupBibleVersionAlign.add(jToggleButtonBibleVersionAlignCenter);
        jToggleButtonBibleVersionAlignCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_center.png"))); // NOI18N
        jToggleButtonBibleVersionAlignCenter.setFocusable(false);
        jToggleButtonBibleVersionAlignCenter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionAlignCenter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar8.add(jToggleButtonBibleVersionAlignCenter);

        buttonGroupBibleVersionAlign.add(jToggleButtonBibleVersionAlignRight);
        jToggleButtonBibleVersionAlignRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_right.png"))); // NOI18N
        jToggleButtonBibleVersionAlignRight.setFocusable(false);
        jToggleButtonBibleVersionAlignRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionAlignRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar8.add(jToggleButtonBibleVersionAlignRight);
        jToolBar8.add(jSeparator10);

        buttonGroupBibleVersionWrap.add(jToggleButtonBibleVersionWrapNone);
        jToggleButtonBibleVersionWrapNone.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jToggleButtonBibleVersionWrapNone.setText("NONE");
        jToggleButtonBibleVersionWrapNone.setFocusable(false);
        jToggleButtonBibleVersionWrapNone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToolBar8.add(jToggleButtonBibleVersionWrapNone);

        buttonGroupBibleVersionWrap.add(jToggleButtonBibleVersionWrapParentheses);
        jToggleButtonBibleVersionWrapParentheses.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBibleVersionWrapParentheses.setText("()");
        jToggleButtonBibleVersionWrapParentheses.setFocusable(false);
        jToggleButtonBibleVersionWrapParentheses.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToolBar8.add(jToggleButtonBibleVersionWrapParentheses);

        buttonGroupBibleVersionWrap.add(jToggleButtonBibleVersionWrapBrackets);
        jToggleButtonBibleVersionWrapBrackets.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBibleVersionWrapBrackets.setText("[]");
        jToggleButtonBibleVersionWrapBrackets.setFocusable(false);
        jToggleButtonBibleVersionWrapBrackets.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToolBar8.add(jToggleButtonBibleVersionWrapBrackets);
        jToolBar8.add(jSeparator11);

        buttonGroupBibleVersionVAlign.add(jToggleButtonBibleVersionVAlignTop);
        jToggleButtonBibleVersionVAlignTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_above.png"))); // NOI18N
        jToggleButtonBibleVersionVAlignTop.setFocusable(false);
        jToggleButtonBibleVersionVAlignTop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionVAlignTop.setMaximumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignTop.setMinimumSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignTop.setPreferredSize(new java.awt.Dimension(31, 31));
        jToggleButtonBibleVersionVAlignTop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar8.add(jToggleButtonBibleVersionVAlignTop);

        buttonGroupBibleVersionVAlign.add(jToggleButtonBibleVersionVAlignBottom);
        jToggleButtonBibleVersionVAlignBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_below.png"))); // NOI18N
        jToggleButtonBibleVersionVAlignBottom.setFocusable(false);
        jToggleButtonBibleVersionVAlignBottom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBibleVersionVAlignBottom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar8.add(jToggleButtonBibleVersionVAlignBottom);

        javax.swing.GroupLayout jPanelBibleVersionLayout = new javax.swing.GroupLayout(jPanelBibleVersion);
        jPanelBibleVersion.setLayout(jPanelBibleVersionLayout);
        jPanelBibleVersionLayout.setHorizontalGroup(
            jPanelBibleVersionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBibleVersionLayout.createSequentialGroup()
                .addComponent(jToolBar8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelBibleVersionLayout.setVerticalGroup(
            jPanelBibleVersionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar8, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanelBookChapter.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Book / Chapter"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

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
        jToolBar2.add(jSeparator4);

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
        jToolBar2.add(jSeparator13);

        buttonGroupBookChapterAlign.add(jToggleButtonBookChapterAlignLeft);
        jToggleButtonBookChapterAlignLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_left.png"))); // NOI18N
        jToggleButtonBookChapterAlignLeft.setFocusable(false);
        jToggleButtonBookChapterAlignLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterAlignLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jToggleButtonBookChapterAlignLeft);

        buttonGroupBookChapterAlign.add(jToggleButtonBookChapterAlignCenter);
        jToggleButtonBookChapterAlignCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_center.png"))); // NOI18N
        jToggleButtonBookChapterAlignCenter.setFocusable(false);
        jToggleButtonBookChapterAlignCenter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterAlignCenter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jToggleButtonBookChapterAlignCenter);

        buttonGroupBookChapterAlign.add(jToggleButtonBookChapterAlignRight);
        jToggleButtonBookChapterAlignRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/align_right.png"))); // NOI18N
        jToggleButtonBookChapterAlignRight.setFocusable(false);
        jToggleButtonBookChapterAlignRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterAlignRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jToggleButtonBookChapterAlignRight);
        jToolBar2.add(jSeparator15);

        jToggleButtonBookChapterWrapNone.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jToggleButtonBookChapterWrapNone.setText("NONE");
        jToggleButtonBookChapterWrapNone.setFocusable(false);
        jToggleButtonBookChapterWrapNone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToolBar2.add(jToggleButtonBookChapterWrapNone);

        jToggleButtonBookChapterWrapParentheses.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBookChapterWrapParentheses.setText("()");
        jToggleButtonBookChapterWrapParentheses.setFocusable(false);
        jToggleButtonBookChapterWrapParentheses.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToolBar2.add(jToggleButtonBookChapterWrapParentheses);

        jToggleButtonBookChapterWrapBrackets.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButtonBookChapterWrapBrackets.setText("[]");
        jToggleButtonBookChapterWrapBrackets.setFocusable(false);
        jToggleButtonBookChapterWrapBrackets.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToolBar2.add(jToggleButtonBookChapterWrapBrackets);
        jToolBar2.add(jSeparator14);

        buttonGroupBookChapterVAlign.add(jToggleButtonBookChapterVAlignTop);
        jToggleButtonBookChapterVAlignTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_above.png"))); // NOI18N
        jToggleButtonBookChapterVAlignTop.setFocusable(false);
        jToggleButtonBookChapterVAlignTop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterVAlignTop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jToggleButtonBookChapterVAlignTop);

        buttonGroupBookChapterVAlign.add(jToggleButtonBookChapterVAlignBottom);
        jToggleButtonBookChapterVAlignBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_below.png"))); // NOI18N
        jToggleButtonBookChapterVAlignBottom.setFocusable(false);
        jToggleButtonBookChapterVAlignBottom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterVAlignBottom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jToggleButtonBookChapterVAlignBottom);

        buttonGroupBookChapterVAlign.add(jToggleButtonBookChapterVAlignBottominline);
        jToggleButtonBookChapterVAlignBottominline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/position_inline.png"))); // NOI18N
        jToggleButtonBookChapterVAlignBottominline.setFocusable(false);
        jToggleButtonBookChapterVAlignBottominline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBookChapterVAlignBottominline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jToggleButtonBookChapterVAlignBottominline);

        javax.swing.GroupLayout jPanelBookChapterLayout = new javax.swing.GroupLayout(jPanelBookChapter);
        jPanelBookChapter.setLayout(jPanelBookChapterLayout);
        jPanelBookChapterLayout.setHorizontalGroup(
            jPanelBookChapterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBookChapterLayout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelBookChapterLayout.setVerticalGroup(
            jPanelBookChapterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanelVerseNumber.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Verse Number"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

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
        jToolBar3.add(jSeparator5);

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

        javax.swing.GroupLayout jPanelVerseNumberLayout = new javax.swing.GroupLayout(jPanelVerseNumber);
        jPanelVerseNumber.setLayout(jPanelVerseNumberLayout);
        jPanelVerseNumberLayout.setHorizontalGroup(
            jPanelVerseNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelVerseNumberLayout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelVerseNumberLayout.setVerticalGroup(
            jPanelVerseNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanelVerseText.setBorder(javax.swing.BorderFactory.createTitledBorder(null, __("Verse Text"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

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
        jToolBar4.add(jSeparator6);

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

        javax.swing.GroupLayout jPanelVerseTextLayout = new javax.swing.GroupLayout(jPanelVerseText);
        jPanelVerseText.setLayout(jPanelVerseTextLayout);
        jPanelVerseTextLayout.setHorizontalGroup(
            jPanelVerseTextLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelVerseTextLayout.createSequentialGroup()
                .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelVerseTextLayout.setVerticalGroup(
            jPanelVerseTextLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jLabel2.setText("<html><head><style>body#versionformatting{border:none;background-color:rgb(214,217,223);}</style></head><body id=\"versionformatting\">"+__("Some Bible versions have their own formatting. This is left by default to keep the text as close as possible to the original.<br> If however you need to have consistent formatting in your document, you may override the Bible version's own formatting.")+"</body></html>");
        jLabel2.setOpaque(true);

        jCheckBoxUseVersionFormatting.setSelected(USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING);
        jCheckBoxUseVersionFormatting.setText(__("Override Bible Version Formatting"));
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
        jToggleButtonBookChapterFullRef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonBookChapterFullRefActionPerformed(evt);
            }
        });

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

        jPanel1.setLayout(new java.awt.GridLayout(4, 1));

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

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterBibleLang);
        jToggleButtonBookChapterBibleLang.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.BIBLELANG);
        jToggleButtonBookChapterBibleLang.setText("Bible Lang");

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterBibleLangAbbrev);
        jToggleButtonBookChapterBibleLangAbbrev.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.BIBLELANGABBREV);
        jToggleButtonBookChapterBibleLangAbbrev.setText("Bible Lang Abbrev");

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterUserLang);
        jToggleButtonBookChapterUserLang.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.USERLANG);
        jToggleButtonBookChapterUserLang.setText("User Lang");

        buttonGroupBookChapterFormat.add(jToggleButtonBookChapterUserLangAbbrev);
        jToggleButtonBookChapterUserLangAbbrev.setSelected(USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FORMAT==BGET.FORMAT.USERLANGABBREV);
        jToggleButtonBookChapterUserLangAbbrev.setText("User Lang Abbrev");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelParagraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxUseVersionFormatting, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator7)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanelBibleVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggleButtonBibleVersionVisibility))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanelVerseNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggleButtonVerseNumberVisibility))
                            .addComponent(jPanelVerseText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanelBookChapter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jToggleButtonBookChapterFullRef)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButtonBookChapterBibleLang, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButtonBookChapterBibleLangAbbrev)
                            .addComponent(jToggleButtonBookChapterUserLang, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButtonBookChapterUserLangAbbrev, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 26, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jPanelParagraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelBibleVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonBibleVersionVisibility, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanelBookChapter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jToggleButtonBookChapterFullRef, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanelVerseNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jToggleButtonVerseNumberVisibility, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelVerseText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jToggleButtonBookChapterBibleLang)
                        .addGap(0, 0, 0)
                        .addComponent(jToggleButtonBookChapterBibleLangAbbrev)
                        .addGap(0, 0, 0)
                        .addComponent(jToggleButtonBookChapterUserLang)
                        .addGap(0, 0, 0)
                        .addComponent(jToggleButtonBookChapterUserLangAbbrev)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxUseVersionFormatting)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jInternalFrame1.setBorder(null);
        jInternalFrame1.setMinimumSize(new java.awt.Dimension(22, 260));
        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jInternalFrame1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jInternalFrame1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED) {
            // do something with object
            USERPREFS.LAYOUTPREFS_VERSENUMBER_SHOW = BGET.VISIBILITY.HIDE;
            jToggleButtonVerseNumberVisibility.setIcon(buttonStateOff);
            jToggleButtonVerseNumberVisibility.setText("HIDE");
        }
        if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_SHOW", USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW.getValue())){
            //System.out.println("NOVERSIONFORMATTING was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_NOVERSIONFORMATTING);
        }
        else{
            //System.out.println("Error updating NOVERSIONFORMATTING in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberVisibilityItemStateChanged

    private void jToggleButtonBookChapterFullRefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonBookChapterFullRefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonBookChapterFullRefActionPerformed

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
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOn);
            //jToggleButtonBookChapterFullRef.setText("SHOW");
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED) {
            // do something with object
            USERPREFS.LAYOUTPREFS_BOOKCHAPTER_FULLQUERY = false;
            jToggleButtonBookChapterFullRef.setIcon(buttonStateOff);
            //jToggleButtonBookChapterFullRef.setText("HIDE");
        }
        if(biblegetDB.setIntOption("LAYOUTPREFS_BIBLEVERSION_SHOW", USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW.getValue())){
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSETEXTSTYLES_UNDERLINE = false;
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSETEXTSTYLES_ITALIC = false;
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSETEXTSTYLES_BOLD = false;
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
            //System.out.println("setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.NORMAL;
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
            //System.out.println("setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_VALIGN = BGET.VALIGN.NORMAL;
            //System.out.println("btn13 :: setting verse-number vertical-align to: "+USERPREFS.VERSENUMBERSTYLES_VALIGN);
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_UNDERLINE = false;
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_ITALIC = false;
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.VERSENUMBERSTYLES_BOLD = false;
        }

        if(biblegetDB.setBooleanOption("VERSENUMBERSTYLES_BOLD", USERPREFS.VERSENUMBERSTYLES_BOLD)){
            //System.out.println("BOLDVERSENUMBER was successfully updated in database to value "+USERPREFS.VERSENUMBERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDVERSENUMBER in database");
        }
    }//GEN-LAST:event_jToggleButtonVerseNumberBoldItemStateChanged

    private void jComboBoxBookChapterFontSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxBookChapterFontSizeItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            USERPREFS.BOOKCHAPTERSTYLES_FONTSIZE = Integer.parseInt(fontsize);
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_UNDERLINE = false;
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_ITALIC = false;
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
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            USERPREFS.BOOKCHAPTERSTYLES_BOLD = false;
        }

        if(biblegetDB.setBooleanOption("BOOKCHAPTERSTYLES_BOLD", USERPREFS.BOOKCHAPTERSTYLES_BOLD)){
            //System.out.println("BOLDBOOKCHAPTER was successfully updated in database to value "+USERPREFS.BOOKCHAPTERSTYLES_BOLD);
        }
        else{
            //System.out.println("Error updating BOLDBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButtonBookChapterBoldItemStateChanged

    private void jComboBoxBibleVersionFontSizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxBibleVersionFontSizeItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxBibleVersionFontSizeItemStateChanged

    private void jButtonBibleVersionHighlightColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBibleVersionHighlightColorMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonBibleVersionHighlightColorMouseClicked

    private void jButtonBibleVersionTextColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBibleVersionTextColorMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonBibleVersionTextColorMouseClicked

    private void jToggleButtonBibleVersionUnderlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionUnderlineItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonBibleVersionUnderlineItemStateChanged

    private void jToggleButtonBibleVersionItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionItalicItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonBibleVersionItalicItemStateChanged

    private void jToggleButtonBibleVersionBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionBoldItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonBibleVersionBoldItemStateChanged

    private void jButtonRightIndentLessMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRightIndentLessMouseClicked
        if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT>=5){USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT -= 5;}

        if(biblegetDB.setIntOption("PARAGRAPHSTYLES_RIGHTINDENT", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)){
            //System.out.println("PARAGRAPHSTYLES_RIGHTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT);
        }
        else{
            //System.out.println("Error updating PARAGRAPHSTYLES_RIGHTINDENT in database");
        }
    }//GEN-LAST:event_jButtonRightIndentLessMouseClicked

    private void jButtonRightIndentMoreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRightIndentMoreMouseClicked
        if(USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT<50){USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT += 5;}

        if(biblegetDB.setIntOption("PARAGRAPHSTYLES_RIGHTINDENT", USERPREFS.PARAGRAPHSTYLES_RIGHTINDENT)){
            //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
        }
        else{
            //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
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
        if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT>=5){USERPREFS.PARAGRAPHSTYLES_LEFTINDENT -= 5;}

        if(biblegetDB.setIntOption("PARAGRAPHSTYLES_LEFTINDENT", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)){
            //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
        }
        else{
            //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
        }
    }//GEN-LAST:event_jButtonLeftIndentLessMouseClicked

    private void jButtonLeftIndentMoreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonLeftIndentMoreMouseClicked
        if(USERPREFS.PARAGRAPHSTYLES_LEFTINDENT<50){USERPREFS.PARAGRAPHSTYLES_LEFTINDENT += 5;}

        if(biblegetDB.setIntOption("PARAGRAPHSTYLES_LEFTINDENT", USERPREFS.PARAGRAPHSTYLES_LEFTINDENT)){
            //System.out.println("PARAGRAPHSTYLES_LEFTINDENT was successfully updated in database to value "+USERPREFS.PARAGRAPHSTYLES_LEFTINDENT);
        }
        else{
            //System.out.println("Error updating PARAGRAPHSTYLES_LEFTINDENT in database");
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

    private void jToggleButtonBibleVersionVisibilityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW = BGET.VISIBILITY.SHOW;
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOn);
            jToggleButtonBibleVersionVisibility.setText("SHOW");
        }
        else {
            USERPREFS.LAYOUTPREFS_BIBLEVERSION_SHOW = BGET.VISIBILITY.HIDE;
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOff);
            jToggleButtonBibleVersionVisibility.setText("HIDE");
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityItemStateChanged

    private void jToggleButtonBibleVersionVisibilityMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityMouseEntered
        if(jToggleButtonBibleVersionVisibility.isSelected()){
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOnHover);
        } else {
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOffHover);
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityMouseEntered

    private void jToggleButtonBibleVersionVisibilityMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityMouseExited
        if(jToggleButtonBibleVersionVisibility.isSelected()){
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOn);
        } else {
            jToggleButtonBibleVersionVisibility.setIcon(buttonStateOff);
        }
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityMouseExited

    private void jToggleButtonBibleVersionVisibilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonBibleVersionVisibilityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonBibleVersionVisibilityActionPerformed

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
                new OptionsFrame().setVisible(true);
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
    private javax.swing.ButtonGroup buttonGroupParagraphAlign;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
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
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
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
