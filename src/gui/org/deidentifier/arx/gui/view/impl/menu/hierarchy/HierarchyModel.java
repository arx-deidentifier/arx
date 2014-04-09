package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.AggregateFunction;

public class HierarchyModel<T> implements IUpdateable{
    
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

    public static class HierarchyGroup<U> {
        public int size;
        public AggregateFunction<U> function;
        
        public HierarchyGroup(int size, AggregateFunction<U> function) {
            this.size = size;
            this.function = function;
        }
    }
    
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

    public List<HierarchyInterval<T>>    intervals     = new ArrayList<HierarchyInterval<T>>();
    public List<List<HierarchyGroup<T>>> groups        = new ArrayList<List<HierarchyGroup<T>>>();
    public DataType<T>                   type;
    public boolean                       showIntervals = true;
    public AggregateFunction<T>          function;
    public HierarchyAdjustment<T>        lower         = null;
    public HierarchyAdjustment<T>        upper         = null;
    public Object                        selected      = null;
    public HierarchyDrawingContext<T>    context       = new HierarchyDrawingContext<T>(this);
    private List<IUpdateable>            components    = new ArrayList<IUpdateable>();

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

    public void update(IUpdateable component){
        context.update();
        for (IUpdateable c : components){
            if (c != component) {
                c.update();
            }
        }
    }
    
    public void update(){
        context.update();
        for (IUpdateable c : components){
            c.update();
        }
    }
    
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
    
    public boolean isFirst(HierarchyInterval<T> interval){
        return intervals.indexOf(interval) == 0;
    }
    
    public boolean isLast(HierarchyInterval<T> interval){
        return intervals.indexOf(interval) == intervals.size()-1;
    }

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
}
