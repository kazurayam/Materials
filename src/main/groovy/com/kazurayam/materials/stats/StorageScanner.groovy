package com.kazurayam.materials.stats

import java.nio.file.Path

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.imagedifference.ImageDifference
import com.kazurayam.materials.FileType
import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId

class StorageScanner {
    
    static Logger logger_ = LoggerFactory.getLogger(StorageScanner.class)
    
    /**
     * This will return ... 
     * <PRE>
     * {
     *  "defaultCriteriaPercentage":5.0,
     *  "statsEntryList":[
     *      // list of StatsEntry objects
     *  ]
     * }
     * </PRE>
     * 
     * @param materialStorage
     * @return a ImageDeltaStats object
     */
    static ImageDeltaStats scan(MaterialStorage materialStorage) {
        ImageDeltaStatsImpl.Builder builder = new ImageDeltaStatsImpl.Builder().
                                defaultCriteriaPercentage(5.0)
        for (TSuiteName tSuiteName : materialStorage.getTSuiteNameList()) {
            StatsEntry se = makeStatsEntry(materialStorage, tSuiteName)
            builder.addImageDeltaStatsEntry(se)
        }
        return builder.build()
    }
    
    /**
     * This will return
     * <PRE>
     * {
     *  "defaultCriteriaPercentage":5.0,
     *  "statsEntryList":[
     *      // a StatsEntry object of the TSuiteName specified
     *  ]
     * }
     * </PRE>
     *
     * @param materialStorage
     * @return a ImageDeltaStats object
     */
    static ImageDeltaStats scan(MaterialStorage materialStorage, TSuiteName tSuiteName) {
        ImageDeltaStatsImpl.Builder builder = new ImageDeltaStatsImpl.Builder().
                                defaultCriteriaPercentage(5.0)
        if (materialStorage.getTSuiteNameList().contains(tSuiteName)) {
            StatsEntry se = makeStatsEntry(materialStorage, tSuiteName)
            builder.addImageDeltaStatsEntry(se)
        } else {
            logger_.warn("No ${tSuiteName} is found in ${materialStorage}")
        }
        return builder.build()
    }
    
    /**
     * This will return
     * <PRE>
     *     {
     *          "TSuiteName": "47News_chronos_capture",
     *          "materialStatsList": [
     *              // list of MaterialStats objects
     *          ]
     *     }
     * </PRE>
     * 
     * @param ms
     * @param tSuiteName
     * @return a StatsEntry object
     */
    static StatsEntry makeStatsEntry(MaterialStorage ms, TSuiteName tSuiteName) {
        StatsEntry statsEntry = new StatsEntry(tSuiteName)
        Set<Path> set = 
            ms.getSetOfMaterialPathRelativeToTSuiteTimestamp(tSuiteName)
        for (Path path : set) {
            MaterialStats materialStats = makeMaterialStats(ms, tSuiteName, path)
            statsEntry.addMaterialStats(materialStats)
        }
        return statsEntry
    }

    
    /**
     * This will return
     * <PRE>
     *              {
     *                  "path: "main.TC_47News.visitSite/47NEWS_TOP.png",
     *                  "imageDeltaList": [
     *                      // list of ImageDelta objects
     *                  ],
     *                  "calculatedCriteriaPercentage": 2.51
     *              }
     * </PRE>
     * 
     * @param ms
     * @param tSuiteName
     * @return
     */
    static MaterialStats makeMaterialStats(
                                MaterialStorage ms,
                                TSuiteName tSuiteName,
                                Path pathRelativeToTSuiteTimestamp) {
                                
        // at first, look up materials of FileType.PNG 
        //   within the TSuiteName across multiple TSuiteTimestamps
        List<Material> materials = getMaterialsOfARelativePathInATSuiteName(
                                        ms,
                                        tSuiteName,
                                        pathRelativeToTSuiteTimestamp)
        
        // sort the Material list by the descending order of TSuiteTimestamp
        Collections.sort(materials, new Comparator<Material>() {
            public int compare(Material materialA, Material materialB) {
                TSuiteResult tsrA = materialA.getTCaseResult().getParent()
                TSuiteResult tsrB = materialB.getTCaseResult().getParent()
                if (tsrA > tsrB) {
                    return -1
                } else if (tsrA == tsrB) {
                    Path pathA = materialA.getPath()
                    Path pathB = materialB.getPath()
                    return pathA.compareTo(pathB)
                } else {
                    return 1
                }
            }
        })

        // build the MaterialStats object while calculating the diff ratio 
        // of two PNG files
        List<ImageDelta> imageDeltaList = new ArrayList<ImageDelta>()
        if (materials.size() > 1) {
            for (int i = 0; i < materials.size() - 1; i++) {
                ImageDelta imageDelta = makeImageDelta(
                                    materials.get(i), materials.get(i + 1))
                imageDeltaList.add(imageDelta)
            }
        }
        MaterialStats materialStats  = new MaterialStats(
                    pathRelativeToTSuiteTimestamp, imageDeltaList)
        return materialStats
    }

    /**
     *
     * @param ms
     * @param tSuiteName
     * @param pathRelativeToTSuiteTimestamp
     * @return
     */
    static List<Material> getMaterialsOfARelativePathInATSuiteName(
                                MaterialStorage ms,
                                TSuiteName tSuiteName,
                                Path pathRelativeToTSuiteTimestamp) {
        List<Material> materialList = new ArrayList<Material>()
        //
        List<TSuiteResultId> idsOfTSuiteName = ms.getTSuiteResultIdList(tSuiteName)
        for (TSuiteResultId tSuiteResultId : idsOfTSuiteName) {
            TSuiteResult tSuiteResult = ms.getTSuiteResult(tSuiteResultId)
            for (Material mate: tSuiteResult.getMaterialList()) {
                if (mate.fileType.equals(FileType.PNG) &&
                    mate.getPathRelativeToTSuiteTimestamp() ==
                            pathRelativeToTSuiteTimestamp) {
                    materialList.add(mate)
                }
            }
        }
        return materialList
    }

    /**
     * This will return
     * <PRE>
     *      { "a": "20190216_064354", "b": "20190216_064149", "delta": 0.10 }
     * </PRE>
     * 
     * @param a
     * @param b
     * @return a ImageDelta object
     */
    static ImageDelta makeImageDelta(Material a, Material b) {
        Objects.requireNonNull(a, "Material a must not be null")
        Objects.requireNonNull(b, "Material b must not be null")
        if (a.getFileType() != FileType.PNG) {
            throw new IllegalArgumentException("${a.path()} is not a PNG file")
        }
        if (b.getFileType() != FileType.PNG) {
            throw new IllegalArgumentException("${b.path()} is not a PNG file")
        }
        // create ImageDifference of the 2 given images to calculate the diff ratio
        ImageDifference diff = new ImageDifference(
                ImageIO.read(a.getPath().toFile()),
                ImageIO.read(b.getPath().toFile()))
        // make the delta
        ImageDelta imageDelta = new ImageDelta(
                                a.getParent().getParent().getTSuiteTimestamp(),
                                b.getParent().getParent().getTSuiteTimestamp(),
                                diff.getRatio())
        return imageDelta
    }
}
