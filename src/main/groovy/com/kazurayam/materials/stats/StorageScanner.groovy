package com.kazurayam.materials.stats

import java.nio.file.Path

import javax.imageio.ImageIO

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
            ImageDeltaStatsEntry idse = makeImageDeltaStatsEntry(materialStorage, tSuiteName)
            builder.addImageDeltaStatsEntry(idse)
        }
        return builder.build()
    }
    
    static ImageDeltaStats scan(MaterialStorage materialStorage, TSuiteName tSuiteName) {
        ImageDeltaStatsImpl.Builder builder = new ImageDeltaStatsImpl.Builder().
                                defaultCriteriaPercentage(5.0)
        if (materialStorage.getTSuiteNameList().contains(tSuiteName)) {
            ImageDeltaStatsEntry idse = makeImageDeltaStatsEntry(materialStorage, tSuiteName)
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
    static ImageDeltaStatsEntry makeImageDeltaStatsEntry(MaterialStorage ms, TSuiteName tSuiteName) {
        // at first, look up materials of PNG within the TSuiteName across multiple TSuiteTimestamps
        List<Material> materials = new ArrayList<Material>()
        for (TSuiteResultId tSuiteResultId : ms.getTSuiteResultIdList(tSuiteName)) {
            TSuiteResult tSuiteResult = ms.getTSuiteResult(tSuiteResultId)
            for (Material mate: tSuiteResult.getMaterialList()) {
                if (mate.fileType.equals(FileType.PNG)) {
                    materials.add(mate)
                }
            }
        }
        // sort the list by the descending order of TSuiteTimestamp
        
        // build ImageDeltaStatsEntry object while ...
        throw new UnsupportedOperationException("TODO")
        //ImageDeltaStatsEntry idse = new ImageDeltaStatsEntry(tSuiteName)
        //return idse
    }
    
    /**
     * 
     * @param ms
     * @param tSuiteName
     * @param path
     * @return
     */
    static List<Delta> makeDeltaList(TSuiteResult tSuiteResult, Path path) {
        List<Delta> deltaList = new ArrayList<Delta>()
        // look up Materials of which path is equal to the given param
        List<Material> materials = tSuiteResult.getMaterialList(path)
        // sort the list of materials by 
        
        //
        return deltaList
    }
    
    /**
     * 
     * @param a
     * @param b
     * @return
     */
    static Delta makeDelta(Material a, Material b) {
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
        Delta delta = new Delta(a.getParent().getParent().getTSuiteTimestamp(),
                        b.getParent().getParent().getTSuiteTimestamp(),
                        diff.getRatio())
        return delta
    }
}
