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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;

/**
 * An abstract base model for all builders
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class HierarchyWizardModelAbstract<T> {

    /** Var */
    protected final String[]      data;
    /** Var */
    protected int[]               groupsizes;
    /** Var */
    protected Hierarchy           hierarchy;
    /** Var */
    protected String              error;
    /** Var */
    protected HierarchyWizardView view;
    /** Var */
    protected boolean             visible = false;

    /**
     * Creates a new instance
     * @param data
     */
    public HierarchyWizardModelAbstract(String[] data) {
        this.data = data;
    }
    
    /**
     * Set visible
     * @param visible
     */
    protected void setVisible(boolean visible){
        this.visible = visible;
        if (visible) update();
    }

    /**
     * Returns the builder currently configured
     * @return
     */
    public abstract HierarchyBuilder<T> getBuilder(boolean serializable) throws Exception;

    /**
     * Returns the data
     * @return
     */
    public String[] getData() {
        return data;
    }

    /**
     * Returns an error message, null if everything is ok
     * @return
     */
    public String getError() {
        return error;
    }
    
    /**
     * Returns the sizes of the resulting groups
     * @return
     */
    public int[] getGroups() {
        return groupsizes;
    }

    /**
     * Returns the resulting hierarchy
     * @return
     */
    public Hierarchy getHierarchy() {
        return hierarchy;
    }
    
    /**
     * Parses a builder and updates the model accordingly
     * @param builder
     */
    public abstract void parse(HierarchyBuilder<T> builder);
    
    /**
     * Sets the according view
     * @param view
     */
    public void setView(HierarchyWizardView view){
        this.view = view;
    }
    
    /**
     * Updates the resulting hierarchy and the view
     */
    public void update(){
        if (visible) build();
        if (view != null) view.update();
    }
    
    /**
     * Implement this to run the builder
     */
    protected abstract void build();
}
