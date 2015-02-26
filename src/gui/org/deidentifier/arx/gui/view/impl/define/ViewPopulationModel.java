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

package org.deidentifier.arx.gui.view.impl.define;


import java.text.DecimalFormat;

import org.deidentifier.arx.ARXPopulationModel.Region;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
    /** View */
    private DecimalFormat    format = new DecimalFormat("0.########################################");
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
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        create(root);
        
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        text.setText("");
        text2.setText("");
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

        Label lbl1 = new Label(parent, SWT.NONE);
        lbl1.setText("Region:");
        
        combo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY);
        for (Region region : Region.values()) {
            combo.add(region.getName());
        }
        combo.setEnabled(false);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        Label lbl2 = new Label(parent, SWT.NONE);
        lbl2.setText("Sample fraction:");
        
        text = new Text(parent, SWT.BORDER | SWT.SINGLE);
        text.setText("0");
        text.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        Label lbl3 = new Label(parent, SWT.NONE);
        lbl3.setText("Population size:");
        
        text2 = new Text(parent, SWT.BORDER | SWT.SINGLE);
        text2.setText("0");
        text2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        combo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (model.getInputConfig() != null && model.getInputConfig().getInput() != null && combo.getSelectionIndex() != -1) {
                    long sampleSize = model.getInputConfig().getInput().getHandle().getNumRows();
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
                        controller.update(new ModelEvent(ViewPopulationModel.this, ModelPart.POPULATION_MODEL, model.getRiskModel()));
                        text.setText(format.format(model.getRiskModel().getSampleFraction(sampleSize)));
                        text2.setText(format.format(model.getRiskModel().getPopulationSize(sampleSize)));
                    }
                }
            }
        });
        
        text.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent arg0) {
                double value;
                try {
                    value = format.parse(text.getText()).doubleValue();
                } catch (Exception e) {
                    value = -1;
                }
                
                if (value <= 0d || value > 1d) {
                    text.setForeground(GUIHelper.COLOR_RED);
                    return;
                } else {
                    text.setForeground(GUIHelper.COLOR_BLACK);
                }

                DataHandle handle = model.getInputConfig().getInput().getHandle();
                if (value == model.getRiskModel().getSampleFraction(handle)) {
                    return;
                }
                
                model.getRiskModel().setSampleFraction(value);
                
                for (int i=0; i<combo.getItemCount(); i++) {
                    if (combo.getItem(i).equals(Region.NONE.getName())) {
                        combo.select(i);
                        break;
                    }
                }
                text2.setText(format.format(model.getRiskModel().getPopulationSize(handle.getNumRows())));
                controller.update(new ModelEvent(ViewPopulationModel.this, ModelPart.POPULATION_MODEL, model.getRiskModel()));
            } 
        });
        
        text2.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent arg0) {
                double value;
                try {
                    value = format.parse(text2.getText()).doubleValue();
                } catch (Exception e) {
                    value = -1;
                }

                if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
                    return;
                }
                
                DataHandle handle = model.getInputConfig().getInput().getHandle();
                
                if (value < handle.getNumRows()) {
                    text2.setForeground(GUIHelper.COLOR_RED);
                    return;
                } else {
                    text2.setForeground(GUIHelper.COLOR_BLACK);
                }

                if (value == model.getRiskModel().getPopulationSize(handle)) {
                    return;
                }
                
                model.getRiskModel().setPopulationSize(handle, value);

                for (int i=0; i<combo.getItemCount(); i++) {
                    if (combo.getItem(i).equals(Region.NONE.getName())) {
                        combo.select(i);
                        break;
                    }
                }
                text.setText(format.format(model.getRiskModel().getSampleFraction(handle)));
                controller.update(new ModelEvent(ViewPopulationModel.this, ModelPart.POPULATION_MODEL, model.getRiskModel()));
            } 
        });
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        root.setRedraw(false);
        
        for (int i=0; i<combo.getItemCount(); i++) {
            if (combo.getItem(i).equals(model.getRiskModel().getRegion().getName())) {
                combo.select(i);
                break;
            }
        }
        
        DataHandle handle = model.getInputConfig().getInput().getHandle();
        text.setText(format.format(model.getRiskModel().getSampleFraction(handle)));
        text2.setText(format.format(model.getRiskModel().getPopulationSize(handle)));
        
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
}
