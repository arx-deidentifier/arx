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
 * First version of the ARX XML vocabulary.
 *
 * @author Fabian Prasser
 */
public class Vocabulary_V1 extends Vocabulary {
	
    @Override
    public void checkVersion(String version) throws SAXException {
        if (!version.equals("2.0")) { //$NON-NLS-1$
            throw new SAXException(Resources.getMessage("WorkerLoad.10") + version); //$NON-NLS-1$
        }
    }

	@Override
	public String getAnonymity() {
		return "anonymity"; //$NON-NLS-1$
	}

	@Override
	public String getAssigment() {
		return "assigment"; //$NON-NLS-1$
	}
    
	@Override
	public String getAttribute() {
		return "attribute"; //$NON-NLS-1$
	}
	
    @Override
    public String getAttributeWeight() {
        return "attributeWeight"; //$NON-NLS-1$
    }
	
    @Override
    public String getAttributeWeights() {
        return "attributeWeights"; //$NON-NLS-1$
    }
	
	@Override
	public String getChecked() {
		return "checked"; //$NON-NLS-1$
	}
	
	@Override
	public String getClipboard() {
		return "clipboard"; //$NON-NLS-1$
	}
	
	@Override
	public String getConfig(){
		return "config"; //$NON-NLS-1$
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
	public String getDatatype() {
		return "datatype"; //$NON-NLS-1$
	}
	
	@Override
	public String getDefinition() {
		return "definition"; //$NON-NLS-1$
	}
	
	@Override
	public String getDepth() {
		return "depth"; //$NON-NLS-1$
	}
	
    @Override
    public String getDescription() {
        return "description"; //$NON-NLS-1$
    }
	
    @Override
    public String getEscape() {
        return "escape"; //$NON-NLS-1$
    }
	
	@Override
	public String getFormat() {
	    return "format"; //$NON-NLS-1$
	}
	
	@Override
	public String getHeader() {
		return "<!-- ARX XML Vocabulary Version 1.0 -->"; //$NON-NLS-1$
	}
	
    @Override
    public String getHistorySize() {
        return "historySize"; //$NON-NLS-1$
    }
	
	@Override
	public String getId() {
		return "id"; //$NON-NLS-1$
	}
	
	@Override
	public String getInfoloss() {
		return "infoloss"; //$NON-NLS-1$
	}

    @Override
    public String getInitialNodesInViewer() {
        return "initialNodesInViewer"; //$NON-NLS-1$
    }
	
    @Override
    public String getInputBytes() {
        return "inputBytes"; //$NON-NLS-1$
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
    public String getLinebreak() {
        return "linebreak"; //$NON-NLS-1$
    }

    @Override
    public String getLocale() {
        return "locale"; //$NON-NLS-1$
    }

	@Override
	public String getMax() {
		return "max"; //$NON-NLS-1$
	}

	@Override
	public String getMax2() {
		return "max"; //$NON-NLS-1$
	}

    @Override
    public String getMaxNodesInLattice() {
        return "maxNodesInLattice"; //$NON-NLS-1$
    }

    @Override
    public String getMaxNodesInViewer() {
        return "maxNodesInViewer"; //$NON-NLS-1$
    }

	@Override
	public String getMetadata() {
		return "metadata"; //$NON-NLS-1$
	}

	@Override
	public String getMetric(){
		return "metric"; //$NON-NLS-1$
	}

	@Override
    public String getMicroAggregationFunction() {
        return "microaggregationFunction";
    }

	@Override
    public String getMicroAggregationIgnoreMissingData() {
        return "microaggregationIgnoreMissingData";
    }

	@Override
	public String getMin() {
		return "min"; //$NON-NLS-1$
	}

	@Override
	public String getMin2() {
		return "min"; //$NON-NLS-1$
	}
	
	@Override
	public String getName() {
		return "name"; //$NON-NLS-1$
	}
	
	@Override
	public String getNode() {
		return "node"; //$NON-NLS-1$
	}

	@Override
	public String getNode2() {
		return "node"; //$NON-NLS-1$
	}

    @Override
	public String getPracticalMonotonicity(){
		return "practicalMonotonicity"; //$NON-NLS-1$
	}

	@Override
	public String getPredecessors() {
		return "predecessors"; //$NON-NLS-1$
	}

    @Override
    public String getProject() {
        return "project"; //$NON-NLS-1$
    }

	@Override
	public String getProtectSensitiveAssociations(){
		return "protectSensitiveAssociations"; //$NON-NLS-1$
	}

	@Override
    public String getQuote() {
        return "quote"; //$NON-NLS-1$
    }

	@Override
	public String getRef() {
		return "ref"; //$NON-NLS-1$
	}

    @Override
	public String getRelativeMaxOutliers(){
		return "relativeMaxOutliers"; //$NON-NLS-1$
	}

    @Override
	public String getRemoveOutliers(){
		return "removeOutliers"; //$NON-NLS-1$
	}

    @Override
    public String getSelectedAttribute() {
        return "selectedAttribute"; //$NON-NLS-1$
    }

    @Override
    public String getSeparator() {
        return "separator"; //$NON-NLS-1$
    }

	@Override
    public String getSnapshotSizeDataset() {
        return "snapshotSizeDataset"; //$NON-NLS-1$
    }

    @Override
    public String getSnapshotSizeSnapshot() {
        return "snapshotSizeSnapshot"; //$NON-NLS-1$
    }

    @Override
	public String getSuccessors() {
		return "successors"; //$NON-NLS-1$
	}

    @Override
    public String getSuppressedAttributeTypes() {
        return "suppressedAttributeTypes"; //$NON-NLS-1$
    }
    
	@Override
    public String getSuppressionAlwaysEnabled() {
        return "suppressTuplesInNonAnonymousOutput"; //$NON-NLS-1$
    }

	@Override
    public String getResponseVariable() {
        return "response"; //$NON-NLS-1$
    }

	@Override
	public String getTransformation() {
		return "transformation"; //$NON-NLS-1$
	}

    @Override
	public String getType() {
		return "type"; //$NON-NLS-1$
	}

    @Override
	public String getVersion() {
		return "version"; //$NON-NLS-1$
	}

    @Override
    public String getVocabulary() {
        return "vocabulary"; //$NON-NLS-1$
    }

    @Override
    public String getVocabularyVersion() {
        return "1.0"; //$NON-NLS-1$
    }

    @Override
    public String getWeight() {
        return "weight"; //$NON-NLS-1$
    }
}
