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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

public class DisclosureRisk {

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
     * measures for a given data set
     * 
     * @param pi
     *            sampling factor
     * @param definition
     *            Encapsulates a definition of the types of attributes contained
     *            in a dataset
     * @param handle
     *            This class provides access to dictionary encoded data.
     */
    public DisclosureRisk(final double pi,
                          final DataDefinition definition,
                          final DataHandle handle) {
        if ((pi == 0) || (pi > 1)) {
            this.samplingFraction = 0.1;
        } else {
            this.samplingFraction = pi;
        }

        // create map containing the equivalence class sizes (as keys) of the
        // data set and the corresponding frequency (as values)
        eqClasses = new HashMap<Integer, Integer>();
        evalInput(definition, handle);

        // set values for Cmin and Cmax
        setExtrema();
    }

    /**
     * Creates a new instance of a class that allows to estimate different risk
     * measures for a given data set
     * 
     * @param pi
     *            sampling fraction, defaults to 0.1
     * @param eqArray
     *            array with size of equivalence class as array position plus
     *            one and frequency as value in the array
     */
    public DisclosureRisk(final double pi, final int[] eqArray) {
        if ((pi <= 0) || (pi > 1)) {
            this.samplingFraction = 0.1;
        } else {
            this.samplingFraction = pi;
        }
        eqClasses = new HashMap<Integer, Integer>();
        for (int i = 0; i < eqArray.length; i++) {
            eqClasses.put(i + 1, eqArray[i]);
        }

        // set values for Cmin and Cmax
        setExtrema();
    }

    /**
     * Creates a new instance of a class that allows to estimate different risk
     * measures for a given data set
     * 
     * @param pi
     *            sampling factor, defaults to 0.1
     * @param eqClasses
     *            takes a map containing the size of equivalence classes as keys
     *            and the frequency as values
     */
    public DisclosureRisk(final double pi, final Map<Integer, Integer> eqClasses) {
        if ((pi <= 0) || (pi > 1)) {
            this.samplingFraction = 0.1;
        } else {
            this.samplingFraction = pi;
        }
        this.eqClasses = eqClasses;

        // set values for Cmin and Cmax
        setExtrema();
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
    public double computeEquivalenceClassRisk() {
        final EquivalenceClassModel equiModel = new EquivalenceClassModel(eqClasses);
        return equiModel.computeRisk();
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
    public double computeHighestIndividualRisk() {
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
    public double computeHighestRiskAffected() {
        if (cMin != 0) {
            return eqClasses.get(cMin);
        } else {
            return Double.NaN;
        }

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
    public double
            computePopulationUniquesRisk() throws IllegalArgumentException {
        double result;

        if (exlcudeSNB) {
            /*
             * Selection rule, according to Danker et al, 2010, modified to
             * exclude the SNB model and anonymized data
             */
            if (eqClasses.containsKey(1) && !eqClasses.containsKey(2)) {
                final ZayatzModel model = new ZayatzModel(samplingFraction,
                                                          eqClasses);
                result = model.computeRisk();
                return result;
            }
            if (!eqClasses.containsKey(1)) {
                new IllegalArgumentException("The data set does not contain any sample uniques! Computing Population Uniques not possible!");
                return Double.NaN;
            }

            if (samplingFraction <= 0.1) {
                final PitmanModel model = new PitmanModel(samplingFraction,
                                                          eqClasses);
                result = model.computeRisk();
                if (Double.isNaN(result)) {
                    final ZayatzModel zayatzModel = new ZayatzModel(samplingFraction,
                                                                    eqClasses);
                    result = zayatzModel.computeRisk();
                }
            } else {
                final ZayatzModel model = new ZayatzModel(samplingFraction,
                                                          eqClasses);
                result = model.computeRisk();
                if (Double.isNaN(result)) {
                    final PitmanModel pitmanModel = new PitmanModel(samplingFraction,
                                                                    eqClasses);
                    result = pitmanModel.computeRisk();
                }
            }
            return result;
        }

        /*
         * Selection rule, according to Danker et al, 2010
         */
        if (eqClasses.containsKey(1) && !eqClasses.containsKey(2)) {
            final ZayatzModel model = new ZayatzModel(samplingFraction,
                                                      eqClasses);
            result = model.computeRisk();
            return result;
        }
        if (!eqClasses.containsKey(1)) {
            new IllegalArgumentException("The data set does not contain any sample uniques! Computing Population Uniques not possible!");
            return Double.NaN;
        }

        if (samplingFraction <= 0.1) {
            final PitmanModel model = new PitmanModel(samplingFraction,
                                                      eqClasses);
            result = model.computeRisk();
            if (Double.isNaN(result)) {
                final ZayatzModel zayatzModel = new ZayatzModel(samplingFraction,
                                                                eqClasses);
                result = zayatzModel.computeRisk();
            }
        } else {
            final ZayatzModel model = new ZayatzModel(samplingFraction,
                                                      eqClasses);
            final SNBModel model2 = new SNBModel(samplingFraction, eqClasses);
            result = model.computeRisk();
            final double result2 = model2.computeRisk();
            if (Double.isNaN(result)) {
                result = result2;
                if (Double.isNaN(result)) {
                    final PitmanModel pitmanModel = new PitmanModel(samplingFraction,
                                                                    eqClasses);
                    result = pitmanModel.computeRisk();
                    return result;
                }
            }
            if (Double.isNaN(result2)) {
                if (Double.isNaN(result)) {
                    final PitmanModel pitmanModel = new PitmanModel(samplingFraction,
                                                                    eqClasses);
                    result = pitmanModel.computeRisk();
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

    /**
     * This functions takes a user defined data set and the defined
     * quasi-identifiers and extracts the size and frequency of all equivalence
     * classes
     * 
     * @param definition
     *            Encapsulates a definition of the types of attributes contained
     *            in a given data set
     * @param handle
     *            This class provides access to dictionary encoded data.
     */
    public void evalInput(final DataDefinition definition,
                          final DataHandle handle) {

        // Sort by quasi-identifiers
        final int[] indices = new int[definition.getQuasiIdentifyingAttributes()
                                                .size()];
        int index = 0;
        for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        handle.sort(true, indices);

        // Iterate over all equivalence classes
        int size = 0;
        for (int row = 0; row < handle.getNumRows(); row++) {

            boolean newClass = false;
            if (row > 0) {
                for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
                    final int column = handle.getColumnIndexOf(attribute);
                    if (!handle.getValue(row, column)
                               .equals(handle.getValue(row - 1, column))) {
                        newClass = true;
                        break;
                    }
                }
            } else {
                newClass = true;
            }

            if (newClass) {
                if (row != 0) {
                    if (!eqClasses.containsKey(size)) {
                        eqClasses.put(size, 1);
                    } else {
                        eqClasses.put(size, eqClasses.get(size) + 1);
                    }
                }
                size = 1;
            } else {
                size++;
            }
        }
        if (handle.getNumRows() > 1) {
            if (!eqClasses.containsKey(size)) {
                eqClasses.put(size, 1);
            } else {
                eqClasses.put(size, eqClasses.get(size) + 1);
            }
        }
    }

    /**
     * This functions takes a user defined data set and the defined
     * quasi-identifiers and marks the entries with the highest
     * re-identification risk. The estimate of the re-identification risk is
     * based solely on the data set and there is no population estimate that
     * plays into the calculation of the re-identification risk
     * 
     * @param definition
     *            Encapsulates a definition of the types of attributes contained
     *            in a given data set
     * @param handle
     *            This class provides access to dictionary encoded data.
     */
    public void markHighRiskEntries(final DataDefinition definition,
                                    final DataHandle handle) {

        // Sort by quasi-identifiers
        final int[] indices = new int[definition.getQuasiIdentifyingAttributes()
                                                .size()];
        int index = 0;
        for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices[index++] = handle.getColumnIndexOf(attribute);
        }
        handle.sort(true, indices);

        // Iterate over all equivalence classes and update array of equivalence
        // classes
        int size = 0;
        boolean newClass = false;
        final int[] classes = new int[handle.getNumRows()];

        for (int row = 0; row < (handle.getNumRows() - 1); row++) {

            size++;
            // discriminate equivalence classes
            newClass = false;
            for (final String attribute : definition.getQuasiIdentifyingAttributes()) {
                final int column = handle.getColumnIndexOf(attribute);
                if (!handle.getValue(row, column)
                           .equals(handle.getValue(row + 1, column))) {
                    newClass = true;
                    break;
                }
            }

            // update entries
            if (newClass) {
                for (int j = 0; j < size; j++) {
                    classes[row - j] = size;
                }
                size = 0;
            }

            // correct last entry
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
         * classes is now a array where every array element indicates the size
         * of the corresponding equivalence class this allows to manipulate
         * attributes of single array elements
         */
        System.out.println(Arrays.toString(classes));
        for (int row = 0; row < handle.getNumRows(); row++) {
            if (classes[row] == cMin) {
                // TODO use Cmin or other value to determine the high risk
                // equivalence classes that are to be marked
                // mark entries in this row as endangered or set other
                // attributes
                // this has to be done on GUI Level
                // System.out.println("Dangerous");
            } else {
                // System.out.println("Not so dangerous");
            }
        }

    }

    /**
     * sets values of Cmin and Cmax, giving range of equivalence class sizes
     */
    public void setExtrema() {
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
