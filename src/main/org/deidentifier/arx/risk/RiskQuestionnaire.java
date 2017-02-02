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

/**
 * The questionnaire holds the sections and calculates the overall score
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskQuestionnaire {

    /** The array containing the sections of the checklist */
    private ArrayList<RiskQuestionnaireSection> sections;

    /** The maximum achievable weight */
    private double                              maximumWeight = 0.0;

    /** Current weight configuration */
    protected RiskQuestionnaireWeights          weightConfiguration;

    /**
     * Create a checklist from an input stream
     * 
     * @param stream
     *            the input stream
     */
    public RiskQuestionnaire(InputStream stream) {
        super();
        weightConfiguration = new RiskQuestionnaireWeights();
        // initialize reader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        loadChecklist(bufferedReader);
    }

    /**
     * Create a checklist from a file
     * 
     * @param filename
     *            the filename
     */
    public RiskQuestionnaire(String filename) {
        super();
        weightConfiguration = new RiskQuestionnaireWeights();
        try {
            // initialize reader
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            loadChecklist(bufferedReader);
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Get the maximum weight
     * 
     * @return the maximum weight
     */
    public double getMaximumWeight() {
        return maximumWeight;
    }

    /**
     * Get the current score of the complete checklist
     * 
     * @return the score
     */
    public double getScore() {
        if (maximumWeight == 0.0) {
            // System.out.println("This Checklist has a maximum weight of 0.0, it can't calculate a score: "+this);
            return 0.0;
        }
        double result = 0.0;
        for (RiskQuestionnaireSection s : sections) {
            result += s.getWeight() * s.getScore();
        }
        result /= maximumWeight;
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
        this.maximumWeight = 0.0;
        for (RiskQuestionnaireSection s : this.sections) {
            s.setWeightConfiguration(weightConfiguration);
            maximumWeight += Math.abs(s.getWeight());
        }
    }

    @Override
    public String toString() {
        return "Checklist [score=" + this.getScore() + ", sections=" + sections + "\n]";
    }

    /**
     * Loads the checklist using a buffered reader
     * 
     * @param bufferedReader
     */
    private void loadChecklist(BufferedReader bufferedReader) {
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
                    currentSection = RiskQuestionnaireSection.sectionFromLine(weightConfiguration,
                                                                              line);
                    sections.add(currentSection);
                    maximumWeight += Math.abs(currentSection.getWeight());
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }
    }
}
