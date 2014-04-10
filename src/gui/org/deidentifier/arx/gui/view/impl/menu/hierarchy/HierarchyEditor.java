package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyRenderer.ComponentContext;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyRenderer.GroupContext;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyRenderer.IntervalContext;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyFunctionEditor.IHierarchyFunctionEditorParent;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * The general editor for hierarchies
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyEditor<T> implements IUpdateable, IHierarchyFunctionEditorParent<T> {

    /** Var*/
    private HierarchyModel<T>            model;
    /** Var*/
    private Composite                    composite;
    /** Var*/
    private Composite                    canvascomposite;
    /** Var*/
    private ScrolledComposite            scrolledcomposite;
    /** Var*/
    private TabFolder                    folder;
    /** Var*/
    private HierarchyEditorMenu<T>       menu;
    
    /**
     * Creates a new instance
     * @param parent
     * @param model
     */
    public HierarchyEditor(Composite parent, HierarchyModel<T> model) {
        
        this.model = model;
        this.model.register(this);

        this.composite = new Composite(parent, SWT.NONE);
        this.composite.setLayoutData(SWTUtil.createFillGridData());
        this.composite.setLayout(SWTUtil.createGridLayout(1));
        
        this.scrolledcomposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.scrolledcomposite.setMinSize(200, 200);
        this.scrolledcomposite.setExpandHorizontal(true);
        this.scrolledcomposite.setExpandVertical(true);
        this.scrolledcomposite.setLayoutData(SWTUtil.createFillGridData());
        
        this.canvascomposite = new Composite(scrolledcomposite, SWT.NONE);
        this.scrolledcomposite.setContent(canvascomposite);
        
        this.canvascomposite.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent e){
                paint(e.gc);
            }
        }); 
        
        this.canvascomposite.addMouseListener(new MouseAdapter(){
            @Override public void mouseUp(MouseEvent arg0) {
                
                if (HierarchyEditor.this.model.getRenderer().select(arg0.x, arg0.y)){
                    HierarchyEditor.this.model.update(HierarchyEditor.this);
                    canvascomposite.redraw();
                    Object selected = HierarchyEditor.this.model.getSelectedElement();
                    if (selected instanceof HierarchyInterval){
                        if (HierarchyEditor.this.model.isShowIntervals()) folder.setSelection(2);
                    } else if (selected instanceof HierarchyGroup){
                        if (HierarchyEditor.this.model.isShowIntervals()) folder.setSelection(3);
                        else folder.setSelection(1);
                    }
                }

                if ((arg0.stateMask & SWT.BUTTON3) != 0 && HierarchyEditor.this.model.getSelectedElement() != null){
                    menu.show(arg0.x, arg0.y);
                }
            }
        });
        
        this.menu = new HierarchyEditorMenu<T>(composite, model);
        
        this.folder = new TabFolder(composite, SWT.BORDER);
        this.folder.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.createGeneralTab(folder);
        if (model.isShowIntervals()) this.createBoundsTab(folder);
        if (model.isShowIntervals()) this.createIntervalTab(folder);
        this.createGroupTab(folder);
        
        this.model.update();
    }
    
    /**
     * Create a tab
     * @param tabFolder
     */
    private void createGroupTab(TabFolder tabFolder) {
        TabItem tabItem3 = new TabItem(tabFolder, SWT.NULL);
        tabItem3.setText("Group");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(1, false));
        new HierarchyGroupEditor<T>(parent, model);
        tabItem3.setControl(parent);
        
    }

    /**
     * Create a tab
     * @param tabFolder
     */
    private void createGeneralTab(TabFolder tabFolder) {
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
        tabItem1.setText("General");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        parent.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        HierarchyFunctionEditor<T> editor = new HierarchyFunctionEditor<T>(this, model, parent, true);
        editor.setFunction(model.getDefaultFunction());
        tabItem1.setControl(parent);
    }

    /**
     * Create a tab
     * @param tabFolder
     */
    private void createIntervalTab(TabFolder tabFolder) {
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
        tabItem2.setText("Interval");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        new HierarchyIntervalEditor<T>(parent, model);
        tabItem2.setControl(parent);
    }

    /**
     * Create a tab
     * @param tabFolder
     */
    private void createBoundsTab(TabFolder tabFolder) {
        TabItem tabItem4 = new TabItem(tabFolder, SWT.NULL);
        tabItem4.setText("Bounds");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        new HierarchyAdjustmentEditor<T>(parent, model, true);
        new HierarchyAdjustmentEditor<T>(parent, model, false);
        tabItem4.setControl(parent);
    }

    /**
     * Set the controls layout data
     * @param object
     */
    public void setLayoutData(Object object){
        this.composite.setLayoutData(object);
    }

    /**
     * Paints the intervals and fanouts
     * @param gc
     */
    protected void paint(GC gc) {
        
        model.getRenderer().update(gc);
        
        for (ComponentContext<T> component : model.getRenderer().getComponents()) {
            
            Color foreground = HierarchyRenderer.NORMAL_FOREGROUND;
            Color background = isSelected(component) ? 
                               HierarchyRenderer.SELECTED_BACKGROUND : 
                               HierarchyRenderer.NORMAL_BACKGROUND;
            
            if (!component.enabled) {
                foreground = HierarchyRenderer.DISABLED_FOREGROUND;
                background = HierarchyRenderer.DISABLED_BACKGROUND;
            }

            gc.setBackground(background);
            gc.fillRectangle(component.rectangle1);
            gc.setForeground(foreground);
            gc.drawRectangle(component.rectangle1);
            drawString(gc, component.bounds, component.rectangle1);

            gc.setBackground(foreground);
            gc.fillRectangle(component.rectangle2);
            gc.drawRectangle(component.rectangle2);
            gc.setForeground(background);
            drawString(gc, component.label, component.rectangle2);
        }
        scrolledcomposite.setMinSize(model.getRenderer().getMinSize());
    }
    
    /**
     * Is the component selected
     * @param component
     * @return
     */
    private boolean isSelected(ComponentContext<T> component) {
        if (model.getSelectedElement() == null) return false;
        if (component instanceof IntervalContext) {
            return ((IntervalContext<T>)component).interval.equals(model.getSelectedElement());
        } else {
            return ((GroupContext<T>)component).group.equals(model.getSelectedElement());
        }
    }

    /**
     * Draws a string
     * @param gc
     * @param string
     * @param r
     */
    private void drawString(GC gc, String string, Rectangle r) {
        gc.setFont(HierarchyRenderer.FONT);
        Point extent = gc.textExtent(string);
        int xx = r.x + (r.width - extent.x) / 2;
        int yy = r.y + (r.height - extent.y) / 2;
        gc.drawText(string, xx, yy, true);
    }

    @Override
    public void update() {
        this.canvascomposite.redraw();
    }

    @Override
    public void setFunction(AggregateFunction<T> function) {
        model.setDefaultFunction(function);
        model.update();
    }
}
