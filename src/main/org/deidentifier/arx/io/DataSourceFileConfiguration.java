package org.deidentifier.arx.io;


/**
 * Configuration describing files in general
 *
 * File based configurations should extend this class as the notion of a
 * {@link #fileLocation} is common to all of them.
 */
abstract public class DataSourceFileConfiguration extends DataSourceConfiguration {

    /**
     * Location of file
     */
    private String fileLocation;


    /**
     * @return {@link #fileLocation}
     */
    public String getFileLocation() {

        return fileLocation;

    }

    /**
     * @param fileLocation {@link #fileLocation}
     */
    public void setFileLocation(String fileLocation)
    {

        this.fileLocation = fileLocation;

    }

}
