/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

public class ModelEvent {
    
    public static enum ModelPart {
        SELECTED_ATTRIBUTE,
        INPUT,
        OUTPUT,
        ATTRIBUTE_TYPE,
        RESULT,
        DATA_TYPE,
        ALGORITHM,
        METRIC,
        MAX_OUTLIERS,
        FILTER,
        SELECTED_NODE,
        MODEL,
        CLIPBOARD,
        HIERARCHY,
        CRITERION_DEFINITION,
        RESEARCH_SUBSET,
        VIEW_CONFIG
    }

    public final ModelPart   part;
    public final Object      data;
    public final Object      source;

    public ModelEvent(final Object source,
                      final ModelPart target,
                      final Object data) {
        this.part = target;
        this.data = data;
        this.source = source;
    }
}