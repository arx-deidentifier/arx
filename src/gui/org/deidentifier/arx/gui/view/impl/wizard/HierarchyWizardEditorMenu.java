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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingGroup;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * The editor's menu.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardEditorMenu<T> {

    /** Var. */
    private final Composite         composite;
    
    /** Var. */
    private final Menu              menu;
    
    /** Var. */
    private final MenuItem          addBefore;
    
    /** Var. */
    private final MenuItem          addAfter;
    
    /** Var. */
    private final MenuItem          mergeUp;
    
    /** Var. */
    private final MenuItem          mergeDown;
    
    /** Var. */
    private final MenuItem          remove;
    
    /** Var. */
    private final MenuItem          addRight;
    
    /** Var. */
    private final HierarchyWizardModelGrouping<T> model;

    /**
     * Creates a new instance.
     *
     * @param composite
     * @param model
     */
    public HierarchyWizardEditorMenu(final Composite composite, 
                               final HierarchyWizardModelGrouping<T> model) {
        
        this.model = model;
        this.composite = composite;
        
        this.menu = new Menu(composite);
        this.remove = new MenuItem(menu, SWT.NONE);
        this.remove.setText("Remove");
        this.remove.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.remove(model.getSelectedElement());
            }
        });
        
        new MenuItem(menu, SWT.SEPARATOR);
        
        this.addBefore = new MenuItem(menu, SWT.NONE);
        this.addBefore.setText("Add before");
        this.addBefore.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addBefore(model.getSelectedElement());
            }
        });
        
        this.addAfter = new MenuItem(menu, SWT.NONE);
        this.addAfter.setText("Add after");
        this.addAfter.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addAfter(model.getSelectedElement());
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);
        
        this.mergeDown = new MenuItem(menu, SWT.NONE);
        this.mergeDown.setText("Merge down");
        this.mergeDown.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.mergeDown(model.getSelectedElement());
            }
        });
        
        this.mergeUp = new MenuItem(menu, SWT.NONE);
        this.mergeUp.setText("Merge up");
        this.mergeUp.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.mergeUp(model.getSelectedElement());
            }
        });
        
        new MenuItem(menu, SWT.SEPARATOR);
        
        this.addRight = new MenuItem(menu, SWT.NONE);
        this.addRight.setText("Add groups");
        this.addRight.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addRight(model.getSelectedElement());
            }
        });
        
    }
    
    /**
     * 
     *
     * @param x
     * @param y
     */
    @SuppressWarnings("unchecked")
    public void show(int x, int y){
        
        if (model.getSelectedElement() == null){
            return;
        }
        
        if (model.getSelectedElement() instanceof HierarchyWizardGroupingInterval){
            
            HierarchyWizardGroupingInterval<T> interval = 
                    (HierarchyWizardGroupingInterval<T>)model.getSelectedElement();
            
            if (model.getIntervals().size()==1) {
                this.remove.setEnabled(false);
            } else if (model.isFirst(interval) || model.isLast(interval)){
                this.remove.setEnabled(true);
            } else {
                this.remove.setEnabled(false);
            }
            
            if (model.isFirst(interval)) {
                this.addBefore.setEnabled(true);
                this.mergeDown.setEnabled(false);
            } else {
                this.addBefore.setEnabled(false);
                this.mergeDown.setEnabled(true);
            }
            if (model.isLast(interval)) {
                this.addAfter.setEnabled(true);
                this.mergeUp.setEnabled(false);
            } else {
                this.addAfter.setEnabled(false);
                this.mergeUp.setEnabled(true);
            }
                
            this.addRight.setEnabled(true);
            
        } else if (model.getSelectedElement() instanceof HierarchyWizardGroupingGroup){
            if (model.isShowIntervals()){
                this.remove.setEnabled(true); 
            } else {
                if (model.getModelGroups().size() == 1 &&
                    model.getModelGroups().get(0).size() == 1) {
                    this.remove.setEnabled(false);
                } else {
                    this.remove.setEnabled(true);

                }
            }
                
            this.addBefore.setEnabled(true);
            this.addAfter.setEnabled(true);
            this.addRight.setEnabled(true);
            this.mergeUp.setEnabled(false);
            this.mergeDown.setEnabled(false);
        }
        
        menu.setLocation(composite.toDisplay(x, y));
        menu.setVisible(true);
    }
}
