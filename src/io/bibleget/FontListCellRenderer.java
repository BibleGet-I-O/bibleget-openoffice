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
import javax.swing.ListCellRenderer;

  /**
   * A ListCellRenderer that displays font names in their own font if possible.  The list's
   * model must contain objects of type Font, or a ClassCastException will be thrown.
   * <P>
   * A font that lacks the glyphs to display its own name is rendered with the default font,
   * in red or a specified color.
   */
  public class FontListCellRenderer extends DefaultListCellRenderer {

    private final Font defaultFont = super.getFont();
    private final Color symbolColor;

    /**
     * Constructs a <CODE>FontListCellRenderer</CODE>.  Any font that lacks the glyphs to display
     * its own name is rendered in red with the default Font of a {@link DefaultListCellRenderer}.
     */
    public FontListCellRenderer() {
      this(Color.RED);
    }

    /**
     * Constructs a <CODE>FontListCellRenderer</CODE> with a custom color for symbolic fonts.
     * Any font that lacks the glyphs to display its own name is rendered in the passed in color
     * with the default Font of a {@link DefaultListCellRenderer}.
     *
     * @param symbolColor the color is which to render a font which is unable to display its name
     */
    public FontListCellRenderer(Color symbolColor) {
      if (symbolColor == null) {
        this.symbolColor = Color.RED;
      } else {
        this.symbolColor = symbolColor;
      }
    }

    /**
     * Return a component that has been configured to display a font name.  Any font that
     * lacks the glyphs to display its own name is rendered with the default Font of a
     * DefaultListCellRenderer in red or a custom color passed to the constructor.
     *
     * @param list the JList to which to render
     * @param value the value returned by list.getModel().getElementAt(index)
     * @param index the cell's index
     * @param isSelected true if the specified cell was selected
     * @param cellHasFocus true if the specified cell has the focus.
     *
     * @return a component whose paint() method will render the specified value.
     *
     * @see ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     * java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList list,
            Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
      Font font = (Font) value;
      setText(font.getName());
      if (isSelected) {
        setForeground(list.getSelectionForeground());
        setBackground(list.getSelectionBackground());
      } else {
        setForeground(list.getForeground());
        setBackground(list.getBackground());
      }
      if (font.canDisplayUpTo(font.getFontName()) == -1 || font.canDisplayUpTo(font.getFontName()) >= font.getFontName().length()) {
        setFont(font);
      } else {
        setForeground(symbolColor);
        setFont(defaultFont);
      }
      return this;
    }
  }