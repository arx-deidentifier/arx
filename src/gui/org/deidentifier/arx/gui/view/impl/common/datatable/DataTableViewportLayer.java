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

package org.deidentifier.arx.gui.view.impl.common.datatable;

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

/**
 * 
 */
public class DataTableViewportLayer extends ViewportLayer{

    /**  TODO */
    private DataTableContext context;
    
    /**
     * 
     *
     * @param underlyingLayer
     * @param context
     */
    public DataTableViewportLayer(IUniqueIndexLayer underlyingLayer, DataTableContext context) {
        super(underlyingLayer);
        this.context = context;
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#doCommand(org.eclipse.nebula.widgets.nattable.command.ILayerCommand)
     */
    @Override
    public boolean doCommand(ILayerCommand command) {
        
        if (context.getTable() != null && !context.getTable().isDisposed()) {
            context.getTable().getHorizontalBar().getParent().setRedraw(false);
            context.getTable().getVerticalBar().getParent().setRedraw(false);
        }
        
        boolean result = super.doCommand(command);
        
        if (command instanceof ClientAreaResizeCommand) {
            checkScrollBars();
        } else if (command instanceof RecalculateScrollBarsCommand) {
            checkScrollBars();
        } else if (command instanceof StructuralRefreshCommand) {
            checkScrollBars();
        }
        
        if (context.getTable() != null && !context.getTable().isDisposed()) {
            context.getTable().getHorizontalBar().getParent().setRedraw(true);
            context.getTable().getVerticalBar().getParent().setRedraw(true);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#moveCellPositionIntoViewport(int, int)
     */
    @Override
    public void moveCellPositionIntoViewport(int scrollableColumnPosition, int scrollableRowPosition) {
        if (!(context.isRowExpanded() && context.isColumnExpanded())) {
            super.moveCellPositionIntoViewport(scrollableColumnPosition, scrollableRowPosition);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#moveColumnPositionIntoViewport(int)
     */
    @Override
    public void moveColumnPositionIntoViewport(int scrollableColumnPosition) {
        if (!context.isColumnExpanded()) {
            super.moveColumnPositionIntoViewport(scrollableColumnPosition);
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#moveRowPositionIntoViewport(int)
     */
    @Override
    public void moveRowPositionIntoViewport(int scrollableRowPosition) {
        if (!context.isRowExpanded()) {
            super.moveRowPositionIntoViewport(scrollableRowPosition);
        }
    }

    /**
     * 
     */
    private void checkScrollBars() {
        
        if (context.getTable().isDisposed()) {
            return;
        }

        Listener[] listeners = null;
        if (context.getTable() != null && !context.getTable().isDisposed()) {
            listeners = context.getTable().getListeners(SWT.Resize);
            for (Listener listener : listeners) {
                context.getTable().removeListener(SWT.Resize, listener);
            }
        }
        
        if (context.isColumnExpanded()) {
            context.getTable().getHorizontalBar().setVisible(true);
            context.getTable().getHorizontalBar().setValues(0, 0, 1, 1, 1, 1);
            context.getTable().getHorizontalBar().setEnabled(false);
        }

        if (context.isRowExpanded()) {
            context.getTable().getVerticalBar().setVisible(true);
            context.getTable().getVerticalBar().setValues(0, 0, 1, 1, 1, 1);
            context.getTable().getVerticalBar().setEnabled(false);
        }
        
        if (context.getTable() != null && !context.getTable().isDisposed()) {
            for (Listener listener : listeners) {
                context.getTable().addListener(SWT.Resize, listener);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#isLastColumnCompletelyDisplayed()
     */
    @Override
    protected boolean isLastColumnCompletelyDisplayed() {
        if (context.isColumnExpanded()) return true;
        else return super.isLastColumnCompletelyDisplayed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#isLastRowCompletelyDisplayed()
     */
    @Override
    protected boolean isLastRowCompletelyDisplayed() {
        if (context.isRowExpanded()) return true;
        else return super.isLastRowCompletelyDisplayed();
    }


    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer#registerCommandHandlers()
     */
    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new RecalculateScrollBarsCommandHandler(this));
        registerCommandHandler(new ScrollSelectionCommandHandler(this));
        registerCommandHandler(new ViewportSelectColumnCommandHandler(this));
        registerCommandHandler(new ViewportSelectRowCommandHandler(this));
        registerCommandHandler(new ViewportDragCommandHandler(this));
    }
}
