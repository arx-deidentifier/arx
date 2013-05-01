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

package org.deidentifier.flash.test;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.deidentifier.flash.FLASHResult;
import org.junit.Before;

public abstract class TestAnonymizer extends TestCase {

    public static enum Metric {
        DMSTAR,
        DM,
        HEIGHT,
        PREC,
        ENTROPY,
        NMENTROPY
    }

    protected DataProvider provider = null;

    protected org.deidentifier.flash.metric.Metric
            createMetric(final Metric metricType) {
        // create metric
        org.deidentifier.flash.metric.Metric metric = null;
        switch (metricType) {
        case PREC:
            metric = org.deidentifier.flash.metric.Metric.createPrecisionMetric();
            break;
        case HEIGHT:
            metric = org.deidentifier.flash.metric.Metric.createHeightMetric();
            break;
        case DMSTAR:
            metric = org.deidentifier.flash.metric.Metric.createDMStarMetric();
            break;
        case DM:
            metric = org.deidentifier.flash.metric.Metric.createDMMetric();
            break;
        case ENTROPY:
            metric = org.deidentifier.flash.metric.Metric.createEntropyMetric();
            break;
        default:
            break;
        }
        return metric;
    }

    protected String[][] iteratorToArray(final Iterator<String[]> iterator) {
        final ArrayList<String[]> list = new ArrayList<String[]>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list.toArray(new String[list.size()][]);
    }

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

    protected String[][] resultToArray(final FLASHResult result) {
        final ArrayList<String[]> list = new ArrayList<String[]>();
        final Iterator<String[]> transformed = result.getHandle().iterator();
        while (transformed.hasNext()) {
            list.add(transformed.next());
        }
        return list.toArray(new String[list.size()][]);
    }

    @Override
    @Before
    public void setUp() {
        provider = new DataProvider();
    }
}
