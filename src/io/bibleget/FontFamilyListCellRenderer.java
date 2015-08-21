/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 *
 * @author Lwangaman
 */
public class FontFamilyListCellRenderer extends DefaultListCellRenderer{

    //protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    
    /**
     *
     */
    public FontFamilyListCellRenderer()
    {
        // Set opaque for the background to be visible
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list,Object val,int idx,boolean isSelected,boolean cellHasFocus)
    {
        // Set text (mandatory)
        setText(val.toString());

        // Set the font according to the selected index
        
        //setFont(new Font(val.toString(),Font.PLAIN,18)); -> deactivated because slowing down too much!
        
        // Set your custom selection background, background
        // Or you can get them as parameters as you got the table
        /*
        if(isSelected){
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else{
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        */
        if(cellHasFocus){
            setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
        }
        else{
            setBorder(noFocusBorder);
        }

        return this;
    }

}


