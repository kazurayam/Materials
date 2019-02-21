package com.kazurayam.materials.stats

import java.awt.image.BufferedImage
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.stats.StorageScanner.BufferedImageBuffer
import com.kazurayam.materials.stats.StorageScanner.Options

import spock.lang.Ignore
import spock.lang.Specification

class StorageScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(StorageScannerSpec.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialStorage ms_
    private static StorageScanner storageScanner_
    
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
    def setup() {
        storageScanner_ = new StorageScanner(ms_)
    }
    def cleanup() {}
    def cleanupSpec() {}
    
    def testScan_47News() {
        when:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = storageScanner_.scan(tSuiteName)
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
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats stats = storageScanner_.scan(tSuiteName)
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        TSuiteName tsn = statsEntry.getTSuiteName()
        Path path = Paths.get("main.TC_47News.visitSite/47NEWS_TOP.png")
        List<Material> materials = storageScanner_.getMaterialsOfARelativePathInATSuiteName(tsn, path)
        BufferedImageBuffer biBuffer = new BufferedImageBuffer()
        expect:
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
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        StorageScanner.Options options = new Options.Builder().
                                            maximumNumberOfImageDeltas(3).
                                            build()
        when:
        StorageScanner scanner = new StorageScanner(ms_, options)
        ImageDeltaStats stats = scanner.scan(tSuiteName)
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        then:
        scanner.getOptions().getMaximumNumberOfImageDeltas() == 3
        mstats != null
        mstats.getImageDeltaList().size() == 3
        
    }

    def testSpecifyingOption_onlySince() {
        setup:
        TSuiteName tSuiteName = new TSuiteName("47News_chronos_capture")
        StorageScanner.Options options = new Options.Builder().
                                            onlySince(TSuiteTimestamp.newInstance("20190216_064149"), true).
                                            build()
        when:
        StorageScanner scanner = new StorageScanner(ms_, options)
        ImageDeltaStats stats = scanner.scan(tSuiteName)
        StatsEntry statsEntry = stats.getImageDeltaStatsEntry(tSuiteName)
        MaterialStats mstats = statsEntry.getMaterialStatsList()[0]
        //println "#testSpecifyingOptions_onlySince mstats:${mstats.toJson()}"
        then:
        mstats != null
        mstats.getImageDeltaList().size() == 2
    }

    def testIsInRangeOfTSuiteTimestamp_inclusive_default() {
        setup:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190216_064149")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        when:
        StorageScanner.Options options = 
            new Options.Builder().
                onlySince(tst).  // onlySinceInclude default: true
                build()
        StorageScanner scanner = new StorageScanner(ms_, options)
        TSuiteResult tsr = ms_.getTSuiteResult(tsri)
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
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190216_064149")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        when:
        StorageScanner.Options options =
            new Options.Builder().
                onlySince(tst, false).   // onlySinceInclusive: false
                build()
        StorageScanner scanner = new StorageScanner(ms_, options)
        TSuiteResult tsr = ms_.getTSuiteResult(tsri)
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
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance("20190216_064149")
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tst)
        when:
        StorageScanner.Options options =
            new Options.Builder().
                onlySince(tst, true).  // onlySinceInclusive: true
                build()
        StorageScanner scanner = new StorageScanner(ms_, options)
        TSuiteResult tsr = ms_.getTSuiteResult(tsri)
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
    
    @Ignore
    def testIgnoring() {}
    
    // helper methods
    def void anything() {}
    
}
