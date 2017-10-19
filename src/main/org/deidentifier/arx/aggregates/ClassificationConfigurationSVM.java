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
package org.deidentifier.arx.aggregates;

import java.io.Serializable;

import org.deidentifier.arx.ARXClassificationConfiguration;

/**
 * Configuration for SVM classifiers
 * @author Fabian Prasser
 */
public class ClassificationConfigurationSVM extends ARXClassificationConfiguration<ClassificationConfigurationSVM> implements Serializable, Cloneable {

    /**
     * Kernel for the SVM
     * 
     * @author Fabian Prasser
     */
    public static enum Kernel {
        /** Kernel*/
        LAPLACIAN,
        /** Kernel*/
        GAUSSIAN,
        /** Kernel*/
        HELLINGER,
        /** Kernel*/
        HYPERBOLIC_TANGENT,
        /** Kernel*/
        LINEAR,
        /** Kernel*/
        PEARSON,
        /** Kernel*/
        POLYNOMIAL,
        /** Kernel*/
        THIN_PLATE_SPLINE,
//        /** Kernel*/
//        BINARY_SPARSE_HYPERBOLIC_TANGENT,
//        /** Kernel*/
//        BINARY_SPARSE_GAUSSIAN,
//        /** Kernel*/
//        BINARY_SPARSE_LAPLACIAN,
//        /** Kernel*/
//        BINARY_SPARSE_LINEAR,
//        /** Kernel*/
//        BINARY_SPARSE_POLYNOMIAL,
//        /** Kernel*/
//        BINARY_SPARSE_THIN_PLATE_SPLINE,
//        /** Kernel*/
//        SPARSE_GAUSSIAN,
//        /** Kernel*/
//        SPARSE_HYPERBOLIC_TANGENT,
//        /** Kernel*/
//        SPARSE_LAPLACIAN,
//        /** Kernel*/
//        SPARSE_LINEAR,
//        /** Kernel*/
//        SPARSE_POLYNOMIAL,
//        /** Kernel*/
//        SPARSE_THIN_PLATE_SPLINE,
    }

    /** 
     * Type of multiclass SVM
     */
    public static enum MulticlassType {
        ONE_VS_ALL,
        ONE_VS_ONE
    }

    /** SVUID */
    private static final long serialVersionUID = -4043281971961479819L;

    /**
     * Returns a new instance
     * @return
     */
    public static ClassificationConfigurationSVM create() {
        return new ClassificationConfigurationSVM();
    }
    
    /** Default value */
    public static final MulticlassType DEFAULT_MULTICLASS_TYPE = MulticlassType.ONE_VS_ALL;
    /** Default value */
    public static final double         DEFAULT_C               = 1.0d;
    /** Default value */
    public static final double         DEFAULT_KERNEL_SIGMA    = 1.0d;
    /** Default value */
    public static final int            DEFAULT_KERNEL_DEGREE   = 2;
    /** Default value */
    public static final Kernel         DEFAULT_KERNEL_TYPE     = Kernel.GAUSSIAN;

    /** Type */
    private MulticlassType             multiclassType          = DEFAULT_MULTICLASS_TYPE;
    /** Soft margin penalty */
    private double                     c                       = DEFAULT_C;
    /** The smooth/width parameter of the kernel. */
    private double                     kernelSigma             = DEFAULT_KERNEL_SIGMA;
    /** The degree of the kernel. */
    private int                        kernelDegree            = DEFAULT_KERNEL_DEGREE;
    /** Kernel */
    private Kernel                     kernelType              = DEFAULT_KERNEL_TYPE;
    
    /**
     * Constructor
     */
    private ClassificationConfigurationSVM(){
        // Empty by design
    }

    /** 
     * Clone constructor
     * @param deterministic
     * @param maxRecords
     * @param numberOfFolds
     * @param seed
     * @param vectorLength
     * @param numberOfTrees
     */
    protected ClassificationConfigurationSVM(boolean deterministic,
                                                    int maxRecords,
                                                    int numberOfFolds,
                                                    long seed,
                                                    int vectorLength,
                                                    MulticlassType multiclassType,
                                                    double c,
                                                    double kernelSigma,
                                                    int kernelDegree,
                                                    Kernel kernelType) {
        super(deterministic, maxRecords, numberOfFolds, seed, vectorLength);
        this.multiclassType = multiclassType;
        this.c = c;
        this.kernelSigma = kernelSigma;
        this.kernelDegree = kernelDegree;
        this.kernelType = kernelType;
    }

    @Override
    public ClassificationConfigurationSVM clone() {
        return new ClassificationConfigurationSVM(super.isDeterministic(),
                                                         super.getMaxRecords(),
                                                         super.getNumFolds(),
                                                         super.getSeed(),
                                                         super.getVectorLength(),
                                                         multiclassType,
                                                         c,
                                                         kernelSigma,
                                                         kernelDegree,
                                                         kernelType);
    }
    
    /**
     * @return the c
     */
    public double getC() {
        return c;
    }

    /**
     * @return the kernelDegree
     */
    public int getKernelDegree() {
        return kernelDegree;
    }

    /**
     * @return the kernelSigma
     */
    public double getKernelSigma() {
        return kernelSigma;
    }

    /**
     * @return the kernel
     */
    public Kernel getKernelType() {
        return kernelType;
    }

    /**
     * @return the multiclassType
     */
    public MulticlassType getMulticlassType() {
        return multiclassType;
    }

    @Override
    public void parse(ARXClassificationConfiguration<?> config) {
        super.parse(config);
        if (config instanceof ClassificationConfigurationSVM) {
            ClassificationConfigurationSVM iconfig = (ClassificationConfigurationSVM)config;
            this.setC(iconfig.c);
            this.setKernelDegree(iconfig.kernelDegree);
            this.setKernelSigma(iconfig.kernelSigma);
            this.setKernelType(iconfig.kernelType);
            this.setMulticlassType(iconfig.multiclassType);
        }
    }

    /**
     * @param c the c to set
     */
    public ClassificationConfigurationSVM setC(double c) {
        if (this.c != c) {
            setModified();
            this.c = c;
        }
        return this;
    }

    /**
     * @param kernelDegree the kernelDegree to set
     */
    public ClassificationConfigurationSVM setKernelDegree(int kernelDegree) {
        if (this.kernelDegree != kernelDegree) {
            setModified();
            this.kernelDegree = kernelDegree;
        }
        return this;
    }

    /**
     * @param kernelSigma the kernelSigma to set
     */
    public ClassificationConfigurationSVM setKernelSigma(double kernelSigma) {
        if (this.kernelSigma != kernelSigma) {
            setModified();
            this.kernelSigma = kernelSigma;
        }
        return this;
    }

    /**
     * @param kernel the kernel to set
     */
    public ClassificationConfigurationSVM setKernelType(Kernel kernelType) {
        if (this.kernelType != kernelType) {
            setModified();
            this.kernelType = kernelType;
        }
        return this;
    }

    /**
     * @param multiclassType the multiclassType to set
     */
    public ClassificationConfigurationSVM setMulticlassType(MulticlassType multiclassType) {
        if (this.multiclassType != multiclassType) {
            setModified();
            this.multiclassType = multiclassType;
        }
        return this;
    }
}
