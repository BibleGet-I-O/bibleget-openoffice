/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.UnoRuntime;
import static io.bibleget.BibleGetI18N.__;
import java.awt.HeadlessException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Lwangaman
 */
public class BibleGetSelection {
    private static BibleGetSelection instance;
    private final XController m_xController;
    private final XModel m_xModel;
    private final XTextDocument m_xTextDocument;
    private static BibleGetDB biblegetDB;
    
    private BibleGetSelection(XController xController) throws ClassNotFoundException, SQLException
    {
        m_xController = xController;
        m_xModel = (XModel) m_xController.getModel();
        m_xTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,m_xModel);        
        
        biblegetDB = BibleGetDB.getInstance();
}

    private BibleGetSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static BibleGetSelection getInstance(XController xController) throws ClassNotFoundException, SQLException
    {
        if(instance == null)
        {
            instance = new BibleGetSelection(xController);
        }
        return instance;
    }

    public void getQuoteFromSelection()
    {
        List<String> preferredVersions = new ArrayList<>();
        Object retVal = biblegetDB.getOption("PREFERREDVERSIONS");
        if(null==retVal){
        }
        else{
            String[] favoriteVersions = StringUtils.split((String)retVal,',');
            preferredVersions = Arrays.asList(favoriteVersions);
        }
        
        if(preferredVersions.isEmpty()){
            preferredVersions.add("NVBSE");
        }
        
        String versions = StringUtils.join(preferredVersions.toArray(), ',');

        Object m_xCurSel;
        try{
            m_xCurSel = m_xTextDocument.getCurrentSelection();
            if(m_xCurSel != null){
                //there is a selection, we can do something with it
                XIndexAccess xIndex = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class,m_xCurSel);
                Object Sel0;
                try {
                    Sel0 = xIndex.getByIndex(0);
                    XTextRange xTextRange = (XTextRange) UnoRuntime.queryInterface(XTextRange.class,Sel0);            
                    String myInputContent = xTextRange.getString();
                    myInputContent = StringUtils.deleteWhitespace(myInputContent);
                    //System.out.println("You typed : "+myInputContent);
                    if(myInputContent.isEmpty()==false){

                        HTTPCaller myHTTPCaller = new HTTPCaller();
                        String myResponse;
                        try {
                            Boolean querycheck = myHTTPCaller.integrityCheck(myInputContent,preferredVersions);
                            if(querycheck){
                                //JOptionPane.showMessageDialog(null, "All is proceeding nicely", "progress info", JOptionPane.INFORMATION_MESSAGE);
                                myResponse = myHTTPCaller.sendGet(myInputContent,versions);
                                if(myResponse != null){
                                    xTextRange.setString("");
                                    BibleGetJSON myJSON = new BibleGetJSON(m_xController);
                                    myJSON.JSONParse(myResponse);
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
                        } catch (HeadlessException | ClassNotFoundException | UnknownPropertyException | PropertyVetoException | com.sun.star.lang.IllegalArgumentException | WrappedTargetException ex) {
                            Logger.getLogger(BibleGetFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,__("You cannot send an empty query."),"ERROR >> EMPTY SELECTION",JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IndexOutOfBoundsException | WrappedTargetException ex) {
                    Logger.getLogger(BibleGetSelection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(BibleGetSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
