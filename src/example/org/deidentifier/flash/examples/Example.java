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

package org.deidentifier.flash.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.Data;
import org.deidentifier.flash.FLASHLattice.FLASHNode;
import org.deidentifier.flash.FLASHResult;

/**
 * This class provides a base class for examples
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class Example {

    /**
     * Prints the result
     * 
     * @param result
     * @param data
     */
    protected static void
            printResult(final FLASHResult result, final Data data) {

        // Extract
        final FLASHNode optimum = result.getGlobalOptimum();
        final List<String> qis = new ArrayList<String>(data.getDefinition()
                                                           .getQuasiIdentifyingAttributes());

        // Initialize
        final StringBuffer[] identifiers = new StringBuffer[qis.size()];
        final StringBuffer[] generalizations = new StringBuffer[qis.size()];
        int lengthI = 0;
        int lengthG = 0;
        for (int i = 0; i < qis.size(); i++) {
            identifiers[i] = new StringBuffer();
            generalizations[i] = new StringBuffer();
            identifiers[i].append(qis.get(i));
            generalizations[i].append(optimum.getGeneralization(qis.get(i)))
                              .append("/")
                              .append(data.getDefinition()
                                          .getHierarchyHeight(qis.get(i)) - 1);
            lengthI = Math.max(lengthI, identifiers[i].length());
            lengthG = Math.max(lengthG, generalizations[i].length());
        }

        // Padding
        for (int i = 0; i < qis.size(); i++) {
            while (identifiers[i].length() < lengthI) {
                identifiers[i].append(" ");
            }
            while (generalizations[i].length() < lengthG) {
                generalizations[i].insert(0, " ");
            }
        }

        // Print time
        final DecimalFormat df1 = new DecimalFormat("#####0.00");
        final String sTotal = df1.format(result.getTime() / 1000d) + "s";
        System.out.println(" - Time needed: " + sTotal);

        // Print
        System.out.println(" - Information loss: " +
                           result.getGlobalOptimum()
                                 .getMaximumInformationLoss()
                                 .getValue());
        System.out.println(" - Optimal generalization");
        for (int i = 0; i < qis.size(); i++) {
            System.out.println("   * " + identifiers[i] + ": " +
                               generalizations[i]);
        }
    }
}
