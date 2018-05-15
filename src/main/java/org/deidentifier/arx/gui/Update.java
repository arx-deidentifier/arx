/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
 */package org.deidentifier.arx.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class checks the version number
 * 
 * @author Florian Kohlmayer
 */
public class Update implements Runnable {
    
    /** The UPDATE_URL. */
    private static final String UPDATE_URL = "http://arx.deidentifier.org/wp-content/uploads/downloads/version.txt"; //$NON-NLS-1$
    
    /** The shell. */
    private final Shell         shell;

    /** The charset used to read the version number */
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    
    /**
     * Instantiates a new update.
     *
     * @param shell the shell
     */
    public Update(Shell shell) {
        this.shell = shell;
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }
    
    @Override
    public void run() {
        
        // Fetch
        final String currentVersion = Resources.getVersion();
        final String onlineVersion = getOnlineVersion();
        
        // Compare
        if (compareVersions(currentVersion, onlineVersion) < 0) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openInformation(shell, Resources.getMessage("Update.1"), Resources.getMessage("Update.2")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            });
        }
    }
    
    /**
     * Compare version string (e.g. 1.1.0 < 1.2.0).
     *
     * @param version1 first version string
     * @param version2 second version string
     * @return the int
     */
    private int compareVersions(String version1, String version2) {
        try {
            String[] version1Parts = version1.split("\\."); //$NON-NLS-1$
            String[] version2Parts = version2.split("\\."); //$NON-NLS-1$
            int length = Math.max(version1Parts.length, version2Parts.length);
            for (int i = 0; i < length; i++) {
                int version1Part = i < version1Parts.length ? Integer.parseInt(version1Parts[i]) : 0;
                int version2Part = i < version2Parts.length ? Integer.parseInt(version2Parts[i]) : 0;
                if (version1Part < version2Part) {
                    return -1;
                }
                if (version1Part > version2Part) {
                    return 1;
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Fetches the current version from the website
     * @return
     */
    private String getOnlineVersion() {
        StringBuilder builder = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(UPDATE_URL).openStream(), CHARSET ));
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line).append("\n"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return builder.toString().trim();
    }
    
}
