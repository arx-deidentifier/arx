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

import org.deidentifier.arx.gui.resources.Resources;
import org.xml.sax.SAXException;


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
    public void checkVersion(String version) throws SAXException {
        if (!(version.equals("2.0") || version.startsWith("2.1") || version.startsWith("2.2"))) {
            throw new SAXException(Resources.getMessage("WorkerLoad.10") + version); //$NON-NLS-1$
        }
    }

    @Override
    public String getVocabularyVersion() {
        return "2.0";
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
}
