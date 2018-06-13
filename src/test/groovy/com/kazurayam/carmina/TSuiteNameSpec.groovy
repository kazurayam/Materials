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
