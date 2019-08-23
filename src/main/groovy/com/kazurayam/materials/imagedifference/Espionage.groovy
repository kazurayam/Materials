package com.kazurayam.materials.imagedifference

/**
 * Espionage means spy.
 * 
 * Espionage class provides a shortcut to evaluate a ImageDifference or a ComparisonResult,
 * and depending on the evaluation result, dispatch to "then closure" and "else closure".
 * 
 * @author kazurayam
 */
class Espionage {
    
	/**
	 * 'ternary' is a term of Mathmatics, meaning 'composed of three parts'
	 * 
	 * Evaluate if imageDifference.getRatio() == 0 or not.
	 * If true, call the actionWhenIdentical closure,
	 * otherwise call the actionWhenDifferent closure.
	 * 
	 * @param imageDifference
	 * @param actionWhenIdentical
	 * @param actionWhenDifferent
	 * @return
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
    
	/**
	 * Evaluate if comparisonResult.imagesAreSimilar() == true or not.
	 * If true, call actionThen closure,
	 * otherwise, call actionElse closure.
	 * 
	 * @param comparisonResult
	 * @param actionThen
	 * @param actionElse
	 * @return
	 */
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
 
}
