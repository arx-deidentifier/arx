/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.gui.view.impl.masking;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This implements the distribution table table
 *
 * @author Karol Babioch
 */
public class ViewVariableDistributionTable implements IView {

    private Controller controller;

    public ViewVariableDistributionTable(final Composite parent, final Controller controller) {

        this.controller = controller;

        build(parent);

        this.controller.addListener(ModelPart.MASKING_VARIABLE_SELECTED, this);

    }

    private void build(Composite parent) {


        // Create table
        TableViewer tableViewer = SWTUtil.createTableViewer(parent, SWT.BORDER);
        tableViewer.setContentProvider(new ArrayContentProvider());

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());


        // Column containing X values
        TableViewerColumn tableViewerColumnX = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnX.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((String[])element)[0];

            }

        });

        TableColumn columnX = tableViewerColumnX.getColumn();
        columnX.setToolTipText("X values");
        columnX.setText("X");
        columnX.setWidth(100);


        // Column containing Y values
        TableViewerColumn tableViewerColumnY = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnY.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((String[])element)[1];

            }

        });

        TableColumn columnY = tableViewerColumnY.getColumn();
        columnY.setToolTipText("Y values");
        columnY.setText("Y");
        columnY.setWidth(100);

        List<String[]> list = new ArrayList<>();
        list.add(new String[] {"0", "0"});
        list.add(new String[] {"1", "0.4"});
        list.add(new String[] {"2", "0.6"});
        tableViewer.setInput(list);

    }

    @Override
    public void dispose() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(ModelEvent event) {

    }

}
