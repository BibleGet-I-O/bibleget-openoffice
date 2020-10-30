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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
//import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
    
    private static DBHelper biblegetDB;
    private SeparatorList<BibleVersion> versionsByLang;
    private EventList<BibleVersion> bibleVersions;
    private boolean[] enabledFlags;
    private int[] indices;    
    /**
     * Creates new form BibleGetSearchFrame
     */
    public BibleGetSearchFrame() {
        
        //SwingWorker swingWorker = new SwingWorker();
        
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
        
        try {
            prepareDynamicInformation();
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BibleGetSearchFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        initComponents();
        jInternalFramePreviewArea.getContentPane().add(browserUI, BorderLayout.CENTER);
    }
    
    private void prepareDynamicInformation() throws ClassNotFoundException, SQLException, Exception{
        biblegetDB = DBHelper.getInstance();
        String bibleVersionsStr = biblegetDB.getMetaData("VERSIONS");
        System.out.println(this.getClass().getSimpleName() + " -> prepareDynamicInformation -> bibleVersionsStr = " + bibleVersionsStr);
        if(null == bibleVersionsStr){
            System.out.println("We have a problem Watson!");
        } else{
            JsonReader jsonReader = Json.createReader(new StringReader(bibleVersionsStr));
            JsonObject bibleVersionsObj = jsonReader.readObject();
            Set<String> versionsabbrev = bibleVersionsObj.keySet();
            bibleVersions = new BasicEventList<>();
            if(!versionsabbrev.isEmpty()){
                for(String s:versionsabbrev) {
                    String versionStr = bibleVersionsObj.getString(s); //store these in an array
                    String[] array; 
                    array = versionStr.split("\\|");
                    bibleVersions.add(new BibleVersion(s,array[0],array[1],StringUtils.capitalize(new Locale(array[2]).getDisplayLanguage())));
                }
            }

            List<String> preferredVersions = new ArrayList<>();
            String retVal = (String)biblegetDB.getOption("PREFERREDVERSIONS");
            if(null==retVal){
                //System.out.println("Attempt to retrieve PREFERREDVERSIONS from the Database resulted in null value");
            }
            else{
                //System.out.println("Retrieved PREFERREDVERSIONS from the Database. Value is:"+retVal);
                String[] favoriteVersions = StringUtils.split(retVal,',');
                preferredVersions = Arrays.asList(favoriteVersions).subList(0, 1) ;
            }
            if(preferredVersions.size() < 1){
                preferredVersions.add("NVBSE");
            }
            List<Integer> preferredVersionsIndices = new ArrayList<>();

            versionsByLang = new SeparatorList<>(bibleVersions, new VersionComparator(),1, 1000);        
            int listLength = versionsByLang.size();
            enabledFlags = new boolean[listLength];        
            ListIterator itr = versionsByLang.listIterator();
            while(itr.hasNext()){
                int idx = itr.nextIndex();
                Object next = itr.next();
                enabledFlags[idx] = !(next.getClass().getSimpleName().equals("GroupSeparator"));
                if(next.getClass().getSimpleName().equals("BibleVersion")){
                    BibleVersion thisBibleVersion = (BibleVersion)next;
                    if(preferredVersions.contains(thisBibleVersion.getAbbrev())){
                        preferredVersionsIndices.add(idx);
                    }
                }
            }
            indices = ArrayUtils.toPrimitive(preferredVersionsIndices.toArray(new Integer[preferredVersionsIndices.size()]));
            //System.out.println("value of indices array: "+Arrays.toString(indices));
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
        jList1 = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jInternalFramePreviewArea = new javax.swing.JInternalFrame();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("searchFrame"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1200, 500));

        jPanelSettingsArea.setPreferredSize(new java.awt.Dimension(500, 500));

        jLabel1.setText("waiting for a browser click...");

        jProgressBar1.setStringPainted(true);

        jList1.setModel(new DefaultEventListModel(versionsByLang));
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setCellRenderer(new VersionCellRenderer());
        jList1.setSelectionModel(new DisabledItemSelectionModel());
        jScrollPane1.setViewportView(jList1);

        jLabel2.setText("Bible version to search from");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/bibleget/images/search_x32.png"))); // NOI18N
        jButton1.setText("Search");
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

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
                    .addComponent(jScrollPane1)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSettingsAreaLayout.createSequentialGroup()
                        .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField1)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelSettingsAreaLayout.setVerticalGroup(
            jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanelSettingsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
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
                            .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelSettingsAreaLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanelSettingsArea, java.awt.BorderLayout.WEST);

        jInternalFramePreviewArea.setVisible(true);
        getContentPane().add(jInternalFramePreviewArea, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

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
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanelSettingsArea;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables


    private class DisabledItemSelectionModel extends DefaultListSelectionModel {

        private static final long serialVersionUID = 1L;

        @Override
        public void setSelectionInterval(int index0, int index1) {
            if(index0 < index1){
                for (int i = index0; i <= index1; i++){
                    if(enabledFlags[i]){
                        super.addSelectionInterval(i, i);
                    }
                }
            }
            else if(index1 < index0){
                for (int i = index1; i <= index0; i++){
                    if(enabledFlags[i]){
                        super.addSelectionInterval(i, i);
                    }
                }
            }
            else if(index0 == index1){
                if(enabledFlags[index0]){ super.setSelectionInterval(index0,index0); }                
            }
        }

        @Override
        public void addSelectionInterval(int index0, int index1) {
            if(index0 < index1){
                for (int i = index0; i <= index1; i++){
                    if(enabledFlags[i]){
                        super.addSelectionInterval(i, i);
                    }
                }
            }
            else if(index1 < index0){
                for (int i = index1; i <= index0; i++){
                    if(enabledFlags[i]){
                        super.addSelectionInterval(i, i);
                    }
                }
            }
            else if(index0 == index1){
                if(enabledFlags[index0]){ super.addSelectionInterval(index0,index0); }
            }
        }        
        
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
        }
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
