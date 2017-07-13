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

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.ARXNaiveBayesConfiguration;
import org.deidentifier.arx.ARXRandomForestConfiguration;
import org.deidentifier.arx.ARXSVMConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration.PriorFunction;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.preferences.PreferenceBoolean;
import de.linearbits.preferences.PreferenceDouble;
import de.linearbits.preferences.PreferenceInteger;
import de.linearbits.preferences.PreferenceSelection;
import de.linearbits.preferences.PreferencesDialog;

/**
 * This class implements a dialog for editing a classification configuration
 *
 * @author Johanna Eicher
 */
public class DialogClassificationConfiguration implements IDialog {

    /** Window */
    private final PreferencesDialog dialog;
    /** Model */
    private final Model             model;
    /** Controller */
    private final Controller        controller;

    public DialogClassificationConfiguration(Shell parent,
                                             Model model,
                                             Controller controller) {

        this.dialog = new PreferencesDialog(parent,
                                            Resources.getMessage("DialogClassificationConfiguration.0"), //$NON-NLS-1$
                                            Resources.getMessage("DialogClassificationConfiguration.1")); //$NON-NLS-1$
        this.model = model;
        this.controller = controller;
        ARXClassificationConfiguration config = this.model.getClassificationModel().getCurrentConfiguration();
        
        if (config instanceof ARXLogisticRegressionConfiguration) {
            createContentForLogisticRegression((ARXLogisticRegressionConfiguration) config);
        } else if (config instanceof ARXNaiveBayesConfiguration) {

        } else if (config instanceof ARXRandomForestConfiguration) {

        } else if (config instanceof ARXSVMConfiguration) {

        } else {
            throw new IllegalArgumentException("Unknown classification configuration");
        }

    }

    private void createContentForLogisticRegression(ARXLogisticRegressionConfiguration config) {
        this.dialog.addCategory(Resources.getMessage("DialogClassificationConfiguration.2")); //$NON-NLS-1$
                  
        // Alpha
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.11")) { //$NON-NLS-1$
            protected Double getValue() { return config.getAlpha(); }
            protected void setValue(Object t) { config.setAlpha((Double)t); }});
        
        // Decay exponent
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.12")) { //$NON-NLS-1$
            protected Double getValue() { return config.getDecayExponent(); }
            protected void setValue(Object t) { config.setDecayExponent((Double)t); triggerUpdate(); }});
        
        // Deterministic
        this.dialog.addPreference(new PreferenceBoolean(Resources.getMessage("ViewClassificationAttributes.13")) { //$NON-NLS-1$
            protected Boolean getValue() { return config.isDeterministic(); }
            protected void setValue(Object t) { config.setDeterministic((Boolean)t); triggerUpdate(); }});
        
        // Lambda
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.14")) { //$NON-NLS-1$
            protected Double getValue() { return config.getLambda(); }
            protected void setValue(Object t) { config.setLambda((Double)t); triggerUpdate(); }});
        
        // Learning rate
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.15")) { //$NON-NLS-1$
            protected Double getValue() { return config.getLearningRate(); }
            protected void setValue(Object t) { config.setLearningRate((Double)t); triggerUpdate(); }});
        
        // Maximal number of records
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.16")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMaxRecords(); }
            protected void setValue(Object t) { config.setMaxRecords((Integer)t); triggerUpdate(); }});

        // Number of folds
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.17")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumFolds(); }
            protected void setValue(Object t) { config.setNumFolds((Integer)t); triggerUpdate(); }});
        
        // Prior function
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getPriorFunctions()) { //$NON-NLS-1$
            protected String getValue() { return config.getPriorFunction().name(); }
            protected void setValue(Object arg0) { config.setPriorFunction(PriorFunction.valueOf((String)arg0)); triggerUpdate(); }
        });
        
        // Seed
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.19")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getSeed(); }
            protected void setValue(Object t) { config.setSeed((Integer)t); triggerUpdate(); }});
        
        // Step offset
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.20")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getStepOffset(); }
            protected void setValue(Object t) { config.setStepOffset((Integer)t); triggerUpdate(); }});

        // Vector length
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.21")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getVectorLength(); }
            protected void setValue(Object t) { config.setVectorLength((Integer)t); triggerUpdate(); }});
    }
    
    /**
     * Creates a list of prior functions
     * @return
     */
    private String[] getPriorFunctions() {
        List<String> result = new ArrayList<String>();
        for (PriorFunction function : PriorFunction.values()) {
            result.add(function.name());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.dialog.open();
    }
    
    /**
     * Trigger event to update other views.
     */
    private void triggerUpdate(){
        this.controller.update(new ModelEvent(this, ModelPart.STATISTICAL_CLASSIFIER, null));
    }

}
