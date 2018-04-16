/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
package org.deidentifier.arx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Configuration for feature scaling
 * @author Fabian Prasser
 */
public class ARXFeatureScaling implements Serializable {

    /** SVUID */
    private static final long serialVersionUID = 5844012255436186950L;

    /**
     * Returns a new instance
     * 
     * @return
     */
    public static ARXFeatureScaling create() {
        return new ARXFeatureScaling();
    }

    /** Functions for feature scaling */
    private Map<String, String>     functions   = new HashMap<>();

    /**
     * Constructor
     */
    private ARXFeatureScaling(){
        // Empty by design
    }
    
    /**
     * Parse expression string.
     * @param function
     * @return the expression 
     * @throws IllegalArgumentException
     */
    private Expression getExpression(String function) throws IllegalArgumentException {
        Expression expression = null;
        try {
            expression = new ExpressionBuilder(function).variable("x").build();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (expression == null || !expression.validate(false).isValid()) {
            throw new IllegalArgumentException("Invalid function: " + function);
        }
        if (expression.getVariableNames().size() != 1 || !expression.getVariableNames().contains("x")) {
            throw new IllegalArgumentException("Function must have exactly one variable 'x': " + function);
        }
        return expression;
    }

    /**
     * Returns a scaling function
     * @param attribute
     * @return
     */
    public Expression getScalingExpression(String attribute) {
        if (attribute == null) {
            return null;
        }
        String function = this.functions.get(attribute);
        if (function == null || function.equals("")) {
            return null;
        }
        return getExpression(function);
    }

    /**
     * Returns a scaling function
     * @param attribute
     * @return
     */
    public String getScalingFunction(String attribute) {
        return this.functions.get(attribute);
    }

    /**
     * Returns whether the function is valid
     * @param function
     * @return
     */
    public boolean isValidScalingFunction(String function) {
        if (function == null || function.equals("")) {
            return true;
        }
        try {
            getExpression(function);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Remove the scaling function, if any
     * @param attribute
     */
    public ARXFeatureScaling removeScalingFunction(String attribute) {
        this.setScalingFunction(attribute, null);
        return this;
    }
    
    /**
     * Sets a scaling function
     * @param attribute
     * @param function
     */
    public ARXFeatureScaling setScalingFunction(String attribute, String function) {
        
        if (attribute == null) {
            return this;
        }
        if (function == null || function.equals("")) {
            this.functions.remove(attribute);
            return this;
        }
        // Check expression string
        getExpression(function);
        this.functions.put(attribute, function);
        return this;
    }

    /**
     * Sets a scaling function
     * @param attribute
     */
    public ARXFeatureScaling setScalingFunctionIdentity(String attribute) {
        this.setScalingFunction(attribute, "x");
        return this;
    }

    /**
     * Sets a scaling function
     * @param attribute
     */
    public ARXFeatureScaling setScalingFunctionInverse(String attribute) {
        this.setScalingFunction(attribute, "1/x");
        return this;
    }

    /**
     * Sets a scaling function
     * @param attribute
     */
    public ARXFeatureScaling setScalingFunctionLog(String attribute) {
        this.setScalingFunction(attribute, "log(x)");
        return this;
    }
    
    /**
     * Sets a scaling function
     * @param attribute
     */
    public ARXFeatureScaling setScalingFunctionSQRT(String attribute) {
        this.setScalingFunction(attribute, "sqrt(x)");
        return this;
    }

    /**
     * Sets a scaling function
     * @param attribute
     */
    public ARXFeatureScaling setScalingFunctionSquare(String attribute) {
        this.setScalingFunction(attribute, "x*x");
        return this;
    }
}
