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

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandler;

/**
 *
 * @author johnrdorazio
 */
public class JCEFContextMenuHandler implements CefContextMenuHandler {

    @Override
    public void onBeforeContextMenu(CefBrowser cb, CefFrame cf, CefContextMenuParams ccmp, CefMenuModel cmm) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        cmm.clear();
    }

    @Override
    public boolean onContextMenuCommand(CefBrowser cb, CefFrame cf, CefContextMenuParams ccmp, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onContextMenuDismissed(CefBrowser cb, CefFrame cf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
