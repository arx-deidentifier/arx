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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * by ordering the data items and merging into groups with predefined sizes
 * 
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyBuilderOrderBased<T> extends HierarchyBuilderGroupingBased<T> {

    @SuppressWarnings("hiding")
    protected class CloseElements<T> extends AbstractGroup {
        
        private static final long serialVersionUID = 7224062023293601561L;
        private String[] values;

        protected CloseElements(String[] values, AggregateFunction<T> function) {
            super(function.aggregate(values));
            this.values = values;
        }

        protected String[] getValues(){
            return values;
        }

        @SuppressWarnings("rawtypes")
        protected CloseElements merge(List<CloseElements<T>> list, AggregateFunction<T> function) {
            List<String> values = new ArrayList<String>();
            for (CloseElements group : list){
                for (String s : ((CloseElements)group).getValues()) {
                    values.add(s);
                }
            }
            return new CloseElements<T>(values.toArray(new String[values.size()]), function);
        }
    }
    
    private static final long serialVersionUID = -2749758635401073668L;
    
    /**
     * Creates a new instance. Either preserves the given order, or 
     * sorts the items according to the order induced by the given data type
     * @param type The data type is also used for ordering data items
     * @param order Should the items be sorted according to the order induced by the data type
     */
    public static <T> HierarchyBuilderOrderBased<T> create(final DataType<T> type, boolean order) {
        return new HierarchyBuilderOrderBased<T>(type, order);
    }

    /**
     * Creates a new instance. Uses the comparator for ordering data items
     * @param type The data type
     * @param comparator Use this comparator for ordering data items
     */
    public static <T> HierarchyBuilderOrderBased<T> create(final DataType<T> type, final Comparator<T> comparator) {
        return new HierarchyBuilderOrderBased<T>(type, comparator);
    }
    
    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> HierarchyBuilderOrderBased<T> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilderOrderBased<T> result = (HierarchyBuilderOrderBased<T>)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }
    
    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderOrderBased<T> create(String file) throws IOException{
        return create(new File(file));
    }

    private final Comparator<String> comparator;
    
    /**
     * Creates a new instance
     * @param type The data type is also used for ordering data items
     * @param order Should the items be sorted according to the order induced by the data type 
     */
    private HierarchyBuilderOrderBased(final DataType<T> type, boolean order) {
        super(Type.ORDER_BASED, type);
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
    private HierarchyBuilderOrderBased(final DataType<T> type, final Comparator<T> comparator) {
        super(Type.ORDER_BASED, type);
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
    
    @SuppressWarnings("unchecked")
    @Override
    protected AbstractGroup[][] prepareGroups() {
        if (comparator != null) {
            Arrays.sort(super.getData(), comparator);
        }

        List<Group<T>> groups = super.getLevel(0).getGroups();
        List<String> items = new ArrayList<String>();
        
        // Prepare
        String[] data = getData();
        List<AbstractGroup[]> result = new ArrayList<AbstractGroup[]>();
        int index = 0;
        int resultIndex = 0;
        int groupCount = 0;
        
        // Break if no groups specified
        if (!super.getLevels().isEmpty() &&
            !super.getLevel(0).getGroups().isEmpty()) {
            
            // Create first column
            AbstractGroup[] first = new AbstractGroup[data.length];
            outer: while (true) {
                for (Group<T> group : groups) {
                    for (int i = 0; i<group.getSize(); i++){
                        items.add(data[index++]);
                        if (index == data.length) break;
                    }
                    CloseElements<T> element = new CloseElements<T>(items.toArray(new String[items.size()]), group.getFunction());
                    for (int i=0; i<items.size(); i++) {
                        first[resultIndex++] = element;
                    }
                    items.clear();
                    if (index == data.length) break outer;
                }
            }
            result.add(first);
            
            // Build higher-level columns
            for (int i=1; i<super.getLevels().size(); i++){
                
                // Break if done
                if (groupCount==1) break;
                
                // Prepare
                groupCount = 0;
                groups = super.getLevel(i).getGroups();
                Map<AbstractGroup, AbstractGroup> map = new HashMap<AbstractGroup, AbstractGroup>();
                List<AbstractGroup> list = new ArrayList<AbstractGroup>();
                AbstractGroup[] column = result.get(i-1);
                for (int j=0; j<column.length; j++){
                    if (!map.containsKey(column[j])) {
                        map.put(column[j], column[j]);
                        list.add(column[j]);
                    }
                }
                
                // Build
                index = 0;
                resultIndex = 0;
                List<CloseElements<T>> gItems = new ArrayList<CloseElements<T>>();
                outer: while (true) {
                    for (Group<T> group : groups) {
                        for (int j = 0; j<group.getSize(); j++){
                            gItems.add((CloseElements<T>)list.get(index++));
                            if (index == list.size()) break;
                        }
                        CloseElements<T> element = gItems.get(0).merge(gItems, group.getFunction());
                        groupCount++;
                        for (int j=0; j<gItems.size(); j++) {
                            map.put(gItems.get(j), element);
                        }
                        
                        gItems.clear();
                        if (index == list.size()) break outer;
                    }
                }
                
                // Store
                AbstractGroup[] ccolumn = new AbstractGroup[data.length];
                for (int j=0; j<column.length; j++){
                    ccolumn[j] = map.get(column[j]);
                }
                result.add(ccolumn);
            }
        } else {
            groupCount = data.length;
        }
        
        // Add one last column if more than one group left
        if (groupCount>1) {
            AbstractGroup[] column = new AbstractGroup[data.length];
            CloseElements<T> element = new CloseElements<T>(new String[]{}, AggregateFunction.forType(getDataType()).createConstantFunction("*"));
            for (int i=0; i<column.length; i++){
                column[i] = element;
            }
            result.add(column);
        }
        
        // Return
        return result.toArray(new AbstractGroup[0][0]);
    }
}
