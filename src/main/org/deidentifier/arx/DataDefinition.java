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

package org.deidentifier.arx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionGeneralization;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportConfiguration;

/**
 * Encapsulates a definition of the types of attributes contained in a dataset.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataDefinition implements Cloneable{

    /** Is this data definition locked. */
    private boolean                                     locked            = false;

    /** The mapped attribute types. */
    private final Map<String, AttributeType>            attributeTypes    = new HashMap<String, AttributeType>();

    /** The mapped builders. */
    private final Map<String, HierarchyBuilder<?>>      builders          = new HashMap<String, HierarchyBuilder<?>>();

    /** The mapped hierchies. */
    private final Map<String, Hierarchy>                hierarchies       = new HashMap<String, Hierarchy>();

    /** The mapped functions. */
    private final Map<String, MicroAggregationFunction> functions         = new HashMap<String, MicroAggregationFunction>();

    /** The mapped data types. */
    private final Map<String, DataType<?>>              dataTypes         = new HashMap<String, DataType<?>>();

    /** The mapped minimum generalization. */
    private final Map<String, Integer>                  minGeneralization = new HashMap<String, Integer>();

    /** The mapped maximum generalization. */
    private final Map<String, Integer>                  maxGeneralization = new HashMap<String, Integer>();

    @Override
    public DataDefinition clone() {

        final DataDefinition d = new DataDefinition();

        for (final String attr : attributeTypes.keySet()) {
            d.attributeTypes.put(attr, attributeTypes.get(attr).clone());
        }
        for (final String attr : dataTypes.keySet()) {
            d.dataTypes.put(attr, dataTypes.get(attr).clone());
        }
        for (final String attr : hierarchies.keySet()) {
            d.hierarchies.put(attr, hierarchies.get(attr));
        }
        for (final String attr : functions.keySet()) {
            d.functions.put(attr, functions.get(attr));
        }
        for (final String attr : minGeneralization.keySet()) {
            d.minGeneralization.put(attr, minGeneralization.get(attr));
        }
        for (final String attr : maxGeneralization.keySet()) {
            d.maxGeneralization.put(attr, maxGeneralization.get(attr));
        }
        for (final String attr : builders.keySet()) {
            d.builders.put(attr, builders.get(attr));
        }
        d.setLocked(this.isLocked());
        return d;
    }

    /**
     * Returns the type defined for the attribute.
     *
     * @param attribute
     * @return
     */
    public AttributeType getAttributeType(final String attribute) {
        return attributeTypes.get(attribute);
    }

    /**
     * Returns the data type for the given column.
     *
     * @param columnName
     * @return
     */
    public DataType<?> getDataType(final String columnName) {
        final DataType<?> t = dataTypes.get(columnName);
        if (t == null) {
            return DataType.STRING;
        } else {
            return t;
        }
    }

    /**
     * Returns the according hierarchy as String array.
     *
     * @param attribute
     * @return
     */
    public String[][] getHierarchy(final String attribute) {
        Hierarchy hierarchy = hierarchies.get(attribute);
        return hierarchy == null ? null : hierarchy.getHierarchy();
    }
    
    /**
     * Returns the associated builder, if any.
     *
     * @param attribute
     * @return
     */
    public HierarchyBuilder<?> getHierarchyBuilder(final String attribute) {
        return builders.get(attribute);
    }
    
    /**
     * Returns the according hierarchy object.
     *
     * @param attribute
     * @return
     */
    public Hierarchy getHierarchyObject(final String attribute) {
        return hierarchies.get(attribute);
    }
    
    /**
     * Returns the direct identifiers.
     *
     * @return
     */
    public Set<String> getIdentifyingAttributes() {
        return getAttributesByType(AttributeType.ATTR_TYPE_ID);
    }
    
    /**
     * Returns the insensitive attributes.
     *
     * @return
     */
    public Set<String> getInsensitiveAttributes() {
        return getAttributesByType(AttributeType.ATTR_TYPE_IS);
    }
    
    /**
     * Returns the maximum generalization for the attribute.
     *
     * @param attribute
     * @return
     */
    public int getMaximumGeneralization(final String attribute) {
        checkQuasiIdentifier(attribute);
        Integer result = maxGeneralization.get(attribute);
        if (result != null) return result;
        if (this.getHierarchy(attribute) != null) {
            String[][] hierarchy = this.getHierarchy(attribute);
            if (hierarchy.length == 0 || hierarchy[0] == null) {
                return 0;
            } else {
                return hierarchy[0].length - 1;
            }
        } else {
            throw new IllegalStateException("No materialized hierarchy specified for attribute ("+attribute+")");
        }
    }
    
    /**
     * Returns the according microaggregation function.
     * 
     * @param attribute
     * @return
     */
    public MicroAggregationFunction getMicroAggregationFunction(final String attribute) {
        return functions.get(attribute);
    }
    
    /**
     * Returns the minimum generalization for the attribute.
     *
     * @param attribute
     * @return
     */
    public int getMinimumGeneralization(final String attribute) {
        checkQuasiIdentifier(attribute);
        Integer result = minGeneralization.get(attribute);
        return result != null ? result : 0;
    }
    
    /**
     * Returns the quasi-identifiers for which generalization is specified.
     * @return
     */
    public Set<String> getQuasiIdentifiersWithGeneralization() {
        final Set<String> result = new HashSet<String>();
        for (String attr : getAttributesByType(AttributeType.ATTR_TYPE_QI)) {
            if (getMicroAggregationFunction(attr) == null) {
                result.add(attr);
            }
        }
        return result;
    }

    /**
     * Returns the quasi-identifiers for which microaggregation is specified.
     * @return
     */
    public Set<String> getQuasiIdentifiersWithMicroaggregation() {
        final Set<String> result = new HashSet<String>();
        for (String attr : getAttributesByType(AttributeType.ATTR_TYPE_QI)) {
            if (getMicroAggregationFunction(attr) != null) {
                result.add(attr);
            }
        }
        return result;
    }
    
    /**
     * Returns the quasi identifying attributes.
     *
     * @return
     */
    public Set<String> getQuasiIdentifyingAttributes() {
        return getAttributesByType(AttributeType.ATTR_TYPE_QI);
    }
    

    /**
     * Returns the sensitive attributes.
     *
     * @return
     */
    public Set<String> getSensitiveAttributes() {
        return getAttributesByType(AttributeType.ATTR_TYPE_SE);
    }
    

    /**
     * Returns whether a hierarchy is available.
     *
     * @param attribute
     * @return
     */
    public boolean isHierarchyAvailable(String attribute) {
        return getHierarchy(attribute) != null;
    }

    /**
     * Returns whether a hierarchy builder is available.
     *
     * @param attribute
     * @return
     */
    public boolean isHierarchyBuilderAvailable(String attribute) {
        return getHierarchyBuilder(attribute) != null;
    }

    /**
     * Returns whether this definition can be altered.
     *
     * @return
     */
    public boolean isLocked(){
        return locked;
    }

    /**
     * Returns whether a maximum generalization level is available.
     *
     * @param attribute
     * @return
     */
    public boolean isMaximumGeneralizationAvailable(String attribute) {
        checkQuasiIdentifier(attribute);
        return maxGeneralization.containsKey(attribute) || (this.getHierarchy(attribute) != null);
        
    }

    /**
     * Returns whether a minimum generalization level is available.
     *
     * @param attribute
     * @return
     */
    public boolean isMinimumGeneralizationAvailable(String attribute) {
        checkQuasiIdentifier(attribute);
        return true;
    }

    /**
     * Reads all settings from the given definition
     * @param definition
     */
    public void read(DataDefinition other) {

        // Clone and copy stuff
        this.attributeTypes.clear();
        this.attributeTypes.putAll(other.attributeTypes);
        this.builders.clear();
        this.builders.putAll(other.builders);
        this.hierarchies.clear();
        this.hierarchies.putAll(other.hierarchies);
        this.functions.clear();
        this.functions.putAll(other.functions);
        this.dataTypes.clear();
        this.dataTypes.putAll(other.dataTypes);
        this.minGeneralization.clear();
        this.minGeneralization.putAll(other.minGeneralization);
        this.maxGeneralization.clear();
        this.maxGeneralization.putAll(other.maxGeneralization);
    }

    /**
     * Resets the according setting
     * @param attr
     */
    public void resetAttributeType(String attr) {
        this.attributeTypes.remove(attr);
    }
    
    /**
     * Resets the according setting
     * @param attr
     */
    public void resetHierarchy(String attr) {
        this.hierarchies.remove(attr);
    }

    /**
     * Resets the according setting
     * @param attr
     */
    public void resetHierarchyBuilder(String attr) {
        this.builders.remove(attr);
    }

    /**
     * Resets the according setting
     * @param attr
     */
    public void resetMaximumGeneralization(String attr) {
        this.minGeneralization.remove(attr);
    }

    /**
     * Resets the according setting
     * @param attr
     */
    public void resetMicroAggregationFunction(String attr) {
        this.functions.remove(attr);
    }

    /**
     * Resets the according setting
     * @param attr
     */
    public void resetMinimumGeneralization(String attr) {
        this.maxGeneralization.remove(attr);
    }

    /**
     * Define the type of a given attribute.
     *
     * @param attribute
     * @param type
     */
    public void setAttributeType(final String attribute,
                                 final AttributeType type) {
    	
        checkLocked();
        checkNullArgument(type, "Type");
        attributeTypes.put(attribute, type);
        if (type instanceof Hierarchy) {
            this.hierarchies.put(attribute, (Hierarchy)type);
        } else if (type instanceof MicroAggregationFunction) {
            this.functions.put(attribute, (MicroAggregationFunction)type);
        }
    }

    /**
     * Defines the given attribute as a quasi-identifier and stores the functional
     * representation of the generalization hierarchy.
     *
     * @param attribute
     * @param builder
     */
    public void setAttributeType(final String attribute,
                                 final HierarchyBuilder<?> builder) {
        
        checkLocked();
        checkNullArgument(builder, "Builder");
        attributeTypes.put(attribute, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        builders.put(attribute, builder);
    }

    /**
     * Define the datatype of a given attribute.
     *
     * @param attribute
     * @param type
     */
    public void setDataType(final String attribute, final DataType<?> type) {
        
        checkLocked();
        checkNullArgument(type, "Type");
        dataTypes.put(attribute, type);
    }

    /**
     * Associates the given hierarchy
     * @param attribute
     * @param hierarchy
     */
    public void setHierarchy(String attribute, Hierarchy hierarchy) {
        this.hierarchies.put(attribute, hierarchy);
    }
    
    /**
     * Associates the given hierarchy builder
     * @param attribute
     * @param builder
     */
    public void setHierarchy(String attribute, HierarchyBuilder<?> builder) {
        this.builders.put(attribute, builder);
    }
    
    /**
     * Define the maximal generalization of a given attribute.
     *
     * @param attribute
     * @param maximum
     */
    public void setMaximumGeneralization(final String attribute,
                                         final int maximum) {
        
        checkLocked();
        maxGeneralization.put(attribute, maximum);
    }

    /**
     * Associates the given microaggregation function
     * @param attribute
     * @param builder
     */
    public void setMicroAggregationFunction(String attribute, MicroAggregationFunction function) {
        this.functions.put(attribute, function);
    }

    /**
     * Define the minimal generalization of a given attribute.
     *
     * @param attribute
     * @param minimum
     */
    public void setMinimumGeneralization(final String attribute,
                                         final int minimum) {
        
        checkLocked();
        minGeneralization.put(attribute, minimum);
    }
    
    /**
     * Checks whether this handle is locked.
     *
     * @throws IllegalStateException
     */
    private void checkLocked() throws IllegalStateException{
        if (locked) {throw new IllegalStateException("This definition is currently locked");}
    }

    /**
     * Checks whether the argument is null.
     *
     * @param argument
     * @param name
     * @throws IllegalArgumentException
     */
    private void checkNullArgument(Object argument, String name) throws IllegalArgumentException {
        if (argument == null) { throw new NullPointerException(name + " must not be null"); }
    }

    /**
     * Checks whether the attribute is a quasi-identifier.
     *
     * @param attribute
     * @throws IllegalArgumentException
     */
    private void checkQuasiIdentifier(String attribute) throws IllegalArgumentException {
        if (attributeTypes.get(attribute) == null ||
            attributeTypes.get(attribute).getType() != AttributeType.ATTR_TYPE_QI) {
            throw new IllegalArgumentException("Attribute ("+attribute+") is not a quasi-identifier");
        }
    }

    /**
     * Returns attributes by type
     * @param type
     * @return
     */
    private Set<String> getAttributesByType(int type) {
        final Set<String> result = new HashSet<String>();
        for (final Entry<String, AttributeType> entry : attributeTypes.entrySet()) {
            if (entry.getValue().getType() == type) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Materializes all functional hierarchies.
     *
     * @param handle
     */
    protected void materializeHierarchies(DataHandle handle) {
        
        // For each qi with generalization
        for (String qi : this.getQuasiIdentifiersWithGeneralization()) {
            
            // If no hierarchy is available
            if (!isHierarchyAvailable(qi)) {
                
                // Obtain data
                String[] data = handle.getDistinctValues(handle.getColumnIndexOf(qi));
                
                // If builder is available
                if (isHierarchyBuilderAvailable(qi)) {
                    // Compute and store hierarchy
                    try {
                        this.hierarchies.put(qi, this.getHierarchyBuilder(qi).build(data));
                    } catch (Exception e) {
                        throw new IllegalStateException("Error building hierarchy for attribute ("+qi+")", e);
                    }
                } else {
                    // Create empty hierarchy
                    String[][] hierarchy = new String[data.length][];
                    for (int i=0; i<data.length; i++) {
                        hierarchy[i] = new String[]{data[i]};
                    }
                    this.hierarchies.put(qi, Hierarchy.create(hierarchy));
                }
            }
        }

        // For each qi with microaggregation
        for (String qi : this.getQuasiIdentifiersWithMicroaggregation()) {
            
            if (this.getMicroAggregationFunction(qi).getFunction() instanceof DistributionAggregateFunctionGeneralization) {
                
                // If no hierarchy is available
                if (!isHierarchyAvailable(qi)) {
                    
                    // Obtain data
                    String[] data = handle.getDistinctValues(handle.getColumnIndexOf(qi));
                    
                    // If builder is available
                    if (isHierarchyBuilderAvailable(qi)) {
                        // Compute and store hierarchy
                        try {
                            this.hierarchies.put(qi, this.getHierarchyBuilder(qi).build(data));
                        } catch (Exception e) {
                            throw new IllegalStateException("Error building hierarchy for attribute ("+qi+")", e);
                        }
                    } else {
                        // Create empty hierarchy
                        String[][] hierarchy = new String[data.length][];
                        for (int i=0; i<data.length; i++) {
                            hierarchy[i] = new String[]{data[i]};
                        }
                        this.hierarchies.put(qi, Hierarchy.create(hierarchy));
                    }
                }
            }
        }
    }

    /**
     * Parses the configuration of the import adapter.
     *
     * @param adapter
     */
    protected void parse(ImportAdapter adapter) {
        String[] header = adapter.getHeader();
        ImportConfiguration config = adapter.getConfig();
        for (int i=0; i<config.getColumns().size(); i++){
            this.setDataType(header[i], config.getColumns().get(i).getDataType());
        }
    }
    
    /**
     * Lock/unlock the definition.
     *
     * @param locked
     */
    protected void setLocked(boolean locked){
        this.locked = locked;
    }
}
