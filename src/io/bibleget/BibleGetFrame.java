/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.frame.XController;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import static io.bibleget.BibleGetI18N.__;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Lwangaman
 */
public final class BibleGetFrame extends javax.swing.JFrame {

    private final XController m_xController;
    private final int screenWidth;
    private final int screenHeight;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameLeft;
    private final int frameTop;
    
    private static BibleGetDB biblegetDB;
    private SeparatorList<BibleVersion> versionsByLang;
    private EventList<BibleVersion> bibleVersions;
    private boolean[] enabledFlags;
    private int[] indices;

    
    private static BibleGetFrame instance;
    
    /**
     * Creates new form BibleGetFrame
     * @param xController
     */
    private BibleGetFrame(XController xController) throws ClassNotFoundException, SQLException {
        m_xController = xController;
        screenWidth = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        screenHeight = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        frameWidth = 500;
        frameHeight = 500;
        frameLeft = (screenWidth / 2) - (frameWidth / 2);
        frameTop = (screenHeight / 2) - (frameHeight / 2);
        
        prepareDynamicInformation();
        initComponents();
    }

    private BibleGetFrame() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static BibleGetFrame getInstance(XController xController) throws ClassNotFoundException, SQLException
    {
        if(instance == null)
        {
            instance = new BibleGetFrame(xController);
        }
        return instance;
    }

    /**
     *
     * @throws ClassNotFoundException
     */
    private void prepareDynamicInformation() throws ClassNotFoundException, SQLException{
        biblegetDB = BibleGetDB.getInstance();
        String bibleVersionsStr = biblegetDB.getMetaData("VERSIONS");
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
            preferredVersions = Arrays.asList(favoriteVersions);
        }
        if(preferredVersions.isEmpty()){
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
    
    public void updateDynamicInformation() throws ClassNotFoundException, SQLException{
        prepareDynamicInformation();
        jList1.setModel(new DefaultEventListModel<>(versionsByLang));
        jList1.setCellRenderer(new VersionCellRenderer());
        jList1.setSelectionModel(new DisabledItemSelectionModel());
        ListSelectionModel listSelectionModel = jList1.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
        jList1.setSelectedIndices(indices);
        jScrollPane2.setViewportView(jList1);
        jScrollPane2.revalidate();        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(__("Insert quote from input window"));
        setBounds(frameLeft,frameTop,frameWidth,frameHeight);
        setType(java.awt.Window.Type.UTILITY);

        jLabel1.setLabelFor(jTextField1);
        jLabel1.setText(__("Type the desired Bible Quote using standard notation:"));

        jTextField1.setToolTipText("e.g. Mt1,1-10");

        jLabel2.setBackground(new java.awt.Color(204, 255, 204));
        jLabel2.setFont(new java.awt.Font("Times New Roman", 2, 11)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText(__("(e.g. Mt 1,1-10.12-15;5,3-4;Jn 3,16)"));

        jLabel4.setText(__("Choose version (or versions)"));

        jList1.setModel(new DefaultEventListModel<>(versionsByLang));
        jList1.setCellRenderer(new VersionCellRenderer());
        jList1.setSelectionModel(new DisabledItemSelectionModel());
        ListSelectionModel listSelectionModel = jList1.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
        jList1.setSelectedIndices(indices);
        jScrollPane2.setViewportView(jList1);

        jButton1.setText(__("Send query"));
        jButton1.setToolTipText(__("Sends the request to the server and returns the results to the document."));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setText(__("Cancel"));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(57, 57, 57))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(39, 39, 39))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        List<String> selectedVersions = new ArrayList<>();
        if (jList1.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(null, __("You must select at least one version in order to make a request."), "ERROR >> NO VERSIONS SELECTED", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            // Find out which indexes are selected.
            int minIndex = jList1.getMinSelectionIndex();
            int maxIndex = jList1.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (jList1.isSelectedIndex(i)) {
                    String abbrev = versionsByLang.get(i).getAbbrev();
                    selectedVersions.add(abbrev);
                }
            }
        }
        String versionsSelcd = StringUtils.join(selectedVersions.toArray(), ',');

        
        String myInputContent = jTextField1.getText();
        myInputContent = StringUtils.deleteWhitespace(myInputContent);
        //System.out.println("You typed : "+myInputContent);
        
        HTTPCaller myHTTPCaller = new HTTPCaller();
        String myResponse;
        try {
            Boolean querycheck = myHTTPCaller.integrityCheck(myInputContent,selectedVersions);
            if(querycheck){
                //JOptionPane.showMessageDialog(null, "All is proceeding nicely", "progress info", JOptionPane.INFORMATION_MESSAGE);
                myResponse = myHTTPCaller.sendGet(myInputContent,versionsSelcd);
                if(myResponse != null){
                    BibleGetJSON myJSON = new BibleGetJSON(m_xController);
                    myJSON.JSONParse(myResponse);
                    this.setVisible(false);
                }
                else{
                    JOptionPane.showMessageDialog(null,__("There was a problem communicating with the BibleGet server. Please try again."),"ERROR >> SERVER CONNECTIVITY ISSUE",JOptionPane.ERROR_MESSAGE);
                }
            }
            else{
                String[] errorMessages = myHTTPCaller.getErrorMessages();
                String errorDialog = StringUtils.join(errorMessages,"\n\n");
                JOptionPane.showMessageDialog(null, errorDialog, "ERROR >> MALFORMED QUERYSTRING", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HeadlessException | ClassNotFoundException | UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException | UnsupportedEncodingException | SQLException ex) {
            Logger.getLogger(BibleGetFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        setVisible(false);
    }//GEN-LAST:event_jButton2MouseClicked
    
    
    private static class VersionComparator implements Comparator<BibleVersion> {

        @Override
        public int compare(BibleVersion o1, BibleVersion o2) {
            return o1.getLang().compareTo(o2.getLang());
        }            
        
    }

    private static class VersionCellRenderer extends DefaultListCellRenderer{
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof SeparatorList.Separator) {
                SeparatorList.Separator separator = (SeparatorList.Separator) value;
                BibleVersion bibleversion = (BibleVersion)separator.getGroup().get(0);
                String lbl = "-- " + bibleversion.getLang() + " --";
                label.setText(lbl);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBackground(Color.decode("#999999"));
                //label.setForeground(Color.BLACK);
                label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
                //label.setEnabled(false);
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
                label.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
                
            }
            
            return label;
        }
    }

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
            int firstIndex = e.getFirstIndex();
            int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            
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
            if(biblegetDB.setStringOption("PREFERREDVERSIONS", versionsSelcd)){
                //System.out.println("Database was updated with preferred versions: "+versionsSelcd);
            }
            else{
                //System.out.println("Database was not updated with preferred versions.");
            }
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
            java.util.logging.Logger.getLogger(BibleGetFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BibleGetFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
