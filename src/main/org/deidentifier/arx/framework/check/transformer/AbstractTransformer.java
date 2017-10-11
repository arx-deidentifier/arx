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
            
            // TODO: Improve!
            int[][] values = new int[otherData.getNumColumns()][];
            int[][] frequencies = new int[otherData.getNumColumns()][];
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

            // TODO: Improve!
            int[][] values = new int[otherData.getNumColumns()][];
            int[][] frequencies = new int[otherData.getNumColumns()][];
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

            // TODO: Improve!
            int[][] values = new int[otherData.getNumColumns()][];
            int[][] frequencies = new int[otherData.getNumColumns()][];
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
    private HashGroupify                     groupify;
    
    /** The buffer. */
    protected DataMatrix                      buffer;
    
    /** The column index array. */
    protected final int[]                     columnIndexArray;
    
    /** The column map array. */
    protected final int[][][]                 columnMapArray;
    
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
    /** The idindex14. */
    protected int[][]                         idindex0, idindex1, idindex2, idindex3, idindex4, idindex5,
                                              idindex6, idindex7, idindex8, idindex9, idindex10, idindex11, idindex12, idindex13, idindex14;
    /** The index14. */
    protected int                             index0, index1, index2, index3, index4, index5, index6, index7,
                                              index8, index9, index10, index11, index12, index13, index14;
    
    /** The generalization hierarchies. */
    protected int[][][]                       map;
    /** The sensitive values. */
    protected final DataMatrix                otherData;
    /** The snapshot. */
    protected int[]                           snapshot;
    
    /** The size of one snapshopt entry *. */
    protected final int                       ssStepWidth;
    /** The start index. */
    protected int                             startIndex;
    
    /** The stateindices. */
    protected int                             generalizationindex0;
    
    /** The stateindices. */
    protected int                             generalizationindex1;
    
    /** The stateindices. */
    protected int                             generalizationindex10;
    
    /** The stateindices. */
    protected int                             generalizationindex11;
    
    /** The stateindices. */
    protected int                             generalizationindex12;
    
    /** The stateindices. */
    protected int                             generalizationindex13;
    
    /** The stateindices. */
    protected int                             generalizationindex14;
    
    /** The stateindices. */
    protected int                             generalizationindex2;
    
    /** The stateindices. */
    protected int                             generalizationindex3;
    
    /** The stateindices. */
    protected int                             generalizationindex4;
    
    /** The stateindices. */
    protected int                             generalizationindex5;
    
    /** The stateindices. */
    protected int                             generalizationindex6;
    
    /** The stateindices. */
    protected int                             generalizationindex7;
    
    /** The stateindices. */
    protected int                             generalizationindex8;
    
    /** The stateindices. */
    protected int                             generalizationindex9;
    /** The state index array. */
    protected final int[]                     generalizationIndexArray;
    
    /** The states. */
    protected int[]                           generalization;
    /** The stop index. */
    protected int                             stopIndex;
    /** The transition. */
    protected TransitionType                  transition;

    /**
     * Instantiates a new abstract transformer.
     *
     * @param data the data
     * @param hierarchies the hierarchies
     * @param otherData
     * @param dictionarySensValue
     * @param dictionarySensFreq
     * @param config
     */
    public AbstractTransformer(final DataMatrix data,
                               final GeneralizationHierarchy[] hierarchies,
                               final DataMatrix otherData,
                               final IntArrayDictionary dictionarySensValue,
                               final IntArrayDictionary dictionarySensFreq,
                               final ARXConfigurationInternal config) {
        this.config = config;
        this.data = data;
        this.hierarchies = hierarchies;
        this.otherData = otherData;
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;
        this.ssStepWidth = config.getSnapshotLength();

        // Init arrays
        this.dimensions = data.getNumColumns();
        int arraySizes = 15;
        if (this.dimensions > arraySizes) {
            arraySizes = this.dimensions;
        }
        this.generalizationIndexArray = new int[arraySizes];
        this.columnIndexArray = new int[arraySizes];
        this.columnMapArray = new int[arraySizes][][];
        this.map = new int[hierarchies.length][][];
        for (int i = 0; i < hierarchies.length; i++) {
            this.map[i] = hierarchies[i].getArray();
        }

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

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public HashGroupify call() {
        // clear local groupify
        groupify.stateClear();

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
                generalizationIndexArray[index] = state[i];
                columnIndexArray[index] = i;
                columnMapArray[index] = hierarchies[i].getArray();
                index++;
            }
        }

        // Store values
        this.index0 = columnIndexArray[0];
        this.index1 = columnIndexArray[1];
        this.index2 = columnIndexArray[2];
        this.index3 = columnIndexArray[3];
        this.index4 = columnIndexArray[4];
        this.index5 = columnIndexArray[5];
        this.index6 = columnIndexArray[6];
        this.index7 = columnIndexArray[7];
        this.index8 = columnIndexArray[8];
        this.index9 = columnIndexArray[9];
        this.index10 = columnIndexArray[10];
        this.index11 = columnIndexArray[11];
        this.index12 = columnIndexArray[12];
        this.index13 = columnIndexArray[13];
        this.index14 = columnIndexArray[14];

        // Store generalization levels
        this.generalizationindex0 = generalizationIndexArray[0];
        this.generalizationindex1 = generalizationIndexArray[1];
        this.generalizationindex2 = generalizationIndexArray[2];
        this.generalizationindex3 = generalizationIndexArray[3];
        this.generalizationindex4 = generalizationIndexArray[4];
        this.generalizationindex5 = generalizationIndexArray[5];
        this.generalizationindex6 = generalizationIndexArray[6];
        this.generalizationindex7 = generalizationIndexArray[7];
        this.generalizationindex8 = generalizationIndexArray[8];
        this.generalizationindex9 = generalizationIndexArray[9];
        this.generalizationindex10 = generalizationIndexArray[10];
        this.generalizationindex11 = generalizationIndexArray[11];
        this.generalizationindex12 = generalizationIndexArray[12];
        this.generalizationindex13 = generalizationIndexArray[13];
        this.generalizationindex14 = generalizationIndexArray[14];

        // Store generalization hierarchies
        this.idindex0 = columnMapArray[0];
        this.idindex1 = columnMapArray[1];
        this.idindex2 = columnMapArray[2];
        this.idindex3 = columnMapArray[3];
        this.idindex4 = columnMapArray[4];
        this.idindex5 = columnMapArray[5];
        this.idindex6 = columnMapArray[6];
        this.idindex7 = columnMapArray[7];
        this.idindex8 = columnMapArray[8];
        this.idindex9 = columnMapArray[9];
        this.idindex10 = columnMapArray[10];
        this.idindex11 = columnMapArray[11];
        this.idindex12 = columnMapArray[12];
        this.idindex13 = columnMapArray[13];
        this.idindex14 = columnMapArray[14];
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
