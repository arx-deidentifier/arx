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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXSolverConfiguration;
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
        this.createTabSolver(this.dialog);
        this.createTabKeys(this.dialog);
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
    private void createTabKeys(PreferencesDialog window) {

        window.addCategory(Resources.getMessage("PropertyDialog.40"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("perspective_attributes.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.7")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.43"), 1, 10, 10) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getMaxQiSize(); }
            protected void setValue(Object t) { model.getRiskModel().setMaxQiSize((Integer)t); }});
        
        window.addPreference(new PreferenceSelection(Resources.getMessage("PropertyDialog.45"), getRiskModelsForAnalyses()) { //$NON-NLS-1$
            protected String getValue() { return model.getRiskModel().getRiskModelForAttributes().name(); }
            protected void setValue(Object arg0) { model.getRiskModel().setRiskModelForAttributes(RiskModelForAttributes.valueOf((String)arg0)); }
        });

        window.addGroup(Resources.getMessage("DialogProperties.21")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceSelection(Resources.getMessage("DialogProperties.22"), getScoreModels()) { //$NON-NLS-1$
            protected String getValue() { return getScoreModel(model.getRiskModel().isSdcMicroScores()); }
            protected void setValue(Object arg0) { model.getRiskModel().setSdcMicroScores(getBooleanForScoreModel((String)arg0)); }
        });

        window.addPreference(new PreferenceInteger(Resources.getMessage("DialogProperties.25"), 0, 100000, 0) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getMaxKeySize(); }
            protected void setValue(Object t) { model.getRiskModel().setMaxKeySize((Integer)t); }});
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
    private void createTabSearch(PreferencesDialog window) {
        window.addCategory(Resources.getMessage("PropertyDialog.130"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-search.png")); //$NON-NLS-1$
        
        window.addGroup(Resources.getMessage("DialogProperties.9")); //$NON-NLS-1$
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.131"), 0, Integer.MAX_VALUE, 100000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getHeuristicSearchThreshold(); }
            protected void setValue(Object t) { model.setHeuristicSearchThreshold((Integer)t); }});

        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.133"), 0, Integer.MAX_VALUE, 30000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getHeuristicSearchTimeLimit(); }
            protected void setValue(Object t) { model.setHeuristicSearchTimeLimit((Integer)t); }});

        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.134"), 0, Integer.MAX_VALUE, 1000) { //$NON-NLS-1$
            protected Integer getValue() { return model.getHeuristicSearchStepLimit(); }
            protected void setValue(Object t) { model.setHeuristicSearchStepLimit((Integer)t); }});

        window.addGroup(Resources.getMessage("DialogProperties.10")); //$NON-NLS-1$

        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.44")) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getInputConfig().isHeuristicForSampleBasedCriteria(); }
            protected void setValue(Object t) { model.getInputConfig().setHeuristicForSampleBasedCriteria((Boolean)t); }});
    }
    /**
     * Create a tab
     * @param window
     */
    private void createTabSolver(PreferencesDialog window) {

        window.addCategory(Resources.getMessage("DialogProperties.8"), //$NON-NLS-1$
                           controller.getResources().getManagedImage("settings-risk.png")); //$NON-NLS-1$

        window.addGroup(Resources.getMessage("DialogProperties.8")); //$NON-NLS-1$

        window.addPreference(new PreferenceDouble(Resources.getMessage("PropertyDialog.50"), 1.0e-12, 1d, ARXSolverConfiguration.getDefaultAccuracy()) { //$NON-NLS-1$
            protected Double getValue() { return model.getRiskModel().getSolverConfiguration().getAccuracy(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().accuracy((Double)t); }});

        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.51"), 1, 100000, ARXSolverConfiguration.getDefaultIterationsPerTry()) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getIterationsPerTry(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().iterationsPerTry((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.52"), 1, 1000000, ARXSolverConfiguration.getDefaultIterationsTotal()) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getIterationsTotal(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().iterationsTotal((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.53"), 1, 100000, ARXSolverConfiguration.getDefaultTimePerTry()) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getTimePerTry(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().timePerTry((Integer)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("PropertyDialog.54"), 1, 1000000, ARXSolverConfiguration.getDefaultTimeTotal()) { //$NON-NLS-1$
            protected Integer getValue() { return model.getRiskModel().getSolverConfiguration().getTimeTotal(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().timeTotal((Integer)t); }});

        window.addPreference(new PreferenceBoolean(Resources.getMessage("PropertyDialog.56"), ARXSolverConfiguration.getDefaultDeterministic()) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getRiskModel().getSolverConfiguration().isDeterministic(); }
            protected void setValue(Object t) { model.getRiskModel().getSolverConfiguration().setDeterministic((Boolean)t); }});
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

        window.addGroup(Resources.getMessage("DialogProperties.14")); //$NON-NLS-1$

        window.addPreference(new PreferenceInteger(Resources.getMessage("DialogProperties.15"), 1000, Integer.MAX_VALUE, ARXClassificationConfiguration.DEFAULT_MAX_RECORDS) { //$NON-NLS-1$
            protected Integer getValue() { return model.getClassificationModel().getCurrentConfiguration().getMaxRecords(); }
            protected void setValue(Object t) { model.getClassificationModel().setMaxRecords((Integer)t); }});

        window.addPreference(new PreferenceBoolean(Resources.getMessage("DialogProperties.17"), ARXClassificationConfiguration.DEFAULT_DETERMINISTIC) { //$NON-NLS-1$
            protected Boolean getValue() { return model.getClassificationModel().getCurrentConfiguration().isDeterministic(); }
            protected void setValue(Object t) { model.getClassificationModel().setDeterministic((Boolean)t); }});
        
        window.addPreference(new PreferenceInteger(Resources.getMessage("DialogProperties.18"), 2, 100, ARXClassificationConfiguration.DEFAULT_NUMBER_OF_FOLDS) { //$NON-NLS-1$
            protected Integer getValue() { return model.getClassificationModel().getCurrentConfiguration().getNumFolds(); }
            protected void setValue(Object t) { model.getClassificationModel().setNumFolds((Integer)t); }});

        window.addPreference(new PreferenceInteger(Resources.getMessage("DialogProperties.19"), 10, Integer.MAX_VALUE, ARXClassificationConfiguration.DEFAULT_VECTOR_LENGTH) { //$NON-NLS-1$
            protected Integer getValue() { return model.getClassificationModel().getCurrentConfiguration().getVectorLength(); }
            protected void setValue(Object t) { model.getClassificationModel().setVectorLength((Integer)t); }});
    }

    /**
     * Reads the strings obtained from getScoreModels()
     * @param arg0
     * @return
     */
    private Boolean getBooleanForScoreModel(String arg0) {
        return arg0.equals(getScoreModels()[0]);
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
     * Returns the strings obtained from getScoreModels()
     * @param sdcMicroScores
     * @return
     */
    private String getScoreModel(boolean sdcMicroScores) {
        if (sdcMicroScores) {
            return getScoreModels()[0];
        } else {
            return getScoreModels()[1];
        }
    }

    /**
     * 0: sdcMicro
     * 1: Elliot
     * @return
     */
    private String[] getScoreModels() {
        return new String[] {Resources.getMessage("DialogProperties.23"),
                             Resources.getMessage("DialogProperties.24")};
    }
}
