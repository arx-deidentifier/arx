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
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This class displays a list of all defined privacy criteria.
 *
 * @author fabian
 */
public class ViewCriteriaList implements IView {

    /**  TODO */
    private Controller  controller;
    
    /**  TODO */
    private Model       model = null;
    
    /**  TODO */
    private Table       table;
    
    /**  TODO */
    private TableColumn column1;
    
    /**  TODO */
    private TableColumn column2;
    
    /**  TODO */
    private TableColumn column3;
    
    /**  TODO */
    private Composite   root;
    
    /**  TODO */
    private Image       symbolL;
    
    /**  TODO */
    private Image       symbolT;
    
    /**  TODO */
    private Image       symbolK;
    
    /**  TODO */
    private Image       symbolD;

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
        this.symbolL = controller.getResources().getImage("symbol_l.png"); //$NON-NLS-1$
        this.symbolT = controller.getResources().getImage("symbol_t.png"); //$NON-NLS-1$
        this.symbolK = controller.getResources().getImage("symbol_k.png"); //$NON-NLS-1$
        this.symbolD = controller.getResources().getImage("symbol_d.png"); //$NON-NLS-1$

        root = new Composite(parent, SWT.NONE);
        GridLayout l = new GridLayout();
        l.numColumns = 1;
        root.setLayout(l);

        table = new Table(root, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        final GridData d = SWTUtil.createFillGridData();
        table.setLayoutData(d);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        column1 = new TableColumn(table, SWT.NONE);
        column1.setText("");
        column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Resources.getMessage("CriterionSelectionDialog.2")); //$NON-NLS-1$
        column3 = new TableColumn(table, SWT.NONE);
        column3.setText(Resources.getMessage("CriterionSelectionDialog.3")); //$NON-NLS-1$

        column1.pack();
        column2.pack();
        column3.pack();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
        this.symbolL.dispose();
        this.symbolT.dispose();
        this.symbolK.dispose();
        this.symbolD.dispose();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        root.setRedraw(false);
        if (table != null) table.removeAll();
        if (column1 != null) column1.pack();
        if (column2 != null) column2.pack();
        if (column3 != null) column3.pack();
        root.setRedraw(true);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
        } 
        
        if (event.part == ModelPart.CRITERION_DEFINITION ||
            event.part == ModelPart.MODEL) {
            if (model!=null) {
                root.setRedraw(false);
                table.removeAll();
                if (model.getKAnonymityModel().isActive() && model.getKAnonymityModel().isEnabled()) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[] { "", model.getKAnonymityModel().toString(), "" });
                    item.setImage(0, symbolK);
                }
    
                if (model.getDPresenceModel().isActive() && model.getDPresenceModel().isEnabled()) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[] { "", model.getDPresenceModel().toString(), "" });
                    item.setImage(0, symbolD);
                }
    
                for (ModelLDiversityCriterion c : model.getLDiversityModel().values()) {
                    if (c.isActive() && c.isEnabled()) {
                        TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(new String[] { "", c.toString(), c.getAttribute() });
                        item.setImage(0, symbolL);
                    }
                }
    
                for (ModelTClosenessCriterion c : model.getTClosenessModel().values()) {
                    if (c.isActive() && c.isEnabled()) {
                        TableItem item = new TableItem(table, SWT.NONE);
                        item.setText(new String[] { "", c.toString(), c.getAttribute() });
                        item.setImage(0, symbolT);
                    }
                }
                column1.pack();
                column2.pack();
                column3.pack();
                root.setRedraw(true);
            }
        }
    }
}
