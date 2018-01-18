package org.deidentifier.arx.gui.view.impl.utility;

import java.util.Date;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.deidentifier.arx.r.OS;
import org.deidentifier.arx.r.RBuffer;
import org.deidentifier.arx.r.RIntegration;
import org.deidentifier.arx.r.RListener;
import org.deidentifier.arx.r.terminal.RCommandListener;
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
    
	/** Buffer size*/
    private static final int BUFFER_SIZE = 100000;
    /** Event delay*/
    private static final int EVENT_DELAY = 10;
    /** Widget */
    private Text       input;
    /** Widget */
    private StyledText output;
    /** Listener */
    private RCommandListener listener;
    
    private RBuffer buffer;
	private RIntegration rIntegration;
    

	public ViewStatisticsRTerminal(Composite parent, 
			Controller controller, ModelPart target, 
			ModelPart reset) {
		
		super(parent, controller, target, reset, true);
	    this.manager = new AnalysisManager(parent.getDisplay());
	}

	@Override
	public ViewUtilityType getType() {
		return LayoutUtility.ViewUtilityType.SUMMARY;
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
                        if (listener != null) {
                            listener.command(command);
                        }
                    }
                }
            }
        });

        // User output
        output = new StyledText(root, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        output.setLayoutData(RLayout.createFillGridData());
	           
        initializeRIntegration();
        
		return this.root;
	}
	
	private void initializeRIntegration() {
		// R integration
	    this.buffer = new RBuffer(BUFFER_SIZE);
	    final RListener listener = new RListener(EVENT_DELAY) {

	        @Override
	        public void bufferUpdated() {
	            setOutput(buffer.toString());
	        }

	        @Override
	        public void closed() {
	            // TODO: Handle
	        }
	    };
	    
	    // Start integration
	    this.rIntegration = new RIntegration(OS.getR(), buffer, listener);
	    
	    // Redirect user input
	    this.listener = new RCommandListener() {
	        @Override
	        public void command(String command) {
	            rIntegration.execute(command);
	        }
	    };	    
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
	
	private String createDataFrame(DataHandle handle, int numRows) {		
		StringBuilder b = new StringBuilder();
		b.append("data.frame(");
		for (int i = 0; i < handle.getNumColumns(); i++) {
			String attributeName = handle.getAttributeName(i);
			b.append('"'); //There have to be quotation marks because there could be hyphens which otherwise provoke an error.
			b.append(attributeName);
			b.append("\"=");
			b.append(convertARXToRType(handle.getDataType(attributeName), numRows)); //speed things up through allocating memory beforehand with numRows
			b.append(',');
		}
		b.append("stringsAsFactors=FALSE)");
		
		return b.toString();
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

        // TODO reset the table?
        buffer.clearBuffer();
        root.setRedraw(true);
        setStatusEmpty();
	}

	@Override
	protected void doUpdate(AnalysisContextR context) {
		System.out.println("doUpdate");
		
		final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
		
		Analysis analysis = new Analysis() {
			
			private boolean                    stopped = false;

			@Override
			public int getProgress() {
				return 0;
			}

			@Override
			public void onError() {
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
				// TODO: create new Table only at the beginning and not everytime a new column gets selected.
				
                long time = System.currentTimeMillis();
                
                DataHandle handle = null;
        		if (ViewStatisticsRTerminal.this.getTarget().name() == "INPUT") {
        			handle = context.model.getInputConfig().getInput().getHandle();
        		} else if (ViewStatisticsRTerminal.this.getTarget().name() == "OUTPUT") {
        			handle = context.model.getOutput();
        		} else {
        			throw new IllegalArgumentException("Unknown target.");
        		}
        		
        		int numRows = builder.getFrequencyDistribution(0).count;//handle.getNumRows() delivers all entries, not only the GUI selection
        		
        		String createcommand = createDataFrame(handle, numRows);
        		rIntegration.execute("f <- " + createcommand);
        		rIntegration.execute("str(f)");
        		
        		StringBuilder b = new StringBuilder();
        		
        		for (int j = 0; j < numRows; j++) {
        			b.append("f[");
        			b.append(j+1);
        			b.append(",] <-");
        			appendRow(handle, j, b);
        			System.out.println(b.toString());
        		
        			if (j % 100 == 0) {
        				rIntegration.execute(b.toString());				
        				b.setLength(0); //reset the String builder so the Java Garbage Collector does not have too much work.
        			} else {
        				b.append('\n');
        			}
        		}	
        		rIntegration.execute(b.toString());
        		
        		rIntegration.execute("str(f)");
        		
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
			}

			@Override
			public void stop() {
				System.out.println("we got stopped :(");
				builder.interrupt();
                this.stopped = true;
			}
		};
		this.manager.start(analysis);
	}

	@Override
	protected boolean isRunning() {
		return manager != null && manager.isRunning();
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

}
