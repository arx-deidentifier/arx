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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.widgets.Composite;

/**
 * A base class for views on privacy criteria.
 *
 * @author Fabian Prasser
 */
public abstract class ViewCriterion implements IView {

	/**  TODO */
	protected static final int LABEL_WIDTH = 50;

	/**  TODO */
	protected Controller controller;
	
	/**  TODO */
	protected Model model;
	
	/**  TODO */
	protected Composite root;

	/**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param model
     */
	public ViewCriterion(final Composite parent, final Controller controller,
			final Model model) {
		this.controller = controller;
		this.controller.addListener(ModelPart.MODEL, this);
		this.controller.addListener(ModelPart.INPUT, this);
		this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
		this.model = model;
		this.root = build(parent);
		SWTUtil.disable(root);
	}

	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.view.def.IView#dispose()
	 */
	@Override
	public void dispose() {
		controller.removeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.view.def.IView#reset()
	 */
	@Override
	public void reset() {
		SWTUtil.disable(root);
	}

	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
	 */
	@Override
	public void update(ModelEvent event) {
		if (event.part == ModelPart.MODEL) {
			this.model = (Model) event.data;
			parse();
		} else if (event.part == ModelPart.INPUT) {
			parse();
		} else if (event.part == ModelPart.CRITERION_DEFINITION) {
            if (event.source!=this) parse();
        }
	}

	/**
     * Implement this to build the view.
     *
     * @param parent
     * @return
     */
	protected abstract Composite build(Composite parent);

	/**
     * Implement this to parse the settings into a privacy criterion.
     */
	protected abstract void parse();

}
