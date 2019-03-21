package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.MaterialCore

/**
 * This class encloses an instance of ImageDifference and a criteriaPercentage against which
 * the ImageDifference is evaluated to see if 2 images are similar enough or not
 * 
 * @author kazurayam
 *
 */
class ComparisonResult implements Comparable<ComparisonResult> {
    
    private MaterialCore expectedMaterial_
    private MaterialCore actualMaterial_
    private MaterialCore diff_
    private double criteriaPercentage_
    private boolean imagesAreSimilar_
    private double diffRatio_
    
    ComparisonResult(MaterialCore expected,
                        MaterialCore actual,
                        MaterialCore diff,
                        double criteriaPercentage,
                        boolean imagesAreSimilar,
                        double diffRatio) {
        this.expectedMaterial_ = expected
        this.actualMaterial_   = actual
        this.diff_ = diff
        this.criteriaPercentage_ = criteriaPercentage
        this.imagesAreSimilar_ = imagesAreSimilar
        this.diffRatio_ = diffRatio
    }
        
    MaterialCore getExpectedMaterial() {
        return this.expectedMaterial_
    }
    
    MaterialCore getActualMaterial() {
        return this.actualMaterial_
    }
    
    MaterialCore getDiff() {
        return this.diff_
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
    
    
    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof ComparisonResult))
            return false
        ComparisonResult other = (ComparisonResult)obj
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
    int compareTo(ComparisonResult other) {
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

    @Override
    String toString() {
        return this.toJsonText()
    }
    
    /**
     * <PRE>
{
    "ComparisonResult": {
        "expectedMaterial": {
            "Material": {
                "path": "build\\tmp\\testOutput\\EvaluationResultSpec\\testSmoke\\Materials\\main.TS1\\20181014_060500\\Main.Basic\\CURA_Appointment.png"
            }
        },
        "actualMaterial": {
            "Material": {
                "path": "build\\tmp\\testOutput\\EvaluationResultSpec\\testSmoke\\Materials\\main.TS1\\20181014_060501\\Main.Basic\\CURA_Appointment.png"
            }
        },
        "diff": {
            "Material": {
                "path": "build\\tmp\\testOutput\\EvaluationResultSpec\\testSmoke\\Materials\\ImageDiff\\20181014_060501\\imageDiff\\Main.Basic\\CURA_Appointment.20181014_060500_product-20181014_060501_develop.(0.01).png"
            }
        }
        "criteriaPercentage": 5.0,
        "imagesAreSimilar": true,
        "diffRatio": 3.56
    }
}
     * </PRE>
     * @return
     */
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('\"ComparisonResult\":{')
        sb.append('\"expectedMaterial\":'   + this.getExpectedMaterial().toJsonText() + ',')
        sb.append('\"actualMaterial\":'     + this.getActualMaterial().toJsonText() + ',')
        sb.append('\"diff\":\"'             + this.getDiff().toJsonText() + ',')
        sb.append('\"criteriaPercentage\":' + this.getCriteriaPercentage() + ',')
        sb.append('\"imagesAreSimilar\":'   + this.imagesAreSimilar() + ',')
        sb.append('\"diffRatio\":'          + this.getDiffRatio())
        sb.append('}')
        sb.append('}')
        return sb.toString()
    }
    
}
