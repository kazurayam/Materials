package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.stats.StorageScanner

import groovy.json.JsonOutput
import spock.lang.Specification

class ImageDeltaStatsSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaStatsSpec.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialStorage ms_
    private static ImageDeltaStats ids_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(ImageDeltaStats.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path storage = workdir_.resolve("Storage")
        ms_ = MaterialStorageFactory.createInstance(storage)
        StorageScanner scanner = new StorageScanner(ms_)
        ids_ = scanner.scan(new TSuiteName("47News_chronos_capture"))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def testToString() {
        when:
        String s = ImageDeltaStats.ZERO.toString()
        String pp = JsonOutput.prettyPrint(s)
        logger_.debug(ImageDeltaStats.getClass().getName() + ".ZERO:\n${pp}")
        then:
        s.contains("0.0")
    }
    
}
