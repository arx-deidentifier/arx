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

import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;

/**
 * A model for interval-based builders.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardModelIntervals<T> extends HierarchyWizardModelGrouping<T>{
    
    /** Var. */
    private final String[] data;

    /**
     * Constructor to create an initial definition.
     *
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelIntervals(final DataType<T> dataType, String[] data) {
        super(data, dataType, true);
        this.data = data;
        this.update();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelAbstract#getBuilder(boolean)
     */
    @Override
    public HierarchyBuilderIntervalBased<T> getBuilder(boolean serializable) throws Exception{
        HierarchyBuilderIntervalBased<T> builder = HierarchyBuilderIntervalBased.create(super.getDataType(),
                                                    new Range<T>(super.getLowerRange().repeat,
                                                                 super.getLowerRange().snap,
                                                                 super.getLowerRange().label),
                                                    new Range<T>(super.getUpperRange().repeat,
                                                                 super.getUpperRange().snap,
                                                                 super.getUpperRange().label));
        
        builder.setAggregateFunction(this.getDefaultFunction());

        for (HierarchyWizardGroupingInterval<T> interval : super.getIntervals()) {
            builder.addInterval(interval.min, interval.max, interval.function);
        }
        
        int level = 0;
        for (List<HierarchyWizardGroupingGroup<T>> list : super.getModelGroups()) {
            for (HierarchyWizardGroupingGroup<T> group : list){
                builder.getLevel(level).addGroup(group.size, group.function);
            }
            level++;
        }
        
        return builder;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelAbstract#parse(org.deidentifier.arx.aggregates.HierarchyBuilder)
     */
    @Override
    public void parse(HierarchyBuilder<T> builder) throws IllegalArgumentException {
        
        if (!(builder instanceof HierarchyBuilderIntervalBased)) {
            return;
        }
        super.parse((HierarchyBuilderIntervalBased<T>)builder);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelAbstract#build()
     */
    @Override
    protected void build() {
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;
        
        if (data==null) return;
        
        HierarchyBuilderIntervalBased<T> builder = null;
        try {
            builder = getBuilder(false);
        } catch (Exception e){
            super.error = e.getMessage();
            return;
        }
        
        String error = builder.isValid();
        if (error != null) {
            super.error = error;
            return;
        }
        
        try {
            super.groupsizes = builder.prepare(data);
        } catch(Exception e){
            super.error = e.getMessage();
            return;
        }
        
        try {
            super.hierarchy = builder.build();
        } catch(Exception e){
            super.error = e.getMessage();
            return;
        }
    }
}
