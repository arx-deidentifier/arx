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

package org.deidentifier.arx.gui.view.impl.analyze;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.linearbits.jhc.JHC;
import de.linearbits.jhc.JHCConfiguration;
import de.linearbits.jhc.JHCGradient;
import de.linearbits.jhc.JHCLayout;

/**
 * This class displays a contingency table as a heat map.
 * TODO: Make the stuff in this class interruptible like anything else
 * @author Fabian Prasser
 */
public class ViewStatisticsContingencyHeatmap extends ViewStatistics<AnalysisContextVisualizationContingency> {

    /** Static stuff. */
    private static final int MAX_SIZE = 500;
    
    /** The heat map widget. */
    private JHC              jhc;
    
    /** The heat map configuration. */
    private JHCGradient      gradient;
    
    /** The heat map configuration. */
    private JHCLayout        layout;

	/**
     * Creates a new density plot.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatisticsContingencyHeatmap(final Composite parent,
                                  final Controller controller,
                                  final ModelPart target,
                                  final ModelPart reset) {
        
        super(parent, controller, target, reset);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createControl(Composite parent) {
        this.jhc = new JHC(parent, SWT.DOUBLE_BUFFERED);
        this.gradient = JHCGradient.GRADIENT_HEAT;
        this.layout = new JHCLayout(2,10,20,2,15,2);
        
        // Update font settings
        Font font = jhc.getFont();
        if (font != null) {
            FontData[] fd = font.getFontData();
            if (fd != null && fd.length>0){
                fd[0].setHeight(8);
                jhc.setFont(new Font(jhc.getDisplay(), fd[0]));
            }
        }
        
        // Update status
        jhc.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                setStatusDone();
            }
        });
        
        // Return
        return jhc.getControl();
    }


    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#doReset()
     */
    @Override
    protected void doReset() {
        jhc.setData(null, new JHCConfiguration("", "", MAX_SIZE, MAX_SIZE, gradient, layout));
    }


    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#doUpdate(org.deidentifier.arx.gui.view.impl.analyze.AnalysisContextVisualization)
     */
    @Override
    protected void doUpdate(AnalysisContextVisualizationContingency context) {
        int column1 = context.handle.getColumnIndexOf(context.attribute1);
        int column2 = context.handle.getColumnIndexOf(context.attribute2);
        jhc.setData(new DensityData(context.handle, column1, column2), new JHCConfiguration(context.attribute1,
                                                                                            context.attribute2,
                                                                                            MAX_SIZE,
                                                                                            MAX_SIZE,
                                                                                            gradient,
                                                                                            layout));

    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.impl.analyze.ViewStatistics#createViewConfig(org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext)
     */
    @Override
    protected AnalysisContextVisualizationContingency createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationContingency(context);
    }
}
