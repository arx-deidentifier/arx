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
    
    // TODO: LOAD/SAVE OF WEIGHTS DOES NOT WORK CURRENTLY

    /** The array containing the sections of the checklist */
    private List<RiskQuestionnaireSection> sections            = new ArrayList<>();

    /** The maximum achievable weight */
    private double                         maxScore       = 0.0d;

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
        for (RiskQuestionnaireSection s : sections) {
            result += s.getScore();
        }
        result /= maxScore;
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
     * Gets the current weight configuration
     * 
     * @return the weight configuration
     */
    public RiskQuestionnaireWeights getWeightConfiguration() {
        return this.weightConfiguration;
    }

    /**
     * Sets the weight configuration and updates the values
     * 
     * @param weightConfiguration
     *            the weight configuration
     */
    public void setWeightConfiguration(RiskQuestionnaireWeights weightConfiguration) {
        if (this.weightConfiguration == weightConfiguration) { return; }

        this.weightConfiguration = weightConfiguration;
        this.maxScore = 0.0;
        for (RiskQuestionnaireSection s : this.sections) {
            s.setWeightConfiguration(weightConfiguration);
            maxScore += Math.abs(s.getWeight());
        }
    }

    @Override
    public String toString() {
        return "Questionnaire [score=" + this.getScore() + ", sections=" + sections + "\n]";
    }

    /**
     * Loads the checklist using a buffered reader
     * 
     * @param bufferedReader
     * @throws IOException 
     */
    private void load(BufferedReader bufferedReader) throws IOException {
        
        // create new and empty sections array
        sections = new ArrayList<RiskQuestionnaireSection>();

        try {

            // hold a reference to the current section
            RiskQuestionnaireSection currentSection = null;

            // read first line, then iterate over the lines
            String line = bufferedReader.readLine();
            while (line != null) {
                line = line.trim(); // get rid of leading/trailing whitespaces
                if (line.length() == 0) {
                    // do nothing
                } else if (line.startsWith("#") == true) {
                    // current line is a section
                    line = line.substring(1);
                    currentSection = RiskQuestionnaireSection.sectionFromLine(weightConfiguration, line);
                    sections.add(currentSection);
                    maxScore += Math.abs(currentSection.getWeight());
                } else {
                    // current line is an item
                    if (currentSection == null) {
                        // System.out.println("Invalid syntax. Checklist item before section (section-lines start with #)");
                    }

                    // parse and add item
                    RiskQuestionnaireQuestion newItem = RiskQuestionnaireQuestion.itemFromLine(currentSection,
                                                                                               line);
                    currentSection.addItem(newItem);
                }

                // read next line
                line = bufferedReader.readLine();
            }
            
            if (maxScore <= 0d) {
                throw new IOException("Invalid overall weight: " + maxScore);
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
