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

package org.deidentifier.arx.framework.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.metric.v2.IO;

import com.carrotsearch.hppc.LongDoubleOpenHashMap;

/**
 * This class maps generalized values to the resulting sum of squared distances from the centroid.
 * 
 * @author Fabian Prasser
 */
public class DataCentroidDistances<T> implements Serializable {

    /** SVUID. */
    private static final long               serialVersionUID = 3701193962867071973L;

    /** The value representing a non-existent entry. */
    private static final double             NOT_AVAILABLE    = -Double.MAX_VALUE;

    /** The average distance. */
    private final double                    average;

    /** One distance per attribute value. */
    private final double[]                  distances;

    /** Number of values on level zero */
    private final int                       offset;

    /** If an attribute exists on different generalization levels, store the distance in this map: <code>(((long)value) << 32) | (level & 0xffffffffL) -> share </code>. */
    private transient LongDoubleOpenHashMap duplicates;

    /**
     * Creates a new set of distances derived from the given hierarchy.
     * @param data
     * @param column
     * @param type
     * @param hierarchy
     * @param normalized Normalize by standard deviation
     */
    DataCentroidDistances(Data data,
                          int column,
                          DataType<T> type,
                          int[][] hierarchy,
                          boolean normalized) {

        // Prepare
        String[] dictionary = data.getDictionary().getMapping()[column];
        double[] values = getValues(data, column, type);
        DataMatrix matrix = data.getArray();
        double stdDev = normalized ? getStandardDeviation(values) : -1d;
        
        // We do not store distances for values on level zero
        // The number of such values is stored in this variable
        this.offset = hierarchy.length;

        // Results
        this.duplicates = new LongDoubleOpenHashMap();
        this.distances = new double[dictionary.length - offset];
        Arrays.fill(this.distances, NOT_AVAILABLE);

        // Calculate distances for higher levels
        for (int level = 1; level < hierarchy[0].length; level++) {

            // Initialize distances
            double[] array1 = new double[dictionary.length - offset];
            double[] array2 = new double[dictionary.length - offset];

            // For each cell value
            for (int row = 0; row < matrix.getNumRows(); row++) {

                // Generalize
                int id = matrix.get(row, column);
                int generalized = hierarchy[id][level];

                // Store
                array1[generalized - offset] += values[row];
                array2[generalized - offset]++;
            }

            // Now calculate averages
            for (int i = 0; i < array1.length; i++) {
                if (array2[i] != 0) {
                    array1[i] /= array2[i];
                    array2[i] = 0;
                } else {
                    array2[i] = NOT_AVAILABLE;
                }
            }

            // Now calculate squared errors
            for (int row = 0; row < matrix.getNumRows(); row++) {

                // Generalize
                int id = matrix.get(row, column);
                int generalized = hierarchy[id][level];

                // Store
                double error = (values[row] - array1[generalized - offset]);
                error = (stdDev == -1d) ? error : (stdDev == 0d ? 0d : error / stdDev);
                array2[generalized - offset] += error * error;
            }

            // Now merge into global distances
            for (int i = 0; i < array2.length; i++) {

                // Check if available
                if (array2[i] != NOT_AVAILABLE) {
                    
                    // Check if duplicate
                    double distance = array2[i];
                    double stored = distances[i];
                    if (stored != NOT_AVAILABLE) {
    
                        // If same share, simply continue
                        if (stored == distance) {
                            continue;
                        }
    
                        // Mark as duplicate, if not already marked
                        if (stored >= 0d) {
                            distances[i] = -distances[i];
                        }
    
                        // Store duplicate value
                        long dkey = (((long) i) << 32) | (level & 0xffffffffL);
                        duplicates.put(dkey, distance);
    
                        // If its not a duplicate, simply store
                    } else {
                        distances[i] = distance;
                    }
                }
            }
        }
        
        // Calculate dataset centroid
        double var1 = 0d;
        for (int row = 0; row < matrix.getNumRows(); row++) {
            var1 += values[row];
        }
        var1 /= (double)values.length;

        // Now calculate squared errors
        double average = 0d;
        for (int row = 0; row < matrix.getNumRows(); row++) {
            double error = values[row] - var1;
            error = (stdDev == -1d) ? error : (stdDev == 0d ? 0d : error / stdDev);
            average += error * error;
        }
    
        // And store
        this.average = average;
    }

    /**
     * Returns the average distance
     * @return
     */
    public double getAverageDistance() {
        return this.average;
    }

    /**
     * Returns the distance for a given value.
     *
     * @param value
     * @param level
     * @return
     */
    public double getDistance(int value, int level) {
        if (level == 0) {
            return 0d;
        }
        value -= offset;
        double distance = distances[value];
        if (distance == NOT_AVAILABLE) {
            throw new IllegalStateException("Unknown distance");
        } else if (distance >= 0) {
            return distance;
        } else {
            long key = (((long) value) << 32) | (level & 0xffffffffL);
            return duplicates.getOrDefault(key, -distance);
        }
    }

    /**
     * Converts a value into a double
     * @param value
     * @param type
     * @return
     */
    private double getDouble(String value, DataTypeWithRatioScale<T> type) {

        // Silently fall back to 0 for NULL values
        if (value == null) {
            return 0d;
        }
        
        // Parse
        Double result = type.toDouble(type.parse(value));
        
        // Silently fall back to 0 for NULL values
        return result != null ? result : 0d;
    }
    
    /**
     * Returns all cell values
     * @param data
     * @param column
     * @param type
     * @return
     */
    private double[] getValues(Data data, int column, DataType<T> type) {

        // Prepare
        double[] result = new double[data.getDataLength()];
        DataMatrix matrix = data.getArray();
        String[] dictionary = data.getDictionary().getMapping()[column];
        
        if (!(type instanceof DataTypeWithRatioScale)) {
            
            // Fall back to dictionary identifiers
            for (int row = 0; row < result.length; row++) {
                result[row] = matrix.get(row, column);
            }
        } else {
            
            // Convert data
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> typewrs = (DataTypeWithRatioScale<T>)type;
            for (int row = 0; row < result.length; row++) {
                String value = dictionary[matrix.get(row, column)];
                result[row] = getDouble(value, typewrs);
            }
        }

        // Return
        return result;
    }
    

    /**
     * Returns the standard deviation for the given values
     * @param values
     * @return
     */
    private double getStandardDeviation(double[] values) {
        
        // Calculate mean
        double mean = 0d;
        for (int i = 0; i < values.length; i += 2) {
            double value = values[i];
            mean += value;
        }
        mean /= (double)(values.length / 2);
        
        // Calculate standard deviation
        double stdDev = 0d;
        for (int i = 0; i < values.length; i += 2) {
            double value = values[i];
            double temp = value - mean;
            temp = temp * temp;
            stdDev += temp;
        }
        stdDev /= (double)(values.length / 2);
        stdDev = Math.sqrt(stdDev);
        
        // Return
        return stdDev;
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
