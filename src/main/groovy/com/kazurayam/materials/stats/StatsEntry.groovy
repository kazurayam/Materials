package com.kazurayam.materials.stats

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TSuiteName

class StatsEntry {
    
    static Logger logger_ = LoggerFactory.getLogger(StatsEntry.class)
    
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
    
    MaterialStats getMaterialStats(Path path) {
        for (MaterialStats ms: materialStatsList) {
            if (ms.getPath().equals(path)) {
                return ms
            } else {
                logger_.warn("#getMaterialStats path:${path.toString()}, ms.getPath():${ms.getPath()}," +
                    " equals?:${ms.getPath().equals(path)}")
            }
        }
        return MaterialStats.NULL
    }
    
    @Override
    String toString() {
        return this.toJson()
    }
    
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"TSuiteName\":")
        sb.append("\"${Helpers.escapeAsJsonText(tSuiteName.getValue())}\",")
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
