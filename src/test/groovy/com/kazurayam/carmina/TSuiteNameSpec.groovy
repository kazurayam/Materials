package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.TSuiteName

import spock.lang.Specification

class TSuiteNameSpec extends Specification {

    // fields
    static Logger logger = LoggerFactory.getLogger(TSuiteNameSpec.class)

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setupSpec() {
        //workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TsNameSpec.class)}")
        //if (!workdir.toFile().exists()) {
        //    workdir.toFile().mkdirs()
        //}
        //Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetValueOfSuiteless() {
        expect:
        TSuiteName.SUITELESS.getValue() == TSuiteName.SUITELESS_DIRNAME

    }

    // helper methods
    def void anything() {}
}
