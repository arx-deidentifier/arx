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
 * display the contingency table.
 *
 * @author Fabian Prasser
 */
public class DensityData extends JHCData{
    
    /**
     * A point implementation for density data.
     *
     * @author Fabian Prasser
     */
    private class DensityPoint implements Point {
        
        /** x. */
        private int x;
        
        /** y. */
        private int y;
        
        /** value. */
        private double value;

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap.Point#getValue()
         */
        @Override
        public double getValue() {
            return value;
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap.Point#getX()
         */
        @Override
        public int getX() {
            return x;
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap.Point#getY()
         */
        @Override
        public int getY() {
            return y;
        }
        
        /**
         * Parses the given contingency table entry.
         *
         * @param entry
         */
        private void parse(Entry entry){
            x = entry.value1;
            y = entry.value2;
            value = entry.frequency;
        }
    };
    
    /**
     * A heatmap implementation for density data.
     *
     * @author Fabian Prasser
     */
    private class DensityHeatmap extends JHCHeatmap{

        /** The one and only point. */
        private final DensityPoint point = new DensityPoint();
        
        /** The table. */
        private final StatisticsContingencyTable table;
        
        /**
         * Creates a new instance.
         *
         * @param table
         */
        private DensityHeatmap(StatisticsContingencyTable table){
            this.table = table;
        }
        
        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#getHeight()
         */
        @Override
        public int getHeight() {
            return table.values2.length;
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#getMax()
         */
        @Override
        public double getMax() {
            return table.maxFrequency;
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#getMin()
         */
        @Override
        public double getMin() {
            return 0d;
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#getWidth()
         */
        @Override
        public int getWidth() {
            return table.values1.length;
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#getXLabel(int)
         */
        @Override
        public String getXLabel(int index) {
            return table.values1[index];
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#getYLabel(int)
         */
        @Override
        public String getYLabel(int index) {
            return table.values2[index];
        }

        /* (non-Javadoc)
         * @see de.linearbits.jhc.JHCHeatmap#iterator()
         */
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

    /** Width. */
    private int width = -1;
    
    /** Height. */
    private int height = -1;
    
    /** First column. */
    private final int column1;
    
    /** Second column. */
    private final int column2;
    
    /** Statistics. */
    private final StatisticsBuilder statistics;
    
    /**
     * Creates a new instance.
     *
     * @param handle
     * @param column1
     * @param column2
     */
    public DensityData(DataHandle handle, int column1, int column2) {
        this.statistics = handle.getStatistics();
        this.column1 = column1;
        this.column2 = column2;
    }

    /* (non-Javadoc)
     * @see de.linearbits.jhc.JHCData#getHeatmap(int, int)
     */
    @Override
    public JHCHeatmap getHeatmap(int width, int height) {
        return new DensityHeatmap(statistics.getContingencyTable(column1, width, column2, height));
    }
       
    /* (non-Javadoc)
     * @see de.linearbits.jhc.JHCData#getHeight()
     */
    @Override
    public int getHeight() {
        if (height == -1) {
            // Perform this process when called by the background thread
            this.height = statistics.getDistinctValues(column2).length;    
        }
        return height;
    }

    /* (non-Javadoc)
     * @see de.linearbits.jhc.JHCData#getWidth()
     */
    @Override
    public int getWidth() {
        if (width == -1) {
            // Perform this process when called by the background thread
            this.width = statistics.getDistinctValues(column1).length;
        }
        return width;
    }
}
