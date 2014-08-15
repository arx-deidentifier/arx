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
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;

/**
 * Adds an additional column to fill up the space
 */
public class LayerColumnGrabLast extends CTLayer implements IUniqueIndexLayer {

    public LayerColumnGrabLast(IUniqueIndexLayer underlyingDataLayer, CTConfiguration config, CTContext context) {
        super(underlyingDataLayer, config, context);
        addConfiguration(new StyleConfigurationNative(config));
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        if (isActive() && isAdditionalColumn(columnPosition)) { return new LayerCell(this, columnPosition, rowPosition); }
        return super.getCellByPosition(columnPosition, rowPosition);
    }

    @Override
    public int getColumnCount() {
        return isActive() ? super.getColumnCount() + 1 : super.getColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (isActive() && isAdditionalColumn(columnPosition)) { return columnPosition; }
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
        if (isActive() && isAdditionalColumn(columnPosition)) { 
            return getGapWidth(); 
        }
        return super.getColumnWidthByPosition(columnPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (isActive() && isAdditionalColumn(columnPosition)) { 
           return new LabelStack(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX +
                                                                       columnPosition, SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL); 
           }
        return super.getConfigLabelsByPosition(columnPosition, rowPosition);
    }

    /**
     * NOTE: Since this is a {@link IUniqueIndexLayer} sitting close to the
     * {@link DataLayer}, columnPosition == columnIndex
     */
    @Override
    public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
        if (isActive() && isAdditionalColumn(columnPosition)) {
            return "";
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
        return isActive() ? super.getPreferredWidth()+ getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getPreferredWidth();
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
    public int getWidth() {
        return isActive() ? super.getWidth()+ getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getWidth();
    }

    private int getAdditionalColumnPosition() {
        return getColumnCount() - 1;
    }

    private int getGapWidth() {
        NatTable table = getContext().getTable();
        return table != null ? table.getSize().x - super.getWidth() : 0;
    }

    private boolean isActive() {
        return getGapWidth()>0;
    }
    
    private boolean isAdditionalColumn(int columnPosition) {
        return columnPosition == getAdditionalColumnPosition();
    }
}
