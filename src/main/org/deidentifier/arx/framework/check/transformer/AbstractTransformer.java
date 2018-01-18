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

package org.deidentifier.arx.framework.check.transformer;

import java.util.concurrent.Callable;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.TransformationCheckerStateMachine.TransitionType;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * This class implements an abstract base class for all transformers.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class AbstractTransformer implements Callable<HashGroupify> {

    /**
     * Implementation of the delegate for Requirements.COUNTER
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounter implements IGroupify {

        @Override
        public final void callAll(final int outtuple, final int i) {
            groupify.addFromBuffer(outtuple, -1, i, 1, -1);
        }

        @Override
        public final void callGroupify(final int outtuple, final HashGroupifyEntry element) {
            groupify.addFromGroupify(outtuple, null, element.representative, element.count, -1);
        }

        @Override
        public final void callSnapshot(final int outtuple, final int[] snapshot, final int i) {
            groupify.addFromSnapshot(outtuple, null, null, snapshot[i], snapshot[i + 1], -1);
        }
    }

    /**
     * Implementation of the delegate for Requirements.COUNTER | Requirements.DISTRIBUTION
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounterDistribution implements IGroupify {
        
        @Override
        public final void callAll(final int outtuple, final int i) {
            groupify.addFromBuffer(outtuple, i, i, 1, -1);
        }

        @Override
        public final void callGroupify(final int outtuple, final HashGroupifyEntry element) {
            groupify.addFromGroupify(outtuple, element.distributions, element.representative, element.count, -1);
        }

        @Override
        public final void callSnapshot(final int outtuple, final int[] snapshot, final int i) {
            
            int[][] values = new int[dataAnalyzedNumberOfColumns][];
            int[][] frequencies = new int[dataAnalyzedNumberOfColumns][];
            int index = 0;
            int offset = i + 2;
            int length = config.getSnapshotLength() - 1 - 2;
            for (int j = offset; j < offset + length; j += 2) {
                values[index] = dictionarySensValue.get(snapshot[j]);
                frequencies[index++] = dictionarySensFreq.get(snapshot[j + 1]);
            }
            
            groupify.addFromSnapshot(outtuple, values, frequencies, snapshot[i], snapshot[i + 1], -1);
        }
    }

    /**
     * Implementation of the delegate for Requirements.COUNTER | Requirements.SECONDARY_COUNTER
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounterSecondaryCounter implements IGroupify {
        
        @Override
        public final void callAll(final int outtuple, final int i) {
            groupify.addFromBuffer(outtuple, -1, i, 1, 1);
        }

        @Override
        public final void callGroupify(final int outtuple, final HashGroupifyEntry element) {
            groupify.addFromGroupify(outtuple, null, element.representative, element.count, element.pcount);
        }

        @Override
        public final void callSnapshot(final int outtuple, final int[] snapshot, final int i) {
            groupify.addFromSnapshot(outtuple, null, null, snapshot[i], snapshot[i + 1], snapshot[i + 2]);
        }
    }


    /**
     * Implementation of the delegate for Requirements.COUNTER | Requirements.SECONDARY_COUNTER | Requirements.DISTRIBUTION
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyCounterSecondaryCounterDistribution implements IGroupify {
        
        @Override
        public final void callAll(final int outtuple, final int i) {
            groupify.addFromBuffer(outtuple, i, i, 1, 1);
        }

        @Override
        public final void callGroupify(final int outtuple, final HashGroupifyEntry element) {
            groupify.addFromGroupify(outtuple, element.distributions, element.representative, element.count, element.pcount);
        }

        @Override
        public final void callSnapshot(final int outtuple, final int[] snapshot, final int i) {

            int[][] values = new int[dataAnalyzedNumberOfColumns][];
            int[][] frequencies = new int[dataAnalyzedNumberOfColumns][];
            int index = 0;
            int offset = i + 3;
            int length = config.getSnapshotLength() - 1 - 3;
            for (int j = offset; j < offset + length; j += 2) {
                values[index] = dictionarySensValue.get(snapshot[j]);
                frequencies[index++] = dictionarySensFreq.get(snapshot[j + 1]);
            }

            groupify.addFromSnapshot(outtuple, values, frequencies, snapshot[i], snapshot[i + 1], snapshot[i + 2]);
        }
    }


    /**
     * Implementation of the delegate for Requirements.DISTRIBUTION
     * @author Kohlmayer, Prasser
     */
    protected final class GroupifyDistribution implements IGroupify {
        
        @Override
        public final void callAll(final int outtuple, final int i) {
            groupify.addFromBuffer(outtuple, i, i, 1, -1);
        }

        @Override
        public final void callGroupify(final int outtuple, final HashGroupifyEntry element) {
            groupify.addFromGroupify(outtuple, element.distributions, element.representative, element.count, -1);
        }

        @Override
        public final void callSnapshot(final int outtuple, final int[] snapshot, final int i) {

            int[][] values = new int[dataAnalyzedNumberOfColumns][];
            int[][] frequencies = new int[dataAnalyzedNumberOfColumns][];
            int index = 0;
            int offset = i + 2;
            int length = config.getSnapshotLength() - 1 - 2;
            for (int j = offset; j < offset + length; j += 2) {
                values[index] = dictionarySensValue.get(snapshot[j]);
                frequencies[index++] = dictionarySensFreq.get(snapshot[j + 1]);
            }

            groupify.addFromSnapshot(outtuple, values, frequencies, snapshot[i], snapshot[i + 1], -1);
        }
    }

    /**
     * Interface for delegates to the groupify .
     *
     * @author Kohlmayer, Prasser
     */
    protected interface IGroupify {
        
        /**
         * Mode ALL.
         *
         * @param outtuple
         * @param i
         */
        public abstract void callAll(final int outtuple, final int i);

        /**
         * Mode GROUPIFY.
         *
         * @param outtuple
         * @param element
         */
        public abstract void callGroupify(final int outtuple, final HashGroupifyEntry element);

        /**
         * Mode SNAPSHOT.
         *
         * @param outtuple
         * @param snapshot
         * @param i
         */
        public abstract void callSnapshot(final int outtuple, final int[] snapshot, final int i);
    }

    /** The hash groupify. */
    private HashGroupify                      groupify;

    /** The buffer. */
    protected DataMatrix                      buffer;

    /** The mode of operation *. */
    protected final ARXConfigurationInternal  config;

    /** The data. */
    protected final DataMatrix                data;

    /** The delegate. */
    protected final IGroupify                 delegate;

    /** The dictionary for the snapshot compression *. */
    protected final IntArrayDictionary        dictionarySensFreq;

    /** The dictionary for the snapshot compression *. */
    protected final IntArrayDictionary        dictionarySensValue;

    /** The dimensions. */
    protected final int                       dimensions;

    /** The element. */
    protected HashGroupifyEntry               element;

    /** The hierarchies. */
    protected final GeneralizationHierarchy[] hierarchies;
    
    /** The hierarchies */
    protected int[][]                         hierarchy0, hierarchy1, hierarchy2, hierarchy3, hierarchy4, hierarchy5,
                                              hierarchy6, hierarchy7, hierarchy8, hierarchy9, hierarchy10, hierarchy11, hierarchy12, hierarchy13, hierarchy14;
    /** The columns. */
    protected int                             column0, column1, column2, column3, column4, column5, column6, column7,
                                              column8, column9, column10, column11, column12, column13, column14;

    /** The levels. */
    protected int                             level0, level1, level10, level11, level12, level13, level14, level2, level3, level4, level5, 
                                              level6, level7, level8, level9;

    /** The sensitive values. */
    protected final DataMatrix                dataAnalyzed;
    /** Analyzed number of columns. */
    protected final int                       dataAnalyzedNumberOfColumns;
    /** The snapshot. */
    protected int[]                           snapshot;

    /** The size of one snapshopt entry *. */
    protected final int                       ssStepWidth;

    /** The start index. */
    protected int                             startIndex;
    /** The stop index. */
    protected int                             stopIndex;

    /** The states. */
    protected int[]                           generalization;

    /** The transition. */
    protected TransitionType                  transition;

    /** The state index array. */
    protected final int[]                     mappedLevels;
    /** The column index array. */
    protected final int[]                     mappedColumns;
    /** The column map array. */
    protected final int[][][]                 mappedHierarchies;
    
    /**
     * Instantiates a new abstract transformer.
     *
     * @param data the data
     * @param hierarchies the hierarchies
     * @param dataAnalyzed
     * @param dataAnalyzedNumberOfColumns 
     * @param dictionarySensValue
     * @param dictionarySensFreq
     * @param config
     */
    public AbstractTransformer(final DataMatrix data,
                               final GeneralizationHierarchy[] hierarchies,
                               final DataMatrix dataAnalyzed,
                               int dataAnalyzedNumberOfColumns, 
                               final IntArrayDictionary dictionarySensValue,
                               final IntArrayDictionary dictionarySensFreq,
                               final ARXConfigurationInternal config) {
        this.config = config;
        this.data = data;
        this.hierarchies = hierarchies;
        this.dataAnalyzed = dataAnalyzed;
        this.dataAnalyzedNumberOfColumns = dataAnalyzedNumberOfColumns;
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;
        this.ssStepWidth = config.getSnapshotLength();

        // Init arrays
        this.dimensions = data.getNumColumns();
        int arraySizes = 15;
        if (this.dimensions > arraySizes) {
            arraySizes = this.dimensions;
        }
        this.mappedLevels = new int[arraySizes];
        this.mappedColumns = new int[arraySizes];
        this.mappedHierarchies = new int[arraySizes][][];

        // Prepare delegate
        switch (config.getRequirements()) {
        case ARXConfiguration.REQUIREMENT_COUNTER:
            delegate = new GroupifyCounter();
            break;
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER:
            delegate = new GroupifyCounterSecondaryCounter();
            break;
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            delegate = new GroupifyCounterSecondaryCounterDistribution();
            break;
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            delegate = new GroupifyCounterDistribution();
            break;
        case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            delegate = new GroupifyDistribution();
            break;
        default:
            RuntimeException e = new RuntimeException("Invalid requirements: " + config.getRequirements());
            throw(e);
        }
    }

    @Override
    public HashGroupify call() {
        
        // Clear local groupify
        groupify.stateClear();

        // Decide
        switch (transition) {
        case UNOPTIMIZED:
            processAll();
            break;
        case ROLLUP:
            processGroupify();
            break;
        case SNAPSHOT:
            processSnapshot();
            break;

        default:
            break;
        }
        return groupify;
    }

    /**
     * Prepares the next transformation
     * 
     * @param projection the projection
     * @param state the state
     * @param groupify the groupify
     * @param source the source
     * @param snapshot the snapshot
     * @param transition the transition
     * @param startIndex the start index
     * @param stopIndex the stop index
     * @param element the element
     * @param buffer the buffer
     */
    public void init(final long projection,
                     final int[] state,
                     final HashGroupify groupify,
                     final HashGroupify source,
                     final int[] snapshot,
                     final TransitionType transition,
                     final int startIndex,
                     final int stopIndex,
                     final HashGroupifyEntry element,
                     final DataMatrix buffer) {

        // Store data
        this.buffer = buffer;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.element = element;
        this.generalization = state;
        this.transition = transition;
        this.groupify = groupify;
        this.snapshot = snapshot;

        // Calculate mapping
        int index = 0;
        for (int i = 0; i < dimensions; i++) {
            if ((projection & (1L << i)) == 0) {
                mappedLevels[index] = state[i];
                mappedColumns[index] = i;
                mappedHierarchies[index] = hierarchies[i].getArray();
                index++;
            }
        }

        // Store values
        this.column0 = mappedColumns[0];
        this.column1 = mappedColumns[1];
        this.column2 = mappedColumns[2];
        this.column3 = mappedColumns[3];
        this.column4 = mappedColumns[4];
        this.column5 = mappedColumns[5];
        this.column6 = mappedColumns[6];
        this.column7 = mappedColumns[7];
        this.column8 = mappedColumns[8];
        this.column9 = mappedColumns[9];
        this.column10 = mappedColumns[10];
        this.column11 = mappedColumns[11];
        this.column12 = mappedColumns[12];
        this.column13 = mappedColumns[13];
        this.column14 = mappedColumns[14];

        // Store generalization levels
        this.level0 = mappedLevels[0];
        this.level1 = mappedLevels[1];
        this.level2 = mappedLevels[2];
        this.level3 = mappedLevels[3];
        this.level4 = mappedLevels[4];
        this.level5 = mappedLevels[5];
        this.level6 = mappedLevels[6];
        this.level7 = mappedLevels[7];
        this.level8 = mappedLevels[8];
        this.level9 = mappedLevels[9];
        this.level10 = mappedLevels[10];
        this.level11 = mappedLevels[11];
        this.level12 = mappedLevels[12];
        this.level13 = mappedLevels[13];
        this.level14 = mappedLevels[14];

        // Store generalization hierarchies
        this.hierarchy0 = mappedHierarchies[0];
        this.hierarchy1 = mappedHierarchies[1];
        this.hierarchy2 = mappedHierarchies[2];
        this.hierarchy3 = mappedHierarchies[3];
        this.hierarchy4 = mappedHierarchies[4];
        this.hierarchy5 = mappedHierarchies[5];
        this.hierarchy6 = mappedHierarchies[6];
        this.hierarchy7 = mappedHierarchies[7];
        this.hierarchy8 = mappedHierarchies[8];
        this.hierarchy9 = mappedHierarchies[9];
        this.hierarchy10 = mappedHierarchies[10];
        this.hierarchy11 = mappedHierarchies[11];
        this.hierarchy12 = mappedHierarchies[12];
        this.hierarchy13 = mappedHierarchies[13];
        this.hierarchy14 = mappedHierarchies[14];
    }

    /**
     * Process complete input dataset.
     */
    protected abstract void processAll();

    /**
     * Process groupify.
     */
    protected abstract void processGroupify();

    /**
     * Process snapshot.
     */
    protected abstract void processSnapshot();
}
