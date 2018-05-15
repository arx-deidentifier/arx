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

package org.deidentifier.arx.gui.worker.io;

import org.deidentifier.arx.gui.resources.Resources;
import org.xml.sax.SAXException;


/**
 * Second version of the ARX XML vocabulary.
 *
 * @author Fabian Prasser
 */
public class Vocabulary_V2 extends Vocabulary_V1 {

    @Override
    public void checkVersion(String version) throws SAXException {
        if (!(version.equals("2.0") || version.startsWith("2.1") || version.startsWith("2.2") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              || version.startsWith("2.3") || version.startsWith("3.0") || version.startsWith("3.1") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              || version.startsWith("3.2") || version.startsWith("3.3") || version.startsWith("3.4") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              || version.startsWith("3.5") || version.startsWith("3.6") || version.startsWith("3.7"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            throw new SAXException(Resources.getMessage("WorkerLoad.10") + version); //$NON-NLS-1$
        }
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
	public String getHeader() {
		StringBuilder b = new StringBuilder();
		b.append("<!-- ARX XML Vocabulary Version 2.0 -->\n"); //$NON-NLS-1$
		b.append("<!-- n = node                       -->\n"); //$NON-NLS-1$
		b.append("<!-- i = id                         -->\n"); //$NON-NLS-1$
		b.append("<!-- t = transformation             -->\n"); //$NON-NLS-1$
		b.append("<!-- a = anonymity                  -->\n"); //$NON-NLS-1$
		b.append("<!-- c = checked                    -->\n"); //$NON-NLS-1$
		b.append("<!-- p = predecessors               -->\n"); //$NON-NLS-1$
		b.append("<!-- s = successors                 -->\n"); //$NON-NLS-1$
		b.append("<!-- l = infoloss                   -->\n"); //$NON-NLS-1$
		b.append("<!-- f = min                        -->\n"); //$NON-NLS-1$
		b.append("<!-- u = max                        -->\n"); //$NON-NLS-1$
		b.append("<!-- z = attribute                  -->\n"); //$NON-NLS-1$
		return b.toString();
	}
	
	@Override
	public String getId() {
		return "i"; //$NON-NLS-1$
	}
	
	@Override
	public String getInfoloss() {
		return "l"; //$NON-NLS-1$
	}

	@Override
	public String getMax2() {
		return "u"; //$NON-NLS-1$
	}

	@Override
	public String getMin2() {
		return "f"; //$NON-NLS-1$
	}

	@Override
	public String getNode2() {
		return "n"; //$NON-NLS-1$
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
	public String getTransformation() {
		return "t"; //$NON-NLS-1$
	}

    @Override
    public String getVocabularyVersion() {
        return "2.0"; //$NON-NLS-1$
    }
}
