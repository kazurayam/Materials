package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.MaterialPairs
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.stats.ImageDeltaStats

abstract class ImageCollectionProcessor implements ImageCollectionProcessingContentHandler {
    
    protected ImageDifferenceFilenameResolver       filenameResolver_
    protected VisualTestingLogger                   vtLogger_
    
	abstract boolean makeImageCollectionDifferences(MaterialPairs magerialPairs, TCaseName tCaseName, double criteriaPercentage)
	
    abstract boolean makeImageCollectionDifferences(MaterialPairs materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats)
    
    void setImageDifferenceFilenameResolver(ImageDifferenceFilenameResolver filenameResolver) {
        Objects.requireNonNull(filenameResolver, "filenameResolver must not be null")
        this.filenameResolver_ = filenameResolver
    }
    
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        Objects.requireNonNull(vtLogger, "vtLogger must not be null")
        this.vtLogger_ = vtLogger
    }
    
    
}
