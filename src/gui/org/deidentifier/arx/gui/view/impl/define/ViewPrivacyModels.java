/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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
import org.deidentifier.arx.gui.model.ModelBLikenessCriterion;
import org.deidentifier.arx.gui.model.ModelDDisclosurePrivacyCriterion;
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

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This class displays a list of all defined privacy criteria.
 *
 * @author fabian
 */
public class ViewPrivacyModels implements IView {

    /** Controller */
    private Controller               controller;

    /** Model */
    private Model                    model = null;

    /** View */
    private final DynamicTable       table;
    /** View */
    private final DynamicTableColumn column1;
    /** View */
    private final DynamicTableColumn column2;
    /** View */
    private final DynamicTableColumn column3;
    /** View */
    private final Composite          root;
    /** View */
    private final Image              symbolL;
    /** View */
    private final Image              symbolT;
    /** View */
    private final Image              symbolK;
    /** View */
    private final Image              symbolD;
    /** View */
    private final Image              symbolDP;
    /** View */
    private final Image              symbolR;
    /** View */
    private final Image              symbolG;
    /** View */
    private final Image              symbolB;
    /** View */
    private final LayoutPrivacySettings     layout;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param layoutCriteria 
     */
    public ViewPrivacyModels(final Composite parent, final Controller controller, LayoutPrivacySettings layoutCriteria) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
        this.layout = layoutCriteria;
        
        this.symbolL = controller.getResources().getManagedImage("symbol_l.png"); //$NON-NLS-1$
        this.symbolT = controller.getResources().getManagedImage("symbol_t.png"); //$NON-NLS-1$
        this.symbolK = controller.getResources().getManagedImage("symbol_k.png"); //$NON-NLS-1$
        this.symbolD = controller.getResources().getManagedImage("symbol_d.png"); //$NON-NLS-1$
        this.symbolDP = controller.getResources().getManagedImage("symbol_dp.png"); //$NON-NLS-1$
        this.symbolR = controller.getResources().getManagedImage("symbol_r.png"); //$NON-NLS-1$
        this.symbolG = controller.getResources().getManagedImage("symbol_gt.png"); //$NON-NLS-1$
        this.symbolB = controller.getResources().getManagedImage("symbol_b.png"); //$NON-NLS-1$
        
        this.root = parent;
        this.table = SWTUtil.createTableDynamic(root, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        GridData gd = SWTUtil.createFillHorizontallyGridData();
        gd.heightHint = 100;
        this.table.setLayoutData(gd);
        SWTUtil.createGenericTooltip(table);
        
        this.table.setMenu(new ClipboardHandlerTable(table).getMenu());
        this.table.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                layout.updateButtons();
            }
        });

        this.column1 = new DynamicTableColumn(table, SWT.NONE);
        this.column1.setText(Resources.getMessage("ViewCriteriaList.0")); //$NON-NLS-1$
        this.column1.setWidth("10%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
        this.column2 = new DynamicTableColumn(table, SWT.NONE);
        this.column2.setText(Resources.getMessage("CriterionSelectionDialog.2")); //$NON-NLS-1$
        this.column2.setWidth("45%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        this.column3 = new DynamicTableColumn(table, SWT.NONE);
        this.column3.setText(Resources.getMessage("CriterionSelectionDialog.3")); //$NON-NLS-1$
        this.column3.setWidth("45%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.column1.pack();
        this.column2.pack();
        this.column3.pack();
        
        this.layout.updateButtons();
        reset();
    }
    
    /**
     * Add
     */
    public void actionAdd() {
        controller.actionCriterionAdd();
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
    
    @Override
    public void dispose() {
        this.controller.removeListener(this);
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
    	
    	// Model update
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
        } 
        
        // Other updates
        if (event.part == ModelPart.CRITERION_DEFINITION ||
            event.part == ModelPart.ATTRIBUTE_TYPE ||
            event.part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE ||
            event.part == ModelPart.MODEL) {
        	
        	// Update table
            if (model!=null) {
                updateTable();
            }
        }
    }

    /**
     * Update table
     */
    private void updateTable() {
        
        root.setRedraw(false);
        
        table.removeAll();

        if (model.getDifferentialPrivacyModel().isEnabled()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", model.getDifferentialPrivacyModel().toString(), "" }); //$NON-NLS-1$ //$NON-NLS-2$
            item.setImage(0, symbolDP);
            item.setData(model.getDifferentialPrivacyModel());
        }

        if (model.getKAnonymityModel().isEnabled()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", model.getKAnonymityModel().toString(), "" }); //$NON-NLS-1$ //$NON-NLS-2$
            item.setImage(0, symbolK);
            item.setData(model.getKAnonymityModel());
        }

        if (model.getKMapModel().isEnabled()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", model.getKMapModel().toString(), "" }); //$NON-NLS-1$ //$NON-NLS-2$
            item.setImage(0, symbolK);
            item.setData(model.getKMapModel());
        }

        if (model.getDPresenceModel().isEnabled()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "", model.getDPresenceModel().toString(), "" }); //$NON-NLS-1$ //$NON-NLS-2$
            item.setImage(0, symbolD);
            item.setData(model.getDPresenceModel());
        }
        
        if (model.getStackelbergModel().isEnabled()) {
        	TableItem item = new TableItem(table, SWT.NONE);
        	item.setText(new String[] { "", model.getStackelbergModel().toString(), ""});
        	item.setImage(0, symbolG);
        	item.setData(model.getStackelbergModel());
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
        for (ModelDDisclosurePrivacyCriterion other : model.getDDisclosurePrivacyModel().values()) {
            if (other.isEnabled()) {
                explicit.add(other);
            }
        }
        for (ModelBLikenessCriterion other : model.getBLikenessModel().values()) {
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
            item.setText(new String[] { "", c.toString(), c.getAttribute() }); //$NON-NLS-1$
            if (c instanceof ModelLDiversityCriterion) {
                item.setImage(0, symbolL);
            } else if (c instanceof ModelTClosenessCriterion) {
                item.setImage(0, symbolT);
            } else if (c instanceof ModelDDisclosurePrivacyCriterion) {
                item.setImage(0, symbolD);
            } else if (c instanceof ModelBLikenessCriterion) {
                item.setImage(0, symbolB);
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
            item.setText(new String[] { "", c.toString(), "" }); //$NON-NLS-1$ //$NON-NLS-2$
            item.setImage(0, symbolR);
            item.setData(c);
        }

        // Update
        layout.updateButtons();
        root.setRedraw(true);
        SWTUtil.enable(root);
        table.redraw();
    }
}
