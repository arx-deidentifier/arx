package org.deidentifier.arx.io.importdata;

import org.deidentifier.arx.DataType;


/**
 * Represents a single data column
 *
 * This represents a single column that will be imported from. Each column
 * consists of an {@link #index}, {@link #name} and {@link #datatype}.
 */
public class Column {

    /**
     * Index of column, starting with 0
     *
     * This is primarily used by file base adapters, e.g. CSV and XLS and
     * describes the number of the column within each row.
     */
    private int index;

    /**
     * Name of the column
     */
    private String name;

    /**
     * Datatype of the column
     */
    private DataType<?> datatype;


    /**
     * Creates a new instance of this object with the given parameters
     *
     * @param index {@link #index}
     * @param index {@link #name}
     * @param datatype {@link #datatype}
     */
    public Column(int index, DataType<?> datatype) {

        this(index, null, datatype);

    }

    /**
     * Creates a new instance of this object with the given parameters
     *
     * @param index {@link #index}
     * @param index {@link #name}
     * @param datatype {@link #datatype}
     */
    public Column(int index, String name, DataType<?> datatype) {

        setIndex(index);
        setName(name);
        setDatatype(datatype);

    }

    /**
     * @return {@link #name}
     */
    public String getName()
    {

        return name;

    }

    /**
     * @param name {@link #name}
     */
    public void setName(String name)
    {

        this.name = name;

    }

    /**
     * @return {@link #datatype}
     */
    public DataType<?> getDatatype()
    {

        return datatype;

    }

    /**
     * @param datatype {@link #datatype}
     */
    public void setDatatype(DataType<?> datatype)
    {

        this.datatype = datatype;

    }

    /**
     * @return {@link #index}
     */
    public int getIndex() {

        return index;

    }

    /**
     * @param index {@link #index}
     */
    public void setIndex(int index) {

        this.index = index;

    }

    /**
     * @return String representation of a column
     */
    @Override
    public String toString() {

        return "Column [index=" + index + ", name=" + name + ", datatype=" + datatype + "]";

    }

}
