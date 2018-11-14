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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataHandleSubset;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelRiskBasedCriterion;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.common.ViewData;	//temp import
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * A view on a <code>Data</code> object within the masking tab.
 *
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class ViewMaskedData extends ViewData {
 
    /**
     * 
     * Creates a new (non-editable) data view.
     *
     * @param parent
     * @param controller
     */
    public ViewMaskedData(final Composite parent,
                         final Controller controller,
                         final String helpid) {
        this (parent, controller, helpid, false);
    }
    
    /**
     * 
     * Creates a new data view.
     *
     * @param parent
     * @param controller
     * @param editable
     */
    public ViewMaskedData(final Composite parent,
                         final Controller controller,
                         final String helpid,
                         final boolean editable) {
        
        super(parent, controller, helpid, Resources.getMessage("MaskingView.0")); //$NON-NLS-1$
        
        // Register
        controller.addListener(ModelPart.RESEARCH_SUBSET, this);
        controller.addListener(ModelPart.RESULT, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.ATTRIBUTE_VALUE, this);
        controller.addListener(ModelPart.RESPONSE_VARIABLES, this);
        
        // Make editable
        if (editable) {
            final Menu menu = new Menu(parent.getShell());
            MenuItem item1 = new MenuItem(menu, SWT.NONE);
            item1.setText(Resources.getMessage("ViewDataInput.0")); //$NON-NLS-1$
            item1.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent arg0) {
                    controller.actionMenuEditFindReplace();
                }
            });
            MenuItem item2 = new MenuItem(menu,SWT.NONE);
            item2.setText(Resources.getMessage("PropertiesView.34"));
            item2.addSelectionListener(new SelectionAdapter() {
            	 @Override
            	 public void widgetSelected(final SelectionEvent arg0) {
            		 actionAttributeTypeChanged(model.getSelectedAttribute(), AttributeType.INSENSITIVE_ATTRIBUTE);
            		 refreshHeader();
            	 }
            });
            MenuItem item3 = new MenuItem(menu,SWT.NONE);
            item3.setText(Resources.getMessage("PropertiesView.27"));
            item3.addSelectionListener(new SelectionAdapter() {
           	 	@Override
           	 	public void widgetSelected(final SelectionEvent arg0) {
           	 		actionAttributeTypeChanged(model.getSelectedAttribute(), AttributeType.SENSITIVE_ATTRIBUTE);
           	 		refreshHeader();
           	 	}
           });
            MenuItem item4 = new MenuItem(menu,SWT.NONE);
            item4.setText(Resources.getMessage("PropertiesView.20"));
            item4.addSelectionListener(new SelectionAdapter() {
           	 	@Override
           	 	public void widgetSelected(final SelectionEvent arg0) {
           	 		actionAttributeTypeChanged(model.getSelectedAttribute(), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
           	 		refreshHeader();
           	 	}
           });
            MenuItem item5 = new MenuItem(menu,SWT.NONE);
            item5.setText(Resources.getMessage("PropertiesView.13"));
            item5.addSelectionListener(new SelectionAdapter() {
           	 	@Override
           	 	public void widgetSelected(final SelectionEvent arg0) {
           	 		actionAttributeTypeChanged(model.getSelectedAttribute(), AttributeType.IDENTIFYING_ATTRIBUTE);
           	 		refreshHeader();
           	 	}
           });

            table.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(MouseEvent arg0) {
                    if (model != null && model.getSelectedAttribute() != null &&
                        model.getInputConfig() != null &&
                        model.getInputConfig().getInput() != null &&
                        model.getInputConfig().getInput().getHandle() != null) {
                        menu.setEnabled(true);
                    } else {
                        menu.setEnabled(false);
                    } 
                    if (arg0.button == 3 && menu.isEnabled()) {
                        Point display = table.toDisplay(arg0.x, arg0.y);
                        menu.setLocation(display.x, display.y);
                        menu.setVisible(true);
                    }
                }
            });
        }
    }
    
    public void refreshHeader()
    {
    	// Obtain data definition
        DataDefinition definition = getDefinition();

        // Update the table
        DataHandle handle = getHandle();
        
        // Update the attribute types
        table.getHeaderImages().clear();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            updateHeaderImage(i, handle.getAttributeName(i), definition);
        }
        // Redraw
        table.setEnabled(true);
        table.redraw();
        this.enableSorting();
    }
    
    @Override
    public void update(final ModelEvent event) {
        
        super.update(event);

        if (event.part == ModelPart.INPUT) {

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
            table.setResearchSubset(model.getInputConfig().getResearchSubset());
            table.setGroups(null);
            table.setData(handle);
            
            // Update the attribute types
            table.getHeaderImages().clear();
            for (int i = 0; i < handle.getNumColumns(); i++) {
                updateHeaderImage(i, handle.getAttributeName(i), definition);
            }
            
            // Redraw
            table.setEnabled(true);
            table.redraw();
            this.enableSorting();

        } else if (event.part == ModelPart.RESEARCH_SUBSET) {
            
            // Update research subset
            table.setResearchSubset((RowSet)event.data);
            table.redraw();
            
        } else if (event.part == ModelPart.ATTRIBUTE_VALUE) {
            table.redraw();
            
        } else if (event.part == ModelPart.SELECTED_VIEW_CONFIG || event.part == ModelPart.RESULT) {

            // Update the table
            DataHandle handle = getHandle();

            // Check
            if (handle == null) {
                reset();
                return;
            }
            
            table.setData(handle);
            table.setGroups(null);
            table.setResearchSubset(model.getInputConfig().getResearchSubset());
            table.redraw();
            
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE || event.part == ModelPart.RESPONSE_VARIABLES) {
            
            if (model != null){
                
            DataHandle handle = getHandle();
   
                if (handle != null) {
    
                    final String attr = (String) event.data;
    
                    // Obtain data definition
                    DataDefinition definition = getDefinition();
    
                    // Update the attribute types
                    final int index = handle.getColumnIndexOf(attr);
                    updateHeaderImage(index, attr, definition);
    
                    // Redraw
                    table.setEnabled(true);
                    table.redraw();
                }
            }
        }
    }
    
    @Override
    protected void actionCellSelected(CellSelectionEvent arg1) {

    	super.actionCellSelected(arg1);
    	
        if (model == null) return;
        
        int column = arg1.getColumnPosition();
        int row = arg1.getRowPosition();
        if (column == 0 && row >= 0) {

            // Remap row index if showing the subset
            if (table.getData() instanceof DataHandleSubset) {
                int[] subset = ((DataHandleSubset) table.getData()).getSubset();
                row = subset[row];
            }

            // Perform change
            RowSet subset = model.getInputConfig().getResearchSubset();
            if (subset.contains(row)) {
                subset.remove(row);
            } else {
                subset.add(row);
            }
            
            // Fire event
            model.setSubsetManual();
            controller.update(new ModelEvent(this,  ModelPart.RESEARCH_SUBSET, subset));
        }
    }

    @Override
    protected void actionSort(){
        controller.actionDataSort(true);
    }

    @Override
    protected DataDefinition getDefinition() {
        if (model == null) return null;
        else return model.getInputDefinition();
    }

    @Override
    protected DataHandle getHandle() {
        if (model != null){
            
            Data data = model.getInputConfig().getInput();
            if (data == null) {
                return null;
            }
            DataHandle handle = data.getHandle();
            
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
    private void actionAttributeTypeChanged(String attribute, AttributeType type) {
        if ((model != null) && (model.getInputConfig().getInput() != null)) {
            final DataDefinition definition = model.getInputDefinition();

            // Handle QIs
            if (type == null) {
                definition.setAttributeType(attribute, Hierarchy.create());
            } else {
                definition.setAttributeType(attribute, type);
            }

            // Do we need to disable criteria?
            boolean criteriaDisabled = false;

            // Enable/disable criteria for sensitive attributes
            if (type != AttributeType.SENSITIVE_ATTRIBUTE) {

                if (model.getLDiversityModel().get(attribute).isEnabled() ||
                    model.getTClosenessModel().get(attribute).isEnabled() ||
                    model.getBLikenessModel().get(attribute).isEnabled() ||
                    model.getDDisclosurePrivacyModel().get(attribute).isEnabled()) {
                    criteriaDisabled = true;
                }

                model.getBLikenessModel().get(attribute).setEnabled(false);
                model.getTClosenessModel().get(attribute).setEnabled(false);
                model.getLDiversityModel().get(attribute).setEnabled(false);
                model.getDDisclosurePrivacyModel().get(attribute).setEnabled(false);
            }

            // Enable/disable criteria for quasi-identifiers
            if (definition.getQuasiIdentifyingAttributes().isEmpty()) {

                if (model.getKAnonymityModel().isEnabled() ||
                    model.getDPresenceModel().isEnabled() ||
                    model.getStackelbergModel().isEnabled()) {
                    criteriaDisabled = true;
                }

                model.getKAnonymityModel().setEnabled(false);
                model.getDPresenceModel().setEnabled(false);
                model.getStackelbergModel().setEnabled(false);
                for (ModelRiskBasedCriterion c : model.getRiskBasedModel()) {
                    if (c.isEnabled()) {
                        criteriaDisabled = true;
                    }
                    c.setEnabled(false);
                }

            }


            // Update criteria
            if (criteriaDisabled) {
                controller.update(new ModelEvent(this,
                                                 ModelPart.CRITERION_DEFINITION,
                                                 null));
            }

            // Update the views
            controller.update(new ModelEvent(this,
                                             ModelPart.ATTRIBUTE_TYPE,
                                             attribute));
            
            // Update Masking Type
            controller.update(new ModelEvent(this,
                    ModelPart.MASKING_ATTRIBUTE_CHANGED,
                    null));
        }
    }
}
