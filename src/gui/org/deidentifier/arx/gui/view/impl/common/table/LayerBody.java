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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * The body layer
 * @author Fabian Prasser
 */
public class LayerBody extends CTLayer {

    /** Selection layer */
    private SelectionLayer selectionLayer;
    /** Data layer */

    /**
     * Creates a new instance
     * @param dataProvider
     */
    public LayerBody(IDataProvider dataProvider, CTConfiguration config, CTContext context) {
        super(config, context);
        IUniqueIndexLayer dataLayer = new DataLayer(dataProvider);
        
        switch(config.getColumnHeaderLayout()){
            case CTConfiguration.COLUMN_HEADER_LAYOUT_GRAB_LAST:
                dataLayer = new LayerColumnGrabLast(dataLayer, config, context);
                break;
            case CTConfiguration.COLUMN_HEADER_LAYOUT_GRAB_EQUAL:
                dataLayer = new LayerColumnGrabEqual(dataLayer, config, context);
                break;
        }
     
        switch(config.getRowHeaderLayout()){
            case CTConfiguration.ROW_HEADER_LAYOUT_FILL:
                dataLayer = new LayerRowFill(dataLayer, config, context);
                break;
        }
        
        selectionLayer = new LayerSelection(dataLayer, config);
        selectionLayer.addConfiguration(new StyleConfigurationSelection(config));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
        setUnderlyingLayer(viewportLayer);
    }

    /**
     * Returns the selection layer
     * @return
     */
    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }
}
