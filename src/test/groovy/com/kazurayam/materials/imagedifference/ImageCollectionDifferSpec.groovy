package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.TExecutionProfile

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import org.apache.commons.io.FileUtils

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

class ImageCollectionDifferSpec extends Specification {

    private static Path fixtureDir
    private static Path specOutputDir

    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(ImageCollectionDifferSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * PNG file should end with "FAILED.png"
     */
    def test_makeImageCollectionDifferences_twins_shouldCreatePngWithFAILED() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_twins_shouldCreatePngWithFAILED")
        Path materials = caseOutputDir.resolve('Materials')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.markAsCurrent(    'Test Suites/ImageDiff',
                'default', '20181014_060501')
        def tsr = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff',
                'default', '20181014_060501')

        MaterialPairs materialPairs =
            mr.createMaterialPairsForTwinsMode(new TSuiteName('Test Suites/main/TS1'))

        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
            materialPairs,
            new TCaseName('Test Cases/ImageDiff'),
            5.0)          // specified value smaller than the actual diff ratio (6.72)
        //
        then:
        Files.exists(materials.resolve('ImageDiff/default/20181014_060501/ImageDiff/Main.Basic'))
        Files.exists(materials.resolve('ImageDiff/default/20181014_060501/ImageDiff/Main.Basic/' +
            'CURA_Homepage(6.72)FAILED.png'))
    }

    /**
     * PNG file should not end with "FAILED.png"
     */
    def test_makeImageCollectionDifferences_twins_shouldCreatePngWithoutFAILED() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_twins_shouldCreatePngWithoutFAILED")
        Path materials = caseOutputDir.resolve('Materials')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.markAsCurrent(    'Test Suites/ImageDiff',
                'default', '20181014_060501')
        def tsr = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff',
                'default', '20181014_060501')
        MaterialPairs materialPairs =
            mr.createMaterialPairsForTwinsMode(new TSuiteName('Test Suites/main/TS1'))

        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
            materialPairs,
            new TCaseName('Test Cases/ImageDiff'),
            10.0)          // specified value larger than the actual diff ratio (6.72)
        //
        then:
        Files.exists(materials.resolve('ImageDiff/default/20181014_060501/ImageDiff/Main.Basic'))
        Files.exists(materials.resolve('ImageDiff/default/20181014_060501/ImageDiff/Main.Basic/' +
            'CURA_Homepage(6.72).png'))   
            // here we expect the file to be (6.72).png, rather than (6.72)FAILED.png
    }

    /**
     * Run ImageCollectionDiffer#makeImageCollectionDifferences() with ImageDeletaStats object as an arugment.
     * In this case, the criteriaPercentage is calculated to be 15.19, and
     * the actual diffRation of a Material is 16.86, and is regarded as FAILED.
     * @return
     */
    def test_makeImageCollectionDifferences_chronos_smallerCriteriaPercentage() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_chronos_smallerCriteriaPercentage")
        Path materials = caseOutputDir.resolve('Materials')
        Path storage = caseOutputDir.resolve('Storage')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
        TSuiteName tsn = new TSuiteName('47News_chronos_capture')
        TExecutionProfile tep = new TExecutionProfile('default')
        ms.restore(mr, new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190216_204329')))
        ms.restore(mr, new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190216_064354')))
        mr.scan()
        mr.markAsCurrent(    'Test Suites/ImageDiff', 'default', '20190216_210203')
        def r = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff', 'default', '20190216_210203')

        when:
        // we use Java 8 Stream API to filter entries
        MaterialPairs materialPairs = mr.createMaterialPairsForChronosMode(tsn, tep)
        //
        TSuiteName examiningTSuiteName = new TSuiteName("47News_chronos_exam")
        TExecutionProfile examiningTExecutionProfile = new TExecutionProfile('default')
        TCaseName  examiningTCaseName  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                    examiningTSuiteName,
                    examiningTExecutionProfile,
                    examiningTCaseName)
        //
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                                previousImageDeltaStats(previousIDS).
                                                build()
        StorageScanner storageScanner = new StorageScanner(ms, options)
        ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn, tep)
        //
        storageScanner.persist(imageDeltaStats,
                examiningTSuiteName,
                examiningTExecutionProfile,
                new TSuiteTimestamp(),
                examiningTCaseName)
        //
        double ccp = imageDeltaStats.getCriteriaPercentage(
                            Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png'))
        then:
        15.0 < ccp && ccp < 16.0 // ccp == 15.197159598135954

        when:
        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/ImageDiff'),
                imageDeltaStats)
        mr.scan()
        then:
        icd.getOutput() != null

        when:
        List<TSuiteResultId> tsriList =
                mr.getTSuiteResultIdList(
                        new TSuiteName('Test Suites/ImageDiff'),
                        new TExecutionProfile('default'))
        assert tsriList.size() == 1
        TSuiteResultId tsri = tsriList.get(0)
        TSuiteResult tsr = mr.getTSuiteResult(tsri)
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/ImageDiff"))
        List<Material> mateList = tcr.getMaterialList()
        assert mateList.size() == 2                     // diffImage + ComparisonResult.json
        Material diffImage = tcr.getMaterialList('png$', true).get(0)
        then:
            diffImage.getPath().toString().endsWith('(16.86)FAILED.png')

        when:
        // assert that we have ComparisonResults.json
        List<Material> jsons = tcr.getMaterialList(ComparisonResultBundle.SERIALIZED_FILE_NAME)
        then:
        jsons.size() == 1
    }

    /**
     * Run ImageCollectionDiffer#makeImageCollectionDifferences() with ImageDeletaStats object as an arugment.
     * In this case, the criteriaPercentage is calculated to be 30.19, and
     * the actual diffRation of a Material is 16.86, and is regarded as FAILED.
     * @return
     */
    def test_makeImageCollectionDifferences_chronos_largerCriteriaPercentage() {
        setup:
            Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_chronos_largerCriteriaPercentage")
            Path materials = caseOutputDir.resolve('Materials')
            Path storage = caseOutputDir.resolve('Storage')
            Files.createDirectories(materials)
            FileUtils.deleteQuietly(materials.toFile())
            Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
            MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
            //
            TSuiteName examiningTSuiteName = new TSuiteName("47News_chronos_exam")
            TExecutionProfile examiningTExecutionProfile = new TExecutionProfile('default')
            TCaseName  examiningTCaseName  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
            Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                    examiningTSuiteName,
                    examiningTExecutionProfile,
                    examiningTCaseName)
            //
            TSuiteName tsn = new TSuiteName('47News_chronos_capture')
            TExecutionProfile tep = new TExecutionProfile('default')
            ms.restore(mr, new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190216_204329')))
            ms.restore(mr, new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190216_064354')))
            mr.scan()
            mr.markAsCurrent(    'Test Suites/ImageDiff',
                    'default', '20190216_210203')
            def r = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff',
                    'default', '20190216_210203')
        when:
            MaterialPairs materialPairs =
                mr.createMaterialPairsForChronosMode(tsn, tep)
            StorageScanner.Options options = new StorageScanner.Options.Builder().
                previousImageDeltaStats(previousIDS).
                shiftCriteriaPercentageBy(15.0).       // THIS IS THE POINT
                build()
            StorageScanner storageScanner = new StorageScanner(ms, options)
            ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn, tep)
            //
            storageScanner.persist(imageDeltaStats,
                    examiningTSuiteName,
                    examiningTExecutionProfile,
                    new TSuiteTimestamp(),
                    examiningTCaseName)
            double ccp = imageDeltaStats.getCriteriaPercentage(
                            Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png'))
        then:
            30.0 < ccp && ccp < 31.0 // ccp == 30.197159598135954
        when:
            ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
            icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/ImageDiff'),
                imageDeltaStats)
            mr.scan()
        then:
            icd.getOutput() != null
        when:
            List<TSuiteResultId> tsriList =
                    mr.getTSuiteResultIdList(
                            new TSuiteName('Test Suites/ImageDiff'),
                            new TExecutionProfile('default'))
            assert tsriList.size() == 1
            TSuiteResultId tsri = tsriList.get(0)
            TSuiteResult tsr = mr.getTSuiteResult(tsri)
            TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/ImageDiff"))
            List<Material> mateList = tcr.getMaterialList()
            assert mateList.size() == 2          // diffImage + ComparisonResults.json
            Material diffImage = tcr.getMaterialList('png$', true).get(0)
        then:
            diffImage.getPath().toString().endsWith('(16.86).png')
    }
    
    /**
     * This case test:
     * static fakeMaterial(TSuiteResult targetTSuiteResult, Material existingMaterial) 
     * 
     * @return
     */
    def test_fakeMaterial() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_fakeMaterial")
        Path materials = caseOutputDir.resolve('Materials')
        Path storage = caseOutputDir.resolve('Storage')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
        TSuiteName tsn = new TSuiteName('Test Suites/CURA/chronos_capture')
        TExecutionProfile tep = new TExecutionProfile('default')
        TSuiteResultId tsr0 = new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190512_153731'))
        TSuiteResultId tsr1 = new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190512_154033'))
        ms.restore(mr, tsr0)
        ms.restore(mr, tsr1)
        mr.scan()
        mr.markAsCurrent(    'Test Suites/ImageDiff',
                'default', '20190512_154033')
        def tsr = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff',
                'default', '20190512_154033')
        when:
        // revisited.png is found in the tsr1 but not in the tsr0
        TCaseResult tcr1 = mr.getTCaseResult(
                            tsn,
                            tep,
                            TSuiteTimestamp.newInstance('20190512_154033'),
                            new TCaseName('Test Cases/CURA/visitSite'))
        assert tcr1 != null
        Material existing1 = tcr1.getMaterial(Paths.get('revisited.png'))
        assert existing1 != null
        Material fake1 = ImageCollectionDiffer.fakeMaterial(
                                    mr.getTSuiteResult(tsr0),
                                    existing1)
        println "fake1.getPath()=${fake1.getPath().toString()}"
        then:
        fake1 != null
        fake1.getPath().toString().endsWith("revisited.png")
        ! fake1.fileExists()
        
        when:
        // appointment.php%23summary.png is found in the tsr0 but not in the tsr1
        TCaseResult tcr2 = mr.getTCaseResult(
                            tsn,
                            tep,
                            TSuiteTimestamp.newInstance('20190512_153731'),
                            new TCaseName('Test Cases/CURA/visitSite'))
        assert tcr2 != null
        Material existing2 = tcr2.getMaterial(Paths.get('appointment.php%23summary.png'))
        assert existing2 != null
        Material fake2 = ImageCollectionDiffer.fakeMaterial(
                                    mr.getTSuiteResult(tsr1),
                                    existing2)
        println "fake2.getPath()=${fake2.getPath().toString()}"
        then:
        fake2 != null
        fake2.getPath().toString().endsWith("appointment.php%23summary.png")
        ! fake2.fileExists()
    }
    
    
    def test_decoratedExpectedImage() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_decoratedExpectedImage")
        Path materials = caseOutputDir.resolve('Materials')
        Path storage = caseOutputDir.resolve('Storage')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
        TSuiteName tsn = new TSuiteName('Test Suites/CURA/chronos_capture')
        TExecutionProfile tep = new TExecutionProfile('default')
        TSuiteResultId tsr0 = new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190512_153731'))
        TSuiteResultId tsr1 = new TSuiteResultIdImpl(tsn, tep, TSuiteTimestamp.newInstance('20190512_154033'))
        ms.restore(mr, tsr0)
        ms.restore(mr, tsr1)
        mr.scan()
        mr.markAsCurrent(    'Test Suites/ImageDiff', 'default', '20190512_154033')
        def r = mr.ensureTSuiteResultPresent('Test Suites/ImageDiff', 'default', '20190512_154033')
        when:
        // revisited.png is found in the tsr1 but not in the tsr0
        MaterialPairs materialPairs = mr.createMaterialPairsForChronosMode(tsn, tep)
        StorageScanner storageScanner = new StorageScanner(ms)
        ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn, tep)
        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
            materialPairs,
            new TCaseName('Test Cases/ImageDiff'),
            imageDeltaStats)
        mr.scan()
        List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(
                new TSuiteName('Test Suites/ImageDiff'),
                new TExecutionProfile('default')
        )
        assert tsriList.size() == 1
        TSuiteResultId tsri = tsriList.get(0)
        TSuiteResult tsr = mr.getTSuiteResult(tsri)
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/ImageDiff"))
        List<Material> mateList = tcr.getMaterialList()
        assert mateList.size() == 6          // diffImage + ComparisonResults.json
        Material diffImage = tcr.getMaterialList('png$', true).get(0)
        then:
        diffImage.getPath().toString().endsWith('(100.00)FAILED.png')
    }
    
    def test_makeMarkerImage() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeMarkerImage")
        Files.createDirectories(caseOutputDir)
        Path out = caseOutputDir.resolve("out.png")
        Path dir = Paths.get(".").toAbsolutePath()
        Path file = dir.resolve("fake.png")
        when:
        BufferedImage bi = ImageCollectionDiffer.makeMarkerImage(file)
        assert bi != null
        ImageIO.write(bi, "PNG", out.toFile())
        then:
        Files.exists(out)
        out.toFile().length() > 0
    }
}
