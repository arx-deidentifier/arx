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

package org.deidentifier.arx.risk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;

/**
 * This class is the frontend for computing different dislcosure risk measures
 * for a given set of micro data
 * 
 * @author Michael Schneider
 * @author Fabian Prasser
 * @version 1.0
 */
public class RiskEstimator {

    /**
     * Allows to include or exclude the SNB Model. If true, the SNBModel is
     * excluded
     */
    public boolean                      exlcudeSNB = true;

    /**
     * Size of biggest equivalence class in the data set
     */
    private int                         cMax;

    /**
     * Size of smallest equivalence class in the data set
     */
    private int                         cMin;

    /**
     * Map containing the equivalence class sizes (as keys) of the data set and
     * the corresponding frequency (as values) e.g. if the key 2 has value 3
     * then there are 3 equivalence classes of size two.
     */
    private final Map<Integer, Integer> eqClasses;

    /**
     * Sampling fraction, i.e. ration of sample size to population size
     */
    private double                      samplingFraction;

    /**
     * Creates a new instance of a class that allows to estimate different risk
     * measures for a given data set with a default sampling fraction of 0.1
     * 
     * @param handle
     *            This class provides access to dictionary encoded data.
     */
    public RiskEstimator(final DataHandle handle) {
        this(handle, 0.1d);
    }

    /**
     * Creates a new instance of a class that allows to estimate different risk
     * measures for a given data set
     * 
     * @param handle This class provides access to dictionary encoded data.
     * 
     * @param pi sampling fraction, defaults to 0.1
     */
    public RiskEstimator(final DataHandle handle, final double pi) {
        if ((pi == 0) || (pi > 1)) {
            this.samplingFraction = 0.1;
        } else {
            this.samplingFraction = pi;
        }

        // create map containing the equivalence class sizes (as keys) of the
        // data set and the corresponding frequency (as values)
        this.eqClasses = getEquivalenceClasses(handle);

        // set values for Cmin and Cmax
        initialize();
    }

    /**
     * The equivalence class risk denotes a risk measure which gives an average
     * of the whole data set. It is an aggregated, average risk for an
     * individual in the sample to be identifiable based on his
     * quasi-identifiers without further knowledge
     * 
     * @return the average risk of the file using as information only the data
     *         set (corresponding to a file level journalist risk)
     */
    public double getEquivalenceClassRisk() {
        final ModelEquivalenceClass equiModel = new ModelEquivalenceClass(eqClasses);
        return equiModel.getRisk();
    }

    /**
     * This functions takes a user defined data set and the defined
     * quasi-identifiers and marks the entries with the highest
     * re-identification risk. The estimate of the re-identification risk is
     * based solely on the data set and there is no population estimate that
     * plays into the calculation of the re-identification risk.<br>
     * <br>
     * As a side effect, this method may sort the data handle
     * 
     * @param definition
     *            Encapsulates a definition of the types of attributes contained
     *            in a given data set
     * @param handle
     *            This class provides access to dictionary encoded data.
     * @return An array, in which the i-th entry contains the size of the equivalence class in which
     *         the i-th data entry is contained
     */
    public int[] getEquivalenceClassSizes(final DataDefinition definition,
                                          final DataHandle handle) {

        // Sort by quasi-identifiers
        final int[] indices = new int[definition.getQuasiIdentifyingAttributes().size()];
        int index = 0;
        for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        handle.sort(true, indices);

        // Iterate over all equivalence classes and update array of equivalence classes
        int size = 0;
        boolean newClass = false;
        final int[] classes = new int[handle.getNumRows()];

        for (int row = 0; row < (handle.getNumRows() - 1); row++) {

            size++;
            // Discriminate equivalence classes
            newClass = false;
            for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
                final int column = handle.getColumnIndexOf(attribute);
                if (!handle.getValue(row, column).equals(handle.getValue(row + 1, column))) {
                    newClass = true;
                    break;
                }
            }

            // Update entries
            if (newClass) {
                for (int j = 0; j < size; j++) {
                    classes[row - j] = size;
                }
                size = 0;
            }

            // Correct last entry
            if (row == (handle.getNumRows() - 2)) {
                if (!newClass) {
                    size++;
                    for (int j = 0; j < size; j++) {
                        classes[(row + 1) - j] = size;
                    }
                } else {
                    classes[row + 1] = 1;
                }
            }
        }

        /**
         * Classes is now a array where every array element indicates the size
         * of the corresponding equivalence class this allows to manipulate
         * attributes of single array elements
         */
        return classes;
    }

    /**
     * The highest individual risk return the highest risk for an individual
     * entry in the data set. For a sample unique entry the risk is maximal
     * (one). This is to say, that it is possible to re-identify an individual
     * with certainty if there is certain knowledge about the quasi-identifying
     * attributes of this individual. Nevertheless, this measure will normally
     * strongly overestimate the actual re-identification risk since not every
     * sample unique entry will be population unique
     * 
     * @return the highest risk for a single entry in the data set.
     */
    public double getHighestIndividualRisk() {
        if (cMin != 0) {
            return (1 / (double) cMin);
        } else {
            return Double.NaN;
        }
    }

    /**
     * Number of entries that are most likely to be re-identified based (risk
     * corresponds to the HighestIndividualRisk) on the sample information
     * 
     * @return number of entries that belong to highest risk category
     */
    public double getHighestRiskAffected() {
        if (cMin != 0) {
            return eqClasses.get(cMin);
        } else {
            return Double.NaN;
        }

    }

    /**
     * Returns the size of the largest equivalence class
     * @return
     */
    public int getMaximalClassSize() {
        return this.cMax;
    }

    /**
     * Returns the size of the smallest equivalence class
     * @return
     */
    public int getMinimalClassSize() {
        return this.cMin;
    }

    /**
     * This class computes the percentage of population uniques, i.e. the
     * entries that are unique in a population. The population parameters are
     * estimated according to different models. We use a model proposed by
     * Dankar et al. (Fida Dankar, Khaled El Emam, Angelica Neisa, and Tyson
     * Roffey. Estimating the re-identification risk of clinical data sets. BMC
     * Medical Informatics and Decision Making, 12(1):66, 2012.) that has been
     * modified for practical purposes to estimate the population and its
     * parameters and to compute the number of population uniques.
     * 
     * @return the percentage of population uniques as an estimate based on our
     *         data set. This is a common measure for disclosure risk.
     */
    public double getPopulationUniquesRisk() throws IllegalStateException {
        double result;

        if (exlcudeSNB) {
            /*
             * Selection rule, according to Danker et al, 2010, modified to
             * exclude the SNB model and anonymized data
             */
            if (eqClasses.containsKey(1) && !eqClasses.containsKey(2)) {
                final ModelZayatz model = new ModelZayatz(samplingFraction,
                                                          eqClasses);
                result = model.getRisk();
                return result;
            }
            if (!eqClasses.containsKey(1)) {
                throw new IllegalStateException("The data set does not contain any sample uniques! Computing Population Uniques not possible!");
            }

            if (samplingFraction <= 0.1) {
                final ModelPitman model = new ModelPitman(samplingFraction,
                                                          eqClasses);
                result = model.getRisk();
                if (Double.isNaN(result)) {
                    final ModelZayatz zayatzModel = new ModelZayatz(samplingFraction,
                                                                    eqClasses);
                    result = zayatzModel.getRisk();
                }
            } else {
                final ModelZayatz model = new ModelZayatz(samplingFraction,
                                                          eqClasses);
                result = model.getRisk();
                if (Double.isNaN(result)) {
                    final ModelPitman pitmanModel = new ModelPitman(samplingFraction,
                                                                    eqClasses);
                    result = pitmanModel.getRisk();
                }
            }
            return result;
        } else {

            /*
             * Selection rule, according to Danker et al, 2010
             */
            if (eqClasses.containsKey(1) && !eqClasses.containsKey(2)) {
                final ModelZayatz model = new ModelZayatz(samplingFraction,
                                                          eqClasses);
                result = model.getRisk();
                return result;
            }
            if (!eqClasses.containsKey(1)) {
                throw new IllegalStateException("The data set does not contain any sample uniques! Computing Population Uniques not possible!");
            }

            if (samplingFraction <= 0.1) {
                final ModelPitman model = new ModelPitman(samplingFraction,
                                                          eqClasses);
                result = model.getRisk();
                if (Double.isNaN(result)) {
                    final ModelZayatz zayatzModel = new ModelZayatz(samplingFraction,
                                                                    eqClasses);
                    result = zayatzModel.getRisk();
                }
            } else {
                final ModelZayatz model = new ModelZayatz(samplingFraction,
                                                          eqClasses);
                final ModelSNB model2 = new ModelSNB(samplingFraction, eqClasses);
                result = model.getRisk();
                final double result2 = model2.getRisk();
                if (Double.isNaN(result)) {
                    result = result2;
                    if (Double.isNaN(result)) {
                        final ModelPitman pitmanModel = new ModelPitman(samplingFraction,
                                                                        eqClasses);
                        result = pitmanModel.getRisk();
                        return result;
                    }
                }
                if (Double.isNaN(result2)) {
                    if (Double.isNaN(result)) {
                        final ModelPitman pitmanModel = new ModelPitman(samplingFraction,
                                                                        eqClasses);
                        result = pitmanModel.getRisk();
                    }
                    return result;
                } else {
                    if (result2 > result) {
                        return result;
                    } else {
                        return result2;
                    }
                }

            }
            return result;
        }
    }

    /**
     * Returns whether the SNB model is excluded from the risk model
     */
    public boolean isExlcudeSNBModel() {
        return exlcudeSNB;
    }

    /**
     * Sets whether the SNB model should be excluded from the risk model
     */
    public void setExlcudeSNBModel(boolean exlcudeSNB) {
        this.exlcudeSNB = exlcudeSNB;
    }

    /**
     * This functions takes a user defined data set and the defined
     * quasi-identifiers and extracts the size and frequency of all equivalence
     * classes
     * 
     * @param handle
     *            This class provides access to dictionary encoded data.
     * 
     * @return Map containing the equivalence class sizes (as keys) of the data set and
     *         the corresponding frequency (as values) e.g. if the key 2 has value 3
     *         then there are 3 equivalence classes of size two.
     */
    private Map<Integer, Integer> getEquivalenceClasses(final DataHandle handle) {

        DataDefinition definition = handle.getDefinition();

        // Get indices of quasi identifiers
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        final int[] indices = new int[definition.getQuasiIdentifyingAttributes()
                                                .size()];
        int index = 0;
        for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }

        // TODO: consider outlier
        // Calculate equivalence classes
        Map<String, Integer> eqClasses = new HashMap<String, Integer>();
        for (int row = 0; row < handle.getNumRows(); row++) {

            String rowString = "";
            for (int column = 0; column < indices.length; column++) {
                rowString += handle.getValue(row, column);
            }

            Integer size = eqClasses.get(rowString);
            if (size == null) {
                size = 1;
            } else {
                size++;
            }
            eqClasses.put(rowString, size);
        }

        Iterator<Entry<String, Integer>> it = eqClasses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();

            Integer size = result.get(entry.getValue());
            if (size == null) {
                size = 1;
            } else {
                size++;
            }
            result.put(entry.getValue(), size);
        }

        return result;
    }

    /**
     * sets values of Cmin and Cmax, giving range of equivalence class sizes
     */
    private void initialize() {
        cMin = Integer.MAX_VALUE;
        cMax = 0;
        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            if (cMin > entry.getKey()) {
                cMin = entry.getKey();
            }
            if (cMax < entry.getKey()) {
                cMax = entry.getKey();
            }
        }
        if (cMin == Integer.MAX_VALUE) {
            cMin = 0;
        }
    }
}
