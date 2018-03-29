/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx.aggregates.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.DataHandleInternal.InterruptHandler;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

/**
 * A complete specification of all input and output data
 * 
 * @author Fabian Prasser
 */
public class ClassificationDataSpecification {
    /** Index */
    public final int                             classIndex;
    /** Map */
    public final Map<String, Integer>            classMap;
    /** Indexes */
    public final int[]                           featureIndices;
    /** Feature meta data */
    public final ClassificationFeatureMetadata[] inputFeatureMetadata;
    /** Feature meta data */
    public final ClassificationFeatureMetadata[] outputFeatureMetadata;
    /** Interrupt */
    private final WrappedBoolean                 interrupt;

    /**
     * Creates a new instance
     * @param inputFeatureHandle
     * @param outputFeatureHandle
     * @param classHandle
     * @param features
     * @param clazz
     * @param interrupt
     */
    public ClassificationDataSpecification(DataHandleInternal inputFeatureHandle, 
                                           DataHandleInternal outputFeatureHandle,
                                           String[] features,
                                           String clazz,
                                           WrappedBoolean interrupt) {
        
        if (clazz == null) {
            throw new IllegalArgumentException("No class attribute defined");
        }
        if (outputFeatureHandle.getColumnIndexOf(clazz) == -1) {
            throw new IllegalArgumentException("Unknown class '"+clazz+"'");
        }
        if (features == null) {
            throw new IllegalArgumentException("No features defined");
        }
        for (String feature : features) {
            if (feature == null) {
                throw new IllegalArgumentException("Feature must not be null");    
            }
            if (inputFeatureHandle.getColumnIndexOf(feature) == -1) {
                throw new IllegalArgumentException("Unknown feature '"+feature+"'");
            }
        }
        this.interrupt = interrupt;
        this.featureIndices = getFeatureIndices(inputFeatureHandle, features, clazz);
        // TODO: Include this and use it for normalizing and scaling continuous variables
        // this.inputFeatureMetadata = getFeatureMetadata(inputFeatureHandle, featureIndices);
        // if (outputFeatureHandle != inputFeatureHandle) {
        //     this.outputFeatureMetadata = getFeatureMetadata(outputFeatureHandle, featureIndices);
        // } else {
        //     this.outputFeatureMetadata = this.inputFeatureMetadata;
        // }
        this.inputFeatureMetadata = null;
        this.outputFeatureMetadata = null;
        this.classIndex = getClassIndex(outputFeatureHandle, clazz);
        this.classMap = getClassMap(outputFeatureHandle, classIndex);

        if (classMap.size() == 0) {
            throw new IllegalArgumentException("No classes defined");
        }
    }

    /**
     * Checks whether an interruption happened.
     */
    private void checkInterrupt() {
        if (interrupt.value) {
            throw new ComputationInterruptedException("Interrupted");
        }
    }

    /**
     * Returns the class index
     * @param handle
     * @param clazz
     * @return
     */
    private int getClassIndex(DataHandleInternal handle, String clazz) {
        return handle.getColumnIndexOf(clazz);
    }
    
    
    /**
     * Returns the class map, ignores suppression while accessing the handle
     * @param handle
     * @param clazz
     * @param interrupt 
     * @return
     */
    private Map<String, Integer> getClassMap(DataHandleInternal handle, int clazz) {
        
        // Prepare
        int index = 0;
        Map<String, Integer> result = new HashMap<String, Integer>();
        
        // Fetch values
        List<String> values = new ArrayList<String>();
        for (String value : handle.getDistinctValues(clazz, true, new InterruptHandler() {
                                                                    @Override
                                                                    public void checkInterrupt() {
                                                                        ClassificationDataSpecification.this.checkInterrupt();
                                                                    }})) {
                
            checkInterrupt();
            values.add(value);
        }
        
        // Sort to obtain comparable results and store ids
        Collections.sort(values);
        for (String value : values) {
            checkInterrupt();
            result.put(value, index++);
        }
        
        return result;
    }

    /**
     * Returns the indexes of all features
     * @param handle
     * @param features
     * @param clazz
     * @return
     */
    protected int[] getFeatureIndices(DataHandleInternal handle, String[] features, String clazz) {
        // Collect
        List<Integer> list = new ArrayList<>();
        for (int column = 0; column < handle.getNumColumns(); column++) {
            String attribute = handle.getAttributeName(column);
            if (isContained(features, attribute) && !attribute.equals(clazz)) {
                list.add(column);
            }
        }
        
        // Convert
        int[] result = new int[list.size()];
        for (int i=0; i<list.size(); i++) {
            result[i] = list.get(i);
        }
        
        // Return
        return result;
    }
    
    /**
     * Returns feature metadata
     * @param handle
     * @param features
     * @return
     */
    protected ClassificationFeatureMetadata[] getFeatureMetadata(DataHandleInternal handle, int[] features) {

        // Prepare
        ClassificationFeatureMetadata[] result = new ClassificationFeatureMetadata[features.length];
        for (int i=0; i<result.length; i++) {
            int column = features[i];
            String attribute = handle.getAttributeName(column);
            DataType<?> type = handle.getDataType(attribute);
            result[i] = new ClassificationFeatureMetadata(attribute, type);
            
            // Compute min & max for numeric attributes
            if (type instanceof ARXDecimal ||
                type instanceof ARXInteger ||
                type instanceof ARXDate) {
                
                // Parse each value
                for (String value : handle.getStatisticsBuilder().getDistinctValues(column)) {
                    checkInterrupt();
                    result[i].updateMinMax(value);
                }
            }
        }
        
        // Return
        return result;
    }

    /**
     * Returns whether the given array contains the given value
     * @param array
     * @param value
     * @return
     */
    protected boolean isContained(String[] array, String value) {
        for (String element : array) {
            if (element.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
