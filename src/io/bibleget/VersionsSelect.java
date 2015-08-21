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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.StringReader;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JList;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Lwangaman
 */
public class VersionsSelect extends javax.swing.JList {
    private final EventList<BibleVersion> bibleVersions;
    private final SeparatorList<BibleVersion> versionsByLang;
    private final boolean[] enabledFlags;
    private int versionLangs = 0;
    private int versionCount = 0;
   
    private static BibleGetDB biblegetDB;
        
    public VersionsSelect() throws ClassNotFoundException
    {       
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
        
        versionsByLang = new SeparatorList<>(bibleVersions, new VersionComparator(),1, 1000);
        
        int listLength = versionsByLang.size();
        enabledFlags = new boolean[listLength];
        
        ListIterator itr = versionsByLang.listIterator();
        while(itr.hasNext()){
            int idx = itr.nextIndex();
            Object next = itr.next();
            enabledFlags[idx] = !(next.getClass().getSimpleName().equals("GroupSeparator"));
            if(enabledFlags[idx]){
                versionCount++;
            }
            else{
                versionLangs++;
            }
        }
    
        this.setModel(new DefaultEventListModel<>(versionsByLang));
        this.setCellRenderer(new VersionCellRenderer());
        this.setSelectionModel(new DisabledItemSelectionModel());
    }
            
    public SeparatorList getVersionsByLangList()
    {
        return versionsByLang;
    }
    
    public int getVersionCount(){
        return versionCount;
    }
    
    public int getVersionLangs(){
        return versionLangs;
    }
    
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

}
