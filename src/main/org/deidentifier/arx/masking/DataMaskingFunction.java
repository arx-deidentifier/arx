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
package org.deidentifier.arx.masking;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.masking.functions.MaskingOperator;
import org.deidentifier.arx.masking.variable.Distribution;
import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.deidentifier.arx.masking.variable.DistributionType;
import org.deidentifier.arx.masking.variable.RandomVariable;

/**
 * This class implements data masking functions
 * 
 * @author Fabian Prasser
 */
public abstract class DataMaskingFunction implements Serializable, MaskingOperator {

    public static class DistributionMasking {
        private DistributionType type;
        private RandomVariable   var;

        public DistributionMasking(DistributionType type) {
            this.type = type;
            var = new RandomVariable("XXX", type);
        }

        public ArrayList<DistributionParameter<?>> getParameters() {
            ArrayList<DistributionParameter<?>> parameters = (ArrayList<DistributionParameter<?>>) type.getDescription()
                                                                                                       .getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                var.addParameter(parameters.get(i));
            }
            return parameters;
        }

        public void addAllParams(RandomVariable var,
                                 ArrayList<DistributionParameter<?>> parameters) {
            for (DistributionParameter p : parameters) {
                var.addParameter(p);
            }

        }

        public int getNameParam(ArrayList<DistributionParameter<?>> parameters) {
            int name = 0;
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getName().equals("name")) {
                    name = i;
                }
            }
            return name;
        }

        /**
         * All x-values of the distribution.
         * 
         * @param parameter
         * @return
         */
        private int[] allX(DistributionParameter<?> parameter) {
            int[] allX = new int[(int) parameter.getMax() - (int) parameter.getMin() + 1];

            for (int i = 0; i < allX.length; i++) {
                allX[i] = (int) parameter.getMin() + i;
            }
            return allX;
        }

        /**
         * All y-values of the distribution.
         * 
         * @param dist
         * @param allX
         * @return
         */
        private double[] allY(Distribution<Integer> dist, int[] allX) {
            double[] allY = new double[allX.length];

            for (int i = 0; i < allX.length; i++) {
                allY[i] = dist.getValue(allX[i]);
            }
            return allY;
        }

        /**
         * Chooses a random number given the distribution.
         * 
         * @param allX
         * @param allY
         * @return
         */
        private int numberFromDistribution(int[] allX, double[] allY) {
            int[] ranges = new int[allX.length];
            HashMap<Integer, Integer> rangeToX = new HashMap<Integer, Integer>();
            int sum = 0;

            for (int i = 0; i < allX.length; i++) {
                sum += Math.round(1000 * (float) allY[i]);
                ranges[i] = sum;
                rangeToX.put(sum, allX[i]);
            }

            int randomNumber = new Random().nextInt(ranges[ranges.length - 1] + 1);
            int out = allX[0];

            for (int i = 0; i < ranges.length; i++) {
                if (randomNumber <= ranges[i]) {
                    out = rangeToX.get(ranges[i]);
                    break;
                }
            }
            return out;
        }

        /**
         * Chooses random object according to distribution.
         * 
         * @param allX
         * @param allY
         * @param values
         * @return
         */
        private String fromDistribution(int[] allX, double[] allY, Object[] values) {
            int len = allX.length;
            HashMap<Integer, String> rangeToString = new HashMap<Integer, String>();
            int[] ranges = new int[len];

            int sum = 0;
            for (int i = 0; i < len; i++) {
                sum += 1000 * Math.round((float) allY[i]);
                ranges[i] = sum;
                rangeToString.put(sum, values[i].toString());
            }

            int randomNumber = new Random().nextInt(ranges[len - 1] + 1);
            String out = values[0].toString();

            for (int i = 0; i < len; i++) {
                if (randomNumber <= ranges[i]) {
                    out = rangeToString.get(ranges[i]);
                    break;
                }
            }

            return out;
        }

        /**
         * Generates LEN random strings of length between MIN and MAX.
         * Duplicates are not allowed.
         * 
         * @param min
         * @param max
         * @param len
         * @return
         */
        private String[] generateStrings(int min, int max, int len) {
            String[] random = new String[len];

            for (int i = 0; i < len; i++) {
                int length = min + new Random().nextInt(max - min + 1);
                random[i] = RandomStringUtils.randomAlphanumeric(length);
            }

            if (checkForDuplicates(random)) {
                return generateStrings(min, max, len);
            } else {
                return random;
            }
        }

        /**
         * Generates LEN random dates of length between EARLIEST and LATEST.
         * Duplicates are not allowed
         * 
         * @param earliest
         * @param latest
         * @param len
         * @return
         */
        public String[] generateDates(long earliest,
                                      long latest,
                                      int len,
                                      SimpleDateFormat format) {
            String[] random = new String[len];

            for (int i = 0; i < len; i++) {
                long dateL = earliest + (long) (Math.random() * (latest - earliest));
                Date date = new Date(dateL);
                random[i] = format.format(date);
            }

            if (checkForDuplicates(random)) {
                return generateDates(earliest, latest, len, format);
            } else {
                return random;
            }
        }

        /**
         * Checks a string-array for duplicates.
         * 
         * @param arr
         * @return
         */
        private boolean checkForDuplicates(String[] arr) {
            HashSet<String> set = new HashSet<String>();

            for (int i = 0; i < arr.length; i++) {
                if (!set.add(arr[i])) return true;
            }
            return false;
        }

        public SimpleDateFormat getDateFormat(String d) {
            if (d != null) {
                for (String parse : DataType.listDateFormats()) {
                    SimpleDateFormat sdf = new SimpleDateFormat(parse);
                    try {
                        sdf.parse(d);
                        return sdf;
                    } catch (ParseException e) {

                    }
                }
            }
            return null;
        }
    }

    /** SVUID */
    private static final long serialVersionUID = -5605460206017591293L;

    /** Ignore missing data */
    private final boolean     ignoreMissingData;

    /** Preserves data types */
    private final boolean     typePreserving;

    /**
     * Creates a new instance
     * 
     * @param ignoreMissingData
     * @param typePreserving
     */
    protected DataMaskingFunction(boolean ignoreMissingData, boolean typePreserving) {
        this.ignoreMissingData = ignoreMissingData;
        this.typePreserving = typePreserving;
    }

    /**
     * Applies the function to the given column
     * 
     * @param column
     * @param dataType
     */
    public abstract void apply(DataColumn column, DataType<?> dataType);

    /** Clone */
    @Override
    public abstract DataMaskingFunction clone();

    /**
     * Returns whether the function ignores missing data
     * 
     * @return
     */
    public boolean isIgnoreMissingData() {
        return this.ignoreMissingData;
    }

    /**
     * Returns whether the function is type preserving
     * 
     * @return
     */
    public boolean isTypePreserving() {
        return this.typePreserving;
    }
}
