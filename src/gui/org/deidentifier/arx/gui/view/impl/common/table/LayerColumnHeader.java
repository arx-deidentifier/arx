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
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.resize.config.DefaultColumnResizeBindings;
import org.eclipse.swt.widgets.Composite;

/**
 * The column layer.
 *
 * @author Fabian Prasser
 */
public class LayerColumnHeader extends CTLayer {
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param dataProvider
     * @param bodyLayer
     * @param config
     * @param context
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