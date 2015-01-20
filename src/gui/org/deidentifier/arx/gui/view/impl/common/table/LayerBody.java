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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * The body layer.
 *
 * @author Fabian Prasser
 */
public class LayerBody extends CTLayer {

    /** Selection layer. */
    private final SelectionLayer selectionLayer;
    
    /** Data layer. */
    private final DataLayer dataLayer;
    
    /** Viewport layer. */
    private final LayerViewport viewportLayer;

    /**
     * Creates a new instance.
     *
     * @param dataProvider
     * @param config
     * @param context
     */
    public LayerBody(IDataProvider dataProvider, CTConfiguration config, CTContext context) {
        super(config, context);
        
        dataLayer = new DataLayer(dataProvider);

        selectionLayer = new LayerSelection(dataLayer, config);
        selectionLayer.addConfiguration(new StyleConfigurationSelection(config));
        IUniqueIndexLayer layer = selectionLayer;
        
        switch(config.getColumnHeaderLayout()){
            case CTConfiguration.COLUMN_HEADER_LAYOUT_FILL:
                layer = new LayerColumnFillLayout(layer, config, context);
                break;
            case CTConfiguration.COLUMN_HEADER_LAYOUT_FILL_EQUAL:
                layer = new LayerColumnFillLayout(layer, config, context, true);
                break;
        }
     
        switch(config.getRowHeaderLayout()){
            case CTConfiguration.ROW_HEADER_LAYOUT_FILL:
                layer = new LayerRowFillLayout(layer, config, context);
                break;
        }
        
        viewportLayer = new LayerViewport(layer, context);
        setUnderlyingLayer(viewportLayer);
    }

    /**
     * Returns the selection layer.
     *
     * @return
     */
    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }
    
    /**
     * Returns the data layer.
     *
     * @return
     */
    public DataLayer getDataLayer() {
        return dataLayer;
    }
    
    /**
     * Returns the viewport layer.
     *
     * @return
     */
    public LayerViewport getViewportLayer() {
        return viewportLayer;
    }
}
