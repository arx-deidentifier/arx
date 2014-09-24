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

package org.deidentifier.arx.gui.view.impl.common.datatable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * A data provider that handles missing values
 * @author Fabian Prasser
 *
 */
public class DataTableDataProvider implements IDataProvider {
	
	/** The wrapped provider*/
	private final IDataProvider provider;
	
	/**
	 * Creates a new instance
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
        if (col == -1 || row == -1) return "";
		try {
			return provider.getDataValue(col, row);
		} catch (Exception e){
			System.out.println("Caught: "+e.getClass().getSimpleName());
			return "";
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
