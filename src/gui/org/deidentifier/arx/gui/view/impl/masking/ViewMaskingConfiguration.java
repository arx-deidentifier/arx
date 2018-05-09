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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentMultiStack;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.masking.ViewAttributeConfiguration.Attribute;
import org.deidentifier.arx.masking.MaskingConfiguration;
import org.deidentifier.arx.masking.MaskingType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ViewMaskingConfiguration implements IView{
	
    /** Controller */
    private final Controller 	controller;
    
    private Object[] identifyingAttributes;
    
    /** Model */
    private Model 				model;
    
    /** Model */
    private String				attribute     = null;
    
    /** Widget */
    private final Combo			cmbMasking;
    
    /** Widget */
    private final Combo			cmbDistribution;
    
    /** Widget. */
    private final ComponentMultiStack stack;
    
    /** Resource */
    private static final MaskingType[] COMBO1_TYPES  = new MaskingType[] {
    													MaskingType.NONE,
    													MaskingType.PSEUDONYMIZATION_MASKING,
    													MaskingType.NOISE_ADDITION_MASKING,
    													MaskingType.RANDOM_SHUFFLING_MASKING,
    													MaskingType.RANDOM_GENERATION_MASKING};
    
    /** Resource */
    private static final String[]        COMBO1_MASKINGTYPES = new String[] {
    													"None", //$NON-NLS-1$
                                                        Resources.getMessage("MaskingConfigurationView.1"), //$NON-NLS-1$
                                                        Resources.getMessage("MaskingConfigurationView.2"), //$NON-NLS-1$
                                                        Resources.getMessage("MaskingConfigurationView.3"), //$NON-NLS-1$
                                                        Resources.getMessage("MaskingConfigurationView.4") }; //$NON-NLS-1$
	
	public ViewMaskingConfiguration(final Composite parent, final Controller controller) {

		this.controller = controller;
		
		// Build view
		//build(parent);
		
        // Title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null);
        folder.setLayoutData(SWTUtil.createFillGridData());

        // First tab
        Composite composite = folder.createItem(Resources.getMessage("MaskingView.2"), null);
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);
		
		// These events are triggered when data is imported or attribute configuration changes
		this.controller.addListener(ModelPart.INPUT, this); // TODO: Is this actually needed? Can data be imported with an attribute being set as identifying?
		this.controller.addListener(ModelPart.MODEL, this);
		this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
		this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
		
		// Get notified whenever the masking for an attribute is changed
		this.controller.addListener(ModelPart.MASKING_ATTRIBUTE_CHANGED, this);
		this.controller.addListener(ModelPart.IDENTIFYING_ATTRIBUTES_CHANGED, this);
		
        // Group
        Composite innerGroup = new Composite(composite, SWT.NULL);
        innerGroup.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout typeInputGridLayout = new GridLayout();
        typeInputGridLayout.numColumns = 2;
        innerGroup.setLayout(typeInputGridLayout);
        
        // Combo for Masking type
        final Label kLabel = new Label(innerGroup, SWT.PUSH);
        kLabel.setText(Resources.getMessage("MaskingConfigurationView.0")); //$NON-NLS-1$
        cmbMasking = new Combo(innerGroup, SWT.READ_ONLY);
        cmbMasking.setLayoutData(SWTUtil.createFillGridData());
        cmbMasking.setItems(COMBO1_MASKINGTYPES);
        cmbMasking.select(0);
        cmbMasking.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if ((cmbMasking.getSelectionIndex() != -1) && (attribute != null)) {
                    boolean identified = false;
        	        for (int i = 0; i< identifyingAttributes.length; i++)
        	        {
        	        	if (((Attribute)identifyingAttributes[i]).equals(attribute))
        	        	{
                        	MaskingType maskingType = COMBO1_TYPES[cmbMasking.getSelectionIndex()];
                            actionMaskingTypeChanged(attribute, maskingType);
                            refreshLayers(cmbMasking.getSelectionIndex());
        	        		identified = true;
        	        		break;
        	        	}
        	        	
        	        }
        	        if (!identified)
        	        	cmbMasking.select(0);
                }
            }
        });
        
        // Create multistack
        stack = new ComponentMultiStack(innerGroup);
        
        // First column
        Composite first = stack.create(SWTUtil.createGridData());
        Composite compositeEmpty = new Composite(first, SWT.NONE);
        Composite compositeString = new Composite(first, SWT.NONE);
        Label labelStrLength = new Label(compositeString, SWT.PUSH);
        GridLayout compositeLabelMinLayout = new GridLayout();
        compositeLabelMinLayout.numColumns = 1;
        compositeLabelMinLayout.marginLeft = 0;
        compositeLabelMinLayout.marginRight = 0;
        compositeLabelMinLayout.marginWidth = 0;
        compositeLabelMinLayout.marginHeight = 0;
        compositeString.setLayout(compositeLabelMinLayout);
        compositeEmpty.setLayout(compositeLabelMinLayout);
        labelStrLength.setText(Resources.getMessage("MaskingConfigurationView.5")); //$NON-NLS-1$
        Composite compositeString2 = new Composite(first, SWT.NONE);
        compositeString2.setLayout(compositeLabelMinLayout);
        final Label labelProbDist = new Label(compositeString2, SWT.PUSH);
        labelProbDist.setText(Resources.getMessage("MaskingConfigurationView.6")); //$NON-NLS-1$

        // Second column
        Composite second = stack.create(SWTUtil.createFillHorizontallyGridData());
        Composite compositeEmpty2 = new Composite(second, SWT.NONE);
        compositeEmpty2.setLayout(compositeLabelMinLayout);
        Composite compositetf= new Composite(second, SWT.NONE);
        final Text textField = new Text(compositetf, SWT.SINGLE | SWT.BORDER);
        textField.setLayoutData(SWTUtil.createFillGridData());
        compositetf.setLayout(compositeLabelMinLayout);
        textField.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent e) {
				Text text = (Text)e.getSource();
				//get old text and create new text by using the VerifyEvent.text
				final String oldS = text.getText();
				String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
				boolean isInt = true;
				if (newS=="")
				{
					System.out.println(((Text)e.getSource()).getText()+" better?");
					return;
				}
				try{
					Integer.parseInt(newS);
					System.out.println(newS+" better?");
				}catch(NumberFormatException ex){
					isInt = false;
				}
				System.out.println("\""+newS+"\"");
				if(!isInt)
					e.doit = false;
			}
		});        
        Composite compositecmb= new Composite(second, SWT.NONE);
        cmbDistribution = new Combo(compositecmb, SWT.READ_ONLY);
        //TODO: Set comboitems for distribution
        compositecmb.setLayout(compositeLabelMinLayout);
        cmbDistribution.setLayoutData(SWTUtil.createFillGridData());
      //  cmbDistribution.setLayoutData(new GridData(SWT.FILL, SWT.CENTER));
        cmbDistribution.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionDistributionChanged();
            }
        });
        
        // Collect info about children in stack
        stack.pack();
        stack.setLayer(0);
        
        

	}

	@Override
    public void dispose() {

        controller.removeListener(this);

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            if (model != null) {
                attribute = model.getSelectedAttribute();
                updateMaskingType();
            }
        }else if (event.part == ModelPart.MASKING_ATTRIBUTE_CHANGED) {
        	updateMaskingType();
        }
        else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            attribute = model.getSelectedAttribute();
        }
        
        else if (event.part == ModelPart.IDENTIFYING_ATTRIBUTES_CHANGED) {
        	identifyingAttributes = (Object[]) event.data;
        	updateMaskingType();
        }
    }
    
    /**
     * Masking type changed
     */
    private void actionMaskingTypeChanged(String attribute, MaskingType maskingType) {
    	if (maskingType!=null)
    		MaskingConfiguration.addMasking(attribute, maskingType);
    	else
    		MaskingConfiguration.removeMasking(attribute);
        controller.update(new ModelEvent(this,ModelPart.MASKING_ATTRIBUTE_CHANGED,null));
    }
    
    private void actionDistributionChanged ()
    {
    	//TODO: what happens when distribution combofield is changed
    }

	private void updateMaskingType() {
        if (model == null || model.getInputConfig() == null || model.getInputDefinition() == null) {
            reset();
            return;
        }
        MaskingType maskingType = MaskingConfiguration.getMaskingType(attribute);
        for (int i = 0; i < COMBO1_TYPES.length; i++) {
            if (maskingType == COMBO1_TYPES[i]) {
                cmbMasking.select(i);
                refreshLayers(i);
                return;
            }
        }
        cmbMasking.select(0);
        stack.setLayer(0);
	}
	
	private void refreshLayers(int selection){
        if (selection == 0) {
            stack.setLayer(0);
        } else if (selection == 1) {
            stack.setLayer(1);
        }else if (selection == 2) {
            stack.setLayer(2);
        } else if (selection == 3) {
            stack.setLayer(0);
        }else if (selection == 4) {
            stack.setLayer(2);
        }
	}
	
}