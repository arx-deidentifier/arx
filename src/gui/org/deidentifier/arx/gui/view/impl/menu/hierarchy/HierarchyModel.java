package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.AggregateFunction;

/**
 * This class implements the model of the hierarchy editor
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyModel<T> implements IUpdateable{
    
    /**
     * This class represents an adjustment
     * @author Fabian Prasser
     *
     * @param <U>
     */
    public static class HierarchyAdjustment<U> {
        public U repeat;
        public U snap;
        public U label;
        
        @SuppressWarnings("unchecked")
        public HierarchyAdjustment(DataType<U> type, boolean lower){
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

        public HierarchyAdjustment(U repeat, U snap, U label) {
            this.repeat = repeat;
            this.snap = snap;
            this.label = label;
        }
    }

    /**
     * This class represents a group
     * @author Fabian Prasser
     *
     * @param <U>
     */
    public static class HierarchyGroup<U> {
        public int size;
        public AggregateFunction<U> function;
        
        public HierarchyGroup(int size, AggregateFunction<U> function) {
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
    public static class HierarchyInterval<U> {
        public U min;
        public U max;
        public AggregateFunction<U> function;
        
        public HierarchyInterval(U min, U max, AggregateFunction<U> function) {
            this.min = min;
            this.max = max;
            this.function = function;
        }
    }

    /** Var*/
    private List<HierarchyInterval<T>>    intervals     = new ArrayList<HierarchyInterval<T>>();
    /** Var*/
    private List<List<HierarchyGroup<T>>> groups        = new ArrayList<List<HierarchyGroup<T>>>();
    /** Var*/
    private DataType<T>                   type;
    /** Var*/
    private boolean                       showIntervals = true;
    /** Var*/
    private AggregateFunction<T>          function;
    /** Var*/
    private HierarchyAdjustment<T>        lower         = null;
    /** Var*/
    private HierarchyAdjustment<T>        upper         = null;
    /** Var */
    private Object                        selected      = null;
    /** Var */
    private HierarchyRenderer<T>          renderer      = new HierarchyRenderer<T>(this);
    /** Var */
    private List<IUpdateable>             components    = new ArrayList<IUpdateable>();

    /**
     * Creates a new instance
     * @param type
     * @param intervals
     */
    @SuppressWarnings("unchecked")
    public HierarchyModel(DataType<T> type, boolean intervals){
        this.type = type;
        this.showIntervals = intervals;
        if (intervals) {
            this.lower = new HierarchyAdjustment<T>(type, true);
            this.upper = new HierarchyAdjustment<T>(type, false);
            if (!(type instanceof DataTypeWithRatioScale)) {
                throw new IllegalArgumentException("Data type with ratio scale is required");
            }
            DataTypeWithRatioScale<T> dtype = (DataTypeWithRatioScale<T>)type;
            this.function = AggregateFunction.INTERVAL(type, true, false);
            this.intervals.add(new HierarchyInterval<T>(dtype.getMinimum(), dtype.getMaximum(), this.function));
        } else {
            this.function = AggregateFunction.SET(type);
            this.groups.add(new ArrayList<HierarchyGroup<T>>());
            this.groups.get(0).add(new HierarchyGroup<T>(1, this.function));
        }
        this.update();
    }
    
    /**
     * Returns the default aggregate function
     * @return
     */
    public AggregateFunction<T> getDefaultFunction(){
        return this.function;
    }
    
    /**
     * Sets the default aggregate function
     * @param function
     */
    public void setDefaultFunction(AggregateFunction<T> function){
        AggregateFunction<T> old = this.function;
        this.function = function;
        // Update
        for (HierarchyInterval<T> interval : intervals) {
            if (interval.function == old){
                interval.function = function;
            }
        }
        for (List<HierarchyGroup<T>> list : groups) {
            for (HierarchyGroup<T> group : list) {
                if (group.function == old){
                    group.function = function;
                }
            }
        }
    }

    /**
     * Update all UI components, apart from the sender
     * @param sender
     */
    public void update(IUpdateable sender){
        renderer.update();
        for (IUpdateable c : components){
            if (c != sender) {
                c.update();
            }
        }
    }
    
    /**
     * Update all UI components
     */
    public void update(){
        renderer.update();
        for (IUpdateable c : components){
            c.update();
        }
    }
    
    /**
     * Registers a part of the UI
     * @param component
     */
    public void register(IUpdateable component){
        this.components.add(component);
    }

    /**
     * Removes the given object
     * @param selected
     */
    public void remove(Object selected) {
        if (selected instanceof HierarchyInterval){
            Iterator<HierarchyInterval<T>> iter = intervals.iterator();
            while (iter.hasNext()){
                if (iter.next().equals(selected)) {
                    iter.remove();
                    selected = null;
                    update();
                    return;
                }
            }
        } else if (selected instanceof HierarchyGroup){
            for (List<HierarchyGroup<T>> list : groups){
                Iterator<HierarchyGroup<T>> iter = list.iterator();
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
     * Adds an element after the given one
     * @param selected
     */
    @SuppressWarnings("unchecked")
    public void addAfter(Object selected) {
        if (selected instanceof HierarchyInterval){
            int index= intervals.indexOf(selected);
            if (index != -1){
                T bound = ((HierarchyInterval<T>)selected).max;
                intervals.add(index + 1, new HierarchyInterval<T>(bound, bound, this.function));
                update();
                return;
            }
        } else if (selected instanceof HierarchyGroup){
            for (List<HierarchyGroup<T>> list : groups){
                int index= list.indexOf(selected);
                if (index != -1){
                    list.add(index + 1, new HierarchyGroup<T>(1, this.function));
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
        if (selected instanceof HierarchyInterval){
            int index= intervals.indexOf(selected);
            if (index != -1){
                T bound = ((HierarchyInterval<T>)selected).min;
                intervals.add(index, new HierarchyInterval<T>(bound, bound, this.function));
                update();
                return;
            }
        } else if (selected instanceof HierarchyGroup){
            for (List<HierarchyGroup<T>> list : groups){
                int index= list.indexOf(selected);
                if (index != -1){
                    list.add(index, new HierarchyGroup<T>(1, this.function));
                    update();
                    return;
                }
            }
        }
    }

    /**
     * Adds a column
     * @param selected
     */
    public void addRight(Object selected) {
        int index = 0;
        if (selected instanceof HierarchyGroup){
            for (int i=0; i<groups.size(); i++){
                if (groups.get(i).indexOf(selected) != -1) {
                    index = i+1; 
                }
            }
        }
        
        List<HierarchyGroup<T>> list = new ArrayList<HierarchyGroup<T>>();
        groups.add(index, list);
        list.add(new HierarchyGroup<T>(1, function));
        update();
    }
    
    /**
     * Is this the first interval
     * @param interval
     * @return
     */
    public boolean isFirst(HierarchyInterval<T> interval){
        return intervals.indexOf(interval) == 0;
    }
    
    /**
     * Is this the last interval
     * @param interval
     * @return
     */
    public boolean isLast(HierarchyInterval<T> interval){
        return intervals.indexOf(interval) == intervals.size()-1;
    }

    /**
     * Merges the interval down
     * @param selected
     */
    public void mergeDown(Object selected) {
        if (selected instanceof HierarchyInterval){
            int index = intervals.indexOf(selected);
            if (index != -1) {
                AggregateFunction<T> function = intervals.get(index).function;
                T min = intervals.get(index-1).min;
                T max = intervals.get(index).max;
                intervals.remove(index-1);
                intervals.remove(index-1);
                intervals.add(index-1, new HierarchyInterval<T>(min, max, function));
                update();
            }
        }
    }

    /**
     * Merges the interval up
     * @param selected
     */
    public void mergeUp(Object selected) {
        if (selected instanceof HierarchyInterval){
            int index = intervals.indexOf(selected);
            if (index != -1) {
                AggregateFunction<T> function = intervals.get(index).function;
                T min = intervals.get(index).min;
                T max = intervals.get(index+1).max;
                intervals.remove(index);
                intervals.remove(index);
                intervals.add(index, new HierarchyInterval<T>(min, max, function));
                update();
            }
        }
    }

    /**
     * Adds an interval
     */
    public void addInterval(HierarchyInterval<T> i) {
        intervals.add(i);
    }

    /**
     * @return the intervals
     */
    public List<HierarchyInterval<T>> getIntervals() {
        return intervals;
    }

    /**
     * @return the groups
     */
    public List<List<HierarchyGroup<T>>> getGroups() {
        return groups;
    }

    /**
     * Adds groups
     */
    public void addGroups(List<HierarchyGroup<T>> list) {
        groups.add(list);
    }

    /**
     * @return the type
     */
    public DataType<T> getDataType() {
        return type;
    }

    /**
     * @return the showIntervals
     */
    public boolean isShowIntervals() {
        return showIntervals;
    }

    /**
     * @return the lower
     */
    public HierarchyAdjustment<T> getLowerAdjustment() {
        return lower;
    }

    /**
     * @return the upper
     */
    public HierarchyAdjustment<T> getUpperAdjustment() {
        return upper;
    }

    /**
     * @return the selected
     */
    public Object getSelectedElement() {
        return selected;
    }

    /**
     * @return the renderer
     */
    public HierarchyRenderer<T> getRenderer() {
        return renderer;
    }

    /**
     * Updates the selected element
     * @param selected
     */
    public void setSelectedElement(Object selected) {
        this.selected = selected;
    }
}
