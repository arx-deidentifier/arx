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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * 
 * @author Fabian Prasser
 *
 */
public class DataProviderWrapped implements CTDataProvider {
    
    /**  TODO */
    private IDataProvider data = null;

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.table.CTDataProvider#clear()
     */
    @Override
    public void clear() {
        this.setData(null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        if (data == null) return 0;
        else return data.getColumnCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getDataValue(int, int)
     */
    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        if (columnIndex == -1 || rowIndex == -1) return "";
        if (data == null) return null;
        else return data.getDataValue(columnIndex, rowIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getRowCount()
     */
    @Override
    public int getRowCount() {
        if (data == null) return 0;
        else return data.getRowCount();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.table.CTDataProvider#setData(org.eclipse.nebula.widgets.nattable.data.IDataProvider)
     */
    @Override
    public void setData(IDataProvider data) {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#setDataValue(int, int, java.lang.Object)
     */
    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        if (data == null) return;
        else data.setDataValue(columnIndex, rowIndex, newValue);
    }
}
