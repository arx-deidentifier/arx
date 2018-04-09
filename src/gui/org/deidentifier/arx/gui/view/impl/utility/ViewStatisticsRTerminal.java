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

import java.util.Date;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.deidentifier.arx.r.OS;
import org.deidentifier.arx.r.RBuffer;
import org.deidentifier.arx.r.RIntegration;
import org.deidentifier.arx.r.RListener;
import org.deidentifier.arx.r.terminal.RLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class ViewStatisticsRTerminal extends ViewStatistics<AnalysisContextR>{
	/** View */
    private Composite                      root;
    /** Internal stuff. */
    private AnalysisManager                manager;
    /** Widget */
    private Text       input;
    /** Widget */
    private StyledText output;
    /** R process*/
	private RIntegration rIntegration;
    
	/**
	 * Creates a new instance
	 * @param parent
	 * @param controller
	 * @param target
	 * @param reset
	 */
	public ViewStatisticsRTerminal(Composite parent, 
			Controller controller, ModelPart target, 
			ModelPart reset) {
		
		super(parent, controller, target, reset, true);
	    this.manager = new AnalysisManager(parent.getDisplay());
	}

	@Override
	public ViewUtilityType getType() {
		return LayoutUtility.ViewUtilityType.R;
	}

	/**
     * Sets the content of the buffer
     * @param text
     */
    public void setOutput(String text) {
        this.root.setRedraw(false);
        this.output.setText(text);
        this.output.setSelection(text.length());
        this.root.setRedraw(true);
    }
	
	private void appendRow(DataHandle handle, int row, StringBuilder b) {
		int numCols = handle.getNumColumns();
		
		b.append("list(");
		
		for (int column = 0; column < numCols; column++) {
			String columnName = handle.getAttributeName(column);
			
			//Get DataType of attribute -> then we know how to deliver it to R that it has the correct data type & getData
			String value = handle.getValue(row, column);
			if (value.equals("*")) {
				value = "NA";
			}
			DataType<?> dataType = handle.getDataType(columnName);
			b.append(convertARXToRValue(dataType, value)); 
			
			if (column < numCols-1) {
				b.append(',');
			}
		}
				
		b.append(")");
	}
	
	private String convertARXToRType(DataType<?> t, int numRows) {
		//speed things up through allocating memory beforehand with numRows
		if (t instanceof DataType.ARXDate) {
			return "as.POSIXct(integer(" + numRows + "), origin=\"1970-01-01\")";
			
		} else if (t instanceof DataType.ARXDecimal) {
			return "numeric(" + numRows + ")";
			
		} else if (t instanceof DataType.ARXInteger) {
			return "integer(" + numRows + ")";
			
		} else if (t instanceof DataType.ARXOrderedString) {
			List<String> levelStringList = ((DataType.ARXOrderedString) t).getElements();
			StringBuilder levelBuilder = new StringBuilder();
			levelBuilder.append("ordered(c(" + numRows + "), levels=c(");
			
			int listSize = levelStringList.size();
			for (int i = 0; i < listSize; i++) {
				levelBuilder.append('"');
				levelBuilder.append(levelStringList.get(i));
				levelBuilder.append('"');
				if (i < listSize - 1) {
					levelBuilder.append(',');
				}
			}
			
			levelBuilder.append("))");
			return levelBuilder.toString();
			
		} else if (t instanceof DataType.ARXString) {
			return "character(" + numRows + ")";
			
		} else {
			// Type unknown
			throw new IllegalArgumentException("Unknown ARX data type, cannot convert to R");
		}
	}
	
	private String convertARXToRValue(DataType<?> t, String value) {
		if (t instanceof DataType.ARXDate) {
			Date javaDate = ((DataType.ARXDate) t).parse(value);
			long seconds = javaDate.getTime() / 1000;
			return "as.POSIXct(" + seconds + ", origin=\"1970-01-01\")";
			
		} else if (t instanceof DataType.ARXDecimal) {
			return "as.numeric(" + value + ")";
			
		} else if (t instanceof DataType.ARXInteger) {
			return "as.integer(" + value + ")";
			
		} else if (t instanceof DataType.ARXOrderedString) {
			return ("\"" + value + "\"");
			
		} else if (t instanceof DataType.ARXString) {
			return ("\"" + value + "\"");
			
		} else {
			// Type unknown
			throw new IllegalArgumentException("Unknown ARX data type, cannot convert to R");
		}
	}
	
	private String createDataFrame(DataHandle handle) {		
		StringBuilder b = new StringBuilder();
		b.append("data.frame(");
		for (int i = 0; i < handle.getNumColumns(); i++) {
			String attributeName = handle.getAttributeName(i);
			b.append('"'); //There have to be quotation marks because there could be hyphens which otherwise provoke an error.
			b.append(attributeName);
			b.append("\"=");
			b.append(convertARXToRType(handle.getDataType(attributeName), handle.getNumColumns())); //speed things up through allocating memory beforehand with numRows
			b.append(',');
		}
		b.append("stringsAsFactors=FALSE)");
		
		return b.toString();
	}
	
	/**
	 * Execute command
	 * @param command
	 */
	private void executeR(String command) {
	    if (this.rIntegration != null && this.rIntegration.isAlive()) {
	        this.rIntegration.execute(command);
	    }
	}

    @Override
    protected ComponentStatusLabelProgressProvider getProgressProvider() {
        return new ComponentStatusLabelProgressProvider(){
            public int getProgress() {
                if (manager == null) {
                    return 0;
                } else {
                    return manager.getProgress();
                }
            }
        };
    }

	/**
	 * Starts R
	 */
	private void startR() {
	    
	    // Stop R
	    this.stopR();

	    // R integration
	    final RBuffer buffer = new RBuffer(getModel().getRModel().getBufferSize());
	    final RListener listener = new RListener(getModel().getRModel().getTicksPerSecond(), this.root.getDisplay()) {

	        @Override
	        public void bufferUpdated() {
	            setOutput(buffer.toString());
	        }

	        @Override
	        public void closed() {
	            stopR();
	        }
	    };
	    
	    // Start R
	    try {
	        this.rIntegration = new RIntegration(OS.getR(), buffer, listener);
	    } catch (Exception e) {
	        this.rIntegration = null;
	    }
	}
	
	/**
	 * Stops R
	 */
	private void stopR() {
	    if (this.rIntegration != null) {
	        this.rIntegration.shutdown();
	        this.rIntegration = null;
	    }
	}

	@Override
	protected Control createControl(Composite parent) {
		root = new Composite(parent, SWT.NONE);
        root.setLayout(RLayout.createGridLayout(1));
        
        // User input
        input = new Text(root, SWT.BORDER);
        input.setLayoutData(RLayout.createFillHorizontallyGridData(true));
        
        // Listen for enter key
        input.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent event) {
                if (event.detail == SWT.TRAVERSE_RETURN) {
                    if (input.getText() != null && !input.getText().isEmpty()) {
                        String command = input.getText();
                        input.setText("");
                        executeR(command);
                    }
                }
            }
        });

        // User output
        output = new StyledText(root, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        output.setLayoutData(RLayout.createFillGridData());
	    
		return this.root;
	}

	@Override
	protected AnalysisContextR createViewConfig(AnalysisContext context) {
		return new AnalysisContextR(context);
	}

	@Override
	protected void doReset() {
		root.setRedraw(false);
        if (this.manager != null) {
            this.manager.stop();
        }
        this.stopR();
        root.setRedraw(true);
        setStatusEmpty();
	}

	@Override
	protected void doUpdate(AnalysisContextR context) {
	    
		final DataHandle handle = context.input;
		
		Analysis analysis = new Analysis() {

            private boolean stopped  = false;
            private int     progress = 0;
            private int     total    = handle.getNumRows();

			@Override
			public int getProgress() {
				return (int)Math.round(100d * (double)progress / (double)total);
			}

			@Override
			public void onError() {
                // --> TODO: Show according message here 
				setStatusEmpty();
			}

			@Override
			public void onFinish() {
				
                if (stopped || !isEnabled()) {
                    return;
                }                
				setStatusDone();
			}

			@Override
			public void onInterrupt() {
				if (!isEnabled()) {
                    setStatusEmpty();
                } else {
                    setStatusWorking();
                }
			}

			@Override
			public void run() throws InterruptedException {
				
                long time = System.currentTimeMillis();
                
                startR();
                
        		String createcommand = createDataFrame(handle);
        		executeR("f <- " + createcommand);
        		executeR("str(f)");
        		
        		StringBuilder b = new StringBuilder();
        		
        		for (int j = 0; j < handle.getNumRows(); j++) {
        		    
        		    this.progress = j;
        		    
        		    if (stopped) {
        		        stopR();
        		        break;
        		    }
        		    
        			b.append("f[");
        			b.append(j+1);
        			b.append(",] <-");
        			appendRow(handle, j, b);
        			// System.out.println(b.toString());
        		
        			if (j % 100 == 0) {
        				executeR(b.toString());				
        				b.setLength(0); //reset the String builder so the Java Garbage Collector does not have too much work.
        			} else {
        				b.append('\n');
        			}
        		}
        		
        		executeR(b.toString());
        		executeR("str(f)");
        		
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
			}

			@Override
			public void stop() {
				System.out.println("we got stopped :(");
                this.stopped = true;
			}
		};
		this.manager.start(analysis);
	}
	
    @Override
	protected boolean isRunning() {
		return manager != null && manager.isRunning();
	}
}
