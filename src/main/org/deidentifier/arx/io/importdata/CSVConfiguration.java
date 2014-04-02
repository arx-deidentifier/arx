package org.deidentifier.arx.io.importdata;

/**
 * Configuration describing a CSV file
 */
public class CSVConfiguration extends DataSourceConfiguration{

    /**
     * Location of file to import from
     */
    private String fileLocation;

    /**
     * Character that separates the columns from each other
     */
    private char separator;

    /**
     * Indicates whether first row contains header (names of columns)
     */
    private boolean containsHeader;


    /**
     * Creates a new instance of this object
     *
     * @param fileLocation {@link #fileLocation}
     * @param separator {@link #separator}
     * @param containsHeader {@link #containsHeader}
     */
    public CSVConfiguration(String fileLocation, char separator, boolean containsHeader) {

        this.fileLocation = fileLocation;
        this.separator = separator;
        this.containsHeader = containsHeader;

    }

    /**
     * @return {@link #fileLocation}
     */
    public String getFileLocation() {

        return fileLocation;

    }

    /**
     * @return {@link #separator}
     */
    public char getSeparator() {

        return separator;

    }

    /**
     * @return {@link #containsHeader}
     */
    public boolean containsHeader() {

        return containsHeader;

    }

}
