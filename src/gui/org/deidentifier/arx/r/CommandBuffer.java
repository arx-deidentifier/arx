package org.deidentifier.arx.r;

import java.util.ArrayList;
import java.util.List;

public class CommandBuffer {

	private static int size = 5;
	private List<String> commands = new ArrayList<String>();

	public void add(String command) {
		commands.add(0, command);
		if (commands.size() > size) {
			commands.remove(size);
		}
	}
	
	public String[] getCommands() {
		return commands.toArray(new String[0]);
	}
	
	// setter for the max size of the commands list
	public static void setCommandBufferSize(int newSize) {
		size = newSize;
	}
	// getter for the max size of the commands list
	public static int getCommandBufferSize() {
		return size;
	}
}
