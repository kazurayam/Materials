package com.kazurayam.materials.imagedifference

/**
 * This class encloses an instance of ImageDifference and a criteriaPercentage against which
 * the ImageDifference is evaluated to see if 2 images are similar enough or not
 * 
 * @author kazurayam
 *
 */
class ImageDifferenceEvaluation {

    private ImageDifference imageDifference_
    
    private double criteriaPercentage_
    
    ImageDifferenceEvaluation(ImageDifference imageDifference, double criteriaPercentage) {
        this.imageDifference_ = imageDifference
        this.criteriaPercentage_ = criteriaPercentage
    }
    
    ImageDifference getImageDifference() {
        return this.imageDifference_
    }
    
    double getCriteriaPercentage() {
        return this.criteriaPercentage_
    }
    
    boolean imagesAreSimilar() {
        return this.getImageDifference().imagesAreSimilar(this.getCriteriaPercentage())
    }

}
