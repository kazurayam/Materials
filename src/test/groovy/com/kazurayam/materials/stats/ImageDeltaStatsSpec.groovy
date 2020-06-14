package com.kazurayam.materials.stats

import com.kazurayam.materials.TExecutionProfile
import org.junit.platform.engine.TestExecutionResult

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.stats.StorageScanner.Options
import com.kazurayam.materials.stats.StorageScanner.Options.Builder

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification

class ImageDeltaStatsSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaStatsSpec.class)
    
    // fields
    private static Path fixtureDir
    private static Path specOutputDir
    
    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("./src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(ImageDeltaStatsSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * 
     */
    
    def testGetStorageScannerOptions() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testGetStorageScannerOptions")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Path reports = caseOutputDir.resolve('reports')
        Files.createDirectories(reports)
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile("default")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        //
        double value = 0.10
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            shiftCriteriaPercentageBy(value).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        ImageDeltaStats ids = scanner.scan(new TSuiteName("47News_chronos_capture"))
        //
        scanner.persist(ids, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        then:
        ids.getStorageScannerOptions().getShiftCriteriaPercentageBy() == value
    }
    
    /**
     * 
     */
    
    def testGetCriteriaPercentage_customizingFilterDataLessThan() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testGetCriteriaPercentage_customizingFilterDataLessThan")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(reports)
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            filterDataLessThan(0.0).  // LOOK HERE
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
        //
        scanner.persist(ids, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        double criteriaPercentage = ids.getCriteriaPercentage(tsn, Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        then:
        // criteriaPercentage == 12.767696022300328 
        12.0 < criteriaPercentage
        criteriaPercentage < 13.0
    }
    
    /**
     * 
     */
    
    def testGetCriteriaPercentage_customizingProbability() {
        setup:
            Path caseOutputDir = specOutputDir.resolve("testGetCriteriaPercentage_customizingProbability")
            Path fixtureStorage = fixtureDir.resolve('Storage')
            Path outputStorage = caseOutputDir.resolve('Storage')
            Path reports = caseOutputDir.resolve('Reports')
            Files.createDirectories(reports)
            Files.createDirectories(outputStorage)
            Helpers.copyDirectory(fixtureStorage, outputStorage)
            MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
            //
            TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
            TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
            TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
            Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                    tSuiteNameExam,
                    tExecutionProfile,
                    tCaseNameExam)
            StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            probability(0.75).  // LOOK HERE
                                            build()
            StorageScanner scanner = new StorageScanner(ms, options)
        when:
            TSuiteName tsn = new TSuiteName("47News_chronos_capture")
            ImageDeltaStats ids = scanner.scan(tsn)
        //
        scanner.persist(ids, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        double criteriaPercentage = ids.getCriteriaPercentage(tsn, Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        then:
            criteriaPercentage == 15.20
    }
    
    /**
     * 
     */
    
    def testToString() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testToString")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(reports)
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile("default")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats ids = scanner.scan(new TSuiteName("47News_chronos_capture"))
        //
        scanner.persist(ids, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        when:
        String s = ImageDeltaStats.ZERO.toString()
        String pp = JsonOutput.prettyPrint(s)
        logger_.debug(ImageDeltaStats.getClass().getName() + ".ZERO:\n${pp}")
        then:
        s.contains("0.0")
    }
    
    /**
     * 
     * @return
     */
    
    def testWrite() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testWrite")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(reports)
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
            previousImageDeltaStats(previousIDS).
            shiftCriteriaPercentageBy(25.0).
            probability(0.75).  // LOOK HERE
            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
        //
        scanner.persist(ids, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        Path file = caseOutputDir.resolve('image-delta-stats.json')
        // now we write this
        ids.write(file)
        then:
        Files.exists(file)
        file.toFile().length() > 0
        when:
        JsonSlurper slurper = new JsonSlurper()
        def json = slurper.parse(file.toFile())
        then:
        json.storageScannerOptions.shiftCriteriaPercentageBy == 25.0
        json.storageScannerOptions.filterDataLessThan == 1.0
        json.storageScannerOptions.maximumNumberOfImageDeltas == 10
        json.storageScannerOptions.onlySince == '19990101_000000'
        json.storageScannerOptions.onlySinceInclusive == true
        json.storageScannerOptions.probability == 0.75
        json.imageDeltaStatsEntries.size() == 1
        json.imageDeltaStatsEntries[0].TSuiteName == '47News_chronos_capture'
        json.imageDeltaStatsEntries[0].materialStatsList.size()== 1
        json.imageDeltaStatsEntries[0].materialStatsList[0].path == "main.TC_47News.visitSite/47NEWS_TOP.png"
        json.imageDeltaStatsEntries[0].materialStatsList[0].imageDeltaList.size()> 0
        json.imageDeltaStatsEntries[0].materialStatsList[0].criteriaPercentage == 40.20
    }
    
    def testResolvePath() {
        when:
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp()
        TCaseName tCaseName = new TCaseName('Test Cases/main/TC_47News/ImageDiff')
        Path jsonPath = ImageDeltaStats.resolvePath(tSuiteNameExam, tSuiteTimestamp, tCaseName)
        logger_.debug("#testResolvePath jsonPath=${jsonPath}")
        then:
        jsonPath != null
        jsonPath.toString().endsWith(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME) 
    }
    
    
    
    def testFromJsonFile() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testFromJson")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
            previousImageDeltaStats(previousIDS).
            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        ImageDeltaStats imageDeltaStats = scanner.scan(new TSuiteName("47News_chronos_capture"))
        Path path = scanner.persist(imageDeltaStats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        then:
        Files.exists(path)
        when:
        ImageDeltaStats ids = ImageDeltaStats.fromJsonFile(path)
        then:
        ids.storageScannerOptions.shiftCriteriaPercentageBy == 0.0
        // ids.storageScannerOptions.previousImageDeltaStats == path
        ids.imageDeltaStatsEntries[0].TSuiteName.value == '47News_chronos_capture'
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getPath().equals(
            Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        ids.imageDeltaStatsEntries[0].materialStatsList[0].degree() == 5
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getCriteriaPercentage() == 15.2
        ids.imageDeltaStatsEntries[0].materialStatsList[0].data()[0] == 16.86
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getImageDeltaList()[0].d == 16.86
    }
    
    
    def testHasImageDelta() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testHasImageDelta")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
            previousImageDeltaStats(previousIDS).
            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = scanner.scan(tsn)
        //
        scanner.persist(stats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        when:
        Path pathRelativeToTSuiteTimestampDir = Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png')
        TSuiteTimestamp a = new TSuiteTimestamp('20190216_204329')
        TSuiteTimestamp b = new TSuiteTimestamp('20190216_064354')
        then:
        stats.hasImageDelta(tsn, pathRelativeToTSuiteTimestampDir, a, b)
        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190301_065500')
        then:
        ! stats.hasImageDelta(tsn, pathRelativeToTSuiteTimestampDir, another, b)
    }
    
    
    def testGetImageDelta() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testHasImageDelta")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
            previousImageDeltaStats(previousIDS).
            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = scanner.scan(tsn)
        scanner.persist(stats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        when:
        Path pathRelativeToTSuiteTimestampDir = Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png')
        TSuiteTimestamp a = new TSuiteTimestamp('20190216_204329')
        TSuiteTimestamp b = new TSuiteTimestamp('20190216_064354')
        ImageDelta id1 = stats.getImageDelta(tsn, pathRelativeToTSuiteTimestampDir, a, b)
        then:
        id1 != null
        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190301_065500')
        ImageDelta id2 = stats.getImageDelta(tsn, pathRelativeToTSuiteTimestampDir, another, b)
        then:
        id2 == null
    }
    

    
    @Ignore
    def testIgnoreThis() {}
}
