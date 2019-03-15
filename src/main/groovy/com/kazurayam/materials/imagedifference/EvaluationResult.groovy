package com.kazurayam.materials.imagedifference

import java.nio.file.Path

import com.kazurayam.materials.Material
import com.kazurayam.materials.TSuiteName

/**
 * This class encloses an instance of ImageDifference and a criteriaPercentage against which
 * the ImageDifference is evaluated to see if 2 images are similar enough or not
 * 
 * @author kazurayam
 *
 */
class EvaluationResult implements Comparable<EvaluationResult> {

    private Material expectedMaterial_
    private Material actualMaterial_
    private double criteriaPercentage_
    private boolean imagesAreSimilar_
    private double diffRatio_
    private Path diff_
    
    EvaluationResult(   Material expected,
                        Material actual, 
                        double criteriaPercentage,
                        boolean imagesAreSimilar,
                        double diffRatio,
                        Path diff) {
        this.expectedMaterial_ = expected
        this.actualMaterial_   = actual
        this.criteriaPercentage_ = criteriaPercentage
        this.imagesAreSimilar_ = imagesAreSimilar
        this.diffRatio_ = diffRatio
        this.diff_ = diff
    }
    
    Material getExpectedMaterial() {
        return this.expectedMaterial_
    }
    
    Material getActualMaterial() {
        return this.actualMaterial_
    }
    
    double getCriteriaPercentage() {
        return this.criteriaPercentage_
    }
    
    boolean imagesAreSimilar() {
        return this.imagesAreSimilar_
    }
    
    double getDiffRatio() {
        return this.diffRatio_
    }
    
    Path getDiff() {
        return this.diff_
    }
    
    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof EvaluationResult))
            return false
        EvaluationResult other = (EvaluationResult)obj
        return this.getExpectedMaterial().equals(other.getExpectedMaterial()) &&
               this.getActualMaterial().equals(other.getActualMaterial()) &&
               this.getDiff().equals(other.getDiff())
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) this.getExpectedMaterial().hashCode()
        hash = 31 * hash + (int) this.getActualMaterial().hashCode()
        return hash;
    }

    @Override
    int compareTo(EvaluationResult other) {
        int orderOfExpected = this.getExpectedMaterial().compareTo(other.getExpectedMaterial())
        if (orderOfExpected == 0) {
            int orderOfActual = this.getActualMaterial().compareTo(other.getActualMaterial())
            if (orderOfActual == 0) {
                return this.getDiff().compareTo(other.getDiff())
            } else {
                return orderOfActual
            }
        } else {
            return orderOfExpected
        }
    }

}
