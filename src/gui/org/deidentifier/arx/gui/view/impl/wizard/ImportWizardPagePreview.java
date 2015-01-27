/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.io.IImportColumnIndexed;
import org.deidentifier.arx.io.ImportColumn;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Preview page
 * 
 * This page gives the user a preview over the data and how it is about to be
 * imported. Only enabled columns will be displayed {@link ImportWizardModelColumn#isEnabled()}. The datatype of each column will
 * be shown additionally as a tooltip.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 * @note Note that only up to {@link ImportWizardModel#previewDataMaxLines} lines will be shown.
 * 
 */
public class ImportWizardPagePreview extends WizardPage {

    /**
     * Returns cell content for each column
     * 
     * The data itself comes in form of
     * {@link ImportWizardModel#getPreviewData()}. This class is a wrapper
     * around the appropriate string arrays and makes specific fields available
     * to the appropriate column.
     */
    private class PreviewColumnLabelProvider extends ColumnLabelProvider {

        /** Column index this provider is meant to be for, starting with 0. */
        private int index;

        /**
         * Creates new instance of this object for given index.
         *
         * @param index Index of column this instance is for
         */
        public PreviewColumnLabelProvider(int index) {
            this.index = index;
        }

        /**
         * Returns content for this column {@link #index}.
         *
         * @param element
         * @return
         */
        @Override
        public String getText(Object element) {
            return ((String[]) element)[index];
        }

        /**
         * Returns tooltip for a particular cell
         * 
         * This will return the datatype and potentially the associated format
         * with the datatype for each cell.
         *
         * @param element
         * @return
         */
        @Override
        public String getToolTipText(Object element) {

            DataType<?> datatype = wizardImport.getData()
                                               .getWizardColumns()
                                               .get(index)
                                               .getColumn()
                                               .getDataType();

            String result = "Datatype: " + datatype.getDescription().getLabel();

            /* Add format for appropriate data types */
            if (datatype.getDescription().hasFormat()) {
                result += ", format: " +
                          ((DataTypeWithFormat) datatype).getFormat();
            }
            return result;
        }
    }
    
    /** Reference to the wizard containing this page. */
    private ImportWizard wizardImport;
    
    /**  TODO */
    private Table        table;

    /**  TODO */
    private TableViewer  tableViewer;

    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPagePreview(ImportWizard wizardImport) {

        super("WizardImportPreviewPage");
        this.wizardImport = wizardImport;

        setTitle("Preview");
        setDescription("Please check whether everything is right");

    }

    /**
     * Creates the design of this page
     * 
     * The page is set to incomplete at this point and will be marked as
     * complete, once the appropriate table has been rendered. Refer to {@link #setVisible(boolean)} for details.
     *
     * @param parent
     */
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        tableViewer = new TableViewer(container, SWT.BORDER |
                                                 SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new ArrayContentProvider());

        table = tableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        setPageComplete(false);
    }

    /**
     * Adds input to table once page gets visible to the user
     * 
     * This retrieves the preview data {@link ImportWizardModel#getPreviewData()} and applies it to the given {@link #tableViewer}. Only columns that have been enabled will be shown.
     * New names and reordering is also considered.
     *
     * @param visible
     */
    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);

        if (visible) {

            /* Disable rendering until everything is finished */
            table.setRedraw(false);

            /* Remove old columns */
            while (table.getColumnCount() > 0) {
                table.getColumns()[0].dispose();
            }

            /* Add enabled columns with appropriate label providers */
            for (ImportColumn column : wizardImport.getData().getEnabledColumns()) {

                TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer,
                                                                            SWT.NONE);
                tableViewerColumn.setLabelProvider(new PreviewColumnLabelProvider(((IImportColumnIndexed) column).getIndex()));

                TableColumn tblclmnColumn = tableViewerColumn.getColumn();
                tblclmnColumn.setToolTipText("Datatype: " +
                                             column.getDataType());
                tblclmnColumn.setWidth(100);
                tblclmnColumn.setText(column.getAliasName());

                ColumnViewerToolTipSupport.enableFor(tableViewer,
                                                     ToolTip.NO_RECREATE);
            }

            /* Apply input to tableViewer */
            tableViewer.setInput(wizardImport.getData().getPreviewData());

            /* Make table visible again */
            table.layout();
            table.setRedraw(true);

            setPageComplete(true);

        } else {
            setPageComplete(false);
        }
    }
}
