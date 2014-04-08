package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyDrawingContext.ComponentContext;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyDrawingContext.GroupContext;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyDrawingContext.IntervalContext;
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

public class HierarchyEditor<T> implements IUpdateable {

    private HierarchyModel<T>            model;
    private Composite                    composite;
    private Composite                    canvascomposite;
    private ScrolledComposite            scrolledcomposite;
    private TabFolder                    folder;

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
                if (HierarchyEditor.this.model.context.select(arg0.x, arg0.y)){
                    HierarchyEditor.this.model.update(HierarchyEditor.this);
                    canvascomposite.redraw();
                    Object selected = HierarchyEditor.this.model.selected;
                    if (selected instanceof HierarchyInterval){
                        if (HierarchyEditor.this.model.showIntervals) folder.setSelection(2);
                    } else if (selected instanceof HierarchyGroup){
                        if (HierarchyEditor.this.model.showIntervals) folder.setSelection(3);
                        else folder.setSelection(1);
                    }
                }
            }
        });
        
        folder = new TabFolder(composite, SWT.BORDER);
        folder.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        createGeneralTab(folder);
        if (model.showIntervals) createBoundsTab(folder);
        if (model.showIntervals) createIntervalTab(folder);
        createGroupTab(folder);
    }
    
    private void createGroupTab(TabFolder tabFolder) {
        TabItem tabItem3 = new TabItem(tabFolder, SWT.NULL);
        tabItem3.setText("Group");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(1, false));
        new HierarchyGroupEditor<T>(parent, model);
        tabItem3.setControl(parent);
        
    }

    private void createGeneralTab(TabFolder tabFolder) {
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
        tabItem1.setText("General");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(1, false));
        new HierarchyFunctionEditor<T>(parent, model, false);
        tabItem1.setControl(parent);
    }

    private void createIntervalTab(TabFolder tabFolder) {
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
        tabItem2.setText("Interval");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        new HierarchyIntervalEditor<T>(parent, model);
        tabItem2.setControl(parent);
    }

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
        
        model.context.update(gc);
        
        for (ComponentContext<T> component : model.context.getComponents()) {
            
            Color foreground = HierarchyDrawingContext.NORMAL_FOREGROUND;
            Color background = isSelected(component) ? 
                               HierarchyDrawingContext.SELECTED_BACKGROUND : 
                               HierarchyDrawingContext.NORMAL_BACKGROUND;
            
            if (!component.enabled) {
                foreground = HierarchyDrawingContext.DISABLED_FOREGROUND;
                background = HierarchyDrawingContext.DISABLED_BACKGROUND;
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
        scrolledcomposite.setMinSize(model.context.getMinSize());
    }
    
    private boolean isSelected(ComponentContext<T> component) {
        if (model.selected == null) return false;
        if (component instanceof IntervalContext) {
            return ((IntervalContext<T>)component).interval.equals(model.selected);
        } else {
            return ((GroupContext<T>)component).group.equals(model.selected);
        }
    }

    /**
     * Draws a string
     * @param gc
     * @param string
     * @param r
     */
    private void drawString(GC gc, String string, Rectangle r) {
        gc.setFont(HierarchyDrawingContext.FONT);
        Point extent = gc.textExtent(string);
        int xx = r.x + (r.width - extent.x) / 2;
        int yy = r.y + (r.height - extent.y) / 2;
        gc.drawText(string, xx, yy, true);
    }

    @Override
    public void update() {
        this.canvascomposite.redraw();
    }
}
