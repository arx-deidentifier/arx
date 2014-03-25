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

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;

public class DebugData {

    private Model model;
    
    protected DebugData(Model model){
        this.model = model;
    }
    
    protected String getData(){

        if (model == null) return "No debug data available"; //$NON-NLS-1$
        
        StringBuilder builder = new StringBuilder();
        builder.append("Handles\n");
        builder.append(" - Input\n");
        builder.append("   * Input   : ").append(getDebugData(model.getInputConfig().getInput().getHandle())).append("\n");
        builder.append("   * Input(V): ").append(getDebugData(model.getInputConfig().getInput().getHandle().getView())).append("\n");
        if (model.getOutput() != null) {
            builder.append(" - Output\n");
            builder.append("   * Input    : ").append(getDebugData(model.getOutputConfig().getInput().getHandle())).append("\n");
            builder.append("   * Input(V) : ").append(getDebugData(model.getOutputConfig().getInput().getHandle().getView())).append("\n");
            builder.append("   * Output   : ").append(getDebugData(model.getOutput())).append("\n");
            builder.append("   * Output(V): ").append(getDebugData(model.getOutput().getView())).append("\n");
        }
        builder.append("Definitions\n");
        builder.append(" - Input\n");
        
        DataDefinition definition = model.getInputConfig().getInput().getDefinition();
        builder.append("   * Data    : ").append(getDebugData(definition)).append("\n");
        definition = model.getInputConfig().getInput().getHandle().getDefinition();
        builder.append("   * Input   : ").append(getDebugData(definition)).append("\n");
        definition = model.getInputConfig().getInput().getHandle().getView().getDefinition();
        builder.append("   * Input(V): ").append(getDebugData(definition)).append("\n");

        if (model.getOutput() != null) {
            builder.append(" - Output\n");

            definition = model.getOutputConfig().getInput().getDefinition();
            builder.append("   * Data     : ").append(getDebugData(definition)).append("\n");

            definition = model.getOutputConfig().getInput().getHandle().getDefinition();
            builder.append("   * Input    : ").append(getDebugData(definition)).append("\n");

            definition = model.getOutputConfig().getInput().getHandle().getView().getDefinition();
            builder.append("   * Input(V) : ").append(getDebugData(definition)).append("\n");

            definition = model.getOutput().getDefinition();
            builder.append("   * Output   : ").append(getDebugData(definition)).append("\n");

            definition = model.getOutput().getView().getDefinition();
            builder.append("   * Output(V): ").append(getDebugData(definition)).append("\n");
        }
        
        return builder.toString();
    }
    
    private String getDebugData(DataHandle handle){
        return "@"+handle.hashCode()+(handle.isOrphaned() ? "[Orphaned]" : "");
    }
    
    private String getDebugData(DataDefinition definition){
        return "@"+definition.hashCode()+(definition.isLocked() ? "[Locked]" : "");
    }
}
