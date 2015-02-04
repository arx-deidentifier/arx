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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.gui.view.impl.menu.properties.PWCharText;
import org.deidentifier.arx.gui.view.impl.menu.properties.PWRestrictedFloatText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.preferenceWindow.PWTab;
import org.mihalis.opal.preferenceWindow.PreferenceWindow;
import org.mihalis.opal.preferenceWindow.widgets.PWCheckbox;
import org.mihalis.opal.preferenceWindow.widgets.PWCombo;
import org.mihalis.opal.preferenceWindow.widgets.PWSpinner;
import org.mihalis.opal.preferenceWindow.widgets.PWStringText;
import org.mihalis.opal.preferenceWindow.widgets.PWTextarea;
import org.mihalis.opal.preferenceWindow.widgets.PWWidget;

/**
 * This class implements a dialog for editing project properties.
 *
 * @author Fabian Prasser
 */
public class DialogProperties implements IDialog {

    /** Model */
    private final Model               model;

    /** Controller */
    private final Controller          controller;

    /** Window */
    private final PreferenceWindow    window;

    /** Properties */
    private final Map<String, Object> properties;

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
        
        // Create
        this.properties = getProperties(model);
        this.window = PreferenceWindow.create(parent, properties);
        createTabProject(window);
        createTabTransformation(window);
        createTabInternals(window);
        createTabVisualization(window);
    }

    /**
     * Opens the dialog
     */
    public void open() {
        if (window.open()) {
            setProperties(window.getValues(), model);
        }
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabInternals(PreferenceWindow window) {
        
        PWTab tab = window.addTab(controller.getResources().getImage("settings.png"), //$NON-NLS-1$
                                  Resources.getMessage("PropertyDialog.16")); //$NON-NLS-1$
        
        tab.add(format(new PWSpinner(Resources.getMessage("PropertyDialog.17"), "historySize", 0, 1000000))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWRestrictedFloatText(Resources.getMessage("PropertyDialog.19"), "snapshotSizeDataset", 0d, 1d))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWRestrictedFloatText(Resources.getMessage("PropertyDialog.21"), "snapshotSizeSnapshot", 0d, 1d))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWSpinner(Resources.getMessage("PropertyDialog.28"), "maximalSizeForComplexOperations", 0, Integer.MAX_VALUE))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWCheckbox(Resources.getMessage("PropertyDialog.29"), "debugEnabled"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Formats a widget
     * @param widget
     * @return
     */
    private PWWidget format(PWWidget widget) {
        widget.setGrabExcessSpace(true);
        widget.setAlignment(GridData.BEGINNING);
        widget.setWidth(300);
        return widget;
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabProject(PreferenceWindow window) {
        PWTab tab = window.addTab(controller.getResources().getImage("settings.png"), //$NON-NLS-1$
                                  Resources.getMessage("PropertyDialog.3")); //$NON-NLS-1$
        
        tab.add(format(new PWStringText(Resources.getMessage("PropertyDialog.4"), "name"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWTextarea(Resources.getMessage("PropertyDialog.7"), "description"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWCharText(Resources.getMessage("PropertyDialog.9"), "separator"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWCombo(Resources.getMessage("PropertyDialog.33"), "locale", getLocales()))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabTransformation(PreferenceWindow window) {
       
        PWTab tab = window.addTab(controller.getResources().getImage("settings.png"), //$NON-NLS-1$
                                  Resources.getMessage("PropertyDialog.10")); //$NON-NLS-1$
        
        tab.add(format(new PWCheckbox(Resources.getMessage("PropertyDialog.11"), "suppressionAlwaysEnabled"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWCheckbox(Resources.getMessage("PropertyDialog.31"), "isSensitiveAttributesSuppressed"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWCheckbox(Resources.getMessage("PropertyDialog.32"), "isInsensitiveAttributesSuppressed"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWStringText(Resources.getMessage("PropertyDialog.13"), "suppressionString"))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWSpinner(Resources.getMessage("PropertyDialog.15"), "maxNodesInLattice", 0, 1000000))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Create a tab
     * @param window
     */
    private void createTabVisualization(PreferenceWindow window) {
        PWTab tab = window.addTab(controller.getResources().getImage("settings.png"), //$NON-NLS-1$
                                  Resources.getMessage("PropertyDialog.22")); //$NON-NLS-1$
        
        tab.add(format(new PWSpinner(Resources.getMessage("PropertyDialog.23"), "initialNodesInViewer", 0, 10000))); //$NON-NLS-1$ //$NON-NLS-2$
        tab.add(format(new PWSpinner(Resources.getMessage("PropertyDialog.25"), "maxNodesInViewer", 0, 10000))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns a list of available locales
     * @return
     */
    private Object[] getLocales() {
        List<String> languages = new ArrayList<String>();
        languages.add("Default");
        for (String lang : Locale.getISOLanguages()) {
            languages.add(lang.toUpperCase());
        }
        return languages.toArray();
    }

    /**
     * Returns a map for the given model
     * @param model
     * @return
     */
    private Map<String, Object> getProperties(Model model) {

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("initialNodesInViewer", model.getInitialNodesInViewer()); //$NON-NLS-1$
        data.put("maxNodesInViewer", model.getMaxNodesInViewer()); //$NON-NLS-1$
        data.put("historySize", model.getHistorySize()); //$NON-NLS-1$
        data.put("snapshotSizeDataset", (float)model.getSnapshotSizeDataset()); //$NON-NLS-1$
        data.put("snapshotSizeSnapshot", (float)model.getSnapshotSizeSnapshot()); //$NON-NLS-1$
        data.put("maximalSizeForComplexOperations", model.getMaximalSizeForComplexOperations()); //$NON-NLS-1$
        data.put("debugEnabled", model.isDebugEnabled()); //$NON-NLS-1$
        data.put("suppressionAlwaysEnabled", model.getInputConfig().isSuppressionAlwaysEnabled()); //$NON-NLS-1$
        data.put("isSensitiveAttributesSuppressed", model.getInputConfig().isAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE)); //$NON-NLS-1$
        data.put("isInsensitiveAttributesSuppressed", model.getInputConfig().isAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE)); //$NON-NLS-1$
        data.put("suppressionString", model.getInputConfig().getSuppressionString()); //$NON-NLS-1$
        data.put("maxNodesInLattice", model.getMaxNodesInLattice()); //$NON-NLS-1$
        data.put("name", model.getName()); //$NON-NLS-1$
        data.put("description", model.getDescription()); //$NON-NLS-1$
        data.put("separator", String.valueOf(model.getSeparator())); //$NON-NLS-1$
        data.put("locale", model.getLocale().getLanguage().toUpperCase()); //$NON-NLS-1$
        return data;
    }
    /**
     * Writes the map to the given model
     * @param data
     * @param model
     */
    private void setProperties(Map<String, Object> data, Model model) {

        int initialNodesInViewer = (Integer)data.get("initialNodesInViewer");
        if (initialNodesInViewer != model.getInitialNodesInViewer()) {
            model.setInitialNodesInViewer(initialNodesInViewer);
        }
        
        int maxNodesInViewer = (Integer)data.get("maxNodesInViewer");
        if (maxNodesInViewer != model.getMaxNodesInViewer()) {
            model.setMaxNodesInViewer(maxNodesInViewer);
        }
        
        int historySize = (Integer)data.get("historySize");
        if (historySize != model.getHistorySize()) {
            model.setHistorySize(historySize);
        }
        
        float snapshotSizeDataset = (Float)data.get("snapshotSizeDataset");
        if (snapshotSizeDataset != model.getSnapshotSizeDataset()) {
            model.setSnapshotSizeDataset(snapshotSizeDataset);
        }

        float snapshotSizeSnapshot = (Float)data.get("snapshotSizeSnapshot");
        if (snapshotSizeSnapshot != model.getSnapshotSizeSnapshot()) {
            model.setSnapshotSizeSnapshot(snapshotSizeSnapshot);
        }

        int maximalSizeForComplexOperations = (Integer)data.get("maximalSizeForComplexOperations");
        if (maximalSizeForComplexOperations != model.getMaximalSizeForComplexOperations()) {
            model.setMaximalSizeForComplexOperations(maximalSizeForComplexOperations);
        }
        
        boolean debugEnabled = (Boolean)data.get("debugEnabled");
        if (debugEnabled != model.isDebugEnabled()) {
            model.setDebugEnabled(debugEnabled);
        }
        
        boolean suppressionAlwaysEnabled = (Boolean)data.get("suppressionAlwaysEnabled");
        if (suppressionAlwaysEnabled != model.getInputConfig().isSuppressionAlwaysEnabled()) {
            model.getInputConfig().setSuppressionAlwaysEnabled(suppressionAlwaysEnabled);
        }
        
        boolean isSensitiveAttributesSuppressed = (Boolean)data.get("isSensitiveAttributesSuppressed");
        if (isSensitiveAttributesSuppressed != model.getInputConfig().isAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE)) {
            model.getInputConfig().setAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE, isSensitiveAttributesSuppressed);
        }
        
        boolean isInsensitiveAttributesSuppressed = (Boolean)data.get("isInsensitiveAttributesSuppressed");
        if (isInsensitiveAttributesSuppressed != model.getInputConfig().isAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE)) {
            model.getInputConfig().setAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE, isInsensitiveAttributesSuppressed);
        }
        
        String suppressionString = (String)data.get("suppressionString");
        if (!suppressionString.equals(model.getInputConfig().getSuppressionString())) {
            model.getInputConfig().setSuppressionString(suppressionString);
        }

        String name = (String)data.get("name");
        if (!name.equals(model.getName())) {
            model.setName(name);
        }

        String description = (String)data.get("description");
        if (!description.equals(model.getDescription())) {
            model.setDescription(description);
        }
        
        char separator = ((String)data.get("separator")).toCharArray()[0];
        if (separator != model.getSeparator()) {
            model.setSeparator(separator);
        }

        int maxNodesInLattice = (Integer)data.get("maxNodesInLattice");
        if (maxNodesInLattice != model.getMaxNodesInLattice()) {
            model.setMaxNodesInLattice(maxNodesInLattice);
        }
        

        String slocale = (String)data.get("locale");
        Locale locale;
        if (slocale.equals("Default")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(slocale.toLowerCase());
        }
        if (!locale.equals(model.getLocale())) {
            model.setLocale(locale);
        }
    }
}
