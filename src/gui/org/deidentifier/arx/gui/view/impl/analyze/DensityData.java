/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.analyze;

import java.util.Iterator;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsBuilder;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;

import de.linearbits.jhc.JHCData;
import de.linearbits.jhc.JHCHeatmap;
import de.linearbits.jhc.JHCHeatmap.Point;

/**
 * This class implements a data object that can be passed to the heatmap widget to
 * display the contingency table
 * 
 * @author Fabian Prasser
 */
public class DensityData extends JHCData{
    
    /**
     * A point implementation for density data
     * @author Fabian Prasser
     *
     */
    private class DensityPoint implements Point {
        
        /** x*/
        private int x;
        /** y*/
        private int y;
        /** value*/
        private double value;

        @Override
        public double getValue() {
            return value;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
        
        /**
         * Parses the given contingency table entry
         * @param entry
         */
        private void parse(Entry entry){
            x = entry.value1;
            y = entry.value2;
            value = entry.frequency;
        }
    };
    
    /**
     * A heatmap implementation for density data
     * @author Fabian Prasser
     */
    private class DensityHeatmap extends JHCHeatmap{

        /** The one and only point*/
        private final DensityPoint point = new DensityPoint();
        /** The table*/
        private final StatisticsContingencyTable table;
        
        /**
         * Creates a new instance
         * @param table
         */
        private DensityHeatmap(StatisticsContingencyTable table){
            this.table = table;
        }
        
        @Override
        public int getHeight() {
            return table.values2.length;
        }

        @Override
        public double getMax() {
            return table.maxFrequency;
        }

        @Override
        public double getMin() {
            return 0d;
        }

        @Override
        public int getWidth() {
            return table.values1.length;
        }

        @Override
        public String getXLabel(int index) {
            return table.values1[index];
        }

        @Override
        public String getYLabel(int index) {
            return table.values2[index];
        }

        @Override
        public Iterator<Point> iterator() {
            return new Iterator<Point>(){

                @Override
                public boolean hasNext() {
                    return table.iterator.hasNext();
                }

                @Override
                public Point next() {
                    point.parse(table.iterator.next());
                    return point;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
            };
        }
    }

    /** Width*/
    private int width = -1;
    /** Height*/
    private int height = -1;
    /** First column*/
    private final int column1;
    /** Second column*/
    private final int column2;
    /** Statistics*/
    private final StatisticsBuilder statistics;
    
    /**
     * Creates a new instance
     * @param handle
     * @param column1
     * @param column2
     */
    public DensityData(DataHandle handle, int column1, int column2) {
        this.statistics = handle.getStatistics();
        this.column1 = column1;
        this.column2 = column2;
    }

    @Override
    public JHCHeatmap getHeatmap(int width, int height) {
        return new DensityHeatmap(statistics.getContingencyTable(column1, width, column2, height));
    }
       
    @Override
    public int getHeight() {
        if (height == -1) {
            // Perform this process when called by the background thread
            this.height = statistics.getDistinctValues(column2).length;    
        }
        return height;
    }

    @Override
    public int getWidth() {
        if (width == -1) {
            // Perform this process when called by the background thread
            this.width = statistics.getDistinctValues(column1).length;
        }
        return width;
    }
}
