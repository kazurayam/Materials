package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

class TSuiteNameSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TSuiteNameSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        //workdir = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TsNameSpec.class)}")
        //if (!workdir.toFile().exists()) {
        //    workdir.toFile().mkdirs()
        //}
        //Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}


    def testGetId() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        when:
        String id = tsn.getId()
        then:
        id == 'Test Suites/main/TS1'
    }

    def testAbbreviatedId() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        when:
        String abbreviatedId = tsn.getAbbreviatedId()
        then:
        abbreviatedId == 'main/TS1'
    }

    def testValue() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        when:
        String value = tsn.getValue()
        then:
        value == 'main.TS1'
    }


    def testChompPrefix() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/TS1')
        when:
        String name = tsn.getValue()
        then:
        name == 'TS1'
    }

    def testFlattenSubdirectory() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        when:
        String name = tsn.getValue()
        then:
        name == 'main.TS1'
    }

    /*
    def testIgnoreWhitespaces() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/foo bar /baz TS1 ')
        when:
        String name = tsn.getValue()
        then:
        name == 'foobar.bazTS1'
    }
     */

    def testNonLatinCharacters() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/main/テスト1')
        when:
        String name = tsn.getValue()
        then:
        name == 'main.テスト1'
    }

    // feature methods
    def testGetValueOfSuiteless() {
        expect:
        TSuiteName.SUITELESS.getValue() == TSuiteName.SUITELESS_DIRNAME

    }

}
