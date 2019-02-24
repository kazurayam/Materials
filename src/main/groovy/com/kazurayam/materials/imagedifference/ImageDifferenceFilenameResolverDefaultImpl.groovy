package com.kazurayam.materials.imagedifference

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.ExecutionProfile
import com.kazurayam.materials.view.ExecutionProfileImpl
import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.Material
import com.kazurayam.materials.TSuiteResult

/**
 *
 * @author kazurayam
 *
 */
class ImageDifferenceFilenameResolverDefaultImpl implements ImageDifferenceFilenameResolver {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDifferenceFilenameResolverDefaultImpl.class)
    
    /**
     * Given with the following arguments:
     *     Material expMate:                 'Materials/Main/TS1/20181014_131314/CURA_Homepage' created by TSuiteResult with 'product' profile
     *     Material actMate:                 'Materials/Main/TS1/20181014_131315/CURA_Homepage' created by TSuiteResult with 'develop' profile
     *     ImageDifference:                  6.71
     *     Double criteriaPercent:           3.0
     *
     * @return 'CURA_Homepage.20181014_131314_product-20181014_131315_develop.(6.71)FAILED.png'
     */
    String resolveImageDifferenceFilename(
            Material expMate,
            Material actMate,
            ImageDifference diff,
            double criteriaPercent) {
        
        // FIXME: the depencency to the "Reports" directory here makes this method fragile
        //        should parameterize those ExecutionProfiles  
        TSuiteResult tSuiteResultExpected = expMate.getParent().getParent()
        TSuiteResult tSuiteResultActual   = actMate.getParent().getParent()
        logger_.debug("#resolveImageDifferenceFilename tSuiteResultExpected=${tSuiteResultExpected}")
        logger_.debug("#resolveImageDifferenceFilename tSuiteResultActual  =${tSuiteResultActual}")
        
        ExecutionPropertiesWrapper epwExpected = tSuiteResultExpected.getExecutionPropertiesWrapper()
        ExecutionPropertiesWrapper epwActual   = tSuiteResultActual.getExecutionPropertiesWrapper()
        logger_.debug("#resolveImageDifferenceFilename epwExpected=${epwExpected}")
        logger_.debug("#resolveImageDifferenceFilename epwActual  =${epwActual}")
        
        ExecutionProfile profileExpected = (epwExpected != null) ? epwExpected.getExecutionProfile() : ExecutionProfileImpl.BLANK
        ExecutionProfile profileActual   = (epwActual   != null) ? epwActual.getExecutionProfile()   : ExecutionProfileImpl.BLANK
        logger_.debug("#resolveImageDifferenceFilename profileExpected=${profileExpected}")
        logger_.debug("#resolveImageDifferenceFilename profileActual  =${profileActual}")
        
        //
        String fileName = expMate.getPath().getFileName().toString()
        String fileId = fileName.substring(0, fileName.lastIndexOf('.'))
        String expTimestamp = expMate.getParent().getParent().getTSuiteTimestamp().format()
        String actTimestamp = actMate.getParent().getParent().getTSuiteTimestamp().format()
        //
        StringBuilder sb = new StringBuilder()
        sb.append("${fileId}.")
        sb.append("${expTimestamp}_${profileExpected}")
        sb.append("-")
        sb.append("${actTimestamp}_${profileActual}")
        sb.append(".")
        sb.append("(${diff.getRatioAsString()})")
        sb.append("${(diff.imagesAreSimilar(criteriaPercent)) ? '.png' : 'FAILED.png'}")
        return sb.toString()
    }
}
