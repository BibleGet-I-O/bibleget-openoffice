/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import static io.bibleget.BibleGetI18N.__;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.JTextPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

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
    
    private String paragraphAlignment;
    private int paragraphLineSpacing; 
    private String paragraphFontFamily;
    private int paragraphLeftIndent;
    
    private Color textColorBookChapter;
    private Color bgColorBookChapter;
    private boolean boldBookChapter;
    private boolean italicsBookChapter;
    private boolean underscoreBookChapter;
    private int fontSizeBookChapter;
    private String vAlignBookChapter;

    private Color textColorVerseNumber;
    private Color bgColorVerseNumber;
    private boolean boldVerseNumber;
    private boolean italicsVerseNumber;
    private boolean underscoreVerseNumber;
    private int fontSizeVerseNumber;
    private String vAlignVerseNumber;

    private Color textColorVerseText;
    private Color bgColorVerseText;
    private boolean boldVerseText;
    private boolean italicsVerseText;
    private boolean underscoreVerseText;
    private int fontSizeVerseText;
    private String vAlignVerseText;
    
    private boolean noVersionFormatting;
    
    private final BibleGetDB biblegetDB;
    
    private final HTMLEditorKit kit;
    private final Document doc;
    private final String HTMLStr;
    private final StyleSheet styles;
    
    private JTextPane myjtextpane;
    //private JEditorPane myjeditorpane;
    
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
        //System.out.println("getting JsonObject of biblegetDB options");
        JsonObject myOptions = biblegetDB.getOptions();
        //System.out.println(myOptions.toString());
        //System.out.println("getting JsonValue of options rows");
        JsonValue myResults = myOptions.get("rows");
        //System.out.println(myResults.toString());
        //System.out.println("navigating values in json tree and setting global variables");
        navigateTree(myResults, null);
        this.fontFamilies = BibleGetIO.getFontFamilies();
        
        //System.out.println("(OptionsFrame: 127) textColorBookChapter ="+textColorBookChapter);
        
        this.kit = new HTMLEditorKit();
        this.doc = kit.createDefaultDocument();
        this.styles = kit.getStyleSheet();
        styles.addRule("body { padding: 6px; background-color: #FFFFFF; }");
        styles.addRule("div.results { font-family: "+paragraphFontFamily+"; }");
        styles.addRule("div.results p.book { font-size:"+fontSizeBookChapter+"pt; }");
        styles.addRule("div.results p.book { font-weight:"+(boldBookChapter?"bold":"normal")+"; }");
        styles.addRule("div.results p.book { color:"+ColorToHexString(textColorBookChapter)+"; }");
        styles.addRule("div.results p.book { background-color:"+ColorToHexString(bgColorBookChapter)+"; }");
        styles.addRule("div.results p.book { line-height: "+paragraphLineSpacing+"%; }");
        styles.addRule("div.results p.book span { vertical-align: "+vAlignBookChapter+"; }");
        styles.addRule("div.results p.verses { text-align: "+paragraphAlignment+"; }");
        styles.addRule("div.results p.verses { line-height: "+paragraphLineSpacing+"%; }");
        styles.addRule("div.results p.verses span.sup { font-size:"+fontSizeVerseNumber+"pt; }");
        styles.addRule("div.results p.verses span.sup { color:"+ColorToHexString(textColorVerseNumber)+";");
        styles.addRule("div.results p.verses span.sup { background-color:"+ColorToHexString(bgColorVerseNumber)+";");
        styles.addRule("div.results p.verses span.sup { vertical-align: "+vAlignVerseNumber+"; }");
        styles.addRule("div.results p.verses span.text { font-size:"+fontSizeVerseText+"pt; }");
        styles.addRule("div.results p.verses span.text { color:"+ColorToHexString(textColorVerseText)+"; }");
        styles.addRule("div.results p.verses span.text { background-color:"+ColorToHexString(bgColorVerseText)+"; }");
        styles.addRule("div.results p.verses span.text { vertical-align: "+vAlignVerseText+"; }");

        String tempStr = "";
        tempStr += "<html><head><meta charset=\"utf-8\"></head><body><div class=\"results\"><p class=\"book\"><span>";
        tempStr += __("Genesi")+"&nbsp;1";
        tempStr += "</span></p><p class=\"verses\" style=\"margin-top:0px;\"><span class=\"sup\">1</span><span class=\"text\">";
        tempStr += __("In the beginning, when God created the heavens and the earth");
        tempStr += "</span><span class=\"sup\">2</span><span class=\"text\">";
        tempStr += __("and the earth was without form or shape, with darkness over the abyss and a mighty wind sweeping over the waters");
        tempStr += "</span><span class=\"sup\">3</span><span class=\"text\">";
        tempStr += __("Then God said: Let there be light, and there was light.");
        tempStr += "</span></p></div></body></html>";
        
        this.HTMLStr = tempStr;
        
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int)screenSize.getWidth();
        screenHeight = (int)screenSize.getHeight();
        frameWidth = 686;
        frameHeight = 503;
        frameLeft = (screenWidth / 2) - (frameWidth / 2);
        frameTop = (screenHeight / 2) - (frameHeight / 2);
                
        //this.myMessages = BibleGetI18N.getMessages();
        
        initComponents();
        
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

        jComboBox1.setFont(new java.awt.Font(paragraphFontFamily, java.awt.Font.PLAIN, 18));
        jComboBox1.setMaximumRowCount(6);
        jComboBox1.setModel(fontFamilies);
        jComboBox1.setSelectedItem(paragraphFontFamily);
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
        jToggleButton1.setSelected(boldBookChapter);
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
        jComboBox2.setSelectedItem(""+fontSizeBookChapter+"");
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
        jToggleButton8.setSelected(vAlignBookChapter.equals("super"));
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
        jToggleButton9.setSelected(vAlignBookChapter.equals("sub"));
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
        jComboBox3.setSelectedItem(""+fontSizeVerseNumber+"");
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
        jComboBox4.setSelectedItem(""+fontSizeVerseText+"");
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        final JColorChooser jColorChooser = new JColorChooser(textColorBookChapter);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            textColorBookChapter = jColorChooser.getColor();
            String rgb = ColorToHexString(textColorBookChapter);
            styles.addRule("div.results p.book { color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("TEXTCOLORBOOKCHAPTER", rgb)){
                //System.out.println("TEXTCOLORBOOKCHAPTER was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating TEXTCOLORBOOKCHAPTER in database");
            }
        };
        ActionListener cancelActionListener = (ActionEvent actionEvent) -> {        
        };
        final JDialog jDialog = JColorChooser.createDialog(null,  __("Choose Book / Chapter Font Color"), true, jColorChooser, okActionListener, cancelActionListener);
        jDialog.setIconImages(setIconImagesTextColor());
        jDialog.setVisible(true);
    }//GEN-LAST:event_jButton1MouseClicked
    
    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        final JColorChooser jColorChooser = new JColorChooser(bgColorBookChapter);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            bgColorBookChapter = jColorChooser.getColor();
            String rgb = ColorToHexString(bgColorBookChapter);
            styles.addRule("div.results p.book { background-color:"+rgb+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("BGCOLORBOOKCHAPTER", rgb)){
                //System.out.println("BGCOLORBOOKCHAPTER was successfully updated in database to value "+rgb);
            }
            else{
                //System.out.println("Error updating BGCOLORBOOKCHAPTER in database");
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
        final JColorChooser jColorChooser = new JColorChooser(textColorVerseNumber);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            textColorVerseNumber = jColorChooser.getColor();
            String rgb = ColorToHexString(textColorVerseNumber);
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
        final JColorChooser jColorChooser = new JColorChooser(bgColorVerseNumber);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            bgColorVerseNumber = jColorChooser.getColor();
            String rgb = ColorToHexString(bgColorVerseNumber);
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
        final JColorChooser jColorChooser = new JColorChooser(textColorVerseText);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            textColorVerseText = jColorChooser.getColor();
            String rgb = ColorToHexString(textColorVerseText);
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
        final JColorChooser jColorChooser = new JColorChooser(bgColorVerseText);
        jColorChooserClean(jColorChooser);
        ActionListener okActionListener = (ActionEvent actionEvent) -> {
            bgColorVerseText = jColorChooser.getColor();
            String rgb = ColorToHexString(bgColorVerseText);
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
            boldBookChapter = true;
            styles.addRule("div.results p.book { font-weight:bold; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            boldBookChapter = false;
            styles.addRule("div.results p.book { font-weight:normal; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        
        if(biblegetDB.setBooleanOption("BOLDBOOKCHAPTER", boldBookChapter)){
            //System.out.println("BOLDBOOKCHAPTER was successfully updated in database to value "+boldBookChapter);
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
            paragraphAlignment = "left";
            styles.addRule("div.results p.verses { text-align:"+paragraphAlignment+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setStringOption("PARAGRAPHALIGNMENT", paragraphAlignment)){
                //System.out.println("PARAGRAPHALIGNMENT was successfully updated in database to value "+paragraphAlignment);
            }
            else{
                //System.out.println("Error updating PARAGRAPHALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton3ItemStateChanged

    private void jToggleButton2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton2ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            paragraphAlignment = "center";
            styles.addRule("div.results p.verses { text-align:"+paragraphAlignment+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setStringOption("PARAGRAPHALIGNMENT", paragraphAlignment)){
                //System.out.println("PARAGRAPHALIGNMENT was successfully updated in database to value "+paragraphAlignment);
            }
            else{
                //System.out.println("Error updating PARAGRAPHALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton2ItemStateChanged

    private void jToggleButton4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton4ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            paragraphAlignment = "right";
            styles.addRule("div.results p.verses { text-align:"+paragraphAlignment+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setStringOption("PARAGRAPHALIGNMENT", paragraphAlignment)){
                //System.out.println("PARAGRAPHALIGNMENT was successfully updated in database to value "+paragraphAlignment);
            }
            else{
                //System.out.println("Error updating PARAGRAPHALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton4ItemStateChanged

    private void jToggleButton5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton5ItemStateChanged
        if(evt.getStateChange()==ItemEvent.SELECTED){
            paragraphAlignment = "justify";
            styles.addRule("div.results p.verses { text-align:"+paragraphAlignment+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        
            if(biblegetDB.setStringOption("PARAGRAPHALIGNMENT", paragraphAlignment)){
                //System.out.println("PARAGRAPHALIGNMENT was successfully updated in database to value "+paragraphAlignment);
            }
            else{
                //System.out.println("Error updating PARAGRAPHALIGNMENT in database");
            }
        }
    }//GEN-LAST:event_jToggleButton5ItemStateChanged

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        paragraphLeftIndent += 5;
        styles.addRule("div.results { padding-left:"+paragraphLeftIndent+"pt; }");
        jTextPane1.setDocument(doc);
        jTextPane1.setText(HTMLStr);
        
        if(biblegetDB.setIntOption("PARAGRAPHLEFTINDENT", paragraphLeftIndent)){
            //System.out.println("PARAGRAPHLEFTINDENT was successfully updated in database to value "+paragraphLeftIndent);
        }
        else{
            //System.out.println("Error updating PARAGRAPHLEFTINDENT in database");
        }
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        if(paragraphLeftIndent>=5){paragraphLeftIndent -= 5;}
        styles.addRule("div.results { padding-left:"+paragraphLeftIndent+"pt; }");
        jTextPane1.setDocument(doc);
        jTextPane1.setText(HTMLStr);
        
        if(biblegetDB.setIntOption("PARAGRAPHLEFTINDENT", paragraphLeftIndent)){
            //System.out.println("PARAGRAPHLEFTINDENT was successfully updated in database to value "+paragraphLeftIndent);
        }
        else{
            //System.out.println("Error updating PARAGRAPHLEFTINDENT in database");
        }
    }//GEN-LAST:event_jButton4MouseClicked

    private void jToggleButton8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton8ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton9.setSelected(false);
            vAlignBookChapter = "super";
            //System.out.println("setting book-chapter vertical-align to: "+vAlignBookChapter);
            styles.addRule("div.results p.book span { vertical-align: "+vAlignBookChapter+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            vAlignBookChapter = "initial";
            //System.out.println("btn8 :: setting book-chapter vertical-align to: "+vAlignBookChapter);
            styles.addRule("div.results p.book span { vertical-align: "+vAlignBookChapter+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VALIGNBOOKCHAPTER", vAlignBookChapter)){
            //System.out.println("VALIGNBOOKCHAPTER was successfully updated in database to value "+vAlignBookChapter);
        }
        else{
            //System.out.println("Error updating VALIGNBOOKCHAPTER in database");
        }
    }//GEN-LAST:event_jToggleButton8ItemStateChanged

    private void jToggleButton9ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton9ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED){
            jToggleButton8.setSelected(false);
            vAlignBookChapter = "sub";
            //System.out.println("setting book-chapter vertical-align to: "+vAlignBookChapter);
            styles.addRule("div.results p.book span { vertical-align: "+vAlignBookChapter+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }
        else if(evt.getStateChange()==ItemEvent.DESELECTED){
            vAlignBookChapter = "initial";
            //System.out.println("btn9 :: setting book-chapter vertical-align to: "+vAlignBookChapter);
            styles.addRule("div.results p.book span { vertical-align: "+vAlignBookChapter+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
        }        
        
        if(biblegetDB.setStringOption("VALIGNBOOKCHAPTER", vAlignBookChapter)){
            System.out.println("VALIGNBOOKCHAPTER was successfully updated in database to value "+vAlignBookChapter);
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
            vAlignVerseText = "super";
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
        
        if(biblegetDB.setStringOption("VALIGNVERSETEXT", vAlignVerseText)){
            //System.out.println("VALIGNVERSETEXT was successfully updated in database to value "+vAlignVerseText);
        }
        else{
            //System.out.println("Error updating VALIGNVERSETEXT in database");
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
        
        if(biblegetDB.setStringOption("VALIGNVERSETEXT", vAlignVerseText)){
            //System.out.println("VALIGNVERSETEXT was successfully updated in database to value "+vAlignVerseText);
        }
        else{
            //System.out.println("Error updating VALIGNVERSETEXT in database");
        }
    }//GEN-LAST:event_jToggleButton19ItemStateChanged

    private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox2ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
            Object item = evt.getItem();
            // do something with object
            String fontsize = item.toString();
            fontSizeBookChapter = Integer.parseInt(fontsize);
            styles.addRule("div.results p.book { font-size:"+fontSizeBookChapter+"pt; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("FONTSIZEBOOKCHAPTER", fontSizeBookChapter)){
                //System.out.println("FONTSIZEBOOKCHAPTER was successfully updated in database to value "+fontSizeBookChapter);
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
            fontSizeVerseNumber = Integer.parseInt(fontsize);
            styles.addRule("div.results p.verses span.sup { font-size:"+fontSizeVerseNumber+"pt;}");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("FONTSIZEVERSENUMBER", fontSizeVerseNumber)){
                //System.out.println("FONTSIZEVERSENUMBER was successfully updated in database to value "+fontSizeVerseNumber);
            }
            else{
                //System.out.println("Error updating FONTSIZEVERSENUMBER in database");
            }                
        }
    }//GEN-LAST:event_jComboBox3ItemStateChanged

    private void jComboBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox4ItemStateChanged
        // TODO add your handling code here:
        if(evt.getStateChange()==ItemEvent.SELECTED) {
          Object item = evt.getItem();
          // do something with object
            String fontsize = item.toString();
            fontSizeVerseText = Integer.parseInt(fontsize);
            styles.addRule("div.results p.verses span.text { font-size:"+fontSizeVerseText+"pt;}");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("FONTSIZEVERSETEXT", fontSizeVerseText)){
                //System.out.println("FONTSIZEVERSETEXT was successfully updated in database to value "+fontSizeVerseText);
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
            paragraphFontFamily = item.toString();
            styles.addRule("div.results { font-family: "+paragraphFontFamily+"; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setStringOption("PARAGRAPHFONTFAMILY", paragraphFontFamily)){
                //System.out.println("PARAGRAPHFONTFAMILY was successfully updated in database to value "+paragraphFontFamily);
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
                case 0: paragraphLineSpacing = 100; break;
                case 1: paragraphLineSpacing = 150; break;
                case 2: paragraphLineSpacing = 200; break;    
            }
            // do something with object
            
            styles.addRule("div.results p.book { line-height: "+paragraphLineSpacing+"%; }");
            styles.addRule("div.results p.verses { line-height: "+paragraphLineSpacing+"%; }");
            jTextPane1.setDocument(doc);
            jTextPane1.setText(HTMLStr);
            if(biblegetDB.setIntOption("PARAGRAPHLINESPACING", paragraphLineSpacing)){
                //System.out.println("PARAGRAPHLINESPACING was successfully updated in database to value "+paragraphLineSpacing);
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
            noVersionFormatting = true;
        }        
        else if(evt.getStateChange()==ItemEvent.DESELECTED) {
          // do something with object
            noVersionFormatting = false;
        }        
        if(biblegetDB.setBooleanOption("NOVERSIONFORMATTING", noVersionFormatting)){
            //System.out.println("NOVERSIONFORMATTING was successfully updated in database to value "+noVersionFormatting);
        }
        else{
            //System.out.println("Error updating NOVERSIONFORMATTING in database");
        }                
    }//GEN-LAST:event_jCheckBox1ItemStateChanged
    
        
    
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
            case "PARAGRAPHALIGNMENT": paragraphAlignment = value; break;
            case "PARAGRAPHFONTFAMILY": paragraphFontFamily = value; break;
            case "TEXTCOLORBOOKCHAPTER": System.out.println("textColorBookChapter="+value); textColorBookChapter = Color.decode(value); break; //decode string representation of hex value
            case "BGCOLORBOOKCHAPTER": System.out.println("bgColorBookChapter="+value); bgColorBookChapter = Color.decode(value); break; //decode string representation of hex value
            case "VALIGNBOOKCHAPTER": vAlignBookChapter = value; break;
            case "TEXTCOLORVERSENUMBER": System.out.println("textColorVerseNumber="+value); textColorVerseNumber = Color.decode(value); break; //decode string representation of hex value
            case "BGCOLORVERSENUMBER": System.out.println("bgColorVerseNumber="+value); bgColorVerseNumber = Color.decode(value); break; //decode string representation of hex value
            case "VALIGNVERSENUMBER": vAlignVerseNumber = value; break;
            case "TEXTCOLORVERSETEXT": System.out.println("textColorVerseText="+value); textColorVerseText = Color.decode(value); break; //decode string representation of hex value
            case "BGCOLORVERSETEXT": System.out.println("bgColorVerseText="+value); bgColorVerseText = Color.decode(value); break; //decode string representation of hex value
            case "VALIGNVERSETEXT": vAlignVerseText = value; break;
        }
    }
    
    private void getNumberOption(String key,int value){
        switch(key){
            case "PARAGRAPHLINESPACING": paragraphLineSpacing = value; break; //think of it as percent
            case "PARAGRAPHLEFTINDENT": paragraphLeftIndent = value; break;
            case "FONTSIZEBOOKCHAPTER": fontSizeBookChapter = value; break;       
            case "FONTSIZEVERSENUMBER": fontSizeVerseNumber = value; break;
            case "FONTSIZEVERSETEXT": fontSizeVerseText = value; break;
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

    private int getParaLineSpaceVal()
    {
        int retval;
        switch(paragraphLineSpacing)
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
