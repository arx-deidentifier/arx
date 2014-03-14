/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.worker.io;

/**
 * First version of the ARX XML vocabulary
 * @author Fabian Prasser
 *
 */
public class Vocabulary_V1 extends Vocabulary {
	
	@Override
	public String getHeader() {
		return "<!-- ARX XML Vocabulary Version 1.0 -->";
	}
	
	@Override
	public String getConfig(){
		return "config"; //$NON-NLS-1$
	}
	@Override
	public String getRemoveOutliers(){
		return "removeOutliers"; //$NON-NLS-1$
	}
	@Override
	public String getPracticalMonotonicity(){
		return "practicalMonotonicity"; //$NON-NLS-1$
	}
	@Override
	public String getProtectSensitiveAssociations(){
		return "protectSensitiveAssociations"; //$NON-NLS-1$
	}
	@Override
	public String getRelativeMaxOutliers(){
		return "relativeMaxOutliers"; //$NON-NLS-1$
	}
	@Override
	public String getMetric(){
		return "metric"; //$NON-NLS-1$
	}
	@Override
	public String getCriteria(){
		return "criteria"; //$NON-NLS-1$
	}
	@Override
	public String getCriterion(){
		return "criterion"; //$NON-NLS-1$
	}
	
	@Override
	public String getDefinition() {
		return "definition"; //$NON-NLS-1$
	}
	@Override
	public String getAssigment() {
		return "assigment"; //$NON-NLS-1$
	}
	@Override
	public String getName() {
		return "name"; //$NON-NLS-1$
	}
	@Override
	public String getType() {
		return "type"; //$NON-NLS-1$
	}
	@Override
	public String getDatatype() {
		return "datatype"; //$NON-NLS-1$
	}
	@Override
	public String getRef() {
		return "ref"; //$NON-NLS-1$
	}
	@Override
	public String getMin() {
		return "min"; //$NON-NLS-1$
	}
	@Override
	public String getMax() {
		return "max"; //$NON-NLS-1$
	}
	@Override
	public String getAttribute() {
		return "attribute"; //$NON-NLS-1$
	}

	
	
	@Override
	public String getLattice() {
		return "lattice"; //$NON-NLS-1$
	}
	
	@Override
	public String getLevel() {
		return "level"; //$NON-NLS-1$
	}
	
	@Override
	public String getDepth() {
		return "depth"; //$NON-NLS-1$
	}
	
	@Override
	public String getNode2() {
		return "node"; //$NON-NLS-1$
	}
	
	@Override
	public String getId() {
		return "id"; //$NON-NLS-1$
	}
	
	@Override
	public String getTransformation() {
		return "transformation"; //$NON-NLS-1$
	}

	@Override
	public String getAnonymity() {
		return "anonymity"; //$NON-NLS-1$
	}

	@Override
	public String getChecked() {
		return "checked"; //$NON-NLS-1$
	}

	@Override
	public String getPredecessors() {
		return "predecessors"; //$NON-NLS-1$
	}

	@Override
	public String getSuccessors() {
		return "successors"; //$NON-NLS-1$
	}

	@Override
	public String getInfoloss() {
		return "infoloss"; //$NON-NLS-1$
	}

	@Override
	public String getMin2() {
		return "min"; //$NON-NLS-1$
	}

	@Override
	public String getMax2() {
		return "max"; //$NON-NLS-1$
	}

	@Override
	public String getClipboard() {
		return "clipboard"; //$NON-NLS-1$
	}

	@Override
	public String getNode() {
		return "node"; //$NON-NLS-1$
	}

	@Override
	public String getMetadata() {
		return "metadata";
	}
	@Override
	public String getVersion() {
		return "version";
	}
}
