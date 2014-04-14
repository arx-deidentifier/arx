package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.List;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;

public class HierarchyWizardModel<T> {

    private Type type;
    private HierarchyWizardModelOrder<T>   orderModel;
    private HierarchyWizardModelIntervals<T>   intervalModel;
    private HierarchyWizardModelRedaction<T>  redactionModel;
    private final DataType<T> dataType;
    private final String[] data;
    
    /**
     * Creates a new instance for the given data type
     * @param dataType
     * @param data
     */
    public HierarchyWizardModel(DataType<T> dataType, String[] data){
        
        // Store
        this.data = data;
        this.dataType = dataType;
        
        // Create models
        orderModel = new HierarchyWizardModelOrder<T>(dataType, getOrderData());
        if (dataType instanceof DataTypeWithRatioScale){
            intervalModel = new HierarchyWizardModelIntervals<T>(dataType, data);
        }
        redactionModel = new HierarchyWizardModelRedaction<T>(dataType, data);
        
        // Propose a dedicated type of builder
        if (equals(dataType, DataType.DATE)) {
            this.type = Type.INTERVAL_BASED;
        } else if (equals(dataType, DataType.DECIMAL)) {
            this.type = Type.INTERVAL_BASED;
        } else if (equals(dataType, DataType.INTEGER)) {
            this.type = Type.INTERVAL_BASED;
        } else if (equals(dataType, DataType.ORDERED_STRING)) {
            this.type = Type.ORDER_BASED;
        } else if (equals(dataType, DataType.STRING)) {
            this.type = Type.REDACTION_BASED;
        }
    }
    
    /**
     * Returns data for the order-based builder
     * @param type
     * @param data
     * @return
     */
    private String[] getOrderData(){
        if (dataType instanceof ARXOrderedString){
            ARXOrderedString os = (ARXOrderedString)dataType;
            List<String> elements = os.getElements();
            if (elements != null && !elements.isEmpty()) {
                return elements.toArray(new String[elements.size()]);
            } 
        } 
        return data;
    }

    /**
     * Simple comparison of data types
     * @param type
     * @param other
     * @return
     */
    private boolean equals(DataType<?> type, DataType<?> other){
        return type.getDescription().getLabel().equals(other.getDescription().getLabel());
    }
    
    /**
     * Sets the type
     * @param type
     */
    public void setType(Type type){
        if (type != this.type) {
            this.type = type;
        }
    }
    
    /**
     * Returns the type
     * @return
     */
    public Type getType(){
        return this.type;
    }
    
    /**
     * Updates the model with a new specification
     * @param builder
     */
    public void setSpecification(HierarchyBuilder<T> builder) throws IllegalArgumentException{
        this.type = builder.getType();
        if (type == Type.INTERVAL_BASED) {
            intervalModel.parse((HierarchyBuilderIntervalBased<T>)builder);
        } else if (type == Type.ORDER_BASED) {
            orderModel.parse((HierarchyBuilderOrderBased<T>)builder);
        } else if (type == Type.REDACTION_BASED) {
            redactionModel.parse((HierarchyBuilderRedactionBased<T>)builder);
        } else {
            throw new IllegalArgumentException("Unknown type of builder");
        }
    }

    public DataType<T> getDataType() {
        return this.dataType;
    }

    public Hierarchy getHierarchy() {
        if (type == Type.INTERVAL_BASED) {
            return intervalModel.getHierarchy();
        } else if (type == Type.REDACTION_BASED) {
            return redactionModel.getHierarchy();
        } else if (type == Type.ORDER_BASED) {
            return orderModel.getHierarchy();
        } else {
            throw new RuntimeException("Unknown type of builder");
        }
    }

    public HierarchyWizardModelRedaction<T> getRedactionModel() {
        return redactionModel;
    }

    public HierarchyWizardModelIntervals<T> getIntervalModel() {
        return intervalModel;
    }

    public HierarchyWizardModelOrder<T> getOrderModel() {
        return orderModel;
    }
}
