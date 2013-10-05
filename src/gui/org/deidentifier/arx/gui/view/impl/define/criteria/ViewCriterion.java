/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.view.impl.define.criteria;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.widgets.Composite;

public abstract class ViewCriterion implements IView {

	protected static final int SLIDER_MAX = 1000;
	protected static final int LABEL_WIDTH = 50;

	protected Controller controller;
	protected Model model;
	protected Composite root;

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

	@Override
	public void dispose() {
		controller.removeListener(this);
	}

	@Override
	public void reset() {
		SWTUtil.disable(root);
	}

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

	protected abstract Composite build(Composite parent);

	/**
	 * TODO: OK?
	 */
	protected int doubleToSlider(final double min, final double max,
			final double value) {
		double val = ((value - min) / max) * SLIDER_MAX;
		val = Math.round(val * SLIDER_MAX) / (double) SLIDER_MAX;
		if (val < 0) {
			val = 0;
		}
		if (val > SLIDER_MAX) {
			val = SLIDER_MAX;
		}
		return (int) val;
	}

	/**
	 * TODO: OK?
	 */
	protected int intToSlider(final int min, final int max, final int value) {

		int val = (int) Math.round(((double) (value - min) / (double) max) * SLIDER_MAX);
		if (val < 0) {
			val = 0;
		}
		if (val > SLIDER_MAX) {
			val = SLIDER_MAX;
		}
		return val;
	}

	protected abstract void parse();

	protected double sliderToDouble(final double min, final double max,
			final int value) {
		double val = ((double) value / (double) SLIDER_MAX) * max;
		val = Math.round(val * SLIDER_MAX) / (double) SLIDER_MAX;
		if (val < min) {
			val = min;
		}
		if (val > max) {
			val = max;
		}
		return val;
	}

	protected int sliderToInt(final int min, final int max, final int value) {
		int val = (int) Math
				.round(((double) value / (double) SLIDER_MAX) * max);
		if (val < min) {
			val = min;
		}
		if (val > max) {
			val = max;
		}
		return val;
	}
}
