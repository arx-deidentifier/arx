/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx;

/**
 * Stupid wrapper class that provides information to StatisticsBuilder
 * @author Fabian Prasser
 *
 */
public class DataHandleStatistics {

    public static interface WrappedBoolean {
        public boolean getValue();
    }
    
    private DataHandle handle;
    
    protected DataHandleStatistics(DataHandle handle){
        this.handle = handle;
    }

    public String[] getDistinctValues(int column, WrappedBoolean stop) {
        return handle.getDistinctValues(column, stop);
    }

    public String getAttributeName(int column) {
        return handle.getAttributeName(column);
    }

    public DataType<?> getDataType(String attribute) {
        return handle.getDataType(attribute);
    }

    public int getGeneralization(String attribute) {
        return handle.getGeneralization(attribute);
    }

    public String getSuppressionString() {
        return handle.getSuppressionString();
    }

    public DataType<?> getBaseDataType(String attribute) {
        return handle.getBaseDataType(attribute);
    }

    public int getNumRows() {   
        return handle.getNumRows();
    }

    public String getValue(int row, int column) {
        return handle.getValue(row, column);
    }

    public DataDefinition getDefinition() {
        return handle.getDefinition();
    }
}
