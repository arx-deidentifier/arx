/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deidentifier.flash.AttributeType.Hierarchy;

/**
 * Encapsulates a definition of the types of attributes contained in a dataset
 * 
 * @author Prasser, Kohlmayer
 */
public class DataDefinition {

    /** The mapped attribute types */
    private final Map<String, AttributeType> attributeTypes    = new HashMap<String, AttributeType>();

    /** The mapped data types */
    private final Map<String, DataType>      dataTypes         = new HashMap<String, DataType>();

    /** The mapped minimum generalization */
    private final Map<String, Integer>       minGeneralization = new HashMap<String, Integer>();

    /** The mapped maximum generalization */
    private final Map<String, Integer>       maxGeneralization = new HashMap<String, Integer>();

    @Override
    public DataDefinition clone() {

        final DataDefinition d = new DataDefinition();

        for (final String attr : attributeTypes.keySet()) {
            d.attributeTypes.put(attr, attributeTypes.get(attr).clone());
        }

        for (final String attr : dataTypes.keySet()) {
            d.dataTypes.put(attr, dataTypes.get(attr).clone());
        }
        for (final String attr : minGeneralization.keySet()) {
            d.minGeneralization.put(attr, minGeneralization.get(attr));
        }
        for (final String attr : maxGeneralization.keySet()) {
            d.maxGeneralization.put(attr, maxGeneralization.get(attr));
        }
        return d;
    }

    /**
     * Returns the type defined for the attribute
     * 
     * @param attribute
     * @return
     */
    public AttributeType getAttributeType(final String attribute) {
        return attributeTypes.get(attribute);
    }

    /**
     * Returns the Datatype for the coulmn name
     * 
     * @param columnName
     * @return
     */
    public DataType getDataType(final String columnName) {
        final DataType t = dataTypes.get(columnName);
        if (t == null) {
            return DataType.STRING;
        } else {
            return t;
        }
    }

    /**
     * Returns the data types
     * 
     * @return
     */
    protected Map<String, DataType> getDataTypes() {
        return dataTypes;
    }

    /**
     * Returns all generalization hierarchies
     * 
     * @return
     */
    protected Map<String, String[][]> getHierarchies() {
        final Map<String, String[][]> result = new HashMap<String, String[][]>();
        for (final Entry<String, AttributeType> entry : attributeTypes.entrySet()) {
            if (entry.getValue().getType() == AttributeType.ATTR_TYPE_QI) {
                result.put(entry.getKey(),
                           ((Hierarchy) entry.getValue()).getHierarchy());
            }
        }
        return result;
    }

    /**
     * Returns the according hierarchy
     * 
     * @return
     */
    public String[][] getHierarchy(final String attribute) {
        return ((Hierarchy) attributeTypes.get(attribute)).getHierarchy();
    }

    /**
     * Returns the height of the according hierarchy
     * 
     * @return
     */
    public int getHierarchyHeight(final String attribute) {
        if (((Hierarchy) attributeTypes.get(attribute)).getHierarchy().length == 0) { return 0; }
        return ((Hierarchy) attributeTypes.get(attribute)).getHierarchy()[0].length;
    }

    /**
     * Returns the direct identifiers
     * 
     * @return
     */
    public Set<String> getIdentifyingAttributes() {
        final Set<String> result = new HashSet<String>();
        for (final Entry<String, AttributeType> entry : attributeTypes.entrySet()) {
            if (entry.getValue().getType() == AttributeType.ATTR_TYPE_ID) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Returns the insensitive attributes
     * 
     * @return
     */
    public Set<String> getInsensitiveAttributes() {
        final Set<String> result = new HashSet<String>();
        for (final Entry<String, AttributeType> entry : attributeTypes.entrySet()) {
            if (entry.getValue().getType() == AttributeType.ATTR_TYPE_IS) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Returns the maximal generalization
     * 
     * @param attribute
     * @return
     */
    protected int getMaximalGeneralization(final String attribute) {
        return maxGeneralization.get(attribute);
    }

    /**
     * Returns the maximal generalization
     * 
     * @param attribute
     * @return
     */
    protected Map<String, Integer> getMaximalGeneralizations() {
        return maxGeneralization;
    }

    /**
     * Returns the maximum generalization for the attribute
     * 
     * @return
     */
    public int getMaximumGeneralization(final String attribute) {
        if (!maxGeneralization.containsKey(attribute)) {
            final int max = getHierarchyHeight(attribute) - 1;
            if (max < 0) {
                return 0;
            } else {
                return max;
            }
        } else {
            return maxGeneralization.get(attribute);
        }
    }

    /**
     * Returns the minimal generalization
     * 
     * @param attribute
     * @return
     */
    protected int getMinimalGeneralization(final String attribute) {
        return minGeneralization.get(attribute);
    }

    /**
     * Returns the minimal generalization
     * 
     * @param attribute
     * @return
     */
    protected Map<String, Integer> getMinimalGeneralizations() {
        return minGeneralization;
    }

    /**
     * Returns the minimum generalization for the attribute
     * 
     * @return
     */
    public int getMinimumGeneralization(final String attribute) {
        if (!minGeneralization.containsKey(attribute)) {
            return 0;
        } else {
            return minGeneralization.get(attribute);
        }
    }

    /**
     * Returns the quasi identifying attributes
     * 
     * @return
     */
    public Set<String> getQuasiIdentifyingAttributes() {
        final Set<String> result = new HashSet<String>();
        for (final Entry<String, AttributeType> entry : attributeTypes.entrySet()) {
            if (entry.getValue().getType() == AttributeType.ATTR_TYPE_QI) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Returns the sensitive attributes
     * 
     * @return
     */
    public Set<String> getSensitiveAttributes() {
        final Set<String> result = new HashSet<String>();
        for (final Entry<String, AttributeType> entry : attributeTypes.entrySet()) {
            if (entry.getValue().getType() == AttributeType.ATTR_TYPE_SE) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Define the type of a given attribute
     * 
     * @param attribute
     * @param type
     */
    public void setAttributeType(final String attribute,
                                 final AttributeType type) {
        if (type == null) { throw new NullPointerException("Type must not be null"); }
        if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            if (!getSensitiveAttributes().isEmpty()) { throw new IllegalArgumentException("Cannot define more than one sensitive attribute!"); }
        }

        // Clear if new hierarchy is set
        // TODO: Good idea?
        if (type instanceof Hierarchy) {
            minGeneralization.remove(attribute);
            maxGeneralization.remove(attribute);
        }
        attributeTypes.put(attribute, type);
    }

    /**
     * Define the datatype of a given attribute
     * 
     * @param attribute
     * @param type
     */
    public void setDataType(final String attribute, final DataType type) {
        if (type == null) { throw new NullPointerException("Type must not be null"); }
        dataTypes.put(attribute, type);
    }

    /**
     * Define the maximal generalization of a given attribute
     * 
     * @param attribute
     * @param type
     */
    public void setMaximumGeneralization(final String attribute,
                                         final int maximum) {
        maxGeneralization.put(attribute, maximum);
    }

    /**
     * Define the minimal generalization of a given attribute
     * 
     * @param attribute
     * @param type
     */
    public void setMinimumGeneralization(final String attribute,
                                         final int minimum) {
        minGeneralization.put(attribute, minimum);
    }
}
