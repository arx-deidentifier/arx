/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.framework.Configuration;
import org.deidentifier.arx.framework.check.StateMachine.Transition;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.Metric;

/**
 * This class orchestrates the process of checking a node for k-anonymity
 * 
 * @author Prasser, Kohlmayer
 */
public class NodeChecker implements INodeChecker {

    /** The current hash groupify. */
    protected IHashGroupify     currentGroupify;

    /** The history. */
    protected History           history;

    /** The last hash groupify. */
    protected IHashGroupify     lastGroupify;

    /** The metric. */
    protected Metric<?>         metric;

    /** The state machine. */
    protected StateMachine      stateMachine;

    /** The data transformer. */
    protected Transformer       transformer;

    /** The data. */
    private final Data          data;

    /** The config */
    private final Configuration config;

    /**
     * Creates a new NodeChecker instance.
     * 
     * @param manager
     *            The manager
     * @param metric
     *            The metric
     * @param config
     *            The anonymization configuration
     * @param historyMaxSize
     *            The history max size
     * @param snapshotSizeDataset
     *            The history threshold
     * @param snapshotSizeSnapshot
     *            The history threshold replacement
     */
    public NodeChecker(final DataManager manager, final Metric<?> metric, final Configuration config, final int historyMaxSize, final double snapshotSizeDataset, final double snapshotSizeSnapshot) {

        // Initialize all operators
        this.metric = metric;
        this.config = config;
        data = manager.getDataQI();
        final int initialSize = (int) (manager.getDataQI().getDataLength() * 0.01d);

        final IntArrayDictionary dictionarySensValue;
        final IntArrayDictionary dictionarySensFreq;

        switch (config.getCriterion()) {
        case K_ANONYMITY:
        case D_PRESENCE:
            // Just to allow bytecode instrumentation
            dictionarySensValue = new IntArrayDictionary(0);
            dictionarySensFreq = new IntArrayDictionary(0);
            break;

        case L_DIVERSITY:
        case T_CLOSENESS:
            dictionarySensValue = new IntArrayDictionary(initialSize);
            dictionarySensFreq = new IntArrayDictionary(initialSize);
            break;

        default:
            throw new UnsupportedOperationException(config.getCriterion() + ": currenty not supported");
        }

        history = new History(manager.getDataQI().getArray().length, historyMaxSize, snapshotSizeDataset, snapshotSizeSnapshot, config, dictionarySensValue, dictionarySensFreq);

        stateMachine = new StateMachine(history);
        currentGroupify = new HashGroupify(initialSize, config);
        lastGroupify = new HashGroupify(initialSize, config);

        transformer = new Transformer(manager.getDataQI().getArray(), manager.getHierarchies(), manager.getDataSE().getArray(), config, dictionarySensValue, dictionarySensFreq);
    }

    @Override
    public void check(final Node node) {

        // Store snapshot from last check
        if (stateMachine.getLastNode() != null) {
            history.store(stateMachine.getLastNode(), currentGroupify, stateMachine.getLastTransition().snapshot);
        }

        // Transition
        final Transition transition = stateMachine.transition(node);

        // Switch groupifies
        final IHashGroupify temp = lastGroupify;
        lastGroupify = currentGroupify;
        currentGroupify = temp;
        currentGroupify.clear();

        // Apply transition
        switch (transition.type) {
        case UNOPTIMIZED:
            currentGroupify = transformer.apply(transition.projection, node.getTransformation(), currentGroupify);
            break;
        case ROLLUP:
            currentGroupify = transformer.applyRollup(transition.projection, node.getTransformation(), lastGroupify, currentGroupify);
            break;
        case SNAPSHOT:
            currentGroupify = transformer.applySnapshot(transition.projection, node.getTransformation(), currentGroupify, transition.snapshot);
            break;
        }

        // Mark as checked
        node.setChecked();

        // Propagate k-anonymity
        node.setKAnonymous(currentGroupify.isKAnonymous());

        // Propagate anonymity and information loss
        if (currentGroupify.isAnonymous()) {
            node.setAnonymous(true);
            metric.evaluate(node, currentGroupify);
        } else {
            node.setInformationLoss(null);
            node.setAnonymous(false);
        }
    }

    @Override
    public Data getBuffer() {
        return new Data(transformer.getBuffer(), data.getHeader(), data.getMap(), data.getDictionary());
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public int getGroupCount() {
        return currentGroupify.size();
    }

    @Override
    public IHashGroupify getGroupify() {
        return currentGroupify;
    }

    @Override
    public int getGroupOutliersCount() {
        return currentGroupify.getGroupOutliersCount();
    }

    @Override
    @Deprecated
    public double getInformationLoss(final Node node) {
        check(node);
        metric.evaluate(node, currentGroupify);
        return node.getInformationLoss().getValue();

    }

    @Override
    public Metric<?> getMetric() {
        return metric;
    }

    @Override
    public int getTupleOutliersCount() {
        return currentGroupify.getTupleOutliersCount();
    }

    @Override
    @Deprecated
    public Data transform(final Node node) {

        // Apply transition and groupify
        currentGroupify.clear();
        currentGroupify = transformer.apply(0L, node.getTransformation(), currentGroupify);

        // Determine outliers and set infoloss
        if (!node.isChecked()) {
            node.setChecked();
            node.setAnonymous(currentGroupify.isAnonymous());
            metric.evaluate(node, currentGroupify);
            node.setTagged();
        }

        return getBuffer();
    }

    @Override
    public Data transformAndMarkOutliers(final Node node) {

        // Apply transition and groupify
        currentGroupify.clear();
        currentGroupify = transformer.apply(0L, node.getTransformation(), currentGroupify);

        // Determine outliers and set infoloss
        node.setAnonymous(currentGroupify.isAnonymous());
        if (!node.isChecked()) {
            node.setChecked();
            metric.evaluate(node, currentGroupify);
            node.setTagged();
        }

        // Find outliers
        if (config.getAbsoluteMaxOutliers() != 0) {
            currentGroupify.markOutliers(transformer.getBuffer());
        }

        // Return the buffer
        return getBuffer();
    }
}
