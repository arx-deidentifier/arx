/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import java.util.Date;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

/**
 * A builder for dates and times, taking into account specifics, such as leap years, timezones etc.
 * 
 * @author Fabian Prasser
 */
public class HierarchyBuilderDateTime extends HierarchyBuilderIntervalBased<Date>{

    /** SVUID*/
    private static final long serialVersionUID = 4206213533246265538L;

    /**
     * Creates a new instance
     */
    protected HierarchyBuilderDateTime() {
        super(DataType.DATE);
    }

    /**
     * Creates a new instance
     * @param lower
     * @param upper
     */
    protected HierarchyBuilderDateTime(Range<Date> lower, Range<Date> upper) {
        super(DataType.DATE, lower, upper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Interval<Date> getInterval(IndexNode index, DataTypeWithRatioScale<Date> type, Date tValue) {

        // Find interval
        int shift = (int)Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        Date offset = type.multiply(type.subtract(index.max, index.min), shift);
        Interval<Date> interval = getInterval(index, type.subtract(tValue, offset));

        // Check
        if (interval == null) { throw new IllegalStateException("No interval found for: " + type.format(tValue)); }
        
        // Create first result interval
        Date lower = type.add(interval.getMin(), offset);
        Date upper = type.add(interval.getMax(), offset);
        return new Interval<Date>(this, (DataType<Date>)type, lower, upper, interval.getFunction());
    }

    @Override
    protected Interval<Date> getInterval(IndexNode node, Date value) {
        @SuppressWarnings("unchecked")
        DataTypeWithRatioScale<Date> type = (DataTypeWithRatioScale<Date>)getDataType();
        if (node.isLeaf) {
            for (Interval<Date> leaf : node.leafs) {
                if (type.compare(leaf.min, value) <= 0 && type.compare(leaf.max, value) > 0) {
                    return leaf;
                }
            }
        } else {
            for (IndexNode child : node.children) {
                if (type.compare(child.min, value) <= 0 && type.compare(child.max, value) > 0) {
                    return getInterval(child, value);
                }
            }
        }
        throw new IllegalStateException("No interval found for: "+type.format(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Interval<Date> getIntervalUpperSnap(IndexNode index, DataTypeWithRatioScale<Date> type, Date tValue) {

        // Find interval
        double shift = Math.floor(type.ratio(type.subtract(tValue, index.min), type.subtract(index.max, index.min)));
        Date offset = type.multiply(type.subtract(index.max, index.min), shift);
        Date value = type.subtract(tValue, offset);
        Interval<Date> interval = null;

        for (int j=0; j<intervals.size(); j++) {
            Interval<Date> i = intervals.get(j);
            if (type.compare(i.min, value) <= 0 &&
                type.compare(i.max, value) > 0) {

                // If on lower bound, use next-lower interval
                if (type.compare(value, i.min) == 0) {
                    if (j>0) {
                        
                        // Simply use the next one
                        interval = intervals.get(j-1);
                        break;
                    } else {
                        
                        // Wrap around
                        interval = intervals.get(intervals.size()-1);
                        offset = type.multiply(type.subtract(index.max, index.min), shift-1);
                        break;
                    }
                } else {
                    interval = i;
                    break;
                }
            }
        }
        
        if (interval == null && intervals.size()==1){
            interval = intervals.get(0);
        }

        // Check
        if (interval == null) { 
            throw new IllegalStateException("Internal error. Sorry for that!"); 
        }
        
        
        // Create first result interval
        Date lower = type.add(interval.min, offset);
        Date upper = type.add(interval.max, offset);
        return new Interval<Date>(this, (DataType<Date>)type, lower, upper, interval.getFunction());
    }
}
