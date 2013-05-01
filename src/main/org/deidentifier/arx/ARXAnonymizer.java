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

package org.deidentifier.arx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.algorithm.AbstractAlgorithm;
import org.deidentifier.arx.algorithm.FLASHAlgorithm;
import org.deidentifier.arx.algorithm.FLASHStrategy;
import org.deidentifier.arx.framework.Configuration;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.LatticeBuilder;
import org.deidentifier.arx.metric.Metric;

/**
 * This class offers several methods to define parameters and execute the ARX
 * algorithm.
 * 
 * @author Prasser, Kohlmayer
 */
public class ARXAnonymizer {

    /**
     * Temporary result of the ARX algorithm.
     * 
     * @author Prasser, Kohlmayer
     */
    class Result {

        /** The checker. */
        final INodeChecker checker;

        /** The metric. */
        final Metric<?>    metric;

        /** The lattice. */
        final Lattice      lattice;

        /**
         * Creates a new instance.
         * 
         * @param metric
         *            the metric
         * @param checker
         *            the checker
         * @param lattice
         *            the lattice
         */
        private Result(final Metric<?> metric, final INodeChecker checker, final Lattice lattice) {
            this.metric = metric;
            this.checker = checker;
            this.lattice = lattice;
        }
    }

    /** Assume practical monotonicity? */
    private boolean       practicalMonotonicity = false;

    /** Remove outliers? */
    private boolean       removeOutliers        = true;

    /** Snapshot size. */
    private double        snapshotSizeDataset   = 0.2d;

    /** Snapshot size snapshot */
    private double        snapshotSizeSnapshot  = 0.8d;

    /** History size. */
    private int           historySize           = 200;

    /** The metric. */
    private Metric<?>     metric                = Metric.createDMStarMetric();

    /** The string to insert for outliers. */
    private String        suppressionString     = "*";

    /** The listener, if any. */
    private ARXListener listener              = null;

    /**
     * Creates a new anonymizer with the default configuration and DMStar
     * metric.
     * 
     */
    public ARXAnonymizer() {
        // Empty by design
    }

    /**
     * Creates a new anonymizer with the given configuration.
     * 
     * @param historySize
     *            the history size
     * @param snapshotSizeDataset
     *            the snapshot size
     */
    public ARXAnonymizer(final int historySize, final double snapshotSize) {
        this.historySize = historySize;
        snapshotSizeDataset = snapshotSize;
    }

    /**
     * Creates a new anonymizer with the given configuration and metric.
     * 
     * @param historySize
     *            the history size
     * @param snapshotSizeDataset
     *            the snapshot size
     * @param metric
     *            the metric
     */
    public ARXAnonymizer(final int historySize, final double snapshotSize, final Metric<?> metric) {
        this.historySize = historySize;
        snapshotSizeDataset = snapshotSize;
        this.metric = metric;
    }

    /**
     * Creates a new anonymizer with the given configuration and metric.
     * 
     * @param historySize
     *            the history size
     * @param snapshotSizeDataset
     *            the snapshot size
     * @param metric
     *            the metric
     * @param suppressionString
     *            the relativeMaxOutliers string
     */
    public ARXAnonymizer(final int historySize, final double snapshotSize, final Metric<?> metric, final String suppressionString) {
        this.suppressionString = suppressionString;
        this.historySize = historySize;
        snapshotSizeDataset = snapshotSize;
        this.metric = metric;
    }

    /**
     * Creates a new anonymizer with the given configuration.
     * 
     * @param historySize
     *            the history size
     * @param snapshotSizeDataset
     *            the snapshot size
     * @param suppressionString
     *            the relativeMaxOutliers string
     */
    public ARXAnonymizer(final int historySize, final double snapshotSize, final String suppressionString) {
        this.historySize = historySize;
        snapshotSizeDataset = snapshotSize;
        this.suppressionString = suppressionString;
    }

    /**
     * Creates a new anonymizer with the default configuration and the given
     * metric.
     * 
     * @param metric
     *            the metric
     */
    public ARXAnonymizer(final Metric<?> metric) {
        this.metric = metric;
    }

    /**
     * Creates a new anonymizer with the given configuration.
     * 
     * @param metric
     *            the metric
     * @param suppressionString
     *            the relativeMaxOutliers string
     */
    public ARXAnonymizer(final Metric<?> metric, final String suppressionString) {
        this.metric = metric;
        this.suppressionString = suppressionString;
    }

    /**
     * Creates a new anonymizer with the given configuration.
     * 
     * @param suppressionString
     *            the relativeMaxOutliers string
     */
    public ARXAnonymizer(final String suppressionString) {
        this.suppressionString = suppressionString;
    }

    protected Result anonymizeInternal(final Configuration config, final DataManager manager) {

        // Prepare if required
        switch (config.getCriterion()) {
        case T_CLOSENESS:
            switch (config.getTClosenessCriterion()) {
            case EMD_EQUAL:
                config.setDistribution(manager.getDistribution());
                break;
            case EMD_HIERARCHICAL:
                config.setTree(manager.getTree());
                break;
            }
        case D_PRESENCE:
            config.createResearchBitSet(manager.getDataQI().getDataLength());
            break;

        case K_ANONYMITY:
        case L_DIVERSITY:
            // do nothing
            break;
        }

        // Check
        checkAfterEncoding(config, manager);

        // Build the lattice
        final Lattice lattice = new LatticeBuilder(manager.getMaxLevels(), manager.getMinLevels(), manager.getHierachyHeights()).build();

        // Attach the listener
        lattice.setListener(listener);

        // Build a node checker
        final INodeChecker checker = new NodeChecker(manager, metric, config, historySize, snapshotSizeDataset, snapshotSizeSnapshot);

        // Initialize the metric
        metric.initialize(manager.getDataQI(), manager.getHierarchies());

        // Initialize the ARX strategy
        final FLASHStrategy strategy = new FLASHStrategy(lattice, manager.getHierarchies());

        // Build an algorithm instance
        config.setPracticalMonotonicity(practicalMonotonicity);
        final AbstractAlgorithm algorithm = new FLASHAlgorithm(lattice, checker, strategy);

        // Attach the listener
        algorithm.setListener(listener);

        // Execute
        algorithm.traverse();

        // Return the result
        final Result result = new Result(metric, checker, lattice);

        return result;
    }

    /**
     * Internal method that executes the ARX algorithm.
     * 
     * @param manager
     *            the data manager
     * @param config
     *            the config
     * @return the ARX result internal
     * @throws IOException
     */
    protected ARXResult anonymizeInternal(final Data data, final Configuration config) throws IOException {

        // Obtain handle
        final DataHandle handle = data.getHandle();

        // Check
        if (!(handle instanceof DataHandleInput)) { throw new IllegalArgumentException("Invalid data handle provided!"); }
        checkBeforeEncoding(config.getRelativeMaxOutliers(), handle.getDefinition().getHierarchies());

        final DataManager manager = prepareDataManager(handle, config);
        final int absoulteMaxOutliers = (int) (manager.getDataQI().getDataLength() * config.getRelativeMaxOutliers());
        config.setAbsoluteMaxOutliers(absoulteMaxOutliers);

        final long time = System.currentTimeMillis();

        final Result result = anonymizeInternal(config, manager);

        final DataHandleOutput outHandle = new DataHandleOutput(result.metric,
                                                                manager,
                                                                result.checker,
                                                                System.currentTimeMillis() - time,
                                                                suppressionString,
                                                                handle.getDefinition(),
                                                                result.lattice,
                                                                practicalMonotonicity,
                                                                removeOutliers,
                                                                config.getAbsoluteMaxOutliers(),
                                                                config);
        // Pairing
        outHandle.associate(handle);
        handle.associate(outHandle);

        return outHandle;
    }

    /**
     * Performs some sanity checks.
     * 
     * @param manager
     *            the manager
     */
    private void checkAfterEncoding(final Configuration config, final DataManager manager) {

        switch (config.getCriterion()) {
        case K_ANONYMITY:
            // Check group size
            final int k = config.getK();
            if ((k > manager.getDataQI().getDataLength()) || (k < 1)) { throw new IllegalArgumentException("Group size k " + k + " musst be positive and less or equal than the number of rows " +
                                                                                                           manager.getDataQI().getDataLength()); }
            break;

        default:
            // check nothing
            break;
        }

        // Check whether all hierarchies are monotonic
        for (final GeneralizationHierarchy hierarchy : manager.getHierarchies()) {
            if (!hierarchy.isMonotonic()) { throw new IllegalArgumentException("The hierarchy for the attribute '" + hierarchy.getName() + "' is not monotonic!"); }
        }

        // check min and max sizes
        final int[] hierarchyHeights = manager.getHierachyHeights();
        final int[] minLevels = manager.getMinLevels();
        final int[] maxLevels = manager.getMaxLevels();

        for (int i = 0; i < hierarchyHeights.length; i++) {
            if (minLevels[i] > (hierarchyHeights[i] - 1)) { throw new IllegalArgumentException("Invalid minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                               minLevels[i] + " > " + (hierarchyHeights[i] - 1)); }
            if (minLevels[i] < 0) { throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "' has to be positive!"); }
            if (maxLevels[i] > (hierarchyHeights[i] - 1)) { throw new IllegalArgumentException("Invalid maximum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                               maxLevels[i] + " > " + (hierarchyHeights[i] - 1)); }
            if (maxLevels[i] < minLevels[i]) { throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() +
                                                                                  "' has to be lower than or requal to the defined maximum!"); }
        }
    }

    /**
     * Performs some sanity checks.
     * 
     * @param relativeMaxOutliers
     *            the allowed maximal number of outliers
     * @param hierarchies
     *            the hierarchies
     */
    private void checkBeforeEncoding(final double relativeMaxOutliers, final Map<String, String[][]> hierarchies) {

        // Perform sanity checks
        if ((relativeMaxOutliers < 0d) || (relativeMaxOutliers >= 1d)) { throw new IllegalArgumentException("Suppression rate " + relativeMaxOutliers + "must be in [0,1["); }
        if (hierarchies.size() > 15) { throw new IllegalArgumentException("The curse of dimensionality strikes. Too many quasi-identifiers: " + hierarchies.size()); }
        if (hierarchies.size() == 0) { throw new IllegalArgumentException("You need to specify at least one quasi identifier!"); }

    }

    /**
     * Returns the maximum number of snapshots allowed to store in the history.
     * 
     * @return The size
     */
    public int getHistorySize() {
        return historySize;
    }

    /**
     * Gets the snapshot size.
     * 
     * @return The maximum size of a snapshot relative to the dataset size
     */
    public double getMaximumSnapshotSizeDataset() {
        return snapshotSizeDataset;
    }

    /**
     * Gets the snapshot size.
     * 
     * @return The maximum size of a snapshot relative to the previous snapshot
     *         size
     */
    public double getMaximumSnapshotSizeSnapshot() {
        return snapshotSizeSnapshot;
    }

    /**
     * Returns the metric utilized by the algorithm.
     * 
     * @return The metric
     */
    public Metric<?> getMetric() {
        return metric;
    }

    /**
     * Returns the string with which outliers are replaced.
     * 
     * @return the relativeMaxOutliers string
     */
    public String getSuppressionString() {
        return suppressionString;
    }

    /**
     * Returns whether the algorithm assumes practical monotonicity
     * 
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }

    /**
     * Does the anonymizer remove outliers from the dataset?
     * 
     * @return
     */
    public boolean isRemoveOutliers() {
        return removeOutliers;
    }

    /**
     * Performs k-anonymization.
     * 
     * @param data
     *            The data definition
     * @param k
     *            The value of k
     * @param maxOutliers
     *            The number of allowed outliers from range [0;1]
     * @return the ARX result
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ARXResult kAnonymize(final Data data, final int k, final double maxOutliers) throws IllegalArgumentException, IOException {

        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        final Configuration config = Configuration.getKAnonymityConfiguration(maxOutliers, k);
        final ARXResult result = anonymizeInternal(data, config);
        return result;
    }

    /**
     * Performs recursive (c,l)-diversity anonymization.
     * 
     * @param data
     *            The data definition
     * @param c
     *            The value of c
     * @param l
     *            The value of l
     * @param maxOutliers
     *            The number of allowed outliers from range [0;1]
     * @return the ARX result
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ARXResult lDiversify(final Data data, final double c, final int l, final double maxOutliers) throws IllegalArgumentException, IOException {

        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        final Configuration config = Configuration.getLDiversityConfiguration(maxOutliers, c, l);
        if (data.getDefinition().getSensitiveAttributes().size() == 0) { throw new IllegalArgumentException("You need to specify a sensitive attribute!"); }

        final ARXResult result = anonymizeInternal(data, config);
        return result;
    }

    /**
     * Performs entropy or distinct l-diversity anonymization.
     * 
     * @param data
     *            The data definition
     * @param l
     *            The value of l
     * @param entropy
     *            Choose between entropy or distinct l-diversity
     * @param maxOutliers
     *            The allowed relative number of allowed outliers from range
     *            [0;1]
     * @return the ARX result
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ARXResult lDiversify(final Data data, final int l, final boolean entropy, final double maxOutliers) throws IllegalArgumentException, IOException {

        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        Configuration config = null;
        if (entropy) {
            config = Configuration.getLDiversityConfiguration(maxOutliers, l, ARXConfiguration.LDiversityCriterion.ENTROPY);
        } else {
            config = Configuration.getLDiversityConfiguration(maxOutliers, l, ARXConfiguration.LDiversityCriterion.DISTINCT);
        }

        if (data.getDefinition().getSensitiveAttributes().size() == 0) { throw new IllegalArgumentException("You need to specify a sensitive attribute!"); }

        final ARXResult result = anonymizeInternal(data, config);
        return result;
    }

    /**
     * Prepares the data manager.
     * 
     * @param handle
     *            the handle
     * @param config
     *            the config
     * @return the data manager
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private DataManager prepareDataManager(final DataHandle handle, final Configuration config) throws IOException {

        // Extract definitions
        final Map<String, String[][]> hierarchies = handle.getDefinition().getHierarchies();
        final Set<String> insensitiveAttributes = handle.getDefinition().getInsensitiveAttributes();
        final Set<String> identifiers = handle.getDefinition().getIdentifyingAttributes();
        final Map<String, Integer> minGeneralizations = handle.getDefinition().getMinimalGeneralizations();
        final Map<String, Integer> maxGeneralizations = handle.getDefinition().getMaximalGeneralizations();

        // Extract data
        final String[] header = ((DataHandleInput) handle).header;
        final int[][] dataArray = ((DataHandleInput) handle).data;
        final Dictionary dictionary = ((DataHandleInput) handle).dictionary;

        // Encode
        final Map<String, String[][]> sensitive = new HashMap<String, String[][]>();
        if (config.getSensitiveHierarchy() != null) {
            sensitive.put(handle.getDefinition().getSensitiveAttributes().iterator().next(), config.getSensitiveHierarchy().getHierarchy());
        } else {
            if (!handle.getDefinition().getSensitiveAttributes().isEmpty()) {
                sensitive.put(handle.getDefinition().getSensitiveAttributes().iterator().next(), null);
            }
        }
        final DataManager manager = new DataManager(header, dataArray, dictionary, hierarchies, minGeneralizations, maxGeneralizations, sensitive, insensitiveAttributes, identifiers);

        return manager;
    }

    /**
     * Sets the maximum number of snapshots allowed to store in the history.
     * 
     * @param historySize
     *            The size
     */
    public void setHistorySize(final int historySize) {
        if (historySize < 1) { throw new IllegalArgumentException("history size must be positive and not 0"); }
        this.historySize = historySize;
    }

    /**
     * Sets a listener.
     * 
     * @param listener
     *            the new listener, if any
     */
    public void setListener(final ARXListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the maximum size of a snapshot relative to the dataset size.
     * 
     * @param snapshotSizeDataset
     *            The size
     */
    public void setMaximumSnapshotSizeDataset(final double snapshotSize) {
        // Perform sanity checks
        if ((snapshotSize <= 0d) || (snapshotSize > 1d)) { throw new IllegalArgumentException("Snapshot size " + snapshotSize + "must be in [0,1]"); }
        snapshotSizeDataset = snapshotSize;
    }

    /**
     * Sets the maximum size of a snapshot relative to the previous snapshot
     * 
     * @param snapshotSizeSnapshot
     *            The size
     */
    public void setMaximumSnapshotSizeSnapshot(final double snapshotSizeSnapshot) {
        // Perform sanity checks
        if ((snapshotSizeSnapshot <= 0d) || (snapshotSizeSnapshot > 1d)) { throw new IllegalArgumentException("Snapshot size " + snapshotSizeSnapshot + "must be in [0,1]"); }
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
    }

    /**
     * Sets the metric to utilize by the algorithm.
     * 
     * @param metric
     *            The metric
     */
    public void setMetric(final Metric<?> metric) {
        if (metric == null) { throw new NullPointerException("metric must not be null"); }
        this.metric = metric;
    }

    /**
     * Should the algorithm assume practical monotonicity
     * 
     * @param val
     * @return
     */
    public void setPracticalMonotonicity(final boolean val) {
        practicalMonotonicity = val;
    }

    /**
     * Set whether the anonymizer should remove outliers
     * 
     * @param value
     */
    public void setRemoveOutliers(final boolean value) {
        removeOutliers = value;
    }

    /**
     * Sets the string with which suppressed values are to be replaced.
     * 
     * @param suppressionString
     *            The relativeMaxOutliers string
     */
    public void setSuppressionString(final String suppressionString) {
        if (suppressionString == null) { throw new NullPointerException("suppressionString must not be null"); }
        this.suppressionString = suppressionString;
    }

    /**
     * Performs t-closeness anoymization based on earth-movers-distance with
     * equal distance assumption.
     * 
     * @param data
     *            the data
     * @param k
     *            the k
     * @param t
     *            the t
     * @param maxOutliers
     *            The number of allowed outliers from range [0;1]
     * @return the ARX result
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ARXResult tClosify(final Data data, final int k, final double t, final double maxOutliers) throws IllegalArgumentException, IOException {

        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        final Configuration config = Configuration.getTClosenessConfiguration(maxOutliers, k, t);
        if (data.getDefinition().getSensitiveAttributes().size() == 0) { throw new IllegalArgumentException("You need to specify a sensitive attribute!"); }

        final ARXResult result = anonymizeInternal(data, config);
        return result;
    }

    /**
     * Performs t-closeness anoymization with earth movers distance with
     * hierarchical distance calculation.
     * 
     * @param data
     *            the data
     * @param k
     *            the k
     * @param t
     *            the t
     * @param maxOutliers
     *            The number of allowed outliers from range [0;1]
     * @param hierarchy
     * @return the ARX result
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ARXResult tClosify(final Data data, final int k, final double t, final double maxOutliers, final Hierarchy hierarchy) throws IllegalArgumentException, IOException {

        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        final Configuration config = Configuration.getTClosenessConfiguration(maxOutliers, k, t, hierarchy);
        if (data.getDefinition().getSensitiveAttributes().size() == 0) { throw new IllegalArgumentException("You need to specify a sensitive attribute!"); }

        if ((hierarchy == null) || (hierarchy.getHierarchy().length == 0) || (hierarchy.getHierarchy()[0].length == 0)) { throw new IllegalArgumentException("You need to specify a generalization hierarchy for the sensitive attribute!"); }

        final ARXResult result = anonymizeInternal(data, config);
        return result;
    }

    /**
     * Performs t-closeness anoymization with earth movers distance with
     * hierarchical distance calculation.
     * 
     * @param data
     *            the data
     * @param k
     *            the k
     * @param t
     *            the t
     * @param maxOutliers
     *            The number of allowed outliers from range [0;1]
     * @param hierarchy
     * @return the ARX result
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ARXResult dpresencify(final Data data, final int k, final double dMin, final double dMax, final double maxOutliers, final Set<Integer> researchSubset) throws IllegalArgumentException,
                                                                                                                                                                   IOException {

        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        if ((researchSubset == null) || (researchSubset.size() == 0)) { throw new IllegalArgumentException("You need to specify a research subset!"); }

        final Configuration config = Configuration.getDPresenceConfiguration(maxOutliers, k, dMin, dMax, researchSubset);
        final ARXResult result = anonymizeInternal(data, config);
        return result;
    }

}
