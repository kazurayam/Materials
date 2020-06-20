package com.kazurayam.materials.stats

import com.kazurayam.materials.TExecutionProfile

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
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
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.stats.StorageScanner.BufferedImageBuffer
import com.kazurayam.materials.stats.StorageScanner.Options

import groovy.json.JsonOutput
import spock.lang.Ignore
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
    
    def test_scan_47News() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_scan_47News")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                    tSuiteNameExam,
                    tExecutionProfile,
                    tCaseNameExam)
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        StringWriter messageBuffer = new StringWriter()
        VisualTestingLogger listener = new VisualTestingListenerCustomImpl(messageBuffer)
        scanner.setVisualTestingLogger(listener)
        when:
        TSuiteName tSuiteNameChronos = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tExecutionProfileChronos = new TExecutionProfile("default")
        // Here we go!
        ImageDeltaStats stats = scanner.scan(tSuiteNameChronos, tExecutionProfile)
        scanner.persist(stats,
                tSuiteNameExam, tExecutionProfile, new TSuiteTimestamp(/* now */), tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteNameChronos, tExecutionProfileChronos)
        then:
        statsEntry != null
        statsEntry.getTSuiteName() == tSuiteNameChronos
        when:
        MaterialStats mStats = statsEntry.getMaterialStatsList()[0]
        then:
        stats != null
        mStats.getPath() == Paths.get("47news.visitSite/47reporters.png")
        when:
        println messageBuffer.toString()
        then:
        true
    }
    
    def test_BufferedImageBuffer() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_BufferedImageBuffer")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tSuiteName = new TSuiteName("47news.chronos_capture")
        ImageDeltaStats stats = scanner.scan(tSuiteName, tExecutionProfile)
        //
        scanner.persist(stats,
                tSuiteNameExam,
                tExecutionProfile,
                new TSuiteTimestamp(/* now */),
                tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName, tExecutionProfile)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        TSuiteName tsn = statsEntry.getTSuiteName()
        Path path = Paths.get("47news.visitSite/top.png")

        when:
        List<Material> materials =
                scanner.getMaterialsOfARelativePathInATSuiteName(
                        tsn,
                        new TExecutionProfile("default"),
                        path)
        BufferedImageBuffer biBuffer = new BufferedImageBuffer()
        then:
        materials.size() == 2

        when:
        BufferedImage bi0 = biBuffer.read(materials.get(0))
        BufferedImage bi1 = biBuffer.read(materials.get(1))
        then:
        bi0 != null
        bi1 != null
        biBuffer.size() == 2

        when:
        bi0 = biBuffer.remove(materials.get(0))
        bi1 = biBuffer.read(materials.get(1))
        BufferedImage bi2 = biBuffer.read(materials.get(1))
        then:
        bi2 != null
        biBuffer.size() == 1

        when:
        bi1 = biBuffer.remove(materials.get(1))
        then:
        biBuffer.size() == 0
    }
    
    /**
     * Specifying maximumNumberOfDelta=3 makes StorageScanner to generate
     * 3 ImageDelta objects in a MatrialStats object.
     * @return
     */
    def test_specifyingOption_maximumNumberOfDelta() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_specifyingOption_maximumNumberOfDelta")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        TSuiteName tSuiteName = new TSuiteName("47news.chronos_capture")
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            maximumNumberOfImageDeltas(3).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(tSuiteName, tExecutionProfile)
        //
        scanner.persist(stats,
                tSuiteNameExam,
                tExecutionProfile,
                new TSuiteTimestamp(),
                tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName, tExecutionProfile)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        then:
        scanner.getOptions().getMaximumNumberOfImageDeltas() == 3
        mstats != null
        mstats.getImageDeltaList().size() == 1
    }

    def test_specifyingOption_onlySince() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_specifyingOption_onlySince")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        TSuiteName tSuiteName = new TSuiteName("47news.chronos_capture")
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            onlySince(TSuiteTimestamp.newInstance("20190216_064149"), true).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(tSuiteName, tExecutionProfile)
        //
        scanner.persist(stats,
                tSuiteNameExam,
                tExecutionProfile,
                new TSuiteTimestamp(),
                tCaseNameExam)
        //
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName, tExecutionProfile)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        //println "#testSpecifyingOptions_onlySince mstats:${mstats.toJsonText()}"
        then:
        mstats != null
        mstats.getImageDeltaList().size() == 1
    }

   
    def test_isInRangeOfTSuiteTimestamp_inclusive_default() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_isInRangeOfTSuiteTimestamp_inclusive_default")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        TSuiteName tsn = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190401_142150")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        StorageScanner.Options options = 
            new Options.Builder().
                previousImageDeltaStats(previousIDS).
                onlySince(tst).  // onlySinceInclude default: true
                build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        List<Material> materials = tsr.getMaterialList()
        then:
        materials.size() == 12
        when:
        Material mate = materials.get(0)
        then:
        scanner.isInRangeOfTSuiteTimestamp(mate)
    }
    
    
    def test_isInRangeOfTSuiteTimestamp_inclusive_false() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_isInRangeOfTSuiteTimestamp_inclusive_false")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        TSuiteName tsn = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tep = new TExecutionProfile('default')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190401_142150")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        StorageScanner.Options options =
            new Options.Builder().
                previousImageDeltaStats(previousIDS).
                onlySince(tst, false).   // onlySinceInclusive: false
                build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        List<Material> materials = tsr.getMaterialList()
        then:
        materials.size() == 12
        when:
        Material mate = materials.get(0)
        then:
        ! scanner.isInRangeOfTSuiteTimestamp(mate)
    }
    

    def test_isInRangeOfTSuiteTimestamp_inclusive_true() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_isInRangeOfTSuiteTimestamp_inclusive_true")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        TSuiteName tsn = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tep = new TExecutionProfile('default')
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190401_142150")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        StorageScanner.Options options =
            new Options.Builder().
                previousImageDeltaStats(previousIDS).
                onlySince(tst, true).  // onlySinceInclusive: true
                build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteResult tsr = ms.getTSuiteResult(tsri)
        List<Material> materials = tsr.getMaterialList()
        then:
        materials.size() == 12
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
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190216_064149")
        TCaseName tCaseNameExam             = new TCaseName("Test Cases/main/TC_47News/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(
                new TSuiteName("Test Suites/47news.chronos_capture"),
                new TExecutionProfile('default'))
        //
        Path persisted = scanner.persist(stats,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam,
                tCaseNameExam)
        //
        String content = persisted.toFile().text
        logger_.debug("#test_Options_previousImageDeltaStats content=\n" + content)
        then:
        content.contains('previousImageDeltaStats')
        when:
        // now scan again to reflect cached image-delta-stats.json under the Storage dir
        options = new Options.Builder().
            previousImageDeltaStats(persisted).
            build()
        scanner = new StorageScanner(ms, options)
        stats = scanner.scan(
                new TSuiteName("Test Suites/47news.chronos_capture"),
                new TExecutionProfile('default'))
        StringWriter sw = new StringWriter()
        stats.write(sw)
        then:
        sw.toString().contains('previousImageDeltaStats')
        sw.toString().contains('image-delta-stats.json')    
    }
    
    /**
     * Verify the case where we give invalid path to Options.previousImageDeltaStatus(String path).
     * Processing should continue with no problem
     * 
     * This test case may take longer secodns than 20
     * 
     */
    def test_Options_previousImageDeltaStats_invalidPath() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_Options_previousImageDeltaStats_invalidPath")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))

        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190401_142150")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        when:
        StorageScanner.Options options = new Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats stats = scanner.scan(
                new TSuiteName("Test Suites/47news.chronos_capture"),
                new TExecutionProfile('default'))
        //
        Path persisted = scanner.persist(stats,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam,
                tCaseNameExam)
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
        stats = scanner.scan(new TSuiteName("Test Suites/47news.chronos_capture"),
                            new TExecutionProfile('default'))
        StringWriter sw = new StringWriter()
        stats.write(sw)
        then:
        sw.toString().contains('previousImageDeltaStats')
        sw.toString().contains('invalid')
    }
    
    
    def test_Options_getPreviousImageDeltaStatsRelativeToProjectDirectory() {
        when:
        Path absolutePath = specOutputDir.
            resolve('test_Options_previousImageDeltaStats_relativized').
            resolve('image-delta-stats.json').
            toAbsolutePath()
        logger_.debug("#test_Options_previousImageDeltaStats_relativized absolutePath=${absolutePath}")
        logger_.debug("#test_Options_previousImageDeltaStats_relativized current dir =${Paths.get('.').toAbsolutePath()}")
        Path projectDirectory = Paths.get('.')
        Path relativePath = projectDirectory.toAbsolutePath().relativize(absolutePath)
        StorageScanner.Options options = 
                                    new Options.Builder().
                                            projectDirectory(projectDirectory).
                                            previousImageDeltaStats( absolutePath ).build()
        logger_.debug("#test_Options_previousImageDeltaStats_relativized options.getPreviousImageDeltaStats()=${options.getPreviousImageDeltaStats()}")
        logger_.debug("#test_Options_previousImageDeltaStats_relativized relativePath=${relativePath}")
        logger_.debug("#test_Options_previousImageDeltaStats_relativized options.getPreviousImageDeltaStatsRelativeToProjectDirectory()=${options.getPreviousImageDeltaStatsRelativeToProjectDirectory().toString()}")
        then:
        options.getPreviousImageDeltaStatsRelativeToProjectDirectory().toString().equals(relativePath.toString())
        options.getPreviousImageDeltaStatsRelativeToProjectDirectory().toString().startsWith('build')   // build\tmp\testOutput\StorageScannerSpec\ .. 
    }
    
    def testPersist() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testPersist")
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        //
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190401_142150")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        //
        StorageScanner.Options options = new com.kazurayam.materials.stats.StorageScanner.Options.Builder().
                                            previousImageDeltaStats(previousIDS).
                                            build()
        StorageScanner scanner = new StorageScanner(ms, options)
        TSuiteName tSuiteNameCapture = new TSuiteName("47news.chronos_capture")
        ImageDeltaStats stats = scanner.scan(tSuiteNameCapture, tExecutionProfile)
        when:
        Path pathOfImageDeltaStats = scanner.persist(stats,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam,
                tCaseNameExam)
        then:
        Files.exists(pathOfImageDeltaStats)
    }
    
    /**
     * test StorageScanner#findLatestImageDeltaStats() method.
     * 
     * This test calls StorageScanner#scan() twice to create 2 image-delta-stats.json files.
     * Once 2 json files are created, this test calls findLatestImageDeltaStats() and assert
     * it returns the json with newer timestamp.
     * 
     * this test case may take long time; more than 20 seconds
     */
    def test_findLatestImageDeltaStats() {
        setup:
        // create case output directory with fixture
        Path caseOutputDir = specOutputDir.resolve("test_findLatestImageDeltaStats")
        if (Files.exists(caseOutputDir)) {
            // intentionally delete the previous fixture
            Helpers.deleteDirectoryContents(caseOutputDir)
        }
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(caseOutputDir.resolve('Materials'))
        MaterialStorage ms = MaterialStorageFactory.createInstance(caseOutputDir.resolve('Storage'))
        when:
        // 1st scanning
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TCaseName tCaseNameExam = new TCaseName("Test Cases/47news/ImageDiff")
        TSuiteTimestamp tSuiteTimestampExam1 = new TSuiteTimestamp("20190401_142151")
        StorageScanner scanner1 = new StorageScanner(ms, new Options.Builder().build())
        ImageDeltaStats imageDeltaStats1 = scanner1.scan(
                new TSuiteName("Test Suites/47news.chronos_capture"),
                new TExecutionProfile('default'))
        Path p1 = scanner1.persist(imageDeltaStats1,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam1,
                tCaseNameExam)
        then:
        Files.exists(p1)
        // 2nd scanning
        TSuiteTimestamp tSuiteTimestampExam2 = new TSuiteTimestamp("20190401_142749")
        StorageScanner scanner2 = new StorageScanner(ms, new Options.Builder().build())
        ImageDeltaStats imageDeltaStats2 = scanner1.scan(
                new TSuiteName("Test Suites/47News_chronos_capture"),
                new TExecutionProfile('default'))
        Path p2 = scanner2.persist(imageDeltaStats2,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam2,
                tCaseNameExam)
        then:
        Files.exists(p2)
        when:
        // Now we test the method in question
        Path latestPath = scanner2.findLatestImageDeltaStats(
                tSuiteNameExam,
                tExecutionProfile,
                tCaseNameExam)
        logger_.debug("#testFindLatestImageDeltaStats latestPath=${latestPath}")
        then:
        latestPath.toString().endsWith(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME)
        latestPath.toString().contains('20190401_142749')   // we expect to get the newer timestamp
        // if failed, you should verify if the sorting feature is functioning properly or not
    }
    
    /**
     * It is expected that StorageScanner.scan runs much faster in the 2nd run
     * when we use StorageScanner.Options.Builder#previousImageDeltaStats(xxx).
     * Let's try it to see how much the speed is improved.
     *
     * this test case may take longer seconds than 20
     */
    def test_PerformanceImprovementByPreviousImageDeltaStats() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_PerformanceImprovementByPreviousImageDeltaStats")
        if (Files.exists(caseOutputDir)) {
            // intentionally delete the previous fixtures
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
        ImageDeltaStats imageDeltaStats = scanner.scan(
                new TSuiteName("Test Suites/47news.chronos_capture"),
                new TExecutionProfile('default'))
        sw1.stop()
        TSuiteName tSuiteNameExam = new TSuiteName("Test Suites/47news.chronos_exam")
        TExecutionProfile tExecutionProfile = new TExecutionProfile('default')
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp("20190401_142151")
        TCaseName tCaseNameExam = new TCaseName("Test Cases/47news/ImageDiff")
        Path pathOfImageDeltaStats = scanner.persist(imageDeltaStats,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam,
                tCaseNameExam)
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
        imageDeltaStats = scanner2.scan(
                new TSuiteName("Test Suites/47news.chronos_capture"),
                new TExecutionProfile('default'))
        sw2.stop()
        Path p = scanner2.persist(imageDeltaStats,
                tSuiteNameExam,
                tExecutionProfile,
                tSuiteTimestampExam,
                tCaseNameExam)
        logger_.debug("#testPeformanceImprovementByPreviousImageDeltaStats sw1=${sw1.getTime()}, sw2=${sw2.getTime()}")
        then:
        // I expect the processing with cache is over 100 times or even more faster 
        // than creating ImageDelta objects from DISK
        sw1.getTime() / 100 > sw2.getTime()
    }
    
    
    @Ignore
    def testIgnoring() {}
    
    // helper methods
    @Ignore
    def void anything() {}
    
    /**
     * 
     * @author kazurayam
     *
     */
    static class VisualTestingListenerCustomImpl implements VisualTestingLogger {
        private Writer writer = null
        VisualTestingListenerCustomImpl(Writer writer) {
            this.writer = writer
        }
        @Override
        void info(String message) {
            writer.write("INFO " + message)
        }
        @Override
        void fatal(String message) {
            writer.write("FATAL " + message)
        }
        @Override
        void failed(String message) {
            writer.write("FAILED " + message)
        }
        
    }
}
