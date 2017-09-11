/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.gui.view.impl.utility;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelLocalRecoding.LocalRecodingMode;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentGSSlider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * This view allows to use local recoding
 *
 * @author Fabian Prasser
 */
public class ViewLocalRecoding implements IView, ViewStatisticsBasic {

    /** Internal stuff. */
    private Model                 model;

    /** Internal stuff. */
    private final Controller      controller;

    /** Internal stuff. */
    private Button                button;

    /** Internal stuff. */
    private final Composite       root;

    /** Widget */
    private ComponentGSSlider     slider;

    /** Widget */
    private Combo                 combo;

    /** Constant */
    private static final double[] FIXPOINT_PARAMETERS  = new double[] { 0.01d, 0.05d, 0.1d, 0.2d, 0.3d};
    
    /** Constant */
    private static final double[] MULTIPASS_PARAMETERS = new double[] { 10d, 50d, 100d, 500d, 1000d };

    /**
     * Constructor.
     *
     * @param parent
     * @param controller
     */
    protected ViewLocalRecoding(final Composite parent,
                                final Controller controller) {

        // Register
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.RESULT, this);
        this.controller = controller;
        this.root = parent;
        this.create(this.root);
    }
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public Composite getParent() {
        return this.root;
    }

    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.LOCAL_RECODING;
    }
    
    @Override
    public void reset() {
        slider.setSelection(0.5d);
        SWTUtil.disable(root);
    }
    
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
        }
        
        this.update();
    }

    /**
     * Creates the view.
     *
     * @param group
     */
    private void create(Composite root) {
        GridLayout layout = SWTUtil.createGridLayout(1);
        layout.marginBottom = 3;
        layout.marginTop = 3;
        layout.marginLeft = 3;
        layout.marginRight = 3;
        root.setLayout(layout);
        root = new Composite(root, SWT.NONE);
        root.setLayoutData(SWTUtil.createFillGridData());
        root.setLayout(SWTUtil.createGridLayout(2));

        this.slider = new ComponentGSSlider(root);
        this.slider.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (model != null && model.getInputConfig() != null) {
                    model.getLocalRecodingModel().setGsFactor(slider.getSelection());
                }
            }
        });
        this.slider.setLayoutData(SWTUtil.createFillGridData(2));
        
        this.combo = new Combo(root, SWT.READ_ONLY);
        this.combo.setItems(getListOftModes());
        this.combo.select(0);
        this.combo.setLayoutData(SWTUtil.createFillHorizontallyGridData(false));
        this.combo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (combo.getSelectionIndex() != -1) {
                    String item = combo.getItem(combo.getSelectionIndex());
                    for (LocalRecodingMode mode : LocalRecodingMode.values()) {
                        if (mode == LocalRecodingMode.FIXPOINT_ADAPTIVE) {
                            for (double parameter : FIXPOINT_PARAMETERS) {
                                if (item.equals(getLabelForMode(mode, parameter))) {
                                    model.getLocalRecodingModel().setMode(LocalRecodingMode.FIXPOINT_ADAPTIVE);
                                    model.getLocalRecodingModel().setAdaptionFactor(parameter);
                                    return;
                                }
                            }
                        } else if (mode == LocalRecodingMode.MULTI_PASS) {
                            for (double parameter : MULTIPASS_PARAMETERS) {
                                if (item.equals(getLabelForMode(mode, parameter))) {
                                    model.getLocalRecodingModel().setMode(LocalRecodingMode.MULTI_PASS);
                                    model.getLocalRecodingModel().setNumIterations((int)parameter);
                                    return;
                                }
                            }
                        } else if (mode == LocalRecodingMode.SINGLE_PASS) {
                            if (item.equals(getLabelForMode(mode, 0d))) {
                                model.getLocalRecodingModel().setMode(LocalRecodingMode.SINGLE_PASS);
                                return;
                            }
                        } else if (mode == LocalRecodingMode.FIXPOINT) {
                            if (item.equals(getLabelForMode(mode, 0d))) {
                                model.getLocalRecodingModel().setMode(LocalRecodingMode.FIXPOINT);
                                return;
                            }
                        }
                    }
                }
            }
        });
        
        this.button = new Button(root, SWT.NONE);
        this.button.setText(Resources.getMessage("ViewLocalRecoding.0")); //$NON-NLS-1$
        this.button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (model.getResult() != null && model.getOutput() != null) {
                    controller.actionApplyLocalRecoding();
                }
            }
        });
    }

    /**
     * Returns the index of the given element in the given array
     * @param array
     * @param element
     * @return
     */
    private int getIndexOf(String[] array, String element) {
        for (int i=0; i<array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a string label for the mode
     * @param mode
     * @return
     */
    private String getLabelForMode(LocalRecodingMode mode) {
        String label = mode.toString().replace("_", "-").toLowerCase();
        return label.substring(0, 1).toUpperCase() + label.substring(1);
    }

    /**
     * Returns a label for the given configuration
     * @param mode
     * @param parameter
     * @return
     */
    private String getLabelForMode(LocalRecodingMode mode, double parameter) {
        String label = getLabelForMode(mode);
        if (mode == LocalRecodingMode.FIXPOINT_ADAPTIVE) {
            return label + " (" + parameter + ")";
        } else if (mode == LocalRecodingMode.MULTI_PASS) {
            return label + " (" + (int)parameter + ")";
        } else if (mode == LocalRecodingMode.SINGLE_PASS) {
            return label;
        } else if (mode == LocalRecodingMode.FIXPOINT) {
            return label;
        } else {
            throw new RuntimeException("Unknown mode");
        }
    }

    /**
     * Returns a list of entries for the combo box
     * @return
     */
    private String[] getListOftModes() {
        List<String> result = new ArrayList<String>();
        for (LocalRecodingMode mode : LocalRecodingMode.values()) {
            if (mode == LocalRecodingMode.FIXPOINT_ADAPTIVE) {
                for (double parameter : FIXPOINT_PARAMETERS) {
                    result.add(getLabelForMode(mode, parameter));
                }
            } else if (mode == LocalRecodingMode.MULTI_PASS) {
                for (double parameter : MULTIPASS_PARAMETERS) {
                    result.add(getLabelForMode(mode, parameter));
                }
            } else if (mode == LocalRecodingMode.SINGLE_PASS) {
                result.add(getLabelForMode(mode, 0d));
            } else if (mode == LocalRecodingMode.FIXPOINT) {
                result.add(getLabelForMode(mode, 0d));
            }
        }
        return result.toArray(new String[result.size()]);
    }


    /**
     * Updates all controlls
     */
    private void update() {
        
        boolean enabled = false;
        if (this.model == null || this.model.getResult() == null || this.model.getOutput() == null) {
            enabled = false;
        } else {
            enabled = this.model.getResult().isOptimizable(this.model.getOutput());
        }
        if (!enabled) {
            reset();
            return;
        } 

        SWTUtil.enable(root);
        this.button.setEnabled(true);
        this.slider.setSelection(this.model.getLocalRecodingModel().getGsFactor());

        String label = null;
        LocalRecodingMode mode = this.model.getLocalRecodingModel().getMode();
        if (mode == LocalRecodingMode.FIXPOINT_ADAPTIVE) {
            label = getLabelForMode(mode, this.model.getLocalRecodingModel().getAdaptionFactor());
        } else if (mode == LocalRecodingMode.MULTI_PASS) {
            label = getLabelForMode(mode, this.model.getLocalRecodingModel().getNumIterations());
        } else {
            label = getLabelForMode(mode, 0d);
        }

        this.combo.select(getIndexOf(this.combo.getItems(), label));
    }
}
