package com.kazurayam.materials

import java.nio.file.Path

import com.kazurayam.materials.stats.ImageDeltaStatsImpl
import com.kazurayam.materials.stats.StatsEntry
import com.kazurayam.materials.stats.StorageScanner.Options

/**
 * ImageDeletaStats object:
 * 
 * <PRE>
{
    "storageScannerOptions": {
        "defaultCriteriaPercentage": 25.0,
        "filterDataLessThan": 1.0,
        "maximumNumberOfImageDeltas": 10,
        "onlySince": "19990101_000000",
        "onlySinceInclusive": true,
        "probability": 0.75
    },
    "imageDeltaStatsEntries": [
        {
            "TSuiteName": "47News_chronos_capture",
            "materialStatsList": [
                {
                    "path": "main.TC_47News.visitSite/47NEWS_TOP.png",
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
                    ],
                    "data": [
                        16.86,
                        4.53,
                        2.83,
                        27.85,
                        16.1
                    ],
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
                    "calculatedCriteriaPercentage": 40.20
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
    
    static final ImageDeltaStats ZERO = 
        new ImageDeltaStatsImpl.Builder().build()
    
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
    
}
