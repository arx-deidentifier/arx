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
import java.io.File;

/**
 * OS-specific functions for finding the R executable
 * 
 * @author Fabian Prasser
 * @author Alexander Beischl
 * @author Thuy Tran
 */
public class OS {

    /**
     * Enum for the OS type
     * 
     * @author Fabian Prasser
     */
    public static enum OSType {
        WINDOWS,
        UNIX,
        MAC
    }
    
	/** Locations*/
	private static final String[] locationsMac = {"/usr/local/bin/",
	                                              "/Applications/R.app/Contents/MacOS/R"};
	/** Locations*/
	private static final String[] locationsUnix = {"/usr/lib/R/bin",
	                                               "/usr/bin/",
	                                               "/usr/share/R/share"};

	/** Location*/
	private static final String locationWindows = "C:\\Program Files\\R\\";
	/** Executables*/
	private static final String[] executablesMac = {"R", "R.app"};
	/** Executables*/
	private static final String[] executablesUnix = {"R","exec"};
    /** Executables*/
	private static final String[] executablesWindows = {"R.exe"};
	
	/**
     * Returns the OS
     * @return
     */
    public static OSType getOS() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("win") >= 0) {
            return OSType.WINDOWS;
        } else if (os.indexOf("mac") >= 0) {
            return OSType.MAC;
        } else if ((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 )) {
            return OSType.UNIX;
        } else {
            throw new IllegalStateException("Unsupported operating system");
        }
    }

    /**
	 * Returns a path to the R executable or null if R cannot be found
	 * @return
	 */
	public static String getR() {
	    switch (getOS()) {
	    case MAC:
	        return getPath(locationsMac, executablesMac);
	    case UNIX:
	        return getPath(locationsUnix, executablesUnix);
	    case WINDOWS:
	    	File rFolder = new File(locationWindows);
	    	// check for the default R path
	    	if (rFolder.exists()) {
	    		String[] paths = rFolder.list();
	    		// check which versions of R (if any) are installed
	    		if (paths.length == 0)
	    			return null;

	    		// fill locations with the full path in reverse order to start with the latest version of R
	    		String[] locations = new String[paths.length];
	    		for(int i = 0; i < paths.length; i++) {
	    			locations[i] = locationWindows + paths[paths.length - 1 - i] + "\\bin";
	            }
	    		
	    		// start trying to find the latest version of R
	    		return getPath(locations, executablesWindows);
	    	}
	    	else {
	    		// The R installation path was not found
	    		return null;
	    	}
	    default:
	        throw new IllegalStateException("Unknown operating system");
	    }
	}

    /**
     * Returns a path to the R executable or null if R cannot be found
     * @param folder The folder to look in
     * @return
     */
    public static String getR(String folder) {
        switch (getOS()) {
        case MAC:
            return getPath(folder, executablesMac);
        case UNIX:
            return getPath(folder, executablesUnix);
        case WINDOWS:
            return getPath(folder, executablesWindows);
        default:
            throw new IllegalStateException("Unknown operating system");
        }
    }

    /**
     * Returns the path of the R executable or null if R cannot be found
     * @param locations The list of possible file system locations
     * @param executables The list of possible names for the executable file
     * @return
     */
    private static String getPath(String[] locations, String[] executables) {
        // For each location
        for (String location : locations) {
        	String path = getPath(location, executables);
        	
        	if (path != null)
        		return path;
        }
        
        // We haven't found anything
        return null;
    }

    /**
     * Returns the path of the R executable or null if R cannot be found
     * @param location A possible file system location
     * @param executables The list of possible names for the executable file
     * @return
     */
    private static String getPath(String location, String[] executables) {
        if (!location.endsWith(File.separator)) {
            location += File.separator;
        }
        
        // For each name of the executable
        for (String executable : executables) {
            try {
                
                // Check whether the file exists
                File file = new File(location + executable);
                if (file.exists()) {
                    
                    // Check if we have the permissions to run the file
                    ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "--vanilla");
                    builder.start().destroy();
                    
                    // Return
                    return file.getCanonicalPath();
                }
            } catch (Exception e) {
                // Ignore: try the next location
            }
        }
        
        // We haven't found anything
        return null;
    }

    /**
     * Returns the parameters for the R process
     * @param path 
     * @return
     */
    public static String[] getParameters(String path) {
        switch (getOS()) {
        case MAC:
            return new String[]{path, "--vanilla", "--quiet", "--interactive"};
        case UNIX:
            return new String[]{path, "--vanilla", "--quiet", "--interactive"};
        case WINDOWS:
            return new String[]{path, "--vanilla", "--quiet", "--ess"};
        default:
            throw new IllegalStateException("Unknown operating system");
        }
       
    }

	public static String[] getPossibleExecutables() {
		switch (getOS()) {
		case MAC:
			return executablesMac;
		case UNIX:
			return executablesUnix;
		case WINDOWS:
			return executablesWindows;
		default:
			throw new IllegalStateException("Unknown operating system");
		}
	}
}
