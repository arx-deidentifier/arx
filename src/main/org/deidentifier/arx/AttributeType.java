/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionArithmeticMean;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionGeometricMean;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionInterval;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionMedian;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction.DistributionAggregateFunctionMode;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.CSVHierarchyInput;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.io.IOUtil;

/**
 * Represents an attribute type.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class AttributeType implements Serializable, Cloneable { // NO_UCD
    
    /**
     * This class implements a generalization hierarchy.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static abstract class Hierarchy extends AttributeType implements Serializable { // NO_UCD

        /**
         * The default implementation of a generalization hierarchy. It allows
         * the user to programmatically define its content.
         * 
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        public static class DefaultHierarchy extends Hierarchy {

            /** SVUID */
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

            @Override
            public Hierarchy clone() {
                if (array != null) {
                    return new DefaultHierarchy(array);
                } else {
                    return new DefaultHierarchy(getHierarchy());
                }
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

            /**
             * This fixes a bug, where hierarchies which have been loaded from CSV files are trimmed but
             * hierarchies which are deserialized are not. We fix this by implementing custom deserialization.
             * @param ois
             * @throws ClassNotFoundException
             * @throws IOException
             */
            private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
                
                // Default deserialization
                ois.defaultReadObject();
                
                // Trim array
                if (array != null) {
                    for (int row = 0; row < array.length; row++) {
                        if (array[row] != null && array[row].length > 0) {
                            array[row][0] = IOUtil.trim(array[row][0]);
                        }
                    }
                }
                // Trim list
                if (hierarchy != null) {
                    for (int row = 0; row < hierarchy.size(); row++) {
                        if (hierarchy.get(row) != null && hierarchy.get(row).length > 0) {
                            hierarchy.get(row)[0] = IOUtil.trim(hierarchy.get(row)[0]);
                        }
                    }
                }
            }            
        }

        /**
         * The implementation for arrays.
         *
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        static class ArrayHierarchy extends Hierarchy {

            /** SVUID. */
            private static final long serialVersionUID = 8966189950800782892L;

            /** Field */
            private final String[][]  hierarchy;

            /**
             * Instantiates a new array hierarchy.
             *
             * @param hierarchy the hierarchy
             */
            private ArrayHierarchy(final String[][] hierarchy) {
                this.hierarchy = hierarchy;
            }

            @Override
            public Hierarchy clone() {
                return new DefaultHierarchy(getHierarchy());
            }

            @Override
            public String[][] getHierarchy() {
                return hierarchy;
            }

            /**
             * This fixes a bug, where hierarchies which have been loaded from CSV files are trimmed but
             * hierarchies which are deserialized are not. We fix this by implementing custom deserialization.
             * @param ois
             * @throws ClassNotFoundException
             * @throws IOException
             */
            private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
                
                // Default deserialization
                ois.defaultReadObject();
                
                // Trim array
                if (hierarchy != null) {
                    for (int row = 0; row < hierarchy.length; row++) {
                        if (hierarchy[row] != null && hierarchy[row].length > 0) {
                            hierarchy[row][0] = IOUtil.trim(hierarchy[row][0]);
                        }
                    }
                }
            }  
        }

        /**
         * The implementation for iterators.
         *
         * @author Fabian Prasser
         * @author Florian Kohlmayer
         */
        static class IterableHierarchy extends Hierarchy {

            /** SVUID */
            private static final long  serialVersionUID = 5734204406574324342L;

            /** Field */
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

            @Override
            public Hierarchy clone() {
                if (array != null) {
                    return new DefaultHierarchy(array);
                } else {
                    return new DefaultHierarchy(getHierarchy());
                }
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

            /**
             * This fixes a bug, where hierarchies which have been loaded from CSV files are trimmed but
             * hierarchies which are deserialized are not. We fix this by implementing custom deserialization.
             * @param ois
             * @throws ClassNotFoundException
             * @throws IOException
             */
            private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
                
                // Default deserialization
                ois.defaultReadObject();
                
                // Trim array
                String[][] hierarchy = getHierarchy();
                if (hierarchy != null) {
                    for (int row = 0; row < hierarchy.length; row++) {
                        if (hierarchy[row] != null && hierarchy[row].length > 0) {
                            hierarchy[row][0] = IOUtil.trim(hierarchy[row][0]);
                        }
                    }
                }
            }  
        }

        /** SVUID */
        private static final long serialVersionUID = -4721439386792383385L;

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
         * @param charset the charset
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final Charset charset) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, charset).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file A file
         * @param delimiter The utilized separator character
         * @return A Hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final Charset charset, final char delimiter) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, charset, delimiter).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @param charset the charset
         * @param delimiter the delimiter
         * @param quote the quote
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final Charset charset, final char delimiter, final char quote) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, charset, delimiter, quote).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @param charset the charset
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, charset, delimiter, quote, escape).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param file the file
         * @param charset the charset
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @param linebreak the linebreak
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final File file, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, charset, delimiter, quote, escape, linebreak).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         * 
         * @param file
         * @param config
         * @return
         * @throws IOException
         */
        public static Hierarchy create(final File file, final Charset charset, final CSVSyntax config) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(file, charset, config).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final Charset charset) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, charset).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream An input stream
         * @param charset the charset
         * @param delimiter The utilized separator character
         * @return A Hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final Charset charset, final char delimiter) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, charset, delimiter).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @param charset the charset
         * @param delimiter the delimiter
         * @param quote the quote
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final Charset charset, final char delimiter, final char quote) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, charset, delimiter, quote).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @param charset the charset
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, charset, delimiter, quote, escape).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         *
         * @param stream the stream
         * @param charset the charset
         * @param delimiter the delimiter
         * @param quote the quote
         * @param escape the escape
         * @param linebreak the linebreak
         * @return the hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, charset, delimiter, quote, escape, linebreak).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         * @param stream
         * @param charset
         * @param config
         * @return
         * @throws IOException
         */
        public static Hierarchy create(final InputStream stream, final Charset charset, final CSVSyntax config) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(stream, charset, config).getHierarchy());
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
         * @param charset the charset
         * @param separator The utilized separator character
         * @return A Hierarchy
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Hierarchy create(final String path, final Charset charset, final char separator) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(path, charset, separator).getHierarchy());
        }

        /**
         * Creates a new hierarchy from a CSV file.
         * @param path
         * @param charset
         * @param config
         * @return
         * @throws IOException
         */
        public static Hierarchy create(final String path, final Charset charset, final CSVSyntax config) throws IOException {
            return new ArrayHierarchy(new CSVHierarchyInput(path, charset, config).getHierarchy());
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

        /**
         * Instantiates a new hierarchy.
         */
        public Hierarchy() {
            super(ATTR_TYPE_QI);
        }

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
         * @param file the file
         * @param config the config
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final File file, final CSVSyntax config) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(file, config);
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
         * @param out the out
         * @param config the config
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final OutputStream out, final CSVSyntax config) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(out, config);
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

        /**
         * Writes the hierarchy to a CSV file.
         *
         * @param path the path
         * @param config the config
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void save(final String path, final CSVSyntax config) throws IOException {
            final CSVDataOutput output = new CSVDataOutput(path, config);
            output.write(getHierarchy());
        }
    }
    
    /**
     * This class is used to define aggregate functions for microaggregation.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     * @param <T>
     *
     */
    public static class MicroAggregationFunction extends AttributeType implements Serializable {

        /** SVUID */
        private static final long            serialVersionUID = -7175337291872533713L;
        
        /**
         * Creates a microaggregation function returning the arithmetic mean. Ignores missing data.
         * @return
         */
        public static MicroAggregationFunction createArithmeticMean() {
            return createArithmeticMean(true);
        }        

        /**
         * Creates a microaggregation function returning the arithmetic mean.
         * 
         * @param ignoreMissingData Should the function ignore missing data. Default is true.
         * @return
         */
        public static MicroAggregationFunction createArithmeticMean(boolean ignoreMissingData) {
            return new MicroAggregationFunction(new DistributionAggregateFunctionArithmeticMean(ignoreMissingData),
                                                DataScale.INTERVAL, "Arithmetric mean");
        }
        
        /**
         * Creates a microaggregation function returning the geometric mean. Ignores missing data.
         * @return
         */
        public static MicroAggregationFunction createGeometricMean() {
            return createGeometricMean(true);
        }
        
        /**
         * Creates a microaggregation function returning the geometric mean.
         * 
         * @param ignoreMissingData Should the function ignore missing data. Default is true.
         * @return
         */
        public static MicroAggregationFunction createGeometricMean(boolean ignoreMissingData) {
            return new MicroAggregationFunction(new DistributionAggregateFunctionGeometricMean(ignoreMissingData),
                                                DataScale.INTERVAL, "Geometric mean");
        }
        
        /**
         * Creates a microaggregation function returning intervals. Ignores missing data.
         * @return
         */
        public static MicroAggregationFunction createInterval() {
            return createInterval(true);
        }

        /**
         * Creates a microaggregation function returning intervals.
         * 
         * @param ignoreMissingData Should the function ignore missing data. Default is true.
         * @return
         */
        public static MicroAggregationFunction createInterval(boolean ignoreMissingData) {
            return new MicroAggregationFunction(new DistributionAggregateFunctionInterval(ignoreMissingData),
                                                DataScale.ORDINAL, "Interval");
        }
        
        /**
         * Creates a microaggregation function returning the median. Ignores missing data.
         * @return
         */
        public static MicroAggregationFunction createMedian() {
            return createMedian(true);
        }

        /**
         * Creates a microaggregation function returning the median.
         * 
         * @param ignoreMissingData Should the function ignore missing data. Default is true.
         * @return
         */
        public static MicroAggregationFunction createMedian(boolean ignoreMissingData) {
            return new MicroAggregationFunction(new DistributionAggregateFunctionMedian(ignoreMissingData),
                                                DataScale.ORDINAL, "Median");
        }
        
        /**
         * Creates a microaggregation function returning the mode. Ignores missing data.
         * @return
         */
        public static MicroAggregationFunction createMode() {
            return createMode(true);
        }
        
        /**
         * Creates a microaggregation function returning the mode.
         * 
         * @param ignoreMissingData Should the function ignore missing data. Default is true.
         * @return
         */
        public static MicroAggregationFunction createMode(boolean ignoreMissingData) {
            return new MicroAggregationFunction(new DistributionAggregateFunctionMode(ignoreMissingData),
                                                DataScale.NOMINAL, "Mode");
        }
        
        /** The microaggregation function */
        private final DistributionAggregateFunction function;
        
        /** The required scale*/
        private final DataScale requiredScale;
        
        /** The label*/
        private final String label;
        
        /**
         * Instantiates a new hierarchy.
         * @param function
         * @param requiredScale
         * @param label 
         */
        private MicroAggregationFunction(DistributionAggregateFunction function,
                                         DataScale requiredScale,
                                         String label) {
            super(ATTR_TYPE_QI);
            this.function = function;
            this.requiredScale = requiredScale;
            this.label = label;
        }
        
        /**
         * Clones this function
         */
        public MicroAggregationFunction clone() {
            return new MicroAggregationFunction(this.function.clone(),
                                                this.requiredScale,
                                                this.label);
        }

        /**
         * Returns a label for this function
         * @return the label
         */
        public String getLabel() {
            return label;
        }
        
        /**
         * Returns the required scale of measure
         * @return
         */
        public DataScale getRequiredScale() {
            return requiredScale;
        }

        /**
         * Returns whether this is a type-preserving function
         * @return
         */
        public boolean isTypePreserving() {
            return function.isTypePreserving();
        }
        
        /**
         * Returns the aggregate function.
         * @return
         */
        protected DistributionAggregateFunction getFunction() {
            return function;
        }
    }


    /**
     * This class describes a microaggregation function
     * @author Fabian Prasser
     */
    public abstract static class MicroAggregationFunctionDescription implements Serializable { // NO_UCD

        /** SVUID*/
        private static final long serialVersionUID = -6608355532280843693L;

        /** The required scale*/
        private final DataScale requiredScale;
        
        /** The label*/
        private final String label;
        
        /**
         * Instantiates a new hierarchy.
         * @param requiredScale
         * @param label 
         */
        private MicroAggregationFunctionDescription(DataScale requiredScale,
                                                    String label) {
            this.requiredScale = requiredScale;
            this.label = label;
        }
        
        /**
         * Creates an instance
         * @param ignoreMissingData
         * @return
         */
        public abstract MicroAggregationFunction createInstance(boolean ignoreMissingData);

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @return the requiredScale
         */
        public DataScale getRequiredScale() {
            return requiredScale;
        }
    }

    /** SVUID. */
    private static final long   serialVersionUID            = -7358540408016873823L;

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

    /**
     * Lists all available microaggregation functions
     * @return
     */
    public static List<MicroAggregationFunctionDescription> listMicroAggregationFunctions() {
        return Arrays.asList(new MicroAggregationFunctionDescription[] {
                new MicroAggregationFunctionDescription(DataScale.INTERVAL, "Arithmetic mean") {
                    /** SVUID*/ private static final long serialVersionUID = -6625783559253337848L; 
                    @Override public MicroAggregationFunction createInstance(boolean ignoreMissingData) {
                        return MicroAggregationFunction.createArithmeticMean(ignoreMissingData);
                    }
                },
                new MicroAggregationFunctionDescription(DataScale.INTERVAL, "Geometric mean") {
                    /** SVUID */ private static final long serialVersionUID = 1705485004601412223L;
                    @Override public MicroAggregationFunction createInstance(boolean ignoreMissingData) {
                        return MicroAggregationFunction.createGeometricMean(ignoreMissingData);
                    }
                },
                new MicroAggregationFunctionDescription(DataScale.ORDINAL, "Median") {
                    /** SVUID*/ private static final long serialVersionUID = -690899494444878587L; 
                    @Override public MicroAggregationFunction createInstance(boolean ignoreMissingData) {
                        return MicroAggregationFunction.createMedian(ignoreMissingData);
                    }
                },
                new MicroAggregationFunctionDescription(DataScale.ORDINAL, "Interval") {
                    /** SVUID*/ private static final long serialVersionUID = 4266891310821078436L;
                    @Override public MicroAggregationFunction createInstance(boolean ignoreMissingData) {
                        return MicroAggregationFunction.createInterval(ignoreMissingData);
                    }
                },
                new MicroAggregationFunctionDescription(DataScale.NOMINAL, "Mode") {
                    /** SVUID*/ private static final long serialVersionUID = 1803670665142101922L;
                    @Override public MicroAggregationFunction createInstance(boolean ignoreMissingData) {
                        return MicroAggregationFunction.createMode(ignoreMissingData);
                    }
                }
        });
    }

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
