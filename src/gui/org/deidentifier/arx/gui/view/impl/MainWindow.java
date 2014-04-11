/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.view.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements the global application window
 * @author Fabian Prasser
 */
public class MainWindow implements IView {

    public static final Font            FONT                      = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL)); //$NON-NLS-1$

    private static final String         TITLE                     = Resources.getMessage("MainWindow.0");                     //$NON-NLS-1$
    private static final String         TAB_ANALYZE_DATA          = Resources.getMessage("MainWindow.1");                     //$NON-NLS-1$
    private static final String         TAB_DEFINE_TRANSFORMATION = Resources.getMessage("MainWindow.2");                     //$NON-NLS-1$
    private static final String         TAB_EXPLORE_SEARCHSPACE   = Resources.getMessage("MainWindow.3");                     //$NON-NLS-1$

    private final Display               display;
    private final Shell                 shell;
    private final Controller            controller;
    private final MainToolTip           tooltip;
    private final MainContextMenu       popup;
    private final MainMenu              menu;
    private final MainToolBar           toolbar;

    private final ComponentTitledFolder root;

    /**
     * Creates a new instance
     */
    public MainWindow() {

        // Init
        Display current = Display.getCurrent();
        display = current != null ? current : new Display();
        shell = new Shell(display);

        // Build controller
        controller = new Controller(this);
        controller.addListener(ModelPart.MODEL, this);

        // Style
        shell.setImage(controller.getResources().getImage("logo.png")); //$NON-NLS-1$
        shell.setMaximized(true);
        shell.setText(TITLE);
        shell.setMinimumSize(800, 600);

        tooltip = new MainToolTip(shell);
        popup = new MainContextMenu(shell, tooltip);
        tooltip.setPopUp(popup);

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
        toolbar = new MainToolBar(shell, controller);

        // Create shell
        shell.setLayout(SWTUtil.createGridLayout(1));

        // Create the tab folder
        root = new ComponentTitledFolder(shell, controller, null, "id-70");
        root.setLayoutData(SWTUtil.createFillGridData());

        // TODO: Remove? Fixes an SWT Bug!
        // root.setBackground(shell.getBackground());

        // Create the subviews
        Composite item1 = root.createItem(TAB_DEFINE_TRANSFORMATION, controller.getResources().getImage("perspective_define.png")); //$NON-NLS-1$
        new LayoutDefinition(item1, controller);
        Composite item2 = root.createItem(TAB_EXPLORE_SEARCHSPACE, controller.getResources().getImage("perspective_explore.png")); //$NON-NLS-1$
        new LayoutExplore(item2, controller);
        Composite item3 = root.createItem(TAB_ANALYZE_DATA, controller.getResources().getImage("perspective_analyze.png")); //$NON-NLS-1$
        new LayoutAnalyze(item3, controller);

        // Now reset and disable
        controller.reset();
    }

    /**
     * Adds a listener
     * @param event
     * @param listener
     */
    public void addListener(int event, Listener listener) {
        shell.addListener(event, listener);
    }

    /**
     * Adds a shell listener
     * @param listener
     */
    public void addShellListener(ShellListener listener) {
        this.shell.addShellListener(listener);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns the popup window
     * @return
     */
    public MainContextMenu getPopUp() {
        return popup;
    }

    /**
     * Returns the shell
     * @return
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * Returns the tooltip window
     * @return
     */
    public MainToolTip getToolTip() {
        return tooltip;
    }

    /**
     * Resets the GUI
     */
    public void reset() {
        root.setSelection(0);
        root.setEnabled(false);
    }

    /**
     * Main SWT event loop
     */
    public void show() {
        shell.open();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (final Exception e) {
                controller.actionShowErrorDialog(Resources.getMessage("MainWindow.8"), Resources.getMessage("MainWindow.9") + Resources.getMessage("MainWindow.10"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                controller.getResources().getLogger().info(sw.toString());
            }
        }
        display.dispose();
    }

    /**
     * Shows a debug dialog
     */
    public void showDebugDialog() {
        final DialogDebug dialog = new DialogDebug(shell, controller);
        dialog.create();
        dialog.open();
    }

    /**
     * Shows an about dialog
     */
    public void showAboutDialog() {
        final DialogAbout dialog = new DialogAbout(shell, controller);
        dialog.create();
        dialog.open();
    }

    /**
     * Shows an error dialog
     * @param header
     * @param message
     */
    public void showErrorDialog(final String header, final String message) {
        final DialogError dialog = new DialogError(shell, controller, header, message);
        dialog.create();
        dialog.open();
    }

    /**
     * Shows an error dialog
     * @param header
     * @param message
     * @param error
     */
    public void showErrorDialog(final String header, final String message, final String error) {
        final DialogError dialog = new DialogError(shell, controller, header, message, error);
        dialog.create();
        dialog.open();
    }

    /**
     * Shows an error dialog
     * @param header
     * @param message
     * @param t
     */
    public void showErrorDialog(final String header, final String message, final Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        final String trace = sw.toString();
        showErrorDialog(header, message, trace);
    }

    /**
     * Shows an input dialog for ordering data items
     * @param header
     * @param text
     * @param type
     * @param values
     * @return
     */
    public String[] showOrderValuesDialog(final String header, final String text, final DataType<?> type, final String[] values) {

        // Open dialog
        DialogOrderSelection dlg = new DialogOrderSelection(shell, values, type, controller);
        if (dlg.open() == Window.OK) {
            return dlg.getResult();
        } else {
            return null;
        }
    }

    /**
     * Shows an input dialog for selecting formats string for data types
     * @param shell
     * @param header
     * @param text
     * @param preselected Preselected format string, can be null
     * @param description
     * @param values
     * @return
     */
    public String showFormatInputDialog(final Shell shell, final String header, final String text, final String preselected, final DataTypeDescription<?> description, final Collection<String> values) {

        // Check
        if (!description.hasFormat()) { throw new RuntimeException("This dialog can only be used for data types with format"); }

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
                        type = description.newInstance(arg0);
                    }
                } catch (final Exception e) {
                    return Resources.getMessage("MainWindow.11"); //$NON-NLS-1$
                }
                for (final String value : values) {
                    if (!type.isValid(value)) { return Resources.getMessage("MainWindow.13"); //$NON-NLS-1$
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
     * Shows a help dialog
     * @param id
     */
    public void showHelpDialog(String id) {
        final DialogHelp dialog = new DialogHelp(shell, controller, id);
        dialog.create();
        dialog.open();
    }

    /**
     * Shows an info dialog
     * @param header
     * @param text
     */
    public void showInfoDialog(final String header, final String text) {
        MessageDialog.openInformation(getShell(), header, text);
    }

    /**
     * Shows an input dialog
     * @param header
     * @param text
     * @param initial
     * @return
     */
    public String showInputDialog(final String header, final String text, final String initial) {

        final InputDialog dlg = new InputDialog(shell, header, text, initial, null);
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        } else {
            return null;
        }
    }

    /**
     * Shows a file open dialog
     * @param filter
     * @return
     */
    public String showOpenFileDialog(String filter) {
        final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { filter });
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    /**
     * Shows a progress dialog
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
     * Shows a query dialog for selecting a research subset
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
     * Shows a question dialog
     * @param header
     * @param text
     * @return
     */
    public boolean showQuestionDialog(final String header, final String text) {
        return MessageDialog.openQuestion(getShell(), header, text);
    }

    /**
     * Shows a file save dialog
     * @param filter
     * @return
     */
    public String showSaveFileDialog(String filter) {
        final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { filter });
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    /**
     * Shows a dialog for selecting privacy criteria
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
