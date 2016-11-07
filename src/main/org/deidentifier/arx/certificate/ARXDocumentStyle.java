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
package org.deidentifier.arx.certificate;

import org.apache.pdfbox.pdmodel.font.PDFont;

import rst.pdfbox.layout.elements.render.LayoutHint;
import rst.pdfbox.layout.elements.render.VerticalLayoutHint;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;

/**
 * Style information for a PDF document
 * @author Fabian Prasser
 */
public class ARXDocumentStyle {

    /** Style information */
    private float      hMargin        = 80;
    /** Style information */
    private float      vMargin        = 75;
    /** Style information */
    private LayoutHint titleHint      = VerticalLayoutHint.CENTER;
    /** Style information */
    private PDFont     textFond       = BaseFont.Helvetica.getPlainFont();
    /** Style information */
    private int        textSize       = 11;
    /** Style information */
    private PDFont     subtitleFont   = BaseFont.Helvetica.getBoldFont();
    /** Style information */
    private int        subtitleSize   = 11;
    /** Style information */
    private PDFont     titleFont      = BaseFont.Helvetica.getBoldFont();
    /** Style information */
    private int        titleSize      = 16;
    /** Style information */
    private Alignment  titleAlignment = Alignment.Center;

    /**
     * Private constructor
     */
    private ARXDocumentStyle() {
        // Empty by design
    }
    
    /**
     * Creates a new instance
     * @return
     */
    public static ARXDocumentStyle create() {
        return new ARXDocumentStyle();
    }

    /**
     * @return the hMargin
     */
    public float gethMargin() {
        return hMargin;
    }

    /**
     * @return the subtitleFont
     */
    public PDFont getSubtitleFont() {
        return subtitleFont;
    }

    /**
     * @return the subtitleSize
     */
    public int getSubtitleSize() {
        return subtitleSize;
    }

    /**
     * @return the textFond
     */
    public PDFont getTextFond() {
        return textFond;
    }

    /**
     * @return the textSize
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * @return the titleAlignment
     */
    public Alignment getTitleAlignment() {
        return titleAlignment;
    }

    /**
     * @return the titleFont
     */
    public PDFont getTitleFont() {
        return titleFont;
    }

    /**
     * @return the titleHint
     */
    public LayoutHint getTitleHint() {
        return titleHint;
    }

    /**
     * @return the titleSize
     */
    public int getTitleSize() {
        return titleSize;
    }

    /**
     * @return the vMargin
     */
    public float getvMargin() {
        return vMargin;
    }

    /**
     * @param hMargin the hMargin to set
     */
    public ARXDocumentStyle sethMargin(float hMargin) {
        this.hMargin = hMargin;
        return this;
    }

    /**
     * @param subtitleFont the subtitleFont to set
     */
    public ARXDocumentStyle setSubtitleFont(PDFont subtitleFont) {
        this.subtitleFont = subtitleFont;
        return this;
    }

    /**
     * @param subtitleSize the subtitleSize to set
     */
    public ARXDocumentStyle setSubtitleSize(int subtitleSize) {
        this.subtitleSize = subtitleSize;
        return this;
    }

    /**
     * @param textFond the textFond to set
     */
    public ARXDocumentStyle setTextFond(PDFont textFond) {
        this.textFond = textFond;
        return this;
    }

    /**
     * @param textSize the textSize to set
     */
    public ARXDocumentStyle setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    /**
     * @param titleAlignment the titleAlignment to set
     */
    public ARXDocumentStyle setTitleAlignment(Alignment titleAlignment) {
        this.titleAlignment = titleAlignment;
        return this;
    }

    /**
     * @param titleFont the titleFont to set
     */
    public ARXDocumentStyle setTitleFont(PDFont titleFont) {
        this.titleFont = titleFont;
        return this;
    }

    /**
     * @param titleHint the titleHint to set
     */
    public ARXDocumentStyle setTitleHint(LayoutHint titleHint) {
        this.titleHint = titleHint;
        return this;
    }

    /**
     * @param titleSize the titleSize to set
     */
    public ARXDocumentStyle setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    /**
     * @param vMargin the vMargin to set
     */
    public ARXDocumentStyle setvMargin(float vMargin) {
        this.vMargin = vMargin;
        return this;
    }
}
