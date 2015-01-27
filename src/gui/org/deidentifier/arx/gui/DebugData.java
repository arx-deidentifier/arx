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
 * Class for creating debug data.
 *
 * @author Fabian Prasser
 */
public class DebugData {
    
    /**  TODO */
    private static final int MAX_BUFFER_SIZE = 1000;
    
    /**  TODO */
    private List<String> eventBuffer = new ArrayList<String>();
    
    /**
     * Returns some debug data.
     *
     * @param model
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
        builder.append(" - Definitions\n");
        builder.append("   * Input : ").append(getDebugData(model.getInputDefinition()));
        builder.append("   * Output: ").append(getDebugData(model.getOutputDefinition()));
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
                    builder.append("   * ").append(entry.getKey()).append(": ").append(getDebugData(entry.getValue())).append("\n");
                }    
            }
            if (model.getOutputConfig() != null){
                builder.append(" - Input\n");
                for (Entry<String, Hierarchy> entry : model.getOutputConfig().getHierarchies().entrySet()) {
                    builder.append("   * ").append(entry.getKey()).append(": ").append(getDebugData(entry.getValue())).append("\n");
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
     * Returns a string representation of a handle.
     *
     * @param prefix
     * @param handle
     * @param view
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
     * Returns a string representation of a definition.
     *
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
     * Returns a string representation of a hierarchy.
     *
     * @param hierarchy
     * @return
     */
    private String getDebugData(Hierarchy hierarchy){
        
        if (hierarchy==null || hierarchy.getHierarchy()==null || hierarchy.getHierarchy().length==0) return "empty";
        else return "height="+hierarchy.getHierarchy()[0].length;
    }

    /**
     * Adds an event to the buffer.
     *
     * @param event
     */
    public void addEvent(ModelEvent event) {
        this.eventBuffer.add(event.toString());
        if (this.eventBuffer.size() > MAX_BUFFER_SIZE) {
            this.eventBuffer.remove(0);
        }
    }

    /**
     * Clears the event log.
     */
    public void clearEventLog() {
        this.eventBuffer.clear();
    }    
}
