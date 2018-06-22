package com.kazurayam.carmina.material

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.carmina.material.TSuiteName

import spock.lang.Specification

class TSuiteNameSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TSuiteNameSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

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

    /**
     * 'Test Suites/TS1' ==> 'TS1'
     */
    def testStripParentDir() {
        setup:
        TSuiteName tsn = new TSuiteName('foo/bar/TS1')
        when:
        String name = tsn.getValue()
        then:
        name == 'TS1'
    }

    def testStripParentDir2() {
        setup:
        TSuiteName tsn = new TSuiteName('foo\\bar\\TS1')
        when:
        String name = tsn.getValue()
        then:
        name == 'TS1'
    }


    /**
     * '§A' ==> '%C2%A7A'
     */
    def testEncoding() {
        setup:
        TSuiteName tsn = new TSuiteName('§A')
        when:
        String name = tsn.getValue()
        then:
        name == '§A'
    }

    // helper methods
    def void anything() {}
}
