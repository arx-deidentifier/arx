/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
import java.util.List;

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
        
        /**  ID */
        public final String id;
        
        /**  Title */
        public final String title;
        
        /**  URL */
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
    
    /**  Entries */
    private List<Entry> entries = new ArrayList<Entry>();
    
    /**
     * Creates a new config.
     */
    public DialogHelpConfig(){
        
        entries.add(new Entry("id-70", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1082&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-140", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.1"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1055&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-3", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.7"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1076&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-1", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.10"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1074&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-51", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.13"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=3638&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-80", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.16"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1059&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-60", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.19"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1061&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-40", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.22"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1057&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-4", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.25"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1078&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-30", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.28"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1063&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-21", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.31"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1065&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-23", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.34"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1067&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-22", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.37"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1069&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-5", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.40"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1080&content-only=1&css=1")); //$NON-NLS-1$
        
        entries.add(new Entry("id-50", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.43"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=1071&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-3000", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.46"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=3797&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-3001", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.49"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=3799&content-only=1&css=1")); //$NON-NLS-1$

        entries.add(new Entry("id-3002", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.52"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/?page_id=3801&content-only=1&css=1")); //$NON-NLS-1$
    }
    
    /**
     * Returns all entries.
     *
     * @return
     */
    public List<Entry> getEntries(){
        return this.entries;
    }

    /**
     * Returns the index for a given ID.
     *
     * @param id
     * @return
     */
    public int getIndexForId(String id) {
        for (int i = 0; i < entries.size(); i++){
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
        for (int i = 0; i < entries.size(); i++){
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
