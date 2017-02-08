/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * The questionnaire holds the sections and calculates the overall score
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskQuestionnaire {
    
    /** The array containing the sections of the checklist */
    private List<RiskQuestionnaireSection> sections            = new ArrayList<>();

    /**
     * Create a questionnaire from an input stream
     * 
     * @param stream the input stream
     * @throws IOException 
     */
    public RiskQuestionnaire(InputStream stream) throws IOException {
        load(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Create a questionnaire from a file
     * 
     * @param filename the filename
     * @throws IOException 
     */
    public RiskQuestionnaire(String filename) throws IOException {
        load(new BufferedReader(new FileReader(filename)));
    }

    /**
     * Get the current score of the complete checklist
     * 
     * @return the score
     */
    public double getScore() {
        
        double result = 0.0;
        double max = 0d;
        for (RiskQuestionnaireSection s : sections) {
            result += s.getScore();
            max += s.getWeight();
        }
        result /= max;
        return result;
    }

    /**
     * Get all sections
     * 
     * @return the sections
     */
    public RiskQuestionnaireSection[] getSections() {
        return (sections.toArray(new RiskQuestionnaireSection[sections.size()]));
    }
    
    /**
     * Sets the weights
     * @param weights
     */
    public void setWeights(RiskQuestionnaireWeights weights) {
        for (RiskQuestionnaireSection section : this.sections) {
            section.setWeights(weights);
        }
    }

    /**
     * Loads the checklist using a buffered reader
     * 
     * @param bufferedReader
     * @throws IOException 
     */
    private void load(BufferedReader bufferedReader) throws IOException {

        try {

            // Hold a reference to the current section
            RiskQuestionnaireSection currentSection = null;

            // Read first line, then iterate over the lines
            String line = bufferedReader.readLine();
            while (line != null) {
                
                // Get rid of leading/trailing whitespaces
                line = line.trim(); 
                if (line.length() == 0) {
                    
                    // Ignore
                    
                } else if (line.startsWith("#")) {
                    
                    // Current line is a section
                    currentSection = new RiskQuestionnaireSection(line.substring(1));
                    sections.add(currentSection);
                    
                } else {
                    
                    // Current line is an item
                    if (currentSection == null) {
                        throw new IOException("Invalid questionnaire specification");
                    }

                    // parse and add item
                    currentSection.addItem(new RiskQuestionnaireQuestion(line));
                }

                // read next line
                line = bufferedReader.readLine();
            }
            
        } catch (FileNotFoundException e) {
            throw(e);
        } catch (IOException e) {
            throw(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                   // Ignore
                }
            }
        }
    }
}
