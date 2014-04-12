package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;

public class HierarchyWizardModel<T> {

    private Type type;
    private HierarchyWizardOrderModel<T>   orderModel;
    private HierarchyWizardIntervalModel<T>   intervalModel;
    private HierarchyWizardRedactionModel<T>  redactionModel;
    private final DataType<T> dataType;
    
    /**
     * Creates a new instance from a given builder
     * @param dataType
     * @param data
     * @param builder
     */
    public HierarchyWizardModel(DataType<T> dataType, String[] data, HierarchyBuilder<T> builder){
        
        this(dataType, data);
        this.type = builder.getType();
        if (type == Type.INTERVAL_BASED) {
            intervalModel = new HierarchyWizardIntervalModel<T>((HierarchyBuilderIntervalBased<T>)builder, data);
        } else if (type == Type.ORDER_BASED) {
            orderModel = new HierarchyWizardOrderModel<T>((HierarchyBuilderOrderBased<T>)builder, data);
        } else if (type == Type.REDACTION_BASED) {
            redactionModel = new HierarchyWizardRedactionModel<T>((HierarchyBuilderRedactionBased<T>)builder, data);
        } else {
            throw new RuntimeException("Unknown type of builder");
        }
    }

    /**
     * Creates a new instance for the given data type
     * @param dataType
     * @param data
     */
    public HierarchyWizardModel(DataType<T> dataType, String[] data){
        
        // Create models
        orderModel = new HierarchyWizardOrderModel<T>(dataType, data);
        if (dataType instanceof DataTypeWithRatioScale){
            intervalModel = new HierarchyWizardIntervalModel<T>(dataType, data);
        }
        redactionModel = new HierarchyWizardRedactionModel<T>(dataType, data);
        
        // Just guess. This can be changed anyways
        this.dataType = dataType;
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
        
        // Initialize
        if (data != null && dataType instanceof DataTypeWithRatioScale){
            @SuppressWarnings("unchecked")
            DataTypeWithRatioScale<T> dataTypeWRS = (DataTypeWithRatioScale<T>)dataType; 
            T min = null;
            T max = null;
            for (String date : data) {
                T value = dataTypeWRS.parse(date);
                if (min==null || dataTypeWRS.compare(value, min) < 0) min = value;
                if (max==null || dataTypeWRS.compare(value, max) > 0) max = value;
            }
            this.intervalModel.getLowerRange().label = min;
            this.intervalModel.getLowerRange().repeat = min;
            this.intervalModel.getLowerRange().snap = min;
            this.intervalModel.getUpperRange().label = max;
            this.intervalModel.getUpperRange().repeat = max;
            this.intervalModel.getUpperRange().snap = max;
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
     * Sets the type
     * @param type
     */
    public void setType(Type type){
        if (type != this.type) {
            this.type = type;
        }
    }
    
    /**
     * Updates the model with a new specification
     * @param builder
     */
    public void setSpecification(HierarchyBuilder<T> builder) {
        this.type = builder.getType();
        if (type == Type.INTERVAL_BASED) {
            intervalModel = new HierarchyWizardIntervalModel<T>((HierarchyBuilderIntervalBased<T>)builder, intervalModel.getData());
        } else if (type == Type.ORDER_BASED) {
            orderModel = new HierarchyWizardOrderModel<T>((HierarchyBuilderOrderBased<T>)builder, orderModel.getData());
        } else if (type == Type.REDACTION_BASED) {
            redactionModel = new HierarchyWizardRedactionModel<T>((HierarchyBuilderRedactionBased<T>)builder, redactionModel.getData());
        } else {
            throw new RuntimeException("Unknown type of builder");
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
}
