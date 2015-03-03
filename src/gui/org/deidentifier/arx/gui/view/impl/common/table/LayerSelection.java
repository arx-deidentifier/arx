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
 * A selection layer for table views.
 *
 * @author Fabian Prasser
 */
public class LayerSelection extends SelectionLayer implements CTComponent {

    /**  TODO */
    private final CTConfiguration config;
    
    /**
     * Creates a new instance.
     *
     * @param underlyingLayer
     * @param config
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.common.table.CTComponent#getConfig()
     */
    @Override
    public CTConfiguration getConfig() {
        return config;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.selection.SelectionLayer#isCellPositionSelected(int, int)
     */
    @Override
    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        return getConfig().isCellSelectionEnabled() && super.isCellPositionSelected(columnPosition, rowPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.selection.SelectionLayer#isColumnPositionFullySelected(int)
     */
    @Override
    public boolean isColumnPositionFullySelected(int columnPosition) {
        return getConfig().isColumnSelectionEnabled() && super.isColumnPositionFullySelected(columnPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.selection.SelectionLayer#isColumnPositionSelected(int)
     */
    @Override
    public boolean isColumnPositionSelected(int columnPosition) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.selection.SelectionLayer#isRowPositionFullySelected(int)
     */
    @Override
    public boolean isRowPositionFullySelected(int rowPosition) {
        return getConfig().isRowSelectionEnabled() && super.isRowPositionFullySelected(rowPosition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.selection.SelectionLayer#isRowPositionSelected(int)
     */
    @Override
    public boolean isRowPositionSelected(int rowPosition) {
        return false;
    }
}