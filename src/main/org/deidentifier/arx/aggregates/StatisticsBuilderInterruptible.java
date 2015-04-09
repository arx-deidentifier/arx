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

package org.deidentifier.arx.aggregates;

import java.util.Map;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.DataHandleStatistics;
import org.deidentifier.arx.aggregates.StatisticsBuilder.ComputationInterruptedException;


/**
 * A class offering basic descriptive statistics about data handles. Instances of this class
 * can be interrupted and are thus suitable for use in multi-threaded environments.
 * 
 * @author Fabian Prasser
 */
public class StatisticsBuilderInterruptible {

    /** The backing builder. */
    private StatisticsBuilder builder;

    /**
     * Creates a new instance.
     *
     * @param handle
     * @param ecStatistics
     */
    StatisticsBuilderInterruptible(DataHandleStatistics handle,
                                   StatisticsEquivalenceClasses ecStatistics) {
        this.builder = new StatisticsBuilder(handle, ecStatistics);
    }

    /**
     * Returns a contingency table for the given columns.
     *
     * @param column1 The first column
     * @param orderFromDefinition1 Indicates whether the order that should be assumed for string
     *            data items can (and should) be derived from the hierarchy
     *            provided in the data definition (if any)
     * @param column2 The second column
     * @param orderFromDefinition2 Indicates whether the order that should be assumed for string
     *            data items can (and should) be derived from the hierarchy
     *            provided in the data definition (if any)
     * @return
     * @throws InterruptedException
     */
    public StatisticsContingencyTable
            getContingencyTable(int column1,
                                boolean orderFromDefinition1,
                                int column2,
                                boolean orderFromDefinition2) throws InterruptedException {

        try {
            return builder.getContingencyTable(column1,
                                               orderFromDefinition1,
                                               column2,
                                               orderFromDefinition2);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a contingency table for the given columns. The order for string
     * data items is derived from the provided hierarchies
     *
     * @param column1 The first column
     * @param hierarchy1 The hierarchy for the first column, may be null
     * @param column2 The second column
     * @param hierarchy2 The hierarchy for the second column, may be null
     * @return
     * @throws InterruptedException
     */
    public StatisticsContingencyTable
            getContingencyTable(int column1,
                                Hierarchy hierarchy1,
                                int column2,
                                Hierarchy hierarchy2) throws InterruptedException {

        try {
            return builder.getContingencyTable(column1,
                                               hierarchy1,
                                               column2,
                                               hierarchy2);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a contingency table for the given columns. This method assumes
     * that the order of string data items can (and should) be derived from the
     * hierarchies provided in the data definition (if any)
     *
     * @param column1 The first column
     * @param column2 The second column
     * @return
     * @throws InterruptedException
     */
    public StatisticsContingencyTable
            getContingencyTable(int column1, int column2) throws InterruptedException {
        try {
            return builder.getContingencyTable(column1, column2);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a contingency table for the given columns.
     *
     * @param column1 The first column
     * @param size1 The maximal size in this dimension
     * @param orderFromDefinition1 Indicates whether the order that should be assumed for string
     *            data items can (and should) be derived from the hierarchy
     *            provided in the data definition (if any)
     * @param column2 The second column
     * @param size2 The maximal size in this dimension
     * @param orderFromDefinition2 Indicates whether the order that should be assumed for string
     *            data items can (and should) be derived from the hierarchy
     *            provided in the data definition (if any)
     * @return
     * @throws InterruptedException
     */
    public StatisticsContingencyTable
            getContingencyTable(int column1,
                                int size1,
                                boolean orderFromDefinition1,
                                int column2,
                                int size2,
                                boolean orderFromDefinition2) throws InterruptedException {
        try {
            return builder.getContingencyTable(column1,
                                               size1,
                                               orderFromDefinition1,
                                               column2,
                                               size2,
                                               orderFromDefinition2);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a contingency table for the given columns. The order for string
     * data items is derived from the provided hierarchies
     *
     * @param column1 The first column
     * @param size1 The maximal size in this dimension
     * @param hierarchy1 The hierarchy for the first column, may be null
     * @param column2 The second column
     * @param size2 The maximal size in this dimension
     * @param hierarchy2 The hierarchy for the second column, may be null
     * @return
     * @throws InterruptedException
     */
    public StatisticsContingencyTable
            getContingencyTable(int column1,
                                int size1,
                                Hierarchy hierarchy1,
                                int column2,
                                int size2,
                                Hierarchy hierarchy2) throws InterruptedException {
        try {
            return builder.getContingencyTable(column1,
                                               size1,
                                               hierarchy1,
                                               column2,
                                               size2,
                                               hierarchy2);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a contingency table for the given columns. This method assumes
     * that the order of string data items can (and should) be derived from the
     * hierarchies provided in the data definition (if any)
     *
     * @param column1 The first column
     * @param size1 The maximal size in this dimension
     * @param column2 The second column
     * @param size2 The maximal size in this dimension
     * @return
     * @throws InterruptedException
     */
    public StatisticsContingencyTable
            getContingencyTable(int column1, int size1, int column2, int size2) throws InterruptedException {
        try {
            return builder.getContingencyTable(column1, size1, column2, size2);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns the distinct set of data items from the given column.
     *
     * @param column The column
     * @return
     * @throws InterruptedException
     */
    public String[] getDistinctValues(int column) throws InterruptedException {
        try {
            return builder.getDistinctValues(column);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns an ordered list of the distinct set of data items from the given
     * column. This method assumes that the order of string data items can (and
     * should) be derived from the hierarchy provided in the data definition (if
     * any)
     *
     * @param column The column
     * @return
     * @throws InterruptedException
     */
    public String[]
            getDistinctValuesOrdered(int column) throws InterruptedException {
        try {
            return builder.getDistinctValuesOrdered(column);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns an ordered list of the distinct set of data items from the given
     * column.
     *
     * @param column The column
     * @param orderFromDefinition Indicates whether the order that should be assumed for string
     *            data items can (and should) be derived from the hierarchy
     *            provided in the data definition (if any)
     * @return
     * @throws InterruptedException
     */
    public String[]
            getDistinctValuesOrdered(int column, boolean orderFromDefinition) throws InterruptedException {
        try {
            return builder.getDistinctValuesOrdered(column, orderFromDefinition);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns an ordered list of the distinct set of data items from the given
     * column. This method assumes that the order of string data items can (and
     * should) be derived from the provided hierarchy
     *
     * @param column The column
     * @param hierarchy The hierarchy, may be null
     * @return
     * @throws InterruptedException
     */
    public String[]
            getDistinctValuesOrdered(int column, Hierarchy hierarchy) throws InterruptedException {
        try {
            return builder.getDistinctValuesOrdered(column, hierarchy);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns statistics about the equivalence classes.
     *
     * @return
     * @throws InterruptedException
     */
    public StatisticsEquivalenceClasses
            getEquivalenceClassStatistics() throws InterruptedException {
        try {
            return builder.getEquivalenceClassStatistics();
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a frequency distribution for the values in the given column. This
     * method assumes that the order of string data items can (and should) be
     * derived from the hierarchy provided in the data definition (if any)
     *
     * @param column The column
     * @return
     * @throws InterruptedException
     */
    public StatisticsFrequencyDistribution
            getFrequencyDistribution(int column) throws InterruptedException {
        try {
            return builder.getFrequencyDistribution(column);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a frequency distribution for the values in the given column.
     *
     * @param column The column
     * @param orderFromDefinition Indicates whether the order that should be assumed for string
     *            data items can (and should) be derived from the hierarchy
     *            provided in the data definition (if any)
     * @return
     * @throws InterruptedException
     */
    public StatisticsFrequencyDistribution
            getFrequencyDistribution(int column, boolean orderFromDefinition) throws InterruptedException {
        try {
            return builder.getFrequencyDistribution(column, orderFromDefinition);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns a frequency distribution for the values in the given column. The
     * order for string data items is derived from the provided hierarchy
     *
     * @param column The column
     * @param hierarchy The hierarchy, may be null
     * @return
     * @throws InterruptedException
     */
    public StatisticsFrequencyDistribution
            getFrequencyDistribution(int column, Hierarchy hierarchy) throws InterruptedException {
        try {
            return builder.getFrequencyDistribution(column, hierarchy);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " +
                                               e.getMessage());
            }
        }
    }

    /**
     * Returns summary statistics for all attributes. 
     * 
     * @param listwiseDeletion A flag enabling list-wise deletion
     * @return
     * @throws InterruptedException
     */
    public Map<String, StatisticsSummary<?>> getSummaryStatistics(boolean listwiseDeletion) throws InterruptedException {
        try {
            return builder.getSummaryStatistics(listwiseDeletion);
        } catch (Exception e) {
            if (e instanceof ComputationInterruptedException) {
                throw new InterruptedException("Interrupted");
            } else {
                throw new InterruptedException("Interrupted by exception: " + e.getMessage());
            }
        }
    }

    /**
     * Interrupts all computations.
     */
    public void interrupt() {
        builder.interrupt();
    }
}
