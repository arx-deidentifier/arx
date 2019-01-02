/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.exceptions.ComputationInterruptedException;

import de.linearbits.suda2.SUDA2;
import de.linearbits.suda2.SUDA2ListenerProgress;
import de.linearbits.suda2.SUDA2StatisticsScores;

/**
 * A risk model based on MSUs in the data set, returning score statistics
 * @author Fabian Prasser
 */
public class RiskModelMSUScoreStatistics {
    

    /** Thresholds */
    private static final double[] thresholdsLow           = new double[] {  0d,
                                                                            0.00000001d,
                                                                            0.0000001d,
                                                                            0.000001d,
                                                                            0.00001d,
                                                                            0.0001d,
                                                                            0.001d,
                                                                            0.01d,
                                                                            0.02d,
                                                                            0.03d,
                                                                            0.04d,
                                                                            0.05d,
                                                                            0.06d,
                                                                            0.07d,
                                                                            0.08d,
                                                                            0.09d,
                                                                            0.1d,
                                                                            0.125d,
                                                                            0.143d,
                                                                            0.167d,
                                                                            0.2d,
                                                                            0.25d,
                                                                            0.334d,
                                                                            0.5d};
    /** Thresholds */
    private static final double[] thresholdsHigh          = new double[] {  0.00000001d,
                                                                            0.0000001d,
                                                                            0.000001d,
                                                                            0.00001d,
                                                                            0.0001d,
                                                                            0.001d,
                                                                            0.01d,
                                                                            0.02d,
                                                                            0.03d,
                                                                            0.04d,
                                                                            0.05d,
                                                                            0.06d,
                                                                            0.07d,
                                                                            0.08d,
                                                                            0.09d,
                                                                            0.1d,
                                                                            0.125d,
                                                                            0.143d,
                                                                            0.167d,
                                                                            0.2d,
                                                                            0.25d,
                                                                            0.334d,
                                                                            0.5d,
                                                                            1d };

    /** Risks */
    private final double[]        recordsAtRisk           = new double[thresholdsLow.length];
    /** Cumulative risks */
    private final double[]        recordsAtCumulativeRisk = new double[thresholdsLow.length];

    /** Progress stuff */
    private final WrappedInteger progress;
    /** Progress stuff */
    private final WrappedBoolean stop;
    /** Maximal size of keys considered */
    private final int            maxKeyLength;
    /** SUDA Scores */
    private final double[]       scoresSUDA;
    /** Score */
    private final double         maxScore;
    /** Score */
    private final double         averageScore;
    /** Attributes */
    private final String[]       attributes;

    /**
     * Creates a new instance
     * @param handle
     * @param identifiers
     * @param stop 
     * @param progress 
     * @param maxKeyLength
     * @param population
     * @param sdcMicroScore
     */
    RiskModelMSUScoreStatistics(DataHandleInternal handle, 
                 Set<String> identifiers, 
                 WrappedInteger progress, 
                 WrappedBoolean stop,
                 int maxKeyLength,
                 ARXPopulationModel population,
                 boolean sdcMicroScore) {

        // Store
        this.stop = stop;
        this.progress = progress;
        maxKeyLength = maxKeyLength < 0 ? 0 : maxKeyLength;
        
        // Add all attributes, if none were specified
        if (identifiers == null || identifiers.isEmpty()) {
            identifiers = new HashSet<String>();
            for (int column = 0; column < handle.getNumColumns(); column++) {
                identifiers.add(handle.getAttributeName(column));
            }
        }
        
        // Build column array
        int[] columns = getColumns(handle, identifiers);
        attributes = getAttributes(handle, columns);
        
        // Update progress
        progress.value = 10;
        checkInterrupt();
        
        // Do something
        SUDA2 suda2 = new SUDA2(handle.getDataArray(columns).getArray());
        suda2.setProgressListener(new SUDA2ListenerProgress() {
            @Override
            public void tick() {
                checkInterrupt();
            }
            @Override
            public void update(double progress) {
                RiskModelMSUScoreStatistics.this.progress.value = 10 + (int)(progress * 90d);
            }
            
        });

        // Calculate
        SUDA2StatisticsScores result = suda2.getStatisticsScores(maxKeyLength, sdcMicroScore);
        double samplingFraction = (double)result.getSUDAScores().length / (double)population.getPopulationSize();
        this.maxKeyLength = result.getMaxKeyLengthConsidered();
        this.maxScore = result.getHighestScore();
        this.averageScore = result.getAverageScore();
        this.scoresSUDA = getDistribution(result.getSUDAScores(), 0d, maxScore, 10);
        double[] scoresDIS = result.getDISScores(samplingFraction);
        
        // Compute distribution
        for (double score : scoresDIS) {
            int index = Arrays.binarySearch(thresholdsHigh, score);
            if (index < 0) {
                index = -index - 1;
            }
            this.recordsAtRisk[index] ++;
            checkInterrupt();
        }
        
        // Cumulative
        double cumulativeRisk = 0;
        for (int i=0; i<thresholdsHigh.length; i++) {
            cumulativeRisk += this.recordsAtRisk[i];
            this.recordsAtCumulativeRisk[i] = cumulativeRisk;
        }
        
        // Normalize
        for (int i=0; i<thresholdsHigh.length; i++) {
            this.recordsAtRisk[i] /= (double)scoresDIS.length;
            this.recordsAtCumulativeRisk[i] /= (double)scoresDIS.length;
        }
    }
    
    /**
     * Returns the attributes addressed by the statistics
     * @return
     */
    public String[] getAttributes() {
        return attributes;
    }

    /**
     * @return the averageScore
     */
    public double getAverageScore() {
        return averageScore;
    }

    /**
     * Returns the distribution of DIS scores in 10 buckets, ranging from 0 to 1.
     * @return the scores
     */
    public double[] getDistributionOfScoresDIS() {
        return recordsAtRisk;
    }

    /**
     * Returns the cumulative distribution of DIS scores in 10 buckets, ranging from 0 to 1.
     * @return the scores
     */
    public double[] getCumulativeDistributionOfScoresDIS() {
        return recordsAtCumulativeRisk;
    }

    /**
     * Returns lower thresholds for the buckets
     * @return
     */
    public double[] getDistributionOfScoresDISLowerThresholds() {
        return thresholdsLow;
    }

    /**
     * Returns upper thresholds for the buckets
     * @return
     */
    public double[] getDistributionOfScoresDISUpperThresholds() {
        return thresholdsHigh;
    }

    /**
     * Returns the distribution of SUDA scores in 100 buckets, ranging from 0 to highest score.
     * @return the scores
     */
    public double[] getDistributionOfScoresSUDA() {
        return scoresSUDA;
    }

    /**
     * @return the maxScore
     */
    public double getHighestScore() {
        return maxScore;
    }
    
    /**
     * Returns the maximal length of keys searched for
     * @return
     */
    public int getMaxKeyLengthConsidered() {
        return maxKeyLength;
    }

    /**
     * Checks for interrupts
     */
    private void checkInterrupt() {
        if (stop.value) { throw new ComputationInterruptedException(); }
    }

    /**
     * Returns the column array
     * @param handle
     * @param columns
     * @return
     */
    private String[] getAttributes(DataHandleInternal handle, int[] columns) {
        String[] result = new String[columns.length];
        int index = 0;
        for (int column : columns) {
            result[index++] = handle.getAttributeName(column);
        }
        return result;
    }

    /**
     * Returns the column array
     * @param handle
     * @param identifiers
     * @return The columns in ascending order
     */
    private int[] getColumns(DataHandleInternal handle, Set<String> identifiers) {
        int[] result = new int[identifiers.size()];
        int index = 0;
        for (String attribute : identifiers) {
            int column = handle.getColumnIndexOf(attribute);
            if (column == -1) {
                throw new IllegalArgumentException("Unknown attribute '" + attribute+"'");
            }
            result[index++] = column;
        }
        Arrays.sort(result);
        return result;
    }

    /**
     * Returns a distribution of array values into buckets ranging from min to max
     * @param values
     * @param min
     * @param max
     * @param numBuckets
     * @return
     */
    private double[] getDistribution(double[] values, double min, double max, int numBuckets) {
        double[] buckets = new double[numBuckets];
        for (double value : values) {
            int bucket = (int)Math.floor(((value - min) / (max - min)) * (double)numBuckets);
            bucket = bucket > buckets.length - 1 ? buckets.length - 1 : bucket;
            buckets[bucket]++;
            checkInterrupt();
        }
        for (int i=0; i<buckets.length; i++) {
            buckets[i] /= (double)values.length;
        }
        return buckets;
    }
}
