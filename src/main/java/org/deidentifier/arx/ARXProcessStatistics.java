/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
package org.deidentifier.arx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXAnonymizer.Result;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.v2.QualityMetadata;

import de.linearbits.jhpl.JHPLIterator.LongIterator;

/**
 * Statistics about the anonymization process for output data
 * 
 * @author Fabian Prasser
 */
public class ARXProcessStatistics implements Serializable {
    
    /**
     * One individual anonymization step
     * 
     * @author Fabian Prasser
     */
    public static class Step implements Serializable {

        /** SVUID */
        private static final long        serialVersionUID      = -7432752645871431439L;

        /** Transformation selected in this step */
        private int[]                    transformation;

        /** Top transformation available in this step */
        private int[]                    top;

        /** Maps attributes to indices */
        private Map<String, Integer>     headermap;

        /** Information loss at this step */
        private InformationLoss<?>       score;

        /** Number of records transformed in this step */
        private int                      numRecordsTransformed = -1;

        /** Is the solution optimal */
        private boolean                  optimal;

        /**
         * Creates a new instance
         * @param optimum
         * @param isOptimal
         */
        public Step(ARXNode top, ARXNode optimum, boolean isOptimal) {
            this.transformation = optimum.getTransformation();
            this.top = top.getTransformation();
            this.headermap = optimum.getHeaderMap();
            this.score = optimum.getHighestScore();
            this.optimal = isOptimal;
            this.numRecordsTransformed = -1;
        }

        /**
         * Creates a new instance
         * @param headermap
         * @param top
         * @param optimum
         * @param isOptimum
         * @param numRecords
         */
        public Step(Transformation top, Transformation optimum, boolean isOptimum, int numRecords) {
            this.transformation = optimum.getGeneralization();
            this.top = top.getGeneralization();
            this.score = optimum.getInformationLoss();
            this.optimal = isOptimum;
            this.numRecordsTransformed = numRecords;
        }

        /**
         * Creates a new instance
         * @param transformation
         * @param top
         * @param headermap
         * @param score
         * @param isOptimum
         */
        protected Step(int[] transformation,
                       int[] top,
                       Map<String, Integer> headermap,
                       InformationLoss<?> score,
                       int numRecordsTransformed,
                       boolean isOptimum) {
            this.transformation = transformation;
            this.top = top;
            this.headermap = headermap;
            this.score = score;
            this.numRecordsTransformed = numRecordsTransformed;
            this.optimal = isOptimum;
        }

        @Override
        public Step clone() {
            return new Step(transformation, top, headermap, score, numRecordsTransformed, optimal);
        }
        
        /**
         * Returns the index of an attribute.
         *
         * @param attribute
         * @return
         */
        public int getDimension(final String attribute) {
            return headermap.get(attribute);
        }

        /**
         * Returns the generalization for the attribute.
         *
         * @param attribute
         * @return
         */
        public int getGeneralization(final String attribute) {
            final Integer index = headermap.get(attribute);
            if (index == null) { return 0; }
            return transformation[index];
        }

        /**
         * Maps attribute name to position
         * @return
         */
        public Map<String, Integer> getHeader() {
            return this.headermap;
        }

        /**
         * Returns the maximal generalization level for the attribute.
         *
         * @param attribute
         * @return
         */
        public int getMaximalGeneralization(final String attribute) {
            final Integer index = headermap.get(attribute);
            if (index == null) { return 0; }
            return top[index];
        }

        /**
         * Returns the transformation as an array.
         *
         * @return
         */
        public int[] getMaximalTransformationLevels() {
            return top;
        }

        /**
         * Returns the metadata associated with the result, if any
         * @return the metadata
         */
        public List<QualityMetadata<?>> getMetadata() {
            return score.getMetadata();
        }
        
        /**
         * Returns the number of records transformed in this step, <code>-1</code>
         * if not known.
         * @return the numRecordsTransformed
         */
        public int getNumberOfRecordsTransformed() {
            return this.numRecordsTransformed;
        }

        /**
         * Returns the quasi identifiers.
         *
         * @return
         */
        public String[] getQuasiIdentifyingAttributes() {
            final String[] result = new String[headermap.size()];
            for (final String key : headermap.keySet()) {
                result[headermap.get(key)] = key;
            }
            return result;
        }
        
        /**
         * Returns a node's lower bound, if any.
         *
         * @return
         */
        public InformationLoss<?> getScore(){
            return this.score;
        }

        /**
         * Returns the sum of all generalization levels.
         *
         * @return
         */
        public int getTotalGeneralizationLevel() {
            int level = 0;
            for (int i : transformation) {
                level += i;
            }
            return level;
        }
        
        /**
         * Returns the transformation as an array.
         *
         * @return
         */
        public int[] getTransformation() {
            return this.transformation;
        }

        /**
         * Returns whether it is known how many records have been transformed
         * in this step.
         * @return
         */
        public boolean isNumberOfRecordsTransformedAvailable() {
            return this.numRecordsTransformed != -1;
        }

        /**
         * Returns whether the solution in this step is optimal
         * @return
         */
        public boolean isOptimal() {
            return optimal;
        }
    }

    /** SVUID */
    private static final long serialVersionUID       = -7984648262848553971L;

    /** List of steps performed */
    private List<Step>        steps                  = new ArrayList<Step>();

    /** Total transformations available in this step */
    private long              transformationsTotal;

    /** Transformations checked in this step */
    private long              transformationsChecked;

    /** If known */
    private int               initialNumberOfRecords = -1;

    /** Duration */
    private long              duration;

    /**
     * Clone constructor
     * @param other
     */
    private ARXProcessStatistics(ARXProcessStatistics other) {
        this.transformationsChecked = other.transformationsChecked;
        this.transformationsTotal = other.transformationsTotal;
        this.duration = other.duration;
        this.initialNumberOfRecords = other.initialNumberOfRecords;
        this.steps = new ArrayList<>();
        for (Step step : other.steps) {
            this.steps.add(step.clone());
        }
    }
    
    /**
     * Creates an instance representing the fact that no optimization has been performed
     */
    protected ARXProcessStatistics() {
        // Empty by design
    }

    /**
     * Creates an instance for the global anonymization step
     * @param lattice
     * @param optimum
     * @param isOptimal
     * @param duration
     */
    protected ARXProcessStatistics(ARXLattice lattice, ARXNode optimum, boolean isOptimal, long duration) {
        
        // Add step
        if (optimum != null) {
            this.steps.add(new Step(lattice.getTop(), optimum, isOptimal));
        }

        // Compute statistics
        this.duration += duration;
        this.transformationsTotal += lattice.getVirtualSize();        
        for (final ARXNode[] level : lattice.getLevels()) {
            for (final ARXNode node : level) {
                if (node.isChecked() || node.getHighestScore().compareTo(node.getLowestScore()) == 0) {
                    transformationsChecked++;
                }
            }
        }
    }

    /**
     * Creates an instance for an individual optimization step
     * @param result
     * @param initialNumberOfRecords
     * @param numRecords
     * @param duration
     */
    protected ARXProcessStatistics(Result result, int initialNumberOfRecords, int numRecords, long duration) {

        // Build header map
        Map<String, Integer> headermap = new HashMap<String, Integer>();
        String[] header = result.checker.getHeader();
        int index = 0;
        for (int i = 0; i < header.length; i++) {
            headermap.put(header[i], index++);
        }
        
        // Add step
        if (result.optimum != null) {
            this.steps.add(new Step(result.solutionSpace.getTop(),
                                    result.optimum,
                                    result.optimumFound,
                                    numRecords));
        }
        
        // Compute statistics
        this.initialNumberOfRecords = initialNumberOfRecords;
        this.transformationsTotal += result.solutionSpace.getSize();
        this.duration += duration;
        
        // Collect number of checked transformations
        for (LongIterator iterator = result.solutionSpace.getMaterializedTransformations(); iterator.hasNext();) {
            Transformation transformation = result.solutionSpace.getTransformation(iterator.next());
            if (transformation.hasProperty(result.solutionSpace.getPropertyChecked()) || (transformation.getInformationLoss() != null)) {
                this.transformationsChecked++;
            }
        }
    }

    @Override
    public ARXProcessStatistics clone() {
        return new ARXProcessStatistics(this);
    }
    
    /**
     * Returns the duration of the process
     * @return
     */
    public long getDuration() {
        return this.duration;
    }
    
    /**
     * Converts the statistics into a lattice
     * @return
     */
    public ARXLattice getLattice() {
        return new ARXLattice(this);
    }
    
    /**
     * Returns the number of steps performed
     * @return
     */
    public int getNumberOfSteps() {
        return steps.size();
    }

    /**
     * Returns a step performed during the anonymization process
     * @param index
     * @return
     */
    public Step getStep(int index) {
        if (index > steps.size() - 1) {
            throw new IndexOutOfBoundsException("Step " + index + " is not available");
        } else {
            return steps.get(index);
        }
    }
    
    /**
     * Returns all steps
     * @return
     */
    public List<Step> getSteps() {
        return this.steps;
    }

    /**
     * Returns the number of transformations available in this process
     * @return
     */
    public long getTransformationsAvailable() {
        return this.transformationsTotal;
    }
    
    /**
     * Returns the number of transformations checked in this process
     * @return
     */
    public long getTransformationsChecked() {
        return this.transformationsChecked;
    }
    
    /**
     * Returns whether the result is a local transformation scheme
     * @return
     */
    public boolean isLocalTransformation() {
        return this.getNumberOfSteps() > 1;
    }

    /**
     * Returns whether optimization has been performed
     * @return
     */
    public boolean isSolutationAvailable() {
        return !this.steps.isEmpty();
    }

    /**
     * Returns new process statistics that are a merger of this and the other statistics
     * @param statistics
     */
    public ARXProcessStatistics merge(ARXProcessStatistics statistics) {
        ARXProcessStatistics result = this.clone();
        result.mergeInternal(statistics);
        return result;
    }

    /**
     * Merges this instance with the other instance
     * @param stats
     */
    private void mergeInternal(ARXProcessStatistics stats) {
        if (this.initialNumberOfRecords == -1 && stats.initialNumberOfRecords != -1) {
            this.initialNumberOfRecords = stats.initialNumberOfRecords;
        }
        for (Step step : stats.steps) {
            step = step.clone();
            if (!this.steps.isEmpty()) {
                step.headermap = steps.get(0).headermap;
            }
            this.steps.add(step);
        }
        if (!this.steps.isEmpty() && !this.steps.get(0).isNumberOfRecordsTransformedAvailable()) {
            this.steps.get(0).numRecordsTransformed = this.initialNumberOfRecords;
        }
        this.transformationsTotal += stats.transformationsTotal;
        this.transformationsChecked += stats.transformationsChecked;
        this.duration += stats.duration;
    }
}