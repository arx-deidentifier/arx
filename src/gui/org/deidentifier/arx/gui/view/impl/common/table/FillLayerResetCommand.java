package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class FillLayerResetCommand implements ILayerCommand{

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new FillLayerResetCommand();
    }
}
