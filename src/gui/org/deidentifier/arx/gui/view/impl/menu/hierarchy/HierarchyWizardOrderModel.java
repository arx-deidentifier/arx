package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;

public class HierarchyWizardOrderModel<T> extends HierarchyWizardGroupingModel<T>{
    
    private final String[] data;

    /**
     * Constructor to create an initial definition
     * @param dataType
     * @param data
     */
    public HierarchyWizardOrderModel(final DataType<T> dataType, String[] data) {
        super(dataType, false);
        if (dataType instanceof ARXOrderedString){
            ARXOrderedString os = (ARXOrderedString)dataType;
            List<String> elements = os.getElements();
            if (!elements.isEmpty()) {
                this.data = elements.toArray(new String[elements.size()]);
            } else {
                this.data = data;
                this.sort();
            }
        } else {
            this.data = data;
            this.sort();
        }
    }

    private void sort() {
        final DataType<T> dataType = super.getDataType();
        Arrays.sort(data, new Comparator<String>(){
            @Override public int compare(String o1, String o2) {
                try {
                    return dataType.compare(o1, o2);
                } catch (NumberFormatException | ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Constructor used to load a definition. The previous sort order of the items is preserved
     * @param builder
     * @param data
     */
    public HierarchyWizardOrderModel(HierarchyBuilderOrderBased<T> builder, String[] data) {
        super(builder);
        this.data = data;
    }

    public String[] getData() {
        return data;
    }

    public Hierarchy getHierarchy() {
        // TODO Auto-generated method stub
        return null;
    }
}
