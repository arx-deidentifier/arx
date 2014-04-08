package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction;

public class HierarchyModel<T> implements IUpdateable{
    
    public static class HierarchyAdjustment<U> {
        public U repeat;
        public U snap;
        public U label;
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

    public List<HierarchyInterval<T>>     intervals     = new ArrayList<HierarchyInterval<T>>();
    public List<List<HierarchyGroup<T>>>  groups       = new ArrayList<List<HierarchyGroup<T>>>();

    public DataType<T>                    type;
    public boolean                        showIntervals = true;
    public AggregateFunction<T>           function;
    public HierarchyAdjustment<T>         lower         = new HierarchyAdjustment<T>();
    public HierarchyAdjustment<T>         upper         = new HierarchyAdjustment<T>();
    public Object                         selected      = null;
    public HierarchyDrawingContext<T>     context       = new HierarchyDrawingContext<T>(this);
    
    private List<IUpdateable> components = new ArrayList<IUpdateable>();

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
}
