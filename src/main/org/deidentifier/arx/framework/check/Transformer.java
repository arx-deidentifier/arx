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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.StateMachine.TransitionType;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
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
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Transformer {

    /** The config. */
    private final ARXConfigurationInternal   config;

    /** The dictionary for the snapshot compression *. */
    private IntArrayDictionary               dictionarySensFreq;

    /** The dictionary for the snapshot compression *. */
    private IntArrayDictionary               dictionarySensValue;

    /** The dimensions. */
    private final int                        dimensions;

    /** The hierarchies. */
    private final GeneralizationHierarchy[]  hierarchies;

    /** Other attribute values. */
    private int[][]                          inputAnalyzed;

    /** The data. */
    private final int[][]                    inputGeneralized;

    /** The instances. */
    private final AbstractTransformer[]      transformers;

    /** The buffer. */
    private int[][]                          outputGeneralized;

    long transformTime = 0;
    
    /**
     * Instantiates a new transformer.
     *
     * @param inputGeneralized
     * @param inputAnalyzed
     * @param hierarchies
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public Transformer(final int[][] inputGeneralized,
                       final int[][] inputAnalyzed,
                       final GeneralizationHierarchy[] hierarchies,
                       final ARXConfigurationInternal config,
                       final IntArrayDictionary dictionarySensValue,
                       final IntArrayDictionary dictionarySensFreq) {

        this.config = config;
        this.inputGeneralized = inputGeneralized;
        this.hierarchies = hierarchies;
        this.outputGeneralized = new int[inputGeneralized.length][];
        this.transformers = createTransformers();
        
        for (int i = 0; i < inputGeneralized.length; i++) {
            outputGeneralized[i] = new int[inputGeneralized[0].length];
        }

        this.dimensions = inputGeneralized[0].length;
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;
        this.inputAnalyzed = inputAnalyzed;
    }

    /**
     * Apply.
     * 
     * @param projection
     *            the projection
     * @param transformation
     *            the transformation
     * @param target
     *            the target
     */
    public void apply(final long projection,
                               final int[] transformation,
                               final HashGroupify target) {
         applyInternal(projection,
                             transformation,
                             null,
                             target,
                             null,
                             TransitionType.UNOPTIMIZED);
    }

    /**
     * Apply rollup.
     * 
     * @param projection
     *            the projection
     * @param state
     *            the state
     * @param source
     *            the source
     * @param target
     *            the target
     */
    public void applyRollup(final long projection,
                                     final int[] state,
                                     final HashGroupify source,
                                     final HashGroupify target) {
         applyInternal(projection,
                             state,
                             source,
                             target,
                             null,
                             TransitionType.ROLLUP);
    }
    
    /**
     * Apply snapshot.
     * 
     * @param projection
     *            the projection
     * @param state
     *            the state
     * @param target
     *            the target
     * @param snapshot
     *            the snapshot
     */
    public void applySnapshot(final long projection,
                                       final int[] state,
                                       final HashGroupify target,
                                       final int[] snapshot) {
         applyInternal(projection,
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
    public int[][] getBuffer() {
        return outputGeneralized;
    }
    
    public void print() {
        System.out.println("Time transform: " + transformTime);
    }

    /**
     * Shutdown
     */
    public void shutdown() {
        // Nothing to do
    }
    
    /**
     * Apply internal.
     * 
     * @param projection
     *            the projection
     * @param transformation
     *            the state
     * @param source
     *            the source
     * @param target
     *            the target
     * @param snapshot
     *            the snapshot
     * @param transition
     *            the transition
     * @return the hash groupify
     */
    protected void applyInternal(final long projection,
                                 final int[] transformation,
                                 final HashGroupify source,
                                 final HashGroupify target,
                                 final int[] snapshot,
                                 final TransitionType transition) {

        long time = System.currentTimeMillis();
        
        int startIndex = 0;
        int stopIndex = 0;
        switch (transition) {
        case UNOPTIMIZED:
            stopIndex = inputGeneralized.length;
            break;
        case ROLLUP:
            stopIndex = source.getNumberOfEquivalenceClasses();
            break;
        case SNAPSHOT:
            stopIndex = snapshot.length /
                        config.getSnapshotLength();
            break;
        }

        AbstractTransformer app = null;

        app = getTransformer(projection, this.transformers);
        
        app.init(projection,
                 transformation,
                 target,
                 source,
                 snapshot,
                 transition,
                 startIndex,
                 stopIndex);

        app.call();
        
        this.transformTime += (System.currentTimeMillis() - time);
    }
    
    /**
     * Builds the transformers.
     */
    protected AbstractTransformer[] createTransformers() {
        AbstractTransformer[] result = new AbstractTransformer[16];
        result[15] = new Transformer15(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        result[14] = new Transformer14(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        result[13] = new Transformer13(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        result[12] = new Transformer12(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        result[11] = new Transformer11(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        result[10] = new Transformer10(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        result[9] = new Transformer09(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[8] = new Transformer08(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[7] = new Transformer07(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[6] = new Transformer06(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[5] = new Transformer05(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[4] = new Transformer04(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[3] = new Transformer03(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[2] = new Transformer02(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[1] = new Transformer01(inputGeneralized,
                                      outputGeneralized,
                                      hierarchies,
                                      inputAnalyzed,
                                      dictionarySensValue,
                                      dictionarySensFreq,
                                      config);
        result[0] = new TransformerAll(inputGeneralized,
                                       outputGeneralized,
                                       hierarchies,
                                       inputAnalyzed,
                                       dictionarySensValue,
                                       dictionarySensFreq,
                                       config);
        return result;
    }

    /**
     * Data length
     * @return
     */
    protected int getDataLength() {
        return this.inputGeneralized.length;
    }
    
    /**
     * Snapshot length
     * @return
     */
    protected int getSnapshotLength() {
        return this.config.getSnapshotLength();
    }

    /**
     * Gets the applicator.
     * 
     * @param projection the projection
     * @param transformers the transformers
     * @return the applicator
     */
    protected AbstractTransformer getTransformer(long projection, AbstractTransformer[] transformers) {
        int index = dimensions - Long.bitCount(projection);
        if (index > (transformers.length - 1)) {
            return transformers[0];
        } else {
            return transformers[index];
        }
    }

    /**
     * For multithreaded implementation
     * @return
     */
    protected AbstractTransformer[] getTransformers() {
        return this.transformers;
    }
}
