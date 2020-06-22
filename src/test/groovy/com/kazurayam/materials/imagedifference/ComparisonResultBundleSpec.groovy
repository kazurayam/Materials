package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.TExecutionProfile

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPairs
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.TSuiteResultIdImpl
import com.kazurayam.materials.stats.ImageDeltaStats
import com.kazurayam.materials.stats.StorageScanner

import spock.lang.Specification

class ComparisonResultBundleSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(ComparisonResultBundleSpec.class)
    
    private static Path fixtureDir
    private static Path specOutputDir

    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(ComparisonResultBundleSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    

    def test_constructor_withJson() {
        when:
            Path caseOutputDir = specOutputDir.resolve("test_constructor_withJson")
            Path materials = caseOutputDir.resolve('Materials')
            String jsonText = makeJsonText(caseOutputDir)
        then:
            jsonText != null
        when:
            //logger_.debug("#test_constructor_withJson jsonText=${jsonText}")
            ComparisonResultBundle bundle = new ComparisonResultBundle(materials, jsonText)
        then:
            bundle.size() == 1
        when:
            ComparisonResult cr = bundle.get(0)
        then:
            comparePaths(
                cr.getExpectedMaterial().getBaseDir(),
                Paths.get('build/tmp/testOutput/ComparisonResultBundleSpec/test_constructor_withJson/Materials')
                )
            comparePaths(
                cr.getExpectedMaterial().getPath(),
                Paths.get('build/tmp/testOutput/ComparisonResultBundleSpec/test_constructor_withJson/Materials/47News_chronos_capture/default/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png')
                )
            comparePaths(
                cr.getExpectedMaterial().getPathRelativeToRepositoryRoot(),
                Paths.get('47News_chronos_capture/default/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png')
                )
            comparePaths(
                cr.getActualMaterial().getBaseDir(),
                Paths.get('build/tmp/testOutput/ComparisonResultBundleSpec/test_constructor_withJson/Materials')
                )
            comparePaths(
                cr.getActualMaterial().getPath(),
                Paths.get('build/tmp/testOutput/ComparisonResultBundleSpec/test_constructor_withJson/Materials/47News_chronos_capture/default/20190216_204329/main.TC_47News.visitSite/47NEWS_TOP.png')
                )
            comparePaths(
                cr.getActualMaterial().getPathRelativeToRepositoryRoot(),
                Paths.get('47News_chronos_capture/default/20190216_204329/main.TC_47News.visitSite/47NEWS_TOP.png')
                )
            comparePaths(
                cr.getDiffMaterial().getBaseDir(),
                Paths.get('build/tmp/testOutput/ComparisonResultBundleSpec/test_constructor_withJson/Materials')
            )
            comparePaths(
                cr.getDiffMaterial().getPath(),
                Paths.get('build/tmp/testOutput/ComparisonResultBundleSpec/test_constructor_withJson/Materials/ImageDiff/default/20190216_210203/ImageDiff/main.TC_47News.visitSite/47NEWS_TOP(16.86).png')
            )                
            comparePaths(
                cr.getDiffMaterial().getPathRelativeToRepositoryRoot(),
                Paths.get('ImageDiff/default/20190216_210203/ImageDiff/main.TC_47News.visitSite/47NEWS_TOP(16.86).png')
                )
            cr.imagesAreSimilar() == true
            cr.getDiffRatio()== 16.86
            cr.getCriteriaPercentage() > 30.0 && cr.getCriteriaPercentage() < 31.0
    }
    
    
    def test_allOfImagesAreSimilar() {
        when:
            Path caseOutputDir = specOutputDir.resolve("test_allOfImagesAreSimilar")
            Path materials = caseOutputDir.resolve('Materials')
            Path reports = caseOutputDir.resolve('Reports')
            Files.createDirectories(reports)
            String jsonText = makeJsonText(caseOutputDir)
        then:
            jsonText != null
        when:
            //logger_.debug("#test_constructor_withJson jsonText=${jsonText}")
            ComparisonResultBundle bundle = new ComparisonResultBundle(materials, jsonText)
        then:
            bundle.size() == 1
        when:
            int sizeDifferent = bundle.sizeOfDifferentComparisonResults()
        then:
            sizeDifferent == 0
        when:
            boolean allSimilar = bundle.allOfImagesAreSimilar()
        then:
            allSimilar == true
        
    }
    
    
    def test_getByDiffMaterial() {
        when:
            Path caseOutputDir = specOutputDir.resolve("test_getByDiffMaterial")
            Path materials = caseOutputDir.resolve('Materials')
            String jsonText = makeJsonText(caseOutputDir)
        then:
            jsonText != null
        when:
            ComparisonResultBundle bundle = new ComparisonResultBundle(materials, jsonText)
        then:
            bundle.size()== 1
        when:
            ComparisonResult cr = bundle.getByDiffMaterial("ImageDiff/default/20190216_210203/ImageDiff/main.TC_47News.visitSite/47NEWS_TOP(16.86).png")
        then:
            cr != null
            cr.getExpectedMaterial().getHrefRelativeToRepositoryRoot() == '47News_chronos_capture/default/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png'
            cr.getActualMaterial().getHrefRelativeToRepositoryRoot()   == '47News_chronos_capture/default/20190216_204329/main.TC_47News.visitSite/47NEWS_TOP.png'
    }
    
    String makeJsonText(Path caseOutputDir) {
        //setup:
        Path materials = caseOutputDir.resolve('Materials')
        Path storage = caseOutputDir.resolve('Storage')
        Files.createDirectories(materials)
        Helpers.deleteDirectoryContents(materials)
        // copy fixtures from the src dir to the Storage dir
        Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
        //
        TSuiteName examiningTSuiteName = new TSuiteName("47News_chronos_exam")
        TExecutionProfile examiningTExecutionProfile = new TExecutionProfile("default")
        TCaseName  examiningTCaseName  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                examiningTSuiteName,
                examiningTExecutionProfile,
                examiningTCaseName)
        // copy fixtures from the Stroage dir to the Materials dir
        TSuiteName tsn = new TSuiteName('47News_chronos_capture')
        TExecutionProfile tep = new TExecutionProfile('default')
        ms.restore(mr, new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190216_204329')))
        ms.restore(mr, new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190216_064354')))
        mr.scan()
        mr.markAsCurrent(    'Test Suites/ImageDiff', 'default','20190216_210203')
        mr.ensureTSuiteResultPresent('Test Suites/ImageDiff', 'default', '20190216_210203')
            
        //when:
        // we use Java 8 Stream API to filter entries
        MaterialPairs materialPairs = mr.createMaterialPairsForChronosMode(tsn, tep)
        StorageScanner.Options options = new StorageScanner.Options.Builder()
                .previousImageDeltaStats(previousIDS)
                .shiftCriteriaPercentageBy(15.0)
                .build()
        StorageScanner storageScanner = new StorageScanner(ms, options)
        ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn, tep)
        //
        storageScanner.persist(imageDeltaStats,
                examiningTSuiteName,
                examiningTExecutionProfile,
                new TSuiteTimestamp(),
                examiningTCaseName)
        double ccp = imageDeltaStats.getCriteriaPercentage(
                            Paths.get('main.TC_47News.visitSite')
                                    .resolve('47NEWS_TOP.png'))
        //then:
        assert 30.0 < ccp && ccp < 31.0 // ccp == 30.197159598135954
        //when:
        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/ImageDiff'),
                imageDeltaStats)
        mr.scan()
        List<TSuiteResultId> tsriList =
                mr.getTSuiteResultIdList(
                        new TSuiteName('Test Suites/ImageDiff'),
                        new TExecutionProfile('default'))
        assert tsriList.size() == 1
        TSuiteResultId tsri = tsriList.get(0)
        TSuiteResult tsr = mr.getTSuiteResult(tsri)
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/ImageDiff"))
        List<Material> mateList = tcr.getMaterialList()
        assert mateList.size() == 2          // diffImage + comparison-result-bundle.json
            
        // now we can read the comparison-result-bundle.json
        Material bundleJson = tcr.getMaterialList('json$', true).get(0)
        String jsonText = bundleJson.getPath().toFile().text
        println "jsonText=${jsonText}"
        return jsonText
    }
    
    boolean comparePaths(Path path1, Path path2) {
        Path p1 = path1.normalize()
        Path p2 = path2.normalize()
        return p1.toString().replace('\\', '/').equals(p2.toString().replace('\\','/'))
    }
    
    
}
