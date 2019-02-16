package com.kazurayam.materials.stats

import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.TSuiteName

/**
 * 
 * @author kazurayam
 */
class ImageDeltaStatsImpl extends ImageDeltaStats implements Comparable<ImageDeltaStatsImpl> {
    
    public static final double SUGGESTED_CRITERIA_PERCENTAGE = 5.0
    
    private double defaultCriteriaPercentage
    
    private List<ImageDeltaStatsEntry> statsEntries
    
    ImageDeltaStatsImpl() {
        defaultCriteriaPercentage = 0.0
        statsEntries = new ArrayList<ImageDeltaStatsEntry>()
    }
    
    
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
        ImageDeltaStats build() {
            return new ImageDeltaStatsImpl(this)
        }
    }
    
    private ImageDeltaStatsImpl(Builder builder) {
        this.defaultCriteriaPercentage = builder.defaultCriteriaPercentage
    }

    @Override
    double getDefaultCriteriaPercentage() {
        return this.defaultCriteriaPercentage
    }
    
    @Override
    List<ImageDeltaStatsEntry> getStatsEntries() {
        return statsEntries
    }
    
    @Override
    ImageDeltaStatsEntry getStatsEntry(TSuiteName tSuiteName) {
        for (ImageDeltaStatsEntry entry: statsEntries) {
            if (entry.getTSuiteName().equals(tSuiteName)) {
                return statsEntries.get(tSuiteName)
            }
        }
    }
    
    void addStatsEntry(ImageDeltaStatsEntry entry ) {
        statsEntries.add(entry)
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
    int compareTo(ImageDeltaStatsImpl other) {
        double d = this.getDefaultCriteriaPercentage()
        return this.getDefaultCriteriaPercentage() - d
    }
}
