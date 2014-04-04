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
package org.deidentifier.arx.aggregates;

import java.util.Arrays;
import java.util.Comparator;

import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * by ordering the data items and merging them according to given fanout parameters
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyBuilderOrderBased<T> extends HierarchyBuilderGroupingBased<T> {

    private static final long serialVersionUID = -2749758635401073668L;
    
    private final Comparator<String> comparator;

    /**
     * Creates a new instance
     * @param data The data items to build a hierarchy for
     * @param type The data type is also used for ordering data items
     * @param order Should the items be sorted according to the order induced by the data type 
     */
    public HierarchyBuilderOrderBased(final String[] data, final DataType<T> type, boolean order) {
        super(data, type);
        if (order) {
            this.comparator = new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    try {
                        return type.compare(o1, o2);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            };
        } else {
            this.comparator = null;
        }
    }
    
    /**
     * Creates a new instance
     * @param type The data type
     * @param comparator Use this comparator for ordering data items
     */
    public HierarchyBuilderOrderBased(final String[] data, final DataType<T> type, final Comparator<T> comparator) {
        super(data, type);
        this.comparator = new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                try {
                    return comparator.compare(type.parse(o1), type.parse(o2));
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }
    
    @Override
    protected String[][] create(String[][] result) {
        return result;
    }
    
    @Override
    protected void doPrepare() {
        if (comparator != null) {
            Arrays.sort(super.getData(), comparator);
        }
    }

    @Override
    protected int getBaseLevel() {
        return 1;
    }

    @Override
    protected String internalIsValid() {
        return null;
    }
    
}
