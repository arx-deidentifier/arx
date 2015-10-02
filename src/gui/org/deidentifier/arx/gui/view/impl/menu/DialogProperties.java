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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelRisk.RiskModelForAttributes;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.io.CSVSyntax;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.preferences.PreferenceBoolean;
import de.linearbits.preferences.PreferenceCharacter;
import de.linearbits.preferences.PreferenceDouble;
import de.linearbits.preferences.PreferenceInteger;
import de.linearbits.preferences.PreferenceSelection;
import de.linearbits.preferences.PreferenceString;
import de.linearbits.preferences.PreferenceText;
import de.linearbits.preferences.PreferencesDialog;

/**
 * This class implements a dialog for editing project properties.
 *
 * @author Fabian Prasser
 */
public class DialogProperties implements IDialog {

    /** Controller */
    private final Controller        controller;

    /** Window */
    private final PreferencesDialog dialog;

    /** Model */
    private final Model             model;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public DialogProperties(final Shell parent, final Model model, final Controller controller) {
        
        // Init
        this.controller = controller;
        this.model = model;
        
        // Create dialog
        this.dialog = new PreferencesDialog(parent, Resources.getMessage("DialogProperties.0"), Resources.getMessage("DialogProperties.1")); //$NON-NLS-1$ //$NON-NLS-2$
        this.createTabProject(this.dialog);
        this.createTabInternals(this.dialog);
        this.createTabTransformation(this.dialog);
        this.createTabSearch(this.dialog);
        this.createTabUtility(this.dialog);
        this.createTabRisk(this.dialog);
    }

    /**
     * Opens the dialog
     */
    public void open() {
        this.dialog.open();
    }
    
    /**
     * Create a tab
     * @param window
     */
    private void createTabInternals(PreferencesDialog window) {
        
        window.addCategory(Resources.getMessage("PropertyDialog.16"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-internals.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.2")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.17"), 0, 1000000, 200) { //$NON-NLS-1$
            protected Integer getValue() { return model.getHistorySize(); }
            protected void setValue(Object t) { model.setHistorySize((Integer)t); }});
        
        window.addPreference(new PreferenceDouble(Resources.getMessage("PropertyDialog.19"), 0d, 1d, 0.2d) { //$NON-NLS-1$
            protected Double getValue() { return model.getSnapshotSizeDataset(); }
            protected void setValue(Object t) { model.setSnapshotSizeDataset((Double)t); }});
        
        window.addPreference(new PreferenceDouble(Resources.getMessage("PropertyDialog.21"), 0d, 1d, 0.8d) { //$NON-NLS-1$
            protected Double getValue() { return model.getSnapshotSizeSnapshot(); }
            protected void setValue(Object t) { model.setSnapshotSizeSnapshot((Double)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.28"), 0, Integer.MAX_VALUE, 5000000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getMaximalSizeForComplexOperations(); }
            protected void setValue(Object t) { model.setMaximalSizeForComplexOperations((Integer)t); }});
        
        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.29"), false) { //$NON-NLS-1$
            protected Boolean getValue() { return model.isDebugEnabled(); }
            protected void setValue(Object t) { model.setDebugEnabled((Boolean)t); }});

        window.addGroup(Resources.getMessage("DialogProperties.3")); //$NON-NLS-1$

        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.23"), 0, 10000, 100) { //$NON-NLS-1$
            protected Integer getValue() { return model.getInitialNodesInViewer(); }
            protected void setValue(Object t) { model.setInitialNodesInViewer((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.25"), 0, 10000, 700) { //$NON-NLS-1$
            protected Integer getValue() { return model.getMaxNodesInViewer(); }
            protected void setValue(Object t) { model.setMaxNodesInViewer((Integer)t); }});
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabProject(PreferencesDialog window) {
        
        window.addCategory(Resources.getMessage("PropertyDialog.3"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-project.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.5")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceString(Resources.getMessage("PropertyDialog.4")) { //$NON-NLS-1$
            protected String getValue() { return model.getName(); }
            protected void setValue(Object t) { model.setName((String)t); }});
        
        window.addPreference(new PreferenceText(Resources.getMessage("PropertyDialog.7")) { //$NON-NLS-1$
            protected String getValue() { return model.getDescription(); }
            protected void setValue(Object t) { model.setDescription((String)t); }});

        window.addPreference(new PreferenceSelection(Resources.getMessage("PropertyDialog.33"), getLocales()) { //$NON-NLS-1$
            protected String getValue() { return model.getLocale().getLanguage().toUpperCase(); }
            protected void setValue(Object t) { model.setLocale(((String)t).equals("Default") ? Locale.getDefault() : new Locale(((String)t).toLowerCase())); }}); //$NON-NLS-1$

        window.addGroup(Resources.getMessage("DialogProperties.6")); //$NON-NLS-1$

        window.addPreference(new PreferenceCharacter(Resources.getMessage("PropertyDialog.35"), ';') { //$NON-NLS-1$
            protected String getValue() { return String.valueOf(model.getCSVSyntax().getDelimiter()); }
            protected void setValue(Object t) { model.getCSVSyntax().setDelimiter(((String)t).charAt(0)); }});

        window.addPreference(new PreferenceCharacter(Resources.getMessage("PropertyDialog.36"), '"') { //$NON-NLS-1$
            protected String getValue() { return String.valueOf(model.getCSVSyntax().getQuote()); }
            protected void setValue(Object t) { model.getCSVSyntax().setQuote(((String)t).charAt(0)); }});

        window.addPreference(new PreferenceCharacter(Resources.getMessage("PropertyDialog.37"), '"') { //$NON-NLS-1$
            protected String getValue() { return String.valueOf(model.getCSVSyntax().getEscape()); }
            protected void setValue(Object t) { model.getCSVSyntax().setEscape(((String)t).charAt(0)); }});  

        window.addPreference(new PreferenceSelection(Resources.getMessage("PropertyDialog.38"), CSVSyntax.getAvailableLinebreaks()) { //$NON-NLS-1$
            protected String getValue() { return CSVSyntax.getLabelForLinebreak(model.getCSVSyntax().getLinebreak()); }
            protected void setValue(Object t) { model.getCSVSyntax().setLinebreak(CSVSyntax.getLinebreakForLabel((String)t)); }}); //$NON-NLS-1$

    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabRisk(PreferencesDialog window) {

        window.addCategory(Resources.getMessage("PropertyDialog.40"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-risk.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.7")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.43"), 1, 10, 10) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getMaxQiSize(); }
            protected void setValue(Object t) { model.getRiskModel().setMaxQiSize((Integer)t); }});
        
        window.addPreference(new PreferenceSelection(Resources.getMessage("PropertyDialog.45"), getRiskModelsForAnalyses()) { //$NON-NLS-1$
            protected String getValue() { return model.getRiskModel().getRiskModelForAttributes().name(); }
            protected void setValue(Object arg0) { model.getRiskModel().setRiskModelForAttributes(RiskModelForAttributes.valueOf((String)arg0)); }
        });

        window.addGroup(Resources.getMessage("DialogProperties.8")); //$NON-NLS-1$

        window.addPreference(new PreferenceDouble(Resources.getMessage("PropertyDialog.50"), 1.0e-12, 1d, 1.0e-6) { //$NON-NLS-1$
            protected Double getValue() { return model.getRiskModel().getSolverConfiguration().getAccuracy(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().accuracy((Double)t); }});

        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.51"), 1, 100000, 1000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getIterationsPerTry(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().iterationsPerTry((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.52"), 1, 1000000, 10000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getIterationsTotal(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().iterationsTotal((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.53"), 1, 100000, 100) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getTimePerTry(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().timePerTry((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.54"), 1, 1000000, 1000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getTimeTotal(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().timeTotal((Integer)t); }});

        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.56"), false) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getRiskModel().getSolverConfiguration().getStartValues() != null; }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().preparedStartValues((Boolean)t ? getSolverStartValues() : null); }});
        
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabSearch(PreferencesDialog window) {
        window.addCategory(Resources.getMessage("PropertyDialog.130"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-search.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.9")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.131"), 0, Integer.MAX_VALUE, 100000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getInputConfig().getHeuristicSearchThreshold(); }
            protected void setValue(Object t) { model.getInputConfig().setHeuristicSearchThreshold((Integer)t); }});

        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.132"), false) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getInputConfig().isHeuristicSearchEnabled(); }
            protected void setValue(Object t) { model.getInputConfig().setHeuristicSearchEnabled((Boolean)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.133"), 0, Integer.MAX_VALUE, 30000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getInputConfig().getHeuristicSearchTimeLimit(); }
            protected void setValue(Object t) { model.getInputConfig().setHeuristicSearchTimeLimit((Integer)t); }});

        window.addGroup(Resources.getMessage("DialogProperties.10")); //$NON-NLS-1$

        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.44")) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getInputConfig().isHeuristicForSampleBasedCriteria(); }
            protected void setValue(Object t) { model.getInputConfig().setHeuristicForSampleBasedCriteria((Boolean)t); }});
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabTransformation(PreferencesDialog window) {
       
        window.addCategory(Resources.getMessage("PropertyDialog.10"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-transformation.png")); //$NON-NLS-1$

        window.addGroup(Resources.getMessage("DialogProperties.11")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.11"), true) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getInputConfig().isSuppressionAlwaysEnabled(); }
            protected void setValue(Object t) { model.getInputConfig().setSuppressionAlwaysEnabled((Boolean)t); }});
        
        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.31"), false) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getInputConfig().isAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE); }
            protected void setValue(Object t) { model.getInputConfig().setAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE, (Boolean)t); }});
        
        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.32"), false) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getInputConfig().isAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE); }
            protected void setValue(Object t) { model.getInputConfig().setAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE, (Boolean)t); }});
        
        window.addPreference(new PreferenceString(Resources.getMessage("PropertyDialog.13"), "*") { //$NON-NLS-1$ //$NON-NLS-2$
            protected String getValue() { return model.getInputConfig().getSuppressionString(); }
            protected void setValue(Object t) { model.getInputConfig().setSuppressionString((String)t); }});
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabUtility(PreferencesDialog window) {

        window.addCategory(Resources.getMessage("PropertyDialog.60"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-utility.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.12")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.61")) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getUseListwiseDeletion(); }
            protected void setValue(Object t) { model.setUseListwiseDeletion((Boolean)t); }});

        window.addGroup(Resources.getMessage("DialogProperties.13")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.62")) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getUseFunctionalHierarchies(); }
            protected void setValue(Object t) { model.setUseFunctionalHierarchies((Boolean)t); }});
    }
    
    /**
     * Returns a list of available locales
     * @return
     */
    private String[] getLocales() {
        List<String> languages = new ArrayList<String>();
        languages.add(Resources.getMessage("DialogProperties.4")); //$NON-NLS-1$
        for (String lang : Locale.getISOLanguages()) {
            languages.add(lang.toUpperCase());
        }
        return languages.toArray(new String[]{});
    }

    /**
     * Creates a list of models
     * @return
     */
    private String[] getRiskModelsForAnalyses() {
        List<String> result = new ArrayList<String>();
        for (RiskModelForAttributes model : RiskModelForAttributes.values()) {
            result.add(model.name());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns start values for the solver, if it is configured to be deterministic
     * @return
     */
    private double[][] getSolverStartValues() {
        double[][] result = new double[16][];
        int index = 0;
        for (double d1 = 0d; d1 < 1d; d1 += 0.33d) {
            for (double d2 = 0d; d2 < 1d; d2 += 0.33d) {
                result[index++] = new double[] { d1, d2 };
            }
        }
        return result;
    }
}
