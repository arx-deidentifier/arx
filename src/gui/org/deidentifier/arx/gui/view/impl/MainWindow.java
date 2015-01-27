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

package org.deidentifier.arx.gui.view.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.analyze.LayoutAnalyze;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.define.LayoutDefinition;
import org.deidentifier.arx.gui.view.impl.explore.LayoutExplore;
import org.deidentifier.arx.gui.view.impl.menu.DialogAbout;
import org.deidentifier.arx.gui.view.impl.menu.DialogComboSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogCriterionSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogDebug;
import org.deidentifier.arx.gui.view.impl.menu.DialogError;
import org.deidentifier.arx.gui.view.impl.menu.DialogHelp;
import org.deidentifier.arx.gui.view.impl.menu.DialogOrderSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogQuery;
import org.deidentifier.arx.gui.view.impl.menu.DialogQueryResult;
import org.deidentifier.arx.gui.worker.Worker;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements the global application window.
 *
 * @author Fabian Prasser
 */
public class MainWindow implements IView {

    /**  TODO */
    private static final String         TITLE                     = Resources.getMessage("MainWindow.0");                     //$NON-NLS-1$
    
    /**  TODO */
    private static final String         TAB_ANALYZE_DATA          = Resources.getMessage("MainWindow.1");                     //$NON-NLS-1$
    
    /**  TODO */
    private static final String         TAB_DEFINE_TRANSFORMATION = Resources.getMessage("MainWindow.2");                     //$NON-NLS-1$
    
    /**  TODO */
    private static final String         TAB_EXPLORE_SEARCHSPACE   = Resources.getMessage("MainWindow.3");                     //$NON-NLS-1$

    /**  TODO */
    private final Display               display;
    
    /**  TODO */
    private final Shell                 shell;
    
    /**  TODO */
    private final Controller            controller;
    
    /**  TODO */
    private final MainMenu              menu;

    /**  TODO */
    private final ComponentTitledFolder root;

    /**
     * Creates a new instance.
     *
     * @param display
     * @param monitor
     */
    public MainWindow(Display display, Monitor monitor) {

        // Init
        this.display = display;
        shell = new Shell(display);

        // Build controller
        controller = new Controller(this);
        controller.addListener(ModelPart.MODEL, this);

        // Style
        shell.setImages(Resources.getIconSet(display));
        shell.setText(TITLE);
        shell.setMinimumSize(800, 600);

        // Center
        SWTUtil.center(shell, monitor);
        
        // Maximize
        shell.setMaximized(true);
        
        // Close listener
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                controller.actionMenuFileExit();
                event.doit = false;
            }
        });

        // Build menu
        menu = new MainMenu(shell, controller);
        new MainToolBar(shell, controller);

        // Create shell
        shell.setLayout(SWTUtil.createGridLayout(1));

        // Create the tab folder
        root = new ComponentTitledFolder(shell, controller, null, "id-70");
        root.setLayoutData(SWTUtil.createFillGridData());

        // Create the subviews
        Composite item1 = root.createItem(TAB_DEFINE_TRANSFORMATION, controller.getResources().getImage("perspective_define.png")); //$NON-NLS-1$
        new LayoutDefinition(item1, controller);
        Composite item2 = root.createItem(TAB_EXPLORE_SEARCHSPACE, controller.getResources().getImage("perspective_explore.png")); //$NON-NLS-1$
        new LayoutExplore(item2, controller);
        Composite item3 = root.createItem(TAB_ANALYZE_DATA, controller.getResources().getImage("perspective_analyze.png")); //$NON-NLS-1$
        new LayoutAnalyze(item3, controller);

        // Hack to update visualizations
        root.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                controller.update(new ModelEvent(this, ModelPart.VISUALIZATION, null));
            }
        });
        
        // Now reset and disable
        controller.reset();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns the controller.
     *
     * @return
     */
    public Controller getController() {
        return this.controller;
    }

    /**
     * Returns the shell.
     *
     * @return
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * Is this shell disposed.
     *
     * @return
     */
    public boolean isDisposed() {
        return this.shell.isDisposed();
    }

    /**
     * Executes the given runnable on show.
     *
     * @param runnable
     */
    public void onShow(final Runnable runnable){
        
        // Using a paint listener is a hack to reliably determine when the shell is visible
        shell.addPaintListener(new PaintListener(){
            @Override
            public void paintControl(PaintEvent arg0) {
                shell.removePaintListener(this);
                display.timerExec(200, runnable);
            }
        });
    }

    /**
     * Resets the GUI.
     */
    public void reset() {
        root.setSelection(0);
        root.setEnabled(false);
    }

    /**
     * Main SWT event loop.
     */
    public void show() {
        shell.open();
    }

    /**
     * Shows an about dialog.
     */
    public void showAboutDialog() {
        final DialogAbout dialog = new DialogAbout(shell, controller);
        dialog.create();
        dialog.open();
    }
    
    /**
     * Shows a debug dialog.
     */
    public void showDebugDialog() {
        final DialogDebug dialog = new DialogDebug(shell, controller);
        dialog.create();
        dialog.open();
    }
    
    /**
     * Shows an error dialog.
     *
     * @param shell
     * @param message
     * @param text
     */
    public void showErrorDialog(final Shell shell, final String message, final String text) {
        DialogError dialog = new DialogError(shell, controller, message, text);
        dialog.create();
        dialog.open();
    }

    /**
     * Shows an error dialog.
     *
     * @param shell
     * @param message
     * @param throwable
     */
    public void showErrorDialog(final Shell shell, final String message, final Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (throwable != null) throwable.printStackTrace(pw);
        final String trace = sw.toString();
        showErrorDialog(shell, message, trace);
    }
    
    /**
     * Shows an error dialog.
     *
     * @param message
     * @param text
     */
    public void showErrorDialog(final String message, final String text) {
        showErrorDialog(this.shell, message, text);
    }

    /**
     * Shows an error dialog.
     *
     * @param message
     * @param throwable
     */
    public void showErrorDialog(final String message, final Throwable throwable) {
        showErrorDialog(this.shell, message, throwable);
    }

    /**
     * Shows an input dialog for selecting formats string for data types.
     *
     * @param shell
     * @param header
     * @param text
     * @param preselected Preselected format string, can be null
     * @param locale The current locale
     * @param description
     * @param values
     * @return
     */
    public String showFormatInputDialog(final Shell shell, final String header, final String text, final String preselected, final Locale locale, final DataTypeDescription<?> description, final Collection<String> values) {

        // Check
        if (!description.hasFormat()) {
            throw new RuntimeException("This dialog can only be used for data types with format");
        }

        // Init
        final String DEFAULT = "Default";

        // Validator
        final IInputValidator validator = new IInputValidator() {
            @Override
            public String isValid(final String arg0) {
                DataType<?> type;
                try {
                    if (arg0.equals(DEFAULT)) {
                        type = description.newInstance();
                    } else {
                        type = description.newInstance(arg0, locale);
                    }
                } catch (final Exception e) {
                    return Resources.getMessage("MainWindow.11"); //$NON-NLS-1$
                }
                for (final String value : values) {
                    if (!type.isValid(value)) {
                        return Resources.getMessage("MainWindow.13"); //$NON-NLS-1$
                    }
                }
                return null;
            }
        };

        // Try to find a valid formatter
        String initial = ""; //$NON-NLS-1$
        if (preselected != null && validator.isValid(preselected) == null) {
            initial = preselected;
        } else if (validator.isValid(DEFAULT) == null) {
            initial = DEFAULT;
        } else {
            for (final String format : description.getExampleFormats()) {
                if (validator.isValid(format) == null) {
                    initial = format;
                    break;
                }
            }
        }

        // Extract list of formats
        List<String> formats = new ArrayList<String>();
        formats.add(DEFAULT);
        formats.addAll(description.getExampleFormats());

        // Open dialog
        final DialogComboSelection dlg = new DialogComboSelection(shell, header, text, formats.toArray(new String[] {}), initial, validator);

        // Return value
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        } else {
            return null;
        }
    }

    /**
     * Shows a help dialog.
     *
     * @param id
     */
    public void showHelpDialog(String id) {
    	try {
    		final DialogHelp dialog = new DialogHelp(shell, controller, id);
            dialog.create();
            dialog.open();	
    	} catch (Exception e) {
    		if (e.getMessage().contains("Mozilla")) {
    			this.showErrorDialog("Your installation of Mozilla Firefox cannot be launched", 
    					"See http://www.eclipse.org/swt/faq.php#browserlinuxrcp for information on how to fix this issue.");
    		} else {
    		    this.showErrorDialog("Your browser cannot be launched", e);
    		}
    	}
    }

    /**
     * Shows an info dialog.
     *
     * @param shell
     * @param header
     * @param text
     */
    public void showInfoDialog(final Shell shell, final String header, final String text) {
        MessageDialog.openInformation(getShell(), header, text);
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
    public String showInputDialog(final Shell shell, final String header, final String text, final String initial) {

        final InputDialog dlg = new InputDialog(shell, header, text, initial, null);
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        } else {
            return null;
        }
    }

    /**
     * Shows a file open dialog.
     *
     * @param shell
     * @param filter
     * @return
     */
    public String showOpenFileDialog(final Shell shell, String filter) {
        final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { filter });
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    /**
     * Shows an input dialog for ordering data items.
     *
     * @param shell
     * @param header
     * @param text
     * @param type
     * @param locale
     * @param values
     * @return
     */
    public String[] showOrderValuesDialog(final Shell shell, final String header, final String text, final DataType<?> type, final Locale locale, final String[] values) {

        // Open dialog
        DialogOrderSelection dlg = new DialogOrderSelection(shell, values, type, locale, controller);
        if (dlg.open() == Window.OK) {
            return dlg.getResult();
        } else {
            return null;
        }
    }

    /**
     * Shows a progress dialog.
     *
     * @param text
     * @param worker
     */
    public void showProgressDialog(final String text, final Worker<?> worker) {
        try {
            new ProgressMonitorDialog(shell).run(true, true, worker);
        } catch (final Exception e) {
            worker.setError(e);
        }
    }

    /**
     * Shows a query dialog for selecting a research subset.
     *
     * @param query
     * @param data
     * @return
     */
    public DialogQueryResult showQueryDialog(String query, Data data) {

        // Dialog
        final DialogQuery dialog = new DialogQuery(data, shell, query);
        dialog.create();
        if (dialog.open() != Window.OK) {
            return null;
        } else {
            return dialog.getResult();
        }
    }

    /**
     * Shows a question dialog.
     *
     * @param shell
     * @param header
     * @param text
     * @return
     */
    public boolean showQuestionDialog(final Shell shell, final String header, final String text) {
        return MessageDialog.openQuestion(getShell(), header, text);
    }

    /**
     * Shows a file save dialog.
     *
     * @param shell
     * @param filter
     * @return
     */
    public String showSaveFileDialog(final Shell shell, String filter) {
        final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { filter });
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    /**
     * Shows a dialog for selecting privacy criteria.
     *
     * @param others
     * @return
     */
    public ModelExplicitCriterion showSelectCriterionDialog(List<ModelExplicitCriterion> others) {

        // Dialog
        final DialogCriterionSelection dialog = new DialogCriterionSelection(controller, shell, others);
        dialog.create();
        if (dialog.open() != Window.OK) {
            return null;
        } else {
            return dialog.getCriterion();
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {

        // Careful! In the main window, this is also called after editing the project properties
        if (event.part == ModelPart.MODEL) {
            final Model model = (Model) event.data;
            shell.setText(TITLE + " - " + model.getName()); //$NON-NLS-1$
            root.setEnabled(true);
            menu.update(event);
        }
    }
}
