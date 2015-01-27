/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
import org.deidentifier.arx.gui.model.ModelViewConfig;
import org.deidentifier.arx.gui.model.ModelViewConfig.Mode;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.MainWindow;
import org.deidentifier.arx.gui.view.impl.menu.DialogProject;
import org.deidentifier.arx.gui.view.impl.menu.DialogProperties;
import org.deidentifier.arx.gui.view.impl.menu.DialogQueryResult;
import org.deidentifier.arx.gui.view.impl.menu.DialogSeparator;
import org.deidentifier.arx.gui.view.impl.wizard.ARXWizard;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizard.HierarchyWizardResult;
import org.deidentifier.arx.gui.view.impl.wizard.ImportWizard;
import org.deidentifier.arx.gui.worker.Worker;
import org.deidentifier.arx.gui.worker.WorkerAnonymize;
import org.deidentifier.arx.gui.worker.WorkerExport;
import org.deidentifier.arx.gui.worker.WorkerImport;
import org.deidentifier.arx.gui.worker.WorkerLoad;
import org.deidentifier.arx.gui.worker.WorkerSave;
import org.deidentifier.arx.gui.worker.WorkerTransform;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.ImportConfiguration;
import org.deidentifier.arx.io.ImportConfigurationCSV;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import cern.colt.Swapper;

/**
 * The main controller for the whole tool.
 *
 * @author Fabian Prasser
 */
public class Controller implements IView {

    /** The debug data. */
    private final DebugData                  debug     = new DebugData();
    
    /** Listeners registered by the views. */
    private final Map<ModelPart, Set<IView>> listeners = Collections.synchronizedMap(new HashMap<ModelPart, Set<IView>>());
    
    /** The main window. */
    private final MainWindow                 main;
    
    /** The model. */
    private Model                            model;
    
    /** The resources. */
    private final Resources                  resources;

    /**
     * Creates a new controller.
     *
     * @param main
     */
    public Controller(final MainWindow main) {
        this.main = main;
        this.resources = new Resources(main.getShell());
    }

    /**
     * Applies the selected transformation.
     */
    public void actionApplySelectedTransformation() {

        // Run the worker
        final WorkerTransform worker = new WorkerTransform(model);
        main.showProgressDialog(Resources.getMessage("Controller.0"), worker); //$NON-NLS-1$

        // Show errors
        if (worker.getError() != null) {
            main.showErrorDialog(main.getShell(),
                                 Resources.getMessage("Controller.2"), //$NON-NLS-1$
                                 worker.getError());
            return;
        }

        // Distribute results
        if (worker.getResult() != null) {
            this.model.setOutput(worker.getResult(), model.getSelectedNode());
            this.update(new ModelEvent(this, ModelPart.OUTPUT, worker.getResult()));

            // Do not sort if dataset is too large
            if (model.getMaximalSizeForComplexOperations() == 0 ||
                model.getInputConfig().getInput().getHandle().getNumRows() <=
                model.getMaximalSizeForComplexOperations()) {
                this.model.getViewConfig().setMode(Mode.GROUPED);
                this.updateViewConfig(true);
            } else {
                this.model.getViewConfig().setMode(Mode.UNSORTED);
            }
            this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
        }
    }

    /**
     * Clears the event log.
     */
    public void actionClearEventLog() {
        this.debug.clearEventLog();
    }

    /**
     * Enables and disables a criterion.
     *
     * @param criterion
     */
    public void actionCriterionEnable(ModelCriterion criterion) {
        if (criterion.isEnabled()) {
            criterion.setEnabled(false);
        } else {
            criterion.setEnabled(true);
        }
        update(new ModelEvent(this, ModelPart.CRITERION_DEFINITION, criterion));
    }

    /**
     * Pull settings into the criterion.
     *
     * @param criterion
     */
    public void actionCriterionPull(ModelCriterion criterion) {

        // Collect all other criteria
        List<ModelExplicitCriterion> others = new ArrayList<ModelExplicitCriterion>();
        if (criterion instanceof ModelLDiversityCriterion) {
            for (ModelLDiversityCriterion other : model.getLDiversityModel().values()) {
                if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                    others.add((ModelExplicitCriterion) other);
                }
            }
        } else if (criterion instanceof ModelTClosenessCriterion) {
            for (ModelTClosenessCriterion other : model.getTClosenessModel().values()) {
                if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                    others.add((ModelExplicitCriterion) other);
                }
            }
        } else {
            throw new RuntimeException("Invalid type of criterion");
        }

        // Break if none found
        if (others.isEmpty()) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.95"), //$NON-NLS-1$
                                Resources.getMessage("Controller.96")); //$NON-NLS-1$ 
            return;
        }

        // Select criterion
        ModelExplicitCriterion element = main.showSelectCriterionDialog(others);

        if (element == null) {
            return;
        } else {
            ((ModelExplicitCriterion) criterion).pull(element);
            update(new ModelEvent(this, ModelPart.CRITERION_DEFINITION, criterion));
        }
    }

    /**
     * Pushes the settings of the criterion.
     *
     * @param criterion
     */
    public void actionCriterionPush(ModelCriterion criterion) {
        if (main.showQuestionDialog(main.getShell(),
                                    Resources.getMessage("Controller.93"), //$NON-NLS-1$
                                    Resources.getMessage("Controller.94"))) { //$NON-NLS-1$

            if (criterion instanceof ModelLDiversityCriterion) {
                for (ModelLDiversityCriterion other : model.getLDiversityModel().values()) {
                    if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                        other.pull((ModelExplicitCriterion) criterion);
                    }
                }
            } else if (criterion instanceof ModelTClosenessCriterion) {
                for (ModelTClosenessCriterion other : model.getTClosenessModel().values()) {
                    if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                        other.pull((ModelExplicitCriterion) criterion);
                    }
                }
            } else {
                throw new RuntimeException("Invalid type of criterion");
            }

            update(new ModelEvent(this, ModelPart.CRITERION_DEFINITION, criterion));
        }
    }

    /**
     * Toggles the "show groups" option.
     */
    public void actionDataShowGroups() {

        // Break if no output
        if (model.getOutput() == null) return;

        this.model.getViewConfig().setMode(Mode.GROUPED);
        this.updateViewConfig(false);

        // Update
        this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
    }

    /**
     * Sorts the data.
     *
     * @param input
     */
    public void actionDataSort(boolean input) {

        // Break if no attribute selected
        if (model.getSelectedAttribute() == null) return;

        if (input) {
            this.model.getViewConfig().setMode(Mode.SORTED_INPUT);
        } else {
            this.model.getViewConfig().setMode(Mode.SORTED_OUTPUT);
        }

        this.model.getViewConfig().setAttribute(model.getSelectedAttribute());
        this.updateViewConfig(false);

        // Update
        this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
    }

    /**
     * Toggles the "subset" option.
     */
    public void actionDataToggleSubset() {

        // Break if no output
        if (model.getOutput() == null) return;

        // Update
        boolean val = !model.getViewConfig().isSubset();
        this.model.getViewConfig().setSubset(val);
        this.updateViewConfig(false);
        this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
    }

    /**
     * Starts the anonymization.
     */
    public void actionMenuEditAnonymize() {

        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.3"), //$NON-NLS-1$
                                Resources.getMessage("Controller.4")); //$NON-NLS-1$
            return;
        }

        if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.5"), //$NON-NLS-1$
                                Resources.getMessage("Controller.6")); //$NON-NLS-1$
            return;
        }

        if (model.getInputConfig().getResearchSubset().size() == 0) {
            final String message = Resources.getMessage("Controller.100"); //$NON-NLS-1$
            main.showInfoDialog(main.getShell(), Resources.getMessage("Controller.11"), message); //$NON-NLS-1$
            return;
        }

        actionResetOutput();

        // Run the worker
        final WorkerAnonymize worker = new WorkerAnonymize(model);
        main.showProgressDialog(Resources.getMessage("Controller.12"), worker); //$NON-NLS-1$

        // Show errors
        if (worker.getError() != null) {
            Throwable t = worker.getError();
            if (worker.getError() instanceof InvocationTargetException) {
                t = worker.getError().getCause();
            }
            if (t instanceof NullPointerException) {
                main.showErrorDialog(main.getShell(), "Internal error", t);
            } else {
                main.showInfoDialog(main.getShell(),
                                    Resources.getMessage("Controller.13"), //$NON-NLS-1$
                                    Resources.getMessage("Controller.14") + t.getMessage()); //$NON-NLS-1$
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            worker.getError().printStackTrace(pw);
            getResources().getLogger().info(sw.toString());
            return;
        }

        // Distribute results
        if (worker.getResult() != null) {

            // Retrieve optimal result
            final ARXResult result = worker.getResult();
            model.createClonedConfig();
            model.setResult(result);
            model.getClipboard().clearClipboard();

            // Update view
            update(new ModelEvent(this, ModelPart.RESULT, result));
            update(new ModelEvent(this, ModelPart.CLIPBOARD, null));
            if (result.isResultAvailable()) {
                model.setOutput(result.getOutput(false), result.getGlobalOptimum());
                model.setSelectedNode(result.getGlobalOptimum());
                update(new ModelEvent(this,
                                      ModelPart.OUTPUT,
                                      result.getOutput(false)));
                update(new ModelEvent(this,
                                      ModelPart.SELECTED_NODE,
                                      result.getGlobalOptimum()));

                // Do not sort if dataset is too large
                if (model.getMaximalSizeForComplexOperations() == 0 ||
                    model.getInputConfig().getInput().getHandle().getNumRows() <=
                    model.getMaximalSizeForComplexOperations()) {
                    this.model.getViewConfig().setSubset(true);
                    this.model.getViewConfig().setMode(Mode.GROUPED);
                    this.updateViewConfig(true);
                } else {
                    this.model.getViewConfig().setSubset(true);
                    this.model.getViewConfig().setMode(Mode.UNSORTED);
                }
                this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
            } else {
                model.setOutput(null, null);
                model.setSelectedNode(null);
                update(new ModelEvent(this, ModelPart.OUTPUT, null));
                update(new ModelEvent(this, ModelPart.SELECTED_NODE, null));
            }

            // Update selected attribute
            update(new ModelEvent(this,
                                  ModelPart.SELECTED_ATTRIBUTE,
                                  model.getSelectedAttribute()));
        }
    }

    /**
     * Starts the wizard.
     */
    public void actionMenuEditCreateHierarchy() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.16"), //$NON-NLS-1$ 
                                Resources.getMessage("Controller.17")); //$NON-NLS-1$
            return;
        } else if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.18"), //$NON-NLS-1$
                                Resources.getMessage("Controller.19")); //$NON-NLS-1$
            return;
        }

        String attr = model.getSelectedAttribute();

        int index = model.getInputConfig()
                         .getInput()
                         .getHandle()
                         .getColumnIndexOf(attr);

        DataType<?> type = model.getInputDefinition().getDataType(attr);

        String[] data = model.getInputConfig()
                             .getInput()
                             .getHandle()
                             .getStatistics()
                             .getDistinctValues(index);

        HierarchyBuilder<?> builder = model.getInputConfig().getHierarchyBuilder(attr);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ARXWizard<HierarchyWizardResult<?>> wizard = new HierarchyWizard(this, attr, builder, type, model.getLocale(), data);

        if (wizard.open(main.getShell())) {
            HierarchyWizardResult<?> result = wizard.getResult();
            if (result.hierarchy != null) {
                model.getInputConfig().setMaximumGeneralization(attr, null);
                model.getInputConfig().setMinimumGeneralization(attr, null);
                model.getInputConfig().setHierarchy(attr, result.hierarchy);
                model.getInputConfig().setHierarchyBuilder(attr, result.builder);
                update(new ModelEvent(this, ModelPart.HIERARCHY, result.hierarchy));
            }
        }
    }

    /**
     * Starts the "edit settings" dialog.
     */
    public void actionMenuEditSettings() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.22"), //$NON-NLS-1$
                                Resources.getMessage("Controller.23")); //$NON-NLS-1$
            return;
        }
        try {
            final DialogProperties dialog = new DialogProperties(main.getShell(),
                                                                 model);
            dialog.create();
            dialog.open();

            // Update the title
            // TODO: This is not sound
            ((IView) main).update(new ModelEvent(this, ModelPart.MODEL, model));
        } catch (final Exception e) {
            main.showErrorDialog(main.getShell(),
                                 Resources.getMessage("Controller.25"), e); //$NON-NLS-1$
            getResources().getLogger().info(e);
        }
    }

    /**
     * File->exit.
     */
    public void actionMenuFileExit() {
        if (main.showQuestionDialog(main.getShell(),
                                    Resources.getMessage("Controller.26"), //$NON-NLS-1$
                                    Resources.getMessage("Controller.27"))) { //$NON-NLS-1$
            if ((model != null) && model.isModified()) {
                if (main.showQuestionDialog(main.getShell(),
                                            Resources.getMessage("Controller.28"), //$NON-NLS-1$
                                            Resources.getMessage("Controller.29"))) { //$NON-NLS-1$
                    actionMenuFileSave();
                }
            }
            main.getShell().getDisplay().dispose();
            System.exit(0);
        }
    }

    /**
     * File->export data.
     */
    public void actionMenuFileExportData() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.30"), //$NON-NLS-1$
                                Resources.getMessage("Controller.31")); //$NON-NLS-1$
            return;
        } else if (model.getOutput() == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.32"), //$NON-NLS-1$
                                Resources.getMessage("Controller.33")); //$NON-NLS-1$
            return;
        }

        // Check node
        if (model.getOutputNode().getAnonymity() != Anonymity.ANONYMOUS) {
            if (!main.showQuestionDialog(main.getShell(),
                                         Resources.getMessage("Controller.34"), //$NON-NLS-1$
                                         Resources.getMessage("Controller.35"))) //$NON-NLS-1$
            {
                return;
            }
        }

        // Ask for file
        String file = main.showSaveFileDialog(main.getShell(), "*.csv"); //$NON-NLS-1$
        if (file == null) {
            return;
        }
        if (!file.endsWith(".csv")) { //$NON-NLS-1$
            file = file + ".csv"; //$NON-NLS-1$
        }

        // Export
        final WorkerExport worker = new WorkerExport(file,
                                                     model.getSeparator(),
                                                     model.getOutput(),
                                                     model.getOutputConfig().getConfig(),
                                                     model.getInputBytes());

        main.showProgressDialog(Resources.getMessage("Controller.39"), worker); //$NON-NLS-1$

        if (worker.getError() != null) {
            main.showErrorDialog(main.getShell(),
                                 Resources.getMessage("Controller.41"), //$NON-NLS-1$
                                 worker.getError());
            return;
        }
    }

    /**
     * File->Export hierarchy.
     */
    public void actionMenuFileExportHierarchy() {

        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.42"), //$NON-NLS-1$
                                Resources.getMessage("Controller.43")); //$NON-NLS-1$
            return;
        }

        Hierarchy hierarchy = model.getInputConfig().getHierarchy(model.getSelectedAttribute());
        if (hierarchy == null || hierarchy.getHierarchy() == null || hierarchy.getHierarchy().length == 0 ||
            hierarchy.getHierarchy()[0].length == 0) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.91"), //$NON-NLS-1$
                                Resources.getMessage("Controller.92")); //$NON-NLS-1$
            return;
        }

        // Ask for file
        String file = main.showSaveFileDialog(main.getShell(), "*.csv"); //$NON-NLS-1$
        if (file == null) {
            return;
        }
        if (!file.endsWith(".csv")) { //$NON-NLS-1$
            file = file + ".csv"; //$NON-NLS-1$
        }

        // Save
        try {
            final CSVDataOutput out = new CSVDataOutput(file,
                                                        model.getSeparator());
            out.write(hierarchy.getHierarchy());

        } catch (final Exception e) {
            main.showErrorDialog(main.getShell(),
                                 Resources.getMessage("Controller.50"), e); //$NON-NLS-1$
        }
    }

    /**
     * File->Import data.
     */
    public void actionMenuFileImportData() {

        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.51"), //$NON-NLS-1$
                                Resources.getMessage("Controller.52")); //$NON-NLS-1$
            return;
        }

        //Shows an 'Are you sure?' dialog if data has been already imported
        if (model.getInputConfig() != null && model.getInputConfig().getInput() != null &&
            !main.showQuestionDialog(main.getShell(),
                                     Resources.getMessage("Controller.101"), //$NON-NLS-1$
                                     Resources.getMessage("Controller.102"))) { //$NON-NLS-1$
            return;
        }

        ARXWizard<ImportConfiguration> wizard = new ImportWizard(this, model);
        if (wizard.open(main.getShell())) {
            ImportConfiguration config = wizard.getResult();
            if (config != null) {
                actionImportData(config);
            }
        }
    }

    /**
     * File->Import hierarchy.
     */
    public void actionMenuFileImportHierarchy() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.54"), //$NON-NLS-1$
                                Resources.getMessage("Controller.55")); //$NON-NLS-1$
            return;
        } else if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.56"), //$NON-NLS-1$
                                Resources.getMessage("Controller.57")); //$NON-NLS-1$
            return;
        }

        final String path = actionShowOpenFileDialog(main.getShell(), "*.csv"); //$NON-NLS-1$
        if (path != null) {

            // Determine separator
            DialogSeparator dialog = null;

            try {
                dialog = new DialogSeparator(main.getShell(), this, path, false);
                dialog.create();
                if (dialog.open() == Window.CANCEL) {
                    return;
                }
            } catch (Throwable error) {
                if (error instanceof RuntimeException) {
                    if (error.getCause() != null) {
                        error = error.getCause();
                    }
                }
                if ((error instanceof IllegalArgumentException) || (error instanceof IOException)) {
                    main.showInfoDialog(main.getShell(), "Error loading hierarchy", error.getMessage());
                } else {
                    main.showErrorDialog(main.getShell(),
                                         Resources.getMessage("Controller.78"), error); //$NON-NLS-1$
                }
                return;
            }

            // Load hierarchy
            final char separator = dialog.getSeparator();
            final Hierarchy hierarchy = actionImportHierarchy(path, separator);
            if (hierarchy != null) {
                String attr = model.getSelectedAttribute();
                model.getInputConfig().setMaximumGeneralization(attr, null);
                model.getInputConfig().setMinimumGeneralization(attr, null);
                model.getInputConfig().setHierarchy(attr, hierarchy);
                update(new ModelEvent(this, ModelPart.HIERARCHY, hierarchy));
            }
        }
    }

    /**
     * File->New project.
     */
    public void actionMenuFileNew() {

        if ((model != null) && model.isModified()) {
            if (main.showQuestionDialog(main.getShell(),
                                        Resources.getMessage("Controller.61"), //$NON-NLS-1$
                                        Resources.getMessage("Controller.62"))) { //$NON-NLS-1$
                actionMenuFileSave();
            }
        }

        // Separator
        final DialogProject dialog = new DialogProject(main.getShell());
        dialog.create();
        if (dialog.open() != Window.OK) {
            return;
        }

        // Set project
        reset();
        model = dialog.getProject();
        update(new ModelEvent(this, ModelPart.MODEL, model));
    }

    /**
     * File->Open project.
     */
    public void actionMenuFileOpen() {

        if ((model != null) && model.isModified()) {
            if (main.showQuestionDialog(main.getShell(),
                                        Resources.getMessage("Controller.63"), //$NON-NLS-1$
                                        Resources.getMessage("Controller.64"))) { //$NON-NLS-1$
                actionMenuFileSave();
            }
        }

        // Check
        final String path = actionShowOpenFileDialog(main.getShell(), "*.deid"); //$NON-NLS-1$
        if (path == null) {
            return;
        }

        // Set project
        reset();

        actionOpenProject(path);

    }

    /**
     * File->Save project.
     */
    public void actionMenuFileSave() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.66"), //$NON-NLS-1$
                                Resources.getMessage("Controller.67")); //$NON-NLS-1$
            return;
        }
        if (model.getPath() == null) {
            actionMenuFileSaveAs();
        } else {
            actionSaveProject();
        }
    }

    /**
     * File->Save project as.
     */
    public void actionMenuFileSaveAs() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.68"), //$NON-NLS-1$
                                Resources.getMessage("Controller.69")); //$NON-NLS-1$
            return;
        }

        // Check
        String path = actionShowSaveFileDialog(main.getShell(), "*.deid"); //$NON-NLS-1$
        if (path == null) {
            return;
        }

        if (!path.endsWith(".deid")) { //$NON-NLS-1$
            path += ".deid"; //$NON-NLS-1$
        }
        model.setPath(path);
        actionSaveProject();
    }

    /**
     * Shows the "about" dialog.
     */
    public void actionMenuHelpAbout() {
        main.showAboutDialog();
    }

    /**
     * Shows the "debug" dialog.
     */
    public void actionMenuHelpDebug() {
        if (model != null && model.isDebugEnabled()) main.showDebugDialog();
    }

    /**
     * Shows the "help" dialog.
     */
    public void actionMenuHelpHelp() {
        actionShowHelpDialog(null);
    }

    /**
     * Internal method for loading a project.
     *
     * @param path
     */
    public void actionOpenProject(String path) {
        if (!path.endsWith(".deid")) { //$NON-NLS-1$
            path += ".deid"; //$NON-NLS-1$
        }

        WorkerLoad worker = null;
        try {
            worker = new WorkerLoad(path, this);
        } catch (final IOException e) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.82"), e.getMessage()); //$NON-NLS-1$
            return;
        }

        main.showProgressDialog(Resources.getMessage("Controller.83"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            
            String message = worker.getError().getMessage();
            if (message == null || message.equals("")) {
                message = "Error loading project: "+worker.getError().getClass().getSimpleName();
            }
            
            getResources().getLogger().info(worker.getError());
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.85"), //$NON-NLS-1$
                                message);
            return;
        }

        // Reset the workbench
        reset();

        // Obtain the result
        model = worker.getResult();
        model.setPath(path);

        // Temporary store parts of the model, because it might be overwritten
        // when updating the workbench
        final ModelNodeFilter tempNodeFilter = model.getNodeFilter();
        final String tempSelectedAttribute = model.getSelectedAttribute();
        final ARXNode tempSelectedNode = model.getSelectedNode();
        final List<ARXNode> tempClipboard = model.getClipboard().getClipboardEntries();

        // Update the model
        update(new ModelEvent(this, ModelPart.MODEL, model));

        // Update subsets of the model
        if (model.getInputConfig().getInput() != null) {
            update(new ModelEvent(this,
                                  ModelPart.INPUT,
                                  model.getInputConfig().getInput().getHandle()));
        }

        // Update subsets of the model
        if (model.getResult() != null) {
            update(new ModelEvent(this, ModelPart.RESULT, model.getResult()));
        }

        // Update subsets of the model
        if (tempSelectedNode != null) {
            model.setSelectedNode(tempSelectedNode);
            update(new ModelEvent(this, ModelPart.SELECTED_NODE, model.getSelectedNode()));
            final DataHandle handle = model.getResult().getOutput(tempSelectedNode, false);
            model.setOutput(handle, tempSelectedNode);
            update(new ModelEvent(this, ModelPart.OUTPUT, handle));
        }

        // Update subsets of the model
        if (tempNodeFilter != null) {
            model.setNodeFilter(tempNodeFilter);
            update(new ModelEvent(this, ModelPart.FILTER, tempNodeFilter));
        }

        // Update hierarchies and selected attribute
        if (model.getInputConfig() != null &&
            model.getInputConfig().getInput() != null) {
            DataHandle handle = model.getInputConfig().getInput().getHandle();
            if (handle != null) {
                for (int i = 0; i < handle.getNumColumns(); i++) {
                    String attr = handle.getAttributeName(i);
                    Hierarchy hierarchy = model.getInputConfig().getHierarchy(attr);
                    if (hierarchy != null) {
                        model.setSelectedAttribute(attr);
                        update(new ModelEvent(this, ModelPart.HIERARCHY, hierarchy));
                    }
                }
                if (handle.getNumColumns() > 0) {
                    String attribute = handle.getAttributeName(0);
                    model.setSelectedAttribute(attribute);
                    update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, attribute));
                }
            }
        }

        // Update subsets of the model
        if (tempSelectedAttribute != null) {
            model.setSelectedAttribute(tempSelectedAttribute);
            update(new ModelEvent(this,
                                  ModelPart.SELECTED_ATTRIBUTE,
                                  tempSelectedAttribute));
        }

        // Update subsets of the model
        if (tempClipboard != null) {
            model.getClipboard().clearClipboard();
            model.getClipboard().addAllToClipboard(tempClipboard);
            update(new ModelEvent(this,
                                  ModelPart.CLIPBOARD,
                                  model.getClipboard().getClipboardEntries()));
        }

        // Update the attribute types
        if (model.getInputConfig().getInput() != null) {
            final DataHandle handle = model.getInputConfig()
                                           .getInput()
                                           .getHandle();
            for (int i = 0; i < handle.getNumColumns(); i++) {
                update(new ModelEvent(this,
                                      ModelPart.ATTRIBUTE_TYPE,
                                      handle.getAttributeName(i)));
            }
        }

        // Update research subset
        update(new ModelEvent(this,
                              ModelPart.RESEARCH_SUBSET,
                              model.getInputConfig().getResearchSubset()));

        // Update view config
        if (model.getOutput() != null) {
            update(new ModelEvent(this,
                                  ModelPart.VIEW_CONFIG,
                                  model.getOutput()));
        }

        // We just loaded the model, so there are no changes
        model.setUnmodified();
    }

    /**
     * Shows an error dialog.
     *
     * @param shell
     * @param text
     * @param t
     */
    public void actionShowErrorDialog(final Shell shell, final String text, final Throwable t) {
        main.showErrorDialog(shell, text, t);
    }

    /**
     * Shows a dialog for selecting a format string for a data type.
     *
     * @param shell The parent shell
     * @param title The dialog's title
     * @param text The dialog's text
     * @param locale The locale
     * @param type The description of the data type for which to choose a format string
     * @param values The values to check the format string against
     * @return The format string, or <code>null</code> if no format was (or could be) selected
     */
    public String actionShowFormatInputDialog(final Shell shell,
                                              final String title,
                                              final String text,
                                              final Locale locale,
                                              final DataTypeDescription<?> type,
                                              final Collection<String> values) {

        return main.showFormatInputDialog(shell, title, text, null, locale, type, values);
    }

    /**
     * Shows a dialog for selecting a format string for a data type.
     *
     * @param shell The parent shell
     * @param title The dialog's title
     * @param text The dialog's text
     * @param locale The locale
     * @param type The description of the data type for which to choose a format string
     * @param values The values to check the format string against
     * @return The format string, or <code>null</code> if no format was (or could be) selected
     */
    public String actionShowFormatInputDialog(final Shell shell,
                                              final String title,
                                              final String text,
                                              final Locale locale,
                                              final DataTypeDescription<?> type,
                                              final String[] values) {

        return main.showFormatInputDialog(shell, title, text, null, locale, type, Arrays.asList(values));
    }

    /**
     * Shows a dialog for selecting a format string for a data type.
     *
     * @param shell The parent shell
     * @param title The dialog's title
     * @param text The dialog's text
     * @param preselected A preselected format string
     * @param locale The locale
     * @param type The description of the data type for which to choose a format string
     * @param values The values to check the format string against
     * @return The format string, or <code>null</code> if no format was (or could be) selected
     */
    public String actionShowFormatInputDialog(final Shell shell,
                                              final String title,
                                              final String text,
                                              final String preselected,
                                              final Locale locale,
                                              final DataTypeDescription<?> type,
                                              final Collection<String> values) {

        return main.showFormatInputDialog(shell, title, text, preselected, locale, type, values);
    }

    /**
     * Shows a help dialog.
     *
     * @param id
     */
    public void actionShowHelpDialog(String id) {
        main.showHelpDialog(id);
    }

    /**
     * Shows an info dialog.
     *
     * @param shell
     * @param header
     * @param text
     */
    public void actionShowInfoDialog(final Shell shell, final String header, final String text) {
        main.showInfoDialog(shell, header, text);
    }

    /**
     * Shows an input dialog.
     *
     * @param shell
     * @param header
     * @param text
     * @param initial
     * @return
     */
    public String actionShowInputDialog(final Shell shell,
                                        final String header,
                                        final String text,
                                        final String initial) {
        return main.showInputDialog(shell, header, text, initial);
    }

    /**
     * Shows a "open file" dialog.
     *
     * @param shell
     * @param filter
     * @return
     */
    public String actionShowOpenFileDialog(final Shell shell, String filter) {
        return main.showOpenFileDialog(shell, filter);
    }

    /**
     * Shows an input dialog for ordering data items.
     *
     * @param shell
     * @param title The dialog's title
     * @param text The dialog's text
     * @param type The data type
     * @param locale
     * @param values The values
     * @return
     */
    public String[] actionShowOrderValuesDialog(final Shell shell,
                                                final String title,
                                                final String text,
                                                final DataType<?> type,
                                                final Locale locale,
                                                final String[] values) {

        return main.showOrderValuesDialog(shell, title, text, type, locale, values);
    }

    /**
     * Shows a progress dialog.
     *
     * @param text
     * @param worker
     */
    public void actionShowProgressDialog(final String text,
                                         final Worker<?> worker) {
        main.showProgressDialog(text, worker);
    }

    /**
     * Shows a question dialog.
     *
     * @param shell
     * @param header
     * @param text
     * @return
     */
    public boolean actionShowQuestionDialog(final Shell shell,
                                            final String header,
                                            final String text) {
        return main.showQuestionDialog(shell, header, text);
    }

    /**
     * Internal method for showing a "save file" dialog.
     *
     * @param shell
     * @param filter
     * @return
     */
    public String actionShowSaveFileDialog(final Shell shell, String filter) {
        return main.showSaveFileDialog(shell, filter);
    }

    /**
     * Includes all tuples in the research subset.
     */
    public void actionSubsetAll() {
        Data data = model.getInputConfig().getInput();
        RowSet set = model.getInputConfig().getResearchSubset();
        for (int i = 0; i < data.getHandle().getNumRows(); i++) {
            set.add(i);
        }
        model.setSubsetOrigin("All");
        update(new ModelEvent(this,
                              ModelPart.RESEARCH_SUBSET,
                              set));
    }

    /**
     * Creates a research subset from a file.
     */
    public void actionSubsetFile() {

        // Open wizard
        ARXWizard<ImportConfiguration> wizard = new ImportWizard(this, model);
        if (!wizard.open(main.getShell())) {
            return;
        }

        ImportConfiguration config = wizard.getResult();
        if (config == null) {
            return;
        }

        final WorkerImport worker = new WorkerImport(config);
        main.showProgressDialog(Resources.getMessage("Controller.74"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            if (worker.getError() instanceof IllegalArgumentException) {
                main.showInfoDialog(main.getShell(),
                                    "Error loading data",
                                    worker.getError().getMessage());
            } else {
                main.showErrorDialog(main.getShell(),
                                     Resources.getMessage("Controller.76"), //$NON-NLS-1$
                                     worker.getError());
            }
            return;
        }

        Data subsetData = worker.getResult();
        Data data = model.getInputConfig().getInput();

        try {
            DataSubset subset = DataSubset.create(data, subsetData);
            model.getInputConfig().setResearchSubset(subset.getSet());
            model.setSubsetOrigin("File");
            update(new ModelEvent(this,
                                  ModelPart.RESEARCH_SUBSET,
                                  subset.getSet()));
        } catch (IllegalArgumentException e) {
            main.showInfoDialog(main.getShell(), "Error matching data", e.getMessage());
        }
    }

    /**
     * Excludes all tuples from the subset.
     */
    public void actionSubsetNone() {
        Data data = model.getInputConfig().getInput();
        RowSet empty = RowSet.create(data);
        model.getInputConfig().setResearchSubset(empty);
        model.setSubsetOrigin("None");
        update(new ModelEvent(this,
                              ModelPart.RESEARCH_SUBSET,
                              empty));
    }

    /**
     * Creates a subset by executing a query.
     */
    public void actionSubsetQuery() {
        DialogQueryResult result = main.showQueryDialog(model.getQuery(), model.getInputConfig().getInput());
        if (result == null) return;

        Data data = model.getInputConfig().getInput();
        DataSelector selector = result.selector;
        DataSubset subset = DataSubset.create(data, selector);

        this.model.getInputConfig().setResearchSubset(subset.getSet());
        this.model.setQuery(result.query);
        model.setSubsetOrigin("Query");
        update(new ModelEvent(this, ModelPart.RESEARCH_SUBSET, subset.getSet()));
    }

    /**
     * Registers a listener at the controller.
     *
     * @param target
     * @param listener
     */
    public void addListener(final ModelPart target, final IView listener) {
        if (!listeners.containsKey(target)) {
            listeners.put(target, new HashSet<IView>());
        }
        listeners.get(target).add(listener);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        for (final Set<IView> listeners : getListeners().values()) {
            for (final IView listener : listeners) {
                listener.dispose();
            }
        }
    }

    /**
     * Returns debug data.
     *
     * @return
     */
    public String getDebugData() {
        return this.debug.getData(model);
    }

    /**
     * Returns the resources.
     *
     * @return
     */
    public Resources getResources() {
        // TODO: Move resources from controller to view?
        return resources;
    }

    /**
     * Unregisters a listener.
     *
     * @param listener
     */
    public void removeListener(final IView listener) {
        for (final Set<IView> listeners : this.listeners.values()) {
            listeners.remove(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        for (final Set<IView> listeners : getListeners().values()) {
            for (final IView listener : listeners) {
                listener.reset();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (model != null && model.isDebugEnabled()) this.debug.addEvent(event);
        final Map<ModelPart, Set<IView>> dlisteners = getListeners();
        if (dlisteners.get(event.part) != null) {
            for (final IView listener : dlisteners.get(event.part)) {
                if (listener != event.source) {
                    listener.update(event);
                }
            }
        }
    }

    /**
     * Internal method for importing data.
     *
     * @param config
     */
    private void actionImportData(ImportConfiguration config) {

        final WorkerImport worker = new WorkerImport(config);
        main.showProgressDialog(Resources.getMessage("Controller.74"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            Throwable error = worker.getError();
            if (error instanceof RuntimeException) {
                if (error.getCause() != null) {
                    error = error.getCause();
                }
            }
            if ((error instanceof IllegalArgumentException) || (error instanceof IOException)) {
                main.showInfoDialog(main.getShell(), "Error loading data", error.getMessage());
            } else {
                main.showErrorDialog(main.getShell(), Resources.getMessage("Controller.76"), error); //$NON-NLS-1$
            }
            return;
        }

        // Reset
        reset();

        final Data data = worker.getResult();
        if (model.getOutput() != null) {
            this.actionResetOutput();
        }
        model.reset();

        // Disable visualization
        if (model.getMaximalSizeForComplexOperations() > 0 &&
            data.getHandle().getNumRows() > model.getMaximalSizeForComplexOperations()) {
            model.setVisualizationEnabled(false);
        }

        // Create a research subset containing all rows
        RowSet subset = RowSet.create(data);
        for (int i = 0; i < subset.length(); i++) {
            subset.add(i);
        }
        model.getInputConfig().setResearchSubset(subset);
        model.getInputConfig().setInput(data);

        // TODO: Fix this
        if (config instanceof ImportConfigurationCSV) {
            model.setInputBytes(new File(((ImportConfigurationCSV) config).getFileLocation()).length());
        } else {
            model.setInputBytes(0);
        }

        // Create definition
        final DataDefinition definition = model.getInputDefinition();
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            definition.setAttributeType(model.getInputConfig()
                                             .getInput()
                                             .getHandle()
                                             .getAttributeName(i),
                                        AttributeType.INSENSITIVE_ATTRIBUTE);
        }

        model.resetCriteria();
        model.setGroups(null);
        model.setOutput(null, null);
        model.setViewConfig(new ModelViewConfig());

        // Display the changes
        update(new ModelEvent(this, ModelPart.MODEL, model));
        update(new ModelEvent(this, ModelPart.INPUT, data.getHandle()));
        if (data.getHandle().getNumColumns() > 0) {
            model.setSelectedAttribute(data.getHandle().getAttributeName(0));
            update(new ModelEvent(this,
                                  ModelPart.SELECTED_ATTRIBUTE,
                                  data.getHandle().getAttributeName(0)));
            update(new ModelEvent(this,
                                  ModelPart.CRITERION_DEFINITION,
                                  null));
            update(new ModelEvent(this,
                                  ModelPart.RESEARCH_SUBSET,
                                  subset));
        }
    }

    /**
     * Internal method for importing hierarchies.
     *
     * @param path
     * @param separator
     * @return
     */
    private Hierarchy actionImportHierarchy(final String path,
                                            final char separator) {
        try {
            return Hierarchy.create(path, separator);
        } catch (Throwable error) {
            if (error instanceof RuntimeException) {
                if (error.getCause() != null) {
                    error = error.getCause();
                }
            }
            if ((error instanceof IllegalArgumentException) || (error instanceof IOException)) {
                main.showInfoDialog(main.getShell(), "Error loading hierarchy", error.getMessage());
            } else {
                main.showErrorDialog(main.getShell(),
                                     Resources.getMessage("Controller.78"), error); //$NON-NLS-1$
            }
        }
        return null;
    }

    /**
     * Resets the output.
     */
    private void actionResetOutput() {

        // Reset output
        model.getViewConfig().setMode(Mode.UNSORTED);
        model.getViewConfig().setSubset(false);
        model.setGroups(null);
        model.setResult(null);
        model.setOutputConfig(null);
        model.setOutput(null, null);
        model.setSelectedNode(null);

        update(new ModelEvent(this, ModelPart.VIEW_CONFIG, null));
        update(new ModelEvent(this, ModelPart.RESULT, null));
        update(new ModelEvent(this, ModelPart.OUTPUT, null));
        update(new ModelEvent(this, ModelPart.SELECTED_NODE, null));
    }

    /**
     * Internal method for saving a project.
     */
    private void actionSaveProject() {
        if (model == null) {
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.86"), //$NON-NLS-1$
                                Resources.getMessage("Controller.87")); //$NON-NLS-1$
            return;
        }

        final WorkerSave worker = new WorkerSave(model.getPath(), this, model);
        main.showProgressDialog(Resources.getMessage("Controller.88"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            getResources().getLogger().info(worker.getError());
            main.showInfoDialog(main.getShell(),
                                Resources.getMessage("Controller.90"), //$NON-NLS-1$
                                worker.getError().getMessage());
            return;
        }
    }

    /**
     * Creates a deep copy of the listeners, to avoid concurrent modification
     * issues during updates of the model.
     *
     * @return
     */
    private Map<ModelPart, Set<IView>> getListeners() {
        final Map<ModelPart, Set<IView>> result = new HashMap<ModelPart, Set<IView>>();
        for (final ModelPart key : listeners.keySet()) {
            result.put(key, new HashSet<IView>());
            result.get(key).addAll(listeners.get(key));
        }
        return result;
    }

    /**
     * Updates the view config.
     *
     * @param force Force update even if unchanged
     */
    private void updateViewConfig(boolean force) {

        ModelViewConfig config = model.getViewConfig();

        if (!force && !config.isChanged()) return;

        DataHandle handle = (config.getMode() == Mode.SORTED_INPUT) ?
                model.getInputConfig().getInput().getHandle() :
                model.getOutput();

        handle = config.isSubset() ? handle.getView() : handle;

        if (config.getMode() == Mode.SORTED_INPUT ||
            config.getMode() == Mode.SORTED_OUTPUT) {

            // Sort
            Swapper swapper = new Swapper() {
                @Override
                public void swap(int arg0, int arg1) {
                    model.getInputConfig().getResearchSubset().swap(arg0, arg1);
                }
            };
            handle.sort(swapper, config.getSortOrder(), handle.getColumnIndexOf(config.getAttribute()));
            model.setGroups(null);

        } else {

            // Groups
            // Create array with indices of all QIs
            DataDefinition definition = model.getOutputDefinition();
            int[] indices = new int[definition.getQuasiIdentifyingAttributes().size()];
            int index = 0;
            for (String attribute : definition.getQuasiIdentifyingAttributes()) {
                indices[index++] = handle.getColumnIndexOf(attribute);
            }

            // Sort by all QIs
            Swapper swapper = new Swapper() {
                @Override
                public void swap(int arg0, int arg1) {
                    model.getInputConfig().getResearchSubset().swap(arg0, arg1);
                }
            };
            handle.sort(swapper, true, indices);

            // Identify groups
            int[] groups = new int[handle.getNumRows()];
            int groupIdx = 0;
            groups[0] = 0;

            // For each row
            for (int row = 1; row < handle.getNumRows(); row++) {

                // Check if different from previous
                boolean newClass = false;
                for (int column : indices) {
                    if (!handle.getValue(row, column).equals(handle.getValue(row - 1, column))) {
                        newClass = true;
                        break;
                    }
                }

                // Store group
                groupIdx += newClass ? 1 : 0;
                groups[row] = groupIdx;
            }

            // Update
            model.setGroups(groups);
        }
    }
}
