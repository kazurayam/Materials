package com.kazurayam.materials.imagedifference

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.apache.commons.io.FileUtils

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
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
    
    def test_deserializeToJsonObject() {
        when:
            Path caseOutputDir = specOutputDir.resolve("test_deserializeToJsonObject")
            String jsonText = makeJsonText(caseOutputDir)
            def obj = ComparisonResultBundle.deserializeToJsonObject(jsonText)
        then:
            obj.size() == 1
            comparePaths(obj.ComparisonResultBundle[0].ComparisonResult.expectedMaterial.Material.path,
                 "build\\tmp\\testOutput\\ComparisonResultBundleSpec\\test_deserializeToJsonObject\\Materials\\47News_chronos_capture\\20190216_064354\\main.TC_47News.visitSite\\47NEWS_TOP.png")
            comparePaths(obj.ComparisonResultBundle[0].ComparisonResult.actualMaterial.Material.path,
                "build\\tmp\\testOutput\\ComparisonResultBundleSpec\\test_deserializeToJsonObject\\Materials\\47News_chronos_capture\\20190216_204329\\main.TC_47News.visitSite\\47NEWS_TOP.png")
            comparePaths(obj.ComparisonResultBundle[0].ComparisonResult.diff,
                "build\\tmp\\testOutput\\ComparisonResultBundleSpec\\test_deserializeToJsonObject\\Materials\\ImageDiff\\20190216_210203\\ImageDiff\\main.TC_47News.visitSite\\47NEWS_TOP.20190216_064354_-20190216_204329_.(16.86).png")
            obj.ComparisonResultBundle[0].ComparisonResult.criteriaPercentage    > 30.0
            obj.ComparisonResultBundle[0].ComparisonResult.imagesAreSimilar      == true
            obj.ComparisonResultBundle[0].ComparisonResult.diffRatio             == 16.86
    }
    
    /*
    def test_deserialize() {
        when:
            Path caseOutputDir = specOutputDir.resolve("test_deserialize")
            String jsontText = makeJsonText(caseOutputDir)
            ComparisonResultBundle bundle = ComparisonResultBundle.deserialize(jsonText)
        then:
            bundle.size() == 1
        when:
            ComparisonResult cr = bundle.get(0)
            Path imageDiffPath = cr.getDiff()
        then:
            bundle.containsImageDiff(imageDiffPath)
        when:
            ComparisonResult cr2 = bundle.get(imageDiffPath)
        then:
            cr2 != null
        when:
            String srcExpected = bundle.srcOfExpectedMaterial(imageDiffPath)
        then:
            srcExpected == "foo"
        when:
            String srcActual = bundle.srcOfActualMaterial(imageDiffPath)
        then:
            srcActual == "bar"
    }
     */
    
    String makeJsonText(Path caseOutputDir) {
        //setup:
            Path materials = caseOutputDir.resolve('Materials')
            Path storage = caseOutputDir.resolve('Storage')
            Files.createDirectories(materials)
            FileUtils.deleteQuietly(materials.toFile())
            assert Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
            MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
            //
            TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
            TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
            Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
            //
            TSuiteName tsn = new TSuiteName('47News_chronos_capture')
            ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190216_204329')))
            ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190216_064354')))
            mr.scan()
            mr.putCurrentTestSuite('Test Suites/ImageDiff', '20190216_210203')
        //when:
            // we use Java 8 Stream API to filter entries
            List<MaterialPair> materialPairs =
                mr.createMaterialPairs(tsn).stream().filter { mp ->
                    mp.getLeft().getFileType() == FileType.PNG
                }.collect(Collectors.toList())
            StorageScanner.Options options = new StorageScanner.Options.Builder().
                previousImageDeltaStats(previousIDS).
                shiftCriteriaPercentageBy(15.0).       // THIS IS THE POINT
                build()
            StorageScanner storageScanner = new StorageScanner(ms, options)
            ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn)
            //
            storageScanner.persist(imageDeltaStats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
            double ccp = imageDeltaStats.getCriteriaPercentage(
                            new TSuiteName("47News_chronos_capture"),
                            Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png'))
        //then:
            assert 30.0 < ccp && ccp < 31.0 // ccp == 30.197159598135954
        //when:
            ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
            icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/ImageDiff'),
                imageDeltaStats)
            mr.scan()
            List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(new TSuiteName('Test Suites/ImageDiff'))
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
    
    boolean comparePaths(String path1, String path2) {
        return path1.replace('\\', '/').equals(path2.replace('\\','/'))
    }
}
