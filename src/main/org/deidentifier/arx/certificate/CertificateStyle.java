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

import rst.pdfbox.layout.elements.render.LayoutHint;
import rst.pdfbox.layout.elements.render.VerticalLayoutHint;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;

/**
 * Style information for a PDF document
 * @author Fabian Prasser
 */
public class CertificateStyle { // NO_UCD
    
    /**
     * Enum for list styles
     * 
     * @author Fabian Prasser
     */
    public enum ListStyle {
        ARABIC, ROMAN, ALPHABETICAL, LOWERCASE_ALPHABETICAL, BULLETS
    }

    /**
     * Creates a new instance
     * @return
     */
    public static CertificateStyle create() {
        return new CertificateStyle();
    }

    /** Style information */
    private float      hMargin        = 80;
    /** Style information */
    private float      vMargin        = 75;
    /** Style information */
    private LayoutHint titleHint      = VerticalLayoutHint.CENTER;
    /** Style information */
    private BaseFont   textFont       = BaseFont.Helvetica;
    /** Style information */
    private int        textSize       = 11;
    /** Style information */
    private BaseFont   subtitleFont   = BaseFont.Helvetica;
    /** Style information */
    private int        subtitleSize   = 11;
    /** Style information */
    private BaseFont   titleFont      = BaseFont.Helvetica;
    /** Style information */
    private int        titleSize      = 16;
    /** Style information */
    private Alignment  titleAlignment = Alignment.Center;
    /** Style information */
    private int        listIndent     = 30;

    /**
     * Private constructor
     */
    private CertificateStyle() {
        // Empty by design
    }

    /**
     * @return the hMargin
     */
    public float gethMargin() {
        return hMargin;
    }
    
    /**
     * @return the listIndent
     */
    public int getListIndent() {
        return listIndent;
    }

    /**
     * @return the subtitleFont
     */
    public BaseFont getSubtitleFont() {
        return subtitleFont;
    }

    /**
     * @return the subtitleSize
     */
    public int getSubtitleSize() {
        return subtitleSize;
    }

    /**
     * @return the textFont
     */
    public BaseFont getTextFont() {
        return textFont;
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
    public BaseFont getTitleFont() {
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
    public CertificateStyle sethMargin(float hMargin) {
        this.hMargin = hMargin;
        return this;
    }

    /**
     * @param listIndent the listIndent to set
     */
    public CertificateStyle setListIndent(int listIndent) {
        this.listIndent = listIndent;
        return this;
    }

    /**
     * @param subtitleFont the subtitleFont to set
     */
    public CertificateStyle setSubtitleFont(BaseFont subtitleFont) {
        this.subtitleFont = subtitleFont;
        return this;
    }

    /**
     * @param subtitleSize the subtitleSize to set
     */
    public CertificateStyle setSubtitleSize(int subtitleSize) {
        this.subtitleSize = subtitleSize;
        return this;
    }

    /**
     * @param textFond the textFont to set
     */
    public CertificateStyle setTextFont(BaseFont textFont) {
        this.textFont = textFont;
        return this;
    }

    /**
     * @param textSize the textSize to set
     */
    public CertificateStyle setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    /**
     * @param titleAlignment the titleAlignment to set
     */
    public CertificateStyle setTitleAlignment(Alignment titleAlignment) {
        this.titleAlignment = titleAlignment;
        return this;
    }

    /**
     * @param titleFont the titleFont to set
     */
    public CertificateStyle setTitleFont(BaseFont titleFont) {
        this.titleFont = titleFont;
        return this;
    }

    /**
     * @param titleHint the titleHint to set
     */
    public CertificateStyle setTitleHint(LayoutHint titleHint) {
        this.titleHint = titleHint;
        return this;
    }

    /**
     * @param titleSize the titleSize to set
     */
    public CertificateStyle setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    /**
     * @param vMargin the vMargin to set
     */
    public CertificateStyle setvMargin(float vMargin) {
        this.vMargin = vMargin;
        return this;
    }
}
