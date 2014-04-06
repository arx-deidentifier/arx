package org.deidentifier.arx.io;

import org.apache.commons.io.FilenameUtils;


/**
 * Configuration describing a Excel files
 *
 * This is used to describe Excel files. Both file types (XLS and XLSX) are
 * supported. The file type can either be detected automatically by the file
 * extension, or alternatively can be set manually. Furthermore there is a
 * sheet index {@link #sheetIndex}, which describes which sheet within the
 * file should be used.
 */
public class ExcelFileConfiguration extends DataSourceFileConfiguration implements IDataSourceCanContainHeader {

    /**
     * Valid file types for Excel files
     *
     * XLS is the "old" Excel file type, XLSX is the "new" Excel file type.
     */
    public enum ExcelFileTypes {XLS, XLSX};

    /**
     * Used file type
     *
     * This is the actual filetype that will be used
     *
     * @see {@link #setExcelFileType(ExcelFileTypes excelFileType)}
     */
    private ExcelFileTypes excelFileType;

    /**
     * Sheet index
     */
    private int sheetIndex;

    /**
     * Indicates whether first row contains header (names of columns)
     *
     * @see {@link IDataSourceCanContainHeader}
     */
    private boolean containsHeader;


    /**
     * Creates a new instance of this object without specifying the file type
     *
     * The file type will be detected automatically using the file extension.
     * By default "xlsx" is assumed. In case the file extension is "xls" the
     * file type will be set to {@link ExcelFileTypes#XLS}.
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param sheetIndex {@link #sheetIndex}
     * @param containsHeader {@link #containsHeader}
     */
    public ExcelFileConfiguration(String fileLocation, int sheetIndex, boolean containsHeader) {

        ExcelFileTypes excelFileType;
        String ext = FilenameUtils.getExtension(fileLocation);

        switch (ext) {

            case "xls":
                excelFileType = ExcelFileTypes.XLS;
                break;

            default:
                excelFileType = ExcelFileTypes.XLSX;
                break;

        }

        setFileLocation(fileLocation);
        setSheetIndex(sheetIndex);
        setContainsHeader(containsHeader);
        setExcelFileType(excelFileType);

    }

    /**
     * Creates a new instance of this object
     *
     * @param fileLocation {@link #setFileLocation(String)}
     * @param excelFileType {@link #setExcelFileType(ExcelFileTypes)}
     * @param sheetIndex {@link #setSheetIndex(int)}
     * @param containsHeader {@link #setContainsHeader(boolean)}
     */
    public ExcelFileConfiguration(String fileLocation, ExcelFileTypes excelFileType, int sheetIndex, boolean containsHeader) {

        setFileLocation(fileLocation);
        setExcelFileType(excelFileType);
        setSheetIndex(sheetIndex);
        setContainsHeader(containsHeader);

    }

    /**
     * @return {@link #ExcelFileTypes}
     */
    public ExcelFileTypes getExcelFileType()
    {

        return excelFileType;

    }

    /**
     * @param excelFileType {@link #ExcelFileTypes}
     */
    public void setExcelFileType(ExcelFileTypes excelFileType)
    {

        this.excelFileType = excelFileType;

    }

    /**
     * @return {@link #sheetIndex}
     */
    public int getSheetIndex()
    {

        return sheetIndex;

    }

    /**
     * @param sheetIndex {@link #sheetIndex}
     */
    public void setSheetIndex(int sheetIndex)
    {

        this.sheetIndex = sheetIndex;

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
