package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.Entry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;


/**
 * Implements the editing support for datatype column within the column page
 *
 * This allows to change the datatype of columns with the column page
 * {@link ColumnPage}. The modifications are performed with a combo box
 * {@link ComboBoxCellEditor}. The datatype itself is stored with the
 * appropriate {@link ImportDataColumn} object.
 *
 * TODO Implement better editor for datatypes of arx framework
 */
public class ColumnPageDatatypeEditingSupport extends EditingSupport {

    /**
     * Actual editor
     */
    private ComboBoxCellEditor editor;

    /**
     * Allowed values for the user to choose from
     */
    private String[] choices;


    /**
     * Creates a new editor for the given {@link TableViewer}.
     *
     * @param viewer The TableViewer this editor is implemented for
     */
    public ColumnPageDatatypeEditingSupport(TableViewer viewer)
    {

        super(viewer);

        List<String> labels = new ArrayList<String>();

        for (Entry<?> entry : DataType.LIST) {

            labels.add(entry.getLabel());

        }

        choices = labels.toArray(new String[labels.size()]);

        editor = new ComboBoxCellEditor(viewer.getTable(), choices, SWT.READ_ONLY);

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
     * Returns index of datatype for given column
     */
    @Override
    protected Object getValue(Object element)
    {

        DataType<?> datatype = ((ImportDataColumn)element).getDatatype();

        int i = 0;

        for (Entry<?> entry : DataType.LIST) {

            if (entry.newInstance().getClass() == datatype.getClass()) {

                return i;

            }

            i++;

        }

        return null;


    }

    /**
     * Sets new datatype for given column
     *
     * Internally this function makes use of
     * {@link ImportDataColumn#setDatatype(Class)}. The values itself are taken
     * from {@link #choices}.
     */
    @Override
    protected void setValue(Object element, Object value)
    {

        String label = choices[(int) value];

        for (Entry<?> entry : DataType.LIST) {

            if (entry.getLabel().equals(label)) {

                ((ImportDataColumn)element).setDatatype(entry.newInstance());
                getViewer().update(element, null);

                return;

            }

        }

    }

}
