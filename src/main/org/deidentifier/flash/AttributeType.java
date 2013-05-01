/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.flash.io.CSVDataOutput;
import org.deidentifier.flash.io.CSVHierarchyInput;

/**
 * Represents an attribute type
 * 
 * @author Prasser, Kohlmayer
 */
public class AttributeType {

    /**
     * This class implements a generalization hierarchy
     * 
     * @author Prasser, Kohlmayer
     */
    public static abstract class Hierarchy extends AttributeType {

        /**
         * The implementation for arrays
         * 
         * @author Prasser, Kohlmayer
         */
        static class ArrayHierarchy extends Hierarchy {

            private final String[][] hierarchy;

            private ArrayHierarchy(final String[][] hierarchy) {
                this.hierarchy = hierarchy;
            }

            @Override
            public Hierarchy clone() {
                final String[][] array = new String[hierarchy.length][];
                for (int i = 0; i < hierarchy.length; i++) {
                    array[i] = Arrays.copyOf(hierarchy[i], hierarchy[i].length);
                }
                return new ArrayHierarchy(hierarchy);
            }

            @Override
            public String[][] getHierarchy() {
                return hierarchy;
            }
        }

        /**
         * The default implementation of a generalization hierarchy. It allows
         * the user to programmatically define its content.
         * 
         * @author Prasser, Kohlmayer
         */
        public static class DefaultHierarchy extends Hierarchy {

            /** The raw data */
            private final List<String[]> hierarchy;

            /** The array */
            private String[][]           array = null;

            public DefaultHierarchy() {
                hierarchy = new ArrayList<String[]>();
            }

            private DefaultHierarchy(final List<String[]> vals) {
                hierarchy = vals;
            }

            /**
             * Adds a row to the tabular representation of this hierarchy
             * 
             * @param row
             */
            public void add(final String... row) {
                hierarchy.add(row);
            }

            @Override
            public Hierarchy clone() {
                final List<String[]> h = new ArrayList<String[]>();
                for (final String[] row : hierarchy) {
                    h.add(Arrays.copyOf(row, row.length));
                }
                return new DefaultHierarchy(h);
            }

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
         * The implementation for iterators
         * 
         * @author Prasser, Kohlmayer
         */
        static class IterableHierarchy extends Hierarchy {

            private Iterator<String[]> iterator;

            /** The array */
            private String[][]         array = null;

            public IterableHierarchy(final Iterator<String[]> iterator) {
                this.iterator = iterator;
            }

            @Override
            public Hierarchy clone() {
                throw new UnsupportedOperationException();
            }

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
         * Creates a new default hierarchy
         * 
         * @param type
         * @return A Hierarchy
         */
        public static DefaultHierarchy create() {
            return new DefaultHierarchy();
        }

        /**
         * Creates a new hierarchy from a CSV file
         * 
         * @param type
         * @param file
         *            A file
         * @param separator
         *            The utilized separator character
         * @return A Hierarchy
         * @throws IOException
         */
        public static Hierarchy
                create(final File file, final char separator) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, separator).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file
         * 
         * @param type
         * @param stream
         *            An input stream
         * @param separator
         *            The utilized separator character
         * @return A Hierarchy
         * @throws IOException
         */
        public static Hierarchy create(final InputStream stream,
                                       final char separator) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, separator).getHierarchy());
        }

        /**
         * Creates a new hierarchy from an iterator over tuples
         * 
         * @param type
         * @param iterator
         *            An iterator
         * @return A Hierarchy
         */
        public static Hierarchy create(final Iterator<String[]> iterator) {
            return new IterableHierarchy(iterator);
        }

        /**
         * Creates a new hierarchy from a list
         * 
         * @param type
         * @param list
         *            The list
         * @return A Hierarchy
         */
        public static Hierarchy create(final List<String[]> list) {
            return new IterableHierarchy(list.iterator());
        }

        /**
         * Creates a new hierarchy from a CSV file
         * 
         * @param type
         * @param path
         *            A path to the file
         * @param separator
         *            The utilized separator character
         * @return A Hierarchy
         * @throws IOException
         */
        public static Hierarchy
                create(final String path, final char separator) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(path, separator).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a two-dimensional string array
         * 
         * @param type
         * @param array
         *            The array
         * @return A Hierarchy
         */
        public static Hierarchy create(final String[][] array) {
            return new ArrayHierarchy(array);
        }

        public Hierarchy() {
            super(ATTR_TYPE_QI);
        }

        @Override
        public abstract Hierarchy clone();

        /**
         * Returns the hierarchy as a two-dimensional string array
         * 
         * @return
         */
        public abstract String[][] getHierarchy();

        /**
         * Writes the hierarchy to a CSV file
         * 
         * @param file
         *            A file
         * @param separator
         *            The utilized separator character
         * @throws IOException
         */
        public void
                save(final File file, final char separator) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(file, separator);
            output.write(getHierarchy());
        }

        /**
         * Writes the hierarchy to a CSV file
         * 
         * @param out
         *            A output stream
         * @param separator
         *            The utilized separator character
         * @throws IOException
         */
        public void
                save(final OutputStream out, final char separator) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(out, separator);
            output.write(getHierarchy());
        }

        /**
         * Writes the hierarchy to a CSV file
         * 
         * @param path
         *            A path
         * @param separator
         *            The utilized separator character
         * @throws IOException
         */
        public void
                save(final String path, final char separator) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(path, separator);
            output.write(getHierarchy());
        }
    }

    /** The shift */
    protected static final int  SHIFT                 = 30;

    /** The mask */
    protected static final int  MASK                  = 0x3fffffff;

    /** Constant for type QI */
    protected static final int  ATTR_TYPE_QI          = 0;

    /** Constant for type SE */
    protected static final int  ATTR_TYPE_SE          = 1;

    /** Constant for type IN */
    protected static final int  ATTR_TYPE_IS          = 2;

    /** Constant for type ID */
    protected static final int  ATTR_TYPE_ID          = 3;

    /** Represents an identifying attribute */
    public static AttributeType IDENTIFYING_ATTRIBUTE = new AttributeType(ATTR_TYPE_ID);

    /** Represents a sensitive attribute */
    public static AttributeType SENSITIVE_ATTRIBUTE   = new AttributeType(ATTR_TYPE_SE);

    /** Represents an insensitive attribute */
    public static AttributeType INSENSITIVE_ATTRIBUTE = new AttributeType(ATTR_TYPE_IS);

    /** The type */
    private int                 type                  = 0x0;

    /** Instantiates a new type */
    private AttributeType(final int type) {
        this.type = type;
    }

    @Override
    public AttributeType clone() {
        return this;
    }

    /** Returns the type identifier */
    protected int getType() {
        return type;
    }

    /** Returns a string representation */
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
            return null;
        }
    }
}
