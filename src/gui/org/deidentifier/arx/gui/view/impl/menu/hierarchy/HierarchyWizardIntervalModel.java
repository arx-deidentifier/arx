package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;

public class HierarchyWizardIntervalModel<T> extends HierarchyWizardGroupingModel<T>{

    private final String[] data;
    
    public HierarchyWizardIntervalModel(DataType<T> dataType, String[] data) {
        super(dataType, true);
        this.data = data;
    }

    public HierarchyWizardIntervalModel(HierarchyBuilderIntervalBased<T> builder, String[] data) {
        super(builder);
        this.data = data;
    }
    
    public String[] getData() {
        return data;
    }
}
