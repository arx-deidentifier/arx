/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.framework.check;

import org.deidentifier.flash.framework.Configuration;
import org.deidentifier.flash.framework.check.StateMachine.TransitionType;
import org.deidentifier.flash.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.flash.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.check.transformer.AbstractTransformer;
import org.deidentifier.flash.framework.check.transformer.Transformer01;
import org.deidentifier.flash.framework.check.transformer.Transformer02;
import org.deidentifier.flash.framework.check.transformer.Transformer03;
import org.deidentifier.flash.framework.check.transformer.Transformer04;
import org.deidentifier.flash.framework.check.transformer.Transformer05;
import org.deidentifier.flash.framework.check.transformer.Transformer06;
import org.deidentifier.flash.framework.check.transformer.Transformer07;
import org.deidentifier.flash.framework.check.transformer.Transformer08;
import org.deidentifier.flash.framework.check.transformer.Transformer09;
import org.deidentifier.flash.framework.check.transformer.Transformer10;
import org.deidentifier.flash.framework.check.transformer.Transformer11;
import org.deidentifier.flash.framework.check.transformer.Transformer12;
import org.deidentifier.flash.framework.check.transformer.Transformer13;
import org.deidentifier.flash.framework.check.transformer.Transformer14;
import org.deidentifier.flash.framework.check.transformer.Transformer15;
import org.deidentifier.flash.framework.check.transformer.TransformerAll;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer.
 * 
 * @author Prasser, Kohlmayer
 */
public class Transformer {

    /** The buffer. */
    protected int[][]                         buffer;

    /** The data. */
    protected final int[][]                   data;

    protected int[]                           sensitiveValues;

    /** The dimensions. */
    protected final int                       dimensions;

    /** The hierarchies. */
    protected final GeneralizationHierarchy[] hierarchies;

    /** The instances. */
    protected final AbstractTransformer[]     instances;
    /** The dictionary for the snapshot compression **/
    protected IntArrayDictionary              dictionarySensValue;

    /** The dictionary for the snapshot compression **/
    protected IntArrayDictionary              dictionarySensFreq;

    protected final Configuration             config;

    /**
     * Instantiates a new transformer.
     * 
     * @param data
     * @param hierarchies
     * @param sensitiveData
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public Transformer(final int[][] data,
                       final GeneralizationHierarchy[] hierarchies,
                       final int[][] sensitiveData,
                       final Configuration config,
                       final IntArrayDictionary dictionarySensValue,
                       final IntArrayDictionary dictionarySensFreq) {

        this.config = config;
        this.data = data;
        this.hierarchies = hierarchies;
        instances = new AbstractTransformer[16];
        // init buffer
        buffer = new int[data.length][];
        for (int i = 0; i < data.length; i++) {
            buffer[i] = new int[data[0].length];
        }

        dimensions = data[0].length;

        sensitiveValues = new int[data.length];
        this.dictionarySensValue = dictionarySensValue;
        this.dictionarySensFreq = dictionarySensFreq;

        if ((sensitiveData != null) && (sensitiveData.length == data.length) &&
            (sensitiveData[0].length > 0)) {
            for (int i = 0; i < sensitiveValues.length; i++) { // BEWARE: only
                                                               // the first
                                                               // sensitive
                                                               // value is used
                sensitiveValues[i] = sensitiveData[i][0];
            }
        }

        buildApplicators();

    }

    /**
     * Apply.
     * 
     * @param projection
     *            the projection
     * @param state
     *            the state
     * @param target
     *            the target
     * @return the hash groupify
     */
    public IHashGroupify apply(final long projection,
                               final int[] state,
                               final IHashGroupify target) {
        return applyInternal(projection,
                             state,
                             null,
                             target,
                             null,
                             TransitionType.UNOPTIMIZED);
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
                        config.getCriterionSpecificSnapshotLength();
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
     * Builds the applicators.
     */
    private void buildApplicators() {
        instances[15] = new Transformer15(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[14] = new Transformer14(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[13] = new Transformer13(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[12] = new Transformer12(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[11] = new Transformer11(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[10] = new Transformer10(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
        instances[9] = new Transformer09(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[8] = new Transformer08(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[7] = new Transformer07(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[6] = new Transformer06(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[5] = new Transformer05(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[4] = new Transformer04(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[3] = new Transformer03(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[2] = new Transformer02(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[1] = new Transformer01(data,
                                         hierarchies,
                                         sensitiveValues,
                                         dictionarySensValue,
                                         dictionarySensFreq,
                                         config);
        instances[0] = new TransformerAll(data,
                                          hierarchies,
                                          sensitiveValues,
                                          dictionarySensValue,
                                          dictionarySensFreq,
                                          config);
    }

    /**
     * Gets the applicator.
     * 
     * @param projection
     *            the projection
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

    /**
     * Gets the buffer.
     * 
     * @return the buffer
     */
    public int[][] getBuffer() {
        return buffer;
    }
}
