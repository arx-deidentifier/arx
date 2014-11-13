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

package org.deidentifier.arx.framework.lattice;

/**
 * A trigger for nodes.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class NodeAction {

    /**
     * A trigger for nodes.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class NodeActionConstant extends NodeAction {

        /**  TODO */
        private final boolean result;

        /**
         * 
         *
         * @param result
         */
        public NodeActionConstant(boolean result) {
            this.result = result;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.framework.lattice.NodeAction#appliesTo(org.deidentifier.arx.framework.lattice.Node)
         */
        @Override
        public boolean appliesTo(Node node) {
            return result;
        }
    }

    /**
     * A trigger for nodes.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class NodeActionInverse extends NodeAction {

        /**  TODO */
        private final NodeAction trigger;

        /**
         * 
         *
         * @param trigger
         */
        public NodeActionInverse(NodeAction trigger) {
            this.trigger = trigger;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.framework.lattice.NodeAction#appliesTo(org.deidentifier.arx.framework.lattice.Node)
         */
        @Override
        public boolean appliesTo(Node node) {
            return !trigger.appliesTo(node);
        }
    }

    /**
     * A trigger for nodes.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static abstract class NodeActionOR extends NodeAction {

        /**  TODO */
        private final NodeAction trigger;

        /**
         * 
         *
         * @param trigger
         */
        public NodeActionOR(NodeAction trigger) {
            this.trigger = trigger;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.framework.lattice.NodeAction#appliesTo(org.deidentifier.arx.framework.lattice.Node)
         */
        @Override
        public boolean appliesTo(Node node) {
            return trigger.appliesTo(node) || additionalConditionAppliesTo(node);
        }
        
        /**
         * The additional condition to implement.
         *
         * @param node
         * @return
         */
        protected abstract boolean additionalConditionAppliesTo(Node node);
    }

    /**
     * A trigger for nodes.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static abstract class NodeActionAND extends NodeAction {

        /**  TODO */
        private final NodeAction trigger;

        /**
         * 
         *
         * @param trigger
         */
        public NodeActionAND(NodeAction trigger) {
            this.trigger = trigger;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.framework.lattice.NodeAction#appliesTo(org.deidentifier.arx.framework.lattice.Node)
         */
        @Override
        public boolean appliesTo(Node node) {
            return trigger.appliesTo(node) && additionalConditionAppliesTo(node);
        }
        
        /**
         * The additional condition to implement.
         *
         * @param node
         * @return
         */
        protected abstract boolean additionalConditionAppliesTo(Node node);
    }
    
    /**
     * Determines whether the trigger action should be performed.
     *
     * @param node
     * @return
     */
    public abstract boolean appliesTo(Node node);
    
    /**
     * Implements the action to be performed.
     *
     * @param node
     */
    public void action(Node node) {
        // Empty by design
    }
    
    /**
     * Applies the trigger to the given node.
     *
     * @param node
     */
    public final void apply(Node node){
        if (appliesTo(node)) {
            action(node);
        }
    }
}
