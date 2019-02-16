package com.kazurayam.materials

import com.kazurayam.materials.impl.ImageDiffStatsImpl

abstract class ImageDiffStats implements Comparable<ImageDiffStats> {
    
    static final ImageDiffStats ZERO = 
        new ImageDiffStatsImpl.Builder().defaultCriteriaPercentage(0.0).build()
    
    static ImageDiffStats newInstance(
            double defaultCriteriaPercentage, MaterialStorage storage) {
        // FIXME
        return ZERO
    }
    
    // --------------- attribute setter & getter ----------------------
    abstract double getDefaultCriteriaPercentage()
    
    @Override
    int compareTo(ImageDiffStats other) {
        double d = this.getDefaultCriteriaPercentage()
        return this.getDefaultCriteriaPercentage() - d
    }
}
