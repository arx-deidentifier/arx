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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class encapsulates a generalization scheme
 * @author Fabian Prasser
 */
public class DataGeneralizationScheme implements Serializable, Cloneable { // NO_UCD
    
    /**
     * A specific generalization degree
     * @author Fabian Prasser
     *
     */
    public static enum GeneralizationDegree implements Serializable {
        
        NONE(0d),
        LOW(0.2d),
        LOW_MEDIUM(0.4d),
        MEDIUM(0.5d),
        MEDIUM_HIGH(0.6d),
        HIGH(0.8d),
        COMPLETE(1d);
        
        /** Factor*/
        private final double factor;
        
        /**
         * Constructor
         * @param factor
         */
        private GeneralizationDegree(double factor) {
            this.factor = factor;
        }
        
        /**
         * Returns the factor
         * @return
         */
        protected double getFactor() {
            return this.factor;
        }
    }

    /** SVUID*/
    private static final long serialVersionUID = -5402090022629905154L;

    /**
     * Creates a new data generalization scheme
     * @return
     */
    public static DataGeneralizationScheme create() {
        return new DataGeneralizationScheme(null, null);
    }
    
    /**
     * Creates a new data generalization scheme
     * @param data
     * @return
     */
    public static DataGeneralizationScheme create(Data data) {
        return new DataGeneralizationScheme(data, null);
    }
    
    /**
     * Creates a new data generalization scheme
     * @param data
     * @param degree
     * @return
     */
    public static DataGeneralizationScheme create(Data data, GeneralizationDegree degree) {
        return new DataGeneralizationScheme(data, degree);
    }

    /**
     * Creates a new data generalization scheme
     * @param degree
     * @return
     */
    public static DataGeneralizationScheme create(GeneralizationDegree degree) {
        return new DataGeneralizationScheme(null, degree);
    }
    
    /** Degrees */
    private Map<String, GeneralizationDegree> degrees = new HashMap<String, GeneralizationDegree>();

    /** Levels */
    private Map<String, Integer>              levels  = new HashMap<String, Integer>();

    /** Degree */
    private GeneralizationDegree              degree  = null;

    /** Data */
    private Set<String>                       attributes;
    
    /**
     * Creates a new instance
     * @param data
     * @param degree 
     */
    private DataGeneralizationScheme(Data data, GeneralizationDegree degree) {
        
        if (data != null) {
            this.attributes = new HashSet<String>();
            for (int i=0; i<data.getHandle().getNumColumns(); i++) {
                this.attributes.add(data.getHandle().getAttributeName(i));
            }
        } else {
            this.attributes = null;
        }
        this.degree = degree;
    }
    
    /**
     * Clone method
     */
    public DataGeneralizationScheme clone() {
        DataGeneralizationScheme result = new DataGeneralizationScheme(null, null);
        result.degrees = new HashMap<String, GeneralizationDegree>(this.degrees);
        result.degree = this.degree;
        result.levels = new HashMap<String, Integer>(this.levels);
        result.attributes = this.attributes != null ? new HashSet<String>(this.attributes) : null;
        return result;
    }

    /**
     * Defines a specific generalization degree
     * @param degree
     * @return
     */
    public DataGeneralizationScheme generalize(GeneralizationDegree degree) {
        this.degree = degree;
        return this;
    }
    

    /**
     * Defines a specific generalization degree
     * @param attribute
     * @param degree
     * @return
     */
    public DataGeneralizationScheme generalize(String attribute, GeneralizationDegree degree) {
        check(attribute);
        this.degrees.put(attribute, degree);
        return this;
    }

    /**
     * Defines a specific generalization level
     * @param attribute
     * @param level
     * @return
     */
    public DataGeneralizationScheme generalize(String attribute, int level) {
        check(attribute);
        if (level < 0) {
            throw new IllegalArgumentException("Invalid generalization level: " + level);
        }
        this.levels.put(attribute, level);
        return this;
    }
    
    /**
     * Returns the overall generalization degree, if any
     * @return
     */
    public GeneralizationDegree getGeneralizationDegree() {
        return this.degree;
    }
    
    /**
     * Returns a generalization level as defined by this class
     * @param attribute
     * @param definition
     * @return
     */
    public int getGeneralizationLevel(String attribute, DataDefinition definition) {

        int result = 0;
        if (definition.isHierarchyAvailable(attribute)) {
            if (this.levels.containsKey(attribute)) {
                result = this.levels.get(attribute);
            } else if (this.degrees.containsKey(attribute)) {
                result = (int) Math.round(  (this.degrees.get(attribute).getFactor() * 
                                            (double) definition.getMaximumGeneralization(attribute)));
            } else if (this.degree != null) {
                result = (int) Math.round(this.degree.getFactor() *
                                          (double) definition.getMaximumGeneralization(attribute));
            }
        }
        result = Math.max(result, definition.getMinimumGeneralization(attribute));
        result = Math.min(result, definition.getMaximumGeneralization(attribute));
        return result;
    }

    /**
     * Checks the given attribute
     * @param attribute
     */
    private void check(String attribute) {
        if (attributes != null && !attributes.contains(attribute)) {
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }
    }
}
