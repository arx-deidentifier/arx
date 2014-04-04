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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    
    @SuppressWarnings("hiding")
    protected class CloseElements<T> extends Group {
        
        private static final long serialVersionUID = 7224062023293601561L;
        private String[] values;

        protected CloseElements(String[] values, AggregateFunction<T> function) {
            super(function.aggregate(values));
            this.values = values;
        }

        @Override
        protected boolean isOutOfBounds() {
            return false;
        }

        protected String[] getValues(){
            return values;
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected String getGroupLabel(Set<Group> groups, Fanout fanout) {
            List<String> values = new ArrayList<String>();
            for (Group group : groups){
                for (String s : ((CloseElements)group).getValues()) {
                    values.add(s);
                }
            }
            return fanout.getFunction().aggregate(values.toArray(new String[values.size()]));
        }
    }

    /**
     * Creates a new instance
     * @param type The data type is also used for ordering data items
     * @param order Should the items be sorted according to the order induced by the data type 
     */
    public HierarchyBuilderOrderBased(final DataType<T> type, boolean order) {
        super(type);
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
    public HierarchyBuilderOrderBased(final DataType<T> type, final Comparator<T> comparator) {
        super(type);
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
    protected List<Group> prepareGroups() {
        if (comparator != null) {
            Arrays.sort(super.getData(), comparator);
        }
        
        List<Fanout<T>> fanouts = super.getLevel(0).getFanouts();
        List<Group> groups = new ArrayList<Group>();
        int index = 0;
        int listIndex = 0;
        int groupSize = 0;
        Fanout<T> fanout = fanouts.get(0);
        List<String> items = new ArrayList<String>();
        String[] data = getData();
        while (index < data.length) {
            
            groupSize++;
            items.add(data[index]);
            
            if (groupSize==fanout.getFanout()) {
                CloseElements<T> element = new CloseElements<T>(items.toArray(new String[items.size()]), fanout.getFunction());
                for (int i=0; i<groupSize; i++) {
                    groups.add(element);
                }
                groupSize = 0;
                listIndex++;
                if (listIndex < fanouts.size()) {
                    fanout = fanouts.get(listIndex);
                } else {
                    fanout = fanouts.get(0);
                    listIndex = 0;
                }
                items.clear();
                index++;
                continue;
            } else if (index==data.length-1) {
                CloseElements<T> element = new CloseElements<T>(items.toArray(new String[items.size()]), fanout.getFunction());
                for (int i=0; i<groupSize; i++) {
                    groups.add(element);
                }
                items.clear();
                break;
            } else {
                index++;
                continue;
            }
        }
        
        return groups;
    }

    @Override
    protected String internalIsValid() {
        return null;
    }

    @Override
    protected int getBaseLevel() {
        return 1;
    }
}
