package com.kazurayam.materials

import com.kazurayam.materials.impl.ImageDiffStatsImpl
import com.kazurayam.matrials.stats.StatsEntry

/**
 * <PRE>
 * {
 *  "defaultCriteriaPercentage":5.0,
 *  "statsEntries":[
 *      {
 *          "TSuiteName": "47News_chronos_capture",
 *          "materialStatsList": [
 *              {
 *                  "path: "main.TC_47News.visitSite/47NEWS_TOP.png",
 *                  "calculatedCriteriaPercentage": 2.51,
 *                  "imageDeltas": [
 *                      { "a": "20190216_064354", "b": "20190216_064149", "delta": 0.10 },
 *                      { "a": "20190216_064149", "b": "20190216_064007", "delta": 0.0  },
 *                      { "a": "20190216_064007", "b": "20190216_063205", "delta": 1.96 },
 *                      { "a": "20190216_063205", "b": "20190215_222316", "delta": 2.67 },
 *                      { "a": "20190215_222316", "b": "20190215_222146", "delta": 0.0  }
 *                  ]
 *              }
 *          ]
 *      },
 *      {
 *          "TSuiteName": "LondonStockExchange",
 *          "materials": [
 *              {
 *                  "path": "Execute/20190216_175116/captureLondonStockExchange/https%3A%2F%2Fwww.londonstockexchange.com%2Fhome%2Fhomepage.htm.png",
 *                  "calculatedCriteriaPercentage": 0.0,
 *                  "imageDeletas": [
 *                      { "a": "20190217_175210", "b": "20190216_092451", "delta": 0.0}
 *                  ]
 *          ]
 *      }
 *  }
 * }
 * </PRE>
 * @author kazurayam
 *
 */
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
    
    abstract List<StatsEntry> getStatsEntries()
    
    abstract StatsEntry getStatsEntry(TSuiteName tSuiteName)
    
    @Override
    int compareTo(ImageDiffStats other) {
        double d = this.getDefaultCriteriaPercentage()
        return this.getDefaultCriteriaPercentage() - d
    }
}
