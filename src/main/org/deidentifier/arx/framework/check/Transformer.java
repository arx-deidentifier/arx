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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.TransformationCheckerStateMachine.TransitionType;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.transformer.AbstractTransformer;
import org.deidentifier.arx.framework.check.transformer.Transformer01;
import org.deidentifier.arx.framework.check.transformer.Transformer02;
import org.deidentifier.arx.framework.check.transformer.Transformer03;
import org.deidentifier.arx.framework.check.transformer.Transformer04;
import org.deidentifier.arx.framework.check.transformer.Transformer05;
import org.deidentifier.arx.framework.check.transformer.Transformer06;
import org.deidentifier.arx.framework.check.transformer.Transformer07;
import org.deidentifier.arx.framework.check.transformer.Transformer08;
import org.deidentifier.arx.framework.check.transformer.Transformer09;
import org.deidentifier.arx.framework.check.transformer.Transformer10;
import org.deidentifier.arx.framework.check.transformer.Transformer11;
import org.deidentifier.arx.framework.check.transformer.Transformer12;
import org.deidentifier.arx.framework.check.transformer.Transformer13;
import org.deidentifier.arx.framework.check.transformer.Transformer14;
import org.deidentifier.arx.framework.check.transformer.Transformer15;
import org.deidentifier.arx.framework.check.transformer.TransformerAll;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Transformer {

    /** The config. */
    protected final ARXConfigurationInternal  config;

    /** The dictionary for the snapshot compression *. */
    protected IntArrayDictionary              dictionarySensFreq;

    /** The dictionary for the snapshot compression *. */
    protected IntArrayDictionary              dictionarySensValue;

    /** The dimensions. */
    protected final int                       dimensions;

    /** The hierarchies. */
    protected final GeneralizationHierarchy[] hierarchies;

    /** Other attribute values. */
    protected DataMatrix                      inputAnalyzed;

    /** The data. */
    protected final DataMatrix                inputGeneralized;

    /** The instances. */
    protected final AbstractTransformer[]     instances;

    /** The buffer. */
    protected DataMatrix                      outputGeneralized;

    /** Number of columns to analyze */
    protected final int                       dataAnalyzedNumberOfColumns;

    /**
     * Instantiates a new transformer.
     *
     * @param inputGeneralized
     * @param inputAnalyzed
     * @param dataAnalyzedNumberOfColumns
     * @param hierarchies
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public Transformer(final DataMatrix inputGeneralized,
                       final DataMatrix inputAnalyzed,
                       final int dataAnalyzedNumberOfColumns,
                       final GeneralizationHierarchy[] hierarchies,
                       final ARXConfigurationInternal config,
                       final IntArrayDictionary dictionarySensValue,
                       final IntArrayDictionary dictionarySensFreq) {

        this.config = config;
        this.inputGeneralized = inputGeneralized;
        this.dataAnalyzedNumberOfColumns = dataAnalyzedNumberOfColumns;
        this.hierarchies = hierarchies;
        this.instances = new AbstractTransformer[16];
        this.outputGeneralized = new DataMatrix(inputGeneralized.getNumRows(), 
                                                inputGeneralized.getNumColumns());

        this.dimensions = inputGeneralized.getNumColumns();
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;
        this.inputAnalyzed = inputAnalyzed;

        buildTransformers();
    }

    /**
     * Instantiates a new transformer for application purposes
     *
     * @param inputGeneralized
     * @param inputAnalyzed
     * @param dataAnalyzedNumberOfColumns
     * @param hierarchies
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public Transformer(final DataMatrix inputGeneralized,
                       final DataMatrix inputAnalyzed,
                       final DataMatrix outputGeneralized,
                       final int dataAnalyzedNumberOfColumns,
                       final GeneralizationHierarchy[] hierarchies,
                       final ARXConfigurationInternal config) {

        this.inputGeneralized = inputGeneralized;
        this.outputGeneralized = outputGeneralized;
        
        this.inputAnalyzed = inputAnalyzed;
        this.dataAnalyzedNumberOfColumns = dataAnalyzedNumberOfColumns;

        this.config = config;
        this.hierarchies = hierarchies;

        this.dimensions = inputGeneralized.getNumColumns();
        this.dictionarySensValue = null;
        this.dictionarySensFreq = null;

        // Build just one applicator
        this.instances = new AbstractTransformer[1];
        this.instances[0] = new TransformerAll(inputGeneralized,
                                               hierarchies,
                                               inputAnalyzed,
                                               dataAnalyzedNumberOfColumns,
                                               dictionarySensValue,
                                               dictionarySensFreq,
                                               config);
    }

    /**
     * Apply.
     * 
     * @param projection the projection
     * @param transformation the transformation
     * @param target the target
     * @return the hash groupify
     */
    public HashGroupify apply(final long projection,
                               final int[] transformation,
                               final HashGroupify target) {
        
        return applyInternal(projection,
                             transformation,
                             null,
                             target,
                             null,
                             TransitionType.UNOPTIMIZED);
    }

    /**
     * Apply rollup.
     * 
     * @param projection the projection
     * @param state the state
     * @param source the source
     * @param target the target
     * @return the hash groupify
     */
    public HashGroupify applyRollup(final long projection,
                                     final int[] state,
                                     final HashGroupify source,
                                     final HashGroupify target) {
        
        return applyInternal(projection,
                             state,
                             source,
                             target,
                             null,
                             TransitionType.ROLLUP);
    }

    /**
     * Apply snapshot.
     * 
     * @param projection the projection
     * @param state the state
     * @param target the target
     * @param snapshot the snapshot
     * @return the hash groupify
     */
    public HashGroupify applySnapshot(final long projection,
                                       final int[] state,
                                       final HashGroupify target,
                                       final int[] snapshot) {
        
        return applyInternal(projection,
                             state,
                             null,
                             target,
                             snapshot,
                             TransitionType.SNAPSHOT);
    }

    /**
     * Gets the buffer.
     * 
     * @return the buffer
     */
    public DataMatrix getBuffer() {
        return outputGeneralized;
    }

    /**
     * Builds the applicators.
     */
    private void buildTransformers() {
        instances[15] = new Transformer15(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[14] = new Transformer14(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[13] = new Transformer13(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[12] = new Transformer12(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[11] = new Transformer11(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[10] = new Transformer10(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[9] = new Transformer09(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[8] = new Transformer08(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[7] = new Transformer07(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[6] = new Transformer06(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[5] = new Transformer05(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[4] = new Transformer04(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[3] = new Transformer03(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[2] = new Transformer02(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[1] = new Transformer01(inputGeneralized,
                                         hierarchies,
                                         inputAnalyzed,
                                         dataAnalyzedNumberOfColumns,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[0] = new TransformerAll(inputGeneralized,
                                          hierarchies,
                                          inputAnalyzed,
                                          dataAnalyzedNumberOfColumns,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
    }

    /**
     * Apply internal.
     * 
     * @param projection the projection
     * @param state the state
     * @param source the source
     * @param target the target
     * @param snapshot the snapshot
     * @param transition the transition
     * @return the hash groupify
     */
    protected HashGroupify applyInternal(final long projection,
                                          final int[] state,
                                          final HashGroupify source,
                                          final HashGroupify target,
                                          final int[] snapshot,
                                          final TransitionType transition) {

        int startIndex = 0;
        int stopIndex = 0;

        HashGroupifyEntry element = null;

        switch (transition) {
        case UNOPTIMIZED:
            startIndex = 0;
            stopIndex = inputGeneralized.getNumRows();
            break;
        case ROLLUP:
            startIndex = 0;
            stopIndex = source.getNumberOfEquivalenceClasses();
            element = source.getFirstEquivalenceClass();
            break;
        case SNAPSHOT:
            startIndex = 0;
            stopIndex = snapshot.length / config.getSnapshotLength();
            break;
        }

        AbstractTransformer transformer = null;

        transformer = getTransformer(projection);
        
        transformer.init(projection,
                 state,
                 target,
                 source,
                 snapshot,
                 transition,
                 startIndex,
                 stopIndex,
                 element,
                 outputGeneralized);

        return transformer.call();
    }

    /**
     * Gets the applicator.
     * 
     * @param projection the projection
     * @return the applicator
     */
    protected AbstractTransformer getTransformer(final long projection) {
        final int index = dimensions - Long.bitCount(projection);
        if (index > (instances.length - 1)) {
            return instances[0];
        } else {
            return instances[index];
        }
    }
}
