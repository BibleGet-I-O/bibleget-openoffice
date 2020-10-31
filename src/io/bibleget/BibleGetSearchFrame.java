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

import ca.odell.glazedlists.SeparatorList;
import static io.bibleget.BGetI18N.__;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang3.StringUtils;
//import javax.swing.SwingWorker;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

/**
 *
 * @author johnrdorazio
 */
public class BibleGetSearchFrame extends javax.swing.JFrame {
    
    private final CefBrowser browser;
    private final Component browserUI;
    private static BibleGetSearchFrame instance;
        
    private VersionsSelect jListBibleVersions;
    
    private String previewDocumentHead;
    private String previewDocumentBodyOpen;
    private String previewDocumentBodyClose;
    private String spinnerPage;
    
    JTable table;
    DefaultTableModel model;
    TableRowSorter sorter;
    
    private String selectedVersion;
    
    private LocalizedBibleBooks localizedBookNames;
    
    /**
     * Creates new form BibleGetSearchFrame
     */
    public BibleGetSearchFrame() {
        
        try {
            jListBibleVersions = new VersionsSelect(true);
            jListBibleVersions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            ListSelectionModel listSelectionModel = jListBibleVersions.getSelectionModel();
            listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object selectedValue = jListBibleVersions.getSelectedValue();
        if(null!=selectedValue){
            selectedVersion = selectedValue.toString().split("—")[0].trim(); 
            //System.out.println("selectedVersion = " + selectedVersion);
        }
        
        previewDocumentHead = "<!DOCTYPE html>"
            + "<head>"
            + "<meta charset=\"UTF-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "<style type=\"text/css\">"
            + "html,body { margin: 0; padding: 0; }"
            //+ "body { border: 1px solid Black; }"
            + "#bibleGetSearchResultsTableContainer {"
            + "	 border: 1px solid #963;"
            + "}"
            + ""
            + "#bibleGetSearchResultsTableContainer table {"
            + "  width: 100%;"
            + "}"
            + ""
            + "#bibleGetSearchResultsTableContainer th, td { padding: 8px 16px; }"
            + "#bibleGetSearchResultsTableContainer thead th {"
            + "	 position: sticky;"
            + "  top: 0;"
            + "	 background: #C96;"
            + "	 border-left: 1px solid #EB8;"
            + "	 border-right: 1px solid #B74;"
            + "	 border-top: 1px solid #EB8;"
            + "	 font-weight: normal;"
            + "	 text-align: center;"
            + "  color: White;"
            + "  font-weight: bold;"
            + "}"
            + ""
            + "#bibleGetSearchResultsTableContainer tbody td {"
            + "  border-bottom: 1px groove White;"
            + "  background-color: #EFEFEF;"
            + "}"
            + ""
            + "#bibleGetSearchResultsTableContainer mark {"
            + "  font-weight: bold;"
            + "}"
            + ""
            + "a.mark { background-color: yellow; font-weight: bold; padding: 2px 4px; }"
            + "a.submark { background-color: lightyellow; padding: 2px 0px; }"
            + "a.button { padding: 6px; color: DarkBlue; font-weight: bold; background-color: LightBlue; border: 2px outset Blue; border-radius: 3px; display: inline-block; cursor: pointer; text-decoration: none; }" //box-shadow: 2px 2px 2px 2px DarkBlue; 
            + "a.button:hover { background-color: #EEF; }"
            + "</style>"
            + "</head>";

        previewDocumentBodyOpen = "<body><div id=\"bibleGetSearchResultsTableContainer\">"
            + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"scrollTable\" id=\"SearchResultsTable\">"
            + "	<thead class=\"fixedHeader\">"
            + "		<tr class=\"alternateRow\"><th>Action</th><th>Verse Reference</th><th>" + __("Verse Text") + "</th></tr>"
            + "	</thead>"
            + "	<tbody class=\"scrollContent\">";

        previewDocumentBodyClose = "</tbody></table></div></body>";
        
        spinnerPage = "<!DOCTYPE html"
                + "<head><meta charset=\"utf-8\">"
                + "<style>"
                + "body { background-color: gray; text-align: center; }"
                + ".lds-dual-ring {" +
                "  display: inline-block;" +
                "  width: 80px;" +
                "  height: 80px;" +
                "  position: absolute;" +
                "  top: 50%;" +
                "  margin-top: -40px;" +
                "}" +
                ".lds-dual-ring:after {" +
                "  content: \" \";" +
                "  display: block;" +
                "  width: 64px;" +
                "  height: 64px;" +
                "  margin: 8px;" +
                "  border-radius: 50%;" +
                "  border: 6px solid #fff;" +
                "  border-color: #fff transparent #fff transparent;" +
                "  animation: lds-dual-ring 1.2s linear infinite;" +
                "}" +
                "@keyframes lds-dual-ring {" +
                "  0% {" +
                "    transform: rotate(0deg);" +
                "  }" +
                "  100% {" +
                "    transform: rotate(360deg);" +
                "  }" +
                "}</style></head>"
                + "<body><div class=\"lds-dual-ring\"></div></body>";
        
        String htmlStr = "<!DOCTYPE html>"
                + "<head><meta charset=\"utf-8\">"
                + "<style>"
                + "  #response { min-height:30px; background-color: lightyellow; color: DarkGreen; padding: 6px 12px; }"
                + "  #error { min-height: 30px; background-color: pink; color: DarkRed; padding: 6px 12px; }"
                + "</style>"
                + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>"
                + "<script>(function($){"
                + "  $(document).ready(function(){"
                + "    $(document).on('click', '#helloworld', function(){"
                + "        console.log('Hello World button was clicked...');"
                + "        cefQuery({"
                + "          request: 'SEARCH: Hello Java World from the JCEF javascript world!',"
                + "          onSuccess: function(response){"
                + "            document.getElementById('response').innerHTML = '<b>Response</b>: '+response;"
                + "          },"
                + "          onFailure: function(error_code, error_message) {"
                + "            document.getElementById('error').innerHTML = '<b>Error code:</b> '+error_code+'<br /><b>Message:</b> '+error_message;"
                + "          }"
                + "        });"
                + "    });"
                + "  }); "
                + "})(jQuery);</script>"
                + "</head>"
                + "<body>"
                + "<h1>Initializing preview area...</h1>"
                + "<button id=\"helloworld\">Hello World!</button>"
                + "<div id=\"response\"></div>"
                + "<div id=\"error\"></div>"
                + "</body>";
        
        browser = BibleGetIO.client.createBrowser( DataUri.create("text/html",htmlStr), OS.isLinux(), false);
        browserUI = browser.getUIComponent();
        
        initComponents();
        
        jInternalFramePreviewArea.getContentPane().add(browserUI, BorderLayout.CENTER);
        
        try {
            localizedBookNames = LocalizedBibleBooks.getInstance();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        MouseAdapter mAdapter = new VersionsHoverHandler(jListBibleVersions);
        jListBibleVersions.addMouseMotionListener(mAdapter);
    }    

    public static BibleGetSearchFrame getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception
    {
        if(instance == null)
        {
            instance = new BibleGetSearchFrame();
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

        jPanelSettingsArea = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jInternalFramePreviewArea = new javax.swing.JInternalFrame();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search for verses by keyword");
        setName("searchFrame"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1200, 800));

        jPanelSettingsArea.setPreferredSize(new java.awt.Dimension(400, 500));

        jLabel1.setText("waiting for a browser click...");

        jProgressBar1.setStringPainted(true);

        jScrollPane1.setViewportView(jListBibleVersions);

        jLabel2.setText("Bible version to search from: " + selectedVersion);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/search_x32.png"))); // NOI18N
        jButton1.setText("Search");
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jTextField1.setText("jTextField1");

        jLabel3.setText("Term to search");

        jCheckBox1.setText("only exact matches");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/filter.png"))); // NOI18N
        jButton2.setText("Apply filter");
        jButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton2.setIconTextGap(10);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/Sort.png"))); // NOI18N
        jButton3.setText("Order by reference");
        jButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        jTextField2.setText("jTextField2");

        jLabel4.setText("Filter results with another term");

        javax.swing.GroupLayout jPanelSettingsAreaLayout = new javax.swing.GroupLayout(jPanelSettingsArea);
        jPanelSettingsArea.setLayout(jPanelSettingsAreaLayout);
        jPanelSettingsAreaLayout.setHorizontalGroup(
            jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(jSeparator1)
                    .addComponent(jScrollPane1)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSettingsAreaLayout.createSequentialGroup()
                        .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                    .addComponent(jTextField2)))
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSettingsAreaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3)))
                .addContainerGap())
        );
        jPanelSettingsAreaLayout.setVerticalGroup(
            jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jCheckBox1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSettingsAreaLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanelSettingsArea, java.awt.BorderLayout.WEST);

        jInternalFramePreviewArea.setTitle("Search results");
        jInternalFramePreviewArea.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/preview.png"))); // NOI18N
        jInternalFramePreviewArea.setPreferredSize(new java.awt.Dimension(800, 800));
        jInternalFramePreviewArea.setVisible(true);
        getContentPane().add(jInternalFramePreviewArea, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        boolean readyToGo = true;
        String errorMessage = "";
        if(jListBibleVersions.getSelectedValue() == null){
            readyToGo = false;
            errorMessage = "Cannot search for a term if we don't know which Bible version to search from!";
        } else if(jTextField1.getText().isEmpty()){
            readyToGo = false;
            errorMessage = "Cannot search for an empty term!";
        } else if(jTextField1.getText().length() < 3){
            readyToGo = false;
            errorMessage = "Cannot search for such a small term! We need at least 3 characters.";
        }
        if(readyToGo){
            browser.loadURL(DataUri.create("text/html", spinnerPage) );
            //let's do this
            String queryString = jTextField1.getText().trim();
            if(queryString.contains(" ")){
                queryString = queryString.split(" ")[0];
            }
            
            HTTPCaller httpCaller =  new HTTPCaller();
            String response = httpCaller.searchRequest(queryString, selectedVersion, jCheckBox1.isSelected());
            
            if(null != response){
                JsonReader jsonReader = Json.createReader(new StringReader(response));
                JsonObject json = jsonReader.readObject();
                JsonArray resultsJArray = json.getJsonArray("results");
                JsonObject infoObj = json.getJsonObject("info");
                String searchTerm = infoObj.getString("keyword");
                String versionSearched = infoObj.getString("version");
                
                String rowsSearchResultsTable = "";
                String previewDocument;
                
                int book;
                int chapter;
                String versenumber;
                String versetext;
                String resultJsonStr;
                
                int numResults = resultsJArray.size();
                table = new JTable(new DefaultTableModel(new Object[]{"IDX", "BOOK", "CHAPTER", "VERSE", "VERSETEXT", "SEARCHTERM", "JSONSTR"}, numResults));
                model = (DefaultTableModel) table.getModel();
                sorter = new TableRowSorter<>(model);
                table.setRowSorter(sorter);
                
                jInternalFramePreviewArea.setTitle("Search results: " + numResults + " verses found containing the term \"" + searchTerm + "\" in version \"" + versionSearched + "\"");
                
                if(numResults > 0){
                    int resultCounter = 0;
                    Iterator pIterator = resultsJArray.iterator();
                    while (pIterator.hasNext()) {
                        JsonObject currentJson = (JsonObject) pIterator.next();
                        book = Integer.decode(currentJson.getString("univbooknum"));
                        LocalizedBibleBook localizedBook = localizedBookNames.GetBookByIndex(book - 1);
                        chapter = Integer.decode(currentJson.getString("chapter"));
                        versenumber = currentJson.getString("verse");
                        versetext = currentJson.getString("text");
                        resultJsonStr = currentJson.toString();
                        //The following regex removes all NABRE formatting tags from the verse text that is displayed in the table
                        versetext = versetext.replaceAll("<(?:[^>=]|='[^']*'|=\"[^\"]*\"|=[^'\"][^\\s>]*)*>","");
                        System.out.println("versetext before AddMark = " + versetext);
                        versetext = AddMark(versetext, searchTerm);
                        System.out.println("versetext after AddMark = " + versetext);
                        System.out.println("IDX="+resultCounter+",BOOK="+book+",CHAPTER="+chapter+",VERSE="+versenumber+",VERSETEXT="+versenumber+",SEARCHTERM="+searchTerm+",JSONSTR="+resultJsonStr);
                        if(null != model){
                            //is it better to add them directly to the model, or gather them in a Vector and then add the Vector to the model?
                            //see for example https://stackoverflow.com/a/22718808/394921
                            model.addRow(new Object[]{resultCounter, book, chapter, versenumber, versetext, searchTerm, resultJsonStr});
                        } else {
                            System.out.println("Table model is null! now why is that?");
                        }
                        rowsSearchResultsTable += "<tr><td><a href=\"#\" class=\"button\" id=\"row" + resultCounter + "\">" + __("Select") + "</a></td><td>" + localizedBook.Fullname + " " + chapter + ":" + versenumber + "</td><td>" + versetext + "</td></tr>";
                        resultCounter++;
                    }
                } else {
                    rowsSearchResultsTable += "<tr><td></td><td></td><td></td></tr>";
                }
                
                previewDocument = previewDocumentHead + previewDocumentBodyOpen + rowsSearchResultsTable + previewDocumentBodyClose;
                browser.loadURL(DataUri.create("text/html",previewDocument));
            }
            
        } else {
            JOptionPane.showMessageDialog(null, errorMessage, "Warning!", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton1MouseClicked

    private String AddMark(String verseText, String searchTerm){
        String pattern = "(?i)\\b(\\w*)(" + searchTerm + ")(\\w*)\\b";
        String replacement = "<a class=\"submark\">$1</a><a class=\"mark\">$2</a><a class=\"submark\">$3</a>";
        verseText = verseText.replaceAll(pattern, replacement);
        if(Normalizer.normalize(searchTerm, Form.NFD).matches(".*\\P{M}\\p{M}.*")){
            System.out.println("found diacritical markings in "+searchTerm);
            String normalizedPattern = "(?i)\\b(\\w*)(" + stripDiacritics(searchTerm) + ")(\\w*)\\b";
            verseText = verseText.replaceAll(normalizedPattern, replacement);
        }
        return verseText;
    }
    
    private String stripDiacritics(String inText){
        String normalizedString = Normalizer.normalize(inText, Form.NFD);
        String buildStr = normalizedString.replaceAll("\\p{M}", "");
        return Normalizer.normalize(buildStr, Form.NFC);
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
            java.util.logging.Logger.getLogger(BibleGetSearchFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new BibleGetSearchFrame().setVisible(true);
            } catch (Exception ex) {
                Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JInternalFrame jInternalFramePreviewArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanelSettingsArea;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
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

    private class SharedListSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            /*
            int firstIndex = e.getFirstIndex();
            int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            */
            @SuppressWarnings("unchecked")
            SeparatorList<BibleVersion> versionsByLang = jListBibleVersions.getVersionsByLangList();
            List<String> selectedVersions = new ArrayList<>();
            if (lsm.isSelectionEmpty()) {
            } else {
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        String abbrev = versionsByLang.get(i).getAbbrev();
                        selectedVersions.add(abbrev);
                    }
                }
            }
            String versionsSelcd = StringUtils.join(selectedVersions.toArray(), ',');
            selectedVersion = versionsSelcd;
            jLabel2.setText("Bible version to search from: " + versionsSelcd);
        }
    }
    
    
    public class MessageRouterHandler extends CefMessageRouterHandlerAdapter {
        @Override
        public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request, boolean persistent, CefQueryCallback callback) {
            
            if (request.startsWith("SEARCH:")) {
                // Reverse the message and return it to the JavaScript caller.
                String msg = request.substring(12);
                callback.success(new StringBuilder(msg).reverse().toString());
                jLabel1.setText(request);
                System.out.println(request); // prints "Hello World"
                return true;
            }
            
            return false;
        }   
    }
    
}
