/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
 * This package implements different variants of k-anonymity, l-diversity, t-closeness and d-presence.
 * k-anonymity and d-presence are implicit privacy criteria, i.e., they are implicitly bound to the
 * quasi-identifiers, while the other criteria are explicitly bound to a specific sensitive attribute.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
package org.deidentifier.arx.criteria;

