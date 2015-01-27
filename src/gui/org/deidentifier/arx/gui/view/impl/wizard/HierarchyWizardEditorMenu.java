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
