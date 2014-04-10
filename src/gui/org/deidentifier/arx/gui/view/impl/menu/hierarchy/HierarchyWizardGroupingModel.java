package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
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

/**
 * This class implements the model of the hierarchy editor
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardGroupingModel<T> implements HierarchyWizardGroupingView{
    
    /**
     * This class represents a group
     * @author Fabian Prasser
     *
     * @param <U>
     */
    public static class HierarchyWizardGroupingGroup<U> {
        public int size;
        public AggregateFunction<U> function;
        
        public HierarchyWizardGroupingGroup(Group<U> group) {
            this.size = group.getSize();
            this.function = group.getFunction();
        }

        public HierarchyWizardGroupingGroup(int size, AggregateFunction<U> function) {
            this.size = size;
            this.function = function;
        }
    }

    /**
     * This class represents an interval
     * @author Fabian Prasser
     *
     * @param <U>
     */
    public static class HierarchyWizardGroupingInterval<U> {
        public U min;
        public U max;
        public AggregateFunction<U> function;
        
        public HierarchyWizardGroupingInterval(Interval<U> interval) {
            this.min = interval.getMin();
            this.max = interval.getMax();
            this.function = interval.getFunction();
        }

        public HierarchyWizardGroupingInterval(U min, U max, AggregateFunction<U> function) {
            this.min = min;
            this.max = max;
            this.function = function;
        }
    }
    
    /**
     * This class represents an adjustment
     * @author Fabian Prasser
     *
     * @param <U>
     */
    public static class HierarchyWizardGroupingRange<U> {
        public U repeat;
        public U snap;
        public U label;
        
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

        public HierarchyWizardGroupingRange(Range<U> range) {
            this.repeat = range.getRepeatBound();
            this.snap = range.getSnapBound();
            this.label = range.getLabelBound();
        }
        
        public HierarchyWizardGroupingRange(U repeat, U snap, U label) {
            this.repeat = repeat;
            this.snap = snap;
            this.label = label;
        }
    }

    /** Var */
    private List<HierarchyWizardGroupingInterval<T>>    intervals     = new ArrayList<HierarchyWizardGroupingInterval<T>>();
    /** Var */
    private List<List<HierarchyWizardGroupingGroup<T>>> groups        = new ArrayList<List<HierarchyWizardGroupingGroup<T>>>();
    /** Var */
    private DataType<T>                                 type;
    /** Var */
    private boolean                                     showIntervals = true;
    /** Var */
    private AggregateFunction<T>                        function;
    /** Var */
    private HierarchyWizardGroupingRange<T>             lower         = null;
    /** Var */
    private HierarchyWizardGroupingRange<T>             upper         = null;
    /** Var */
    private Object                                      selected      = null;
    /** Var */
    private HierarchyWizardGroupingRenderer<T>          renderer      = new HierarchyWizardGroupingRenderer<T>(this);
    /** Var */
    private List<HierarchyWizardGroupingView>           components    = new ArrayList<HierarchyWizardGroupingView>();

    /**
     * Creates a new instance
     * @param type
     * @param intervals
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizardGroupingModel(DataType<T> type, boolean intervals){
        this.type = type;
        this.showIntervals = intervals;
        if (intervals) {
            this.lower = new HierarchyWizardGroupingRange<T>(type, true);
            this.upper = new HierarchyWizardGroupingRange<T>(type, false);
            if (!(type instanceof DataTypeWithRatioScale)) {
                throw new IllegalArgumentException("Data type with ratio scale is required");
            }
            DataTypeWithRatioScale<T> dtype = (DataTypeWithRatioScale<T>)type;
            this.function = AggregateFunction.forType(type).createIntervalFunction(true, false);
            this.intervals.add(new HierarchyWizardGroupingInterval<T>(dtype.getMinimum(), dtype.getMaximum(), this.function));
        } else {
            this.function = AggregateFunction.forType(type).createSetFunction();
            this.groups.add(new ArrayList<HierarchyWizardGroupingGroup<T>>());
            this.groups.get(0).add(new HierarchyWizardGroupingGroup<T>(1, this.function));
        }
        this.update();
    }

    /**
     * Creates a new instance from the given builder
     * @param builder
     */
    public HierarchyWizardGroupingModel(HierarchyBuilderIntervalBased<T> builder){
        this.type = builder.getDataType();
        this.showIntervals = true;
        this.lower = new HierarchyWizardGroupingRange<T>(builder.getLowerRange());
        this.upper = new HierarchyWizardGroupingRange<T>(builder.getUpperRange());
        this.function = builder.getDefaultFunction();
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
    }
    /**
     * Creates a new instance from the given builder
     * @param builder
     */
    public HierarchyWizardGroupingModel(HierarchyBuilderOrderBased<T> builder){
        this.type = builder.getDataType();
        this.showIntervals = false;
        this.function = builder.getDefaultFunction();
        for (Level<T> level : builder.getLevels()){
            List<HierarchyWizardGroupingGroup<T>> list = new ArrayList<HierarchyWizardGroupingGroup<T>>();
            this.groups.add(list);
            for (Group<T> group : level.getGroups()) {
                list.add(new HierarchyWizardGroupingGroup<T>(group));
            }
        }
    }
    
    /**
     * Adds an element after the given one
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
     * Adds an element before the given one
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
     * Adds groups
     */
    public void addGroups(List<HierarchyWizardGroupingGroup<T>> list) {
        groups.add(list);
    }
    
    /**
     * Adds an interval
     */
    public void addInterval(HierarchyWizardGroupingInterval<T> i) {
        intervals.add(i);
    }
    
    /**
     * Adds a column
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
     * Returns the default aggregate function
     * @return
     */
    public AggregateFunction<T> getDefaultFunction(){
        return this.function;
    }

    /**
     * @return the groups
     */
    public List<List<HierarchyWizardGroupingGroup<T>>> getGroups() {
        return groups;
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
    public HierarchyWizardGroupingRange<T> getLowerAdjustment() {
        return lower;
    }
    
    /**
     * @return the renderer
     */
    public HierarchyWizardGroupingRenderer<T> getRenderer() {
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
    public HierarchyWizardGroupingRange<T> getUpperAdjustment() {
        return upper;
    }

    /**
     * Is this the first interval
     * @param interval
     * @return
     */
    public boolean isFirst(HierarchyWizardGroupingInterval<T> interval){
        return intervals.indexOf(interval) == 0;
    }

    /**
     * Is this the last interval
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
     * Merges the interval down
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
     * Merges the interval up
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
     * Registers a part of the UI
     * @param component
     */
    public void register(HierarchyWizardGroupingView component){
        this.components.add(component);
    }

    /**
     * Removes the given object
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
     * Sets the default aggregate function
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
     * Updates the selected element
     * @param selected
     */
    public void setSelectedElement(Object selected) {
        this.selected = selected;
    }

    /**
     * Update all UI components
     */
    public void update(){
        renderer.update();
        for (HierarchyWizardGroupingView c : components){
            c.update();
        }
    }

    /**
     * Update all UI components, apart from the sender
     * @param sender
     */
    public void update(HierarchyWizardGroupingView sender){
        renderer.update();
        for (HierarchyWizardGroupingView c : components){
            if (c != sender) {
                c.update();
            }
        }
    }
}
