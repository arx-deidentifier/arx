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

package org.deidentifier.arx.metric.v2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;

import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;

import com.carrotsearch.hppc.LongDoubleOpenHashMap;

/**
 * This class represents a set of domain shares for an attribute. The shares are derived from a functional
 * interval-based generalization hierarchy
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class DomainShareInterval<T> extends HierarchyBuilderIntervalBased<T> implements DomainShare {

    /** SVUID. */
    private static final long           serialVersionUID = 3430961217394466615L;

    /** The value representing a non-existent entry. */
    private static final double         NOT_AVAILABLE    = -Double.MAX_VALUE;

    /** The domain size. */
    private double                      domainSize       = 0d;

    /** Data type. */
    private DataTypeWithRatioScale<T>   dataType;

    /** One share per attribute. */
    private final double[]              shares;

    /** If an attribute exists with different shares on different generalization levels, store the share in this map: <code>(((long)value) << 32) | (level & 0xffffffffL) -> share </code>. */
    private transient LongDoubleOpenHashMap duplicates;

    /**
     * Creates a new set of domain shares derived from the given functional interval-based hierarchy.
     *
     * @param builder
     * @param hierarchy
     * @param dictionary
     */

    @SuppressWarnings("unchecked")
    public DomainShareInterval(HierarchyBuilderIntervalBased<T> builder,
                               int[][] hierarchy, String[] dictionary) {
        
        // Super
        super(builder.getDataType(), builder.getLowerRange(), builder.getUpperRange());
        
        // Prepare
        this.duplicates = new LongDoubleOpenHashMap();
        this.shares = new double[dictionary.length];
        Arrays.fill(shares, NOT_AVAILABLE);
        
        // Copy intervals
        for (Interval<T> interval : builder.getIntervals()) {
            this.addInterval(interval);
        }
        
        // Copy levels
        this.setLevels(builder.getLevels());

        // Store ranges and type
        this.dataType = (DataTypeWithRatioScale<T>)this.getDataType();
        Range<T>[] ranges = this.getAdjustedRanges();
        this.domainSize = toDouble(dataType.subtract(ranges[1].getLabelBound(), ranges[0].getLabelBound()));
        
        // Prepare the array
        String[] input = new String[hierarchy.length];
        for (int i=0; i<hierarchy.length; i++) {
            input[i] = dictionary[hierarchy[i][0]];
        }
        
        // Re-build the intervals
        this.setData(input);
        AbstractGroup[][] groups = this.prepareGroups();
        
        // Sanity check
        if (groups[0].length != hierarchy.length) {
            throw new IllegalStateException("Invalid number of intervals");
        } else if (groups.length != hierarchy[0].length - 1) {
            throw new IllegalStateException("Invalid number of intervals");
        }
        
        for (int i=0; i<hierarchy.length; i++) {
            for (int level = 0; level < hierarchy[i].length; level++) {
                
                int value = hierarchy[i][level];
                double stored = shares[value];
                double share = 0d;
                
                if (level == 0) {
                    share = 1d / domainSize;
                } else {
                    Interval<T> interval = (Interval<T>)groups[level - 1][i];
                    share = toDouble(dataType.subtract(interval.getMax(), interval.getMin())) / domainSize;
                }
                
                // If duplicate
                if (stored != NOT_AVAILABLE) {

                    // If same share, simply continue
                    if (stored == share) {
                        continue;
                    }

                    // Mark as duplicate, if not already marked
                    if (stored >= 0d) {
                        shares[value] = -shares[value];
                    }

                    // Store duplicate value
                    long dkey = (((long) value) << 32) | (level & 0xffffffffL);
                    duplicates.put(dkey, share);

                    // If its not a duplicate, simply store
                } else {
                    shares[value] = share;
                }
                
            }
        }
    }

    /**
     * Returns the size of the domain.
     *
     * @return
     */
    @Override
    public double getDomainSize() {
        return this.domainSize;
    }

    /**
     * Returns the share of the given value.
     *
     * @param value
     * @param level
     * @return
     */
    @Override
    public double getShare(int value, int level) {
        double share = shares[value];
        if (share >= 0) {
            return share;
        } else {
            long key = (((long) value) << 32) | (level & 0xffffffffL);
            return duplicates.getOrDefault(key, -share);
        }
    }

    /**
     * De-serialization.
     *
     * @param aInputStream
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

        // Default de-serialization
        aInputStream.defaultReadObject();

        // Read map
        duplicates = IO.readLongDoubleOpenHashMap(aInputStream);
    }

    /**
     * Converts the given value of the attribute's data type to a double.
     *
     * @param value
     * @return
     */
    private double toDouble(T value) {
        if (this.dataType instanceof ARXDate) {
            return ((Date)value).getTime();
        } else if (this.dataType instanceof ARXDecimal) {
            return (Double)value;
        } else if (this.dataType instanceof ARXInteger) {
            return (Long)value;
        } else {
            throw new IllegalStateException("Unknown data type");
        }
    }

    /**
     * Serialization.
     *
     * @param aOutputStream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

        // Default serialization
        aOutputStream.defaultWriteObject();
        
        // Write map
        IO.writeLongDoubleOpenHashMap(aOutputStream, duplicates);
    }
}
