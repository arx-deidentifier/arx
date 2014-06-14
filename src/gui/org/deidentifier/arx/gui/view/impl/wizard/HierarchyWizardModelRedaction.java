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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased.Order;

/**
 * A model for redaction-based builders
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardModelRedaction<T> extends HierarchyWizardModelAbstract<T> {

    /** Var */
    private Order redactionOrder = Order.RIGHT_TO_LEFT;
    /** Var */
    private Order alignmentOrder = Order.LEFT_TO_RIGHT;
    /** Var */
    private char  paddingCharacter = ' ';
    /** Var */
    private char  redactionCharacter = '*';
    /** Var */
    
    /**
     * Creates a new instance
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelRedaction(DataType<T> dataType, String[] data) {
        super(data);
        this.update();
    }

    /**
     * Returns the alignment order
     * @return
     */
    public Order getAlignmentOrder() {
        return alignmentOrder;
    }

    @Override
    public HierarchyBuilderRedactionBased<T> getBuilder(boolean serializable) {
        return HierarchyBuilderRedactionBased.create(  alignmentOrder, 
                                                       redactionOrder, 
                                                       paddingCharacter, 
                                                       redactionCharacter);
    }

    /**
     * Returns the padding character
     * @return
     */
    public char getPaddingCharacter() {
        return paddingCharacter;
    }

    /**
     * Returns the redaction parameter
     * @return
     */
    public char getRedactionCharacter() {
        return redactionCharacter;
    }

    /**
     * Returns the redaction order
     * @return
     */
    public Order getRedactionOrder() {
        return redactionOrder;
    }

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
        this.update();
    }

    /**
     * Sets the alignment order
     * @param alignmentOrder
     */
    public void setAlignmentOrder(Order alignmentOrder) {
        if (alignmentOrder != this.alignmentOrder) {
            this.alignmentOrder = alignmentOrder;
            this.update();
        }
    }

    /**
     * Sets the padding character
     * @param paddingCharacter
     */
    public void setPaddingCharacter(char paddingCharacter) {
        if (this.paddingCharacter != paddingCharacter){
            this.paddingCharacter = paddingCharacter;
            this.update();
        }
    }

    /**
     * Sets the redaction character
     * @param redactionCharacter
     */
    public void setRedactionCharacter(char redactionCharacter) {
        if (this.redactionCharacter != redactionCharacter){
            this.redactionCharacter = redactionCharacter;
            this.update();
        }
    }

    /**
     * Sets the redaction order
     * @param redactionOrder
     */
    public void setRedactionOrder(Order redactionOrder) {
        if (redactionOrder != this.redactionOrder){
            this.redactionOrder = redactionOrder;
            this.update();
        }
    }

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
