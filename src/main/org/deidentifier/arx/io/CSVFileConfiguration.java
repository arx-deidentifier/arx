package org.deidentifier.arx.io;


/**
 * Configuration describing a CSV file
 */
public class CSVFileConfiguration extends DataSourceFileConfiguration implements IDataSourceCanContainHeader {

    /**
     * Character that separates the columns from each other
     */
    private char separator;

    /**
     * Indicates whether first row contains header (names of columns)
     *
     * @see {@link IDataSourceCanContainHeader}
     */
    private boolean containsHeader;


    /**
     * Creates a new instance of this object
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param separator {@link #separator}
     * @param containsHeader {@link #containsHeader}
     */
    public CSVFileConfiguration(String fileLocation, char separator, boolean containsHeader) {

        setFileLocation(fileLocation);
        this.separator = separator;
        this.containsHeader = containsHeader;

    }

    /**
     * @return {@link #separator}
     */
    public char getSeparator() {

        return separator;

    }

    @Override
    public boolean getContainsHeader() {

        return containsHeader;

    }

    @Override
    public void setContainsHeader(boolean containsHeader)
    {

        this.containsHeader = containsHeader;

    }

}
