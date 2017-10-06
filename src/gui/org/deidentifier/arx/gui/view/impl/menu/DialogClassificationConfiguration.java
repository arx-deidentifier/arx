package org.deidentifier.arx.gui.view.impl.menu;

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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression.PriorFunction;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;
import org.deidentifier.arx.aggregates.ClassificationConfigurationSVM;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.preferences.PreferenceDouble;
import de.linearbits.preferences.PreferenceInteger;
import de.linearbits.preferences.PreferenceSelection;
import de.linearbits.preferences.PreferencesDialog;

/**
 * This class implements a dialog for editing a classification configuration
 *
 * @author Johanna Eicher
 * @author Fabian Prasser
 */
public class DialogClassificationConfiguration implements IDialog {

    /** Window */
    private final PreferencesDialog                 dialog;
    /** Config */
    private final ARXClassificationConfiguration<?> config;

    /**
     * Creates a new instance
     * @param parent
     * @param config
     */
    public DialogClassificationConfiguration(Shell parent,
                                             ARXClassificationConfiguration<?> config) {

        this.dialog = new PreferencesDialog(parent,
                                            Resources.getMessage("DialogClassificationConfiguration.0"), //$NON-NLS-1$
                                            Resources.getMessage("DialogClassificationConfiguration.1")); //$NON-NLS-1$
        
        this.config = config;

        if (config instanceof ClassificationConfigurationLogisticRegression) {
            createContentForLogisticRegression((ClassificationConfigurationLogisticRegression) config);
        } else if (config instanceof ClassificationConfigurationNaiveBayes) {
            // TODO
        } else if (config instanceof ClassificationConfigurationRandomForest) {
            // TODO
        } else if (config instanceof ClassificationConfigurationSVM) {
            // TODO
        } else {
            throw new IllegalArgumentException("Unknown classification configuration");
        }
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.dialog.open();
    }
    
    /**
     * Creates content for logistic regression
     * @param config
     */
    private void createContentForLogisticRegression(final ClassificationConfigurationLogisticRegression config) {
        this.dialog.addCategory(Resources.getMessage("DialogClassificationConfiguration.2")); //$NON-NLS-1$
                  
        // Alpha
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.11")) { //$NON-NLS-1$
            protected Double getValue() { return config.getAlpha(); }
            protected void setValue(Object t) { config.setAlpha((Double)t); }});
        
        // Decay exponent
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.12")) { //$NON-NLS-1$
            protected Double getValue() { return config.getDecayExponent(); }
            protected void setValue(Object t) { config.setDecayExponent((Double)t); }});
        
        // Lambda
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.14")) { //$NON-NLS-1$
            protected Double getValue() { return config.getLambda(); }
            protected void setValue(Object t) { config.setLambda((Double)t); }});
        
        // Learning rate
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.15")) { //$NON-NLS-1$
            protected Double getValue() { return config.getLearningRate(); }
            protected void setValue(Object t) { config.setLearningRate((Double)t); }});
        
        // Prior function
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getPriorFunctions()) { //$NON-NLS-1$
            protected String getValue() { return config.getPriorFunction().name(); }
            protected void setValue(Object arg0) { config.setPriorFunction(PriorFunction.valueOf((String)arg0));  }
        });
        
        // Step offset
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.20")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getStepOffset(); }
            protected void setValue(Object t) { config.setStepOffset((Integer)t); }});
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
     * Returns a modified configuration, if available, <code>null</code>, otherwise.
     * @return
     */
    public ARXClassificationConfiguration<?> getResult() {
        if (dialog.getReturnCode() == PreferencesDialog.OK) {
            return config;
        } else {
            return null;
        }
    }
}