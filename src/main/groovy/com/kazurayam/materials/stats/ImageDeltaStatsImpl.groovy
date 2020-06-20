package com.kazurayam.materials.stats

import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.stats.StorageScanner.Options
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

/**
 * 
 * @author kazurayam
 */
class ImageDeltaStatsImpl extends ImageDeltaStats {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaStatsImpl.class)
    
    private Options options
    
    private List<StatsEntry> imageDeltaStatsEntries
    
    /**
     *
     */
    static class Builder {
        private Options options
        private List<StatsEntry> statsEntries
        Builder() {
            options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().build()
            statsEntries = new ArrayList<StatsEntry>()
        }
        Builder storageScannerOptions(Options value) {
            Objects.requireNonNull(options, "options must not be null")
            this.options = value
            return this
        }
        Builder addImageDeltaStatsEntry(StatsEntry entry) {
            statsEntries.add(entry)
            return this
        }
        ImageDeltaStatsImpl build() {
            return new ImageDeltaStatsImpl(this)
        }
    }
    
    private ImageDeltaStatsImpl(Builder builder) {
        this.options = builder.options
        this.imageDeltaStatsEntries = builder.statsEntries
    }
    
    @Override
    double getCriteriaPercentage(TSuiteName tSuiteName,
                                 TExecutionProfile tExecutionProfile,
                                 Path pathRelativeToTSuiteTimestamp) {
        StatsEntry statsEntry = this.getImageDeltaStatsEntry(tSuiteName, tExecutionProfile)
        if (statsEntry != StatsEntry.NULL) {
            MaterialStats materialStats = statsEntry.getMaterialStats(pathRelativeToTSuiteTimestamp)
            if (materialStats != null) {
                return materialStats.getCriteriaPercentage()
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
    Options getStorageScannerOptions() {
        return this.options
    }
    
    @Override
    List<StatsEntry> getImageDeltaStatsEntryList() {
        return imageDeltaStatsEntries
    }
    
    @Override
    StatsEntry getImageDeltaStatsEntry(TSuiteName tSuiteName,
                                       TExecutionProfile tExecutionProfile) {
        for (StatsEntry entry: imageDeltaStatsEntries) {
            if (entry.getTSuiteName() == tSuiteName &&
                entry.getTExecutionProfile() == tExecutionProfile) {
                return entry
            }
        }
        return StatsEntry.NULL
    }
    
    @Override
    boolean hasImageDelta(TSuiteName tSuiteName,
                          TExecutionProfile tExecutionProfile,
                          Path relativeToTSuiteTimestampDir,
                          TSuiteTimestamp a, TSuiteTimestamp b) {
        for (StatsEntry se: imageDeltaStatsEntries) {
            if (se.getTSuiteName() == tSuiteName &&
                se.getTExecutionProfile() == tExecutionProfile) {
                if (se.hasImageDelta(relativeToTSuiteTimestampDir, a, b)) {
                    return true
                }
            }
        }
        return false
    }
    
    @Override
    ImageDelta getImageDelta(TSuiteName tSuiteName,
                             TExecutionProfile tExecutionProfile,
                             Path relativeToTSuiteTimestampDir,
                             TSuiteTimestamp a,
                             TSuiteTimestamp b) {
        for (StatsEntry se: imageDeltaStatsEntries) {
            if (se.getTSuiteName() == tSuiteName &&
                se.getTExecutionProfile() == tExecutionProfile) {

                // logger_.debug("se.hasImageDelta: ${se.hasImageDelta(relativeToTSuiteTimestampDir, a, b)}")

                if (se.hasImageDelta(relativeToTSuiteTimestampDir, a, b)) {
                    return se.getImageDelta(relativeToTSuiteTimestampDir, a, b)
                }
            }
        }
        return null
    }

    @Override
    void write(Path output) {
        Files.createDirectories(output.getParent())
        output.toFile().text = JsonOutput.prettyPrint(this.toJsonText())
    }
    
    @Override
    void write(Writer writer) {
        BufferedWriter bw = new BufferedWriter(writer)
        String text = JsonOutput.prettyPrint(this.toJsonText())
        writer.print(text)
        writer.flush()
    }    
        
    void addStatsEntry(StatsEntry entry ) {
        imageDeltaStatsEntries.add(entry)
    }

    @Override
    String toString() {
        return this.toJsonText()
    }
    
    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"storageScannerOptions\":")
        sb.append("${this.getStorageScannerOptions().toJsonText()},")
        sb.append("\"imageDeltaStatsEntries\":[")
        int count = 0
        for (StatsEntry statsEntry : imageDeltaStatsEntries) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(statsEntry.toJsonText())
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }

}
