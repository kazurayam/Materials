package com.kazurayam.materials.imagedifference

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.impl.MaterialCoreImpl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class ComparisonResultBundle {
    
    static final String SERIALIZED_FILE_NAME = 'comparison-result-bundle.json'
    
    private List<ComparisonResult> bundle_
    
    ComparisonResultBundle() {
        bundle_ = new ArrayList<ComparisonResult>()
    }
    
    
    ComparisonResultBundle(Path baseDir, String jsonText) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Objects.requireNonNull(jsonText, "jsonText must not be null")
        bundle_ = new ArrayList<ComparisonResult>()
        //
        JsonSlurper slurper = new JsonSlurper()
        def jsonObjectCRB = slurper.parseText(jsonText)
        if (jsonObjectCRB.ComparisonResultBundle == null) {
            throw new IllegalArgumentException("jsonText does not have ComparisonResultBundle; ${jsonText}")
        }
        def comparisonResults = jsonObjectCRB.ComparisonResultBundle
        if ( ! comparisonResults instanceof List ) {
            throw new IllegalArgumentException("comparisonResults was expected to be List but was ${comparisonResults.class.getName()}")
        }

        for (def jsonObjectCR : comparisonResults) {
            // println "#ComparisonResultBundle jsonObjectCR=${jsonObjectCR}"
            MaterialCore expected     = new MaterialCoreImpl(baseDir, JsonOutput.toJson(jsonObjectCR.ComparisonResult.expectedMaterial))
            MaterialCore actual       = new MaterialCoreImpl(baseDir, JsonOutput.toJson(jsonObjectCR.ComparisonResult.actualMaterial))
            MaterialCore diff         = new MaterialCoreImpl(baseDir, JsonOutput.toJson(jsonObjectCR.ComparisonResult.diffMaterial))
            double criteriaPercentage = jsonObjectCR.ComparisonResult.criteriaPercentage
            boolean imagesAreSimilar  = jsonObjectCR.ComparisonResult.imagesAreSimilar
            double diffRatio          = jsonObjectCR.ComparisonResult.diffRatio
            //
            ComparisonResult cr = new ComparisonResult(expected, actual, diff,
                                            criteriaPercentage, imagesAreSimilar, diffRatio)
            bundle_.add(cr)
        }
    }
    
    void addComparisonResult(ComparisonResult cp) {
        bundle_.add(cp)
    }
    
    int size() {
        return bundle_.size()
    }
    
    /**
     * 
     * @return int how many ComparisonResult in this bundle returns false for the call to imagesAreSimilar()
     */
    int sizeOfDifferentComparisonResults() {
        int result = 0
        for (ComparisonResult cr : bundle_) {
            if ( ! cr.imagesAreSimilar() ) {
                result += 1
            }
        }
        return result
    }
    
    /**
     * 
     * @return true if all of ComparisonResult in this bundle returns true for the call to imagesAreSimilar() 
     */
    boolean allOfImagesAreSimilar() {
        return this.sizeOfDifferentComparisonResults() == 0
    }
    
    ComparisonResult get(index) {
        return bundle_.get(index)
    }
    
    /**
     * look up a ComparionResult object in the bundle.
     * The argument imageDiffPath is matched against the diff property of each
     * contained ComparisonResutl objects.
     *  
     * @param imageDiff
     * @return
     */
    ComparisonResult get(Path imageDiffPath) {
        for (ComparisonResult cr : bundle_) {
            if (cr.getDiffMaterial().getPath().equals(imageDiffPath)) {
                return cr
            }
        }
        return null
    }
    
    /**
     * @return true if this.get(imageDiff) returned non-null value,
     *         false otherwise
     */
    boolean containsImageDiff(Path imageDiffPath) {
        return this.get(imageDiffPath) != null
    }
        
    String srcOfExpectedMaterial(Path imageDiffPath) {
        ComparisonResult cr = this.get(imageDiffPath)
        if (cr != null) {
            return cr.getExpectedMaterial().getPathRelativeToRepositoryRoot()
        } else {
            return null
        }
    }
    
    String srcOfActualMaterial(Path imageDiffPath) {
        ComparisonResult cr = this.get(imageDiffPath)
        if (cr != null) {
            return cr.getActualMaterial().getPathRelativeToRepositoryRoot()
        } else {
            return null
        }
    }

        
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"ComparisonResultBundle":[')
        int count = 0
        for (ComparisonResult cp : bundle_) {
            if (count > 0) {
                sb.append(',')
            }
            count += 1
            sb.append(cp.toJsonText())
        }
        sb.append(']}')
        return sb.toString()
    }
    
}
