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

        @Override
        public boolean appliesTo(Transformation node) {
            return trigger.appliesTo(node) && additionalConditionAppliesTo(node);
        }
        
        /**
         * The additional condition to implement.
         *
         * @param node
         * @return
         */
        protected abstract boolean additionalConditionAppliesTo(Transformation node);
    }

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

        @Override
        public boolean appliesTo(Transformation node) {
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

        @Override
        public boolean appliesTo(Transformation node) {
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

        @Override
        public boolean appliesTo(Transformation node) {
            return trigger.appliesTo(node) || additionalConditionAppliesTo(node);
        }
        
        /**
         * The additional condition to implement.
         *
         * @param node
         * @return
         */
        protected abstract boolean additionalConditionAppliesTo(Transformation node);
    }
    
    /**
     * Implements the action to be performed.
     *
     * @param node
     */
    public void action(Transformation node) {
        // Empty by design
    }
    
    /**
     * Determines whether the trigger action should be performed.
     *
     * @param node
     * @return
     */
    public abstract boolean appliesTo(Transformation node);
    
    /**
     * Applies the trigger to the given node.
     *
     * @param node
     */
    public final void apply(Transformation node){
        if (appliesTo(node)) {
            action(node);
        }
    }
}
