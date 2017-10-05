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
package org.deidentifier.arx;

import java.io.Serializable;

/**
 * Configuration for SVM classifiers
 * @author Fabian Prasser
 */
public class ARXSVMConfiguration extends ARXClassificationConfiguration implements Serializable {

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
    public static ARXSVMConfiguration create() {
        return new ARXSVMConfiguration();
    }

    /** Type */
    private MulticlassType multiclassType         = MulticlassType.ONE_VS_ALL;
    /** Soft margin penalty */
    private double         c            = 1.0d;
    /** The smooth/width parameter of the kernel. */
    private double         kernelSigma  = 1.0d;
    /** The degree of the kernel. */
    private int            kernelDegree = 2;
    /** Kernel */
    private Kernel         kernelType       = Kernel.GAUSSIAN;

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
     * Constructor
     */
    private ARXSVMConfiguration(){
        // Empty by design
    }

    /**
     * @return the c
     */
    public double getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public ARXSVMConfiguration setC(double c) {
        if (this.c != c) {
            setModified();
            this.c = c;
        }
        return this;
    }

    /**
     * @return the kernelSigma
     */
    public double getKernelSigma() {
        return kernelSigma;
    }

    /**
     * @param kernelSigma the kernelSigma to set
     */
    public ARXSVMConfiguration setKernelSigma(double kernelSigma) {
        if (this.kernelSigma != kernelSigma) {
            setModified();
            this.kernelSigma = kernelSigma;
        }
        return this;
    }

    /**
     * @return the kernelDegree
     */
    public int getKernelDegree() {
        return kernelDegree;
    }

    /**
     * @param kernelDegree the kernelDegree to set
     */
    public ARXSVMConfiguration setKernelDegree(int kernelDegree) {
        if (this.kernelDegree != kernelDegree) {
            setModified();
            this.kernelDegree = kernelDegree;
        }
        return this;
    }

    /**
     * @return the kernel
     */
    public Kernel getKernelType() {
        return kernelType;
    }

    /**
     * @param kernel the kernel to set
     */
    public ARXSVMConfiguration setKernelType(Kernel kernelType) {
        if (this.kernelType != kernelType) {
            setModified();
            this.kernelType = kernelType;
        }
        return this;
    }

    /**
     * @return the multiclassType
     */
    public MulticlassType getMulticlassType() {
        return multiclassType;
    }

    /**
     * @param multiclassType the multiclassType to set
     */
    public ARXSVMConfiguration setMulticlassType(MulticlassType multiclassType) {
        if (this.multiclassType != multiclassType) {
            setModified();
            this.multiclassType = multiclassType;
        }
        return this;
    }
}
