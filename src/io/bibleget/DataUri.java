/*
 * Copyright 2020 John R. D'Orazio
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

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John R. D'Orazio (priest@johnromanodorazio.com)
 * Utility class for creating data: URIs that can be passed to CefBrowser.loadURL.
 * Example provided in jcefbuild tests.detailed.util
 */
public class DataUri {
    public static String create(String mimeType, String contents) {
        try {
            return "data:" + mimeType + ";base64,"
                    + java.util.Base64.getEncoder().encodeToString(contents.getBytes("utf-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DataUri.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "data:" + mimeType + ";base64,"
                    + java.util.Base64.getEncoder().encodeToString(contents.getBytes());
    }
};
