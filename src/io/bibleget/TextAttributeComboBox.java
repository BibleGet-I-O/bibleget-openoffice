/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 *
 * @author Lwangaman
 * original author 
 */
 public class TextAttributeComboBox extends JComboBox {

    TextAttribute textAttribute;
    private final Font comboFont = UIManager.getFont("ComboBox.font");
    private static final Font DEFAULT_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
    private final FontModel fontModel = new FontModel(DEFAULT_FONT);
  
    public TextAttributeComboBox(TextAttribute textAttribute) {
      super(TextAttributeConstants.values(textAttribute));
      this.textAttribute = textAttribute;
      init();
    }

    public TextAttributeComboBox(Object[] values) {
      super(values);
    }
    
    //TODO: needs an empty attribute constructor to be able to put into palette; how do you handle this?
    public TextAttributeComboBox() { 

    }
        
    private void init() {
      setToolTipText(TextAttributeConstants.getFriendlyName(textAttribute));
      setSelectedIndex(0);

      setRenderer(new DefaultListCellRenderer() {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
          Map<TextAttribute, Object> attribute =
                  new HashMap<>();
          attribute.put(textAttribute, value);
          Font font = getFont().deriveFont(attribute);
          setFont(font);
          setText(TextAttributeConstants.lookup(textAttribute, value));
          if (isSelected) {
            setForeground(list.getSelectionForeground());
            setBackground(list.getSelectionBackground());
          } else {
            setForeground(list.getForeground());
            setBackground(list.getBackground());
          }
          return this;
        }
      });

      addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          Map<TextAttribute, Object> attribute = new HashMap<>();
          attribute.put(textAttribute, getSelectedItem());
          setFont(comboFont.deriveFont(attribute));
          fontModel.setAttributeValue(textAttribute, getSelectedItem());
        }
      });
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(208, 25);
    }

    @Override
    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void updateSelection() {
      if (textAttribute != null) { // it is null for fontCombo
        Object selectedItem = fontModel.getAttributeValue(textAttribute);
        setSelectedItem(selectedItem);
        if (getSelectedIndex() == -1) {
          setSelectedIndex(0);
        }
      }
    }
  }
