package com.kazurayam.materials.imagedifference

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import org.apache.commons.io.FileUtils

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp

import spock.lang.Specification

class ImageDifferenceFilenameResolverDefaultImplSpec extends Specification {
    
    private static Path fixtureDir
    private static Path specOutputDir
    
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(ImageDifferenceFilenameResolverDefaultImplSpec.class.getName())
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def test_resolveImageDifferenceFilename() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_resolveImageDifferenceFilename")
        Path materials = caseOutputDir.resolve('Materials')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        when:
        boolean materialsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        boolean reportsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Reports'), reports)
        then:
        materialsCopyResult
        reportsCopyResult
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        TSuiteName tsn = new TSuiteName('main/TS1')
        List<MaterialPair> materialPairList = mr.createMaterialPairs(tsn)
        then:
        materialPairList.size() > 0
        when:
        TSuiteTimestamp tstExp = TSuiteTimestamp.newInstance('20181014_060500')
        TSuiteTimestamp tstAct = TSuiteTimestamp.newInstance('20181014_060501')
        Path relativePath = Paths.get('Main.Basic/CURA_Homepage.png')
        Material expMate = null
        Material actMate = null
        for (MaterialPair pair: materialPairList) {
            if (pair.getLeft().getPathRelativeToTSuiteTimestamp() == relativePath) {
                expMate = pair.getLeft()
                actMate = pair.getRight()
            }
        }
        then:
        expMate != null
        actMate != null
        when:
        BufferedImage expBI = ImageIO.read(expMate.getPath().toFile())
        BufferedImage actBI = ImageIO.read(actMate.getPath().toFile())
        ImageDifference diff = new ImageDifference(expBI, actBI)
        then:
        expBI != null
        actBI != null
        diff != null
        when:
        // verify the case of criteriaPercent is less than the actual diff magnitude -> file name should be FAILED.png
        double criteriaPercentF = 5.0
        ImageDifferenceFilenameResolverDefaultImpl instanceF = new ImageDifferenceFilenameResolverDefaultImpl()
        String fileNameF = instanceF.resolveImageDifferenceFilename(expMate, actMate, diff, criteriaPercentF)
        then:
        fileNameF == "CURA_Homepage.20181014_060500_product-20181014_060501_develop.(6.72)FAILED.png"
        when:
        // verify the case of criteriaPercent is less than the actual diff magnitude -> file name should be FAILED.png
        Double criteriaPercentP = 10.0
        ImageDifferenceFilenameResolverDefaultImpl instanceP = new ImageDifferenceFilenameResolverDefaultImpl()
        String fileNameP = instanceP.resolveImageDifferenceFilename(expMate, actMate, diff, criteriaPercentP)
        then:
        fileNameP == "CURA_Homepage.20181014_060500_product-20181014_060501_develop.(6.72).png"
        
    }
    
    /*
    def test_resolveImageDifferenceFilename_whenPASSED() {
        expect:
        false
    }
    */
}
