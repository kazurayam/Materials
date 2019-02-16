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

import spock.lang.Ignore
import spock.lang.Specification

class StorageScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(StorageScannerSpec.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialStorage ms_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(StorageScannerSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path msPath = workdir_.resolve("Storage")
        ms_ = MaterialStorageFactory.createInstance(msPath)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testScan_all() {
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = StorageScanner.scan(ms_)
        ImageDeltaStatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        then:
        statsEntry != null
        statsEntry.getTSuiteName().equals(tSuiteName)
        when:
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        then:
        mstats != null
        mstats.getPath().toString()== 'main.TC_47News.visitSite/47NEWS_TOP.png'
    }
    
    def testScan_47News() {
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = StorageScanner.scan(ms_, tSuiteName)
        ImageDeltaStatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        then:
        statsEntry != null
        statsEntry.getTSuiteName().equals(tSuiteName)
        when:
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        then:
        mstats != null
        mstats.getPath().toString()== 'main.TC_47News.visitSite/47NEWS_TOP.png'
    }
    
    def testFeature() {
        setup:
        anything()
        when:
        anything()
        then:
        anything()
        cleanup:
        anything()
    }
    
    @Ignore
    def testIgnoring() {}
    
    // helper methods
    def void anything() {}
    
}
