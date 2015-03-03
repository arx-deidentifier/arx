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
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
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

    /** The buffer. */
    protected int[][]                          buffer;

    /** Sensitive attribute values. */
    protected int[][]                          sensitive;

    /** The config. */
    protected final ARXConfigurationInternal config;

    /** The data. */
    protected final int[][]                    data;

    /** The dictionary for the snapshot compression *. */
    protected IntArrayDictionary               dictionarySensFreq;

    /** The dictionary for the snapshot compression *. */
    protected IntArrayDictionary               dictionarySensValue;

    /** The dimensions. */
    protected final int                        dimensions;

    /** The hierarchies. */
    protected final GeneralizationHierarchy[]  hierarchies;

    /** The instances. */
    protected final AbstractTransformer[]      instances;

    /**
     * Instantiates a new transformer.
     *
     * @param data
     * @param hierarchies
     * @param sensitive
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public Transformer(final int[][] data,
                       final GeneralizationHierarchy[] hierarchies,
                       final int[][] sensitive,
                       final ARXConfigurationInternal config,
                       final IntArrayDictionary dictionarySensValue,
                       final IntArrayDictionary dictionarySensFreq) {

        this.config = config;
        this.data = data;
        this.hierarchies = hierarchies;
        this.instances = new AbstractTransformer[16];
        this.buffer = new int[data.length][];
        
        for (int i = 0; i < data.length; i++) {
            buffer[i] = new int[data[0].length];
        }

        this.dimensions = data[0].length;
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;
        this.sensitive = sensitive;

        buildApplicators();
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
     * @return the hash groupify
     */
    public IHashGroupify apply(final long projection,
                               final int[] transformation,
                               final IHashGroupify target) {
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
     * @param projection
     *            the projection
     * @param state
     *            the state
     * @param source
     *            the source
     * @param target
     *            the target
     * @return the hash groupify
     */
    public IHashGroupify applyRollup(final long projection,
                                     final int[] state,
                                     final IHashGroupify source,
                                     final IHashGroupify target) {
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
     * @param projection
     *            the projection
     * @param state
     *            the state
     * @param target
     *            the target
     * @param snapshot
     *            the snapshot
     * @return the hash groupify
     */
    public IHashGroupify applySnapshot(final long projection,
                                       final int[] state,
                                       final IHashGroupify target,
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
    public int[][] getBuffer() {
        return buffer;
    }

    /**
     * Builds the applicators.
     */
    private void buildApplicators() {
        instances[15] = new Transformer15(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[14] = new Transformer14(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[13] = new Transformer13(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[12] = new Transformer12(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[11] = new Transformer11(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[10] = new Transformer10(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[9] = new Transformer09(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[8] = new Transformer08(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[7] = new Transformer07(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[6] = new Transformer06(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[5] = new Transformer05(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[4] = new Transformer04(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[3] = new Transformer03(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[2] = new Transformer02(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[1] = new Transformer01(data,
                                         hierarchies,
                                         sensitive,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[0] = new TransformerAll(data,
                                          hierarchies,
                                          sensitive,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
    }

    /**
     * Apply internal.
     * 
     * @param projection
     *            the projection
     * @param state
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
    protected IHashGroupify applyInternal(final long projection,
                                          final int[] state,
                                          final IHashGroupify source,
                                          final IHashGroupify target,
                                          final int[] snapshot,
                                          final TransitionType transition) {

        int startIndex = 0;
        int stopIndex = 0;

        int bucket = 0;
        HashGroupifyEntry element = null;

        switch (transition) {
        case UNOPTIMIZED:
            startIndex = 0;
            stopIndex = data.length;
            break;
        case ROLLUP:
            startIndex = 0;
            stopIndex = source.size();
            bucket = 0;
            element = source.getFirstEntry();
            break;
        case SNAPSHOT:
            startIndex = 0;
            stopIndex = snapshot.length /
                        config.getSnapshotLength();
            break;
        }

        AbstractTransformer app = null;

        app = getApplicator(projection);
        
        app.init(projection,
                 state,
                 target,
                 source,
                 snapshot,
                 transition,
                 startIndex,
                 stopIndex,
                 bucket,
                 element,
                 buffer);

        return app.call();
    }

    /**
     * Gets the applicator.
     * 
     * @param projection the projection
     * @return the applicator
     */
    protected AbstractTransformer getApplicator(final long projection) {
        final int index = dimensions - Long.bitCount(projection);
        if (index > (instances.length - 1)) {
            return instances[0];
        } else {
            return instances[index];
        }
    }
}
