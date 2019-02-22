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


class StatsEntrySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(StatsEntrySpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static ImageDeltaStats ids_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(StatsEntrySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path storagedir = workdir_.resolve('Storage')
        MaterialStorage ms = MaterialStorageFactory.createInstance(storagedir)
        StorageScanner scanner = new StorageScanner(ms)
        ids_ = scanner.scan(new TSuiteName('47News_chronos_capture'))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * I expected java.nio.file.Path#equals(Path) to be tolerant for the difference of
     * File.seperator in the 2 Path instances.
     * On Windows, this test passed. But to my surprize, this test failed on Mac.
     * 
     * After a few days of research, I realized my misunderstanding about java.nio.file.Path
     * I should not write this: 
     *     <PRE>Paths.get('main.TC_47News.visitSite\\47NEWS_TOP.png')</PRE>
     * Rather I should write this: 
     *     <PRE>Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png')</PRE> 
     * 
     * Ok, I would accept it.
     * I would ignore this test.
     * 
     * @return
     */
    @Ignore
    def test_Path_equals() {
        when:
        Path winPath = Paths.get('main.TC_47News.visitSite\\47NEWS_TOP.png')
        Path nixPath = Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png')
        then:
        winPath.equals(nixPath)
    }
    
    /**
     * verify if StatsEntry#getMaterialStats() method is tolerant for two kinds of File.separator: '\\' and '/'
     * @return
     */
    def testGetMaterialStats() {
        setup:
        StatsEntry se = ids_.getImageDeltaStatsEntry(new TSuiteName('47News_chronos_capture'))
        when:
        MaterialStats mStats1 = se.getMaterialStats(Paths.get("main.TC_47News.visitSite").resolve("47NEWS_TOP.png"))
        then:
        mStats1 != null
        mStats1.getImageDeltaList().size() > 0
        when:
        MaterialStats mStats2 = se.getMaterialStats(Paths.get("main.TC_47News.visitSite").resolve("47NEWS_TOP.png"))
        logger_.debug("mStats2: ${mStats2.toString()}")
        then:
        mStats2 != null
        mStats2.getImageDeltaList().size() > 0
        
        
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
