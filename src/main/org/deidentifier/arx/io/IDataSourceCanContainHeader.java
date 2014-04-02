package org.deidentifier.arx.io;


/**
 * Interface for data configurations that can contain a header
 *
 * A header describes the columns itself, e.g. by naming them. Usually it will
 * be the first row, but there might be more complex configurations.
 *
 * TODO: Add getHeader() method and implement it for CSV and XLS
 */
public interface IDataSourceCanContainHeader {

    /**
     * Indicates whether there is header
     *
     * A header is not necessarily mandatory. This returns a boolean value that
     * describes whether or not the configuration contains a header or not.
     *
     * @return True if there is header, false otherwise
     */
    public boolean getContainsHeader();

}
