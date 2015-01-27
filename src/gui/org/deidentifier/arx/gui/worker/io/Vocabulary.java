/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

/**
 * An abstract base class for the XML vocabulary.
 *
 * @author Fabian Prasser
 */
public abstract class Vocabulary {
    
    /**  TODO */
    private static final Map<String, Vocabulary> versions = new HashMap<String, Vocabulary>();
    
    static {
        Vocabulary_V1 v1 = new Vocabulary_V1();
        Vocabulary_V2 v2 = new Vocabulary_V2();
        versions.put(v1.getVocabularyVersion(), v1);
        versions.put(v2.getVocabularyVersion(), v2);
    }
    
    /**
     * 
     *
     * @param version
     * @return
     */
    public static Vocabulary forVersion(String version){
        return versions.get(version);
    }
	
	/**
     * 
     *
     * @return
     */
	public abstract String getHeader();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getVocabularyVersion();

	/**
     * 
     *
     * @return
     */
	public abstract String getMetadata();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getVersion();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getVocabulary();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getClipboard();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getNode();

	/**
     * 
     *
     * @return
     */
	public abstract String getConfig();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getRemoveOutliers();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getPracticalMonotonicity();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getProtectSensitiveAssociations();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getRelativeMaxOutliers();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getMetric();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getCriteria();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getCriterion();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getDefinition();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getAssigment();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getName();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getType();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getDatatype();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getFormat();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getRef();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getMin();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getMax();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getLattice();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getLevel();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getNode2();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getDepth();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getId();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getTransformation();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getAnonymity();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getChecked();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getPredecessors();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getSuccessors();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getInfoloss();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getMin2();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getMax2();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getAttribute();
	
	/**
     * 
     *
     * @return
     */
	public abstract String getProject();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSeparator();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getDescription();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSuppressionString();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getHistorySize();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSnapshotSizeDataset();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSnapshotSizeSnapshot();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getInitialNodesInViewer();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getMaxNodesInLattice();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getMaxNodesInViewer();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSelectedAttribute();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getInputBytes();

    /**
     * 
     *
     * @return
     */
    public abstract String getAttributeWeights();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getAttributeWeight();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getWeight();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSuppressionAlwaysEnabled();
    
    /**
     * 
     *
     * @return
     */
    public abstract String getSuppressedAttributeTypes();

    /**
     * 
     *
     * @return
     */
    public abstract String getLocale();
    
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isMetadata(String value){ return value.equals(getMetadata()); }
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isVersion(String value){return value.equals(getVersion());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isVocabulary(String value) { return value.equals(getVocabulary()); }
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isClipboard(String value){ return value.equals(getClipboard()); }
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isNode(String value){return value.equals(getNode());}

	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isConfig(String value){ return value.equals(getConfig()); }
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isRemoveOutliers(String value){return value.equals(getRemoveOutliers());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isPracticalMonotonicity(String value){return value.equals(getPracticalMonotonicity());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isProtectSensitiveAssociations(String value){return value.equals(getProtectSensitiveAssociations());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isRelativeMaxOutliers(String value){return value.equals(getRelativeMaxOutliers());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isMetric(String value){return value.equals(getMetric());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isCriteria(String value){return value.equals(getCriteria());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isCriterion(String value){return value.equals(getCriterion());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isDefinition(String value){return value.equals(getDefinition());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isAssigment(String value){return value.equals(getAssigment());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isName(String value){return value.equals(getName());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isType(String value){return value.equals(getType());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isDatatype(String value){return value.equals(getDatatype());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isFormat(String value){return value.equals(getFormat());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isRef(String value){return value.equals(getRef());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isMin(String value){return value.equals(getMin());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isMax(String value){return value.equals(getMax());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isLattice(String value){return value.equals(getLattice());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isLevel(String value){return value.equals(getLevel());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isNode2(String value){return value.equals(getNode2());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isDepth(String value){return value.equals(getDepth());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isId(String value){return value.equals(getId());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isTransformation(String value){return value.equals(getTransformation());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isAnonymity(String value){return value.equals(getAnonymity());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isChecked(String value){return value.equals(getChecked());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isPredecessors(String value){return value.equals(getPredecessors());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isSuccessors(String value){return value.equals(getSuccessors());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isInfoloss(String value){return value.equals(getInfoloss());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isMin2(String value){return value.equals(getMin2());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isMax2(String value){return value.equals(getMax2());}
	
	/**
     * 
     *
     * @param value
     * @return
     */
	public boolean isAttribute(String value){return value.equals(getAttribute());}
	
    /**
     * 
     *
     * @param value
     * @return
     */
    public boolean isAttributeWeight(String value) {return value.equals(getAttributeWeight());}
    
    /**
     * 
     *
     * @param value
     * @return
     */
    public boolean isAttributeWeights(String value) {return value.equals(getAttributeWeights());}
    
    /**
     * 
     *
     * @param value
     * @return
     */
    public boolean isWeight(String value) {return value.equals(getWeight());}

    /**
     * 
     *
     * @param value
     * @return
     */
    public boolean isSuppressionAlwaysEnabled(String value) {return value.equals(getSuppressionAlwaysEnabled());}
    
    /**
     * 
     *
     * @param value
     * @return
     */
    public boolean isSuppressedAttributeTypes(String value) {return value.equals(getSuppressedAttributeTypes());}
    
    /**
     * 
     *
     * @param value
     * @return
     */
    public boolean isLocale(String value) {return value.equals(getLocale());}
    
    /**
     * 
     *
     * @param version
     * @throws SAXException
     */
    public abstract void checkVersion(String version) throws SAXException;    
}
