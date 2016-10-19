/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.gui.model;

import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.FinancialJournalistNoAttackPrivacy;
import org.deidentifier.arx.criteria.FinancialJournalistPrivacy;
import org.deidentifier.arx.criteria.FinancialProsecutorNoAttackPrivacy;
import org.deidentifier.arx.criteria.FinancialProsecutorPrivacy;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;

/**
 * This class provides a model for the game-theoretic privacy model
 * 
 * @author James Gaupp
 * @author Fabian Prasser
 */
public class ModelStackelbergPrivacyCriterion extends ModelImplicitCriterion {
    
    /**
     * The attacker model used by the privacy model
     * @author Fabian Prasser
     *
     */
    public static enum AttackerModel {
        PROSECUTOR,
        JOURNALIST
    }

    /** SVUID */
    private static final long serialVersionUID = -4305859036159393453L;

    /** Prosecutor model */
    private AttackerModel     attackerModel    = AttackerModel.PROSECUTOR;

    /** Do we allow attacks to happen? */
    private boolean           allowAttacks     = true;

    /**
     * Creates a new instance
     * @param attackerModel
     * @param allowAttacks
     */
    public ModelStackelbergPrivacyCriterion(AttackerModel attackerModel, boolean allowAttacks) {
        this.attackerModel = attackerModel;
        this.allowAttacks = allowAttacks;
    }

    /**
     * Creates a new instance
     */
    public ModelStackelbergPrivacyCriterion() {
    	this.attackerModel = AttackerModel.PROSECUTOR; 
    	this.allowAttacks = true;
    }

	@Override
	public ModelCriterion clone() {
		ModelStackelbergPrivacyCriterion  result = new ModelStackelbergPrivacyCriterion();
		result.setAttackerModel(attackerModel);
		return result;
	}
	
    /**
     * @return the attackerModel
     */
    public AttackerModel getAttackerModel() {
        return this.attackerModel;
    }
    
    /**
     * @return
     */
    public boolean isAllowAttacks() {
        return this.allowAttacks;
    }
    
    @Override
	public PrivacyCriterion getCriterion(Model model) {
        
        // Create privacy model
        if (this.attackerModel == AttackerModel.PROSECUTOR) {
            if (this.allowAttacks) {
                return new FinancialProsecutorPrivacy();
            } else {
                return new FinancialProsecutorNoAttackPrivacy();
            }
        } else if (this.attackerModel == AttackerModel.JOURNALIST) {
            DataSubset subset = DataSubset.create(model.getInputConfig().getInput(), 
                                                  model.getInputConfig().getResearchSubset());
            if (this.allowAttacks) {
                return new FinancialJournalistPrivacy(subset);
            } else {
                return new FinancialJournalistNoAttackPrivacy(subset);
            }
        } else {
            throw new IllegalStateException("Unknown attacker model"); //$NON-NLS-1$
        }
	}

	@Override
	public String getLabel() {
		return Resources.getMessage("ModelCriterion.4"); //$NON-NLS-1$
	}
    
    @Override
	public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelStackelbergPrivacyCriterion)) {
            return;
        }
        ModelStackelbergPrivacyCriterion other = (ModelStackelbergPrivacyCriterion)criterion;
        this.allowAttacks = other.allowAttacks;
        this.attackerModel = other.attackerModel;
        if (!_default) {
            this.setEnabled(other.isEnabled());
        }

	}
    
	/**
	 * Defines the attacker model
     * @param attackerModel
     */
    public void setAttackerModel(AttackerModel attackerModel) {
        this.attackerModel = attackerModel;
    }
    
    /**
     * Defines whether we allow attacks to happen
     * @param allowAttacks
     */
    public void setAllowAttacks(boolean allowAttacks) {
        this.allowAttacks = allowAttacks;
    }

	@Override
	public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.allowAttacks) {
            builder.append(Resources.getMessage("ModelCriterion.4")); //$NON-NLS-1$   
        } else {
            builder.append(Resources.getMessage("ModelCriterion.7")); //$NON-NLS-1$   
        }
        builder.append(" ("); //$NON-NLS-1$
        if (this.attackerModel == AttackerModel.PROSECUTOR) {
            builder.append(Resources.getMessage("ModelCriterion.5")); //$NON-NLS-1$
        } else if (this.attackerModel == AttackerModel.JOURNALIST) {
            builder.append(Resources.getMessage("ModelCriterion.6")); //$NON-NLS-1$
        } else {
            throw new IllegalStateException("Unknown attacker model"); //$NON-NLS-1$
        }
        builder.append(")"); //$NON-NLS-1$
        return builder.toString();
	}
}
