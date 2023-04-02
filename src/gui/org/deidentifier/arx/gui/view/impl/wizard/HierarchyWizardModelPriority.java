/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2023 Fabian Prasser and contributors
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

import java.util.Map;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderPriorityBased;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardView;

/**
 * A model for priority-based builders.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardModelPriority<T> extends HierarchyWizardModelAbstract<T> {
    
    /**
     * Priorities
     * @author Fabian Prasser
     */
    public static enum Priority {
        FREQUENCY_LOWEST_TO_HIGHEST,
        FREQUENCY_HIGHEST_TO_LOWEST,
        ORDER_LOWEST_TO_HIGHEST,
        ORDER_HIGHEST_TO_LOWEST,
    }
    
    /** Frequency*/
    private Map<String, Integer> frequency;

    /** Priority */
    private Priority    priority = Priority.FREQUENCY_HIGHEST_TO_LOWEST;
    
    /** Max levels */
    private int         maxLevels = 10;

    /** Data type */
    private DataType<T> dataType;

    /**
     * Creates a new instance.
     *
     * @param dataType
     * @param data
     * @param frequency
     */
    public HierarchyWizardModelPriority(DataType<T> dataType, 
                                        String[] data,
                                        Map<String, Integer> frequency) {
        
        // Super
        super(data);
        
        // Store
        this.dataType = dataType;
        this.frequency = frequency;
        
        // Update
        this.update();
    }
    
    @Override
    public HierarchyBuilderPriorityBased<T> getBuilder(boolean serializable) {

        if (priority == Priority.FREQUENCY_HIGHEST_TO_LOWEST) {
            return HierarchyBuilderPriorityBased.create(frequency,
                                                        HierarchyBuilderPriorityBased.Priority.HIGHEST_TO_LOWEST,
                                                        maxLevels);
        } else if (priority == Priority.FREQUENCY_LOWEST_TO_HIGHEST) {
            return HierarchyBuilderPriorityBased.create(frequency,
                                                        HierarchyBuilderPriorityBased.Priority.LOWEST_TO_HIGHEST,
                                                        maxLevels);
        } else if (priority == Priority.ORDER_HIGHEST_TO_LOWEST) {
            return HierarchyBuilderPriorityBased.create(dataType,
                                                        HierarchyBuilderPriorityBased.Priority.HIGHEST_TO_LOWEST,
                                                        maxLevels);
        } else if (priority == Priority.ORDER_LOWEST_TO_HIGHEST) {
            return HierarchyBuilderPriorityBased.create(dataType,
                                                        HierarchyBuilderPriorityBased.Priority.LOWEST_TO_HIGHEST,
                                                        maxLevels);
        }
        throw new IllegalStateException("Invalid internal state");
    }
    
    /**
     * Max levels
     * @return
     */
    public int getMaxLevels() {
        return this.maxLevels;
    }
    
    /**
     * Returns the priority
     * @return
     */
    public Priority getPriority() {
        return this.priority;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void parse(HierarchyBuilder<T> hierarchyBuilder) {
        
        if (!(hierarchyBuilder instanceof HierarchyBuilderPriorityBased)) {
            return;
        }
        HierarchyBuilderPriorityBased<?> builder = ((HierarchyBuilderPriorityBased<?>)hierarchyBuilder);
        
        if (builder.getDataType() != null && !builder.getDataType().equals(this.dataType)) {
            return;
        }
        
        this.maxLevels = ((HierarchyBuilderPriorityBased<?>)hierarchyBuilder).getMaxLevels();
        if (builder.getPriorities() != null) {
            this.frequency = builder.getPriorities();
            if (builder.getPriority() == HierarchyBuilderPriorityBased.Priority.HIGHEST_TO_LOWEST) {
                this.priority = Priority.FREQUENCY_HIGHEST_TO_LOWEST;
            } else {
                this.priority = Priority.FREQUENCY_LOWEST_TO_HIGHEST;
            }
        }
        if (builder.getDataType() != null) {
            this.dataType = (DataType<T>)builder.getDataType();
            if (builder.getPriority() == HierarchyBuilderPriorityBased.Priority.HIGHEST_TO_LOWEST) {
                this.priority = Priority.ORDER_HIGHEST_TO_LOWEST;
            } else {
                this.priority = Priority.ORDER_LOWEST_TO_HIGHEST;
            }
        }
    }
    
    /**
     * Set max levels
     * @param maxLevels
     */
    public void setMaxLevels(int maxLevels) {
        if (this.maxLevels != maxLevels) {
            this.maxLevels = maxLevels;
            this.update();
        }
    }

    /**
     * Sets the priority
     *
     * @param priority
     */
    public void setPriority(Priority priority) {
        if (priority != this.priority) {
            this.priority = priority;
            this.update();
        }
    }
    
    @Override
    public void updateUI(HierarchyWizardView sender) {
        // Empty by design
    }
    
    @Override
    protected void build() {
        
        // Clear
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;

        // Check
        if (data == null) return;
        
        // Prepare
        HierarchyBuilderPriorityBased<T> builder = getBuilder(false);
        
        try {
            super.groupsizes = builder.prepare(data);
        } catch(Exception e){
            super.error = Resources.getMessage("HierarchyWizardModelRedaction.0"); //$NON-NLS-1$
            return;
        }
        
        try {
            super.hierarchy = builder.build();
        } catch(Exception e){
            super.error = Resources.getMessage("HierarchyWizardModelRedaction.1"); //$NON-NLS-1$
            return;
        }
    }
}
