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
import java.util.List;

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
        
        //TODO: replace by a loop  
        entries.add(new Entry("id.0100", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0100"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0100"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0101", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0101"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0101"))); //$NON-NLS-1$

        entries.add(new Entry("id.0102", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0102"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0102"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0103", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0103"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0103"))); //$NON-NLS-1$

        entries.add(new Entry("id.0104", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0104"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0104"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0200", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0200"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0200"))); //$NON-NLS-1$

        entries.add(new Entry("id.0201", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0201"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0201"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0202", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0202"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0202"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0203", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0203"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0203"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0204", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0204"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0204"))); //$NON-NLS-1$

        entries.add(new Entry("id.0300", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0300"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0300"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0301", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0301"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0301"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0302", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0302"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0302"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0303", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0303"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0303"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0304", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0304"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0304"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0305", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0305"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0305"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0306", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0306"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0306"))); //$NON-NLS-1$

        entries.add(new Entry("id.0307", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0307"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0307"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0400", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0400"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0400"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0401", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0401"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0401"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0402", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0402"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0402"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0403", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0403"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0403"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0404", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0404"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0404"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0500", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0500"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0500"))); //$NON-NLS-1$

        entries.add(new Entry("id.0501", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0501"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0501"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0502", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0502"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0502"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0503", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0503"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0503"))); //$NON-NLS-1$

        entries.add(new Entry("id.0504", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0504"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0504"))); //$NON-NLS-1$

        entries.add(new Entry("id.0505", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0505"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0505"))); //$NON-NLS-1$

        entries.add(new Entry("id.0506", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0506"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0506"))); //$NON-NLS-1$

        entries.add(new Entry("id.0507", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0507"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0507"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0508", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0508"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0508"))); //$NON-NLS-1$

        entries.add(new Entry("id.0509", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0509"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0509"))); //$NON-NLS-1$

        entries.add(new Entry("id.0510", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0510"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0510"))); //$NON-NLS-1$
        
        entries.add(new Entry("id.0600", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0600"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0600"))); //$NON-NLS-1$

        entries.add(new Entry("id.0601", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0601"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0601"))); //$NON-NLS-1$

        entries.add(new Entry("id.0602", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0602"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0602"))); //$NON-NLS-1$

        entries.add(new Entry("id.0603", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0603"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0603"))); //$NON-NLS-1$

        entries.add(new Entry("id.0604", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0604"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0604"))); //$NON-NLS-1$

        entries.add(new Entry("id.0605", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0605"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0605"))); //$NON-NLS-1$

        entries.add(new Entry("id.0606", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0606"), //$NON-NLS-1$
                              helpWebSite + version + Resources.getMessage("DialogHelpPage.0606"))); //$NON-NLS-1$
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
