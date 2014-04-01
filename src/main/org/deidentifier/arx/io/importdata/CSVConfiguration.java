package org.deidentifier.arx.io.importdata;

public class CSVConfiguration extends DataSourceConfiguration{

    /**
     * Name of file to import from
     */
    private String file;

    /**
     * Character that separates the column from each other
     */
    private char separator;

    /**
     * Indicates whether first row contains header with names of columns
     */
    private boolean containsHeader;

    /**
     * Creates a new instance
     * @param file
     * @param separator
     * @param containsHeader
     */
    public CSVConfiguration(String file, char separator, boolean containsHeader) {
        this.file = file;
        this.separator = separator;
        this.containsHeader = containsHeader;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @return the separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * @return the containsHeader
     */
    public boolean isContainsHeader() {
        return containsHeader;
    }
}
