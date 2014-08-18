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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;

/**
 * Equally distributed the columns, if possible
 */
public class LayerColumnGrabEqual extends CTLayer implements IUniqueIndexLayer {
    
    private Map<Integer, Integer> underlyingWidths = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> widths = new HashMap<Integer, Integer>();
    private boolean registered = false;

    public LayerColumnGrabEqual(IUniqueIndexLayer underlyingDataLayer, CTConfiguration config, CTContext context) {
        super(underlyingDataLayer, config, context);
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return ((IUniqueIndexLayer)underlyingLayer).getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int getColumnPositionByX(int x) {
        int min = 0;
        int max = 0;
        for (int i=0; i<getColumnCount(); i++){
            min = max;
            max += getColumnWidthByPosition(i);
            if (min<= x && max>=x) {
                return i;
            }
        }
        return -1;
        
    }
        
    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        
        Integer width = widths.get(columnPosition);
        if (width == null) {
            return underlyingLayer.getColumnWidthByPosition(columnPosition);
        } else {
            return width;
        }
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof CTLayerCommandReset) {
            this.underlyingWidths.clear();
            this.widths.clear();
        }
        return super.doCommand(command);
    }

    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        // TODO
        return super.getRegionLabelsByXY(x, y);
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
        int start = 0;
        for (int i=0; i<columnPosition; i++){
            start += getColumnWidthByPosition(i);
        }
        return start;
    }

    @Override
    public int getWidth() {
        int width = 0;
        for (int i=0; i<this.getColumnCount(); i++){
            width += getColumnWidthByPosition(i);
        }
        return width;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if ((event instanceof ColumnResizeEvent) ||
            (event instanceof StructuralRefreshEvent)){
            
            // Register listener
            if (!registered && getContext().getTable() != null){
                registered = true;
                getContext().getTable().addControlListener(new ControlAdapter(){
                    public void controlResized(ControlEvent arg0) {
                        computeWidths();
                        fireLayerEvent(new StructuralRefreshEvent(LayerColumnGrabEqual.this));
                    }
                });
            }
            
            // Compute widths
            computeWidths();
        }
        super.handleLayerEvent(event);
    }

    /**
     * Computes the widths
     */
    private void computeWidths() {

        // Compute total width
        int underlyingWidth = 0;
        for (int i=0; i<underlyingLayer.getColumnCount(); i++){
            underlyingWidth += underlyingLayer.getColumnWidthByPosition(i);
        }
        
        // Compare with table width
        int tableWidth = getContext().getTable().getSize().x;
        if (underlyingWidth > tableWidth) {
            for (int i=0; i<underlyingLayer.getColumnCount(); i++){
                int width = underlyingLayer.getColumnWidthByPosition(i);
                widths.put(i, width);
                underlyingWidths.put(i, width);
            }
            
            getContext().setColumnExpanded(false);
            
        } else {
            
            // Compute difference
            int deltaWidth = tableWidth - underlyingWidth;
            
            // Determine unresizable position
            int unresizable = -1;
            int numResizable = 0;
            for (int i=0; i<underlyingLayer.getColumnCount(); i++){
                int width = underlyingLayer.getColumnWidthByPosition(i);
                Integer previous = underlyingWidths.get(i);
                if (previous!=null && previous!=width && unresizable == -1) {
                    unresizable = i;
                } else {
                    numResizable++;
                }
                underlyingWidths.put(i, width);
            }
            
            for (int i=0; i<underlyingLayer.getColumnCount(); i++){
                int width = underlyingLayer.getColumnWidthByPosition(i);
                if (i==unresizable) {
                    widths.put(i, width);
                } else {
                    widths.put(i, width  + deltaWidth / numResizable);
                }
            }
            
            getContext().setColumnExpanded(true);
        }
    }
}
