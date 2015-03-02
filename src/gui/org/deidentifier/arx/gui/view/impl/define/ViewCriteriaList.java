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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.dynamictablecolumns.DynamicTable;
import org.mihalis.opal.dynamictablecolumns.DynamicTableColumn;

/**
 * This class displays a list of all defined privacy criteria.
 *
 * @author fabian
 */
public class ViewCriteriaList implements IView {

    /** Controller */
    private Controller         controller;

    /** Model */
    private Model              model = null;

    /** View */
    private DynamicTable       table;
    /** View */
    private DynamicTableColumn column1;
    /** View */
    private DynamicTableColumn column2;
    /** View */
    private DynamicTableColumn column3;
    /** View */
    private Composite          root;
    /** View */
    private Image              symbolL;
    /** View */
    private Image              symbolT;
    /** View */
    private Image              symbolK;
    /** View */
    private Image              symbolD;
    /** View */
    private Image              symbolR;
    /** View */
    private LayoutCriteria     layout;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param layoutCriteria 
     */
    public ViewCriteriaList(final Composite parent, final Controller controller, LayoutCriteria layoutCriteria) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.layout = layoutCriteria;
        
        this.symbolL = controller.getResources().getImage("symbol_l.png"); //$NON-NLS-1$
        this.symbolT = controller.getResources().getImage("symbol_t.png"); //$NON-NLS-1$
        this.symbolK = controller.getResources().getImage("symbol_k.png"); //$NON-NLS-1$
        this.symbolD = controller.getResources().getImage("symbol_d.png"); //$NON-NLS-1$
        this.symbolR = controller.getResources().getImage("symbol_r.png"); //$NON-NLS-1$
        
        this.root = parent;
        table = new DynamicTable(root, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gd = SWTUtil.createFillHorizontallyGridData();
        gd.heightHint = 100;
        table.setLayoutData(gd);
        SWTUtil.createGenericTooltip(table);
        
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        table.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                layout.updateButtons();
            }
        });

        column1 = new DynamicTableColumn(table, SWT.NONE);
        column1.setText("Type");
        column1.setWidth("10%", "30px");
        column2 = new DynamicTableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("CriterionSelectionDialog.2")); //$NON-NLS-1$
        column2.setWidth("45%", "100px");
        column3 = new DynamicTableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("CriterionSelectionDialog.3")); //$NON-NLS-1$
        column3.setWidth("45%", "100px");
        
        column1.pack();
        column2.pack();
        column3.pack();
        
        layout.updateButtons();
        reset();
    }
    
    /**
     * Add
     */
    public void actionAdd() {
        controller.actionCriterionAdd();
    }
    
    /**
     * Pull
     */
    public void actionPull() {
        ModelCriterion criterion = this.getSelectedCriterion();
        if (criterion != null && criterion instanceof ModelExplicitCriterion) {
            controller.actionCriterionPull(criterion);
        }
    }
    
    /**
     * Push
     */
    public void actionPush() {
        ModelCriterion criterion = this.getSelectedCriterion();
        if (criterion != null && criterion instanceof ModelExplicitCriterion) {
            controller.actionCriterionPush(criterion);
        }
    }
    
    /**
     * Remove
     */
    public void actionRemove() {
        ModelCriterion criterion = this.getSelectedCriterion();
        if (criterion != null) {
            controller.actionCriterionEnable(criterion);
        }
    }

    /**
     * Configure
     */
    public void actionConfigure() {
        ModelCriterion criterion = this.getSelectedCriterion();
        if (criterion != null) {
            controller.actionCriterionConfigure(criterion);
        }
    }
    
    @Override
    public void dispose() {
        this.controller.removeListener(this);
        this.symbolL.dispose();
        this.symbolT.dispose();
        this.symbolK.dispose();
        this.symbolD.dispose();
        this.symbolR.dispose();
    }

    /**
     * Returns the currently selected criterion, if any
     * @return
     */
    public ModelCriterion getSelectedCriterion() {
        if (table.getSelection() == null || table.getSelection().length == 0) {
            return null;
        }
        return (ModelCriterion)table.getSelection()[0].getData();
    }

    /**
     * May criteria be added
     * @return
     */
    public boolean isAddEnabled() {
        return model != null && model.getInputDefinition() != null &&
               model.getInputDefinition().getQuasiIdentifyingAttributes() != null;
    }

    @Override
    public void reset() {
        root.setRedraw(false);
        if (table != null) {
            table.removeAll();
        }
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
                updateTable();
            }
        }
    }

    private void updateTable() {
        
        root.setRedraw(false);
        
        table.removeAll();
        
        if (model.getKAnonymityModel().isEnabled()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", model.getKAnonymityModel().toString(), "" });
            item.setImage(0, symbolK);
            item.setData(model.getKAnonymityModel());
        }

        if (model.getDPresenceModel().isEnabled()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", model.getDPresenceModel().toString(), "" });
            item.setImage(0, symbolD);
            item.setData(model.getDPresenceModel());
        }


        List<ModelExplicitCriterion> explicit = new ArrayList<ModelExplicitCriterion>();
        for (ModelLDiversityCriterion other : model.getLDiversityModel().values()) {
            if (other.isEnabled()) {
                explicit.add(other);
            }
        }
        for (ModelTClosenessCriterion other : model.getTClosenessModel().values()) {
            if (other.isEnabled()) {
                explicit.add(other);
            }
        }
        Collections.sort(explicit, new Comparator<ModelExplicitCriterion>(){
            public int compare(ModelExplicitCriterion o1, ModelExplicitCriterion o2) {
                return o1.getAttribute().compareTo(o2.getAttribute());
            }
        });
        
        for (ModelExplicitCriterion c :explicit) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", c.toString(), c.getAttribute() });
            if (c instanceof ModelLDiversityCriterion) {
                item.setImage(0, symbolL);
            } else {
                item.setImage(0, symbolT);
            }
            item.setData(c);
        }

        List<ModelRiskBasedCriterion> riskBased = new ArrayList<ModelRiskBasedCriterion>();
        for (ModelRiskBasedCriterion other : model.getRiskBasedModel()) {
            if (other.isEnabled()) {
                riskBased.add(other);
            }
        }
        Collections.sort(riskBased, new Comparator<ModelRiskBasedCriterion>(){
            public int compare(ModelRiskBasedCriterion o1, ModelRiskBasedCriterion o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        
        for (ModelRiskBasedCriterion c : riskBased) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", c.toString(), "" });
            item.setImage(0, symbolR);
            item.setData(c);
        }

        layout.updateButtons();
        root.setRedraw(true);
        SWTUtil.enable(root);
    }
}
