/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.def;

public interface IView {

    public static class ModelEvent {
        public static enum EventTarget {
            SELECTED_ATTRIBUTE,
            INPUT,
            OUTPUT,
            ATTRIBUTE_TYPE,
            RESULT,
            DATA_TYPE,
            ALGORITHM,
            K,
            METRIC,
            MAX_OUTLIERS,
            FILTER,
            SELECTED_NODE,
            MODEL,
            CLIPBOARD,
            HIERARCHY
        }

        public final EventTarget target;
        public final Object      data;
        public final Object      source;

        public ModelEvent(final Object source,
                          final EventTarget target,
                          final Object data) {
            this.target = target;
            this.data = data;
            this.source = source;
        }
    }

    public void dispose();

    public void reset();

    public void update(ModelEvent event);
}
