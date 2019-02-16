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
    
    static ImageDeltaStats scan(MaterialStorage materialStorage) {
        ImageDeltaStatsImpl.Builder builder = new ImageDeltaStatsImpl.Builder().
                                defaultCriteriaPercentage(5.0)
        for (TSuiteName tSuiteName : materialStorage.getTSuiteNameList()) {
            StatsEntry idse = makeStatsEntry(materialStorage, tSuiteName)
            builder.addImageDeltaStatsEntry(idse)
        }
        return builder.build()
    }
    
    static ImageDeltaStats scan(MaterialStorage materialStorage, TSuiteName tSuiteName) {
        ImageDeltaStatsImpl.Builder builder = new ImageDeltaStatsImpl.Builder().
                                defaultCriteriaPercentage(5.0)
        if (materialStorage.getTSuiteNameList().contains(tSuiteName)) {
            StatsEntry idse = makeStatsEntry(materialStorage, tSuiteName)
            builder.addImageDeltaStatsEntry(idse)
        } else {
            logger_.warn("No ${tSuiteName} is found in ${materialStorage}")
        }
        return builder.build()
    }
    
    /**
     * 
     * @param mr
     * @param tSuiteName
     * @return
     */
    static StatsEntry makeStatsEntry(MaterialStorage ms, TSuiteName tSuiteName) {
        // at first, look up materials of FileType.PNG 
        //   within the TSuiteName across multiple TSuiteTimestamps
        List<Material> materials = new ArrayList<Material>()
        for (TSuiteResultId tSuiteResultId : ms.getTSuiteResultIdList(tSuiteName)) {
            TSuiteResult tSuiteResult = ms.getTSuiteResult(tSuiteResultId)
            for (Material mate: tSuiteResult.getMaterialList()) {
                if (mate.fileType.equals(FileType.PNG)) {
                    materials.add(mate)
                }
            }
        }
        
        if (materials.size() == 0) {
            throw new IllegalArgumentException("No PNG file find in the TSuiteName ${tSuiteName}")
        }
        
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
        // build the MaterialStats object while calculating the diff ratio of two PNG files
        List<ImageDelta> imageDeltaList = new ArrayList<ImageDelta>()
        if (materials.size() > 1) {
            for (int i = 0; i < materials.size() - 1; i++) {
                ImageDelta imageDelta = makeImageDelta(materials.get(i), materials.get(i + 1))
                imageDeltaList.add(imageDelta)
            }
        }
        Path relativePath = materials.get(0).getPathRelativeToTSuiteTimestamp()
        MaterialStats materialStats  = new MaterialStats(relativePath, imageDeltaList)
        return materialStats
    }

    /**
     * 
     * @param a
     * @param b
     * @return
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
        ImageDelta imageDelta = new ImageDelta(a.getParent().getParent().getTSuiteTimestamp(),
                        b.getParent().getParent().getTSuiteTimestamp(),
                        diff.getRatio())
        return imageDelta
    }
}
