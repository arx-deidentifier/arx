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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.ScrollSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommandHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;

public class LayerViewport extends ViewportLayer{

    private CTContext context;
    
    public LayerViewport(IUniqueIndexLayer underlyingLayer, CTContext context) {
        super(underlyingLayer);
        this.context = context;
    }
    

    @Override
    public boolean doCommand(ILayerCommand command) {
        
        boolean result = super.doCommand(command);
        
        if (command instanceof ClientAreaResizeCommand) {
            checkScrollBars();
        } else if (command instanceof RecalculateScrollBarsCommand) {
            checkScrollBars();
        } else if (command instanceof StructuralRefreshCommand) {
            checkScrollBars();
        }
        
        return result;
    }

    @Override
    public void moveCellPositionIntoViewport(int scrollableColumnPosition, int scrollableRowPosition) {
        if (!(context.isRowExpanded() && context.isColumnExpanded())) {
            super.moveCellPositionIntoViewport(scrollableColumnPosition, scrollableRowPosition);
        }
    }

    @Override
    public void moveColumnPositionIntoViewport(int scrollableColumnPosition) {
        if (!context.isColumnExpanded()) {
            super.moveColumnPositionIntoViewport(scrollableColumnPosition);
        }
    }


    @Override
    public void moveRowPositionIntoViewport(int scrollableRowPosition) {
        if (!context.isRowExpanded()) {
            super.moveRowPositionIntoViewport(scrollableRowPosition);
        }
    }

    private void checkScrollBars() {

        Listener[] listeners = context.getTable().getListeners(SWT.Resize);
        for (Listener listener : listeners) {
            context.getTable().removeListener(SWT.Resize, listener);
        }
        
        if (context.isColumnExpanded()) {
            context.getTable().getHorizontalBar().setVisible(false);
        }

        if (context.isRowExpanded()) {
            context.getTable().getVerticalBar().setVisible(false);
        }
        
        for (Listener listener : listeners) {
            context.getTable().addListener(SWT.Resize, listener);
        }
    }

    @Override
    protected boolean isLastColumnCompletelyDisplayed() {
        if (context.isColumnExpanded()) return true;
        else return super.isLastColumnCompletelyDisplayed();
    }

    @Override
    protected boolean isLastRowCompletelyDisplayed() {
        if (context.isRowExpanded()) return true;
        else return super.isLastRowCompletelyDisplayed();
    }


    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new RecalculateScrollBarsCommandHandler(this));
        registerCommandHandler(new ScrollSelectionCommandHandler(this));
        registerCommandHandler(new ViewportSelectColumnCommandHandler(this));
        registerCommandHandler(new ViewportSelectRowCommandHandler(this));
        registerCommandHandler(new ViewportDragCommandHandler(this));
    }
}
