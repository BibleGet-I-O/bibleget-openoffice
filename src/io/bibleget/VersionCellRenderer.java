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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author johnrdorazio
 */
public class VersionCellRenderer extends DefaultListCellRenderer {
    
    private int hoverIndex = -1;
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof SeparatorList.Separator) {
            SeparatorList.Separator separator = (SeparatorList.Separator) value;
            BibleVersion bibleversion = (BibleVersion)separator.getGroup().get(0);
            String lbl = "-- " + bibleversion.getLang() + " --";
            label.setText(lbl);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setForeground(Color.WHITE);
            label.setBackground(Color.decode("#000099"));
            //label.setForeground(Color.BLACK);
            label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
            //label.setEnabled(false);
        } else {
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            label.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
            if(isSelected){
                label.setBackground(Color.CYAN);
            } else {
                if(index == hoverIndex){
                    label.setBackground(Color.YELLOW);
                }
            }
        }

        return label;
    }
    
    public void setHoverIndex(int idx){
        this.hoverIndex = idx;
    }
}
