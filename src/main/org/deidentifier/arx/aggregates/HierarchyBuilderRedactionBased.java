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
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;

import com.carrotsearch.hppc.CharOpenHashSet;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * using redaction. Data items are 1) aligned left-to-right or right-to-left, 2) differences in
 * length are filled with a padding character, 3) then, equally long values are redacted character by character
 * from left-to-right or right-to-left.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyBuilderRedactionBased<T> extends HierarchyBuilder<T> implements Serializable {

    /**
     * Order
     */
    public static enum Order {
        
        /**  TODO */
        LEFT_TO_RIGHT,
        
        /**  TODO */
        RIGHT_TO_LEFT
    }

    /**  TODO */
    private static final long serialVersionUID = 3625654600380531803L;

    /**
     * Values are aligned left-to-right and redacted right-to-left. Redacted characters
     * are replaced with the given character. The same character is used for padding.
     *
     * @param <T>
     * @param redactionCharacter
     * @return
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(char redactionCharacter){
        return new HierarchyBuilderRedactionBased<T>(redactionCharacter);
    }
    
    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> HierarchyBuilderRedactionBased<T> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilderRedactionBased<T> result = (HierarchyBuilderRedactionBased<T>)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder.
     * Redacted characters are replaced with the given character. The same character is used for padding.
     *
     * @param <T>
     * @param alignmentOrder
     * @param redactionOrder
     * @param redactionCharacter
     * @return
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(Order alignmentOrder, 
                                                               Order redactionOrder, 
                                                               char redactionCharacter){
        return new HierarchyBuilderRedactionBased<T>(alignmentOrder, redactionOrder, redactionCharacter);
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder.
     * Redacted characters are replaced with the given character. The padding character is used for padding.
     *
     * @param <T>
     * @param alignmentOrder
     * @param redactionOrder
     * @param paddingCharacter
     * @param redactionCharacter
     * @return
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(Order alignmentOrder, 
                                                               Order redactionOrder, 
                                                               char paddingCharacter, 
                                                               char redactionCharacter){
        return new HierarchyBuilderRedactionBased<T>(alignmentOrder, redactionOrder, paddingCharacter, redactionCharacter);
    }

    /**
     * Loads a builder specification from the given file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(String file) throws IOException{
        return create(new File(file));
    }
    
    /** Alignment order. */
    private Order                aligmentOrder      = Order.LEFT_TO_RIGHT;
    
    /** Padding character. */
    private char                 paddingCharacter   = '*';
    
    /** Redaction character. */
    private char                 redactionCharacter = '*';
    
    /** Redaction order. */
    private Order                redactionOrder     = Order.RIGHT_TO_LEFT;

    /** Result. */
    private transient String[][] result;

    /**
     * Meta-data about the nature of the domain of the attribute. Modeled as Double
     * for backwards compatibility
     */
    private Double              maxValueLength;
    
    /**
     * Meta-data about the nature of the domain of the attribute. Modeled as Double
     * for backwards compatibility
     */
    private Double              domainSize;
    
    /**
     * Meta-data about the nature of the domain of the attribute. Modeled as Double
     * for backwards compatibility
     */
    private Double              alphabetSize;
    
    /**
     * Values are aligned left-to-right and redacted right-to-left. Redacted characters
     * are replaced with the given character. The same character is used for padding.
     * @param redactionCharacter
     */
    private HierarchyBuilderRedactionBased(char redactionCharacter){
        super(Type.REDACTION_BASED);
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = redactionCharacter;
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The same character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param redactionCharacter
     */
    private HierarchyBuilderRedactionBased(Order alignmentOrder, 
                                          Order redactionOrder, 
                                          char redactionCharacter){
        super(Type.REDACTION_BASED);
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = redactionCharacter;
        this.aligmentOrder = alignmentOrder;
        this.redactionOrder = redactionOrder;
    }

    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The padding character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param paddingCharacter
     * @param redactionCharacter
     */
    private HierarchyBuilderRedactionBased(Order alignmentOrder, 
                                          Order redactionOrder, 
                                          char paddingCharacter, 
                                          char redactionCharacter){
        super(Type.REDACTION_BASED);
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = paddingCharacter;
        this.aligmentOrder = alignmentOrder;
        this.redactionOrder = redactionOrder;
    }


    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @return
     */
    public Hierarchy build(){
        
        // Check
        if (result == null) {
            throw new IllegalArgumentException("Please call prepare() first");
        }
        
        // Return
        Hierarchy h = Hierarchy.create(result);
        this.result = null;
        return h;
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @param data
     * @return
     */
    public Hierarchy build(String[] data){
        prepare(data);
        return build();
    }
    
    /**
     * Returns the alignment order.
     *
     * @return
     */
    public Order getAligmentOrder() {
        return aligmentOrder;
    }
    
    /**
     * <p>Returns properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies. May return <code>null</code>.</p>
     * @return Size of the alphabet: the possible number of elements per character of any value from the domain
     */
    public Double getAlphabetSize() {
        return alphabetSize;
    }
    
    /**
     * <p>Returns properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies. May return <code>null</code>.</p>
     * @return Size of the domain: the number of elements in the domain of the attribute
     */
    public Double getDomainSize() {
        return domainSize;
    }

    /**
     * <p>Returns properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies. May return <code>null</code>.</p>
     * 
     * @return Max. length of an element: the number of characters of the largest element in the domain
     */
    public Double getMaxValueLength() {
        return maxValueLength;
    }
    
    /**
     * Returns the padding character.
     *
     * @return
     */
    public char getPaddingCharacter() {
        return paddingCharacter;
    }
    
    /**
     * Returns the redaction character.
     *
     * @return
     */
    public char getRedactionCharacter() {
        return redactionCharacter;
    }

    /**
     * Returns the redaction order.
     *
     * @return
     */
    public Order getRedactionOrder() {
        return redactionOrder;
    }
    
    /**
     * Returns whether domain-properties are available for this builder. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies.
     * @return
     */
    public boolean isDomainPropertiesAvailable() {
        return maxValueLength != null && domainSize != null && alphabetSize != null;
    }

    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     *
     * @param data
     * @return
     */
    public int[] prepare(String[] data){
        
        // Check
        if (this.result == null) {
            prepareResult(data);
        }
        
        // Compute
        int[] sizes = new int[this.result[0].length];
        for (int i=0; i < sizes.length; i++){
            Set<String> set = new HashSet<String>();
            for (int j=0; j<this.result.length; j++) {
                set.add(result[j][i]);
            }
            sizes[i] = set.size();
        }
        
        // Return
        return sizes;
    }

    /**
     * <p>Sets properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies. Required properties are:</p>
     * <ul>
     * <li>Size of the domain: the number of elements in the domain of the attribute</li>
     * <li>Size of the alphabet: the possible number of elements per character of any value from the domain</li>
     * <li>Max. length of an element: the number of characters of the largest element in the domain</li>
     * </ul>
     * <p>As a simplifying assumption, it is assumed that the domain values are distributed equally regarding 
     * their length and their characters from the alphabet.</p>
     * <p>This method will estimate the size of the domain as 
     * domainSize = alphabetSize^{maxValueLength}</p>
     * 
     * 
     * @param alphabetSize
     * @param maxValueLength
     */
    public void setAlphabetSize(int alphabetSize, int maxValueLength){

        this.domainSize = Math.pow((double)alphabetSize, (double)maxValueLength);
        this.maxValueLength = Double.valueOf(maxValueLength);
        this.alphabetSize = Double.valueOf(alphabetSize);
    }

    /**
     * <p>Sets properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies. Required properties are:</p>
     * <ul>
     * <li>Size of the domain: the number of elements in the domain of the attribute</li>
     * <li>Size of the alphabet: the possible number of elements per character of any value from the domain</li>
     * <li>Max. length of an element: the number of characters of the largest element in the domain</li>
     * </ul>
     * 
     * @param domainSize
     * @param alphabetSize
     * @param maxValueLength
     */
    public void setDomainAndAlphabetSize(int domainSize, int alphabetSize, int maxValueLength){

        this.domainSize = Double.valueOf(domainSize);
        this.maxValueLength = Double.valueOf(maxValueLength);
        this.alphabetSize = Double.valueOf(alphabetSize);
    }

    /**
     * <p>Sets properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies. Required properties are:</p>
     * <ul>
     * <li>Size of the domain: the number of elements in the domain of the attribute</li>
     * <li>Size of the alphabet: the possible number of elements per character of any value from the domain</li>
     * <li>Max. length of an element: the number of characters of the largest element in the domain</li>
     * </ul>
     * <p>As a simplifying assumption, it is assumed that the domain values are distributed equally regarding 
     * their length and their characters from the alphabet.</p>
     * <p>This method will estimate the size of the alphabet as 
     * alphabetSize = pow(domainSize, 1.0d / maxValueLength)</p>
     * 
     * @param domainSize
     * @param maxValueLength
     */
    public void setDomainSize(int domainSize, int maxValueLength){
        
        this.domainSize = Double.valueOf(domainSize);
        this.maxValueLength = Double.valueOf(maxValueLength);
        this.alphabetSize = Math.pow(domainSize, 1.0d / (double)maxValueLength);
    }

    /**
     * <p>Sets properties about the attribute's domain. Currently, this information is only used for
     * evaluating information loss with the generalized loss metric for attributes with functional
     * redaction-based hierarchies.</p>
     * 
     * @param data
     */
    public void setDomainMetadata(String[] data) {
        
        CharOpenHashSet characterSet = new CharOpenHashSet();
        this.maxValueLength = 0d;
        for (int i = 0; i < data.length; i++) {
            String value = data[i];
            this.maxValueLength = Math.max(this.maxValueLength, value.length());
            char[] charArray = value.toCharArray();
            for (int j = 0; j < charArray.length; j++) {
                characterSet.add(charArray[j]);
            }
        }
        this.domainSize = (double)data.length;
        this.alphabetSize = (double)characterSet.size();
    }

    
    /**
     * Computes the hierarchy.
     *
     * @param data
     */
    private void prepareResult(String[] data){

        // Determine length
        int length = Integer.MIN_VALUE;
        for (String s : data) {
            length = Math.max(length, s.length());
        }
        
        // Build padding string
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i=0; i<length; i++) paddingBuilder.append(paddingCharacter);
        String padding = paddingBuilder.toString();

        // Build list of base strings
        String[] base = new String[data.length];
        for (int i=0; i<data.length; i++) {
            if (data[i].length()<length) {
                String pad = padding.substring(0, length - data[i].length());
                if (aligmentOrder == Order.RIGHT_TO_LEFT) {
                    base[i] =  pad + data[i];
                } else {
                    base[i] =  data[i] + pad;
                }
            } else {
                base[i] = data[i];
            }
        }
        
        // Build padding string
        StringBuilder redactionBuilder = new StringBuilder();
        for (int i=0; i<length; i++) redactionBuilder.append(redactionCharacter);
        String redaction = redactionBuilder.toString();

        // Build result
        this.result = new String[base.length][length + 1];
        for (int i=0; i<base.length; i++){
            result[i] = new String[length + 1];
            result[i][0] = data[i];
            for (int j=1; j<length + 1; j++){
                String redact = redaction.substring(0, j);
                if (redactionOrder == Order.RIGHT_TO_LEFT) {
                    result[i][j] =  base[i].substring(0, length - j) + redact;
                } else {
                    result[i][j] =  redact + base[i].substring(0, length - j);
                }
            }
        }
    }
}
