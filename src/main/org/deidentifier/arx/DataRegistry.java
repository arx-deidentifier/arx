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

package org.deidentifier.arx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
import org.deidentifier.arx.criteria.DPresence;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * This class implements sorting and swapping for a set of paired data handles.
 *
 * @author Fabian Prasser
 */
class DataRegistry {

    /** The input handle, if any. */
    private DataHandleInput input;
    
    /** The input subset handle, if any. */
    private DataHandleSubset inputSubset;
    
    /** The output handle, if any. */
    private Map<ARXNode, DataHandleOutput> output = new HashMap<ARXNode, DataHandleOutput>();
    
    /** The output subset handle, if any. */
    private Map<ARXNode, DataHandleSubset> outputSubset = new HashMap<ARXNode, DataHandleSubset>();

    /**
     * Default constructor.
     */
    public DataRegistry(){
        // Empty by design
    }
    
    /**
     * Helper that creates a view on a research subset.
     *
     * @param handle
     * @param subset
     * @param eqStatistics
     * @return
     */
    private DataHandleSubset createSubset(DataHandle handle, DataSubset subset, StatisticsEquivalenceClasses eqStatistics) {
        
        DataHandleSubset result = new DataHandleSubset(handle, subset, eqStatistics);
        result.setRegistry(this);
        return result;
    }
    
    /**
     * Returns any of the registered subsets.
     *
     * @return
     */
    private DataHandleSubset getSubset() {
        DataHandleSubset subset = null;
        if (inputSubset!=null){
            subset = inputSubset;
        } else if (!outputSubset.isEmpty()){
            return outputSubset.values().iterator().next();
        }
        return subset;
    }
    
    /**
     * Sort.
     *
     * @param handle
     * @param swapper
     * @param from
     * @param to
     * @param ascending
     * @param columns
     */
    private void sortAll(final DataHandle handle,
                         final Swapper swapper,
                         final int from,
                         final int to,
                         final boolean ascending,
                         final int... columns) {

        final DataHandle outer = handle;
        final DataHandleSubset subset = getSubset();
        
        final IntComparator c = new IntComparator() {
            @Override
            public int compare(final int arg0, final int arg1) {
                return outer.internalCompare(arg0, arg1, columns, ascending);
            }
        };
        final Swapper s = new Swapper() {
            @Override
            public void swap(final int arg0, final int arg1) {
                if (input != null) input.internalSwap(arg0, arg1);
                for (DataHandleOutput handle : output.values()) handle.internalSwap(arg0, arg1);
                if (subset != null) subset.internalSwap(arg0, arg1);
                if (swapper != null) swapper.swap(arg0, arg1);
            }
        };
        
        GenericSorting.mergeSort(from, to, c, s);
        
        if (subset != null){
            subset.internalRebuild();
        }
    }
    
    /**
     * Sort.
     *
     * @param handle
     * @param swapper
     * @param from
     * @param to
     * @param ascending
     * @param columns
     */
    private void sortSubset(final DataHandleSubset handle,
                              final Swapper swapper, 
                              final int from,
                              final int to,
                              final boolean ascending,
                              final int... columns) {

        final DataHandleSubset outer = handle;
        final IntComparator c = new IntComparator() {
            @Override
            public int compare(final int arg0, final int arg1) {
                return outer.internalCompare(arg0, arg1, columns, ascending);
            }
        };
        final Swapper s = new Swapper() {
            @Override
            public void swap(final int arg0, final int arg1) {
                if (input != null) input.internalSwap(outer.internalTranslate(arg0), outer.internalTranslate(arg1));
                for (DataHandleOutput handle : output.values()) handle.internalSwap(outer.internalTranslate(arg0), outer.internalTranslate(arg1));
                if (swapper != null) swapper.swap(outer.internalTranslate(arg0), outer.internalTranslate(arg1));
            }
        };
        
        // No need to swap and rebuild the subset views
        GenericSorting.mergeSort(from, to, c, s);
    }

    /**
     * Swap.
     *
     * @param handle
     * @param row1
     * @param row2
     */
    private void swapAll(DataHandle handle, int row1, int row2) {
        if (input!=null) input.internalSwap(row1, row2);
        for (DataHandleOutput outhandle : output.values()) outhandle.internalSwap(row1, row2);
        
        // Important to swap in only one subset
        DataHandleSubset subset = getSubset();
        if (subset!=null){
            subset.internalSwap(row1, row2);
            subset.internalRebuild();
        }
    }
    
    /**
     * Swap.
     *
     * @param handle
     * @param row1
     * @param row2
     */
    private void swapSubset(DataHandleSubset handle, int row1, int row2) {
        
        // Nothing to do for subsets
        row1 = handle.internalTranslate(row1);
        row2 = handle.internalTranslate(row2);
        if (input!=null) input.internalSwap(row1, row2);
        for (DataHandleOutput outhandle : output.values()) outhandle.internalSwap(row1, row2);
    }
    
    /**
     * Creates the views on the subset.
     *
     * @param config
     */
    protected void createInputSubset(ARXConfiguration config){
        
        if (config.containsCriterion(DPresence.class)) {
            this.inputSubset = createSubset(this.input, config.getCriterion(DPresence.class).getSubset(), null);
        } else {
            this.inputSubset = null;
        }
        this.input.setView(this.inputSubset);
    }

    /**
     * Creates the views on the subset.
     *
     * @param node
     * @param config
     * @param peqStatistics
     */
    protected void createOutputSubset(ARXNode node, ARXConfiguration config, StatisticsEquivalenceClasses peqStatistics){
        if (config.containsCriterion(DPresence.class)) {
            this.outputSubset.put(node, createSubset(this.output.get(node), config.getCriterion(DPresence.class).getSubset(), peqStatistics));
        } else {
            this.outputSubset.remove(node);
        }
        this.output.get(node).setView(this.outputSubset.get(node));
    }

    /**
     * Returns the base data type without generalization.
     *
     * @param attribute
     * @return
     */
    protected DataType<?> getBaseDataType(String attribute) {
        return this.input.getBaseDataType(attribute);
    }

    /**
     * Returns a registered handle, if any.
     *
     * @param node
     * @return
     */
    protected DataHandle getOutputHandle(ARXNode node) {
        return this.output.get(node);
    }
    
    /**
     * Implementation of {@link DataHandle#isOutlier(row)}.
     *
     * @param handle
     * @param row
     * @return
     */
    protected boolean isOutlier(DataHandle handle, int row){
        
        if (handle instanceof DataHandleInput){
            return false;
        } else if (handle instanceof DataHandleOutput){
            return ((DataHandleOutput)handle).internalIsOutlier(row);
        } else if (handle instanceof DataHandleSubset){
            return isOutlier(((DataHandleSubset)handle).getSource(), row);
        } else {
            throw new RuntimeException("Illegal state");
        }
    }

    /**
     * Releases the given handle.
     *
     * @param handle
     */
    protected void release(DataHandle handle) {
        
        // Handle subsets
        if (handle instanceof DataHandleSubset) {
           return;
        }
        
        // Handle output
        Iterator<Entry<ARXNode, DataHandleOutput>> iter = output.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<ARXNode, DataHandleOutput> entry = iter.next();
            if (entry.getValue().equals(handle)) {
                outputSubset.remove(entry.getKey());
                iter.remove();
                handle.doRelease();
                return;
            }
        }
        
        // Handle input
        if (handle.equals(input)) {
            this.reset();
            input.doRelease();
        }
    }

    /**
     * Removes the association to all handles, but the input handle.
     */
    protected void reset() {
        while (!this.output.entrySet().isEmpty()) {
            Entry<ARXNode, DataHandleOutput> entry = this.output.entrySet().iterator().next();
            release(entry.getValue());
            this.output.remove(entry.getKey());
        }
        this.output.clear();
        
        for (DataHandle handle : this.outputSubset.values()) {
            handle.setRegistry(null);
        }
        this.outputSubset.clear();
        
        if (this.inputSubset != null) {
            this.inputSubset.setRegistry(null);
            this.inputSubset = null;
        }
    }
    
    /**
     * Implementation of {@link DataHandle#sort(boolean, int...)}
     * @param handle
     * @param ascending
     * @param columns
     */
    protected void sort(final DataHandle handle, final boolean ascending, final int... columns) {
        sort(handle, 0, handle.getNumRows(), ascending, columns);
    }

    /**
     * Implementation of {@link DataHandle#sort(int, int, boolean, int...)}
     * @param handle
     * @param from
     * @param to
     * @param ascending
     * @param columns
     */
    protected void sort(final DataHandle handle,
                     final int from,
                     final int to,
                     final boolean ascending,
                     final int... columns) {
        this.sort(handle, null, from, to, ascending, columns);
    }

    /**
     * Implementation of {@link DataHandle#sort(Swapper, boolean, int...)}
     * @param handle
     * @param swapper
     * @param ascending
     * @param columns
     */
    protected void sort(final DataHandle handle, final Swapper swapper, final boolean ascending, final int... columns) {
        sort(handle, swapper, 0, handle.getNumRows(), ascending, columns);
    }

    /**
     * Implementation of {@link DataHandle#sort(Swapper, int, int, boolean, int...)}
     * @param handle
     * @param swapper
     * @param from
     * @param to
     * @param ascending
     * @param columns
     */
    protected void sort(final DataHandle handle,
                        final Swapper swapper,
                        final int from,
                        final int to,
                        final boolean ascending,
                        final int... columns) {
        handle.checkColumns(columns);
        handle.checkRow(from, handle.getNumRows());
        handle.checkRow(to, handle.getNumRows());
        
        if (handle instanceof DataHandleSubset){
            sortSubset((DataHandleSubset)handle, swapper, from, to, ascending, columns);
        } else {
            sortAll(handle, swapper, from, to, ascending, columns);
        }
    }

    /**
     * Implementation of {@link DataHandle#swap(int, int)}.
     *
     * @param handle
     * @param row1
     * @param row2
     */
    protected void swap(DataHandle handle, int row1, int row2) {
        if (handle instanceof DataHandleSubset){
            swapSubset((DataHandleSubset)handle, row1, row2);
        } else {
            swapAll(handle, row1, row2);
        }
    }

    /**
     * Update the registry.
     *
     * @param input
     */
    protected void updateInput(DataHandleInput input){
        this.input = input;
    }

    /**
     * Update the registry.
     *
     * @param inputSubset
     */
    protected void updateInputSubset(DataHandleSubset inputSubset){
        this.inputSubset = inputSubset;
    }

    /**
     * Update the registry.
     *
     * @param node
     * @param output
     */
    protected void updateOutput(ARXNode node, DataHandleOutput output){
        this.output.put(node, output);
    }

    /**
     * Update the registry.
     *
     * @param node
     * @param outputSubset
     */
    protected void updateOutputSubset(ARXNode node, DataHandleSubset outputSubset){
        this.outputSubset.put(node, outputSubset);
    }
}