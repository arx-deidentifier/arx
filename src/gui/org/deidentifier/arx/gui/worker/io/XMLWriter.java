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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;

/**
 * A writer for XML documents that can handle common objects from the ARX model.
 *
 * @author Fabian Prasser
 */
public class XMLWriter {
	
	/** The current prefix for indentation. */
	private StringBuilder prefix = new StringBuilder();
	
	/** A backing string builder. */
	private StringBuilder sBuilder = null;
	
	/** A backing file builder. */
	private FileBuilder fBuilder = null;
	
	/** The current stack of nested elements. */
	private Stack<String> elements = new Stack<String>();

	/**
     * Creates a new writer backed by a StringBuilder.
     *
     * @throws IOException
     */
	public XMLWriter() throws IOException{
		this.sBuilder = new StringBuilder();
	}
	
	/**
     * Creates a new writer backed by the given FileBuilder.
     *
     * @param builder
     * @throws IOException
     */
	public XMLWriter(FileBuilder builder) throws IOException{
		this.fBuilder = builder;
	}
	
	/**
     * Append stuff to the backing builder.
     *
     * @param value
     * @throws IOException
     */
	private void append(String value) throws IOException{
		if (fBuilder != null) fBuilder.append(value);
		else sBuilder.append(value);
	}

	/**
     * Intend the document.
     *
     * @param element
     * @throws IOException
     */
	public void indent(String element) throws IOException{
		elements.push(element);
		this.append(prefix.toString());
		this.append("<");
		this.append(element);
		this.append(">\n");
		this.prefix.append("\t");
	}

	/**
     * Intend the document.
     *
     * @param element
     * @param attribute
     * @param value
     * @throws IOException
     */
	public void indent(String element, String attribute, int value) throws IOException{
		elements.push(element);
		this.append(prefix.toString());
		this.append("<");
		this.append(element);
		this.append(" ");
		this.append(attribute);
		this.append("=\"");
		this.append(String.valueOf(value));
		this.append("\"");
		this.append(">\n");
		this.prefix.append("\t");
	}

	/**
     * Returns a string representation.
     *
     * @return
     */
	public String toString(){
		return sBuilder.toString();
	}

	/**
     * Unintend.
     *
     * @throws IOException
     */
	public void unindent() throws IOException{
		this.prefix.setLength(this.prefix.length()-1);
		String element = elements.pop();
		this.append(prefix.toString());
		this.append("</");
		this.append(element);
		this.append(">\n");
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param anonymity
     * @throws IOException
     */
	public void write(String attribute, Anonymity anonymity) throws IOException {
		write(attribute, anonymity.toString());
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param array
     * @param map
     * @throws IOException
     */
	public void write(String attribute, ARXNode[] array, Map<String, Integer> map) throws IOException {
		StringBuilder builder = new StringBuilder();
		 for (int j = 0; j < array.length; j++) {
			 builder.append(map.get(Arrays.toString(array[j].getTransformation())));
             if (j < (array.length - 1)) {
            	 builder.append(","); //$NON-NLS-1$
             }
         }
		 write(attribute, builder.toString());
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     * @throws IOException
     */
	public void write(String attribute, boolean value) throws IOException{
		write(attribute, String.valueOf(value));
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     * @throws IOException
     */
	public void write(String attribute, char value) throws IOException{
		write(attribute, String.valueOf(value));
	}
	
	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     * @throws IOException
     */
	public void write(String attribute, double value) throws IOException{
		write(attribute, String.valueOf(value));
	}

	/**
     * Create an element.
     *
     * @param attribute
     * @param array
     * @throws IOException
     */
	public void write(String attribute, int[] array) throws IOException {
		write(attribute, Arrays.toString(array));
	}

	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     * @throws IOException
     */
	public void write(String attribute, long value) throws IOException{
		write(attribute, String.valueOf(value));
	}

	/**
     * Create an element.
     *
     * @param attribute
     * @param value
     * @throws IOException
     */
	public void write(String attribute, String value) throws IOException{
		this.append(prefix.toString());
		this.append("<");
		this.append(attribute);
		this.append(">");
		this.append(StringEscapeUtils.escapeXml(value));
		this.append("</");
		this.append(attribute);
		this.append(">\n");
	}

	/**
     * Appends the string.
     *
     * @param string
     * @throws IOException
     */
	public void write(String string) throws IOException {
		this.append(string);
	}
}
