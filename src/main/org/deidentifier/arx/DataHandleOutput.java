/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.Metric;

/**
 * An implementation of the class DataHandle and the ARXResult interface for
 * output data.
 * 
 * @author Prasser, Kohlmayer
 */
public class DataHandleOutput extends DataHandle implements ARXResult {

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
                    result[i] = internalGetValue(row, i);
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

    /** The node checker. */
    private INodeChecker  checker;

    /** The configuration */
    private ARXConfiguration config;

    /** The current node */
    private ARXNode     currentNode;

    /** The data. */
    private Data          dataIS;

    /** The buffer. */
    private Data          source;
    
    /** The data. */
    private Data          dataQI;

    /** The data. */
    private Data          dataSE;

    /** Wall clock. */
    private long          duration;

    /** An inverse map to data arrays. */
    private int[][][]     inverseData;

    /** An inverse map to dictionaries. */
    private Dictionary[]  inverseDictionaries;

    /** An inverse map for column indices. */
    private int[]         inverseMap;

    /** The last node */
    private ARXNode     lastNode;

    /** The lattice. */
    private ARXLattice  lattice;

    /** The generalization hierarchies. */
    private int[][][]     map;

    /** The global optimum. */
    private ARXNode     optimalNode;

    /** The names of the quasiIdentifer. */
    private String[]      quasiIdentifiers;

    /** Should we remove outliers */
    private boolean       removeOutliers;

    /** The string to insert. */
    private String        suppressionString;

    /**
     * Internal constructor for deserialization
     */
    public DataHandleOutput(final DataHandle handle,
                            final DataDefinition definition,
                            final ARXLattice lattice,
                            final boolean removeOutliers,
                            final String suppressionString,
                            final int historySize,
                            final double snapshotSizeSnapshot,
                            final double snapshotSizeDataset,
                            final Metric<?> metric,
                            final ARXConfiguration config,
                            final ARXNode optimum,
                            final long time) {

        // Set registry
        this.setRegistry(handle.getRegistry());
        this.getRegistry().updateOutput(this);
        this.getRegistry().createInputSubset(config);
        this.getRegistry().createOutputSubset(config);

        // Set optimum in lattice
        lattice.access().setOptimum(optimum);

        // Extract data
        final String[] header = ((DataHandleInput) handle).header;
        final int[][] dataArray = ((DataHandleInput) handle).data;
        final Dictionary dictionary = ((DataHandleInput) handle).dictionary;
        final DataManager manager = new DataManager(header,
                                                    dataArray,
                                                    dictionary,
                                                    handle.getDefinition(),
                                                    config.getCriteria());

        // Initialize
        config.initialize(manager);

        // Initialize the metric
        metric.initialize(manager.getDataQI(), manager.getHierarchies(), config);

        // Create a node checker
        final INodeChecker checker = new NodeChecker(manager,
                                                     metric,
                                                     config,
                                                     historySize,
                                                     snapshotSizeDataset,
                                                     snapshotSizeSnapshot);

        // Store buffer
        this.source = checker.getData();
        
        // Initialize the result
        init(manager,
             checker,
             time,
             suppressionString,
             definition,
             lattice,
             removeOutliers,
             config);
    }

    /**
     * Instantiates a new ARX result.
     * 
     * @param manager The data manager
     * @param checker The node checker
     * @param time The elapsed wall-clock  time
     * @param suppressionString The suppression string
     * @param definition The data definition
     * @param lattice The lattice
     * @param practicalMonotonicity Do we assume practical monotonicity
     * @param removeOutliers Do we remove outliers
     * @param maximumAbsoluteOutliers The maximum number of outliers
     * @param config The configuration
     * @param projection A projection mask for results of iterative executions.
     *                   projection[i] must contain true if the ith QI is to be preserved
     */
    public DataHandleOutput(final DataRegistry registry,
                            final DataManager manager,
                            final INodeChecker checker,
                            final long time,
                            final String suppressionString,
                            final DataDefinition definition,
                            final Lattice lattice,
                            final boolean removeOutliers,
                            final ARXConfiguration config) {

        registry.updateOutput(this);
        this.setRegistry(registry);
        
        this.source = checker.getData();

        final ARXLattice flattice = new ARXLattice(lattice,
                                                   manager.getDataQI().getHeader(),
                                                   config);

        init(manager,
             checker,
             time,
             suppressionString,
             definition,
             flattice,
             removeOutliers,
             config);
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
    // TODO: Removed because the result config might be different from the input config
    //       in case of multiple sensitive attributes
    // @Override
    // public ARXConfiguration getConfiguration() {
    //  return config;
    // }

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
     * @see org.deidentifier.ARX.ARXResult#getGlobalOptimum()
     */
    /**
     * Gets the global optimalARXNode.
     * 
     * @return the global optimalARXNode
     */
    @Override
    public ARXNode getGlobalOptimum() {
        return optimalNode;
    }

    @Override
    public int getNumberOfGroups() {
        getHandle(currentNode);
        return checker.getNumberOfGroups();
    }

    @Override
    public int getNumberOfOutlyingGroups() {
        getHandle(currentNode);
        return checker.getNumberOfOutlyingGroups();
    }

    @Override
    public DataHandle getHandle() {
        if (optimalNode == null) { return null; }
        return getHandle(optimalNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.ARXResult#getHandle()
     */
    /**
     * Gets the handle.
     * 
     * @return the handle
     */
    @Override
    public DataHandle getHandle(final ARXNode fnode) {

        currentNode = fnode;

        // Dont transform twice
        if ((currentNode != null) && (currentNode == lastNode)) { return this; }

        // Prepare
        lastNode = currentNode;
        final Node node = new Node(0);
        int level = 0; for (int i : fnode.getTransformation()) level+= i;
        node.setTransformation(fnode.getTransformation(), level);
        if (currentNode.isChecked()) {
            node.setChecked();
        }

        // Suppress
        checker.transformAndMarkOutliers(node);

        // Store
        if (!currentNode.isChecked()) {
            
            currentNode.access().setChecked(true);
            // Only in this case, due to the special case 
            // with multiple sensitive attributes
            if (config.isPracticalMonotonicity()) {
                if (node.isAnonymous()) {
                    currentNode.access().setAnonymous();
                } else {
                    currentNode.access().setNotAnonymous();
                }
            }
            currentNode.access().setMaximumInformationLoss(node.getInformationLoss());
            currentNode.access().setMinimumInformationLoss(node.getInformationLoss());
            lattice.estimateInformationLoss();
        }

        // Create datatype array
        createDataTypeArray();
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.ARXResult#getLattice()
     */
    /**
     * Gets the lattice.
     * 
     * @return the lattice
     */
    @Override
    public ARXLattice getLattice() {
        return lattice;
    }

    /**
     * Gets the num columns.
     * 
     * @return the num columns
     */
    @Override
    public int getNumColumns() {
        checkRegistry();
        return header.length;
    }

    /**
     * Gets the num rows.
     * 
     * @return the num rows
     */
    @Override
    public int getNumRows() {
        checkRegistry();
        return dataQI.getDataLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.ARXResult#getTime()
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
    public int getNumberOfOutlyingTuples() {
        getHandle(currentNode);
        return checker.getNumberOfOutlyingTuples();
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
        checkRegistry();
        getHandle(currentNode);
        checkColumn(col);
        checkRow(row, dataQI.getDataLength());

        // Perform
        return internalGetValue(row, col);
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
     * Initializer
     * @param manager
     * @param checker
     * @param time
     * @param suppressionString
     * @param defintion
     * @param lattice
     * @param removeOutliers
     * @param config
     */
    private void init(final DataManager manager,
                      final INodeChecker checker,
                      final long time,
                      final String suppressionString,
                      final DataDefinition defintion,
                      final ARXLattice lattice,
                      final boolean removeOutliers,
                      final ARXConfiguration config) {

        this.config = config;
        this.removeOutliers = removeOutliers;
        this.lattice = lattice;
        this.optimalNode = this.lattice.getOptimum();
        this.duration = time;
        this.currentNode = optimalNode;
        this.lastNode = null;
        this.suppressionString = suppressionString;
        this.checker = checker;
        this.definition = defintion;

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
        
        // Create view
        this.getRegistry().createOutputSubset(config);
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
            final DataType<?>[] type = dataTypes[i];

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
    protected int internalCompare(final int row1,
                                  final int row2,
                                  final int[] columns,
                                  final boolean ascending) {
        getHandle(currentNode);
        for (final int index : columns) {

            final int attributeType = inverseMap[index] >>> AttributeType.SHIFT;
            final int indexMap = inverseMap[index] & AttributeType.MASK;
            if (attributeType == AttributeType.ATTR_TYPE_ID) return 0;
            
            int cmp = 0;
            try {
                cmp = dataTypes[attributeType][indexMap].compare(internalGetValue(row1,
                                                                                  index),
                                                                 internalGetValue(row2,
                                                                                  index));
            } catch (final Exception e) {
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
     * Gets the value internal.
     * 
     * @param row
     *            the row
     * @param col
     *            the col
     * @return the value internal
     */
    @Override
    protected String internalGetValue(final int row, final int col) {

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

    /**
     * Returns whether the given row is an outlier
     * @param row
     * @return
     */
    protected boolean internalIsOutlier(final int row) {
        return ((dataQI.getArray()[row][0] & Data.OUTLIER_MASK) != 0);
    }
    
    /**
     * Swap internal.
     * 
     * @param row1
     *            the row1
     * @param row2
     *            the row2
     */
    protected void internalSwap(final int row1, final int row2) {
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
        temp = source.getArray()[row1];
        source.getArray()[row1] = source.getArray()[row2];
        source.getArray()[row2] = temp;
    }
}
