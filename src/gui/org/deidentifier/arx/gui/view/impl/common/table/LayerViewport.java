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
public class LayerViewport extends ViewportLayer{

    /**  TODO */
    private CTContext context;
    
    /**
     * 
     *
     * @param underlyingLayer
     * @param context
     */
    public LayerViewport(IUniqueIndexLayer underlyingLayer, CTContext context) {
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
