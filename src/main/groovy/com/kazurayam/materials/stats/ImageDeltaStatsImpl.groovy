package com.kazurayam.materials.stats

import java.nio.file.Files
import java.nio.file.Path

import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.TSuiteName

import groovy.json.JsonOutput

/**
 * 
 * @author kazurayam
 */
class ImageDeltaStatsImpl extends ImageDeltaStats {
    
    public static final double SUGGESTED_CRITERIA_PERCENTAGE = 5.0
    
    private double defaultCriteriaPercentage
    
    private List<StatsEntry> imageDeltaStatsEntries
    
    /**
     *
     */
    static class Builder {
        private double defaultCriteriaPercentage
        private List<StatsEntry> imageDeltaStatsEntries
        Builder() {
            defaultCriteriaPercentage = SUGGESTED_CRITERIA_PERCENTAGE
            imageDeltaStatsEntries = new ArrayList<StatsEntry>()
        }
        Builder defaultCriteriaPercentage(double value) {
            if (value < 0.0 || value > 100.0) {
                throw new IllegalArgumentException(
                    "defaultCrieteriaPercentage(${value}) must be positive; less than or equal to 100.0")
            }
            defaultCriteriaPercentage = value
            return this
        }
        Builder addImageDeltaStatsEntry(StatsEntry entry) {
            imageDeltaStatsEntries.add(entry)
            return this
        }
        ImageDeltaStatsImpl build() {
            return new ImageDeltaStatsImpl(this)
        }
    }
    
    private ImageDeltaStatsImpl(Builder builder) {
        this.defaultCriteriaPercentage = builder.defaultCriteriaPercentage
        this.imageDeltaStatsEntries = builder.imageDeltaStatsEntries
    }
    
    @Override
    double criteriaPercentage(TSuiteName tSuiteName, Path pathRelativeToTSuiteTimestamp) {
        double value
        try {
            value = this.getCalculatedCriteriaPercentage(tSuiteName, pathRelativeToTSuiteTimestamp)
        } catch (IllegalArgumentException e) {
            value = this.getDefaultCriteriaPercentage()
        }
        return value
    }
    
    @Override
    double getCalculatedCriteriaPercentage(TSuiteName tSuiteName, Path pathRelativeToTSuiteTimestamp) {
        StatsEntry statsEntry = this.getImageDeltaStatsEntry(tSuiteName)
        if (statsEntry != StatsEntry.NULL) {
            MaterialStats materialStats = statsEntry.getMaterialStats(pathRelativeToTSuiteTimestamp)
            if (materialStats != null) {
                return materialStats.getCalculatedCriteriaPercentage()
            } else {
                throw new IllegalArgumentException("path \"${pathRelativeToTSuiteTimestamp}\" is not " + 
                    "found in MaterialStats ${materialStats.toString()}")
            }
            
        } else {
            throw new IllegalArgumentException("TSuiteName \"${tSuiteName}\" is not " + 
                "found in this ImageDeltaStats")
        }
    }
    
    @Override
    double getDefaultCriteriaPercentage() {
        return this.defaultCriteriaPercentage
    }
    
    @Override
    List<StatsEntry> getImageDeltaStatsEntryList() {
        return imageDeltaStatsEntries
    }
    
    @Override
    StatsEntry getImageDeltaStatsEntry(TSuiteName tSuiteName) {
        for (StatsEntry entry: imageDeltaStatsEntries) {
            if (entry.getTSuiteName().equals(tSuiteName)) {
                return entry
            }
        }
        return StatsEntry.NULL
    }
    
    @Override
    void write(Path output) {
        Files.createDirectories(output.getParent())
        output.toFile().text = JsonOutput.prettyPrint(this.toJson())
    }
    
    void addStatsEntry(StatsEntry entry ) {
        imageDeltaStatsEntries.add(entry)
    }

    @Override
    String toString() {
        return this.toJson()
    }
    
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"defaultCriteriaPercentage\":")
        sb.append("${this.getDefaultCriteriaPercentage()},")
        sb.append("\"imageDeltaStatsEntries\":[")
        int count = 0
        for (StatsEntry statsEntry : imageDeltaStatsEntries) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(statsEntry.toJson())
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }

}
