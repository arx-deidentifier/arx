/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx.aggregates.classification;

import net.objecthunter.exp4j.Expression;

import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.DataTypeWithRatioScale;

/**
 * Metadata about a single feature
 * 
 * @author Fabian Prasser
 */
public class ClassificationFeatureMetadata {

    /** Attribute */
    private final String      attribute;
    /** Data type */
    private final DataType<?> type;
    /** Expression */
    private final Expression  expression;
    /** Is this a numeric attribute */
    private final boolean     numeric;
    /** Is this attribute microaggregated in a type preserving manner */
    private final boolean     isTypePreservingMicroaggregation;

    /**
     * Creates a new instance
     * @param attribute
     * @param type
     * @param scaling
     * @param isTypePreservingMicroaggregation
     */
    public ClassificationFeatureMetadata(String attribute, 
                                         DataType<?> type,
                                         ARXFeatureScaling scaling,
                                         boolean isTypePreservingMicroaggregation) {
        this.attribute = attribute;
        this.type = type;
        this.numeric = (type instanceof ARXDecimal) || (type instanceof ARXInteger) || (type instanceof ARXDate);
        this.isTypePreservingMicroaggregation = isTypePreservingMicroaggregation;
        Expression e = scaling != null ? scaling.getScalingExpression(attribute) : null;
        if (e != null && this.numeric) {
            this.expression = e;
        } else {
            this.expression = null;
        }
    }

    /**
     * Returns the name of the feature
     * @return
     */
    public String getName() {
        return this.attribute;
    }
    
    /**
     * Returns whether this attribute is numeric and microaggregated in a type preserving manner
     * @return
     */
    public boolean isNumericMicroaggregation() {
        return isTypePreservingMicroaggregation && numeric;
    }

    /**
     * Returns whether this is a numeric attribute
     * @return
     */
    public boolean isNumeric() {
        return this.expression != null;
    }
    
    /**
     * Returns a scaled double representation, NaN if the value cannot be parsed or scaled
     * 
     * @param value
     */
    public double getNumericValue(String value) {
        
        if (!isNumeric()) {
            return Double.NaN;
        }
        
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            double numeric = ((DataTypeWithRatioScale)type).toDouble(type.parse(value));
            return this.expression != null ? this.expression.setVariable("x", numeric).evaluate() : Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
        }
    }
}
