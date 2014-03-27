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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.DataStatistics.EquivalenceClassStatistics;
import org.deidentifier.arx.criteria.DPresence;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/**
 * This class implements sorting and swapping for a set of paired data handles
 * @author Fabian Prasser
 */
class DataRegistry {

    /** The input handle, if any*/
    private DataHandleInput input;
    /** The input subset handle, if any*/
    private DataHandleSubset inputSubset;
    /** The output handle, if any*/
    private Map<ARXNode, DataHandleOutput> output = new HashMap<ARXNode, DataHandleOutput>();
    /** The output subset handle, if any*/
    private Map<ARXNode, DataHandleSubset> outputSubset = new HashMap<ARXNode, DataHandleSubset>();

    /**
     * Default constructor
     */
    public DataRegistry(){
        // Empty by design
    }
    
    /**
     * Helper that creates a view on a research subset
     * @param handle
     * @param subset
     * @return
     */
    private DataHandleSubset createSubset(DataHandle handle, DataSubset subset, EquivalenceClassStatistics eqStatistics) {
        
        DataHandleSubset result = new DataHandleSubset(handle, subset, eqStatistics);
        result.setRegistry(this);
        return result;
    }
    
    /**
     * Returns any of the registered subsets
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
     * Sort
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
     * Sort
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
     * Swap
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
     * Swap
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
     * Creates the views on the subset
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
     * Returns the base data type without generalization
     * @param attribute
     * @return
     */
    protected DataType<?> getBaseDataType(String attribute) {
        return this.input.getBaseDataType(attribute);
    }

    /**
     * Implementation of {@link DataHandle#isOutlier(row)}
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
     * Removes the association to all handles, but the input handle
     */
    protected void reset() {
        for (DataHandle handle : this.output.values()) {
            release(handle);
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
     * Implementation of {@link DataHandle#swap(int, int)}
     * @param dataHandle
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
     * Update the registry
     * @param input
     */
    protected void updateInput(DataHandleInput input){
        this.input = input;
    }

    /**
     * Update the registry
     * @param inputSubset
     */
    protected void updateInputSubset(DataHandleSubset inputSubset){
        this.inputSubset = inputSubset;
    }

    /**
     * Update the registry
     * @param output
     */
    protected void updateOutput(ARXNode node, DataHandleOutput output){
        this.output.put(node, output);
    }

    /**
     * Update the registry
     * @param outputSubset
     */
    protected void updateOutputSubset(ARXNode node, DataHandleSubset outputSubset){
        this.outputSubset.put(node, outputSubset);
    }

    /**
     * Returns a registered handle, if any
     * @param node
     * @return
     */
    protected DataHandle getOutputHandle(ARXNode node) {
        return this.output.get(node);
    }

    /**
     * Creates the views on the subset
     * @param peqStatistics 
     */
    protected void createOutputSubset(ARXNode node, ARXConfiguration config, EquivalenceClassStatistics peqStatistics){
        if (config.containsCriterion(DPresence.class)) {
            this.outputSubset.put(node, createSubset(this.output.get(node), config.getCriterion(DPresence.class).getSubset(), peqStatistics));
        } else {
            this.outputSubset.remove(node);
        }
        this.output.get(node).setView(this.outputSubset.get(node));
    }

    /**
     * Releases the given handle
     * @param dataHandle
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
}