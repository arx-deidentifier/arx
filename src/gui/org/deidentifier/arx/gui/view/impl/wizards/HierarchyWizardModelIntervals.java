package org.deidentifier.arx.gui.view.impl.wizards;

import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;

public class HierarchyWizardModelIntervals<T> extends HierarchyWizardModelGrouping<T>{
    
    private final String[] data;

    /**
     * Constructor to create an initial definition
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelIntervals(final DataType<T> dataType, String[] data) {
        super(data, dataType, true);
        this.data = data;
        this.update();
    }

    @Override
    public HierarchyBuilderIntervalBased<T> getBuilder() {
        HierarchyBuilderIntervalBased<T> builder = HierarchyBuilderIntervalBased.create(super.getDataType(),
                                                    new Range<T>(super.getLowerRange().repeat,
                                                                 super.getLowerRange().snap,
                                                                 super.getLowerRange().label),
                                                    new Range<T>(super.getUpperRange().repeat,
                                                                 super.getUpperRange().snap,
                                                                 super.getUpperRange().label));

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

    @Override
    public void parse(HierarchyBuilder<T> builder) throws IllegalArgumentException {
        
        if (!(builder instanceof HierarchyBuilderIntervalBased)) {
            return;
        }
        super.parse((HierarchyBuilderIntervalBased<T>)builder);
    }

    @Override
    protected void internalUpdate() {
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;
        
        HierarchyBuilderIntervalBased<T> builder = getBuilder();
        
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
