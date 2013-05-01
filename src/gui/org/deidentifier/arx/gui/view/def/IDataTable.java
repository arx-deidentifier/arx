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

package org.deidentifier.arx.gui.view.def;

import java.util.Map;

import org.deidentifier.arx.DataHandle;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Listener;

public interface IDataTable {

    public abstract void addSelectionListener(Listener listener);

    public abstract UiBindingRegistry getUiBindingRegistry();

    public abstract ViewportLayer getViewportLayer();

    public abstract void redraw();

    public abstract void reset();

    public abstract void setData(DataHandle handle);

    public abstract void setData(String[][] data);

    public abstract void setData(DataHandle handle, int[] colors, int[] groups);

    public abstract void setData(String[][] data, int[] colors, int[] groups);

    public abstract void setLayoutData(Object data);
}
