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

	@Override
	public int getColumnCount() {
		return this.provider.getColumnCount();
	}

	@Override
	public Object getDataValue(int col, int row) {
        if (col == -1 || row == -1) return ""; //$NON-NLS-1$
		try {
			return provider.getDataValue(col, row);
		} catch (Exception e){
		    // TODO: We silently ignore all errors at this point
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	public int getRowCount() {
		return this.provider.getRowCount();
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// Ignore
	}
}
