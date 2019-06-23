package com.kazurayam.materials.imagedifference

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import javax.imageio.ImageIO

import org.apache.commons.io.FileUtils

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.ReportsAccessorFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.impl.MaterialCoreImpl

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
            mr.markAsCurrent(    'Test Suites/ImageDiff', '20181014_060501')
            def tsr = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff', '20181014_060501')
            List<MaterialPair> materialPairs =
            // we use Java 8 Stream API to filter entries
            mr.createMaterialPairs(new TSuiteName('Test Suites/main/TS1')).getList().stream().filter { mp ->
                    mp.getLeft().getFileType() == FileType.PNG
                }.collect(Collectors.toList())
            Material expected = materialPairs.get(0).getExpected()
            Material actual = materialPairs.get(0).getActual()
            ImageDifference diff = new ImageDifference(
                ImageIO.read(expected.getPath().toFile()),
                ImageIO.read(actual.getPath().toFile())
                )
            double criteriaPercentage = 5.0
            ReportsAccessor reportsAccessor = ReportsAccessorFactory.createInstance(reports)
            ImageDifferenceFilenameResolver filenameResolver = new ImageDifferenceFilenameResolverDefaultImpl(reportsAccessor)
            String fileName = filenameResolver.resolveImageDifferenceFilename(
                expected,
                actual,
                diff,
                criteriaPercentage)
            Path diffFile = mr.resolveMaterialPath(
                new TCaseName("imageDiff"),
                expected.getParentDirectoryPathRelativeToTSuiteResult().toString(),
                fileName)
            MaterialCore diffMaterial = new MaterialCoreImpl(mr.getBaseDir(), diffFile)
            boolean imagesAreSimilar = diff.imagesAreSimilar(criteriaPercentage)
            double diffRatio = 3.56
            ComparisonResult result = new ComparisonResult(
                                                (MaterialCore)expected,
                                                (MaterialCore)actual,
                                                diffMaterial,
                                                criteriaPercentage,
                                                imagesAreSimilar,
                                                diffRatio)
        then:
            result != null
            result.getExpectedMaterial().equals(expected)
            result.getActualMaterial().equals(actual)
            result.getDiffMaterial().toString().contains('imageDiff')
            result.getCriteriaPercentage() == criteriaPercentage
            result.imagesAreSimilar() == true
            result.getDiffRatio() == 3.56
        when:
            String jsonText = result.toJsonText()
            println "#testSmoke jsonText=${jsonText}"
            String pretty = JsonOutput.prettyPrint(jsonText)
            println "#testSmoke pretty=${pretty}"
        then:
            pretty != null
            pretty.contains('ComparisonResult')
            pretty.contains('expectedMaterial')
            pretty.contains('actualMaterial')
            pretty.contains('criteriaPercentage')
            pretty.contains('diffRatio')
            pretty.contains('imagesAreSimilar')
        when:
            JsonSlurper slurper = new JsonSlurper()
            def deserialized = slurper.parseText(jsonText)
        then:
            deserialized != null
    }

}

