/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.deidentifier.arx.DataHandleInput;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.RowSet;
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
import org.deidentifier.arx.gui.view.impl.MainPopUp;
import org.deidentifier.arx.gui.view.impl.MainToolTip;
import org.deidentifier.arx.gui.view.impl.MainWindow;
import org.deidentifier.arx.gui.view.impl.menu.DialogAbout;
import org.deidentifier.arx.gui.view.impl.menu.DialogProject;
import org.deidentifier.arx.gui.view.impl.menu.DialogProperties;
import org.deidentifier.arx.gui.view.impl.menu.DialogQueryResult;
import org.deidentifier.arx.gui.view.impl.menu.DialogSeparator;
import org.deidentifier.arx.gui.view.impl.menu.WizardHierarchy;
import org.deidentifier.arx.gui.worker.Worker;
import org.deidentifier.arx.gui.worker.WorkerAnonymize;
import org.deidentifier.arx.gui.worker.WorkerExport;
import org.deidentifier.arx.gui.worker.WorkerImport;
import org.deidentifier.arx.gui.worker.WorkerLoad;
import org.deidentifier.arx.gui.worker.WorkerSave;
import org.deidentifier.arx.gui.worker.WorkerTransform;
import org.deidentifier.arx.io.CSVDataOutput;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.program.Program;

public class Controller implements IView {

    private Model                            model;

    private final MainWindow                 main;
    private final Resources                  resources;
    private final Map<ModelPart, Set<IView>> listeners = Collections.synchronizedMap(new HashMap<ModelPart, Set<IView>>());

    public Controller(final MainWindow main) {
        this.main = main;
        this.resources = new Resources(main.getShell());
    }

    public void actionApplySelectedTransformation() {

        // Run the worker
        final WorkerTransform worker = new WorkerTransform(model);
        main.showProgressDialog(Resources.getMessage("Controller.0"), worker); //$NON-NLS-1$

        // Show errors
        if (worker.getError() != null) {
            main.showErrorDialog("Error!", Resources.getMessage("Controller.2") + worker.getError().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Distribute results
        if (worker.getResult() != null) {
        	this.model.setOutput(worker.getResult(), model.getSelectedNode());
            this.update(new ModelEvent(this, ModelPart.OUTPUT, worker.getResult()));
            this.model.getViewConfig().setMode(Mode.GROUPED);
            this.updateViewConfig(true);
            this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
        }
    }

    /**
     * Enables and disables a criterion
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
     * Pull settings into the criterion
     * @param criterion
     */
    public void actionCriterionPull(ModelCriterion criterion) {
        
        // Collect all other criteria
        List<ModelExplicitCriterion> others = new ArrayList<ModelExplicitCriterion>();
        if (criterion instanceof ModelLDiversityCriterion){
            for (ModelLDiversityCriterion other : model.getLDiversityModel().values()){
                if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                    others.add((ModelExplicitCriterion)other);
                }
            }
        } else if (criterion instanceof ModelTClosenessCriterion){
            for (ModelTClosenessCriterion other : model.getTClosenessModel().values()){
                if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                    others.add((ModelExplicitCriterion)other);
                }
            }
        } else {
            throw new RuntimeException("Invalid type of criterion");
        }
        
        // Break if none found
        if (others.isEmpty()) {
            main.showErrorDialog(Resources.getMessage("Controller.95"), Resources.getMessage("Controller.96")); //$NON-NLS-1$ //$NON-NLS-1$
            return;
        }
        
        // Select criterion
        ModelExplicitCriterion element = main.showSelectCriterionDialog(others);
        
        if (element == null){
            return;
        } else {
            ((ModelExplicitCriterion)criterion).pull(element);
            update(new ModelEvent(this, ModelPart.CRITERION_DEFINITION, criterion));
        }
    }

    /**
     * Pushes the settings of the criterion
     * @param criterion
     */
    public void actionCriterionPush(ModelCriterion criterion) {
        if (main.showQuestionDialog(Resources.getMessage("Controller.93"), Resources.getMessage("Controller.94"))){ //$NON-NLS-1$ //$NON-NLS-1$
            
            if (criterion instanceof ModelLDiversityCriterion){
                for (ModelLDiversityCriterion other : model.getLDiversityModel().values()){
                    if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                        other.pull((ModelExplicitCriterion)criterion);
                    }
                }
            } else if (criterion instanceof ModelTClosenessCriterion){
                for (ModelTClosenessCriterion other : model.getTClosenessModel().values()){
                    if (!other.equals(criterion) && other.isActive() && other.isEnabled()) {
                        other.pull((ModelExplicitCriterion)criterion);
                    }
                }
            } else {
                throw new RuntimeException("Invalid type of criterion");
            }
            
            update(new ModelEvent(this, ModelPart.CRITERION_DEFINITION, criterion));
        }
    }

    public void actionDataShowGroups() {
        
        // Break if no output
        if (model.getOutput() == null) return;

        this.model.getViewConfig().setMode(Mode.GROUPED);
        this.updateViewConfig(false);
        
        // Update
        this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
    }

    public void actionDataSort(boolean input) {
        
        // Break if no output
        if (model.getOutput() == null) return;
        if (model.getSelectedAttribute() == null) return;
        
        if (input){
            this.model.getViewConfig().setMode(Mode.SORTED_INPUT);
        } else {
            this.model.getViewConfig().setMode(Mode.SORTED_OUTPUT);
        }
        
        this.model.getViewConfig().setAttribute(model.getSelectedAttribute());
        this.updateViewConfig(false);
        
        // Update
        this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
    }

    public void actionDataToggleSubset() {

        // Break if no output
        if (model.getOutput() == null) return;

        // TODO: Make sure that statistics are updated as well
        
        // Update
        boolean val = !model.getViewConfig().isSubset();
        this.model.getViewConfig().setSubset(val);
        this.updateViewConfig(false);
        this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
    }

    public void actionMenuEditAnonymize() {

        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.3"), Resources.getMessage("Controller.4")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(Resources.getMessage("Controller.5"), Resources.getMessage("Controller.6")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (!model.isValidLatticeSize()) {
            final String message = Resources.getMessage("Controller.7") + Resources.getMessage("Controller.8") + //$NON-NLS-1$ //$NON-NLS-2$
                                   Resources.getMessage("Controller.9") + Resources.getMessage("Controller.10"); //$NON-NLS-1$ //$NON-NLS-2$
            
            main.showInfoDialog(Resources.getMessage("Controller.11"), message); //$NON-NLS-1$
            return;
        }

        if (model.getInputConfig().getResearchSubset().size()==0) {
            final String message = Resources.getMessage("Controller.100"); //$NON-NLS-1$
            main.showInfoDialog(Resources.getMessage("Controller.11"), message); //$NON-NLS-1$
            return;
        }
        // Free resources before anonymizing again
        // TODO: Is this enough?
        model.setResult(null);
        model.setOutput(null, null);

        // Run the worker
        final WorkerAnonymize worker = new WorkerAnonymize(model);
        main.showProgressDialog(Resources.getMessage("Controller.12"), worker); //$NON-NLS-1$
        model.createClonedConfig();
        
        // Show errors
        if (worker.getError() != null) {
            String message = worker.getError().getMessage();
            if (worker.getError() instanceof InvocationTargetException) {
                message = worker.getError().getCause().getMessage();
            }
            main.showErrorDialog(Resources.getMessage("Controller.13"), Resources.getMessage("Controller.14") + message); //$NON-NLS-1$ //$NON-NLS-2$
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
            model.setResult(result);
            model.getClipboard().clear();

            // Update view
            update(new ModelEvent(this, ModelPart.RESULT, result));
            update(new ModelEvent(this, ModelPart.CLIPBOARD, null));
            if (result.isResultAvailable()) {
                model.setOutput(result.getHandle(), result.getGlobalOptimum());
                model.setSelectedNode(result.getGlobalOptimum());
                update(new ModelEvent(this,
                                      ModelPart.OUTPUT,
                                      result.getHandle()));
                update(new ModelEvent(this,
                                      ModelPart.SELECTED_NODE,
                                      result.getGlobalOptimum()));
                this.model.getViewConfig().setMode(Mode.GROUPED);
                this.updateViewConfig(true);
                this.update(new ModelEvent(this, ModelPart.VIEW_CONFIG, model.getOutput()));
            } else {
                // Select bottom node
                model.setOutput(null, null);
                model.setSelectedNode(null);
                model.setGroups(null);
                update(new ModelEvent(this, ModelPart.OUTPUT, null));
                update(new ModelEvent(this, ModelPart.SELECTED_NODE, null));
            }

            // TODO: This is an ugly hack to synchronize the analysis views
            // Update selected attribute twice
            update(new ModelEvent(this,
                                  ModelPart.SELECTED_ATTRIBUTE,
                                  model.getSelectedAttribute()));
            update(new ModelEvent(this,
                                  ModelPart.SELECTED_ATTRIBUTE,
                                  model.getSelectedAttribute()));
        }
    }

    public void actionMenuEditCreateHierarchy() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.16"), Resources.getMessage("Controller.17")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(Resources.getMessage("Controller.18"), Resources.getMessage("Controller.19")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (!(model.isQuasiIdentifierSelected() || model.isSensitiveAttributeSelected())) {
            main.showInfoDialog(Resources.getMessage("Controller.20"), Resources.getMessage("Controller.21")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        final String attr = model.getSelectedAttribute();
        final int index = model.getInputConfig()
                               .getInput()
                               .getHandle()
                               .getColumnIndexOf(attr);
        final WizardHierarchy i = new WizardHierarchy(this,
                                                      attr,
                                                      model.getInputConfig()
                                                           .getInput()
                                                           .getDefinition()
                                                           .getDataType(attr),
                                                      model.getSuppressionString(),
                                                      model.getInputConfig()
                                                           .getInput()
                                                           .getHandle()
                                                           .getDistinctValues(index));
        if (i.open(main.getShell())) {
            update(new ModelEvent(this, ModelPart.HIERARCHY, i.getModel()
                                                                .getHierarchy()));
        }
    }

    public void actionMenuEditSettings() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.22"), Resources.getMessage("Controller.23")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        try {
            final DialogProperties dialog = new DialogProperties(main.getShell(),
                                                             model);
            dialog.create();
            dialog.open();

            // Update the title
            ((IView) main).update(new ModelEvent(this, ModelPart.MODEL, model));
        } catch (final Exception e) {
            main.showErrorDialog(Resources.getMessage("Controller.24"), Resources.getMessage("Controller.25")); //$NON-NLS-1$ //$NON-NLS-2$
            getResources().getLogger().info(e);
        }
    }

    public void actionMenuFileExit() {
        if (main.showQuestionDialog(Resources.getMessage("Controller.26"), Resources.getMessage("Controller.27"))) { //$NON-NLS-1$ //$NON-NLS-2$
            if ((model != null) && model.isModified()) {
                if (main.showQuestionDialog(Resources.getMessage("Controller.28"), Resources.getMessage("Controller.29"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    actionMenuFileSave();
                }
            }
            main.getShell().getDisplay().dispose();
            System.exit(0);
        }
    }

    public void actionMenuFileExportData() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.30"), Resources.getMessage("Controller.31")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (model.getOutput() == null) {
            main.showInfoDialog(Resources.getMessage("Controller.32"), Resources.getMessage("Controller.33")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check node
        if (model.getOutputNode().isAnonymous() != Anonymity.ANONYMOUS) {
            if (!main.showQuestionDialog(Resources.getMessage("Controller.34"), Resources.getMessage("Controller.35"))) { return; } //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Ask for file
        String file = main.showSaveFileDialog("*.csv"); //$NON-NLS-1$
        if (file == null) { return; }
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
            main.showErrorDialog(Resources.getMessage("Controller.40"), Resources.getMessage("Controller.41") + worker.getError().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
    }

    public void actionMenuFileExportHierarchy() {

        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.42"), Resources.getMessage("Controller.43")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (!(model.isQuasiIdentifierSelected() || model.isSensitiveAttributeSelected())) {
            main.showInfoDialog(Resources.getMessage("Controller.44"), Resources.getMessage("Controller.45")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Ask for file
        String file = main.showSaveFileDialog("*.csv"); //$NON-NLS-1$
        if (file == null) { return; }
        if (!file.endsWith(".csv")) { //$NON-NLS-1$
            file = file + ".csv"; //$NON-NLS-1$
        }

        // Save
        try {
            final CSVDataOutput out = new CSVDataOutput(file,
                                                        model.getSeparator());
            
            Hierarchy h = model.getInputConfig().getHierarchy(model.getSelectedAttribute());
            if (h==null){
               main.showInfoDialog(Resources.getMessage("Controller.91"), Resources.getMessage("Controller.92")); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                out.write(h.getHierarchy());
            }

        } catch (final Exception e) {
            main.showErrorDialog(Resources.getMessage("Controller.49"), Resources.getMessage("Controller.50") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void actionMenuFileImportData() {

        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.51"), Resources.getMessage("Controller.52")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check
        final String path = actionShowOpenFileDialog("*.csv"); //$NON-NLS-1$
        if (path == null) { return; }

        // Separator
        final DialogSeparator dialog = new DialogSeparator(main.getShell(),
                                                           this,
                                                           path,
                                                           true);
        dialog.create();
        if (dialog.open() == Window.CANCEL) {
            return;
        } else {
            actionImportData(path, dialog.getSeparator());
        }
    }

    public void actionMenuFileImportHierarchy() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.54"), Resources.getMessage("Controller.55")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(Resources.getMessage("Controller.56"), Resources.getMessage("Controller.57")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        } else if (!(model.isQuasiIdentifierSelected() || model.isSensitiveAttributeSelected())) {
            main.showInfoDialog(Resources.getMessage("Controller.58"), Resources.getMessage("Controller.59")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        final String path = actionShowOpenFileDialog("*.csv"); //$NON-NLS-1$
        if (path != null) {

            // Separator
            final DialogSeparator dialog = new DialogSeparator(main.getShell(),
                                                               this,
                                                               path,
                                                               false);
            dialog.create();
            if (dialog.open() == Window.CANCEL) {
                return;
            } else {
                final char separator = dialog.getSeparator();
                final AttributeType h = actionImportHierarchy(path, separator);
                update(new ModelEvent(this, ModelPart.HIERARCHY, h));
            }
        }
    }

    public void actionMenuFileNew() {

        if ((model != null) && model.isModified()) {
            if (main.showQuestionDialog(Resources.getMessage("Controller.61"), Resources.getMessage("Controller.62"))) { //$NON-NLS-1$ //$NON-NLS-2$
                actionMenuFileSave();
            }
        }

        // Separator
        final DialogProject dialog = new DialogProject(main.getShell());
        dialog.create();
        if (dialog.open() != Window.OK) { return; }

        // Set project
        reset();
        model = dialog.getProject();
        update(new ModelEvent(this, ModelPart.MODEL, model));
    }

    public void actionMenuFileOpen() {

        if ((model != null) && model.isModified()) {
            if (main.showQuestionDialog(Resources.getMessage("Controller.63"), Resources.getMessage("Controller.64"))) { //$NON-NLS-1$ //$NON-NLS-2$
                actionMenuFileSave();
            }
        }

        // Check
        final String path = actionShowOpenFileDialog("*.deid"); //$NON-NLS-1$
        if (path == null) { return; }

        // Set project
        reset();

        actionOpenProject(path);

    }

    public void actionMenuFileSave() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.66"), Resources.getMessage("Controller.67")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        if (model.getPath() == null) {
            actionMenuFileSaveAs();
        } else {
            actionSaveProject();
        }
    }

    public void actionMenuFileSaveAs() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.68"), Resources.getMessage("Controller.69")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check
        String path = actionShowSaveFileDialog("*.deid"); //$NON-NLS-1$
        if (path == null) { return; }

        if (!path.endsWith(".deid")) { //$NON-NLS-1$
            path += ".deid"; //$NON-NLS-1$
        }
        model.setPath(path);
        actionSaveProject();
    }

    public void actionMenuHelpAbout() {
        final DialogAbout dialog = new DialogAbout(main.getShell(), this);
        dialog.create();
        dialog.open();
    }

    public void actionMenuHelpHelp() {
        Program.launch(Resources.getMessage("Controller.73")); //$NON-NLS-1$
    }

    public String
            actionShowDateFormatInputDialog(final String header,
                                            final String text,
                                            final Collection<String> values) {
        return main.showDateFormatInputDialog(header, text, values);
    }

    public void actionShowErrorDialog(final String header, final String text) {
        main.showErrorDialog(header, text);
    }

    /**
     * Shows the help
     * @param id
     */
    public void actionShowHelp(String id) {
        main.showInfoDialog("Help", id);
    }

    public void actionShowInfoDialog(final String header, final String text) {
        main.showInfoDialog(header, text);
    }

    public String actionShowInputDialog(final String header,
                                        final String text,
                                        final String initial) {
        return main.showInputDialog(header, text, initial);
    }

    public String actionShowOpenFileDialog(String filter) {
        return main.showOpenFileDialog(filter);
    }

    public void actionShowProgressDialog(final String text,
                                         final Worker<?> worker) {
        main.showProgressDialog(text, worker);
    }

    public boolean actionShowQuestionDialog(final String header,
                                            final String text) {
        return main.showQuestionDialog(header, text);
    }

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

    public void actionSubsetFile() {

        // Check
        final String path = actionShowOpenFileDialog("*.csv"); //$NON-NLS-1$
        if (path == null) { return; }

        // Separator
        final DialogSeparator dialog = new DialogSeparator(main.getShell(),
                                                           this,
                                                           path,
                                                           true);
        dialog.create();
        if (dialog.open() == Window.CANCEL) {
            return;
        } 
   
        final WorkerImport worker = new WorkerImport(path, dialog.getSeparator());
        main.showProgressDialog(Resources.getMessage("Controller.74"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            main.showErrorDialog(Resources.getMessage("Controller.75"), Resources.getMessage("Controller.76") + worker.getError().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        Data subsetData = worker.getResult();
        Data data = model.getInputConfig().getInput();
        
        try {
            DataSubset subset = DataSubset.create(data, subsetData);
            model.getInputConfig().setResearchSubset(subset.getRowSet());
            model.setSubsetOrigin("File");
            update(new ModelEvent(this,
                                  ModelPart.RESEARCH_SUBSET,
                                  subset.getRowSet()));
        } catch (IllegalArgumentException e){
            main.showErrorDialog("Error!", e.getMessage());
        }
    }

    public void actionSubsetNone() {
        Data data = model.getInputConfig().getInput();
        RowSet empty = RowSet.create(data);
        model.getInputConfig().setResearchSubset(empty);
        model.setSubsetOrigin("None");
        update(new ModelEvent(this,
                              ModelPart.RESEARCH_SUBSET,
                              empty));
    }

    public void actionSubsetQuery() {
        DialogQueryResult result = main.showQueryDialog(model.getQuery(), model.getInputConfig().getInput());
        if (result == null) return;
        
        Data data = model.getInputConfig().getInput();
        DataSelector selector = result.selector;
        DataSubset subset = DataSubset.create(data, selector);
        
        this.model.getInputConfig().setResearchSubset(subset.getRowSet());
        this.model.setQuery(result.query);
        model.setSubsetOrigin("Query");
        update(new ModelEvent(this, ModelPart.RESEARCH_SUBSET, subset.getRowSet()));
    }

    public void addListener(final ModelPart target, final IView listener) {
        if (!listeners.containsKey(target)) {
            listeners.put(target, new HashSet<IView>());
        }
        listeners.get(target).add(listener);
    }

    @Override
    public void dispose() {
        for (final Set<IView> listeners : getListeners().values()) {
            for (final IView listener : listeners) {
                listener.dispose();
            }
        }
    }

    public MainPopUp getPopup() {
        return main.getPopUp();
    }

    // TODO: Move resources from controller to view?
    public Resources getResources() {
        return resources;
    }

    public MainToolTip getToolTip() {
        return main.getToolTip();
    }

    public void removeListener(final IView listener) {
        for (final Set<IView> listeners : this.listeners.values()) {
            listeners.remove(listener);
        }
    }

    @Override
    public void reset() {
        for (final Set<IView> listeners : getListeners().values()) {
            for (final IView listener : listeners) {
                listener.reset();
            }
        }
    }

    @Override
    public void update(final ModelEvent event) {
        final Map<ModelPart, Set<IView>> dlisteners = getListeners();
        if (dlisteners.get(event.part) != null) {
            for (final IView listener : dlisteners.get(event.part)) {
                if (listener != event.source) {
                    listener.update(event);
                }
            }
        }
    }

    private void actionImportData(final String path, final char separator) {

        final WorkerImport worker = new WorkerImport(path, separator);
        main.showProgressDialog(Resources.getMessage("Controller.74"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            main.showErrorDialog(Resources.getMessage("Controller.75"), Resources.getMessage("Controller.76") + worker.getError().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Reset
        reset();

        final Data data = worker.getResult();
        model.reset();
        
        // Create a research subset containing all rows
        RowSet subset = RowSet.create(data);
        for (int i=0; i<subset.length(); i++){
            subset.add(i);
        }
        model.getInputConfig().setResearchSubset(subset);
        model.getInputConfig().setInput(data);
        model.setInputBytes(new File(path).length());

        // Create definition
        final DataDefinition definition = data.getDefinition();
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            definition.setAttributeType(model.getInputConfig()
                                             .getInput()
                                             .getHandle()
                                             .getAttributeName(i),
                                        AttributeType.INSENSITIVE_ATTRIBUTE);
            definition.setDataType(model.getInputConfig()
                                        .getInput()
                                        .getHandle()
                                        .getAttributeName(i), DataType.STRING);
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

    private AttributeType actionImportHierarchy(final String path,
                                                final char separator) {
        try {
            return Hierarchy.create(path, separator);
        } catch (final IOException e) {
            main.showErrorDialog(Resources.getMessage("Controller.77"), Resources.getMessage("Controller.78") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    private void actionOpenProject(String path) {
        if (!path.endsWith(".deid")) { //$NON-NLS-1$
            path += ".deid"; //$NON-NLS-1$
        }

        WorkerLoad worker = null;
        try {
            worker = new WorkerLoad(path, this);
        } catch (final IOException e) {
            main.showErrorDialog(Resources.getMessage("Controller.81"), Resources.getMessage("Controller.82") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        main.showProgressDialog(Resources.getMessage("Controller.83"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            getResources().getLogger().info(worker.getError());
            main.showErrorDialog(Resources.getMessage("Controller.84"), Resources.getMessage("Controller.85") + worker.getError().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
        final Set<ARXNode> tempClipboard = new HashSet<ARXNode>();
        if (model.getClipboard() == null) {
            model.setClipboard(new HashSet<ARXNode>());
        } else {
            tempClipboard.addAll(model.getClipboard());
        }

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
            final DataHandle handle = model.getResult().getHandle(tempSelectedNode);
            model.setOutput(handle, tempSelectedNode);
            update(new ModelEvent(this, ModelPart.OUTPUT, handle));
        }

        // Update subsets of the model
        if (tempNodeFilter != null) {
            model.setNodeFilter(tempNodeFilter);
            update(new ModelEvent(this, ModelPart.FILTER, tempNodeFilter));
        }

        // Try to trigger some events under MS Windows
        // TODO: This is ugly!
        if (model.getInputConfig() != null &&
            model.getInputConfig().getInput() != null) {
            DataHandle h = model.getInputConfig().getInput().getHandle();
            if (h != null) {
                if (h.getNumColumns() > 0) {
                    String a = h.getAttributeName(0);

                    // Fire once
                    model.setSelectedAttribute(a);
                    update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, a));

                    // Fire twice
                    model.setSelectedAttribute(a);
                    update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, a));
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
            model.getClipboard().clear();
            model.getClipboard().addAll(tempClipboard);
            update(new ModelEvent(this,
                                  ModelPart.CLIPBOARD,
                                  model.getClipboard()));
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
        if (model.getOutput() != null){
	        update(new ModelEvent(this,
	                              ModelPart.VIEW_CONFIG,
	                              model.getOutput()));
        }

        // We just loaded the model, so there are no changes
        model.setUnmodified();
    }

    private void actionSaveProject() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.86"), Resources.getMessage("Controller.87")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        final WorkerSave worker = new WorkerSave(model.getPath(), this, model);
        main.showProgressDialog(Resources.getMessage("Controller.88"), worker); //$NON-NLS-1$
        if (worker.getError() != null) {
            getResources().getLogger().info(worker.getError());
            main.showErrorDialog(Resources.getMessage("Controller.89"), Resources.getMessage("Controller.90") + worker.getError().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
    }

    private String actionShowSaveFileDialog(String filter) {
        return main.showSaveFileDialog(filter);
    }

    /**
     * Creates a deep copy of the listeners, to avoid concurrent modification
     * issues during updates of the model
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

    private void updateViewConfig(boolean force) {
        
        if (!force && !model.isViewConfigChanged()) return;
        
        ModelViewConfig config = model.getViewConfig();
        
        DataHandle handle = (config.getMode() == Mode.SORTED_INPUT) ? 
                            model.getInputConfig().getInput().getHandle() : 
                            model.getOutput();
        
        handle = config.isSubset() ? 
                 handle.getView(model.getOutputConfig().getConfig()) :
                 handle;
                 
        DataDefinition definition = handle.getDefinition();
        
        if (config.getMode() == Mode.SORTED_INPUT ||
            config.getMode() == Mode.SORTED_OUTPUT) {
            
            // Sort
            handle.sort(true, handle.getColumnIndexOf(config.getAttribute()));
            model.setGroups(null);
            
            // Update subset as it might have changed
            DataHandleInput inHandle = (DataHandleInput)model.getInputConfig().getInput().getHandle();
            model.getInputConfig().setResearchSubset(inHandle.getSubset().clone());
            
        } else {
            
            // Groups
            // Create array with indices of all QIs
            int[] indices = new int[definition.getQuasiIdentifyingAttributes().size()];
            int index = 0;
            for (String attribute : definition.getQuasiIdentifyingAttributes()) {
                indices[index++] = handle.getColumnIndexOf(attribute);
            }
            
            // Sort by all QIs
            handle.sort(true, indices);

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
            
            // Update subset as it might have changed
            DataHandleInput inHandle = (DataHandleInput)model.getInputConfig().getInput().getHandle();
            model.getInputConfig().setResearchSubset(inHandle.getSubset().clone());
        }
    }
}
