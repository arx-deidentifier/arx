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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDataView;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import com.sun.java.swing.plaf.nimbus.CheckBoxPainter;

public class DataView implements IDataView, IView {

    private final Image       IMAGE_INSENSITIVE;
    private final Image       IMAGE_SENSITIVE;
    private final Image       IMAGE_QUASI_IDENTIFYING;
    private final Image       IMAGE_IDENTIFYING;

    private final DataTable   table;

    /** The handle */
    private DataHandle        handle = null;

    /** The attrs */
    private final EventTarget target;
    private final EventTarget reset;
    private Model             model;
    private final Controller  controller;

    /** Create */
    public DataView(final Composite parent,
                    final Controller controller,
                    final String title,
                    final EventTarget target,
                    final EventTarget reset) {

        // Register
        controller.addListener(EventTarget.ATTRIBUTE_TYPE, this);
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        IMAGE_INSENSITIVE       = controller.getResources()
                                            .getImage("bullet_green.png"); //$NON-NLS-1$
        IMAGE_SENSITIVE         = controller.getResources()
                                            .getImage("bullet_purple.png"); //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources()
                                            .getImage("bullet_yellow.png"); //$NON-NLS-1$
        IMAGE_IDENTIFYING       = controller.getResources()
                                            .getImage("bullet_red.png"); //$NON-NLS-1$

        TitledBorder border = new TitledBorder(parent, controller, title, "id-40");
        border.setLayoutData(SWTUtil.createFillGridData());
        Composite c = new Composite(border.getControl(), SWT.NONE);
        border.setChild(c);
        GridLayout l = new GridLayout();
        l.numColumns = 1;
        c.setLayout(l);
        
        table = new DataTable(controller, c);
        table.getUiBindingRegistry().registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,
                                                                                    GridRegion.COLUMN_HEADER,
                                                                                    MouseEventMatcher.LEFT_BUTTON),
                                                              new IMouseAction() {
                                                                  public void run(final NatTable arg0,
                                                                                  final MouseEvent arg1) {
                                                                      actionHeaderClicked(arg1);
                                                                  }
                                                              });
        

        table.getUiBindingRegistry().registerMouseDownBinding(new MouseEventMatcher(SWT.NONE,
                                                                                    GridRegion.BODY,
                                                                                    MouseEventMatcher.LEFT_BUTTON),
                                                              new IMouseAction() {
                                                                  public void run(final NatTable arg0,
                                                                                  final MouseEvent arg1) {
                                                                      actionBodyClicked(arg1);
                                                                  }
                                                              });
    }
    
    private void actionHeaderClicked(MouseEvent arg1) {
        if (!(arg1.data instanceof NatEventData)) { return; }
        if (model != null) {
            int i = ((NatEventData) arg1.data).getColumnPosition() - 2;
            i += table.getViewportLayer().getOriginColumnPosition();
            if (i>=0){
                final String attr = handle.getAttributeName(i);
                model.setSelectedAttribute(attr);
                controller.update(new ModelEvent(this,
                                                 EventTarget.SELECTED_ATTRIBUTE,
                                                 attr));
            }
        }
    }
    

    private void actionBodyClicked(MouseEvent arg1) {

        System.out.println("CLICKED!");
        
        if (!(arg1.data instanceof NatEventData)) { return; }
        if (model != null) {
            int column = ((NatEventData) arg1.data).getColumnPosition() - 2;
            column += table.getViewportLayer().getOriginColumnPosition();
            
            if (column==0){
                int row = ((NatEventData) arg1.data).getRowPosition() - 1;
                row += table.getViewportLayer().getOriginRowPosition();
                if (row>=0){
                    RowSet subset = model.getInputConfig().getResearchSubset();
                    if (subset.contains(row)) {
                        subset.remove(row);
                    } else {
                        subset.add(row);
                    }
                    controller.update(new ModelEvent(this,
                                                     EventTarget.RESEARCH_SUBSET,
                                                     subset));
                }
            }
        }
    }

    @Override
    public void addSelectionListener(final Listener listener) {
        table.addSelectionListener(listener);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        IMAGE_INSENSITIVE.dispose();
        IMAGE_SENSITIVE.dispose();
        IMAGE_QUASI_IDENTIFYING.dispose();
        IMAGE_IDENTIFYING.dispose();
        table.dispose();
    }

    @Override
    public ViewportLayer getViewportLayer() {
        return table.getViewportLayer();
    }

    @Override
    public void reset() {
        table.reset();
        handle = null;
    }

    @Override
    public void update(final ModelEvent event) {

        // Handle reset target, i.e., e.g. input has changed
        if (event.target == reset) {
            reset();
            // Handle project
        } else if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
            reset();
            // Handle new data
        } else if (event.target == target) {

            // No result avail
            if (event.data == null) {
                reset();
            } else {
                handle = (DataHandle) event.data;
                table.setData(handle, model.getInputConfig().getResearchSubset(), model.getColors(), model.getGroups());
                table.getHeaderImages().clear();
                for (int i = 0; i < handle.getNumColumns(); i++) {
                    // TODO: Hmm.. Seems ok to use input config here
                    final AttributeType type = model.getInputConfig()
                                                    .getInput()
                                                    .getDefinition()
                                                    .getAttributeType(handle.getAttributeName(i));
                    setAttributeType(i, type);
                }
                if (table != null) {
                    table.setEnabled(true);
                }
                table.redraw();
            }
            // Handle attribute type change
        } else if (event.target == EventTarget.ATTRIBUTE_TYPE) {
            if ((model != null) && (handle != null)) {
                final String attr = (String) event.data;

                // TODO: Hmm.. Seems ok to use input config here
                final AttributeType type = model.getInputConfig()
                                                .getInput()
                                                .getDefinition()
                                                .getAttributeType(attr);
                final int index = handle.getColumnIndexOf(attr);
                setAttributeType(index, type);
                if (table != null) {
                    table.setEnabled(true);
                }
                table.redraw();
            }
        }
    }

    private void setAttributeType(final int i, final AttributeType type) {
        if (table.getHeaderImages().size() <= i) {
            table.getHeaderImages().add(null);
        }
        if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
            table.getHeaderImages().set(i, IMAGE_INSENSITIVE);
        } else if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
            table.getHeaderImages().set(i, IMAGE_IDENTIFYING);
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            table.getHeaderImages().set(i, IMAGE_SENSITIVE);
        } else {
            table.getHeaderImages().set(i, IMAGE_QUASI_IDENTIFYING);
        }
    }
}
