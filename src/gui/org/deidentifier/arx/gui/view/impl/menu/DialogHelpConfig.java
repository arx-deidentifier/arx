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
        
        /**  TODO */
        public final String id;
        
        /**  TODO */
        public final String title;
        
        /**  TODO */
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
    
    /**  TODO */
    private List<Entry> entries = new ArrayList<Entry>();
    
    /**
     * Creates a new config.
     */
    public DialogHelpConfig(){
        
        entries.add(new Entry("id-70",
                              "1. Perspectives",
                              "http://arx.deidentifier.org/?page_id=1082&content-only=1&css=1"));

        entries.add(new Entry("id-140",
                              "1.1. Viewing and Manipulating Data",
                              "http://arx.deidentifier.org/?page_id=1055&content-only=1&css=1"));

        entries.add(new Entry("id-3",
                              "2. Defining the transformation",
                              "http://arx.deidentifier.org/?page_id=1076&content-only=1&css=1"));

        entries.add(new Entry("id-1",
                              "2.1. Defining attribute properties",
                              "http://arx.deidentifier.org/?page_id=1074&content-only=1&css=1"));
        
        entries.add(new Entry("id-51",
                              "2.2. Creating generalization hierarchies",
                              "http://arx.deidentifier.org/?page_id=3638&content-only=1&css=1"));
        
        entries.add(new Entry("id-80",
                              "2.3. Defining privacy criteria",
                              "http://arx.deidentifier.org/?page_id=1059&content-only=1&css=1"));
        
        entries.add(new Entry("id-60",
                              // TODO: Change title
                              "2.4. Defining general properties",
                              "http://arx.deidentifier.org/?page_id=1061&content-only=1&css=1"));

        entries.add(new Entry("id-40",
                              "2.5. Defining a research subset",
                              "http://arx.deidentifier.org/?page_id=1057&content-only=1&css=1"));

        entries.add(new Entry("id-4",
                              "3. Exploring the solution space",
                              "http://arx.deidentifier.org/?page_id=1078&content-only=1&css=1"));

        entries.add(new Entry("id-30",
                              "3.1. Exploring the lattice",
                              "http://arx.deidentifier.org/?page_id=1063&content-only=1&css=1"));
        
        entries.add(new Entry("id-21",
                              "3.2. Filtering the lattice",
                              "http://arx.deidentifier.org/?page_id=1065&content-only=1&css=1"));
        
        entries.add(new Entry("id-23",
                              "3.3. Using the clipboard",
                              "http://arx.deidentifier.org/?page_id=1067&content-only=1&css=1"));
        
        entries.add(new Entry("id-22",
                              "3.4. Properties of transformations",
                              "http://arx.deidentifier.org/?page_id=1069&content-only=1&css=1"));

        entries.add(new Entry("id-5",
                              "4. Analyzing transformed datasets",
                              "http://arx.deidentifier.org/?page_id=1080&content-only=1&css=1"));
        
        entries.add(new Entry("id-50",
                              "4.1. Visualizations and properties",
                              "http://arx.deidentifier.org/?page_id=1071&content-only=1&css=1"));
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
