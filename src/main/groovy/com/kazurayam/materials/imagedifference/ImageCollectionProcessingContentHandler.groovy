package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.MaterialDescription
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.TCaseName

interface ImageCollectionProcessingContentHandler {

    void endImageCollection(TCaseName tCaseName) throws ImageDifferenceException
    
    void endMaterialPair(ComparisonResult evalResult) throws ImageDifferenceException
    
    void startImageCollection(TCaseName tCaseName) throws ImageDifferenceException
    
    ComparisonResult startMaterialPair(
            TCaseName tCaseName,
            MaterialPair materialPair,
            double criteriaPercentage,
            MaterialDescription materialDescription) throws ImageDifferenceException
}
