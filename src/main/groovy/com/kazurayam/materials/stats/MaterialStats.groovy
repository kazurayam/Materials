package com.kazurayam.materials.stats

import java.nio.file.Path

class MaterialStats {

    private Path path
    private List<ImageDelta> deltaList
    
    MaterialStats(Path path, List<ImageDelta> deltaList) {
        this.path = path
        this.deltaList = deltaList
    }

    Path getPath() {
        return path
    }
    
    double getCalculatedCriteriaPercentage() {
        throw new UnsupportedOperationException("FIXME")
    }
    
    List<ImageDelta> getDeltaList() {
        return deltaList
    }

}
