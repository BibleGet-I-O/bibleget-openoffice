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
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import org.apache.commons.lang3.ArrayUtils;
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
    private VersionCellRenderer renderer;
    
    private static DBHelper biblegetDB;
        
    @SuppressWarnings("unchecked")
    public VersionsSelect(boolean loadPreferred, int listSelectionModel) throws ClassNotFoundException, SQLException, Exception
    {       
        biblegetDB = DBHelper.getInstance();
        String bibleVersionsStr = biblegetDB.getMetaData("VERSIONS");
        if(null == bibleVersionsStr){
            System.out.println(this.getClass().getSimpleName() + "-> We have a problem Watson! bibleVersionsStr is null.");
            bibleVersions = null;
            versionsByLang = null;
            enabledFlags = null;
        } else {
            JsonReader jsonReader = Json.createReader(new StringReader(bibleVersionsStr));
            JsonObject bibleVersionsObj = jsonReader.readObject();
            Set<String> versionsabbrev = bibleVersionsObj.keySet();
            bibleVersions = new BasicEventList<>();
            if(!versionsabbrev.isEmpty()){
                versionsabbrev.stream().forEach((String s) -> {
                    String versionStr = bibleVersionsObj.getString(s); //store these in an array
                    String[] array;
                    array = versionStr.split("\\|");
                    bibleVersions.add(new BibleVersion(s,array[0],array[1],StringUtils.capitalize(new Locale(array[2]).getDisplayLanguage())));
                });
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
                if(enabledFlags[idx]){
                    versionCount++;
                }
                else{
                    versionLangs++;
                }
                if(next.getClass().getSimpleName().equals("BibleVersion")){
                    BibleVersion thisBibleVersion = (BibleVersion)next;
                    if(preferredVersions.contains(thisBibleVersion.getAbbrev())){
                        preferredVersionsIndices.add(idx);
                    }
                }
            }
            int[] indices;
            indices = ArrayUtils.toPrimitive(preferredVersionsIndices.toArray(new Integer[preferredVersionsIndices.size()]));

            this.setModel(new DefaultEventListModel<>(versionsByLang));
            this.renderer = new VersionCellRenderer();
            this.setCellRenderer(renderer);
            
            this.setSelectionMode(listSelectionModel);
            this.setSelectionModel(new DisabledItemSelectionModel());
            if(loadPreferred && (listSelectionModel == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION || listSelectionModel == ListSelectionModel.SINGLE_INTERVAL_SELECTION ) ){
                this.setSelectedIndices(indices);
            } else if(loadPreferred && (listSelectionModel == ListSelectionModel.SINGLE_SELECTION ) ){
                this.setSelectedIndex(indices[0]);
            }
        }
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
    
    public VersionCellRenderer getRenderer(){
        return renderer;
    }
    
    private static class VersionComparator implements Comparator<BibleVersion> {

        @Override
        public int compare(BibleVersion o1, BibleVersion o2) {
            return o1.getLang().compareTo(o2.getLang());
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
