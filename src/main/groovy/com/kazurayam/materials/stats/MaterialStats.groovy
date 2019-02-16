package com.kazurayam.materials.stats

import java.nio.file.Path

class MaterialStats {

    private Path path
    private List<Delta> deltaList
    
    MaterialStats(Path path, List<Delta> deltaList) {
        this.path = path
        this.deltaList = deltaList
    }

    Path getPath() {
        return path
    }
    
    double getCalculatedCriteriaPercentage() {
        throw new UnsupportedOperationException("FIXME")
    }
    
    List<Delta> getDeltaList() {
        return deltaList
    }

}
