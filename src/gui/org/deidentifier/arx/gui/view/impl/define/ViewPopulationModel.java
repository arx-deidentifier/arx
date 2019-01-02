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

package org.deidentifier.arx.gui.view.impl.define;


import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This view displays the population settings
 * 
 * @author Fabian Prasser
 */
public class ViewPopulationModel implements IView {

    /** Controller */
    private final Controller controller;

    /** View */
    private final Composite  root;
    /** View */
    private Combo            combo;
    /** View */
    private Text             text;
    /** View */
    private Text             text2;
    /** Model */
    private Model            model;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     * @param layoutCriteria 
     */
    public ViewPopulationModel(final Composite parent,
                               final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
        controller.addListener(ModelPart.MODEL, this);
        this.controller = controller;

        // Create group
        root = new Composite(parent, SWT.NONE);
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());
        create(root);
        
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        text.setText(""); //$NON-NLS-1$
        text2.setText(""); //$NON-NLS-1$
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           update();
        } else if (event.part == ModelPart.INPUT || event.part == ModelPart.POPULATION_MODEL) {
           update();
        }
    }

    /**
     * Creates the required controls.
     * 
     * @param parent
     */
    private void create(final Composite parent) {

        // Region
        Label lbl1 = new Label(parent, SWT.NONE);
        lbl1.setText(Resources.getMessage("ViewPopulationModel.3")); //$NON-NLS-1$
        combo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);
        for (Region region : Region.values()) {
            combo.add(region.getName());
        }
        combo.setEnabled(false);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData(true, 2));
        combo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (model.getInputConfig() != null && model.getInputConfig().getInput() != null && combo.getSelectionIndex() != -1) {
                    Region selected = null;
                    String sselected = combo.getItem(combo.getSelectionIndex());
                    for (Region region : Region.values()) {
                        if (region.getName().equals(sselected)) {
                            selected = region;
                            break;
                        }
                    }
                    if (selected != null) {
                        model.getRiskModel().setRegion(selected);
                        updateLabels();
                        controller.update(new ModelEvent(ViewPopulationModel.this, ModelPart.POPULATION_MODEL, model.getRiskModel()));
                    }
                }
            }
        });
        
        // Sampling fraction
        Label lbl2 = new Label(parent, SWT.NONE);
        lbl2.setText(Resources.getMessage("ViewPopulationModel.4")); //$NON-NLS-1$
        text = new Text(parent, SWT.BORDER | SWT.SINGLE);
        text.setText("0"); //$NON-NLS-1$
        text.setToolTipText("0"); //$NON-NLS-1$
        text.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        text.setEditable(false);
        
        // Button for updating
        Button btn1 = new Button(parent, SWT.FLAT);
        btn1.setText(Resources.getMessage("ViewPopulationModel.0")); //$NON-NLS-1$
        btn1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                String _value = controller.actionShowInputDialog(parent.getShell(), 
                                                                Resources.getMessage("ViewPopulationModel.1"),  //$NON-NLS-1$
                                                                Resources.getMessage("ViewPopulationModel.2"),  //$NON-NLS-1$
                                                                text.getToolTipText(), 
                                                                new IInputValidator(){
                                                                    @Override
                                                                    public String isValid(String arg0) {
                                                                        double value = 0d;
                                                                        try {
                                                                            value = Double.valueOf(arg0);
                                                                        } catch (Exception e) {
                                                                            return Resources.getMessage("ViewPopulationModel.5"); //$NON-NLS-1$
                                                                        }
                                                                        if (value > 0d && value <= 1d) {
                                                                            return null;
                                                                        } else {
                                                                            return Resources.getMessage("ViewPopulationModel.7"); //$NON-NLS-1$
                                                                        }
                                                                    }});
                if (_value != null) {
                    
                    DataHandle handle = model.getInputConfig().getInput().getHandle();
                    long populationSize = (long)Math.round((double)handle.getNumRows() / Double.valueOf(_value));
                    if (populationSize == model.getRiskModel().getPopulationSize()) {
                        return;
                    }
                    
                    model.getRiskModel().setPopulationSize(populationSize);
                    
                    for (int i=0; i<combo.getItemCount(); i++) {
                        if (combo.getItem(i).equals(Region.NONE.getName())) {
                            combo.select(i);
                            break;
                        }
                    }
                    updateLabels();
                    controller.update(new ModelEvent(ViewPopulationModel.this, ModelPart.POPULATION_MODEL, model.getRiskModel()));
                }
            }
        });
        
        Label lbl3 = new Label(parent, SWT.NONE);
        lbl3.setText(Resources.getMessage("ViewPopulationModel.6")); //$NON-NLS-1$
        
        text2 = new Text(parent, SWT.BORDER | SWT.SINGLE);
        text2.setText("0"); //$NON-NLS-1$
        text2.setToolTipText("0"); //$NON-NLS-1$
        text2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        text2.setEditable(false);

        // Button for updating
        Button btn2 = new Button(parent, SWT.FLAT);
        btn2.setText(Resources.getMessage("ViewPopulationModel.8")); //$NON-NLS-1$
        btn2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                
                if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
                    return;
                }
                
                final DataHandle handle = model.getInputConfig().getInput().getHandle();
                
                String _value = controller.actionShowInputDialog(parent.getShell(), 
                                                                Resources.getMessage("ViewPopulationModel.9"),  //$NON-NLS-1$
                                                                Resources.getMessage("ViewPopulationModel.10") + handle.getNumRows(),  //$NON-NLS-1$
                                                                text2.getToolTipText(), 
                                                                new IInputValidator(){
                                                                    @Override
                                                                    public String isValid(String arg0) {
                                                                        int value = 0;
                                                                        try {
                                                                            value = Integer.valueOf(arg0);
                                                                        } catch (Exception e) {
                                                                            return Resources.getMessage("ViewPopulationModel.11"); //$NON-NLS-1$
                                                                        }
                                                                        if (value >= handle.getNumRows()) {
                                                                            return null;
                                                                        } else {
                                                                            return Resources.getMessage("ViewPopulationModel.12"); //$NON-NLS-1$
                                                                        }
                                                                    }});
                if (_value != null) {
                    
                    long value = Long.valueOf(_value);
                    model.getRiskModel().setPopulationSize(value);

                    for (int i=0; i<combo.getItemCount(); i++) {
                        if (combo.getItem(i).equals(Region.NONE.getName())) {
                            combo.select(i);
                            break;
                        }
                    }
                    updateLabels();
                    controller.update(new ModelEvent(ViewPopulationModel.this, ModelPart.POPULATION_MODEL, model.getRiskModel()));
                }
            }
        });
    }
    
    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        // Check
        if (model == null || model.getInputConfig() == null ||
            model.getInputConfig().getInput() == null) { 
            return; 
        }

        root.setRedraw(false);
        
        for (int i=0; i<combo.getItemCount(); i++) {
            if (combo.getItem(i).equals(model.getRiskModel().getRegion().getName())) {
                combo.select(i);
                break;
            }
        }
        
        updateLabels();
        root.setRedraw(true);
        SWTUtil.enable(root);
    }

    /**
     * Updates both labels
     */
    private void updateLabels() {

        if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
            return;
        }

        DataHandle handle = model.getInputConfig().getInput().getHandle();
        long sampleSize = handle.getNumRows();
        long populationSize = (long)model.getRiskModel().getPopulationSize();
        double fraction = (double)sampleSize / (double)populationSize;
        text.setText(SWTUtil.getPrettyString(fraction));
        text.setToolTipText(String.valueOf(fraction));
        text2.setText(SWTUtil.getPrettyString(populationSize));
        text2.setToolTipText(String.valueOf(populationSize));
    }
}
