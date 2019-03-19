package com.kazurayam.materials.imagedifference

import groovy.json.JsonSlurper

class ComparisonResultBundle {
    
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
    
    ComparisonResult get(int index) {
        return bundle_.get(index)
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
