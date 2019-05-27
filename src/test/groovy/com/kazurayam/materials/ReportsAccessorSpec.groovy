package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

class ReportsAccessorSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(ReportsAccessorSpec)
    
    private static Path specOutputDir_
    
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ReportsAccessorSpec.class)}")
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
}
