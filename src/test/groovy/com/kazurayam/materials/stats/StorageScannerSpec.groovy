package com.kazurayam.materials.stats

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.stats.StorageScanner.BufferedImageBuffer
import com.kazurayam.materials.stats.StorageScanner.Options
import com.kazurayam.materials.stats.StorageScanner.Options.Builder

import groovy.json.JsonOutput
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class StorageScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(StorageScannerSpec.class)
    
    // fields
    private static Path fixtureDir
    private static Path specOutputDir

    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("./src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(StorageScannerSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    def testScan_47News() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testScan_47News")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = scanner.scan(tSuiteName)
        //
        scanner.persist(stats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        then:
        statsEntry != null
        statsEntry.getTSuiteName().equals(tSuiteName)
        when:
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        then:
        mstats != null
        mstats.getPath().equals(Paths.get("main.TC_47News.visitSite/47NEWS_TOP.png"))
    }
    
    def testBufferedImageBuffer() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testBufferedImageBuffer")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = scanner.scan(tSuiteName)
        //
        scanner.persist(stats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        TSuiteName tsn = statsEntry.getTSuiteName()
        Path path = Paths.get("main.TC_47News.visitSite/47NEWS_TOP.png")
        List<Material> materials = scanner.getMaterialsOfARelativePathInATSuiteName(tsn, path)
        BufferedImageBuffer biBuffer = new BufferedImageBuffer()
        then:
        materials.size() == 7
        when:
        BufferedImage bi0 = biBuffer.read(materials.get(0))
        BufferedImage bi1 = biBuffer.read(materials.get(1))
        then:
        bi0 != null
        bi1 != null
        biBuffer.size() == 2
        when:
        bi0 = biBuffer.remove(materials.get(0))
        //
        bi1 = biBuffer.read(materials.get(1))
        BufferedImage bi2 = biBuffer.read(materials.get(2))
        then:
        bi2 != null
        biBuffer.size() == 2   // must not be 3!
        when:
        bi1 = biBuffer.remove(materials.get(1))
        then:
        biBuffer.size() == 1
    }
    
    /**
     * Specifying maximumNumberOfDelta=3 makes StorageScanner to generate
     * 3 ImageDelta objects in a MatrialStats object.
     * @return
     */
    def testSpecifyingOption_maximumNumberOfDelta() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testSpecifyingOption_maximumNumberOfDelta")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)        
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            maximumNumberOfImageDeltas(3).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(tSuiteName)
        //
        scanner.persist(stats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        then:
        scanner.getOptions().getMaximumNumberOfImageDeltas() == 3
        mstats != null
        mstats.getImageDeltaList().size() == 3
    }

    def testSpecifyingOption_onlySince() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testSpecifyingOption_onlySince")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            onlySince(TSuiteTimestamp.newInstance("20190216_064149"), true).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(tSuiteName)
        //
        scanner.persist(stats, tSuiteNameExam, new TSuiteTimestamp(), tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        //println "#testSpecifyingOptions_onlySince mstats:${mstats.toJsonText()}"
        then:
        mstats != null
        mstats.getImageDeltaList().size() == 2
    }

   
    def testIsInRangeOfTSuiteTimestamp_inclusive_default() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testIsInRangeOfTSuiteTimestamp_inclusive_default")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190216_064149")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        StorageScanner.Options options = 
            new Options.Builder().
                previousImageDeltaStats(previousIDS).
                onlySince(tst).  // onlySinceInclude default: true
                build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        List<Material> materials = tsr.getMaterialList()
        then:
        materials.size() == 1
        when:
        Material mate = materials.get(0)
        then:
        scanner.isInRangeOfTSuiteTimestamp(mate)
    }
    
    
    def testIsInRangeOfTSuiteTimestamp_inclusive_false() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testIsInRangeOfTSuiteTimestamp_inclusive_false")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190216_064149")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        StorageScanner.Options options =
            new Options.Builder().
                previousImageDeltaStats(previousIDS).
                onlySince(tst, false).   // onlySinceInclusive: false
                build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        List<Material> materials = tsr.getMaterialList()
        then:
        materials.size() == 1
        when:
        Material mate = materials.get(0)
        then:
        ! scanner.isInRangeOfTSuiteTimestamp(mate)
    }
    

    def testIsInRangeOfTSuiteTimestamp_inclusive_true() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testIsInRangeOfTSuiteTimestamp_inclusive_true")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47News_chronos_exam")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190216_064149")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        StorageScanner.Options options =
            new Options.Builder().
                previousImageDeltaStats(previousIDS).
                onlySince(tst, true).  // onlySinceInclusive: true
                build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        List<Material> materials = tsr.getMaterialList()
        then:
        materials.size() == 1
        when:
        Material mate = materials.get(0)
        then:
        scanner.isInRangeOfTSuiteTimestamp(mate)  
    }

    def test_Options_maximumNumberOfDelta() {
        when:
        StorageScanner.Options options = new Options.Builder().maximumNumberOfImageDeltas(3).build()
        then:
        options.getMaximumNumberOfImageDeltas() == 3
    }
    
    def test_Options_onlySince() {
        when:
        TSuiteTimestamp tsn = TSuiteTimestamp.newInstance("20190101_000000")
        StorageScanner.Options options = new Options.Builder().onlySince(tsn, false).build()
        then:
        options.getOnlySince().equals(tsn)
        options.getOnlySinceInclusive() == false
    }
    
    def test_Options_toString() {
        when:
        StorageScanner.Options options = new Options.Builder().build()
        logger_.debug("#test_Options_toString() optons.toString()=${options.toString()}")
        String json = JsonOutput.prettyPrint(options.toString())
        logger_.debug("#test_Options_toString() json=${json}")
        then:
        json.length() > 0
    }
    
    def test_Options_previousImageDeltaStats_validPath() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_Options_previousImageDeltaStats_validPath")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam           = new TSuiteName("Test Suites/47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190216_064149")
        TCaseName tCaseNameExam             = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        //
        Path persisted = scanner.persist(stats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        //
        String content = persisted.toFile().text
        logger_.debug("#test_Options_previousImageDeltaStats content=\n" + content)
        then:
        content.contains('previousImageDeltaStats')
        when:
        // now scan again to reflect cached image-delta-stats.json under the Strogae dir
        options = new Options.Builder().
            previousImageDeltaStats(persisted).
            build()
        scanner = new StorageScanner(ms, options)
        stats = scanner.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        StringWriter sw = new StringWriter()
        stats.write(sw)
        then:
        sw.toString().contains('previousImageDeltaStats')
        sw.toString().contains('image-delta-stats.json')    
    }
    
    /**
     * Verify the case where we give invalid path to Options.previousImageDeltaStatus(String path).
     * Processing should continue with no problem
     */
    def test_Options_previousImageDeltaStats_invalidPath() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_Options_previousImageDeltaStats_invalidPath")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190216_064149")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        //
        Path persisted = scanner.persist(stats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        //
        String content = persisted.toFile().text
        logger_.debug("#test_Options_previousImageDeltaStats content=\n" + content)
        then:
        content.contains('previousImageDeltaStats')
        when:
        // now scan again to reflect cached image-delta-stats.json under the Strogae dir
        options = new Options.Builder().
            previousImageDeltaStats(Paths.get('./invalid/path')).   // due to this, it would take long time
            build()
        scanner = new StorageScanner(ms, options)
        stats = scanner.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        StringWriter sw = new StringWriter()
        stats.write(sw)
        then:
        sw.toString().contains('previousImageDeltaStats')
        sw.toString().contains('invalid')
    }
    
    def testPersist() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testPersist")
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190216_064149")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        //
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tSuiteNameCapture = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = scanner.scan(tSuiteNameCapture)
        //
        Path persisted = scanner.persist(stats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        //
        when:
        Path pathOfImageDeltaStats = scanner.persist(stats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        then:
        Files.exists(pathOfImageDeltaStats)
    }
    
    @IgnoreRest
    def testFindLatestImageDeltaStats() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testFindLatestImageDeltaStats")
        if (Files.exists(caseOutputDir)) {
            Helpers.deleteDirectoryContents(caseOutputDir)
        }
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190216_064149")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms, tSuiteNameExam, tCaseNameExam)
        when:
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats imageDeltaStats = scanner.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        //
        Path p = scanner.persist(imageDeltaStats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        then:
        Files.exists(p)
        when:
        Path latestPath = scanner.findLatestImageDeltaStats(tSuiteNameExam, tCaseNameExam)
        logger_.debug("#testFindLatestImageDeltaStats latestPath=${latestPath}")
        then:
        latestPath.toString().endsWith(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME)
    }
    
    /**
     * It is expected that StorageScanner.scan runs much faster in the 2nd run
     * when we use StorageScanner.Options.Builder#previousImageDeltaStats(xxx).
     * Let's try it to see how much the speed is improved.
     *
     * @return
     */
    
    def testPeformanceImprovementByPreviousImageDeltaStats() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_PerformanceImprovementByPreviousImageDeltaStats")
        if (Files.exists(caseOutputDir)) {
            Helpers.deleteDirectoryContents(caseOutputDir)
        }
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        when:
        StorageScanner.Options options = new Options.Builder().build()
        StorageScanner scanner = new StorageScanner(ms, options)
        StopWatch sw1 = new StopWatch()
        sw1.start()
        ImageDeltaStats imageDeltaStats = scanner.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        sw1.stop()
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47News_chronos_exam")
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190301_095547")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path pathOfImageDeltaStats = scanner.persist(imageDeltaStats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        logger_.debug("#testPerformanceImprovementByPreviousImageDeltaStats pathOfImageDeltaStats=${pathOfImageDeltaStats}")
        then:
        Files.exists(pathOfImageDeltaStats)
        
        when:
        //Path latestPath = scanner.findLatestImageDeltaStats(tSuiteNameExam, tCaseNameExam)
        StorageScanner.Options options2 = new Options.Builder().
                                            previousImageDeltaStats(pathOfImageDeltaStats).
                                            build()
        StorageScanner scanner2 = new StorageScanner(ms, options2)
        StopWatch sw2 = new StopWatch()
        sw2.start()
        imageDeltaStats = scanner2.scan(new TSuiteName("Test Suites/47News_chronos_capture"))
        sw2.stop()
        Path p = scanner2.persist(imageDeltaStats, tSuiteNameExam, tSuiteTimestampExam, tCaseNameExam)
        logger_.debug("#testPeformanceImprovementByPreviousImageDeltaStats sw1=${sw1.getTime()}, sw2=${sw2.getTime()}")
        then:
        // I expect the processing with cache is over 100 times or even more faster 
        // than creating ImageDilta objects from DISK 
        sw1.getTime() / 100 > sw2.getTime()
    }
    
    
    @Ignore
    def testIgnoring() {}
    
    // helper methods
    @Ignore
    def void anything() {}
    
}
