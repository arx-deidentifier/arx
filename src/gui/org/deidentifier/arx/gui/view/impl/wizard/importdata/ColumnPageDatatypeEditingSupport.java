package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.ARXString;
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
    private String[] choices = new String[] {

        ARXString.class.getSimpleName(),
        ARXDecimal.class.getSimpleName(),
        ARXInteger.class.getSimpleName(),
        ARXDate.class.getSimpleName()

    };


    /**
     * Creates a new editor for the given {@link TableViewer}.
     *
     * @param viewer The TableViewer this editor is implemented for
     */
    public ColumnPageDatatypeEditingSupport(TableViewer viewer)
    {

        super(viewer);

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

        Class<? extends DataType<?>> datatype = ((ImportDataColumn)element).getDatatype();

        int value = 0;

        if (datatype == ARXString.class) {

            value = 0;

        } else if (datatype == ARXDecimal.class) {

            value = 1;

        } else if (datatype == ARXInteger.class) {

            value = 2;

        } else if (datatype == ARXDate.class) {

            value = 3;

        }

        return value;


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

        Class<? extends DataType<?>> datatype = null;

        switch ((int)value) {

        case 0:

            datatype = ARXString.class;
            break;

        case 1:

            datatype = ARXDecimal.class;
            break;

        case 2:
            datatype = ARXInteger.class;
            break;

        case 3:

            datatype = ARXDate.class;
            break;

        }

        ((ImportDataColumn)element).setDatatype(datatype);
        getViewer().update(element, null);

    }

}
