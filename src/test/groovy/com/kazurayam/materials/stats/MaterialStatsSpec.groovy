package com.kazurayam.materials.stats

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.stats.StatsEntry
import com.kazurayam.materials.stats.StorageScanner

import spock.lang.Specification

import groovy.json.JsonOutput

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
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialStatsSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        storagedir_ = workdir_.resolve("Storage")
        ms_ = MaterialStorageFactory.createInstance(storagedir_)
        ids_ = StorageScanner.scan(ms_)
    }
    def setup() {
        StatsEntry se = ids_.getImageDeltaStatsEntry(new TSuiteName('47News_chronos_capture'))
        materialStats_ = se.getMaterialStats(Paths.get("main.TC_47News.visitSite\\47NEWS_TOP.png"))
    }
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testData() {
        when:
        double[] data = materialStats_.data()
        then:
        data == [16.86, 4.53, 2.83, 27.85, 16.1, 0.01]    
    }

    def testSum() {
        when:
        double sum = materialStats_.sum()  // "sum": 68.18,
        then:
        sum == 68.18
    }
    
    def testMean() {
        when:
        double mean = materialStats_.mean()  // "mean": 11.363333333333335,
        then:
        11.36 < mean
        mean < 11.37
    }
    
    def testVaiance() {
        when:
        double variance = materialStats_.variance()  // "variance": 2.914330912658107,
        then:
        2.91 < variance
        variance < 2.92
    }
    
    def testStandardDeviation() {
        when:
        double standardDeviation = materialStats_.standardDeviation()   // "standardDeviation": 1.7071411519432442,
        then:
        1.70 < standardDeviation
        standardDeviation < 1.71
    }
    
    def testPROBABILITY() {
        expect:
        0.95 == MaterialStats.PROBABILITY
    }
    
    def testTDistribution() {
        when:
        double tdistribution = materialStats_.tDistribution()   // "tDistribution": 2.0150483736252975,
        then:
        2.0150 < tdistribution
        tdistribution < 2.0151
    }
    
    def testGetCalculatedCriteriaPercentage() {
        when:
        double upperBound = materialStats_.getCalculatedCriteriaPercentage() // "calculatedCriteriaPercentage": 12.77
        then:
        12.00 < upperBound
        upperBound < 13.00
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
        str.contains("calculatedCriteriaPercentage")
    }
    
    

}
