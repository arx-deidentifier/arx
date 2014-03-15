package org.deidentifier.arx.gui.view.impl.importwizard;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;


public class WizardImportColumnPageNameEditingSupport extends EditingSupport {

    private TextCellEditor editor;


    public WizardImportColumnPageNameEditingSupport(TableViewer viewer)
    {

        super(viewer);

        editor = new TextCellEditor(viewer.getTable());

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

        return ((WizardImportDataColumn)arg0).getName();

    }

    @Override
    protected void setValue(Object element, Object value)
    {

        ((WizardImportDataColumn)element).setName((String)value);
        getViewer().update(element, null);

    }

}
