/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.model;

/**
 * This class implements an event for model changes.
 *
 * @author Fabian Prasser
 */
public class ModelEvent {
    
    /**
     * The part of the model that has changed.
     *
     * @author Fabian Prasser
     */
    public static enum ModelPart {
        
        /**  TODO */
        SELECTED_ATTRIBUTE,
        
        /**  TODO */
        INPUT,
        
        /**  TODO */
        OUTPUT,
        
        /**  TODO */
        ATTRIBUTE_TYPE,
        
        /**  TODO */
        RESULT,
        
        /**  TODO */
        DATA_TYPE,
        
        /**  TODO */
        ALGORITHM,
        
        /**  TODO */
        METRIC,
        
        /**  TODO */
        MAX_OUTLIERS,
        
        /**  TODO */
        FILTER,
        
        /**  TODO */
        SELECTED_NODE,
        
        /**  TODO */
        MODEL,
        
        /**  TODO */
        CLIPBOARD,
        
        /**  TODO */
        HIERARCHY,
        
        /**  TODO */
        CRITERION_DEFINITION,
        
        /**  TODO */
        RESEARCH_SUBSET,
        
        /**  TODO */
        VIEW_CONFIG,
        
        /**  TODO */
        VISUALIZATION
    }

    /** The part of the model that has changed. */
    public final ModelPart   part;
    
    /** The associated data, if any. */
    public final Object      data;
    
    /** The sender. */
    public final Object      source;

    /**
     * Creates a new instance.
     *
     * @param source
     * @param target
     * @param data
     */
    public ModelEvent(final Object source,
                      final ModelPart target,
                      final Object data) {
        this.part = target;
        this.data = data;
        this.source = source;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String sourceLabel = "NULL";
        if (source != null) sourceLabel = source.getClass().getSimpleName()+"@" + source.hashCode();
        String dataLabel = "NULL";
        if (data != null) dataLabel = data.getClass().getSimpleName()+"@" + data.hashCode();
        return "[part=" + part + ", source=" + sourceLabel + ", data=" + dataLabel + "]";
    }
}