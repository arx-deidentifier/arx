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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.FLASHLattice;
import org.deidentifier.flash.FLASHLattice.Anonymity;
import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import cern.colt.Arrays;

public class MainToolBar implements IView {

    private final ToolBar    toolBar;
    private final Controller controller;
    private Label            latticeLabel;
    private Label            appliedLabel;
    private Label            selectedLabel;
    private Model            model;
    private List<ToolItem>   toolitems;
    private static final int TOOL_BAR_HEIGHT         = 30;
    private static final int TOOL_BAR_LABEL_OFFSET_Y = 2;
    private static final int TOOL_BAR_LABEL_OFFSET_X = 10;

    public MainToolBar(final Shell parent, final Controller controller) {
        toolBar = new ToolBar(parent, SWT.FLAT);
        toolBar.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.controller = controller;
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(EventTarget.SELECTED_NODE, this);
        controller.addListener(EventTarget.OUTPUT, this);
        controller.addListener(EventTarget.RESULT, this);
        build();
        toolBar.pack();
    }

    private void build() {
        toolitems = new ArrayList<ToolItem>();

        ToolItem item;

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.9")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_new.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileNew();
            }
        });
        toolitems.add(item);

        toolitems.add(new ToolItem(toolBar, SWT.SEPARATOR));

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.11")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_load.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileOpen();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.13")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_save.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileSave();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.15")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_save_as.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileSaveAs();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolBar, SWT.SEPARATOR));

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.17")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_import_data.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileImportData();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.19")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_export_data.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExportData();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolBar, SWT.SEPARATOR));

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.21")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_import_hierarchy.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileImportHierarchy();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.23")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_export_hierarchy.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExportHierarchy();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolBar, SWT.SEPARATOR));

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.25")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("edit_anonymize.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditAnonymize();
            }
        });

        toolitems.add(item);

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.27")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("edit_create_hierarchy.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditCreateHierarchy();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolBar, SWT.SEPARATOR));

        item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.29")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("edit_settings.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditSettings();
            }
        });
        toolitems.add(item);
        toolitems.add(new ToolItem(toolBar, SWT.SEPARATOR));

        // Add status labels
        final ToolItem infoBase = new ToolItem(toolBar, SWT.SEPARATOR);
        final Composite infoComposite = new Composite(toolBar, SWT.NONE);
        infoComposite.setSize(5000, TOOL_BAR_HEIGHT);
        infoBase.setWidth(5000);
        infoBase.setControl(infoComposite);
        infoComposite.setLayout(null);

        selectedLabel = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        selectedLabel.setText(Resources.getMessage("MainToolBar.31")); //$NON-NLS-1$
        selectedLabel.pack();
        appliedLabel = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        appliedLabel.setText(Resources.getMessage("MainToolBar.32")); //$NON-NLS-1$
        appliedLabel.pack();
        latticeLabel = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        latticeLabel.setText(Resources.getMessage("MainToolBar.33")); //$NON-NLS-1$
        latticeLabel.pack();

        toolBar.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(final ControlEvent arg0) {
                labelLayout();
            }
        });
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns the number of anonymous nodes in the lattice
     * 
     * @param lattice
     * @return
     */
    private int getAnonymousCount(final FLASHLattice lattice) {
        int count = 0;
        for (final FLASHNode[] level : lattice.getLevels()) {
            for (final FLASHNode node : level) {
                if (node.isAnonymous() == Anonymity.ANONYMOUS) {
                    count++;
                }
            }
        }
        return count;
    }

    private void labelLayout() {
        int remaining = toolBar.getBounds().width;
        for (final ToolItem item : toolitems) {
            remaining -= item.getBounds().width;
        }
        int location = remaining - latticeLabel.getSize().x;
        latticeLabel.setLocation(location, TOOL_BAR_LABEL_OFFSET_Y);
        location -= appliedLabel.getSize().x + TOOL_BAR_LABEL_OFFSET_X;
        appliedLabel.setLocation(location, TOOL_BAR_LABEL_OFFSET_Y);
        location -= selectedLabel.getSize().x + TOOL_BAR_LABEL_OFFSET_X;
        selectedLabel.setLocation(location, TOOL_BAR_LABEL_OFFSET_Y);
    }

    @Override
    public void reset() {
        if (selectedLabel != null) {
            toolBar.setRedraw(false);
            selectedLabel.setText(""); //$NON-NLS-1$
            selectedLabel.pack();
            labelLayout();
            toolBar.setRedraw(true);
        }
        if (appliedLabel != null) {
            toolBar.setRedraw(false);
            appliedLabel.setText(""); //$NON-NLS-1$
            appliedLabel.pack();
            labelLayout();
            toolBar.setRedraw(true);
        }
        if (latticeLabel != null) {
            toolBar.setRedraw(false);
            latticeLabel.setText(""); //$NON-NLS-1$
            latticeLabel.pack();
            labelLayout();
            toolBar.setRedraw(true);
        }
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.target == EventTarget.SELECTED_NODE) {
            if (model.getSelectedNode() != null) {
                toolBar.setRedraw(false);
                selectedLabel.setText(Resources.getMessage("MainToolBar.3") + Arrays.toString(model.getSelectedNode().getTransformation())); //$NON-NLS-1$
                selectedLabel.pack();
                labelLayout();
                toolBar.setRedraw(true);
            }
        } else if (event.target == EventTarget.OUTPUT) {
            if (model.getOutputNode() != null) {
                toolBar.setRedraw(false);
                appliedLabel.setText(Resources.getMessage("MainToolBar.4") + Arrays.toString(model.getOutputNode().getTransformation())); //$NON-NLS-1$
                appliedLabel.pack();
                labelLayout();
                toolBar.setRedraw(true);
            }
        } else if (event.target == EventTarget.RESULT) {
            if (model.getResult() != null) {
                toolBar.setRedraw(false);
                latticeLabel.setText(Resources.getMessage("MainToolBar.5") + String.valueOf(getAnonymousCount(model.getResult().getLattice())) + Resources.getMessage("MainToolBar.6") + String.valueOf(model.getResult().getLattice().getSize())); //$NON-NLS-1$ //$NON-NLS-2$
                latticeLabel.pack();

                selectedLabel.setText(Resources.getMessage("MainToolBar.7")); //$NON-NLS-1$
                selectedLabel.pack();

                appliedLabel.setText(Resources.getMessage("MainToolBar.8")); //$NON-NLS-1$
                appliedLabel.pack();
                labelLayout();
                toolBar.setRedraw(true);
            }
        } else if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
        }
    }
}
