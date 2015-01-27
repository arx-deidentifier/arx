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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Group;
import org.deidentifier.arx.aggregates.HierarchyBuilderGroupingBased.Level;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Interval;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;

/**
 * A base-class for grouping-based builders, i.e., order-based and interval-based builders
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class HierarchyWizardModelGrouping<T> extends HierarchyWizardModelAbstract<T>{
    
    /**
     * This class represents a group.
     *
     * @author Fabian Prasser
     * @param <U>
     */
    public static class HierarchyWizardGroupingGroup<U> {
        
        /** Var. */
        public int size;
        
        /** Var. */
        public AggregateFunction<U> function;
        
        /**
         * 
         *
         * @param group
         */
        public HierarchyWizardGroupingGroup(Group<U> group) {
            this.size = group.getSize();
            this.function = group.getFunction();
        }

        /**
         * 
         *
         * @param size
         * @param function
         */
        public HierarchyWizardGroupingGroup(int size, AggregateFunction<U> function) {
            this.size = size;
            this.function = function;
        }
    }

    /**
     * This class represents an interval.
     *
     * @author Fabian Prasser
     * @param <U>
     */
    public static class HierarchyWizardGroupingInterval<U> {
        
        /**  TODO */
        public U min;
        
        /**  TODO */
        public U max;
        
        /**  TODO */
        public AggregateFunction<U> function;
        
        /**
         * 
         *
         * @param interval
         */
        public HierarchyWizardGroupingInterval(Interval<U> interval) {
            this.min = interval.getMin();
            this.max = interval.getMax();
            this.function = interval.getFunction();
        }

        /**
         * 
         *
         * @param min
         * @param max
         * @param function
         */
        public HierarchyWizardGroupingInterval(U min, U max, AggregateFunction<U> function) {
            this.min = min;
            this.max = max;
            this.function = function;
        }
    }
    
    /**
     * This class represents an adjustment.
     *
     * @author Fabian Prasser
     * @param <U>
     */
    public static class HierarchyWizardGroupingRange<U> {
        
        /**  TODO */
        public U repeat;
        
        /**  TODO */
        public U snap;
        
        /**  TODO */
        public U label;
        
        /**
         * 
         *
         * @param type
         * @param lower
         */
        @SuppressWarnings("unchecked")
        public HierarchyWizardGroupingRange(DataType<U> type, boolean lower){
            DataTypeWithRatioScale<U> dtype = (DataTypeWithRatioScale<U>)type;
            if (lower){
                this.repeat = dtype.getMinimum();
                this.snap = dtype.getMinimum();
                this.label = dtype.getMinimum();
            } else {
                this.repeat = dtype.getMaximum();
                this.snap = dtype.getMaximum();
                this.label = dtype.getMaximum();
            }
        }

        /**
         * 
         *
         * @param range
         */
        public HierarchyWizardGroupingRange(Range<U> range) {
            this.repeat = range.getRepeatBound();
            this.snap = range.getSnapBound();
            this.label = range.getLabelBound();
        }
        
        /**
         * 
         *
         * @param repeat
         * @param snap
         * @param label
         */
        public HierarchyWizardGroupingRange(U repeat, U snap, U label) {
            this.repeat = repeat;
            this.snap = snap;
            this.label = label;
        }
    }

    /** Var. */
    private List<HierarchyWizardGroupingInterval<T>>    intervals     = new ArrayList<HierarchyWizardGroupingInterval<T>>();
    
    /** Var. */
    private List<List<HierarchyWizardGroupingGroup<T>>> groups        = new ArrayList<List<HierarchyWizardGroupingGroup<T>>>();
    
    /** Var. */
    private DataType<T>                                 type;
    
    /** Var. */
    private boolean                                     showIntervals = true;
    
    /** Var. */
    private AggregateFunction<T>                        function;
    
    /** Var. */
    private HierarchyWizardGroupingRange<T>             lower         = null;
    
    /** Var. */
    private HierarchyWizardGroupingRange<T>             upper         = null;
    
    /** Var. */
    private Object                                      selected      = null;
    
    /** Var. */
    private HierarchyWizardEditorRenderer<T>            renderer      = new HierarchyWizardEditorRenderer<T>(this);
    
    /** Var. */
    private List<HierarchyWizardView>                   components    = new ArrayList<HierarchyWizardView>();

    /**
     * Creates a new instance.
     *
     * @param data
     * @param type
     * @param intervals
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizardModelGrouping(String[] data, DataType<T> type, boolean intervals){
        super(data);
        this.type = type;
        this.showIntervals = intervals;
        if (intervals) {
            
            // Check
            if (!(type instanceof DataTypeWithRatioScale)) {
                throw new IllegalArgumentException("Data type with ratio scale is required");
            }
            
            // Prepare
            DataTypeWithRatioScale<T> dtype = (DataTypeWithRatioScale<T>)type;
            this.lower = new HierarchyWizardGroupingRange<T>(type, true);
            this.upper = new HierarchyWizardGroupingRange<T>(type, false);
            this.function = AggregateFunction.forType(type).createIntervalFunction(true, false);
            
            // Initialize
            if (data != null){
                T min = null;
                T max = null;
                for (String date : data) {
                    T value = dtype.parse(date);
                    if (min==null || dtype.compare(value, min) < 0) min = value;
                    if (max==null || dtype.compare(value, max) > 0) max = value;
                }
                
                if (equals(type, DataType.INTEGER)) {
                    max = dtype.add(max, (T)new Long(1)); // Add 1
                } else if (equals(type, DataType.DECIMAL)) {
                    max = dtype.add(max, (T)new Double(1)); // Add 1
                } else if (equals(type, DataType.DATE)) {
                    max = dtype.add(max, (T)new Date(3600l * 1000l)); // Add 1 day
                }
                
                this.lower.label = min;
                this.lower.repeat = min;
                this.lower.snap = min;
                this.upper.label = max;
                this.upper.repeat = max;
                this.upper.snap = max;
                this.intervals.add(new HierarchyWizardGroupingInterval<T>(min, max, this.function));
            } else {
                this.intervals.add(new HierarchyWizardGroupingInterval<T>(dtype.getMinimum(), dtype.getMaximum(), this.function));
            }
        } else {
            this.function = AggregateFunction.forType(type).createSetFunction();
            this.groups.add(new ArrayList<HierarchyWizardGroupingGroup<T>>());
            this.groups.get(0).add(new HierarchyWizardGroupingGroup<T>(1, this.function));
        }
        this.update();
    }
    

    /**
     * Adds an element after the given one.
     *
     * @param selected
     */
    @SuppressWarnings("unchecked")
    public void addAfter(Object selected) {
        if (selected instanceof HierarchyWizardGroupingInterval){
            int index= intervals.indexOf(selected);
            if (index != -1){
                T bound = ((HierarchyWizardGroupingInterval<T>)selected).max;
                intervals.add(index + 1, new HierarchyWizardGroupingInterval<T>(bound, bound, this.function));
                update();
                return;
            }
        } else if (selected instanceof HierarchyWizardGroupingGroup){
            for (List<HierarchyWizardGroupingGroup<T>> list : groups){
                int index= list.indexOf(selected);
                if (index != -1){
                    list.add(index + 1, new HierarchyWizardGroupingGroup<T>(1, this.function));
                    update();
                    return;
                }
            }
        }
    }

    /**
     * Adds an element before the given one.
     *
     * @param selected
     */
    @SuppressWarnings("unchecked")
    public void addBefore(Object selected) {
        if (selected instanceof HierarchyWizardGroupingInterval){
            int index= intervals.indexOf(selected);
            if (index != -1){
                T bound = ((HierarchyWizardGroupingInterval<T>)selected).min;
                intervals.add(index, new HierarchyWizardGroupingInterval<T>(bound, bound, this.function));
                update();
                return;
            }
        } else if (selected instanceof HierarchyWizardGroupingGroup){
            for (List<HierarchyWizardGroupingGroup<T>> list : groups){
                int index= list.indexOf(selected);
                if (index != -1){
                    list.add(index, new HierarchyWizardGroupingGroup<T>(1, this.function));
                    update();
                    return;
                }
            }
        }
    }
    
    /**
     * Adds groups.
     *
     * @param list
     */
    public void addGroups(List<HierarchyWizardGroupingGroup<T>> list) {
        groups.add(list);
    }
    
    /**
     * Adds an interval.
     *
     * @param i
     */
    public void addInterval(HierarchyWizardGroupingInterval<T> i) {
        intervals.add(i);
    }
    
    /**
     * Adds a column.
     *
     * @param selected
     */
    public void addRight(Object selected) {
        int index = 0;
        if (selected instanceof HierarchyWizardGroupingGroup){
            for (int i=0; i<groups.size(); i++){
                if (groups.get(i).indexOf(selected) != -1) {
                    index = i+1; 
                }
            }
        }
        
        List<HierarchyWizardGroupingGroup<T>> list = new ArrayList<HierarchyWizardGroupingGroup<T>>();
        groups.add(index, list);
        list.add(new HierarchyWizardGroupingGroup<T>(1, function));
        update();
    }

    /**
     * @return the type
     */
    public DataType<T> getDataType() {
        return type;
    }
    
    /**
     * Returns the default aggregate function.
     *
     * @return
     */
    public AggregateFunction<T> getDefaultFunction(){
        return this.function;
    }
    
    /**
     * @return the intervals
     */
    public List<HierarchyWizardGroupingInterval<T>> getIntervals() {
        return intervals;
    }

    /**
     * @return the lower
     */
    public HierarchyWizardGroupingRange<T> getLowerRange() {
        return lower;
    }

    /**
     * @return the groups
     */
    public List<List<HierarchyWizardGroupingGroup<T>>> getModelGroups() {
        return groups;
    }

    /**
     * @return the renderer
     */
    public HierarchyWizardEditorRenderer<T> getRenderer() {
        return renderer;
    }
    
    /**
     * @return the selected
     */
    public Object getSelectedElement() {
        return selected;
    }
    
    /**
     * @return the upper
     */
    public HierarchyWizardGroupingRange<T> getUpperRange() {
        return upper;
    }
    
    /**
     * Is this the first interval.
     *
     * @param interval
     * @return
     */
    public boolean isFirst(HierarchyWizardGroupingInterval<T> interval){
        return intervals.indexOf(interval) == 0;
    }

    /**
     * Is this the last interval.
     *
     * @param interval
     * @return
     */
    public boolean isLast(HierarchyWizardGroupingInterval<T> interval){
        return intervals.indexOf(interval) == intervals.size()-1;
    }

    /**
     * @return the showIntervals
     */
    public boolean isShowIntervals() {
        return showIntervals;
    }

    /**
     * Merges the interval down.
     *
     * @param selected
     */
    public void mergeDown(Object selected) {
        if (selected instanceof HierarchyWizardGroupingInterval){
            int index = intervals.indexOf(selected);
            if (index != -1) {
                AggregateFunction<T> function = intervals.get(index).function;
                T min = intervals.get(index-1).min;
                T max = intervals.get(index).max;
                intervals.remove(index-1);
                intervals.remove(index-1);
                intervals.add(index-1, new HierarchyWizardGroupingInterval<T>(min, max, function));
                update();
            }
        }
    }

    /**
     * Merges the interval up.
     *
     * @param selected
     */
    public void mergeUp(Object selected) {
        if (selected instanceof HierarchyWizardGroupingInterval){
            int index = intervals.indexOf(selected);
            if (index != -1) {
                AggregateFunction<T> function = intervals.get(index).function;
                T min = intervals.get(index).min;
                T max = intervals.get(index+1).max;
                intervals.remove(index);
                intervals.remove(index);
                intervals.add(index, new HierarchyWizardGroupingInterval<T>(min, max, function));
                update();
            }
        }
    }

    /**
     * Parses an interval-based spec.
     *
     * @param builder
     */
    public void parse(HierarchyBuilderIntervalBased<T> builder){
        this.type = builder.getDataType();
        this.showIntervals = true;
        this.lower.label = builder.getLowerRange().getLabelBound();
        this.lower.repeat = builder.getLowerRange().getRepeatBound();
        this.lower.snap = builder.getLowerRange().getSnapBound();
        this.upper.label = builder.getUpperRange().getLabelBound();
        this.upper.repeat = builder.getUpperRange().getRepeatBound();
        this.upper.snap = builder.getUpperRange().getSnapBound();
        this.function = builder.getDefaultFunction();
        this.intervals.clear();
        this.groups.clear();
        for (Interval<T> interval : builder.getIntervals()) {
            this.intervals.add(new HierarchyWizardGroupingInterval<T>(interval));
        }
        for (Level<T> level : builder.getLevels()){
            List<HierarchyWizardGroupingGroup<T>> list = new ArrayList<HierarchyWizardGroupingGroup<T>>();
            this.groups.add(list);
            for (Group<T> group : level.getGroups()) {
                list.add(new HierarchyWizardGroupingGroup<T>(group));
            }
        }
        update();
    }

    /**
     * Parses an order-based spec.
     *
     * @param builder
     * @throws IllegalArgumentException
     */
    public void parse(HierarchyBuilderOrderBased<T> builder) throws IllegalArgumentException{
        
        if (builder.getComparator() != null) {
            try {
                Arrays.sort(this.data, builder.getComparator());
            } catch (Exception e){
                throw new IllegalArgumentException("The given order cannot be applied to the data");
            }
        }
        this.showIntervals = false;
        this.function = builder.getDefaultFunction();
        this.intervals.clear();
        this.groups.clear();
        this.lower = null;
        this.upper = null;
        for (Level<T> level : builder.getLevels()){
            List<HierarchyWizardGroupingGroup<T>> list = new ArrayList<HierarchyWizardGroupingGroup<T>>();
            this.groups.add(list);
            for (Group<T> group : level.getGroups()) {
                list.add(new HierarchyWizardGroupingGroup<T>(group));
            }
        }
        update();
    }

    /**
     * Registers a part of the UI.
     *
     * @param component
     */
    public void register(HierarchyWizardView component){
        this.components.add(component);
    }

    /**
     * Removes the given object.
     *
     * @param selected
     */
    public void remove(Object selected) {
        if (selected instanceof HierarchyWizardGroupingInterval){
            Iterator<HierarchyWizardGroupingInterval<T>> iter = intervals.iterator();
            while (iter.hasNext()){
                if (iter.next().equals(selected)) {
                    iter.remove();
                    selected = null;
                    update();
                    return;
                }
            }
        } else if (selected instanceof HierarchyWizardGroupingGroup){
            for (List<HierarchyWizardGroupingGroup<T>> list : groups){
                Iterator<HierarchyWizardGroupingGroup<T>> iter = list.iterator();
                while (iter.hasNext()){
                    if (iter.next().equals(selected)) {
                        iter.remove();
                        if (list.isEmpty()) {
                            groups.remove(list);
                        }
                        selected = null;
                        update();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Sets the default aggregate function.
     *
     * @param function
     */
    public void setDefaultFunction(AggregateFunction<T> function){
        AggregateFunction<T> old = this.function;
        this.function = function;
        // Update
        for (HierarchyWizardGroupingInterval<T> interval : intervals) {
            if (interval.function == old){
                interval.function = function;
            }
        }
        for (List<HierarchyWizardGroupingGroup<T>> list : groups) {
            for (HierarchyWizardGroupingGroup<T> group : list) {
                if (group.function == old){
                    group.function = function;
                }
            }
        }
    }

    /**
     * Updates the selected element.
     *
     * @param selected
     */
    public void setSelectedElement(Object selected) {
        this.selected = selected;
    }

    /**
     * Update the model and all UI components.
     */
    @Override
    public void update(){
        super.update();
        renderer.update();
        updateUI(null);
    }

    /**
     * Update the model and all UI components, apart from the sender.
     *
     * @param sender
     */
    public void update(HierarchyWizardView sender){
        super.update();
        renderer.update();
        updateUI(sender);
    }
    
    /**
     * Update the UI components.
     */
    @Override
    public void updateUI(HierarchyWizardView sender){
        for (HierarchyWizardView c : components){
            if (c != sender) {
                c.update();
            }
        }
    }

    /**
     * Simple comparison of data types.
     *
     * @param type
     * @param other
     * @return
     */
    private boolean equals(DataType<?> type, DataType<?> other){
        return type.getDescription().getLabel().equals(other.getDescription().getLabel());
    }
}
