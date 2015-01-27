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
import java.util.Locale;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXOrderedString;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilder.Type;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;

/**
 * The base model for the wizard.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardModel<T> {

    /** Var. */
    private Type                             type;
    
    /** Var. */
    private HierarchyWizardModelOrder<T>     orderModel;
    
    /** Var. */
    private HierarchyWizardModelIntervals<T> intervalModel;
    
    /** Var. */
    private HierarchyWizardModelRedaction<T> redactionModel;
    
    /** Var. */
    private final DataType<T>                dataType;
    
    /** Var. */
    private final String[]                   data;
    
    /**
     * Creates a new instance for the given data type.
     *
     * @param dataType
     * @param locale
     * @param data
     */
    public HierarchyWizardModel(DataType<T> dataType, Locale locale, String[] data){
        
        // Store
        this.data = data;
        this.dataType = dataType;
        
        // Create models
        orderModel = new HierarchyWizardModelOrder<T>(dataType, locale, getOrderData());
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
     * Returns the current builder.
     *
     * @param serializable
     * @return
     * @throws Exception
     */
    public HierarchyBuilder<T> getBuilder(boolean serializable) throws Exception {
        if (type == Type.INTERVAL_BASED) {
            return intervalModel.getBuilder(serializable);
        } else if (type == Type.REDACTION_BASED) {
            return redactionModel.getBuilder(serializable);
        } else if (type == Type.ORDER_BASED) {
            return orderModel.getBuilder(serializable);
        } else {
            throw new IllegalArgumentException("Unknown type of builder");
        }
    }
    
    /**
     * Returns the data type.
     *
     * @return
     */
    public DataType<T> getDataType() {
        return this.dataType;
    }

    /**
     * Returns the current hierarchy.
     *
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
    
    /**
     * Returns the model of the interval-based builder.
     *
     * @return
     */
    public HierarchyWizardModelIntervals<T> getIntervalModel() {
        return intervalModel;
    }
    
    /**
     * Returns the model of the order-based builder.
     *
     * @return
     */
    public HierarchyWizardModelOrder<T> getOrderModel() {
        return orderModel;
    }
    
    /**
     * Returns the model of the redaction-based builder.
     *
     * @return
     */
    public HierarchyWizardModelRedaction<T> getRedactionModel() {
        return redactionModel;
    }

    /**
     * Returns the type.
     *
     * @return
     */
    public Type getType(){
        return this.type;
    }

    /**
     * Updates the model with a new specification.
     *
     * @param builder
     * @throws IllegalArgumentException
     */
    public void parse(HierarchyBuilder<T> builder) throws IllegalArgumentException{
        
        if (builder.getType() == Type.INTERVAL_BASED) {
            if (intervalModel != null){
                this.intervalModel.parse((HierarchyBuilderIntervalBased<T>)builder);
                this.type = Type.INTERVAL_BASED;
            }
        } else if (builder.getType() == Type.ORDER_BASED) {
            this.orderModel.parse((HierarchyBuilderOrderBased<T>)builder);
            this.type = Type.ORDER_BASED;
        } else if (builder.getType() == Type.REDACTION_BASED) {
            this.redactionModel.parse(builder);
            this.type = Type.REDACTION_BASED;
        } else {
            throw new IllegalArgumentException("Unknown type of builder");
        }
    }

    /**
     * Sets the type.
     *
     * @param type
     */
    public void setType(Type type){
        if (type != this.type) {
            this.type = type;
        }
    }

    /**
     * Simple comparison of data types.
     *
     * @param type
     * @param other
     * @return
     */
    private boolean equals(DataType<?> type, DataType<?> other){
        return type.getDescription().getLabel().equals(other.getDescription().getLabel());
    }

    /**
     * Returns data for the order-based builder.
     *
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
