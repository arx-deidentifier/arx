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

package org.deidentifier.flash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.FLASHConfiguration.Criterion;
import org.deidentifier.flash.FLASHConfiguration.LDiversityCriterion;
import org.deidentifier.flash.FLASHConfiguration.TClosenessCriterion;
import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.framework.Configuration;
import org.deidentifier.flash.framework.check.INodeChecker;
import org.deidentifier.flash.framework.check.NodeChecker;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.DataManager;
import org.deidentifier.flash.framework.data.Dictionary;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.IDGenerator;
import org.deidentifier.flash.framework.lattice.Lattice;
import org.deidentifier.flash.framework.lattice.Node;
import org.deidentifier.flash.metric.Metric;

/**
 * An implementation of the class DataHandle and the FLASHResult interface for
 * output data.
 * 
 * @author Prasser, Kohlmayer
 */
public class DataHandleOutput extends DataHandle implements FLASHResult {

    /**
     * The class ResultIterator.
     * 
     * @author Prasser, Kohlmayer
     */
    public class ResultIterator implements Iterator<String[]> {

        /** The current row. */
        private int row = -1;

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return row < dataQI.getArray().length;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        @Override
        public String[] next() {

            String[] result = null;

            /* write header */
            if (row == -1) {
                result = header;

                /* write a normal row */
            } else {

                // Create row
                result = new String[header.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = getValueInternal(row, i);
                }
            }

            row++;
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** The global optimum. */
    private FLASHNode     optimalNode;

    /** The current node */
    private FLASHNode     currentNode;

    /** The last node */
    private FLASHNode     lastNode;

    /** The lattice. */
    private FLASHLattice  lattice;

    /** The names of the quasiIdentifer. */
    private String[]      quasiIdentifiers;

    /** Wall clock. */
    private long          duration;

    /** The data. */
    private Data          dataSE;

    /** The data. */
    private Data          dataIS;

    /** The data. */
    private Data          dataQI;

    /** The generalization hierarchies. */
    private int[][][]     map;

    /** The string to insert. */
    private String        suppressionString;

    /** An inverse map for column indices. */
    private int[]         inverseMap;

    /** An inverse map to data arrays. */
    private int[][][]     inverseData;

    /** An inverse map to dictionaries. */
    private Dictionary[]  inverseDictionaries;

    /** The node checker. */
    private INodeChecker  checker;

    /** Should we remove outliers */
    private boolean       removeOutliers;

    /** The configuration */
    private Configuration config;

    /**
     * Internal constructor for deserialization
     */
    public DataHandleOutput(final DataHandle handle,
                            final DataDefinition definition,
                            final Hierarchy sensitiveHierarchy,
                            final FLASHLattice lattice,
                            final boolean removeOutliers,
                            final String suppressionString,
                            final int historySize,
                            final double snapshotSizeSnapshot,
                            final double snapshotSizeDataset,
                            final int k,
                            final double relativeMaxOutliers,
                            final LDiversityCriterion criterionL,
                            final TClosenessCriterion criterionT,
                            final Metric<?> metric,
                            final Criterion algorithm,
                            final int l,
                            final double c,
                            final boolean practicalMonotonicity,
                            final double t,
                            final FLASHNode optimum,
                            final long time) {

        // Set optimum in lattice
        lattice.access().setOptimum(optimum);

        // Create a data manager
        // TODO: Copy from FLASHAnonymizer
        final Map<String, String[][]> hierarchies = handle.getDefinition()
                                                          .getHierarchies();
        final Set<String> insensitiveAttributes = handle.getDefinition()
                                                        .getInsensitiveAttributes();
        final Set<String> identifiers = handle.getDefinition()
                                              .getIdentifyingAttributes();
        final Map<String, Integer> minGeneralizations = handle.getDefinition()
                                                              .getMinimalGeneralizations();
        final Map<String, Integer> maxGeneralizations = handle.getDefinition()
                                                              .getMaximalGeneralizations();

        // Extract data
        final String[] header = ((DataHandleInput) handle).header;
        final int[][] dataArray = ((DataHandleInput) handle).data;
        final Dictionary dictionary = ((DataHandleInput) handle).dictionary;

        // Encode
        final Map<String, String[][]> sensitive = new HashMap<String, String[][]>();
        if (sensitiveHierarchy != null) {
            sensitive.put(handle.getDefinition()
                                .getSensitiveAttributes()
                                .iterator()
                                .next(), sensitiveHierarchy.getHierarchy());
        } else {
            if (!handle.getDefinition().getSensitiveAttributes().isEmpty()) {
                sensitive.put(handle.getDefinition()
                                    .getSensitiveAttributes()
                                    .iterator()
                                    .next(), null);
            }
        }
        final DataManager manager = new DataManager(header,
                                                    dataArray,
                                                    dictionary,
                                                    hierarchies,
                                                    minGeneralizations,
                                                    maxGeneralizations,
                                                    sensitive,
                                                    insensitiveAttributes,
                                                    identifiers);

        // Initialize the metric
        metric.initialize(manager.getDataQI(), manager.getHierarchies());

        final int absoluteMaxOutliers = (int) (handle.getNumRows() * relativeMaxOutliers);

        // Create a config
        final Configuration config = Configuration.getDeserializedConfiguration(algorithm,
                                                                                absoluteMaxOutliers,
                                                                                relativeMaxOutliers,
                                                                                k,
                                                                                l,
                                                                                c,
                                                                                criterionL,
                                                                                t,
                                                                                criterionT,
                                                                                practicalMonotonicity);

        // Create a node checker
        final INodeChecker checker = new NodeChecker(manager,
                                                     metric,
                                                     config,
                                                     historySize,
                                                     snapshotSizeDataset,
                                                     snapshotSizeSnapshot);

        // Initialize the result
        init(manager,
             checker,
             time,
             suppressionString,
             definition,
             lattice,
             practicalMonotonicity,
             removeOutliers,
             absoluteMaxOutliers,
             config);
    }

    /**
     * Instantiates a new FLASH result.
     * 
     * @param metric
     * @param manager
     * @param checker
     * @param time
     * @param suppressionString
     * @param defintion
     * @param lattice
     * @param practicalMonotonicity
     * @param removeOutliers
     * @param maximumAbsoluteOutliers
     * @param config
     */
    public DataHandleOutput(final Metric<?> metric,
                            final DataManager manager,
                            final INodeChecker checker,
                            final long time,
                            final String suppressionString,
                            final DataDefinition defintion,
                            final Lattice lattice,
                            final boolean practicalMonotonicity,
                            final boolean removeOutliers,
                            final int maximumAbsoluteOutliers,
                            final Configuration config) {

        final FLASHLattice flattice = new FLASHLattice(lattice,
                                                       manager.getDataQI()
                                                              .getHeader(),
                                                       metric,
                                                       practicalMonotonicity,
                                                       maximumAbsoluteOutliers);

        init(manager,
             checker,
             time,
             suppressionString,
             defintion,
             flattice,
             practicalMonotonicity,
             removeOutliers,
             maximumAbsoluteOutliers,
             config);
    }

    /**
     * Associates this handle with an input data handle.
     * 
     * @param inHandle
     *            the in handle
     */
    protected void associate(final DataHandleInput inHandle) {
        other = inHandle;
    }

    /**
     * A negative integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second. It uses the specified
     * data types for comparison if no generalization was applied, otherwise it
     * uses string comparison.
     * 
     * @param row1
     *            the row1
     * @param row2
     *            the row2
     * @param columns
     *            the columns
     * @param ascending
     *            the ascending
     * @return the int
     */
    @Override
    protected int compare(final int row1,
                          final int row2,
                          final int[] columns,
                          final boolean ascending) {
        getHandle(currentNode);
        for (final int index : columns) {

            final int attributeType = inverseMap[index] >>> AttributeType.SHIFT;
            final int indexMap = inverseMap[index] & AttributeType.MASK;

            int cmp = 0;
            try {
                cmp = dataTypes[attributeType][indexMap].compare(getValueInternal(row1,
                                                                                  index),
                                                                 getValueInternal(row2,
                                                                                  index));
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            if (cmp != 0) {
                if (ascending) {
                    return -cmp;
                } else {
                    return cmp;
                }
            }
        }
        return 0;
    }

    /**
     * Creates the data type array.
     */
    @Override
    protected void createDataTypeArray() {

        dataTypes = new DataType[3][];
        dataTypes[AttributeType.ATTR_TYPE_IS] = new DataType[dataIS.getHeader().length];
        dataTypes[AttributeType.ATTR_TYPE_SE] = new DataType[dataSE.getHeader().length];
        dataTypes[AttributeType.ATTR_TYPE_QI] = new DataType[dataQI.getHeader().length];

        for (int i = 0; i < dataTypes.length; i++) {
            final DataType[] type = dataTypes[i];

            String[] headers = null;

            switch (i) {
            case AttributeType.ATTR_TYPE_IS:
                headers = dataIS.getHeader();
                break;
            case AttributeType.ATTR_TYPE_QI:
                headers = dataQI.getHeader();
                break;
            case AttributeType.ATTR_TYPE_SE:
                headers = dataSE.getHeader();
                break;
            }

            for (int j = 0; j < type.length; j++) {
                dataTypes[i][j] = definition.getDataType(headers[j]);
                if ((i == AttributeType.ATTR_TYPE_QI) &&
                    (currentNode.getTransformation()[j] > 0)) {
                    dataTypes[i][j] = DataType.STRING;
                }
            }
        }
    }

    /**
     * Gets the attribute name.
     * 
     * @param col
     *            the col
     * @return the attribute name
     */
    @Override
    public String getAttributeName(final int col) {
        checkColumn(col);
        return header[col];
    }

    @Override
    public FLASHConfiguration getConfiguration() {
        return config;
    }

    /**
     * Gets the distinct values.
     * 
     * @param col
     *            the col
     * @return the distinct values
     */
    @Override
    public String[] getDistinctValues(final int col) {

        getHandle(currentNode);

        // Check
        checkColumn(col);

        // TODO: Inefficient
        final Set<String> vals = new HashSet<String>();
        for (int i = 0; i < getNumRows(); i++) {
            vals.add(getValue(i, col));
        }
        return vals.toArray(new String[vals.size()]);
    }

    @Override
    public int getGeneralization(final String attribute) {
        return currentNode.getGeneralization(attribute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.flash.FLASHResult#getGlobalOptimum()
     */
    /**
     * Gets the global optimalFlashNode.
     * 
     * @return the global optimalFlashNode
     */
    @Override
    public FLASHNode getGlobalOptimum() {
        return optimalNode;
    }

    @Override
    public int getGroupCount() {
        getHandle(currentNode);
        return checker.getGroupCount();
    }

    @Override
    public int getGroupOutliersCount() {
        getHandle(currentNode);
        return checker.getGroupOutliersCount();
    }

    @Override
    public DataHandle getHandle() {
        if (optimalNode == null) { return null; }
        return getHandle(optimalNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.flash.FLASHResult#getHandle()
     */
    /**
     * Gets the handle.
     * 
     * @return the handle
     */
    @Override
    public DataHandle getHandle(final FLASHNode fnode) {

        currentNode = fnode;

        // Dont transform twice
        if ((currentNode != null) && (currentNode == lastNode)) { return this; }

        // Prepare
        lastNode = currentNode;
        final Node node = new Node(new IDGenerator());
        node.setTransformation(fnode.getTransformation(), 0);
        if (currentNode.isChecked()) {
            node.setChecked();
        }

        // Suppress
        checker.transformAndMarkOutliers(node);

        // Store
        if (!currentNode.isChecked()) {
            currentNode.access().setChecked(true);
            if (node.isAnonymous()) {
                currentNode.access().setAnonymous();
            } else {
                currentNode.access().setNotAnonymous();
            }
            currentNode.access()
                       .setMaximumInformationLoss(node.getInformationLoss());
            currentNode.access()
                       .setMinimumInformationLoss(node.getInformationLoss());
            lattice.estimateInformationLoss();
        }

        // Create datatype array
        createDataTypeArray();
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.flash.FLASHResult#getLattice()
     */
    /**
     * Gets the lattice.
     * 
     * @return the lattice
     */
    @Override
    public FLASHLattice getLattice() {
        return lattice;
    }

    /**
     * Gets the num columns.
     * 
     * @return the num columns
     */
    @Override
    public int getNumColumns() {
        return header.length;
    }

    /**
     * Gets the num rows.
     * 
     * @return the num rows
     */
    @Override
    public int getNumRows() {
        return dataQI.getDataLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.flash.FLASHResult#getTime()
     */
    /**
     * Gets the time.
     * 
     * @return the time
     */
    @Override
    public long getTime() {
        return duration;
    }

    @Override
    public int getTupleOutliersCount() {
        getHandle(currentNode);
        return checker.getTupleOutliersCount();
    }

    /**
     * Gets the value.
     * 
     * @param row
     *            the row
     * @param col
     *            the col
     * @return the value
     */
    @Override
    public String getValue(final int row, final int col) {

        // Check
        getHandle(currentNode);
        checkColumn(col);
        checkRow(row, dataQI.getDataLength());

        // Perform
        return getValueInternal(row, col);
    }

    /**
     * Gets the value internal.
     * 
     * @param row
     *            the row
     * @param col
     *            the col
     * @return the value internal
     */
    @Override
    protected String getValueInternal(final int row, final int col) {

        // Return the according values
        final int type = inverseMap[col] >>> AttributeType.SHIFT;
        switch (type) {
        case AttributeType.ATTR_TYPE_ID:
            return suppressionString;
        default:
            final int index = inverseMap[col] & AttributeType.MASK;
            final int[][] data = inverseData[type];

            if (removeOutliers &&
                ((dataQI.getArray()[row][0] & Data.OUTLIER_MASK) != 0)) { return suppressionString; }

            final int value = data[row][index] & Data.REMOVE_OUTLIER_MASK;
            final String[][] dictionary = inverseDictionaries[type].getMapping();
            return dictionary[index][value];
        }
    }

    private void init(final DataManager manager,
                      final INodeChecker checker,
                      final long time,
                      final String suppressionString,
                      final DataDefinition defintion,
                      final FLASHLattice lattice,
                      final boolean practicalMonotonicity,
                      final boolean removeOutliers,
                      final int maximumAbsoluteOutliers,
                      final Configuration config) {

        this.config = config;
        this.removeOutliers = removeOutliers;
        this.lattice = lattice;
        optimalNode = this.lattice.getOptimum();
        duration = time;
        currentNode = optimalNode;
        lastNode = null;
        this.suppressionString = suppressionString;
        this.checker = checker;
        definition = defintion;

        // Extract data
        dataQI = checker.getBuffer();
        dataSE = manager.getDataSE();
        dataIS = manager.getDataIS();
        super.header = manager.getHeader();

        // Init quasi identifiers and hierarchies
        final GeneralizationHierarchy[] hierarchies = manager.getHierarchies();
        quasiIdentifiers = new String[hierarchies.length];
        map = new int[hierarchies.length][][];
        for (int i = 0; i < hierarchies.length; i++) {
            quasiIdentifiers[i] = hierarchies[i].getName();
            map[i] = hierarchies[i].getArray();
        }

        // Build map inverse
        inverseMap = new int[header.length];
        for (int i = 0; i < inverseMap.length; i++) {
            inverseMap[i] = (AttributeType.ATTR_TYPE_ID << AttributeType.SHIFT);
        }
        for (int i = 0; i < dataQI.getMap().length; i++) {
            inverseMap[dataQI.getMap()[i]] = i |
                                             (AttributeType.ATTR_TYPE_QI << AttributeType.SHIFT);
        }
        for (int i = 0; i < dataSE.getMap().length; i++) {
            inverseMap[dataSE.getMap()[i]] = i |
                                             (AttributeType.ATTR_TYPE_SE << AttributeType.SHIFT);
        }
        for (int i = 0; i < dataIS.getMap().length; i++) {
            inverseMap[dataIS.getMap()[i]] = i |
                                             (AttributeType.ATTR_TYPE_IS << AttributeType.SHIFT);
        }

        // Build inverse data array
        inverseData = new int[3][][];
        inverseData[AttributeType.ATTR_TYPE_IS] = dataIS.getArray();
        inverseData[AttributeType.ATTR_TYPE_SE] = dataSE.getArray();
        inverseData[AttributeType.ATTR_TYPE_QI] = dataQI.getArray();

        // Build inverse dictionary array
        inverseDictionaries = new Dictionary[3];
        inverseDictionaries[AttributeType.ATTR_TYPE_IS] = dataIS.getDictionary();
        inverseDictionaries[AttributeType.ATTR_TYPE_SE] = dataSE.getDictionary();
        inverseDictionaries[AttributeType.ATTR_TYPE_QI] = dataQI.getDictionary();
    }

    @Override
    protected boolean isOutlierInternal(final int row) {
        return ((dataQI.getArray()[row][0] & Data.OUTLIER_MASK) != 0);
    }

    /**
     * Checks if is result available.
     * 
     * @return true, if is result available
     */
    @Override
    public boolean isResultAvailable() {
        return optimalNode != null;
    }

    /**
     * Iterator.
     * 
     * @return the iterator
     */
    @Override
    public Iterator<String[]> iterator() {
        getHandle(currentNode);
        return new ResultIterator();
    }

    /**
     * Swap.
     * 
     * @param row1
     *            the row1
     * @param row2
     *            the row2
     */
    @Override
    public void swap(final int row1, final int row2) {

        // Check
        checkRow(row1, dataQI.getDataLength());
        checkRow(row2, dataQI.getDataLength());

        // Swap input data
        if (other != null) {
            other.swapInternal(row1, row2);
        }

        // Swap
        swapInternal(row1, row2);
    }

    /**
     * Swap internal.
     * 
     * @param row1
     *            the row1
     * @param row2
     *            the row2
     */
    @Override
    protected void swapInternal(final int row1, final int row2) {
        // Now swap
        getHandle(currentNode);
        int[] temp = dataQI.getArray()[row1];
        dataQI.getArray()[row1] = dataQI.getArray()[row2];
        dataQI.getArray()[row2] = temp;
        temp = dataSE.getArray()[row1];
        dataSE.getArray()[row1] = dataSE.getArray()[row2];
        dataSE.getArray()[row2] = temp;
        temp = dataIS.getArray()[row1];
        dataIS.getArray()[row1] = dataIS.getArray()[row2];
        dataIS.getArray()[row2] = temp;
    }
}
