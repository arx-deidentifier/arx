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

package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.gui.model.ModelCriterion;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class
 * 
 * @author Fabian Prasser
 */
public abstract class EditorCriterion<T extends ModelCriterion> {

    /** Constant */
    protected static final int LABEL_WIDTH = 50;
    /** Model */
    protected final T          model;
    /** View */
    private final Composite    root;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param model
     */
    public EditorCriterion(final Composite parent, final T model) {
        this.model = (T) model;
        root = this.build(parent);
        this.parse(this.model);
    }

    /**
     * Disposes the editor
     */
    public void dispose() {
        this.root.dispose();
    }

    /**
     * Returns the altered model
     * 
     * @return
     */
    public T getModel() {
        return this.model;
    }

    /**
     * Build the composite
     * 
     * @param parent
     */
    protected abstract Composite build(Composite parent);

    /**
     * Parse
     * 
     * @param model
     */
    protected abstract void parse(T model);
}
