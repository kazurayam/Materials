package com.kazurayam.materials.imagedifference

import java.nio.file.Path

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName

/**
 * This class is designed to implement the "Visual Testing in Katalon Studio" feature.
 *
 * This class uses the following 2 external libraries:
 * 1. AShot (https://github.com/yandex-qatools/ashot)
 * 2. Materials (https://github.com/kazurayam/Materials)
 *
 * The makeImageCollectionDifferences() method provides the core value of this class.
 * This method accepts Materials (image files) to compare them, make differences, and
 * store the diff-images into files.
 *
 * @author kazurayam
 */
class ImageCollectionDiffer {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageCollectionDiffer.class)

    private MaterialRepository mr_
    private ImageDifferenceFilenameResolver idfResolver_
    private VisualTestingListener listener_ = new VisualTestingListenerDefaultImpl()
    

    /**
     * constructor
     *
     * @param mr
     * @author kazurayam
     */
    ImageCollectionDiffer(MaterialRepository mr) {
        mr_ = mr
        idfResolver_ = new ImageDifferenceFilenameResolverDefaultImpl()
    }

    /*
     * Non-argument constructor is required to pass "Test Cases/Test/Prologue"
     * which calls `CustomKeywords."${className}.getClass"().getName()`
     */
    ImageCollectionDiffer() {}

    void setImageDifferenceFilenameResolver(ImageDifferenceFilenameResolver idfResolver) {
        idfResolver_ = idfResolver
    }

    /**
     * set instance of VisualTestingListener, which will consume messages from ImageCollectionDiffer
     * 
     * @param listener
     */
    void setVTListener(VisualTestingListener listener) {
        listener_ = listener
    }

    /**
     * 
     * @param materialPairs
     * @param tCaseName
     * @param imageDeltaStats
     */
    void makeImageCollectionDifferences(
            List<MaterialPair> materialPairs,
            TCaseName tCaseName,
            ImageDeltaStats imageDeltaStats) {
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {
            double criteriaPercentage = 0.0
            this.writeDiffImage(pair.getExpected(), pair.getActual(), tCaseName, criteriaPercentage)
        }
    }
        
    /**
     * compare 2 Material files in each MaterialPair object,
     * create ImageDiff and store the diff image files under the directory
     * 
     * ./Materials/<tSuiteName>/yyyyMMdd_hhmmss/<tCaseName>.
     * 
     * The difference ratio is compared with the criteriaPercent given.
     * Will be marked FAILED if any of the pairs has greater difference.
     *
     * @param materialPairs created by
     *     com.kazurayam.materials.MaterialRpository#createMaterialPairs() method
     * @param tCaseName the name of test case which called the ImageCollectionDiffer#makeImageCollectionDifferences()
     * @param criteriaPercent e.g. 3.00 percent. If the difference of
     *     a MaterialPair is greater than this,
     *     the MaterialPair is evaluated FAILED
     */
    void makeImageCollectionDifferences(
            List<MaterialPair> materialPairs,
            TCaseName tCaseName,
            double criteriaPercent) {
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {
            this.writeDiffImage(pair.getExpected(), pair.getActual(), tCaseName, criteriaPercent)
        }
    }
    
    
    /**
     * 
     * @param expMate
     * @param actMate
     * @param tCaseName
     * @param criteriaPercent
     * @return
     */
    private writeDiffImage(Material expMate, Material actMate, TCaseName tCaseName, double criteriaPercent) {
        // create ImageDifference of the 2 given images
        ImageDifference diff = new ImageDifference(
                ImageIO.read(expMate.getPath().toFile()),
                ImageIO.read(actMate.getPath().toFile()))

        // resolve the name of output file to save the ImageDiff
        String fileName = idfResolver_.resolveImageDifferenceFilename(
                expMate,
                actMate,
                diff,
                criteriaPercent)

        // resolve the path of output file to save the ImageDiff
        Path pngFile = mr_.resolveMaterialPath(
                tCaseName,
                expMate.getDirpathRelativeToTSuiteResult(),
                fileName)

        // write the ImageDiff into the output file
        ImageIO.write(diff.getDiffImage(), "PNG", pngFile.toFile())

        // verify the diffRatio, fail the test if the ratio is greater than criteria
        if (diff.getRatio() > criteriaPercent && listener_ != null) {
            listener_.failed(">>> diffRatio = ${diff.getRatio()} is exceeding criteria = ${criteriaPercent}")
        }
    }

}
