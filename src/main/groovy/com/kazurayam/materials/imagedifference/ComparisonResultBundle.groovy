package com.kazurayam.materials.imagedifference

import java.nio.file.Path

import groovy.json.JsonSlurper

class ComparisonResultBundle {
    
    static final String SERIALIZED_FILE_NAME = 'comparison-result-bundle.json'
    
    private List<ComparisonResult> bundle_
    
    ComparisonResultBundle() {
        bundle_ = new ArrayList<ComparisonResult>()
    }
    
    void addComparisonResult(ComparisonResult cp) {
        bundle_.add(cp)
    }
    
    int size() {
        return bundle_.size()
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
            if (cr.getDiff().equals(imageDiffPath)) {
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
    
    static def deserializeToJsonObject(String jsonText) {
        JsonSlurper slurper = new JsonSlurper()
        def jsonObject = slurper.parseText(jsonText)
        return jsonObject
    }
}
