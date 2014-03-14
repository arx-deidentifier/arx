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

import org.xml.sax.Attributes;

/**
 * An abstract base class for the XML vocabulary
 * @author Fabian Prasser
 */
public abstract class Vocabulary {
	
	public abstract String getHeader();
	
	public abstract String getMetadata();
	public abstract String getVersion();
	
	public abstract String getClipboard();
	public abstract String getNode();

	public abstract String getConfig();
	public abstract String getRemoveOutliers();
	public abstract String getPracticalMonotonicity();
	public abstract String getProtectSensitiveAssociations();
	public abstract String getRelativeMaxOutliers();
	public abstract String getMetric();
	public abstract String getCriteria();
	public abstract String getCriterion();
	
	public abstract String getDefinition();
	public abstract String getAssigment();
	public abstract String getName();
	public abstract String getType();
	public abstract String getDatatype();
	public abstract String getRef();
	public abstract String getMin();
	public abstract String getMax();
	
	public abstract String getLattice();
	public abstract String getLevel();
	public abstract String getNode2();
	public abstract String getDepth();
	public abstract String getId();
	public abstract String getTransformation();
	public abstract String getAnonymity();
	public abstract String getChecked();
	public abstract String getPredecessors();
	public abstract String getSuccessors();
	public abstract String getInfoloss();
	public abstract String getMin2();
	public abstract String getMax2();
	public abstract String getAttribute();

	public boolean isMetadata(String value){ return value.equals(getMetadata()); }
	public boolean isVersion(String value){return value.equals(getVersion());}

	public boolean isClipboard(String value){ return value.equals(getClipboard()); }
	public boolean isNode(String value){return value.equals(getNode());}

	public boolean isConfig(String value){ return value.equals(getConfig()); }
	public boolean isRemoveOutliers(String value){return value.equals(getRemoveOutliers());}
	public boolean isPracticalMonotonicity(String value){return value.equals(getPracticalMonotonicity());}
	public boolean isProtectSensitiveAssociations(String value){return value.equals(getProtectSensitiveAssociations());}
	public boolean isRelativeMaxOutliers(String value){return value.equals(getRelativeMaxOutliers());}
	public boolean isMetric(String value){return value.equals(getMetric());}
	public boolean isCriteria(String value){return value.equals(getCriteria());}
	public boolean isCriterion(String value){return value.equals(getCriterion());}
	
	public boolean isDefinition(String value){return value.equals(getDefinition());}
	public boolean isAssigment(String value){return value.equals(getAssigment());}
	public boolean isName(String value){return value.equals(getName());}
	public boolean isType(String value){return value.equals(getType());}
	public boolean isDatatype(String value){return value.equals(getDatatype());}
	public boolean isRef(String value){return value.equals(getRef());}
	public boolean isMin(String value){return value.equals(getMin());}
	public boolean isMax(String value){return value.equals(getMax());}
	
	public boolean isLattice(String value){return value.equals(getLattice());}
	public boolean isLevel(String value){return value.equals(getLevel());}
	public boolean isNode2(String value){return value.equals(getNode2());}
	public boolean isDepth(String value){return value.equals(getDepth());}
	public boolean isId(String value){return value.equals(getId());}
	public boolean isTransformation(String value){return value.equals(getTransformation());}
	public boolean isAnonymity(String value){return value.equals(getAnonymity());}
	public boolean isChecked(String value){return value.equals(getChecked());}
	public boolean isPredecessors(String value){return value.equals(getPredecessors());}
	public boolean isSuccessors(String value){return value.equals(getSuccessors());}
	public boolean isInfoloss(String value){return value.equals(getInfoloss());}
	public boolean isMin2(String value){return value.equals(getMin2());}
	public boolean isMax2(String value){return value.equals(getMax2());}
	public boolean isAttribute(String value){return value.equals(getAttribute());}

    public String getId(Attributes attributes) {
        return attributes.getValue(this.getId());
    }

    public String getDepth(Attributes attributes) {
        return attributes.getValue(this.getDepth());
    }
}
