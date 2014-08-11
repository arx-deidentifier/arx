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

package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

/**
 * Configuration for table view
 * @author Fabian Prasser
 *
 */
public class ComponentTableHeaderConfigurationList extends ComponentTableHeaderConfiguration{

    /** Data*/
    private NatTable table;
    /** Data*/
    private DataLayer dataLayer;
    
    /**
     * Creates a new instance
     */
    public ComponentTableHeaderConfigurationList() {
        super(0);
    }

    @Override
    protected void init(NatTable table, DataLayer dataLayer, CornerLayer cornerLayer, IDataProvider dataProvider, int width) {

        // Store
        this.dataLayer = dataLayer;
        this.table = table;

        if (dataLayer.getColumnCount()!=1) return;
        
        // Done
        table.setRedraw(false);
        
        // Set
        dataLayer.setColumnWidthByPosition(0, width, true);
        
        // Done
        table.setRedraw(true);
        table.redraw();
    }

    @Override
    protected void update(int width) {

        if (table == null || table.isDisposed()) return;
        if (dataLayer == null) return;
        if (dataLayer.getColumnCount()!=1) return;
        
        // Done
        table.setRedraw(false);
        
        // Set
        dataLayer.setColumnWidthByPosition(0, width, true);
        
        // Done
        table.setRedraw(true);
        table.redraw();
    }
}
