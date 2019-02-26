package com.kazurayam.materials.stats

import java.nio.file.Files
import java.nio.file.Path

import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.ImageDeltaStats.PersistedImageDeltaStats
import com.kazurayam.materials.stats.StorageScanner
import com.kazurayam.materials.stats.StorageScanner.Options
import com.kazurayam.materials.stats.StorageScanner.Options.Builder

import groovy.json.JsonOutput

/**
 * 
 * @author kazurayam
 */
class ImageDeltaStatsImpl extends ImageDeltaStats {
    
    private Options options
    
    private List<StatsEntry> imageDeltaStatsEntries
    
    /**
     *
     */
    static class Builder {
        private Options options
        private List<StatsEntry> imageDeltaStatsEntries
        Builder() {
            options = new StorageScanner.Options.Builder() .build()
            imageDeltaStatsEntries = new ArrayList<StatsEntry>()
        }
        Builder storageScannerOptions(Options value) {
            Objects.requireNonNull(options, "options must not be null")
            this.options = value
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
        this.options = builder.options
        this.imageDeltaStatsEntries = builder.imageDeltaStatsEntries
    }
    
    @Override
    double getCriteriaPercentage(TSuiteName tSuiteName, Path pathRelativeToTSuiteTimestamp) {
        StatsEntry statsEntry = this.getImageDeltaStatsEntry(tSuiteName)
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
    
    /**
     * 
     */
    @Override
    PersistedImageDeltaStats persist(MaterialStorage ms, MaterialRepository mr,
        TSuiteName imageDiffTSuiteName, TSuiteTimestamp tSuiteTimestamp, TCaseName tCaseName, String fileName) {
        mr.putCurrentTestSuite(imageDiffTSuiteName, tSuiteTimestamp)
        Path inMaterials = mr.resolveMaterialPath(tCaseName, fileName)
        this.write(inMaterials)
        mr.scan()
        //
        TSuiteResultId tsri = TSuiteResultId.newInstance(imageDiffTSuiteName, tSuiteTimestamp)
        ms.backup(mr, tsri, true)
        List<Material> mateList = ms.getTSuiteResult(tsri).getMaterialList()
        assert mateList.size() == 1
        Material mate = mateList.get(0)
        Path inStorage = mate.getPath()
        //
        return new PersistedImageDeltaStats(inStorage, inMaterials)
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
        sb.append("\"storageScannerOptions\":")
        sb.append("${this.getStorageScannerOptions().toJson()},")
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
