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
 * Adds an additional column to fill up the space.
 *
 * @author Fabian Prasser
 */
public class LayerColumnFillLayout extends CTLayer implements IUniqueIndexLayer {
    
    /**  TODO */
    private final boolean equalWidth;
    
    /**  TODO */
    private boolean modified = false;
    
    /**  TODO */
    private boolean ignore = false;

    /**
     * 
     *
     * @param underlyingDataLayer
     * @param config
     * @param context
     */
    public LayerColumnFillLayout(IUniqueIndexLayer underlyingDataLayer, 
                                 CTConfiguration config, 
                                 CTContext context) {
        this(underlyingDataLayer, config, context, false);
    }
    
    /**
     * 
     *
     * @param underlyingDataLayer
     * @param config
     * @param context
     * @param equalWidth
     */
    public LayerColumnFillLayout(IUniqueIndexLayer underlyingDataLayer, 
                                 CTConfiguration config, 
                                 CTContext context,
                                 boolean equalWidth) {
        super(underlyingDataLayer, config, context);
        this.equalWidth = equalWidth;
        this.addConfiguration(new StyleConfigurationFillLayout(config));
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#doCommand(org.eclipse.nebula.widgets.nattable.command.ILayerCommand)
     */
    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof FillLayerResetCommand) {
            this.modified = false;
            this.ignore = true;
            for (int i = 0; i < getColumnCount(); i++) {
                ColumnResizeCommand resize = new ColumnResizeCommand(this, i, DataLayer.DEFAULT_COLUMN_WIDTH);
                underlyingLayer.doCommand(resize);
            }
            this.ignore = false;
        }
        return super.doCommand(command);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getCellByPosition(int, int)
     */
    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) { return new LayerCell(this, columnPosition, rowPosition); }
        return super.getCellByPosition(columnPosition, rowPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return isAdditionalColumnActive() ? super.getColumnCount() + 1 : super.getColumnCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getColumnIndexByPosition(int)
     */
    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (isAdditionalColumnActive() && isAdditionalColumn(columnPosition)) { return columnPosition; }
        return super.getColumnIndexByPosition(columnPosition);
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
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getColumnPositionByX(int)
     */
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

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getColumnWidthByPosition(int)
     */
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

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getConfigLabelsByPosition(int, int)
     */
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
            return "";
        } else {
            return super.getDataValueByPosition(columnPosition, rowPosition);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getPreferredColumnCount()
     */
    @Override
    public int getPreferredColumnCount() {
        return getColumnCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getPreferredWidth()
     */
    @Override
    public int getPreferredWidth() {
        return isEqualWidthActive() ? super.getWidth() + getGapWidth() :
                isAdditionalColumnActive() ? super.getPreferredWidth()+ getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getPreferredWidth();
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
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getStartXOfColumnPosition(int)
     */
    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        if (isEqualWidthActive()) {
            return columnPosition * getEqualWidth();
        } else {
            return super.getStartXOfColumnPosition(columnPosition);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform#getWidth()
     */
    @Override
    public int getWidth() {
        return isEqualWidthActive() ? super.getWidth() + getGapWidth() :
               isAdditionalColumnActive() ? super.getWidth() + getColumnWidthByPosition(getAdditionalColumnPosition()) : super.getWidth();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.layer.AbstractLayer#handleLayerEvent(org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent)
     */
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

    /**
     * 
     *
     * @return
     */
    private int getGapWidth() {
        NatTable table = getContext().getTable();
        if (table.isDisposed()) return 0;
        return table != null ? table.getSize().x - super.getWidth() : 0;
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
        
        if (isEqualWidthActive()) {
            getContext().setColumnExpanded(true);
            return false;
        } else {
            boolean result = getGapWidth()>0;
            getContext().setColumnExpanded(result);
            return result;
        }
    }
    
    /**
     * 
     *
     * @return
     */
    private boolean isEqualWidthActive() {
        boolean result = equalWidth && !modified && getGapWidth()>0;
        getContext().setColumnExpanded(result);
        return result;
    }
}
