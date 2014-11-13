package org.deidentifier.arx.gui.view.impl.common.table;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * 
 */
public class FillLayerResetCommand implements ILayerCommand{

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.command.ILayerCommand#convertToTargetLayer(org.eclipse.nebula.widgets.nattable.layer.ILayer)
     */
    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.command.ILayerCommand#cloneCommand()
     */
    @Override
    public ILayerCommand cloneCommand() {
        return new FillLayerResetCommand();
    }
}
