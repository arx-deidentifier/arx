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

package org.deidentifier.arx.gui.view.impl;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.Model.Perspective;
import org.deidentifier.arx.gui.model.ModelAnonymizationConfiguration;
import org.deidentifier.arx.gui.model.ModelAuditTrailEntry;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.resources.Charsets;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IValidator;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.attributes.LayoutAttributes;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.define.LayoutDefinition;
import org.deidentifier.arx.gui.view.impl.explore.LayoutExplore;
import org.deidentifier.arx.gui.view.impl.menu.DialogAbout;
import org.deidentifier.arx.gui.view.impl.menu.DialogAnonymization;
import org.deidentifier.arx.gui.view.impl.menu.DialogAuditTrail;
import org.deidentifier.arx.gui.view.impl.menu.DialogClassificationConfiguration;
import org.deidentifier.arx.gui.view.impl.menu.DialogComboDoubleSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogComboSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogCriterionSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogCriterionUpdate;
import org.deidentifier.arx.gui.view.impl.menu.DialogDebug;
import org.deidentifier.arx.gui.view.impl.menu.DialogError;
import org.deidentifier.arx.gui.view.impl.menu.DialogFindReplace;
import org.deidentifier.arx.gui.view.impl.menu.DialogHelp;
import org.deidentifier.arx.gui.view.impl.menu.DialogMultiSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogOrderSelection;
import org.deidentifier.arx.gui.view.impl.menu.DialogQuery;
import org.deidentifier.arx.gui.view.impl.menu.DialogQueryResult;
import org.deidentifier.arx.gui.view.impl.menu.DialogTopBottomCoding;
import org.deidentifier.arx.gui.view.impl.risk.LayoutRisks;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility;
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

    /** Controller */
    private final Controller            controller;

    /** View */
    private final Display               display;
    /** View */
    private final Shell                 shell;
    /** View */
    private final AbstractMenu          menu;
    /** View */
    private final ComponentTitledFolder root;
    /** View */
    private final LayoutExplore         layoutExplore;

    /**
     * Creates a new instance.
     *
     * @param display
     * @param monitor
     */
    public MainWindow(Display display, Monitor monitor) {

        // Init
        this.display = display;
        this.shell = new Shell(display);

        // Build controller
        controller = new Controller(this);
        controller.addListener(ModelPart.MODEL, this);

        // Style
        shell.setImages(Resources.getIconSet(display));
        shell.setText(Resources.getMessage("MainWindow.0")); //$NON-NLS-1$
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
        List<MainMenuItem> items = getMenu();
        menu = new MainMenu(shell, controller, items);
        new MainToolBar(shell, controller, items);

        // Create shell
        shell.setLayout(SWTUtil.createGridLayout(1));

        // Create the tab folder
        Map<Composite, String> helpids = new HashMap<Composite, String>();
        root = new ComponentTitledFolder(shell, controller, null, "id-70", helpids); //$NON-NLS-1$
        root.setLayoutData(SWTUtil.createFillGridData());

        // Create the subviews
        Composite item1 = root.createItem(Resources.getMessage("MainWindow.2"), controller.getResources().getManagedImage("perspective_define.png")); //$NON-NLS-1$ //$NON-NLS-2$
        helpids.put(item1, "id-3"); //$NON-NLS-1$
        new LayoutDefinition(item1, controller);
        Composite item2 = root.createItem(Resources.getMessage("MainWindow.3"), controller.getResources().getManagedImage("perspective_explore.png")); //$NON-NLS-1$ //$NON-NLS-2$
        helpids.put(item2, "id-4"); //$NON-NLS-1$
        this.layoutExplore = new LayoutExplore(item2, controller);
        Composite item3 = root.createItem(Resources.getMessage("MainWindow.1"), controller.getResources().getManagedImage("perspective_analyze.png")); //$NON-NLS-1$ //$NON-NLS-2$
        helpids.put(item3, "help.utility.overview"); //$NON-NLS-1$
        new LayoutUtility(item3, controller);
        Composite item4 = root.createItem(Resources.getMessage("MainWindow.4"), controller.getResources().getManagedImage("perspective_risk.png")); //$NON-NLS-1$ //$NON-NLS-2$
        helpids.put(item4, "help.risk.overview"); //$NON-NLS-1$
        new LayoutRisks(item4, controller);
        Composite item5 = root.createItem(Resources.getMessage("MainWindow.21"), controller.getResources().getManagedImage("perspective_attributes.png")); //$NON-NLS-1$ //$NON-NLS-2$
        helpids.put(item5, "help.attributes.overview"); //$NON-NLS-1$
        new LayoutAttributes(item5, controller);

        // Hack to update visualizations
        root.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                switch (root.getSelectionIndex()) {
                    case 0: 
                        controller.getModel().setPerspective(Perspective.CONFIGURATION);
                        break;
                    case 1: 
                        controller.getModel().setPerspective(Perspective.EXPLORATION);
                        break;
                    case 2: 
                        controller.getModel().setPerspective(Perspective.ANALYSIS);
                        break;
                    case 3: 
                        controller.getModel().setPerspective(Perspective.RISK);
                        break;
                    case 4: 
                        controller.getModel().setPerspective(Perspective.ATTRIBUTES);
                        break;
                }
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
                controller.update(new ModelEvent(this, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
        });
        
        // Now reset and disable
        controller.reset();
    }

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
     * Shows a dialog for selecting privacy criteria.
     *
     * @param criteria
     * @return
     */
    public ModelCriterion showAddCriterionDialog(Model model,
                                                 List<ModelCriterion> criteria) {

        // Dialog
        final DialogCriterionUpdate dialog = new DialogCriterionUpdate(controller, shell, criteria, model, true);
        dialog.create();
        if (dialog.open() != Window.OK) {
            return null;
        } else {
            return dialog.getCriterion();
        }
    }

    /**
     * Shows the audit trail
     */
    public void showAuditTrail(List<ModelAuditTrailEntry> auditTrail) {
        DialogAuditTrail dialog = new DialogAuditTrail(shell, auditTrail);
        dialog.create();
        dialog.open();
    }
    /**
     * Shows an input dialog for selecting a charset.
     *
     * @param shell
     * @return
     */
    public Charset showCharsetInputDialog(final Shell shell) {

        // Validator
        final IInputValidator validator = new IInputValidator() {
            @Override
            public String isValid(final String arg0) {
                return null;
            }
        };

        // Extract list of formats
        List<String> charsets = new ArrayList<String>();
        for (String charset : Charsets.getNamesOfAvailableCharsets()) {
            charsets.add(charset);
        }

        // Open dialog
        final DialogComboSelection dlg = new DialogComboSelection(shell, Resources.getMessage("MainWindow.19"), //$NON-NLS-1$
                                                                  Resources.getMessage("MainWindow.20"), //$NON-NLS-1$
                                                                  charsets.toArray(new String[] {}),
                                                                  Charsets.getNameOfDefaultCharset(),
                                                                  validator);

        // Return value
        if (dlg.open() == Window.OK) {
            return Charsets.getCharsetForName(dlg.getValue());
        } else {
            return null;
        }
    }
    
    /**
     * Shows a preference dialog for editing parameter values of classification
     * configurations.
     * @param config 
     * 
     * @param model
     */
    public ARXClassificationConfiguration<?> showClassificationConfigurationDialog(ARXClassificationConfiguration<?> config) {
        DialogClassificationConfiguration dialog = new DialogClassificationConfiguration(shell, config);
        dialog.open();
        return dialog.getResult();
    }

    /**
     * Shows a dialog for configuring privacy criteria.
     *
     * @param criteria
     * @param criterion 
     * @return
     */
    public void showConfigureCriterionDialog(Model model, List<ModelCriterion> criteria, ModelCriterion criterion) {
        DialogCriterionUpdate dialog = new DialogCriterionUpdate(controller, shell, criteria, model, false, criterion);
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
     * @param throwable
     */
    public void showErrorDialog(final String message, final Throwable throwable) {
        showErrorDialog(this.shell, message, throwable);
    }
    
    /**
     * Shows a find & replace dialog
     * @param handle
     * @param column
     * @return A pair containing the string to be found and the string with which it is to be replaced,
     *         <code>null</code> if cancel was pressed.
     */
    public Pair<String, String> showFindReplaceDialog(Model model, DataHandle handle, int column) {
        DialogFindReplace dialog = new DialogFindReplace(shell, model, handle, column);
        dialog.create();
        dialog.open();
        return dialog.getValue();
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
    public String[] showFormatInputDialog(final Shell shell, final String header, final String text, final String preselected, final Locale locale, final DataTypeDescription<?> description, final Collection<String> values) {
        
        // Check
        if (!description.hasFormat()) {
            throw new RuntimeException(Resources.getMessage("MainWindow.6")); //$NON-NLS-1$
        }

        // Init
        final String DEFAULT = Resources.getMessage("MainWindow.7"); //$NON-NLS-1$
        
        // Validator
        final IValidator<String[]> validator = new IValidator<String[]>() {
            @Override
            public String isValid(final String[] state) {
                DataType<?> type;
                try {
                    if (state[0].equals(DEFAULT) || state[1] == null) {
                        type = description.newInstance();
                    } else {
                        type = description.newInstance(state[0], getLocale(state[1]));
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
        String initial1 = ""; //$NON-NLS-1$
        String initial2 = locale == null ? "" : locale.getLanguage().toUpperCase();
        if (preselected != null && validator.isValid(new String[]{preselected, initial2}) == null) {
            initial1 = preselected;
        } else if (validator.isValid(new String[]{DEFAULT, initial2}) == null) {
            initial1 = DEFAULT;
        } else {
            for (final String format : description.getExampleFormats()) {
                if (validator.isValid(new String[]{format, initial2}) == null) {
                    initial1 = format;
                    break;
                }
            }
        }
        // Extract list of formats
        List<String> formats = new ArrayList<String>();
        formats.add(DEFAULT);
        formats.addAll(description.getExampleFormats());
        
        // Extract list of locales
        Set<String> set = new HashSet<String>();
        for (Locale _locale : Locale.getAvailableLocales()) {
            set.add(_locale.getLanguage().toUpperCase());
        }
        List<String> locales = new ArrayList<>(set);
        Collections.sort(locales);

        // Create dialog
        DialogComboDoubleSelection dlg = new DialogComboDoubleSelection(shell, header, text, 
                                                                        formats.toArray(new String[] {}), initial1, 
                                                                        locales.toArray(new String[] {}), initial2,
                                                                        validator);

        // Open and return value
        if (dlg.open() == Window.OK) {
            return new String[]{dlg.getValue1(), dlg.getValue2()};
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
    	    DialogHelp dialog = new DialogHelp(shell, controller, id);
    	    dialog.open();
    	} catch (Exception e) {
    		this.showErrorDialog(Resources.getMessage("MainWindow.12"), e); //$NON-NLS-1$
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
        return showInputDialog(shell, header, text, initial, null);
    }

    /**
     * Shows an input dialog.
     *
     * @param shell
     * @param header
     * @param text
     * @param initial
     * @param validator
     * @return
     */
    public String showInputDialog(final Shell shell, final String header, final String text, final String initial, final IInputValidator validator) {
        final InputDialog dlg = new InputDialog(shell, header, text, initial, validator);
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        } else {
            return null;
        }
    }
    /**
     * Shows a dialog for anonymization parameters
     * @param model
     * @return Returns the parameters selected by the user. Returns a pair. 
     *         First: max. time per iteration. Second: min. records per iteration.
     */
    public ModelAnonymizationConfiguration showLocalAnonymizationDialog(Model model) {
        DialogAnonymization dialog = new DialogAnonymization(shell, model);
        dialog.create();
        dialog.open();
        return dialog.getResult();
    }

    /**
     * Shows a dialog that allows selecting multiple elements
     * @param shell
     * @param title
     * @param text
     * @param elements
     * @param selected
     * @return
     */
    public List<String> showMultiSelectionDialog(Shell shell,
                                                 String title,
                                                 String text,
                                                 List<String> elements,
                                                 List<String> selected) {

        // Open dialog
        DialogMultiSelection dlg = new DialogMultiSelection(shell, title, text, elements, selected);
        if (dlg.open() == Window.OK) {
            return dlg.getSelectedItems();
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
        String file = dialog.open();
        if (file == null) {
            return null;
        } else if (!new File(file).exists()) {
            showInfoDialog(shell, Resources.getMessage("MainWindow.5"), Resources.getMessage("MainWindow.14")); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } else {
            return file;
        }
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
     * @param criteria
     * @return
     */
    public ModelExplicitCriterion showSelectCriterionDialog(List<ModelExplicitCriterion> criteria) {

        // Dialog
        final DialogCriterionSelection dialog = new DialogCriterionSelection(controller, shell, criteria);
        dialog.create();
        if (dialog.open() != Window.OK) {
            return null;
        } else {
            return dialog.getCriterion();
        }
    }

    /**
     * Shows a top/bottom coding dialog
     * @param type
     * @return A pair containing the bottom value + inclusive and the top value + inclusive.
     *         Either bottom or top may be <code>null</code> if they have not been defined. The overall pair may be
     *         <code>null</code> if cancel was pressed.
     */
    public Pair<Pair<String, Boolean>, Pair<String, Boolean>> showTopBottomCodingDialog(DataType<?> type) {
        DialogTopBottomCoding dialog = new DialogTopBottomCoding(shell, type);
        dialog.create();
        dialog.open();
        return dialog.getValue();
    }
    
    @Override
    public void update(final ModelEvent event) {

        // Careful! In the main window, this is also called after editing the project properties
        if (event.part == ModelPart.MODEL) {
            final Model model = (Model) event.data;
            shell.setText(Resources.getMessage("MainWindow.0") + " - " + model.getName()); //$NON-NLS-1$ //$NON-NLS-2$
            root.setEnabled(true);
            menu.update(event);
        }
    }
    
    /**
     * Returns the local for the given isoLanguage
     * @param isoLanguage
     * @return
     */
    private Locale getLocale(String isoLanguage) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getLanguage().toUpperCase().equals(isoLanguage.toUpperCase())) {
                return locale;
            }
        }
        throw new IllegalStateException("Unknown locale");
    }

    /**
     * Creates the global menu
     * @return
     */
    private List<MainMenuItem> getMenu() {
        
        List<MainMenuItem> menu = new ArrayList<MainMenuItem>();

        menu.add(getMenuFile());
        menu.add(getMenuEdit());
        menu.add(getMenuView());
        menu.add(getMenuHelp());
        
        return menu;
    }

    /**
     * Creates the edit menu
     * @return
     */
    private MainMenuItem getMenuEdit() {
        
        List<MainMenuItem> items = new ArrayList<MainMenuItem>();
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.21"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_anonymize.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuEditAnonymize(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.39"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("cross.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuEditReset(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });
        
        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.41"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_create_hierarchy.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionMenuEditInitializeHierarchy(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getSelectedAttribute() != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.42"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_create_hierarchy.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionMenuEditCreateTopBottomCodingHierarchy(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getSelectedAttribute() != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.23"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_create_hierarchy.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuEditCreateHierarchy(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getSelectedAttribute() != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.30"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_find_replace.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionMenuEditFindReplace(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getSelectedAttribute() != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.31"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_audit_trail.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionShowAuditTrail(); }
            public boolean isEnabled(Model model) { 
                return model != null;
            }
        });

        items.add(new MainMenuSeparator());

        List<MainMenuItem> subset = new ArrayList<MainMenuItem>();

        subset.add(new MainMenuItem(Resources.getMessage("SubsetDefinitionView.1"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("page_white.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionSubsetNone(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null;
            }
        });
        
        subset.add(new MainMenuItem(Resources.getMessage("SubsetDefinitionView.2"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("page_white_text.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionSubsetAll(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null;
            }
        });
        
        subset.add(new MainMenuItem(Resources.getMessage("SubsetDefinitionView.3"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("disk.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionSubsetFile(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null;
            }
        });
        
        subset.add(new MainMenuItem(Resources.getMessage("SubsetDefinitionView.4"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("find.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionSubsetQuery(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null;
            }
        });

        items.add(new MainMenuGroup(Resources.getMessage("MainMenu.35"), subset) { //$NON-NLS-1$
            public boolean isEnabled(Model model) {
                return model != null && model.getInputConfig() != null && model.getInputConfig().getInput() != null;
            }  
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.37"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("apply.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionApplySelectedTransformation(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getResult() != null && model.getSelectedNode() != null && !model.getProcessStatistics().isLocalTransformation();
            }
        });
        
        items.add(new MainMenuSeparator());
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.25"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("edit_settings.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuEditSettings(); }
            public boolean isEnabled(Model model) { 
                return model != null;
            }
        });

        return new MainMenuGroup(Resources.getMessage("MainMenu.1"), items) { //$NON-NLS-1$
            public boolean isEnabled(Model model) {
                return true;
            }  
        };
    }

    /**
     * Creates the file menu
     * @return
     */
    private MainMenuItem getMenuFile() {
        
        List<MainMenuItem> items = new ArrayList<MainMenuItem>();
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.3"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_new.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileNew(); }
            public boolean isEnabled(Model model) { return true; }
        });

        items.add(new MainMenuSeparator());
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.5"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_load.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileOpen(); }
            public boolean isEnabled(Model model) { return true; }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.4"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_save.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileSave(); }
            public boolean isEnabled(Model model) { return model != null; }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.9"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_save_as.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileSaveAs(); }
            public boolean isEnabled(Model model) { return model != null; }
        });
        
        items.add(new MainMenuSeparator());
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.11"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_import_data.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileImportData(); }
            public boolean isEnabled(Model model) { return model != null && model.getPerspective() == Perspective.CONFIGURATION; }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.13"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_export_data.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileExportData(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getOutput() != null && model.getPerspective() == Perspective.ANALYSIS;
            }
        });

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.43"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_create_certificate.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileCreateCertificate(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getOutput() != null && model.getPerspective() == Perspective.ANALYSIS;
            }
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.15"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_import_hierarchy.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileImportHierarchy(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getSelectedAttribute() != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.17"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("file_export_hierarchy.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuFileExportHierarchy(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.getSelectedAttribute() != null && model.getPerspective() == Perspective.CONFIGURATION;
            }
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.19"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("exit.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionMenuFileExit(); }
            public boolean isEnabled(Model model) { 
                return true;
            }
        });
        
        return new MainMenuGroup(Resources.getMessage("MainMenu.0"), items) { //$NON-NLS-1$
            public boolean isEnabled(Model model) {
                return true;
            }  
        };
    }

    /**
     * Creates the help menu
     * @return
     */
    private MainMenuItem getMenuHelp() {


        List<MainMenuItem> items = new ArrayList<MainMenuItem>();
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.27"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("help.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { controller.actionMenuHelpHelp(); }
            public boolean isEnabled(Model model) { return true; }
        });

        items.add(new MainMenuSeparator());
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.29"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("information.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionMenuHelpAbout(); }
            public boolean isEnabled(Model model) { return true; }
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.32"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("information.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { controller.actionMenuHelpDebug(); }
            public boolean isEnabled(Model model) { 
                return model != null && model.isDebugEnabled(); 
            }
        });
        
        return new MainMenuGroup(Resources.getMessage("MainMenu.2"), items) { //$NON-NLS-1$
            public boolean isEnabled(Model model) {
                return true;
            }  
        };
    }

    /**
     * Creates the help menu
     * @return
     */
    private MainMenuItem getMenuView() {


        List<MainMenuItem> items = new ArrayList<MainMenuItem>();
        
        items.add(new MainMenuItem(Resources.getMessage("MainWindow.2"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("perspective_define.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { 
                root.setSelection(0);
                controller.getModel().setPerspective(Perspective.CONFIGURATION);
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { return model != null && model.getPerspective() != Perspective.CONFIGURATION; }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainWindow.3"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("perspective_explore.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { 
                root.setSelection(1);
                controller.getModel().setPerspective(Perspective.EXPLORATION);
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { return model != null && model.getPerspective() != Perspective.EXPLORATION; }
        });

        items.add(new MainMenuItem(Resources.getMessage("MainWindow.1"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("perspective_analyze.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { 
                root.setSelection(2);
                controller.getModel().setPerspective(Perspective.ANALYSIS);
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { return model != null && model.getPerspective() != Perspective.ANALYSIS; }
        });

        items.add(new MainMenuItem(Resources.getMessage("MainWindow.4"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("perspective_risk.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { 
                root.setSelection(3);
                controller.getModel().setPerspective(Perspective.RISK);
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { return model != null && model.getPerspective() != Perspective.RISK; }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainWindow.21"), //$NON-NLS-1$
                                   controller.getResources().getManagedImage("perspective_attributes.png"), //$NON-NLS-1$
                                   false) {
            public void action(Controller controller) { 
                root.setSelection(4);
                controller.getModel().setPerspective(Perspective.ATTRIBUTES);
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { return model != null && model.getPerspective() != Perspective.ATTRIBUTES; }
        });

        items.add(new MainMenuSeparator());

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.34") + " " + Resources.getMessage("ExploreView.0"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                   controller.getResources().getManagedImage("explore_lattice.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { 
                layoutExplore.showLattice(); 
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { 
                return model != null && 
                       model.getPerspective() == Perspective.EXPLORATION && 
                       model.getResult() != null &&
                       !layoutExplore.isShowLattice(); 
            }
        });
        
        items.add(new MainMenuItem(Resources.getMessage("MainMenu.34") + " " + Resources.getMessage("ExploreView.2"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                   controller.getResources().getManagedImage("explore_list.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { 
                layoutExplore.showList();
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { 
                return model != null && 
                       model.getPerspective() == Perspective.EXPLORATION && 
                       model.getResult() != null &&
                       !layoutExplore.isShowList();
            }
        });

        items.add(new MainMenuItem(Resources.getMessage("MainMenu.34") + " " + Resources.getMessage("ExploreView.3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                   controller.getResources().getManagedImage("explore_tiles.png"), //$NON-NLS-1$
                                   true) {
            public void action(Controller controller) { 
                layoutExplore.showTiles(); 
                controller.update(new ModelEvent(controller, ModelPart.SELECTED_PERSPECTIVE, controller.getModel().getPerspective()));
            }
            public boolean isEnabled(Model model) { 
                return model != null && 
                       model.getPerspective() == Perspective.EXPLORATION && 
                       model.getResult() != null &&
                       !layoutExplore.isShowTiles();
            }
        });

        
        return new MainMenuGroup(Resources.getMessage("MainMenu.33"), items) { //$NON-NLS-1$
            public boolean isEnabled(Model model) {
                return true;
            }  
        };
    }

}
