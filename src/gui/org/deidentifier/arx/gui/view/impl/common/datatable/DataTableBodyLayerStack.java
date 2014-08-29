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
 * A BodyLayerStack for the DataView
 * @author Fabian Prasser
 */
public class DataTableBodyLayerStack extends AbstractLayerTransform {

	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;
	private ILayer rowHeaderLayer;

    /**
     * Creates a new instance
     * @param underlyingLayer
     * @param table
     * @param context
     */
    public DataTableBodyLayerStack(IUniqueIndexLayer underlyingLayer, NatTable table, DataTableContext context, Control parent) {
        this.selectionLayer = new DataTableSelectionLayer(underlyingLayer, context);
        this.viewportLayer = new DataTableViewportLayer(new DataTableFillLayout(parent, selectionLayer, context, this), context);
        this.setUnderlyingLayer(viewportLayer);
        this.setConfigLabelAccumulator(new DataTableConfigLabelAccumulator(table, context));
        this.registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
    }

    /**
     * Returns the selection layer
     * @return
     */
    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    /**
     * Returns the viewport layer
     * @return
     */
    public ViewportLayer getViewportLayer() {
        return viewportLayer;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }

    /**
     * Sets the row header layer
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
