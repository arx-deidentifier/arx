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

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * A selection layer for data views.
 *
 * @author Fabian Prasser
 */
public class DataTableSelectionLayer extends SelectionLayer {

    /**  TODO */
    private DataTableContext context;
    
    /**
     * Creates a new instance.
     *
     * @param underlyingLayer
     * @param context
     */
    public DataTableSelectionLayer(IUniqueIndexLayer underlyingLayer, DataTableContext context) {
        super(underlyingLayer);
        this.context = context;
    }

    @Override
    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        return false;
    }

    @Override
    public boolean isColumnPositionFullySelected(int columnPosition) {
        return false;
    }

    @Override
    public boolean isColumnPositionSelected(int columnPosition) {
        return columnPosition-1==context.getSelectedIndex();
    }

    @Override
    public boolean isRowPositionFullySelected(int rowPosition) {
        return false;
    }

    @Override
    public boolean isRowPositionSelected(int rowPosition) {
        return false;
    }
}
