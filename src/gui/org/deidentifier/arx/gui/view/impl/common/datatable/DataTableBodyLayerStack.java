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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Control;

/**
 * A BodyLayerStack for the DataView.
 *
 * @author Fabian Prasser
 */
public class DataTableBodyLayerStack extends AbstractLayerTransform {

	/**  TODO */
	private final SelectionLayer selectionLayer;
	
	/**  TODO */
	private final ViewportLayer viewportLayer;
	
	/**  TODO */
	private ILayer rowHeaderLayer;

    /**
     * Creates a new instance.
     *
     * @param underlyingLayer
     * @param table
     * @param context
     * @param parent
     */
    public DataTableBodyLayerStack(IUniqueIndexLayer underlyingLayer, NatTable table, DataTableContext context, Control parent) {
        this.selectionLayer = new DataTableSelectionLayer(underlyingLayer, context);
        this.viewportLayer = new DataTableViewportLayer(new DataTableFillLayout(parent, selectionLayer, context, this), context);
        this.setUnderlyingLayer(viewportLayer);
        this.setConfigLabelAccumulator(new DataTableConfigLabelAccumulator(table, context));
        this.registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
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
     * Returns the viewport layer.
     *
     * @return
     */
    public ViewportLayer getViewportLayer() {
        return viewportLayer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#setClientAreaProvider(org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider)
     */
    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }

    /**
     * Sets the row header layer.
     *
     * @param rowHeaderLayer
     */
	public void setRowHeaderLayer(ILayer rowHeaderLayer) {
		this.rowHeaderLayer = rowHeaderLayer;
	}

	/**
	 * @return the rowHeaderLayer
	 */
	public ILayer getRowHeaderLayer() {
		return rowHeaderLayer;
	}
}
