package org.deidentifier.arx.r;

import java.util.ArrayList;
import java.util.List;

public class CommandBuffer {

	private static final int SIZE = 5;
	private List<String> commands = new ArrayList<String>();

	public void add(String command) {
		commands.add(0, command);
		if (commands.size() > SIZE) {
			commands.remove(SIZE);
		}
	}

	public String[] getCommands() {
		return commands.toArray(new String[0]);
	}
}