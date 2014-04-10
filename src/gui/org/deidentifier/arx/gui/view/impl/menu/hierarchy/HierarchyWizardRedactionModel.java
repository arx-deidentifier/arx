package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;

public class HierarchyWizardRedactionModel<T> {

    private final String[] data;
    
    public HierarchyWizardRedactionModel(DataType<T> dataType, String[] data) {
        this.data = data;
    }

    public HierarchyWizardRedactionModel(HierarchyBuilderRedactionBased<T> builder, String[] data) {
        this.data = data;
    }

    public String[] getData() {
        return data;
    }
}
