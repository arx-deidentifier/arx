/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
        
        final String version = Resources.getVersion();
        
        entries.add(new Entry("id.overview.1", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.-5"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/methods/overview.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id.overview.2", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.-4"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/methods/privacy.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id.overview.3", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.-3"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/methods/transformation.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id.overview.4", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.-2"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/methods/utility.html")); //$NON-NLS-1$

        entries.add(new Entry("id.anonymization", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.-6"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/anonymization.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-70", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.0"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/perspectives.html")); //$NON-NLS-1$

        entries.add(new Entry("id-71", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.2"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/perspectives_configuration.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-72", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.3"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/perspectives_exploration.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-73", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.4"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/perspectives_utility.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-74", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.5"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/perspectives_risk.html")); //$NON-NLS-1$

        entries.add(new Entry("id-3", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.7"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/overview.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-140", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.1"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/inputdata.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-1", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.10"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/attributes.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-51", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.13"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/generalization_hierarchies.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-80", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.16"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/criteria.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-60", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.19"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/general_settings.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-40", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.22"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/configuration/research_subset.html")); //$NON-NLS-1$

        entries.add(new Entry("help.overview.settings", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.-1"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/global_settings.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-4", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.25"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/exploration/overview.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-30", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.28"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/exploration/solution_space.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-21", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.31"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/exploration/filtering.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-23", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.34"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/exploration/clipboard.html")); //$NON-NLS-1$
        
        entries.add(new Entry("id-22", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.37"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/exploration/properties.html")); //$NON-NLS-1$
        
        entries.add(new Entry("help.utility.overview", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.60"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/overview.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.data", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.61"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/data.html")); //$NON-NLS-1$
        
        entries.add(new Entry("help.utility.summary", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.62"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/summary.html")); //$NON-NLS-1$
        
        entries.add(new Entry("help.utility.distribution", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.63"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/distribution.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.contingency", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.64"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/contingency.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.classes", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.65"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/classes.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.inputproperties", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.66"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/inputproperties.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.outputproperties", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.67"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/outputproperties.html")); //$NON-NLS-1$
        
        entries.add(new Entry("help.utility.accuracy", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.68"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/accuracy.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.quality", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.70"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/quality.html")); //$NON-NLS-1$

        entries.add(new Entry("help.utility.localrecoding", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.69"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/utility/localrecoding.html")); //$NON-NLS-1$
        
        entries.add(new Entry("help.risk.overview", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.50"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/overview.html")); //$NON-NLS-1$

        entries.add(new Entry("help.risk.classsizes", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.51"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/classsizes.html")); //$NON-NLS-1$

        entries.add(new Entry("help.risk.quasiidentifiers", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.52"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/quasiidentifiers.html")); //$NON-NLS-1$

        entries.add(new Entry("help.risk.reidentification", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.53"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/reidentification.html")); //$NON-NLS-1$

        entries.add(new Entry("help.risk.hipaa", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.54"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/hipaa.html")); //$NON-NLS-1$

        entries.add(new Entry("help.risk.uniques", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.55"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/uniques.html")); //$NON-NLS-1$

        entries.add(new Entry("help.risk.population", //$NON-NLS-1$
                              Resources.getMessage("DialogHelpConfig.56"), //$NON-NLS-1$
                              "http://arx.deidentifier.org/help/v" + version + "/risk/population.html")); //$NON-NLS-1$
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
