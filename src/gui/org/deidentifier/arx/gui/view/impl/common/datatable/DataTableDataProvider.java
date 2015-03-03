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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * A data provider that handles missing values.
 *
 * @author Fabian Prasser
 */
public class DataTableDataProvider implements IDataProvider {
	
	/** The wrapped provider. */
	private final IDataProvider provider;
	
	/**
     * Creates a new instance.
     *
     * @param provider
     */
	public DataTableDataProvider(IDataProvider provider){
		this.provider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return this.provider.getColumnCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getDataValue(int, int)
	 */
	@Override
	public Object getDataValue(int col, int row) {
        if (col == -1 || row == -1) return "";
		try {
			return provider.getDataValue(col, row);
		} catch (Exception e){
		    // TODO: We silently ignore all errors at this point
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return this.provider.getRowCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IDataProvider#setDataValue(int, int, java.lang.Object)
	 */
	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// Ignore
	}
}
