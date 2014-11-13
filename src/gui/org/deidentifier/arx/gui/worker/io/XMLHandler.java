/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.worker.io;

import org.deidentifier.arx.gui.resources.Resources;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The default XML handler.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class XMLHandler extends DefaultHandler {

    /**  TODO */
    public String payload;

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(final char[] ch,
                           final int start,
                           final int length) throws SAXException {
        // Directly unescape stuff
        payload = new String(ch, start, length);
    }

    /**
     * 
     *
     * @param uri
     * @param localName
     * @param qName
     * @return
     * @throws SAXException
     */
    protected abstract boolean end(String uri,
                                   String localName,
                                   String qName) throws SAXException;

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(final String uri,
                           final String localName,
                           final String qName) throws SAXException {
        if (!end(uri, localName, qName)) { throw new SAXException(Resources.getMessage("WorkerLoad.0") + localName); } //$NON-NLS-1$
    }

    /**
     * 
     *
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @return
     * @throws SAXException
     */
    protected abstract boolean
            start(String uri,
                  String localName,
                  String qName,
                  Attributes attributes) throws SAXException;

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void
            startElement(final String uri,
                         final String localName,
                         final String qName,
                         final Attributes attributes) throws SAXException {
        if (!start(uri, localName, qName, attributes)) { throw new SAXException(Resources.getMessage("WorkerLoad.1") + localName); } //$NON-NLS-1$
    }
}
