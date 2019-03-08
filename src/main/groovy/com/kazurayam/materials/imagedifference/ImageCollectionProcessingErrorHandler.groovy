package com.kazurayam.materials.imagedifference

interface ImageCollectionProcessingErrorHandler {
    
    void error(ImageDifferenceException ex)
    
    void fatalError(ImageDifferenceException ex)
    
    void warning(ImageDifferenceException ex)
    
}
