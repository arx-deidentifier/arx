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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * A view on a <code>Data</code> object.
 *
 * @author Fabian Prasser
 */
public class ViewDataOutput extends ViewData {

    /**
     * 
     * Creates a new data view.
     *
     * @param parent
     * @param controller
     */
    public ViewDataOutput(final Composite parent,
                         final Controller controller) {
        
        super(parent, controller, Resources.getMessage("AnalyzeView.0")); //$NON-NLS-1
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.ViewData#actionCellSelected(org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent)
     */
    @Override
    protected void actionCellSelected(CellSelectionEvent arg1) {
    	super.actionCellSelected(arg1);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.ViewData#actionSort()
     */
    @Override
    protected void actionSort(){
        controller.actionDataSort(false);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.ViewData#getDefinition()
     */
    @Override
    protected DataDefinition getDefinition() {
        if (model == null) return null;
        else return model.getOutputDefinition();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.ViewData#getHandle()
     */
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
     * Returns the research subset.
     *
     * @return
     */
    private RowSet getSubset(){
        if (model != null && model.getOutputConfig() != null){
            return model.getOutputConfig().getResearchSubset();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.ViewData#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
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
