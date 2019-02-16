package com.kazurayam.materials.stats

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
        ImageDeltaStatsEntry idse = new ImageDeltaStatsEntry(tSuiteName)
        List<TSuiteResultId> idList = ms.getTSuiteResultIdList(tSuiteName)
        for (TSuiteResultId tSuiteResultId : idList) {
            TSuiteResult tSuiteResult = ms.getTSuiteResult(tSuiteResultId)
            List<Material> materials = tSuiteResult.getMaterialList()
            for (Material mate: materials) {
                if (mate.fileType.equals(FileType.PNG)) {
                    Path path = mate.getDirpathRelativeToTSuiteResult()
                    List<Delta> deltaList = makeDeltaList(tSuiteResult, path)
                    if (deltaList.size()> 0) {
                        MaterialStats materialStats = new MaterialStats(path, deltaList)
                        idse.addMaterialStats(materialStats)
                    }
                }
            }
        }
        return idse
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
        for (Material mate : tSuiteResult.getMaterialList()) {
            if (mate.getDirpathRelativeToTSuiteResult() == path) {
                
            }
        }
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
        throw new UnsupportedOperationException("FIXME: makeDelta() depends on the aShot library.")
    }
}
