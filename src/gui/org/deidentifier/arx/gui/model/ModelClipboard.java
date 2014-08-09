/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.metric.InformationLoss;

/**
 * A model for the clipboard
 * @author Fabian Prasser
 */
public class ModelClipboard {

    /** The clipboard, an ordered list of nodes */
    private transient List<ARXNode> clipboard = new ArrayList<ARXNode>();
    
    /** Is this modified*/
    private boolean modified = false;

    /**
     * Add a set of elements to the clipboard
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
     * Add a node to the clipboard
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
    
    /** Clear the clipboard*/
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
     * Returns a copy of all clipboard entries
     * @return
     */
    public List<ARXNode> getClipboardEntries() {
        if (this.clipboard == null) {
            this.clipboard = new ArrayList<ARXNode>();
        }
        return new ArrayList<ARXNode>(this.clipboard);
    }

    /**
     * Removes an entry from the clipboard
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
     * Moves the entry up
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
     * Moves the entry down
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
     * Sets as modified
     */
    private void setModified(){
        this.modified = true;
    }
    
    /**
     * Sets as unmodified
     */
    public void setUnmodified(){
        this.modified = false;
    }

    /**
     * Is the clipboard modified
     * @return
     */
    public boolean isModified() {
        return this.modified;
    }

    /**
     * Sorts all nodes according to their minimal information loss
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
}