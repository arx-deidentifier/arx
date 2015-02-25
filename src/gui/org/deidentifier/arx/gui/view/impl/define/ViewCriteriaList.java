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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelExplicitCriterion;
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
import org.deidentifier.arx.gui.model.ModelRiskBasedCriterion;
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This class displays a list of all defined privacy criteria.
 *
 * @author fabian
 */
public class ViewCriteriaList implements IView {

    /** Controller */
    private Controller     controller;

    /** Model */
    private Model          model = null;

    /** View */
    private Table          table;
    /** View */
    private TableColumn    column1;
    /** View */
    private TableColumn    column2;
    /** View */
    private TableColumn    column3;
    /** View */
    private Composite      root;
    /** View */
    private Image          symbolL;
    /** View */
    private Image          symbolT;
    /** View */
    private Image          symbolK;
    /** View */
    private Image          symbolD;
    /** View */
    private final ToolItem buttonAdd;
    /** View */
    private final ToolItem buttonCross;
    /** View */
    private final ToolItem buttonUp;
    /** View */
    private final ToolItem buttonDown;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewCriteriaList(final Composite parent, final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        
        this.symbolL = controller.getResources().getImage("symbol_l.png"); //$NON-NLS-1$
        this.symbolT = controller.getResources().getImage("symbol_t.png"); //$NON-NLS-1$
        this.symbolK = controller.getResources().getImage("symbol_k.png"); //$NON-NLS-1$
        this.symbolD = controller.getResources().getImage("symbol_d.png"); //$NON-NLS-1$
        
        ComponentTitledFolderButton bar = new ComponentTitledFolderButton("id-80");
        bar.add(Resources.getMessage("CriterionDefinitionView.80"), 
                controller.getResources().getImage("cross.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        actionCriterionAdd();
                    }
                });
        bar.add(Resources.getMessage("CriterionDefinitionView.59"), 
                controller.getResources().getImage("cross.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        ModelCriterion c = getSelectedCriterion();
                        if (c != null) {
                            controller.actionCriterionEnable(c);
                        }
                    }
                });
        bar.add(Resources.getMessage("CriterionDefinitionView.57"), 
                controller.getResources().getImage("bullet_arrow_up.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        ModelCriterion c = getSelectedCriterion();
                        if (c != null) {
                            controller.actionCriterionPush(c);
                        }
                    }
                });
        
        bar.add(Resources.getMessage("CriterionDefinitionView.58"), 
                controller.getResources().getImage("bullet_arrow_down.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        ModelCriterion c = getSelectedCriterion();
                        if (c != null) {
                            controller.actionCriterionPull(c);
                        }
                    }
                });

        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).minSize(1, 150).create());
        root = folder.createItem(Resources.getMessage("CriterionSelectionDialog.4"), null);
        root.setLayout(new FillLayout());
        folder.setSelection(0);
        
        buttonCross = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.59"));
        buttonUp = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.57"));
        buttonDown = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.58"));
        buttonAdd = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.80"));

        table = new Table(root, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        table.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                updateButtons();
            }
        });

        column1 = new TableColumn(table, SWT.NONE);
        column1.setText("");
        column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("CriterionSelectionDialog.2")); //$NON-NLS-1$
        column3 = new TableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("CriterionSelectionDialog.3")); //$NON-NLS-1$

        column1.pack();
        column2.pack();
        column3.pack();
        
        updateButtons();
        reset();
    }
    
    @Override
    public void dispose() {
        this.controller.removeListener(this);
        this.symbolL.dispose();
        this.symbolT.dispose();
        this.symbolK.dispose();
        this.symbolD.dispose();
    }

    @Override
    public void reset() {
        root.setRedraw(false);
        if (table != null) table.removeAll();
        if (column1 != null) column1.pack();
        if (column2 != null) column2.pack();
        if (column3 != null) column3.pack();
        root.setRedraw(true);
        SWTUtil.disable(root);
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
        } 
        
        if (event.part == ModelPart.CRITERION_DEFINITION ||
            event.part == ModelPart.ATTRIBUTE_TYPE ||
            event.part == ModelPart.MODEL) {
            if (model!=null) {
                root.setRedraw(false);
                table.removeAll();
                if (model.getKAnonymityModel().isActive() && model.getKAnonymityModel().isEnabled()) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[] { "", model.getKAnonymityModel().toString(), "" });
                    item.setImage(0, symbolK);
                    item.setData(model.getKAnonymityModel());
                }
    
                if (model.getDPresenceModel().isActive() && model.getDPresenceModel().isEnabled()) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[] { "", model.getDPresenceModel().toString(), "" });
                    item.setImage(0, symbolD);
                    item.setData(model.getDPresenceModel());
                }
    
                for (ModelLDiversityCriterion c : model.getLDiversityModel().values()) {
                    if (c.isActive() && c.isEnabled()) {
                        TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(new String[] { "", c.toString(), c.getAttribute() });
                        item.setImage(0, symbolL);
                        item.setData(c);
                    }
                }
    
                for (ModelTClosenessCriterion c : model.getTClosenessModel().values()) {
                    if (c.isActive() && c.isEnabled()) {
                        TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(new String[] { "", c.toString(), c.getAttribute() });
                        item.setImage(0, symbolT);
                        item.setData(c);
                    }
                }

                for (ModelRiskBasedCriterion c : model.getRiskBasedModel()) {
                    if (c.isActive() && c.isEnabled()) {
                        TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(new String[] { "", c.toString(), "" });
                        item.setData(c);
                    }
                }
                column1.pack();
                column2.pack();
                column3.pack();
                updateButtons();
                root.setRedraw(true);
                SWTUtil.enable(root);
            }
        }
    }

    /**
     * Returns the currently selected criterion, if any
     * @return
     */
    private ModelCriterion getSelectedCriterion() {
        if (table.getSelection() == null || table.getSelection().length == 0) {
            return null;
        }
        return (ModelCriterion)table.getSelection()[0].getData();
    }

    /**
     * Updates the buttons
     */
    private void updateButtons() {
        boolean enabled = !(table.getSelection() == null || table.getSelection().length == 0);
        buttonCross.setEnabled(enabled);
        if (enabled) {
            buttonUp.setEnabled(getSelectedCriterion() instanceof ModelExplicitCriterion);
            buttonDown.setEnabled(getSelectedCriterion() instanceof ModelExplicitCriterion);
        } else {
            buttonUp.setEnabled(enabled);
            buttonDown.setEnabled(enabled);
        }
        buttonAdd.setEnabled(model != null && model.getInputDefinition() != null &&
                             model.getInputDefinition().getQuasiIdentifyingAttributes() != null);
    }

    /**
     * Action
     */
    private void actionCriterionAdd() {
        
    }
}
