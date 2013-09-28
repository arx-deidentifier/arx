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
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

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
        controller.addListener(EventTarget.RESEARCH_SUBSET, this);
        controller.addListener(EventTarget.ATTRIBUTE_TYPE, this);
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        IMAGE_INSENSITIVE       = controller.getResources().getImage("bullet_green.png"); //$NON-NLS-1$
        IMAGE_SENSITIVE         = controller.getResources().getImage("bullet_purple.png"); //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources().getImage("bullet_yellow.png"); //$NON-NLS-1$
        IMAGE_IDENTIFYING       = controller.getResources().getImage("bullet_red.png"); //$NON-NLS-1$

        TitledBorder border = new TitledBorder(parent, controller, title, "id-40");
        border.setLayoutData(SWTUtil.createFillGridData());
        Composite c = new Composite(border.getControl(), SWT.NONE);
        border.setChild(c);
        GridLayout l = new GridLayout();
        l.numColumns = 1;
        c.setLayout(l);
        
        table = new DataTable(controller, c);
       
        table.addSelectionLayerListener(new ILayerListener(){
            @Override
            public void handleLayerEvent(ILayerEvent arg0) {
                if (arg0 instanceof CellSelectionEvent) {
                    actionCellSelected((CellSelectionEvent)arg0);
                } else if (arg0 instanceof ColumnSelectionEvent) {
                    actionColumnSelected((ColumnSelectionEvent)arg0);
                }
            }
        });
    }
    
    private void actionColumnSelected(ColumnSelectionEvent arg1) {
        if (model != null) {
            int column = arg1.getColumnPositionRanges().iterator().next().start - 1;
            if (column>=0){
                final String attr = handle.getAttributeName(column);
                model.setSelectedAttribute(attr);
                controller.update(new ModelEvent(this,
                                                 EventTarget.SELECTED_ATTRIBUTE,
                                                 attr));
            }
        }
    }
    

    private void actionCellSelected(CellSelectionEvent arg1) {

        if (model != null) {
            int column = arg1.getColumnPosition();
            if (column==0){
                int row = arg1.getRowPosition();
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
        table.addScrollBarListener(listener);
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
                table.setData(handle, 
                              model.getInputConfig().getResearchSubset(), 
                              model.getColors(), model.getGroups());
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
        } else if (event.target == EventTarget.RESEARCH_SUBSET) {
            table.setResearchSubset((RowSet)event.data);
            table.redraw();
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
