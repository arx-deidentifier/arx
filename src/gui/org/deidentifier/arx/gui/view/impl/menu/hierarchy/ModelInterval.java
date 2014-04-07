package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.AggregateFunction;

public class ModelInterval<T> {
    
    public static class ModelIntervalAdjustment<U> {
        public U repeat;
        public U snap;
        public U label;
    }

    public static class ModelIntervalFanout<U> {
        public int size;
        public AggregateFunction<U> function;
        
        public ModelIntervalFanout(int size, AggregateFunction<U> function) {
            this.size = size;
            this.function = function;
        }
    }
    
    public static class ModelIntervalInterval<U> {
        public U min;
        public U max;
        public AggregateFunction<U> function;
        
        public ModelIntervalInterval(U min, U max, AggregateFunction<U> function) {
            this.min = min;
            this.max = max;
            this.function = function;
        }
    }

    private static final int              PRETTY_THRESHOLD    = 100;

    public List<ModelIntervalInterval<T>> intervals           = new ArrayList<ModelIntervalInterval<T>>();
    public List<ModelIntervalFanout<T>>   fanouts             = new ArrayList<ModelIntervalFanout<T>>();
    public List<String>                   intervalBoundLabels = new ArrayList<String>();
    public List<String>                   fanoutBoundLabels   = new ArrayList<String>();
    public List<String>                   intervalLabels      = new ArrayList<String>();
    public List<String>                   fanoutLabels        = new ArrayList<String>();

    public DataType<T>                    type;
    public boolean                        showIntervals;
    public AggregateFunction<T>           function;
    public ModelIntervalAdjustment<T>     lower;
    public ModelIntervalAdjustment<T>     upper;
    public int                            additionalIntervals = 0;
    public int                            additionalFanouts   = 0;

    public Object                         selected            = null;

    public boolean                        pretty              = true;

    public void update(){
        updateAdditionals();
        updatePretty();
        updateLabels();
    }

    private void updatePretty() {
        int sum = 0;
        for (int i=0; i<fanouts.size() + additionalFanouts; i++) {
            ModelIntervalFanout<T> fanout = fanouts.get(i % fanouts.size());
            sum += fanout.size;
        } 
        if (sum>PRETTY_THRESHOLD){
            additionalFanouts = 0;
            additionalIntervals = 0;
            pretty = false;
        } else {
            pretty = true;
        }
    }

    private void updateAdditionals() {
        int rowsFanout = 0;
        int rowsInterval = intervals.size();
        for (ModelIntervalFanout<T> fanout : fanouts) {
            rowsFanout += fanout.size;
        }
        
        if (rowsFanout < rowsInterval) {
            additionalFanouts = 0;
            outer: while (true){
                for (ModelIntervalFanout<T> fanout : fanouts) {
                    rowsFanout += fanout.size;
                    additionalFanouts++;
                    if (rowsFanout >= rowsInterval) {
                        break outer;
                    }
                }
            }
            if (rowsFanout > rowsInterval){
                additionalIntervals = rowsFanout - rowsInterval;
            }
            
        } else if (rowsFanout > rowsInterval){
            additionalIntervals = rowsFanout - rowsInterval;
        }
    }

    private void updateLabels() {

        T additional = null; 
        @SuppressWarnings("unchecked")
        DataTypeWithRatioScale<T> dtype = (DataTypeWithRatioScale<T>)type;
        T offset = dtype.subtract(intervals.get(intervals.size() - 1).max, intervals.get(0).min);
        intervalLabels.clear();
        for (int i=0; i<intervals.size() + additionalIntervals; i++) {
            ModelIntervalInterval<T> interval = intervals.get(i % intervals.size());
            T min = additional == null ? interval.min : dtype.add(interval.min, additional);
            T max = additional == null ? interval.max : dtype.add(interval.max, additional);
            String[] values = {type.format(min), type.format(max)};
            intervalLabels.add(interval.function.aggregate(values));
            if (i>0 && ((i+1) % intervals.size()) == 0) {
                if (additional == null) {
                    additional = intervals.get(intervals.size() - 1).max;
                } else {
                    additional = dtype.add(additional, offset);
                }
            }
        }
        

        fanoutLabels.clear();
        int index = 0;
        for (int i=0; i<fanouts.size() + additionalFanouts; i++) {
            ModelIntervalFanout<T> fanout = fanouts.get(i % fanouts.size());
            ModelIntervalInterval<T> minInterval = intervals.get(index % intervals.size());
            ModelIntervalInterval<T> maxInterval = intervals.get((index + fanout.size - 1) % intervals.size());
            int minMultiplier = index / intervals.size();
            int maxMultiplier = (index + fanout.size - 1) / intervals.size();
            T min = dtype.add(minInterval.min, dtype.multiply(offset, minMultiplier));
            T max = dtype.add(maxInterval.max, dtype.multiply(offset, maxMultiplier));
            String[] values = {type.format(min), type.format(max)};
            fanoutLabels.add(fanout.function.aggregate(values));
            index+=fanout.size;
        }        

        additional = null;
        intervalBoundLabels.clear();
        for (int i=0; i<intervals.size() + additionalIntervals; i++) {
            ModelIntervalInterval<T> interval = intervals.get(i % intervals.size());
            T min = additional == null ? interval.min : dtype.add(interval.min, additional);
            T max = additional == null ? interval.max : dtype.add(interval.max, additional);
            intervalBoundLabels.add("["+type.format(min)+", "+type.format(max)+"[");
            if (i>0 && ((i+1) % intervals.size()) == 0) {
                if (additional == null) {
                    additional = intervals.get(intervals.size() - 1).max;
                } else {
                    additional = dtype.add(additional, offset);
                }
            }
        }

        fanoutBoundLabels.clear();
        for (int i=0; i<fanouts.size() + additionalFanouts; i++) {
            ModelIntervalFanout<T> fanout = fanouts.get(i % fanouts.size());
            fanoutBoundLabels.add(String.valueOf(fanout.size));
        }        
    }
}
