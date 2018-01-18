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

import org.deidentifier.arx.gui.view.impl.common.table.StyleConfigurationFillLayout;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.swt.widgets.Control;

/**
 * Adds an additional column to fill up the space.
 *
 * @author Fabian Prasser
 */
public class DataTableFillLayout extends AbstractLayerTransform implements IUniqueIndexLayer {
    
    /**  TODO */
    private final DataTableContext context;
    
    /**  TODO */
    private final DataTableBodyLayerStack bodyLayerStack;

    /**
     * 
     *
     * @param parent
     * @param underlyingDataLayer
     * @param context
     * @param bodyLayerStack
     */
    public DataTableFillLayout(Control parent, IUniqueIndexLayer underlyingDataLayer, DataTableContext context, DataTableBodyLayerStack bodyLayerStack) {
        super(underlyingDataLayer);
        this.context = context;
        this.addConfiguration(new DataTableFillLayoutStyle(parent));
        this.bodyLayerStack = bodyLayerStack;
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) { return new LayerCell(this, columnPosition, rowPosition); }
        return super.getCellByPosition(columnPosition, rowPosition);
    }

    @Override
    public int getColumnCount() {
        return isAdditionalColumnActive() ? super.getColumnCount() + 1 : super.getColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) { return columnPosition; }
        return super.getColumnIndexByPosition(columnPosition);
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < getColumnCount()) {
            return columnIndex;
        } else {
            return -1;
        }
    }
    
    @Override
    public int getColumnPositionByX(int x) {
        return LayerUtil.getColumnPositionByX(this, x);
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) { 
            return getGapWidth(); 
        } else {
            return super.getColumnWidthByPosition(columnPosition);
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) {
            return new LabelStack(StyleConfigurationFillLayout.DEFAULT_FILL_LAYOUT_CELL_CONFIG_LABEL);
        } else {
            return super.getConfigLabelsByPosition(columnPosition, rowPosition);
        }
    }

    /**
     * NOTE: Since this is a {@link IUniqueIndexLayer} sitting close to the {@link DataLayer}, columnPosition == columnIndex.
     *
     * @param columnPosition
     * @param rowPosition
     * @return
     */
    @Override
    public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) {
            return ""; //$NON-NLS-1$
        } else {
            return super.getDataValueByPosition(columnPosition, rowPosition);
        }
    }
    
    @Override
    public int getPreferredColumnCount() {
        return getColumnCount();
    }

    @Override
    public int getPreferredWidth() {
        return isAdditionalColumnActive() ? super.getPreferredWidth()+ getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getPreferredWidth();
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < getRowCount()) {
            return rowIndex;
        } else {
            return -1;
        }
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return super.getStartXOfColumnPosition(columnPosition);
    }

    @Override
    public int getWidth() {
        return isAdditionalColumnActive() ? super.getWidth() + getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getWidth();
    }
    
    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (context.getTable() != null && !context.getTable().isDisposed()) {
            super.handleLayerEvent(event);
        }
    }
    
    /**
     * 
     *
     * @return
     */
    private int getAdditionalColumnPosition() {
        return getColumnCount() - 1;
    }

    /**
     * 
     *
     * @return
     */
    private int getGapWidth() {
        NatTable table = context.getTable();
        if (table == null || table.isDisposed()) return 0;
        int offset = this.bodyLayerStack != null ? this.bodyLayerStack.getRowHeaderLayer() != null ? this.bodyLayerStack.getRowHeaderLayer().getWidth() : 0 : 0; 
        return table != null ? table.getSize().x - super.getWidth() - offset: 0;
    }
    
    /**
     * 
     *
     * @param columnPosition
     * @return
     */
    private boolean isAdditionalColumn(int columnPosition) {
        return columnPosition == getAdditionalColumnPosition();
    }

    /**
     * 
     *
     * @return
     */
    private boolean isAdditionalColumnActive() {
    	return getGapWidth()>0;
    }
}
