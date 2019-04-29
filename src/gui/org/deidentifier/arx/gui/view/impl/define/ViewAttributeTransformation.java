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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.MicroAggregationFunctionDescription;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataScale;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelAuditTrailEntry.AuditTrailEntryFindReplace;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRiskBasedCriterion;
import org.deidentifier.arx.gui.model.ModelTransformationMode;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentHierarchy;
import org.deidentifier.arx.gui.view.impl.common.ComponentHierarchyMenu;
import org.deidentifier.arx.gui.view.impl.common.ComponentMultiStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This view displays basic attribute information.
 * 
 * @author Fabian Prasser
 */
public class ViewAttributeTransformation implements IView {
    
    /** Resource */
    private static final AttributeType[] COMBO1_TYPES  = new AttributeType[] {
                                                               AttributeType.INSENSITIVE_ATTRIBUTE,
                                                               AttributeType.SENSITIVE_ATTRIBUTE,
                                                               AttributeType.QUASI_IDENTIFYING_ATTRIBUTE,
                                                               AttributeType.IDENTIFYING_ATTRIBUTE };
    
    /** Resource */
    private static final String[]        COMBO1_VALUES = new String[] {
                                                        Resources.getMessage("AttributeDefinitionView.0"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.1"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.2"), //$NON-NLS-1$
                                                        Resources.getMessage("AttributeDefinitionView.3") }; //$NON-NLS-1$

    /** Resource */
    private static final List<MicroAggregationFunctionDescription> FUNCTIONS     = AttributeType.listMicroAggregationFunctions();

    /** Resource. */
    private static final String                                    ITEM_ALL      = Resources.getMessage("HierarchyView.0");      //$NON-NLS-1$

    /** Model */
    private String                                                 attribute     = null;
    /** Model */
    private Model                                                  model;

    /** Controller */
    private final Controller                                       controller;

    /** Widget */
    private final Combo                                            cmbType;
    /** Widget */
    private final Combo                                            cmbMode;
    /** Widget */
    private final Combo                                            cmbFunction;
    /** Widget */
    private final Button                                           btnMissing;
    /** Widget. */
    private final Combo                                            cmbMin;
    /** Widget. */
    private final Combo                                            cmbMax;
    /** Widget. */
    private final ComponentMultiStack                              stack;
    /** View */
    private final Composite                                        root;
    /** View */
    private final ComponentHierarchyMenu                           menu;
    /** View */
    private final ComponentHierarchy                               hierarchy;

    /**
     * Constructor.
     *
     * @param parent
     * @param attribute
     * @param controller
     */
    public ViewAttributeTransformation(final Composite parent,
                                   final Controller controller) {
        
        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_VALUE, this);
        this.controller.addListener(ModelPart.DATA_TYPE, this);
        this.controller.addListener(ModelPart.HIERARCHY, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        
        // Group
        root = new Composite(parent, SWT.NULL);
        root.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 1;
        root.setLayout(groupInputGridLayout);
        
        // Group
        final Composite innerGroup = new Composite(root, SWT.NULL);
        innerGroup.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout typeInputGridLayout = new GridLayout();
        typeInputGridLayout.numColumns = 4;
        innerGroup.setLayout(typeInputGridLayout);
        
        // Combo for attribute type
        final Label kLabel = new Label(innerGroup, SWT.PUSH);
        kLabel.setText(Resources.getMessage("AttributeDefinitionView.7")); //$NON-NLS-1$
        cmbType = new Combo(innerGroup, SWT.READ_ONLY);
        cmbType.setLayoutData(SWTUtil.createFillGridData());
        cmbType.setItems(COMBO1_VALUES);
        cmbType.select(0);
        cmbType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if ((cmbType.getSelectionIndex() != -1) && (attribute != null)) {
                    
                    // Update combo
                    actionAttributeTypeChanged(attribute, COMBO1_TYPES[cmbType.getSelectionIndex()]);

                    // Update the other views
                    controller.update(new ModelEvent(this, ModelPart.ATTRIBUTE_TYPE, attribute));
                }
            }
        });
        
        // Add combo for mode
        final Label fLabel2 = new Label(innerGroup, SWT.PUSH);
        fLabel2.setText(Resources.getMessage("ViewMicoaggregation.4")); //$NON-NLS-1$
        cmbMode = new Combo(innerGroup, SWT.READ_ONLY);
        cmbMode.setLayoutData(SWTUtil.createFillGridData());
        cmbMode.setItems(new String[]{Resources.getMessage("ViewMicoaggregation.5"),
                                      Resources.getMessage("ViewMicoaggregation.6"),
                                      Resources.getMessage("ViewMicoaggregation.8")});
        cmbMode.select(0);
        cmbMode.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionTransformationModeChanged();
            }
        });
        
        // Create multistack
        stack = new ComponentMultiStack(innerGroup);
        
        // First column
        Composite first = stack.create(SWTUtil.createGridData());
        Composite compositeLabelMin = new Composite(first, SWT.NONE);
        GridLayout compositeLabelMinLayout = new GridLayout();
        compositeLabelMinLayout.numColumns = 1;
        compositeLabelMinLayout.marginLeft = 0;
        compositeLabelMinLayout.marginRight = 0;
        compositeLabelMinLayout.marginWidth = 0;
        compositeLabelMin.setLayout(compositeLabelMinLayout);
        Label labelMin = new Label(compositeLabelMin, SWT.PUSH);
        labelMin.setText(Resources.getMessage("HierarchyView.4")); //$NON-NLS-1$
        Composite compositelabelFunction = new Composite(first, SWT.NONE);
        GridLayout compositelabelFunctionLayout = new GridLayout();
        compositelabelFunctionLayout.numColumns = 1;
        compositelabelFunctionLayout.marginLeft = 0;
        compositelabelFunctionLayout.marginRight = 0;
        compositelabelFunctionLayout.marginWidth = 0;
        compositelabelFunction.setLayout(compositelabelFunctionLayout);
        final Label labelFunction = new Label(compositelabelFunction, SWT.PUSH);
        labelFunction.setText(Resources.getMessage("ViewMicoaggregation.0")); //$NON-NLS-1$

        // Second column
        Composite second = stack.create(SWTUtil.createFillHorizontallyGridData());
        this.cmbMin = new Combo(second, SWT.READ_ONLY);
        this.cmbMin.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.cmbMin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionMinChanged();
            }
        });
        this.cmbFunction = new Combo(second, SWT.READ_ONLY);
        this.cmbFunction.setLayoutData(SWTUtil.createFillGridData());
        this.cmbFunction.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionFunctionChanged();
            }
        });

        // Third column
        Composite third = stack.create(SWTUtil.createGridData());
        Composite compositelabelMax = new Composite(third, SWT.NONE);
        GridLayout compositelabelMaxLayout = new GridLayout();
        compositelabelMaxLayout.numColumns = 1;
        compositelabelMaxLayout.marginLeft = 0;
        compositelabelMaxLayout.marginRight = 0;
        compositelabelMaxLayout.marginWidth = 0;
        compositelabelMax.setLayout(compositelabelMaxLayout);
        Label labelMax = new Label(compositelabelMax, SWT.PUSH);
        labelMax.setText(Resources.getMessage("HierarchyView.6")); //$NON-NLS-1$
        Composite compositelabelMissing = new Composite(third, SWT.NONE);
        GridLayout compositelabelMissingLayout = new GridLayout();
        compositelabelMissingLayout.numColumns = 1;
        compositelabelMissingLayout.marginLeft = 0;
        compositelabelMissingLayout.marginRight = 0;
        compositelabelMissingLayout.marginWidth = 0;
        compositelabelMissing.setLayout(compositelabelMissingLayout);
        Label labelMissing = new Label(compositelabelMissing, SWT.PUSH);
        labelMissing.setText(Resources.getMessage("ViewMicoaggregation.7")); //$NON-NLS-1$

        // Fourth column
        Composite fourth = stack.create(SWTUtil.createFillHorizontallyGridData());
        this.cmbMax = new Combo(fourth, SWT.READ_ONLY);
        this.cmbMax.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.cmbMax.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionMaxChanged();
            }
        });
        btnMissing = new Button(fourth, SWT.CHECK);
        GridData btnMissingData = SWTUtil.createFillGridData();
        btnMissingData.horizontalSpan = 2;
        btnMissing.setLayoutData(btnMissingData);
        btnMissing.setText(Resources.getMessage("ViewMicoaggregation.2")); //$NON-NLS-1$
        btnMissing.setSelection(true);
        btnMissing.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionMissingChanged();
            }
        });
        
        // Collect info about children in stack
        stack.pack();
        stack.setLayer(0);
          
        // Editor hierarchy
        this.hierarchy = new ComponentHierarchy(root, new ModifyListener(){
            @Override public void modifyText(ModifyEvent arg0) {
                actionHierarchyChanged((Hierarchy)arg0.data);
            }
        });
        this.menu = new ComponentHierarchyMenu(hierarchy, controller);
    }
    
    @Override
    public void dispose() {
        controller.removeListener(this);
        menu.dispose();
    }
    
    @Override
    public void reset() {
        if (stack != null) stack.setLayer(0);
        if (cmbType != null && cmbType.getItemCount() != 0) cmbType.select(0);
        if (cmbMode != null && cmbMode.getItemCount() != 0) cmbMode.select(0);
        if (cmbMin != null && cmbMin.getItemCount() != 0) cmbMin.select(0);
        if (cmbMax != null && cmbMax.getItemCount() != 0) cmbMax.select(cmbMax.getItemCount() - 1);
        if (cmbFunction != null && cmbFunction.getItemCount() != 0) cmbFunction.select(0);
        if (hierarchy != null) hierarchy.actionReset();
        SWTUtil.disable(root);
    }
    
    @Override
    public void update(final ModelEvent event) {
        
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            if (model != null) {
                SWTUtil.enable(root);
                attribute = model.getSelectedAttribute();
                updateAttributeType();
                updateFunction();
                updateMode();
                updateMinMax();
                hierarchy.setHierarchy(getHierarchy());
            }
        } else if (event.part == ModelPart.MODEL) {
            SWTUtil.enable(root);
            model = (Model) event.data;
            attribute = model.getSelectedAttribute();
            updateAttributeType();
            updateFunction();
            updateMode();
            updateMinMax();
            hierarchy.setHierarchy(getHierarchy());
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            SWTUtil.enable(root);
            final String attr = (String) event.data;
            if (attr.equals(attribute)) {
                updateAttributeType();
            }
        } else if (event.part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE) {
            SWTUtil.enable(root);
            if (attribute != null) {
                updateAttributeType();
            }
        } else if (event.part == ModelPart.HIERARCHY) {
            SWTUtil.enable(root);
            if (attribute.equals(model.getSelectedAttribute())) {
                hierarchy.setHierarchy(getHierarchy());
                updateMinMax();
            }
        } else if (event.part == ModelPart.ATTRIBUTE_VALUE) {
            SWTUtil.enable(root);
            AuditTrailEntryFindReplace entry = (AuditTrailEntryFindReplace)event.data;
            if (entry.getAttribute().equals(attribute)) {
                hierarchy.setHierarchy(getHierarchy());
            }
        } else if (event.part == ModelPart.INPUT) {
            SWTUtil.enable(root);
            attribute = model.getSelectedAttribute();
            updateAttributeType();
            updateMode();
            updateMinMax();
            hierarchy.setHierarchy(getHierarchy());
        } else if (event.part == ModelPart.DATA_TYPE) {
            SWTUtil.enable(root);
            if (attribute.equals(model.getSelectedAttribute())) {
                updateFunction();
                updateMode();
            }
        } else if (event.part == ModelPart.ATTRIBUTE_VALUE) {
            SWTUtil.enable(root);
            if (attribute.equals(model.getSelectedAttribute())) {
                hierarchy.setHierarchy(getHierarchy());
            }
        }
    }

    /**
     * Attribute type changed
     */
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

            // Update mode
            updateMode();

            // Update criteria
            if (criteriaDisabled) {
                controller.update(new ModelEvent(this, ModelPart.CRITERION_DEFINITION, null));
            }
        }
    }
    
    /**
     * Function changed
     */
    private void actionFunctionChanged() {
        if (cmbFunction.getSelectionIndex() != -1 && model != null && model.getInputConfig() != null) {
            String function = cmbFunction.getItem(cmbFunction.getSelectionIndex());
            model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(function));
        }
    }

    /**
     * Called when the hierarchy changes
     * @param hierarchy
     */
    private void actionHierarchyChanged(Hierarchy hierarchy) {
        
        // Check
        if (model == null || model.getInputConfig() == null) {
            return;
        }
        
        // Update view
        updateMinMax();
        
        // Update model
        if (hierarchy == null || hierarchy.getHierarchy() == null) {
            model.getInputConfig().removeHierarchy(attribute);
        } else {
            model.getInputConfig().setHierarchy(attribute, hierarchy);
        }
        
        // Remove functional hierarchy
        model.getInputConfig().removeHierarchyBuilder(attribute);
        
        // Fire event
        controller.update(new ModelEvent(ViewAttributeTransformation.this, ModelPart.HIERARCHY, hierarchy));
    }
    
    
    /**
     * Updates the max generalization level.
     *
     * @return
     */
    private boolean actionMaxChanged() {
        
        if (cmbMax.getSelectionIndex() >= 0) {
            if (cmbMax.getSelectionIndex() < (cmbMin.getSelectionIndex() - 1)) {
                cmbMax.select(cmbMin.getSelectionIndex() - 1);
            }
            if (model != null) {
                String val = cmbMax.getItem(cmbMax.getSelectionIndex());
                model.getInputConfig().setMaximumGeneralization(attribute, val.equals(ITEM_ALL) ? null : Integer.valueOf(val));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates the min generalization level.
     *
     * @return
     */
    private boolean actionMinChanged() {
        
        if (cmbMin.getSelectionIndex() >= 0) {
            if (cmbMin.getSelectionIndex() > (cmbMax.getSelectionIndex() + 1)) {
                cmbMin.select(cmbMax.getSelectionIndex() + 1);
            }
            if (model != null) {
                String val = cmbMin.getItem(cmbMin.getSelectionIndex());
                model.getInputConfig().setMinimumGeneralization(attribute, val.equals(ITEM_ALL) ? null : Integer.valueOf(val));
                return true;
            }
        }
        return false;
    }

    /**
     * Missing changed
     */
    private void actionMissingChanged() {
        if (model != null && model.getInputConfig() != null) {
            model.getInputConfig().setMicroAggregationIgnoreMissingData(attribute, btnMissing.getSelection());
        }
    }

    /**
     * Transformation mode changed
     */
    private void actionTransformationModeChanged() {
        if (model == null || model.getInputConfig() == null) {
            reset();
        }
        if (cmbMode.getSelectionIndex() == 0) {
            model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.GENERALIZATION);
            stack.setLayer(0);
        } else if (cmbMode.getSelectionIndex() == 1) {
            model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.MICRO_AGGREGATION);
            stack.setLayer(1);
        } else if (cmbMode.getSelectionIndex() == 2) {
            model.getInputConfig().setTransformationMode(attribute, ModelTransformationMode.CLUSTERING_AND_MICRO_AGGREGATION);
            stack.setLayer(1);
        }
    }
    
    /**
     * Update attribute type of all attributes. Called from the parent layout only.
     * @param typeNew
     */
    public void actionUpdateAttributeTypes(AttributeType typeNew) {

        if (model.getInputConfig() != null && model.getInputConfig().getInput() != null) {
            DataHandle handle = model.getInputConfig().getInput().getHandle();
            if (handle != null) {
                // For each attribute, check
                for (int i = 0; i < handle.getNumColumns(); i++) {
                    String attribute = handle.getAttributeName(i);
                    AttributeType type = model.getInputDefinition().getAttributeType(attribute);
                    // Type changed
                    if (type != typeNew) {
                        // Change selection in combo
                        if (attribute.equals(this.attribute)) {
                            for (int k = 0; k < COMBO1_TYPES.length; k++) {
                                if (typeNew == COMBO1_TYPES[k]) {
                                    cmbType.select(k);
                                    break;
                                }
                            }
                        }
                        actionAttributeTypeChanged(attribute, typeNew);
                    }
                }

                // Update the other views
                controller.update(new ModelEvent(this, ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, model.getInputDefinition()));
            }
        }
    }
    
    /**
     * Returns the current hierarchy
     * @return
     */
    private Hierarchy getHierarchy() {
        if (model == null || model.getSelectedAttribute() == null || model.getInputConfig() == null) {
            return null;
        }
        return model.getInputConfig().getHierarchy(model.getSelectedAttribute());
    }
    
    /**
     * Returns the microaggregation function for the given label
     * @param function
     * @return
     */
    private MicroAggregationFunctionDescription getMicroAggregationFunction(String label) {
        for (MicroAggregationFunctionDescription function : FUNCTIONS) {
            if (function.getLabel().equals(label)) {
                return function;
            }
        }
        return null;
    }
    
    /**
     * 
     * Update the attribute type.
     */
    private void updateAttributeType() {
        if (model == null || model.getInputConfig() == null || model.getInputDefinition() == null) {
            reset();
            return;
        }
        AttributeType type = model.getInputDefinition().getAttributeType(attribute);
        for (int i = 0; i < COMBO1_TYPES.length; i++) {
            if (type == COMBO1_TYPES[i]) {
                cmbType.select(i);
                break;
            }
        }
    }
    
    /**
     * Update function
     */
    private void updateFunction() {
        if (model != null && model.getInputConfig() != null && model.getInputDefinition() != null) {
            DataScale scale = model.getInputDefinition().getDataType(attribute).getDescription().getScale();
            List<String> functions = new ArrayList<String>();
            for (MicroAggregationFunctionDescription function : FUNCTIONS) {
                if (scale.provides(function.getRequiredScale())) {
                    functions.add(function.getLabel());
                } 
            }
            this.cmbFunction.setItems(functions.toArray(new String[functions.size()]));
            MicroAggregationFunctionDescription description = model.getInputConfig().getMicroAggregationFunction(attribute);
            int index = description == null ? -1 : functions.indexOf(description.getLabel());
            if (index == -1) {
                this.cmbFunction.select(0);
                this.model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(functions.get(0)));
            } else {
                this.cmbFunction.select(index);
                this.model.getInputConfig().setMicroAggregationFunction(attribute, getMicroAggregationFunction(functions.get(index)));
            }
            this.btnMissing.setSelection(this.model.getInputConfig().getMicroAggregationIgnoreMissingData(attribute));
        }
    }

    /**
     * Updates the combos.
     */
    private void updateMinMax() {
        
        // Check whether min & max are still ok
        if (model == null || model.getInputConfig() == null || cmbMin == null || cmbMin.isDisposed() || model.getInputConfig().getInput() == null) {
            reset();
            return;
        }
        
        // Prepare lists
        final List<String> minItems = new ArrayList<String>();
        final List<String> maxItems = new ArrayList<String>();
        minItems.add(ITEM_ALL);
        int length = 0;
        Hierarchy hierarchy = model.getInputConfig().getHierarchy(attribute);
        if (!(hierarchy == null || hierarchy.getHierarchy() == null || hierarchy.getHierarchy().length == 0 || hierarchy.getHierarchy()[0] == null || hierarchy.getHierarchy()[0].length == 0)) {
            length = hierarchy.getHierarchy()[0].length;
        }
        for (int i = 0; i < length; i++) {
            minItems.add(String.valueOf(i));
            maxItems.add(String.valueOf(i));
        }
        maxItems.add(ITEM_ALL);
        
        // Determine min index
        Integer minModel = model.getInputConfig().getMinimumGeneralization(attribute);
        int minIndex = minModel != null ? minModel + 1 : 0;
        
        // Determine max index
        Integer maxModel = model.getInputConfig().getMaximumGeneralization(attribute);
        int maxIndex = maxModel != null ? maxModel : maxItems.size() - 1;
        
        // Fix indices
        maxIndex = maxIndex > maxItems.size() - 1 ? maxItems.size() - 1 : maxIndex;
        maxIndex = maxIndex < 0 ? maxItems.size() - 1 : maxIndex;
        minIndex = minIndex > minItems.size() - 1 ? minItems.size() - 1 : minIndex;
        minIndex = minIndex < 0 ? 0 : minIndex;
        minIndex = minIndex > (maxIndex + 1) ? maxIndex + 1 : minIndex;
        
        // Set items
        cmbMin.setItems(minItems.toArray(new String[minItems.size()]));
        cmbMax.setItems(maxItems.toArray(new String[maxItems.size()]));
        
        // Select
        cmbMin.select(minIndex);
        cmbMax.select(maxIndex);
        actionMinChanged();
        actionMaxChanged();
    }

    /**
     * Update mode
     */
    private void updateMode() {
        
        if (model == null || model.getInputConfig() == null || model.getInputDefinition() == null) {
            reset();
            return;
        }
        
        if (model.getInputDefinition().getQuasiIdentifyingAttributes().contains(attribute) && model != null && model.getInputConfig() != null) {
            
            if (model.getInputConfig().getTransformationMode(attribute) == ModelTransformationMode.GENERALIZATION) {
                stack.setLayer(0);
                cmbMode.select(0);
            } else if (model.getInputConfig().getTransformationMode(attribute) == ModelTransformationMode.MICRO_AGGREGATION) {
                stack.setLayer(1);
                cmbMode.select(1);
            } else if (model.getInputConfig().getTransformationMode(attribute) == ModelTransformationMode.CLUSTERING_AND_MICRO_AGGREGATION) {
                stack.setLayer(1);
                cmbMode.select(2);
            }
        } else {
            cmbMode.select(0);
            stack.setLayer(0);
        }
    }
}
