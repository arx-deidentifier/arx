/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.gui.resources.Resources;

/**
 * Configuration for the help dialog. Stores help topics and associated URLs
 * @author Fabian Prasser
 */
public class DialogHelpConfig {
    
    /**
     * An entry in the help dialog.
     *
     * @author Fabian Prasser
     */
    public static class Entry {
        
        /** ID */
        public final String id;
        
        /** Title */
        public final String title;
        
        /** URL */
        public final String url;
        
        /**
         * Creates a new entry.
         *
         * @param id
         * @param title
         * @param url
         */
        private Entry(String id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
        }
    }
    
    /** Entries */
    private List<Entry> entries = new ArrayList<Entry>();
    
    /**
     * Creates a new config.
     */
    public DialogHelpConfig() {
        
        final String version = ARXAnonymizer.VERSION;
        final String helpWebSite = ARXAnonymizer.HELP_WEBSITE;
        
        // Read messages.properties file
        final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("org.deidentifier.arx.gui.resources.messages"); //$NON-NLS-1$
        
        // Get all keys 
        Enumeration<String> messagesKeys = MESSAGES_BUNDLE.getKeys();
              
        // Get all keys for the help web pages    
        List<String> configList = new ArrayList<String>();
        while (messagesKeys.hasMoreElements()) {
        	String currentKey =   messagesKeys.nextElement(); 
        	if (currentKey.contains("DialogHelpConfig")){
               configList.add(currentKey.substring(17));
        	}
        }
        
        // Sorting
        configList.sort(null);
        
        // Create the help entries
        for (String idx : configList) {
             System.out.println(idx);
             entries.add(new Entry("id." + idx, //$NON-NLS-1$
                     Resources.getMessage("DialogHelpConfig." + idx), //$NON-NLS-1$
                     helpWebSite + version + Resources.getMessage("DialogHelpPage." + idx))); //$NON-NLS-1$
        }
    }
    
    /**
     * Returns all entries.
     *
     * @return
     */
    public List<Entry> getEntries() {
        return this.entries;
    }
    
    /**
     * Returns the index for a given ID.
     *
     * @param id
     * @return
     */
    public int getIndexForId(String id) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id.equals(id)) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Returns the index of a given URL.
     *
     * @param url
     * @return
     */
    public int getIndexForUrl(String url) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).url.equals(url)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns the URL for a given index.
     *
     * @param index
     * @return
     */
    public String getUrlForIndex(int index) {
        return entries.get(index).url;
    }
}
