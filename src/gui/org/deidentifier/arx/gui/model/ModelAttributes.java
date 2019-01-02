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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A model for attribute analyses
 *
 * @author Fabian Prasser
 */
public class ModelAttributes implements Serializable {
    
    /** SVUID*/
    private static final long serialVersionUID = -8845225440683404329L;

    /**
     * A enum for views
     * @author Fabian Prasser
     */
    public static enum ViewAttributesType {
        COLUMN_CONTRIBUTIONS,
        ATTRIBUTES,
        HIPAA_ATTRIBUTES,
    }

    /** Modified */
    private boolean                          modified             = false;
    /** Model */
    private Map<ViewAttributesType, Boolean> viewEnabledForInput  = new HashMap<>();
    /** Model */
    private Map<ViewAttributesType, Boolean> viewEnabledForOutput = new HashMap<>();

    /**
     * Creates a new instance
     */
    public ModelAttributes() {
       // Empty by design
    }
    
    /**
     * Is this model modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /***
     * Returns whether a view is enabled
     * @param view
     * @return
     */
    public boolean isViewEnabledForInput(ViewAttributesType view) {
        if (!viewEnabledForInput.containsKey(view)) {
            return true;
        } else {
            return viewEnabledForInput.get(view);
        }
    }
    
    /***
     * Returns whether a view is enabled
     * @param view
     * @return
     */
    public boolean isViewEnabledForOutput(ViewAttributesType view) {
        if (!viewEnabledForOutput.containsKey(view)) {
            return true;
        } else {
            return viewEnabledForOutput.get(view);
        }
    }
    
    /**
     * Set unmodified
     */
    public void setUnmodified() {
        this.modified = false;
    }

    /**
     * Allows to enable/disable views
     * @param view
     * @param value
     */
    public void setViewEnabledForInput(ViewAttributesType view, boolean value) {
        this.viewEnabledForInput.put(view, value);
    }

    /**
     * Allows to enable/disable views
     * @param view
     * @param value
     */
    public void setViewEnabledForOutput(ViewAttributesType view, boolean value) {
        this.viewEnabledForOutput.put(view, value);
    }
}
