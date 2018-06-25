/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.r;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

/**
 * Java integration with R. 
 * 
 * @author Fabian Prasser
 */
public class RIntegration {

    /** Debug flag */
    private static final boolean DEBUG   = false;
    /** Newline */
    private static final char[]  NEWLINE = System.getProperty("line.separator").toCharArray();

    /** Process */
    private Process              process;
    /** Listener */
    private RListener            listener;
    /** Buffer */
    private final RBuffer        buffer;

	public static final char[] ENDSEQUENCE = "clear()".toCharArray();

	/**
	 * Creates a new instance
	 * @param path
	 * @param buffer
	 * @throws IOException 
	 */
	public RIntegration(final String path, 
	                         final RBuffer buffer,
	                         final RListener listener) throws IOException {
	    
	    // Check args
	    if (path == null || buffer == null || listener == null) {
	        throw new NullPointerException("Argument must not be null");
	    }
	    
	    // Store
	    this.listener = listener;
	    this.buffer = buffer;
	    
	    // Create process
	    ProcessBuilder builder = new ProcessBuilder(OS.getParameters(path))
	                                 .redirectErrorStream(true); // Redirect stderr to stdout
	    
	    // Try
        try {
            
            // Start
            this.process = builder.start();
            
            // Attach process to buffer
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
					try {
                        Reader reader = new InputStreamReader(RIntegration.this.process.getInputStream());
                        int character;
                        while ((character = reader.read()) != -1) {
                            buffer.append((char) character);
							if ((char) character == ENDSEQUENCE[ENDSEQUENCE.length -1]) {
								if (buffer.compareEnding(ENDSEQUENCE)) {
									buffer.clearBuffer();
								}
							}
                            listener.fireBufferUpdatedEvent();
                        }
                        shutdown();
                    } catch (IOException e) {
                        debug(e);
                        shutdown();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
            
        } catch (IOException e) {
            shutdown();
            throw(e);
        }
	}

	/**
	 * Executes a command
	 * 
	 * @param command
	 */
	public void execute(String command) {
	    if (this.process == null) {
	        return;
	    }

        try {
            this.buffer.append(command.toCharArray());
            this.buffer.append(NEWLINE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
            writer.write(command);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            debug(e);
            shutdown();
        }
	}

	/**
     * Returns whether R is alive
     * @return
     */
    public boolean isAlive() {
        return this.process != null;
    }

    /**
	 * Closes R
	 */
    public void shutdown() {
        if (this.process != null) {
            RIntegration.this.process.destroy();
            RIntegration.this.process = null;
            listener.fireClosedEvent();
        }
    }

    /**
	 * Debug helper
	 * @param exception
	 */
	private void debug(Exception exception) {
        if (DEBUG) {
            exception.printStackTrace();
        }
    }
}