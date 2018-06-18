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
package org.deidentifier.arx.io;

import java.io.Serializable;

import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * Additional options for reading/writing CSV files
 * 
 * @author Fabian Prasser
 */
public class CSVOptions implements Serializable { // NO_UCD

    /** SVUID */
    private static final long serialVersionUID = 2422613628612481137L;

    /** The max columns. */
    private Integer           maxColumns;
    
    /**
     * Creates new options
     * @param maxColumns
     */
    public CSVOptions(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    /**
     * @return the maxColumns
     */
    public int getMaxColumns() {
        return maxColumns;
    }

    /**
     * @param maxColumns the maxColumns to set
     */
    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    /**
     * Applies the options
     * @param settings
     */
    void apply(CsvParserSettings settings) {
        if (maxColumns != null) {
            settings.setMaxColumns(maxColumns);
        }
    }

    /**
     * Applies the options
     * @param settings
     */
    void apply(CsvWriterSettings settings) {
        if (maxColumns != null) {
            settings.setMaxColumns(maxColumns);
        }
    }
}
