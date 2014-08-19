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
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;

/**
 * Adds an additional column to fill up the space
 * @author Fabian Prasser
 */
public class LayerColumnFillLayout extends CTLayer implements IUniqueIndexLayer {
    
    private final boolean equalWidth;
    private boolean modified = false;
    private boolean ignore = false;

    public LayerColumnFillLayout(IUniqueIndexLayer underlyingDataLayer, 
                                 CTConfiguration config, 
                                 CTContext context) {
        this(underlyingDataLayer, config, context, false);
    }
    
    public LayerColumnFillLayout(IUniqueIndexLayer underlyingDataLayer, 
                                 CTConfiguration config, 
                                 CTContext context,
                                 boolean equalWidth) {
        super(underlyingDataLayer, config, context);
        this.equalWidth = equalWidth;
        this.addConfiguration(new StyleConfigurationFillLayout(config));
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof FillLayerResetCommand) {
            this.modified = false;
            if (isEqualWidthActive()) {
                this.ignore = true;
                for (int i=0; i<getColumnCount(); i++) {
                    ColumnResizeCommand resize = new ColumnResizeCommand(this, i, DataLayer.DEFAULT_COLUMN_WIDTH);
                    underlyingLayer.doCommand(resize);
                }
                this.ignore = false;
            }
        }
        return super.doCommand(command);
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
        
        if (isEqualWidthActive()) {
            int min = 0;
            int width = getEqualWidth();
            for (int i=0; i<super.getColumnCount(); i++) {
                if (x>=min && x<=min+width) {
                    return i;
                } else {
                    min += width;
                }
            }
            return -1;
        } else {
            return LayerUtil.getColumnPositionByX(this, x);
        }
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        
        if (isEqualWidthActive()) { 
            return getEqualWidth(); 
        } else if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) { 
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
     * NOTE: Since this is a {@link IUniqueIndexLayer} sitting close to the
     * {@link DataLayer}, columnPosition == columnIndex
     */
    @Override
    public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) {
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
        return isEqualWidthActive() ? super.getWidth() + getGapWidth() :
                isAdditionalColumnActive() ? super.getPreferredWidth()+ getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getPreferredWidth();
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
        if (isEqualWidthActive()) {
            return columnPosition * getEqualWidth();
        } else {
            return super.getStartXOfColumnPosition(columnPosition);
        }
    }

    @Override
    public int getWidth() {
        return isEqualWidthActive() ? super.getWidth() + getGapWidth() :
               isAdditionalColumnActive() ? super.getWidth() + getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getWidth();
    }
    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof ColumnResizeEvent) {
            if (!ignore && isEqualWidthActive()) {
                modified = true;
                int index = ((ColumnResizeEvent)event).getColumnPositionRanges().iterator().next().start;
                for (int i=0; i<getColumnCount(); i++) {
                    if (i != index) {
                        ColumnResizeCommand command = new ColumnResizeCommand(this, i, getEqualWidth());
                        underlyingLayer.doCommand(command);
                    }
                }
            }
        }
        if (!getContext().getTable().isDisposed()) {
            super.handleLayerEvent(event);
        }
    }
    private int getAdditionalColumnPosition() {
        return getColumnCount() - 1;
    }

    private int getEqualWidth() {
        NatTable table = getContext().getTable();
        if (table.isDisposed()) return 0;
        int offset = underlyingLayer.getClientAreaProvider().getClientArea().x;
        int width = table.getSize().x - offset;
        if (underlyingLayer.getColumnCount()==0) {
            return width;
        }
        return width / underlyingLayer.getColumnCount();
    }

    private int getGapWidth() {
        NatTable table = getContext().getTable();
        if (table.isDisposed()) return 0;
        return table != null ? table.getSize().x - super.getWidth() : 0;
    }
    
    private boolean isAdditionalColumn(int columnPosition) {
        return columnPosition == getAdditionalColumnPosition();
    }

    private boolean isAdditionalColumnActive() {
        
        if (isEqualWidthActive()) {
            getContext().setColumnExpanded(true);
            return false;
        } else {
            boolean result = getGapWidth()>0;
            getContext().setColumnExpanded(result);
            return result;
        }
    }
    
    private boolean isEqualWidthActive() {
        boolean result = equalWidth && !modified && getGapWidth()>0;
        getContext().setColumnExpanded(result);
        return result;
    }
}
