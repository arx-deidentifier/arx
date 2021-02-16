/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2021 Fabian Prasser and contributors
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

/**
 * This package provides the public API for the ARX anonymization framework.
 * <p>
 * <ul>
 * <li>The class {@link AttributeType} encapsulates the four different kinds of attributes recognized by the framework.</li>
 * <li>The class {@link DataType} encapsulates the three different kinds of data types recognized by the framework.</li>
 * <li>The class {@link Data} represents an input dataset for the algorithm.</li>
 * <li>The class {@link DataDefinitio}, an instance of which can be obtain for any input data by calling {@link Data.getDefinition()}, allows assign attribute types, data types and further parameters to a dataset.</li>
 * <li>The class {@link DataHandle}, an instance of which can be obtain for any input data by calling {@link Data.getHandle()} and for any output data by calling {@link ARXResult.getHandle()}, implements convenience methods for accessing a dictionary encoded representation of a dataset. It also allows manipulating the data (e.g., sorting it) and makes sure that input and output data are always in sync.</li>
 * <li>The class {@link ARXAnonymizer} represents the actual algorithm and allows to derive anonymous representations of the input dataset, e.g., by calling {@link ARXAnonymizer.kAnonymize()} or {@link ARXAnonymizer.lDiversify()}.</li>
 * <li>The class {@link ARXResult} represents the results of executing the algorithm. It provides access to the globally optimal solution and the underlying generalization lattice {@link ARXLattice}.</li>
 * <li>The class {@link ARXAdapter} can be utilized to attach a listener to the ARX framework, tracking its progress while anonymizing a dataset.</li>
 * </ul>
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
package org.deidentifier.arx;

