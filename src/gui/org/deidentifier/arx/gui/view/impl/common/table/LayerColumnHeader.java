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
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultColumnResizeBindings;
import org.eclipse.swt.widgets.Composite;

/**
 * The column layer
 * @author Fabian Prasser
 */
public class LayerColumnHeader extends CTLayer {
    
    /**
     * Creates a new instance
     * @param parent
     * @param dataProvider
     * @param bodyLayer
     */
    public LayerColumnHeader(Composite parent,
                                  IDataProvider dataProvider,
                                  LayerBody bodyLayer,
                                  CTConfiguration config, CTContext context) {
        super(config, context);
        
        DataLayer dataLayer = new DataLayer(dataProvider);
        ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer,
                                                                 bodyLayer,
                                                                 bodyLayer.getSelectionLayer(),
                                                                 false);
        colHeaderLayer.addConfiguration(new StyleConfigurationHeader(parent, GridRegion.COLUMN_HEADER, config));
        colHeaderLayer.addConfiguration(new DefaultColumnResizeBindings());
        setUnderlyingLayer(colHeaderLayer);
    }
}