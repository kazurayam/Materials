package com.kazurayam.materials.imagedifference

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.kazurayam.materials.Material

class ImageDifferenceFilenameResolverCompactImpl implements ImageDifferenceFilenameResolver {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDifferenceFilenameResolverCompactImpl.class)
    
    /**
     * Given with the following arguments:
     *     Material expMate:                 'Materials/Main/TS1/20181014_131314/CURA_Homepage' created by TSuiteResult with 'product' profile
     *     Material actMate:                 'Materials/Main/TS1/20181014_131315/CURA_Homepage' created by TSuiteResult with 'develop' profile
     *     ImageDifference:                  6.71
     *     Double criteriaPercent:           3.0
     *
     * @return 'CURA_Homepage(6.71)FAILED.png'
     */
    String resolveImageDifferenceFilename(
            Material expMate,
            Material actMate,
            ImageDifference diff,
            double criteriaPercent) {

        //
        String fileName = expMate.getPath().getFileName().toString()
        String fileId = fileName.substring(0, fileName.lastIndexOf('.'))
        //
        StringBuilder sb = new StringBuilder()
        sb.append("${fileId}")
        sb.append("(${diff.getRatioAsString()})")
        sb.append("${(diff.imagesAreSimilar(criteriaPercent)) ? '.png' : 'FAILED.png'}")
        return sb.toString()
    }

}
