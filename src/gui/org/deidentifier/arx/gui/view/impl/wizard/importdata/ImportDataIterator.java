package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.io.IOException;
import java.util.Iterator;

import org.deidentifier.arx.io.CSVDataInput;

/**
 * This class can be used to iterate over the data to import and returns the
 * next row each time {@link #next()} gets invoked. It uses {@link ImportData}
 * to create the iterator for the appropriate source. For now only CSV is
 * supported.
 *
 * TODO Implement for other sources (refactor with subclasses if needed)
 * TODO Implement better with performance in mind
 */
public class ImportDataIterator implements Iterator<String[]> {

    /**
     * A mask indicating which columns to import from
     * 
     * Each element within this mask represents a column. If the element is
     * true it means that data from this column should be imported.
     */
    private Boolean[] columnMask;

    private CSVDataInput in;
    private Iterator<String[]> it;

    public ImportDataIterator(ImportData importData) throws IOException {

        in = new CSVDataInput(importData.getFileLocation(), importData.getCsvSeparator());
        it = in.iterator();

        columnMask = new Boolean[importData.getNumberOfEnabledColumns()];
        int i = 0;

        for (ImportDataColumn column : importData.getColumns()) {

            if (column.isEnabled()) {

                columnMask[i] = true;

            } else {

                columnMask[i] = false;

            }

            i++;

        }

    }

    @Override
    public boolean hasNext() {

        return it.hasNext();

    }

    @Override
    public String[] next() {

        String[] result = new String[columnMask.length];
        String[] row = it.next();

        int i = 0;
        int j = 0;

        for (String column : row) {

            if (columnMask[i]) {

                result[j++] = column;

            }

            i++;

        }

        return result;

    }

    /**
     * Only a dummy, items cannot be removed
     */
    @Override
    public void remove() {

    }

}
