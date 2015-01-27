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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.linearbits.swt.widgets.Knob;
import de.linearbits.swt.widgets.KnobColorProfile;
import de.linearbits.swt.widgets.KnobRange;

/**
 * This class allows to define weights for attributes.
 *
 * @author Fabian Prasser
 */
public class ViewAttributeWeights implements IView {

    /** Controller. */
    private Controller          controller = null;
    
    /** Model. */
    private Model               model      = null;
    
    /** Model. */
    private final Set<String>   attributes = new HashSet<String>();
    
    /** View. */
    private Composite           panel      = null;
    
    /** View. */
    private final Composite     root;
    
    /** Misc. */
    private final DecimalFormat format     = new DecimalFormat("0.000");

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewAttributeWeights(final Composite parent, final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(3, 3).create());
        this.root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(0, 0).create());
        
        root.pack();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
        root.dispose();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        root.setRedraw(false);
        if (panel != null) {
            panel.dispose();
            panel = null;
        }
        attributes.clear();
        root.setRedraw(true);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(ModelEvent event) {

        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
        } 
        
        if (event.part == ModelPart.MODEL ||
            event.part == ModelPart.INPUT) {
            this.attributes.clear();
        } 
        
        if (event.part == ModelPart.ATTRIBUTE_TYPE ||
            event.part == ModelPart.MODEL) {
            if (model!=null) {
                
                // Create ordered list of QIs
                DataDefinition definition = model.getInputDefinition();
                List<String> qis = new ArrayList<String>();
                
                if (definition != null) {
                    Set<String> _qis = definition.getQuasiIdentifyingAttributes();
                    
                    // Break if nothing has changed
                    if (this.attributes.equals(_qis)) {
                        return;
                    }
                    
                    DataHandle handle = model.getInputConfig().getInput().getHandle();
                    for (int i=0; i<handle.getNumColumns(); i++){
                        String attr = handle.getAttributeName(i);
                        if (_qis.contains(attr)){
                            qis.add(attr);
                        }
                    }
                    attributes.clear();
                    attributes.addAll(qis);
                }

                if (root.isDisposed()) return;
                
                root.setRedraw(false);
                
                // Dispose widgets
                if (panel != null) {
                    panel.dispose();
                }
                
                // Create layout
                panel = new Composite(root, SWT.NONE);
                panel.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).create());
                panel.setLayout(GridLayoutFactory.swtDefaults().numColumns(qis.size()).margins(0, 0).equalWidth(true).create());
                
                // Create composites
                List<Composite> composites = new ArrayList<Composite>();
                for(int i=0; i<qis.size(); i++){
                    Composite c = new Composite(panel, SWT.NONE);
                    c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                    c.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(2, 0).create());
                    composites.add(c);
                }
                
                // Create labels
                for(int i=0; i<qis.size(); i++){
                    Label label = new Label(composites.get(i), SWT.CENTER);
                    label.setText(qis.get(i));
                    label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                }
                
                // Create knob widgets
                List<Knob<Double>> knobs = new ArrayList<Knob<Double>>();
                for(int i=0; i<qis.size(); i++){
                    Knob<Double> knob = new Knob<Double>(composites.get(i), SWT.NULL, new KnobRange.Double(0d, 1d));
                    knob.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(30, 30).create());
                    knob.setDefaultColorProfile(KnobColorProfile.createDefaultSystemProfile(root.getDisplay()));
                    knob.setFocusedColorProfile(KnobColorProfile.createFocusedBlueRedProfile(root.getDisplay()));
                    knobs.add(knob);
                }

                // Create labels
                for(int i=0; i<qis.size(); i++){
                    
                    final Label label = new Label(composites.get(i), SWT.CENTER);
                    label.setText("0.0");
                    label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                    
                    final String attribute = qis.get(i);
                    final Knob<Double> knob = knobs.get(i);
                    knob.addSelectionListener(new SelectionAdapter(){
                        public void widgetSelected(SelectionEvent arg0) {
                            
                            double value = knob.getValue();
                            label.setText(format.format(value));
                            label.setToolTipText(String.valueOf(value));
                            
                            try {
                                
                                // Correctly indicate weights slightly > 0
                                double parsedValue = format.parse(format.format(value)).doubleValue();
                                if (parsedValue == 0d && value > 0d) {
                                    label.setText(">0");
                                }
                                
                                // Correctly indicate weights slightly < 1
                                if (parsedValue == 1d && value < 1d) {
                                    label.setText("<1");
                                }
                                
                            } catch (ParseException e) {
                                // Drop silently
                            }
                            
                            if (model != null && model.getInputConfig() != null) {
                                model.getInputConfig().setAttributeWeight(attribute, value);
                            }
                        }
                    });
                }
                
                // Set values
                for(int i=0; i<qis.size(); i++){
                    if (model != null && model.getInputConfig() != null) {
                        knobs.get(i).setValue(model.getInputConfig().getAttributeWeight(qis.get(i)));
                    }
                }
                
                // Update root composite
                root.setVisible(!qis.isEmpty());
                root.layout(true, true);    
                root.setRedraw(true);
            }
        }
    }
}
