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

package org.deidentifier.arx.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
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
     * Removes an entry from the clipboard.
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
     * Sorts all nodes according to their minimal information loss.
     */
    public void sort() {
        Collections.sort(clipboard, new Comparator<ARXNode>(){
            @Override
            public int compare(ARXNode arg0, ARXNode arg1) {
                InformationLoss<?> loss0 = arg0.getMinimumInformationLoss();
                InformationLoss<?> loss1 = arg1.getMinimumInformationLoss();
                if (loss0==null && loss1==null) return 0;
                else if (loss0==null && loss1!=null) return -1;
                else if (loss0!=null && loss1==null) return +1;
                else return loss0.compareTo(loss1);
            } 
        });
    }

    /**
     * Sets as modified.
     */
    private void setModified(){
        this.modified = true;
    }
}