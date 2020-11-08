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
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang3.StringUtils;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefFocusHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

/**
 *
 * @author johnrdorazio
 */
public class BibleGetSearchFrame extends javax.swing.JFrame {
    
    private CefBrowser browser = null;
    private Component browserUI = null;
    private static BibleGetSearchFrame instance;
        
    private VersionsSelect jListBibleVersions;
    
    private String previewDocumentHead;
    private String previewDocumentBodyOpen;
    private String previewDocumentBodyClose;
    private String spinnerPage;
    
    JTable table;
    DefaultTableModel model;
    TableRowSorter<DefaultTableModel> sorter;
    RowFilter<DefaultTableModel,Integer> rf = null;
    
    private String selectedVersion;
    
    private LocalizedBibleBooks localizedBookNames;
    
    private String currentURL;
    private boolean browserFocus_ = true;
    private List<Integer> insertedIndexes = new ArrayList<>();
    private String lastTermSearched = "";
    private String lastVersionSelected = "";
    
    private final ImageIcon filter = new ImageIcon(getClass().getResource("/io/bibleget/images/filter.png"));
    private final ImageIcon removeFilter = new ImageIcon(getClass().getResource("/io/bibleget/images/remove_filter.png"));
    
    /**
     * Creates new form BibleGetSearchFrame
     */
    public BibleGetSearchFrame() {
        
        table = new JTable();
        String[] columnsNames = new String[]{"IDX","BOOK","CHAPTER","VERSE","VERSETEXT","SEARCHTERM","JSONSTR"};
        model = new DefaultTableModel(null,columnsNames){
            @Override
            public Class getColumnClass(int column){
                switch(column){
                    case 0:
                        return Integer.class;
                    case 1:
                        return Integer.class;
                    case 2:
                        return Integer.class;
                    case 3:
                        return String.class;
                    case 4:
                        return String.class;
                    case 5:
                        return String.class;
                    case 6:
                        return String.class;
                }
                return String.class;
            }
        };
        //new Object[]{"IDX", "BOOK", "CHAPTER", "VERSE", "VERSETEXT", "SEARCHTERM", "JSONSTR"}, numResults)
        /*
        model.addColumn("IDX");
        model.addColumn("BOOK");
        model.addColumn("CHAPTER");
        model.addColumn("VERSE");
        model.addColumn("VERSETEXT");
        model.addColumn("SEARCHTERM");
        model.addColumn("JSONSTR");
        */
        table.setModel(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        try {
            jListBibleVersions = new VersionsSelect(true, ListSelectionModel.SINGLE_SELECTION);
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
            + "body, body * {"
            + "-webkit-touch-callout: none;" +
            "-webkit-user-select: none;" +
            " -khtml-user-select: none;" +
            "   -moz-user-select: none;" +
            "    -ms-user-select: none;" +
            "        user-select: none;" +
            "}"
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
            + "#bibleGetSearchResultsTableContainer table span.button { padding: 6px; color: DarkBlue; font-weight: bold; background-color: LightBlue; border: 2px outset Blue; border-radius: 3px; display: inline-block; cursor: pointer; text-decoration: none; }" //box-shadow: 2px 2px 2px 2px DarkBlue; 
            + "#bibleGetSearchResultsTableContainer table span.button:hover { background-color: #EEF; }"
            + "#bibleGetSearchResultsTableContainer table span.button.rowinserted { color: Grey; background-color: LightGrey; border: 2px outset DarkGray; cursor: not-allowed; }" //box-shadow: 2px 2px 2px 2px DarkBlue; 
            + "#bibleGetSearchResultsTableContainer table span.button:hover { background-color: LightGrey; }"
            + "</style>"
            + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>"
            + "<script>(function($){"
            + "  let insertedIndexes = [" + insertedIndexes.stream().map(Object::toString).collect(Collectors.joining(",")) + "];"
            + "  $(document).ready(function(){"
            + "    $(document).on('click', '#bibleGetSearchResultsTableContainer table span.button', function(){"
            + "        if($(this).hasClass('rowinserted') === false){ "
            + "          let idx = parseInt($(this).attr('id').replace('row',''));"
            + "          console.log('Insert button for IDX '+idx+' was clicked...');"
            + "          cefQuery({"
            + "            request: 'INSERT:'+idx,"
            + "            onSuccess: function(response){"
            + "              if(response.startsWith('INJECTIONCOMPLETE:') ){"
            + "                let row = 'row'+response.replace('INJECTIONCOMPLETE:','');"
            + "                $('#'+row).text('INSERTED').addClass('rowinserted');"
            + "              }"
            //+ "              document.getElementById('response').innerHTML = '<b>Response</b>: '+response;"
            + "            },"
            + "            onFailure: function(error_code, error_message) {"
            + "              console.log('Error code: '+error_code+', Message: '+error_message);"
            //+ "              document.getElementById('error').innerHTML = '<b>Error code:</b> '+error_code+'<br /><b>Message:</b> '+error_message;"
            + "            }"
            + "          });"
            + "        }"
            + "    });"
            + "  }); "
            + "})(jQuery);</script>"
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
                "  left: 50%;" +
                "  margin-left: -40px;" +
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
                + "    $(document).on('click', '.helloworld', function(){"
                + "        console.log('Hello World button was clicked...');"
                + "        cefQuery({"
                + "          request: 'SEARCH:Ave Maria gratia plena Dominus tecum',"
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
                + "<h1>Welcome to the BibleGet verses search</h1>"
                + "<p class=\"helloworld\">You can perform a partial word search, which is the default (i.e. a search for 'Spirit' will find verses with the word 'Spirit' as well as verses with the word 'spiritual'). In this case you cannot search for three letter words (i.e. a search for 'God' will not find verses with 'God' but only with 'godly').</p>"
                + "<p class=\"helloworld\">You can also choose to perform a search for an exact match, which means that when searching for 'Spirit' only verses that have the exact match for 'Spirit' will be returned, whereas verses with the word 'spiritual' will not. In this case you can search for three letter words such as 'God', but not for two letter words.</p>"
                + "<p class=\"helloworld\">In any case you cannot search for parts of speech such as pronouns or articles, only words that contain the searched term will be returned in this case. For example, an exact match search for 'her' will return verses containing 'herd', but not verses containing 'her'.</p>"
                + "<div id=\"response\"><i>(test area for internal communications... clicking on any paragraph above should perform the test)</i></div>"
                + "<div id=\"error\"><i>(errors for internal communications would show here if there were any...)</i></div>"
                + "</body>";
        
        currentURL = DataUri.create("text/html", htmlStr);
        if(BibleGetIO.client != null){
            browser = BibleGetIO.client.createBrowser( currentURL, OS.isLinux(), false, null);
            browserUI = browser.getUIComponent();
        }
        initComponents();
               
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
        
        if(BibleGetIO.client != null){
            jInternalFramePreviewArea.getContentPane().add(browserUI, BorderLayout.CENTER);
            jTextFieldSearchForTerm.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!browserFocus_) return;
                    browserFocus_ = false;
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                    jTextFieldSearchForTerm.requestFocus();
                }
            });
            jTextFieldFilterForTerm.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!browserFocus_) return;
                    browserFocus_ = false;
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                    jTextFieldFilterForTerm.requestFocus();
                }
            });
            // Clear focus from the address field when the browser gains focus.
            BibleGetIO.client.addFocusHandler(new CefFocusHandlerAdapter() {
                @Override
                public void onGotFocus(CefBrowser browser) {
                    if (browserFocus_) return;
                    browserFocus_ = true;
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                    browser.setFocus(true);
                }

                @Override
                public void onTakeFocus(CefBrowser browser, boolean next) {
                    browserFocus_ = false;
                }
            });
        }
        
    }    

    public static BibleGetSearchFrame getInstance() throws ClassNotFoundException, UnsupportedEncodingException, SQLException, Exception
    {
        if(instance == null)
        {
            instance = new BibleGetSearchFrame();
        }
        return instance;
    }
    
    private List<Image> setIconImages()
    {
        List<Image> images = new ArrayList<>(4);
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/search_x16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/io/bibleget/images/search_x32.png")));
        return images;
    }

    private class Task_Force extends SwingWorker<Void, Integer> {
        JProgressBar jProgressBar;
        
        public Task_Force(JProgressBar jpb){
            this.jProgressBar = jpb;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int i = chunks.get(chunks.size()-1);
            jProgressBar.setValue(i); // The last value in this array is all we care about.
            //System.out.println(i);            
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            publish(0);
            System.out.println("SearchFrame background work started for browser URL loading");
            browser.loadURL(DataUri.create("text/html", spinnerPage) );
            publish(15);
            //let's do this
            String queryString = jTextFieldSearchForTerm.getText().trim();
            if(queryString.contains(" ")){
                queryString = queryString.split(" ")[0];
            }
            lastTermSearched = queryString;
            HTTPCaller httpCaller =  new HTTPCaller();
            String response = httpCaller.searchRequest(queryString, selectedVersion, jCheckBox1.isSelected());
            
            if(null != response){
                publish(30);
                JsonReader jsonReader = Json.createReader(new StringReader(response));
                JsonObject json = jsonReader.readObject();
                JsonArray resultsJArray = json.getJsonArray("results");
                JsonObject infoObj = json.getJsonObject("info");
                String searchTerm = infoObj.getString("keyword");
                lastVersionSelected = infoObj.getString("version");
                
                String rowsSearchResultsTable = "";
                String previewDocument;
                
                int book;
                int chapter;
                String versenumber;
                String versetext;
                String resultJsonStr;
                
                int numResults = resultsJArray.size();
                
                jInternalFramePreviewArea.setTitle("Search results: " + numResults + " verses found containing the term \"" + searchTerm + "\" in version \"" + lastVersionSelected + "\"");
                
                if(numResults > 0){
                    model.setRowCount(0);
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
                        //System.out.println("versetext before AddMark = " + versetext);
                        versetext = AddMark(versetext, searchTerm);
                        //System.out.println("versetext after AddMark = " + versetext);
                        //System.out.println("IDX="+resultCounter+",BOOK="+book+",CHAPTER="+chapter+",VERSE="+versenumber+",VERSETEXT="+versenumber+",SEARCHTERM="+searchTerm+",JSONSTR="+resultJsonStr);
                        if(null != model){
                            //is it better to add them directly to the model, or gather them in a Vector and then add the Vector to the model?
                            //see for example https://stackoverflow.com/a/22718808/394921
                            model.addRow(new Object[]{resultCounter, book, chapter, versenumber, versetext, searchTerm, resultJsonStr});
                        } else {
                            System.out.println("Table model is null! now why is that?");
                        }
                        rowsSearchResultsTable += "<tr><td><span class=\"button\" id=\"row" + resultCounter + "\">" + __("Select") + "</span></td><td>" + localizedBook.Fullname + " " + chapter + ":" + versenumber + "</td><td>" + versetext + "</td></tr>";
                        resultCounter++;
                        int remappedVal = BibleGetIO.remap(resultCounter, 0, numResults, 0, 60);
                        System.out.println("resultCounter = " + resultCounter + ", remappedVal = " + remappedVal);
                        publish(30+remappedVal);
                    }
                    System.out.println("model now has " + model.getRowCount() + " rows");
                } else {
                    rowsSearchResultsTable += "<tr><td></td><td></td><td></td></tr>";
                }
                
                previewDocument = previewDocumentHead + previewDocumentBodyOpen + rowsSearchResultsTable + previewDocumentBodyClose;
                browser.loadURL(DataUri.create("text/html",previewDocument));
                publish(100);
            } else {
                JOptionPane.showMessageDialog(null, "Unable to communicate with the BibleGet server. Please check your internet connection.", "Warning!", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        
        @Override
        protected void done(){
            System.out.println("Background worker has finished");
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

        jPanelSettingsArea = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        jButtonSearch = new javax.swing.JButton();
        jTextFieldSearchForTerm = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButtonFilter = new javax.swing.JButton();
        jButtonSort = new javax.swing.JButton();
        jTextFieldFilterForTerm = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jInternalFramePreviewArea = new javax.swing.JInternalFrame();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search for verses by keyword");
        setIconImages(setIconImages());
        setName("searchFrame"); // NOI18N

        jPanelSettingsArea.setName("jPanelOnTheLeft"); // NOI18N
        jPanelSettingsArea.setPreferredSize(new java.awt.Dimension(400, 500));

        jLabel1.setText("waiting for a browser click...");

        jProgressBar1.setStringPainted(true);

        jScrollPane1.setViewportView(jListBibleVersions);

        jLabel2.setText("Bible version to search from: " + selectedVersion);

        jButtonSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/search_x32.png"))); // NOI18N
        jButtonSearch.setText("Search");
        jButtonSearch.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchActionPerformed(evt);
            }
        });

        jLabel3.setText("Term to search");

        jCheckBox1.setText("only exact matches");
        jCheckBox1.setToolTipText("<html>The default behaviour for keyword search is to find any word of 4 or more letters which contains the keyword.<br />\nIf you prefer to query only exact word matches even if less than 4 letters, turn this option on.<br />\nNote that parts of speech will not return results.\n</html>");

        jButtonFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/filter.png"))); // NOI18N
        jButtonFilter.setText("Apply filter");
        jButtonFilter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonFilter.setIconTextGap(10);
        jButtonFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFilterActionPerformed(evt);
            }
        });

        jButtonSort.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/Sort.png"))); // NOI18N
        jButtonSort.setText("Order by reference");
        jButtonSort.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSortActionPerformed(evt);
            }
        });

        jTextFieldFilterForTerm.setToolTipText("");

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
                                    .addComponent(jTextFieldFilterForTerm)))
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextFieldSearchForTerm, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                            .addComponent(jButtonSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSettingsAreaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSort)))
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
                        .addComponent(jTextFieldSearchForTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jCheckBox1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSettingsAreaLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFilterForTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSort, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 478, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanelSettingsArea, java.awt.BorderLayout.WEST);

        jInternalFramePreviewArea.setTitle("Search results");
        jInternalFramePreviewArea.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/wysiwyg/24x24/preview.png"))); // NOI18N
        jInternalFramePreviewArea.setName("browserInternalFrame"); // NOI18N
        jInternalFramePreviewArea.setPreferredSize(new java.awt.Dimension(800, 800));
        jInternalFramePreviewArea.setVisible(true);
        getContentPane().add(jInternalFramePreviewArea, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFilterActionPerformed
        if("Apply filter".equals(jButtonFilter.getText())) {
            if(jTextFieldFilterForTerm.getText().isEmpty() ){
                JOptionPane.showMessageDialog(null, "Filter term cannot be empty!", "Warning!", JOptionPane.WARNING_MESSAGE);
            } else {
                jButtonFilter.setText("Remove filter"); // __("Remove filter")
                jButtonFilter.setIcon(removeFilter);
                String filterTerm = jTextFieldFilterForTerm.getText().trim();
                if(filterTerm.contains(" ")){
                    filterTerm = filterTerm.split(" ")[0];
                }
                System.out.println("filterTerm = " + filterTerm);
                try {
                    rf = RowFilter.regexFilter("(?i)" + filterTerm);
                    System.out.println("RowFilter did not create an exception");
                } catch (java.util.regex.PatternSyntaxException e) {
                    System.out.println(e);
                    return;
                }
                sorter.setRowFilter(rf);
                System.out.println("RowFilter set!");
                if(table.getRowCount() > 0){
                    System.out.println("there are " + table.getRowCount() + " results after applying the filter");
                    jInternalFramePreviewArea.setTitle("Search results: " + table.getRowCount() + " verses filtered by the term \"" + filterTerm + "\"  out of " + model.getRowCount() + " found containing the term \"" + lastTermSearched + "\" in version \"" + lastVersionSelected + "\"");
                    /*
                    for(int viewRow = 0; viewRow < table.getRowCount(); viewRow++){
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        //System.out.println("selected view row = " + viewRow + ", selected model row = " + modelRow);
                    }
                    */
                } else {
                    System.out.println("there are no results");
                }
                //System.out.println("Value at column 4 row 0 = " + model.getValueAt(table.convertRowIndexToModel(0), 4) );
            }
        } else {
            jButtonFilter.setText("Apply filter");// __("Apply filter")
            jButtonFilter.setIcon(filter);
            //searchResultsDT.DefaultView.RowFilter = ""
            jTextFieldFilterForTerm.setText("");
            sorter.setRowFilter(null);
            jInternalFramePreviewArea.setTitle("Search results: " + table.getRowCount() + " verses found containing the term \"" + lastTermSearched + "\" in version \"" + lastVersionSelected + "\"");
        }
        refreshSearchResults();
    }//GEN-LAST:event_jButtonFilterActionPerformed

    private void jButtonSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSortActionPerformed
        if("Order by reference".equals(jButtonSort.getText())){ 
            List <RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING)); //BOOK ASC
            sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING)); //CHAPTER ASC
            sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING)); //VERSE ASC
            sorter.setSortKeys(sortKeys);
            sorter.sort();
            jButtonSort.setText("Order by importance");
        } else {
            List <RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING)); //IDX ASC
            sorter.setSortKeys(sortKeys);
            sorter.sort();
            jButtonSort.setText("Order by reference");
        }
        refreshSearchResults();
    }//GEN-LAST:event_jButtonSortActionPerformed

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        insertedIndexes.clear();
        boolean readyToGo = true;
        String errorMessage = "";
        if(jListBibleVersions.getSelectedValue() == null){
            readyToGo = false;
            errorMessage = "Cannot search for a term if we don't know which Bible version to search from!";
        } else if(jTextFieldSearchForTerm.getText().isEmpty()){
            readyToGo = false;
            errorMessage = "Cannot search for an empty term!";
        } else if(jTextFieldSearchForTerm.getText().length() < 3){
            readyToGo = false;
            errorMessage = "Cannot search for such a small term! We need at least 3 characters.";
        }
        if(readyToGo){
            jButtonFilter.setText("Apply filter");
            jButtonFilter.setIcon(filter);
            jTextFieldFilterForTerm.setText("");
            jButtonSort.setText("Order by reference");
            Task_Force tf = new BibleGetSearchFrame.Task_Force(jProgressBar1);
            tf.execute();
        } else {
            JOptionPane.showMessageDialog(null, errorMessage, "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSearchActionPerformed

    private String AddMark(String verseText, String searchTerm){
        String pattern = "(?i)\\b(\\w*)(" + searchTerm + ")(\\w*)\\b";
        String replacement = "<a class=\"submark\">$1</a><a class=\"mark\">$2</a><a class=\"submark\">$3</a>";
        verseText = verseText.replaceAll(pattern, replacement);
        if(Normalizer.normalize(searchTerm, Form.NFD).matches(".*\\P{M}\\p{M}.*")){
            //System.out.println("found diacritical markings in "+searchTerm);
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
    
    private void refreshSearchResults(){
        String previewDocument;
        String rowsSearchResultsTable = "";
        String filterTerm = "";
        if(!"".equals(jTextFieldFilterForTerm.getText())){
            filterTerm = jTextFieldFilterForTerm.getText().trim();
            if(filterTerm.contains(" ")){
                filterTerm = filterTerm.split(" ")[0];
            }
        }
        
        if(table.getRowCount() > 0){
            System.out.println("refreshSearchResults found " + table.getRowCount() + " rows");
            for(int i = 0; i < table.getRowCount(); i++){
                int tableRow = table.convertRowIndexToModel(i);
                System.out.println("now dealing with row "+i+" (corresponds to row "+tableRow+" in model)");
                int book = (int) model.getValueAt(tableRow, table.getColumn("BOOK").getModelIndex());
                LocalizedBibleBook localizedBook = localizedBookNames.GetBookByIndex(book - 1);
                int chapter = (int) model.getValueAt(tableRow, table.getColumn("CHAPTER").getModelIndex());
                String versenumber = (String) model.getValueAt(tableRow, table.getColumn("VERSE").getModelIndex());
                String versetext = (String) model.getValueAt(tableRow, table.getColumn("VERSETEXT").getModelIndex());
                String searchTerm = (String) model.getValueAt(tableRow, table.getColumn("SEARCHTERM").getModelIndex());
                int rowIdx = (int) model.getValueAt(tableRow, table.getColumn("IDX").getModelIndex());
                //String resultJsonStr = (String) model.getValueAt(tableRow, table.getColumn("JSONSTR").getModelIndex());
                versetext = AddMark(versetext, searchTerm);
                if(!"".equals(filterTerm)){
                    versetext = AddMark(versetext, filterTerm);
                }
                String rowinsertedClass = insertedIndexes.indexOf(rowIdx) != -1 ? " rowinserted" : "";
                String spanButtonText = insertedIndexes.indexOf(rowIdx) != -1 ? "INSERTED" : "Select";
                rowsSearchResultsTable += "<tr><td><span class=\"button"+rowinsertedClass+"\" id=\"row" + rowIdx + "\">" + spanButtonText + "</span></td><td>" + localizedBook.Fullname + " " + chapter + ":" + versenumber + "</td><td>" + versetext + "</td></tr>";
            }
        } else {
            System.out.println("refreshSearchResults found 0 rows");
        }
        previewDocument = previewDocumentHead + previewDocumentBodyOpen + rowsSearchResultsTable + previewDocumentBodyClose;
        browser.loadURL(DataUri.create("text/html", previewDocument) );
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
    private javax.swing.JButton jButtonFilter;
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JButton jButtonSort;
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
    private javax.swing.JTextField jTextFieldFilterForTerm;
    private javax.swing.JTextField jTextFieldSearchForTerm;
    // End of variables declaration//GEN-END:variables

    
    @Override
    public void setVisible(final boolean visible) {
        // make sure that frame is marked as not disposed if it is asked to be visible
        if (visible) {
            //setDisposed(false);
        }
        // let's handle visibility...
        if (!visible || !isVisible()) { // have to check this condition simply because super.setVisible(true) invokes toFront if frame was already visible
            jProgressBar1.setValue(0);
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
                //callback.success(new StringBuilder(msg).reverse().toString());
                callback.success("Sancta Maria mater Dei, ora pro nobis peccatoribus.");
                jLabel1.setText(request);
                System.out.println(request); // prints "Hello World"
                return true;
            } else if(request.startsWith("INSERT:")){
                int IDX = Integer.parseInt(request.split(":")[1]);
                String JSONSTR = (String) "{\"results\": [" + model.getValueAt(IDX, table.getColumn("JSONSTR").getModelIndex()) + "]}";
                System.out.println(JSONSTR);
                callback.success("INJECTIONCOMPLETE:"+IDX);
                jLabel1.setText("verse at index " + IDX + " will now be inserted into the document...");
                BGetDocInject jsonResponseToDoc;
                try {
                    jsonResponseToDoc = new BGetDocInject(BibleGetIO.getXController(),null);
                    jsonResponseToDoc.InsertTextAtCurrentCursor(JSONSTR);
                    insertedIndexes.add(IDX);
                    jLabel1.setText("verse at index " + IDX + " was successfully inserted in the document");
                    return true;
                } catch (SQLException ex) {
                    Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            }
            
            return false;
        }   
    }
            
}
