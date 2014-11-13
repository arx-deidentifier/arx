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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;

/**
 * Adds additional rows at the end that fill up the available height.
 *
 * @author Fabian Prasser
 */
public class LayerRowFillLayout extends CTLayer implements IUniqueIndexLayer {

    /**
     * 
     *
     * @param underlyingDataLayer
     * @param config
     * @param context
     */
    public LayerRowFillLayout(IUniqueIndexLayer underlyingDataLayer, CTConfiguration config, CTContext context) {
        super(underlyingDataLayer, config, context);
        addConfiguration(new StyleConfigurationFillLayout(config));
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getCellByPosition(int, int)
     */
    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        if (isActive() && isAdditionalRowPosition(rowPosition)) { return new LayerCell(this, columnPosition, rowPosition); }
        return super.getCellByPosition(columnPosition, rowPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer#getColumnPositionByIndex(int)
     */
    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < getColumnCount()) {
            return columnIndex;
        } else {
            return -1;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getConfigLabelsByPosition(int, int)
     */
    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (isActive() && isAdditionalRowPosition(rowPosition)) {
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
        if (isActive() && isAdditionalRowPosition(rowPosition)) {
            return "";
        } else {
            return super.getDataValueByPosition(columnPosition, rowPosition);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getHeight()
     */
    @Override
    public int getHeight() {
        return isActive() ? super.getHeight() + getGapHeight() : super.getHeight();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getPreferredHeight()
     */
    @Override
    public int getPreferredHeight() {
        return isActive() ? super.getPreferredHeight() + getGapHeight() : super.getPreferredHeight();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getPreferredRowCount()
     */
    @Override
    public int getPreferredRowCount() {
        return getRowCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getRowCount()
     */
    @Override
    public int getRowCount() {
        return isActive() ? super.getRowCount() + getAdditionalRowCount() : super.getRowCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getRowHeightByPosition(int)
     */
    @Override
    public int getRowHeightByPosition(int rowPosition) {
        if (isActive() && isAdditionalRowPosition(rowPosition)) { return DataLayer.DEFAULT_ROW_HEIGHT; }
        return super.getRowHeightByPosition(rowPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getRowIndexByPosition(int)
     */
    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (isActive() && isAdditionalRowPosition(rowPosition)) { return rowPosition; }
        return super.getRowIndexByPosition(rowPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer#getRowPositionByIndex(int)
     */
    @Override
    public int getRowPositionByIndex(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < getRowCount()) {
            return rowIndex;
        } else {
            return -1;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getRowPositionByY(int)
     */
    @Override
    public int getRowPositionByY(int y) {
        return LayerUtil.getRowPositionByY(this, y);
    }
    
    /**
     * 
     *
     * @return
     */
    private int getAdditionalRowCount() {
        return getGapHeight()/DataLayer.DEFAULT_ROW_HEIGHT;
    }
    
    /**
     * 
     *
     * @return
     */
    private int getGapHeight() {
        NatTable table = getContext().getTable();
        if (table.isDisposed()) return 0;
        return table != null ? table.getSize().y - DataLayer.DEFAULT_ROW_HEIGHT - super.getHeight() : 0;
    }

    /**
     * 
     *
     * @return
     */
    private boolean isActive() {
        boolean result = getGapHeight()>0;
        getContext().setRowExpanded(result);
        return result;
    }

    /**
     * 
     *
     * @param rowPosition
     * @return
     */
    private boolean isAdditionalRowPosition(int rowPosition) {
        return super.getRowIndexByPosition(rowPosition)==-1;
    }
}
