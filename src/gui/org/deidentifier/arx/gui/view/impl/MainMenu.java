/*
 * ARX: Powerful Data Anonymization
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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements the global main menu
 * @author Fabian Prasser
 */
public class MainMenu implements IView {

    private static final String FILE_NAME = Resources.getMessage("MainMenu.0"); //$NON-NLS-1$
    private static final String EDIT_NAME = Resources.getMessage("MainMenu.1"); //$NON-NLS-1$
    private static final String HELP_NAME = Resources.getMessage("MainMenu.2"); //$NON-NLS-1$

    private Menu menu;
    /**
     * Creates a new instance
     * @param shell
     * @param controller
     */
    public MainMenu(final Shell shell, final Controller controller) {

        // Create Menu
        menu = new Menu(shell, SWT.BAR);

        /** File Menu */
        final MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
        final Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileMenuItem.setText(FILE_NAME);
        fileMenuItem.setMenu(fileMenu);

        final MenuItem newItem = new MenuItem(fileMenu, SWT.PUSH);
        newItem.setText(Resources.getMessage("MainMenu.3")); //$NON-NLS-1$
        newItem.setImage(controller.getResources().getImage("file_new.png")); //$NON-NLS-1$
        newItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileNew();
            }
        });

        new MenuItem(fileMenu, SWT.SEPARATOR);

        final MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
        openItem.setText(Resources.getMessage("MainMenu.5")); //$NON-NLS-1$
        openItem.setImage(controller.getResources().getImage("file_load.png")); //$NON-NLS-1$
        openItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileOpen();
            }
        });

        final MenuItem saveItem = new MenuItem(fileMenu, SWT.PUSH);
        saveItem.setText(Resources.getMessage("MainMenu.4")); //$NON-NLS-1$
        saveItem.setImage(controller.getResources().getImage("file_save.png")); //$NON-NLS-1$
        saveItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileSave();
            }
        });

        final MenuItem saveasItem = new MenuItem(fileMenu, SWT.PUSH);
        saveasItem.setText(Resources.getMessage("MainMenu.9")); //$NON-NLS-1$
        saveasItem.setImage(controller.getResources()
                                      .getImage("file_save_as.png")); //$NON-NLS-1$
        saveasItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileSaveAs();
            }
        });

        new MenuItem(fileMenu, SWT.SEPARATOR);

        final MenuItem importItem = new MenuItem(fileMenu, SWT.PUSH);
        importItem.setText(Resources.getMessage("MainMenu.11")); //$NON-NLS-1$
        importItem.setImage(controller.getResources()
                                      .getImage("file_import_data.png")); //$NON-NLS-1$
        importItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileImportData();
            }
        });

        final MenuItem exportItem = new MenuItem(fileMenu, SWT.PUSH);
        exportItem.setText(Resources.getMessage("MainMenu.13")); //$NON-NLS-1$
        exportItem.setImage(controller.getResources()
                                      .getImage("file_export_data.png")); //$NON-NLS-1$
        exportItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExportData();
            }
        });

        new MenuItem(fileMenu, SWT.SEPARATOR);

        final MenuItem importHier = new MenuItem(fileMenu, SWT.PUSH);
        importHier.setText(Resources.getMessage("MainMenu.15")); //$NON-NLS-1$
        importHier.setImage(controller.getResources()
                                      .getImage("file_import_hierarchy.png")); //$NON-NLS-1$
        importHier.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileImportHierarchy();
            }
        });

        final MenuItem exportHier = new MenuItem(fileMenu, SWT.PUSH);
        exportHier.setText(Resources.getMessage("MainMenu.17")); //$NON-NLS-1$
        exportHier.setImage(controller.getResources()
                                      .getImage("file_export_hierarchy.png")); //$NON-NLS-1$
        exportHier.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExportHierarchy();
            }
        });

        new MenuItem(fileMenu, SWT.SEPARATOR);

        final MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
        exitItem.setText(Resources.getMessage("MainMenu.19")); //$NON-NLS-1$
        exitItem.setImage(controller.getResources().getImage("exit.png")); //$NON-NLS-1$
        exitItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExit();
            }
        });

        shell.setMenuBar(menu);

        /** Edit Menu */
        final MenuItem editMenuItem = new MenuItem(menu, SWT.CASCADE);
        final Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
        editMenuItem.setText(EDIT_NAME);
        editMenuItem.setMenu(editMenu);

        final MenuItem editAnonymize = new MenuItem(editMenu, SWT.PUSH);
        editAnonymize.setText(Resources.getMessage("MainMenu.21")); //$NON-NLS-1$
        editAnonymize.setImage(controller.getResources()
                                         .getImage("edit_anonymize.png")); //$NON-NLS-1$
        editAnonymize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditAnonymize();
            }
        });

        new MenuItem(editMenu, SWT.SEPARATOR);

        final MenuItem editCreateHierarchy = new MenuItem(editMenu, SWT.PUSH);
        editCreateHierarchy.setText(Resources.getMessage("MainMenu.23")); //$NON-NLS-1$
        editCreateHierarchy.setImage(controller.getResources()
                                               .getImage("edit_create_hierarchy.png")); //$NON-NLS-1$
        editCreateHierarchy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditCreateHierarchy();
            }
        });

        new MenuItem(editMenu, SWT.SEPARATOR);

        final MenuItem editSettings = new MenuItem(editMenu, SWT.PUSH);
        editSettings.setText(Resources.getMessage("MainMenu.25")); //$NON-NLS-1$
        editSettings.setImage(controller.getResources()
                                        .getImage("edit_settings.png")); //$NON-NLS-1$
        editSettings.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditSettings();
            }
        });

        /** Help Menu */
        final MenuItem helpMenuItem = new MenuItem(menu, SWT.CASCADE);
        final Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
        helpMenuItem.setText(HELP_NAME);
        helpMenuItem.setMenu(helpMenu);

        final MenuItem updateItem = new MenuItem(helpMenu, SWT.PUSH);
        updateItem.setText(Resources.getMessage("MainMenu.27")); //$NON-NLS-1$
        updateItem.setImage(controller.getResources().getImage("help.png")); //$NON-NLS-1$
        updateItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuHelpHelp();
            }
        });

        final MenuItem infoItem = new MenuItem(helpMenu, SWT.PUSH);
        infoItem.setText(Resources.getMessage("MainMenu.29")); //$NON-NLS-1$
        infoItem.setImage(controller.getResources().getImage("information.png")); //$NON-NLS-1$
        infoItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuHelpAbout();
            }
        });

        final MenuItem debugItem = new MenuItem(helpMenu, SWT.PUSH);
        debugItem.setText("Debug"); //$NON-NLS-1$
        debugItem.setImage(controller.getResources().getImage("information.png")); //$NON-NLS-1$
        debugItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuHelpDebug();
            }
        });
        debugItem.setEnabled(false);
    }
    
    /**
     * Enable/disable a menu
     * @param menu
     * @param text
     * @param enabled
     */
    private void setEnabled(Menu menu, String text, boolean enabled) {
        for (MenuItem item : menu.getItems()){
            if (item.getText().equals(text)) {
                item.setEnabled(enabled);
            } else {
                if (item.getMenu() != null){
                    setEnabled(item.getMenu(), text, enabled);
                }
            }
        }
    }

    @Override
    public void dispose() {
        // Nothing to do for now
    }

    @Override
    public void reset() {
        // Nothing to do for now
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            if (event.data != null && (event.data instanceof Model)){
                setEnabled(menu, "Debug", ((Model)event.data).isDebugEnabled());
            }
        }
    }
}
