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
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.ScrollSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommandHandler;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;

public class LayerViewport extends ViewportLayer{

    private CTContext context;
    private boolean registered = false;
    
    public LayerViewport(IUniqueIndexLayer underlyingLayer, CTContext context) {
        super(underlyingLayer);
        this.context = context;
    }
    

    @Override
    public boolean doCommand(ILayerCommand command) {
        
        // Register listener
        if (!registered && context.getTable() != null){
            registered = true;
            context.getTable().addControlListener(new ControlAdapter(){
                public void controlResized(ControlEvent arg0) {
                    checkScrollBars();
                }
            });
        }
        
        if (context.isColumnExpanded() && context.isRowExpanded()) {
            return underlyingLayer.doCommand(command);
        } else {
            return super.doCommand(command);
        }
    }

    @Override
    public void moveCellPositionIntoViewport(int scrollableColumnPosition, int scrollableRowPosition) {
        // Ignore
    }

    @Override
    public void moveColumnPositionIntoViewport(int scrollableColumnPosition) {
        // Ignore
    }


    @Override
    public void moveRowPositionIntoViewport(int scrollableRowPosition) {
        // Ignore
    }


    @Override
    public void recalculateScrollBars() {
        // Ignore
    }


    private void checkScrollBars() {

        if (context.isColumnExpanded()) {
            context.getTable().getHorizontalBar().setEnabled(false);
            context.getTable().getHorizontalBar().setMinimum(0);
            context.getTable().getHorizontalBar().setMaximum(1);
        }

        if (context.isRowExpanded()) {
            context.getTable().getVerticalBar().setEnabled(false);
            context.getTable().getVerticalBar().setMinimum(0);
            context.getTable().getVerticalBar().setMaximum(1);
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
