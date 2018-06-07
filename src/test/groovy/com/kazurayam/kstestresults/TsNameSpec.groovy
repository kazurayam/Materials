package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

class TsNameSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setup() {
    }
    def cleanup() {}
    def setupSpec() {
        //workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TsNameSpec.class)}")
        //if (!workdir.toFile().exists()) {
        //    workdir.toFile().mkdirs()
        //}
        //Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testGetValueOfSuiteless() {
        expect:
        TsName.SUITELESS.getValue() == TsName.SUITELESS_DIRNAME

    }

    // helper methods
    def void anything() {}
}
