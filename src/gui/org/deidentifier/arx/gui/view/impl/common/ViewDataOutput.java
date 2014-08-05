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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * A view on a <code>Data</code> object
 * @author Fabian Prasser
 */
public class ViewDataOutput extends ViewData {

    /** 
     * Creates a new data view
     * 
     * @param parent
     * @param controller
     */
    public ViewDataOutput(final Composite parent,
                         final Controller controller) {
        
        super(parent, controller, Resources.getMessage("AnalyzeView.0")); //$NON-NLS-1
    }
    
    @Override
    protected void actionCellSelected(CellSelectionEvent arg1) {
    	super.actionCellSelected(arg1);
    }
    
    @Override
    protected void actionSort(){
        controller.actionDataSort(false);
    }

    @Override
    protected DataDefinition getDefinition() {
        if (model == null) return null;
        else return model.getOutputDefinition();
    }

    @Override
    protected DataHandle getHandle() {
        if (model != null){
            DataHandle handle = model.getOutput();
            if (model.getViewConfig().isSubset() && 
                model.getOutputConfig() != null &&
                model.getOutputConfig().getConfig() != null) {
                handle = handle.getView();
            }
            return handle;
        } else {
            return null;
        }
    }
    
    /**
     * Returns the research subset
     */
    private RowSet getSubset(){
        if (model != null && model.getOutputConfig() != null){
            DPresence d = model.getOutputConfig().getCriterion(DPresence.class);
            if (d != null) {
                return d.getSubset().getSet();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void update(final ModelEvent event) {
        
        super.update(event);

        if (event.part == ModelPart.INPUT) {
            reset();
            return;
        }
        
        if (event.part == ModelPart.OUTPUT) {
            
            // No result avail
            if (event.data == null) {
                reset();
                return;
            } 
            
            // Obtain data definition
            DataDefinition definition = getDefinition();
            
            // Check
            if (definition == null) {
                reset();
                return;
            } 

            // Update the table
            DataHandle handle = getHandle();

            // Check
            if (handle == null) {
                reset();
                return;
            } 

            // Use input subset
            table.setResearchSubset(getSubset());
            table.setGroups(model.getGroups());
            table.setData(handle);
            
            // Update the attribute types
            table.getHeaderImages().clear();
            for (int i = 0; i < handle.getNumColumns(); i++) {
                updateHeaderImage(i, definition.getAttributeType(handle.getAttributeName(i)));
            }
            
            // Redraw
            table.setEnabled(true);
            table.redraw();
            this.enableSorting();

        } else if (event.part == ModelPart.RESEARCH_SUBSET) {
            
            // Update research subset
            table.setResearchSubset((RowSet)event.data);
            table.redraw();
            
        } else if (event.part == ModelPart.VIEW_CONFIG || event.part == ModelPart.RESULT) {

            // Update the table
            DataHandle handle = getHandle();

            // Check
            if (handle == null) {
                reset();
                return;
            }
            
            table.setData(handle);
            table.setGroups(model.getGroups());
            table.setResearchSubset(getSubset());
            table.redraw();
        }
    }
}
