package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;

public class HierarchyWizardOrderModel<T> extends HierarchyWizardGroupingModel<T>{

    public HierarchyWizardOrderModel(DataType<T> dataType, String[] data) {
        super(dataType, false);
        // TODO: Create initial order based on data type
    }

    public HierarchyWizardOrderModel(HierarchyBuilderOrderBased<T> builder,
                                     String[] data) {
        super(builder);
    }
}
