/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.impl.analyze;

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IStatisticsView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class StatisticsView implements IStatisticsView {

    private static final String TAB_DISTRIBUTION = Resources.getMessage("StatisticsView.0"); //$NON-NLS-1$
    private static final String TAB_HEATMAP      = Resources.getMessage("StatisticsView.1"); //$NON-NLS-1$
    private static final String TAB_PROPERTIES   = Resources.getMessage("StatisticsView.2"); //$NON-NLS-1$

    private final TabFolder     folder;

    public StatisticsView(final Composite parent,
                          final Controller controller,
                          final EventTarget target,
                          final EventTarget reset) {

        // Create the tab folder
        folder = new TabFolder(parent, SWT.NONE);

        // Create the tabs
        final TabItem tabDistribution = new TabItem(folder, SWT.NULL);
        tabDistribution.setText(TAB_DISTRIBUTION);
        final Composite tabDistributionComposite = new Composite(folder,
                                                                 SWT.NONE);
        tabDistributionComposite.setLayout(new FillLayout());
        tabDistribution.setControl(tabDistributionComposite);

        final TabItem tabHeatmap = new TabItem(folder, SWT.NULL);
        tabHeatmap.setText(TAB_HEATMAP);
        final Composite tabHeatmapComposite = new Composite(folder, SWT.NONE);
        tabHeatmapComposite.setLayout(new FillLayout());
        tabHeatmap.setControl(tabHeatmapComposite);

        final TabItem tabProperties = new TabItem(folder, SWT.NULL);
        tabProperties.setText(TAB_PROPERTIES);
        final Composite tabPropertiesComposite = new Composite(folder, SWT.NONE);
        tabPropertiesComposite.setLayout(new FillLayout());
        tabProperties.setControl(tabPropertiesComposite);

        // Create the views
        new DistributionView(tabDistributionComposite,
                             controller,
                             target,
                             reset);
        new DensityView(tabHeatmapComposite, controller, target, reset);

        new PropertiesView(tabPropertiesComposite, controller, target, reset);
    }

    @Override
    public void addSelectionListener(final SelectionListener listener) {
        folder.addSelectionListener(listener);
    }

    @Override
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    @Override
    public void setSelectionIdex(final int index) {
        folder.setSelection(index);
    }
}
