package org.deidentifier.arx.gui.view.impl.wizards;

import java.util.List;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;

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
     * @param builder 
     */
    @SuppressWarnings("unchecked")
    public HierarchyWizardModel(DataType<T> dataType, String[] data, HierarchyBuilder<?> builder){
        
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
        
        try {
            if (builder != null){
                this.setSpecification((HierarchyBuilder<T>)builder);
            }
        } catch (Exception e){
            // Ignore
            e.printStackTrace();
        }
    }

    /**
     * Returns the current builder
     * @return
     */
    public HierarchyBuilder<T> getBuilder() {
        if (type == Type.INTERVAL_BASED) {
            return intervalModel.getBuilder();
        } else if (type == Type.REDACTION_BASED) {
            return redactionModel.getBuilder();
        } else if (type == Type.ORDER_BASED) {
            return orderModel.getBuilder();
        } else {
            throw new IllegalArgumentException("Unknown type of builder");
        }
    }
    
    /**
     * Returns the data type
     * @return
     */
    public DataType<T> getDataType() {
        return this.dataType;
    }

    /**
     * Returns the current hierarchy
     * @return
     */
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
    
    public HierarchyWizardModelIntervals<T> getIntervalModel() {
        return intervalModel;
    }
    
    public HierarchyWizardModelOrder<T> getOrderModel() {
        return orderModel;
    }
    
    public HierarchyWizardModelRedaction<T> getRedactionModel() {
        return redactionModel;
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
        if (type == Type.INTERVAL_BASED) {
            if (intervalModel != null){
                this.intervalModel.parse((HierarchyBuilderIntervalBased<T>)builder);
                this.type = Type.INTERVAL_BASED;
            }
        } else if (type == Type.ORDER_BASED) {
            this.orderModel.parse((HierarchyBuilderOrderBased<T>)builder);
            this.type = Type.ORDER_BASED;
        } else if (type == Type.REDACTION_BASED) {
            this.redactionModel.parse(builder);
            this.type = Type.REDACTION_BASED;
        } else {
            throw new IllegalArgumentException("Unknown type of builder");
        }
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
     * Simple comparison of data types
     * @param type
     * @param other
     * @return
     */
    private boolean equals(DataType<?> type, DataType<?> other){
        return type.getDescription().getLabel().equals(other.getDescription().getLabel());
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
}
