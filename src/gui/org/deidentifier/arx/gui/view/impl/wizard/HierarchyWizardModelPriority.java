/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

import java.util.Arrays;
import java.util.Comparator;
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
        return HierarchyBuilderPriorityBased.create(dataType, this.maxLevels);
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
    
    @Override
    public void parse(HierarchyBuilder<T> _builder) {
        
        if (!(_builder instanceof HierarchyBuilderPriorityBased)) {
            return;
        }
        this.maxLevels = ((HierarchyBuilderPriorityBased<?>)_builder).getMaxLevels();
        this.update();
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
        if (data==null) return;
        
        // Prepare
        String[] array = data.clone();
        
        // Build prioritized array of items
        if (priority == Priority.FREQUENCY_HIGHEST_TO_LOWEST || priority == Priority.FREQUENCY_LOWEST_TO_HIGHEST) {
            
            // Check
            if (frequency != null) {
                Arrays.sort(array, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        int prio1 = frequency.getOrDefault(o1, 0);
                        int prio2 = frequency.getOrDefault(o2, 0);
                        return (priority == Priority.FREQUENCY_HIGHEST_TO_LOWEST) ? -Integer.compare(prio1, prio2) : Integer.compare(prio1, prio2); 
                    }
                });
            }
            
        } else if (priority == Priority.ORDER_HIGHEST_TO_LOWEST || priority == Priority.ORDER_LOWEST_TO_HIGHEST) {
            
            // Check
            if (dataType != null) {
                Arrays.sort(array, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        try {
                            return (priority == Priority.ORDER_HIGHEST_TO_LOWEST) ? -dataType.compare(o1, o2) : dataType.compare(o1, o2);
                        } catch (Exception e) {
                            return 0;
                        } 
                    }
                });
            }
        }
        
        HierarchyBuilderPriorityBased<T> builder = getBuilder(false);
        try {
            super.groupsizes = builder.prepare(array);
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
