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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ViewCriteriaList implements IView {

    private Controller  controller;
    private Model       model = null;
    private Table       table;
    private TableColumn column1;
    private TableColumn column2;
    private TableColumn column3;
    private Composite   root;
    private Image       symbolL;
    private Image       symbolT;
    private Image       symbolK;
    private Image       symbolD;

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

    @Override
    public void dispose() {
        controller.removeListener(this);
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
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
        } else if (event.part == ModelPart.CRITERION_DEFINITION) {
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
