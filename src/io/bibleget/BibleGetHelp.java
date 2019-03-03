/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import static io.bibleget.BibleGetI18N.__;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Lwangaman
 */
public class BibleGetHelp extends javax.swing.JFrame {

    private static BibleGetHelp instance;
    private LocalCellRenderer renderer;
    
    private final HTMLEditorKit kit;
    private final Document doc;
    private final String HTMLStr0;
    private final String HTMLStr1;
    private final String HTMLStr2;
    private final String HTMLStr3;
    private final StyleSheet styles;

    private final int screenWidth;
    private final int screenHeight;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameLeft;
    private final int frameTop;
    
    private final BibleGetDB bibleGetDB;
    //private JsonArray bibleVersionsObj;
    private JsonArray bibleBooksLangsObj;
    private final int bibleBooksLangsAmount;
    //private final int booksLangs;
    //private final String booksStr;
    private final String langsStr;
    private String langsSupported;
    private List<String> langsLocalized;
    
    private Map<String,String> booksAndAbbreviations;
    
    /**
     * Creates new form BibleGetHelp
     */
    private BibleGetHelp() throws ClassNotFoundException, UnsupportedEncodingException, SQLException {
        this.langsLocalized = new ArrayList<>();
        //jTextPane does not initialize correctly, it causes a Null Exception Pointer
        //Following line keeps this from crashing the program
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        String packagePath = BibleGetIO.getPackagePath();
        //String locale = BibleGetIO.getLocale();
                
        String bbBooks;
        
        bibleGetDB = BibleGetDB.getInstance();
        if(bibleGetDB != null){
            //System.out.println("oh good, biblegetDB is not null!");
            JsonReader jsonReader;
            langsSupported = bibleGetDB.getMetaData("LANGUAGES");
            //System.out.println("BibleGetHelp.java: langsSupported = "+langsSupported);
            jsonReader = Json.createReader(new StringReader(langsSupported));
            bibleBooksLangsObj = jsonReader.readArray();
            bibleBooksLangsAmount = bibleBooksLangsObj.size(); //il numero di lingue in cui vengono riconosciuti i nomi dei libri della Bibbia?
            bibleBooksLangsObj.stream().map((jsonValue) -> (JsonString) jsonValue).map((langToLocalize) -> BibleGetI18N.localizeLanguage(langToLocalize.getString())).forEach((localizedLang) -> {
                //System.out.println("BibleGetHelp.java: supported language = "+langToLocalize.getString()+", localized = "+localizedLang);
                langsLocalized.add(localizedLang);
            });
            Collections.sort(langsLocalized);
            langsStr = StringUtils.join(langsLocalized,", ");
            System.out.println("BibleGetHelp.java: langsStr = "+langsStr);
            
            List<JsonArray> bibleBooksTemp = new ArrayList<>();
            for(int i=0;i<73;i++) {
                bbBooks = bibleGetDB.getMetaData("BIBLEBOOKS"+Integer.toString(i));
                jsonReader = Json.createReader(new StringReader(bbBooks));
                JsonArray bibleBooksObj = jsonReader.readArray();
                bibleBooksTemp.add(bibleBooksObj);
            }
            
            String curLang;
            //List<String[]> booksForCurLang;
            //bibleBooks = new HashMap<>();
            booksAndAbbreviations = new HashMap<>();
            String buildStr;
            for(int q=0;q<bibleBooksLangsObj.size();q++) {
                curLang = (bibleBooksLangsObj.getString(q) != null) ? BibleGetI18N.localizeLanguage(bibleBooksLangsObj.getString(q)).toUpperCase() : "";
                buildStr = "";
                for(int i=0;i<73;i++) {
                    
                    String styleStr = "";
                    if(bibleBooksLangsObj.getString(q).equals("TAMIL") || bibleBooksLangsObj.getString(q).equals("KOREAN")) {
                        styleStr = " style=\"font-family:'Arial Unicode MS';\"";
                    }
                    
                    JsonArray curBook = bibleBooksTemp.get(i);
                    JsonArray curBookCurLang = curBook.getJsonArray(q);
                    String str1 = (curBookCurLang.getString(0) != null) ? curBookCurLang.getString(0) : "";
                    String str2 = (curBookCurLang.getString(1) != null) ? curBookCurLang.getString(1) : "";
                    buildStr += "<tr><td"+styleStr+">"+str1+"</td><td"+styleStr+">"+str2+"</td></tr>";
                    //buildStr += "<tr><td style=\"font-family:'Arial Unicode MS';\">"+str1+"</td><td style=\"font-family:'Arial Unicode MS';\">"+str2+"</td></tr>";
                }
                booksAndAbbreviations.put(curLang,buildStr);
            }
            
        }
        else{
            bibleBooksLangsAmount = 0;
            langsStr = "";
        }
        kit = new HTMLEditorKit();
        doc = kit.createDefaultDocument();
        styles = kit.getStyleSheet();
        styles.addRule("body { background-color: #FFFFDD; border: 2px inset #CC9900; }");
        styles.addRule("h1 { color: #0000AA; }");
        styles.addRule("h2 { color: #0000AA; }");
        styles.addRule("h3 { color: #0000AA; }");
        styles.addRule("p { text-align: justify; }");
        styles.addRule("table { border-collapse: collapse; width: 600px; margin-left: auto; }");
        styles.addRule("th { text-align: center; border: 4px ridge #DEB887; background-color: #F5F5DC; padding: 3px; }");
        styles.addRule("td { text-align: justify; border: 3px ridge #DEB887; background-color: #F5F5DC; padding: 3px; }");
        
        //System.out.println("We have package path in BibleGetHelp! It is: "+packagePath);
        
        HTMLStr0 = "<html><head><meta charset=\"utf-8\"></head><body>"
                + "<h1>"+__("Help for BibleGet (Open Office Writer)")+"</h1>"
                + "<p>"+__("This Help dialog window introduces the user to the usage of the BibleGet I/O plugin for Open Office Writer.")+"</p>"
                + "<p>"+__("The Help is divided into three sections:")+"</p>"
                + "<ul>"
                + "<li>"+__("Usage of the Plugin")+"</li>"
                + "<li>"+__("Formulation of the Queries")+"</li>"
                + "<li>"+__("Biblical Books and Abbreviations")+"</li>"
                + "</ul>"
                + "<p><b>"+__("AUTHOR")+":</b> John R. D'Orazio</p>"
                + "<p><b>"+__("COLLABORATORS")+":</b> "+__("Giovanni Gregori (computing) and Simone Urbinati (MUG Roma Tre)")+"</p>"
                + "<p><b>"+__("Version").toUpperCase()+":</b> "+String.valueOf(BibleGetIO.VERSION)+"</p>"
                + "<p>© <b>Copyright 2014 BibleGet I/O by John R. D'Orazio</b> <span style=\"color:blue;\">priest@johnromanodorazio.com</span></p>"
                + "<p><b>"+__("PROJECT WEBSITE")+": </b><span style=\"color:blue;\">https://www.bibleget.io</span> | <b>"+__("EMAIL ADDRESS FOR INFORMATION OR FEEDBACK ON THE PROJECT")+":</b> <span style=\"color:blue;\">bibleget.io@gmail.com</span></p>"
                + "</body></html>";
        
        String strfmt1 = __("Insert quote from input window");
        String strfmt2 = __("About this plugin");
        String strfmt3 = __("RENEW SERVER DATA");
        String strfmt4 = strfmt1;
        String strfmt5 = __("Insert quote from text selection");
        String strfmt6 = strfmt1;
        
        HTMLStr1 = "<html><head><meta charset=\"utf-8\"></head><body>"
                + "<h2>" + __("How to use the plugin") + "</h2>"
                + "<h3>" + __("Description of the menu icons and their functionality.") + "</h3>" 
                + "<p>" + __("Once the extension is installed, a new menu 'BibleGet I/O' will appear on the menu bar. Also a new floating toolbar will appear. The buttons on the floating toolbar correspond to the menu items in the new menu, as can be seen in this image:") + "</p><br /><br />"
                + "<img src=\""+packagePath+"/images/Screenshot.jpg\" /><br /><br />"
                + "<p>" + __("The floating toolbar can be dragged and placed anywhere on the screen. It can also be docked to certain areas of the workspace, for example on the toolbar below the menu bar, like in this image:") + "</p><br /><br />"
                + "<img src=\""+packagePath+"/images/Screenshot2.jpg\" /><br /><br />"
                + "<p>" + __("There are two ways of inserting a bible quote into a document.")
                + " "
                + __("The first way is by using the input window.")
                + " "
                + MessageFormat.format(__("If you click on the menu item ''{0}'', an input window will open where you can input your query and choose the version or versions you would like to take the quote from."),strfmt1)
                + " "
                + __("This list of versions is updated from the available versions on the BibleGet server, but since the information is stored locally it may be necessary to renew the server information when new versions are added to the BibleGet server database.")
                + " "
                + MessageFormat.format(__("In order to renew the information from the BibleGet server, click on the ''{0}'' menu item, and then click on the button ''{1}''."),strfmt2,strfmt3)
                + " "
                + MessageFormat.format(__("When you choose a version or multiple versions to quote from, this choice is automatically saved as a preference, and will be pre-selected the next time you open the ''{0}'' menu item."),strfmt4)
                + "<br /><br />"
                + "<img src=\""+packagePath+"/images/Screenshot4.jpg\" /><br /><br />"
                + MessageFormat.format(__("The second way is by writing your desired quote directly in the document, and then selecting it and choosing the menu item ''{0}''. The selected text will be substituted by the Bible Quote retrieved from the BibleGet server."),strfmt5)
                + " "
                + MessageFormat.format(__("The versions previously selected in the ''{0}'' window will be used, so you must have selected your preferred versions at least once from the ''{0}'' window."),strfmt6)
                + "</p><br /><br />"
                + "<img src=\""+packagePath+"/images/Screenshot3.jpg\" /><br /><br />"
                + "<p>"
                + __("Formatting preferences can be set using the 'Preferences' window. You can choose the desired font for the Bible quotes as well as the desired line-spacing, and you can choose separate formatting (font size, font color, font style) for the book / chapter, for the verse numbers, and for the verse text. Preferences are saved automatically.")
                + "</p><br /><br />"
                + "<img src=\""+packagePath+"/images/Screenshot5.jpg\" /><br /><br />"
                + "<p>"
                + __("After the 'Help' menu item that opens up this same help window, the last three menu items are:")
                + "</p>"
                + "<ul>"
                + "<li><img src=\""+packagePath+"/images/email.png\" />"
                + " '"
                + __("Send feedback")
                + "': <span>"
                + __("This will open up your system's default email application with the bibleget.io@gmail.com feedback address already filled in.")
                + "</span></li>"
                + "<li><img src=\""+packagePath+"/images/paypal.png\" />"
                + " '"
                + __("Contribute")
                + "': <span>"
                + __("This will open a Paypal page in the system's default browser where you can make a donation to contribute to the project. Even just €1 can help to cover the expenses of this project. Just the server costs €120 a year.")
                + "</span></li>"
                + "<li><img src=\""+packagePath+"/images/info.png\" />"
                + " '"
                + __("Information on the BibleGet I/O Project")
                + "': <span>"
                + __("This opens a dialog window with some information on the project and it's plugins, on the author and contributors, and on the current locally stored information about the versions and languages that the BibleGet server supports.")
                + "</span></li>"
                + "</ul>"
                + "</body></html>";
        
        String strfmt7 = __("Biblical Books and Abbreviations");
        
        HTMLStr2 = "<html>"
                + "<head><meta charset=\"utf-8\"></head>"
                + "<body>"
                + "<h2>" + __("How to formulate a bible query") + "</h2>"
                + "<p>" 
                + __("The queries for bible quotes must be formulated using standard notation for bible citation.") 
                + " "
                + __("This can be either the english notation (as explained here: https://en.wikipedia.org/wiki/Bible_citation), or the european notation as explained here below. ")
                + "</p>"
                + "<p>" 
                + __("A basic query consists of at least two elements: the bible book and the chapter.")
                + " "
                + __("The bible book can be written out in full, or in an abbreviated form.")
                + " "
                + MessageFormat.format(__("The BibleGet engine recognizes the names of the books of the bible in {0} different languages: {1}"),Integer.toString(bibleBooksLangsAmount),langsStr)
                + " "
                + MessageFormat.format(__("See the list of valid books and abbreviations in the section {0}."),"<span class=\"internal-link\" id=\"to-bookabbrevs\">" + strfmt7 + "</span>")
                + " "
                + __("For example, the query \"Matthew 1\" means the book of Matthew (or better the gospel according to Matthew) at chapter 1.")
                + " "
                + __("This can also be written as \"Mt 1\".") 
                + "</p>" 
                + "<p>" + __("Different combinations of books, chapters, and verses can be formed using the comma delimiter and the dot delimiter (in european notation, in english notation instead a colon is used instead of a comma and a comma is used instead of a dot):") + "</p>" 
                + "<ul>" 
                + "<li>" + __("\",\": the comma is the chapter-verse delimiter. \"Matthew 1,5\" means the book (gospel) of Matthew, chapter 1, verse 5. (In English notation: \"Matthew 1:5\".)") + "</li>" 
                + "<li>" + __("\".\": the dot is a delimiter between verses. \"Matthew 1,5.7\" means the book (gospel) of Matthew, chapter 1, verses 5 and 7. (In English notation: \"Matthew 1:5,7\".)") + "</li>"
                + "<li>" + __("\"-\": the dash is a range delimiter, which can be used in a variety of ways:")
                + "<ol>"
                + "<li>" + __("For a range of chapters: \"Matthew 1-2\" means the gospel according to Matthew, from chapter 1 to chapter 2.") + "</li>"
                + "<li>" + __("For a range of verses within the same chapter: \"Matthew 1,1-5\" means the gospel according to Matthew, chapter 1, from verse 1 to verse 5. (In English notation: \"Matthew 1:1-5\".)") + "</li>"
                + "<li>" + __("For a range of verses that span over different chapters: \"Matthew 1,5-2,13\" means the gospel according to Matthew, from chapter 1, verse 5 to chapter 2, verse 13. (In English notation: \"Matthew 1:5-2:13\".)") + "</li>"
                + "</ol>" 
                + "</ul>" 
                + "<p>" + __("Different combinations of these delimiters can form fairly complex queries, for example \"Mt1,1-3.5.7-9\" means the gospel according to Matthew, chapter 1, verses 1 to 3, verse 5, and verses 7 to 9. (In English notation: \"Mt1:1-3,5,7-9\".)") +"</p>"
                + "<p>" + __("Multiple queries can be combined together using a semi-colon \";\".")
                + " "
                + __("If the query following the semi-colon refers to the same book as the preceding query, it is not necessary to indicate the book a second time.")
                + " "
                + __("For example, \"Matthew 1,1;2,13\" means the gospel according to Matthew, chapter 1 verse 1 and chapter 2 verse 13. (In English notation: \"Matthew 1:1;2:13\".)")
                + " "
                + __("Here is an example of multiple complex queries combined into a single querystring: \"Genesis 1,3-5.7.9-11.13;2,4-9.11-13;Apocalypse 3,10.12-14\". (In English notation: \"Genesis 1:3-5,7,9-11,13;2:4-9,11-13;Apocalypse 3:10,12-14\").") + "</p>"
                + "<p>" + __("It doesn't matter whether or not you use a space between the book and the chapter, the querystring will be interpreted just the same.")
                + __("It is also indifferent whether you use uppercase or lowercase letters, the querystring will be interpreted just the same.")
                + "</p>"
                + "</body>"
                + "</html>";
        
        
        HTMLStr3 = "<html>"
                + "<head><meta charset=\"utf-8\"></head>"
                + "<body>"
                + "<h2>" + __("Biblical Books and Abbreviations") + "</h2>"
                + "<p>" + __("Here is a list of valid books and their corresponding abbreviations, either of which can be used in the querystrings.")
                + " "
                + __("The abbreviations do not always correspond with those proposed by the various editions of the Bible, because they would conflict with those proposed by other editions.")
                + " "
                + __("For example some english editions propose \"Gn\" as an abbreviation for \"Genesis\", while some italian editions propose \"Gn\" as an abbreviation for \"Giona\" (= \"Jonah\").")
                + " "
                + __("Therefore you will not always be able to use the abbreviations proposed by any single edition of the Bible, you must use the abbreviations that are recognized by the BibleGet engine as listed in the following table:")
                + "</p><br /><br />"
                + "<table cellspacing='0'>"
                + "<caption>{1}</caption>"
                + "<tr><th style=\"width:70%;\">" + __("BOOK") + "</th><th style=\"width:30%;\">" + __("ABBREVIATION") + "</th></tr>" 
                + "{0}"
                + "</table>"
                + "</body>"
                + "</html>";
                
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int)screenSize.getWidth();
        screenHeight = (int)screenSize.getHeight();
        frameWidth = 1250;
        frameHeight = 700;
        frameLeft = (screenWidth / 2) - (frameWidth / 2);
        frameTop = (screenHeight / 2) - (frameHeight / 2);
        initComponents();
    }

    public static BibleGetHelp getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException
    {
        if(instance == null)
        {
            instance = new BibleGetHelp();
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jTextPane2.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(__("Instructions"));
        setBounds(frameLeft,frameTop,frameWidth,frameHeight);
        setIconImages(setIconImages());

        renderer = new LocalCellRenderer(jTree1);
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode(__("Help"));
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode(__("Usage of the Plugin"));
        javax.swing.tree.DefaultMutableTreeNode treeNode3;
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode(__("Formulation of the Queries"));
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode(__("Biblical Books and Abbreviations"));
        treeNode1.add(treeNode2);

        for (String lclzdLang : langsLocalized) {
            treeNode3 = new javax.swing.tree.DefaultMutableTreeNode(lclzdLang);
            treeNode2.add(treeNode3);
        }
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setCellRenderer(renderer);
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane2);

        jTextPane2.setContentType("text/html;charset=UTF-8"); // NOI18N
        jTextPane2.setDocument(doc);
        jTextPane2.setEditorKit(kit);
        jTextPane2.setText(HTMLStr0);
        jScrollPane3.setViewportView(jTextPane2);

        jSplitPane1.setRightComponent(jScrollPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1250, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseClicked
        // TODO add your handling code here:
        int tp = jTree1.getRowForLocation(evt.getX(), evt.getY());
        //System.out.println(tp);
        if (tp != -1 && tp < 4){
            switch(tp){
                case 0: jTextPane2.setDocument(doc); jTextPane2.setText(HTMLStr0); jTextPane2.setCaretPosition(0); break;
                case 1: jTextPane2.setDocument(doc); jTextPane2.setText(HTMLStr1); jTextPane2.setCaretPosition(0); break;
                case 2: jTextPane2.setDocument(doc); jTextPane2.setText(HTMLStr2); jTextPane2.setCaretPosition(0); break;
                case 3: 
                    String curLang = Locale.getDefault().getDisplayLanguage().toUpperCase();
                    //System.out.println(curLang);
                    jTextPane2.setDocument(doc); 
                    jTextPane2.setText(MessageFormat.format(HTMLStr3,booksAndAbbreviations.get(curLang),curLang)); 
                    jTextPane2.setCaretPosition(0); 
                    break;
                default: jTextPane2.setDocument(doc); jTextPane2.setText(HTMLStr0); jTextPane2.setCaretPosition(0);
            }
        }
        else if(tp > 3){
            TreePath treePath = jTree1.getPathForLocation(evt.getX(), evt.getY());
            String curPath = treePath.getLastPathComponent().toString().toUpperCase(Locale.ENGLISH);
            //System.out.println(curPath);
            if(booksAndAbbreviations.get(curPath) != null) {
                jTextPane2.setDocument(doc); 
                jTextPane2.setText(MessageFormat.format(HTMLStr3,booksAndAbbreviations.get(curPath),curPath)); 
                jTextPane2.setCaretPosition(0); 
            }
        }   
        //jTextPane2.setText("<span>"+tp+"</span>");
         //else
           //jtf.setText("");
    }//GEN-LAST:event_jTree1MouseClicked

 /**
     * Cell renderer that knows the currently highlighted cell
     */
    static private class LocalCellRenderer extends DefaultTreeCellRenderer
    {
        private TreePath oldSelectedPath = null;

        private LocalCellRenderer(final JTree tree)
        {
            tree.addMouseMotionListener(new MouseMotionListener()
            {
                @Override
                public void mouseDragged(MouseEvent mouseEvent)
                {
                    // Nothing to do
                }

                @Override
                public void mouseMoved(MouseEvent mouseEvent)
                {
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    int selRow = tree.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
                    //System.out.println(selRow);
                    if (selRow < 0)
                    {
                        TreePath currentSelected = oldSelectedPath;
                        oldSelectedPath = null;
                        if (currentSelected != null)
                            treeModel.nodeChanged((TreeNode) currentSelected.getLastPathComponent());
                    } else
                    {
                        TreePath selectedPath = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                        if ((oldSelectedPath == null) || !selectedPath.equals(oldSelectedPath))
                        {
                            oldSelectedPath = selectedPath;
                            treeModel.nodeChanged((TreeNode) oldSelectedPath.getLastPathComponent());
                        }
                    }
                    tree.repaint();
                }
            });
        }
        
        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus)
        {
            Color transp = new Color((float)1.0,(float)1.0,(float)1.0,(float)0.0);
            Color lightBlue = new Color((float)0.8,(float)0.8,(float)1.0,(float)0.7);
            JComponent comp = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            comp.setOpaque(true);
            boolean highlight = (oldSelectedPath != null) && (value == oldSelectedPath.getLastPathComponent());
            //comp.setBackground(leaf ? tree.getBackground() : Color.white);
            //comp.setBackground(selected ? transp : (leaf ? tree.getBackground() : Color.white));
            comp.setBorder(selected ? BorderFactory.createMatteBorder(1,0,1,0,lightBlue) : tree.getBorder());
            comp.setBackground(highlight ? Color.yellow : (selected ? transp : (leaf ? tree.getBackground() : Color.white)));
            comp.setForeground(highlight ? Color.blue : (selected ? Color.white : tree.getForeground()));
            return comp;
        }
   
    }
    
    private List<Image> setIconImages()
    {
        List<Image> images = new ArrayList<>(4);
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/help_x16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/help_x24.png")));
        return images;
    }
    
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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BibleGetHelp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new BibleGetHelp().setVisible(true);
                } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
                    Logger.getLogger(BibleGetHelp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
