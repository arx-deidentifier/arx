package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class HierarchyEditorMenu<T> {

    private final Composite         composite;
    private final Menu              menu;
    private final MenuItem          addBefore;
    private final MenuItem          addAfter;
    private final MenuItem          mergeUp;
    private final MenuItem          mergeDown;
    private final MenuItem          remove;
    private final MenuItem          addRight;
    private final HierarchyModel<T> model;

    public HierarchyEditorMenu(final Composite composite, 
                               final HierarchyModel<T> model) {
        
        this.model = model;
        this.composite = composite;
        
        this.menu = new Menu(composite);
        this.remove = new MenuItem(menu, SWT.NONE);
        this.remove.setText("Remove");
        this.remove.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.remove(model.selected);
            }
        });
        
        new MenuItem(menu, SWT.SEPARATOR);
        
        this.addBefore = new MenuItem(menu, SWT.NONE);
        this.addBefore.setText("Add before");
        this.addBefore.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addBefore(model.selected);
            }
        });
        
        this.addAfter = new MenuItem(menu, SWT.NONE);
        this.addAfter.setText("Add after");
        this.addAfter.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addAfter(model.selected);
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);
        
        this.mergeDown = new MenuItem(menu, SWT.NONE);
        this.mergeDown.setText("Merge down");
        this.mergeDown.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.mergeDown(model.selected);
            }
        });
        
        this.mergeUp = new MenuItem(menu, SWT.NONE);
        this.mergeUp.setText("Merge up");
        this.mergeUp.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.mergeUp(model.selected);
            }
        });
        
        new MenuItem(menu, SWT.SEPARATOR);
        
        this.addRight = new MenuItem(menu, SWT.NONE);
        this.addRight.setText("Add right");
        this.addRight.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addRight(model.selected);
            }
        });
        
    }
    
    @SuppressWarnings("unchecked")
    public void show(int x, int y){
        
        if (model.selected == null){
            return;
        }
        
        if (model.selected instanceof HierarchyInterval){
            if (model.intervals.size()==1) {
                this.remove.setEnabled(false);
            } else {
                this.remove.setEnabled(true);
            }
            if (model.isFirst((HierarchyInterval<T>)model.selected)) {
                this.addBefore.setEnabled(true);
                this.mergeDown.setEnabled(false);
            } else {
                this.addBefore.setEnabled(false);
                this.mergeDown.setEnabled(true);
            }
            if (model.isLast((HierarchyInterval<T>)model.selected)) {
                this.addAfter.setEnabled(true);
                this.mergeUp.setEnabled(false);
            } else {
                this.addAfter.setEnabled(false);
                this.mergeUp.setEnabled(true);
            }
                
            this.addRight.setEnabled(true);
            
        } else if (model.selected instanceof HierarchyGroup){
            this.remove.setEnabled(true);
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
