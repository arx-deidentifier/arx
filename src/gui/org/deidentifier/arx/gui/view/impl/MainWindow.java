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

package org.deidentifier.arx.gui.view.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IMainWindow;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.analyze.LayoutAnalyze;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.define.LayoutDefinition;
import org.deidentifier.arx.gui.view.impl.explore.LayoutExplore;
import org.deidentifier.arx.gui.view.impl.menu.DialogCriterionSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogQuery;
import org.deidentifier.arx.gui.view.impl.menu.DialogQueryResult;
import org.deidentifier.arx.gui.worker.Worker;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class MainWindow implements IMainWindow, IView {

    private static final String TITLE                     = Resources.getMessage("MainWindow.0"); //$NON-NLS-1$
    private static final String TAB_ANALYZE_DATA          = Resources.getMessage("MainWindow.1"); //$NON-NLS-1$
    private static final String TAB_DEFINE_TRANSFORMATION = Resources.getMessage("MainWindow.2"); //$NON-NLS-1$
    private static final String TAB_EXPLORE_SEARCHSPACE   = Resources.getMessage("MainWindow.3"); //$NON-NLS-1$

    private final Display       display;

    private final Shell         shell;

    private final Controller    controller;

    private final MainToolTip   tooltip;
    private final MainPopUp     popup;

    private final ComponentTitledFolder  root;

    public MainWindow() {

        // Init
        display = new Display();
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
        popup = new MainPopUp(shell);

        // Close listener
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                controller.actionMenuFileExit();
                event.doit = false;
            }
        });

        // Build menu
        new MainMenu(shell, controller);
        new MainToolBar(shell, controller);

        // Create shell
        shell.setLayout(SWTUtil.createGridLayout(1));

        // Create the tab folder
        root = new ComponentTitledFolder(shell, controller, null, "id-70");
        root.setLayoutData(SWTUtil.createFillGridData());
        
        // TODO: Remove? Fixes an SWT Bug!
        // root.setBackground(shell.getBackground());

        // Create the subviews
        Composite item1 = root.createItem(TAB_DEFINE_TRANSFORMATION, controller.getResources().getImage("perspective_define.png"));  //$NON-NLS-1$
        new LayoutDefinition(item1, controller);
        Composite item2 = root.createItem(TAB_EXPLORE_SEARCHSPACE, controller.getResources().getImage("perspective_explore.png"));  //$NON-NLS-1$
        new LayoutExplore(item2, controller);
        Composite item3 = root.createItem(TAB_ANALYZE_DATA, controller.getResources().getImage("perspective_analyze.png"));  //$NON-NLS-1$
        new LayoutAnalyze(item3, controller);
        
        // Now reset and disable
        controller.reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public MainPopUp getPopUp() {
        return popup;
    }

    @Override
    public Shell getShell() {
        return shell;
    }

    @Override
    public MainToolTip getToolTip() {
        return tooltip;
    }

    @Override
    public void reset() {
        root.setSelection(0);
        root.setEnabled(false);
    }

    @Override
    public void show() {
        shell.open();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (final Exception e) {
                controller.actionShowErrorDialog(Resources.getMessage("MainWindow.8"), Resources.getMessage("MainWindow.9") + e.getMessage() + Resources.getMessage("MainWindow.10")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                controller.getResources().getLogger().info(sw.toString());
            }
        }
        display.dispose();
    }

    @Override
    public String showDateFormatInputDialog(final String header,
                                            final String text,
                                            final Collection<String> dates) {

        // Validator
        final IInputValidator validator = new IInputValidator() {
            @Override
            public String isValid(final String arg0) {
                DateFormat f = null;
                try {
                    f = new SimpleDateFormat(arg0);
                } catch (final Exception e) {
                    return Resources.getMessage("MainWindow.11"); //$NON-NLS-1$
                }
                for (final String date : dates) {
                    try {
                        f.parse(date);
                    } catch (final Exception e) {
                        return Resources.getMessage("MainWindow.13"); //$NON-NLS-1$
                    }
                }
                return null;
            }
        };

        // Try to find a valid formatter
        String initial = ""; //$NON-NLS-1$
        for (final String format : controller.getResources().getDateFormats()) {
            if (validator.isValid(format) == null) {
                initial = format;
                break;
            }
        }

        // Input dialog
        final InputDialog dlg = new InputDialog(shell,
                                                header,
                                                text,
                                                initial,
                                                validator);

        // Return value
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void showErrorDialog(final String header, final String text) {
        MessageDialog.openError(getShell(), header, text);
    }

    @Override
    public void showInfoDialog(final String header, final String text) {
        MessageDialog.openInformation(getShell(), header, text);
    }

    @Override
    public String showInputDialog(final String header,
                                  final String text,
                                  final String initial) {

        final InputDialog dlg = new InputDialog(shell,
                                                header,
                                                text,
                                                initial,
                                                null);
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        } else {
            return null;
        }
    }

    @Override
    public String showOpenFileDialog(String filter) {
        final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { filter });
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    @Override
    public void showProgressDialog(final String text, final Worker<?> worker) {
        try {
            new ProgressMonitorDialog(shell).run(true, true, worker);
        } catch (final Exception e) {
            worker.setError(e);
        }
    }

    @Override
    public DialogQueryResult showQueryDialog(String query, Data data) {

        // Dialog
        final DialogQuery dialog = new DialogQuery(data, shell, query);
        dialog.create();
        if (dialog.open() != Window.OK) { return null; }
        else {return dialog.getResult();}
    }

    @Override
    public boolean showQuestionDialog(final String header, final String text) {
        return MessageDialog.openQuestion(getShell(), header, text);
    }

    @Override
    public String showSaveFileDialog(String filter) {
        final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { filter });
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    @Override
    public ModelExplicitCriterion showSelectCriterionDialog(List<ModelExplicitCriterion> others) {

        // Dialog
        final DialogCriterionSelection dialog = new DialogCriterionSelection(controller, shell, others);
        dialog.create();
        if (dialog.open() != Window.OK) { return null; }
        else {return dialog.getCriterion();}
    }

    @Override
    public void update(final ModelEvent event) {

        // Careful! In the main window, this is also called after editing the
        // project properties
        if (event.part == ModelPart.MODEL) {
            final Model model = (Model) event.data;
            shell.setText(TITLE + " - " + model.getName()); //$NON-NLS-1$
            root.setEnabled(true);
        }
    }
}
