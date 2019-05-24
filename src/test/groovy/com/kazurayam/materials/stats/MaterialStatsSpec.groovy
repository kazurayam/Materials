package com.kazurayam.materials.stats

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materials.stats.MaterialStats.ConfidenceInterval
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp

import groovy.json.JsonOutput
import spock.lang.IgnoreRest
import spock.lang.Specification

class MaterialStatsSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStatsSpec.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static Path storagedir_
    private static MaterialStorage ms_
    private static ImageDeltaStats ids_
    private static MaterialStats materialStats_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialStatsSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        storagedir_ = workdir_.resolve("Storage")
        ms_ = MaterialStorageFactory.createInstance(storagedir_)
        StorageScanner scanner = new StorageScanner(ms_)
        ids_ = scanner.scan(new TSuiteName('47News_chronos_capture'))
    }
    def setup() {
        StatsEntry se = ids_.getImageDeltaStatsEntry(new TSuiteName('47News_chronos_capture'))
        materialStats_ = se.getMaterialStats(Paths.get("main.TC_47News.visitSite/47NEWS_TOP.png"))
    }
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testData() {
        when:
        double[] data = materialStats_.data()
        then:
        data == [16.86, 4.53, 2.83, 27.85, 16.1]    
    }

    def testSum() {
        when:
        double sum = materialStats_.sum()  // "sum": 68.17,
        then:
        sum == 68.17
    }
    
    def testMean() {
        when:
        double mean = materialStats_.mean()  // "mean": 13.634,
        then:
        13.0 < mean
        mean < 14.0
    }
    
    def testVaiance() {
        when:
        double variance = materialStats_.variance()  // "variance": 2.6882191428856
        then:
        2.680 < variance
        variance < 2.690
    }
    
    def testStandardDeviation() {
        when:
        double standardDeviation = materialStats_.standardDeviation()   // "standardDeviation": 1.6395789529283424
        then:
        1.63 < standardDeviation
        standardDeviation < 1.64
    }
    
    def testPROBABILITY() {
        expect:
        0.95 == MaterialStats.DEFAULT_PROBABILITY
    }
    
    def testTDistribution() {
        when:
        double tdistribution = materialStats_.tDistribution()   // "tDistribution": 2.1318467859510317
        then:
        2.130 < tdistribution
        tdistribution < 2.140
    }
    
    def testConfidenceInteval() {
        when:
        ConfidenceInterval confidenceInterval = materialStats_.getConfidenceInterval()
        then:
        12.0 < confidenceInterval.getLowerBound()              // "lowerBound": 12.070840401864046
        confidenceInterval.getLowerBound() < 13.0 
        15.0 < confidenceInterval.getUpperBound()              // "upperBound": 15.197159598135954
        confidenceInterval.getUpperBound() < 16.0 
    }
    
    def testGetCriteriaPercentage() {
        when:
        double criteria = materialStats_.getCriteriaPercentage() // "criteriaPercentage": 15.20
        then:
        15.00 < criteria
        criteria < 16.00
    }
    
    def testGetCriteriaPercengate_shouldBeRounded() {
        when:
        double criteria = materialStats_.getCriteriaPercentage() // "criteriaPercentage": 15.20
        then:
        15.20 == criteria
    }
    
    def testHasMaterialStats() {
        when:
        TSuiteTimestamp a = new TSuiteTimestamp('20190216_204329')
        TSuiteTimestamp b = new TSuiteTimestamp('20190216_064354')
        then:
        materialStats_.hasImageDelta(a, b)
        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190301_065500')
        then:
        ! materialStats_.hasImageDelta(another, b)
    }
    
    def testGetMaterialStats() {
        when:
        TSuiteTimestamp a = new TSuiteTimestamp('20190216_204329')
        TSuiteTimestamp b = new TSuiteTimestamp('20190216_064354')
        ImageDelta ms1 = materialStats_.getImageDelta(a, b)
        then:
        ms1 != null
        when:
        TSuiteTimestamp another = new TSuiteTimestamp('20190301_065500')
        ImageDelta ms2 = materialStats_.getImageDelta(another, b)
        then:
        ms2 == null
    }
    
    
    def testToString() {
        when:
        String str = ids_.toString()
        then:
        str != null
        when:
        println "#testToString str:\n" + JsonOutput.prettyPrint(str)
        //println "#testToString str:\n" + str
        then:
        str.contains("criteriaPercentage")
    }
    
    /**
     * Test if a MaterialStats object works fine in a marginal condition where we have:
     *     <PRE>build/tmp/testOutput/MaterialStatsSpec/testMarginalCondition/Storage/</PRE>
     * but we do not have
     *     <PRE>build/tmp/testOutput/MaterialStatsSpec/testMarginalCondition/Storage/47News_chronos_capture</PRE>
     */
    def testMarginalCondition0_noTSuiteName() {   
    }
    
    def testMarginalCondition0_noTSuiteTimestamp() {
    }
    
    def testMarginalCondition0_noMaterials() {
    }
    
    /**
     * Test if a MaterialStats object works fine in a marginal condition where we have:
     *     <PRE>build/tmp/testOutput/MaterialStatsSpec/testMarginalCondition1/Storage/47News_chronos_capture/20190216_204329/main.TC_47News.visitSite/47NEWS_TOP.png</PRE>
     * but we have no other Materials.
     * 
     */
    def testMarginalCondition1() {
        setup:
        Path caseOutputDir = workdir_.resolve('testMarginalCondition1')
        Path storageDir = caseOutputDir.resolve('Storage')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(reports)
        Path fixtureSourceDir = fixture_.resolve('Storage').resolve('47News_chronos_capture').resolve('20190216_204329')
        Path fixtureTargetDir = storageDir.resolve('47News_chronos_capture').resolve('20190216_204329')
        Files.createDirectories(fixtureTargetDir)
        Helpers.copyDirectory(fixtureSourceDir, fixtureTargetDir)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storageDir)
        StorageScanner scanner = new StorageScanner(ms)
        ImageDeltaStats ids = scanner.scan(new TSuiteName('47News_chronos_capture'))
        StatsEntry se = ids.getImageDeltaStatsEntry(new TSuiteName('47News_chronos_capture'))
        MaterialStats materialStats = se.getMaterialStats(Paths.get("main.TC_47News.visitSite/47NEWS_TOP.png"))
        when:
        double[] data = materialStats.data()
        then:
        data.length == 0
        when:
        int degree = materialStats.degree()
        then:
        degree == 0
        when:
        double sum = materialStats.sum()
        then:
        sum == 0.0
        when:
        double mean = materialStats.mean()
        then:
        mean == 0.0
        when:
        double variance = materialStats.variance()
        then:
        variance == 0.0
        when:
        double standardDeviation = materialStats.standardDeviation()
        then:
        standardDeviation == 0.0
        when:
        double tDistribution = materialStats.tDistribution()
        then:
        tDistribution == 0.0
        when:
        ConfidenceInterval interval = materialStats.getConfidenceInterval()
        then:
        interval == new ConfidenceInterval(0.00, 0.00, 0.00)
        when:
        double criteriaPercentage = materialStats.getCriteriaPercentage()
        then:
        criteriaPercentage == 0.0
    }

}
