package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.VisualTestingListener
import com.kazurayam.materials.stats.ImageDeltaStats

abstract class ImageCollectionProcessor implements ImageCollectionProcessingContentHandler {
    
    protected ImageCollectionProcessingErrorHandler   errorHandler_
    protected ImageDifferenceFilenameResolver         filenameResolver_
    protected VisualTestingListener                   vtListener_
    
    abstract void chronos(List<MaterialPair> materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats)
    
    void setImageDifferenceFilenameResolver(ImageDifferenceFilenameResolver filenameResolver) {
        Objects.requireNonNull(filenameResolver, "filenameResolver must not be null")
        this.filenameResolver_ = filenameResolver
    }
    
    void setErrorHandler(ImageCollectionProcessingErrorHandler errorHandler) {
        Objects.requireNonNull(errorHandler, "errorHandler must not be null")
        this.errorHandler_ = errorHandler
    }
    
    void setVisualTestingListener(VisualTestingListener vtListener) {
        Objects.requireNonNull(vtListener, "vtListener")
        this.vtListener_ = vtListener
    }
    
    abstract void twins(List<MaterialPair> magerialPairs, TCaseName tCaseName, double criteriaPercentage)
    
}
