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

package org.deidentifier.arx.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.explore.ViewClipboard;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * A model for the clipboard.
 *
 * @author Fabian Prasser
 */
public class ModelClipboard {

    /** The clipboard, an ordered list of nodes. */
    private transient List<ARXNode> clipboard = new ArrayList<ARXNode>();
    
    /** Is this modified. */
    private boolean modified = false;

    /**
     * Add a set of elements to the clipboard.
     *
     * @param list
     */
    public void addAllToClipboard(List<ARXNode> list) {
        this.setModified();
        if (this.clipboard == null) {
            this.clipboard = new ArrayList<ARXNode>();
        }
        this.clipboard.addAll(list);
    }
    
    /**
     * Extracts interesting transformations from the given result
     * @param model
     */
    public void addInterestingTransformations(Model model) {

        // If there is no result, return
        ARXResult result = model.getResult();
        if (model == null || result == null || !result.isResultAvailable()) {
            return;
        }

        // Collect top-10 in terms of best score
        List<ARXNode> utility = new ArrayList<ARXNode>();
        utility.add(result.getGlobalOptimum());
        collectTopSolutions(utility, result.getLattice(), new Comparator<ARXNode>(){
            @Override
            public int compare(ARXNode o1, ARXNode o2) {
                return o1.getHighestScore().compareTo(o2.getHighestScore());
            }
        }, 10);
        
        // Collect top-10 in terms of lowest generalization degree
        final DataDefinition definition = model.getOutputDefinition() != null ? model.getOutputDefinition() : model.getInputDefinition();
        List<ARXNode> generalization = new ArrayList<ARXNode>();
        collectTopSolutions(generalization, result.getLattice(), new Comparator<ARXNode>(){
            @Override
            public int compare(ARXNode o1, ARXNode o2) {
                double val1 = 0d;
                double val2 = 0d;
                for (int i = 0; i< o1.getTransformation().length; i++) {
                    double max = (double)definition.getMaximumGeneralization(o1.getQuasiIdentifyingAttributes()[i]);
                    max = max > 0d ? max : 1d;
                    val1 += (double)o1.getTransformation()[i] / max;
                    val2 += (double)o2.getTransformation()[i] / max;
                }
                int cmp = Double.valueOf(val1).compareTo(val2);
                if (cmp == 0) {
                    return o1.getHighestScore().compareTo(o2.getHighestScore());
                } else {
                    return cmp;
                }
            }
        }, 10);

        // TODO: It would be interesting to collect the number of suppressed records per transformation
        // TODO: and extract the top-10 solutions as well
        
        // Add to clip board
        ARXNode optimum = utility.isEmpty() ? null : utility.remove(0);
        if (optimum != null) {
            optimum.getAttributes().put(ViewClipboard.NODE_COMMENT, Resources.getMessage("ModelClipboard.0")); //$NON-NLS-1$
            this.addToClipboard(optimum);
        }
        int rank = 2;
        for (ARXNode node : utility) {
            node.getAttributes().put(ViewClipboard.NODE_COMMENT, Resources.getMessage("ModelClipboard.1") + (rank++) +Resources.getMessage("ModelClipboard.2")); //$NON-NLS-1$ //$NON-NLS-2$
            this.addToClipboard(node);
        }
        optimum = generalization.isEmpty() ? null : generalization.remove(0);
        if (optimum != null) {
            optimum.getAttributes().put(ViewClipboard.NODE_COMMENT, Resources.getMessage("ModelClipboard.3")); //$NON-NLS-1$
            this.addToClipboard(optimum);
        }
        rank = 2;
        for (ARXNode node : generalization) {
            node.getAttributes().put(ViewClipboard.NODE_COMMENT, Resources.getMessage("ModelClipboard.4") + (rank++) +Resources.getMessage("ModelClipboard.5")); //$NON-NLS-1$ //$NON-NLS-2$
            this.addToClipboard(node);
        }
        this.setModified();
    }
    
    /**
     * Add a node to the clipboard.
     *
     * @param node
     */
    public void addToClipboard(ARXNode node) {
        if (this.clipboard == null) {
            this.clipboard = new ArrayList<ARXNode>();
        }
        if (!this.clipboard.contains(node)) {
            setModified();
            clipboard.add(node);
        }
    }

    /**
     * Clear the clipboard.
     */
    public void clearClipboard() {
        if (this.clipboard == null) {
            this.clipboard = new ArrayList<ARXNode>();
        }
        if (!this.clipboard.isEmpty()) {
            setModified();
        }
        clipboard.clear();
    }

    /**
     * Returns a copy of all clipboard entries.
     *
     * @return
     */
    public List<ARXNode> getClipboardEntries() {
        if (this.clipboard == null) {
            this.clipboard = new ArrayList<ARXNode>();
        }
        return new ArrayList<ARXNode>(this.clipboard);
    }
    
    /**
     * Is the clipboard modified.
     *
     * @return
     */
    public boolean isModified() {
        return this.modified;
    }

    /**
     * Moves the entry down.
     *
     * @param node
     */
    public void moveEntryDown(ARXNode node){
        int index = clipboard.indexOf(node);
        if (index<clipboard.size()-1){
            clipboard.remove(index);
            clipboard.add(index+1, node);
        }
    }
    
    /**
     * Moves the entry up.
     *
     * @param node
     */
    public void moveEntryUp(ARXNode node){
        int index = clipboard.indexOf(node);
        if (index>0){
            clipboard.remove(index);
            clipboard.add(index-1, node);
        }
    }
    
    /**
     * Removes an entry from the clip board.
     *
     * @param node
     */
    public void removeFromClipboard(ARXNode node) {
        if (this.clipboard == null) {
            this.clipboard = new ArrayList<ARXNode>();
        }
        if (this.clipboard.remove(node)){
            setModified();
        }
    }

    /**
     * Sets as unmodified.
     */
    public void setUnmodified(){
        this.modified = false;
    }

    /**
     * Sorts all nodes according to their minimal score.
     */
    public void sort() {
        Collections.sort(clipboard, new Comparator<ARXNode>(){
            @Override
            public int compare(ARXNode arg0, ARXNode arg1) {
                InformationLoss<?> loss0 = arg0.getLowestScore();
                InformationLoss<?> loss1 = arg1.getLowestScore();
                if (loss0==null && loss1==null) return 0;
                else if (loss0==null && loss1!=null) return -1;
                else if (loss0!=null && loss1==null) return +1;
                else return loss0.compareTo(loss1);
            } 
        });
    }

    /**
     * Returns the top-n solutions
     * @param elements
     * @param lattice
     * @param comparator
     * @param n
     */
    private void collectTopSolutions(List<ARXNode> elements,
                                     ARXLattice lattice,
                                     Comparator<ARXNode> comparator,
                                     int n) {
        
        // For each node
        for (ARXNode[] level : lattice.getLevels()) {
            for (ARXNode node : level) {
                
                // If not already contained
                if (node.getAnonymity() == Anonymity.ANONYMOUS && !elements.contains(node)) {
                    
                    // See if it can be inserted at some point
                    boolean canbeinserted = false;
                    int i = elements.size() - 1;
                    for (; i >= -1; i--) {
                        
                        // Break
                        if (i==-1) {
                            canbeinserted = true;
                            break;
                        }
                        
                        // Yes
                        if (comparator.compare(node, elements.get(i)) < 0) {
                            canbeinserted = true;
                            
                        // Maybe
                        } else {
                            break;
                        }
                    }
                    
                    // Insert
                    if (canbeinserted) {

                        elements.add(i + 1, node);
                        
                        // Ensure that we do not return more than n elements
                        while (elements.size() > n) {
                            elements.remove(elements.size() - 1);
                        }
                        
                    } else if (elements.size() < n) {
                        // If it was not inserted but there is still space left, insert
                        elements.add(node);
                    }
                }
            }
        }
    }

    /**
     * Sets as modified.
     */
    private void setModified(){
        this.modified = true;
    }
}