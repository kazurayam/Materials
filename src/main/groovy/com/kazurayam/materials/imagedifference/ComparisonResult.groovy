package com.kazurayam.materials.imagedifference

import java.nio.file.Path

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TSuiteName

/**
 * This class encloses an instance of ImageDifference and a criteriaPercentage against which
 * the ImageDifference is evaluated to see if 2 images are similar enough or not
 * 
 * @author kazurayam
 *
 */
class ComparisonResult implements Comparable<ComparisonResult> {

    private Material expectedMaterial_
    private Material actualMaterial_
    private double criteriaPercentage_
    private boolean imagesAreSimilar_
    private double diffRatio_
    private Path diff_
    
    ComparisonResult(Material expected,
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
                "url": "null",
                "suffix": "",
                "fileType": {
                    "FileType": {
                        "extension": "png",
                        "mimeTypes": [
                            "image/png"
                        ]
                    }
                },
                "path": "build\\tmp\\testOutput\\EvaluationResultSpec\\testSmoke\\Materials\\main.TS1\\20181014_060500\\Main.Basic\\CURA_Appointment.png",
                "lastModified": "2019-03-04T00:26:49.686"
            }
        },
        "actualMaterial": {
            "Material": {
                "url": "null",
                "suffix": "",
                "fileType": {
                    "FileType": {
                        "extension": "png",
                        "mimeTypes": [
                            "image/png"
                        ]
                    }
                },
                "path": "build\\tmp\\testOutput\\EvaluationResultSpec\\testSmoke\\Materials\\main.TS1\\20181014_060501\\Main.Basic\\CURA_Appointment.png",
                "lastModified": "2019-03-04T00:26:49.733"
            }
        },
        "criteriaPercentage": 5.0,
        "imagesAreSimilar": true,
        "diffRatio": 3.56,
        "diff": "build\\tmp\\testOutput\\EvaluationResultSpec\\testSmoke\\Materials\\ImageDiff\\20181014_060501\\imageDiff\\Main.Basic\\CURA_Appointment.20181014_060500_product-20181014_060501_develop.(0.01).png"
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
        sb.append('\"criteriaPercentage\":' + this.getCriteriaPercentage() + ',')
        sb.append('\"imagesAreSimilar\":'   + this.imagesAreSimilar() + ',')
        sb.append('\"diffRatio\":'          + this.getDiffRatio() + ',')
        sb.append('\"diff\":\"' + Helpers.escapeAsJsonText(this.getDiff().toString()) + '\"')
        sb.append('}')
        sb.append('}')
        return sb.toString()
    }
}
