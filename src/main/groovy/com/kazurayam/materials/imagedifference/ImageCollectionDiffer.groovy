package com.kazurayam.materials.imagedifference

import java.nio.file.Files
import java.nio.file.Path

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.MaterialCoreImpl
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.stats.ImageDeltaStats

import groovy.json.JsonOutput

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
    
    private ComparisonResultBundle bundle_

    private Path output_
    
    
    
    /**
     * constructor
     *
     * @param mr
     * @author kazurayam
     */
    ImageCollectionDiffer(MaterialRepository mr) {
        this.mr_ = mr
        this.filenameResolver_ = new ImageDifferenceFilenameResolverDefaultImpl()
        this.vtLogger_ = new VisualTestingLoggerDefaultImpl()
        this.bundle_ = new ComparisonResultBundle()
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
    void setVisualTestingLogger(VisualTestingLogger logger) {
        this.vtLogger_ = logger
    }

    /**
     * 
     * @param materialPairs
     * @param tCaseName
     * @param imageDeltaStats
     */
    @Override
    boolean chronos(List<MaterialPair> materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats) {
        Objects.requireNonNull(materialPairs, "materialPairs must not be null")
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        Objects.requireNonNull(imageDeltaStats, "imageDeltaStats must not be null")
        //
        this.startImageCollection(tCaseName)
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {
            // resolve the criteria percentage for this Material
            Material expected = pair.getExpected()
            TSuiteName tsn = expected.getParent().getParent().getTSuiteName()
            Path path = expected.getPathRelativeToTSuiteTimestamp()
            double criteriaPercentage = imageDeltaStats.getCriteriaPercentage(tsn, path)
            // make an ImageDifference object and store it into file
            ComparisonResult evalResult = this.startMaterialPair(tCaseName, pair.getExpected(), pair.getActual(), criteriaPercentage)
            this.endMaterialPair(evalResult)
        }
        this.endImageCollection(tCaseName)
        
        return bundle_.allOfImagesAreSimilar()
    }
    
    /**
     * alias to chronos(MaterialPair, TCaseName, ImageDeltaStats)
     * 
     * @param materialPairs
     * @param tCaseName
     * @param imageDeltaStats
     */
    boolean makeImageCollectionDifferences(
            List<MaterialPair> materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats) {
        return this.chronos(materialPairs, tCaseName, imageDeltaStats)
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
    boolean twins(List<MaterialPair> materialPairs, TCaseName tCaseName, double criteriaPercentage) {
        Objects.requireNonNull(materialPairs, "materialPairs must not be null")
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        //
        this.startImageCollection(tCaseName)
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {
            // compare 2 images, make an diff image, store it into file, record and return the comparison result
            ComparisonResult evalResult = this.startMaterialPair(tCaseName, pair.getExpected(), pair.getActual(), criteriaPercentage)
            // logging etc
            this.endMaterialPair(evalResult)
        }
        this.endImageCollection(tCaseName)
        
        return bundle_.allOfImagesAreSimilar()
    }

    /**
     * alias to twins(MaterialPair, TCaseName, double)
     * 
     * @param materialPairs
     * @param tCaseName
     * @param criteriaPercent
     */
    boolean makeImageCollectionDifferences(
            List<MaterialPair> materialPairs, TCaseName tCaseName, double criteriaPercentage) {
        return this.twins(materialPairs, tCaseName, criteriaPercentage)
    }
    
    
    
    
    
    
    // -------- implementation of ImageCollectionProcessingContentHandler -----
    
    /**
     * serialize the list of EvaluationResult objects into file.
     */
    @Override
    void endImageCollection(TCaseName tCaseName) throws ImageDifferenceException {
        this.output_.text = JsonOutput.prettyPrint(this.bundle_.toJsonText())
    }
    
    /**
     * 
     */
    @Override
    void endMaterialPair(ComparisonResult cr) throws ImageDifferenceException {
        
        // memorize all of the EvaluationResult objects.
        // EvaluationResult object is just a struct of references and has no large byte array. Its size is small.
        this.bundle_.addComparisonResult(cr)
        
        // verify the diffRatio, fail the test if the ratio is greater than criteria
        if (this.vtLogger_ != null && ! cr.imagesAreSimilar()) {
            StringBuilder sb = new StringBuilder()
            sb.append(">>> diffRatio(${cr.getDiffRatio()}) > criteria(${cr.getCriteriaPercentage()}) ")
            sb.append("expected(${cr.getExpectedMaterial().getPathRelativeToRepositoryRoot()}) ")
            sb.append("actual(${cr.getActualMaterial().getPathRelativeToRepositoryRoot()})")
            this.vtLogger_.failed(sb.toString())
        }
    }
    
    /**
     * prepare File of 'comparison-result.json'
     */
    @Override
    void startImageCollection(TCaseName tCaseName) throws ImageDifferenceException {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        this.output_ = this.mr_.resolveMaterialPath(tCaseName, ComparisonResultBundle.SERIALIZED_FILE_NAME)
        Files.createDirectories(this.output_.getParent())
    }

    /**
     * This method is the core part of ImageCollectionDiffer.
     */
    @Override
    ComparisonResult startMaterialPair( TCaseName tCaseName,
                                        Material expectedMaterial,
                                        Material actualMaterial,
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
        MaterialCoreImpl diffMaterial = new MaterialCoreImpl(mr_.getBaseDir(), pngFile)
        // construct a record of image comparison
        ComparisonResult evalResult = new ComparisonResult( expectedMaterial,
                                                            actualMaterial,
                                                            diffMaterial,
                                                            criteriaPercentage,
                                                            diff.imagesAreSimilar(criteriaPercentage),
                                                            diff.getRatio()
                                                            )
        return evalResult
    }

}
