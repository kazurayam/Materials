package com.kazurayam.materials.stats

import java.nio.file.Path

import com.kazurayam.materials.Material

class MaterialStats {

    private Path path
    private double calculatedCriteriaPercentage
    private List<Delta> deltaList
    
    MaterialStats(Material material) {
        path = material.getDirpathRelativeToTSuiteResult()
        calculatedCriteriaPercentage = 0.0
        deltaList = new ArrayList<Delta>()
    }

    Path getPath() {
        return path
    }
    
    double getCalculatedCriteriaPercentage() {
        return calculatedCriteriaPercentage
    }
    
    void setCalculatedCriteriaPercentage(double value) {
        calculatedCriteriaPercentage = value
    }
    
    List<Delta> getDeletaList() {
        return deltaList
    }
    
    void addDeleta(Delta delta) {
        
    }
}
