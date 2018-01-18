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

    @Override
    public void clear() {
        this.setData(null);
    }

    @Override
    public int getColumnCount() {
        if (data == null) return 0;
        else return data.getColumnCount();
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        if (columnIndex == -1 || rowIndex == -1) return ""; //$NON-NLS-1$
        if (data == null) return null;
        else return data.getDataValue(columnIndex, rowIndex);
    }

    @Override
    public int getRowCount() {
        if (data == null) return 0;
        else return data.getRowCount();
    }

    @Override
    public void setData(IDataProvider data) {
        this.data = data;
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        if (data == null) return;
        else data.setDataValue(columnIndex, rowIndex, newValue);
    }
}
