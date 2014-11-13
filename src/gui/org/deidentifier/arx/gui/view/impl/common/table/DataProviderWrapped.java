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
