package com.kazurayam.materials.impl

import com.kazurayam.materials.ImageDiffStats

/**
 * 
 * @author kazurayam
 */
class ImageDiffStatsImpl extends ImageDiffStats implements Comparable<ImageDiffStatsImpl> {
    
    public static final double SUGGESTED_CRITERIA_PERCENTAGE = 5.0
    
    private double defaultCriteriaPercentage
    
    /**
     *
     */
    static class Builder {
        private double defaultCriteriaPercentage
        Builder() {
            defaultCriteriaPercentage = SUGGESTED_CRITERIA_PERCENTAGE
        }
        Builder defaultCriteriaPercentage(double value) {
            if (value < 0.0 || value > 100.0) {
                throw new IllegalArgumentException(
                    "defaultCrieteriaPercentage(${value}) must be positive; less than or equal to 100.0")
            }
            defaultCriteriaPercentage = value
            return this
        }
        ImageDiffStats build() {
            return new ImageDiffStatsImpl(this)
        }
    }
    
    private ImageDiffStatsImpl(Builder builder) {
        this.defaultCriteriaPercentage = builder.defaultCriteriaPercentage
    }

    double getDefaultCriteriaPercentage() {
        return this.defaultCriteriaPercentage
    }

    @Override
    String toString() {
        return this.toJson()
    }
    
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"defaultCriteriaPercentage\":")
        sb.append("${this.getDefaultCriteriaPercentage()}")
        sb.append("}")
        return sb.toString()
    }
    
    @Override
    int compareTo(ImageDiffStatsImpl other) {
        double d = this.getDefaultCriteriaPercentage()
        return this.getDefaultCriteriaPercentage() - d
    }
}
