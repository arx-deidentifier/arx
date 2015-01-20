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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;

/**
 * An abstract base model for all builders.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class HierarchyWizardModelAbstract<T> {

    /** Var. */
    protected final String[]      data;
    
    /** Var. */
    protected int[]               groupsizes;
    
    /** Var. */
    protected Hierarchy           hierarchy;
    
    /** Var. */
    protected String              error;
    
    /** Var. */
    protected HierarchyWizardView view;
    
    /** Var. */
    protected boolean             visible = false;

    /**
     * Creates a new instance.
     *
     * @param data
     */
    public HierarchyWizardModelAbstract(String[] data) {
        this.data = data;
    }
    
    /**
     * Returns the builder currently configured.
     *
     * @param serializable
     * @return
     * @throws Exception
     */
    public abstract HierarchyBuilder<T> getBuilder(boolean serializable) throws Exception;

    /**
     * Returns the data.
     *
     * @return
     */
    public String[] getData() {
        return data;
    }

    /**
     * Returns an error message, null if everything is ok.
     *
     * @return
     */
    public String getError() {
        return error;
    }

    /**
     * Returns the sizes of the resulting groups.
     *
     * @return
     */
    public int[] getGroups() {
        return groupsizes;
    }
    
    /**
     * Returns the resulting hierarchy.
     *
     * @return
     */
    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    /**
     * Parses a builder and updates the model accordingly.
     *
     * @param builder
     */
    public abstract void parse(HierarchyBuilder<T> builder);
    
    /**
     * Sets the according view.
     *
     * @param view
     */
    public void setView(HierarchyWizardView view){
        this.view = view;
    }
    
    /**
     * Updates the resulting hierarchy and the view.
     */
    public void update(){
        if (visible) build();
        if (view != null) view.update();
    }
    
    /** 
     * Updates all UI components apart from the sender
     */
    public abstract void updateUI(HierarchyWizardView sender);
    
    /**
     * Implement this to run the builder.
     */
    protected abstract void build();
    
    /**
     * Set visible.
     *
     * @param visible
     */
    protected void setVisible(boolean visible){
        this.visible = visible;
        if (visible) update();
    }
}
