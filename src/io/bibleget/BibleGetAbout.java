/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import static io.bibleget.BibleGetI18N.__;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.swing.JFrame;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Lwangaman
 */
public class BibleGetAbout extends javax.swing.JFrame {

    private final int screenWidth;
    private final int screenHeight;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameLeft;
    private final int frameTop;
    private final HTMLEditorKit kit;
    private final Document doc;
    private final String HTMLStr;
    private final StyleSheet styles;

    private VersionsSelect jList1;
    private BibleGetDB bibleGetDB;
    private int versionLangs;
    private int versionCount;
    private int booksLangs;
    private String booksStr;
    
    private static BibleGetAbout instance;
    
    /**
     * Creates new form BibleGetAbout
     */
    private BibleGetAbout() throws ClassNotFoundException, UnsupportedEncodingException {
        //jTextPane does not initialize correctly, it causes a Null Exception Pointer
        //Following line keeps this from crashing the program
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        screenWidth = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        screenHeight = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        frameWidth = screenWidth - 200;
        frameHeight = screenHeight - 100;
        frameLeft = (screenWidth / 2) - (frameWidth / 2);
        frameTop = (screenHeight / 2) - (frameHeight / 2);
        
        kit = new HTMLEditorKit();
        doc = kit.createDefaultDocument();
        styles = kit.getStyleSheet();
        styles.addRule("body { padding:6px; }");
                
        HTMLStr = "" 
                + "<html><body><h2>"
                + __("BibleGet I/O plugin for Open Office Writer")
                + "</h2><h4>"
                + __("Version") + " " + String.valueOf(BibleGetIO.VERSION)
                + "</h4><p style='text-align:justify;width:90%;'>"
                + __("This plugin was developed by <b>John R. D'Orazio</b>, a priest in the diocese of Rome, chaplain at Roma Tre University.")
                + " "
                + MessageFormat.format(__("It is a part of the <b>BibleGet Project</b> at {0}."),"<span style='color:Blue;'>http://www.bibleget.io</span>")
                + " "
                + __("The author would like to thank <b>Giovanni Gregori</b> and <b>Simone Urbinati</b> for their code contributions.")
                + " "
                + __("The <b>BibleGet Project</b> is an independent project born from the personal initiative of John R. D'Orazio, and is not funded by any kind of corporation.")
                + " "
                + __("All of the expenses of the project server and domain, which amount to €200 a year, are accounted for personally by the author. All code contributions and development are entirely volunteered.")
                + " "
                + __("If you like the plugin and find it useful, please consider contributing even a small amount to help keep this project running. Even just €1 can make a difference. You can contribute using the appropriate menu item in this plugin's menu.")
                + "<br/><br/>© Copyright BibleGet I/O 2014 | E-Mail <span style='color:Blue;'>bibleget.io@gmail.com</span>"
                + "</p>"
                + "</body></html>";

        prepareDynamicInformation();
        initComponents();
    }
    
    public static BibleGetAbout getInstance() throws ClassNotFoundException, UnsupportedEncodingException{
        if(instance ==  null){
            instance = new BibleGetAbout();
        }
        return instance;
    }

    private void prepareDynamicInformation() throws ClassNotFoundException {
        bibleGetDB = BibleGetDB.getInstance();
        jList1 = new VersionsSelect();
        versionLangs = jList1.getVersionLangs();
        versionCount = jList1.getVersionCount();
        
        String langsSupported;
        List<String> langsLocalized = new ArrayList<>();
        booksLangs = 0;
        booksStr = "";
        if(bibleGetDB != null){
            //System.out.println("oh good, biblegetDB is not null!");
            langsSupported = bibleGetDB.getMetaData("LANGUAGES");
            //System.out.println(langsSupported);
            JsonReader jsonReader = Json.createReader(new StringReader(langsSupported));
            JsonArray bibleVersionsObj = jsonReader.readArray();
            booksLangs = bibleVersionsObj.size();
            for (JsonValue jsonValue : bibleVersionsObj) {
                //System.out.println(jsonValue.toString());
                langsLocalized.add(BibleGetI18N.localizeLanguage(jsonValue.toString()));
            }
            booksStr = StringUtils.join(langsLocalized,", ");
        }
        else{
            //System.out.println("Looks like biblegetDB is null :/");
        }
    
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(__("About this plugin"));
        setBounds(frameLeft,frameTop,frameWidth,frameHeight);
        setIconImages(setIconImages());
        setResizable(false);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/info_64x64.png"))); // NOI18N
        jLabel3.setText("jLabel3");

        jScrollPane1.setPreferredSize(new java.awt.Dimension(370, 300));

        jTextPane1.setEditable(false);
        jTextPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextPane1.setContentType("text/html;charset=UTF-8"); // NOI18N
        jTextPane1.setDocument(doc);
        jTextPane1.setEditorKit(kit);
        jTextPane1.setText(HTMLStr);
        jTextPane1.setMaximumSize(new java.awt.Dimension(450, 325));
        jTextPane1.setPreferredSize(new java.awt.Dimension(400, 300));
        jScrollPane1.setViewportView(jTextPane1);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText(__("Current information from the BibleGet Server:"));

        jLabel2.setText(MessageFormat.format(__("The BibleGet database currently supports {0} versions of the Bible in {1} different languages:"),versionCount,versionLangs));

        jScrollPane2.setViewportView(jList1);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(400, 100));

        jLabel4.setText(MessageFormat.format(__("The BibleGet engine currently understands the names of the books of the Bible in {0} different languages:"),booksLangs));

        jScrollPane3.setPreferredSize(new java.awt.Dimension(400, 48));

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText(booksStr);
        jTextArea1.setWrapStyleWord(true);
        jScrollPane3.setViewportView(jTextArea1);

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButton1.setText(__("RENEW SERVER DATA"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(612, 612, 612))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if(bibleGetDB.renewMetaData()){
            try {
                prepareDynamicInformation();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BibleGetAbout.class.getName()).log(Level.SEVERE, null, ex);
            }
            jLabel2.setText(MessageFormat.format(__("The BibleGet database currently supports {0} versions of the Bible in {1} different languages:"),versionCount,versionLangs));
            jLabel2.revalidate();
            jLabel4.setText(MessageFormat.format(__("The BibleGet engine currently understands the names of the books of the Bible in {0} different languages:"),booksLangs));
            jLabel4.revalidate();
            jScrollPane2.setViewportView(jList1);
            jScrollPane2.revalidate();
            try {
                BibleGetFrame bbGetFrameInstance;
                bbGetFrameInstance = BibleGetFrame.getInstance(BibleGetIO.getXController());
                bbGetFrameInstance.updateDynamicInformation();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BibleGetAbout.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    
    private List<Image> setIconImages()
    {
        List<Image> images = new ArrayList<>(4);
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/info_16x16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/info_24x24.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/info_32x32.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/info_48x48.png")));        
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
            java.util.logging.Logger.getLogger(BibleGetAbout.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new BibleGetAbout().setVisible(true);
                } catch (ClassNotFoundException | UnsupportedEncodingException ex) {
                    Logger.getLogger(BibleGetAbout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
