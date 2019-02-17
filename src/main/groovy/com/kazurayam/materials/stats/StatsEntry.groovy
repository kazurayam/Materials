package com.kazurayam.materials.stats

import com.kazurayam.materials.TSuiteName

class StatsEntry {
    
    static final StatsEntry NULL = new StatsEntry(null)

    private TSuiteName tSuiteName
    
    private List<MaterialStats> materialStatsList
    
    StatsEntry(TSuiteName tSuiteName) {
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
    
    @Override
    String toString() {
        return this.toJson()
    }
    
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"TSuiteName\":")
        sb.append("\"${tSuiteName.getValue()}\",")
        sb.append("\"materialStatsList\":")
        int count = 0
        sb.append("[")
        for (MaterialStats ms : materialStatsList) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(ms.toJson())
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }
}
