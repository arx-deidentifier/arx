/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.wizards;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.define.ViewHierarchy;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class HierarchyWizardPageFinal<T> extends WizardPage{

    private final HierarchyWizardModel<T> model;
    private final HierarchyWizard<T> wizard;
    

    private Composite composite;
    private Table table;
    private TableColumn column1;
    private ViewHierarchy view;
    
    private int[] groups;
    private Hierarchy hierarchy;
    
    public HierarchyWizardPageFinal(final HierarchyWizard<T> wizard, final HierarchyWizardModel<T> model) {
        super("");
        this.model = model;
        this.wizard = wizard;
        setTitle("Review the hierarchy");
        setDescription("Overview of groups and values");
        setPageComplete(true);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    @Override
    public void  createControl(final Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(2, false));

        final Group center = new Group(composite, SWT.SHADOW_ETCHED_IN);
        center.setText("Groups");
        center.setLayoutData(SWTUtil.createFillVerticallyGridData());
        center.setLayout(SWTUtil.createGridLayout(1, false));
        
        Composite base = new Composite(center, SWT.NONE);
        base.setLayoutData(SWTUtil.createFillGridData());
        base.setLayout(SWTUtil.createGridLayout(1, false));
        
        table = new Table(base, SWT.BORDER);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.setHeaderVisible(true);
        column1 = new TableColumn(table, SWT.LEFT);
        column1.setText("#Groups");
        column1.pack();
        
        final Group right = new Group(composite, SWT.SHADOW_ETCHED_IN);
        right.setText("Table");
        right.setLayoutData(SWTUtil.createFillGridData());
        right.setLayout(SWTUtil.createGridLayout(1, false));
        view = new ViewHierarchy(right);

        setControl(composite);
    }

    @Override
    public boolean isPageComplete() {
        return false;
    }
    
    public void setGroups(int[] groups){
        this.groups = groups;
    }
    
    public void setHierarchy(Hierarchy hierarchy){
        this.hierarchy = hierarchy;
    }
    
    @Override
    public void setVisible(boolean value){
        
        if (value) {
            this.composite.setRedraw(false);
            
            // Reset
            for (TableItem item : table.getItems()) {
                item.dispose();
            }
            this.view.setHierarchy(Hierarchy.create());
            
            if (groups != null) {
                for (int count : groups){
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[]{String.valueOf(count)});
                }
                column1.pack();
            }
            if (hierarchy != null) {
                view.setHierarchy(hierarchy);
            }
            
            this.composite.setRedraw(true);

            // Deactivate buttons
            Button load = this.wizard.getLoadButton();
            if (load != null) load.setEnabled(false);
            Button save = this.wizard.getSaveButton();
            if (save != null) save.setEnabled(false);
        }
        super.setVisible(value);
    }
}
