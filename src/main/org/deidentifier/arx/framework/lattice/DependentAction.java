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

package org.deidentifier.arx.framework.lattice;

/**
 * A action that depends on properties of transformations.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class DependentAction {

    /**
     * A trigger for nodes.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class NodeActionConstant extends DependentAction {

        /** Other */
        private final boolean result;

        /**
         * Creates a new instance
         * @param result
         */
        public NodeActionConstant(boolean result) {
            this.result = result;
        }

        @Override
        public boolean appliesTo(Transformation transformation) {
            return result;
        }
    }

    /**
     * A trigger for nodes.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class NodeActionInverse extends DependentAction {

        /** Other */
        private final DependentAction action;

        /**
         * Creates a new instance
         * @param action
         */
        public NodeActionInverse(DependentAction action) {
            this.action = action;
        }

        @Override
        public boolean appliesTo(Transformation transformation) {
            return !action.appliesTo(transformation);
        }
    }

    /**
     * Implements the action to be performed.
     *
     * @param transformation
     */
    public void action(Transformation transformation) {
        // Empty by design
    }
    
    /**
     * Determines whether the trigger action should be performed.
     *
     * @param transformation
     * @return
     */
    public abstract boolean appliesTo(Transformation transformation);
    
    /**
     * Applies the trigger to the given transformation.
     *
     * @param transformation
     */
    public final void apply(Transformation transformation){
        if (appliesTo(transformation)) {
            action(transformation);
        }
    }
}
