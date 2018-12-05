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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.certificate.elements.ElementData;
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

    /** Whether to perform clustering before aggregation. */
    private Map<String, Boolean>                        clustering        = new HashMap<String, Boolean>();

    /** Set of response variables. */
    private Map<String, Boolean>                        response          = new HashMap<String, Boolean>();
    
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
            d.functions.put(attr, functions.get(attr) == null ? null : functions.get(attr).clone());
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
        if (this.clustering != null) {
            for (final String attr : clustering.keySet()) {
                d.clustering.put(attr, clustering.get(attr));
            }
        }
        if (this.response != null) {
            for (final String attr : response.keySet()) {
                d.response.put(attr, response.get(attr));
            }
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
            return 0;
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
     * Returns the quasi-identifiers for which clustering and microaggregation has been specified. The
     * result of this method is a subset of the attributes returned by 
     * <code>getQuasiIdentifiersWithMicroaggregation()</code>.
     * @return
     */
    public Set<String> getQuasiIdentifiersWithClusteringAndMicroaggregation() {
        final Set<String> result = new HashSet<String>();
        for (String attr : getAttributesByType(AttributeType.ATTR_TYPE_QI)) {
            if (getMicroAggregationFunction(attr) != null &&
                clustering != null && clustering.containsKey(attr) && clustering.get(attr)) {
                result.add(attr);
            }
        }
        return result;
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
     * Returns the set of defined response variables
     * @return
     */
    public Set<String> getResponseVariables() {
        Set<String> result = new HashSet<>();
        if (response != null) {
            for (String attribute : response.keySet()) {
                if (response.get(attribute) != null && response.get(attribute)) {
                    result.add(attribute);
                }
            }
        }
        return result;
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
     * Returns whether the given attribute is a response variable
     * @param attribute
     * @return
     */
    public boolean isResponseVariable(String attribute) {
        return this.response != null && this.response.get(attribute) != null && this.response.get(attribute);
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
        if (this.clustering != null) {
            this.clustering.clear();
            this.clustering.putAll(other.clustering);
        }
        if (this.response != null) {
            this.response.clear();
            this.response.putAll(other.response);
        }
    }

    /**
     * Renders this object 
     * @return
     */
    public List<ElementData> render() {

        // Render attribute types
        List<ElementData> result = new ArrayList<>();
        result.add(render("Insensitive attributes", getInsensitiveAttributes()));
        result.add(render("Sensitive attributes", getSensitiveAttributes()));
        result.add(render("Identifying attributes", getIdentifyingAttributes()));
        result.add(render("Quasi-identifying attributes", getQuasiIdentifyingAttributes()));
        result.add(render("Response variables", getResponseVariables()));

        // Render hierarchies
        Set<String> attributes = new HashSet<>();
        attributes.addAll(getInsensitiveAttributes());
        attributes.addAll(getSensitiveAttributes());
        attributes.addAll(getIdentifyingAttributes());
        attributes.addAll(getQuasiIdentifyingAttributes());
        for (String attribute : attributes) {
            if ((!this.functions.containsKey(attribute) || this.functions.get(attribute) == null ) && 
                (this.hierarchies.containsKey(attribute) || this.builders.containsKey(attribute))) {
                result.add(render(attribute, this.hierarchies.get(attribute), this.builders.get(attribute)));
            }
        }
        for (String attribute : attributes) {
            if (this.functions.containsKey(attribute) && this.functions.get(attribute) != null) {
                result.add(render(attribute, this.functions.get(attribute)));
            }
        }
        return result;
    }

    /**
     * Resets the according setting
     * @param attribute
     */
    public void resetAttributeType(String attribute) {
        checkLocked();
        this.attributeTypes.remove(attribute);
    }

    /**
     * Resets the according setting
     * @param attribute
     */
    public void resetHierarchy(String attribute) {
        checkLocked();
        this.hierarchies.remove(attribute);
    }

    /**
     * Resets the according setting
     * @param attribute
     */
    public void resetHierarchyBuilder(String attribute) {
        checkLocked();
        this.builders.remove(attribute);
    }
    
    /**
     * Resets the according setting
     * @param attribute
     */
    public void resetMaximumGeneralization(String attribute) {
        checkLocked();
        this.minGeneralization.remove(attribute);
    }

    /**
     * Resets the according setting
     * @param attribute
     */
    public void resetMicroAggregationFunction(String attribute) {
        checkLocked();
        this.functions.remove(attribute);
    }
    
    /**
     * Resets the according setting
     * @param attribute
     */
    public void resetMinimumGeneralization(String attribute) {
        checkLocked();
        this.maxGeneralization.remove(attribute);
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
            setMicroAggregationFunction(attribute, (MicroAggregationFunction)type);
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
        checkLocked();
        this.hierarchies.put(attribute, hierarchy);
    }

    /**
     * Associates the given hierarchy builder
     * @param attribute
     * @param builder
     */
    public void setHierarchy(String attribute, HierarchyBuilder<?> builder) {
        checkLocked();
        this.builders.put(attribute, builder);
    }

    /**
     * Define the maximal generalization of a given attribute.
     *
     * @param attribute
     * @param maximumLevel
     */
    public void setMaximumGeneralization(final String attribute,
                                         final int maximumLevel) {
        
        checkLocked();
        maxGeneralization.put(attribute, maximumLevel);
    }

    /**
     * Associates the given microaggregation function. When configuring microaggregation with this method
     * generalization hierarchies will not be used for clustering attribute values before aggregation.
     * @param attribute
     * @param function
     */
    public void setMicroAggregationFunction(String attribute, MicroAggregationFunction function) {
        this.setMicroAggregationFunction(attribute, function, false);
    }

    /**
     * Associates the given microaggregation function
     * @param attribute
     * @param function
     * @param performClustering When set to true, available generalization hierarchies will 
     *                          be used for clustering attribute values before aggregation.
     */
    public void setMicroAggregationFunction(String attribute, MicroAggregationFunction function, boolean performClustering) {
        checkLocked();
        this.functions.put(attribute, function);
        if (this.clustering == null) {
            this.clustering = new HashMap<>();
        }
        this.clustering.put(attribute, performClustering);
    }
    
    /**
     * Define the minimal generalization of a given attribute.
     *
     * @param attribute
     * @param minimumLevel
     */
    public void setMinimumGeneralization(final String attribute,
                                         final int minimumLevel) {
        
        checkLocked();
        minGeneralization.put(attribute, minimumLevel);
    }
    
    /**
     * Sets whether the given attribute is a response variable
     * @param attribute
     * @param value
     */
    public void setResponseVariable(String attribute, boolean value) {
        if (this.response == null) {
            this.response = new HashMap<>();
        }
        checkLocked();
        this.response.put(attribute,  value);
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
     * Renders a hierarchy
     * @param attribute
     * @param hierarchy
     * @param builder
     */
    private ElementData render(String attribute, Hierarchy hierarchy, HierarchyBuilder<?> builder) {
        ElementData result = new ElementData("Generalization hierarchy");
        result.addProperty("Attribute", attribute);
        if (hierarchy != null && hierarchy.getHierarchy() != null && 
            hierarchy.getHierarchy().length != 0 && hierarchy.getHierarchy()[0] != null) {
            result.addProperty("Height", hierarchy.getHierarchy()[0].length);
            if (this.getQuasiIdentifyingAttributes().contains(attribute)) {
                result.addProperty("Minimum level", this.getMinimumGeneralization(attribute));
                result.addProperty("Maximum level", this.getMaximumGeneralization(attribute));
            }
        } else if (builder != null){
            result.addProperty("Builder type", builder.getType().toString());
        }
        return result;
    }

    /**
     * Renders a microaggregation function
     * @param attribute
     * @param function
     * @return
     */
    private ElementData render(String attribute, MicroAggregationFunction function) {
        ElementData result = new ElementData("Microaggregation function");
        result.addProperty("Attribute", attribute);
        if (function != null) {
            result.addProperty("Type", function.getLabel());
        }
        if (clustering != null && clustering.containsKey(attribute)) {
            result.addProperty("Clustering", clustering.get(attribute));
        }
        return result;
    }

    /**
     * Renders a set of attributes
     * @param title
     * @param attributes
     * @return
     */
    private ElementData render(String title, Set<String> attributes) {
         ElementData result = new ElementData(title);
         if (attributes.isEmpty()) {
             result.addItem("None");
         } else {
             for (String attribute : attributes) {
                 result.addProperty(attribute, this.getDataType(attribute).toString());
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
        
        Set<String> attributes = new HashSet<>(this.getQuasiIdentifiersWithGeneralization());
        attributes.addAll(this.getQuasiIdentifiersWithClusteringAndMicroaggregation());
        
        // For each relevant attribute
        for (String attribute : attributes) {

            // Obtain data
            String[] data = handle.getDistinctValues(handle.getColumnIndexOf(attribute));

            // If builder is available
            if (isHierarchyBuilderAvailable(attribute)) {
                // Compute and store hierarchy
                try {
                    this.hierarchies.put(attribute, this.getHierarchyBuilder(attribute).build(data));
                } catch (Exception e) {
                    throw new IllegalStateException("Error building hierarchy for attribute (" + attribute + ")", e);
                }
            } else if (!isHierarchyAvailable(attribute)){
                // Create empty hierarchy
                String[][] hierarchy = new String[data.length][];
                for (int i = 0; i < data.length; i++) {
                    hierarchy[i] = new String[] { data[i] };
                }
                this.hierarchies.put(attribute, Hierarchy.create(hierarchy));
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
