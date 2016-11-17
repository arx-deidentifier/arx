package org.deidentifier.arx.r.terminal;

/**
 * A simple listener
 * 
 * @author Fabian Prasser
 */
public interface RCommandListener {
    
    /** 
     * Implement this to listen for commands
     * 
     * @param command
     */
    public abstract void command(String command);
}
