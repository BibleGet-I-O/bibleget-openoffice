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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author johnrdorazio
 */

public class VersionsHoverHandler extends MouseAdapter {
    private final VersionsSelect list;
    private final VersionCellRenderer renderer;
    private int hoverIndex = -1;

    public VersionsHoverHandler(VersionsSelect list){
        this.list = list;
        this.renderer = list.getRenderer();
    }

    @Override
    public void mouseExited(MouseEvent e) {
      setHoverIndex(-1);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      int index = list.locationToIndex(e.getPoint());
      setHoverIndex(list.getCellBounds(index, index).contains(e.getPoint())
              ? index : -1);
    }

    private void setHoverIndex(int index) {
      if (hoverIndex == index) return;
      hoverIndex = index;
      renderer.setHoverIndex(index);
      list.repaint();
    }

}

