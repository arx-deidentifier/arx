package org.deidentifier.arx.r.terminal;

import java.io.IOException;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Test {

    public static void main(String[] args) throws IOException {

        Display display = new Display();
        Shell shell = createShell(display);
        new TestTerminal(shell);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /**
     * Creates the shell
     * @param display
     * @return
     */
    private static Shell createShell(Display display) {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("R-Terminal");
        return shell;
    }
}
