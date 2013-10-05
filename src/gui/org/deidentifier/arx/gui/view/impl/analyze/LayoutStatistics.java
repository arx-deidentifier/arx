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

package org.deidentifier.arx.gui.view.impl.analyze;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class LayoutStatistics implements ILayout {

    private static final String TAB_DISTRIBUTION = Resources.getMessage("StatisticsView.0"); //$NON-NLS-1$
    private static final String TAB_HEATMAP      = Resources.getMessage("StatisticsView.1"); //$NON-NLS-1$
    private static final String TAB_PROPERTIES   = Resources.getMessage("StatisticsView.2"); //$NON-NLS-1$

    private final ComponentTitledFolder     folder;

    public LayoutStatistics(final Composite parent,
                          final Controller controller,
                          final ModelPart target,
                          final ModelPart reset) {

        // Create the tab folder
        folder = new ComponentTitledFolder(parent, controller, null, "id-50");
        final Composite item1 = folder.createItem(TAB_DISTRIBUTION, null);
        item1.setLayout(new FillLayout());
        final Composite item2 = folder.createItem(TAB_HEATMAP, null);
        item2.setLayout(new FillLayout());
        final Composite item3 = folder.createItem(TAB_PROPERTIES, null);
        item3.setLayout(new FillLayout());
        folder.setSelection(0);

        // Create the views
        new ViewDistribution(item1, controller, target, reset);
        new ViewDensity(item2, controller, target, reset);
        new ViewProperties(item3, controller, target, reset);
    }

    public void addSelectionListener(final SelectionListener listener) {
        folder.addSelectionListener(listener);
    }

    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    public void setSelectionIdex(final int index) {
        folder.setSelection(index);
    }
}
