package com.kazurayam.materials.imagedifference

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialPairs
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.MaterialCoreImpl
import com.kazurayam.materials.impl.MaterialImpl
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
        this.filenameResolver_ = new ImageDifferenceFilenameResolverCompactImpl()
        this.vtLogger_ = new VisualTestingLoggerDefaultImpl()
        this.bundle_ = new ComparisonResultBundle()
        this.output_ = null
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
    boolean makeImageCollectionDifferences(MaterialPairs materialPairs, TCaseName tCaseName, ImageDeltaStats imageDeltaStats) {
        Objects.requireNonNull(materialPairs, "materialPairs must not be null")
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        Objects.requireNonNull(imageDeltaStats, "imageDeltaStats must not be null")
        //
        this.startImageCollection(tCaseName)
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs.getList()) {
            // make an orphan material paired
            MaterialPair decorated = decorateMaterialPair(
                                        pair, 
                                        materialPairs.getExpectedTSuiteResult(),
                                        materialPairs.getActualTSuiteResult())
            
            // resolve the criteria percentage for this Material
            if (decorated.hasExpected() && decorated.hasActual() &&
                decorated.getExpected().getFileType() == FileType.PNG &&
                decorated.getActual().getFileType() == FileType.PNG) {
                Material expected = decorated.getExpected()
                TSuiteName tsn = expected.getParent().getParent().getTSuiteName()
                Path path = expected.getPathRelativeToTSuiteTimestamp()
                double criteriaPercentage = imageDeltaStats.getCriteriaPercentage(tsn, path)
                // compare 2 images and create a ComparisonResult object
                ComparisonResult cr = this.startMaterialPair(tCaseName, decorated, criteriaPercentage)
                // and put the ComparisonResult into buffer
                this.endMaterialPair(cr)
            }
        }
        // 
        this.endImageCollection(tCaseName)
        //
        return bundle_.allOfImagesAreSimilar()
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
    boolean makeImageCollectionDifferences(MaterialPairs materialPairs, TCaseName tCaseName, double criteriaPercentage) {
        Objects.requireNonNull(materialPairs, "materialPairs must not be null")
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        //
        this.startImageCollection(tCaseName)
        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs.getList()) {
            // make an orphan material paired
            MaterialPair decorated = decorateMaterialPair(
                                        pair, 
                                        materialPairs.getExpectedTSuiteResult(),
                                        materialPairs.getActualTSuiteResult())
            
            if (decorated.hasExpected() && decorated.hasActual() &&
                decorated.getExpected().getFileType() == FileType.PNG &&
                decorated.getActual().getFileType() == FileType.PNG) {
                // compare 2 images, make an diff image, store it into file, record and return the comparison result
                ComparisonResult evalResult = this.startMaterialPair(tCaseName, decorated, criteriaPercentage)
                // logging etc
                this.endMaterialPair(evalResult)
            }
        }
        this.endImageCollection(tCaseName)
        //
        return bundle_.allOfImagesAreSimilar()
    }
    
    /**
     * 
     * @return the Path of comparison-result-bundle.json file created by endImageCollection() method call.
     */
    Path getOutput() {
		return this.output_
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
                                        MaterialPair materialPair,
                                        double criteriaPercentage) throws ImageDifferenceException {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        Objects.requireNonNull(materialPair, "materialPair must not be null")

        Material expectedMaterial = materialPair.getExpected()
        Material actualMaterial = materialPair.getActual()
        
        BufferedImage expectedBI
        if (expectedMaterial.fileExists()) {
            expectedBI = ImageIO.read(expectedMaterial.getPath().toFile())
        } else {
            // generate a marker image and save it
            expectedBI = Helpers.convertTextToImage("file ${expectedMaterial.getPath()} is not found")
            Files.createDirectories(expectedMaterial.getPath().getParent())
            ImageIO.write(expectedBI, "PNG", expectedMaterial.getPath().toFile())
        }
        
        BufferedImage actualBI
        if (actualMaterial.fileExists()) {
            actualBI = ImageIO.read(actualMaterial.getPath().toFile())
        } else {
            // generate a marker image and save it
            actualBI = Helpers.convertTextToImage("file ${actualMaterial.getPath()} is not found")
            Files.createDirectories(actualMaterial.getPath().getParent())
            ImageIO.write(actualBI, "PNG", actualMaterial.getPath().toFile())
        }
        
        // create ImageDifference of the 2 given images
        ImageDifference diff = new ImageDifference(expectedBI, actualBI)
        
        // resolve the name of output file to save the ImageDiff
        String fileName = this.filenameResolver_.resolveImageDifferenceFilename(
                                        expectedMaterial,
                                        actualMaterial,
                                        diff,
                                        criteriaPercentage)

        // resolve the path of output file to save the ImageDiff
        Path pngFile = this.mr_.resolveMaterialPath(
                            tCaseName,
                            expectedMaterial.getParentDirectoryPathRelativeToTSuiteResult().toString(),
                            fileName)

        // write the ImageDiff into the output file
        ImageIO.write(diff.getDiffImage(), "PNG", pngFile.toFile())
        MaterialCoreImpl diffMaterial = new MaterialCoreImpl(mr_.getBaseDir(), pngFile)
        // construct a record of image comparison
        boolean similarity = diff.imagesAreSimilar(criteriaPercentage)
        ComparisonResult evalResult = new ComparisonResult( expectedMaterial,
                                                            actualMaterial,
                                                            diffMaterial,
                                                            criteriaPercentage,
                                                            similarity,
                                                            diff.getRatio()
                                                            )
        if (vtLogger_ != null) {
            String eval = (similarity) ? 'Similar' : 'Different'
            vtLogger_.info("${eval} ${diffMaterial.getPathRelativeToRepositoryRoot().toString()} ")
        }

        return evalResult
    }
    
    /**
     * Check the source MaterialPair has an orphan, which means 
     * if it lacks the Expected Material or it lacks the Actual Material.
     * Fill the vacancy with a Material object which returns fileExists()==false
     * 
     * @param source
     * @return
     */
    static MaterialPair decorateMaterialPair(MaterialPair source, TSuiteResult expectedTSR, TSuiteResult actualTSR) {
        
        Objects.requireNonNull(source, "source must not be null")
        Objects.requireNonNull(expectedTSR, "expectedTSR must not be null")
        Objects.requireNonNull(actualTSR, "actualTSR must not be null")
        
        if (!source.hasExpected() && !source.hasActual()) {
            throw new IllegalStateException("!source.hasExpected() && !source.hasActual()")
        }
        MaterialPair clone = source.clone()
        if (!source.hasExpected()) {
            // make a fake Material object as the Expected one
            clone.setExpected(fakeMaterial(expectedTSR, source.getActual()))
        }
        if (!source.hasActual()) {
            // make a fake Material object as the Actuail one
            clone.setActual(fakeMaterial(actualTSR, source.getExpected()))
        }
        return clone
    }

    /**
     * @param targetTSuiteResult e.g, './Materials/main.TS1/20180530_130419/'
     * @param existingMaterial   e.g, './Materials/main.TS1/20180530_150000/main.TC1/foo/bar/fixture.xls'
     * @return fakeMaterial with path './Materials/main.TS1/20180530_130419/main.TC1/foo/bar/fixture.xls'
     */
    static fakeMaterial(TSuiteResult targetTSuiteResult, Material existingMaterial) {
        TCaseName tcn = existingMaterial.getParent().getTCaseName()
        TCaseResult fakeTCR = TCaseResult.newInstance(tcn)
        fakeTCR.setParent(targetTSuiteResult)
        //
        Path fakePath = targetTSuiteResult.getTSuiteTimestampDirectory()
                            .resolve(existingMaterial.getPathRelativeToTSuiteTimestamp())
        //
        Material result = new MaterialImpl(fakeTCR, fakePath)
        return result
    }

}
