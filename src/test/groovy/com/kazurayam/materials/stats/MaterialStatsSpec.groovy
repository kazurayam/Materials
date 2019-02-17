package com.kazurayam.materials.stats

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers

class MaterialStatsSpec {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStatsSpec.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialStatsSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testSomething() {
    }

}
