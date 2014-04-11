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

package org.deidentifier.arx.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;

/**
 * Class for creating debug data
 * @author Fabian Prasser
 *
 */
public class DebugData {
    
    private static final int MAX_BUFFER_SIZE = 1000;
    private List<String> eventBuffer = new ArrayList<String>();
    
    /**
     * Returns some debug data
     * @param Model The model
     * @return
     */
    protected String getData(Model model){

        if (model == null ||
            model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null) { 
            return "No debug data available"; //$NON-NLS-1$
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append("Handles\n");
        builder.append(" - Data\n");
        builder.append("   * Input : ").append(getDebugData(model.getInputConfig().getInput().getDefinition()));
        builder.append(" - Input\n");
        builder.append("   * Input : ").append(getDebugData("             ", model.getInputConfig().getInput().getHandle(), false));
        if (model.getOutput() != null) {
            builder.append(" - Output\n");
            builder.append("   * Input : ").append(getDebugData("             ", model.getOutputConfig().getInput().getHandle(), false));
            builder.append("   * Output: ").append(getDebugData("             ", model.getOutput(), false));
        }
        builder.append("\n");
        if (model.getInputConfig() != null || model.getOutputConfig() != null){
            builder.append("Hierarchies\n");
            if (model.getInputConfig() != null){
                builder.append(" - Input\n");
                for (Entry<String, Hierarchy> entry : model.getInputConfig().getHierarchies().entrySet()) {
                    builder.append("   * "+entry.getKey()+": height="+entry.getValue().getHierarchy()[0].length);
                }    
            }
            if (model.getOutputConfig() != null){
                builder.append(" - Input\n");
                for (Entry<String, Hierarchy> entry : model.getOutputConfig().getHierarchies().entrySet()) {
                    builder.append("   * "+entry.getKey()+": height="+entry.getValue().getHierarchy()[0].length);
                }    
            }
            builder.append("\n");
        }
        
        builder.append("Visualization\n");
        builder.append(" - Hidden   : ").append(!model.isVisualizationEnabled()).append("\n");
        builder.append(" - Hidden at: ").append(model.getMaximalSizeForComplexOperations()).append("\n");
        builder.append("\n");
        builder.append("Event log\n");
        if (eventBuffer.isEmpty()) {
            builder.append(" - Empty\n");
        } else {
            for (String s : eventBuffer){
                builder.append(s).append("\n");
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Returns a string representation of a handle
     * @param prefix
     * @param handle
     * @return
     */
    private String getDebugData(String prefix, DataHandle handle, boolean view){
        
        StringBuilder builder = new StringBuilder();
        builder.append("DataHandle@").append(handle.hashCode());
        if (handle.isOrphaned()) {
            builder.append(" [Orphaned]\n");
        } else {
            builder.append("\n");
            builder.append(prefix).append("DataDefinition@").append(handle.getDefinition().hashCode());
            builder.append(handle.getDefinition().isLocked() ? " [Locked]\n" : "\n");
            if (!view) {
                builder.append(prefix).append("View").append(getDebugData(prefix+"View", handle.getView(), true));
            }
        }  
        return builder.toString();
    }

    /**
     * Returns a string representation of a definition
     * @param definition
     * @return
     */
    private String getDebugData(DataDefinition definition){
        
        StringBuilder builder = new StringBuilder();
        builder.append("DataDefinition@").append(definition.hashCode());
        builder.append(definition.isLocked() ? " [Locked]\n" : "\n");
        return builder.toString();
    }

    /**
     * Adds an event to the buffer
     * @param event
     */
    public void addEvent(ModelEvent event) {
        this.eventBuffer.add(event.toString());
        if (this.eventBuffer.size() > MAX_BUFFER_SIZE) {
            this.eventBuffer.remove(0);
        }
    }

    /**
     * Clears the event log
     */
    public void clearEventLog() {
        this.eventBuffer.clear();
    }    
}
