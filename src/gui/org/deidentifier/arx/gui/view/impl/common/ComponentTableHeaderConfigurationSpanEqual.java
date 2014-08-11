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

package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

/**
 * Configuration for table view
 * @author Fabian Prasser
 *
 */
public class ComponentTableHeaderConfigurationSpanEqual extends ComponentTableHeaderConfiguration{
    
    /** Data*/
    private NatTable table;
    /** Data*/
    private DataLayer dataLayer;
    /** Data*/
    private CornerLayer cornerLayer;
    /** Data*/
    private int defaultWidth = 0;
    
    /**
     * Creates a new instance
     * @param width
     */
    public ComponentTableHeaderConfigurationSpanEqual(int width) {
        super(width);
    }

    @Override
    protected void init(NatTable table, DataLayer dataLayer, CornerLayer cornerLayer, IDataProvider dataProvider, int width) {
     
        // Store
        this.cornerLayer = cornerLayer;
        this.dataLayer = dataLayer;
        this.table = table;
        this.defaultWidth = this.width;

        // Check
        int columns = dataLayer.getColumnCount();
        if (columns==0) return;
        
        // Prepare
        table.setRedraw(false);
        
        // Check
        int total = this.width * columns + (cornerLayer != null ? cornerLayer.getColumnWidthByPosition(0) : 0);
        if (total >= width) {
            // Its too large, anyways
            for (int i=0; i<columns; i++){
                dataLayer.setColumnWidthByPosition(i, this.width, i==columns-1);
            }
        } else {
            // Extend to cover the whole area
            int columnWidth = width - (cornerLayer != null ? cornerLayer.getColumnWidthByPosition(0) : 0);
            columnWidth = columnWidth / dataProvider.getColumnCount();
            total = (cornerLayer != null) ? cornerLayer.getColumnWidthByPosition(0) : 0;

            // Set
            for (int i = 0; i < columns; i++) {
                dataLayer.setColumnWidthByPosition(i, columnWidth, i == columns - 1);
                total += columnWidth;
            }   
            this.defaultWidth = columnWidth;
        }
        
        // Done
        table.setRedraw(true);
        table.redraw();
    }

    @Override
    protected void update(int width) {
        
        if (table==null || table.isDisposed()) return;

        // Check
        int columns = dataLayer.getColumnCount();
        if (columns==0) return;
        
        // Prepare
        table.setRedraw(false);
        
        // Check
        int total = (cornerLayer != null ? cornerLayer.getColumnWidthByPosition(0) : 0);
        for (int i=0; i<columns; i++){
            total += dataLayer.getColumnWidthByPosition(i);
        }

        // Count again
        int difference = width - total;
        int numColumnsToAdjust = 0;
        for (int i=0; i<columns; i++){
            if (dataLayer.getColumnWidthByPosition(i) == defaultWidth) {
                numColumnsToAdjust++;
            }
        }
        
        if (numColumnsToAdjust>0 && total<width) {

            // Difference
            int count = 0;
            int delta = difference / numColumnsToAdjust;
            for (int i = 0; i < columns; i++) {
                count++;
                int currentWidth = dataLayer.getColumnWidthByPosition(i);
                if (currentWidth == defaultWidth) {
                    dataLayer.setColumnWidthByPosition(i,
                                                       defaultWidth + delta,
                                                       count == numColumnsToAdjust);
                }
            }

            // Store for later
            this.defaultWidth = defaultWidth + delta;
            
        } else if (numColumnsToAdjust>0 && total>width) {
            
            int newWidth = defaultWidth - (total-width) / numColumnsToAdjust;
            if (newWidth>=this.width) {
                int count = 0;
                for (int i = 0; i < columns; i++) {
                    count++;
                    int currentWidth = dataLayer.getColumnWidthByPosition(i);
                    if (currentWidth == defaultWidth) {
                        dataLayer.setColumnWidthByPosition(i,
                                                           newWidth,
                                                           count == numColumnsToAdjust);
                    }
                }
                this.defaultWidth = newWidth;
            }
        }
        
        // Done
        table.setRedraw(true);
        table.redraw();
        
    }
}
