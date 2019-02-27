package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.stats.ImageDeltaStatsImpl
import com.kazurayam.materials.stats.StatsEntry
import com.kazurayam.materials.stats.StorageScanner
import com.kazurayam.materials.stats.StorageScanner.Options
import com.kazurayam.materials.stats.StorageScanner.Options.Builder

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * ImageDeletaStats object:
 * 
<PRE>
{
    "storageScannerOptions": {
        "shiftCriteriaPercentageBy": 25.0,
        "filterDataLessThan": 1.0,
        "maximumNumberOfImageDeltas": 10,
        "onlySince": "19990101_000000",
        "onlySinceInclusive": true,
        "probability": 0.75,
        "previousImageDeltaStats": ""
    },
    "imageDeltaStatsEntries": [
        {
            "TSuiteName": "47News_chronos_capture",
            "materialStatsList": [
                {
                    "path": "main.TC_47News.visitSite/47NEWS_TOP.png",
                    "degree": 5,
                    "sum": 68.17,
                    "mean": 13.634,
                    "variance": 2.6882191428856,
                    "standardDeviation": 1.6395789529283424,
                    "tDistribution": 2.1318467859510317,
                    "confidenceInterval": {
                        "lowerBound": 12.070840401864046,
                        "upperBound": 15.197159598135954
                    },
                    "criteriaPercentage": 40.20,
                    "data": [
                        16.86,
                        4.53,
                        2.83,
                        27.85,
                        16.1
                    ],
                    "imageDeltaList": [
                        {
                            "a": "20190216_204329",
                            "b": "20190216_064354",
                            "d": 16.86
                        },
                        {
                            "a": "20190216_064354",
                            "b": "20190216_064149",
                            "d": 4.53
                        },
                        {
                            "a": "20190216_064149",
                            "b": "20190216_064007",
                            "d": 2.83
                        },
                        {
                            "a": "20190216_064007",
                            "b": "20190216_063205",
                            "d": 27.85
                        },
                        {
                            "a": "20190216_063205",
                            "b": "20190215_222316",
                            "d": 16.10
                        },
                        {
                            "a": "20190215_222316",
                            "b": "20190215_222146",
                            "d": 0.01
                        }
                    ]
                }
            ]
        }
    ]
}

 * </PRE>
 * @author kazurayam
 *
 */
abstract class ImageDeltaStats {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaStats.class)
    
    static final ImageDeltaStats ZERO = 
        new ImageDeltaStatsImpl.Builder().build()
    
    static final String IMAGE_DELTA_STATS_FILE_NAME = 'image-delta-stats.json'
    
    // --------------- attribute setter & getter ----------------------
    /**
     * if getCalculatedCriteriaPercentage() returns a valid value, then returns it.
     * otherwise return the value returned by getDefaultCriteriaPercentage()
     * 
     * @param tSuiteName
     * @param pathRelativeToTSuiteTimestamp
     * @return
     */
    abstract Options getStorageScannerOptions()
    
    abstract List<StatsEntry> getImageDeltaStatsEntryList()
    
    abstract double getCriteriaPercentage(TSuiteName tSuiteName, Path pathRelativeToTSuiteTimestamp)
    
    abstract StatsEntry getImageDeltaStatsEntry(TSuiteName tSuiteName)
    
    abstract void write(Path output)
    
    static Path resolvePath(TSuiteName imageDiffTSuiteName, TSuiteTimestamp tSuiteTimestamp, TCaseName tCaseName) {
        Path jsonPath = Paths.get(imageDiffTSuiteName.getValue()).
                        resolve(tSuiteTimestamp.format()).
                        resolve(tCaseName.getValue()).
                        resolve(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME)
        return jsonPath
    }
    
    abstract PersistedImageDeltaStats persist(MaterialStorage ms, MaterialRepository mr, Path jsonPath)
    
    
    /**
     * create an instance of ImageDeltaStats class from a file
     *
     * @param jsonPath
</PRE>
     */
    static ImageDeltaStatsImpl deserialize(String pathString) {
        Path jsonFilePath = Paths.get(pathString)
        if (Files.exists(jsonFilePath)) {
            JsonSlurper slurper = new JsonSlurper()
            def json = slurper.parse(jsonFilePath.toFile())
            logger_.debug("#deserialize json=\n${json}")
            StorageScanner.Options ssOptions = 
                new StorageScanner.Options.Builder().
                    shiftCriteriaPercentageBy (json.storageScannerOptions.shiftCriteriaPercentageBy      ).
                    filterDataLessThan        (json.storageScannerOptions.filterDataLessThan             ).
                    maximumNumberOfImageDeltas(json.storageScannerOptions.maximumNumberOfImageDeltas     ).
                    onlySince                 (new TSuiteTimestamp(json.storageScannerOptions.onlySince), 
                                                            json.storageScannerOptions.onlySinceInclusive).
                    probability               (json.storageScannerOptions.probability                    ).
                    previousImageDeltaStats   (json.storageScannerOptions.previousImageDeltaStats        ).
                    build()
            ImageDeltaStatsImpl.Builder builder = new ImageDeltaStatsImpl.Builder()
            builder.storageScannerOptions(ssOptions)
            //for (Object imageDeltaStatsEntry : (List)json.getImageDeltaStatsEntries) {
            //    StatsEntry s    tatsEntry = StatsEntry.createInstance(imageDeltaStatsEntry)
            //    builder.addImageDeltaStatsEntry(statsEntry)
            //}
            ImageDeltaStatsImpl result = builder.build()
            return result
        } else {
            logger_.warn("${jsonFilePath} does not exist")
            return null
        }
    }

    
    /**
     *
     */
    static class PersistedImageDeltaStats {
        private Path inStorage
        private Path inMaterials
        PersistedImageDeltaStats(Path inStorage, Path inMaterials) {
            this.inStorage = inStorage
            this.inMaterials = inMaterials
        }
        Path getPathInMaterials() {
            return this.inMaterials
        }
        Path getPathInStorage() {
            return this.inStorage
        }
    } 
    
}
