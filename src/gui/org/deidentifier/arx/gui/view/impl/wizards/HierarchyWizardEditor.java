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

import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizard.HierarchyWizardView;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardEditorFunction.IHierarchyFunctionEditorParent;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardEditorRenderer.RenderedComponent;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardEditorRenderer.RenderedGroup;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardEditorRenderer.RenderedInterval;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardModelGrouping.HierarchyWizardGroupingGroup;
import org.deidentifier.arx.gui.view.impl.wizards.HierarchyWizardModelGrouping.HierarchyWizardGroupingInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.TabItem;

/**
 * The general editor for hierarchies
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardEditor<T> implements HierarchyWizardView, IHierarchyFunctionEditorParent<T> {

	/** Var */
	private HierarchyWizardModelGrouping<T> model;
	/** Var */
	private Composite composite;
	/** Var */
	private Composite canvascomposite;
	/** Var */
	private ScrolledComposite scrolledcomposite;
	/** Var */
	private CTabFolder folder;
	/** Var */
	private HierarchyWizardEditorMenu<T> menu;
    
    /**
     * Creates a new instance
     * @param parent
     * @param model
     */
    public HierarchyWizardEditor(Composite parent, HierarchyWizardModelGrouping<T> model) {
        
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
            @Override
            public void paintControl(PaintEvent e){
                paint(e.gc);
            }
        }); 
        
        this.canvascomposite.addMouseListener(new MouseAdapter(){
            @Override public void mouseUp(MouseEvent arg0) {
                
                if (HierarchyWizardEditor.this.model.getRenderer().select(arg0.x, arg0.y)){
                    HierarchyWizardEditor.this.model.update(HierarchyWizardEditor.this);
                    canvascomposite.redraw();
                    Object selected = HierarchyWizardEditor.this.model.getSelectedElement();
                    if (selected instanceof HierarchyWizardGroupingInterval){
                        if (HierarchyWizardEditor.this.model.isShowIntervals()) folder.setSelection(2);
                    } else if (selected instanceof HierarchyWizardGroupingGroup){
                        if (HierarchyWizardEditor.this.model.isShowIntervals()) folder.setSelection(3);
                        else folder.setSelection(1);
                    }
                }

                if ((arg0.stateMask & SWT.BUTTON3) != 0 && HierarchyWizardEditor.this.model.getSelectedElement() != null){
                    menu.show(arg0.x, arg0.y);
                }
            }
        });
        
        this.menu = new HierarchyWizardEditorMenu<T>(composite, model);
        
        this.folder = new CTabFolder(composite, SWT.BORDER);
        this.folder.setSimple(false);
        this.folder.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.createGeneralTab(folder);
        if (model.isShowIntervals()) this.createRangeTab(folder);
        if (model.isShowIntervals()) this.createIntervalTab(folder);
        this.createGroupTab(folder);
        this.folder.setSelection(0);
        
        this.model.update();
    }
    
    @Override
    public void setFunction(AggregateFunction<T> function) {
        model.setDefaultFunction(function);
        model.update();
    }

    /**
     * Set the controls layout data
     * @param object
     */
    public void setLayoutData(Object object){
        this.composite.setLayoutData(object);
    }

    @Override
    public void update() {
        this.canvascomposite.redraw();
    }

    /**
     * Create a tab
     * @param tabFolder
     */
    private void createRangeTab(CTabFolder tabFolder) {
    	CTabItem tabItem4 = new CTabItem(tabFolder, SWT.NULL);
        tabItem4.setText("Range");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        new HierarchyWizardEditorRange<T>(parent, model, true);
        new HierarchyWizardEditorRange<T>(parent, model, false);
        tabItem4.setControl(parent);
    }

    /**
     * Create a tab
     * @param tabFolder
     */
    private void createGeneralTab(CTabFolder tabFolder) {
    	CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NULL);
        tabItem1.setText("General");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        parent.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        HierarchyWizardEditorFunction<T> editor = new HierarchyWizardEditorFunction<T>(this, model, parent, true);
        editor.setFunction(model.getDefaultFunction());
        tabItem1.setControl(parent);
    }

    /**
     * Create a tab
     * @param tabFolder
     */
    private void createGroupTab(CTabFolder tabFolder) {
    	CTabItem tabItem3 = new CTabItem(tabFolder, SWT.NULL);
        tabItem3.setText("Group");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(1, false));
        new HierarchyWizardEditorGroup<T>(parent, model);
        tabItem3.setControl(parent);
        
    }
    
    /**
     * Create a tab
     * @param tabFolder
     */
    private void createIntervalTab(CTabFolder tabFolder) {
        CTabItem tabItem2 = new CTabItem(tabFolder, SWT.NULL);
        tabItem2.setText("Interval");
        Composite parent = new Composite(tabFolder, SWT.NULL);
        parent.setLayout(SWTUtil.createGridLayout(2, false));
        new HierarchyWizardEditorInterval<T>(parent, model);
        tabItem2.setControl(parent);
    }

    /**
     * Draws a string
     * @param gc
     * @param string
     * @param r
     */
    private void drawString(GC gc, String string, Rectangle r) {
        gc.setFont(HierarchyWizardEditorRenderer.FONT);
        Point extent = gc.textExtent(string);
        int xx = r.x + (r.width - extent.x) / 2;
        int yy = r.y + (r.height - extent.y) / 2;
        gc.drawText(string, xx, yy, true);
    }

    /**
     * Is the component selected
     * @param component
     * @return
     */
    private boolean isSelected(RenderedComponent<T> component) {
        if (model.getSelectedElement() == null) return false;
        if (component instanceof RenderedInterval) {
            return ((RenderedInterval<T>)component).interval.equals(model.getSelectedElement());
        } else {
            return ((RenderedGroup<T>)component).group.equals(model.getSelectedElement());
        }
    }

    /**
     * Paints the intervals and fanouts
     * @param gc
     */
    protected void paint(GC gc) {
        
        model.getRenderer().update(gc);
        
        for (RenderedComponent<T> component : model.getRenderer().getComponents()) {
            
            Color foreground = HierarchyWizardEditorRenderer.NORMAL_FOREGROUND;
            Color background = isSelected(component) ? 
                               HierarchyWizardEditorRenderer.SELECTED_BACKGROUND : 
                               HierarchyWizardEditorRenderer.NORMAL_BACKGROUND;
            
            if (!component.enabled) {
                foreground = HierarchyWizardEditorRenderer.DISABLED_FOREGROUND;
                background = HierarchyWizardEditorRenderer.DISABLED_BACKGROUND;
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
}
