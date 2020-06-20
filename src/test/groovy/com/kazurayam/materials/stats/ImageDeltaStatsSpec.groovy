package com.kazurayam.materials.stats

import com.kazurayam.materials.TExecutionProfile

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.stats.StorageScanner.Options

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification

class ImageDeltaStatsSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaStatsSpec.class)
    
    // fields
    private static Path fixtureDir
    private static Path specOutputDir

    static Path resolvePath(TSuiteName imageDiffTSuiteName,
                            TExecutionProfile tExecutionProfile,
                            TSuiteTimestamp tSuiteTimestamp,
                            TCaseName tCaseName) {
        Path jsonPath = Paths.get(imageDiffTSuiteName.getValue()).
                            resolve(tExecutionProfile.getNameInPathSafeChars()).
                            resolve(tSuiteTimestamp.format()).
                            resolve(tCaseName.getValue()).
                            resolve(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME)
        return jsonPath
    }

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

    private MaterialStorage prepareMS(String testCaseName) {
        Path caseOutputDir = specOutputDir.resolve(testCaseName)
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(
                caseOutputDir.resolve('Storage'))
        return ms
    }

    def testGetStorageScannerOptions() {
        setup:
        MaterialStorage ms = prepareMS("testGetStorageScannerOptions")
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile("default")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousImageDeltaStats = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        when:
        double value = 0.10
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousImageDeltaStats)
                .shiftCriteriaPercentageBy(value)
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats ids = scanner.scan(
                new TSuiteName("47news.chronos_capture"),
                new TExecutionProfile('default'))
        scanner.persist(ids,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
        then:
        ids.getStorageScannerOptions().getShiftCriteriaPercentageBy() == value
    }
    
    /**
     * 
     */
    
    def testGetCriteriaPercentage_customizingFilterDataLessThan() {
        setup:
        MaterialStorage ms = prepareMS("testGetCriteriaPercentage_customizingFilterDataLessThan")
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousIDS)
                .filterDataLessThan(0.0)  // LOOK HERE
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)

        when:
        TSuiteName tsn = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tep = new TExecutionProfile('default')
        ImageDeltaStats ids = scanner.scan(tsn, tep)
        scanner.persist(ids,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
        double criteriaPercentage = ids.getCriteriaPercentage(
                Paths.get('47news.visitSite/top.png'))
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
        MaterialStorage ms = prepareMS("testGetCriteriaPercentage_customizingProbability")
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        Options options = new Options.Builder().
                previousImageDeltaStats(previousIDS).
                probability(0.75).  // LOOK HERE
                build()
        StorageScanner scanner = new StorageScanner(ms, options)

        when:
        TSuiteName tsn = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tep = new TExecutionProfile('default')
        ImageDeltaStats ids = scanner.scan(tsn, tep)
        scanner.persist(ids,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
        double criteriaPercentage = ids.getCriteriaPercentage(
                Paths.get('47news.visitSite/top.png'))
        then:
            criteriaPercentage == 12.04
    }

    def testToString() {
        setup:
        MaterialStorage ms = prepareMS("testToString")
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile("default")
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousIDS)
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)
        ImageDeltaStats ids = scanner.scan(
                new TSuiteName("47news.chronos_capture"),
                new TExecutionProfile('default'))
        //
        scanner.persist(ids,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
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
        MaterialStorage ms = prepareMS("testWrite")
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousIDS)
                .shiftCriteriaPercentageBy(25.0)
                .probability(0.75)  // LOOK HERE
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        TSuiteName tsn = new TSuiteName("47news.chronos_capture")
        TExecutionProfile tep = new TExecutionProfile('default')
        ImageDeltaStats ids = scanner.scan(tsn, tep)
        //
        scanner.persist(ids,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
        //
        Path file = specOutputDir.resolve('testWrite').resolve('image-delta-stats.json')
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
        json.imageDeltaStatsEntries[0].TSuiteName == '47news.chronos_capture'
        json.imageDeltaStatsEntries[0].materialStatsList.size()== 12
        json.imageDeltaStatsEntries[0].materialStatsList[0].path == "47news.visitSite/47reporters.png"
        json.imageDeltaStatsEntries[0].materialStatsList[0].imageDeltaList.size() > 0
        json.imageDeltaStatsEntries[0].materialStatsList[0].criteriaPercentage == 27.65
    }
    
    def testResolvePath() {
        when:
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile('default')
        TSuiteTimestamp tSuiteTimestampExam = new TSuiteTimestamp()
        TCaseName tCaseNameExam = new TCaseName('Test Cases/47news/ImageDiff')
        Path jsonPath = resolvePath(tSuiteNameExam,
                tExecutionProfileExam,
                tSuiteTimestampExam,
                tCaseNameExam)
        logger_.debug("#testResolvePath jsonPath=${jsonPath}")
        then:
        jsonPath != null
        jsonPath.toString().endsWith(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME) 
    }
    
    
    
    def testFromJsonFile() {
        setup:
        MaterialStorage ms = prepareMS("testFromJson")
        //
        TSuiteName tSuiteNameExam = new TSuiteName("47news.chronos_exam")
        TExecutionProfile tExecutionProfileExam = new TExecutionProfile('default')
        TCaseName  tCaseNameExam  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                tSuiteNameExam,
                tExecutionProfileExam,
                tCaseNameExam)
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousIDS)
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)
        when:
        ImageDeltaStats imageDeltaStats = scanner.scan(
                new TSuiteName("47news.chronos_capture"),
                new TExecutionProfile('default'))
        Path path = scanner.persist(imageDeltaStats,
                tSuiteNameExam,
                tExecutionProfileExam,
                new TSuiteTimestamp(),
                tCaseNameExam)
        then:
        Files.exists(path)
        when:
        ImageDeltaStats ids = ImageDeltaStats.fromJsonFile(path)
        then:
        ids.storageScannerOptions.shiftCriteriaPercentageBy == 0.0
        //ids.storageScannerOptions.previousImageDeltaStats == path
        ids.imageDeltaStatsEntries[0].TSuiteName.value == '47news.chronos_capture'
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getPath() == Paths.get('47news.visitSite/47reporters.png')
        ids.imageDeltaStatsEntries[0].materialStatsList[0].degree() == 1
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getCriteriaPercentage() == 2.65
        ids.imageDeltaStatsEntries[0].materialStatsList[0].data()[0] == 2.64
        ids.imageDeltaStatsEntries[0].materialStatsList[0].getImageDeltaList()[0].d == 2.64
    }
    

    def testHasImageDelta() {
        setup:
        MaterialStorage ms = prepareMS("testHasImageDelta")
        //
        TSuiteName examiningTSuiteName = new TSuiteName("47news.chronos_exam")
        TExecutionProfile examiningTExecutionProfile = new TExecutionProfile('default')
        TCaseName  examiningTCaseName  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousIDS = StorageScanner.findLatestImageDeltaStats(ms,
                examiningTSuiteName,
                examiningTExecutionProfile,
                examiningTCaseName)
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousIDS)
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)
        //
        ImageDeltaStats stats= scanner.scan(
                new TSuiteName("47news.chronos_capture"),
                new TExecutionProfile('default'))

        Path file = scanner.persist(stats,
                examiningTSuiteName,
                examiningTExecutionProfile,
                new TSuiteTimestamp(),
                examiningTCaseName)
        assert Files.exists(file)

        when:
        TSuiteTimestamp a = new TSuiteTimestamp('20190401_142748')
        TSuiteTimestamp b = new TSuiteTimestamp('20190401_142150')
        Path pathRelativeToTSuiteTimestampDir = Paths.get('47news.visitSite/47reporters.png')
        then:
        stats.hasImageDelta(pathRelativeToTSuiteTimestampDir, a, b)
    }
    

    def testGetImageDelta() {
        setup:
        MaterialStorage ms = prepareMS("testGetImageDelta")
        //
        TSuiteName examiningTSuiteName = new TSuiteName("47news.chronos_exam")
        TExecutionProfile examiningTExecutionProfile = new TExecutionProfile('default')
        TCaseName  examiningTCaseName  = new TCaseName("Test Cases/47news/ImageDiff")
        Path previousImageDeltaStats = StorageScanner.findLatestImageDeltaStats(ms,
                examiningTSuiteName,
                examiningTExecutionProfile,
                examiningTCaseName)
        Options options = new Options.Builder()
                .previousImageDeltaStats(previousImageDeltaStats)
                .build()
        StorageScanner scanner = new StorageScanner(ms, options)
        //
        ImageDeltaStats stats= scanner.scan(
                new TSuiteName("47news.chronos_capture"),
                new TExecutionProfile('default'))
        //
        Path file = scanner.persist(stats,
                examiningTSuiteName,
                examiningTExecutionProfile,
                new TSuiteTimestamp(),
                examiningTCaseName)
        assert Files.exists(file)

        when:
        TSuiteTimestamp a = new TSuiteTimestamp('20190401_142748')
        TSuiteTimestamp b = new TSuiteTimestamp('20190401_142150')
        Path pathRelativeToTSuiteTimestampDir = Paths.get('47news.visitSite/47reporters.png')
        ImageDelta id1 = stats.getImageDelta(pathRelativeToTSuiteTimestampDir, a, b)
        then:
        id1 != null

        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190401_150000')
        ImageDelta id2 = stats.getImageDelta(pathRelativeToTSuiteTimestampDir, another, b)
        then:
        id2 == null
    }
    
    @Ignore
    def testIgnoreThis() {}
}
