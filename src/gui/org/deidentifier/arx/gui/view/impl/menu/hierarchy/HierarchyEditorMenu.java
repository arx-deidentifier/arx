package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * The editor's menu
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyEditorMenu<T> {

    /** Var*/
    private final Composite         composite;
    /** Var*/
    private final Menu              menu;
    /** Var*/
    private final MenuItem          addBefore;
    /** Var*/
    private final MenuItem          addAfter;
    /** Var*/
    private final MenuItem          mergeUp;
    /** Var*/
    private final MenuItem          mergeDown;
    /** Var*/
    private final MenuItem          remove;
    /** Var*/
    private final MenuItem          addRight;
    /** Var*/
    private final HierarchyModel<T> model;

    /**
     * Creates a new instance
     * @param composite
     * @param model
     */
    public HierarchyEditorMenu(final Composite composite, 
                               final HierarchyModel<T> model) {
        
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
        this.addRight.setText("Add right");
        this.addRight.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                model.addRight(model.getSelectedElement());
            }
        });
        
    }
    
    @SuppressWarnings("unchecked")
    public void show(int x, int y){
        
        if (model.getSelectedElement() == null){
            return;
        }
        
        if (model.getSelectedElement() instanceof HierarchyInterval){
            if (model.getIntervals().size()==1) {
                this.remove.setEnabled(false);
            } else {
                this.remove.setEnabled(true);
            }
            if (model.isFirst((HierarchyInterval<T>)model.getSelectedElement())) {
                this.addBefore.setEnabled(true);
                this.mergeDown.setEnabled(false);
            } else {
                this.addBefore.setEnabled(false);
                this.mergeDown.setEnabled(true);
            }
            if (model.isLast((HierarchyInterval<T>)model.getSelectedElement())) {
                this.addAfter.setEnabled(true);
                this.mergeUp.setEnabled(false);
            } else {
                this.addAfter.setEnabled(false);
                this.mergeUp.setEnabled(true);
            }
                
            this.addRight.setEnabled(true);
            
        } else if (model.getSelectedElement() instanceof HierarchyGroup){
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
