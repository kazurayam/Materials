package com.kazurayam.materials.imagedifference

import java.nio.file.Path

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.stats.ImageDeltaStats

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
final class ImageCollectionDiffer extends ImageCollectionProcessor {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageCollectionDiffer.class)

    private MaterialRepository mr_

    /**
     * constructor
     *
     * @param mr
     * @author kazurayam
     */
    ImageCollectionDiffer(MaterialRepository mr) {
        this.mr_ = mr
        this.errorHandler_ = new ImageDiffProcessingErrorHandler()
        this.filenameResolver_ = new ImageDifferenceFilenameResolverDefaultImpl()
        this.vtListener_ = new VisualTestingListenerDefaultImpl()
    }

    /*
     * Non-argument constructor is required to pass "Test Cases/Test/Prologue"
     * which calls `CustomKeywords."${className}.getClass"().getName()`
     */
    private ImageCollectionDiffer() {}

    void setImageDifferenceFilenameResolver(ImageDifferenceFilenameResolver filenameResolver) {
        this.filenameResolver_ = filenameResolver
    }

    /**
     * set instance of VisualTestingListener, which will consume messages from ImageCollectionDiffer
     * 
     * @param listener
     */
    void setVTListener(VisualTestingListener listener) {
        this.vtListener_ = listener
    }

    /**
     * 
     * @param materialPairs
     * @param tCaseName
     * @param imageDeltaStats
     */
    @Override
    void chronos(List<MaterialPair> materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats) {
        Objects.requireNonNull(this.errorHandler_, "this.errorHandler_ must not be null")
        Objects.requireNonNull(this.filenameResolver_, "this.filenameResolver_ must not be null")
        Objects.requireNonNull(this.vtListener_, "this.vtListener_ must not be null")
        this.startImageCollection(tCaseName)
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {
            // resolve the criteria percentage for this Material
            Material expected = pair.getExpected()
            TSuiteName tsn = expected.getParent().getParent().getTSuiteName()
            Path path = expected.getPathRelativeToTSuiteTimestamp()
            double criteriaPercentage = imageDeltaStats.getCriteriaPercentage(tsn, path)
            // make an ImageDifference object and store it into file
            ImageDifference imageDifference = this.startMaterialPair(tCaseName, pair.getExpected(), pair.getActual(), criteriaPercentage)
            this.endMaterialPair  (new ImageDifferenceEvaluation(imageDifference, criteriaPercentage))
        }
        this.endImageCollection(tCaseName)
    }
    
    /**
     * alias to chronos(MaterialPair, TCaseName, ImageDeltaStats)
     * 
     * @param materialPairs
     * @param tCaseName
     * @param imageDeltaStats
     */
    void makeImageCollectionDifferences(
            List<MaterialPair> materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats) {
        this.chronos(materialPairs, tCaseName, imageDeltaStats)
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
    @Override
    void twins(List<MaterialPair> materialPairs, TCaseName tCaseName, double criteriaPercentage) {
        Objects.requireNonNull(this.errorHandler_, "this.errorHandler_ must not be null")
        Objects.requireNonNull(this.filenameResolver_, "this.filenameResolver_ must not be null")
        Objects.requireNonNull(this.vtListener_, "this.vtListener_ must not be null")
        this.startImageCollection(tCaseName)
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {
            // make an ImageDifference object and store it into file
            ImageDifference imageDifference = this.startMaterialPair(tCaseName, pair.getExpected(), pair.getActual(), criteriaPercentage)
            // logging etc
            this.endMaterialPair  (new ImageDifferenceEvaluation(imageDifference, criteriaPercentage))
        }
        this.endImageCollection(tCaseName)
    }

    /**
     * alias to twins(MaterialPair, TCaseName, double)
     * 
     * @param materialPairs
     * @param tCaseName
     * @param criteriaPercent
     */
    void makeImageCollectionDifferences(
            List<MaterialPair> materialPairs, TCaseName tCaseName, double criteriaPercentage) {
        this.twins(materialPairs, tCaseName, criteriaPercentage)
    }
    
    @Override
    void endImageCollection(TCaseName tCaseName) throws ImageDifferenceException {
        //throw new UnsupportedOperationException("TODO")
    }

    @Override
    void endMaterialPair(ImageDifferenceEvaluation diffEvaluation) throws ImageDifferenceException {
        // verify the diffRatio, fail the test if the ratio is greater than criteria
        ImageDifference diff = diffEvaluation.getImageDifference()
        double criteriaPercentage = diffEvaluation.getCriteriaPercentage()
        if (diff.getRatio() > criteriaPercentage && this.vtListener_ != null) {
            this.vtListener_.failed(">>> diffRatio = ${diff.getRatio()} is exceeding criteria = ${criteriaPercentage}")
        }
    
    }
    
    @Override
    void startImageCollection(TCaseName tCaseName) throws ImageDifferenceException {
        //throw new UnsupportedOperationException("TODO")
    }

    @Override
    ImageDifference startMaterialPair(TCaseName tCaseName,
                Material expectedMaterial, Material actualMaterial,
                double criteriaPercentage) throws ImageDifferenceException {
        // create ImageDifference of the 2 given images
        ImageDifference diff = new ImageDifference(
                                    ImageIO.read(expectedMaterial.getPath().toFile()),
                                    ImageIO.read(actualMaterial.getPath().toFile()))
    
        // resolve the name of output file to save the ImageDiff
        String fileName = this.filenameResolver_.resolveImageDifferenceFilename(
                                        expectedMaterial,
                                        actualMaterial,
                                        diff,
                                        criteriaPercentage)
    
        // resolve the path of output file to save the ImageDiff
        Path pngFile = this.mr_.resolveMaterialPath(
                            tCaseName,
                            expectedMaterial.getDirpathRelativeToTSuiteResult(),
                            fileName)
    
        // write the ImageDiff into the output file
        ImageIO.write(diff.getDiffImage(), "PNG", pngFile.toFile())
    
        //
        diff.setStoredInto(pngFile)
        
        return diff
    }

            
    /**
     * 
     */
    static class ImageDiffProcessingErrorHandler implements ImageCollectionProcessingErrorHandler {
        
        @Override
        void error(ImageDifferenceException ex) {
            //throw new UnsupportedOperationException("TODO")
        }
        
        @Override
        void fatalError(ImageDifferenceException ex) {
            //throw new UnsupportedOperationException("TODO")
        }
        
        @Override
        void warning(ImageDifferenceException ex) {
            //throw new UnsupportedOperationException("TODO")
        }
        
        
    }

}
