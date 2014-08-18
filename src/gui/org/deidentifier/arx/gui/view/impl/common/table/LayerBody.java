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

/**
 * The body layer
 * @author Fabian Prasser
 */
public class LayerBody extends CTLayer {

    /** Selection layer */
    private final SelectionLayer selectionLayer;
    /** Data layer */
    private final DataLayer dataLayer;
    /** Viewport layer */
    private final LayerViewport viewportLayer;

    /**
     * Creates a new instance
     * @param dataProvider
     */
    public LayerBody(IDataProvider dataProvider, CTConfiguration config, CTContext context) {
        super(config, context);
        
        dataLayer = new DataLayer(dataProvider);

        selectionLayer = new LayerSelection(dataLayer, config);
        selectionLayer.addConfiguration(new StyleConfigurationSelection(config));
        IUniqueIndexLayer layer = selectionLayer;
        
        switch(config.getColumnHeaderLayout()){
            case CTConfiguration.COLUMN_HEADER_LAYOUT_GRAB_LAST:
                layer = new LayerColumnGrabLast(layer, config, context);
                break;
            case CTConfiguration.COLUMN_HEADER_LAYOUT_GRAB_EQUAL:
                layer = new LayerColumnGrabEqual(layer, config, context);
                break;
        }
     
        switch(config.getRowHeaderLayout()){
            case CTConfiguration.ROW_HEADER_LAYOUT_FILL:
                layer = new LayerRowFill(layer, config, context);
                break;
        }
        
        viewportLayer = new LayerViewport(layer, context);
        setUnderlyingLayer(viewportLayer);
    }

    /**
     * Returns the selection layer
     * @return
     */
    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }
    
    /**
     * Returns the data layer
     * @return
     */
    public DataLayer getDataLayer() {
        return dataLayer;
    }
    
    /**
     * Returns the viewport layer
     * @return
     */
    public LayerViewport getViewportLayer() {
        return viewportLayer;
    }
}
