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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IMainWindow;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.gui.view.impl.MainPopUp;
import org.deidentifier.arx.gui.view.impl.MainToolTip;
import org.deidentifier.arx.gui.view.impl.explore.NodeFilter;
import org.deidentifier.arx.gui.view.impl.menu.AboutDialog;
import org.deidentifier.arx.gui.view.impl.menu.HierarchyWizard;
import org.deidentifier.arx.gui.view.impl.menu.ProjectDialog;
import org.deidentifier.arx.gui.view.impl.menu.PropertyDialog;
import org.deidentifier.arx.gui.view.impl.menu.SeparatorDialog;
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

    private final IMainWindow                  main;

    private Model                              model     = null;

    private final Resources                    resources;

    private final Map<EventTarget, Set<IView>> listeners = Collections.synchronizedMap(new HashMap<EventTarget, Set<IView>>());

    public Controller(final IMainWindow main) {
        this.main = main;
        resources = new Resources(main.getShell());
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
            sort(model.getOutputConfig().getInput().getDefinition(), worker.getResult());
            model.setOutput(worker.getResult(), model.getSelectedNode());
            int[][] analysis = analyze(model.getOutputConfig().getInput().getDefinition(), worker.getResult());
            model.setColors(analysis[0]);
            model.setGroups(analysis[1]);
            update(new ModelEvent(this, EventTarget.OUTPUT, worker.getResult()));
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

        // Display the changes
        update(new ModelEvent(this, EventTarget.MODEL, model));
        update(new ModelEvent(this, EventTarget.INPUT, data.getHandle()));
        if (data.getHandle().getNumColumns() > 0) {
            model.setSelectedAttribute(data.getHandle().getAttributeName(0));
            update(new ModelEvent(this,
                                  EventTarget.SELECTED_ATTRIBUTE,
                                  data.getHandle().getAttributeName(0)));
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

    public void actionMenuEditAnonymize() {

        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.3"), Resources.getMessage("Controller.4")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (model.getInputConfig().getInput() == null) {
            main.showInfoDialog(Resources.getMessage("Controller.5"), Resources.getMessage("Controller.6")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (!model.validLatticeSize()) {
            final String message = Resources.getMessage("Controller.7") + Resources.getMessage("Controller.8") //$NON-NLS-1$ //$NON-NLS-2$
                                   +
                                   Resources.getMessage("Controller.9") + Resources.getMessage("Controller.10"); //$NON-NLS-1$ //$NON-NLS-2$
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
            update(new ModelEvent(this, EventTarget.RESULT, result));
            update(new ModelEvent(this, EventTarget.CLIPBOARD, null));
            if (result.isResultAvailable()) {
                sort(model.getOutputConfig().getInput().getDefinition(), result.getHandle());
                int[][] analysis = analyze(model.getOutputConfig().getInput().getDefinition(), result.getHandle());
                model.setColors(analysis[0]);
                model.setGroups(analysis[1]);
                model.setOutput(result.getHandle(), result.getGlobalOptimum());
                model.setSelectedNode(result.getGlobalOptimum());
                update(new ModelEvent(this,
                                      EventTarget.OUTPUT,
                                      result.getHandle()));
                update(new ModelEvent(this,
                                      EventTarget.SELECTED_NODE,
                                      result.getGlobalOptimum()));
            } else {
                // Select bottom node
                model.setOutput(null, null);
                model.setSelectedNode(null);
                update(new ModelEvent(this, EventTarget.OUTPUT, null));
                update(new ModelEvent(this, EventTarget.SELECTED_NODE, null));
            }

            // TODO: This is an ugly hack to synchronize the analysis views
            // Update selected attribute twice
            update(new ModelEvent(this,
                                  EventTarget.SELECTED_ATTRIBUTE,
                                  model.getSelectedAttribute()));
            update(new ModelEvent(this,
                                  EventTarget.SELECTED_ATTRIBUTE,
                                  model.getSelectedAttribute()));
        }
    }

    private int[][] analyze(DataDefinition definition, DataHandle handle) {

        int max = 0;
        int min = 0;
        
        // Iterate over all equivalence classes and compute min and max
        int[] groups = new int[handle.getNumRows()];
        int size = 0;
        int groupIdx = 0;
        for (int row = 0; row < handle.getNumRows(); row++) {

            boolean newClass = false;
            if (row > 0) {
                // TODO: Create array similar to sorting
                for (String attribute : definition.getQuasiIdentifyingAttributes()) {
                    int column = handle.getColumnIndexOf(attribute);
                    if (!handle.getValue(row,
                            column).equals(handle.getValue(row - 1, column))) {
                        newClass = true;
                        break;
                    }
                }
            } else {
                newClass = true;
            }

            if (newClass) {
                groupIdx++;
                if (row != 0) {
                    max = Math.max(max, size);
                    min = Math.min(min, size);
                }
                size = 1;
            } else {
                size++;
            }
            
            groups[row] = groupIdx;

        }
        if (handle.getNumRows() > 1) {
            max = Math.max(max, size);
            min = Math.min(min, size);
        }
        

        
        // Iterate over all equivalence classes and create color array
        int[] colors = new int[handle.getNumRows()];
        size = 0;
        for (int row = 0; row < handle.getNumRows(); row++) {

            boolean newClass = false;
            if (row > 0) {
                // TODO: Create array similar to sorting
                for (String attribute : definition.getQuasiIdentifyingAttributes()) {
                    int column = handle.getColumnIndexOf(attribute);
                    if (!handle.getValue(row,
                            column).equals(handle.getValue(row - 1, column))) {
                        newClass = true;
                        break;
                    }
                }
            } else {
                newClass = true;
            }

            if (newClass) {
                if (row != 0) {
                    
                    int val = (int)(((double)(size-min) / (double)max) * ((double)getResources().getGradientLength()-1));
                    for (int i=1; i<=size; i++){
                        colors[row-i] = val;
                    }
                }
                size = 1;
            } else {
                size++;
            }
        }
        if (handle.getNumRows() > 1) {
            int val = (int)(((double)(size-min) / (double)max) * ((double)getResources().getGradientLength()-1));
            for (int i=1; i<=size; i++){
                colors[handle.getNumRows()-i] = val;
            }
        }
        
        return new int[][]{colors, groups};
    }

    private void sort(DataDefinition definition, DataHandle handle) {

        // Sort by quasi-identifiers
        int[] indices = new int[definition.getQuasiIdentifyingAttributes().size()];
        int index = 0;
        for (String attribute :
            definition.getQuasiIdentifyingAttributes()) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        handle.sort(true, indices);
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
        final HierarchyWizard i = new HierarchyWizard(this,
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
            update(new ModelEvent(this, EventTarget.HIERARCHY, i.getModel()
                                                                .getHierarchy()));
        }
    }

    public void actionMenuEditSettings() {
        if (model == null) {
            main.showInfoDialog(Resources.getMessage("Controller.22"), Resources.getMessage("Controller.23")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        try {
            final PropertyDialog dialog = new PropertyDialog(main.getShell(),
                                                             model);
            dialog.create();
            dialog.open();

            // Update the title
            ((IView) main).update(new ModelEvent(this, EventTarget.MODEL, model));
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
        final SeparatorDialog dialog = new SeparatorDialog(main.getShell(),
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
            final SeparatorDialog dialog = new SeparatorDialog(main.getShell(),
                                                               this,
                                                               path,
                                                               false);
            dialog.create();
            if (dialog.open() == Window.CANCEL) {
                return;
            } else {
                final char separator = dialog.getSeparator();
                final AttributeType h = actionImportHierarchy(path, separator);
                update(new ModelEvent(this, EventTarget.HIERARCHY, h));
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
        final ProjectDialog dialog = new ProjectDialog(main.getShell());
        dialog.create();
        if (dialog.open() != Window.OK) { return; }

        // Set project
        reset();
        model = dialog.getProject();
        update(new ModelEvent(this, EventTarget.MODEL, model));
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
        final AboutDialog dialog = new AboutDialog(main.getShell(), this);
        dialog.create();
        dialog.open();
    }

    public void actionMenuHelpHelp() {
        Program.launch(Resources.getMessage("Controller.73")); //$NON-NLS-1$
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
        final NodeFilter tempNodeFilter = model.getNodeFilter();
        final String tempSelectedAttribute = model.getSelectedAttribute();
        final ARXNode tempSelectedNode = model.getSelectedNode();
        final Set<ARXNode> tempClipboard = new HashSet<ARXNode>();
        if (model.getClipboard() == null) {
            model.setClipboard(new HashSet<ARXNode>());
        } else {
            tempClipboard.addAll(model.getClipboard());
        }

        // Update the model
        update(new ModelEvent(this, EventTarget.MODEL, model));

        // Update subsets of the model
        if (model.getInputConfig().getInput() != null) {
            update(new ModelEvent(this,
                                  EventTarget.INPUT,
                                  model.getInputConfig().getInput().getHandle()));
        }

        // Update subsets of the model
        if (model.getResult() != null) {
            update(new ModelEvent(this, EventTarget.RESULT, model.getResult()));
        }

        // Update subsets of the model
        if (tempSelectedNode != null) {
            model.setSelectedNode(tempSelectedNode);
            update(new ModelEvent(this,
                                  EventTarget.SELECTED_NODE,
                                  model.getSelectedNode()));
            final DataHandle handle = model.getResult()
                                           .getHandle(tempSelectedNode);
            model.setOutput(handle, tempSelectedNode);
            update(new ModelEvent(this, EventTarget.OUTPUT, handle));
        }

        // Update subsets of the model
        if (tempNodeFilter != null) {
            model.setNodeFilter(tempNodeFilter);
            update(new ModelEvent(this, EventTarget.FILTER, tempNodeFilter));
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
                    update(new ModelEvent(this,
                                          EventTarget.SELECTED_ATTRIBUTE,
                                          a));

                    // Fire twice
                    model.setSelectedAttribute(a);
                    update(new ModelEvent(this,
                                          EventTarget.SELECTED_ATTRIBUTE,
                                          a));
                }
            }
        }

        // Update subsets of the model
        if (tempSelectedAttribute != null) {
            model.setSelectedAttribute(tempSelectedAttribute);
            update(new ModelEvent(this,
                                  EventTarget.SELECTED_ATTRIBUTE,
                                  tempSelectedAttribute));
        }

        // Update subsets of the model
        if (tempClipboard != null) {
            model.getClipboard().clear();
            model.getClipboard().addAll(tempClipboard);
            update(new ModelEvent(this,
                                  EventTarget.CLIPBOARD,
                                  model.getClipboard()));
        }

        // Update the attribute types
        if (model.getInputConfig().getInput() != null) {
            final DataHandle handle = model.getInputConfig()
                                           .getInput()
                                           .getHandle();
            for (int i = 0; i < handle.getNumColumns(); i++) {
                update(new ModelEvent(this,
                                      EventTarget.ATTRIBUTE_TYPE,
                                      handle.getAttributeName(i)));
            }
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

    public String
            actionShowDateFormatInputDialog(final String header,
                                            final String text,
                                            final Collection<String> values) {
        return main.showDateFormatInputDialog(header, text, values);
    }

    public void actionShowErrorDialog(final String header, final String text) {
        main.showErrorDialog(header, text);
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

    private String actionShowSaveFileDialog(String filter) {
        return main.showSaveFileDialog(filter);
    }

    public void addListener(final EventTarget target, final IView listener) {
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

    /**
     * Creates a deep copy of the listeners, to avoid concurrent modification
     * issues during updates of the model
     * 
     * @return
     */
    private Map<EventTarget, Set<IView>> getListeners() {
        final Map<EventTarget, Set<IView>> result = new HashMap<EventTarget, Set<IView>>();
        for (final EventTarget key : listeners.keySet()) {
            result.put(key, new HashSet<IView>());
            result.get(key).addAll(listeners.get(key));
        }
        return result;
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
        final Map<EventTarget, Set<IView>> dlisteners = getListeners();
        if (dlisteners.get(event.target) != null) {
            for (final IView listener : dlisteners.get(event.target)) {
                if (listener != event.source) {
                    listener.update(event);
                }
            }
        }
    }
}
