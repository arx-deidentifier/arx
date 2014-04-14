package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased.Range;

public class HierarchyWizardModelInterval<T> extends HierarchyWizardModelGrouping<T>{
    
    private final String[] data;

    /**
     * Constructor to create an initial definition
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelInterval(final DataType<T> dataType, String[] data) {
        super(data, dataType, true);
        this.data = data;
        this.update();
    }

    /**
     * Constructor used to load a definition. The previous sort order of the items is preserved
     * @param builder
     * @param data
     */
    public HierarchyWizardModelInterval(HierarchyBuilderIntervalBased<T> builder, String[] data) {
        super(data, builder);
        this.data = data;
        this.update();
    }

  
    @Override
    protected void internalUpdate() {
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;
        
        HierarchyBuilderIntervalBased<T> builder = HierarchyBuilderIntervalBased.create(
                                super.getDataType(),
                                new Range<T>(super.getLowerRange().repeat,
                                             super.getLowerRange().snap,
                                             super.getLowerRange().label),
                                new Range<T>(super.getUpperRange().repeat,
                                             super.getUpperRange().snap,
                                             super.getUpperRange().label)

        );
        
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
