package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;


/**
 * Implements the editing support for name column within the column page
 *
 * This allows to change the name of columns with the column page
 * {@link WizardImportColumnPage}. The modifications are performed within a
 * simple text field {@link TextCellEditor}. The name itself is stored
 * with the appropriate {@link WizardImportDataColumn} object.
 */
public class WizardImportColumnPageNameEditingSupport extends EditingSupport {

    /**
     * Actual editor
     */
    private TextCellEditor editor;


    /**
     * Creates a new editor for the given {@link TableViewer}.
     *
     * @param viewer The TableViewer this editor is implemented for
     */
    public WizardImportColumnPageNameEditingSupport(TableViewer viewer)
    {

        super(viewer);

        editor = new TextCellEditor(viewer.getTable());

    }

    /**
     * Indicate that all cells within this column can be edited
     */
    @Override
    protected boolean canEdit(Object arg0)
    {

        return true;

    }

    /**
     * Returns a reference to {@link #editor}.
     */
    @Override
    protected CellEditor getCellEditor(Object arg0)
    {

        return editor;

    }

    /**
     * Gets name of column ({@link WizardImportDataColumn#getName()})
     */
    @Override
    protected Object getValue(Object arg0)
    {

        return ((WizardImportDataColumn)arg0).getName();

    }

    /**
     * Sets name of column ({@link WizardImportDataColumn#setName(String)})
     */
    @Override
    protected void setValue(Object element, Object value)
    {

        ((WizardImportDataColumn)element).setName((String)value);
        getViewer().update(element, null);

    }

}
