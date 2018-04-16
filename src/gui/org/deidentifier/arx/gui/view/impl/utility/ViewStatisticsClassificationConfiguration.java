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

package org.deidentifier.arx.gui.view.impl.utility;


import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view shows a hint message regarding attribute selection for classification analysis
 * 
 * @author Johanna Eicher
 * @author Fabian Prasser
 */
public class ViewStatisticsClassificationConfiguration implements IView, ViewStatisticsBasic {

    /** View */
    private final Composite  root;
    /** Controller */
    private final Controller controller;
    /** Model */
    private Model            model;
    /** Widget */
    private Combo            combo;
    /** View */
    private DynamicTable     table;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationConfiguration(final Composite parent,
                                                        final Controller controller) {
        
        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        
        this.root = parent;
        this.root.setLayout(SWTUtil.createGridLayout(1));
        
        // Composite for classifier combo
        final Composite composite = new Composite(this.root, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        composite.setLayout(SWTUtil.createGridLayout(3, false));
        final Label label = new Label(composite, SWT.PUSH);
        label.setText(Resources.getMessage("ViewClassificationAttributes.32")); //$NON-NLS-1$
        
        // Combo for selecting classifier
        this.combo = new Combo(composite, SWT.READ_ONLY);
        this.combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() >= 0) {
                    String label = combo.getItem(combo.getSelectionIndex());
                    ARXClassificationConfiguration<?> config = getConfiguration(label);
                    if (model != null && model.getClassificationModel().getCurrentConfiguration() != config) {
                        updateTable(config);
                        model.getClassificationModel().setCurrentConfiguration(config);
                        controller.update(new ModelEvent(ViewStatisticsClassificationConfiguration.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
                    }
                }
            }
        });
        
        // Create items
        combo.setItems(new String[]{getLabel(ClassificationConfigurationLogisticRegression.class),
                                    getLabel(ClassificationConfigurationNaiveBayes.class),
                                    getLabel(ClassificationConfigurationRandomForest.class)});
        
        // Button to prompt edit dialog
        Button button = new Button(composite, SWT.PUSH);
        button.setText(Resources.getMessage("ViewClassificationAttributes.30")); //$NON-NLS-1$
        button.setToolTipText(Resources.getMessage("ViewClassificationAttributes.31")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               ARXClassificationConfiguration<?> input = model.getClassificationModel().getCurrentConfiguration().clone();
               ARXClassificationConfiguration<?> output = controller.actionShowClassificationConfigurationDialog(input);
               if (output != null) {

                   // Update
                   model.getClassificationModel().getCurrentConfiguration().parse(output);
                   updateTable(model.getClassificationModel().getCurrentConfiguration());
                   controller.update(new ModelEvent(ViewStatisticsClassificationConfiguration.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));   
               }
            }
        });

        // Table for displaying classifier parameters/values
        this.table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.table.setLayoutData(SWTUtil.createFillGridData(2));
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        DynamicTableColumn c = new DynamicTableColumn(this.table, SWT.LEFT);
        c.setWidth("50%", "100px");
        c.setText(Resources.getMessage("ViewClassificationAttributes.5")); //$NON-NLS-1$
        c = new DynamicTableColumn(this.table, SWT.LEFT);
        c.setWidth("50%", "100px");
        c.setText(Resources.getMessage("ViewClassificationAttributes.6")); //$NON-NLS-1$
        for (final TableColumn col : this.table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(this.table);
    }
    
    @Override
    public void dispose() {
        this.controller.removeListener(this);
    }
    

    @Override
    public Composite getParent() {
        return this.root;
    }

    /**
     * Returns the type
     * 
     * @return
     */
    public ViewUtilityType getType() {
        return ViewUtilityType.CLASSIFICATION;
    }

    @Override
    public void reset() {
        for(TableItem item : this.table.getItems()){
            item.dispose();
        }
        this.table.removeAll();
        SWTUtil.disable(this.root);
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            
            // Store model
            this.model = (Model) event.data;
            
            // Update
            this.combo.select(this.getIndexOf(this.model.getClassificationModel().getCurrentConfiguration().getClass()));
            updateTable(this.model.getClassificationModel().getCurrentConfiguration());
        }
    }

    /**
     * Creates table items for each parameter of a logistic regression
     * configuration.
     * 
     * @param config
     */
    private void createItemsForLogisticRegression(ClassificationConfigurationLogisticRegression config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.11")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getAlpha()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.12")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getDecayExponent()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.14")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getLambda()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.15")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getLearningRate()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.18")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getPriorFunction()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.20")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getStepOffset()));
    }

    /**
     * Creates table items for each parameter of a naive bayes configuration
     * 
     * @param config
     */
    private void createItemsForNaiveBayes(ClassificationConfigurationNaiveBayes config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.22")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSigma()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.23")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getType()));
    }
    
    /**
     * Creates table items for each parameter of a random forest configuration
     * 
     * @param config
     */
    private void createItemsForRandomForest(ClassificationConfigurationRandomForest config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.24")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumberOfTrees()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.33")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumberOfVariablesToSplit()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.34")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMinimumSizeOfLeafNodes()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.35")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMaximumNumberOfLeafNodes()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.36")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSubsample()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.37")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSplitRule()));
    }

    /**
     * Return the classification configuration for this label.
     * @param label
     * @return
     */
    private ARXClassificationConfiguration<?> getConfiguration(String label) {
        
        // List available configurations
        ARXClassificationConfiguration<?>[] configs = new ARXClassificationConfiguration[]{
            this.model.getClassificationModel().getLogisticRegressionConfiguration(),
            this.model.getClassificationModel().getNaiveBayesConfiguration(),
            this.model.getClassificationModel().getRandomForestConfiguration()  
        };
        
        // Search
        for (ARXClassificationConfiguration<?> config : configs) {
            if (label.equals(getLabel(config.getClass()))) {
                return config;
            }
        }
        
        // Nothing found
        return null;
    }
    
    /**
     * Returns the index of the configuration in the combo box
     * @param config
     * @return
     */
    private int getIndexOf(Class<?> config) {
        for (int i=0; i<combo.getItemCount(); i++) {
            if (combo.getItem(i).equals(getLabel(config))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return label for classifier
     * 
     * @param config
     * @return
     */
    private String getLabel(Class<?> config) {
        
        // Logistic regression
        if (config == ClassificationConfigurationLogisticRegression.class) { 
            return Resources.getMessage("ViewClassificationAttributes.7"); 
        }
        // Naive Bayes
        if (config == ClassificationConfigurationNaiveBayes.class) { 
            return Resources.getMessage("ViewClassificationAttributes.8");
        }
        // Random forest
        if (config == ClassificationConfigurationRandomForest.class) { 
            return Resources.getMessage("ViewClassificationAttributes.9"); 
        }
        return null;
    }

    /**
     * Updates the view.
     */
    private void updateTable(ARXClassificationConfiguration<?> config){
        
        // Check
        if (this.model == null || config == null) {
            return;
        }
        
        // Clear
        this.root.setRedraw(false);
        for(TableItem item : this.table.getItems()){
            item.dispose();
        }
        
        // Create items based on config
        if (config instanceof ClassificationConfigurationLogisticRegression) {
            createItemsForLogisticRegression((ClassificationConfigurationLogisticRegression) config);
        } else if (config instanceof ClassificationConfigurationNaiveBayes) {
            createItemsForNaiveBayes((ClassificationConfigurationNaiveBayes) config);
        } else if (config instanceof ClassificationConfigurationRandomForest) {
            createItemsForRandomForest((ClassificationConfigurationRandomForest) config);
        }
        
        this.root.setRedraw(true);
        SWTUtil.enable(this.root);
    }
}
