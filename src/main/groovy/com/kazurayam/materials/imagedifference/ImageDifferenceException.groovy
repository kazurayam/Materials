package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName

class ImageDifferenceException extends Exception {

    private String    message_
    private TCaseName tCaseName_
    private Material  expectedMaterial_
    private Material  actualMaterial_
    private double    criteriaPercentage_
    private Exception exception_
    
    ImageDifferenceException(String message, TCaseName tCaseName, 
                Material expectedMaterial, Material actualMaterial,
                double criteriaPercentage, Exception exception) {
        this.message_ = message
        this.tCaseName_ = tCaseName
        this.expectedMaterial_ = expectedMaterial
        this.actualMaterial_ = actualMaterial
        this.criteriaPercentage_ = criteriaPercentage
        this.exception_ = exception
    }

    ImageDifferenceException(String message, TCaseName tCaseName,
            Material expectedMaterial, Material actualMaterial,
            double criteriaPercentage) {
        this(message, tCaseName, expectedMaterial, actualMaterial, criteriaPercentage, null)
    }
}
