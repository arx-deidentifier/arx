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

package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
public class ViewStatisticsContingencyHeatmap extends ViewStatistics<AnalysisContextContingency> {

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
        
        super(parent, controller, target, reset, true);
    }

    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.CONTINGENCY;
    }

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
                final Font _font = new Font(jhc.getDisplay(), fd[0]);
                jhc.setFont(_font);
                parent.addDisposeListener(new DisposeListener(){
                    public void widgetDisposed(DisposeEvent arg0) {
                        if (_font != null && !_font.isDisposed()) {
                            _font.dispose();
                        }
                    }
                });
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

    @Override
    protected AnalysisContextContingency createViewConfig(AnalysisContext context) {
        return new AnalysisContextContingency(context);
    }

    @Override
    protected void doReset() {
        jhc.setData(null, new JHCConfiguration("", "", MAX_SIZE, MAX_SIZE, gradient, layout)); //$NON-NLS-1$ //$NON-NLS-2$
        setStatusEmpty();
    }
    
    @Override
    protected void doUpdate(AnalysisContextContingency context) {
        int column1 = context.handle.getColumnIndexOf(context.attribute1);
        int column2 = context.handle.getColumnIndexOf(context.attribute2);
        jhc.setData(new DensityData(context.handle, column1, column2), new JHCConfiguration(context.attribute1,
                                                                                            context.attribute2,
                                                                                            MAX_SIZE,
                                                                                            MAX_SIZE,
                                                                                            gradient,
                                                                                            layout));

    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return false;
    }
}
