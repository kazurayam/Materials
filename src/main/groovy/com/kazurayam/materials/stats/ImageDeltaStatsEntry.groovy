package com.kazurayam.materials.stats

import com.kazurayam.materials.TSuiteName

class ImageDeltaStatsEntry {
    
    static final ImageDeltaStatsEntry NULL = new ImageDeltaStatsEntry(null)

    private TSuiteName tSuiteName
    
    private List<MaterialStats> materialStatsList
    
    ImageDeltaStatsEntry(TSuiteName tSuiteName) {
        this.tSuiteName = tSuiteName
        this.materialStatsList = new ArrayList<MaterialStats>() 
    }

    TSuiteName getTSuiteName() {
        return tSuiteName
    }
    
    void addMaterialStats(MaterialStats materialStats) {
        this.materialStatsList.add(materialStats)
    }
    
    List<MaterialStats> getMaterialStatsList() {
        return materialStatsList
    }
    
}
