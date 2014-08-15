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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * Equally distributed the columns, if possible
 */
public class LayerColumnGrabEqual extends CTLayer implements IUniqueIndexLayer {
    
    private Map<Integer, Integer> previousWidths = new HashMap<Integer, Integer>();

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return ((IUniqueIndexLayer)underlyingLayer).getColumnPositionByIndex(columnIndex);
    }

    public LayerColumnGrabEqual(IUniqueIndexLayer underlyingDataLayer, CTConfiguration config, CTContext context) {
        super(underlyingDataLayer, config, context);
        addConfiguration(new StyleConfigurationNative(config));
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        
        // Compute total width
        int underlyingWidth = 0;
        for (int i=0; i<underlyingLayer.getColumnCount(); i++){
            underlyingWidth += underlyingLayer.getColumnWidthByPosition(i);
        }
        
        // Compare with table width
        int tableWidth = getContext().getTable().getSize().x;
        if (underlyingWidth > tableWidth) {
            return underlyingLayer.getColumnWidthByPosition(columnPosition);
        }
        
        // Compute difference
        int deltaWidth = tableWidth - underlyingWidth;
        
        // Determine resizable positions
        Set<Integer> resizablePositions = new HashSet<Integer>();
        for (int i=0; i<underlyingLayer.getColumnCount(); i++){
            int width = underlyingLayer.getColumnWidthByPosition(i);
            Integer previous = previousWidths.get(i);
            if (previous==null || previous==width) {
                resizablePositions.add(i);
            }
            previousWidths.put(i, width);
        }
        
        // Return width
        if (!resizablePositions.contains(columnPosition)) {
            return underlyingLayer.getColumnWidthByPosition(columnPosition);
        } else {
            return underlyingLayer.getColumnWidthByPosition(columnPosition) + deltaWidth / resizablePositions.size();
        }
    }

    @Override
    public int getPreferredWidth() {
        return getWidth();
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
        int width = 0;
        for (int i=0; i<this.getColumnCount(); i++){
            width += getColumnWidthByPosition(i);
        }
        return width;
    }
}
