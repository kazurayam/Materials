package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.ImageDeltaStats.PersistedImageDeltaStats
import com.kazurayam.materials.stats.ImageDelta
import com.kazurayam.materials.stats.StorageScanner

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
    @Ignore
    def testGetStorageScannerOptions() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testGetStorageScannerOptions")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        double value = 0.10
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                            shiftCriteriaPercentageBy(value).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        ImageDeltaStats ids = scanner.scan(new TSuiteName("47News_chronos_capture"))
        then:
        ids.getStorageScannerOptions().getShiftCriteriaPercentageBy() == value
    }
    
    /**
     * 
     */
    @Ignore
    def testGetCriteriaPercentage_customizingFilterDataLessThan() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testGetCriteriaPercentage_customizingFilterDataLessThan")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                            filterDataLessThan(0.0).  // LOOK HERE
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
        double criteriaPercentage = ids.getCriteriaPercentage(tsn, Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        then:
        // criteriaPercentage == 12.767696022300328 
        12.0 < criteriaPercentage
        criteriaPercentage < 13.0
    }
    
    /**
     * 
     */
    @Ignore
    def testGetCriteriaPercentage_customizingProbability() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testGetCriteriaPercentage_customizingProbability")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                            probability(0.75).  // LOOK HERE
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
        double criteriaPercentage = ids.getCriteriaPercentage(tsn, Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        then:
        // criteriaPercentage == 15.197159598135954
        15.00 < criteriaPercentage
        criteriaPercentage < 15.20
    }
    
    /**
     * 
     */
    @Ignore
    def testToString() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testToString")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        StorageScanner scanner = new StorageScanner(ms)
        ImageDeltaStats ids = scanner.scan(new TSuiteName("47News_chronos_capture"))
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
    @Ignore
    def testWrite() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testWrite")
        Path fixtureStorage = fixtureDir.resolve('Storage')
        Path outputStorage = caseOutputDir.resolve('Storage')
        Files.createDirectories(outputStorage)
        Helpers.copyDirectory(fixtureStorage, outputStorage)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        StorageScanner.Options options = new StorageScanner.Options.Builder().
            shiftCriteriaPercentageBy(25.0).
            probability(0.75).  // LOOK HERE
            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
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
    
    @Ignore
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
    
    @Ignore
    def testPersist() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testPersist")
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        StorageScanner.Options options = new StorageScanner.Options.Builder().build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tSuiteNameCapture = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats imageDeltaStats = scanner.scan(tSuiteNameCapture)
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestamp = new TSuiteTimestamp()
        TCaseName tCaseName = new TCaseName('Test Cases/main/TC_47News/ImageDiff')
        when:
        Path jsonPath = ImageDeltaStats.resolvePath(tSuiteNameExam, tSuiteTimestamp, tCaseName)
        PersistedImageDeltaStats stats = imageDeltaStats.persist(ms, mr, jsonPath)
        then:
        Files.exists(stats.getPathInStorage())
        Files.exists(stats.getPathInMaterials())
    }
    
    @Ignore
    def testFromJsonFile() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testFromJson")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        StorageScanner.Options options = new StorageScanner.Options.Builder().build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats imageDeltaStats = scanner.scan(new TSuiteName("47News_chronos_capture"))
        Path path = ImageDeltaStats.resolvePath(new TSuiteName('47News_chronos_exam'), 
            new TSuiteTimestamp(), new TCaseName('Test Cases/main/TS1/ImageDiff'))
        imageDeltaStats.persist(ms, mr, path)
        when:
        Path jsonFilePath = ms.getBaseDir().resolve(path)
        then:
        Files.exists(jsonFilePath)
        when:
        ImageDeltaStats ids = ImageDeltaStats.fromJsonFile(jsonFilePath)
        then:
        ids.storageScannerOptions.shiftCriteriaPercentageBy == 0.0
        ids.storageScannerOptions.previousImageDeltaStats == ""
        ids.imageDeltaStatsEntries[0].TSuiteName.value == '47News_chronos_capture'
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getPath().equals(
            Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        ids.imageDeltaStatsEntries[0].materialStatsList[0].degree() == 5
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getCriteriaPercentage() > 15.19 // 15.197159598135954
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getCriteriaPercentage() < 15.20 // 15.197159598135954
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
        StorageScanner.Options options = new StorageScanner.Options.Builder().build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats imageDeltaStats = scanner.scan(tsn)
        when:
        Path pathRelativeToTSuiteTimestampDir = Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png')
        TSuiteTimestamp a = new TSuiteTimestamp('20190216_204329')
        TSuiteTimestamp b = new TSuiteTimestamp('20190216_064354')
        then:
        imageDeltaStats.hasImageDelta(tsn, pathRelativeToTSuiteTimestampDir, a, b)
        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190301_065500')
        then:
        ! imageDeltaStats.hasImageDelta(tsn, pathRelativeToTSuiteTimestampDir, another, b)
    }
    
    def testGetImageDelta() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testHasImageDelta")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        StorageScanner.Options options = new StorageScanner.Options.Builder().build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats imageDeltaStats = scanner.scan(tsn)
        when:
        Path pathRelativeToTSuiteTimestampDir = Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png')
        TSuiteTimestamp a = new TSuiteTimestamp('20190216_204329')
        TSuiteTimestamp b = new TSuiteTimestamp('20190216_064354')
        ImageDelta id1 = imageDeltaStats.getImageDelta(tsn, pathRelativeToTSuiteTimestampDir, a, b)
        then:
        id1 != null
        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190301_065500')
        ImageDelta id2 = imageDeltaStats.getImageDelta(tsn, pathRelativeToTSuiteTimestampDir, another, b)
        then:
        id2 == null
    }
    
    @Ignore
    def testFoo() {}
}
