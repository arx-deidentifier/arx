/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.worker.io;

import org.deidentifier.arx.gui.resources.Resources;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.carrotsearch.hppc.CharArrayList;

/**
 * The default XML handler.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class XMLHandler extends DefaultHandler {

    /**  The payload */
    public String payload;
    
    /** The arraylist */
    private CharArrayList sb = new CharArrayList();

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(final char[] ch,
                           final int start,
                           final int length) throws SAXException {
        // Add to chararraylist
        sb.add(ch, start, length);
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
        payload =  new String(sb.buffer, 0, sb.size());
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
        sb.clear();
        if (!start(uri, localName, qName, attributes)) { throw new SAXException(Resources.getMessage("WorkerLoad.1") + localName); } //$NON-NLS-1$
    }
}
