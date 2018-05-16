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

	/** Locations*/
	private static final String[] locationsWindows = {"C:\\Program Files\\R\\R-3.3.2\\bin", //Meanwhile there exists version 3.5.0
	                                                  "C:\\Program Files\\R\\R-2.1.5.1\\bin"}; //Suggestion: TODO change to version independent
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
	        return getPath(locationsWindows, executablesWindows);
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
            return getPath(new String[]{folder}, executablesMac);
        case UNIX:
            return getPath(new String[]{folder}, executablesUnix);
        case WINDOWS:
            return getPath(new String[]{folder}, executablesWindows);
        default:
            throw new IllegalStateException("Unknown operating system");
        }
    }
    
    /**
     * Returns the path of the R executable or null if R cannot be found
     * @return
     */
    private static String getPath(String[] locations, String[] executables) {
        
        // For each location
        for (String location : locations) {
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
