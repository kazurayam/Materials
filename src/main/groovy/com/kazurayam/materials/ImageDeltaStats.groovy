package com.kazurayam.materials

import java.nio.file.Path

import com.kazurayam.materials.stats.ImageDeltaStatsImpl
import com.kazurayam.materials.stats.StatsEntry
import com.kazurayam.materials.stats.StorageScanner.Options

/**
 * ImageDeletaStats object:
 * 
 * <PRE>
 * {
 *  "storageScannerOptions": {
 *      "defaultCriteriaPercentage": 5.0,
 *      "filterDataLessThan": 1.0,
 *      "maximumNumberOfImageDeltas": 10,
 *      "onlySince": "20190219_021648",
 *      "onlySinceInclusive": true,
 *      "probability": 0.95
 *  },
 *  "statsEntryList":[
 *      {
 *          "TSuiteName": "47News_chronos_capture",
 *          "materialStatsList": [
 *              {
 *                  "path: "main.TC_47News.visitSite/47NEWS_TOP.png",
 *                  "imageDeltaList": [
 *                      { "a": "20190216_064354", "b": "20190216_064149", "delta": 0.10 },
 *                      { "a": "20190216_064149", "b": "20190216_064007", "delta": 0.0  },
 *                      { "a": "20190216_064007", "b": "20190216_063205", "delta": 1.96 },
 *                      { "a": "20190216_063205", "b": "20190215_222316", "delta": 2.67 },
 *                      { "a": "20190215_222316", "b": "20190215_222146", "delta": 0.0  }
 *                  ],
 *                  "calculatedCriteriaPercentage": 2.51
 *              }
 *          ]
 *      },
 *      {
 *          "TSuiteName": "LondonStockExchange",
 *          "materialStatsList": [
 *              {
 *                  "path": "Execute/20190216_175116/captureLondonStockExchange/https%3A%2F%2Fwww.londonstockexchange.com%2Fhome%2Fhomepage.htm.png",
 *                  "imageDeltaList": [
 *                      { "a": "20190217_175210", "b": "20190216_092451", "delta": 0.0}
 *                  ],
 *                  "calculatedCriteriaPercentage": 0.0
 *              }
 *          ]
 *      }
 *  }
 *  
 * }
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
