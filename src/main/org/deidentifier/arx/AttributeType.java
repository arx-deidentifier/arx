/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.CSVHierarchyInput;

/**
 * Represents an attribute type.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class AttributeType implements Serializable, Cloneable {

    /**
     * This class implements a generalization hierarchy.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static abstract class Hierarchy extends AttributeType implements Serializable {

        /**
         * The default implementation of a generalization hierarchy. It allows
         * the user to programmatically define its content.
         * 
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        public static class DefaultHierarchy extends Hierarchy {

            /** TODO. */
            private static final long    serialVersionUID = 7493568420925738049L;

            /** The raw data. */
            private final List<String[]> hierarchy;

            /** The array. */
            private String[][]           array            = null;

            /**
             * Instantiates a new default hierarchy.
             */
            public DefaultHierarchy() {
                hierarchy = new ArrayList<String[]>();
            }

            /**
             * Instantiates a new default hierarchy.
             *
             * @param array the array
             */
            private DefaultHierarchy(final String[][] array) {
                this.array = array;
                hierarchy = null;
            }

            /**
             * Adds a row to the tabular representation of this hierarchy.
             *
             * @param row the row
             */
            public void add(final String... row) {
                hierarchy.add(row);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.deidentifier.arx.AttributeType.Hierarchy#clone()
             */
            @Override
            public Hierarchy clone() {
                if (array != null) {
                    return new DefaultHierarchy(array);
                } else {
                    return new DefaultHierarchy(getHierarchy());
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.deidentifier.arx.AttributeType.Hierarchy#getHierarchy()
             */
            @Override
            public String[][] getHierarchy() {
                if (array == null) {
                    array = new String[hierarchy.size()][];
                    for (int i = 0; i < hierarchy.size(); i++) {
                        array[i] = hierarchy.get(i);
                    }
                    hierarchy.clear();
                }
                return array;
            }
        }

        /**
         * The implementation for arrays.
         *
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        static class ArrayHierarchy extends Hierarchy {

            /** TODO. */
            private static final long serialVersionUID = 8966189950800782892L;

            /** TODO. */
            private final String[][]  hierarchy;

            /**
             * Instantiates a new array hierarchy.
             *
             * @param hierarchy the hierarchy
             */
            private ArrayHierarchy(final String[][] hierarchy) {
                this.hierarchy = hierarchy;
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.deidentifier.arx.AttributeType.Hierarchy#clone()
             */
            @Override
            public Hierarchy clone() {
                return new DefaultHierarchy(getHierarchy());
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.deidentifier.arx.AttributeType.Hierarchy#getHierarchy()
             */
            @Override
            public String[][] getHierarchy() {
                return hierarchy;
            }
        }

        /**
         * The implementation for iterators.
         *
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        static class IterableHierarchy extends Hierarchy {

            /** TODO. */
            private static final long  serialVersionUID = 5734204406574324342L;

            /** TODO. */
            private Iterator<String[]> iterator;

            /** The array. */
            private String[][]         array            = null;

            /**
             * Instantiates a new iterable hierarchy.
             *
             * @param iterator the iterator
             */
            public IterableHierarchy(final Iterator<String[]> iterator) {
                this.iterator = iterator;
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.deidentifier.arx.AttributeType.Hierarchy#clone()
             */
            @Override
            public Hierarchy clone() {
                if (array != null) {
                    return new DefaultHierarchy(array);
                } else {
                    return new DefaultHierarchy(getHierarchy());
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.deidentifier.arx.AttributeType.Hierarchy#getHierarchy()
             */
            @Override
            public String[][] getHierarchy() {
                if (array == null) {
                    final List<String[]> list = new ArrayList<String[]>();
                    while (iterator.hasNext()) {
                        list.add(iterator.next());
                    }
                    array = new String[list.size()][];
                    for (int i = 0; i < list.size(); i++) {
                        array[i] = list.get(i);
                    }
                    iterator = null;
                }
                return array;
            }
        }

        /**
         * Creates a new default hierarchy.
         *
         * @return A Hierarchy
         */
        public static DefaultHierarchy create() {
            return new DefaultHierarchy();
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file A file
         * @param delimiter The utilized separator character
         * @return A Hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final char delimiter) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, delimiter).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @param delimiter the delimiter
         * @param quote the quote
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final char delimiter, final char quote) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, delimiter, quote).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final char delimiter, final char quote, final char escape) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, delimiter, quote, escape).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @param linebreak the linebreak
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, delimiter, quote, escape, linebreak).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream An input stream
         * @param delimiter The utilized separator character
         * @return A Hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final char delimiter) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, delimiter).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @param delimiter the delimiter
         * @param quote the quote
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final char delimiter, final char quote) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, delimiter, quote).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final char delimiter, final char quote, final char escape) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, delimiter, quote, escape).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @param linebreak the linebreak
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, delimiter, quote, escape, linebreak).getHierarchy());
        }

        /**
         * Creates a new hierarchy from an iterator over tuples.
         *
         * @param iterator An iterator
         * @return A Hierarchy
         */
        public static Hierarchy create(final Iterator<String[]> iterator) {
            return new IterableHierarchy(iterator);
        }

        /**
         * Creates a new hierarchy from a list.
         *
         * @param list The list
         * @return A Hierarchy
         */
        public static Hierarchy create(final List<String[]> list) {
            return new IterableHierarchy(list.iterator());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param path A path to the file
         * @param separator The utilized separator character
         * @return A Hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final String path, final char separator) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(path, separator).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a two-dimensional string array.
         *
         * @param array The array
         * @return A Hierarchy
         */
        public static Hierarchy create(final String[][] array) {
            return new ArrayHierarchy(array);
        }

        /** TODO. */
        private static final long serialVersionUID = -4721439386792383385L;

        /**
         * Instantiates a new hierarchy.
         */
        public Hierarchy() {
            super(ATTR_TYPE_QI);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.AttributeType#clone()
         */
        @Override
        public abstract Hierarchy clone();

        /**
         * Returns the hierarchy as a two-dimensional string array.
         *
         * @return the hierarchy
         */
        public abstract String[][] getHierarchy();

        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param file the file
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final File file) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(file);
            output.write(getHierarchy());
        }
        
        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param file A file
         * @param delimiter The utilized separator character
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final File file, final char delimiter) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(file, delimiter);
            output.write(getHierarchy());
        }
        
        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param out the out
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final OutputStream out) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(out);
            output.write(getHierarchy());
        }
        
        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param out A output stream
         * @param delimiter The utilized separator character
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final OutputStream out, final char delimiter) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(out, delimiter);
            output.write(getHierarchy());
        }
        
        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param path the path
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final String path) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(path);
            output.write(getHierarchy());
        }

        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param path A path
         * @param delimiter The utilized separator character
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final String path, final char delimiter) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(path, delimiter);
            output.write(getHierarchy());
        }
    }

    /** TODO. */
    private static final long   serialVersionUID            = -7358540408016873823L;

    /** The shift. */
    protected static final int  SHIFT                       = 30;

    /** The mask. */
    protected static final int  MASK                        = 0x3fffffff;

    /** Constant for type QI. */
    protected static final int  ATTR_TYPE_QI                = 0;

    /** Constant for type SE. */
    protected static final int  ATTR_TYPE_SE                = 1;

    /** Constant for type IN. */
    protected static final int  ATTR_TYPE_IS                = 2;

    /** Constant for type ID. */
    protected static final int  ATTR_TYPE_ID                = 3;

    /** Represents an identifying attribute. */
    public static AttributeType IDENTIFYING_ATTRIBUTE       = new AttributeType(ATTR_TYPE_ID);

    /** Represents a sensitive attribute. */
    public static AttributeType SENSITIVE_ATTRIBUTE         = new AttributeType(ATTR_TYPE_SE);

    /** Represents an insensitive attribute. */
    public static AttributeType INSENSITIVE_ATTRIBUTE       = new AttributeType(ATTR_TYPE_IS);

    /** Represents a quasi-identifying attribute. */
    public static AttributeType QUASI_IDENTIFYING_ATTRIBUTE = new AttributeType(ATTR_TYPE_QI);

    /** The type. */
    private int                 type                        = 0x0;

    /**
     * Instantiates a new type.
     *
     * @param type the type
     */
    private AttributeType(final int type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public AttributeType clone() {
        return this;
    }

    /**
     * Returns a string representation.
     *
     * @return the string
     */
    @Override
    public String toString() {
        switch (type) {
        case ATTR_TYPE_ID:
            return "IDENTIFYING_ATTRIBUTE";
        case ATTR_TYPE_SE:
            return "SENSITIVE_ATTRIBUTE";
        case ATTR_TYPE_IS:
            return "INSENSITIVE_ATTRIBUTE";
        case ATTR_TYPE_QI:
            return "QUASI_IDENTIFYING_ATTRIBUTE";
        default:
            return "UNKNOWN_ATTRIBUTE_TYPE";
        }
    }

    /**
     * Returns the type identifier.
     *
     * @return the type
     */
    protected int getType() {
        return type;
    }
}
