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

package org.deidentifier.arx.test;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.metric.Metric;
import org.junit.Before;

/**
 * 
 */
public abstract class AbstractTest extends TestCase {

    /**
     * 
     */
    public static enum TestMetric {
        
        /**  TODO */
        DMSTAR,
        
        /**  TODO */
        DM,
        
        /**  TODO */
        HEIGHT,
        
        /**  TODO */
        PREC,
        
        /**  TODO */
        ENTROPY,
        
        /**  TODO */
        NMENTROPY
    }

    /**  TODO */
    protected DataProvider provider = null;

    /**
     * 
     *
     * @param metricType
     * @return
     */
    protected Metric<?> createMetric(final TestMetric metricType) {
        // create metric
        Metric<?> metric = null;
        switch (metricType) {
        case PREC:
            metric = Metric.createPrecisionMetric();
            break;
        case HEIGHT:
            metric = Metric.createHeightMetric();
            break;
        case DMSTAR:
            metric = Metric.createDiscernabilityMetric(true);
            break;
        case DM:
            metric = Metric.createDiscernabilityMetric(false);
            break;
        case ENTROPY:
            metric = Metric.createEntropyMetric();
            break;
        default:
            break;
        }
        return metric;
    }

    /**
     * 
     *
     * @param iterator
     * @return
     */
    protected String[][] iteratorToArray(final Iterator<String[]> iterator) {
        final ArrayList<String[]> list = new ArrayList<String[]>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list.toArray(new String[list.size()][]);
    }

    /**
     * 
     *
     * @param array
     */
    protected void printArray(final String[][] array) {
        System.out.print("{");

        for (int j = 0; j < array.length; j++) {
            final String[] strings = array[j];
            System.out.print("{");
            for (int i = 0; i < strings.length; i++) {
                final String string = strings[i];
                System.out.print("\"");
                System.out.print(string);
                System.out.print("\"");
                if (i < (strings.length - 1)) {
                    System.out.print(",");
                }
            }
            System.out.print("}");
            if (j < (array.length - 1)) {
                System.out.print(",");
            }
            System.out.println();
        }
        System.out.print("}");
    }

    /**
     * 
     *
     * @param result
     * @return
     */
    protected String[][] resultToArray(final ARXResult result) {
        final ArrayList<String[]> list = new ArrayList<String[]>();
        final Iterator<String[]> transformed = result.getOutput(false).iterator();
        while (transformed.hasNext()) {
            list.add(transformed.next());
        }
        return list.toArray(new String[list.size()][]);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    @Before
    public void setUp() {
        provider = new DataProvider();
    }
}
