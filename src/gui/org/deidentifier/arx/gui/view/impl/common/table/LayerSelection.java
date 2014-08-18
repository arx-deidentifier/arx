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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.search.config.DefaultSearchBindings;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultMoveSelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.tickupdate.config.DefaultTickUpdateConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectColumnAction;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;

/**
 * A selection layer for table views
 * @author Fabian Prasser
 *
 */
public class LayerSelection extends SelectionLayer implements CTComponent {

    private final CTConfiguration config;
    
    /**
     * Creates a new instance
     * @param underlyingLayer
     */
    public LayerSelection(IUniqueIndexLayer underlyingLayer,
                            CTConfiguration config) {
        super(underlyingLayer, false);
        this.config = config;
        addConfiguration(new DefaultSelectionStyleConfiguration());
        addConfiguration(new DefaultSelectionBindings(){
            /** Override some default behavior */
            protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
                IMouseAction action = new SelectCellAction();
                uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
            }
            /** Override some default behavior */
            protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
                // Ignore
            }
            /** Override some default behavior */
            protected void configureColumnHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.columnHeaderLeftClick(SWT.NONE), new ViewportSelectColumnAction(false, false));
            }
            /** Override some default behavior */
            protected void configureRowHeaderMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.rowHeaderLeftClick(SWT.NONE), new ViewportSelectRowAction(false, false));
            }
        });
        addConfiguration(new DefaultSearchBindings());
        addConfiguration(new DefaultTickUpdateConfiguration());
        addConfiguration(new DefaultMoveSelectionConfiguration());
    }

    @Override
    public CTConfiguration getConfig() {
        return config;
    }

    @Override
    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        return getConfig().isCellSelectionEnabled() && super.isCellPositionSelected(columnPosition, rowPosition);
    }

    @Override
    public boolean isColumnPositionFullySelected(int columnPosition) {
        return getConfig().isColumnSelectionEnabled() && super.isColumnPositionFullySelected(columnPosition);
    }

    @Override
    public boolean isColumnPositionSelected(int columnPosition) {
        return false;
    }

    @Override
    public boolean isRowPositionFullySelected(int rowPosition) {
        return getConfig().isRowSelectionEnabled() && super.isRowPositionFullySelected(rowPosition);
    }

    @Override
    public boolean isRowPositionSelected(int rowPosition) {
        return false;
    }
}