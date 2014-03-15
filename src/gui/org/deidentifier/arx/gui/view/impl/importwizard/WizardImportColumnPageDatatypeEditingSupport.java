package org.deidentifier.arx.gui.view.impl.importwizard;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;


public class WizardImportColumnPageDatatypeEditingSupport extends EditingSupport {

    private ComboBoxCellEditor editor;

    private String[] choices = new String[]{"String", "Numerical", "Date/Time"};


    public WizardImportColumnPageDatatypeEditingSupport(TableViewer viewer)
    {

        super(viewer);

        editor = new ComboBoxCellEditor(viewer.getTable(), choices, SWT.READ_ONLY);

    }

    @Override
    protected boolean canEdit(Object arg0)
    {

        return true;

    }

    @Override
    protected CellEditor getCellEditor(Object arg0)
    {

        return editor;

    }

    @Override
    protected Object getValue(Object arg0)
    {

        return 0;

    }

    @Override
    protected void setValue(Object element, Object value)
    {

        ((WizardImportDataColumn)element).setDatatype(choices[(int)value]);
        getViewer().update(element, null);

    }

}
