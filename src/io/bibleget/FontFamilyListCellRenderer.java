/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 *
 * @author Lwangaman
 */
public class FontFamilyListCellRenderer extends DefaultListCellRenderer{

    //protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private final Font defaultFont = new Font(super.getFont().getFontName(),Font.PLAIN,18);

    @Override
    public Color getBackground() {
        return super.getBackground(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     *
     */
    public FontFamilyListCellRenderer()
    {
    }

    @Override
    public Component getListCellRendererComponent(JList list,Object val,int idx,boolean isSelected,boolean cellHasFocus)
    {

        // Set the font according to the selected index
        //Font valFont = (Font) val;
        Font font = new Font(val.toString(),Font.PLAIN,18);
        // Set text (mandatory)
        //setText(val.toString());
        setText(font.getFontName());
        //setFont(font); //-> deactivated because slowing down too much!
        if (font.canDisplayUpTo(font.getFontName()) == -1 || font.canDisplayUpTo(font.getFontName()) >= font.getFontName().length()) {
            setForeground(Color.BLACK);
            setFont(font);
        } else {
          setForeground(Color.RED);
          setFont(defaultFont);
        }
        
        
        // Set your custom selection background, background
        // Or you can get them as parameters as you got the table
        
        if(isSelected){
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else{
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
        //setOpaque(true);
        if(cellHasFocus){
            setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
            //setBackground(getBackground());
            //setBackground(list.getSelectionBackground());
        }
        else{
            setBorder(noFocusBorder);
        }
        return this;
    }

}


