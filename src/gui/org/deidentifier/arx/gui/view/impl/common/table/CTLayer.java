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

package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * 
 * @author Fabian Prasser
 *
 */
public abstract class CTLayer extends AbstractLayerTransform implements CTComponent {

    private final CTConfiguration config;
    private final CTContext context;
    
    public CTLayer(CTConfiguration config, CTContext context){
        this.config = config;
        this.context = context;
    }
    
    public CTLayer(IUniqueIndexLayer underlyingDataLayer, CTConfiguration config, CTContext context) {
        super(underlyingDataLayer);
        this.config = config;
        this.context = context;
    }

    public CTConfiguration getConfig(){
        return config;
    }

    public CTContext getContext() {
        return context;
    }
}
