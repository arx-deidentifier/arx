/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.impl;

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class MainMenu {

    private static final String FILE_NAME = Resources.getMessage("MainMenu.0"); //$NON-NLS-1$
    private static final String EDIT_NAME = Resources.getMessage("MainMenu.1"); //$NON-NLS-1$
    private static final String HELP_NAME = Resources.getMessage("MainMenu.2"); //$NON-NLS-1$

    public MainMenu(final Shell shell, final Controller controller) {

        // Create Menu
        final Menu menuBar = new Menu(shell, SWT.BAR);

        /** File Menu */
        final MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
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

        shell.setMenuBar(menuBar);

        /** Edit Menu */
        final MenuItem editMenuItem = new MenuItem(menuBar, SWT.CASCADE);
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
        final MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
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

    }
}
