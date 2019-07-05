package com.kazurayam.materials.imagedifference

/**
 * Espionage means spy.
 * 
 * @author kazurayam
 */
class Espionage {
    
    /*
    static Object ternary(ComparisonResult comparisonResult, Closure actionThen, Closure actionElse) {
        Objects.requireNonNull(comparisonResult, "comparisonResult must not be null")
        Objects.requireNonNull(actionThen, "actionThen must not be null")
        Objects.requireNonNull(actionElse, "actionElse must not be null")
        
        if (comparisonResult.imagesAreSimilar()) {
            return actionThen.call()
        } else {
            return actionElse.call()
        }
    }
     */
    
    static Object ternary(ImageDifference imageDifference, Closure actionWhenIdentical, Closure actionWhenDifferent) {
        Objects.requireNonNull(imageDifference, "imageDifference must not be null")
        Objects.requireNonNull(actionWhenIdentical, "actionWhenIdentical must not be null")
        Objects.requireNonNull(actionWhenDifferent, "actionWhenDifferent must not be null")
        
        if (imageDifference.getRatio() == 0) {
            return actionWhenIdentical.call()
        } else {
            return actionWhenDifferent.call()
        }
    }
    
}
