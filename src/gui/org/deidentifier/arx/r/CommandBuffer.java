package org.deidentifier.arx.r;

public class CommandBuffer {
	
	private String[] commandBuffer; 
	private int commandBufferOffset;
	private int length;

	public CommandBuffer() {
		commandBuffer = new String[5];
		commandBufferOffset = 0;
		length = 0;
	}
	
	public void appendToCommandBuffer(String command) {
		if (commandBufferOffset < commandBuffer.length) {
			commandBuffer[commandBufferOffset] = command;
			commandBufferOffset++;
		} else {
			commandBuffer[0] = command;
			commandBufferOffset = 1;
		}
		if(length < commandBuffer.length) length++;
	}
	
	public String[] getLastCommands() {
		String[] lastCommands = new String[length];
		int templ = length;
		//from offset backwards to 0, from length-1 backwards to either empty string(/null?) or offset+1.
        int j = 0;
		for (int i=commandBufferOffset-1; i >= 0; i--) {
        	lastCommands[j] = commandBuffer[i];
        	j++;
        	templ--;
        }
		if (templ > 0) { //If templ > 0 length should be equal commandBuffer.length and there is no null case
			for (int i= commandBuffer.length-1; i>= commandBufferOffset; i--) {
				lastCommands[j] = commandBuffer[i];
			 	j++;
			}
		}
		return lastCommands;
	}
	
}
