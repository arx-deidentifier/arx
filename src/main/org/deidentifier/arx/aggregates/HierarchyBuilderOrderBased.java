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
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataType;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * by ordering the data items and merging into groups with predefined sizes.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyBuilderOrderBased<T> extends HierarchyBuilderGroupingBased<T> {

    /**
     * A serializable comparator.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static abstract class SerializableComparator<T> implements Comparator<T>, Serializable {
        
        /**  TODO */
        private static final long serialVersionUID = 3851134667082727602L;
    }
    
    /**
     * 
     *
     * @param <T>
     */
    @SuppressWarnings("hiding")
    protected class CloseElements<T> extends AbstractGroup {
        
        /**  TODO */
        private static final long serialVersionUID = 7224062023293601561L;
        
        /**  TODO */
        private String[] values;

        /**
         * 
         *
         * @param values
         * @param function
         */
        protected CloseElements(String[] values, AggregateFunction<T> function) {
            super(function.aggregate(values));
            this.values = values;
        }

        /**
         * 
         *
         * @return
         */
        protected String[] getValues(){
            return values;
        }

        /**
         * 
         *
         * @param list
         * @param function
         * @return
         */
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
    
    /**  TODO */
    private static final long serialVersionUID = -2749758635401073668L;
    
    /**
     * Creates a new instance. Either preserves the given order, or
     * sorts the items according to the order induced by the given data type
     *
     * @param <T>
     * @param type The data type is also used for ordering data items
     * @param order Should the items be sorted according to the order induced by the data type
     * @return
     */
    public static <T> HierarchyBuilderOrderBased<T> create(final DataType<T> type, boolean order) {
        return new HierarchyBuilderOrderBased<T>(type, order);
    }

    /**
     * Creates a new instance. Uses the comparator for ordering data items
     *
     * @param <T>
     * @param type The data type
     * @param comparator Use this comparator for ordering data items
     * @return
     */
    public static <T> HierarchyBuilderOrderBased<T> create(final DataType<T> type, final Comparator<T> comparator) {
        return new HierarchyBuilderOrderBased<T>(type, comparator);
    }

    /**
     * Creates a new instance. Uses the defined order for data items
     *
     * @param <T>
     * @param type The data type
     * @param order Use this for ordering data items
     * @return
     */
    public static <T> HierarchyBuilderOrderBased<T> create(final DataType<T> type, final String[] order) {
        return new HierarchyBuilderOrderBased<T>(type, order);
    }
    
    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
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
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderOrderBased<T> create(String file) throws IOException{
        return create(new File(file));
    }

    /**  TODO */
    private final Comparator<String> comparator;

    /**
     * Creates a new instance.
     *
     * @param type The data type is also used for ordering data items
     * @param order Should the items be sorted according to the order induced by the data type
     */
    private HierarchyBuilderOrderBased(final DataType<T> type, boolean order) {
        super(Type.ORDER_BASED, type);
        if (order) {
            this.comparator = new SerializableComparator<String>(){
                private static final long serialVersionUID = -5728888259809544706L;
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
        this.function = AggregateFunction.forType(type).createSetFunction();
    }

    /**
     * Creates a new instance.
     *
     * @param type The data type
     * @param order Use this for ordering data items
     */
    private HierarchyBuilderOrderBased(final DataType<T> type, final String[] order) {
        super(Type.ORDER_BASED, type);
        
        final Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i=0; i<order.length; i++) {
            map.put(order[i], i);
        }
        this.comparator = new SerializableComparator<String>(){
            private static final long serialVersionUID = 8016783606581696832L;
            @Override
            public int compare(String o1, String o2) {
                try {
                    return map.get(o1).compareTo(map.get(o2));
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
        this.function = AggregateFunction.forType(type).createSetFunction();
    }

    /**
     * Creates a new instance.
     *
     * @param type The data type
     * @param comparator Use this comparator for ordering data items
     */
    private HierarchyBuilderOrderBased(final DataType<T> type, final Comparator<T> comparator) {
        super(Type.ORDER_BASED, type);
        if (!(comparator instanceof Serializable)) {
            throw new IllegalArgumentException("Comparator must be serializable");
        }
        this.comparator = new SerializableComparator<String>(){
            private static final long serialVersionUID = -487411642974218418L;
            @Override
            public int compare(String o1, String o2) {
                try {
                    return comparator.compare(type.parse(o1), type.parse(o2));
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
        this.function = AggregateFunction.forType(type).createSetFunction();
    }
    
    /**
     * Returns the comparator.
     *
     * @return
     */
    public Comparator<String> getComparator(){
        return comparator;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased#prepareGroups()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected AbstractGroup[][] prepareGroups() {
        if (comparator != null) {
            try {
                Arrays.sort(super.getData(), comparator);
            } catch (Exception e){
                throw new IllegalArgumentException(e.getMessage());
            }
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
                    groupCount++;
                    items.clear();
                    if (index == data.length) break outer;
                }
            }
            result.add(first);
            
            // Break if done
            if (groupCount>1) {
                
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
