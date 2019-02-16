package com.kazurayam.matrials.stats

import com.kazurayam.materials.Material
import com.kazurayam.materials.TSuiteName

class MaterialStats {

    private TSuiteName tSuiteName
    private double calculatedCriteriaPercentage
    private List<ImageDelta> imageDeltas
    
    MaterialStats(Material material) {
        tSuiteName = material.getDirpathRelativeToTSuiteResult()
        calculatedCriteriaPercentage = 0.0
        imageDeltas = new ArrayList<ImageDelta>()
    }

}
