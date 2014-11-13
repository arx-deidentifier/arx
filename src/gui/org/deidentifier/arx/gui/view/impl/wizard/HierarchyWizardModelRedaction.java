/*
 * ARX: Powerful Data Anonymization
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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;

/**
 * A model for redaction-based builders.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardModelRedaction<T> extends HierarchyWizardModelAbstract<T> {

    /** Var. */
    private Order  redactionOrder     = Order.RIGHT_TO_LEFT;
    
    /** Var. */
    private Order  alignmentOrder     = Order.LEFT_TO_RIGHT;
    
    /** Var. */
    private char   paddingCharacter   = ' ';
    
    /** Var. */
    private char   redactionCharacter = '*';
    
    /** Meta-data about the nature of the domain of the attribute. */
    private Integer maxValueLength;
    
    /** Meta-data about the nature of the domain of the attribute. */
    private Integer domainSize;
    
    /** Meta-data about the nature of the domain of the attribute. */
    private Integer alphabetSize;
    
    /**
     * Creates a new instance.
     *
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelRedaction(DataType<T> dataType, String[] data) {
        super(data);
        this.update();
    }

    /**
     * Returns the alignment order.
     *
     * @return
     */
    public Order getAlignmentOrder() {
        return alignmentOrder;
    }

    /**
     * @return the alphabetSize
     */
    public Integer getAlphabetSize() {
        return alphabetSize;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelAbstract#getBuilder(boolean)
     */
    @Override
    public HierarchyBuilderRedactionBased<T> getBuilder(boolean serializable) {
        
        // Create
        HierarchyBuilderRedactionBased<T> builder = HierarchyBuilderRedactionBased.create(alignmentOrder,
                                                                                          redactionOrder,
                                                                                          paddingCharacter,
                                                                                          redactionCharacter);
        
        // Set domain properties
        if (domainSize != null && alphabetSize != null && maxValueLength != null) {
            builder.setDomainAndAlphabetSize(domainSize, alphabetSize, maxValueLength);
        } else if (domainSize != null && maxValueLength != null) {
            builder.setDomainSize(domainSize, maxValueLength);
        } else if (alphabetSize != null && maxValueLength != null) {
            builder.setAlphabetSize(alphabetSize, maxValueLength);
        }
        
        // Return
        return builder;
    }

    /**
     * @return the domainSize
     */
    public Integer getDomainSize() {
        return domainSize;
    }

    /**
     * @return the maxValueLength
     */
    public Integer getMaxValueLength() {
        return maxValueLength;
    }

    /**
     * Returns the padding character.
     *
     * @return
     */
    public char getPaddingCharacter() {
        return paddingCharacter;
    }

    /**
     * Returns the redaction parameter.
     *
     * @return
     */
    public char getRedactionCharacter() {
        return redactionCharacter;
    }

    /**
     * Returns the redaction order.
     *
     * @return
     */
    public Order getRedactionOrder() {
        return redactionOrder;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelAbstract#parse(org.deidentifier.arx.aggregates.HierarchyBuilder)
     */
    @Override
    public void parse(HierarchyBuilder<T> _builder) {
        
        if (!(_builder instanceof HierarchyBuilderRedactionBased)) {
            return;
        }
        HierarchyBuilderRedactionBased<T> builder = (HierarchyBuilderRedactionBased<T>)_builder; 
        this.redactionOrder = builder.getRedactionOrder();
        this.alignmentOrder = builder.getAligmentOrder();
        this.redactionCharacter = builder.getRedactionCharacter();
        this.paddingCharacter = builder.getPaddingCharacter();
        this.domainSize = builder.getDomainSize().intValue();
        this.alphabetSize = builder.getAlphabetSize().intValue();
        this.maxValueLength = builder.getMaxValueLength().intValue();
        this.update();
    }

    /**
     * Sets the alignment order.
     *
     * @param alignmentOrder
     */
    public void setAlignmentOrder(Order alignmentOrder) {
        if (alignmentOrder != this.alignmentOrder) {
            this.alignmentOrder = alignmentOrder;
            this.update();
        }
    }

    /**
     * @param alphabetSize the alphabetSize to set
     */
    public void setAlphabetSize(Integer alphabetSize) {
        this.alphabetSize = alphabetSize;
    }

    /**
     * @param domainSize the domainSize to set
     */
    public void setDomainSize(Integer domainSize) {
        this.domainSize = domainSize;
    }

    /**
     * @param maxValueLength the maxValueLength to set
     */
    public void setMaxValueLength(Integer maxValueLength) {
        this.maxValueLength = maxValueLength;
    }

    /**
     * Sets the padding character.
     *
     * @param paddingCharacter
     */
    public void setPaddingCharacter(char paddingCharacter) {
        if (this.paddingCharacter != paddingCharacter){
            this.paddingCharacter = paddingCharacter;
            this.update();
        }
    }

    /**
     * Sets the redaction character.
     *
     * @param redactionCharacter
     */
    public void setRedactionCharacter(char redactionCharacter) {
        if (this.redactionCharacter != redactionCharacter){
            this.redactionCharacter = redactionCharacter;
            this.update();
        }
    }

    /**
     * Sets the redaction order.
     *
     * @param redactionOrder
     */
    public void setRedactionOrder(Order redactionOrder) {
        if (redactionOrder != this.redactionOrder){
            this.redactionOrder = redactionOrder;
            this.update();
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelAbstract#build()
     */
    @Override
    protected void build() {
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;

        if (data==null) return;
        
        HierarchyBuilderRedactionBased<T> builder = getBuilder(false);
        try {
            super.groupsizes = builder.prepare(data);
        } catch(Exception e){
            super.error = "Unknown error";
            return;
        }
        
        try {
            super.hierarchy = builder.build();
        } catch(Exception e){
            super.error = "Unknown error";
            return;
        }
    }
}
