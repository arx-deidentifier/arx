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
 * Second version of the ARX XML vocabulary
 * @author Fabian Prasser
 *
 */
public class Vocabulary_V2 extends Vocabulary_V1 {

	@Override
	public String getHeader() {
		StringBuilder b = new StringBuilder();
		b.append("<!-- ARX XML Vocabulary Version 2.0 -->\n");
		b.append("<!-- n = node                       -->\n");
		b.append("<!-- i = id                         -->\n");
		b.append("<!-- t = transformation             -->\n");
		b.append("<!-- a = anonymity                  -->\n");
		b.append("<!-- c = checked                    -->\n");
		b.append("<!-- p = predecessors               -->\n");
		b.append("<!-- s = successors                 -->\n");
		b.append("<!-- l = infoloss                   -->\n");
		b.append("<!-- f = min                        -->\n");
		b.append("<!-- u = max                        -->\n");
		b.append("<!-- z = attribute                  -->\n");
		return b.toString();
	}
	
	@Override
	public String getNode2() {
		return "n"; //$NON-NLS-1$
	}
	
	@Override
	public String getId() {
		return "i"; //$NON-NLS-1$
	}
	
	@Override
	public String getTransformation() {
		return "t"; //$NON-NLS-1$
	}

	@Override
	public String getAnonymity() {
		return "a"; //$NON-NLS-1$
	}

	@Override
	public String getChecked() {
		return "c"; //$NON-NLS-1$
	}

	@Override
	public String getPredecessors() {
		return "p"; //$NON-NLS-1$
	}

	@Override
	public String getSuccessors() {
		return "s"; //$NON-NLS-1$
	}

	@Override
	public String getInfoloss() {
		return "l"; //$NON-NLS-1$
	}

	@Override
	public String getMin2() {
		return "f"; //$NON-NLS-1$
	}

	@Override
	public String getMax2() {
		return "u"; //$NON-NLS-1$
	}
	
	@Override
	public String getAttribute() {
		return "attribute"; //$NON-NLS-1$
	}

	@Override
	public boolean isNode2(String value) {
		return value.equals(getNode2()) || value.equals(super.getNode2());
	}

	@Override
	public boolean isId(String value) {
		return value.equals(getId()) || value.equals(super.getId());
	}

	@Override
	public boolean isTransformation(String value) {
		return value.equals(getTransformation())
				|| value.equals(super.getTransformation());
	}

	@Override
	public boolean isAnonymity(String value) {
		return value.equals(getAnonymity())
				|| value.equals(super.getAnonymity());
	}

	@Override
	public boolean isChecked(String value) {
		return value.equals(getChecked()) || value.equals(super.getChecked());
	}

	@Override
	public boolean isPredecessors(String value) {
		return value.equals(getPredecessors())
				|| value.equals(super.getPredecessors());
	}

	@Override
	public boolean isSuccessors(String value) {
		return value.equals(getSuccessors())
				|| value.equals(super.getSuccessors());
	}

	@Override
	public boolean isInfoloss(String value) {
		return value.equals(getInfoloss()) || value.equals(super.getInfoloss());
	}

	@Override
	public boolean isMin2(String value) {
		return value.equals(getMin2()) || value.equals(super.getMin2());
	}

	@Override
	public boolean isMax2(String value) {
		return value.equals(getMax2()) || value.equals(super.getMax2());
	}

	@Override
	public boolean isAttribute(String value) {
		return value.equals(getAttribute()) || value.equals(super.getAttribute());
	}
	

    @Override
    public String getId(Attributes attributes) {
        String val = attributes.getValue(this.getId());
        if (val == null) {
            return attributes.getValue(super.getId());
        } else {
            return val;
        }
    }

    @Override
    public String getDepth(Attributes attributes) {
        String val = attributes.getValue(this.getDepth());
        if (val == null) {
            return attributes.getValue(super.getDepth());
        } else {
            return val;
        }
    }
}
