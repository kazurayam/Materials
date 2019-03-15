package com.kazurayam.materials.imagedifference

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import javax.imageio.ImageIO

import org.apache.commons.io.FileUtils

import com.kazurayam.materials.imagedifference.ImageDifference
import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class ComparisonResultSpec extends Specification {
    
    private static Path fixtureDir
    private static Path specOutputDir
    
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(ComparisonResultSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    def testSmoke() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testSmoke")
        Path materials = caseOutputDir.resolve('Materials')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        Helpers.copyDirectory(fixtureDir.resolve('Reports'), reports)
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.putCurrentTestSuite('Test Suites/ImageDiff', '20181014_060501')
        List<MaterialPair> materialPairs =
        // we use Java 8 Stream API to filter entries
        mr.createMaterialPairs(new TSuiteName('Test Suites/main/TS1')).stream().filter { mp ->
                mp.getLeft().getFileType() == FileType.PNG
            }.collect(Collectors.toList())
        Material expected = materialPairs.get(0).getExpected()
        Material actual = materialPairs.get(0).getActual()
        ImageDifference diff = new ImageDifference(
            ImageIO.read(expected.getPath().toFile()),
            ImageIO.read(actual.getPath().toFile())
            )
        double criteriaPercentage = 5.0    
        ImageDifferenceFilenameResolver filenameResolver = new ImageDifferenceFilenameResolverDefaultImpl()
        String fileName = filenameResolver.resolveImageDifferenceFilename(
            expected,
            actual,
            diff,
            criteriaPercentage)
        Path diffFile = mr.resolveMaterialPath(
            new TCaseName("imageDiff"),
            expected.getDirpathRelativeToTSuiteResult(),
            fileName)
        boolean imagesAreSimilar = diff.imagesAreSimilar(criteriaPercentage)
        double diffRatio = 3.56
        ComparisonResult result = new ComparisonResult(expected, actual, criteriaPercentage, imagesAreSimilar, diffRatio , diffFile)
        then:
        result != null
        result.getExpectedMaterial().equals(expected)
        result.getActualMaterial().equals(actual)
        result.getCriteriaPercentage() == criteriaPercentage
        result.imagesAreSimilar() == true
        result.getDiffRatio() == 3.56
        result.getDiff().toString().contains('imageDiff')
        when:
        String jsonText = result.toJsonText()
        String pretty = JsonOutput.prettyPrint(jsonText)
        println pretty
        then:
        pretty != null
        pretty.contains('ComparisonResult')
        when:
        JsonSlurper slurper = new JsonSlurper()
        def deserialized = slurper.parseText(jsonText)
        then:
        deserialized != null
    }
}

