package org.deidentifier.arx.r.terminal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

/**
 * Terminal tab
 * 
 * @author Fabian Prasser
 */
public class RTerminalTab {

    /** Widget */
    private final Text       input;
    /** Widget */
    private final StyledText output;
    /** Widget */
    private final Composite  root;
    /** Listener */
    private RCommandListener listener;

    /**
     * Creates a new instance
     * @param folder
     */
    public RTerminalTab(TabFolder folder) {

        // Root
        root = new Composite(folder, SWT.NONE);
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
        output = new StyledText(root, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        output.setLayoutData(RLayout.createFillGridData());
    }

    /**
     * Returns the control
     * @return
     */
    public Control getControl() {
        return this.root;
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
    
    /**
     * Sets the listener
     * @param listener
     */
    public void setCommandListener(RCommandListener listener) {
        this.listener = listener;
    }
}
